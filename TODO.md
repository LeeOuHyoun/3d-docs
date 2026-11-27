# TODO

사양서 사이트(`docs/`) 보완 작업 목록입니다. 끝난 항목은 지웁니다.

## 서버에서 확인할 것

- [ ] `npm run docs:dev` 로 미리 보기: 제목 배지("서버 확인", "원본 분석") 때문에 브라우저 탭 제목이 이상해지지 않는지 확인. 이상하면 해당 페이지에 frontmatter `title` 추가
- [ ] DataGridViewSample 06 ~ 10 을 서버에서 실행 (원본 분석으로만 작성). 확인하면 링크하는 페이지의 배지를 "서버 확인" 으로 변경하고 `DataGridViewSample/index.html`, `datagridview/index.md`, `guide/index.md` 의 "06 ~ 10 은 확인하지 않았습니다" 문장 수정
  - [ ] `06_tree.html` → `datagridview/tree` : 펼칠 때 불러오기(`useAsyncPreExpand`, `preExpandDone`), 행 아이콘 / 배지 모양, 끝 행에 `addChild` 했을 때 펼침 아이콘
  - [ ] `07_columnFunctions.html` → `datagridview/cell-functions` : 수량을 편집하면 금액 셀이 다시 계산되는지, 계산 컬럼 정렬, 헤더 셀에서 컬럼 함수가 호출되는지
  - [ ] `08_contextMenu.html` → `datagridview/context-menu` : 메뉴 항목 형식(`state`, `submenu`), 메뉴 버튼 컬럼이 같은 메뉴를 여는지, `getXSO().onEmpty`
  - [ ] `09_save.html` → `datagridview/save-and-reflect` : 저장 중 변경 표시, 실패 시 되돌리기, 변경 트랜잭션 모드의 추가 / 삭제 표시
  - [ ] `10_customCell.html` → `datagridview/custom-cell`, `status-bar` : 스크롤할 때 셀 내용 재사용, 칩 클릭 후 다시 그려지는지, 상태바 자동 갱신과 `updateNodeModel`
- [ ] 스크린샷 추가 (서버 화면을 직접 찍어야 함): `02_cellTypes.html` 전체 화면 → `datagridview/cell-types` "한눈에 보기" 위, `05_options.html` 에서 `editionPolicy` 별 화면 → `datagridview/editing`. 이미지는 `docs/public/datagridview/` 에 넣고 `![설명](/datagridview/파일.png)` 로 넣음
- [ ] 배지 기준 검토: 지금은 DataGridViewSample 을 링크하는 7개 페이지만 "서버 확인"
  - [ ] `datagridview/editing` : DataGridViewSample(02, 05)을 링크했지만 배지는 "원본 분석". 편집 동작을 서버에서 확인하면 "서버 확인"으로 변경
  - [ ] 그 밖에 서버에서 확인한 페이지가 있으면 배지 변경
- [ ] DataGridViewSample 11 ~ 17 을 ENOVIA 서버에서 실행. 원본 webapps 파일을 로컬에서 띄워 Edge 로 동작은 확인했음 (배지 "로컬 실행 확인"). 서버에서도 같으면 배지를 "서버 확인" 으로 변경. 로컬에서 확인하지 못한 것:
  - [ ] `12_header.html` → `datagridview/header`, `api/layout-methods` : 터치 모드의 헤더 높이, 헤더에 마우스를 올렸을 때 툴팁
  - [ ] `14_rowGrouping.html` → `datagridview/row-grouping` : 트리 데이터와 `depth`, `mapGroupingKeys`, `canNodeBeGrouped`, 필터와 같이 쓸 때
  - [ ] `11_dragDrop.html` → `datagridview/drag-and-drop` : 마우스를 올렸을 때 손잡이 표시, 컬럼 헤더 끌기, 자동 스크롤, 다른 위젯과 주고받기 (3DXContent)
  - [ ] `13_customViews.html` → `datagridview/custom-views` : `useWidgetPreferencesFlag: true` (위젯 설정 저장, 2025 의 삭제 문제)
  - [ ] `15_serverSide.html` → `datagridview/server-side` : 그룹핑 응답 모양, 컬럼 필터의 `filterModel`, `pushUpdatesNotifications`
  - [ ] `16_findPrint.html` → `datagridview/find-and-print` : 인쇄 창과 바로 인쇄, 일괄 수정 대화상자 화면, 찾기 막대 위치와 `showMatchingCellBackgroundFlag` 강조 모양
  - [ ] `17_mergedHeader.html` → `datagridview/header` : 단독 컬럼을 같은 이름의 그룹으로 감싼 모양(위 줄 이름, 아래 줄 빈칸)이 괜찮은지, 빈칸 테두리를 지워 한 칸처럼 보이게 할지
- [ ] TweakersSample 01 ~ 05 를 ENOVIA 서버에서 실행. 로컬 실행으로는 확인함 (배지 "로컬 실행 확인"). 로컬에서 확인하지 못한 것: 슬라이더 끌기 이벤트, 달력에서 범위 밖 날짜 표시, 툴바 `onViewFocus` / `giveFocusTo` / 넘침 드롭다운, `chevronMenuIcon`, 직접 만든 편집기를 툴바에서 쓰기
- [ ] DataGridViewSample 02 를 서버에서 다시 확인: 여러 개 선택 컬럼을 `string` 타입으로, 링크 값을 `{ label, url }` 로 바꿈 (2026-11-12, 로컬 실행으로 기존 방식의 오류를 찾음). 같은 방식으로 바꾼 05 의 "담당자 (멀티)" 컬럼도 함께 확인
- [ ] Recipes 의 `DataGridViewUtils.js` 를 ENOVIA 서버에서 실행 → `datagridview/utils`. 원본 webapps (2025, 2026) 를 로컬에서 띄워 함수 39개는 확인함 (배지 "로컬 실행 확인"). 서버에서도 같으면 배지를 "서버 확인" 으로 변경. 로컬에서 확인하지 못한 것: 3DDashboard 위젯 안에서의 동작, 터치 모드, `dataSource` 그리드, 현장에 넣어 둔 lodash 모듈 이름(`DS/lodash-4.18.1/lodash`)으로 불러오기
- [ ] Recipes 의 `asyncColumn/asyncColumn.html` 을 ENOVIA 서버(3DDashboard 위젯)에서 실행 → `datagridview/async-values`. 원본 webapps (2025, 2026) 를 로컬에서 띄워 샘플과 문서 예제 12개는 확인함 (배지 "로컬 실행 확인"). 서버에서도 같으면 배지를 "서버 확인" 으로 변경. 로컬에서 확인하지 못한 것: 가짜 API 대신 실제 3DSpace 호출(WAFData)의 지연과 실패, 위젯 안에서의 동작, 터치 모드, `dataSource` 그리드, 행 그룹핑 중의 패턴 1 ~ 4, 패턴 6(함수형 `possibleValues`)에서 입력 후 포커스를 옮겼을 때 저장되는 값 (2025 와 2026 이 다르게 보였음)
- [ ] ControlsSample 04 ~ 06 (다이얼로그 안 그리드, 창 묶기 / 위치 저장, DockingElement) 을 3DDashboard 위젯 안에서 실행. 로컬 실행으로는 확인함 (배지 "로컬 실행 확인"). 로컬에서 확인하지 못한 것: DockingElement `COLLAPSER_STYLE.MOVABLE` 접기 단추가 보이는 조건, `addContent` 탭 방식, 위젯 iframe 안에서 localStorage 위치 저장
- [ ] 확인 안 된 샘플을 서버에서 실행: EventsSample, UWASample, ControlsSample, WidgetSample. 확인한 샘플을 링크하는 페이지는 배지를 "서버 확인"으로 변경하고 `guide/index.md` 의 "샘플 소스" 설명도 수정
- [ ] `WidgetSample/TransientSample.html` : 넘기는 값을 `SelectedItem` 배열 모양으로 바꿨음. 서버에서 physicalid 를 넣어 OOTB 정보 앱(ENOLCMT_AP)이 열리는지 확인
- [ ] WidgetSample 03 ~ 08 을 3DDashboard 위젯으로 실행 (원본 분석으로만 작성. 3DDashboard 밖에서는 동작하지 않음). 확인하면 링크하는 페이지의 배지를 "서버 확인"으로 변경
  - [ ] `SearchSample.html` → `widget/search` : 타입 제한, 여러 개 선택, 사람 고르기, `onCloseTransient` 해제, federated search 결과
  - [ ] `TaggerSample.html` → `widget/tagger` : 태그 창에 태그가 보이는지, 고르면 `onFilterSubjectsChange` 로 목록이 걸러지는지, 위젯을 숨기거나 닫을 때 `deactivate` / `die`
  - [ ] `CompassSample.html` → `widget/compass-pubsub` : 행을 고르면 Compass 앱 목록이 바뀌는지, `launchApp` 으로 연 앱이 객체를 받는지
  - [ ] `TopBarSample.html` → `widget/top-bar` : + / 공유 / 도움말 메뉴에 항목이 들어가는지, 하위 메뉴와 배지, 위젯을 지울 때 항목이 없어지는지
  - [ ] `3DPlaySample.html` → `widget/3dplay` : 3D / 2D 미리 보기와 닫기, X3DPLAW_AP 앱 열기
  - [ ] `LifecycleSample.html` → `rest-api/lifecycle` : promote / demote 결과와 `report` 오류, dslc 요청의 CSRF, 개정, 소유자 넘기기(일부 실패 시 HTTP 207 + 최상위 배열 응답), OOTB 성숙도 창 / 개정 창
- [ ] `WidgetSample/WebSocketSample.html` → `widget/websocket` 을 3DDashboard 위젯으로 실행. 로컬 시험 서버(ws, socket.io 4.7 에코)로 주고받기, 다시 연결, 끊기는 확인함 (배지 "로컬 실행 확인"). 확인하지 못한 것:
  - [ ] 위젯 iframe 에서 연결할 때 서버가 받는 `Origin` 헤더 값, 대시보드 / 3DSpace 의 CSP `connect-src` 제한 여부
  - [ ] `PlatformAPI.getApplicationConfiguration('app.urls.passport')` + `/login?service=<서비스>` 로 `access_token` 을 받을 수 있는지, 커스텀 서비스를 3DPassport 에 등록할 수 있는지와 서버에서 토큰을 확인하는 방법 (플랫폼 관리자 확인)
  - [ ] 3DSpace 앞 리버스 프록시 뒤에 WebSocket 서버를 둘 때 Upgrade 통과
- [ ] ControlsSample 07 (`07_properties_panel.html`) 을 3DDashboard 또는 3DSpace 서버에서 실행 → `controls/properties-panel` (원본 분석으로만 작성). 08 ~ 10 (메뉴, 자동완성, ENOX) 은 로컬 실행으로 확인함 (배지 "로컬 실행 확인"). 서버에서도 같으면 "서버 확인"으로 변경
- [ ] `npm run docs:dev` 에서 한글 제목 앵커 확인: `.vitepress/config.mjs` 에 `markdown.anchor.slugify` 를 넣어 한글 제목 id 를 링크와 같은 NFC 로 맞춤 (기본 slugify 는 NFKD 라서 `#고르는-기준` 같은 링크가 절로 가지 않았음). 페이지 안 링크와 다른 페이지의 `#절` 링크를 몇 개 눌러 확인

## 실무 검증: 샘플 작성 후 서버에서 확인

실무에서 제대로 동작해야 하는 기능입니다. 샘플을 만들고 서버에서 실행해 확인합니다.

- [ ] `datagridview/save-and-reflect` : 그리드 편집 → 저장 → 반영, 실패 시 되돌리기 (updateOptions, 변경 트랜잭션 모드). 샘플 `DataGridViewSample/09_save.html` 작성함, 서버 확인만 남음
- [ ] `rest-api/documents` : 3DSpace 문서 조회 / 생성 (modeler/documents)
- [ ] 위젯 간 통신
  - [ ] `widget/platform-api` : publish / subscribe, 사용자 정보
  - [ ] `uwa/intercom` : 위젯끼리 메시지
- [ ] 선택, Promise, 주소
  - [ ] `events/selection` : CSOManager
  - [ ] `uwa/promise` : UWA/Promise
  - [ ] `widget/widget-services` : 3DSpace 주소 보조 함수

## 실무 검증: 추정 사양 확인

문서 본문에 <검증 필요> 배지가 붙은 문장(원본에서 확인하지 못한 추정, 227 곳)입니다. 원본(webapps, JSP, MQL, jar 바이트코드)으로 확인할 수 있는 것은 이미 확인했으므로, 남은 것은 서버나 3DDashboard 에서 실행해야 확인할 수 있습니다. 확인하면 배지를 지우고 문장을 단정형으로 고친 뒤 `npm run docs:verify-list` 로 `docs/guide/verify-list.md` 를 다시 만듭니다. 문장별 목록은 그 페이지에 있습니다. 괄호 안은 배지 수입니다.

- [ ] 먼저 확인: `jpo/args.md` 편집 폼 `postProcessJPO` 에서 `void` 반환이 실패하지 않는지. 프로젝트 edit 커맨드 2건이 실제로 `void` 메서드를 연결해 씀
- [ ] server-java (18): `rest-service` 6, `scheduler` 4, `index` 3, `mail` 3, `external-db` 1, `migration` 1 (항목마다 다시 여는 트랜잭션과 바깥 abort)
- [ ] deploy (10): `index` 10 (JPO 컴파일 명령, 재시작 필요 여부, jar 위치, `loadFrameworkProperty` 용도 등)
- [ ] ui-components (26): `settings` 10, `url-parameters` 8, `command-menu` 4, `table` 3, `portal-channel` 1
- [ ] schema (21): `mql` 7, `conventions` 5, `policy` 5, `admin` 3, `index` 1
- [ ] jpo (9): `args` 3, `domain-object` 2, `transaction-context` 2, `index` 1, `mql-util` 1
- [ ] trigger (8): `generator` 2, `index` 2, `lifecycle` 2, `jpo-args` 1, `register` 1
- [ ] widget (66): `search` 14, `dashboard-register` 12, `tagger` 7, `3dplay` 6, `top-bar` 5, `compass-pubsub` 4, `i18n-preferences` 4, `transient-widget` 4, `index` 2, `preferences` 2, `space-url` 2, `widget-api` 2, `compass-services` 1, `platform-api` 1
- [ ] properties (3): `system-settings` 2, `string-resource` 1
- [ ] events (4): `selection` 2, `drag-and-drop` 1, `index` 1
- [ ] rest-api (29): `lifecycle` 10, `e6w` 7, `navigate` 6, `dictionary` 3, `index` 2, `security-context` 1
- [ ] controls (26): `properties-panel` 10, `enox-components` 6, `docking-element` 2, `api/layout` 1, `api/windows` 1, `collection-views` 1, `controls-ex` 1, `dialog` 1, `index` 1, `input-controls` 1, `menu` 1
- [ ] amd (5): `utilities` 3, `libraries` 1, `webapps-utils` 1
- [ ] datagridview (2): `server-side` 1, `custom-views` 1

## 문서 보완

- [ ] DataGridView 남은 가이드(편집, 행 높이, 저장 후 화면 반영)의 예제 코드도 샘플에서 불러오도록 변경. 편집은 05, 저장은 09 샘플에 해당 코드가 있는지 먼저 대조
- [ ] server-java, deploy 샘플 폴더 (ServerJavaSample) 작성

### Webapps

- [ ] DataGridView / CollectionView / TreeModel 2026: `columnWrapChange` 이벤트, `getContainingImmersiveFrame`, `showKPIColorFlag` / `KPIColor`, `defaultOnColumnHeaderRequest`, `getDragSourceUuid`, `setColumnMinWidth`, `getMinMaxVisibleRowIDs`, `isAFitToContentColumn` / `getFitAllContentIndexes`, `DataGridViewComparisonConditionFilterView`, `CollectionViewPaginationManager`, `CollectionViewServerSideBridge`, `ResponsiveTweakersCollectionView`, `dimensionsChanged` / `width`, `TreeModel/DataModelSet`, `TreeDocument.isEmptying`
- [ ] Core: 2026 `DS/Core/CSSSheetRegistry`, `DS/Core/AnalyticsManager`, 공통 `DS/Core/ActionsStack`
- [ ] UWA2 / WAFData: 2026 `Core.hosts` 와 `Frame.js` 의 불일치 (`license_error`, `getCompassCSRFToken` 은 `uwa/request` 에 반영됨)
- [ ] `guide/module-index.md` 에 고빈도 미수록 모듈 추가
  - 프레임워크 / 공통 UI: `DS/ApplicationFrame/Command` · `CommandsManager`, `DS/Core/WebUXComponents`, `DS/WebSystem/Environment`, `UWA/Controls/Input`, `DS/SNInfraUX/Logger` · `SearchSettings`
  - ENOVIA / PAD / 문서 / 3D: `DS/PADUtils/PADSettingsMgt` · `PADUtilsServices` · `views/PADAlert`, `DS/PADServices/utils/PADTrackerManager` · `GlobalUtils`, `DS/E6WCommonUI/UIHelper`, `DS/RDFFoundation/RDFUtils`, `DS/DocumentManagement/DocumentManagement`, `DS/TreeModel/DataModelSet`, `DS/Visualization/ThreeJS_DS`
  - 2026 신규 대형 모듈: `DS/ENOXCustomTableEditor/*`, `DS/ENOXEngineer/*`, `DS/ENOXEngineerCommonUtils/*`

### REST API

- [ ] `catalog.md`: 미수록 주요 서비스 (`modeler/configuration`, `issues`, `AppsMngt*`, `pno/ownership`, `IPClassification*`, `ParamWS`, `DELWebInfrastructure*`, `enopad`, `enoauthoring`, `accessRightServices`, `FilterBIAPI`, `modeler/browsing`, `compare`, `ecm/modeler`, 2026 `v1/engineeringItem`, `v1/usersgroup`, `6WVocab2`, `modeler/dseng` · `dsprcs` 등). 집계할 때 2026 날짜 백업 파일 10개는 뺄 것. 형태는 `_YYMMDD.js` 이고 2606 ~ 2608 이 있으며, 모듈 최상위에 9개, 하위 폴더에 1개(`ENOAEFStructureBrowser/webroot/common/scripts/emxUIFreezePane_260629.js`)가 있음

### 서버

- [ ] `properties/`: 프로젝트 시스템 설정 properties 파일 정리 (앱 등록 키, LicensedProducts, HomePage 는 반영됨)
- [ ] `ui-components/`: 프로젝트 커스텀 JSP 중 범용 패턴을 반영할지 검토
- [ ] `trigger/`: `Override` MQL 예시 (원본에 프로젝트 사용 예 없음)
- [ ] `deploy/`: `CacheManager.reset6WRestCache` (JSP 에 호출 없음. jar 확인 필요)

## 원본 대조가 덜 된 영역

- [ ] `controls/properties-panel.md`, `menu.md`, `autocomplete.md`, `notifications.md`
- [ ] `ui-components/` 의 XSL / JSP 옵션 전수
- [ ] `deploy/` 의 `97.prod-mql/*.zip` 과 JPO 컴파일 절차
- [ ] 샘플 소스 전수 대조: `EventsSample`, `WidgetSample`, `UWASample`, `ControlsSample/02~10`, `TweakersSample/01~05`, `JPOSample`, `TriggerSample`
