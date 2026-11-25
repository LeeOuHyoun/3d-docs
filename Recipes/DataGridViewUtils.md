# DataGridViewUtils 사용법

DataGridView 를 다룰 때 반복되는 처리(행 갱신, 키로 찾기, 선택, 트리 지연 로딩, 변경 모으기 등)를 모은 개발용 보조 모듈입니다.

- 소스: `DataGridViewUtils.js` (같은 폴더)
- 각 함수의 인자와 반환값 상세는 소스의 JSDoc 을 봅니다.
- 같은 내용을 더 자세히 풀어 쓴 사양서: `docs/datagridview/utils.md` (사양서 사이트의 DataGridView > 실무 레시피 > 보조 함수 모음)

## 1. 불러오기

```js
define('DS/MyApp/PartListView', ['DS/Recipes/DataGridViewUtils'], (GridUtils) => {
  GridUtils.loadRows('partGrid', fetchParts);
});
```

- 모듈 이름 `DS/Recipes/DataGridViewUtils` 와 의존 모듈 `DS/lodash-4.18.1/lodash` 의 루트는 현장 경로로 바꿔서 씁니다.
- 그 밖의 의존 모듈: `DS/DataGridView/DataGridView`, `DS/TreeModel/TreeDocument`, `DS/TreeModel/TreeNodeModel`

## 2. 첫 인자 target

모든 함수의 첫 인자로 아래 중 아무거나 넘길 수 있습니다.

| 넘기는 값 | 예 |
| --- | --- |
| 그리드 인스턴스 | `GridUtils.getRows(grid)` |
| 그리드를 담은 DOM 요소의 id | `GridUtils.getRows('partGrid')` (`grid.inject(document.getElementById('partGrid'))` 한 경우) |
| DOM 요소 (그리드를 담은 요소, 셀 안의 요소) | `GridUtils.getRows(event.target)` |
| TreeDocument | `GridUtils.getRows(treeDocument)` (데이터만 다루는 함수만) |

- 그리드를 못 찾으면 `DataGridView not found: ...` 오류를 던집니다.
- 그리드 화면을 다루는 함수(선택, 이동, 정렬, 컬럼, 로딩, 이벤트, CSV)는 TreeDocument 를 받지 않습니다. 아래 함수 목록의 target 열을 봅니다.

## 3. 행 데이터와 조건

### 행 데이터(Row)

행을 넣고 읽을 때 쓰는 평범한 객체입니다. 아래 키를 뺀 나머지는 모두 컬럼 값(`dataIndex: 값`)이 됩니다.

```js
{
  label: 'PRT-0001',
  id: 'a1b2',
  title: '브라켓',
  status: '작업중',
  icons: [{ iconName: 'folder', fontIconFamily: 1 }],
  children: []
}
```

| 키 | 뜻 |
| --- | --- |
| `label` | 트리 컬럼(`dataIndex: 'tree'`) 값 |
| `children` | 하위 행 배열. 빈 배열 `[]` 은 "하위 행이 있지만 아직 안 불러옴" (펼침 아이콘 표시) |
| `icons`, `badges`, `data` | TreeNodeModel 옵션 그대로 |

`getRow(node)` 와 `getRows(...)` 는 `{ label, ...컬럼 값 }` 형태로 돌려줍니다. children 은 넣지 않습니다.

### 행 조건(predicate)

행을 고르는 함수(`getNodes`, `findNode`, `removeNodes`, `selectNodes`, `showRowsWhere` 등)는 lodash iteratee 형태로 조건을 받습니다.

| 형태 | 예 | 뜻 |
| --- | --- | --- |
| 함수 | `(row, node) => row.qty > 10` | 행 데이터와 TreeNodeModel 을 받음 |
| 객체 | `{ status: '완료' }` | 값이 모두 같은 행 |
| 문자열 | `'checked'` | 그 컬럼 값이 truthy 인 행 |

`removeNodes`, `selectNodes`, `revealNode` 는 조건 대신 TreeNodeModel 하나나 배열도 받습니다. `null`, `undefined` 를 넘기면 아무 행도 고르지 않습니다.

### 키 컬럼(key)

`getNodeById`, `selectById`, `removeById`, `updateRows`, `upsertRows`, `keyNodesBy` 는 행을 키 컬럼 값으로 찾습니다.

- 기본 키 컬럼은 `'id'` 입니다. 트리 컬럼 값으로 찾으려면 `'tree'` 를 줍니다.
- 키 컬럼 값이 없는 행은 찾는 대상이 아닙니다. 찾을 값으로 `null`, `undefined` 를 넘기면 아무 행도 찾지 않습니다.
- 같은 키 값을 가진 행이 여럿이면 `selectById`, `removeById`, `updateRows` 는 모두 다루고, `getNodeById` 는 앞의 행, `keyNodesBy` 는 뒤의 행 하나만 돌려줍니다.

## 4. 상황별 사용법

### 데이터 불러와서 전체 교체

```js
GridUtils.loadRows('partGrid', () => fetchParts(), 'Loading...');
GridUtils.setRows('partGrid', rows);
GridUtils.clearRows('partGrid');
```

- `loadRows` 는 불러오는 동안 그리드에 로딩 표시를 덮고, 끝나면 `setRows` 로 교체합니다. 함수 대신 Promise 를 넘겨도 됩니다. 실패하면 기존 행은 그대로 두고 로딩 표시만 지웁니다.
- `setRows` 는 변경 트랜잭션 모드가 켜져 있어도 변경 기록 없이 새 데이터로 시작합니다. 저장 안 된 변경은 버려집니다.
- 행 데이터가 `null`, `undefined` 면 빈 그리드가 됩니다.

### 서버 저장 결과 반영

```js
saveToServer(payload).then((results) => {
  const notFound = GridUtils.updateRows('partGrid', results);
  GridUtils.upsertRows('partGrid', results, 'physicalId');
});
```

- `updateRows` 는 키 컬럼(기본 `id`) 값이 같은 행을 모두 찾아 넘긴 값만 바꾸고, 못 찾은 행 데이터를 돌려줍니다.
- `upsertRows` 는 없는 행을 최상위에 추가하고, 추가된 행을 돌려줍니다.
- 행 하나만 바꿀 때는 `GridUtils.updateNode(node, { title: '새 품명', label: 'PRT-0001-A' })` 를 씁니다.
- 여러 번 바꿀 때는 `GridUtils.batch(target, (doc) => { ... })` 로 감싸면 화면을 한 번만 다시 그립니다. 위 함수들은 안에서 이미 묶여 있습니다.

### 행 추가, 삭제

```js
GridUtils.addRows('partGrid', { label: 'NEW-1', id: 'tmp1', status: '작업중' });
GridUtils.addRows('partGrid', childRows, parentNode);
GridUtils.removeSelected('partGrid');
GridUtils.removeById('partGrid', ['a1b2', 'c3d4']);
GridUtils.removeNodes('partGrid', { status: '폐기' });
```

### 찾기, 읽기

```js
const node = GridUtils.getNodeById('partGrid', 'a1b2');
const first = GridUtils.findNode('partGrid', (row) => row.label.startsWith('ASM'));
const doneRows = GridUtils.getRows('partGrid', { status: '완료' });
const selected = GridUtils.getSelectedRows('partGrid');
const active = GridUtils.getActiveNode('partGrid');
const nodeById = GridUtils.keyNodesBy('partGrid', 'id');
const qtyList = GridUtils.getColumnValues('partGrid', 'qty');
```

- 읽기 함수는 하위 행, 접힌 행, `showRowsWhere` 로 숨긴 행까지 모두 포함합니다. 행 그룹핑이 만든 그룹 행은 넣지 않습니다.
- `getSelectedNodes` 는 선택 배열의 복사본을 돌려줍니다. 돌면서 선택이나 삭제를 해도 안전합니다.
- 행 그룹핑으로 묶인 상태에서 `setRows`, `loadRows`, `addRows` 로 넣은 행은 그룹에 들어가지 않습니다. 넣은 뒤 `grid.reapplyRowGrouping()` 을 부릅니다.

### 선택, 행으로 이동

```js
GridUtils.selectById('partGrid', 'a1b2');
GridUtils.selectNodes('partGrid', { status: '작업중' });
GridUtils.selectNodes('partGrid', []);
GridUtils.revealNode('partGrid', { id: 'a1b2' });

const off = GridUtils.onSelectionChange('partGrid', (nodes) => updateDetailPanel(nodes));
off();
```

- 선택은 그리드 옵션 `rowSelection` 이 `'single'` 또는 `'multiple'` 이어야 동작합니다. `add` 인자(기존 선택 유지)는 `'multiple'` 일 때만 의미가 있습니다.
- `revealNode` 는 접힌 부모를 펼치고, 행이 화면 가운데 오게 스크롤한 뒤 깜빡여 표시하고 선택합니다.
- `onSelectionChange` 는 한 번의 조작으로 선택이 여러 번 바뀌어도 콜백을 한 번만 부릅니다.

### 트리 하위 행 지연 로딩

```js
const treeDocument = new TreeDocument({ useAsyncPreExpand: true });
GridUtils.setRows(treeDocument, [{ label: 'ASM-0001', id: 'x1', children: [] }]);
GridUtils.setChildrenLoader(treeDocument, (node, row) => fetchChildren(row.id));
```

- TreeDocument 를 `useAsyncPreExpand: true` 로 만들고, 불러올 행은 `children: []` 로 넣어 둡니다.
- 하위 행이 이미 있는 행은 다시 불러오지 않습니다. 불러오기가 실패해도 행이 "펼치는 중" 에서 멈추지 않고, 접었다가 다시 펼치면 loader 를 다시 부릅니다.
- `onPreExpand` 는 하나만 등록됩니다. 다시 부르면 이전 loader 를 대체합니다.

### 검색, 필터, 정렬, 컬럼

```js
GridUtils.showRowsWhere('partGrid', (row) => row.label.includes(keyword));
GridUtils.showAllRows('partGrid');
GridUtils.setFilters('partGrid', { tree: 'stringRegexp', status: 'set', qty: 'number' });
GridUtils.sortBy('partGrid', 'dueDate', 'desc');
GridUtils.sortBy('partGrid', [{ dataIndex: 'status', sort: 'asc' }, { dataIndex: 'qty', sort: 'desc' }]);
GridUtils.sortBy('partGrid');
GridUtils.setColumnsVisible('partGrid', ['owner', 'modified'], false);
```

- `showRowsWhere` 는 헤더 필터와 별개로 행을 숨깁니다. 하위 행이 조건에 맞으면 부모 행도 보이지만, 접힌 부모를 펼치지는 않습니다. 숨겨진 행은 선택이 풀립니다.
- `setFilters` 는 헤더 필터를 쓸 컬럼을 지정합니다. 키 `tree` 는 자동으로 `label` 로 바뀝니다. 부를 때마다 전체를 바꾸므로 뺀 컬럼의 필터는 없어집니다.
- `sortBy(target)` 처럼 정렬 조건을 빼면 정렬을 해제합니다. 행 순서는 마지막으로 정렬된 그대로 남습니다.
- `alwaysVisibleFlag` 컬럼은 숨겨지지 않습니다.

### 편집을 모아서 한 번에 저장 (변경 트랜잭션 모드)

```js
const doc = GridUtils.getDoc('partGrid');
doc.setUseChangeTransactionMode(true);

function handleSave() {
  const { added, modified, removed } = GridUtils.getChanges(doc);
  saveAll({
    create: added.map(GridUtils.getRow),
    update: modified.map(GridUtils.getRow),
    delete: removed.map(GridUtils.getRow)
  })
    .then((results) => {
      GridUtils.updateRows(doc, results);
      doc.acceptChanges();
    })
    .catch(() => doc.cancelChanges());
}
```

- 이 모드에서 `removeNodes` 계열은 삭제 표시만 합니다. 실제로 지워지는 것은 `acceptChanges()` 때입니다.
- 추가했다가 지운 행은 `getChanges` 결과에 넣지 않습니다. `children` 으로 같이 넣은 하위 행도 `added` 에 들어갑니다 (부모가 앞).
- 서버 응답 값을 먼저 반영(`updateRows`)한 뒤에 `acceptChanges()` 를 부릅니다. 순서가 바뀌면 응답 반영분이 다시 "변경됨" 으로 남습니다.
- 새로 만든 행은 서버가 id 를 새로 주므로 키로 찾을 수 없습니다. 보낸 순서대로 `GridUtils.updateNode(added[i], created[i])` 로 반영합니다.
- 행에 없던 키는 변경으로 기록되지 않습니다 (원본 `setAttribute`, `updateOptions` 의 동작). 편집할 컬럼의 키는 값이 없어도 `''` 로 채워서 넣습니다.

### 이벤트

```js
const off = GridUtils.on('partGrid', 'change', (event, cellInfos) => {
  console.log(cellInfos.nodeModel.getLabel(), cellInfos.cellModel);
});
off();
```

- `grid.removeEventListener` 는 원래 함수가 아니라 `addEventListener` 의 반환값으로 해제해야 합니다. `on` 이 돌려주는 해제 함수가 이를 처리합니다.

### 로딩 표시, CSV 다운로드

```js
GridUtils.withLoader('partGrid', () => runLongTask(), 'Processing...');
GridUtils.downloadCSV('partGrid', 'parts.csv', { onlySelectedRows: true });
```

`downloadCSV` 의 옵션은 `getAsCSV` 와 같습니다: `onlySelectedRows`, `columnKeys`, `allColumns`, `columnSeparator`, `forceQuotes`.

- `withLoader` 는 띄울 때와 같은 문구로 로딩 표시를 지웁니다 (문구가 다르면 원본 `removeModalLoader` 는 지우지 않음). 겹쳐 불러도 됩니다.
- `downloadCSV` 는 내려받았으면 `true` 를 돌려줍니다. `onlySelectedRows: true` 인데 선택한 행이 없으면 내려받지 않고 `false` 를 돌려줍니다.
- CSV 에는 화면에 펼쳐져 보이는 행만 나갑니다. 접힌 하위 행은 빠집니다.

## 5. 함수 목록

target 열의 Grid 는 그리드, DOM 요소, DOM id 만 받습니다. Doc 은 TreeDocument 도 받습니다.

| 함수 | target | 반환 | 설명 |
| --- | --- | --- | --- |
| `getGrid(target)` | Grid | DataGridView | 그리드 인스턴스 |
| `getDoc(target)` | Doc | TreeDocument | 그리드의 데이터 |
| `batch(target, fn)` | Doc | fn 반환값 | 변경을 묶어 한 번만 다시 그림 |
| `getValue(node, dataIndex)` | - | 값 | 셀 값. `'tree'` 는 label |
| `getRow(node)` | - | Row | 행 데이터 객체 |
| `getNodes(target, predicate?)` | Doc | TreeNodeModel[] | 전체 또는 조건에 맞는 행 |
| `getRows(target, predicate?)` | Doc | Row[] | 위의 행 데이터 버전 |
| `findNode(target, predicate)` | Doc | TreeNodeModel | 조건에 맞는 첫 행 |
| `getNodeById(target, id, key='id')` | Doc | TreeNodeModel | 키 값으로 찾기 |
| `keyNodesBy(target, key='id')` | Doc | { 키: TreeNodeModel } | 키 사전 |
| `getSelectedNodes(target)` | Doc | TreeNodeModel[] | 선택 행 (복사본) |
| `getSelectedRows(target)` | Doc | Row[] | 선택 행 데이터 |
| `getActiveNode(target)` | Grid | TreeNodeModel | 활성 셀의 행 |
| `getColumnValues(target, dataIndex)` | Doc | 값[] | 한 컬럼의 값 목록 |
| `toNode(row)` | - | TreeNodeModel | 행 데이터로 행 만들기 (children 포함) |
| `addRows(target, rows, parent?)` | Doc | TreeNodeModel[] | 최상위 또는 parent 하위에 추가 |
| `setRows(target, rows)` | Doc | TreeNodeModel[] | 전체 교체 (변경 기록 없음) |
| `clearRows(target)` | Doc | - | 전체 삭제 (변경 기록 없음) |
| `updateNode(node, values)` | - | TreeNodeModel | 행 값 바꾸기 |
| `updateRows(target, rows, key='id')` | Doc | Row[] | 키로 찾아 바꾸기 (같은 키의 행 모두). 못 찾은 행 반환 |
| `upsertRows(target, rows, key='id')` | Doc | TreeNodeModel[] | 바꾸고 없으면 추가. 추가된 행 반환 |
| `removeNodes(target, selector)` | Doc | TreeNodeModel[] | 행, 배열, 조건으로 삭제 |
| `removeById(target, ids, key='id')` | Doc | TreeNodeModel[] | 키 값으로 삭제 |
| `removeSelected(target)` | Doc | TreeNodeModel[] | 선택 행 삭제 |
| `selectNodes(target, selector, add=false)` | Grid | TreeNodeModel[] | 선택. `[]` 이면 해제 |
| `selectById(target, ids, key='id', add=false)` | Grid | TreeNodeModel[] | 키 값으로 선택 |
| `onSelectionChange(target, fn)` | Doc | 해제 함수 | 선택 변경 구독 |
| `revealNode(target, selector, select=true)` | Grid | TreeNodeModel | 펼치고 스크롤하고 깜빡이고 선택 |
| `setChildrenLoader(target, loader)` | Doc | - | 펼칠 때 하위 행 불러오기 |
| `showRowsWhere(target, predicate)` | Doc | - | 조건에 맞는 행만 표시 |
| `showAllRows(target)` | Doc | - | 모두 표시 |
| `setFilters(target, filters)` | Doc | - | 헤더 필터 지정 |
| `sortBy(target, spec?, sort='asc')` | Grid | - | 정렬. spec 이 없으면 해제 |
| `setColumnsVisible(target, dataIndexes, visible=true)` | Grid | - | 컬럼 표시/숨김 |
| `getChanges(target)` | Doc | { added, modified, removed } | 변경 트랜잭션 모드의 변경 행 |
| `on(target, name, fn)` | Grid | 해제 함수 | 그리드 이벤트 등록 |
| `withLoader(target, task, text?)` | Grid | Promise | 로딩 표시 중 작업 실행 |
| `loadRows(target, task, text?)` | Grid | Promise&lt;TreeNodeModel[]&gt; | 로딩 표시 + 전체 교체 |
| `downloadCSV(target, fileName?, options?)` | Grid | boolean | CSV 파일 내려받기. 내려받았으면 true |

## 6. 확인 상태

- 2025, 2026 원본 webapps 를 ENOVIA 서버 없이 내 PC 에서 띄우고, 실제 DataGridView 에 함수 39개를 모두 실행해 확인했습니다 (Edge, 한국어 화면, lodash 4.18.1).
- 확인하지 않은 것: ENOVIA 서버와 3DDashboard 위젯 안에서의 실행, 터치 모드, `dataSource` 로 행을 나눠 불러오는 그리드.
