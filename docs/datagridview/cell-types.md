# 셀 타입 <Badge type="tip" text="서버 확인" />

::: warning 2026-11-12 수정
로컬 실행으로 다시 확인하다가 두 가지를 고쳤습니다. 여러 개 선택은 `customEnumMember` 가 아니라 `string` 타입으로 만들어야 하고, 링크(`url`) 값은 문자열이 아니라 `{ label, url }` 객체여야 보입니다. 샘플 `02_cellTypes.html` 도 같이 고쳤습니다.
:::

컬럼의 `typeRepresentation` 으로 셀 모양과 편집기를 정합니다. 편집하려면 `editableFlag: true` 를 같이 넣습니다.

샘플: [02_cellTypes.html](https://github.com/LeeOuHyoun/3d-docs/blob/main/DataGridViewSample/02_cellTypes.html)

## 한눈에 보기

| 원하는 셀 | typeRepresentation | 셀 값 | 추가 설정 |
| --- | --- | --- | --- |
| 텍스트 | `string` (기본) | 문자열 | - |
| 여러 줄 텍스트 | `multilineString` | 문자열 | - |
| 콤보박스 (하나 선택) | `customEnumMember` | 문자열 | `semantics.possibleValues` |
| 여러 개 선택 (칩) | `string` | 문자열 배열 | `semantics.possibleValues`, `semantics.allowMultipleValuesFlag: true` |
| 버튼 그룹 | `customSmallEnumMember` | 문자열 | `semantics.possibleValues` |
| 프로그레스 바 (편집 가능하면 슬라이더) | `progress` | 0~100 숫자 | `semantics.minValue / maxValue / stepValue` |
| 체크박스 | `boolean` | true / false | `editionPolicy: 'EditionOnOver'` 권장 |
| 날짜 | `date` (시간까지는 `datetime`) | Date 객체 | - |
| 정수 | `integer` | 숫자 | `semantics.minValue / maxValue` |
| 실수 | `float` (또는 `double`) | 숫자 | `semantics.units` 로 단위 표시 |
| 색상 | `color` | `'#rrggbb'` 를 넣으면 보이고, 편집 뒤 값은 `#` 없는 `'rrggbb'` | - |
| 링크 | `url` | `{ label: '표시 글자', url: '주소' }` 또는 `{ label, func }` 객체 (문자열은 빈 칸) | `hyperlinkTarget` ([모든 행에 링크](#모든-행에-링크)) |

전체 목록은 [typeRepresentation](/datagridview/api/type-representation), 세부 설정은 [semantics](/tweakers/api/semantics) 에 있습니다.

## 콤보박스 (하나 선택)

<<< @/../DataGridViewSample/02_cellTypes.html#combo{js}

`possibleValues` 는 문자열 배열 또는 `{ label: '표시값', value: '저장값' }` 배열 모두 됩니다.

::: warning 셀 값이 ''(빈 문자열)이거나 목록에 없으면 첫 항목이 저장되지 않습니다
콤보 셀의 값이 `''`, `null` 이거나 `possibleValues` 에 없는 값이면, 사용자가 목록의 **첫 항목**을 골라도 행 데이터가 바뀌지 않습니다 (화면에는 고른 값이 보임). `null` 이나 목록에 없는 값이면 고르기 전부터 셀에 첫 항목이 보입니다. 빈 값은 `undefined` 로 넣고, 지금 값은 항상 목록에 들어 있게 합니다. 값별 결과와 다른 해결 방법은 [콤보 셀의 값이 비었거나 목록에 없을 때](/datagridview/async-values#콤보-셀의-값이-비었거나-목록에-없을-때) 에 있습니다. (로컬 실행 확인)
:::

## 여러 개 선택 (칩)

<<< @/../DataGridViewSample/02_cellTypes.html#multi{js}

- 셀 값은 배열로 저장됩니다 (예: `['이영희', '박민수']`).
- 셀을 누르면 오른쪽에 `...` 버튼이 생기고, 그 버튼을 누르면 고른 값이 칩으로 보이는 편집기와 목록이 열립니다.
- `customEnumMember` (하나 선택 콤보)에 `allowMultipleValuesFlag` 를 주면 화면 표시는 되지만, 편집기가 하나만 고르는 콤보라서 편집하면 값이 배열이 아니라 문자열 하나로 바뀝니다 (로컬 실행 확인). 자세한 차이는 [편집기 종류별 사용법](/tweakers/editors).
- 화면에 보이는 구분자는 `viewOptions.multipleValuesSeparator` 로 바꿉니다. 기본은 `|` 입니다.

::: info 근거
Tweakers.js 의 TweakerBase.getStringFromValue. 구분자 앞뒤에 항상 공백을 붙이므로 `','` 를 주면 `김철수 , 이영희` 로 표시됩니다.
:::

## 버튼 그룹

선택지가 적을 때 콤보 대신 씁니다. 라디오 버튼처럼 하나만 고릅니다.

<<< @/../DataGridViewSample/02_cellTypes.html#buttonGroup{js}

## 프로그레스 바

<<< @/../DataGridViewSample/02_cellTypes.html#progress{js}

## 체크박스

<<< @/../DataGridViewSample/02_cellTypes.html#checkbox{js}

`EditionOnOver` 로 두면 클릭 한 번에 바로 토글됩니다.

## 날짜, 숫자

<<< @/../DataGridViewSample/02_cellTypes.html#dateNumber{js}

날짜 셀 값은 문자열이 아니라 `Date` 객체로 넣습니다.

## 색상, 링크, 여러 줄 텍스트

<<< @/../DataGridViewSample/02_cellTypes.html#colorLink{js}

색상 값은 `'#rrggbb'` 문자열, 링크 값은 `{ label, url }` 객체입니다. 링크에 문자열만 넣으면 빈 칸으로 보입니다.

## 모든 행에 링크

데이터에 링크 값이 없어도 컬럼 함수 `getCellValue` 가 행 값을 가공해 링크 객체를 돌려주면 모든 행이 링크가 됩니다. 누르면 주소로 이동하거나(`url`) 함수를 부릅니다(`func`). (로컬 실행 확인)

::: info 근거
Tweakers.js 의 TweakerURLObject (`hyperlinkTarget` 기본값 `'_blank'`) 와 링크 보기의 setValue / 클릭 처리, DataGridView.js 의 셀 그리기 (`semantics.executionContext` 에 cellInfos 를 넣음), getAsCSV / `_getCellValueForExport` / `_getCellValueForSort`.
:::

<<< @/../DataGridViewSample/02_cellTypes.html#linkCell{js}

<<< @/../DataGridViewSample/02_cellTypes.html#linkHelpers{js}

링크 객체의 키입니다.

| 키 | 동작 |
| --- | --- |
| `label` | 셀에 보이는 글자 |
| `url`, `parameters` | `<a href>` 는 `url + parameters` 입니다. 누르면 그 주소로 이동합니다 |
| `target` | 이 링크만의 열기 대상. 없으면 컬럼 옵션 `hyperlinkTarget`, 그것도 없으면 `'_blank'` (새 창) |
| `func` | 누르면 `func({ context })` 를 부릅니다. `context` 는 그 셀의 cellInfos 라서 `context.nodeModel` 이 누른 행입니다 |
| `module`, `func`, `argument` | `func` 가 문자열이면 AMD 모듈 `module` 을 불러 그 함수를 `{ ...argument, context }` 로 부릅니다 |
| `icon` | 글자 앞 아이콘 |

- `dataIndex` 는 행 데이터에 없는 키여도 되지만 **컬럼마다 달라야 합니다.** 두 컬럼에 같은 `dataIndex` 를 주면 앞 컬럼의 셀이 그려지지 않습니다.
- 값이 객체라서 정렬할 값(`getCellValueForSort`)과 내보낼 글자(`getCellValueForExport`)를 따로 돌려줍니다. 넣지 않으면 정렬할 값이 없고, `getAsCSV()` 는 링크 객체를 JSON 문자열로 씁니다.
- `getCellValueForExport` 는 `getAsCSV()` 가 머리글 행에도 부릅니다. 이때는 `nodeModel` 이 없으므로 `undefined` 를 돌려주면 머리글은 컬럼 `text` 를 씁니다.
- 찾기(Ctrl+F)는 따로 넣지 않아도 셀에 보이는 `label` 로 찾습니다.
- 링크를 누르면 그리드 `click` 이벤트도 같이 발생합니다. 같은 동작을 양쪽에서 처리하지 않도록 한쪽에서만 처리합니다.
- 모양은 일반 글자로 두고 클릭만 받으려면 그리드 `click` 이벤트에서 `cellInfos.columnID` 로 컬럼을 구분하는 방법도 있습니다 ([셀 직접 그리기 › 클릭 처리](/datagridview/custom-cell#클릭-처리)).

## 행마다 다른 타입이나 목록

컬럼 하나에서 행마다 셀 타입이나 콤보 목록이 달라야 하면 컬럼 함수 `getCellTypeRepresentation`, `getCellSemantics` 를 씁니다. 예제는 [컬럼 함수](/datagridview/cell-functions) 가이드, 전체 목록은 [컬럼 함수 레퍼런스](/datagridview/api/column-functions) 에 있습니다.

<<< @/../DataGridViewSample/07_columnFunctions.html#cellType{js}

`rowOf(cellInfos)` 는 행 값을 읽는 샘플의 보조 함수입니다 ([컬럼 함수](/datagridview/cell-functions)).

## 편집한 값 읽기

셀을 편집하면 행(TreeNodeModel)의 값이 바로 바뀝니다.

<<< @/../DataGridViewSample/02_cellTypes.html#dump{js}

`node.getAttributeValue(dataIndex)` 로 지금 값을 읽습니다.
