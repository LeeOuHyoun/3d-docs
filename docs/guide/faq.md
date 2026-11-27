# 자주 틀리는 점 (트러블슈팅)

각 페이지의 주의 상자에 흩어져 있는 "자주 틀리는 점"을 **증상별**로 모았습니다. 증상을 찾고, 자세한 내용은 오른쪽 링크의 원래 절에서 봅니다.

::: tip
ENOVIA 코드는 잘못 넣은 키나 이름을 **오류 없이 무시**하는 경우가 많습니다. 콘솔이 조용한데 동작하지 않으면 먼저 [설정이 무시될 때](#설정이-오류-없이-무시될-때)를 봅니다.
:::

각 항목의 근거는 링크한 페이지의 "근거" 상자에 있습니다. 새 주의 사항을 문서에 쓰면 이 페이지에도 한 줄 추가합니다.

## 화면에 아무것도 안 보일 때

| 증상 | 원인과 해결 | 자세히 |
| --- | --- | --- |
| 그리드가 보이지 않음 | 그리드 높이 기본값이 `'inherit'` 라 부모 높이를 씀. 부모에 높이를 주거나 `maxVisibleRowCount` 사용 | [기본 그리드](/datagridview/getting-started#_1-화면-준비) |
| 그리드가 페이지 전체를 덮어 제목, 버튼이 가려짐 | 높이 기본값일 때 그리드는 `position: absolute` 로 놓임. 담는 요소에 `position: relative` 를 줌 | [기본 그리드](/datagridview/getting-started#_1-화면-준비) |
| 다이얼로그 안 그리드가 안 보임 (높이 0) | Dialog 의 height 기본값 -1 이면 내용 영역이 auto. Dialog 에 height 를 주고 내용 요소에 position: relative, height: 100% | [다이얼로그 안에 DataGridView](/controls/dialog#다이얼로그-안에-datagridview-넣기) |
| `require` 콜백이 아예 불리지 않음 | `css!` 로 부른 CSS 가 404 면 그 `require` 전체가 실패. 네트워크 탭에서 `.css` 요청 확인 | [css!](/amd/plugins#css) |
| 불러온 모듈 값이 `undefined` | 파일 안 `define` 이름이 틀려도 오류 없이 `undefined` 가 들어옴 | [define / require](/amd/#define-require-쓰는-법) |
| 처음 열 때 AMD timeout | `waitSeconds` 는 끝나지 않은 요청 전체에 걸림. `dsRequireWaitSeconds` 를 늘려 확인 | [오류 메시지](/amd/#오류-메시지) |
| `widget is not defined` | 전역 `widget` 은 AMD 모듈 실행 시점에 생김. `require([...], function () { widget... })` 안에서 사용 | [위젯 HTML 구조](/widget/#위젯-html-구조) |
| 알림(`addNotif`)이 화면에 안 나옴 | 앱 시작 때 `ViewOnScreen.setNotificationManager(UXMessages)` 를 한 번 불러야 함 | [알림 메시지](/controls/notifications#기본-사용) |
| 로딩 가리개(ModalLoader)가 안 사라짐 | `removeModalLoader` 는 `displayModalLoader` 에 준 **같은 글자**로 지움 | [Loader, ModalLoader](/controls/webux-controls#loader-modalloader) |
| `The given container is not positionned...` 예외 | 대상 요소에 `position: relative` 를 줌 | [Loader, ModalLoader](/controls/webux-controls#loader-modalloader) |
| 아이콘이 빈 칸 | 아이콘 이름이 글꼴마다 다름 (`floppy` 는 3DS 글꼴, FontAwesome 은 `floppy-o`) | [아이콘](/controls/webux-controls#아이콘) |
| 단독 실행에서 3DSearch 가 안 열림 | 받는 쪽(상단 3DSearch)이 3DDashboard 최상위 창에 있음. 대시보드 위젯에서만 동작 | [3DSearch 동작 방식](/widget/search#동작-방식) |
| 태그 창에 위젯 태그가 하나도 안 보임 | `object` 에 숫자나 빈 값이 섞이면 개수 계산에서 오류가 나 그 호출의 태그가 모두 빠짐. `String(n)` 으로 넣고 `sixw` 는 `ds6w:what/...` 처럼 6W 루트로 시작 | [6W 태그 연동](/widget/tagger#주의) |
| 위젯을 닫은 뒤에도 상단 바에 그 위젯 항목이 남는 것 같음 | 항목 함수(onExecute)는 위젯 안에서 실행됨. 끝낼 때 `die()` 로 지움 (대시보드가 대신 지우는지는 검증 필요) | [대시보드 상단 바](/widget/top-bar#주의) |
| 속성 패널에 ID 카드만 보이고 탭 내용이 안 보임 | 담는 요소에 높이가 없음. 패널은 `height: 100%` 와 absolute 로 그림 | [패널 만들기](/controls/properties-panel#패널-만들기) |
| 속성 패널에 편집 버튼과 저장이 안 나옴 | `readOnly` 를 안 주면 읽기 전용. `readOnly: false` 를 줌 | [생성 옵션](/controls/properties-panel#생성-옵션) |
| 속성만 보이는 패널에서 저장 성공 / 오류 메시지가 안 보임 | `ALL` 이 아니면 메시지를 붙일 곳이 없음. `events.onNotification` 에서 직접 띄움 | [typeOfDisplay](/controls/properties-panel#보이는-모양-typeofdisplay) |
| 작은 성숙도 표시(ChangeMaturityWidget)가 모양 없이 나옴 | 이 모듈은 자기 CSS 를 부르지 않음. `css!DS/ChangeMaturityWidget/ChangeMaturityWidget` 을 같이 require | [성숙도 변경, 개정](/rest-api/lifecycle#작은-성숙도-표시-changematuritywidget) |
| `DSpointer...` 이벤트가 오지 않음, 글꼴 아이콘이 안 보임 | `DS/Core/Core` 를 읽지 않아 PointerEvents 가 초기화되지 않고 wux.css 가 없음. 의존성에 추가 | [공통 유틸](/amd/utilities#어떻게-부르나) |
| ENOXTriptych 패널이 300px 로 보이고 가운데가 오른쪽 패널 밑으로 들어감 | `container` 없이 만들면 넓이 0 에서 계산하고, inject 뒤에 다시 계산하지 않음. `container` 에 화면에 붙은 요소를 주거나, 붙인 뒤 `triptych-set-size` 를 보냄 | [Triptych 주의](/controls/enox-components#triptych-주의) |
| ENOXIDCard 썸네일이 보이지 않음 | 넓이가 868px 보다 좁거나, 화면에 붙기 전에 render 함. 붙은 요소에 `render().inject()` 후 `attachResizeSensor()` | [객체 머리글](/controls/enox-components#객체-머리글-enoxidcard) |
| 떠 있는 패널을 최대화하자 화면 밖으로 나가 닫기 단추가 안 보임 | `limitParent` 가 기본값(false)이면 위치를 그대로 둔 채 키움. `limitParent: true` 를 줌 | [떠 있는 패널](/controls/enox-components#떠-있는-패널-enofloatingpanel) |
| 하위 메뉴가 안 열리고 콘솔에 `Invalid content, menu must contain at least one item.` | Promise 를 돌려주는 보통 함수를 submenu 에 줌. `async function` 으로 씀 | [하위 메뉴](/controls/menu#하위-메뉴) |
| 2026 으로 올린 뒤 `DS/ENO6WPlugins/jQuery` 등이 404 / scripterror | 2026 에는 ENO6WPlugins, VENENO6WPlugins, JSZipTools, SPYTempVenHC 폴더가 없음. CKEditor 는 같은 이름 안에서 4.18.0 → 4.25.1-lts 로 바뀜. OOTB 처럼 `DS/ENOjquery/ENOjquery` 로 바꾸거나 라이브러리를 내 폴더에 넣음 | [외부 라이브러리](/amd/libraries) |
| `Handlebars helper '...' has already been registered!` | Handlebars 객체는 한 창에 하나이고, `DS/Handlebars/Handlebars` 가 같은 이름의 재등록을 막음. 이름에 앱 접두어를 붙이고 한 번만 등록 | [Handlebars](/amd/libraries#템플릿-handlebars) |
| 3DPlay 미리 보기를 닫거나 바꿀 때 콘솔에 `Experience is not ready` | 준비(ExperienceReady) 전에 `dispose()` 하면 experience 를 정리하지 않고 스플래시만 지움. 준비 뒤에 바꾸거나 닫음 | [3DPlay](/widget/3dplay#다른-객체로-바꾸기와-닫기) |
| 새 타입의 상세 화면이 안 나옴 | 프로젝트 타입은 `type_새타입` menu 를 만들어 `Tree` menu 에 넣어야 함 | [객체 트리 menu](/ui-components/command-menu#객체-트리-menu-type-menu) |

## 설정이 오류 없이 무시될 때

### webapps

| 증상 | 원인과 해결 | 자세히 |
| --- | --- | --- |
| 행 높이(`cellHeight`)가 안 바뀜 | 그리드 최상위가 아니라 `layoutOptions: { ... }` 안에 넣어야 함 | [행 높이](/datagridview/row-height#주의) |
| 그리드 헤더 높이(`columnHeaderHeight`)가 안 바뀜 | `layoutOptions: { ... }` 안에 넣어야 함. 27 보다 작은 값은 27 로 되돌아감. `layoutDensity`, `cellHeight` 는 헤더 높이를 바꾸지 않음 | [헤더 높이](/datagridview/header#헤더-높이) |
| 그리드 컬럼이 `visibleFlag: false` 인데도 보임 | `alwaysVisibleFlag: true` 인 컬럼은 숨길 수 없음 | [컬럼 보이기와 숨기기](/datagridview/api/layout-methods#컬럼-보이기와-숨기기) |
| 컬럼 정의 객체를 고쳤는데 그리드가 그대로 | `grid.layout.setColumnOrGroupProperties(...)` 를 쓰거나, 고친 뒤 `grid.layout.applyColumns()` 를 부름 | [컬럼 설정 바꾸기](/datagridview/api/layout-methods#컬럼-설정-바꾸기) |
| 그리드 컬럼 너비, 순서가 새로 고침하면 원래대로 돌아감 | 그리드 옵션 `identifier` 가 없으면 화면 상태를 저장하지 않음. `useWidgetPreferencesFlag` 는 저장 위치만 바꿈 | [사용자 뷰와 화면 상태 저장](/datagridview/custom-views#주의) |
| 그리드 `columns` 의 `width`, `visibleFlag` 를 고쳐도 화면이 그대로 | 예전에 저장된 화면 상태가 컬럼 정의보다 우선. `grid.layout.deleteDataGridViewPreferences()` 나 localStorage 의 `wux-collectionView-dataGridView` 삭제 | [사용자 뷰와 화면 상태 저장](/datagridview/custom-views#주의) |
| 코드로 컬럼을 숨기거나 너비를 바꾼 것이 기억되지 않음 | `grid.layout.setColumn...` 은 화면만 바꿈. 마지막 인자에 `{ isUserInteraction: true }` | [언제 저장하고 언제 되살리나](/datagridview/custom-views#언제-저장하고-언제-되살리나) |
| `rowGroupingOptions` 를 넣었는데 행이 안 묶임 | 옵션은 묶는 방법만 정함. `grid.groupRows({ dataIndexesToGroup: ['status'] })` 를 불러야 묶임. 인자는 배열이 아니라 객체 | [행 그룹핑](/datagridview/row-grouping#주의) |
| `rowGroupingEnabledFlag: false` 인데 묶임 | 이 값은 화면 조작만 끔. 코드의 `groupRows` 는 그대로 동작 | [행 그룹핑](/datagridview/row-grouping#주의) |
| `rowReorderEnabledFlag: true` 인데 행 순서가 안 바뀜 | 기본값이 이미 true. `rowDragEnabledFlag: true` 를 켜야 함 | [옵션 네 개와 잡는 곳](/datagridview/drag-and-drop#옵션-네-개와-잡는-곳) |
| 그리드 행을 밖으로 끌게만 했는데 순서도 바뀜 | `rowDragEnabledFlag` 를 켜면 순서 바꾸기도 켜짐. `rowReorderEnabledFlag: false` 추가 | [드래그앤드롭 주의](/datagridview/drag-and-drop#주의) |
| 그리드에서 여러 개 선택 콤보를 편집하면 값이 문자열 하나가 됨 | `customEnumMember` 는 하나만 고르는 콤보. 여러 개 선택은 `typeRepresentation: 'string'` + `possibleValues` + `allowMultipleValuesFlag` | [여러 개 선택](/datagridview/cell-types#여러-개-선택-칩) |
| 링크(`url`) 셀이 비어 있음 | 값은 문자열이 아니라 `{ label, url }` 객체 | [셀 타입](/datagridview/cell-types#한눈에-보기) |
| `functionIcon` 셀 아이콘이 안 보임 | `semantics.icon` 에 문자열만 주면 Font Awesome 이름으로 찾음. `{ iconName, fontIconFamily }` 로 줌 | [버튼, 링크, 색상](/tweakers/editors#버튼-링크-색상) |
| 숫자 편집기 `minValue: 0` 인데 음수가 들어감 | 경계값이 0 이면 코드로 넣는 값은 검사하지 않음 | [범위를 벗어난 값](/tweakers/editors#범위를-벗어난-값) |
| 콤보에 값을 넣었더니 첫 항목이 됨 | `possibleValues` 의 `value` 가 아닌 값(`label` 포함)을 넣으면 첫 항목으로 바뀜 | [콤보](/tweakers/editors#콤보) |
| 툴바 라벨을 `updateNodeModel` 로 바꿔도 그대로 | 툴바를 다시 그려야 반영됨. `prepareUpdateView` / `pushUpdateView` 로 감쌈 | [prepareUpdateView / pushUpdateView](/tweakers/generated-toolbar#prepareupdateview-pushupdateview) |
| 직접 만든 편집기가 그리드 편집 모드에서 비어 있음 | 템플릿 `options.displayStyle` 이 화면 모듈(`VIEW_MODULES`) 키와 맞아야 함 | [편집기 직접 만들기](/tweakers/custom-tweaker#view-modules-키) |
| 트리 컬럼 필터가 안 생김 | `setFilterModel` 의 키는 `tree` 가 아니라 `label` | [필터](/datagridview/api/filter) |
| 그리드 컬럼의 `getCellTooltip` 이 안 불림 | 그리드 옵션 `getCellTooltip` 을 넣으면 컬럼 쪽은 무시됨 | [조건부 모양과 툴팁](/datagridview/cell-functions#조건부-모양과-툴팁) |
| WebUX 컨트롤 옵션이 안 먹음 | `publishedProperties` 에 있는 키만 반영. 대소문자까지 맞아야 함 (`visibleflag` ✕). UIKIT 은 오타도 저장만 됨 | [두 계열의 차이](/controls/#두-계열의-차이) |
| 속성에 대입해도 그대로 | `readOnly` 속성은 getter 만 있음. `'use strict'` 에서는 `TypeError`. 생성자 옵션으로 넣음 | [속성 읽기와 쓰기](/controls/webux-controls#속성-읽기와-쓰기) |
| Dialog `className` 이 안 붙음 | Dialog / Window 에 `className` 이 없음. `dlg.getContent().addClassName(...)` | [다이얼로그](/controls/dialog#자주-쓰는-옵션) |
| UIKIT 체크박스가 라디오로 나옴 | `type` 기본값이 `radio`. `type: 'checkbox'` 를 꼭 씀 | [Input/Toggle](/controls/uikit#input-toggle) |
| UIKIT Alert `closable: false` 가 안 됨 | 값을 읽기만 하고 닫기 아이콘을 항상 붙임 (2025) | [Alert](/controls/uikit#alert) |
| `createElement` 에 준 클래스, 스타일이 안 먹음 | `className` → `'class'`, `style` → `styles`, `innerHTML` → `html` | [옵션 키](/uwa/element#옵션-키) |
| `UWA.is(v, 'Boolean')` 이 항상 false | type 이름 오타(대문자 포함)는 검사 없이 항상 false | [is, typeOf](/uwa/core#is-typeof) |
| `tweaker.setOptions({...})` 가 적용 안 됨 | 옵션 저장만 함. `tweaker.requiredFlag = true` 또는 `setProperties` | [편집기 단독 사용](/tweakers/standalone#직접-만들기) |
| `semantics.readOnly: true` 가 false 가 됨 | `'true'` 문자열과 비교. `generateView(value, { readOnly: true })` 처럼 최상위로 | [타입 이름으로 만들기](/tweakers/standalone#타입-이름으로-만들기) |
| 툴바 버튼 `action` 에 넣은 함수가 안 불림 | `action` 은 `module`, `func`, `argument` 만 복사. 함수는 `value` 에 | [버튼 콜백](/tweakers/generated-toolbar#버튼-콜백) |
| typeRepresentation 을 다시 등록해도 그대로 | 같은 이름은 처음 것이 남음. 등록된 객체를 직접 고침 | [사용자 타입 등록](/tweakers/custom-type#등록은-전역에-한-번) |
| 위젯 설정 항목이 안 생김 | type 은 7 가지만. 그 밖의 type 은 조용히 무시 | [위젯 설정 type](/widget/preferences#type) |
| HTML 의 설정 `onchange` 가 안 됨 | 위젯 읽기 코드가 일부 속성만 남김. `addPreference` 로 정의 | [항목 키](/widget/preferences#항목-키) |
| Additional App 의 Type 을 못 바꿈 | 수정 창에서 Type 은 비활성. 지우고 다시 등록 | [입력 항목](/widget/dashboard-register#입력-항목) |
| Internal 로 올린 위젯이 js, css 를 못 찾음 | HTML 한 장만 올라감. `webapps/` 모듈을 쓰는 위젯은 External (주소) 로 등록 | [Internal 저장](/widget/dashboard-register#internal-저장) |
| `widget.mutate` 가 `do not support` 오류 | 프레임 안 `widget` 에는 `mutate` 가 없음. 부모 창의 위젯 인스턴스로 호출 | [Run Your App 동작](/widget/dashboard-register#동작) |
| `triptych-toggle-panel` 로 왼쪽을 열려 했는데 오른쪽이 움직임 | data 를 `{ side: 'left' }` 객체로 보냄. 문자열 `'left'` / `'right'` 로 보냄 | [Triptych 주의](/controls/enox-components#triptych-주의) |
| `Dom.createElement` 로 만든 요소에 class, 글자가 안 들어감 (2026 은 콘솔 경고) | 옵션 이름이 버전마다 다름 (`class` / `html` 과 `className` / `text`). `UWA.createElement` 사용 | [요소 만들기와 찾기](/amd/utilities#요소-만들기와-찾기) |
| `DS/PDFjs/PDFjs` 가 오래된 PDF.js 로 동작 | `window.PDFJS_VERSION` 이 없으면 1.9.426. 처음 부르기 전에 `'2.13.216'` 등을 넣음 (OOTB 방식) | [문서와 파일](/amd/libraries#문서와-파일) |
| 자동완성이 칩으로 여러 개를 받고, 목록에 없는 글자도 값이 됨 | `multiSearchMode`, `allowFreeInputFlag` 기본값이 true. false 를 줌 | [하나만 고르기](/controls/autocomplete#하나만-고르기) |
| 자동완성 배열 후보에 넣은 id, type 이 고른 노드에 없음 | 배열 항목은 label, value, icon(s), tooltip, typeRepresentation, grid 만 남음. grid 에 넣음 | [하나만 고르기](/controls/autocomplete#하나만-고르기) |

### 3DSpace 서버 (settings, MQL)

| 증상 | 원인과 해결 | 자세히 |
| --- | --- | --- |
| command / table / webform 설정이 안 먹음 | settings 키는 대소문자까지 그대로 비교 (`Alternate TYPE expression` ✕) | [settings 레퍼런스](/ui-components/settings) |
| 툴바 command 의 선택 개수 검사가 안 됨 | `Row Select` 값은 `single` / `multiple` / `none` 소문자. command 에 `selection` 은 효과 없음 | [선택 행 넘기기](/ui-components/command-menu#툴바-command-선택-행-넘기기) |
| form 의 `On Change Handler` 가 무시됨 | form 은 `OnChange Handler` 가 있으면 그것만 읽음 | [편집 setting](/ui-components/webform#편집-setting) |
| attribute 열이 이상하게 나옴 | `Column Type` 이 아니라 `Field Type` `attribute` + `Admin Type` | [열 종류](/ui-components/table#열-종류-column-type-field-type) |
| 프레임 지정(Target Location)이 안 먹음 | 값은 소문자 `content` 등 | [Target Location](/ui-components/command-menu#어디에-열까-target-location) |
| JPO 를 못 찾음 | 설정에는 `_mxJPO` 를 뺀 이름 (`XX_MyPartUI`) | [이름 규칙](/jpo/#이름-규칙) |
| 채번기(generator)가 동작 안 함 | generator 는 심볼릭 type 이름으로 찾음 | [채번기 만들기](/trigger/generator#만들기) |
| 코드에서 스키마 실제 이름을 못 얻음 | `eServiceSchemaVariableMapping.tcl` 에 property 등록 누락 | [심볼릭 이름](/schema/#심볼릭-이름) |

## 이벤트, 콜백이 안 오거나 두 번 올 때

| 증상 | 원인과 해결 | 자세히 |
| --- | --- | --- |
| 초기값을 넣을 때 저장 로직이 돎 | 코드로 값을 바꿔도 `change` 가 남. `fireOnlyFromUIInteractionFlag: true` 또는 `disableEvents()` | [WebUX 이벤트](/controls/webux-controls#이벤트), [Tweakers 이벤트](/tweakers/standalone#이벤트) |
| 부모에 건 `change` 가 여러 컨트롤에서 옴 | WebUX 이벤트는 버블링됨. `e.dsModel` 로 컨트롤 확인 | [WebUX 이벤트](/controls/webux-controls#이벤트) |
| `el.addEvent('onClick')` 이 안 불림 | 요소는 `'click'`, UWA 클래스와 UIKIT 컨트롤은 `'onClick'` | [DOM 이벤트](/uwa/element#이벤트), [UIKIT](/controls/uikit#공통-사용법) |
| 다이얼로그 버튼을 눌러도 안 닫힘 | 버튼은 클릭 이벤트만 냄. 핸들러에서 `e.dsModel.dialog.close()` | [버튼](/controls/dialog#버튼-buttons) |
| 닫은 다이얼로그를 다시 열 수 없음 | 기본은 `close()` 때 `destroy()` 까지. 새로 만들거나 `autoCloseFlag: false` | [닫기와 다시 열기](/controls/dialog#닫기와-다시-열기) |
| 창에 identifier 를 줬는데 위치가 저장되지 않음 | 사용자가 끌기 / 크기 조절을 놓을 때만 저장. 코드로 바꾼 값은 저장 안 됨. 숫자 identifier 는 쓰이지 않음 | [위치 저장](/controls/window-layout#위치-저장) |
| Panel 을 위아래로 붙여 묶을 수 없음 (attachToWindow 가 -1) | 격자 묶음은 원본에서 꺼져 있음. InsideArea (탭 묶음)만 됨. Dialog 는 묶이지 않음 | [탭으로 묶기](/controls/window-layout#탭으로-묶기) |
| 다이얼로그를 열 때마다 ImmersiveFrame 이 쌓임 | 열 때마다 틀을 만들고 정리하지 않는 OOTB 패턴을 따름. 틀 하나를 만들어 같이 씀 | [OOTB 패턴](/controls/dialog#ootb-앱의-다이얼로그-패턴) |
| `showWidget` 을 불러도 아무것도 안 열림 | 창은 3DDashboard 최상위 창이 그림. 단독 실행에서는 받는 곳이 없음 | [TransientWidget](/widget/transient-widget#동작-방식) |
| 다른 위젯이 `Events.publish` 를 못 받음 | `Events`, `CSOManager` 는 같은 창 안에서만 하나. 위젯 사이는 PlatformAPI, InterCom, 끌어 놓기 | [어떤 것을 쓰나](/events/#어떤-것을-쓰나) |
| 구독 함수 일부만 불림 | 구독 함수에서 예외가 나면 나머지는 안 불림. 기본 순서는 나중 구독이 먼저 | [PubSub 주의](/events/pubsub#주의) |
| CSOManager `onUpdate` 를 해제할 수 없음 | 토큰을 안 돌려줌. `onAdd`, `onRemove`, `onEmpty` 를 따로 등록 | [선택 이벤트](/events/selection#이벤트) |
| 끌어 놓기 `over` 에서 데이터가 비어 있음 | 브라우저가 dragover 중에는 빈 값을 줌. `drop` 에서 판단. `draggable(el)` 은 옵션 객체를 꼭 넘김 | [droppable 옵션](/events/drag-and-drop#droppable-옵션) |
| 새로고침할 때마다 이벤트, DOM 이 겹침 | `onRefresh` 가 없으면 `onLoad` 가 다시 불림 | [onRefresh](/widget/widget-api#onrefresh) |
| 그리드 값 변경 알림이 끝없이 반복 | `onNodeModelUpdate` 안에서 저장 후 `updateOptions` 하면 다시 알림. `setCellValue` 방식 사용 | [이벤트 안에서 저장하지 않기](/datagridview/save-and-reflect#주의-이벤트-안에서-저장하지-않기) |
| 그리드 리스너가 해제되지 않음 | `removeEventListener` 에는 `addEventListener` 의 **반환값**을 넘김 | [등록 / 해제](/datagridview/api/events#등록-해제) |
| 트리 행이 "펼치는 중" 에서 멈춤 | `useAsyncPreExpand` 일 때 `onPreExpand` 콜백이 모든 경우에 `node.preExpandDone()` 을 불러야 함 | [펼칠 때 하위 행 불러오기](/datagridview/tree#펼칠-때-하위-행-불러오기) |
| 툴바 `onReady` 에서 상태를 바꾸면 끝없이 돎 | `onReady` 는 툴바를 다시 그릴 때마다 옴. 첫 상태는 JSON 에 넣음 | [툴바 이벤트](/tweakers/generated-toolbar#툴바-이벤트) |
| 직접 만든 편집기에서 값을 바꿨는데 그리드 데이터에 안 들어감 | 화면 모듈의 이벤트는 `Dom.addEventOnElement` 로 등록하고 `setTweakerValue` 로 값을 바꿈 | [값과 이벤트](/tweakers/custom-tweaker#값과-이벤트) |
| 그리드 `onPreExpand` 콜백이 하나만 동작 | 다시 등록하면 이전 콜백을 대체함 | [펼칠 때 하위 행 불러오기](/datagridview/tree#펼칠-때-하위-행-불러오기) |
| 그리드 `onInfiniteScroll` 콜백이 한 번만 옴 | 처리 끝에 `grid.infiniteScrollProcessDone()` 을 안 부름. 실패했을 때와 더 없을 때도 부름 | [스크롤이 끝에 닿으면 행 더하기](/datagridview/server-side#예제-5-대안-스크롤이-끝에-닿으면-행-더하기) |
| 행이 적으면 그리드 `onInfiniteScroll` 이 안 옴 | 첫 묶음이 화면을 못 채우면 스크롤 막대가 없어 불리지 않음. 첫 묶음을 늘리거나 `onEndResizeOrOnReadyModelRequest` 사용 | [서버 사이드 데이터 주의](/datagridview/server-side#주의) |
| 그리드 드래그 콜백을 넣었더니 행 이동(컬럼 순서 바꾸기)이 안 됨 | 콜백이 `true` 를 돌려줘야 그리드 기본 동작이 이어짐. `return` 이 없으면 건너뜀 | [반환값](/datagridview/drag-and-drop#반환값) |
| 그리드에 밖에서 끌어 온 것을 놓을 수 없음 | `onDragOverCell` (빈 곳은 `onDragOverBlank`) 에서 `event.preventDefault()` 를 불러야 함 | [밖에서 끌어 온 객체 받기](/datagridview/drag-and-drop#예제-5-밖에서-끌어-온-객체-받기) |
| 그리드 `onDropCell` 에서 읽은 순서가 옮기기 전 순서 | 콜백이 기본 이동보다 먼저 불림. `onDragEndCell` / `onDragEndRowHeader` 에서 읽음 | [순서가 바뀐 것을 알기](/datagridview/drag-and-drop#순서가-바뀐-것을-알기) |
| 그리드 `userPreferencesChange` 가 한 번 조작에 여러 번 옴 | 저장 키마다 발생. 타이머로 모아서 한 번만 처리 | [상태를 꺼내 서버에 저장하고 되살리기](/datagridview/custom-views#예제-4-상태를-꺼내-서버에-저장하고-되살리기) |
| 그리드 `onCustomViewsSave` 가 불리지 않음 | 사용자 뷰 대화상자의 저장 버튼을 누를 때만 불림. 코드로 뷰를 바꾼 것은 대상 아님 | [이벤트와 콜백](/datagridview/custom-views#이벤트와-콜백) |
| WAFData 를 감싼 Promise 가 끝나지 않음 | 세션 만료, 라이선스 오류는 `onFailure` 가 안 불림. `onPassportError` 를 꼭 넣음 | [Promise 로 감싸기](/uwa/request#promise-로-감싸기) |
| `getServiceUrl` 응답이 오지 않음 | 대시보드 밖에서는 `onComplete`, `onFailure` 둘 다 안 불림. 시간 제한과 대체값을 둠 | [서버 주소](/widget/space-url#서버-주소-3dspace) |
| 콜백에서 `this._parent()` 가 엉뚱하게 동작 | 메서드 실행 중에만 유효. 먼저 변수에 담음 | [상속과 _parent](/uwa/class#상속과-parent) |
| `onXxx` 메서드가 저절로 불림 | 이벤트 이름과 같은 메서드는 `dispatchEvent` 때 자동 호출 | [클래스 Events](/uwa/class#events) |
| 지운 View 의 `render` 가 계속 불림 | `destroy()` 는 `stopListening()` 을 안 부름. 직접 부름 | [View](/uwa/mvc#view) |
| 3DSearch 에서 OK 를 눌러도 결과가 안 옴 | `app_socket_id` 가 소켓 id 와 다르거나, `mode` 가 `'furtive'` 가 아니거나, `RegisterContext` 없이 `InContextSearch` 만 보냄. 같은 옵션을 둘 다 보냄 | [3DSearch 옵션](/widget/search#옵션) |
| 다른 위젯의 Compass 응답을 내 것으로 처리함 / 다른 위젯의 Compass 구독이 사라짐 | top 창 MessageBus 하나를 대시보드 전체가 씀. 응답의 `widgetId` 를 확인하고, `unsubscribe(topic)` 은 남의 구독까지 지우므로 쓰지 않음 | [Compass 에 선택 객체 알리기](/widget/compass-pubsub#주의) |
| 속성 패널 내용이 다른 위젯의 선택으로 바뀜 | `selectionType` 기본값이 `ALL_SELECTION`. `NO_SELECTION` 을 줌 | [selectionType](/controls/properties-panel#선택을-따라갈지-selectiontype) |
| 오른쪽 클릭 메뉴 callback 이 두 번 불림 (서버 요청 두 번) | 부모와 자식 요소(그리드를 감싼 요소 포함)에 ContextualMenu 를 따로 등록함. 바깥 한 곳에 등록하고 `e.info.target` 으로 나눔 | [오른쪽 클릭 메뉴](/controls/menu#오른쪽-클릭-메뉴-contextualmenu) |
| 2025 에서 메뉴 항목을 누르면 콘솔에 `ReferenceError: item is not defined` | submenu 가 함수인 항목을 하위 메뉴가 만들어지기 전에 누름 (2025 원본 오류, 2026 고침). 그런 항목에 action 을 두지 않음 | [하위 메뉴](/controls/menu#하위-메뉴) |
| 2025 에서 Enter 로 메뉴 항목을 고르면 메뉴가 다시 열림 | 메뉴를 연 WebUX Button 에 포커스가 남아 같은 Enter 를 받음. 2026 은 생기지 않음 | [키보드](/controls/menu#키보드) |
| 서버 검색 결과가 자동완성 목록에 안 보이고 "개체를 찾을 수 없음" | 기본 filterCB 가 결과를 label 로 다시 거름. `filterCB: function () {}` 와 `keepSearchResultsFlag: false` | [서버에서 찾기](/controls/autocomplete#서버에서-찾기) |
| 자동완성에서 고를 때마다 그리드 선택이 바뀜 | 그리드의 TreeDocument 를 elementsTree 로 넘김. 후보용 TreeDocument 를 따로 만듦 | [TreeDocument 후보](/controls/autocomplete#treedocument-후보) |
| ENOFloatingPanel 의 X / Esc 를 눌러도 닫히지 않음 | 옵션 `events.onClose` 가 있으면 자동 destroy 하지 않음. 그 안에서 `destroy()` 를 부름 | [떠 있는 패널](/controls/enox-components#떠-있는-패널-enofloatingpanel) |
| Esc 한 번에 떠 있는 패널이 모두 닫힘 | 패널마다 document 에 Esc 처리기를 검. `escapeToClose: false` 로 만들고 직접 처리 | [떠 있는 패널](/controls/enox-components#떠-있는-패널-enofloatingpanel) |
| `LinkManager.subscribe` 가 아무것도 받지 못함 / `publish` 가 TypeError | `ENOWidgetLinkEvents.json` 에 등록된 토픽만 처리함. 직접 만든 토픽은 WidgetLink 나 PlatformAPI 로 | [위젯 사이 연결](/controls/enox-components#위젯-사이-연결-linkmanager) |
| 크기 리스너 하나를 지웠더니 다른 리스너가 멈춤 | `Dom.removeResizeObserver` 가 항상 첫 항목을 지움 (2026 도 같음). 요소당 하나만 | [크기 변화 받기](/amd/utilities#크기-변화-받기) |
| 단독 실행에서 Me 설정 읽은 뒤 코드가 안 돎 | 2025 `BetaOptions.getMePreference` 는 대시보드 최상위 창이 없으면 Promise 가 끝나지 않음 | [BetaOptions](/widget/i18n-preferences#betaoptions-getmepreference) |

## 값이 이상할 때

| 증상 | 원인과 해결 | 자세히 |
| --- | --- | --- |
| `widget.getValue('pageSize') + 10` 이 `'2010'` | 설정 값은 문자열. `widget.getInt` 사용 | [값의 형태](/widget/preferences#값의-형태) |
| `addPreference` 로 기본값을 바꿔도 이전 값 | `getValue` 가 기본값까지 캐시함. `setValue` 로 저장 | [getValue 순서](/widget/preferences#getvalue-가-값을-찾는-순서) |
| `UWA.merge` 가 값을 안 덮어씀 | merge 는 빈 키만 채움. 덮어쓰기는 `UWA.extend` (깊은 복사는 세 번째 인자 `true`) | [Core 유틸](/uwa/core#extend) |
| `allSettled` 결과에 `status` 가 없음 | UWA Promise 는 `state: 'fullfilled'` (철자 그대로) | [Promise 메서드](/uwa/promise#메서드) |
| Model 에 `null` 을 넣었는데 기본값이 됨 | UWA Model 은 `null` 도 defaults 로 바꿈. 객체 기본값은 함수로 | [Model](/uwa/mvc#model) |
| Model `sync` 로 보낸 본문을 서버가 못 읽음 | `headers` 를 넘기면 기본 `Content-Type` 이 사라짐. 직접 넣음 | [서버 연동](/uwa/mvc#서버-연동) |
| WAFData 오류에서 상태 코드를 못 찾음 | `onFailure(error, data, headers)`. 상태 코드는 `error.message` 안에만 | [서버 호출 주의](/uwa/request#주의) |
| REST 가 200 인데 처리가 안 됨 | 실패도 200 으로 오는 경우가 있음. `res.success` 를 먼저 확인 | [REST 오류](/rest-api/#오류) |
| REST 가 SecurityContext 로 거부함 | 컨텍스트 문자열에는 `title` 이 아니라 `name` | [SecurityContext 형식](/rest-api/security-context#형식) |
| `/resources/v1/modeler/dslc/...` 호출만 실패함 (lifecycle 호출은 됨) | dslc (CAA) 는 서버가 `ENO_CSRF_TOKEN` 헤더를 검사함. 토큰을 받아 헤더에 넣음 | [성숙도 변경, 개정](/rest-api/lifecycle#rest-묶음) |
| 트리거가 막은 이유 대신 "성숙도를 변경하는 데 필요한 허가가 없습니다" 가 보임 | OOTB 성숙도 창이 `1500168`, `all requirements not satisfied` 가 든 문구를 권한 문구로 바꿈. REST 를 직접 부르고 `report[].error` 를 표시 | [서버에서 일어나는 일](/rest-api/lifecycle#서버에서-일어나는-일-트리거) |
| federated search 결과가 비었는데 오류가 없음 | 소스별 오류는 성공 응답의 `infos.sources[].error` 에 옴 (`FS_AUTHENTICATION_ERROR` 등) | [federated search 응답](/widget/search#응답) |
| 3DSearch 에서 받은 제목에 `&amp;` 가 보임 | 표시 값(`ds6w:label`)은 HTML 이스케이프됨. `ds6w:label_value` 처럼 `_value` 키를 씀 | [받는 데이터](/widget/search#받는-데이터) |
| getTypeInfo 의 하위 타입 목록에 기준 타입이 섞임 | 응답 키에 보낸 타입도 들어 있음. 하위 타입이 있는지는 키가 2 개 이상인지로 보고, 목록에서는 직접 뺌 | [타입, 속성 정보](/rest-api/dictionary#주의) |
| PersonSearchByName 에 다른 사람이 같이 옴 | 검색 가능한 칸에 그 로그인이 들어 있는 사람도 옴. `dataelements.name === 로그인` 으로 다시 거름 | [e6w](/rest-api/e6w#사람-찾기-personsearch) |
| enorelnav 결과에 다른 화면의 관계 설정이 섞임 | 세션 방식은 `setPreferences` 를 `widgetId` 로 다시 찾음. 한 번에 부를 때는 `widgetId` 에 시각을 붙여 새로 만들고, 세션이 끝나면 `clear` 를 부름 | [관계 탐색](/rest-api/navigate#주의) |
| v3 e6w 결과에서 `dataelements` 가 undefined | v3 는 `data[i].name` 처럼 바로 읽음. v2 와 읽는 위치가 다름 | [e6w 버전과 주소](/rest-api/e6w#버전과-주소) |
| OOTB 정보 창(ENOLCMT_AP)이 "아이템을 선택하지 않음" 으로 열림 | `objectId` 키는 읽지 않음. `SelectedItem` 배열을 넘기거나 `EditPropCmdBase().launchProperties` 를 씀 | [OOTB 정보 창](/controls/properties-panel#ootb-정보-창으로-열기) |
| 3DSpace 주소에 포트가 빠짐 | `get3DSpaceUrl()` 대체값은 포트 없이 `/3DSpace`. Compass 응답이 없었다는 뜻 | [get3DSpaceUrlAsync](/widget/widget-services#get3dspaceurlasync) |
| 한국어 JSON 이 있는데 영어가 나옴 | `폴더_en.js` 묶음이 있으면 JSON 으로 안 넘어감. 언어는 `widget.lang` 이 아니라 로더 설정 | [언어를 찾는 순서](/amd/plugins#언어를-찾는-순서) |
| 그리드 행을 rowID 로 다시 찾으니 다른 행 | rowID 는 펼침, 정렬, 필터로 바뀜. `nodeModel` 을 보관 | [cellID 와 rowID](/datagridview/api/grid-methods#먼저-알아-둘-것-cellid-와-rowid) |
| 그리드 컬럼 함수에서 `nodeModel` 이 undefined | 컬럼 헤더 셀(rowID -1)에도 호출됨. `cellInfos.nodeModel` 을 먼저 확인 | [nodeModel 이 있는지 확인하기](/datagridview/cell-functions#먼저-nodemodel-이-있는지-확인하기) |
| 직접 그린 그리드 셀에 다른 행의 모양이 섞임 | 꺼낸 셀 내용에 이전 행 값이 남아 있음. 글자, 속성, 스타일을 매번 모두 다시 넣음 | [onCellRequest](/datagridview/custom-cell#oncellrequest) |
| 변수를 바꾸고 `grid.updateView()` 를 불러도 그리드 셀이 그대로 | 옵션 없이 부르면 셀 내용을 다시 읽지 않음. `{ updateCellContent: true }` 를 넘기고, 범위가 좁은 `updateColumnView` 를 씀 | [화면 갱신](/datagridview/api/grid-methods#화면-갱신) |
| 그리드 셀에 `{}` 가 보임, 막은 셀이 항상 편집됨 | 컬럼 함수에 `async` 를 붙임 (Promise 를 돌려줌). 컬럼 함수는 받아 둔 값을 읽기만 함 | [세 가지 규칙](/datagridview/async-values#세-가지-규칙) |
| 서버 응답이 올 때마다 그리드에 입력하던 글자가 지워짐 | 응답마다 `updateView({ updateCellContent: true })`, 행 추가, 정렬을 부름. 값은 `updateOptions`, 목록은 그 컬럼의 `updateColumnView` 로 반영 | [응답이 왔을 때 사용자가 입력 중이면](/datagridview/async-values#응답이-왔을-때-사용자가-입력-중이면) |
| 그리드 콤보 셀에 값 대신 `Default` 가 보임 | `customEnumMember` 컬럼에 `possibleValues` 가 없음. 목록이 오기 전에는 빈 배열을 줌 | [컬럼 전체의 콤보 목록](/datagridview/async-values#패턴-1-컬럼-전체의-콤보-목록) |
| 그리드 콤보에서 첫 항목을 골라도 저장되지 않음, 고르지 않은 첫 항목이 셀에 보임 | 셀 값이 `''`, `null` 이거나 목록에 없음. 빈 값은 `undefined` 로, 지금 값은 목록에 넣음 | [콤보 셀의 값이 비었거나 목록에 없을 때](/datagridview/async-values#콤보-셀의-값이-비었거나-목록에-없을-때) |
| 그리드 드롭다운을 열 때 서버에서 목록을 받고 싶음 | 하나 선택은 `preDropdown` 이벤트에서 요청하고 그 컬럼을 다시 그림. 여러 개 선택은 `possibleValues` 에 함수. 묶어 둔 도우미 `lazyComboColumn` | [드롭다운을 열 때 목록 받기](/datagridview/async-values#패턴-5-드롭다운을-열-때-목록-받기) |
| 조회를 연달아 하면 그리드에 이전 결과가 남음 | 느린 응답이 나중에 와서 덮어씀. 요청 번호를 비교하거나 `GridUtils.loadRows` 를 씀 | [조회를 다시 할 때](/datagridview/async-values#조회를-다시-할-때-늦게-온-응답) |
| 나중에 채운 값 때문에 손대지 않은 그리드 행이 "변경됨" | 변경 트랜잭션 모드에서 이미 있는 키를 채움. 키를 미리 넣지 않거나 기록을 잠시 끔 | [변경 트랜잭션 모드와 같이 쓸 때](/datagridview/async-values#변경-트랜잭션-모드와-같이-쓸-때) |
| 2026 에서 툴바 layout 값의 `.value` 가 `undefined` | 2025 는 객체, 2026 은 문자열로 옴. `data.value || data` 로 읽음 | [보기 전환](/tweakers/generated-toolbar#보기-전환-layout-typeenums) |
| 숫자 편집기 decimals 0 인데 값에 소수가 남음 | `decimals` 는 화면 자릿수일 뿐. 정수로 저장하려면 `valueType: 'integer'` | [숫자](/tweakers/editors#숫자) |
| 그리드에서 `node.remove()` 해도 행이 남음 | 변경 트랜잭션 모드에서는 삭제 표시만 함. `acceptChanges()` 때 제거 | [행의 변경 상태](/datagridview/save-and-reflect#행의-변경-상태) |
| 행을 묶은 그리드에서 선택된 행, CSV 에 빈 행이 섞임 | 그룹 행(품번 칸에 `상태: 승인 (3)` 같은 제목만 있는 행)이 TreeDocument 에 실제로 들어감. 헤더 전체 선택 체크박스로도 선택됨. `getNodeSource()` 로 걸러냄. `getRoots()` 도 그룹 행을 돌려줌 | [그룹 행은 어떤 행인가](/datagridview/row-grouping#그룹-행은-어떤-행인가) |
| 행을 묶은 뒤 추가한 행이 그룹 밖에 나옴 | 자동으로 다시 묶지 않음. `grid.reapplyRowGrouping()` 호출 | [행을 추가한 뒤 다시 묶기](/datagridview/row-grouping#예제-9-행을-추가한-뒤-다시-묶기) |
| `dataSource` 그리드에 행이 안 들어옴 (2025) | `successCallback` 을 객체 한 개로 넘김. 인자를 나눠 `(rows, lastRow)` 로 넘김 (세 번째 전체 행 수는 `virtuallyLoadAllModelItems: true` 일 때만). `dataSource` 는 일반 객체가 아니라 `new DataSourceBase()` | [서버 사이드 데이터 주의](/datagridview/server-side#주의) |
| `dataSource` 그리드에서 가운데로 건너뛰면 빈 행만 보임 | 기본 방식에서 `successCallback` 세 번째 인자(전체 행 수)를 넘김. 빼거나 `virtuallyLoadAllModelItems: true` 를 씀 | [서버 사이드 데이터 주의](/datagridview/server-side#주의) |
| `virtuallyLoadAllModelItems: true` 를 줘도 행을 다 넣어야 함 | `dataSource` 없이는 효과 없음 (상태바 행 수 계산만 바뀜) | [서버 사이드 데이터 주의](/datagridview/server-side#주의) |
| 그리드 CSV 에 일부 행만 나옴 | 나눠 불러오는 그리드의 `getAsCSV()` 는 그 순간 불러온 행만 담음. 전체 내보내기는 서버에서 | [정렬, 필터, 찾기](/datagridview/server-side#정렬-필터-찾기는-어떻게-되나) |
| 그리드 행을 끌어 옮겼더니 정렬이 풀림 | 기본 이동이 `sortModel` 을 `[]` 로 비움. 그룹핑 중에는 순서 바꾸기가 꺼짐 | [정렬, 그룹핑, 필터와 같이 쓸 때](/datagridview/drag-and-drop#정렬-그룹핑-필터와-같이-쓸-때) |
| 목록 그리드에서 끌어 놓은 행이 다른 행의 하위로 들어감 | 행 가운데에 놓으면 하위로 넣는 것이 기본. `shouldAcceptDrop` 에서 `dropPosition === 'middle'` 을 거절 | [행 순서 바꾸기](/datagridview/drag-and-drop#예제-1-행-순서-바꾸기) |
| 그리드 `cellInfos.droppedNodes` 가 undefined | 같은 그리드 안에서 옮길 때만 들어 있음. 다른 곳에서 온 것은 `event.dataTransfer` 로 읽음 | [콜백이 받는 인자](/datagridview/drag-and-drop#콜백이-받는-인자) |
| 체크 / 라디오 메뉴 항목을 눌러도 표시가 안 바뀜 | Menu 는 state 를 바꾸지 않음. 앱이 값을 갖고 열 때마다 항목을 새로 만들고, 열어 둔 채면 `Menu.updateModel` | [체크와 라디오 항목](/controls/menu#체크와-라디오-항목) |
| 자동완성에서 Enter 를 누르면 목록 항목이 아니라 친 글자가 들어감 | 자유 입력이 켜져 있으면 친 글자 행이 맨 위에 미리 골라짐. ↓ 로 옮기거나 클릭 | [자유 입력과 칩](/controls/autocomplete#자유-입력과-칩) |
| 속성 패널 destroyComponent 에서 `reading 'destroy'` 오류 | `ALL` 이 아니면 탭 막대가 없음. try / catch 로 감싼 뒤 담는 요소를 비움 | [정리](/controls/properties-panel#다시-읽기-읽기-전용-크기-정리) |
| ENOXEmptyContent 문장의 태그가 그대로 해석됨 | `text` 를 innerHTML 로 넣음. 사용자 입력은 이스케이프 | [빈 화면 안내](/controls/enox-components#빈-화면-안내-enoxemptycontent) |
| `Utils.clamp` 결과가 이상함 | 인자 순서가 (최소, 최대, 값) | [Utils](/amd/utilities#utils) |
| 언어를 바꿨더니 위젯만 다시 읽히고 계정 언어는 그대로 | `I18n.setCurrentLanguage` 는 Me 설정 창 전용 (쿠키 + 2025 는 다시 읽기) | [I18n](/widget/i18n-preferences#ds-uwpclientcode-i18n) |
| 2026 에서 형식 언어가 `undefined`, 바꿔도 반영 안 됨 | `WUX.currentFormatsLanguage` 가 없어지고 `LanguageUtils.regionalFormatLanguage` 로 바뀜 | [2026](/widget/i18n-preferences#_2026) |
| 꺼진 Me 설정이 켜진 것처럼 동작 | `readPreferences` 값은 `'false'` 같은 문자열 | [Me 환경설정 읽기](/widget/i18n-preferences#me-환경설정-읽기) |
| select 결과가 String 이었다 StringList 였다 함, 여러 값인데 하나만 옴 | 읽는 메서드마다 다름. `getInfo` 는 첫 값만, 여러 객체 `getInfo` 와 `findObjects` 는 `\u0007` 로 이은 문자열, `getRelatedObjects` 는 여럿일 때만 StringList. 전부 받으려면 `getInfoList` 나 `multiSelects` | [값이 여러 개인 select](/jpo/api/read#값이-여러-개인-select) |
| attribute 값이 계속 비어 있음 | 이름 오타도 예외 없이 빈 값. MQL `print bus ... select ... dump` 로 확인 | [관계](/jpo/domain-object#관계) |
| 저장 함수에서 이전 값이 `null` | 이전 값 (`Old value`) 은 편집 폼만 넘김. 표 셀 저장과 생성 폼은 저장 전에 직접 읽음 | [저장](/jpo/args#저장-update-function) |
| 표 / 폼의 range 표시 이름이 안 나옴 | 폼은 `RangeDisplayValues`, 표는 `RangeDisplayValue` | [표 (Structure Browser)](/jpo/args#표-structure-browser) |
| 날짜 속성을 읽을 때 ParseException, 오전 / 오후가 틀림 | `SimpleDateFormat` 을 `Locale.US` 로 만듦 (`a` 는 `AM` / `PM`) | [날짜](/jpo/utils#날짜) |
| 로그인 사용자 정보 (`PersonUtil`) 가 User Agent 로 나옴 | `pushContext` 안에서 부름. push 전에 구해 둠 | [현재 사용자](/jpo/utils#현재-사용자) |
| 다른 사용자의 값이 보임 | `CacheUtil` 은 서버 전체가 같이 씀. 사용자별 값을 넣지 않음 | [서버 전체 캐시](/jpo/utils#서버-전체-캐시-cacheutil) |
| 화면에 문자열 키가 그대로 보임 | 키 오타나 배포 누락. 없을 때 키를 돌려주는 함수를 둠 | [문자열 리소스 Java](/properties/string-resource#java) |
| `_ko` 파일 한글이 깨짐 | `\uXXXX` 로 적음 | [파일 규칙](/properties/string-resource#파일-규칙) |
| 어떤 policy 에서는 state 이름이 안 맞음 | `In Work` / `InWork` 가 섞임. 심볼릭 이름 `state_InWork` 로 찾음 | [state 이름](/schema/policy#state-이름과-심볼릭-이름) |

## 서버 (JPO, MQL, 트리거)

| 증상 | 원인과 해결 | 자세히 |
| --- | --- | --- |
| 경고만 보여 주려 했는데 저장이 취소됨 | postProcess 에서 `Message` 만 넣으면 abort. `Action: "continue"` 를 함께 | [postProcessJPO](/jpo/args#postprocessjpo) |
| 생성 후처리에서 엉뚱한 객체를 수정 | `paramMap.objectId` 는 새 객체, `requestMap.objectId` 는 부모 | [postProcessJPO](/jpo/args#postprocessjpo) |
| "JPO Access Program Returns NULL" | 접근 함수는 `null` 대신 `false` 를 돌려줌 | [검사 순서](/jpo/args#검사-순서) |
| 표 열이 느림 | 행마다 `getInfo` 하지 말고 id 배열로 `DomainObject.getInfo` 한 번 | [표 열 function](/jpo/args#표-열-function) |
| `FrameworkUtil.split` 결과에 구분자 글자가 섞여 나옴 | 구분자는 한 글자만 씀. 구분자 문자열의 글자 하나하나를 구분자로 봄 | [빈 값, 문자열](/jpo/api/util/string-list) |
| 관계가 반대 방향으로 연결됨 | `DomainRelationship.connect(context, obj, rel, true, ids)` 는 `obj` 가 from, `obj.connect(context, rel, other, true)` 와 `createAndConnect(..., other, true)` 는 `other` 가 from | [연결](/jpo/api/write#연결) |
| 내보내기 / 인쇄에 HTML 이 섞임 | `reportFormat`, `exportFormat` 이 있으면 글자만 돌려줌 | [표 열 function](/jpo/args#표-열-function) |
| 이름에 `<` 나 따옴표가 든 객체에서 표가 깨짐 | HTML 열 값은 `XSSUtil.encodeForHTML` 로 감쌈 | [HTML 이스케이프](/jpo/utils#html-이스케이프) |
| commit 에서 엉뚱한 오류 | 앞에서 예외를 삼켜 트랜잭션이 aborting 상태. 로그를 남겨도 다시 던짐 | [예외](/jpo/i18n-exception#많이-쓰는-형태), [트랜잭션](/jpo/transaction-context#중첩-호출은-참조-카운트로-처리) |
| 안쪽에서 abort 했는데 변경이 남음 | 안쪽 abort 는 카운트만 줄임. 예외를 다시 던져 바깥도 abort | [트랜잭션](/jpo/transaction-context#중첩-호출은-참조-카운트로-처리) |
| 이후 처리가 슈퍼유저로 실행됨 | `popContext` 를 finally 에서 부르지 않음 | [pushContext](/jpo/transaction-context#슈퍼유저로-바꾸기-pushcontext) |
| 소유자가 User Agent 로 들어감 | `pushContext` 중에는 `context.getUser()` 가 바뀜. push 전에 담아 둠 | [pushContext](/jpo/transaction-context#슈퍼유저로-바꾸기-pushcontext) |
| 권한 없는 사용자가 수정됨 | `mqlCommand(context, cmd, true, ...)` 의 `true` 는 슈퍼유저 실행 | [boolean 인자](/jpo/mql-util#boolean-인자) |
| 이름에 공백이 있으면 MQL 이 깨짐 | 문자열을 이어 붙이지 말고 `$1` 사용 (인젝션 방지) | [기본 형태](/jpo/mql-util#기본-형태) |
| 다른 객체의 트리거도 안 돎 | `trigger off` 는 세션 전체. finally 에서 다시 켬 | [트리거 / 히스토리 끄기](/jpo/mql-util#트리거-히스토리-끄기) |
| 내 트리거를 붙였더니 OOTB 트리거가 빠짐 | 기존 이름 목록까지 함께 `input` 에 적음 | [admin 에 붙이기](/trigger/register#_2-admin-에-붙이기) |
| 화면에서 막았는데 다른 길로 통과 | 상태 규칙은 화면이 아니라 Check 트리거에 둠 | [화면에 보이는 것](/trigger/jpo-args#화면에-보이는-것) |
| `mod type ... add attribute` 실패 | attribute 를 먼저 만듦 (`01.attribute` 먼저 실행) | [나중에 고치기](/schema/admin#나중에-고치기) |
| 고친 properties 가 업그레이드 뒤 사라짐 | OOTB 파일 대신 Custom 파일, 내 앱 문자열 파일에 둠 | [고친 뒤 반영](/properties/#고친-뒤-반영) |
| command, table 을 MQL 로 고쳤는데 화면이 그대로 | 서버가 UI 정의를 기억해 둠. Reload Cache 실행 | [Reload Cache](/deploy/#reload-cache) |
| 커스텀 설정 파일 값을 고쳤는데 그대로 | `static` 블록에서 한 번만 읽는 값은 재시작해야 바뀜 | [설정 로더](/server-java/#설정-로더) |
| 커스텀 REST 가 실패했는데 `onFailure` 가 안 불림 | 예외여도 HTTP 200. 응답의 `result` 값을 확인 | [응답 형식](/server-java/rest-service#응답-형식) |
