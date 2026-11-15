# 찾기, 페이지 나누기, 일괄 수정, 인쇄 <Badge type="tip" text="로컬 실행 확인" />

그리드에 이미 들어 있어서 옵션이나 함수 하나로 켜는 기능 네 가지입니다.

샘플: [16_findPrint.html](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/DataGridViewSample/16_findPrint.html)

::: info 근거
CollectionView.js 의 CollectionView.setFindStr / onFindRequest / goToNextMatchingCell / closeFind / getMatchingFindNodeModelArray / _getColumnsToProcessForFindData, 찾기 모델의 _findAllCellIDsMatchingFindStrInClientSide, `DS/CollectionView/CollectionViewPaginationManager`. DataGridView.js 의 `DS/DataGridView/DataGridViewPaginationManager` (nodeDepth), 행 모델의 _syncModel (페이지 범위), _getCellValueAsStringForFind, `DS/DataGridView/ModelMassUpdateDialog`, showModelMassUpdateDialog / _buildMassUpdateDialogAttributes, openPrintableViewWindow / generatePrintableViewHTML / getColumnOrGroupPrintableState. 호출부: ENXHistoryUX, ENOUserGroupMgmt (찾기, 인쇄), E6WCommonApps 이력 그리드 (페이지), ENOChgGovernanceUX, ENOProjectMgmtCommonComponents (일괄 수정). 찾은 범위, 페이지 수, 일괄 수정 대상, 인쇄 내용은 샘플을 [내 PC 에서 실행](/guide/contributing#샘플을-내-pc-에서-실행하기)해 확인했습니다. 인쇄 창이 실제로 뜨는 것과 대화상자 화면은 확인하지 않았습니다.
:::

## 그리드 옵션

<<< @/../DataGridViewSample/16_findPrint.html#grid{js}

| 키 | 넣는 자리 | 내용 |
| --- | --- | --- |
| `findNavigationMode` | 그리드 옵션 | 찾기 결과 이동 단위 ([아래](#이동-단위)) |
| `showMatchingCellBackgroundFlag` | 그리드 옵션 | 찾은 셀의 배경을 강조 |
| `paginationManager` | 그리드 옵션 | 행을 페이지로 나눔 ([아래](#페이지-나누기)) |
| `findableFlag: false` | 컬럼 옵션 | 찾기 대상에서 뺌 |
| `massUpdateAllowedFlag: false` | 컬럼 옵션 | 편집은 되지만 일괄 수정 대화상자에는 넣지 않음 |
| `printableFlag: false` | 컬럼 옵션 | 인쇄용 표에서 뺌 |

## 찾기

그리드에는 찾기 입력칸이 없습니다. 찾기 막대(`DS/Controls/Find`)를 붙이고 그리드 함수를 연결합니다.

<<< @/../DataGridViewSample/16_findPrint.html#find{js}

- 찾기 막대의 키와 동작(Enter, Shift+Enter, Esc)은 [입력 컨트롤의 Find](/controls/input-controls#find) 에 있습니다. 찾은 개수는 그리드가 찾기 막대의 `count` 에 넣어 `1/21` 처럼 보입니다.
- 닫기 버튼은 막대를 숨기기만 합니다. 다시 열 때는 `grid.findWidget.visibleFlag = true` 입니다 (만들면 `grid.findWidget` 에 막대가 들어감).
- 막대 없이 직접 만든 입력칸으로 찾을 때는 `grid.setFindStr(글자, 대소문자구분)`, `grid.goToNextMatchingCell()` / `goToPreviousMatchingCell()`, `grid.closeFind()` 를 직접 부릅니다.

<<< @/../DataGridViewSample/16_findPrint.html#reopen{js}

### 무엇을 찾나

| 대상 | 내용 |
| --- | --- |
| 행 | 필터로 걸러지지 않은 행. 접힌 하위 행도 찾습니다. **페이지를 나눈 그리드는 지금 페이지의 행만** 찾습니다 |
| 컬럼 | 보이는 컬럼 중 `findableFlag: false` 가 아닌 컬럼. 숨긴 컬럼은 찾지 않습니다 |
| 비교하는 글자 | 셀에 보이는 글자 (셀 타입의 표시 글자. 콤보는 값이 아니라 보이는 이름). 대소문자 구분을 끄면 양쪽을 소문자로 바꿔 비교 |

비교 방법을 바꾸려면 컬럼에 함수를 넣습니다 ([컬럼 함수](/datagridview/api/column-functions)).

| 컬럼 함수 | 쓰는 때 |
| --- | --- |
| `getCellValueAsStringForFind(cellInfos)` | 찾을 때 쓸 글자를 직접 돌려줌. 셀에 보이는 글자와 다른 값(코드, 숨은 이름)으로도 찾게 할 때 |
| `isCellMatchingFind(cellInfos, 찾을 글자)` | 일치 여부를 직접 판단해 true / false. 셀 안에서 강조할 위치는 없음 |
| `getFindPositionsInCellString(셀 글자, 찾을 글자, 결과 배열)` | 셀 안에서 강조할 위치를 직접 넣음 |

### 이동 단위

`findNavigationMode` 에 따라 찾은 개수와 다음 / 이전 버튼이 움직이는 단위가 바뀝니다. 만든 뒤에 바꾸면 지금 글자로 다시 찾습니다.

| 값 | 개수를 세는 단위 |
| --- | --- |
| `'substring'` (기본) | 글자가 나온 곳마다. 한 셀에 세 번 나오면 3 |
| `'cell'` | 셀마다 |
| `'row'` | 행마다 |

<<< @/../DataGridViewSample/16_findPrint.html#mode{js}

### 찾은 행 읽기

`grid.getMatchingFindNodeModelArray()` 는 찾은 행(TreeNodeModel) 배열입니다. 찾은 행만 선택하거나 내보낼 때 씁니다. 한 번도 찾지 않았으면 `undefined` 일 수 있으므로 `|| []` 로 받습니다.

<<< @/../DataGridViewSample/16_findPrint.html#matched{js}

- 행 값이 바뀌거나 컬럼을 보이게 / 숨기면 그리드가 지금 글자로 다시 찾습니다.
- `dataSource` 그리드([서버 사이드 데이터](/datagridview/server-side))는 필터가 바뀌면 찾기 막대를 닫습니다.

## 페이지 나누기

그리드 옵션 `paginationManager` 에 `DS/DataGridView/DataGridViewPaginationManager` 를 넣습니다. 앱 호출부는 E6WCommonApps 이력 그리드 (`pageSize: 30, nodeDepth: -1`) 입니다.

| 키 | 기본값 | 내용 |
| --- | --- | --- |
| `pageSize` | `100` | 한 페이지의 행 수 |
| `currentPage` | `1` | 지금 페이지 (1 부터). 바꾸면 바로 그 페이지로 갑니다 |
| `nodeDepth` | `0` | 행 수를 셀 깊이. `0` 이면 최상위 행만 세고, 펼친 하위 행은 부모와 같은 페이지에 붙습니다. `-1` 이면 펼쳐 보이는 행을 모두 셉니다 |

페이지를 옮기는 화면은 상태바의 페이지 칸입니다. 그리드에 `paginationManager` 가 있어야 보입니다 ([상태바](/datagridview/status-bar)).

<<< @/../DataGridViewSample/16_findPrint.html#statusBar{js}

코드로 옮길 때는 `currentPage` 를 바꿉니다. 전체 페이지 수는 `getPageNumber()`, 그 페이지의 행 수는 `getNumberOfItemsOnPage(page?)` 입니다.

<<< @/../DataGridViewSample/16_findPrint.html#pages{js}

::: warning 페이지를 나누면 행 번호는 지금 페이지 기준입니다
`grid.getModelLength()` 는 지금 페이지의 행 수, `grid.getNodeModelAtRowID(0)` 은 지금 페이지의 첫 행입니다 (50행을 20개씩 나눈 3페이지에서 10, `PRT-0041`). 전체 행은 TreeDocument 에서 읽습니다. 찾기와 인쇄도 지금 페이지만 대상이고, 일괄 수정의 "모든 항목" 은 다른 페이지 행까지 바꿉니다.
:::

- `currentPage` 에 1 보다 작은 값이나 정수가 아닌 값을 넣으면 무시됩니다. 마지막 페이지보다 큰 값도 로컬 실행에서는 반영되지 않았지만, 넣기 전에 `getPageNumber()` 로 확인합니다.
- 페이지가 바뀐 뒤 할 일은 `paginationManager.subscribeToPaginationUpdate(객체, 함수)` 로 받습니다. 함수의 `this` 가 첫 인자입니다. 돌려받는 번호는 내부에 등록된 번호보다 1 큽니다 (원본이 `id: this._id++` 로 넣고 `this._id` 를 돌려줌). `unsubscribeToPaginationUpdate(번호 - 1)` 로 풉니다.
- 페이지마다 서버에서 불러오는 방식(`onPaginationChangeAsyncLoadModel`, `paginationChangeAsyncProcessDone`)은 DataGridView 앱 호출부가 없어 다루지 않습니다 (SNResultUX 의 목록 / 썸네일 뷰가 씀).

## 일괄 수정

`grid.showModelMassUpdateDialog()` 는 여러 행의 값을 한 번에 바꾸는 대화상자("대규모 업데이트")를 엽니다.

<<< @/../DataGridViewSample/16_findPrint.html#massUpdate{js}

- 대화상자에는 `editableFlag` 와 `massUpdateAllowedFlag` (기본 `true`) 가 모두 true 인 컬럼이 화면 순서로 한 줄씩 나옵니다. 샘플은 상태, 수량입니다 (품명은 `massUpdateAllowedFlag: false`).
- 줄에 값을 넣으면 그 줄이 체크됩니다. 체크된 줄만 반영합니다.
- 대상은 "선택한 항목" / "모든 항목" 중에서 고릅니다. 행을 선택한 상태로 열면 "선택한 항목" 이 기본입니다. "모든 항목" 은 펼쳐 보이는 행 전체이고, 페이지를 나눈 그리드에서는 다른 페이지 행도 바뀝니다.
- 확인은 반영 후 닫기, 적용은 반영만 합니다. 행마다 그 셀이 편집 가능할 때만(`getCellEditableState`) `grid.setNodeModelValue` 로 값을 넣고 셀을 깜빡입니다.

::: tip 서버 저장은 직접 합니다
대화상자는 행 값만 바꿉니다. 컬럼에 `setCellValue` 함수가 있으면 셀 편집 때처럼 그 함수가 불리므로, 거기서 저장하거나 바뀐 행을 모아 저장합니다 ([저장 후 화면 반영](/datagridview/save-and-reflect)).
:::

## 인쇄

<<< @/../DataGridViewSample/16_findPrint.html#print{js}

| 호출 | 동작 |
| --- | --- |
| `grid.openPrintableViewWindow()` | 새 창(1200 x 800, `webapps/DataGridView/index-datagridview-print.htm`)에 인쇄용 표를 엽니다. 사용자가 그 창에서 인쇄합니다 |
| `grid.openPrintableViewWindow(true)` | 숨긴 iframe 에 표를 만들고 바로 브라우저 인쇄 창을 엽니다 (ENXHistoryUX). Firefox 는 항상 새 창 |
| `grid.generatePrintableViewHTML()` | 인쇄용 `table` HTML 문자열만 돌려줍니다. 직접 만든 화면에 넣을 때 |

- 지금 화면에 보이는 행(펼친 행, 필터 통과)과 보이는 컬럼 중 `printableFlag: false` 가 아닌 컬럼이 들어갑니다. 페이지를 나눈 그리드는 지금 페이지만 들어갑니다.
- 하위 행이 있는 그리드는 맨 앞에 "수준" 칸이 붙습니다.
- 새 창에는 그리드가 있는 문서의 스타일시트를 그대로 넣어서 셀 모양이 화면과 비슷합니다.
- 표를 만들기 전에 `onDelayedModelDataRequest` 를 `context: 'OnPrint'` 로 부릅니다 ([서버 사이드 데이터](/datagridview/server-side)).

::: warning 찾기 강조가 인쇄에 남습니다
찾기 중이면 셀 글자가 강조 태그(`mark`)로 나뉜 채 인쇄용 표에 들어갑니다. 인쇄 전에 `grid.closeFind()` 를 부르거나 찾기 막대를 닫습니다.
:::
