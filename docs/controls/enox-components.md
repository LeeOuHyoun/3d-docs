---
title: ENOVIA 공용 화면 틀 (ENOX)
---

# ENOVIA 공용 화면 틀 (ENOX) <Badge type="tip" text="로컬 실행 확인" />

OOTB ENOVIA 앱(ENX*, ENO* 위젯)은 같은 화면 틀을 씁니다. 왼쪽 / 가운데 / 오른쪽 3단 화면(`ENOXTriptych`), 객체 머리글(`ENOXIDCard`), 빈 화면 안내(`ENOXEmptyContent`), 떠 있는 패널(`ENOFloatingPanel`) 등입니다. 이 문서는 이 모듈들을 커스텀 위젯에서 쓰는 방법과, 같은 일을 하는 WebUX 컨트롤과 고르는 기준을 정리합니다.

샘플: [10_enox_layout.html](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/ControlsSample/10_enox_layout.html)

::: info 근거
ENOXTriptych/ENOXTriptych.js (`init`, `_subscribeToEvents`, `_subscribeToDomEvents`, `_initDivs`, `_initResizer`, `_refreshContainerLeftSize`, `_refreshContainerRightSize`, `_showPanel`, `_hidePanel`, `_makeModal`), html/ENOXTriptych.html, css/ENOXTriptych.css. ENOXWelcomePanel/ENOXWelcomePanel.js. ENOFloatingPanel/ENOFloatingPanel.js (`defaultOptions`, `init`, `initUI`, `_close`, `setNewSize`, `_actionMove`, `_resizeContainer`, `_saveDimensionAndPosition`), UIKIT.js 의 `DS/UIKIT/Modal`, WidgetServices.js 의 `getWidgetBody`. ENOXInterWdgCom/ENOXInterWdgCom.js (`LinkManager`, `Proxy`), assets/init/ENOWidgetLinkEvents.json, assets/doc/select.md, WidgetLink/WidgetLink.js. ENOXIDCard/js/IDCard.js, html/IDCard.html. ENOXEmptyContent/js/ENOXEmptyContent.js, SentenceDetails.js. W3DXComponents/W3DXComponents.js (`Skeleton`, `Views/Item/TileView`, `Collections/ActionsCollection`, `Models/ActionModel`). LevelSelector/LevelSelector.js. Skeleton/Skeleton.js (`DS/Skeleton/SkeletonLayout`).
호출부: ENOUserGroupMgmt (TriptychWrapper), IssueManagement (TriptchWrapper, IssueIdcard, TopBarWrapper), XSRCommonComponents (XSpecTriptych), LifecycleWidget, CommonReportingPanel, SharePopup, IssueCommon, EditPropWidget, 3DPlayWidget, ENOCockpit, ENXNav, DELWebWidgetLinkInfra (WidgetLink 직접), ENOXIDCardContainer, CfgConfigurationRevisionUX, PCOverview, ENOPartMgt (ContentDisplaySkeleton, PartSkeleton), CATW3DCommands (LevelSelector).
앱 수: enovia2025 webapps 에서 모듈 문자열 `"DS/폴더/모듈"` 이 나오는 폴더 수 (그 모듈의 폴더 제외). 2026 은 enovia2026_webapps 의 같은 파일과 비교함.
로컬 실행: 2025 webapps 를 `scripts/samples-server.mjs` 로 띄우고 Edge 헤드리스로 샘플을 열어 확인 (3DSpace, 3DDashboard 없음)
:::

## 한눈에 보기

| 모듈 | 하는 일 | 앱 수 | 비슷한 것 (문서) |
| --- | --- | --- | --- |
| `DS/ENOFloatingPanel/ENOFloatingPanel` | 위젯 안에 떠 있는 패널. 끌어 옮기기, 크기 조절, 최대화, 위치 저장 | 38 | [Dialog](/controls/dialog), [UIKIT Modal](/controls/uikit#modal) |
| `DS/ENOXTriptych/js/ENOXTriptych` | 왼쪽 / 가운데 / 오른쪽 3단 화면. 좁으면 패널을 겹쳐 엶 | 27 | [DockingElement](/controls/docking-element) 두 개 |
| `DS/ENOXInterWdgCom/LinkManager` | 사용자가 연결(Link)한 위젯끼리 3D 내용 맞추기. PAD 계열 3D 앱 전용 | 22 | [PlatformAPI](/widget/platform-api), [InterCom](/uwa/intercom) |
| `DS/W3DXComponents/Views/Item/TileView` | W3DX 목록의 타일 한 칸 | 15 | [타일 뷰](/controls/collection-views) |
| `DS/W3DXComponents/Collections/ActionsCollection` | W3DX 화면의 명령 목록 (UWA Collection) | 14 | - |
| `DS/LevelSelector/LevelSelector` | 3D 화면에서 선택 경로의 어느 단계(제품, 조립품, 부품)를 고를지 보여 주는 막대 | 14 | - |
| `DS/W3DXComponents/Skeleton` | 목록 → 상세(머리글 + 탭)로 밀려 들어가는 앱 뼈대 | 12 | [목록, 트리, 타일 뷰](/controls/collection-views) + 3단 화면 |
| `DS/ENOXEmptyContent/js/ENOXEmptyContent` | 내용이 없을 때 안내 문장 ("여기에 놓기" 또는 "검색") | 10 | `DS/W3DXComponents/Views/EmptyView` (10) |
| `DS/ENOXIDCard/js/IDCard` | 객체 머리글. 3단 화면의 오른쪽 패널과 묶임 | 7 | [ControlsEx IDCard](/controls/controls-ex#idcard) |

그 밖의 ENOX 모듈(시작 패널, 페이지 툴바, 목록 툴바, 2단 분할 ...)은 [그 밖의 ENOX 모듈](#그-밖의-enox-모듈) 에 있습니다.

### 공통 규칙: ModelEvents 하나로 묶기

ENOX 모듈은 WebUX 컨트롤(`DS/Controls/Abstract`)처럼 속성을 바꾸는 방식이 아닙니다. 대부분 `modelEvents` 옵션으로 [`DS/CoreEvents/ModelEvents`](/events/pubsub#modelevents) 객체를 받고, **이벤트 이름 문자열로 명령을 받고 알림을 냅니다.**

- 여러 모듈에 같은 `ModelEvents` 를 주면 서로 연결됩니다. 예: ENOXIDCard 의 정보 아이콘이 `triptych-toggle-panel` 을 보내 ENOXTriptych 의 오른쪽 패널을 열고, ENOXTriptych 의 `triptych-panel-visible` 을 받아 아이콘을 켭니다.
- `modelEvents` 를 주지 않으면 모듈이 안에서 새로 만듭니다. 그러면 밖에서 명령을 보낼 길이 없으므로 **항상 하나 만들어 넘깁니다.**
- `publish` 는 같은 자리에서 바로(동기) 구독 함수를 부릅니다. 구독 함수에서 예외가 나면 뒤 구독 함수가 불리지 않습니다 ([PubSub 주의](/events/pubsub#주의)).

| 모듈 | 만드는 법 | 바탕 |
| --- | --- | --- |
| ENOXTriptych | `new ENOXTriptych()` 후 `init(옵션, 왼쪽, 가운데, 오른쪽, 시작패널옵션)` | 일반 생성자 |
| ENOXIDCard | `new IDCard({ model: UWA 모델 })` 후 `render().inject(요소)` | `UWA/Class/View` ([MVC](/uwa/mvc)) |
| ENOXEmptyContent | `new ENOXEmptyContent(옵션)` 후 `inject(요소)` | 일반 생성자 |
| ENOFloatingPanel | `new ENOFloatingPanel(옵션)` 후 `inject(widget.body)`, `show()` | `DS/UIKIT/Modal` |
| LinkManager | 모듈 값이 객체 하나 (`new` 하지 않음) | - |
| LevelSelector | 모듈 값이 객체 하나. `createView(옵션)` / `destroyView()` | - |

## 3단 화면 (ENOXTriptych)

`DS/ENOXTriptych/js/ENOXTriptych` 는 왼쪽 패널, 가운데, 오른쪽 패널로 나눈 화면입니다. ENX* 앱의 "왼쪽 시작 패널 | 가운데 목록 | 오른쪽 속성" 화면이 이것입니다. 파일은 `webapps/ENOXTriptych/ENOXTriptych.js` 입니다.

### 만들기

<<< @/../ControlsSample/10_enox_layout.html#layout{js}

```css
#layout { position: relative; height: 420px; }
.pane { height: 100%; overflow: auto; box-sizing: border-box; }
```

- `init(options, left, main, right, welcome)` 의 둘째 ~ 넷째 인자는 각 칸에 넣을 요소입니다. `null` 이면 빈 칸입니다. 다섯째는 [시작 패널](#시작-패널-다섯째-인자) 옵션입니다.
- `container` 에 **이미 화면에 붙은 요소** 를 주면 그 안에 그리고, 만들 때 바로 크기를 계산합니다. `getContent()` 는 그 요소를 돌려줍니다. ENOUserGroupMgmt, IssueManagement, XSRCommonComponents 가 이 방식입니다.
- `container` 를 주지 않으면 새 `div.triptych-container` 를 만듭니다. 이때는 `triptych.inject(부모)` 로 붙입니다. 아래 [주의](#triptych-주의) 를 봅니다.
- 담는 요소에는 높이를 줍니다. 칸은 모두 `position: absolute` 로 놓입니다.

로컬 실행으로 확인한 것: 1208px 영역에서 왼쪽 180px, 가운데 1026px, 오른쪽 닫힘. 그리드 행을 고르면 오른쪽 260px 이 열리고 가운데 766px. "오른쪽 360px" 이면 오른쪽 360px, 가운데 666px. 왼쪽 경계를 +60px 끌면 240px, 최소값(120)보다 작게 끌면 120px.

### 옵션

| 키 | 기본값 | 설명 |
| --- | --- | --- |
| `modelEvents` | 새 ModelEvents | 명령과 알림을 주고받을 버스 |
| `container` | - | 이 요소 안에 그림. **기존 내용은 지워짐** (`innerHTML` 로 덮어씀) |
| `withtransition` | `false` | 0.4 초 애니메이션. `true` 면 열림 / 닫힘 알림도 0.4 초 뒤에 남 |
| `borderLeft` | `false` | 왼쪽 패널 오른쪽 테두리 |
| `borderRight` | `true` | 오른쪽 패널 왼쪽 테두리 |
| `left.originalState` | `'close'` | `'open'` 이면 열린 채 시작 |
| `left.originalSize` | `300` | 넓이(px). `minWidth` 보다 작으면 `minWidth` |
| `left.minWidth` | `40` | 최소 넓이 |
| `left.resizable` | `false` | 경계를 끌어 크기 바꾸기 |
| `left.withClose` | `false` | 오른쪽 위 닫기(X) 아이콘 |
| `right.originalState` | `'close'` | 위와 같음 |
| `right.originalSize` | `300` | 위와 같음 |
| `right.minWidth` | `250` | 위와 같음 |
| `right.resizable` | `true` | 위와 같음 |
| `right.withClose` | `true` | 위와 같음 |

- 넓이는 늘 "전체 - 100px" 을 넘지 않고, 양쪽이 다 열려 있으면 가운데에 150px 이 남도록 줄어듭니다. 그다음 `minWidth` 를 적용하므로 `minWidth` 가 우선합니다.
- OOTB 호출부가 넘기는 `overMobile`, `autoCloseFlag`, 소문자 `minwidth`, `useNewUXGuidelines` 는 ENOXTriptych 가 읽지 않습니다 (`useNewUXGuidelines` 는 시작 패널 옵션에서만 읽음).

### 패널 명령 (보내는 이벤트)

<<< @/../ControlsSample/10_enox_layout.html#commands{js}

| 이벤트 | data | 하는 일 |
| --- | --- | --- |
| `triptych-show-panel` | `'left'` / `'right'` | 열기 |
| `triptych-hide-panel` | `'left'` / `'right'` | 닫기. [닫기 전 확인](#닫기-전에-묻기) 콜백이 하나라도 `false` 면 닫지 않음 |
| `triptych-toggle-panel` | `'left'` / `'right'` | 열기 / 닫기 전환 |
| `triptych-set-size` | `{ side, size }` | 넓이(px). 닫혀 있으면 다음에 열 때의 넓이 |
| `triptych-set-content` | `{ side, content }` | `side` 는 `'left'`, `'middle'`, `'right'`. 그 칸의 **첫 자식 하나만** 지우고 `content` 를 붙임 |
| `triptych-add-check-before-close` | `{ side, id, callback }` | 닫기 전 확인 콜백 등록 |
| `triptych-remove-check-before-close` | `{ side, id }` | 등록 해제 |
| `triptych-make-modal` | `'left'` / `'right'` | 그 패널만 누를 수 있게 하고 나머지를 흐리게 (`opacity: .8`, 어두운 덮개) |
| `triptych-unmake-modal` | - | 위를 풂. 패널을 닫을 때도 저절로 풂 |
| `triptych-activate-transition` / `triptych-deactivate-transition` | - | 애니메이션 켜기 / 끄기 |
| `triptych-get-panel-visibility` | `'left'` / `'right'` | 답으로 `triptych-panel-left-visilibity` / `triptych-panel-right-visilibity` (철자 그대로) 를 `true` / `false` 와 함께 냄 |
| `triptych-fetch-panel-visibility-status` | `function (s)` 또는 `{ side, cbFunc }` | 콜백을 바로 부름. `s` 는 `{ left, right }` (side 를 주면 그쪽 값만) |

### 알림 (받는 이벤트)

<<< @/../ControlsSample/10_enox_layout.html#events{js}

| 이벤트 | data | 시점 |
| --- | --- | --- |
| `triptych-panel-show-started` | `'left'` / `'right'` | 열기 시작 |
| `triptych-panel-visible` | `'left'` / `'right'` | 열린 뒤. **이미 열려 있어도 `triptych-show-panel` 을 보내면 다시 남** |
| `triptych-panel-visible-size` | `{ side, width }` | 위와 같이. 열린 넓이 |
| `triptych-panel-hide-started` | `'left'` / `'right'` | 닫기 시작 |
| `triptych-panel-hidden` | `'left'` / `'right'` | 닫힌 뒤. 이어서 `triptych-made-unmodal` |
| `triptych-resized` | `{ side, width }` | 넓이를 다시 계산할 때마다 (열기, 경계 끄는 중, 창 크기 변경) |
| `resizing-triptych-container` | 전체 넓이 | 창(window) 크기가 바뀐 뒤 (0.15 초 지연) |
| `triptych-entering-mobile` / `triptych-leaving-mobile` | - | 전체 넓이가 550px 이하가 됨 / 넘음 |
| `triptych-clicked-overlay` | `'left'` / `'right'` | 좁은 화면에서 패널 뒤 덮개를 눌러 닫음 |
| `triptych-made-modal` / `triptych-made-unmodal` | `'left'` / `'right'` / - | 위 명령의 결과 |

로컬 실행으로 확인한 것: `withtransition: false` 에서 오른쪽을 닫으면 `triptych-panel-hide-started` → `triptych-panel-hidden` → `triptych-made-unmodal` 이 `publish` 안에서 바로 남.

### 메서드

| 메서드 | 설명 |
| --- | --- |
| `init(options, left, main, right, welcome)` | 만들기 (위) |
| `inject(parent)` | `container` 를 주지 않았을 때 붙이기 |
| `getContent()` | 바깥 요소 |
| `getLeftOpen()` / `getRightOpen()` | 열려 있는지 |
| `isInMobileMode()` | 전체 넓이가 550px 이하인지 |
| `getLeftPanelContainer()` / `getRightPanelContainer()` / `getMainPanelContainer()` | 칸 요소 (`.triptych-left` 등) |
| `getLeftPanelContent()` / `getRightPanelContent()` | 칸 안에서 내 요소가 붙는 곳 (`.triptych-left-content` 등) |
| `destroy()` | 창 크기 감시와 구독을 풀고 속성을 비움. **DOM 은 지우지 않음** |

### 닫기 전에 묻기

<<< @/../ControlsSample/10_enox_layout.html#check{js}

- 콜백은 닫기 명령(`triptych-hide-panel`, `triptych-toggle-panel`, 닫기 아이콘, 좁은 화면의 덮개)마다 불립니다. 하나라도 거짓 값이면 닫지 않고 콘솔에 `preventing from closing right` 를 남깁니다.
- 좁은 화면에서 한쪽을 열 때 다른 쪽을 자동으로 닫는 동작도 이 콜백을 거칩니다.

로컬 실행으로 확인한 것: "편집 중" 을 켜고 오른쪽 X 를 누르면 닫히지 않고 로그가 남음. 끄고 누르면 닫힘.

### 좁은 화면 (550px 이하)

전체 넓이가 550px 이하가 되면 패널이 가운데를 밀지 않고 **넓이 90% 로 겹쳐** 열립니다. 뒤에는 반투명 덮개가 깔리고, 덮개를 누르면 그 패널이 닫힙니다(`triptych-clicked-overlay`). 한 번에 한쪽만 열립니다.

로컬 실행으로 확인한 것: 담는 요소를 500px 로 줄이고 창 `resize` 를 보내자 `triptych-entering-mobile` 이 나고, 열려 있던 오른쪽이 닫히며 왼쪽이 448px(90%) 로 겹침. 오른쪽을 열면 왼쪽이 닫힘. 다시 900px 로 넓히면 `triptych-leaving-mobile`.

::: tip 좁은 화면을 거치면 넓이가 줄어든 채 남을 수 있습니다
좁은 화면에서 계산한 넓이를 기억합니다. 로컬 실행에서는 900px → 500px → 900px 로 바꾼 뒤 왼쪽이 488px 에서 88px 로 줄어든 채 남았습니다. 필요하면 `triptych-leaving-mobile` 에서 `triptych-set-size` 로 다시 줍니다.
:::

### 시작 패널 (다섯째 인자)

다섯째 인자에 `DS/ENOXWelcomePanel/js/ENOXWelcomePanel` 옵션을 주면 왼쪽 칸에 "앱 설명 + 할 일 목록" 시작 패널을 그리고, 가운데 왼쪽 위에 접기 단추(25 × 45px)를 놓습니다. 단추는 왼쪽을 300px ↔ 40px(아이콘만)로 바꿉니다.

```js
triptych.init({
  modelEvents: modelEvents,
  container: document.getElementById('layout'),
  left: { originalState: 'open' }
}, null, mainEl, rightEl, {
  modelEvents: modelEvents,
  appDescription: { title: '내 앱', description: '부품을 관리합니다' },
  activities: [{
    id: 'parts',
    title: '부품',
    actions: [
      { id: 'myParts', text: '내 부품', fonticon: 'user', isDefault: true },
      { id: 'allParts', text: '전체 부품', fonticon: 'list' }
    ]
  }],
  selectedItem: 'myParts'
});

modelEvents.subscribe({ event: 'welcome-panel-action-selected' }, function (data) {
  console.log(data.id);
});
```

| 옵션 | 설명 |
| --- | --- |
| `modelEvents` | 같은 버스. 없으면 아래 명령을 받지 못함 |
| `appDescription` | `{ title, description, className }`. 맨 위 앱 설명 |
| `activities` | `[{ id, title, actions }]`. `actions` 항목은 `{ id, text, fonticon, tooltip, isDefault, className, letterAsIcon, isNotHighlightable }` |
| `footer` | `activities` 와 같은 형식. 아래쪽에 |
| `selectedItem` | 처음 선택된 `actions` 의 `id` |
| `collapsed` | 처음부터 아이콘만 |
| `useWebUX` | 아이콘을 `wux-ui-3ds-이름` 글꼴로. 없으면 `fonticon fonticon-이름` |
| `useNewUXGuidelines` | 진한 파란 배경 |
| `companyId` | `welcome-panel-action-selected` 의 `param` 으로 그대로 감 |

| 이벤트 | 방향 | data |
| --- | --- | --- |
| `welcome-panel-action-selected` | 알림 | `{ id, param }` |
| `welcome-panel-collapsed` / `welcome-panel-expanded` | 알림 | - |
| `welcome-panel-toggle` / `welcome-panel-expand` / `welcome-panel-collapse` | 명령 | 패널 모양만 바꿈. 왼쪽 칸 넓이는 그대로 |
| `welcome-panel-select-action` / `welcome-panel-unselect-action` | 명령 | `{ id }` / - |

- `welcome-panel-collapse` 같은 명령은 시작 패널 모양만 바꿉니다. 왼쪽 칸 넓이까지 줄이려면 `triptych-set-size` 를 같이 보냅니다 (IssueManagement 의 TopBarWrapper 방식).
- 가운데 칸 왼쪽 위를 접기 단추가 덮으므로 가운데 내용에 왼쪽 여백(약 25px)을 둡니다.

로컬 실행으로 확인한 것: 시작 패널이 300px 로 그려지고, "전체 부품" 을 누르면 `welcome-panel-action-selected` `{ id: 'allParts' }`. 접기 단추를 누르면 왼쪽 40px 과 `welcome-panel-collapsed`. `selectedItem` 없이 `isDefault` 만 주었을 때는 다른 항목을 눌러도 처음 항목의 선택 표시가 남음 (원본의 선택 바꾸기 코드가 `selectedItem` 만 봄).

### Triptych 주의

::: warning container 없이 만들면 크기가 틀어집니다
`container` 를 주지 않으면 화면에 붙기 전(넓이 0)에 크기를 계산합니다. `inject` 뒤에도 다시 계산하지 않아서, 열린 패널이 CSS 기본값(300px)으로 보이고 가운데가 오른쪽 패널 밑으로 들어갑니다. 창 `resize` 가 나야 맞춰집니다. 로컬 실행에서 `left: 200, right: 260` 으로 열린 채 만들었더니 붙인 뒤 왼쪽 300px, 가운데 948px (오른쪽과 겹침) 이었고, 창 `resize` 후에 200 / 438 / 260px 이 되었습니다. `container` 에 화면에 붙은 요소를 주거나, 붙인 뒤 `triptych-set-size` 를 한 번 보냅니다.
:::

::: warning data 는 문자열 'left' / 'right' 입니다
열기 / 닫기 / 전환 명령은 `data === 'left'` 인지만 보고 나머지는 모두 오른쪽으로 처리합니다. `{ side: 'left' }` 처럼 객체를 보내면 오른쪽이 움직입니다 (로컬 실행 확인). XSRCommonComponents 의 `togglePanel` 이 이렇게 보냅니다. `triptych-set-size`, `triptych-set-content` 만 `{ side, ... }` 객체입니다.
:::

- 가운데를 바꿀 때 `side` 는 `'middle'` 입니다 (`'main'`, `'center'` 는 무시됨).
- 크기 변화는 창(window) `resize` 로만 감지합니다. 창 크기는 그대로인데 담는 요소만 넓어지면 다시 계산하지 않습니다. 이때는 `triptych-set-size` 를 보냅니다.
- LifecycleWidget, CommonReportingPanel 은 `_showPanel(' ')`, `_isRightOpen` 같은 내부 이름을 직접 부릅니다. 직접 만들 때는 위 명령 이벤트만 씁니다.
- 2026 은 경계 끌기를 시작할 때 이벤트 전파를 막지 않습니다(`stopPropagation` 제거). 나머지 동작은 같습니다.

## 빈 화면 안내 (ENOXEmptyContent)

`DS/ENOXEmptyContent/js/ENOXEmptyContent` 는 목록이 비었을 때 가운데에 아이콘과 안내 문장을 보여 줍니다. 문장이 둘 이상이면 사이에 "또는" 구분선이 들어갑니다. LifecycleWidget 은 "여기에 놓기" + 검색 단추, CfgConfigurationRevisionUX 는 "변경 조치 선택 단추" + "끌어 놓기" 로 씁니다.

<<< @/../ControlsSample/10_enox_layout.html#empty{js}

| 옵션 | 기본값 | 설명 |
| --- | --- | --- |
| (문자열 하나) | - | `new ENOXEmptyContent('글자')` 처럼 주면 기본 화면(큰 아이콘 + 글자) |
| `defaultEmptyContentText` | "페이지가 비어 있습니다." | `sentences` 가 없을 때 기본 화면 글자 |
| `sentences` | - | 문장 목록 (아래 표) |
| `fontSize` | - | 문장 글자 크기 (`'16px'`) |
| `align` | `'center'` | `left`, `center`, `right` |
| `orderedListType` | `'none'` | 문장을 감싸는 `li` 의 `list-style` |
| `dialogLine` | `{ disable: true }` | `disable: false` 면 아래에 꺾인 선과 `bottomSentence` (기본 "위의 지침 준수"), `fontSize` |
| `modelEvents` | 새 ModelEvents | `empty-content-change-align` (data `'left'` 등)을 받아 정렬을 바꿈 |

| `type` | 키 | 모양 |
| --- | --- | --- |
| `search` | `text`, `fonticon` (기본 `search`), `sizeicon` | 검색 아이콘 + "콘텐츠에서 검색" |
| `drop` | `text`, `fonticon` (기본 `drag-drop`), `sizeicon` | 끌어 놓기 아이콘 + "여기에 콘텐츠 놓기" |
| `icon` | `fonticon`, `sizeicon`, `text` | `wux-ui-3ds-` 아이콘 + 글자 |
| `img` | `src`, `text` | 그림 + 글자 |
| `dom` | `DOMelement` | 내 요소를 그대로 (단추 등). `DOMelement` 가 없으면 `text` 를 HTML 로 |

- 모든 `type` 에 `events: { onclick: fn }` 을 주면 그 문장을 누를 때 부릅니다 (키가 `onclick` 하나일 때만).
- `DOMelement` 는 `type: 'dom'` 에서만 씁니다. LifecycleWidget 은 `type: 'drop'` 에 `DOMelement` 를 같이 넘기는데, 이 요소는 화면에 들어가지 않습니다.
- `inject(parent)` 로 붙이고 `destroy()` 로 DOM 에서 뗍니다. 바깥 요소는 `_containerEmptyContent` 이고, CATFLCockpitWidget 은 이것을 다이얼로그 내용으로 넣습니다.
- 바깥 요소는 `height: 100%` 의 flex 상자입니다. 담는 요소에 높이를 줍니다.

로컬 실행으로 확인한 것: 문장 3개면 `ol` 1개, `li` 3개(`list-style: none`), 구분선 2개. `onclick` 이 불림. 문자열 하나로 만든 기본 화면과 `destroy()` 도 오류 없이 동작.

::: warning text 는 HTML 로 들어갑니다
`text`, `defaultEmptyContentText`, `bottomSentence`, 문자열 인자는 모두 `innerHTML` 로 넣습니다. 객체 이름 같은 사용자 입력을 넣을 때는 이스케이프합니다 (로컬 실행에서 `'<b>굵게</b>'` 가 굵은 글자로 보임).
:::

2026 은 둘째 인자로 빈 화면 우클릭 메뉴(`DS/Menu/ContextualMenu`)를 받습니다. 항목은 `{ id, type, title (또는 text), state, handler, fonticon, content (하위 항목), submenu }` 입니다 <Badge type="warning" text="2026" />. 항목에 `multiselect` 키를 넣으면 원본이 정의되지 않은 변수 `multipleItemSelected` 를 읽어 `ReferenceError` 가 나므로 넣지 않습니다.

## 객체 머리글 (ENOXIDCard)

`DS/ENOXIDCard/js/IDCard` 는 ENX* 앱의 객체 머리글입니다. 썸네일, 이름 + 버전, 속성 칸(최대 12개, 3개씩 4열), 오른쪽 단추(메뉴, 정보, 접기)로 되어 있고 높이는 110px(접으면 38px) 입니다. 단추를 누르면 `modelEvents` 로 이벤트를 냅니다.

<<< @/../ControlsSample/10_enox_layout.html#idcard{js}

- `model` 은 `UWA/Class/Model` 입니다 ([MVC](/uwa/mvc#model)). 화면 옵션도 모두 모델 값으로 줍니다.
- 모델에 `modelEvents` 가 없으면 만들 때 오류가 납니다 (`subscribe` 를 부름).
- `attachResizeSensor()` 는 화면에 붙고 보이는 뒤에 부릅니다 (원본 주석). ENOXIDCardContainer 는 `render().inject()` → 부모에 붙이기 → `attachResizeSensor()` 순서입니다.

로컬 실행으로 확인한 것: 행을 고를 때 `model.set` 으로 이름, 버전, 속성이 바뀜. 정보 아이콘을 누르면 오른쪽 패널이 열리고 아이콘이 `active` 가 됨. 접기 단추를 누르면 높이 38px 과 `idcard-minified`.

### 모델 값

| 키 | 기본값 | 설명 |
| --- | --- | --- |
| `modelEvents` | - | 필수. 이벤트 버스 |
| `name`, `version` | - | 제목 줄. `version` 은 파란 글자이고 누르면 `idcard-open-version-explorer` |
| `versionModel`, `modelVersion`, `configuration`, `ngfilter` | - | 제목 줄 뒤에 붙는 값. 누르면 각각의 이벤트 (아래 표) |
| `thumbnail` | - | 썸네일 주소 |
| `attributes` | - | `[{ name, value, id, editable, image, icon, classname, container, isMaturity, displayWhenMinified }]`. 앞의 12개만 보임 |
| `freezones` | - | 속성 오른쪽 자유 칸(최대 2). 문자열(HTML), 요소, `render()` 가 있는 UWA View |
| `dropdown` | - | 메뉴 단추. `{ items, events }` 면 UIKIT DropdownMenu, `{ content, callbacks }` 면 `DS/Menu/Menu` |
| `withActionsButton` | `true` | 메뉴(▼) 단추. 메뉴가 없으면 `false` 로 끔 |
| `withInformationButton` | `false` | 정보(i) 단추 |
| `withToggleMinifiedButton` | `false` | 접기 단추 |
| `withHomeButton`, `withExpandCollapseButton`, `showBackButton` | `false` | 왼쪽 세로 단추 (홈, 시작 패널 접기, 뒤로) |
| `enableNavigation` | `false` | ◀ ▶ 이동 단추 |
| `withGenericButton`, `withDivider` | `false` | 빈 단추 자리, 구분선 |
| `minified` | `false` | 접힌 채 시작 |
| `customEvents` | `{}` | 단추가 보낼 이벤트를 바꿈. 예: `{ informationIconClick: { event: 'my-info', data: 1 } }` |
| `useBrowserTooltip` | `false` | `true` 면 `title` 툴팁, 아니면 WebUX 툴팁 |
| `dragwithimage` | - | `{ item, img }`. 머리글을 끌면 3DXContent 로 `item` 을 넘김 ([끌어 놓기](/events/drag-and-drop)). `widget` 이 있어야 함 |

`customEvents` 키: `homeIconClick`, `backIconClick`, `expandCollapseIconClick`, `versionLabelClick`, `versionModelLabelClick`, `configurationLabelClick`, `ngfilterLabelClick`, `modelVersionLabelClick`, `actionsMenuClick`, `informationIconClick`.

### 이벤트

| 이벤트 | 방향 | 시점, data |
| --- | --- | --- |
| `triptych-toggle-panel` | 알림 | 정보 단추. data `'right'` |
| `idcard-open-actions-menu` | 알림 | 메뉴 단추 |
| `idcard-open-version-explorer` | 알림 | 버전 글자. data `{ id: 모델의 id }` |
| `idcard-open-configuration-selector`, `idcard-open-ngfilter-selector`, `idcard-open-modelVersion-selector` | 알림 | 해당 글자 |
| `idcard-show-landing-page` / `idcard-back` / `welcome-panel-toggle` | 알림 | 홈 / 뒤로 / 시작 패널 단추 |
| `idcard-attributes-clicked` | 알림 | 속성 값. data `{ index, attributeName }` |
| `idcard-thumbnail-clicked` | 알림 | 썸네일 |
| `navigate-backward` / `navigate-forward` | 알림 | ◀ ▶ (비활성이 아닐 때) |
| `custom-generic-icon-event` | 알림 | 빈 단추. data 는 클릭 이벤트 |
| `idcard-minified` / `idcard-expanded` | 알림 | 접힘 / 펼침 |
| `idcard-thumbnail-changed` | 알림 | `idcard-change-thumbnail` 처리 후. data 주소 |
| `idcard-minify` / `idcard-expand` / `idcard-toggle-minified-view` | 명령 | 접기 / 펴기 / 전환 |
| `idcard-toggle-information-icon` | 명령 | data `true` / `false` / 없음(전환) |
| `idcard-change-thumbnail` | 명령 | data 주소 |
| `triptych-panel-visible` / `triptych-panel-hidden` | 받음 | data `'right'` 이면 정보 아이콘 켜기 / 끄기 |

메서드: `render()`, `inject(el)`, `attachResizeSensor()`, `resize()`, `minify()`, `expand()`, `toggleMinified()`, `toggleInformationIcon(flag)`, `changeThumbnail(src)`, `destroy()`.

::: warning 화면에 붙기 전에 render 하면 썸네일이 숨은 채 남습니다
넓이가 868px 보다 좁으면 썸네일을, 640px 보다 좁으면 제목 글자 크기를 줄입니다. 이 계산은 `render()` 직후 한 번(`requestAnimationFrame`)과 ResizeSensor 가 크기 변화를 볼 때 합니다. 붙지 않은 요소에서 `render()` 하면 넓이 0 으로 계산해 썸네일을 숨기고, 붙인 뒤 `attachResizeSensor()` 를 불러도 크기가 바뀌기 전까지 그대로입니다 (로컬 실행 확인). 화면에 붙은 요소에 `render().inject()` 합니다.
:::

- 속성 `value` 는 글자로 들어갑니다. `container: true` 인 속성만 HTML 로 들어갑니다.
- `isMaturity: true` 인 속성 자리에는 `DS/MaturityCompactView/MaturityCompactView` 를 불러 성숙도 배지를 그립니다 (모델의 `id`, `securityContext` 사용).
- 2026 은 속성 `value` 에 `Date` (또는 `Date` 배열)를 주면 날짜 편집기(`DS/Tweakers/TweakerDate`) 모양으로 그립니다 (`displayTime` 키) <Badge type="warning" text="2026" />.

### ControlsEx IDCard 와 비교

| | ENOXIDCard | [ControlsEx IDCard](/controls/controls-ex#idcard) |
| --- | --- | --- |
| 모듈 | `DS/ENOXIDCard/js/IDCard` (7 앱) | `DS/ControlsEx/IDCard` (2 앱) |
| 바탕 | UWA View + UWA Model | WebUX `DS/Controls/Abstract` |
| 값 바꾸기 | `model.set({ name: ... })` | `idCard.titleText = ...` |
| 단추 알림 | `modelEvents` 의 이벤트 이름 | `addEventListener('infoClicked', ...)` |
| 3단 화면 연결 | 정보 단추가 Triptych 오른쪽 패널을 바로 열고 닫음 | 직접 연결 |
| 상태 콤보, 끌기 | 없음 (성숙도는 `isMaturity`) / `dragwithimage` | 상태 콤보, `draggableFlag` |
| 접힘 저장 | 없음 | `identifier` 로 localStorage 저장 |

ENOXTriptych 와 ModelEvents 로 화면을 짜면 ENOXIDCard, WebUX 컨트롤로 짠 화면이면 ControlsEx IDCard 가 맞습니다. W3DXComponents 에도 `DS/W3DXComponents/IdCard` (5 앱)가 있는데, [W3DX Skeleton](#목록과-상세-앱-뼈대-w3dxcomponents) 이 상세 패널 머리에 쓰는 것입니다.

## 떠 있는 패널 (ENOFloatingPanel)

`DS/ENOFloatingPanel/ENOFloatingPanel` 은 위젯 안에 떠 있는 패널입니다. 머리줄을 끌어 옮기고, 오른쪽 아래 손잡이로 크기를 바꾸고, 최대화하고, 위치와 크기를 저장합니다. ENOVIA 앱 중 가장 많은 38 앱이 씁니다. 공유(SharePopup), 접근 권한(AccessRightPopup), 속성 창(EditPropWidget 의 POPUP 모드, [속성 패널](/controls/properties-panel)), 첨부(IssueCommon) 창이 이것입니다. `DS/UIKIT/Modal` 을 상속하므로 Modal 의 `show()`, `hide()`, `setBody()` 도 씁니다.

<<< @/../ControlsSample/10_enox_layout.html#floating{js}

- 위젯에서는 `inject(widget.body)` 합니다. 크기 제한, 가운데 놓기, 최대화의 기준이 늘 `widget.body` (위젯 밖이면 `document.body`) 이기 때문입니다 (`WidgetServices.getWidgetBody`). 다른 요소에 붙이면 위치 계산이 어긋날 수 있습니다.
- `new` 바로 뒤에 `inject` 와 `show()` 를 부릅니다. 첫 크기 계산과 저장 값 적용은 `setTimeout(0)` 뒤에 하므로 그 전에 화면에 붙어 있어야 합니다.
- 다 쓰면 `destroy()` 합니다. `hide()` 는 숨기기만 하고 문서(document)에 건 마우스, 키 처리기는 남습니다.

로컬 실행으로 확인한 것: 360 × 220px 으로 가운데(443, 292)에 열림. 머리줄을 (+100, +50) 끌자 543, 341 로 옮겨지고 localStorage `EFP_sample-history` 에 `{"width":360,"height":220,"left":543,"top":341}` 저장. 손잡이로 440 × 260px 로 늘림. Esc 로 닫은 뒤 다시 열자 저장된 위치에 열림.

### 옵션

| 키 | 기본값 | 설명 |
| --- | --- | --- |
| `title` | `''` | 머리줄 제목 |
| `body` | `''` | 내용. HTML 문자열 또는 요소 |
| `footer` | - | 아래 줄 (Modal 옵션) |
| `className` | `''` | 바깥 요소 클래스 |
| `closable` | `false` | 닫기(X) 아이콘 |
| `maximizable` | `false` | 최대화 / 되돌리기 아이콘 |
| `resizable` | `false` | 오른쪽 아래 크기 손잡이 (마우스를 올리면 보임). 내용 높이를 패널에 맞춤 |
| `limitParent` | `false` | `true` 면 끌기와 최대화가 위젯 안에서만. `false` 면 40px 만 보이게 남김 |
| `autocenter` | `true` | 저장된 위치가 없으면 가운데에 엶 |
| `persistId` | `null` | 위치, 크기를 localStorage `EFP_` + 값에 저장하고 다음에 되살림 |
| `overlay` | `false` | 뒤를 덮개로 막음 (Modal) |
| `animate` | `false` | 나타나기 / 사라지기 효과 (Modal) |
| `escapeToClose` | `true` | Esc 로 닫기 (Modal) |
| `visible` | `false` | 붙이자마자 보임 (Modal) |
| `minWidth` / `minHeight` | CSS 값 | 손잡이로 줄일 수 있는 최소 크기 |
| `header` | - | 머리줄을 내 요소로 바꿈. 이때 제목과 위 아이콘들은 만들지 않음 |
| `hasSettings` / `settingsCallback` | `false` / `null` | 톱니 아이콘과 누를 때 부를 함수 |
| `pinOptions` / `pinCallback` | `null` | 고정(pin) 아이콘. `DS/W3DDComponents/Views/PinView` 옵션(`widgetAppId`, `widgetData` 등)으로 대시보드에 위젯을 고정하는 것으로 보입니다 <Badge type="danger" text="검증 필요" /> |
| `appNamePosition` | `1` | 제목을 `' - '` 로 나눈 줄임 후보 수. 1 이면 줄이지 않음 <Badge type="danger" text="검증 필요" /> |
| `events` | - | `{ onClose, onMove, onResizeFloating, onShow, onHide }` |

### 메서드

| 메서드 | 설명 |
| --- | --- |
| `show()` / `hide()` / `destroy()` | 보이기 / 숨기기 / 정리 (Modal) |
| `setTitle(text)` | 제목 바꾸기 |
| `setNewSize({ width, height })` | 패널 크기. 위젯 크기 - 15px 를 넘지 않고 최소 크기보다 작아지지 않음. `persistId` 면 저장 |
| `setNewSizeWithDiff({ x, y })` | 지금 크기에서 더하기 |
| `setNewBodySize({ width, height })` | 내용 영역 크기로 맞추기 |
| `centerIt()` | 위젯 가운데로 |
| `setMinSize(width, height)` | 최소 크기 |
| `maximize()` | 위젯 크기로 |
| `lockWidth()` | 지금 넓이로 고정 |
| `isDimensionSaved()` | 만들 때 `persistId` 저장 값이 있었는지 |
| `getSavedDimensionFromId(id)` | 다른 `persistId` 의 저장 값 |
| `getContainerHeight()` / `getModalBodySize()` | 패널 높이 / 내용 영역 크기 |
| `setBody(c)` / `setFooter(c)` / `getBody()` | 내용 바꾸기 (Modal) |

### 이벤트

| 이벤트 | 시점 |
| --- | --- |
| `onClose` | X 아이콘, Esc |
| `onMove` | 머리줄을 끄는 동안 계속 |
| `onResizeFloating` | 크기를 다시 계산한 뒤 (열 때, 손잡이, 최대화, 위젯 크기 변경). 2026 은 인자 `{ resizeOrigin_Widget }` (위젯 크기 변경 때문이면 `true`) <Badge type="warning" text="2026" /> |
| `onShow` / `onHide` | 보임 / 숨김 (Modal) |

::: warning events.onClose 를 주면 X 를 눌러도 닫히지 않습니다
X 와 Esc 는 `onClose` 를 냅니다. 이때 **옵션의 `events.onClose` 가 함수면 아무것도 하지 않고**, 없으면 스스로 `destroy()` 합니다. 그래서 `events.onClose` 를 준 경우 그 안에서 `hide()` 나 `destroy()` 를 직접 불러야 합니다 (SharePopup, IssueCommon 이 이렇게 함). `panel.addEvent('onClose', fn)` 으로 건 함수는 이 판단에 들어가지 않아서 불린 뒤 저절로 `destroy()` 됩니다. 로컬 실행에서 세 경우 모두 확인했습니다.
:::

::: warning Esc 한 번에 열린 패널이 모두 닫힙니다
패널마다 문서(document)에 Esc 처리기를 겁니다. 패널 두 개를 열고 Esc 를 한 번 누르자 둘 다 닫혔습니다 (로컬 실행). 하나만 닫혀야 하면 `escapeToClose: false` 로 만들고 직접 처리합니다.
:::

::: warning limitParent 가 없으면 최대화한 패널이 화면 밖으로 넘칩니다
`maximize()` 는 크기만 위젯 크기로 바꾸고 위치는 그대로 둡니다. 로컬 실행에서 (443, 292) 에 있던 패널이 1231 × 788px 이 되어 오른쪽 아래가 화면 밖으로 나가고, 되돌리기 / 닫기 아이콘도 화면 밖에 있었습니다. `limitParent: true` 면 (15, 15) 로 옮겨져 화면 안에 들어왔습니다. 최대화를 쓰면 `limitParent: true` 를 줍니다.
:::

- `persistId` 가 있으면 열자마자(가운데 놓기, `setNewSize`) 값이 저장됩니다. 최대화한 크기도 저장되어 다음에 그 크기로 열립니다.
- 최상위 창(위젯 iframe 이 아닌 단독 페이지)에서는 열려 있는 동안 `body` 에 `modal-open` 클래스가 붙어 페이지 스크롤이 막힙니다 (UIKIT Modal 동작, 로컬 실행 확인).
- 처음 크기는 내용에 맞춰 정해집니다. 내용이 짧으면 좁게 열리므로 `setNewSize` 로 크기를 줍니다.

### 고르는 기준

| 하고 싶은 일 | 쓸 것 |
| --- | --- |
| 확인 / 취소 단추가 있는 대화상자 | [Dialog](/controls/dialog) |
| 다른 화면을 막지 않고 옆에 띄워 두는 도구 창, 위치 저장 | ENOFloatingPanel (`overlay: false`, `persistId`) 또는 [ImmersiveWindow](/controls/window-layout) |
| ENOVIA 공유 / 속성 창과 같은 모양 | ENOFloatingPanel |
| 짧은 알림, 확인 묻기 | [UIKIT SuperModal](/controls/uikit#supermodal) 또는 [Dialog 확인 묻기](/controls/dialog#확인-취소-묻기) |

## 위젯 사이 연결 (LinkManager)

`DS/ENOXInterWdgCom/LinkManager` 는 3DDashboard 의 **위젯 연결(Link)** 기능 `DS/WidgetLink/WidgetLink` 을 PAD 계열 3D 앱(3DPlayApp, CAT3DComposeWidget, CAT3DAnnotationWidget, DMU3DReviewWidget, ENOPAD3DViewer, ENXNav, Issue3DReview 등)에 맞게 감싼 것입니다. 사용자가 대시보드에서 두 위젯을 연결하면 그 둘 사이에서만 3D 내용, 시점, 필터를 맞춥니다. 일반 위젯 사이 메시지 도구가 아닙니다.

### 위젯 사이 통신 방법

| 방법 | 받는 쪽 | 연결하는 사람 | 문서 |
| --- | --- | --- | --- |
| `PlatformAPI.publish` / `subscribe` | 대시보드의 모든 위젯 (`'Widgets'` 채널) | 없음 (토픽 이름만 맞추면 됨) | [PlatformAPI](/widget/platform-api#publish-subscribe) |
| `DS/ENO/select` 등 (`ENOXInterWdgCom/Proxy`) | PlatformAPI 위에 메타데이터 봉투를 씌운 OOTB 공용 토픽 | 없음 | [아래](#ds-eno-select-형식) |
| `DS/WidgetLink/WidgetLink` | **사용자가 연결한 위젯만** | 사용자 (대시보드의 연결 기능) | [아래](#widgetlink-직접-쓰기) |
| LinkManager | 위와 같고, 등록된 토픽만 | 사용자 | 이 절 |
| `UWA/Utils/InterCom` | Server 를 연 창과 Socket 으로 붙은 창 | 코드 | [InterCom](/uwa/intercom) |

커스텀 위젯끼리 신호를 주고받는 것은 PlatformAPI 가 가장 간단합니다. "사용자가 고른 두 위젯끼리만" 이 필요하면 WidgetLink 를 직접 씁니다. LinkManager 를 커스텀 위젯에서 쓸 이유는 거의 없습니다.

### LinkManager API

```js
require(['DS/ENOXInterWdgCom/LinkManager'], function (LinkManager) {
  LinkManager.initializeWidgetLink(widget, {
    appIds: [],
    open: false,
    uses: ['ENXContentSync3D'],
    implements: ['ENXContentSync3D']
  }).then(function () {
    LinkManager.addEventListener('link', function (e) { console.log('연결', e.link.id); });
    LinkManager.subscribe('DS/ExternalWdgtCom/SyncContent', function (data) { console.log(data); });
  });
});
```

위 모양은 3DPlayWidget(`uses` / `implements` 에 `3DPlayDummy`)과 ENOCockpit(PAD 설정 `widgetLink_options` 를 넘김)의 호출을 옮긴 것입니다. 3DDashboard 밖에서는 실행하지 않았습니다 <Badge type="danger" text="검증 필요" />.

| 메서드 | 설명 |
| --- | --- |
| `initializeWidgetLink(widget, linkability)` | Promise. `WidgetLink.get(widget)` 에 연결 능력(`x3dLinkability`)을 넣고 내부 구독을 준비 |
| `subscribe(topic, fn)` / `unsubscribe(topic)` | 받기. 아래 주의 참고 |
| `publish(topic, data)` | 연결된 위젯이 있을 때만 보냄 |
| `getLinked()` | 연결된 위젯 목록 (`id`, `appId`, `x3dSharedId`, `matchLinkability()` ...) |
| `getLinkedWithAppId(appId)` / `getLinkedWithMatchingFeatures([기능])` | 조건에 맞는 연결 위젯 |
| `getLinkability()` / `updateLinkability(l)` | 연결 능력 읽기 / 바꾸기 |
| `addEventListener('link' 또는 'unlink', fn)` | 연결 / 끊김. `fn({ type, target, link, initiator, reason })` |
| `createView(predicate)` | 일부 연결 위젯에만 보내는 보기 |
| `getSyncFeatures()` / `enableSyncFeature(name)` / `disableSyncFeature(name)` | 맞출 항목(`Content`, `ViewPoint`). 위젯 값 `syncFeatures` 에 저장 |
| `isLinkInitiator()` / `isLinkedWithPartnerApp(x3dSharedId)` | 내가 연결을 시작했는지 / 그 앱과 연결됐는지 |

::: warning 등록된 토픽만 다룹니다
`subscribe` 는 토픽의 마지막 칸(`SyncContent` 등)이 `ENOXInterWdgCom/assets/init/ENOWidgetLinkEvents.json` (2025 73 개)에 있고, 그 기능(`ENXContentSync`, `ENXContent`, `ENXAuthoring` ...)이 연결 상대와 맞을 때만 실제로 구독합니다. 아니면 아무것도 하지 않고 `undefined` 를 돌려줍니다. `publish` 는 연결된 위젯이 있을 때 등록되지 않은 토픽이면 `TypeError` 가 납니다 (`o[n].syncFeature`). 직접 만든 토픽은 WidgetLink 를 직접 씁니다.
:::

- 모듈 값은 객체 하나(singleton)입니다. 같은 창의 모든 코드가 같은 객체를 씁니다.
- `initializeWidgetLink` 전에 부르면 콘솔에 `Widget Link is not initialized, further calls to Widget Link will be ignored` 를 한 번 찍고 `getLinked()` 는 `[]`, 나머지는 `undefined` 입니다 (로컬 실행 확인).
- 안에서 `DS/PADUtils/PADContext`, `DS/PADServices/utils/GlobalModelEvents` 의 PAD 이벤트(`ENXModel/Root/FBIProxy`)를 기다립니다. PAD 앱이 아니면 연결 뒤 동기화 단계가 끝나지 않는 것으로 보입니다 <Badge type="danger" text="검증 필요" />.

### WidgetLink 직접 쓰기

DELWebWidgetLinkInfra 는 LinkManager 없이 `DS/WidgetLink/WidgetLink` 를 바로 씁니다. 아래는 그 순서를 옮긴 것입니다. 커스텀 위젯이 대시보드에서 연결 대상으로 잡히려면 연결 능력(`x3dLinkability`)이 서로 맞아야 하는데, 이 조건은 대시보드 쪽(`DS/Dashboard/WidgetLinkManager`, webapps 에 없음)에 있어 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.

```js
require(['DS/WidgetLink/WidgetLink'], function (WidgetLink) {
  var link = WidgetLink.get(widget);

  function listen() {
    link.subscribe('XX/MyApp/SelectPart', function (data) { console.log(data.id); });
  }

  link.addEventListener('link', listen);
  link.addEventListener('unlink', function () { link.unsubscribe('XX/MyApp/SelectPart'); });
  if (link.getLinked().length > 0) { listen(); }

  link.publish('XX/MyApp/SelectPart', { id: 'PRT-0001' });
});
```

- 대시보드 안 위젯에서는 이 모듈이 대시보드 최상위 창의 모듈을 돌려줍니다. 메시지는 `DS/MessageBus/MessageBus` 의 `widget-link:위젯id:번호` 채널로 **연결된 위젯에게만** 갑니다.
- `getLinkability()` / `setLinkability(l)` 는 위젯 값 `x3dLinkability` 를 읽고 씁니다.
- 2026 은 연결 묶음(group)이 생겼습니다: `getGroupId()`, `getGroupMembers()`, `addEventListener('group-changed', fn)` <Badge type="warning" text="2026" />.

### DS/ENO/select 형식

ENOXInterWdgCom 의 `Proxy` 는 PlatformAPI 로 `DS/ENO/` + 이름 토픽을 보냅니다. 원본 문서(assets/doc)에 정리된 공용 토픽은 `select` (다른 앱과 선택 맞추기), `focus` (앱 안에서 찾아 보여 주기), `searchInApp` (앱 안 검색) 세 가지입니다. 데이터는 `{ metadata, data }` 입니다.

| 키 | 내용 |
| --- | --- |
| `metadata.uid` / `originUid` | 이벤트 id / 보낸 Proxy 의 id. 자기가 보낸 것을 거르는 데 씀 |
| `metadata.appid` | `'/' + widget.getValue('x3dSharedId')` (없으면 `x3dAppId`) |
| `metadata.originWidgetId`, `timestamp` | 보낸 위젯 id, 시각 |
| `data.paths` (select, focus) | 물리 id 경로 배열의 배열 `[[참조id, 인스턴스id, 참조id]]` |
| `data.attributes` (select) | `{ 물리id: { type, label } }` |
| `data.tenant`, `data.version` (select) | 테넌트, `'1.1'` |
| `data.intent` (focus) | `'focus'` |
| `data.query` (searchInApp) | 검색 글자 |

자기 위젯이 보낸 `select` 도 다시 받으므로 `originUid` 로 거릅니다. 3DSpace 객체 선택을 대시보드에 알리는 일반적인 방법은 [Compass 에 선택 객체 알리기](/widget/compass-pubsub) 를 봅니다.

## 목록과 상세 앱 뼈대 (W3DXComponents)

`webapps/W3DXComponents/W3DXComponents.js` 는 SwymUIComponents, W3DSpace, PlatformManagement, ENOPartMgt, CfgBaseUX 등이 쓰는 UWA MVC 화면 부품 묶음(모듈 46 개)입니다. 이 중 `Skeleton`, `Views/Item/TileView`, `Collections/ActionsCollection` 을 많이 씁니다. 바탕은 `UWA/Class/View`, `Collection`, `Model` 입니다 ([MVC](/uwa/mvc)).

::: tip DockingElement 문서의 Skeleton 과 다른 모듈입니다
[분할 레이아웃](/controls/docking-element) 에 나오는 "DockingElement 를 겹쳐 3단으로 나누는 Skeleton" 은 `DS/Skeleton/SkeletonLayout` (WebUX, 6 앱)입니다. `DS/W3DXComponents/Skeleton` 은 DockingElement 를 쓰지 않고, 라우터와 패널 슬라이더로 "목록 패널 → 상세 패널" 을 밀어 넣습니다.
:::

### Skeleton

`new Skeleton(rendererMap, options)` 로 만들고 `render().inject(요소)` 합니다. `render()` 전에 `inject` 하면 `You must render this view before you try to inject it.` 오류가 납니다. 첫 화면은 `options.root` 의 renderer 로 목록을 그리고, 항목을 고르면 그 항목의 머리글(IdCard)과 탭(facet)이 있는 상세 패널로 넘어갑니다.

ENOPartMgt (ContentDisplaySkeleton, PartSkeleton) 의 구성:

```js
var skeleton = new Skeleton({
  contents: {
    collection: MyPartCollection,
    view: ContentSet,
    viewOptions: { contents: { views: [{ id: 'tile', view: GridScrollView, itemView: TileView, default: true }] } },
    idCardOptions: {
      attributesMapping: { title: 'name' },
      facets: [{ name: 'props', text: '속성', icon: 'doc-text', handler: Skeleton.getRendererHandler(MyPropertiesView) }]
    },
    idKey: 'id'
  }
}, {
  root: 'contents',
  rootIdCardOptions: { model: new MyModel({ title: '부품 목록' }), facets: [{ name: 'contents', text: '내용', icon: 'picture', handler: Skeleton.getRendererHandler('contents') }] }
});
skeleton.render().inject(widget.body);
```

이 예는 실행해 보지 않았습니다. 서버 목록(Collection 의 `url`, `fetch`)이 있어야 화면이 채워집니다 <Badge type="danger" text="검증 필요" />.

| rendererMap 항목 키 | 설명 |
| --- | --- |
| `collection` | 목록 데이터 `UWA/Class/Collection` 클래스 |
| `view` | 목록을 그릴 View 클래스 (보통 `DS/W3DXComponents/ContentSet`) |
| `viewOptions` / `collectionOptions` / `fetchOptions` | 각각 View, Collection, `fetch` 에 넘길 옵션 (함수면 불러서 씀) |
| `idCardOptions` | 상세 패널 머리글. `facets` (탭: `name`, `text`, `icon`, `handler`), `actions`, `attributesMapping` |
| `idKey` | 경로의 id 와 맞출 모델 키 |
| `fetchMode` / `cacheFacets` | 기본 `'always'` / `true` |

| options 키 | 기본값 | 설명 |
| --- | --- | --- |
| `root` | 첫 renderer | 첫 목록 |
| `rootIdCardOptions` | `null` | 맨 앞 패널의 머리글. 주면 `model`, `facets` 가 꼭 있어야 함 (없으면 오류) |
| `startRoute` | `'/' + root + '/'` | 처음 경로. 경로는 `/renderer/id/renderer/...` 에 `?f=탭번호` |
| `widthThreshold` | `560` | 넓이가 이 값 이상이면 왼쪽 목록(약 25%) + 오른쪽 상세를 나란히, 좁으면 한 패널씩 |
| `responsiveTrigger` | `false` 반환 | `true` 를 돌려주면 넓이와 상관없이 나란히 배치 (ENOPartMgt 는 위젯이 최대화 / 전체 화면일 때) |
| `minimizeIdCards` / `height` | `true` / `'100%'` | 머리글 줄이기 / 높이 |

메서드: `getCollectionAt(i)`, `getViewAt(i)`, `getModelAt(i)`, `getIdCardAt(i)`, `getActiveIdCard()`, `getCurrentPanelIndex()`, `getRoute()`, `setRoute(route)`, `slideBack()`, `toggleCollapse()`, `setHeight(h)`, `refresh()`, `hideFacet` / `showFacet` / `switchFacets`, `destroy()`. 정적 함수 `Skeleton.getRendererHandler(이름 또는 View 클래스)` 로 탭 `handler` 를 만듭니다. 이벤트: `onItemSelect`, `onFacetSelect`, `onFacetUnselect`, `onRouteChange`, `onPositionChange`, `onSlide`.

### TileView, ActionsCollection

| 모듈 | 내용 |
| --- | --- |
| `DS/W3DXComponents/Views/Item/TileView` | 모델 하나를 타일로 그리는 View. `image`, `icon`, `title`, `subtitle`, `content` 를 그림. 이벤트 `onClick`, `onDblClick`, `onContextMenu`, `onDrag`, `onCheck`. `title`, `subtitle`, `content` 는 이스케이프하지 않고 HTML 로 들어감 (Handlebars 세 겹 중괄호) |
| `DS/W3DXComponents/Collections/ActionsCollection` | 명령 목록 Collection. 항목 모델(`Models/ActionModel`) 기본값은 `{ title: '', icon: '', id: '', context: false, overflow: true }` |
| `DS/W3DXComponents/Views/EmptyView` | 빈 목록 View (10 앱). ENOXEmptyContent 와 같은 용도 |

### 고르는 기준

새로 만드는 위젯은 W3DX 보다 WebUX 를 권합니다. 목록은 [DataGridView](/datagridview/) 나 [타일 목록](/controls/collection-views), 상세 머리글은 ENOXIDCard 나 [ControlsEx IDCard](/controls/controls-ex#idcard), 화면 나누기는 ENOXTriptych 나 [DockingElement](/controls/docking-element) 로 짜면 서버 Collection, 라우터 없이 만들 수 있습니다. W3DX Skeleton 은 기존 W3DX 앱을 고칠 때 봅니다.

## 경로 단계 고르기 (LevelSelector)

`DS/LevelSelector/LevelSelector` 는 이름과 달리 **트리를 몇 단계까지 펼칠지 고르는 부품이 아닙니다.** 3D 화면(CATW3D, DMU, ENOPAD3DViewer, Issue3DReview 등)에서 부품을 누르면 그 자리에 "제품 → 조립품 → 부품" 경로를 사다리꼴 막대로 보여 주고, 어느 단계를 선택할지 고르게 합니다. 트리 펼치기는 [트리 구조](/datagridview/tree) 의 `expand`, `expandAll` 을 씁니다.

```js
require(['UWA/Core', 'DS/LevelSelector/LevelSelector'], function (UWA, LevelSelector) {
  LevelSelector.createView({
    uiFrame: UWA.extendElement(frameEl),
    display: { type: 'position', x: 200, y: 150 },
    getLevels: function (refresh) {
      return {
        levels: [
          { pathElement: rootPath, name: '제품' },
          { pathElement: assemblyPath, name: '조립품 A' },
          { pathElement: partPath, name: '브라켓', selection: 'notSelectable' }
        ],
        currentLevel: 1
      };
    },
    onSelect: function (e) { console.log(e.pathElement); },
    onHover: function (e) {},
    onLeave: function (e) {}
  });
});
```

| 키 | 설명 |
| --- | --- |
| `uiFrame` | 막대를 붙일 요소. **UWA 요소**여야 함 (`getParent`, `getSize` 사용) |
| `display` | `{ type: 'position', x, y }` (그 자리) 또는 `{ type: 'docked', marginW, marginH }` (오른쪽 위). 옛 키 `positionX`, `positionY` 도 받음 |
| `getLevels(refresh)` | 단계 배열, 또는 `{ levels, currentLevel (0부터), selectColor, selectOverColor }`. 단계는 `{ pathElement (필수), name, icon, selection, canvasColor }`. `selection` 은 `'notSelectable'` (고를 수 없음), `'notCurrent'` |
| `onSelect` / `onHover` / `onLeave` | `{ pathElement }` 를 받음. `onSelect` 는 필수 |
| `disableKeyboard`, `displayWhileViewpointIsManipulated`, `background` | 키보드 끄기, 3D 시점을 돌리는 중에도 보이기, 배경색 맞추기 |

- 필수 값이 빠지거나 형식이 틀리면 아무것도 그리지 않고 `undefined` 를 돌려줍니다. 그리면 `0` 입니다.
- 화면에 하나만 있습니다. 다시 `createView` 하면 이전 것을 지우고, `destroyView()` 로 지웁니다.

로컬 실행으로 확인한 것: 3D 화면 없이 UWA 요소에 그려짐. 3단계 막대가 커지는 사다리꼴로 그려지고 셋째 단계는 회색. 마우스를 올리면 이름 표("제품")와 `onHover` / `onLeave`. 단계 요소에 `mousedown` → `mouseup` 을 보내자 `onSelect({ pathElement: 'ASM' })`. `uiFrame` 에 UWA 요소가 아닌 `div` 를 주면 `m.getParent is not a function` 오류.

## 그 밖의 ENOX 모듈

| 모듈 | 앱 수 | 내용 |
| --- | --- | --- |
| `DS/ENOXWidgetPreferences/js/ENOXWidgetPreferences` | 13 | 위젯 설정에 자격 증명(Credentials), 3DEXPERIENCE 플랫폼, "Show Work Under", "시작할 때 설정 화면" 항목을 넣는 도우미. [위젯 설정](/widget/preferences), [언어와 사용자 환경설정](/widget/i18n-preferences) 참고 |
| `DS/ENOXCollectionToolBar/js/ENOXCollectionToolBarV2` | 7 | 목록 위 툴바 (찾기, 여러 개 선택, 정렬, 보기 전환, 개수). `modelEvents` 의 `enox-collection-toolbar-*` 이벤트로 주고받음 |
| `DS/ENOXWelcomePanel/js/ENOXWelcomePanel` | 6 | [시작 패널](#시작-패널-다섯째-인자). Triptych 다섯째 인자로 씀 |
| `DS/ENOXPageToolBar/js/PageToolBar` | 6 | 가운데 위 줄 (시작 패널 단추, 본문, ◀ ▶, 정보 단추). 정보 단추는 `information-panel-open` / `-close` 를 냄. Triptych 와 묶으려면 앱이 `triptych-show-panel` 로 바꿔 보냄 (IssueManagement TopBarWrapper) |
| `DS/ENOXSplitView/js/ENOXSplitView` | 4 | 좌우 2단 (넓이는 %). 좁으면 단추로 한쪽씩 |
| `DS/ENOXSplitViewVertical/js/ENOXSplitViewVertical` | 4 | 위아래 2단. ENOXIDCardContainer 가 머리글 + 내용에 씀 |
| `DS/ENOXViewFilter/js/ENOXViewFilter` | 2 | 보기 필터 |
| `DS/ENOXIDCardContainer/js/ENOXIDCardContainer` | 0 | ENOXIDCard + 위아래 2단. 접으면 위 칸을 40px 로 |
| `DS/ENOXTabs/js/ENOXTabs` | 0 | 탭. 앱에서 쓰지 않음. 탭이 필요하면 [Tab](/controls/layout#tab) |

이 표의 모듈은 이름과 쓰임만 정리했습니다. 옵션과 이벤트는 다루지 않았습니다.

## 고르는 기준

| 하고 싶은 일 | ENOX | WebUX / 다른 것 | 고르는 기준 |
| --- | --- | --- | --- |
| 왼쪽 / 가운데 / 오른쪽 3단 | ENOXTriptych | [DockingElement](/controls/docking-element) 두 개, `DS/Skeleton/SkeletonLayout` | ENX 앱과 같은 동작(오른쪽 정보 패널, 좁은 화면 겹침, 닫기 전 확인)이 필요하면 Triptych. 탭 영역, 접기 막대, 접힘 저장이 필요하면 DockingElement |
| 객체 머리글 | ENOXIDCard | [ControlsEx IDCard](/controls/controls-ex#idcard) | Triptych + ModelEvents 로 짜면 ENOXIDCard, WebUX 속성 / 이벤트로 짜면 ControlsEx |
| 떠 있는 창 | ENOFloatingPanel | [Dialog](/controls/dialog), [ImmersiveWindow](/controls/window-layout) | 확인 단추가 있는 창은 Dialog. 옆에 띄워 두는 도구 창, ENOVIA 공유 / 속성 창 모양은 ENOFloatingPanel |
| 빈 화면 안내 | ENOXEmptyContent | W3DX EmptyView, 직접 만든 요소 | "여기에 놓기 / 또는 / 검색" 모양이 필요하면 ENOXEmptyContent |
| 위젯 사이 신호 | LinkManager | [PlatformAPI](/widget/platform-api), WidgetLink, [InterCom](/uwa/intercom) | 보통 PlatformAPI. 사용자가 연결한 위젯끼리만이면 WidgetLink. LinkManager 는 PAD 3D 앱 전용 |
| 목록 → 상세 | W3DX Skeleton | [타일 목록](/controls/collection-views), [DataGridView](/datagridview/) + Triptych | 새 위젯은 WebUX. W3DX 는 기존 앱 수정 때 |
| 트리를 몇 단계 펼치기 | (LevelSelector 아님) | [트리 구조](/datagridview/tree) | `expand(n)`, `expandAll()` |

## 2026 변경

| 모듈 | 차이 |
| --- | --- |
| ENOXTriptych | 경계를 끌기 시작할 때 `stopPropagation` 을 하지 않음 (`preventDefault` 만). CSS, HTML 은 같음 |
| ENOFloatingPanel | `onResizeFloating` 에 `{ resizeOrigin_Widget }` 인자 |
| ENOXIDCard | 속성 값 `Date` 표시 (`TweakerDate`, `displayTime`), 자유 칸 넓이를 CSS 에서 읽음, `DS/Core/TooltipModel` 사용 (2025 는 콘솔에 `DS/Controls/TooltipModel` 사용 중단 경고) |
| ENOXEmptyContent | 둘째 인자 빈 화면 우클릭 메뉴 |
| LinkManager | `getLinkedWithMatchingFeaturesExclusive(기능들)` (모든 기능이 맞는 위젯만). 토픽 3개(`HideShowInherit`, `UpdateHideShowInheritStatus`, `setRootsPositions`)와 기능 `ENXContentExt` 추가 |
| WidgetLink | 연결 묶음 `getGroupId()`, `getGroupMembers()`, `'group-changed'`. 떠 있는 위젯 연결 |
| W3DX Skeleton, LevelSelector, ENOXWelcomePanel, ENOXSplitView, ENOXViewFilter, ENOXIDCardContainer | 문자열, 속성 이름 비교로 차이 없음 |
