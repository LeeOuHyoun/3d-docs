# 모듈 경로 색인

원본 소스의 `define([...])`, `require([...])` 에서 본 모듈 경로로 문서를 찾는 표입니다. 경로의 일부만 알면 <kbd>Ctrl</kbd> + <kbd>F</kbd> 로 이 페이지 안에서 찾습니다.

- `DS/폴더/모듈` 은 `webapps/폴더/` 아래 파일에 들어 있습니다. 한 파일에 여러 모듈이 묶여 있는 경우가 많습니다. → [AMD 로딩](/amd/)
- 여기 없는 모듈은 아직 분석하지 않은 것입니다. 없는 모듈이라는 뜻이 아닙니다.

## 로딩, 공통 유틸

| 모듈 | 내용 | 문서 |
| --- | --- | --- |
| `DS/WebappsUtils/WebappsUtils` | webapps 폴더 주소, 자산(이미지, 아이콘) 주소 | [WebappsUtils](/amd/webapps-utils) |
| `DS/WebappsUtils/Utils` | uuid, 쿠키, debounce, throttle | [WebappsUtils](/amd/webapps-utils) |
| `DS/WebappsUtils/Emitter`, `Inherit`, `Promise`, `Map`, `Console`, `Performance`, `BetaOptions` | 보조 모듈 | [WebappsUtils](/amd/webapps-utils) |
| `i18n!`, `css!`, `text!` 접두어 | 다국어, 스타일, 텍스트 플러그인 | [플러그인](/amd/plugins) |
| `DS/Utilities/Dom` | DOM 이벤트 등록 / 해제(addEventOnElement), 아이콘(generateIcon), 요소 찾기, 크기 감지 | [공통 유틸](/amd/utilities#dom) |
| `DS/Utilities/Utils` | debounce, throttle, applyMixin, localStorage 화면 상태, clamp | [공통 유틸](/amd/utilities#utils) |
| `DS/Utilities/TouchUtils` | 터치 모드인지 (getTouchMode) | [공통 유틸](/amd/utilities#touchutils) |
| `DS/Utilities/UUID`, `DS/Utilities/Array`, `DS/Utilities/KeyboardUtils` | UUID, 배열 도우미, 키 코드 상수 | [공통 유틸](/amd/utilities#uuid-array-keyboardutils) |
| `DS/Utilities/DateUtils`, `StringUtils`, `Object`, `DeviceUtils`, `Debug`, `Scroll`, `DocUtils`, `Simulate` | 날짜 비교, 지역 문자열 비교, 깊은 비교, 기기 판별, 디버그 로그 | [공통 유틸](/amd/utilities#그-밖의-ds-utilities-모듈) |
| `DS/Logger/Logger`, `DS/Logger/Appenders/Quake`, `DS/Logger/Appenders/GelfHttp` | 이름별 수준이 있는 로그, 출력 위치 | [공통 유틸](/amd/utilities#logger) |
| `DS/Core/PointerEvents` | 마우스 / 터치를 `DSpointer...` 이벤트로 (더블 클릭, 길게 누르기) | [공통 유틸](/amd/utilities#pointerevents) |
| `DS/Core/Core` | 전역 WUX, PointerEvents / TouchUtils 초기화, wux.css | [공통 유틸](/amd/utilities#ds-core-core-의-wux-함수) |
| `DS/ResizeSensor/js/ResizeSensor` | 요소 크기 변화 감지 | [공통 유틸](/amd/utilities#resizesensor) |
| `DS/WebUAUtils/WebUAUtils`, `DS/WebUtils/*` | 앱 도움말 열기 / 3DPlay 내부 도구 | [공통 유틸](/amd/utilities#webutils-webuautils) |

## 외부 라이브러리

| 모듈 | 내용 | 문서 |
| --- | --- | --- |
| `DS/Handlebars/Handlebars`, `DS/Handlebars/Handlebars4` | Handlebars 4.7.8 템플릿 | [외부 라이브러리](/amd/libraries#템플릿-handlebars) |
| `DS/vue/vue`, `DS/vue-3.4.15/vue`, `DS/vuekit/vuekit`, `DS/vuekit3/VUEKIT.umd` | Vue 2 / 3, DS 의 Vue 화면 부품(vu-kit) | [외부 라이브러리](/amd/libraries#vue) |
| `DS/React18Loader/React`, `DS/React18Loader/ReactDomClient`, `DS/ReactLoader/React`, `DS/ReactRedux/*` | React 18 (2026) / 17, Redux | [외부 라이브러리](/amd/libraries#react) |
| `DS/ENOjquery/ENOjquery`, `DS/ENO6WPlugins/jQuery`, `DS/WebjQueryLib/WebjQuery` | jQuery (2026 3.7.1 / 2025 2.1.1 / 3.5.1) | [외부 라이브러리](/amd/libraries#jquery) |
| `DS/3DXHighcharts/3DXHighcharts`, `DS/VENHighChart/UIHighCharts`, `DS/D3js_external/D3js` | Highcharts 4.0.3 / 11.4.4, d3 | [외부 라이브러리](/amd/libraries#차트) |
| `DS/egraph/*`, `DS/etree/*` | DS 의 그래프 / 트리 그리기 | [외부 라이브러리](/amd/libraries#그래프와-트리-그리기-egraph-etree) |
| `DS/CKEditor/CKEditor`, `DS/WUXRichText/CKEditor` | CKEditor 4 / CKEditor 5 컨트롤 (2026) | [외부 라이브러리](/amd/libraries#편집기) |
| `DS/PDFjs/PDFjs`, `DS/PDFLib/PDFLib`, `DS/ZipJS2/ZipJS2`, `DS/ZipJS/zip` | PDF, zip | [외부 라이브러리](/amd/libraries#문서와-파일) |
| `DS/VENMomentJS-2.23.0/moment-timezone`, `DS/VENMathjs/VENMathjs` | 날짜, 수학 | [외부 라이브러리](/amd/libraries#날짜와-수학) |
| `DS/ENODOMPurify/ENODOMPurify` | HTML 거르기 (DOMPurify) | [외부 라이브러리](/amd/libraries#보안과-데이터-처리) |
| `DS/Leaflet/LeafletLoader` | 지도 | [외부 라이브러리](/amd/libraries#지도와-공간) |
| `DS/DSNodeSocketioClient_4.7.4/DSNodeSocketioClient_4.7.4`, `DS/DSVideoJS/DSVideoJS` | socket.io, video.js | [외부 라이브러리](/amd/libraries#통신-미디어-입력) |

## 위젯, 플랫폼

| 모듈 | 내용 | 문서 |
| --- | --- | --- |
| `UWA/Widget` | 전역 `widget` 객체의 클래스 | [widget 객체](/widget/widget-api) |
| `UWA/Environments/Dashboard` | 위젯 실행 환경 (3DDashboard 가 아님) | [위젯 개요](/widget/) |
| `DS/PlatformAPI/PlatformAPI` | 로그인 사용자, 테넌트, 대시보드 설정, 위젯 간 publish / subscribe | [PlatformAPI](/widget/platform-api) |
| `DS/UWPClientCode/PublicAPI`, `DS/Tenant/Tenant`, `DS/PubSub/PubSub` | PlatformAPI 가 내부에서 쓰는 모듈 | [PlatformAPI](/widget/platform-api) |
| `DS/i3DXCompassPlatformServices/i3DXCompassPlatformServices` | 서비스 주소(3DSpace, 3DSwym ...), 역할, 앱 정보 | [Compass 서비스](/widget/compass-services) |
| `DS/i3DXCompass/i3DXCompassServices`, `DS/i3DXCompass/i3DXCompass` | 위 모듈의 실제 구현 | [Compass 서비스](/widget/compass-services) · [Compass 가 객체로 하는 일](/widget/compass-pubsub#compass-가-객체로-하는-일) |
| `DS/i3DXCompass/ServiceApi/GrantedRolesApi`, `PortfolioApi` | 부여된 역할 조회 | [Compass 서비스](/widget/compass-services#getgrantedroles) |
| `DS/i3DXCompassPlatformServices/OpenWith` | "다른 앱으로 열기" 메뉴 | [Compass 서비스](/widget/compass-services#openwith) |
| `DS/WidgetServices/WidgetServices` | 3DSpace 주소, SecurityContext 보조 함수 | [WidgetServices](/widget/widget-services) |
| `DS/TransientWidget/TransientWidget` | 다른 앱(위젯)을 창으로 열기 | [TransientWidget](/widget/transient-widget) |
| `DS/WidgetServices/securityContextServices/SecurityContextServices` | SecurityContext 목록과 선택 값 | [WidgetServices](/widget/widget-services) · [3DSpace 주소와 SecurityContext](/widget/space-url) |
| `RunYourApp/RunYourApp.html` | 주소를 넣으면 그 위젯으로 바뀌는 OOTB 위젯 | [Run Your App](/widget/dashboard-register#run-your-app-으로-열기) |
| `DS/PlatformManagement/Modal/ThirdPartyApps` | Additional App 등록 창 (Platform Management) | [Additional App](/widget/dashboard-register#additional-app-으로-등록) |
| `DS/PlatformManagement/Model/AppModel`, `Collection/AppsCollection` | 등록한 앱의 저장, 목록 조회 (`app/custom`) | [저장 요청](/widget/dashboard-register#저장-요청) |
| `DS/UWPClientCode/I18n` | 지금 언어, 지원 언어 목록, `{이름}` 바꾸기 | [언어와 사용자 환경설정](/widget/i18n-preferences#ds-uwpclientcode-i18n) |
| `DS/Utilities/LanguageUtils`, `DS/Utilities/NumberUtils` <Badge type="warning" text="2026" /> | 지역 형식 언어, ISO 8601, 숫자를 글자로 | [언어와 사용자 환경설정](/widget/i18n-preferences#_2026) |
| `DS/MePreferencesClientAPI/MePreferencesClientAPI` | Me 환경설정 읽기(readPreferences), Me 설정 창(getMePreferencesDialog) | [언어와 사용자 환경설정](/widget/i18n-preferences#me-환경설정-읽기) |
| `DS/WebappsUtils/BetaOptions` | Me 설정 값 하나 읽기 (최상위 창 경유) | [언어와 사용자 환경설정](/widget/i18n-preferences#betaoptions-getmepreference) |
| `DS/MePreferencesClientCacheMgr/*`, `DS/MePreferencesClientUtilities/*`, `DS/WebSystem/Nls` | Me 설정 창 내부용 / 3DPlay CATNls 로더 | [언어와 사용자 환경설정](/widget/i18n-preferences#그-밖의-모듈) |
| `DS/SNInfraUX/SearchCom` | 3DSearch 를 열어 고른 객체 / 사람 받기 (in-context search 소켓) | [3DSearch 에서 골라 오기](/widget/search) |
| `DS/SNInfraUX/Api/SearchApi` | federated search 요청 `search()` | [3DSearch 에서 골라 오기](/widget/search#searchapi-로-부르기) |
| `DS/SNInfraUX/SearchUtils` | 3DSearch 내부 도우미 (위젯에서 직접 쓸 일 거의 없음) | [3DSearch 에서 골라 오기](/widget/search#부르는-법) |
| `DS/SNSearchUX/InContextSearch` | in-context search 를 받는 쪽 (상단 3DSearch) | [3DSearch 에서 골라 오기](/widget/search#동작-방식) |
| `DS/E6WCommonUI/Search` | E6WCommonUI 의 in-context search 래퍼 | [3DSearch 에서 골라 오기](/widget/search#searchcom) |
| `DS/SearchWidget/SearchWidget` | 사람, 조직, 협업 공간을 입력창에서 찾는 컨트롤 | [3DSearch 에서 골라 오기](/widget/search#searchwidget-과-다른-점) |
| `DS/TagNavigatorProxy/TagNavigatorProxy` | 위젯 목록에 6W 태그를 붙이고 태그 창에서 고르면 거르기 (`createProxy`) | [6W 태그 연동](/widget/tagger) |
| `DS/TagNavigatorProxy/Tagger6WProxy`, `Tagger6WProxyWithFilteringServices`, `AbstractTagger6WProxy` | createProxy 가 만드는 proxy (서버로 거르기 / 화면에서 거르기) | [6W 태그 연동](/widget/tagger#고른-태그-받기) |
| `DS/TagNavigator/TagNavigator` | 태그 창 본체 (InterCom 서버 `com.ds.tagger6w`) | [6W 태그 연동](/widget/tagger#동작-방식) |
| `DS/i3dxUtils6W/i3dxUtils` <Badge type="warning" text="2026" /> | `i3dx:도메인!id` 주소 나누기, 도메인 레지스트리 | [6W 태그 연동](/widget/tagger#_2026-변경) |
| `DS/i3DXCompassServices/i3DXCompassPubSub` | Compass 에 고른 객체 알리기, 앱 실행 요청 (`setObject`, `setX3DContent`, `launchApp`) | [Compass 에 선택 객체 알리기](/widget/compass-pubsub) |
| `DS/i3DXCompassServices/i3DXCompassServices` | caa 용 getServiceUrl / getPlatformServices, getCompatibleApps | [Compass 에 선택 객체 알리기](/widget/compass-pubsub#_2026-변경) |
| `DS/MessageBus/MessageBus` | 채널 publish / subscribe 버스 (PlatformAPI, i3DXCompassPubSub 가 씀) | [Compass 에 선택 객체 알리기](/widget/compass-pubsub#동작-방식) |
| `DS/TopBarProxy/TopBarProxy` | 대시보드 상단 바 메뉴(+, 공유, 도움말, 사용자)에 위젯 명령 넣기 | [대시보드 상단 바](/widget/top-bar) |
| `DS/TopBarProxy/MenuItem`, `Menu`, `MenuBarItem` | 상단 바 항목 / 항목 목록 / 칸 | [대시보드 상단 바](/widget/top-bar#항목-형식) |
| `DS/TopBar/model/MainMenuBar` | 상단 바 쪽 (InterCom 서버 `MainMenuBar`, `setActiveMenuProviders`) | [대시보드 상단 바](/widget/top-bar#동작-방식) |
| `DS/3DPlayHelper/3DPlayHelper` | 위젯 안 3D / 2D 미리 보기 (`Player3DPlayWeb`) | [3D / 2D 미리 보기](/widget/3dplay) |
| `DS/3DPlay/3DPlay` | 3DPlay 커널 (experience 생성, ctx 별 이벤트) | [3D / 2D 미리 보기](/widget/3dplay#같이-보이는-모듈) |
| `DS/3DPlaySupport/AssetSupport`, `SupportList` | 3DPlay 로 볼 수 있는지 확인, 타입별 화면 | [3D / 2D 미리 보기](/widget/3dplay#볼-수-있는지-먼저-확인하기) |
| `3DPlayHelper/3DPlaySyndication.html` | 3DPlay 를 iframe 으로 띄우는 페이지 | [3D / 2D 미리 보기](/widget/3dplay#iframe-으로-띄우기-3dplaysyndication-html) |

## 서버 호출

| 모듈 | 내용 | 문서 |
| --- | --- | --- |
| `DS/WAFData/WAFData` | 3DSpace REST 호출 (`authenticatedRequest`) | [서버 호출](/uwa/request) · [REST 공통 규칙](/rest-api/) |
| `UWA/Data` | UWA 의 요청 함수. 3DSpace 호출에는 직접 쓰지 않음 | [서버 호출](/uwa/request) |
| `DS/LifecycleCmd/MaturityCmd` | OOTB 성숙도 창(`executeAsync`), 작은 성숙도 표시(`executeCompactViewAsync`) | [성숙도 변경, 개정](/rest-api/lifecycle) |
| `DS/LifecycleCmd/ReviseCmd`, `HistoryCmd`, `DuplicateCmd`, `DeleteCmd` | OOTB 개정, revision 목록, 복제, 삭제 창 | [성숙도 변경, 개정](/rest-api/lifecycle) |
| `DS/LifecycleCmd/MessageMediator` | lifecycle 명령 사이 이벤트 `onModification` | [성숙도 변경, 개정](/rest-api/lifecycle) |
| `DS/LifecycleServices/LifecycleServicesSettings` | lifecycle REST 주소, tenant, 헤더 (`app_initialization`, `get3DSpaceWSUrl`, `getHeaders`) | [성숙도 변경, 개정](/rest-api/lifecycle) |
| `DS/LifecycleServices/LifecycleServices` | SecurityContext, CSRF, 오류 문구, 개정 방식 조회 | [성숙도 변경, 개정](/rest-api/lifecycle) |
| `DS/LifecycleServices/LifecycleAlert` | 알림 띄우기 `displayNotification` | [성숙도 변경, 개정](/rest-api/lifecycle) |
| `DS/MaturityWidget/MaturityWidget`, `DS/ChangeMaturityWidget/ChangeMaturityWidget`, `DS/ReviseWidget/ReviseWidget` | 성숙도 창, 작은 성숙도 표시, 개정 창의 본체 | [성숙도 변경, 개정](/rest-api/lifecycle) |
| `DS/LifecycleControls/EditPropDialog`, `CommandDialog` | 속성 창(`ENOLCMT_AP`), 명령 창 틀 | [성숙도 변경, 개정](/rest-api/lifecycle) · [OOTB 정보 창](/controls/properties-panel#ootb-정보-창으로-열기) |
| `DS/ENOCollabSharingCmds/commands/TransferOwnershipCmd/ActionBar_TransferOwnershipCmd` | OOTB 소유자 변경 창 | [성숙도 변경, 개정](/rest-api/lifecycle) |
| `DS/FedDictionaryAccess/FedDictionaryAccessAPI` | 타입, 속성 이름 번역, 속성 목록, 값 번역, 확장 (`getResourcesInfo`, `getNlsOfPropertiesValues`, `getPropertiesForClass`, `getExtensionsForClass`) | [타입, 속성 정보](/rest-api/dictionary#feddictionaryaccessapi) |
| `DS/FedDictionaryAccess/FedDictionaryAccess3DSpace` | FedDictionaryAccessAPI 가 부르는 3DSpace `6WVocab/access` REST 계층 (안쪽에서만 씀) | [타입, 속성 정보](/rest-api/dictionary#안에서-부르는-rest) |
| `DS/SearchDictionaryAccess/SearchDictionaryAccessAPI` | 검색용 사전 (`getNlsOfSearchValuesBySource`, `get6wResourcesInfo`) | [타입, 속성 정보](/rest-api/dictionary#값-번역-getnlsofpropertiesvalues) |
| `DS/SixwDictionaryAccess/SixwDictionaryAccessAPI` | 6W 어휘 (`get6wVocabularies`, `get6wProperties`, `get6wPropertiesRoots`, `get6wRangeValues`) | [타입, 속성 정보](/rest-api/dictionary#api-함수) |
| `DS/i3DXSNDictionaryAPI/3DXSearchDictionaryAccess` | 예전 검색 사전. 2025 에만 있고 부르는 앱 없음 | [타입, 속성 정보](/rest-api/dictionary#_2026-변경) |
| `DS/Foundation/FoundationAjax` | e6w 호출 도우미 (`setupEnoviaServer`, `ajaxRequest`) | [e6w](/rest-api/e6w#foundation-이-감싸는-방식) |
| `DS/Foundation/FoundationData` | v1 e6w (`loadWidget`, `applyUpdates`) | [e6w](/rest-api/e6w#foundation-이-감싸는-방식) |
| `DS/Foundation2/FoundationV2Data` | v2 e6w (`loadServiceData`, `getWidgetConstants`) | [e6w](/rest-api/e6w#foundation-이-감싸는-방식) |
| `DS/DocumentManagement/ObjectInfoService` | v3 ObjectInfo (`$ids=` form POST) | [e6w](/rest-api/e6w#objectinfo) |
| `DS/ENORIPE_Search/WhereUsed` | 3DSearch 의 `action_RelatedObjects` 동작. 관계 앱(ENORIPE_AP)을 엶 | [관계 탐색](/rest-api/navigate#엔드포인트) |
| `DS/ENORIPE_Columns/WSRelations` | enorelnav `getRelationCount`, `getEcosystem` 래퍼 | [관계 탐색](/rest-api/navigate#개수만-받기-getrelationcount) |

## UWA

| 모듈 | 내용 | 문서 |
| --- | --- | --- |
| `UWA/Core` | 기반 함수 (`UWA.extend`, `UWA.createElement` ...) | [Core 유틸](/uwa/core) |
| `UWA/Utils`, `UWA/String`, `UWA/Array`, `UWA/Json` | 유틸 | [Core 유틸](/uwa/core) |
| `UWA/Utils/Client` | 브라우저, OS, 기능 지원 여부 | [Core 유틸](/uwa/core#환경-정보-utils-client) |
| `UWA/Element`, `UWA/Event` | DOM 요소 확장, 이벤트 | [DOM](/uwa/element) |
| `UWA/Class` | 클래스 정의 (`extend`, `_parent`) | [클래스](/uwa/class) |
| `UWA/Class/Options`, `Events`, `Listener`, `Timed`, `Debug` | 클래스에 섞어 쓰는 기능 | [클래스](/uwa/class) |
| `UWA/Class/Model`, `Collection`, `View`, `Store` | Model, Collection, View | [Model, Collection, View](/uwa/mvc) |
| `UWA/Promise`, `UWA/Class/Promise` | Promise | [Promise](/uwa/promise) |
| `UWA/Utils/InterCom` | 위젯 간 통신 | [InterCom](/uwa/intercom) |
| `UWA/Controls/Popup`, `Overlay`, `Lightbox` | 브라우저 새 창, 덮는 층 (다이얼로그 아님) | [다이얼로그](/controls/dialog#uwa-로-다이얼로그를-띄우는가) |

## 화면 컨트롤

| 모듈 | 내용 | 문서 |
| --- | --- | --- |
| `DS/Controls/Button` | 버튼 | [WebUX 컨트롤](/controls/webux-controls#button) · [API](/controls/api/webux-controls) |
| `DS/Controls/Toggle` | 체크박스, 스위치, 라디오 | [WebUX 컨트롤](/controls/webux-controls#toggle) |
| `DS/Controls/LineEditor`, `DS/Controls/Editor` | 한 줄 / 여러 줄 입력 | [WebUX 컨트롤](/controls/webux-controls#lineeditor-editor) |
| `DS/Controls/ComboBox` | 선택 목록 | [WebUX 컨트롤](/controls/webux-controls#combobox) |
| `DS/Controls/DatePicker`, `TimePicker`, `Calendar`, `DS/Tweakers/TweakerDate` | 날짜, 시간 입력 | [WebUX 컨트롤](/controls/webux-controls#날짜-datepicker-tweakerdate) · [API](/controls/api/webux-controls#datepicker) |
| `DS/Controls/Loader`, `DS/Controls/ModalLoader` | 로딩 표시, 로딩 가리개 | [WebUX 컨트롤](/controls/webux-controls#loader-modalloader) |
| `DS/Controls/ButtonGroup`, `Label`, `Expander`, `Popup`, `ProgressBar` | 그 밖의 WebUX 컨트롤 | [WebUX 컨트롤 API](/controls/api/webux-controls) |
| `DS/Controls/SpinBox`, `Slider`, `ColoredSlider`, `GradientSlider`, `Knob`, `Switch` | 숫자 입력, 슬라이더 | [입력 컨트롤](/controls/input-controls) · [API](/controls/api/input-controls) |
| `DS/Controls/SelectionChips`, `SelectionChipsEditor`, `Find`, `FilePicker`, `SmartInput` | 칩, 찾기, 파일 선택 | [입력 컨트롤](/controls/input-controls) · [API](/controls/api/input-controls) |
| `DS/Controls/AbstractDecorator`, `CustomizableDecorator`, `CustomizableDecoratorItems`, `DecoratorManager`, `SpinBoxDecorator` | 터치 입력 키패드 | [입력 컨트롤](/controls/input-controls) · [API](/controls/api/input-controls#abstractdecorator) |
| `DS/Controls/Accordeon`, `Tab`, `TabBar`, `Toolbar`, `Breadcrumb`, `GroupBox`, `Flyout` | 펼침 목록, 탭, 툴바, 경로 표시 | [레이아웃 컨트롤](/controls/layout) · [API](/controls/api/layout) |
| `DS/Controls/BadgesContainer`, `Scroller`, `Stack`, `LayerSet`, `Deck`, `Stepper`, `DropdownButton`, `ModeChooser` | 그 밖의 레이아웃 컨트롤 (뒤의 셋은 2026) | [레이아웃 컨트롤](/controls/layout) · [API](/controls/api/layout) |
| `DS/Controls/ColorPicker`, `ColorChooser`, `ColorPalette`, `ColorCanvas`, `ColorEditors` | 색상 선택 | [색상, 표시 컨트롤](/controls/display-controls) · [API](/controls/api/display-controls) |
| `DS/Controls/TimeChooser`, `Gauge`, `Timeline` | 시간 팝업, 게이지, 타임라인 | [색상, 표시 컨트롤](/controls/display-controls) · [API](/controls/api/display-controls) |
| `DS/Controls/Tooltip`, `TooltipLoader`, `AbstractTooltipView`, `StandardTooltipView`, `RichUserTooltipView`, `UserCard`, `BaseCard` | 툴팁 화면, 사용자 카드 (뒤의 다섯은 2026) | [색상, 표시 컨트롤](/controls/display-controls#tooltip) |
| `DS/Controls/ResponsiveTileView`, `ResponsiveThumbnailView`, `ResponsiveTextualTileView`, `ResponsiveViewAdaptor` | 카드 (타일, 썸네일) | [목록, 트리, 타일 뷰](/controls/collection-views) · [API](/controls/api/collection-views) |
| `DS/Controls/TreeBase`, `TreeNodeBase`, `Tree`, `TreeNode`, `Tile`, `TileView`, `ThumbnailView`, `TileCellView`, `ThumbnailCellView`, `ViewBase` | DOM 트리, 옛 타일 / 썸네일 | [목록, 트리, 타일 뷰](/controls/collection-views) · [API](/controls/api/collection-views) |
| `DS/ControlsEx/Card`, `IDCard`, `LoadingPanel`, `CodeMirror`, `PathExplorer`, `Wizard`, `MagnitudeEditor` | 확장 컨트롤 | [확장 컨트롤 (ControlsEx)](/controls/controls-ex) |
| `DS/Controls/Abstract`, `DS/Controls/AbstractEditor`, `DS/Core/BaseComponent` | WebUX 컨트롤의 공통 바탕 | [WebUX 컨트롤 API](/controls/api/webux-controls) |
| `DS/Core/TooltipModel`, `DS/Controls/TooltipModel` | 툴팁 내용 | [WebUX 컨트롤](/controls/webux-controls#툴팁) |
| `DS/Windows/Dialog` | 모달 다이얼로그 | [다이얼로그](/controls/dialog) · [Windows API](/controls/api/windows) |
| `DS/Windows/ImmersiveFrame`, `Panel`, `Window` | 다이얼로그를 올리는 틀, 패널 | [Windows API](/controls/api/windows) · [창 묶기, 붙이기](/controls/window-layout) |
| `DS/Windows/WindowGroup`, `WindowGroupAsTab`, `WindowGroupAsGrid` | 패널 묶음 (탭 / 격자) | [창 묶기, 붙이기](/controls/window-layout#탭으로-묶기) · [API](/controls/api/windows#windowgroup-windowgroupastab-windowgroupasgrid) |
| `DS/Windows/ImmersiveWindow` | 붙지 않는 떠 있는 창 | [창 묶기, 붙이기](/controls/window-layout#떠-있는-도구-창-immersivewindow) · [API](/controls/api/windows#immersivewindow) |
| `DS/Windows/WindowManager`, `WindowActivator` | 활성 창, 맨 앞으로 | [창 묶기, 붙이기](/controls/window-layout#활성-창-맨-앞으로-windowmanager) · [API](/controls/api/windows#windowmanager-windowactivator) |
| `DS/Windows/DockingElement`, `DockingElementForImmersiveFrame` | 분할 레이아웃, ImmersiveFrame 가장자리 영역 | [분할 레이아웃](/controls/docking-element) · [API](/controls/api/windows#dockingelement) |
| `DS/Notifications/NotificationsManagerUXMessages`, `NotificationsManagerViewOnScreen` | 알림 메시지 | [알림 메시지](/controls/notifications) |
| `DS/UIKIT/Modal`, `DS/UIKIT/SuperModal` | UIKIT 모달, 확인 창 | [UIKIT](/controls/uikit) · [UIKIT API](/controls/api/uikit) |
| `DS/UIKIT/Mask` | 로딩 가리개 | [UIKIT](/controls/uikit#mask) |
| `DS/UIKIT/Alert`, `DropdownMenu`, `Form`, `Popover`, `Tooltip` | UIKIT 컨트롤 | [UIKIT](/controls/uikit) |
| `DS/UIKIT/Input/Button`, `IconButton`, `Select`, `Text`, `Toggle` | UIKIT 입력 컨트롤 | [UIKIT](/controls/uikit) · [무엇을 쓸까](/controls/#무엇을-쓸까) |
| `DS/Menu/Menu` | 팝업 메뉴 (Menu.show, 항목 형식, 체크 / 라디오, 하위 메뉴, 키보드) | [메뉴](/controls/menu) |
| `DS/Menu/ContextualMenu` | 요소에 오른쪽 클릭 메뉴 연결 (addEventListener, buildAndDisplayContextualMenu) | [메뉴](/controls/menu#오른쪽-클릭-메뉴-contextualmenu) |
| `DS/WUXAutoComplete/AutoComplete` | 자동완성 입력 (하나 / 여러 개 칩, 자유 입력, 서버 검색) | [자동완성 입력](/controls/autocomplete) |
| `DS/UIKIT/Autocomplete` | UIKIT 자동완성 (datasets, remoteSearchEngine). 비교만 있음 | [자동완성 입력](/controls/autocomplete#비슷한-것과-고르기) |
| `DS/EditPropWidget/EditPropWidget` | OOTB 속성(정보) 화면을 내 요소에 넣는 패널 (ID 카드, 탭, 속성 편집 / 저장) | [속성 패널](/controls/properties-panel) |
| `DS/EditPropWidget/constants/EditPropConstants` | typeOfDisplay, selectionType, displayType, FACET_* 상수 | [속성 패널](/controls/properties-panel#상수-editpropconstants) |
| `DS/EditPropWidget/models/EditPropModel` | 패널에 넣는 객체 모델 (physicalid), onSave / onChangeAttributes | [속성 패널](/controls/properties-panel#객체-넣기-initdatas) |
| `DS/EditPropWidget/facets/Common/FacetsBase` | 속성 패널 탭(facet)의 바탕 클래스 | [직접 만든 탭](/controls/properties-panel#직접-만든-탭) |
| `DS/EditPropWidget/utils/EditPropUtils` | 선택 노드를 모델로 바꿈 (getModelFromSelectedNode) | [속성 패널](/controls/properties-panel#객체-넣기-initdatas) |
| `DS/EditPropWidget/EditPropComponent`, `dockablecomponent/EditPropDockableComponent`, `factory/EditPropComponentFactory` | 속성 패널의 팝업 / 도킹 창 형 | [팝업과 도킹 창](/controls/properties-panel#팝업과-도킹-창) |
| `DS/ENOCollabSharingCmds/commands/EditPropCmd/EditPropCmdBase` | OOTB 정보(Information) 창 (ENOLCMT_AP) 을 여는 명령 | [OOTB 정보 창](/controls/properties-panel#ootb-정보-창으로-열기) |
| `DS/ENOXTriptych/js/ENOXTriptych` | 왼쪽 / 가운데 / 오른쪽 3단 화면 (ModelEvents 명령 `triptych-*`) | [ENOVIA 공용 화면 틀](/controls/enox-components#_3단-화면-enoxtriptych) |
| `DS/ENOXWelcomePanel/js/ENOXWelcomePanel` | 3단 화면 왼쪽 시작 패널 (Triptych 다섯째 인자) | [ENOVIA 공용 화면 틀](/controls/enox-components#시작-패널-다섯째-인자) |
| `DS/ENOFloatingPanel/ENOFloatingPanel` | 위젯 안 떠 있는 패널 (끌기, 크기, 최대화, 위치 저장) | [ENOVIA 공용 화면 틀](/controls/enox-components#떠-있는-패널-enofloatingpanel) |
| `DS/ENOXIDCard/js/IDCard`, `DS/ENOXIDCardContainer/js/ENOXIDCardContainer` | ENX 앱 객체 머리글 (UWA View + Model) | [ENOVIA 공용 화면 틀](/controls/enox-components#객체-머리글-enoxidcard) |
| `DS/ENOXEmptyContent/js/ENOXEmptyContent` | 빈 화면 안내 ("여기에 놓기 / 또는 / 검색") | [ENOVIA 공용 화면 틀](/controls/enox-components#빈-화면-안내-enoxemptycontent) |
| `DS/W3DXComponents/Skeleton`, `Views/Item/TileView`, `Collections/ActionsCollection`, `Views/EmptyView` | W3DX 목록과 상세 앱 뼈대, 부품 | [ENOVIA 공용 화면 틀](/controls/enox-components#목록과-상세-앱-뼈대-w3dxcomponents) |
| `DS/Skeleton/SkeletonLayout` | DockingElement 두 개로 만든 WebUX 3단 레이아웃 | [ENOVIA 공용 화면 틀](/controls/enox-components#목록과-상세-앱-뼈대-w3dxcomponents) · [분할 레이아웃](/controls/docking-element) |
| `DS/LevelSelector/LevelSelector` | 3D 선택 경로의 단계(제품 / 조립품 / 부품) 고르기 막대 | [ENOVIA 공용 화면 틀](/controls/enox-components#경로-단계-고르기-levelselector) |
| `DS/ENOXPageToolBar/js/PageToolBar`, `DS/ENOXCollectionToolBar/js/ENOXCollectionToolBarV2`, `DS/ENOXSplitView/js/ENOXSplitView`, `DS/ENOXSplitViewVertical/js/ENOXSplitViewVertical`, `DS/ENOXWidgetPreferences/js/ENOXWidgetPreferences` | 그 밖의 ENOX 모듈 (이름과 쓰임만) | [ENOVIA 공용 화면 틀](/controls/enox-components#그-밖의-enox-모듈) |

## 이벤트, 선택, 끌어 놓기

| 모듈 | 내용 | 문서 |
| --- | --- | --- |
| `DS/CoreEvents/Events` | 창 전체에서 하나인 publish / subscribe | [PubSub](/events/pubsub) |
| `DS/CoreEvents/ModelEvents` | 만들어서 공유하는 publish / subscribe | [PubSub](/events/pubsub) |
| `DS/Selection/CSOManager`, `HSOManager`, `PSOManager`, `DS/Selection/XSO` | 선택된 객체 공유 | [선택](/events/selection) |
| `DS/DataDragAndDrop/DataDragAndDrop` | 끌기, 놓기 | [끌어 놓기](/events/drag-and-drop) |
| `DS/3DXContentChecker/3DXContentChecker_v2.1` | 끌어 놓기 데이터(3DXContent) 검사 | [끌어 놓기](/events/drag-and-drop) |
| `DS/WidgetLink/WidgetLink` | 사용자가 연결한 위젯끼리만 publish / subscribe | [ENOVIA 공용 화면 틀](/controls/enox-components#widgetlink-직접-쓰기) |
| `DS/ENOXInterWdgCom/LinkManager`, `DS/ENOXInterWdgCom/Proxy` | 연결한 위젯끼리 3D 내용 맞추기 (PAD 3D 앱 전용), `DS/ENO/select` 토픽 | [ENOVIA 공용 화면 틀](/controls/enox-components#위젯-사이-연결-linkmanager) |

## DataGridView, Tweakers

| 모듈 | 내용 | 문서 |
| --- | --- | --- |
| `DS/DataGridView/DataGridView` | 트리 / 표 그리드 | [DataGridView](/datagridview/) · [옵션](/datagridview/api/grid-options) · [메서드](/datagridview/api/grid-methods) |
| `DS/CollectionView/CollectionView` | 그리드의 부모 클래스. 2026 에서 일부 메서드가 여기로 옮겨짐 | [그리드 메서드](/datagridview/api/grid-methods) |
| `DS/CollectionView/CollectionViewStatusBar` | 그리드 상태바. `STATUS` 값으로 항목 종류 지정 | [상태바](/datagridview/status-bar) |
| `DS/TreeModel/TreeDocument`, `DS/TreeModel/TreeNodeModel` | 그리드 데이터 (행) | [데이터 (TreeDocument)](/datagridview/api/data) · [트리 구조](/datagridview/tree) |
| `DS/TreeModel/DataSourceBase` | 행을 필요한 만큼만 불러오는 그리드의 데이터 소스 | [서버 사이드 데이터](/datagridview/server-side) |
| `DS/DataGridView/DataGridViewPaginationManager` | 그리드 행을 페이지로 나눠 표시 | [그리드 옵션](/datagridview/api/grid-options#데이터) |
| `DS/Tweakers/TypeRepresentationFactory` | 값 타입별 편집기 등록, 생성 | [사용자 타입 등록](/tweakers/custom-type) · [API](/tweakers/api/type-representation-factory) |
| `DS/Tweakers/TweakerString`, `TweakerNumber`, `TweakerBoolean` | 값 타입별 편집기 | [편집기 단독 사용](/tweakers/standalone) · [API](/tweakers/api/tweaker) |
| `DS/Tweakers/TweakerDuration`, `TweakerArray`, `TweakerVector`, `TweakerFile`, `TweakerImage`, `TweakerBlob`, `TweakerObject` 등 | 기간, 배열, 벡터, 파일, 객체 편집기와 보조 모듈 | [그 밖의 편집기](/tweakers/more-editors) |
| `DS/Tweakers/GeneratedToolbar` | JSON 으로 만드는 툴바 | [GeneratedToolbar](/tweakers/generated-toolbar) · [API](/tweakers/api/generated-toolbar) |
