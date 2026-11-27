---
title: API 로 받아 넣기 (비동기와 타이밍)
---

# API 로 받아 넣기 (비동기와 타이밍) <Badge type="tip" text="로컬 실행 확인" />

셀 값, 콤보 목록, 편집 가능 여부를 서버(API)에서 받아 넣는 방법입니다. 응답은 그리드를 만든 **뒤에**, **순서 없이**, 사용자가 화면을 **만지는 중에** 도착합니다. 그래서 받은 것을 "언제 도착해도 맞게 들어가는 자리" 에 넣어야 합니다. 이 페이지는 그 자리와, 자리를 잘못 골랐을 때 생기는 문제를 정리합니다.

::: tip 콤보박스 목록만 넣으면 될 때
콤보박스(단독, 그리드 셀)에 서버 목록을 넣는 가장 간단한 방법만 필요하면 [콤보박스에 서버 목록 넣기](/controls/combobox-list) 를 먼저 봅니다. 이 페이지는 값, 권한, 행마다 다른 목록까지 모든 경우를 다룹니다.
:::

샘플: [asyncColumn.js](https://github.com/LeeOuHyoun/3d-docs/blob/main/Recipes/asyncColumn/asyncColumn.js) (그리드 하나에 패턴 1 ~ 4), [lazyComboColumn.js](https://github.com/LeeOuHyoun/3d-docs/blob/main/Recipes/lazyComboColumn/lazyComboColumn.js) (패턴 5 를 컬럼 한 줄로 쓰는 도우미). 둘 다 [전체 소스](#전체-소스) 에 있습니다.

::: info 근거
CollectionView.js 의 updateView / `_renderViewport` (옵션 기본값 `updateCellContent: false`) / updateCellView / `_setEditionMode` / isCellInEdition, `_listenToTreeDocument` (행 값이 바뀌면 그 행만 다시 그리고, 편집 중인 활성 셀은 `ignoreActiveCell` 로 건너뜀). DataGridView.js 의 updateRowView / updateColumnView / `_setTweakerForCell` (행에 키가 없으면 편집기를 만들지 않음) / getCellEditableState / `_getCellSemantics` / requestDelayedModelData / reapplySortModel / reapplyRowGrouping. TreeModel.js 의 TreeNodeModel.updateOptions / setAttribute / notifyModelUpdate (변경 기록 조건), TreeDocument.setUseChangeTransactionMode, FilterManager.reapplyPropertyFilterModel. Utilities.js 의 Utils.extendObjectInstanceWithNewInside (`options.grid` 를 새 객체로 바꿈). Tweakers.js 의 string 편집기 (minLengthBeforeSearch / typeDelayBeforeSearch), enum 편집기의 setValue (값이 목록에 없으면 선택 위치를 바꾸지 않음). Controls.js 의 ComboBox `_showPopup` (열기 직전에 `preDropdown` 을 냄). TweakersEx.js 의 AutoCompleteViewModuleTweakerString (`possibleValues` 가 함수면 그대로 `elementsTree` 로 넘김), WUXAutoComplete.js 의 `_applyValue` (값이 `{ label, value }` 객체면 칩에 label 을 씀). 호출부: ENXSBGridConnector, ENXNav, FolderEditor, IPClassifyNav (응답을 `withTransactionUpdate` 안에서 `updateOptions` 로 반영), ConfiguratorPanel (캐시 + `getCellValue` + `updateRenderedColumn`), ENOXDocumentControlUI (요청 번호 비교), ENOChgGovernanceUX (`possibleValues` 를 async 함수로), PCsBulkViewPresenter (`addColumnOrGroup`), IPClassifyUX (`Promise.all` 뒤에 그리드 생성). 동작은 2025, 2026 원본 webapps 를 내 PC 에서 띄워 실제 그리드로 확인했습니다 ([확인 상태](#확인-상태)).
:::

## 세 가지 규칙

**1. 컬럼 함수는 읽기만 합니다.** `getCellValue`, `getCellSemantics`, `getCellEditableState` 에 `async` 를 붙이지 않고, 그 안에서 서버를 부르지 않습니다. 이미 받아 둔 것을 읽어서 바로 돌려줍니다.

**2. 받은 것을 두는 자리는 두 곳입니다.**

| 받은 것 | 두는 곳 | 화면 갱신 |
| --- | --- | --- |
| 행마다 다른 **값** (재고, 이름, 권한) | 그 행의 데이터. `nodeModel.updateOptions({ grid: { ... } })` | 그리드가 알아서 그 행만 다시 그립니다. 입력 중인 셀은 건드리지 않습니다 |
| **목록** (콤보 목록) | 변수. 컬럼 전체가 같으면 배열 하나, 행마다 다르면 `{ 키: 목록 }` | `grid.updateColumnView(컬럼 번호, { updateCellContent: true })` 로 그 컬럼만 |

**3. 아직 안 온 것은 "없는 상태" 로 둡니다.** 값의 키는 미리 넣지 않고, 목록이 올 때까지는 `getCellEditableState` 로 그 셀의 편집을 막습니다. (드롭다운을 여는 순간에 목록을 받는 [패턴 5](#패턴-5-드롭다운을-열-때-목록-받기) 는 막지 않고 엽니다.)

행을 가리킬 때는 `TreeNodeModel` 을 들고 있습니다. `rowID`, `cellID`, `nodeModel.options.grid` 객체는 응답을 기다리는 동안 달라질 수 있습니다 ([자주 겪는 문제](#자주-겪는-문제)).

## 먼저: 기다렸다가 만들 수 있으면

필요한 것을 다 받은 뒤에 그리드를 만들면 타이밍 문제가 생기지 않습니다. 응답이 모두 빠르면 이 방법이 가장 쉽습니다. 실제 앱(IPClassifyUX, ENXNav)도 그리드 옵션을 다 받은 뒤에 `new DataGridView(...)` 를 부릅니다.

```js
Promise.all([requestStatuses(), requestParts()]).then(function (results) {
  const statuses = results[0];
  const parts = results[1];
  const treeDocument = new TreeDocument();
  treeDocument.prepareUpdate();
  parts.forEach(function (part) {
    treeDocument.addRoot(new TreeNodeModel({ label: part.id, grid: part }));
  });
  treeDocument.pushUpdate();
  new DataGridView({
    treeDocument: treeDocument,
    columns: [
      { text: '번호', dataIndex: 'id' },
      { text: '상태', dataIndex: 'status', typeRepresentation: 'customEnumMember', editableFlag: true, semantics: { possibleValues: statuses } }
    ]
  }).inject(container);
});
```

느린 API 가 하나라도 섞여 있으면 그만큼 첫 화면이 늦어집니다. 그때는 그리드를 먼저 띄우고 아래 패턴으로 나중에 채웁니다.

## 하고 싶은 일로 찾기

| 하고 싶은 일 | 패턴 |
| --- | --- |
| 콤보 목록 하나를 받아 컬럼 전체에 쓰기 | [패턴 1](#패턴-1-컬럼-전체의-콤보-목록) |
| 행마다 값을 받아 채우기 (재고, 편집 권한) | [패턴 2](#패턴-2-행마다-값-채우기) |
| 행마다 다른 콤보 목록 | [패턴 3](#패턴-3-행마다-다른-콤보-목록) |
| 앞 칸에서 고른 값에 따라 달라지는 콤보 목록 | [패턴 4](#패턴-4-앞-칸-값에-따라-달라지는-콤보-목록) |
| 드롭다운을 **여는 순간**에 서버에서 목록 받기 | [패턴 5](#패턴-5-드롭다운을-열-때-목록-받기) |
| 행이 아주 많아서 화면에 보이는 행만 채우기 | [패턴 6](#패턴-6-보이는-행만-채우기) |
| 목록이 너무 커서, 입력한 글자로 서버에서 찾기 | [패턴 7](#패턴-7-입력한-글자로-서버에서-찾기) |
| 컬럼 자체를 받아서 만들기 | [패턴 8](#패턴-8-컬럼을-받아서-만들기) |
| 조회 버튼을 연달아 눌러도 마지막 결과만 남기기 | [조회를 다시 할 때](#조회를-다시-할-때-늦게-온-응답) |
| 콤보에서 첫 항목을 골라도 저장되지 않을 때 | [콤보 셀의 값이 비었거나 목록에 없을 때](#콤보-셀의-값이-비었거나-목록에-없을-때) |

아래 예제에서 `requestParts()`, `requestStatuses()`, `requestAllowedStatuses()`, `requestStocks()`, `requestOwners()`, `requestItems()`, `requestPersons()`, `requestColumns()`, `requestPrice()` 는 이미 갖고 있는 서버 호출 함수이고 Promise 를 돌려준다고 가정합니다. `treeDocument` 에는 `{ id: 'P-001', title: '브라켓', status: 'WIP', owners: ['kcs'], category: 'BOLT', item: 'M6' }` 같은 행이 들어 있습니다.

## 패턴 1. 컬럼 전체의 콤보 목록

목록을 변수에 두고, 컬럼 함수가 그 변수를 읽게 합니다. 응답이 오면 변수를 바꾸고 그 컬럼만 다시 그립니다.

```js
let statuses = [];

const grid = new DataGridView({
  treeDocument: treeDocument,
  columns: [
    { text: '번호', dataIndex: 'id' },
    {
      text: '상태',
      dataIndex: 'status',
      typeRepresentation: 'customEnumMember',
      editableFlag: true,
      editionPolicy: 'EditionOnClick',
      getCellEditableState: function () {
        return statuses.length > 0;
      },
      getCellSemantics: function () {
        return { possibleValues: statuses };
      }
    }
  ]
});

requestStatuses().then(function (result) {
  statuses = result;
  grid.updateColumnView(grid.layout.getColumnIndex('status'), { updateCellContent: true });
});
```

- 목록이 `[{ label: '작업중', value: 'WIP' }, ...]` 모양이면 목록이 오기 전에는 셀에 값(`WIP`)이 그대로 보이고, 온 뒤에 `작업중` 으로 바뀝니다.
- 목록이 오기 전에도 `possibleValues` 는 **빈 배열**로 줍니다. `customEnumMember` 컬럼에 `possibleValues` 가 없으면(`undefined`) 셀에 값 대신 `Default` 가 보입니다.
- `updateColumnView` 는 화면에 보이는 그 컬럼의 셀만 다시 그립니다. 다른 컬럼에서 입력 중이던 글자는 그대로 남습니다.
- 응답이 행보다 먼저 와도, 그리드가 화면에 붙기 전에 와도 됩니다. 그때는 다시 그릴 셀이 없을 뿐이고, 나중에 그려지는 셀이 바뀐 변수를 읽습니다.

## 패턴 2. 행마다 값 채우기

받은 값을 그 행의 데이터에 넣습니다. 컬럼이 없는 키도 넣을 수 있으므로, 편집 권한 같은 값은 화면에 안 보이는 키(아래의 `locked`)로 넣고 컬럼 함수에서 읽습니다.

```js
const grid = new DataGridView({
  treeDocument: treeDocument,
  columns: [
    { text: '번호', dataIndex: 'id' },
    {
      text: '품명',
      dataIndex: 'title',
      editableFlag: true,
      getCellEditableState: function (cellInfos) {
        return cellInfos.nodeModel.options.grid.locked === false;
      }
    },
    { text: '재고', dataIndex: 'stock', typeRepresentation: 'integer' }
  ]
});

const nodes = treeDocument.getAllDescendants();
const ids = nodes.map(function (node) { return node.options.grid.id; });

requestStocks(ids).then(function (stocks) {
  const nodeById = {};
  nodes.forEach(function (node) {
    nodeById[node.options.grid.id] = node;
  });
  treeDocument.withTransactionUpdate(function () {
    stocks.forEach(function (stock) {
      const node = nodeById[stock.id];
      if (node) {
        node.updateOptions({ grid: { stock: stock.stock, locked: stock.locked } });
      }
    });
  });
});
```

`stocks` 는 `[{ id: 'P-001', stock: 10, locked: false }, ...]` 모양입니다. `locked` 가 아직 없는 행(응답 전)은 품명을 편집할 수 없고, `false` 로 온 행만 편집할 수 있습니다.

- 값을 넣으면 그리드가 그 행을 바로 다시 그립니다. `grid.updateView(...)` 를 따로 부르지 않습니다.
- 여러 행을 한꺼번에 넣을 때는 `withTransactionUpdate` (또는 `prepareUpdate()` ~ `pushUpdate()`)로 묶습니다. 3,000 행을 하나씩 넣으면 4.5 ~ 5 초, 묶으면 0.03 ~ 0.05 초였습니다. 실제 앱(ENXSBGridConnector, FolderEditor, IPClassifyNav)도 이렇게 묶어서 넣습니다.
- 행마다 따로 부르는 API 라면 응답이 올 때마다 `node.updateOptions(...)` 를 부르면 됩니다. 응답 하나에 한 행이므로 묶을 것이 없습니다.
- `stock`, `locked` 키는 행을 만들 때 넣지 않습니다. 넣어 두면(`stock: ''`) [변경 트랜잭션 모드](#변경-트랜잭션-모드와-같이-쓸-때) 에서 채운 값이 "사용자가 고친 것" 으로 기록됩니다.
- [보조 함수 모음](/datagridview/utils) 을 쓰면 응답 반영은 `GridUtils.updateRows(grid, stocks)` 한 줄입니다. 키(`id`)로 행을 다시 찾고 묶어서 넣습니다.

::: warning 편집하는 컬럼을 비동기로 채울 때
사용자가 고칠 수 있는 컬럼의 값을 나중에 채우면, 사용자가 먼저 고친 값을 서버 값이 덮어쓸 수 있습니다. 그런 컬럼은 값이 올 때까지 `getCellEditableState` 로 편집을 막아 둡니다. 행에 그 키가 아예 없으면 `editableFlag: true` 여도 편집기가 열리지 않고, 값이 들어온 뒤부터 열립니다.
:::

## 패턴 3. 행마다 다른 콤보 목록

목록은 행 데이터에 넣지 않고 `{ 키: 목록 }` 표에 둡니다. 컬럼 함수가 그 행의 키로 표를 읽습니다.

```js
const ownersByPart = {};
const ownerRequests = {};

const grid = new DataGridView({
  treeDocument: treeDocument,
  columns: [
    { text: '번호', dataIndex: 'id' },
    {
      text: '담당자',
      dataIndex: 'owners',
      typeRepresentation: 'string',
      editableFlag: true,
      editionPolicy: 'EditionOnClick',
      getCellEditableState: function (cellInfos) {
        return cellInfos.nodeModel.options.grid.id in ownersByPart;
      },
      getCellSemantics: function (cellInfos) {
        return { allowMultipleValuesFlag: true, possibleValues: ownersByPart[cellInfos.nodeModel.options.grid.id] };
      }
    }
  ]
});

function loadOwners(partId) {
  if (!ownerRequests[partId]) {
    ownerRequests[partId] = requestOwners(partId).then(function (owners) {
      ownersByPart[partId] = owners;
      grid.updateColumnView(grid.layout.getColumnIndex('owners'), { updateCellContent: true });
    });
  }
  return ownerRequests[partId];
}

treeDocument.getAllDescendants().forEach(function (node) {
  loadOwners(node.options.grid.id);
});
```

- `ownerRequests` 는 같은 키를 두 번 요청하지 않게 합니다. lodash 가 있으면 `_.memoize` 한 줄로 같은 일을 합니다 (샘플의 `loadInto`).
- 목록이 온 행부터 편집이 열립니다. 여러 개 선택(`allowMultipleValuesFlag`) 편집기는 열려 있는 동안 목록을 바꿔도 화면에 반영되지 않으므로(닫았다가 다시 열어야 보임), 이렇게 목록이 올 때까지 막아 둡니다. 미리 받지 않고 **여는 순간에** 받으려면 [패턴 5](#패턴-5-드롭다운을-열-때-목록-받기) 를 씁니다.
- 행을 나중에 추가하면 그 행에 대해 `loadOwners` 를 부릅니다. 이미 받은 키면 요청 없이 바로 쓰입니다.
- 요청이 실패하면 그 행은 계속 잠겨 있습니다. 다시 시도하게 하려면 `.catch` 에서 `delete ownerRequests[partId]` 로 지웁니다.
- 응답이 200 개 따로 와서 `updateColumnView` 를 200 번 불러도 0.15 초 정도였습니다 (보이는 행 12 개 기준).

## 패턴 4. 앞 칸 값에 따라 달라지는 콤보 목록

"분류" 를 고르면 "품목" 목록이 바뀌는 경우입니다. 패턴 3 과 같은데 표의 키가 앞 칸의 값입니다. 목록이 행이 아니라 **분류에** 붙어 있으므로, 분류가 어떻게 바뀌든 컬럼 함수가 그 행의 지금 분류로 목록을 찾습니다.

```js
const itemsByCategory = {};
const itemRequests = {};

const grid = new DataGridView({
  treeDocument: treeDocument,
  columns: [
    { text: '번호', dataIndex: 'id' },
    {
      text: '분류',
      dataIndex: 'category',
      typeRepresentation: 'customEnumMember',
      editableFlag: true,
      editionPolicy: 'EditionOnClick',
      semantics: { possibleValues: ['BOLT', 'NUT', 'WASHER'] }
    },
    {
      text: '품목',
      dataIndex: 'item',
      typeRepresentation: 'customEnumMember',
      editableFlag: true,
      editionPolicy: 'EditionOnClick',
      getCellEditableState: function (cellInfos) {
        return cellInfos.nodeModel.options.grid.category in itemsByCategory;
      },
      getCellSemantics: function (cellInfos) {
        return { possibleValues: itemsByCategory[cellInfos.nodeModel.options.grid.category] || [] };
      }
    }
  ]
});

function loadItems(category) {
  if (!itemRequests[category]) {
    itemRequests[category] = requestItems(category).then(function (items) {
      itemsByCategory[category] = items;
      grid.updateColumnView(grid.layout.getColumnIndex('item'), { updateCellContent: true });
    });
  }
  return itemRequests[category];
}

treeDocument.getAllDescendants().forEach(function (node) {
  loadItems(node.options.grid.category);
});

treeDocument.onNodeModelUpdate(function (e) {
  const changed = e.data.attributes || {};
  if (!('category' in changed)) {
    return;
  }
  loadItems(changed.category);
  if (!('item' in changed)) {
    e.data.nodeModel.updateOptions({ grid: { item: undefined } });
  }
});
```

- 사용자가 분류를 바꾸면 그 행의 품목을 비우고 새 분류의 목록을 요청합니다. 목록이 오기 전까지 그 행의 품목 셀은 잠기고, 이미 받은 분류면 바로 열립니다.
- 품목은 `''` 가 아니라 **`undefined`** 로 비웁니다. `''` 로 비우면 새 목록의 첫 항목을 골라도 저장되지 않습니다 ([콤보 셀의 값이 비었거나 목록에 없을 때](#콤보-셀의-값이-비었거나-목록에-없을-때)).
- 같은 분류는 한 번만 요청합니다. 행이 500 개여도 분류가 3 가지면 요청은 3 번입니다.
- 목록을 행에 넣는 방식(`node.updateOptions({ grid: { itemOptions: 목록 } })`)이면 "분류를 두 번 연달아 바꿨을 때 먼저 요청한 목록이 늦게 와서 덮어쓰는" 경우를 따로 막아야 합니다. 표에 두면 응답이 어느 순서로 와도 자기 분류 칸에만 들어갑니다.
- `'item' in changed` 를 확인하는 이유: `cancelChanges()` 처럼 분류와 품목이 **같이** 되돌아오는 경우에는 품목을 비우면 안 되기 때문입니다.
- 이 리스너 안에서 `category` 는 바꾸지 않습니다. 받는 값과 같은 값을 다시 바꾸면 끝없이 반복됩니다 ([이벤트 안에서 저장하지 않기](/datagridview/save-and-reflect#주의-이벤트-안에서-저장하지-않기)).

## 패턴 5. 드롭다운을 열 때 목록 받기

목록을 미리 받지 않고, 사용자가 그 셀의 드롭다운을 **여는 순간**에 서버에 묻습니다. 행마다 목록이 다른데 행이 많아서 미리 받기 아까울 때, 열 때마다 최신 목록이어야 할 때 씁니다.

하나만 고르는 드롭다운과 여러 개 고르는 칸은 편집기가 달라서 방법도 다릅니다.

| | 하나 선택 (`customEnumMember`) | 여러 개 선택 (`string` + `allowMultipleValuesFlag`) |
| --- | --- | --- |
| 여는 순간을 아는 방법 | 그리드의 `preDropdown` 이벤트 | `possibleValues` 에 준 함수가 불림 |
| 응답이 왔을 때 | 목록을 변수에 넣고 그 컬럼을 다시 그리면 **열려 있는 목록이 바로 바뀜** | 함수가 돌려준 Promise 가 끝나면 편집기가 목록을 띄움 |
| 기다리는 동안 | 지금 값 하나만 든 목록이 열려 있음 | 편집기가 `로드 중...` 을 보여 줌 |

### 하나 선택

`preDropdown` 은 셀의 드롭다운이 열리기 직전에 납니다. 그때 요청을 보내고, 응답이 오면 패턴 3 과 똑같이 변수에 넣고 그 컬럼을 다시 그립니다.

```js
const statusesByPart = {};

const grid = new DataGridView({
  treeDocument: treeDocument,
  columns: [
    { text: '번호', dataIndex: 'id' },
    {
      text: '상태',
      dataIndex: 'status',
      typeRepresentation: 'customEnumMember',
      editableFlag: true,
      editionPolicy: 'EditionOnClick',
      getCellSemantics: function (cellInfos) {
        const row = cellInfos.nodeModel.options.grid;
        return { possibleValues: statusesByPart[row.id] || [{ label: row.statusName, value: row.status }] };
      }
    }
  ]
});

grid.addEventListener('preDropdown', function (event, cellInfos) {
  if (!cellInfos || !cellInfos.nodeModel || grid.layout.getDataIndexFromColumnIndex(cellInfos.columnID) !== 'status') {
    return;
  }
  const partId = cellInfos.nodeModel.options.grid.id;
  requestAllowedStatuses(partId).then(function (statuses) {
    statusesByPart[partId] = statuses;
    grid.updateColumnView(grid.layout.getColumnIndex('status'), { updateCellContent: true });
  });
});
```

행은 `{ id: 'P-001', status: 'WIP', statusName: '작업중' }` 처럼 값과 표시 글자를 같이 갖고 있고, `requestAllowedStatuses` 는 `[{ label: '작업중', value: 'WIP' }, { label: '승인', value: 'DONE' }]` 을 돌려줍니다.

- 목록을 받기 전에는 **지금 값 하나만 든 목록**을 줍니다. 그래야 셀에 `WIP` 대신 `작업중` 이 보입니다. 값과 표시 글자가 같으면 `[row.status]` 로 충분합니다.
- 드롭다운은 바로 열리고(지금 값 하나), 응답이 오면 열린 채로 전체 목록으로 바뀝니다. 그 뒤에 고르면 값이 저장됩니다. 값이 비어 있는 셀은 응답이 올 때까지 목록이 보이지 않다가, 오면 나타납니다.
- 열 때마다 요청합니다. 한 번 받은 목록을 다시 쓰려면 패턴 3 의 `ownerRequests` 처럼 요청을 키별로 보관합니다.
- 반영은 꼭 **그리드를 거쳐서** (변수에 넣고 `updateColumnView`) 합니다. 이벤트에서 꺼낸 콤보 컨트롤의 `elementsList` 를 직접 바꾸면 목록은 보이지만, 고른 값이 행에 저장되지 않습니다.
- 받은 목록에 지금 값이 없으면 지금 값도 목록에 넣어 줍니다. 빠져 있으면 [첫 항목 문제](#콤보-셀의-값이-비었거나-목록에-없을-때) 가 생깁니다.

### 여러 개 선택

여러 개 선택 편집기는 `possibleValues` 에 함수를 주면 여는 순간에 그 함수를 부릅니다 ([패턴 7](#패턴-7-입력한-글자로-서버에서-찾기) 과 같은 기능). 다만 함수를 주면 그리드가 목록을 미리 알 수 없어서, 값이 코드(`kcs`)면 셀과 칩에 코드가 그대로 보입니다. 이름을 보이게 하려면 두 가지를 더 합니다.

- 편집 중이 아닐 때는 `possibleValues` 에 함수 대신 **지금 값들의 `{ label, value }` 배열**을 줍니다. 셀에 이름이 보입니다.
- 편집 중일 때는 `getCellValue` 로 값을 **`{ label, value }` 객체 배열**로 넘깁니다. 칩에 이름이 보이고, 고른 결과는 코드 배열(`['kcs', 'lyh']`)로 저장됩니다.

편집 중인지는 컬럼 함수 안에서 `this.isCellInEdition(cellInfos.cellID)` 로 압니다. 이 처리는 아래 도우미에 들어 있습니다.

### 도우미: lazyComboColumn

위 두 방법을 컬럼 한 줄로 쓰게 묶은 모듈입니다. ENOVIA 원본에 들어 있는 모듈이 **아닙니다.** `Recipes/lazyComboColumn/lazyComboColumn.js` 를 프로젝트 webapps 에 넣어서 씁니다 ([전체 소스](#전체-소스)). 돌려주는 값이 컬럼 옵션이라 컬럼 정의에 합치면 됩니다.

```js
const grid = new DataGridView({
  treeDocument: treeDocument,
  columns: [
    { text: '번호', dataIndex: 'id' },
    Object.assign({ text: '상태', dataIndex: 'status' }, lazyComboColumn(function (row) {
      return requestAllowedStatuses(row.id);
    }, { labelIndex: 'statusName' })),
    Object.assign({ text: '담당자', dataIndex: 'owners' }, lazyComboColumn(function (row) {
      return requestOwners(row.id);
    }, { multiple: true, labelIndex: 'ownerNames' })),
    Object.assign({ text: '품목', dataIndex: 'item' }, lazyComboColumn(function (row) {
      return requestItems(row.category);
    }))
  ]
});
```

`lazyComboColumn(request, options)` 이 받는 것:

| 인자 | 설명 |
| --- | --- |
| `request(row, nodeModel, text)` | 목록을 돌려주는 함수. Promise 나 배열을 돌려줌. `row` 는 그 행의 **지금** 데이터라서 앞 칸 값(`row.category`)에 따라 다른 목록을 요청할 수 있음. `text` 는 여러 개 선택에서 입력한 글자 (열 때는 `''`) |
| `options.multiple` | `true` 면 여러 개 선택. 빼면 하나 선택 |
| `options.labelIndex` | 지금 값의 표시 글자가 들어 있는 행의 키. 목록을 받기 전에도 셀에 이름을 보여 줄 때 넣음. 여러 개 선택이면 값과 같은 순서의 배열 (`owners: ['kcs']`, `ownerNames: ['김철수']`). 빼면 값이 그대로 보임 |

돌려주는 컬럼 옵션은 `typeRepresentation`, `editableFlag: true`, `editionPolicy: 'EditionOnClick'`, `getCellValue`, `getCellSemantics` 입니다. `editionPolicy` 를 바꾸려면 `Object.assign` 의 세 번째 인자로 덮어씁니다. 컬럼마다 따로 부릅니다 (한 번 부른 결과를 두 컬럼에 같이 쓰지 않음).

- 목록은 `['M6', 'M8']` 또는 `[{ label: '작업중', value: 'WIP' }]` 모양입니다.
- 열 때마다 `request` 를 부릅니다. 같은 조건이면 한 번만 부르게 하려면 `request` 를 `_.memoize` 로 감쌉니다. 이미 받은 목록이면 열자마자 전체 목록이 보입니다.

```js
const requestItemsOnce = _.memoize(function (row) {
  return requestItems(row.category);
}, function (row) {
  return row.category;
});
```

- 하나 선택에서 받은 목록에 지금 값이 없으면 목록 끝에 지금 값을 덧붙이고, 값이 `''` 나 `null` 이면 편집기에는 `undefined` 로 넘깁니다. 둘 다 [첫 항목 문제](#콤보-셀의-값이-비었거나-목록에-없을-때) 를 피하기 위해서입니다.
- 같은 셀을 연달아 열어서 응답 순서가 뒤바뀌어도 마지막에 연 요청의 목록만 씁니다.
- 여러 개 선택에서는 글자를 입력하면 입력이 멈춘 뒤에 `request` 가 그 글자와 함께 다시 불립니다. 글자를 무시하고 전체 목록을 돌려줘도 편집기가 입력한 글자로 걸러서 보여 줍니다.
- 행에는 코드가 저장됩니다 (`status: 'DONE'`, `owners: ['kcs', 'lyh']`). `labelIndex` 로 준 키는 읽기만 하고 바꾸지 않습니다. 고른 뒤의 표시 글자는 받은 목록에서 찾습니다.
- `request` 가 실패하면 목록은 지금 값만 든 채로 남습니다. 알림은 `request` 안의 `.catch` 에서 띄웁니다.
- 다른 컬럼에서 입력 중이던 글자는 응답이 와도 그대로 남습니다 (그 컬럼만 다시 그리기 때문).

## 패턴 6. 보이는 행만 채우기

행이 수천 개인데 값 조회가 느리면, 화면에 보이는 행만 요청합니다. 그리드 옵션 `onDelayedModelDataRequest` 가 "지금 몇 번째 행부터 몇 번째 행까지 보인다" 를 알려 줍니다. 불리는 때와 `info` 의 내용은 [서버 사이드 데이터 › 예제 6](/datagridview/server-side#예제-6-대안-보이는-행의-값만-나중에-채우기) 에 있습니다.

```js
const requested = new WeakSet();

const grid = new DataGridView({
  treeDocument: treeDocument,
  columns: [
    { text: '번호', dataIndex: 'id' },
    { text: '재고', dataIndex: 'stock', typeRepresentation: 'integer' }
  ],
  onDelayedModelDataRequest: function (info) {
    const nodes = info.collectionView.model
      .slice(info.startNodeIndex, info.endNodeIndex + 1)
      .filter(function (node) { return !requested.has(node); });
    if (nodes.length === 0) {
      return;
    }
    nodes.forEach(function (node) {
      requested.add(node);
    });
    requestStocks(nodes.map(function (node) { return node.options.grid.id; })).then(function (stocks) {
      const stockById = {};
      stocks.forEach(function (stock) {
        stockById[stock.id] = stock.stock;
      });
      treeDocument.withTransactionUpdate(function () {
        nodes.forEach(function (node) {
          node.updateOptions({ grid: { stock: stockById[node.options.grid.id] } });
        });
      });
    });
  }
});
```

- 콜백은 스크롤이 멈출 때, 크기가 바뀔 때마다 불리고 연달아 오기도 합니다. `requested` 로 이미 요청한 행을 거릅니다. 요청 표시를 행 데이터가 아니라 `WeakSet` 에 두므로 행 데이터에는 받은 값만 들어갑니다.
- 실제 앱은 이 콜백을 200 ~ 1,000 ms 의 debounce 로 감쌉니다 (FolderEditor 200 ms, ENOXEngineer, ConfiguratorPanel 1 초).
- 요청이 실패하면 `.catch` 에서 `requested.delete(node)` 로 표시를 지워야 다음에 다시 요청됩니다.
- 정렬, 필터, CSV 내보내기는 **채워진 행의 값만** 씁니다 ([값이 다 온 뒤에](#값이-다-온-뒤에-정렬-필터-그룹-csv)).

## 패턴 7. 입력한 글자로 서버에서 찾기

사람처럼 목록이 너무 커서 미리 받을 수 없으면, `possibleValues` 에 **함수**를 줍니다. 편집기가 열릴 때와 글자를 입력할 때 그 함수를 부르고, 돌려준 Promise 가 끝나면 목록을 띄웁니다. 기다리는 동안에는 편집기가 `로드 중...` 을 보여 줍니다. 타이밍 처리를 직접 할 것이 없습니다.

```js
const grid = new DataGridView({
  treeDocument: treeDocument,
  columns: [
    { text: '번호', dataIndex: 'id' },
    {
      text: '검토자',
      dataIndex: 'reviewers',
      typeRepresentation: 'string',
      editableFlag: true,
      editionPolicy: 'EditionOnClick',
      semantics: {
        allowMultipleValuesFlag: true,
        possibleValues: function (text) {
          return requestPersons(text);
        }
      }
    }
  ]
});
```

- 함수는 편집기를 열 때 빈 문자열(`''`)로 한 번, 글자를 입력하면 입력이 멈춘 뒤에 그 글자로 다시 불립니다. 실제 앱(ENOChgGovernanceUX)은 3 글자 미만이면 서버를 부르지 않고 끝냅니다.
- `typeRepresentation: 'string'` 에서만 됩니다. `customEnumMember` 컬럼에 함수를 주면 편집기를 열 때 `e.map is not a function` 오류가 납니다.
- 그리드는 목록을 미리 알 수 없으므로 셀에는 **저장된 값이 그대로** 보입니다. `{ label: '김철수', value: 'kcs' }` 를 돌려주면 `kcs` 가 저장되고 셀에도 `kcs` 가 보입니다. 이름 자체를 값으로 쓰면(`['김철수', '이영희']` 를 돌려줌) 이 예제 그대로 이름이 보입니다.
- 값은 코드로 저장하면서 이름을 보여 주려면 [패턴 5 의 여러 개 선택](#여러-개-선택) 방법을 씁니다 (`lazyComboColumn(request, { multiple: true })`).

## 패턴 8. 컬럼을 받아서 만들기

컬럼 정의가 서버에서 오면, 받은 뒤에 그리드를 만드는 것이 가장 단순합니다 ([먼저: 기다렸다가 만들 수 있으면](#먼저-기다렸다가-만들-수-있으면)). 그리드를 먼저 띄워야 하면 만든 뒤에 바꿉니다.

```js
const grid = new DataGridView({
  treeDocument: treeDocument,
  columns: [{ text: '번호', dataIndex: 'id' }]
});

requestColumns().then(function (columns) {
  grid.columns = [{ text: '번호', dataIndex: 'id' }].concat(columns);
});
```

- 전체를 바꿀 때는 `grid.columns = 새 배열`, 하나를 붙일 때는 `grid.addColumnOrGroup(컬럼 정의)` 입니다 ([그리드 메서드 › 컬럼](/datagridview/api/grid-methods#컬럼)).
- 컬럼을 바꾸면 그 순간 편집 중이던 셀의 편집이 끝나고, 입력 중이던 글자는 버려집니다. 컬럼은 화면을 열 때 한 번만 바꿉니다.
- 실제 앱(PCsBulkViewPresenter)은 컬럼에 넣을 데이터가 도착한 뒤에 `addColumnOrGroup` 으로 그 컬럼을 붙입니다.
- 컬럼 안의 목록이나 편집 가능 여부만 나중에 정해지는 것이면 컬럼을 바꾸지 말고 패턴 1 처럼 컬럼 함수가 변수를 읽게 합니다.

## 응답이 왔을 때 사용자가 입력 중이면

응답은 사용자가 다른 셀에 글자를 치고 있는 중에도 옵니다. 그때 무엇을 부르느냐에 따라 입력 중인 글자가 남거나 사라집니다. 품명 셀에 글자를 입력하는 도중에 아래를 불러서 확인한 결과입니다.

| 응답을 받고 부른 것 | 입력 중이던 글자 |
| --- | --- |
| `node.updateOptions(...)`, `node.setAttribute(...)` 로 **다른 컬럼** 값을 바꿈 (같은 행이든 다른 행이든) | 남음 |
| 위 호출 여러 개를 `withTransactionUpdate`, `prepareUpdate()` ~ `pushUpdate()` 로 묶음 | 남음 |
| `grid.updateColumnView(다른 컬럼, { updateCellContent: true })` | 남음 |
| `grid.updateView()` (인자 없음) | 남음. 대신 셀 내용도 다시 읽지 않음 |
| `node.updateOptions(...)` 로 **입력 중인 그 칸**의 값을 바꿈 | 서버 값으로 바뀜 |
| `grid.updateView({ updateCellContent: true })` | 사라짐 (원래 값으로 돌아감) |
| `grid.updateRowView(그 행, ...)`, `grid.updateColumnView(그 컬럼, ...)`, `grid.updateCellView(그 셀, ...)` 에 `updateCellContent: true` | 사라짐 |
| 행 추가(`addRoot`, `addChild`), 행 삭제(`remove`) | 사라짐 |
| 정렬로 행 순서가 바뀜 (`grid.sortModel = ...`, `grid.reapplySortModel()`). 순서가 그대로면 남음 | 사라짐 |
| `grid.layout.applyColumns()` | 사라짐 |
| `grid.columns = ...`, `grid.addColumnOrGroup(...)` | 사라짐. 편집도 끝남 |

"사라짐" 은 편집기가 열린 채로 글자만 원래 값으로 돌아간다는 뜻입니다. 사용자는 치던 글자가 갑자기 지워진 것으로 봅니다.

그래서 응답마다 `grid.updateView({ updateCellContent: true })` 를 부르는 방식은 피합니다. 행 3 개의 응답이 따로 오면 화면 전체를 3 번 다시 그리고, 그때마다 사용자가 치던 글자가 지워집니다. 값은 `updateOptions` 로, 목록은 그 컬럼의 `updateColumnView` 로 반영하면 위 표의 "남음" 쪽만 쓰게 됩니다.

## 조회를 다시 할 때 (늦게 온 응답)

조회 버튼을 연달아 누르면 요청 두 개가 같이 떠 있게 됩니다. 먼저 보낸 요청이 더 느리면, 그 응답이 **나중에** 와서 새 결과를 덮어씁니다. 요청할 때 번호를 매기고, 응답이 왔을 때 마지막 번호가 아니면 버립니다.

```js
let lastRequest = 0;

function search(keyword) {
  const myRequest = ++lastRequest;
  return requestParts(keyword).then(function (parts) {
    if (myRequest !== lastRequest) {
      return;
    }
    treeDocument.prepareUpdate();
    treeDocument.empty();
    parts.forEach(function (part) {
      treeDocument.addRoot(new TreeNodeModel({ label: part.id, grid: part }));
    });
    treeDocument.pushUpdate();
  });
}
```

- [보조 함수 모음](/datagridview/utils) 의 `GridUtils.loadRows(grid, 요청)` 에는 이 처리가 들어 있습니다. 겹쳐 부르면 마지막에 부른 것만 반영합니다.
- 행을 통째로 바꾼 뒤에, 바꾸기 전 행에 대한 응답(패턴 2 의 재고)이 늦게 올 수 있습니다. 이미 없어진 행에 `updateOptions` 를 불러도 오류는 나지 않고 화면에도 아무 일이 없습니다. 행이 아직 그리드에 있는지는 `node.getTreeDocument()` 로 알 수 있습니다 (없어진 행은 `undefined`).
- 화면을 닫아 `grid.destroy()` 를 부른 뒤에 응답이 와서 `updateOptions`, `updateView`, `updateColumnView` 를 불러도 오류는 나지 않습니다.
- 같은 행을 다시 요청하는 경우(패턴 4 를 행 데이터로 구현할 때 등)에는 응답이 왔을 때 **요청할 때의 조건이 아직 그대로인지** 행에서 다시 읽어 확인합니다.

```js
function fillPrice(node) {
  const currency = node.options.grid.currency;
  return requestPrice(node.options.grid.id, currency).then(function (price) {
    if (node.options.grid.currency === currency) {
      node.updateOptions({ grid: { price: price } });
    }
  });
}
```

## 값이 다 온 뒤에 (정렬, 필터, 그룹, CSV)

정렬, 필터, 행 그룹핑은 **적용한 순간의 값**으로 계산하고, 값이 나중에 들어와도 다시 계산하지 않습니다. 재고가 비어 있을 때 재고로 정렬해 두면, 재고가 채워진 뒤에도 행 순서는 그대로입니다. 값이 다 온 뒤에 한 번 다시 적용합니다.

| 기능 | 다시 적용 |
| --- | --- |
| 정렬 | `grid.reapplySortModel()` (`grid.sortModel` 에 같은 값을 다시 넣는 것으로는 다시 정렬되지 않음) |
| 컬럼 필터 | `treeDocument.getFilterManager().reapplyPropertyFilterModel('status')` (인자는 필터 키) |
| 행 그룹핑 | `grid.reapplyRowGrouping()` |

```js
Promise.all([fillingStocks, fillingOwners]).then(function () {
  grid.reapplySortModel();
});
```

`fillingStocks`, `fillingOwners` 는 값을 채우는 호출이 돌려준 Promise 입니다 (패턴 2 의 `requestStocks(ids).then(...)` 결과).

- 다시 정렬해서 행 순서가 바뀌면 입력 중이던 글자가 사라집니다 ([위 표](#응답이-왔을-때-사용자가-입력-중이면)). 응답마다 부르지 말고 다 온 뒤에 한 번만 부릅니다.
- `grid.getAsCSV()` 는 부른 순간에 들어 있는 값만 내보냅니다. 아직 안 온 값은 빈 칸으로 나갑니다. 패턴 6 의 `'OnExport'` 도 콜백을 부르기만 하고 응답을 기다리지 않습니다.

## 변경 트랜잭션 모드와 같이 쓸 때

[편집을 모아서 한 번에 저장](/datagridview/save-and-reflect#편집을-모아서-한-번에-저장-변경-트랜잭션-모드) 하는 화면에서는 `updateOptions` 로 바꾼 값도 "변경됨" 으로 기록됩니다. 비동기로 채운 값이 기록되면 사용자가 손대지 않은 행이 저장 대상에 들어갑니다.

| 채우는 키의 상태 | 채웠을 때 |
| --- | --- |
| 행에 그 키가 없음 | 기록되지 않음 |
| 키가 있음 (`''`, `undefined` 로 넣어 둔 것 포함) | "변경됨" 으로 기록됨 |

- 그래서 나중에 채울 키는 행을 만들 때 넣지 않습니다 (규칙 3).
- 이미 있는 키를 다시 채워야 하면(값 새로 고침) 그동안 기록을 끕니다. 사용자가 고쳐 둔 다른 변경은 그대로 남습니다. 실제 앱(ENXSBGridConnector)도 지연 응답을 넣기 전에 `setUseChangeTransactionMode(false)` 를 부릅니다.

```js
treeDocument.setUseChangeTransactionMode(false);
node.updateOptions({ grid: { stock: 50 } });
treeDocument.setUseChangeTransactionMode(true);
```

- 사용자가 이미 고친 키를 서버 값으로 다시 채우면 고친 값이 사라집니다. `node.hasReferenceValue('title')` 가 참이면 사용자가 고친 키이므로 건너뜁니다.
- 패턴 3, 4, 5 의 목록은 행 데이터가 아니라 변수에 있으므로 변경 기록과 관계가 없습니다.

## 콤보 셀의 값이 비었거나 목록에 없을 때

비동기와 관계없이 `customEnumMember` 콤보 셀에 있는 원본 동작입니다. 목록을 나중에 받으면 "지금 값이 목록에 없는" 순간이 생기기 쉬워서 여기에 적습니다. 목록이 `['N6', 'N8']` 인 콤보 셀에서 확인한 결과입니다.

| 셀 값 | 셀에 보이는 것 | 첫 항목(`N6`)을 고르면 |
| --- | --- | --- |
| 목록에 있는 값 (`'N8'`) | `N8` | 저장됨 |
| `undefined` (키는 있음) | 빈 칸 | 저장됨 |
| `''` (빈 문자열) | 빈 칸 | **저장 안 됨.** 둘째 항목부터는 저장됨 |
| `null` | 첫 항목 `N6` 이 보임 | 저장 안 됨 |
| 목록에 없는 값 (`'M10'`) | 첫 항목 `N6` 이 보임 | 저장 안 됨 |

콤보가 "첫 항목이 이미 골라져 있다" 고 여겨서, 첫 항목을 골라도 바뀐 것이 없다고 보기 때문입니다. 화면에는 고른 값이 보이지만 행 데이터는 그대로입니다.

- 값을 비울 때는 `''` 가 아니라 `undefined` 를 넣습니다 (`node.updateOptions({ grid: { item: undefined } })`). 서버가 빈 문자열을 주면 행을 만들 때 `undefined` 로 바꿉니다.
- `''` 를 그대로 둬야 하면 컬럼에 `allowResetToUndefinedByUIFlag: true` 를 주거나, 목록에 `{ label: '(없음)', value: '' }` 항목을 넣습니다. 둘 다 첫 항목이 저장됩니다.
- 지금 값이 목록에 없을 수 있으면 목록에 지금 값을 덧붙여서 줍니다. 실제 앱(ENXSBGridConnector)도 `getCellSemantics` 에서 지금 값이 목록에 없으면 목록에 넣습니다.
- 덧붙인 항목은 사용자가 다른 값을 고른 **직후에 바로 빼지 않습니다.** 고르는 동작 도중에 목록 내용이 바뀌면 원본 목록 화면에서 `Cannot read properties of null (reading 'options')` 오류가 납니다 (값은 저장됨). 다음에 목록을 새로 받을 때 정리합니다.

## 자주 겪는 문제

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 응답을 받았는데 셀이 그대로 | `node.options.grid.stock = 값` 처럼 직접 대입함 | `node.updateOptions({ grid: { stock: 값 } })` |
| 변수를 바꾸고 `grid.updateView()` 를 불렀는데 셀이 그대로 | 인자 없이 부르면 셀 내용을 다시 읽지 않음 (CSS 클래스만 다시 계산) | 그 컬럼만 `grid.updateColumnView(컬럼 번호, { updateCellContent: true })` |
| 셀에 `{}` 가 보임 | `getCellValue` 가 `async` 함수 (Promise 를 돌려줌) | 컬럼 함수는 받아 둔 값을 읽기만 함 |
| 막아야 할 셀이 항상 편집됨 | `getCellEditableState` 가 `async` 함수. Promise 는 항상 참 | 권한을 행 데이터에 받아 두고 읽음 (패턴 2) |
| 콤보 셀에 `Default` 가 보임 | `customEnumMember` 컬럼에 `possibleValues` 가 없음 | 목록이 오기 전에는 `possibleValues: []` |
| 값이 다른 행에 들어감 | 요청 전에 잡아 둔 `rowID`, `cellID` 로 넣음. 정렬, 필터, 펼침, 행 추가로 번호가 바뀜 | `nodeModel` 을 들고 있다가 `nodeModel.updateOptions(...)` |
| 응답 뒤에 읽은 행 값이 옛 값 | 요청 전에 `const row = node.options.grid` 로 잡아 둠. `updateOptions` 는 `options.grid` 를 새 객체로 바꿈 | 응답 뒤에 `node.options.grid` 를 다시 읽음 |
| 입력하던 글자가 지워짐 | 응답마다 `updateView({ updateCellContent: true })`, 행 추가, 정렬을 부름 | [위 표](#응답이-왔을-때-사용자가-입력-중이면) 의 "남음" 쪽으로 반영 |
| `editableFlag: true` 인데 편집기가 안 열림 | 그 행에 그 컬럼의 키가 없음 | 값이 들어오면 열림. 빈 값으로도 편집하게 하려면 키를 넣어 둠 (글자 칸은 `''`, 콤보는 `undefined`) |
| 콤보에서 첫 항목을 골라도 저장되지 않음, 셀에 고르지 않은 첫 항목이 보임 | 셀 값이 `''`, `null` 이거나 목록에 없음 | [콤보 셀의 값이 비었거나 목록에 없을 때](#콤보-셀의-값이-비었거나-목록에-없을-때) |
| 여러 개 선택 칸을 열었는데 목록이 비어 있음 | 목록이 오기 전에 편집기를 엶. 이 편집기는 열린 동안 목록을 바꿔도 반영되지 않음 | 목록이 올 때까지 `getCellEditableState` 로 막거나(패턴 3), `possibleValues` 에 함수를 줌(패턴 5) |
| 드롭다운을 열 때마다 서버에서 목록을 받고 싶음 | - | `preDropdown` 이벤트에서 요청하고 그 컬럼을 다시 그림 (패턴 5) |
| 손대지 않은 행이 "변경됨" | 변경 트랜잭션 모드에서 이미 있는 키를 채움 | [변경 트랜잭션 모드와 같이 쓸 때](#변경-트랜잭션-모드와-같이-쓸-때) |
| 값이 왔는데 정렬 순서, 필터 결과, 그룹이 안 맞음 | 값이 들어와도 다시 계산하지 않음 | [값이 다 온 뒤에](#값이-다-온-뒤에-정렬-필터-그룹-csv) 다시 적용 |
| 행이 많을 때 채우는 동안 화면이 멈춤 | 행마다 `updateOptions` 를 따로 부름 | `withTransactionUpdate` 로 묶음. `grid.prepareUpdateView()` ~ `pushUpdateView()` 로는 빨라지지 않음 |
| 조회를 연달아 하면 이전 결과가 남음 | 느린 응답이 나중에 와서 덮어씀 | [요청 번호 비교](#조회를-다시-할-때-늦게-온-응답) |
| 같은 목록을 행 수만큼 요청함 | 행마다 요청함 | 키별로 한 번만 요청 (패턴 3, 4 의 `ownerRequests`) |

## 확인 상태

- 2025, 2026 원본 webapps 를 ENOVIA 서버 없이 내 PC 에서 띄우고(Edge, 한국어 화면), 실제 DataGridView 에서 이 페이지의 예제와 표를 실행해 확인했습니다. 두 버전의 결과는 같았습니다. API 는 정해진 시간 뒤에 답하는 가짜 함수로 대신했습니다.
- 샘플 `asyncColumn.js` 는 응답 순서를 바꿔 가며(목록이 행보다 먼저, 모두 동시에, 값이 목록보다 먼저) 실행해 마지막 화면이 같은지, 응답이 오는 동안 입력하던 글자가 남는지, 변경 트랜잭션 모드에서 채운 값이 변경으로 기록되지 않는지와 `cancelChanges()` 로 분류와 품목이 같이 돌아오는지를 확인했습니다.
- 도우미 `lazyComboColumn.js` 는 하나 선택(값과 이름이 다른 목록, 글자 목록, 앞 칸에 따라 달라지는 목록), 여러 개 선택(칩 이름, 글자 입력 후 다시 요청, 저장되는 값)을 실제 마우스 조작으로 확인했습니다. 지금 값이 받은 목록에 없을 때와 빈 값일 때 첫 항목을 고르는 경우, 같은 셀을 연달아 열어 응답 순서가 뒤바뀌는 경우, 응답 전에 스크롤해서 그 행이 화면 밖으로 나간 경우, 응답이 오는 동안 다른 칸에 입력하는 경우, `_.memoize` 로 감싼 경우도 확인했습니다.
- 콤보 셀의 첫 항목 문제는 비동기 없이 고정 목록으로도 두 버전에서 같게 나왔습니다.
- 속도는 이 PC 에서 잰 값입니다 (3,000 행, 컬럼 2 개).
- 확인하지 않은 것: ENOVIA 서버와 3DDashboard 위젯 안에서의 실행, 실제 3DSpace API 의 지연과 실패, 터치 모드, `dataSource` 로 행을 나눠 불러오는 그리드, 행 그룹핑 중의 패턴 1 ~ 5, 패턴 5 에서 키보드로 드롭다운을 여는 경우와 `editionPolicy` 가 `EditionOnOver`, `EditionInPlace` 인 경우.

## 같이 보기

- 컬럼 함수 전체 → [컬럼 함수](/datagridview/cell-functions), [컬럼 함수 레퍼런스](/datagridview/api/column-functions)
- 저장 결과 반영, 변경 트랜잭션 모드 → [저장 후 화면 반영](/datagridview/save-and-reflect)
- 행을 나눠 불러오기 → [서버 사이드 데이터](/datagridview/server-side), 펼칠 때 하위 행 불러오기 → [트리 구조](/datagridview/tree#펼칠-때-하위-행-불러오기)
- 화면 갱신 함수 → [그리드 메서드 › 화면 갱신](/datagridview/api/grid-methods#화면-갱신)
- 서버 호출을 Promise 로 감싸기 → [서버 호출 (WAFData)](/uwa/request)

## 전체 소스

`<3DSpace>/webapps/Recipes/asyncColumn/` 에 두면 모듈 이름이 `DS/Recipes/asyncColumn/asyncColumn` 이 됩니다. 위젯 html 은 [asyncColumn.html](https://github.com/LeeOuHyoun/3d-docs/blob/main/Recipes/asyncColumn/asyncColumn.html) 입니다. `respond(값, 지연)` 으로 만든 `request...` 함수들이 가짜 API 이고, 이 자리를 실제 서버 호출로 바꿉니다. lodash 는 프로젝트에 넣어 둔 모듈 이름(`DS/lodash-4.18.1/lodash`)으로 불러옵니다 ([보조 함수 모음 › 불러오기](/datagridview/utils#불러오기)).

::: details asyncColumn.js

<<< @/../Recipes/asyncColumn/asyncColumn.js

:::

`lazyComboColumn.js` 는 `<3DSpace>/webapps/Recipes/lazyComboColumn/` 에 두고 `DS/Recipes/lazyComboColumn/lazyComboColumn` 으로 불러옵니다. 모듈이 돌려주는 것은 함수 하나(`lazyComboColumn(request, options)`)입니다. 실행해 볼 위젯 html 은 [lazyComboColumn.html](https://github.com/LeeOuHyoun/3d-docs/blob/main/Recipes/lazyComboColumn/lazyComboColumn.html) 입니다.

::: details lazyComboColumn.js

<<< @/../Recipes/lazyComboColumn/lazyComboColumn.js

:::
