# 자동완성 입력 (WUXAutoComplete) <Badge type="tip" text="로컬 실행 확인" />

`DS/WUXAutoComplete/AutoComplete` 는 글자를 치면 후보 목록을 보여 주고 고르게 하는 입력 칸입니다. 하나만 고르기, 여러 개 고르기(칩), 목록에 없는 글자 받기(자유 입력), 서버 검색을 한 컨트롤로 합니다. WebUX 컨트롤(`DS/Controls/Abstract` 상속)이라 만들기, 속성 읽고 쓰기, 이벤트 받기는 [WebUX 컨트롤 공통 사용법](/controls/webux-controls#공통-사용법) 과 같습니다.

샘플: [09_autocomplete.html](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/ControlsSample/09_autocomplete.html) (배열로 하나 고르기, 서버 검색으로 여러 명 고르기, TreeDocument 후보, 자유 입력 태그, 코드로 값 넣고 읽기, 이벤트)

::: info 근거
WUXAutoComplete/WUXAutoComplete.js (2025, 2026) 의 publishedProperties, 후보 불러오기 함수(배열, TreeDocument, 함수, Promise 처리와 다시 부를지 정하는 부분), `_applyValue`, `_applySelectedItems`, `filterItems`, `_manageReturn`, `_delayPopupCreation`, 키 맵, `getDefaultChipInfo`. 2026 의 `customValidator`, `getValueForFiltering`, `acceptDrop`, `onDrop`, `configureCustomSeparator`, `retrieveMatchingPossibleValue`, `_getCellIDToPreselect`. TweakersEx.js 의 `AutoCompleteViewModuleTweakerString`, UIKIT.js 의 `DS/UIKIT/Autocomplete`. 호출부: ENOUserGroupMgmt (AutoCompleteUtil 의 사람 찾기, `selectedItems[i].options.grid.physicalId`), ENXMeetingMgmt, ENOChgGovernanceUX (`columnOptions`, `getChipInfo`), DMUReviewPersistence. 집계: 50 개 앱. 옵션 표의 "사용" 은 AutoComplete 를 불러오는 파일 54 개 중 그 이름이 나오는 파일 수입니다. 로컬 실행: 2025, 2026 webapps 를 `npm run samples` 로 띄워 Edge 로 확인
:::

## 하나만 고르기

<<< @/../ControlsSample/09_autocomplete.html#single{js}

::: warning 기본값은 "여러 개 + 자유 입력" 입니다
`multiSearchMode` 와 `allowFreeInputFlag` 의 기본값이 모두 `true` 입니다. 옵션 없이 만들면 칩으로 여러 개를 받고, 목록에 없는 글자도 값이 됩니다. 하나만 고르려면 `multiSearchMode: false`, 목록 값만 받으려면 `allowFreeInputFlag: false` 를 줍니다. OOTB 도 대부분 `allowFreeInputFlag: false` 를 줍니다.
:::

| 읽기 | 하나만 (`multiSearchMode: false`) | 여러 개 (기본) |
| --- | --- | --- |
| `value` | 고른 후보의 value. 후보의 value 가 `{ value: ... }` 객체면 그 안의 `value` | value 배열. 객체 value 는 객체 그대로 |
| `selectedItems` | TreeNodeModel **하나** (배열 아님) | TreeNodeModel 배열 |
| 비었을 때 | `undefined` | `[]` (한 번도 고르지 않았으면 `undefined`) |

- 고른 항목의 글자는 `selectedItems.options.label`, 함께 넘긴 데이터는 `selectedItems.options.grid` 입니다.
- `allowFreeInputFlag: false` 에서 목록에 없는 글자를 치고 Tab 이나 다른 곳을 누르면 칸 글자가 고른 값으로 되돌아가고 `change` 는 나지 않습니다.
- 하나만 모드의 오른쪽 X 버튼(`allowResetToUndefinedByUIFlag`, 기본 `true`)을 누르면 `value` 가 `undefined` 가 되고 `change` 가 납니다.
- 목록은 `body` 끝에 붙는 팝업(`div.wux-controls-autoComplete-popup`) 안의 DataGridView 입니다. 10 행이 넘으면 스크롤합니다.

배열 후보는 안에서 TreeNodeModel 로 바뀝니다. 이때 남는 키는 아래뿐입니다.

| 배열 항목 | TreeNodeModel `options` |
| --- | --- |
| `'작업 중'` (문자열, 숫자) | `label` 과 `value` 모두 그 글자 |
| `label` | `label`. 없으면 `value` (객체면 `value.label`) |
| `value` | `value`. 없으면 `label` |
| `icon`, `icons` | `icons` 배열 |
| `tooltip`, `typeRepresentation` | 그대로 |
| `grid` | 그대로. 고른 뒤 `node.options.grid` 로 읽음 |

::: warning 배열 항목의 다른 키는 버려집니다
`{ label, value, id: 'x', type: 'Part' }` 처럼 넘기면 `id`, `type`, `subLabel` 은 노드에 남지 않습니다. 고른 뒤에 읽을 데이터는 `grid` 에 넣거나 `value` 를 객체로 주거나, TreeNodeModel 을 직접 만들어 TreeDocument 로 넘깁니다.
:::

로컬 실행으로 확인한 것: 칸을 누르면 세 후보가 모두 보이고, "검" 을 치면 "검토 중" 만 남음. Enter 로 고르면 `value` 는 `'IN_REVIEW'`, `selectedItems` 는 배열이 아닌 노드, 칸에는 "검토 중". 목록에 없는 글자를 치면 목록에 "개체를 찾을 수 없음" 이 보이고, Tab 을 누르면 글자가 "검토 중" 으로 돌아감. X 버튼을 누르면 `value` 가 `undefined`. `{ label, value, extra, type, subLabel, grid, icon }` 을 넘기면 `grid` 와 `icons` 만 남음.

## 생성 옵션

| 옵션 | 사용 | 기본값 | 설명 |
| --- | --- | --- | --- |
| `elementsTree` | 52 | - | 후보. 배열, TreeDocument, 함수, Promise ([후보를 주는 방법](#후보를-주는-방법)) |
| `multiSearchMode` | 45 | `true` | 여러 개 고르기(칩). `false` 면 하나만 |
| `allowFreeInputFlag` | 44 | `true` | 목록에 없는 글자도 값으로 받음 |
| `placeholder` | 52 | "검색" (언어별) | 빈 칸 안내 글자 |
| `minLengthBeforeSearch` | 23 | `0` | 이 글자 수보다 짧으면 목록을 열지 않음. OOTB 는 `3` 이 가장 많음 |
| `typeDelayBeforeSearch` | 6 | `0` | 입력이 멈추고 이 시간(ms)이 지나야 찾음. OOTB 는 300 ~ 500 |
| `keepSearchResultsFlag` | 23 | `true` | 함수 후보에서 앞 결과를 다시 쓸지 ([언제 다시 부르나](#후보를-주는-방법)). 서버 검색 OOTB 는 `false` |
| `filterCB` | 15 | `label` 에 친 글자가 들어 있는 것만 남김 | `function (text)`. 목록 거르기. 미리 고를 행 번호(cellID)를 돌려줄 수 있음 |
| `defaultFilterOptions` | 1 | `{ property: 'label', type: 'contains' }` | 기본 `filterCB` 의 조건. `type` 은 `contains`, `startsWith`, `endsWith`, `equal`, `notContain`, `notEqual` (대소문자 무시) |
| `customFilterMessage` | 15 | "개체를 찾을 수 없음" (언어별) | 맞는 후보가 없을 때 목록에 보이는 글자 |
| `allowResetToUndefinedByUIFlag` | 16 | `true` | 하나만 모드에서 X(지우기) 버튼 |
| `displayCustomNodeInDropDown` | 3 | `true` | 자유 입력일 때 친 글자를 목록 맨 위 행(연필 아이콘)으로 보여 줌 |
| `getChipInfo` | 5 | `getDefaultChipInfo` | `function (node)`. 칩에 보일 `{ label, icon, tooltip }` |
| `columnOptions` | 5 | - | 목록 DataGridView 컬럼 정의에 합침. OOTB: `{ typeRepresentation: 'detailedUser', autoRowHeightFlag: true }`, `style: 'classic'` / `'triangle'` / `'none'` (트리 펼침 모양) |
| `popupSizeAdjustPolicy` | 7 | `AdjustToVisibleContents` | 목록 폭. `Popup.SizeAdjustPolicyEnum` 의 `NoAdjust` (칸 폭), `AdjustToVisibleContents` (보이는 행 내용), `AdjustToAllContents` (모든 행 내용) |
| `popupConstraint` | 0 | `'screen'` | 목록이 넘지 않을 영역 |
| `displayLoader` | 1 | `false` | 목록에 로딩 막대. 숫자를 주면 % 로 보임 |
| `value`, `selectedItems` | 39 | - | 처음 값 ([코드로 값 넣고 읽기](#코드로-값-넣고-읽기)) |

`disabled`, `visibleFlag`, `fireOnlyFromUIInteractionFlag`, `touchMode`, `tooltipInfos` 는 WebUX 공통입니다. 2026 에 생긴 옵션은 [2026 변경](#_2026-변경) 에 있습니다.

## 후보를 주는 방법

| `elementsTree` | 동작 |
| --- | --- |
| 배열 | 안에서 TreeDocument 를 만들어 씀 (위 변환 표) |
| TreeDocument | 그대로 씀. 그 TreeDocument 의 선택 목록(XSO)이 곧 고른 값 ([TreeDocument 후보](#treedocument-후보)) |
| 함수 `(text)` | 친 글자를 받아 부름. 배열, TreeDocument, Promise 를 돌려줌. 함수 안의 `this` 는 컨트롤 |
| Promise | 결과(배열 또는 TreeDocument)가 올 때까지 목록에 "로드 중..." |

함수는 목록을 열 때(칸을 누름, ↓, 글자를 침) 부릅니다. `minLengthBeforeSearch` 보다 짧은 글자로는 부르지 않습니다. 그 뒤 글자가 바뀔 때 다시 부를지는 `keepSearchResultsFlag` 로 정합니다.

| 글자 변화 | `keepSearchResultsFlag: true` (기본) | `false` |
| --- | --- | --- |
| 처음 열 때 (빈 칸이면 `''`) | 부름 | 부름 |
| 앞 검색어가 들어 있는 글자 (김 → 김철) | 부르지 않고 앞 결과 안에서 거름 | 부름 |
| 앞 검색어가 없는 글자 (김 → 이) | 부름 | 부름 |
| 앞 Promise 를 기다리는 중 | 앞 검색어가 없는 글자일 때만 부름 | 같음 |

- Promise 는 마지막에 부른 것의 결과만 씁니다. 늦게 온 앞 결과는 버립니다.
- Promise 가 실패(reject)하면 목록이 비고 `customFilterMessage` 가 보입니다.
- Promise 인지는 `instanceof Promise` 로 봅니다. 네이티브 Promise 를 돌려줍니다.

로컬 실행으로 확인한 것: 기본값에서 칸을 누르고 "김", "철", 지우고 "이" 를 치면 함수는 `''`, `'김'`, `'이'` 로 세 번 불림 ("김철" 에서는 안 불림).

## 서버에서 찾기

<<< @/../ControlsSample/09_autocomplete.html#search{js}

::: warning 서버가 찾아 준 후보가 목록에서 사라집니다
기본 `filterCB` 는 받은 결과도 `label` 에 친 글자가 들어 있는 것만 남깁니다. 서버가 아이디나 다른 속성으로 찾아 준 사람(label "김철수", 친 글자 "kcs")은 목록에서 숨겨지고 "개체를 찾을 수 없음" 이 보입니다. 서버가 이미 걸렀으면 `filterCB: function () {}` 로 거르기를 끄고 `keepSearchResultsFlag: false` 로 글자마다 다시 찾습니다. 앞 결과 안에서도 거르고 싶으면 OOTB 처럼 TreeDocument 에 이름과 label 을 같이 보는 필터를 걸어 `filterCB` 에서 씁니다.
:::

OOTB 의 사람 찾기 (ENOUserGroupMgmt, 그룹에 구성원 추가)는 이렇게 만듭니다.

| 단계 | 내용 |
| --- | --- |
| 옵션 | `multiSearchMode: true`, `allowFreeInputFlag: false`, `keepSearchResultsFlag: false`, `minLengthBeforeSearch: 3` |
| 검색 | `elementsTree` 함수가 Promise 를 돌려주고, 안에서 연합 검색(`/search`, POST, `isfederated`)을 부름. 조건은 `([ds6w:label]:(*글자*) OR [ds6w:identifier]:(*글자*)) AND (flattenedtaxonomies:"types/Person" AND current:"active")` |
| 결과 | `results[].attributes` 의 `resourceid`, `ds6w:label`, `ds6w:identifier` 로 TreeNodeModel 을 만들고 `options.grid = { physicalId, label, identifier }` 를 붙여 TreeDocument 로 resolve. 이미 고른 사람은 뺌 |
| 거르기 | `filterCB` 에서 이름 또는 label 에 글자가 있으면 남기는 필터(`BaseFilter` 상속)를 TreeDocument 에 걸음 |
| 읽기 | 추가 버튼에서 `selectedItems` 의 `item.options.grid.physicalId` 를 모아 서버로 보냄 |
| 옆 버튼 | 검색 아이콘을 누르면 3DSearch 를 열고, 고른 결과로 TreeNodeModel 을 만들어 `selectedItems` 에 이어 붙임 |

검색 서비스의 요청과 결과 형식은 [사람 찾기 (e6w)](/rest-api/e6w#사람-찾기-personsearch), 3DSearch 에서 골라 오기는 [3DSearch 에서 골라 오기](/widget/search) 에 있습니다. 사람 목록을 두 줄(이름, 아이디)로 보이려면 OOTB 처럼 `columnOptions.typeRepresentation` 을 `'detailedUser'` 로 줍니다 ([타입 표현](/datagridview/api/type-representation)).

로컬 실행으로 확인한 것: 한 글자에서는 목록도 검색도 없음. 두 글자를 치면 300ms 뒤 검색이 불리고 목록에 "로드 중..." 이 보였다가 결과가 뜸. Enter 로 고르면 칩이 생기고 `value` 는 `['kcs01']`, `selectedItems[0].options.grid` 는 넘긴 객체 그대로. "kc" → "kcs" 로 칠 때마다 검색이 다시 불림. `filterCB` 를 빼면 "kcs" 결과 "김철수 (설계1팀)" 이 목록에서 숨겨짐.

## TreeDocument 후보

<<< @/../ControlsSample/09_autocomplete.html#tree{js}

- 목록이 트리로 보입니다. 글자로 거르면 맞는 노드와 그 부모가 남습니다.
- 만들 때 TreeDocument 에 이미 선택된 노드가 있으면 그것이 처음 값이 됩니다.

::: warning TreeDocument 를 넘기면 선택을 같이 씁니다
TreeDocument 를 넘기면 AutoComplete 가 그 TreeDocument 의 노드를 `select()` / `unselect()` 해서 값을 정합니다. 그리드에 보여 주는 TreeDocument 를 그대로 넘기면 자동완성에서 고를 때마다 그리드 선택도 바뀝니다. 후보용 TreeDocument 를 따로 만듭니다.
:::

로컬 실행으로 확인한 것: 목록에 "기구, 브라켓, 샤프트, 전장, 커넥터, 하네스" 가 트리로 보이고, "샤" 를 치면 "기구, 샤프트" 만 남음. Enter 로 고르면 `value` 는 `'기구/샤프트'`, `categoryDoc.getSelectedNodes()` 는 "샤프트".

## 자유 입력과 칩

<<< @/../ControlsSample/09_autocomplete.html#free{js}

- 여러 개 모드는 고른 항목이 칸 안의 칩([SelectionChips](/controls/input-controls#selectionchips))이 되고, 칩의 X 로 뺍니다. 오른쪽 `⋮` 버튼은 SelectionChips 메뉴입니다.
- 칩 글자와 아이콘은 `getChipInfo(node)` 가 정합니다. OOTB 는 `label (아이디)` 로 바꿔 씁니다 (ENOChgGovernanceUX).
- 여러 개 모드에서 ↓ 로 행을 고른 뒤 Space 를 누르면 그 행을 고르거나 뺍니다.

::: warning 자유 입력이 켜져 있으면 Enter 는 친 글자를 넣습니다
`allowFreeInputFlag: true` 이고 `displayCustomNodeInDropDown: true` (둘 다 기본값)면 친 글자가 목록 맨 위 행으로 들어가고 그 행이 미리 골라집니다. "긴" 을 치고 Enter 를 누르면 목록의 "긴급" 이 아니라 "긴" 이 값이 됩니다. 목록 항목은 ↓ 로 옮긴 뒤 Enter 를 누르거나 클릭합니다.
:::

로컬 실행으로 확인한 것: "도면" 을 치고 Enter 를 누르면 칩 "도면" 이 생기고 `customNode` 는 `true`. "긴" 을 치면 목록이 "긴, 긴급" 이고 Enter 로 "긴" 이 들어감.

## 코드로 값 넣고 읽기

<<< @/../ControlsSample/09_autocomplete.html#api{js}

| 할 일 | 코드 | 결과 |
| --- | --- | --- |
| 값으로 고르기 | `ac.value = 'IN_REVIEW'` (여러 개 모드는 배열) | value 또는 label 이 같은 후보(대소문자 무시)를 찾아 고름. 고른 뒤 `value` 는 후보의 value 로 바뀜 (`'승인'` → `'RELEASED'`) |
| 없는 값 (하나만, 자유 입력 아님) | `ac.value = 'UNKNOWN'` | 무시하고 앞 값을 둠 |
| 없는 값 (자유 입력 또는 함수 후보) | `tags.value = ['도면 변경']` | `customNode: true` 인 노드로 붙음 |
| 노드로 고르기 | `ac.selectedItems = [new TreeNodeModel({ ... })]` | 후보에 없어도 붙음 (`customNode: true`). 콘솔에 "You tried to select a treeNode which is not attached to a tree document" 경고 |
| 비우기 | `ac.value = undefined`, `ac.selectedItems = []` | `change` 발생 |
| 치는 중인 글자 | `ac.getValueToCommit()` | 아직 확정하지 않은 칸 글자 |

- 코드로 바꿔도 `change` 가 납니다. 처음 값을 넣을 때 막으려면 `fireOnlyFromUIInteractionFlag: true` 를 줍니다 (그러면 사용자 조작의 `change` 만 옴).
- 저장할 때는 `value` 보다 `selectedItems` 의 `options.grid` 나 `options.value` 를 읽는 편이 안전합니다. 자유 입력 칩은 `options.customNode` 로 가립니다.

로컬 실행으로 확인한 것: "코드로 값 넣기" 를 누르면 `change` 세 번, 상태 "검토 중", 태그 "긴급"(후보) + "도면 변경"(customNode), 담당자 "외부 사용자 (협력사)"(customNode) 칩. "모두 비우기" 로 네 칸 모두 `change`. `fireOnlyFromUIInteractionFlag: true` 로 만든 칸은 코드로 값을 넣어도 `change` 0 번.

## 이벤트

<<< @/../ControlsSample/09_autocomplete.html#events{js}

| 이벤트 | 때 | `e.options` |
| --- | --- | --- |
| `change` | 고른 항목이 바뀜 (사용자, 코드 모두) | `oldValue`: 앞 value 배열. 2025 하나만 모드는 늘 `[]` |
| `uncommittedChange` | 글자를 칠 때마다 (확정 전). 여러 개 모드는 고른 뒤 칸이 비면서 한 번 더 | - |
| `show` / `hide` | 후보 목록이 열림 / 닫힘 | - |
| `popupVisibilityChange` | 목록 크기를 맞춘 뒤 실제로 보일 때 | - |

## 키보드와 목록 열기

| 키 | 동작 |
| --- | --- |
| ↓ ↑ PageDown PageUp Home End | 목록이 닫혀 있으면 엶. 열려 있으면 행 이동 |
| Enter | 미리 골라진 행을 확정. 행이 없으면 친 글자를 확정 |
| Space | 여러 개 모드에서 화살표로 고른 행을 고르거나 뺌 |
| Esc | 목록을 닫음. 하나만 모드는 칸 글자를 고른 값으로 되돌림 |
| Tab | 목록을 닫고 친 글자를 확정 (자유 입력이 아니면 되돌림) |

- 처음 포커스를 받을 때 후보를 보여 주는 옵션은 없습니다. 칸을 누르거나 ↓ 를 누르면 열립니다. 코드로는 `showPopup()` / `hidePopup()` 입니다.
- 목록이 열린 채 칸을 다시 누르면 닫힙니다.

로컬 실행으로 확인한 것: `input.focus()` 만으로는 목록이 열리지 않고 ↓ 를 누르면 열림. `showPopup()` 으로 세 후보가 보이고 `hidePopup()` 으로 닫힘. 포커스 이벤트 안에서 `showPopup()` 을 불러 보았지만 열리지 않았음.

## 메서드

| 메서드 | 하는 일 |
| --- | --- |
| `showPopup()` / `hidePopup()` | 목록 열기 / 닫기. `minLengthBeforeSearch` 보다 글자가 짧으면 열리지 않음 |
| `filterItems(text, options?)` | 후보 거르기. `options` 는 `defaultFilterOptions` 와 같은 모양. 직접 만든 `filterCB` 안에서 부름 |
| `getValueToCommit()` | 칸에 친 글자 |
| `getDefaultChipInfo(node)` | 기본 칩 정보. `getChipInfo` 안에서 고쳐 쓸 때 |
| `getTypeRepresentation(node)` | 행의 표현 타입 (노드 → `columnOptions` → `'string'`) |
| `hide()` | 컨트롤을 숨김 (열린 목록과 메뉴도 닫음) |
| `destroy()` | 안의 DataGridView, LineEditor, SelectionChips 까지 정리 |

## 비슷한 것과 고르기

| | WUXAutoComplete | Tweakers `string` + `possibleValues` | UIKIT `Autocomplete` | SelectionChips(Editor) |
| --- | --- | --- | --- | --- |
| 모듈 | `DS/WUXAutoComplete/AutoComplete` | TweakerString (안에서 이 컨트롤을 씀) | `DS/UIKIT/Autocomplete` | `DS/Controls/SelectionChips`, `SelectionChipsEditor` |
| 앱 수 | 50 | - | 34 | 14 / 10 |
| 후보 | 배열, TreeDocument, 함수, Promise | `possibleValues` 배열 (`elementsTree` 로 넘김) | `datasets` (`items`, `searchEngine`, `remoteSearchEngine`, `searchUrl`) | 없음 |
| 값 | `value`, `selectedItems` (TreeNodeModel) | `value` (문자열 / 문자열 배열) | `getSelection()`, `getValue()` (`;` 로 이은 글자) | 메서드 / 문자열 배열 |
| 기본값 | 여러 개 + 자유 입력, 0 글자 | 하나 + 자유 입력 아님 | 하나, 자유 입력 아님, 1 글자, 500ms | - |
| 포커스 때 목록 | 없음 (누르거나 ↓) | 같음 | `showSuggestsOnFocus` | - |
| 이벤트 | `change`, `uncommittedChange` | Tweakers `change` / `liveChange` | `onSelect`, `onUnselect` | `change`, `onPostRemove` 등 |

- **새로 만드는 WebUX 화면**에서 서버 검색, 고른 객체의 id(`grid`)가 필요하면 WUXAutoComplete 를 씁니다.
- **Tweakers 폼이나 그리드 셀**의 값이면 [`string` + `possibleValues`](/tweakers/editors#입력하며-고르기-검색-멀티-자유-입력) 를 씁니다. 안에서 이 컨트롤을 만들고 `allowMultipleValuesFlag` → `multiSearchMode`, `allowFreeInputFlag`, `minLengthBeforeSearch`, `typeDelayBeforeSearch`, `keepSearchResultsFlag` 를 넘깁니다. `viewOptions` 에 이 페이지의 옵션을 더 넣을 수 있습니다.
- **UIKIT 으로 만든 화면**은 UIKIT `Autocomplete` 를 씁니다. 원격 검색은 `datasets: { configuration: { remoteSearchEngine: function (dataset, text) { ... } } }` 로 넣고, `{ matchingItems, dataset }` 을 주는 Promise 를 돌려줍니다. 결과는 글자별로 캐시합니다. UIKIT 목록 전체는 [UIKIT](/controls/uikit) 에 있습니다.
- **후보 없이 글자를 칩으로만** 받으면 [SelectionChipsEditor](/controls/input-controls#selectionchipseditor), 칩을 코드로만 다루면 [SelectionChips](/controls/input-controls#selectionchips) 입니다.

## 2026 변경

| 항목 | 2025 | 2026 |
| --- | --- | --- |
| 정의 방식 | `Abstract.inherit` | ES `class` (사용법 같음) |
| 새 옵션 | - | `customValidator(text)`: 확정할 글자를 값 배열로 바꿈 (Promise 가능). `getValueForFiltering(text)`: 목록을 거를 글자. `acceptDrop(info)`, `onDrop(event, dndInfos)`: 칩, 칸에 끌어 놓기 |
| 새 함수 | - | `AutoComplete.configureCustomSeparator(options, ';')`: 만들기 전 옵션에 위 두 함수를 넣어 `;` 로 여러 값을 한 번에 입력. `retrieveMatchingPossibleValue(text)`: label 이나 value 가 같은 후보의 value |
| 칩 글자 | `label` | 노드에 `subLabel` 이 있으면 `label(subLabel)` |
| 칩 아이콘 | `icons` → `icon` → `value.icon` | `icons` → `value.icon` (`options.icon` 은 안 봄) |
| 미리 고르는 행 | 보이는 첫 행 | 글자와 같은 행 → 글자로 시작하는 행 → 단어 첫머리가 맞는 행 → 첫 행 |
| 하나만 모드 `change` 의 `oldValue` | 늘 `[]` | 앞 값 `[v]` |

로컬 실행으로 확인한 것 (2026): `configureCustomSeparator(options, ';')` 로 만든 칸에 "도면;고객" 을 치면 목록은 "고객" 으로 걸러지고, Enter 로 "도면", "고객" 두 칩이 생김. `retrieveMatchingPossibleValue('승인')` 은 `'RELEASED'`, `'in_work'` 는 `'IN_WORK'`, 없는 글자는 `undefined`. 샘플의 나머지 동작은 2025 와 같음.
