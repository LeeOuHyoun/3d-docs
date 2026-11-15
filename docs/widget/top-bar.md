# 대시보드 상단 바 (TopBarProxy) <Badge type="info" text="원본 분석" />

`DS/TopBarProxy/TopBarProxy` 는 위젯이 3DDashboard **상단 바** 의 메뉴(사용자 메뉴, + 메뉴, 공유, 도움말 ...)에 자기 명령을 넣는 모듈입니다. 사용자가 그 항목을 누르면 위젯 안의 함수가 불립니다. 위젯 자체 메뉴는 [widget.setMenu](/widget/widget-api#보기-메뉴-기타) 로 따로 만듭니다.

::: info 근거
TopBarProxy/TopBarProxy.js (`DS/TopBarProxy/TopBarProxy`, `MenuItem`, `Menu`, `MenuBarItem`), TopBar/TopBar.js (상단 바 쪽: InterCom 서버 `MainMenuBar` 를 여는 `DS/TopBar/model/MainMenuBar`, `MenuBarItemView`, `MenuView`), TopFrame/TopFrame.js (`setStaticMenuProviders`). 모듈을 쓰는 앱 21 개 (`DS/TopBarProxy/MenuItem` 8, `DS/TopBarProxy/Menu` 6). 호출부: Foundation (`initWidgetTopBar`, `initHelpSystem`), ENORouteMgmt, ENXMeetingMgmt, ENXDecisionMgmt, ENXHistoryUX, ENOSubscriptionMgmt (도움말), FolderEditor, ClassificationEditor, LibraryEditor, ENOCollabSharingCmds (+ 메뉴), 3DPlayUtils, 3DPlayExperienceModule (공유 메뉴, activate / deactivate / die), InstantMessaging, MyAccount, W3DDriveWrapper (사용자 메뉴). 호출 집계 (변수 이름 `topBarProxy`): `setContent(` 8, `addContent(` 4, `deactivate(` 4, `activate(` 2, `empty(` 2, `die(` 2, `get(` 1
:::

샘플: [TopBarSample.html](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/WidgetSample/TopBarSample.html) (+ / 공유 / 도움말 메뉴에 항목 넣기, 하위 메뉴, 배지, activate / deactivate / empty / die). 3DDashboard 에서만 동작합니다. 단독 실행에서는 상단 바가 없어 아무것도 보이지 않습니다.

## 상단 바의 칸

proxy 는 칸(slot) 마다 항목 목록을 가집니다. 칸 id 와 상단 바 아이콘은 `DS/TopBarProxy/MenuBarItem` 에 정해져 있습니다.

| 칸 id | 아이콘 (`fonticon-`) | 쓰임 | OOTB 가 넣는 것 |
| --- | --- | --- | --- |
| `profile` | `user-alt` | 사용자(Me) 메뉴 | InstantMessaging, MyAccount, W3DDriveWrapper, Foundation (`DashboardPersonMenu`) |
| `add` | `plus` | + 메뉴 (새로 만들기) | FolderEditor (새 북마크), ClassificationEditor, LibraryEditor, ENOCollabSharingCmds, Foundation (`Actions`) |
| `share` | `mail-forward-alt` | 공유 메뉴 | 3DPlayUtils, 3DPlayExperienceModule |
| `home` | `home-alt` | 홈 | 없음 |
| `help` | `question-circle` | 도움말 메뉴 | Foundation, ENORouteMgmt, ENXMeetingMgmt, ENXDecisionMgmt, ENXHistoryUX, ENOSubscriptionMgmt, W3DLegal |
| `search` | 없음 | 검색창 자리 | 없음 |
| `content`, `social`, `notification` | `component`, `users-alt`, `bell-alt` | 오른쪽 패널 버튼. 누를 때 할 일은 TopFrame 이 정함 | 없음 |
| `enovTasks` <Badge type="warning" text="2026" /> | `task` | 작업 패널 버튼 (`ENOTASK_AP`) | 없음 |

- 상단 바에 어느 칸이 보이는지는 TopFrame 이 상단 바에 넘기는 `topMenus` 설정이 정합니다. 설정이 없으면 `context`, `profile`, `add`, `share`, `home`, `help` 와 검색창입니다. 3DDashboard 가 주는 실제 값은 webapps 에 없습니다.
- 칸 하나에는 플랫폼 자체 항목과 여러 위젯의 항목이 같이 들어갑니다. 위젯의 `setContent` 는 자기 항목만 바꿉니다.

## 만들기

<<< @/../WidgetSample/TopBarSample.html#create{js}

| 옵션 | 설명 |
| --- | --- |
| `id` | 필수. 채널 id. OOTB 는 모두 `widget.id`. 상단 바는 채널 단위로 메뉴를 보이거나 숨깁니다 |
| `content` | 만들면서 바로 `setContent` 할 내용 |
| `options.rightParentWindow` | 상단 바가 있는 창. 기본은 `window.parent`. 위젯이 iframe 안의 iframe 이면 지정합니다 (3DPlayUtils) |

- 클래스입니다. `new` 로 만듭니다.
- 만들면 InterCom 소켓으로 부모 창의 서버 `MainMenuBar` 에 붙고, 채널에 등록되면서 바로 활성이 됩니다.

## 항목 형식

`setContent({ 칸 id: [항목, ...] })` 의 항목입니다. 평범한 객체 또는 `DS/TopBarProxy/MenuItem` 인스턴스를 넣습니다.

| 키 | 설명 |
| --- | --- |
| `label` | 보일 글자 |
| `onExecute` | 누르면 **위젯 안에서** 불릴 함수. 인자와 `this` 는 그 항목(MenuItem) |
| `submenu` | 하위 항목 배열 |
| `fonticon` | 아이콘 글꼴 이름 (`'plus'` → `fonticon-plus`) |
| `icon` | 아이콘 그림 주소 |
| `disabled` | `true` 면 흐리게 보이고 누를 수 없음 |
| `countBadge` | 0 이상 정수. 항목과 상단 바 아이콘에 배지로 보임. 아이콘에는 그 칸 활성 항목의 합계 (100 이상이면 `+99`) |
| `selectable`, `selected` / `checked`, `type: 'checkbox'`, `multiSelect` | 선택 표시가 있는 항목 |
| `separator: true` | 구분선 |
| `group: true` | `label` 과 `submenu` 를 가진 제목 있는 묶음 |
| `className` | 항목에 붙일 CSS 클래스 |
| `id` | 없으면 자동 (`proxy id + '/' + cid`) |
| 그 밖의 키 | 그대로 보관. Foundation 은 `url` 을 넣고 `onExecute` 에서 `item.get('url')` 로 읽음 |

<<< @/../WidgetSample/TopBarSample.html#submenu{js}

- `MenuItem` 을 직접 만들어 두면 나중에 `item.set('label', ...)`, `item.set('countBadge', n)` 으로 바꿀 수 있습니다. 바뀌면 proxy 가 내용을 다시 보냅니다. FolderEditor, ENOCollabSharingCmds 가 `new MenuItem({ label, onExecute })` 로 만들어 `addContent({ add: item })` 합니다.
- `addContent` 는 항목 하나를 배열 없이 넣어도 됩니다 (FolderEditor `addContent({ add: item })`).

## 메서드

| 메서드 | 설명 |
| --- | --- |
| `setContent(content)` | 적은 칸의 이 proxy 항목을 `content` 로 바꿈. 적지 않은 칸은 그대로 |
| `addContent(content)` | 적은 칸 뒤에 덧붙임 |
| `empty()` | `profile`, `add`, `share`, `home`, `help` 칸의 이 proxy 항목을 지움 |
| `activate()` / `deactivate()` | 이 proxy 항목을 보이기 / 숨기기. 항목은 지우지 않음 |
| `die()` | 상단 바에서 이 proxy 를 빼고 항목을 지움. 그 뒤 메서드를 부르면 예외 (`Cannot call method setContent on zombie ...`) |
| `get(칸 id)` | 그 칸 (`MenuBarItem`). `.get('submenu')` 가 항목 목록 (`Menu`). `get(['add', 항목 id])` 처럼 경로로 항목을 찾음 |
| `addOnSubMenuDisplayedEvent({ 칸 id: fn })` | 그 칸의 메뉴가 열릴 때 부를 함수로 보입니다 <Badge type="danger" text="검증 필요" />. OOTB 사용 없음 |
| `setSearchOptionValues(box, placeholder)` | 검색 옵션 메시지(`onSearchOptionChange`)를 보냄. webapps 의 상단 바(MainMenuBar)에는 받는 곳이 없음 |
| `dump()` | 콘솔에 내용 출력 |

## 동작 방식

```text
new TopBarProxy({ id })
  → InterCom 소켓을 window.parent 의 서버 MainMenuBar 에 붙이고 subscribeToChannel(id)
  → 상단 바: 채널 id 에 proxy 를 등록하고 활성
setContent / addContent / 항목 set
  → 칸별 내용 전체를 JSON 으로 보냄 (onExecute 함수는 true 로 바뀜)
  → 상단 바: 칸마다 이 proxy 의 항목만 바꿔 넣음
사용자가 항목을 누름
  → 상단 바가 그 proxy 소켓에 onExecute + 항목 경로 (['add', 항목 id])
  → proxy: 경로로 항목을 찾아 onExecute(item) 를 위젯 안에서 부름
```

- 상단 바는 `MainMenuBar.setActiveMenuProviders(채널 id 배열)` 로 정한 채널의 활성 proxy 항목만 보입니다. TopFrame 은 플랫폼 자체 메뉴를 `setStaticMenuProviders` 로 늘 보이게 둡니다.
- 3DDashboard 밖의 앱은 직접 부릅니다. Store 는 `new TopBarProxy({ id: 'persoMenu' })` 뒤에 `MainMenuBar.setActiveMenuProviders('persoMenu')`, 3DPlayAppHybrid 는 `'CommandProvider1'` 입니다.

::: info 메뉴를 보일 위젯을 고르는 코드는 webapps 에 없습니다
상단 바를 그리는 코드(TopBar)와 띄우는 코드(TopFrame)는 webapps 에 있습니다. 하지만 3DDashboard 에서 `setActiveMenuProviders` 를 부르는 코드(탭을 바꾸거나 위젯을 고를 때)는 3DDashboard 본체에 있어 webapps 에 없습니다. 지금 보이는 위젯의 채널(`widget.id`)을 활성으로 하는 것으로 보입니다 <Badge type="danger" text="검증 필요" />.
:::

## 숨길 때, 닫을 때

<<< @/../WidgetSample/TopBarSample.html#lifecycle{js}

| 상황 | OOTB 처리 |
| --- | --- |
| 위젯 화면이 잠시 안 보일 때 | `deactivate()`, 다시 보이면 `activate()` (3DPlayUtils `setVisibility`) |
| 처음부터 숨겨 둘 때 | 만든 뒤 바로 `deactivate()` (3DPlayUtils `activeState: false`) |
| 끝낼 때 | `die()` (3DPlayUtils `dispose`) |
| 위젯이 다시 읽힐 때 | 한 번만 만듦. `TopBarProxy[widget.id]` 에 만들어 둔 것을 보관 (Foundation `initHelpSystem`, ENORouteMgmt) |

## 주의

::: warning onExecute 는 위젯 안에서 실행됩니다
상단 바로는 항목 내용이 JSON 으로만 가고 함수는 `true` 로 바뀝니다. 누르면 상단 바가 위젯에 눌린 항목의 경로를 보내고 위젯 쪽 함수가 불립니다. 위젯이 없어지면 실행할 곳도 없어지므로 끝낼 때 `die()` 합니다. 상단 바에는 채널의 proxy 를 한꺼번에 지우는 `_removeProvider(채널 id)` 도 있지만 webapps 안에서 부르는 곳이 없어, 대시보드가 위젯을 지울 때 대신 정리해 주는지는 확인하지 않았습니다 <Badge type="danger" text="검증 필요" />.
:::

- 칸 id 는 위 표의 것만 씁니다. proxy 에 없는 id (예: `context`, 2025 의 `enovTasks`)를 `setContent` / `addContent` 에 넣으면 그 칸을 찾지 못해 예외(TypeError)가 납니다.
- `countBadge` 에 음수나 소수를 넣으면 값이 들어가지 않고 로그만 남습니다 (`Invalid badgeCount attribute ... Must be a positive integer or zero!`).
- 같은 위젯에서 proxy 를 또 만들면 상단 바 쪽 등록이 초기화되거나 항목이 겹칠 수 있습니다 <Badge type="danger" text="검증 필요" />. OOTB 처럼 한 번만 만들어 보관합니다 (샘플은 `if (!topBar)`).
- `die()` 뒤에는 모든 메서드가 예외를 던집니다. 다시 쓰려면 새로 만듭니다.
- 상단 바 쪽에서 모르는 proxy 를 `activate` / `deactivate` 하면 최상위 창에서 `Failed to activate unknown Proxy` 예외가 납니다.

## 2026 변경

| 항목 | 내용 |
| --- | --- |
| `enovTasks` 칸 <Badge type="warning" text="2026" /> | proxy 칸 목록과 상단 바 칸 목록에 추가 (아이콘 `task`). TopFrame 은 이 버튼으로 작업 패널(`ENOTASK_AP`)을 엽니다. 위젯 항목을 넣으면 보이는지는 확인하지 않았습니다 <Badge type="danger" text="검증 필요" /> |
| `empty()` | 2025 와 같음 (`enovTasks` 는 비우지 않음). 상단 바가 proxy 를 지울 때(`die`)는 `enovTasks` 도 비움 |
| 그 밖 | 같음 (메서드, InterCom 메시지 이름 동일) |

## 관련 문서

- 위젯 자체 메뉴 (`setMenu`): [widget 객체](/widget/widget-api#보기-메뉴-기타)
- 창 사이 메시지 구조: [InterCom](/uwa/intercom)
- 화면 안 메뉴 컨트롤: [메뉴 (DS/Menu)](/controls/menu)
- 같은 방식으로 대시보드 공용 화면과 연동: [6W 태그 연동](/widget/tagger), [Compass 에 선택 객체 알리기](/widget/compass-pubsub)
