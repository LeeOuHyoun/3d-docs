---
title: 보조 함수 모음 (DataGridViewUtils)
---

# 보조 함수 모음 (DataGridViewUtils) <Badge type="tip" text="로컬 실행 확인" />

DataGridView 화면을 만들 때 되풀이되는 처리(행 넣기와 바꾸기, 키로 찾기, 선택, 펼칠 때 불러오기, 변경 모으기, CSV 내려받기)를 함수 39개로 묶은 보조 모듈입니다.

- ENOVIA 원본에 들어 있는 모듈이 **아닙니다.** 이 저장소의 `Recipes/DataGridViewUtils.js` 파일 하나를 프로젝트 webapps 에 넣어서 씁니다.
- 안에서는 원본의 [그리드 메서드](/datagridview/api/grid-methods) 와 [TreeDocument, TreeNodeModel](/datagridview/api/data) 함수만 부릅니다. 원본 함수를 직접 쓰는 방법은 각 가이드에 있고, 이 페이지는 그것을 한 줄로 줄여 쓰는 방법입니다.

소스: [DataGridViewUtils.js](https://github.com/LeeOuHyoun/3d-docs/blob/main/Recipes/DataGridViewUtils.js) ([전체 소스](#전체-소스))

::: info 근거
Recipes/DataGridViewUtils.js 와 그 안에서 부르는 원본 함수. Controls.js 의 `DS/Controls/Abstract` `_preBuildView` (그리드 DOM 에 넣는 `dsModel`, `wux-controls-abstract` 클래스), `DS/Controls/ModalLoader`, `DS/Controls/Loader` 의 on / off. CollectionView.js 의 selectNodeModel, ensureNodeModelVisible, addEventListener, getActiveCellID, displayModalLoader. DataGridView.js 의 getNodeModelAt, getAsCSV, setNodeModelValue, DataGridViewLayout.setColumnVisibleFlag, `sortModel`. TreeModel.js 의 TreeDocument (withTransactionUpdate, empty, onPreExpand, setFilterModel, setUseChangeTransactionMode, acceptChanges) 와 TreeNodeModel (생성자, addChild, `_addChildNode`, updateOptions, setAttribute, remove, show / hide, getChangeStates, getNodeSource). Selection.js 의 XSO.onChange. 동작은 2025, 2026 원본 webapps 를 내 PC 에서 띄워 함수 39개를 모두 실행해 확인했습니다 ([확인 상태](#확인-상태)).
:::

## 불러오기

`DataGridViewUtils.js` 를 `<3DSpace>/webapps/Recipes/` 에 넣으면 모듈 이름이 `DS/Recipes/DataGridViewUtils` 가 됩니다 ([모듈 이름과 파일](/amd/#모듈-이름과-파일)). 화면 모듈의 의존성에 넣어 받습니다.

```js
define('DS/MyApp/PartListView', ['DS/Recipes/DataGridViewUtils'], function (GridUtils) {
  'use strict';

  GridUtils.loadRows('partGrid', fetchParts);
});
```

| 이 모듈이 불러오는 모듈 | 비고 |
| --- | --- |
| `DS/lodash-4.18.1/lodash` | lodash. **원본 webapps 에는 없습니다.** 프로젝트에 넣어 둔 lodash 의 모듈 이름으로 바꿉니다 |
| `DS/DataGridView/DataGridView`, `DS/TreeModel/TreeDocument`, `DS/TreeModel/TreeNodeModel` | 원본 모듈 |

- 다른 폴더에 두면 파일 첫 줄 `define('DS/Recipes/DataGridViewUtils', ...)` 의 이름도 폴더에 맞게 바꿉니다. 이름이 맞지 않으면 오류 없이 `undefined` 가 들어옵니다 ([define / require 쓰는 법](/amd/#define-require-쓰는-법)).
- 파일을 따로 두면 처음 한 번 `Recipes/Recipes.js` 를 찾는 404 가 콘솔에 남습니다. 동작에는 문제가 없습니다 ([로더가 묶음 파일을 찾는 방법](/amd/#로더가-묶음-파일을-찾는-방법)).

## 먼저 알아 둘 것

### 첫 인자 target

모든 함수는 첫 인자로 "어느 그리드인지" 를 받습니다. 아래 중 편한 것을 넘깁니다.

| 넘기는 값 | 예 |
| --- | --- |
| 그리드 인스턴스 | `GridUtils.getRows(grid)` |
| 그리드를 넣은 DOM 요소의 id | `GridUtils.getRows('partGrid')` |
| DOM 요소 (그리드를 넣은 요소, 그리드 안의 셀이나 버튼) | `GridUtils.getRows(event.target)` |
| TreeDocument | `GridUtils.getRows(treeDocument)` |

- id 와 DOM 요소는 `grid.inject(document.getElementById('partGrid'))` 처럼 그리드를 넣은 요소를 말합니다. 그 요소와 상위 요소에서 먼저 찾고, 없으면 그 안에서 찾습니다. 요소 안에 그리드가 둘이면 앞의 것을 씁니다.
- 그리드를 못 찾으면 `DataGridView not found: partGrid` 같은 오류를 던집니다.
- TreeDocument 는 데이터만 다루는 함수만 받습니다. 그리드 화면이 있어야 하는 함수(선택, 행으로 이동, 활성 셀, 정렬, 컬럼, 이벤트, 로딩 표시, CSV)는 받지 않습니다. [함수 목록](#함수-목록) 의 target 칸이 `Grid` 인 함수입니다.

### 행 데이터 (Row)

행을 넣고 읽을 때 쓰는 평범한 객체입니다. `new TreeNodeModel({ label: ..., grid: { ... } })` 에서 `grid` 안의 값을 밖으로 꺼내 놓은 모양입니다.

```js
const row = {
  label: 'PRT-0001',
  id: 'a1b2',
  title: '브라켓',
  status: '작업중',
  qty: 2,
  icons: [{ iconName: 'doc', fontIconFamily: 1 }],
  children: []
};
```

| 키 | 뜻 |
| --- | --- |
| `label` | 트리 컬럼(`dataIndex: 'tree'`) 값 |
| `children` | 하위 행 데이터 배열. 빈 배열 `[]` 은 "하위 행이 있지만 아직 안 불러옴" 이라 펼침 아이콘이 생김. 없으면 끝 행 |
| `icons`, `badges`, `data` | [TreeNodeModel 옵션](/datagridview/api/data) 그대로 |
| 그 밖의 키 | 컬럼 값 (`dataIndex: 값`). 컬럼이 없는 키(`id` 같은 서버 ID)도 넣어 둘 수 있음 |

읽는 함수(`getRow`, `getRows`, `getSelectedRows`)는 `{ label, 컬럼 값 ... }` 만 돌려줍니다. `children`, `icons`, `badges`, `data` 는 넣지 않습니다.

### 행 조건

행을 고르는 함수는 조건을 세 가지 형태로 받습니다.

| 형태 | 예 | 고르는 행 |
| --- | --- | --- |
| 함수 | `function (row, node) { return row.qty > 10; }` | 행 데이터와 TreeNodeModel 을 받아 참을 돌려준 행 |
| 객체 | `{ status: '완료' }` | 적은 값이 모두 같은 행 |
| 문자열 | `'checked'` | 그 컬럼 값이 참(truthy)인 행 |

- 조건을 받는 함수: `getNodes`, `getRows`, `findNode`, `removeNodes`, `selectNodes`, `revealNode`, `showRowsWhere`
- `removeNodes`, `selectNodes`, `revealNode` 는 조건 대신 TreeNodeModel 하나나 배열도 받습니다. `null`, `undefined` 를 넘기면 아무 행도 고르지 않습니다. `getNodeById` 가 못 찾아 돌려준 `undefined` 를 그대로 넘겨도 전체가 지워지거나 선택되지 않습니다.

### 키 컬럼

`getNodeById`, `selectById`, `removeById`, `updateRows`, `upsertRows`, `keyNodesBy` 는 행을 키 컬럼 값으로 찾습니다.

- 키 컬럼은 인자 `key` 로 정하고, 기본은 `'id'` 입니다. 트리 컬럼 값(품번)으로 찾으려면 `'tree'` 를 줍니다.
- 키 컬럼 값이 없는(`null`, `undefined`) 행은 찾는 대상이 아닙니다. 찾을 값으로 `null`, `undefined` 를 넘기면 아무 행도 찾지 않습니다.
- 같은 키 값을 가진 행이 여럿일 수 있습니다 (같은 부품이 트리 여러 곳에 나오는 BOM). `selectById`, `removeById`, `updateRows` 는 그 행을 모두 다루고, `getNodeById` 는 앞의 행 하나, `keyNodesBy` 는 뒤의 행 하나만 돌려줍니다.

## 하고 싶은 일로 찾기

| 하고 싶은 일 | 함수 |
| --- | --- |
| 서버에서 받은 목록으로 그리드 채우기 (로딩 표시 포함) | [`loadRows`](#데이터를-불러와-전체-교체) |
| 가지고 있는 목록으로 전체 교체, 모두 지우기 | [`setRows`, `clearRows`](#데이터를-불러와-전체-교체) |
| 저장 결과를 행에 반영 | [`updateRows`, `upsertRows`, `updateNode`](#서버-저장-결과-반영) |
| 행 추가, 삭제 | [`addRows`, `removeById`, `removeSelected`, `removeNodes`](#행-추가-삭제) |
| id 나 조건으로 행 찾기 | [`getNodeById`, `findNode`, `getNodes`](#찾기-읽기) |
| 행 값을 평범한 객체로 읽기 | [`getRows`, `getSelectedRows`, `getRow`](#찾기-읽기) |
| 코드로 행 선택, 선택이 바뀔 때 알기 | [`selectById`, `selectNodes`, `onSelectionChange`](#선택-행으로-이동) |
| 특정 행으로 스크롤해서 보여 주기 | [`revealNode`](#선택-행으로-이동) |
| 펼칠 때 하위 행 불러오기 | [`setChildrenLoader`](#펼칠-때-하위-행-불러오기) |
| 검색어에 맞는 행만 보이기 | [`showRowsWhere`, `showAllRows`](#검색-필터-정렬-컬럼) |
| 헤더 필터, 정렬, 컬럼 숨기기 | [`setFilters`, `sortBy`, `setColumnsVisible`](#검색-필터-정렬-컬럼) |
| 편집을 모았다가 한 번에 저장 | [`getChanges`](#편집을-모아서-한-번에-저장) |
| 그리드 이벤트 등록과 해제 | [`on`](#이벤트) |
| 오래 걸리는 작업 동안 로딩 표시 | [`withLoader`](#로딩-표시-csv-내려받기) |
| CSV 파일로 내려받기 | [`downloadCSV`](#로딩-표시-csv-내려받기) |

## 상황별 사용법

예제의 `'partGrid'` 는 그리드를 넣은 요소의 id 입니다. `fetchParts()`, `saveToServer()`, `saveAll()`, `fetchChildren()`, `runLongTask()` 는 이미 갖고 있는 서버 호출 함수이고 Promise 를 돌려준다고 가정합니다.

### 데이터를 불러와 전체 교체

```js
GridUtils.loadRows('partGrid', function () { return fetchParts(); }, '불러오는 중...');
GridUtils.setRows('partGrid', rows);
GridUtils.clearRows('partGrid');
```

| 함수 | 하는 일 | 돌려받는 값 |
| --- | --- | --- |
| `loadRows(target, task, text?)` | 로딩 표시를 덮고 `task` 로 행 데이터를 받아 전체 교체. `task` 는 함수 또는 Promise. `text` 는 로딩 문구 | Promise. 값은 새로 넣은 최상위 행(TreeNodeModel) 배열 |
| `setRows(target, rows)` | 모든 행을 지우고 `rows` 로 채움 | 새로 넣은 최상위 행 배열 |
| `clearRows(target)` | 모든 행을 지움 | 없음 |

- `task` 가 실패하면 기존 행은 그대로 두고 로딩 표시만 지웁니다. 실패는 돌려받은 Promise 로 전달되므로 `.catch` 에서 알림을 띄웁니다.
- 행 데이터가 `null`, `undefined` 면 빈 그리드가 됩니다.
- `setRows` 는 [변경 트랜잭션 모드](#편집을-모아서-한-번에-저장) 가 켜져 있어도 변경 기록 없이 새 데이터로 시작합니다. 저장 안 된 변경은 버려집니다.

### 서버 저장 결과 반영

```js
saveToServer(payload).then(function (results) {
  const notFound = GridUtils.updateRows('partGrid', results);
});
```

`results` 가 아래와 같으면 `id` 가 `'a1b2'` 인 행의 `title`, `modified` 만 바뀌고, `notFound` 에는 그리드에 없는 두 번째 행 데이터가 들어옵니다.

```js
const results = [
  { id: 'a1b2', title: '브라켓 A', modified: '2026-11-26' },
  { id: 'zzzz', title: '그리드에 없는 행' }
];
```

| 함수 | 하는 일 | 돌려받는 값 |
| --- | --- | --- |
| `updateRows(target, rows, key?)` | 키 컬럼 값이 같은 행을 모두 찾아, 넘긴 값만 바꿈 | 그리드에서 못 찾은 행 데이터 배열 |
| `upsertRows(target, rows, key?)` | 위와 같고, 못 찾은 행 데이터는 최상위에 추가 | 추가된 행 배열 |
| `updateNode(node, values)` | 행 하나의 값을 바꿈. `label` 을 넣으면 트리 컬럼 값이 바뀜 | 그 행 |
| `batch(target, fn)` | `fn(treeDocument)` 안의 변경을 묶어 화면을 한 번만 다시 그림 | `fn` 의 반환값 |

```js
GridUtils.upsertRows('partGrid', results);
GridUtils.updateRows('partGrid', [{ label: 'PRT-0005', status: '폐기' }], 'tree');
GridUtils.updateNode(node, { title: '새 품명', label: 'PRT-0001-A' });

GridUtils.batch('partGrid', function (treeDocument) {
  GridUtils.getNodes(treeDocument, { status: '작업중' }).forEach(function (workingNode) {
    GridUtils.updateNode(workingNode, { status: '완료' });
  });
});
```

- 넘긴 행 데이터에 없는 키는 건드리지 않습니다.
- 두 번째 줄처럼 `key` 에 `'tree'` 를 주면 `id` 대신 트리 컬럼 값(`label`)으로 행을 찾습니다.
- `updateRows`, `upsertRows`, `addRows`, `setRows`, `removeNodes`, `showRowsWhere` 는 안에서 이미 묶여 있습니다. `batch` 는 위처럼 `updateNode` 를 여러 번 부를 때 씁니다.

### 행 추가, 삭제

```js
GridUtils.addRows('partGrid', { label: 'NEW-1', id: 'tmp1', status: '작업중' });
GridUtils.addRows('partGrid', childRows, parentNode);
GridUtils.removeSelected('partGrid');
GridUtils.removeById('partGrid', ['a1b2', 'c3d4']);
GridUtils.removeNodes('partGrid', { status: '폐기' });
```

| 함수 | 하는 일 | 돌려받는 값 |
| --- | --- | --- |
| `addRows(target, rows, parent?)` | 행 데이터 하나나 배열을 최상위 또는 `parent` 행 아래에 추가. `children` 이 있으면 하위 행도 같이 추가 | 추가된 행 배열 (하위 행은 넣지 않음) |
| `removeSelected(target)` | 선택된 행 삭제 | 지운 행 배열 |
| `removeById(target, ids, key?)` | 키 값(하나 또는 배열)으로 삭제 | 지운 행 배열 |
| `removeNodes(target, selector)` | 행, 행 배열, 조건으로 삭제 | 지운 행 배열 |

- 부모 행을 지우면 하위 행도 같이 없어집니다.
- 변경 트랜잭션 모드에서는 바로 지워지지 않고 삭제 표시만 됩니다 ([편집을 모아서 한 번에 저장](#편집을-모아서-한-번에-저장)).

### 찾기, 읽기

```js
const node = GridUtils.getNodeById('partGrid', 'a1b2');
const first = GridUtils.findNode('partGrid', function (row) { return row.label.startsWith('ASM'); });
const doneRows = GridUtils.getRows('partGrid', { status: '완료' });
const selected = GridUtils.getSelectedRows('partGrid');
const active = GridUtils.getActiveNode('partGrid');
const nodeById = GridUtils.keyNodesBy('partGrid');
const qtyList = GridUtils.getColumnValues('partGrid', 'qty');
```

`getRows`, `getSelectedRows` 는 행마다 아래 모양의 객체를 돌려줍니다.

```js
[
  { label: 'PRT-0001', id: 'a1b2', title: '브라켓', status: '완료', qty: 2 },
  { label: 'PRT-0005', id: 'e5f6', title: '베어링', status: '완료', qty: 20 }
]
```

| 함수 | 돌려받는 값 |
| --- | --- |
| `getNodes(target, predicate?)` / `getRows(target, predicate?)` | 조건에 맞는 행 배열 / 그 행 데이터 배열. 조건을 빼면 전체 |
| `findNode(target, predicate)` | 조건에 맞는 첫 행. 없으면 `undefined` |
| `getNodeById(target, id, key?)` | 키 값이 같은 첫 행. 없으면 `undefined` |
| `keyNodesBy(target, key?)` | `{ 키 값: 행 }` 사전. 행을 여러 번 찾을 때 한 번 만들어 두고 씀 |
| `getSelectedNodes(target)` / `getSelectedRows(target)` | 선택된 행 배열 / 그 행 데이터 배열 |
| `getActiveNode(target)` | 활성 셀(포커스)이 있는 행. 활성 셀이 없으면 `undefined` |
| `getColumnValues(target, dataIndex)` | 한 컬럼의 값 배열 (행 순서대로) |
| `getRow(node)` / `getValue(node, dataIndex)` | 행 하나의 데이터 / 셀 값 하나 (`'tree'` 는 트리 컬럼 값) |

- 하위 행, 접힌 행, `showRowsWhere` 로 숨긴 행까지 모두 포함합니다. 순서는 트리를 위에서 아래로 읽은 순서입니다.
- [행 그룹핑](#행-그룹핑과-같이-쓸-때) 이 만든 그룹 행은 넣지 않습니다.
- `getSelectedNodes` 는 새 배열을 돌려주므로, 배열을 돌면서 선택을 바꾸거나 행을 지워도 됩니다.

### 선택, 행으로 이동

```js
GridUtils.selectById('partGrid', 'a1b2');
GridUtils.selectById('partGrid', ['a1b2', 'c3d4'], 'id', true);
GridUtils.selectNodes('partGrid', { status: '작업중' });
GridUtils.selectNodes('partGrid', []);
GridUtils.revealNode('partGrid', { id: 'a1b2' });

const off = GridUtils.onSelectionChange('partGrid', function (nodes) {
  updateDetailPanel(nodes.map(GridUtils.getRow));
});
off();
```

| 함수 | 하는 일 | 돌려받는 값 |
| --- | --- | --- |
| `selectById(target, ids, key?, add?)` | 키 값(하나 또는 배열)으로 선택. `add` 가 `true` 면 기존 선택 유지 | 선택한 행 배열 |
| `selectNodes(target, selector, add?)` | 행, 행 배열, 조건으로 선택. `[]` 를 넘기면 선택 해제 | 선택한 행 배열 |
| `revealNode(target, selector, select?)` | 접힌 부모를 펼치고, 행이 화면 가운데 오게 스크롤한 뒤 깜빡여 표시하고 선택. `select` 가 `false` 면 선택은 안 함 | 그 행. 없으면 `undefined` |
| `onSelectionChange(target, fn)` | 선택이 바뀌면 `fn(선택된 행 배열)` 을 부름 | 해제 함수 |

- 선택은 그리드 옵션 `rowSelection` 이 `'single'` 또는 `'multiple'` 일 때만 됩니다. `'single'` 에서 여러 행을 넘기면 마지막 행만 선택되고, `add` 는 `'multiple'` 에서만 뜻이 있습니다.
- 접혀 있어 보이지 않는 하위 행도 선택됩니다.
- `onSelectionChange` 의 콜백은 한 번의 조작에 한 번만, 선택이 다 바뀐 뒤에 불립니다. 원본 `getXSO().onChange` 는 행 3개를 한꺼번에 선택하면 여러 번 불립니다.

### 펼칠 때 하위 행 불러오기

```js
const treeDocument = new TreeDocument({ useAsyncPreExpand: true });
GridUtils.setRows(treeDocument, [{ label: 'ASM-0001', id: 'x1', children: [] }]);
GridUtils.setChildrenLoader(treeDocument, function (node, row) {
  return fetchChildren(row.id);
});
```

- `useAsyncPreExpand: true`, 불러올 행의 `children: []`, `setChildrenLoader` 세 가지를 같이 씁니다. 원본 함수로 직접 쓰는 방법은 [트리 구조](/datagridview/tree#펼칠-때-하위-행-불러오기) 에 있습니다.
- loader 는 펼치는 행(TreeNodeModel)과 그 행 데이터를 받아 하위 행 데이터 배열이나 그 Promise 를 돌려줍니다. 돌려준 하위 행 데이터에 `children: []` 를 넣으면 그 행도 펼칠 때 불러옵니다.
- 하위 행이 이미 있는 행은 다시 불러오지 않습니다.
- 불러오기가 실패해도 행이 "펼치는 중" 에서 멈추지 않습니다. 하위 행 없이 펼쳐진 상태가 되고, 접었다가 다시 펼치면 loader 를 다시 부릅니다. 실패 알림은 loader 안의 `.catch` 에서 띄웁니다.
- `onPreExpand` 는 하나만 등록되므로 `setChildrenLoader` 를 다시 부르면 이전 loader 를 대체합니다.

### 검색, 필터, 정렬, 컬럼

```js
GridUtils.showRowsWhere('partGrid', function (row) { return row.label.includes(keyword); });
GridUtils.showAllRows('partGrid');
GridUtils.setFilters('partGrid', { tree: 'stringRegexp', status: 'set', qty: 'number' });
GridUtils.sortBy('partGrid', 'dueDate', 'desc');
GridUtils.sortBy('partGrid', [{ dataIndex: 'status', sort: 'asc' }, { dataIndex: 'qty', sort: 'desc' }]);
GridUtils.sortBy('partGrid');
GridUtils.setColumnsVisible('partGrid', ['owner', 'modified'], false);
```

| 함수 | 하는 일 |
| --- | --- |
| `showRowsWhere(target, predicate)` / `showAllRows(target)` | 조건에 맞는 행만 보이기 / 숨긴 행을 모두 다시 보이기 |
| `setFilters(target, filters)` | 헤더 필터를 쓸 컬럼 정하기. `{ dataIndex: filterId }` ([필터 종류](/datagridview/api/filter)) |
| `sortBy(target, spec?, sort?)` | 정렬. `spec` 은 dataIndex 하나 또는 `sortModel` 배열. `sort` 는 `'asc'`(기본) 또는 `'desc'` |
| `setColumnsVisible(target, dataIndexes, visible?)` | 컬럼 보이기 / 숨기기. `visible` 을 빼면 보이기 |

- `showRowsWhere` 는 헤더 필터와 별개로 행을 숨깁니다. 하위 행이 조건에 맞으면 부모 행도 보이지만, 접힌 부모를 펼치지는 않습니다. 숨겨진 행은 선택이 풀립니다.
- `setFilters` 의 키 `tree` 는 TreeDocument 규칙대로 `label` 로 바꿔 넣습니다. 부를 때마다 전체를 바꾸므로, 뺀 컬럼의 필터는 없어집니다.
- `sortBy('partGrid')` 처럼 조건을 빼면 정렬을 풉니다. 이때 행 순서는 마지막으로 정렬된 그대로 남습니다 ([정렬은 행 순서 자체를 바꿉니다](/datagridview/sort-filter#실행-중-정렬-바꾸기)).
- `alwaysVisibleFlag: true` 인 컬럼은 숨겨지지 않습니다.

### 편집을 모아서 한 번에 저장

"저장" 버튼을 누를 때까지 편집을 모아 두는 화면에 씁니다. TreeDocument 의 변경 트랜잭션 모드를 켜고 ([저장 후 화면 반영](/datagridview/save-and-reflect#편집을-모아서-한-번에-저장-변경-트랜잭션-모드)), `getChanges` 로 바뀐 행을 종류별로 받습니다.

```js
const doc = GridUtils.getDoc('partGrid');
doc.setUseChangeTransactionMode(true);

function handleSave() {
  const changes = GridUtils.getChanges(doc);
  saveAll({
    create: changes.added.map(GridUtils.getRow),
    update: changes.modified.map(GridUtils.getRow),
    remove: changes.removed.map(GridUtils.getRow)
  }).then(function (result) {
    changes.added.forEach(function (addedNode, index) {
      GridUtils.updateNode(addedNode, result.created[index]);
    });
    GridUtils.updateRows(doc, result.updated);
    doc.acceptChanges();
  }).catch(function () {
    doc.cancelChanges();
  });
}
```

`getChanges(target)` 가 돌려주는 값:

| 키 | 들어 있는 행 |
| --- | --- |
| `added` | 새로 추가한 행. `children` 으로 같이 넣은 하위 행도 들어 있고, 부모가 하위 행보다 앞에 옴 |
| `modified` | 값이 바뀐 기존 행 |
| `removed` | 삭제 표시된 기존 행. `acceptChanges()` 를 부를 때 실제로 지워짐 |

- 추가했다가 지운 행은 어디에도 넣지 않습니다. 추가한 뒤 값을 고친 행은 `added` 에만 들어갑니다.
- 모드는 처음 데이터를 넣은 **뒤에** 켭니다. `setRows`, `loadRows` 는 모드가 켜져 있어도 변경으로 기록하지 않으므로, 다시 불러올 때는 순서를 신경 쓰지 않아도 됩니다.
- 서버 응답을 먼저 반영(`updateNode`, `updateRows`)한 뒤에 `acceptChanges()` 를 부릅니다. 순서가 바뀌면 응답 반영분이 다시 "변경됨" 으로 남습니다.
- 새로 만든 행은 서버가 id 를 새로 주므로 키로 찾을 수 없습니다. 위 예제처럼 보낸 순서대로 `updateNode` 로 반영합니다.
- `cancelChanges()` 는 편집을 모두 버리고 원래 값으로 되돌립니다. 저장에 실패해도 편집을 남겨 두려면 부르지 않습니다.

::: warning 행에 없던 키는 변경으로 기록되지 않습니다
행 데이터에 `remark` 키가 없는 행의 비고 칸을 사용자가 편집하면, 값은 들어가지만 `modified` 에 잡히지 않고 `cancelChanges()` 로도 되돌려지지 않습니다 (원본 TreeNodeModel 의 `setAttribute`, `updateOptions` 가 이미 있는 키만 기록함). 편집할 컬럼의 키는 값이 없어도 `''` 로 채워서 넣습니다.
:::

### 이벤트

```js
const off = GridUtils.on('partGrid', 'click', function (event, cellInfos) {
  if (cellInfos && cellInfos.nodeModel) {
    console.log(GridUtils.getRow(cellInfos.nodeModel));
  }
});
off();
```

- `on(target, name, fn)` 은 `grid.addEventListener(name, fn)` 과 같고, 해제 함수를 돌려줍니다. 원본은 등록한 함수가 아니라 `addEventListener` 의 반환값으로 해제해야 하는데 ([등록 / 해제](/datagridview/api/events#등록-해제)), 그 처리가 들어 있습니다.
- 이벤트 이름과 `cellInfos` 는 [이벤트](/datagridview/api/events) 에 있습니다. 셀 밖에서 일어난 이벤트는 `cellInfos` 없이 불립니다.

### 로딩 표시, CSV 내려받기

```js
GridUtils.withLoader('partGrid', function () { return runLongTask(); }, '처리 중...');
const downloaded = GridUtils.downloadCSV('partGrid', 'parts.csv', { onlySelectedRows: true });
```

| 함수 | 하는 일 | 돌려받는 값 |
| --- | --- | --- |
| `withLoader(target, task, text?)` | `task` (함수 또는 Promise)가 끝날 때까지 그리드에 로딩 표시를 덮고, 성공하든 실패하든 지움 | Promise. `task` 의 결과나 실패를 그대로 전달 |
| `downloadCSV(target, fileName?, options?)` | 그리드 내용을 CSV 파일로 내려받음. `fileName` 기본값은 `'export.csv'`. `options` 는 [getAsCSV 옵션](/datagridview/api/grid-methods#툴바-그룹-내보내기) | 내려받았으면 `true` |

- `withLoader` 는 겹쳐 불러도 됩니다. 마지막 작업이 끝날 때 로딩 표시가 없어집니다.
- `onlySelectedRows: true` 인데 선택한 행이 없으면 내려받지 않고 `false` 를 돌려줍니다.
- CSV 에는 **화면에 펼쳐져 보이는 행** 만 나갑니다. 접힌 하위 행은 빠지고, 선택된 채 접혀 있는 행은 빈 줄로 나갑니다. 모두 내보내려면 먼저 `treeDocument.expandAll()` 로 펼칩니다.
- 파일은 BOM 이 붙은 UTF-8 이라 엑셀에서 한글이 깨지지 않습니다.

## 함수 목록

target 칸의 `Grid` 는 그리드, DOM 요소, DOM id 만 받습니다. `Doc` 은 TreeDocument 도 받습니다. `-` 는 target 을 받지 않습니다.

| 함수 | target | 돌려받는 값 | 하는 일 |
| --- | --- | --- | --- |
| `getGrid(target)` | Grid | DataGridView | 그리드 인스턴스. 못 찾으면 오류 |
| `getDoc(target)` | Doc | TreeDocument | 그리드의 데이터 |
| `batch(target, fn)` | Doc | `fn` 의 반환값 | 변경을 묶어 한 번만 다시 그림 |
| `getValue(node, dataIndex)` | - | 값 | 셀 값. `'tree'` 는 트리 컬럼 값 |
| `getRow(node)` | - | 행 데이터 | 행을 평범한 객체로 |
| `getNodes(target, predicate?)` | Doc | 행 배열 | 전체 또는 조건에 맞는 행 |
| `getRows(target, predicate?)` | Doc | 행 데이터 배열 | 위의 행 데이터 버전 |
| `findNode(target, predicate)` | Doc | 행 또는 `undefined` | 조건에 맞는 첫 행 |
| `getNodeById(target, id, key = 'id')` | Doc | 행 또는 `undefined` | 키 값이 같은 첫 행 |
| `keyNodesBy(target, key = 'id')` | Doc | `{ 키 값: 행 }` | 키 값으로 행을 찾는 사전 |
| `getSelectedNodes(target)` | Doc | 행 배열 | 선택된 행 (새 배열) |
| `getSelectedRows(target)` | Doc | 행 데이터 배열 | 선택된 행의 데이터 |
| `getActiveNode(target)` | Grid | 행 또는 `undefined` | 활성 셀이 있는 행 |
| `getColumnValues(target, dataIndex)` | Doc | 값 배열 | 한 컬럼의 값 목록 |
| `toNode(row)` | - | 행 | 행 데이터로 TreeNodeModel 만들기 (`children` 포함). 그리드에 넣지는 않음 |
| `addRows(target, rows, parent?)` | Doc | 행 배열 | 최상위 또는 `parent` 아래에 추가 |
| `setRows(target, rows)` | Doc | 행 배열 | 전체 교체 (변경 기록 없음) |
| `clearRows(target)` | Doc | 없음 | 전체 삭제 (변경 기록 없음) |
| `updateNode(node, values)` | - | 행 | 행 하나의 값 바꾸기 |
| `updateRows(target, rows, key = 'id')` | Doc | 행 데이터 배열 | 키로 찾아 바꾸기. 못 찾은 행 데이터를 돌려줌 |
| `upsertRows(target, rows, key = 'id')` | Doc | 행 배열 | 바꾸고, 없으면 최상위에 추가. 추가된 행을 돌려줌 |
| `removeNodes(target, selector)` | Doc | 행 배열 | 행, 행 배열, 조건으로 삭제 |
| `removeById(target, ids, key = 'id')` | Doc | 행 배열 | 키 값으로 삭제 |
| `removeSelected(target)` | Doc | 행 배열 | 선택된 행 삭제 |
| `selectNodes(target, selector, add = false)` | Grid | 행 배열 | 선택. `[]` 이면 선택 해제 |
| `selectById(target, ids, key = 'id', add = false)` | Grid | 행 배열 | 키 값으로 선택 |
| `onSelectionChange(target, fn)` | Doc | 해제 함수 | 선택이 바뀌면 `fn(선택된 행 배열)` |
| `revealNode(target, selector, select = true)` | Grid | 행 또는 `undefined` | 펼치고, 스크롤하고, 깜빡이고, 선택 |
| `setChildrenLoader(target, loader)` | Doc | 없음 | 펼칠 때 `loader(node, row)` 로 하위 행 불러오기 |
| `showRowsWhere(target, predicate)` | Doc | 없음 | 조건에 맞는 행만 표시 |
| `showAllRows(target)` | Doc | 없음 | 숨긴 행 모두 표시 |
| `setFilters(target, filters)` | Doc | 없음 | 헤더 필터 지정 |
| `sortBy(target, spec?, sort = 'asc')` | Grid | 없음 | 정렬. `spec` 이 없으면 해제 |
| `setColumnsVisible(target, dataIndexes, visible = true)` | Grid | 없음 | 컬럼 보이기 / 숨기기 |
| `getChanges(target)` | Doc | `{ added, modified, removed }` | 변경 트랜잭션 모드의 변경 행 |
| `on(target, name, fn)` | Grid | 해제 함수 | 그리드 이벤트 등록 |
| `withLoader(target, task, text?)` | Grid | Promise | 로딩 표시를 덮고 작업 실행 |
| `loadRows(target, task, text?)` | Grid | Promise | 로딩 표시 + 전체 교체 |
| `downloadCSV(target, fileName = 'export.csv', options?)` | Grid | `true` 또는 `false` | CSV 파일 내려받기 |

"행" 은 TreeNodeModel, "행 데이터" 는 [위의 평범한 객체](#행-데이터-row) 입니다. 인자의 자세한 형은 소스의 JSDoc 에 있습니다.

## 행 그룹핑과 같이 쓸 때

[행 그룹핑](/datagridview/row-grouping) 으로 행을 묶으면 그리드가 TreeDocument 에 "그룹 행" 을 끼워 넣습니다. 앱이 묶지 않아도 사용자가 헤더 메뉴의 `테이블 보기 구성...` 에서 직접 묶을 수 있습니다.

- 이 모듈의 읽는 함수(`getNodes`, `getRows`, `findNode`, `getNodeById`, `keyNodesBy`, `getColumnValues`, `getSelectedNodes`, `getSelectedRows`, `getActiveNode`)와 조건으로 고르는 함수는 그룹 행을 빼고 일반 행만 다룹니다. 원본 `getAllDescendants()`, `getSelectedNodes()` 에는 그룹 행이 섞여 나옵니다.
- 묶인 상태에서 `setRows`, `loadRows`, `addRows` 로 넣은 행은 그룹에 들어가지 않습니다. 넣은 뒤에 `grid.reapplyRowGrouping()` 을 부릅니다.

## 확인 상태

- 2025, 2026 원본 webapps 를 ENOVIA 서버 없이 내 PC 에서 띄우고, 실제 DataGridView 에 함수 39개를 모두 실행해 이 페이지에 적은 동작을 확인했습니다 (Edge, 한국어 화면). lodash 는 4.18.1 배포 파일 `lodash.js` 를 `webapps/lodash-4.18.1/` 에 그대로 넣어 불렀습니다.
- 하위 행이 없는 3,000 행에서 `setRows`, 조건으로 1,500 행 선택, `updateRows` 3,000 행은 각각 0.05 초 안쪽이었습니다. 하위 행이 9개씩 달린 3,000 행 트리의 `setRows` 는 0.15 초 안쪽이었습니다.
- 확인하지 않은 것: ENOVIA 서버와 3DDashboard 위젯 안에서의 실행, 터치 모드, `dataSource` 로 행을 나눠 불러오는 그리드 ([서버 사이드 데이터](/datagridview/server-side)).

## 전체 소스

::: details DataGridViewUtils.js

<<< @/../Recipes/DataGridViewUtils.js

:::
