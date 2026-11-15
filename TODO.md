# TODO

사양서 사이트(`docs/`) 보완 작업 목록입니다. 끝난 항목은 체크하거나 지웁니다.

## 서버에서 확인할 것

- [ ] `npm run docs:dev` 로 미리 보기: 제목 배지("서버 확인", "원본 분석") 때문에 브라우저 탭 제목이 이상해지지 않는지 확인. 이상하면 해당 페이지에 frontmatter `title` 추가
- [x] 샘플 코드 불러오기 확인 (2026-11-12, `vitepress build` 결과를 브라우저로 열어 06 ~ 15 를 쓰는 페이지에 `#region` 이 새지 않고 코드가 보이는 것을 확인): 샘플 코드 불러오기(`<<< @/../DataGridViewSample/...#범위{js}`)가 제대로 보이는지 확인. 대상은 `datagridview/tree`, `cell-functions`, `context-menu`, `custom-cell`, `status-bar`. 코드 대신 파일 전체나 "not found" 가 보이면 범위 이름이나 경로가 틀린 것
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
- [ ] DataGridViewSample 11 ~ 15 를 ENOVIA 서버에서 실행. 원본 webapps 파일을 로컬에서 띄워 Edge 로 동작은 확인했음 (배지 "로컬 실행 확인"). 서버에서도 같으면 배지를 "서버 확인" 으로 변경. 로컬에서 확인하지 못한 것:
  - [ ] `12_header.html` → `datagridview/header`, `api/layout-methods` : 터치 모드의 헤더 높이, 헤더에 마우스를 올렸을 때 툴팁
  - [ ] `14_rowGrouping.html` → `datagridview/row-grouping` : 트리 데이터와 `depth`, `mapGroupingKeys`, `canNodeBeGrouped`, 필터와 같이 쓸 때
  - [ ] `11_dragDrop.html` → `datagridview/drag-and-drop` : 마우스를 올렸을 때 손잡이 표시, 컬럼 헤더 끌기, 자동 스크롤, 다른 위젯과 주고받기 (3DXContent)
  - [ ] `13_customViews.html` → `datagridview/custom-views` : `useWidgetPreferencesFlag: true` (위젯 설정 저장, 2025 의 삭제 문제)
  - [ ] `15_serverSide.html` → `datagridview/server-side` : 그룹핑 응답 모양, 컬럼 필터의 `filterModel`, `pushUpdatesNotifications`
- [x] 사이트 검색 확인: `HeaderHeight`, `Tooltip`, `VisibleFlag` 처럼 키 이름의 일부로 검색됨 (빌드 결과로 확인)
- [ ] TweakersSample 01 ~ 05 를 ENOVIA 서버에서 실행. 로컬 실행으로는 확인함 (배지 "로컬 실행 확인"). 로컬에서 확인하지 못한 것: 슬라이더 끌기 이벤트, 달력에서 범위 밖 날짜 표시, 툴바 `onViewFocus` / `giveFocusTo` / 넘침 드롭다운, `chevronMenuIcon`, 직접 만든 편집기를 툴바에서 쓰기
- [ ] DataGridViewSample 02 를 서버에서 다시 확인: 여러 개 선택 컬럼을 `string` 타입으로, 링크 값을 `{ label, url }` 로 바꿈 (2026-11-12, 로컬 실행으로 기존 방식의 오류를 찾음)
- [ ] ControlsSample 04 ~ 06 (다이얼로그 안 그리드, 창 묶기 / 위치 저장, DockingElement) 을 3DDashboard 위젯 안에서 실행. 로컬 실행으로는 확인함 (배지 "로컬 실행 확인"). 로컬에서 확인하지 못한 것: DockingElement `COLLAPSER_STYLE.MOVABLE` 접기 단추가 보이는 조건, `addContent` 탭 방식, 위젯 iframe 안에서 localStorage 위치 저장
- [ ] 확인 안 된 샘플을 서버에서 실행: EventsSample, UWASample, ControlsSample, WidgetSample. 확인한 샘플을 링크하는 페이지는 배지를 "서버 확인"으로 변경하고 `guide/index.md` 의 "샘플 소스" 설명도 수정
- [ ] WidgetSample 03 ~ 08 을 3DDashboard 위젯으로 실행 (2026-11-15 작성, 원본 분석으로만 작성. 3DDashboard 밖에서는 동작하지 않음). 확인하면 링크하는 페이지의 배지를 "서버 확인"으로 변경
  - [ ] `SearchSample.html` → `widget/search` : 타입 제한, 여러 개 선택, 사람 고르기, `onCloseTransient` 해제, federated search 결과
  - [ ] `TaggerSample.html` → `widget/tagger` : 태그 창에 태그가 보이는지, 고르면 `onFilterSubjectsChange` 로 목록이 걸러지는지, 위젯을 숨기거나 닫을 때 `deactivate` / `die`
  - [ ] `CompassSample.html` → `widget/compass-pubsub` : 행을 고르면 Compass 앱 목록이 바뀌는지, `launchApp` 으로 연 앱이 객체를 받는지
  - [ ] `TopBarSample.html` → `widget/top-bar` : + / 공유 / 도움말 메뉴에 항목이 들어가는지, 하위 메뉴와 배지, 위젯을 지울 때 항목이 없어지는지
  - [ ] `3DPlaySample.html` → `widget/3dplay` : 3D / 2D 미리 보기와 닫기, X3DPLAW_AP 앱 열기
  - [ ] `LifecycleSample.html` → `rest-api/lifecycle` : promote / demote 결과와 `report` 오류, dslc 요청의 CSRF, 개정, 소유자 넘기기, OOTB 성숙도 창 / 개정 창
- [ ] ControlsSample 07 (`07_properties_panel.html`) 을 3DDashboard 또는 3DSpace 서버에서 실행 → `controls/properties-panel` (원본 분석으로만 작성). 08 ~ 10 (메뉴, 자동완성, ENOX) 은 로컬 실행으로 확인함 (배지 "로컬 실행 확인"). 서버에서도 같으면 "서버 확인"으로 변경
- [ ] `npm run docs:dev` 에서 한글 제목 앵커 확인: `.vitepress/config.mjs` 에 `markdown.anchor.slugify` 를 넣어 한글 제목 id 를 링크와 같은 NFC 로 맞춤 (2026-11-15. 기본 slugify 는 NFKD 라서 `#고르는-기준` 같은 링크가 절로 가지 않았음). 페이지 안 링크와 다른 페이지의 `#절` 링크를 몇 개 눌러 확인

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

문서 본문에 <검증 필요> 배지가 붙은 문장(원본에서 확인하지 못한 추정, 253 곳)입니다. 확인하면 배지를 지우고 문장을 단정형으로 고친 뒤 `npm run docs:verify-list` 로 `docs/guide/verify-list.md` 를 다시 만듭니다. 문장별 목록은 그 페이지에 있습니다. 괄호 안은 배지 수입니다.

- [ ] server-java (18): `rest-service` 6, `mail` 4, `scheduler` 4, `index` 3, `external-db` 1
- [ ] deploy (10): `index` 10 (JPO 컴파일 명령, 재시작 필요 여부, jar 위치 등)
- [ ] ui-components (28): `settings` 10, `url-parameters` 7, `command-menu` 4, `table` 3, `portal-channel` 2, `webform` 2
- [ ] schema (22): `conventions` 7, `mql` 7, `policy` 5, `admin` 2, `index` 1
- [ ] jpo (13): `args` 5, `domain-object` 3, `transaction-context` 2, `i18n-exception` 1, `mql-util` 1, `index` 1
- [ ] trigger (11): `generator` 3, `lifecycle` 3, `index` 2, `register` 2, `jpo-args` 1
- [ ] widget (66): `search` 14, `dashboard-register` 12, `tagger` 7, `3dplay` 6, `top-bar` 5, `compass-pubsub` 4, `i18n-preferences` 4, `transient-widget` 4, `index` 2, `preferences` 2, `space-url` 2, `widget-api` 2, `compass-services` 1, `platform-api` 1
- [ ] properties (7): `system-settings` 5, `string-resource` 2
- [ ] events (4): `selection` 2, `drag-and-drop` 1, `index` 1
- [ ] rest-api (31): `lifecycle` 12, `e6w` 7, `navigate` 6, `dictionary` 3, `index` 2, `security-context` 1
- [ ] controls (30): `properties-panel` 10, `enox-components` 6, `api/*` 4 (`collection-views`, `layout`, `webux-controls`, `windows` 각 1), `docking-element` 2, `collection-views` 1, `controls-ex` 1, `dialog` 1, `display-controls` 1, `index` 1, `input-controls` 1, `menu` 1, `webux-controls` 1
- [ ] amd (8): `libraries` 4, `utilities` 3, `webapps-utils` 1
- [ ] uwa (1) `intercom`, tweakers (1) `more-editors`
- [ ] datagridview (3): `server-side` 1, `custom-views` 1, `header` 1 (나머지 11 곳은 샘플 실행으로 확인해 지움)

## 문서 보완

- [x] FAQ / 트러블슈팅 페이지: `guide/faq.md` (증상별 6 묶음)
- [x] 찾아보기 보강: 홈 표 누락 보완, API 레퍼런스 메뉴, `guide/module-index.md`, `guide/verify-list.md`, `guide/glossary.md`
- [x] 서버 반영 절차: `deploy/index.md`
- [x] 서버 Java: `server-java/` (개요, 커스텀 REST 서비스, 스케줄러, 외부 DB, 메일)
- [x] 위젯을 3DDashboard 에 등록하는 방법: `widget/dashboard-register.md` (배포 위치, Run Your App, Additional App)
- [x] 주제 개요 페이지의 "이 섹션의 문서" 표 형식 통일
- [x] DataGridView 보완: 트리, 컬럼 함수, 우클릭 메뉴와 선택, 셀 직접 그리기, 상태바 가이드와 샘플 06 ~ 10, 레퍼런스 표 정리, 사용 파일 수, `npm run docs:coverage`
- [x] DataGridView 남은 분석: 헤더와 컬럼 그룹(`header`), 행 그룹핑(`row-grouping`), 드래그앤드롭(`drag-and-drop`), 서버 사이드(`server-side`), 사용자 뷰(`custom-views`), 레이아웃 메서드(`api/layout-methods`)
- [x] DataGridView 찾아보기: 이름 색인(`api/key-index`, `npm run docs:key-index`), 개요의 "하고 싶은 일로 찾기", 사이드바 묶음 나누기, 키 이름 일부로 검색
- [x] DataGridView 새 가이드의 샘플 작성: 11 드래그앤드롭, 12 헤더와 컬럼, 13 사용자 뷰, 14 행 그룹핑, 15 서버 사이드. 가이드 예제는 샘플에서 불러옴
- [x] 모든 샘플의 그리드 담는 요소에 `position: relative` 추가 (없으면 그리드가 페이지 전체를 덮음)
- [x] Tweakers 보강 (2026-11-12): 가이드 `editors` (편집기 종류별 사용법), `custom-tweaker` (편집기 직접 만들기), 샘플 04 · 05, 기존 샘플 01 ~ 03 실행 확인과 가이드 정리, 개요의 "하고 싶은 일로 찾기", 이름 색인 (`npm run docs:key-index`)
- [ ] DataGridView 남은 이름 (`npm run docs:coverage`): 페이지 나누기(`paginationManager`) 가이드, 찾기(`setFindStr`) 가이드, 일괄 수정, 인쇄
- [ ] DataGridView 기존 가이드(기본 그리드, 셀 타입, 툴바, 정렬과 필터)의 예제 코드도 샘플에서 불러오도록 변경. 새 가이드에서 불러오기가 제대로 보이는 것을 확인한 뒤에 진행
- [ ] server-java 남은 분석: Slack 알림, 독립 실행 이관 프로그램 내부
- [ ] server-java, deploy 샘플 폴더 (ServerJavaSample) 작성
- [x] webapps 보강 (2026-11-15, webapps 폴더별로 불러오는 모듈을 집계해 많이 쓰는데 문서에 없는 것을 찾음. 괄호 안은 그 모듈을 쓰는 앱 수). 페이지 16 개, 샘플 10 개 (WidgetSample 03 ~ 08, ControlsSample 07 ~ 10), 사이드바, 홈, 섹션 개요, 모듈 경로 색인, 용어집, FAQ, 처음 읽는 순서, 엔드포인트 목록 반영. 기존 페이지가 원본과 다른 곳도 고침 (TransientWidget 의 ENOLCMT_AP 예, closeSearch, DockingElement 의 Skeleton, 엔드포인트 목록의 e6w / dslc / lifecycle 메서드)
  - [x] 1순위: 3DSearch 에서 골라 오기 `widget/search` (SearchCom 15), 6W 태그 `widget/tagger` (TagNavigatorProxy 27), Compass 에 선택 객체 알리기 `widget/compass-pubsub` (i3DXCompassPubSub 41), 성숙도 변경과 개정 `rest-api/lifecycle` (LifecycleServices 35 ~ 46, MaturityCmd 24), 속성 패널 `controls/properties-panel` (EditPropWidget 32), 타입과 속성 정보 `rest-api/dictionary` (FedDictionaryAccessAPI 37)
  - [x] 2순위: 메뉴 `controls/menu` (Menu 52), 자동완성 `controls/autocomplete` (WUXAutoComplete 50), 공통 유틸 `amd/utilities` (Utilities/Dom 105, Logger 97, Core/PointerEvents 102), 언어와 사용자 환경설정 `widget/i18n-preferences` (UWPClientCode/I18n 22, MePreferencesClientAPI 14), 상단 바 `widget/top-bar` (TopBarProxy 21), 들어 있는 외부 라이브러리 `amd/libraries` (Handlebars 51, 2026 React18Loader 31)
  - [x] 3순위: ENOVIA 공용 화면 틀 `controls/enox-components` (ENOXTriptych 27, ENOFloatingPanel 38), 3D / 2D 미리 보기 `widget/3dplay` (3DPlay 23), 객체 정보와 찾기 `rest-api/e6w` (ObjectInfo, PersonSearch), 관계 탐색 `rest-api/navigate` (enorelnav)
- [x] 샘플과 원본이 다른 곳 고치기 (2026-11-15)
  - [x] `WidgetSample/TransientSample.html` : 넘기는 값의 기본값을 `{ objectId: '' }` 에서 `SelectedItem` 배열 모양으로 바꿈 (OOTB 정보 앱 ENOLCMT_AP 는 `objectId` 를 읽지 않음). 서버에서 physicalid 를 넣어 열리는지 확인 필요
  - [x] `ControlsSample/06_docking_element.html` : 로그 영역 `#log` 에 `white-space: pre-wrap` 추가 (여러 줄 로그가 한 줄로 이어져 보였음)

## 저장소

- [ ] `develop` 의 변경을 `master` 에 반영해 사이트 배포 (`master` push 시 GitHub Actions 가 배포)
