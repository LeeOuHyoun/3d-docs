---
title: 콤보박스에 서버 목록 넣기
---

# 콤보박스에 서버 목록 넣기 <Badge type="tip" text="로컬 실행 확인" />

콤보박스의 목록을 서버(API)에서 받아 넣는 방법입니다. 화면마다 꼭 나오는 일이라, 가장 간단한 방법 하나씩만 적었습니다. 단독 콤보박스(`DS/Controls/ComboBox`)와 DataGridView 셀의 콤보를 같이 다룹니다.

::: info 근거
Controls.js 의 ComboBox (`elementsList`, `value`, `reachablePlaceholderFlag`, `_showPopup` 이 여는 직전에 내는 `preDropdown`), Controls.js 의 Abstract (`fireOnlyFromUIInteractionFlag`), Tweakers.js 의 enum 편집기 (그리드 셀 콤보가 같은 ComboBox 를 쓰고, 값이 `undefined` 일 때만 `reachablePlaceholderFlag` 를 켬), DataGridView.js 의 updateColumnView / getColumnOrGroup, CollectionView.js 의 addEventListener. 호출부: ENOXEngineer (`preDropdown` 에서 목록 조정), ENXSBGridConnector (지금 값이 목록에 없으면 목록에 넣음). 동작은 2025, 2026 원본 webapps 를 내 PC 에서 띄워 실제 컨트롤을 마우스로 조작해 확인했습니다 ([확인 상태](#확인-상태)).
:::

## 한눈에 보기

| | 화면을 열 때 한 번 받기 | 드롭다운을 열 때 받기 |
| --- | --- | --- |
| **단독 콤보박스** | `combo.elementsList = 목록` → `combo.value = 값` ([예제](#화면을-열-때-한-번-받기)) | `preDropdown` 이벤트에서 같은 대입 ([예제](#드롭다운을-열-때-받기)) |
| **그리드 셀 콤보** | 컬럼의 `semantics` 를 바꾸고 `grid.updateColumnView(...)` ([예제](#화면을-열-때-한-번-받기-1)) | `lazyComboColumn(request)` ([예제](#드롭다운을-열-때-받기-1)) |

아래 예제에서 `requestStatuses()`, `requestAllowedStatuses()`, `requestOwners()`, `requestItems()` 는 이미 갖고 있는 서버 호출 함수이고 Promise 를 돌려준다고 가정합니다. 목록은 `[{ label: '작업중', value: 'WIP' }, ...]` 또는 `['BOLT', 'NUT']` 모양입니다.

## 꼭 알아 둘 세 가지

**1. 목록이 먼저, 값은 그 다음입니다.** 목록이 비어 있을 때 넣은 `value` 는 버려집니다. 생성자에 넣어도 같습니다. 목록을 넣은 **뒤에** 값을 넣습니다.

**2. 콤보박스는 "선택 없음" 을 허용해 두지 않으면 첫 항목을 고른 것으로 칩니다.** 그래서 목록을 넣는 순간 첫 항목이 선택되고 `change` 가 납니다. 사용자가 그 첫 항목을 골라도 "바뀐 것이 없다" 고 봅니다.

| 어디 | "선택 없음" 을 허용하는 방법 |
| --- | --- |
| 단독 콤보박스 | 만들 때 `reachablePlaceholderFlag: true` (검색을 끈 `enableSearchFlag: false` 콤보에서 꼭 필요) |
| 그리드 셀 콤보 | 빈 값을 `''` 가 아니라 `undefined` 로 넣음 (그리드는 셀 값이 `undefined` 일 때 위 옵션을 켬) |

**3. 목록을 바꾸면 선택은 값이 아니라 "몇 번째" 로 남습니다.** 두 번째 항목을 골라 둔 콤보의 목록을 바꾸면, 새 목록의 두 번째 항목이 고른 것이 됩니다. 목록을 바꾼 뒤에는 값을 다시 넣습니다.

## 단독 콤보박스

서버 목록을 넣는 콤보박스는 아래 두 옵션을 같이 넣어 만듭니다.

| 옵션 | 넣으면 | 빼면 |
| --- | --- | --- |
| `reachablePlaceholderFlag: true` | 목록을 넣어도 아무것도 선택되지 않음. `combo.value = undefined` 로 선택을 비울 수 있음 | `enableSearchFlag: false` 로 만든 콤보는 목록을 넣는 순간 첫 항목이 선택되고, 선택을 비울 수도 없음 |
| `fireOnlyFromUIInteractionFlag: true` | `change` 가 **사용자가 골랐을 때만** 남 | 코드에서 목록이나 값을 넣을 때도 `change` 가 남 |

`enableSearchFlag` (입력해서 목록 거르기)가 기본값 `true` 인 콤보는 `reachablePlaceholderFlag` 가 저절로 켜집니다. 검색을 끈(`enableSearchFlag: false`) 콤보에서만 꺼져 있어서 첫 항목이 선택됩니다. 어느 쪽이든 같게 동작하도록 직접 넣어 둡니다.

### 화면을 열 때 한 번 받기

```js
const status = new ComboBox({
  placeholder: '상태 선택',
  reachablePlaceholderFlag: true,
  fireOnlyFromUIInteractionFlag: true
}).inject(container);

status.addEventListener('change', function () {
  console.log(status.value);
});

requestStatuses().then(function (statuses) {
  status.elementsList = statuses;
  status.value = 'REVIEW';
});
```

- 응답이 오기 전에는 안내 글자(`placeholder`)만 보입니다. 그동안 사용자가 열면 빈 목록이고, 응답이 오면 열린 채로 목록이 채워집니다.
- 저장된 값이 없으면 `status.value = ...` 줄을 뺍니다. 아무것도 선택되지 않은 채로 남습니다.
- 고른 값은 `status.value` (`'REVIEW'`), 보이는 글자는 `status.label` (`'검토중'`) 입니다.

### 드롭다운을 열 때 받기

`preDropdown` 은 목록이 열리기 직전에 납니다. 그때 요청하고, 응답이 오면 목록을 넣습니다.

```js
const owner = new ComboBox({
  elementsList: [{ label: '김철수', value: 'kcs' }],
  value: 'kcs',
  reachablePlaceholderFlag: true,
  fireOnlyFromUIInteractionFlag: true
}).inject(container);

let ownersLoading = null;

owner.addEventListener('preDropdown', function () {
  if (ownersLoading) {
    return;
  }
  ownersLoading = requestOwners().then(function (owners) {
    const value = owner.value;
    owner.elementsList = owners;
    owner.value = value;
  }).finally(function () {
    ownersLoading = null;
  });
});
```

- 목록을 받기 전에도 지금 값을 보여 주려면, 처음에 **지금 값 하나만 든 목록**을 넣어 둡니다 (위의 `elementsList`, `value`). 지금 값이 없으면 둘 다 빼고 `placeholder` 를 넣습니다.
- 목록은 바로 열리고, 응답이 오면 열린 채로 전체 목록으로 바뀝니다.
- 목록을 넣은 뒤 `owner.value = value` 로 값을 다시 넣습니다 (꼭 알아 둘 세 가지의 3 번).
- `preDropdown` 은 열려 있는 콤보에 목록을 넣을 때 **한 번 더** 납니다. `ownersLoading` 이 그것을 걸러서 요청이 두 번 나가지 않게 합니다.
- 이 예제는 열 때마다 새로 받습니다. 처음 열 때 한 번만 받으려면 `.finally(...)` 를 뺍니다.

### 앞 콤보에 따라 목록 바꾸기

"분류" 를 고르면 "품목" 목록이 바뀌는 경우입니다.

```js
const category = new ComboBox({
  elementsList: ['BOLT', 'NUT', 'WASHER'],
  placeholder: '분류',
  reachablePlaceholderFlag: true,
  fireOnlyFromUIInteractionFlag: true
}).inject(container);

const item = new ComboBox({
  placeholder: '품목',
  reachablePlaceholderFlag: true,
  fireOnlyFromUIInteractionFlag: true
}).inject(container);

category.addEventListener('change', function () {
  const selected = category.value;
  requestItems(selected).then(function (items) {
    if (category.value !== selected) {
      return;
    }
    item.elementsList = items;
    item.value = undefined;
  });
});
```

- `item.value = undefined` 로 앞에서 골랐던 품목을 비웁니다. 이 줄이 없으면 새 목록에서 같은 위치의 항목이 고른 것으로 남습니다.
- `category.value !== selected` 확인은 분류를 연달아 바꿨을 때를 위한 것입니다. 먼저 보낸 요청의 응답이 늦게 오면 버립니다.

## 그리드 셀 콤보

그리드 셀의 콤보는 컬럼의 `typeRepresentation: 'customEnumMember'` 와 `semantics.possibleValues` 로 만듭니다 ([셀 타입](/datagridview/cell-types#콤보박스-하나-선택)).

### 화면을 열 때 한 번 받기

모든 행이 같은 목록을 쓰는 경우입니다. 빈 목록으로 그리드를 먼저 띄우고, 응답이 오면 컬럼의 목록을 바꾼 뒤 그 컬럼을 다시 그립니다.

```js
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
      semantics: { possibleValues: [] }
    }
  ]
});

requestStatuses().then(function (statuses) {
  grid.getColumnOrGroup('status').semantics = { possibleValues: statuses };
  grid.updateColumnView(grid.layout.getColumnIndex('status'), { updateCellContent: true });
});
```

- 처음에 `possibleValues: []` 를 꼭 넣습니다. 빼면 셀에 값 대신 `Default` 가 보입니다.
- 응답이 오기 전에는 셀에 값(`WIP`)이 그대로 보이고, 온 뒤에 `작업중` 으로 바뀝니다.
- `updateColumnView` 는 그 컬럼만 다시 그립니다. 다른 컬럼에서 입력 중이던 글자는 그대로 남습니다.
- 목록을 다 받은 뒤에 그리드를 만들 수 있으면 그게 더 간단합니다. 받은 목록을 처음부터 `semantics: { possibleValues: statuses }` 에 넣으면 됩니다.

### 드롭다운을 열 때 받기

행마다 목록이 다르거나, 열 때마다 최신 목록이어야 하는 경우입니다. 보조 모듈 `lazyComboColumn` 이 돌려주는 컬럼 옵션을 컬럼 정의에 합칩니다. ENOVIA 원본 모듈이 아니므로 [lazyComboColumn.js](https://github.com/LeeOuHyoun/3d-docs/blob/main/Recipes/lazyComboColumn/lazyComboColumn.js) 를 프로젝트 webapps 에 넣어서 씁니다.

```js
const grid = new DataGridView({
  treeDocument: treeDocument,
  columns: [
    { text: '번호', dataIndex: 'id' },
    Object.assign({ text: '상태', dataIndex: 'status' }, lazyComboColumn(function (row) {
      return requestAllowedStatuses(row.id);
    }, { labelIndex: 'statusName' }))
  ]
});
```

- 넘기는 함수는 그 행의 데이터(`row`)를 받아 목록을 돌려줍니다. 사용자가 그 셀의 드롭다운을 열 때마다 불립니다.
- `labelIndex` 는 지금 값의 표시 글자가 들어 있는 행의 키입니다 (`{ status: 'WIP', statusName: '작업중' }`). 목록을 받기 전에도 셀에 `작업중` 이 보입니다. 값과 표시 글자가 같으면 뺍니다.
- 여러 개를 고르는 칸은 두 번째 인자에 `multiple: true` 를 넣습니다.
- 한 번 받은 목록을 다시 쓰는 방법, 직접 구현하는 방법(`preDropdown` 이벤트)은 [API 로 받아 넣기 › 패턴 5](/datagridview/async-values#패턴-5-드롭다운을-열-때-목록-받기) 에 있습니다.

### 앞 칸에 따라 목록 바꾸기

같은 모듈로 됩니다. 넘기는 함수가 그 행의 **지금** 값을 받으므로, 앞 칸 값으로 요청하면 됩니다. 앞 칸이 바뀌면 뒤 칸 값을 비우는 것만 따로 넣습니다.

```js
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
    Object.assign({ text: '품목', dataIndex: 'item' }, lazyComboColumn(function (row) {
      return requestItems(row.category);
    }))
  ]
});

treeDocument.onNodeModelUpdate(function (e) {
  const changed = e.data.attributes || {};
  if ('category' in changed && !('item' in changed)) {
    e.data.nodeModel.updateOptions({ grid: { item: undefined } });
  }
});
```

- 품목은 `''` 가 아니라 **`undefined`** 로 비웁니다. `''` 로 비우면 새 목록의 첫 항목을 골라도 저장되지 않습니다 (꼭 알아 둘 세 가지의 2 번).

## 자주 겪는 문제

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 목록을 넣었더니 고르지도 않은 첫 항목이 선택되고 `change` 가 남 | "선택 없음" 을 허용하지 않은 콤보 (`enableSearchFlag: false` 로 만든 경우) | `reachablePlaceholderFlag: true` |
| `combo.selectedIndex = -1` 이나 `combo.value = undefined` 로 비워지지 않음 | 위와 같음 | `reachablePlaceholderFlag: true` |
| `value` 를 넣었는데 선택되지 않음 | 목록보다 먼저 넣음 | 목록을 넣은 뒤에 값을 넣음 |
| 목록을 바꿨더니 엉뚱한 항목이 선택됨 | 선택이 "몇 번째" 로 남음 | 바꾼 뒤에 `combo.value = 값` (비우려면 `undefined`) |
| 목록, 값을 넣을 때마다 `change` 처리 코드가 돎 | 코드에서 바꿔도 `change` 가 남 | `fireOnlyFromUIInteractionFlag: true` |
| 열 때 받는데 요청이 두 번 나감 | 열린 콤보에 목록을 넣으면 `preDropdown` 이 한 번 더 남 | 받는 중이면 무시 (위 예제의 `ownersLoading`) |
| 그리드 콤보에서 첫 항목을 골라도 저장되지 않음 | 셀 값이 `''`, `null` 이거나 목록에 없는 값 | 빈 값은 `undefined`, 지금 값은 목록에 넣음 ([자세히](/datagridview/async-values#콤보-셀의-값이-비었거나-목록에-없을-때)) |
| 그리드 콤보 셀에 `Default` 가 보임 | 컬럼에 `possibleValues` 가 없음 | 목록이 오기 전에는 `possibleValues: []` |
| 그리드에서 목록을 바꿨는데 셀이 그대로 | 다시 그리지 않음 | `grid.updateColumnView(컬럼 번호, { updateCellContent: true })` |
| 콤보 컨트롤을 꺼내 `elementsList` 를 직접 바꿨더니 그리드에 저장이 안 됨 | 그리드를 거치지 않음 | 컬럼의 `semantics` 를 바꾸고 `updateColumnView` |

## 확인 상태

- 2025, 2026 원본 webapps 를 ENOVIA 서버 없이 내 PC 에서 띄우고(Edge, 한국어 화면), 이 페이지의 예제를 그대로 실행한 뒤 마우스로 열고 골라서 확인했습니다. 두 버전의 결과는 같았습니다. API 는 정해진 시간 뒤에 답하는 가짜 함수로 대신했습니다.
- 확인하지 않은 것: ENOVIA 서버와 3DDashboard 위젯 안에서의 실행, 실제 3DSpace API, 키보드로 여는 경우, 터치 모드, 여러 개 선택(`multiSelFlag`) 단독 콤보박스에서 열 때 받기.

## 같이 보기

- 콤보박스 옵션과 이벤트 → [WebUX 컨트롤 › ComboBox](/controls/webux-controls#combobox), [API](/controls/api/webux-controls#combobox)
- 그리드에서 값, 목록, 권한을 나중에 넣는 모든 경우 → [API 로 받아 넣기 (비동기와 타이밍)](/datagridview/async-values)
- 사람처럼 목록이 너무 커서 입력한 글자로 찾아야 할 때 → [자동완성 입력](/controls/autocomplete#서버에서-찾기)
- 서버 호출을 Promise 로 감싸기 → [서버 호출 (WAFData)](/uwa/request)
