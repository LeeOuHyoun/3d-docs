# 용어집

이 문서에 반복해서 나오는 말을 한 줄로 풀고, 자세한 설명이 있는 페이지로 잇습니다. 처음 읽을 때 막히는 말이 있으면 여기서 찾습니다.

## 제품과 실행 환경

| 용어 | 뜻 | 자세히 |
| --- | --- | --- |
| 3DEXPERIENCE | 다쏘시스템의 플랫폼 이름. ENOVIA 는 그 안의 데이터, 프로세스 관리 제품군 | [문서 소개](/guide/) |
| 3DSpace | 업무 데이터를 가진 서버(웹앱). JPO, MQL, REST 가 여기서 돎 | [서버 반영 절차](/deploy/) |
| 3DDashboard | 위젯을 올려 쓰는 화면. 위젯은 각자 iframe 에서 돎 | [위젯 개요](/widget/#실행-환경) |
| 3DPassport | 로그인(SSO) 서버 | [서버 호출](/uwa/request) |
| 3DCompass | 앱 목록과 서비스 주소를 알려 주는 서비스. 위젯이 고른 객체를 알려 두면 앱을 실행할 때 넘김 | [i3DXCompassServices](/widget/compass-services) · [Compass 에 선택 객체 알리기](/widget/compass-pubsub) |
| Platform Management | 구성원, 역할, 앱을 관리하는 관리자 화면 | [Additional App 으로 등록](/widget/dashboard-register#additional-app-으로-등록) |
| Additional App | 관리자가 주소를 넣어 Compass 에 추가한 앱 (위젯, 웹 페이지, PC 프로그램) | [Additional App 으로 등록](/widget/dashboard-register#additional-app-으로-등록) |
| Run Your App | 주소를 넣으면 그 위젯으로 바뀌는 OOTB 위젯. 개발 중 확인용 | [Run Your App 으로 열기](/widget/dashboard-register#run-your-app-으로-열기) |
| OOTB | Out Of The Box. 제품이 처음부터 주는 것. "커스텀" 의 반대말 | [OOTB 수정 규칙](/schema/conventions#ootb-수정-규칙) |
| tenant, 플랫폼 id | 한 서버 묶음을 가리키는 id. 온프레미스는 보통 `OnPremise` | [플랫폼 id](/widget/space-url#플랫폼-id) |
| webapps | 3DSpace 의 `webapps/` 폴더. 웹 화면 모듈이 폴더 하나씩 들어 있음 | [모듈 경로 색인](/guide/module-index) |

## 데이터 모델

| 용어 | 뜻 | 자세히 |
| --- | --- | --- |
| admin 객체 | 스키마를 이루는 정의. type, attribute, relationship, policy, role, command, table 등 | [admin 객체 한눈에](/schema/#admin-객체-한눈에) |
| business object | 업무 데이터 한 건. type 의 인스턴스 | [business object 식별](/schema/#business-object-식별) |
| TNR | type, name, revision. business object 를 구별하는 세 값 | [business object 식별](/schema/#business-object-식별) |
| id, physicalid | 시스템이 붙이는 객체 id. 웹(REST, 위젯)은 주로 physicalid (32 자리 16 진수) 를 씀 | [select 문법](/jpo/domain-object#객체) |
| type | 객체의 종류. 상속 구조를 가짐 | [type](/schema/admin#type) |
| attribute | type 이나 relationship 에 붙는 값 | [attribute](/schema/admin#attribute) |
| relationship | 두 객체를 잇는 연결. from 쪽과 to 쪽이 있음 | [relationship](/schema/admin#relationship) |
| 관계 id | 연결 하나하나에 붙는 id. select `id[connection]` 으로 읽고, 관계 속성을 고치거나 관계를 끊을 때 넘김 | [API 레퍼런스](/jpo/api/#_1-객체는-id-로-관계는-관계-id-로-가리킵니다) |
| policy | type 의 상태 흐름과 상태별 권한을 정한 것 | [policy](/schema/policy) |
| state, current | policy 안의 상태. 객체의 지금 상태가 `current` | [정의](/schema/policy#정의) |
| promote, demote | 다음 상태로 올리기, 이전 상태로 내리기 | [트리거 수명 주기](/trigger/lifecycle) |
| role | 권한을 주는 단위 | [role](/schema/admin#role) |
| vault | 객체의 저장 구역. 업무 데이터는 `eService Production` | [business object 식별](/schema/#business-object-식별) |
| 심볼릭 이름 | `type_Part` 처럼 코드가 스키마를 찾을 때 쓰는 별명. 실제 이름이 바뀌어도 코드를 고치지 않으려고 씀 | [심볼릭 이름](/schema/#심볼릭-이름) |
| MQL | Matrix Query Language. 스키마를 만들고 데이터를 다루는 명령 언어 | [MQL 기본](/schema/mql) |
| property | admin 객체에 붙이는 이름-값 표시. 설치 버전, 원래 이름 등을 적음 | [property](/schema/mql#property) |
| 채번기 | 이름을 접두어 + 일련번호로 자동으로 만드는 객체 (`eService Object Generator`) | [채번기](/schema/conventions#채번기-13-generator) |
| SecurityContext | `ctx::역할.조직.협업공간`. 어떤 자격으로 일하는지를 나타내는 문자열 | [SecurityContext](/rest-api/security-context#형식) |
| 협업공간 | Collaborative Space. 데이터를 나눠 담는 공간. 권한의 한 축 | [SecurityContext](/rest-api/security-context#형식) |

## 서버 화면 (UI 정의)

| 용어 | 뜻 | 자세히 |
| --- | --- | --- |
| command | 버튼이나 링크 하나. `href` 와 setting 을 가짐 | [command, menu](/ui-components/command-menu#command-정의) |
| menu | command 와 menu 의 묶음. 툴바, 탭, 객체 트리에 쓰임 | [menu 정의](/ui-components/command-menu#menu-정의) |
| table | 목록 화면의 컬럼 정의 | [table](/ui-components/table) |
| form (webform) | 속성 화면, 생성 화면의 필드 정의 | [form](/ui-components/webform) |
| portal, channel | 한 화면을 여러 칸으로 나누고 칸마다 탭을 넣는 구성 | [portal, channel](/ui-components/portal-channel) |
| setting | UI 정의에 붙이는 이름-값 옵션 (`Target Location`, `Registered Suite` ...) | [setting 사전](/ui-components/settings) |
| Structure Browser | 트리 형태의 목록 화면 (`emxIndentedTable.jsp`) | [table](/ui-components/table) |
| Registered Suite | 그 UI 정의가 속한 앱. 어떤 문자열 파일과 폴더를 쓸지 정함 | [공통 규칙](/ui-components/#공통-규칙) |
| StringResource | 화면 글자를 담은 properties. 언어별 파일이 있음 | [문자열 리소스](/properties/string-resource) |
| Reload Cache | 서버가 기억해 둔 UI 정의와 심볼릭 이름을 다시 읽게 하는 관리자 메뉴 | [Reload Cache](/deploy/#reload-cache) |

## 서버 코드

| 용어 | 뜻 | 자세히 |
| --- | --- | --- |
| JPO | Java Program Object. DB 에 저장되고 서버가 컴파일해 실행하는 Java class | [JPO 개요](/jpo/) |
| program 객체 | JPO 소스나 CSS 같은 코드를 담는 admin 객체 | [OOTB program 을 고칠 때](/deploy/#ootb-program-을-고칠-때) |
| `_mxJPO` | JPO 파일과 class 이름 끝에 붙는 접미어 | [이름 규칙](/jpo/#이름-규칙) |
| `${CLASSNAME}`, `${CLASS:이름}` | JPO 소스에서 자기 class, 다른 JPO class 를 가리키는 매크로 | [매크로](/jpo/#classname-class-이름) |
| 패키지 JPO | `package` 선언이 있는 JPO. 설정에는 `jpo.plmprovider.Connect` 처럼 패키지까지 씀 | [패키지가 있는 JPO](/jpo/#패키지가-있는-jpo) |
| `mxMain` | 메서드 이름 없이 JPO 를 실행할 때 불리는 기본 메서드. `execute program`, 트리거가 씀 | [MQL 에서 실행](/jpo/#mql-에서-실행-mxmain) |
| Context | 접속 사용자, 역할, 트랜잭션을 가진 객체. 거의 모든 API 의 첫 인자 | [Context 와 트랜잭션](/jpo/transaction-context) |
| programMap, paramMap, requestMap | 화면이 JPO 에 넘기는 `Map`. `JPO.unpackArgs(args)` 로 꺼냄 | [호출 형태별 인자](/jpo/args#공통) |
| packArgs, unpackArgs | `Map` 을 `String[]` 로 싸고 푸는 함수. JPO 인자 전달에 씀 | [다른 JPO 에서](/jpo/#다른-jpo-에서) |
| DomainObject | business object 를 다루는 Java class | [DomainObject](/jpo/domain-object) |
| select | 객체에서 읽을 값을 가리키는 표현식 (`attribute[X]`, `from[R].to.name`) | [select 문법](/jpo/domain-object#select-문법) |
| MapList, StringList | 조회 결과와 select 목록에 쓰는 컬렉션 | [MapList, StringList](/jpo/domain-object#maplist-stringlist) |
| pushContext | 잠깐 슈퍼유저로 바꿔 실행하는 호출. `popContext` 로 되돌림 | [pushContext](/jpo/transaction-context#슈퍼유저로-바꾸기-pushcontext) |
| trigger | 생성, 수정, promote 같은 이벤트에 걸어 두는 JPO 호출 | [트리거 개요](/trigger/) |
| Check, Action, Override | 트리거의 세 시점. 막기, 뒤처리, 대신 실행 | [Check, Action, Override](/trigger/#check-action-override) |
| 트리거 설정 객체 | `eService Trigger Program Parameters` type 의 객체. 어떤 JPO 를 어떤 인자로 부를지 담음 | [등록](/trigger/register#_1-설정-객체-만들기) |
| Modeler, Service | 커스텀 REST 주소를 만드는 두 class | [커스텀 REST 서비스](/server-java/rest-service) |
| 인터페이스 테이블 | 외부 시스템과 데이터를 주고받는 중간 테이블 | [외부 DB](/server-java/external-db#인터페이스-테이블) |

## 웹 (위젯, 화면 컨트롤)

| 용어 | 뜻 | 자세히 |
| --- | --- | --- |
| AMD | `define` / `require` 로 모듈을 불러오는 방식. 모듈 이름은 `DS/폴더/파일` | [AMD 로딩](/amd/) |
| 묶음 파일 | 한 폴더의 모듈들을 합쳐 minify 한 파일. 원본 분석의 주 대상 | [묶음 파일](/amd/#묶음-파일-concatenated-module) |
| UWA | 위젯 실행 틀과 기본 라이브러리 (class, DOM, 요청) | [UWA 개요](/uwa/) |
| widget | 3DDashboard 에 올리는 화면 단위. 전역 `widget` 객체로 설정과 이벤트를 다룸 | [위젯 개요](/widget/) |
| preference | 위젯마다 저장되는 설정 값 | [preference](/widget/preferences) |
| WAFData | 로그인 정보를 붙여 서버를 부르는 요청 모듈 | [서버 호출](/uwa/request) |
| CSRF 토큰 | 3DSpace 에 쓰기 요청을 할 때 먼저 받아 헤더에 넣는 값 | [CSRF 토큰](/uwa/request#csrf-토큰-3dspace-쓰기-요청) |
| PlatformAPI | 위젯 사이에 메시지를 주고받고 플랫폼 정보를 얻는 모듈 | [PlatformAPI](/widget/platform-api) |
| i3DXCompassServices | 이름이 비슷한 모듈이 둘. `DS/i3DXCompass/i3DXCompassServices` 는 서비스 주소, 사용자, 역할을 얻는 모듈, `DS/i3DXCompassServices/i3DXCompassPubSub` 은 Compass 에 고른 객체를 알리는 모듈 | [Compass 서비스](/widget/compass-services) · [Compass 에 선택 객체 알리기](/widget/compass-pubsub) |
| WebUX (WUX), UIKIT | 화면 컨트롤의 두 계열 | [두 계열의 차이](/controls/#두-계열의-차이) |
| DataGridView | WebUX 의 표 / 트리 그리드 | [DataGridView](/datagridview/) |
| TreeDocument, TreeNodeModel | 그리드의 데이터 모델. 문서 하나에 행(노드)들이 달림 | [구성 요소](/datagridview/#구성-요소) |
| 행 머리 (row header) | 그리드 맨 왼쪽의 행 번호, 체크박스, 끌기 손잡이가 있는 칸. cellInfos 에서는 columnID 가 -1 | [헤더의 구역](/datagridview/header#헤더의-구역) |
| 컬럼 그룹 | 컬럼 여러 개를 한 제목 아래로 묶은 2단 헤더. 컬럼 옵션 `children` 으로 만듦 | [헤더를 2단으로 묶기](/datagridview/header#헤더를-2단으로-묶기-컬럼-그룹) |
| layout (grid.layout) | 그리드의 컬럼 배치와 높이를 맡는 객체. 만든 뒤 컬럼을 숨기거나 너비를 바꾸는 함수가 여기에 있음 | [레이아웃 메서드](/datagridview/api/layout-methods) |
| 행 그룹핑, 그룹 행 | 같은 컬럼 값을 가진 행을 그룹 행 아래로 모으는 그리드 기능. 그룹 행은 그리드가 TreeDocument 에 만들어 넣는 행 | [행 그룹핑](/datagridview/row-grouping) |
| 사용자 뷰 (custom view) | 이름을 붙여 저장해 두는 그리드 컬럼 구성(표시, 너비, 순서, 고정, 정렬, 행 그룹). 화면에서는 "테이블 보기 구성..." | [사용자 뷰와 화면 상태 저장](/datagridview/custom-views) |
| 화면 상태 (user preferences) | 사용자가 그리드에서 바꾼 컬럼 너비, 순서, 표시, 정렬. `identifier` 가 있으면 그리드가 자동 저장 | [무엇이 어디에 저장되나](/datagridview/custom-views#무엇이-어디에-저장되나) |
| 서버 사이드 데이터 (dataSource) | 행을 다 넣지 않고 그리드가 필요한 범위만 서버에 요청하는 방식. `DataSourceBase` 객체의 `getModelItems` 를 덮어써서 씀 | [서버 사이드 데이터](/datagridview/server-side) |
| 무한 스크롤 (onInfiniteScroll) | 스크롤이 끝에 닿으면 불리는 콜백에서 TreeDocument 에 행을 이어 붙이는 방식 | [스크롤이 끝에 닿으면 행 더하기](/datagridview/server-side#예제-5-대안-스크롤이-끝에-닿으면-행-더하기) |
| dropPosition | 끌어 온 것이 행의 어디에 있는지. top(앞에 넣기), middle(하위로 넣기), bottom(뒤에 넣기) | [놓을 위치 표시](/datagridview/drag-and-drop#놓을-위치-표시) |
| typeRepresentation | 컬럼의 타입 이름. 셀 모양과 편집기를 정함 | [typeRepresentation](/datagridview/api/type-representation) |
| semantics | 편집기에 넘기는 세부 옵션 (`possibleValues`, `minValue` ...) | [semantics](/tweakers/api/semantics) |
| 자동완성 (string + possibleValues) | 타입 `string` 에 `possibleValues` 를 준 입력칸. 입력하며 고르고, 여러 개 선택과 자유 입력이 되는 방식 | [입력하며 고르기](/tweakers/editors#입력하며-고르기-검색-멀티-자유-입력) |
| 화면 모듈 (view module) | 편집기 안에서 모드(편집, 읽기)별 화면을 그리는 객체. `VIEW_MODULES` 로 고름 | [편집기 직접 만들기](/tweakers/custom-tweaker#화면-모듈) |
| setTweakerValue | 화면 모듈이 사용자 입력으로 편집기 값을 바꿀 때 부르는 메서드 | [값과 이벤트](/tweakers/custom-tweaker#값과-이벤트) |
| Tweaker | 값 하나를 보여 주고 고치는 편집기 컨트롤 | [Tweakers](/tweakers/) |
| XSO, CSO | 선택 목록 객체. CSO 는 그 창의 "현재 선택" | [선택](/events/selection) |
| ModelEvents | 화면 부품 사이에서 쓰는 publish / subscribe 객체 | [ModelEvents](/events/pubsub#modelevents) |
| 3DXContent | 앱 사이에 객체 목록을 넘기는 형식 `{ protocol, version, source, data: { items } }`. 끌어 놓기, Compass, 3DPlay 앱 열기에 씀 | [3DXContent 형식](/events/drag-and-drop#_3dxcontent-형식) |
| X3DContentId | 다른 앱이 이 위젯을 열면서 넘긴 3DXContent 를 담는 위젯 값 (JSON 문자열) | [플랫폼이 넣어 주는 값](/widget/preferences#플랫폼이-넣어-주는-값) · [3DPlay 앱으로 열기](/widget/3dplay#_3dplay-앱으로-열기-transientwidget) |
| in-context search | 위젯에서 3DSearch 화면을 열어 사용자가 고른 객체, 사람을 받아 오는 방식 (`RegisterContext`, `InContextSearch` 이벤트) | [3DSearch 에서 골라 오기](/widget/search) |
| federated search | 3DSearch 서비스의 검색 REST (`<3DSearch>/federated/search`). 결과는 `results[].attributes[]` | [화면 없이 검색](/widget/search#화면-없이-검색-federated-search) |
| precond | 3DSearch 검색에 늘 `AND` 로 붙는 조건식. 타입 제한에 씀 | [precond](/widget/search#precond) |
| furtive 모드 | in-context search 의 고르기 모드 (`mode: 'furtive'`). 결과 아래 취소 / OK 단추 | [옵션](/widget/search#옵션) |
| 6W 태그 | 무엇 / 누가 / 언제 / 어디서 / 왜 / 어떻게 로 나눈 태그. 3DDashboard 태그 창에서 골라 위젯 목록을 거름 | [6W 태그 연동](/widget/tagger) |
| 태그 창 (6W Tagger) | 3DDashboard 왼쪽의 태그 목록 화면. 위젯은 TagNavigatorProxy 로 태그를 보냄 | [6W 태그 연동](/widget/tagger) |
| sixw | 태그 분류 경로. `ds6w:what/ds6w:type` 처럼 6W 루트로 시작 | [태그 데이터](/widget/tagger#태그-데이터) |
| 주제 (subject) | 태그를 붙이는 행의 id. OOTB 는 `pid://` + physicalid | [행 id](/widget/tagger#행-id-주제) |
| 상단 바 (Top Bar) | 3DDashboard 맨 위 막대. 사용자 / + / 공유 / 도움말 메뉴가 있음 | [대시보드 상단 바](/widget/top-bar) |
| 성숙도 (maturity) | 객체의 policy state. 실제 이름 `stateSysName` 과 번역 이름 `stateUserName` 이 따로 있음 | [성숙도 변경, 개정](/rest-api/lifecycle) |
| 개정 (revise) | 같은 객체의 새 revision 을 만들기 | [성숙도 변경, 개정](/rest-api/lifecycle) |
| dslc | lifecycle REST 묶음. `/resources/v1/modeler/dslc/` 는 CAA 웹 서비스(CSRF 필요), `/resources/v1/dslc/` 는 버전 그래프 등 | [성숙도 변경, 개정](/rest-api/lifecycle) |
| MaturityCmd | OOTB 성숙도 창을 여는 명령 모듈 | [성숙도 변경, 개정](/rest-api/lifecycle) |
| Collaborative Lifecycle | 성숙도, 개정, 복제, 삭제 명령을 모은 OOTB 앱 (`ENOLCMI_AP`) | [성숙도 변경, 개정](/rest-api/lifecycle) |
| EditPropWidget (속성 패널) | OOTB 정보 화면(ID 카드, 탭, 속성 편집 / 저장)을 위젯 안에 넣는 컨트롤 | [속성 패널](/controls/properties-panel) |
| facet (탭) | 속성 패널의 탭 하나. `FACET_*` 상수, 또는 FacetsBase 를 상속한 모듈 | [탭](/controls/properties-panel#탭-facet) |
| ENOLCMT_AP | OOTB 정보(Information) 앱 id. EditPropWidget/EditPropWidget.html | [OOTB 정보 창](/controls/properties-panel#ootb-정보-창으로-열기) |
| ContextualMenu | `DS/Menu/ContextualMenu`. 요소에 오른쪽 클릭 메뉴를 붙이는 모듈. 눌린 요소부터 window 까지 등록된 callback 결과를 합쳐서 엶 | [오른쪽 클릭 메뉴](/controls/menu#오른쪽-클릭-메뉴-contextualmenu) |
| PushItem / CheckItem / RadioItem | DS/Menu 항목 종류(`type`): 일반 / 체크 / 하나만 고르기. 체크 상태는 앱이 관리 | [메뉴 항목](/controls/menu#항목) |
| elementsTree | WUXAutoComplete 의 후보 옵션 (배열, TreeDocument, 함수, Promise) | [후보를 주는 방법](/controls/autocomplete#후보를-주는-방법) |
| customNode | WUXAutoComplete 에서 자유 입력 글자나 후보에 없는 노드로 붙은 항목 (`node.options.customNode`) | [자유 입력과 칩](/controls/autocomplete#자유-입력과-칩) |
| ENOX | OOTB ENOVIA 앱(ENX*, ENO*)이 같이 쓰는 화면 부품 모음 (ENOXTriptych, ENOXIDCard, ENOXEmptyContent ...). 같은 ModelEvents 의 이벤트 이름으로 서로 묶임 | [ENOVIA 공용 화면 틀](/controls/enox-components) |
| Triptych | 왼쪽 / 가운데 / 오른쪽 3단 화면 (`DS/ENOXTriptych`) | [3단 화면](/controls/enox-components#_3단-화면-enoxtriptych) |
| 시작 패널 (Welcome Panel) | ENX 앱 왼쪽의 "앱 설명 + 할 일 목록" 패널 | [시작 패널](/controls/enox-components#시작-패널-다섯째-인자) |
| 위젯 연결 (Widget Link) | 사용자가 3DDashboard 에서 두 위젯을 연결하면 그 둘 사이에만 메시지를 보내는 기능 (`DS/WidgetLink/WidgetLink`) | [위젯 사이 연결](/controls/enox-components#위젯-사이-연결-linkmanager) |
| PointerEvents | 마우스, 터치를 `DSpointerdown` 같은 같은 이름으로 바꿔 보내는 WUX 이벤트. `multipleHitCount` 로 더블 클릭 | [공통 유틸](/amd/utilities#pointerevents) |
| WUX (전역) | `DS/Core/Core` 가 만드는 WUX 컨트롤 공통 객체 | [공통 유틸](/amd/utilities#ds-core-core-의-wux-함수) |
| 터치 모드 | WUX 컨트롤이 터치용 크기로 그리는 상태 (`TouchUtils.getTouchMode()`) | [공통 유틸](/amd/utilities#touchutils) |
| Me 환경설정 (Me preferences) | 사용자가 3DDashboard "Me" 메뉴에서 정한 값. 저장소(Repository)와 키로 읽음 | [언어와 사용자 환경설정](/widget/i18n-preferences#me-환경설정-읽기) |
| 지역 형식 언어 | 2026 Me 설정 `RegionalFormatLanguage`. 날짜, 숫자 형식에 쓰는 언어 | [언어와 사용자 환경설정](/widget/i18n-preferences#_2026) |
| swymlang | Me 메뉴에서 정한 언어를 담는 쿠키 | [언어와 사용자 환경설정](/widget/i18n-preferences#ds-uwpclientcode-i18n) |
| VEN 폴더 | 외부 라이브러리 원본을 담은 webapps 폴더 (VENCKEditor, VENHighChart ...). 위젯은 보통 DS 래퍼 모듈(`DS/CKEditor/CKEditor`)로 부름 | [외부 라이브러리](/amd/libraries#래퍼가-하는-일) |
| vu-kit (vuekit) | DS 의 Vue 화면 부품 모음 (`vu-btn`, `vu-modal` ...). Vue 2 는 vuekit, Vue 3 은 vuekit3 | [외부 라이브러리](/amd/libraries#vue) |
| 3DPlay | 3D / 2D 뷰어. 위젯 안에는 `Player3DPlayWeb`, 앱 id 는 `X3DPLAW_AP` | [3D / 2D 미리 보기](/widget/3dplay) |
| experience (3DPlay) | 3DPlay 가 타입과 usage 로 고르는 화면 모듈 (3DPlayModelViewer, 3DPlay2DExperience ...) | [타입별로 열리는 화면](/widget/3dplay#타입별로-열리는-화면) |
| ds6wg / ds6w URI (curi) | FedDictionaryAccessAPI 의 짧은 URI. `ds6wg:<타입>`, `ds6wg:<타입>.<속성>` 은 3DSpace, `ds6w:` 는 6W 검색 속성 | [타입, 속성 정보](/rest-api/dictionary#uri-표기) |
| e6w | 3DSpace 범용 서비스 `/resources/v2/e6w/service/...` (ObjectInfo, PersonSearch ...) | [e6w](/rest-api/e6w) |
| ExpresssionSearch | e6w 의 서버 설정 값 읽기. s 가 세 개인 것이 실제 경로 | [e6w](/rest-api/e6w#설정-값-읽기-expresssionsearch) |
| enoviaServer | Foundation 이 만드는 전역 객체 (tenant, SecurityContext, 서버 주소, e6w 파라미터) | [e6w](/rest-api/e6w#foundation-이-감싸는-방식) |
| 관계 패턴 | enorelnav 에서 관계 종류를 고르는 이름. `<관계>_to` 는 하위, 첨부 쪽, `<관계>_from` 은 상위, 사용처 쪽 | [관계 탐색](/rest-api/navigate#관계-패턴) |
| getEcosystem | 시작 객체에서 관계로 이어진 객체를 받는 enorelnav 요청 | [관계 탐색](/rest-api/navigate#getecosystem-요청) |

## 이 문서에서 쓰는 말

| 용어 | 뜻 | 자세히 |
| --- | --- | --- |
| 원본 분석 | minify 된 원본 소스를 읽어 정리한 내용 | [작성 규칙](/guide/contributing) |
| 서버 확인 | 실제 서버에서 동작을 확인한 내용 | [작성 규칙](/guide/contributing) |
| 검증 필요 | 소스만 보고 추정한 문장. 실무에 쓰기 전에 확인이 필요 | [검증 대기 목록](/guide/verify-list) |
| 근거 | 그 페이지가 어떤 파일을 보고 쓰였는지 적은 상자 | [작성 규칙](/guide/contributing) |
| 프로젝트 | 분석 대상인 커스터마이징 소스. OOTB 와 구별해서 부름 | [문서 소개](/guide/) |
| `My...`, `XX...` | 샘플에서 실제 이름 대신 쓰는 이름 | [작성 규칙](/guide/contributing) |
