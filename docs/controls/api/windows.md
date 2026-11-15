---
title: Windows API
---

# Windows API <Badge type="info" text="원본 분석" />

`DS/Windows/*` 의 모든 모듈(Window, Dialog, Panel, ImmersiveWindow, WindowGroup, ImmersiveFrame, DockingElement, WindowManager)의 속성, 메서드, 이벤트와 열거형입니다. 사용법은 [다이얼로그](/controls/dialog), [창 묶기, 붙이기, 위치 저장](/controls/window-layout), [분할 레이아웃 (DockingElement)](/controls/docking-element) 에 있습니다.

```js
var dlg = new Dialog({ title: '승인', content: el, modalFlag: true, buttons: { Ok: new Button({ onClick: function (e) { e.dsModel.dialog.close(); } }) } });
dlg.addEventListener('close', function () { console.log('닫힘'); });
```

::: info 근거
Windows.js (2025: 10,547 줄, 2026: 10,728 줄) 의 모듈 12개 publishedProperties / prototype / `fire("...")` / `_fireAndBroadcast("...")`. CoreBase.js 의 WebUXGlobalEnums (WUXStandardButtonEnum, WUXStandardButtonRoleEnum, WUXCustomButtonRoleEnum, WUXDockAreaEnum, WUXSnapAreaEnum, WUXWindowGroupTypeEnum). Utilities.js 의 saveDataInLocalPreferences / getDataFromLocalPreferences. 2026 은 enovia2026_webapps 의 같은 파일. 사용 파일 수(2025, Windows 폴더 밖): Dialog 148, ImmersiveFrame 134, Panel 54, DockingElement 7, DockingElementForImmersiveFrame 2, WindowManager 1, WindowGroup / WindowGroupAsTab / WindowGroupAsGrid / ImmersiveWindow / WindowActivator 0
:::

## 모듈과 상속

| 모듈 | 부모 | 하는 일 | 직접 쓰는 일 |
| --- | --- | --- | --- |
| `DS/Windows/Window` | `DS/Controls/Abstract` | 창의 공통 바탕 (제목줄, 이동, 크기, 붙이기, 위치 저장) | 드묾 |
| `DS/Windows/Dialog` | Window | 아래쪽 버튼이 있는 다이얼로그, 모달 | 많음 |
| `DS/Windows/Panel` | Window | 붙이기, 접기, 탭 묶기가 되는 패널 | 보통 |
| `DS/Windows/ImmersiveWindow` | Panel | 붙지 않는 떠 있는 창 (배경, 테두리, 투명도) | 드묾 |
| `DS/Windows/WindowGroup` | Window | 창 묶음의 공통 바탕 | 없음 |
| `DS/Windows/WindowGroupAsTab` | WindowGroup | 탭 묶음. Panel 을 다른 Panel 제목줄에 놓으면 생김 | 없음 (묶은 결과로 받음) |
| `DS/Windows/WindowGroupAsGrid` | WindowGroup | 위아래 / 좌우 격자 묶음. 기본으로 꺼져 있음 | 없음 |
| `DS/Windows/ImmersiveFrame` | `DS/Controls/Abstract` | 창을 담는 틀, 가장자리 붙는 영역 | 많음 |
| `DS/Windows/DockingElement` | `DS/Controls/Abstract` | 한쪽 영역 + 나머지 영역 분할 레이아웃 | 보통 (CollectionView, Tree, Skeleton) |
| `DS/Windows/DockingElementForImmersiveFrame` | DockingElement | ImmersiveFrame 의 가장자리 붙는 영역 | 없음 (`getDockingElement` 로 받음) |
| `DS/Windows/WindowManager` | - | 활성 창, 맨 앞으로 (정적 함수) | 드묾 |
| `DS/Windows/WindowActivator` | - | 창 안의 포커스 요소 찾기 (정적 함수) | 없음 |

모든 클래스는 [Abstract](/controls/api/webux-controls#abstract) 의 속성(`disabled`, `visibleFlag`, `identifier` ...)도 가집니다.

## Window

Dialog, Panel, WindowGroup 이 물려받는 속성입니다. `Window` 를 직접 만드는 일은 드뭅니다.

| 키 | 타입 | 기본값 | 설명 |
| --- | --- | --- | --- |
| <code>title</code> | <code>string</code> | <code>''</code> | 제목 |
| <code>icon</code> | <code>object</code> | <code>''</code> | 제목 아이콘 |
| <code>header</code> | <code>object</code> | <code>null</code> | 제목줄 아래 요소 |
| <code>content</code> | <code>object</code> | <code>null</code> | 내용 (요소 / 문자열은 HTML) |
| <code>immersiveFrame</code> | <code>object</code> | <code>null</code> | 담을 ImmersiveFrame |
| <code>position</code> | <code>object</code> | - | <code>{ my, at, of, offsetX, offsetY, collision }</code> |
| <code>activeFlag</code> | <code>boolean</code> | <code>true</code> | 활성 창 (WindowManager 가 하나만 켬) |
| <code>movableFlag</code> | <code>boolean</code> | <code>true</code> | 끌어서 이동 |
| <code>closeButtonFlag</code> | <code>boolean</code> | <code>true</code> | X 버튼 |
| <code>expandedFlag</code> | <code>boolean</code> | <code>true</code> | 펼침 |
| <code>allowedDockAreas</code> | <code>integer</code> | - | 붙일 수 있는 위치 (WUXDockAreaEnum 비트 합) |
| <code>currentDockArea</code> | <code>integer</code> | - | 붙어 있는 위치 (WUXDockAreaEnum). 바꾸면 붙거나 떨어짐 |
| <code>windowGroup</code> | <code>object</code> | <code>null</code> | 속한 묶음 (WindowGroupAsTab / AsGrid). <code>null</code> 을 넣으면 묶음에서 빠짐 |
| <code>activeSnapArea</code> | <code>integer</code> | - | 끌어 놓는 중 강조된 붙는 곳 (WUXSnapAreaEnum) |
| <code>resizableFlag</code> | <code>boolean</code> | <code>false</code> | 크기 조절 |
| <code>width</code> | <code>integer</code> | <code>-1</code> | 너비 px (<code>-1</code> 자동) |
| <code>height</code> | <code>integer</code> | <code>-1</code> | 높이 px (<code>-1</code> 자동. 이때 내용 영역 높이는 <code>auto</code>) |
| <code>minWidth</code> | <code>integer</code> | <code>150</code> | 최소 너비 |
| <code>minHeight</code> | <code>integer</code> | <code>0</code> | 최소 높이 |
| <code>maximizedFlag</code> | <code>boolean</code> | <code>false</code> | 최대화 상태 |
| <code>allowMaximizeFlag</code> | <code>boolean</code> | <code>true</code> | 최대화 허용 |
| <code>maximizeButtonFlag</code> | <code>boolean</code> | <code>false</code> | 최대화 버튼 |
| <code>horizontallyStretchableFlag</code> | <code>boolean</code> | <code>false</code> | 가로로 늘림 |
| <code>verticallyStretchableFlag</code> | <code>boolean</code> | <code>false</code> | 세로로 늘림 |
| <code>autoCloseFlag</code> | <code>boolean</code> | <code>true</code> | <code>close()</code> 때 <code>destroy()</code> |
| <code>titleBarVisibleFlag</code> | <code>boolean</code> | <code>true</code> | 제목줄 표시 |
| <code>usePaddingFlag</code> | <code>boolean</code> | <code>true</code> | 안쪽 여백 |
| <code>ensureHeightDefinitionOnHierarchy</code> | <code>boolean</code> | <code>false</code> | 탭 묶음 안에서 창 높이를 100% 로 (CSS 속성 선택자) |
| <code>activateButtonsIfActionPerformed</code> | <code>boolean</code> | <code>true</code> | Enter / Esc 가 이미 처리된 경우에도 버튼 실행 |
| <code>buttons</code> | <code>object</code> | <code>{}</code> | 표준 버튼 <code>{ Ok: Button, ... }</code> |
| <code>onContextualEvent</code> | <code>function</code> | 함수 | 제목줄 오른쪽 클릭 메뉴 |
| <code>getDefaultFocusableElement</code> | <code>function</code> | - | 처음 포커스 받을 요소를 돌려주는 함수 |
| <code>identifier</code> (Abstract) | <code>string</code> | - | 숫자가 아닌 문자열이면 위치, 크기, 붙은 곳, 최대화, 묶음을 localStorage 에 저장하고 복원 ([위치 저장](#위치-저장-localstorage)). 생성 옵션으로만 |
| <code>titleBarButtons</code> <Badge type="warning" text="2026" /> | <code>Window.TitleBarButtonsEnum</code> | <code>Close</code> (1) | 제목줄 버튼 (TitleBarButtonsEnum 비트 합) |
| <code>informationData</code> <Badge type="warning" text="2026" /> | <code>object</code> | - | 정보 버튼 내용 |
| <code>settingsData</code> <Badge type="warning" text="2026" /> | <code>object</code> | - | 설정 버튼 내용 |
| <code>helpData</code> <Badge type="warning" text="2026" /> | <code>function</code> | - | 도움말 버튼 함수 |

| 메서드 | 설명 |
| --- | --- |
| <code>close()</code> | <code>close</code> 이벤트 후 <code>autoCloseFlag</code> 면 <code>destroy()</code> |
| <code>isBeingClosed()</code> | 닫히는 중인지 |
| <code>destroy()</code> | 정리 (이벤트 없음) |
| <code>bringToFront()</code> | 맨 앞으로 (붙어 있거나 최대화면 무시) |
| <code>ensureVisible()</code> | 보이게 하고 펼침. 묶음 안이면 그 탭을 보임 |
| <code>getAbsolutePosition()</code> / <code>setAbsolutePosition(pos)</code> | 화면 기준 위치 |
| <code>getOffsetParentPosition()</code> / <code>getOffsetParentMargins()</code> / <code>getPositionRelativeToOffsetParent()</code> | 부모 기준 위치 |
| <code>getTitleBar()</code> | 제목줄 요소 |
| <code>getDefaultButtonLabel(name)</code> | 표준 버튼의 번역된 글자 |
| <code>getBoundingClientRect()</code> | 크기, 위치 |
| <code>getDockedFlag()</code> | 가장자리에 붙어 있는지 (<code>currentDockArea !== NoneDockArea</code>) |
| <code>getEmbeddedState()</code> | 붙어 있거나 묶음 안에 있는지 |
| <code>getContentVisibleState()</code> | 내용이 실제로 보이는지 (보임 + 펼침 + 묶음에서 지금 탭) |
| <code>getCollapsibleState()</code> / <code>getFloatableState()</code> / <code>getMovableState()</code> / <code>getResizableFlag()</code> | 지금 접기 / 떼기 / 이동 / 크기 조절이 되는지 |
| <code>getMaxWidth()</code> / <code>getMaxHeight()</code> | 지금 놓인 곳에서의 최대 크기 |
| <code>getFloatLayer()</code> | 떠 있는 층 (<code>'bottom'</code> / <code>'middle'</code> / <code>'top'</code>) |
| <code>isAWindowGroup()</code> | WindowGroup 인지 |
| <code>attachToWindow(target, snapArea, beforeOrAfter?)</code> | <code>target</code> 창과 묶음. <code>snapArea</code> 가 <code>InsideArea</code> 면 탭 묶음. 성공 1, 실패 -1 ([탭으로 묶기](/controls/window-layout#탭으로-묶기)) |
| <code>arePreferencesRestored()</code> | 저장된 위치를 복원했는지 |
| <code>deleteWindowPreferences()</code> | localStorage 에 저장된 이 창의 값을 지움 |
| <code>buildDefaultContextualMenu(e, info)</code> | 제목줄 기본 오른쪽 클릭 메뉴 항목 |
| <code>getMinWidth()</code> <Badge type="warning" text="2026" /> | <code>minWidth</code> 와 내용이 요구하는 최소 너비 중 큰 값 |
| <code>hasTitleBarButton(button)</code> / <code>setTitleBarButtonVisibility(button, flag)</code> <Badge type="warning" text="2026" /> | 제목줄 버튼이 있는지 / 켜고 끄기 (<code>titleBarButtons</code> 비트 조작) |
| <code>buildAndUpdateModeChooser(options)</code> <Badge type="warning" text="2026" /> | 내용 위에 [ModeChooser](/controls/layout) 를 만들거나 옵션을 바꿈 |

| 이벤트 | 시점 | <code>e.options</code> |
| --- | --- | --- |
| <code>close</code> | <code>close()</code> 또는 X 버튼 | - |
| <code>visibilityChange</code> | 처음 화면에 그려져 보일 때 | - |
| <code>visibleStateChange</code> | 표시 상태 변경 | - |
| <code>contentVisibleStateChange</code> | 내용 표시 상태 변경 (접기, 탭 바뀜 포함) | - |
| <code>activate</code> / <code>deactivate</code> | 활성 / 비활성 | - |
| <code>move</code> | 위치 변경 | - |
| <code>resize</code> | 크기 변경 (<code>width</code> / <code>height</code> 변경, 최대화, 붙는 영역 크기 변경) | - |
| <code>startResize</code> / <code>stopResize</code> | 사용자가 크기 조절 손잡이를 누름 / 놓음 | <code>startResize</code>: <code>{ side }</code> (<code>'bottom right'</code>, <code>'top'</code>, <code>'bottom'</code>, <code>'right'</code> ...) |
| <code>expand</code> / <code>collapse</code> | 펼침 / 접힘 | - |
| <code>prePlaceChange</code> / <code>placeChange</code> | 놓인 곳(붙은 곳, 묶음) 변경 전 / 후 | <code>prePlaceChange</code>: <code>{ previousPlace }</code> |
| <code>startWindowDrag</code> / <code>windowDrag</code> / <code>stopWindowDrag</code> | 제목줄 끌기 | - |
| <code>settingsStateChange</code> <Badge type="warning" text="2026" /> | 설정 버튼 | - |
| <code>informationStateChange</code> <Badge type="warning" text="2026" /> | 정보 버튼 | - |
| <code>helpTrigger</code> <Badge type="warning" text="2026" /> | 도움말 버튼 | - |

`resize`, `stopResize`, `visibilityChange` 는 창 안의 컨트롤에도 전달됩니다 (`_fireAndBroadcast`). 묶음(WindowGroup)에서 나면 묶인 창에도 전달됩니다.

## Dialog

`DS/Windows/Dialog`. Window 속성에 아래가 더해집니다.

| 키 | 타입 | 기본값 | 설명 |
| --- | --- | --- | --- |
| <code>modalFlag</code> | <code>boolean</code> | <code>false</code> | 뒤를 가리고 막음 |
| <code>checkboxInfos</code> | <code>object</code> | - | 아래쪽 체크박스 <code>{ label, checkFlag, tooltipInfos, saveCheckStateInPreferences }</code> |
| <code>customButtonsDefinition</code> | <code>object</code> | - | Button 옵션 배열 (<code>role</code>, 2026 은 <code>secondaryActions</code> 포함) |

| 메서드 | 설명 |
| --- | --- |
| <code>getCustomButtons()</code> | <code>customButtonsDefinition</code> 로 만든 버튼 배열 |
| <code>Dialog.getCheckBoxInfoStateInPreferences(identifier)</code> <Badge type="warning" text="2026" /> | 정적 함수. 저장된 체크 상태 (<code>true</code> / <code>false</code> / 없으면 <code>undefined</code>) |

- `customButtonsDefinition` 은 `buttons` 가 비어 있을 때만 씁니다. 항목은 Button 옵션에 `role` (`'validate'` / `'escape'`) 을 더한 것입니다.
- <Badge type="warning" text="2026" /> 항목에 `secondaryActions` 를 주면 `DS/Controls/DropdownButton` (분할 버튼)으로 만듭니다. `label`, `icon`, `disabled`, `onClick` 은 주 버튼(`mainAction`)이 됩니다. 이 버튼의 `onClick` 에서는 `e.dsModel.dialog` 대신 다이얼로그 변수를 직접 씁니다.
- `checkboxInfos` 는 `{ label, checkFlag, tooltipInfos }` 입니다. 사용자가 체크하면 `checkFlag` 가 바뀝니다.
- <Badge type="warning" text="2026" /> `identifier` 와 `checkboxInfos.saveCheckStateInPreferences: true` 를 같이 주면 체크 상태가 localStorage 에 저장되고, 다음에 같은 `identifier` 로 열 때 그 값으로 체크됩니다 ([다시 묻지 않음](/controls/window-layout#다시-묻지-않음-저장-2026)).
- `immersiveFrame` 이 없으면 `document.body` 에 붙고 가운데에 놓입니다.

## Panel

`DS/Windows/Panel`. Window 속성에 아래가 더해집니다.

| 키 | 타입 | 기본값 | 설명 |
| --- | --- | --- | --- |
| <code>collapsibleFlag</code> | <code>boolean</code> | <code>true</code> | 접기 버튼 |
| <code>snappableFlag</code> | <code>boolean</code> | <code>true</code> | 다른 Panel 과 묶기 (제목줄을 끌어 놓기, <code>attachToWindow</code>) |
| <code>floatableFlag</code> | <code>boolean</code> | <code>true</code> | 떼어서 띄우기 |
| <code>headerPopupContent</code> | <code>Object</code> | - | 제목줄 팝업 내용 |
| <code>pinnedFlag</code> | <code>boolean</code> | <code>false</code> | 고정 |
| <code>showTitleFlag</code> | <code>boolean</code> | <code>true</code> | 제목 표시 |

| 메서드 | 설명 |
| --- | --- |
| <code>supportsGroupType(type)</code> | 그 묶음 종류(WUXWindowGroupTypeEnum)를 지원하는지. 탭은 항상, 격자는 ImmersiveFrame 이 허용할 때만 |
| <code>buildStandardButtons()</code> | 제목줄에 아이콘 버튼 두 개(<code>check</code>, <code>wrong</code>)를 <code>buttons = { Ok, Cancel }</code> 로 만듦 |
| <code>buildSuperFeature({ title, content: [{ label, icon, onSelect }] })</code> | 제목줄에 라디오 버튼 묶음을 만듦. 고르면 <code>onSelect</code> |

## ImmersiveWindow

`DS/Windows/ImmersiveWindow`. Panel 을 상속한 **떠 있는 창** 입니다. `allowedDockAreas` 가 `NoneDockArea` 로 정해져 붙지 않습니다. Panel 속성에 아래가 더해집니다.

| 키 | 타입 | 기본값 | 설명 |
| --- | --- | --- | --- |
| <code>useBackgroundFlag</code> | <code>boolean</code> | <code>true</code> | 배경. <code>false</code> 면 <code>no-background</code> 속성 (제목줄도) |
| <code>backgroundDraggableFlag</code> | <code>boolean</code> | <code>false</code> | 내용 빈 곳을 끌어도 이동 |
| <code>decorationFlag</code> | <code>boolean</code> | <code>true</code> | 테두리, 그림자. <code>false</code> 면 <code>no-decoration</code> |
| <code>opacity</code> | <code>number</code> | - | 창 투명도 (0 ~ 1) |
| <code>snappableFlag</code> | <code>boolean</code> | <code>false</code> | 다른 Panel 과 묶기 (Panel 과 기본값이 다름) |
| <code>collapsibleFlag</code> | <code>boolean</code> | <code>false</code> | 접기 버튼 (Panel 과 기본값이 다름) |
| <code>useGripFlag</code> | <code>boolean</code> | <code>false</code> | 위쪽에 끌기 손잡이 (<code>wux-ui-3ds-drag-grip</code>) |

## WindowGroup, WindowGroupAsTab, WindowGroupAsGrid

`DS/Windows/WindowGroup` 은 묶음의 공통 바탕(Window 상속), `WindowGroupAsTab` 은 탭 묶음, `WindowGroupAsGrid` 는 격자 묶음입니다. 직접 만들지 않고, Panel 을 묶으면 Panel 이 만들어 각 창의 `windowGroup` 에 넣습니다.

- 묶을 수 있는 창은 Panel 계열뿐입니다. Window 와 Dialog 는 `_canBeSnapSource` / `_canBeSnapTarget` 이 항상 `false` 입니다.
- Panel 끼리, 같은 ImmersiveFrame 안에서, `snappableFlag: true` 일 때만 묶입니다.
- **격자 묶음은 기본으로 꺼져 있습니다.** `Panel.supportsGroupType(GroupAsGrid)` 는 ImmersiveFrame 의 내부 값 `__acceptsSnapping__` 이 `true` 일 때만 `true` 인데, 원본(2025, 2026) 어디에서도 `true` 로 바꾸지 않습니다.
- 창이 하나만 남으면 묶음은 그 창을 풀고 스스로 없어집니다.
- 묶음의 제목은 보이는 창 제목을 이어 붙인 `[부품, 문서]` 입니다 (6개가 넘으면 `, ...`).

| 메서드 (공통) | 설명 |
| --- | --- |
| <code>getGroupType()</code> | WUXWindowGroupTypeEnum (<code>GroupAsGrid</code> 0, <code>GroupAsTab</code> 1) |
| <code>getEmbeddedWindows()</code> | 묶인 창 전부 |
| <code>getVisibleEmbeddedWindows()</code> | 그중 <code>visibleFlag</code> 인 창 |
| <code>getDisplayedWindows()</code> | 지금 화면에 보이는 창 (탭 묶음은 지금 탭) |
| <code>containsWindow(win)</code> | 묶음 안에 있는지 |
| <code>attachWindow(win, target, snapArea, beforeOrAfter?, interactive?)</code> | 창을 넣음. 성공 1, 실패 -1. 보통 <code>win.attachToWindow(...)</code> 또는 <code>win.windowGroup = group</code> 을 씀 |
| <code>detachWindow(win)</code> | 창을 뺌. 보통 <code>win.windowGroup = null</code> 을 씀 |
| <code>ensureWindowVisible(win)</code> | 그 창이 보이게 함 (탭 묶음은 그 탭으로) |
| <code>close()</code> | <code>autoCloseFlag</code> 면 묶인 창을 모두 <code>close()</code>, 아니면 <code>close</code> 이벤트만 |

| WindowGroupAsTab | 설명 |
| --- | --- |
| <code>currentWindows</code> (속성, <code>Array</code>, 기본 <code>[]</code>) | 지금 보이는 탭의 창. <code>[win]</code> 을 넣으면 그 탭으로 바뀜 |
| <code>getCollapsibleState()</code> | 접을 수 있는지 |

| WindowGroupAsGrid | 설명 |
| --- | --- |
| <code>replaceWindow(old, new)</code> | 격자 안의 창을 다른 창(또는 묶음)으로 바꿈 |

## ImmersiveFrame

`DS/Windows/ImmersiveFrame`

| 키 | 타입 | 기본값 | 설명 |
| --- | --- | --- | --- |
| <code>dockingElementsPriority</code> | <code>Array</code> | <code>[LeftDockArea, RightDockArea, TopDockArea, BottomDockArea]</code> (<code>[4, 8, 1, 2]</code>) | 붙는 영역을 만드는 순서. 읽기 전용 |
| <code>reactToPointerEventsFlag</code> | <code>boolean</code> | <code>true</code> | 포인터 이벤트 받음 |
| <code>dockingElementsAutoExpandFlag</code> | <code>boolean</code> | <code>true</code> | 붙는 영역 자동 펼침 |

| 메서드 | 설명 |
| --- | --- |
| <code>addWindow(win)</code> / <code>removeWindow(win)</code> / <code>containsWindow(win)</code> | 창 관리 (보통 <code>immersiveFrame</code> 옵션으로 자동) |
| <code>dockWindow(win, area, options?)</code> | 가장자리에 붙임. 묶음 안의 창이면 묶음에서 빠져서 붙음. <code>options.ignorePreferences</code> / <code>restoreDockedPreferences</code> |
| <code>undockWindow(win)</code> | 떼서 띄움 (<code>currentDockArea = NoneDockArea</code>) |
| <code>getDockingElement(area)</code> | 그 가장자리의 DockingElementForImmersiveFrame |
| <code>getWindowsInDockingElement(area)</code> | 그 가장자리에 붙은 창 |
| <code>getFreeZone()</code> | 붙는 영역을 뺀 가운데 영역 요소 |
| <code>setContentInBackgroundLayer(el)</code> | 배경 층 내용 |
| <code>moveWindowToMaximizeZone(win)</code> | 창을 가운데 영역에 넣음 (최대화 자리) |
| <code>getSnapTargetWindow()</code> | 끌어 놓는 중 묶일 대상 창 |
| <code>isInsertedInDOM()</code> | 틀이 문서에 붙었는지 |
| <code>setViewer(v)</code> / <code>getViewer()</code> / <code>getViewerAsync()</code> | 틀에 연결된 3D 뷰어 객체 보관 (3D 앱용). <code>getViewerAsync</code> 는 <code>setViewer</code> 까지 기다리는 Promise |
| <code>setSlidingWindowGroup(group)</code> | 내부용 (탭 막대 미끄러짐) |
| <code>destroyAllWindows()</code> / <code>destroyWindows(list)</code> | 창 정리 |
| <code>destroy()</code> | 틀과 안의 창 모두 정리 |

이벤트: <code>addWindow</code>, <code>removeWindow</code>, <code>freeZoneResize</code>

- `identifier` 가 없는 창에는 틀이 `"1"`, `"2"` ... 를 붙입니다. 숫자 identifier 는 위치 저장에 쓰이지 않습니다.

## DockingElement

`DS/Windows/DockingElement`. 영역을 "한쪽 영역(docking zone) + 나머지 영역(free zone)" 으로 나누는 분할 레이아웃입니다. 경계를 끌어 크기를 바꾸고, 접기 막대로 접습니다. 사용법은 [분할 레이아웃](/controls/docking-element) 에 있습니다.

| 키 | 타입 | 기본값 | 설명 |
| --- | --- | --- | --- |
| <code>side</code> | <code>number</code> | <code>TopDockArea</code> | 한쪽 영역의 위치 (WUXDockAreaEnum). 읽기 전용이라 생성 옵션으로만 |
| <code>dockingZoneContent</code> | <code>object</code> | <code>null</code> | 한쪽 영역 내용 (요소) |
| <code>freeZoneContent</code> | <code>object</code> | <code>null</code> | 나머지 영역 내용 (요소. 다른 DockingElement 의 <code>getContent()</code> 를 넣어 겹침) |
| <code>dockingZoneSize</code> | <code>number</code> | <code>100</code> | 한쪽 영역 크기 px (좌우면 너비, 위아래면 높이) |
| <code>minSize</code> / <code>maxSize</code> | <code>number</code> | <code>20</code> / <code>screen.width</code> | 끌어서 바꿀 때 최소 / 최대 |
| <code>resizableFlag</code> | <code>boolean</code> | <code>true</code> | 경계를 끌어 크기 조절 |
| <code>collapsibleFlag</code> | <code>boolean</code> | <code>true</code> | 접을 수 있음 |
| <code>collapseDockingZoneFlag</code> | <code>boolean</code> | <code>false</code> | 접힘 상태 |
| <code>visibleCollapserFlag</code> | <code>boolean</code> | <code>true</code> | 접기 막대 표시 |
| <code>collapserStyle</code> | <code>enum</code> | <code>STANDARD</code> (0) | 접기 막대 모양 (<code>DockingElement.COLLAPSER_STYLE</code>). 생성 옵션으로만 |
| <code>autoExpandFlag</code> | <code>boolean</code> | <code>true</code> | 접힌 영역에 마우스를 올리면 잠시 펼침 |
| <code>visibleDockingZoneFlag</code> | <code>boolean</code> | <code>true</code> | 한쪽 영역과 접기 막대를 모두 숨김 / 보임 |
| <code>overlayDockingZoneFlag</code> | <code>boolean</code> | <code>false</code> | 한쪽 영역이 나머지 영역을 밀지 않고 위에 겹침 |
| <code>interactiveDockAreaVisibleFlag</code> | <code>boolean</code> | <code>false</code> | 끌어 놓을 수 있는 영역 표시 (ImmersiveFrame 용) |
| <code>useBordersFlag</code> | <code>boolean</code> | <code>true</code> | 경계선 |
| <code>useBackgroundFlag</code> | <code>boolean</code> | <code>false</code> | 배경 |
| <code>alignment</code> | <code>string</code> | <code>'near'</code> | 한쪽 영역 안 정렬: <code>'near'</code>, <code>'center'</code>, <code>'far'</code> |
| <code>identifier</code> (Abstract) | <code>string</code> | - | 주면 사용자가 접기 막대로 바꾼 접힘 상태를 localStorage 에 저장, 복원 |

| 메서드 | 설명 |
| --- | --- |
| <code>addContent(el)</code> | 한쪽 영역에 요소를 덧붙임 |
| <code>addContent({ content, label, icon ... }, { dockingZoneSize })</code> | 둘째 인자를 주면 한쪽 영역을 탭으로 바꾸고 탭을 더함. 탭마다 크기 |
| <code>containsContent(el)</code> / <code>removeContent(el)</code> / <code>replaceContent(old, new)</code> | 내용 확인 / 빼기 / 바꾸기 |
| <code>updateContent(tabDef)</code> / <code>updateContentVisibility(el, flag)</code> | 탭 내용, 탭 보임 바꾸기 |
| <code>activateTab(el)</code> / <code>isContentActive(el)</code> / <code>getIndexFromContent(el)</code> | 탭 고르기 / 지금 탭인지 / 순번 |
| <code>getCollasperSize()</code> | 접기 막대 크기 (원본 이름의 철자 그대로) |
| <code>getResizeInfos()</code> | 끌기 방향, 커서, 가능한 최대 크기 |
| <code>getContent()</code> (Abstract) | 바깥 요소. 다른 DockingElement 의 <code>freeZoneContent</code> 에 넣음 |

| 이벤트 | 시점 |
| --- | --- |
| <code>dockStartResize</code> / <code>dockIsResizing</code> / <code>dockResize</code> | 사용자가 경계를 누름 / 끄는 중 / 놓음 |
| <code>collapse</code> / <code>expand</code> | 접힘 / 펼침 (코드로 바꿔도) |
| <code>dimensionsChanged</code> | 접기 / 펼치기 애니메이션이 끝나 나머지 영역 크기가 정해진 뒤 |

## DockingElementForImmersiveFrame

`DS/Windows/DockingElementForImmersiveFrame`. ImmersiveFrame 이 가장자리마다 만드는 DockingElement 입니다. `frame.getDockingElement(WUXDockAreaEnum.RightDockArea)` 로 받습니다.

| 메서드 | 설명 |
| --- | --- |
| <code>getContainedWindows()</code> / <code>getContainedVisibleWindows()</code> / <code>getDisplayedWindows()</code> | 붙은 창 / 보이는 창 / 화면에 나온 창 |
| <code>shouldDockingZoneSizeChangeWhenDockingWindow(win)</code> | 창을 붙일 때 영역 크기를 창에 맞출지 |
| <code>updateVisibleDockingZoneFlag()</code> | 보이는 창이 있으면 영역을 보이고, 없으면 숨김 |

이벤트: <code>resize</code>, <code>startResize</code>, <code>stopResize</code> (붙은 창에도 전달). 사용자가 경계를 끌어 바꾼 영역 크기는 **붙어 있는 창의** `identifier` 로 저장됩니다 (창의 붙은 크기 값). 그 창을 다시 붙이면 저장된 크기로 열립니다.

## WindowManager, WindowActivator

정적 함수만 있는 모듈입니다.

| 함수 | 설명 |
| --- | --- |
| <code>WindowManager.getActiveWindow()</code> | 지금 활성인 창 (<code>activeFlag: true</code> 인 하나) |
| <code>WindowManager.setActiveWindow(win)</code> | 그 창을 활성으로, 이전 창은 비활성으로 |
| <code>WindowManager.bringToFront(win)</code> | 같은 층의 창보다 <code>z-index</code> 를 높임 (붙어 있거나 최대화면 무시) |
| <code>WindowActivator.getFocusedWidget(el)</code> | <code>el</code> 안에 포커스가 있으면 그 요소 |

## 위치 저장 (localStorage)

`identifier` 를 준 창과 DockingElement 는 **사용자가 바꾼 값** 을 브라우저 localStorage 에 저장하고, 다음에 만들 때 복원합니다. 코드로 `position`, `width` 를 바꾸는 것은 저장하지 않습니다.

| localStorage 키 | 값 | 저장 시점 |
| --- | --- | --- |
| <code>wux-windows-window</code> | <code>{ [identifier]: { position, ... } }</code>. 위치, 크기, 붙은 크기, 붙은 곳, 최대화, 탭 묶음, 떼어 낸 창, (2026) 체크 상태(<code>checkBoxInfoState</code>) | 제목줄 끌기를 놓을 때, 크기 조절을 놓을 때, 최대화, 붙이기 / 묶기, 붙는 영역 경계를 놓을 때, (2026) 체크박스를 누를 때 |
| <code>wux-docking-element-left</code> / <code>-right</code> / <code>-top</code> / <code>-bottom</code> | <code>{ [identifier]: { collapseDockingZoneFlag: { collapseDockingZoneFlag } } }</code> | 접기 막대를 누를 때 |

- `identifier` 가 `'1'`, `'2'` 처럼 0 이상의 정수 문자열이면 저장하지 않습니다 (ImmersiveFrame 이 자동으로 붙이는 값과 구분).
- 위젯 설정(`widget.setValue`)에 저장하는 함수도 있지만(`saveDataInWidgetPreferences`) Windows 는 localStorage 만 씁니다.
- localStorage 가 꽉 차면 콘솔에 `Failed to store in Local Storage the key: ...` 를 찍고 넘어갑니다.

## 열거형

`DS/Windows/*` 또는 `DS/Controls/*` 를 불러오면 전역(`window`)에 생깁니다.

### WUXStandardButtonEnum

`buttons` 의 키 이름입니다. `value` 순서로 표시됩니다.

| 이름 | value | role |
| --- | --- | --- |
| <code>Ok</code> | 1 | AcceptRole |
| <code>Open</code> | 2 | AcceptRole |
| <code>Save</code> | 4 | AcceptRole |
| <code>Apply</code> | 8 | ApplyRole |
| <code>Reset</code> | 16 | ResetRole |
| <code>RestoreDefaults</code> | 32 | ResetRole |
| <code>Help</code> | 64 | HelpRole |
| <code>SaveAll</code> | 128 | AcceptRole |
| <code>Yes</code> | 256 | YesRole |
| <code>YesToAll</code> | 512 | YesRole |
| <code>No</code> | 1024 | NoRole |
| <code>NoToAll</code> | 2048 | NoRole |
| <code>Abort</code> | 4096 | RejectRole |
| <code>Retry</code> | 8192 | AcceptRole |
| <code>Ignore</code> | 16384 | AcceptRole |
| <code>Discard</code> | 32768 | DestructiveRole |
| <code>Cancel</code> | 65536 | RejectRole |
| <code>Close</code> | 131072 | RejectRole |

### WUXStandardButtonRoleEnum

| 역할 | 값 | Dialog 동작 |
| --- | --- | --- |
| <code>AcceptRole</code> | 1 | <code>emphasize: 'primary'</code>, 첫 버튼이 Enter |
| <code>YesRole</code> | 5 | <code>emphasize: 'primary'</code>, 첫 버튼이 Enter |
| <code>RejectRole</code> | 2 | 첫 버튼이 Esc |
| <code>NoRole</code> | 6 | 첫 버튼이 Esc |
| <code>DestructiveRole</code>, <code>HelpRole</code>, <code>ApplyRole</code>, <code>ResetRole</code> | 3, 4, 7, 8 | 키 연결 없음 |

### WUXCustomButtonRoleEnum

| 이름 | 값 |
| --- | --- |
| <code>Validate</code> | <code>'validate'</code> |
| <code>Escape</code> | <code>'escape'</code> |

### WUXDockAreaEnum

| 이름 | 값 |
| --- | --- |
| <code>NoneDockArea</code> | 0 |
| <code>TopDockArea</code> | 1 |
| <code>BottomDockArea</code> | 2 |
| <code>LeftDockArea</code> | 4 |
| <code>RightDockArea</code> | 8 |

### WUXSnapAreaEnum

창을 다른 창에 끌어 놓을 때 놓는 곳입니다. `attachToWindow` 의 둘째 인자입니다.

| 이름 | 값 | 결과 |
| --- | --- | --- |
| <code>NoneArea</code> | 0 | 묶지 않음 |
| <code>OutsideArea</code> | 1 | 대상 밖 |
| <code>TopArea</code> / <code>BottomArea</code> / <code>LeftArea</code> / <code>RightArea</code> | 2 / 3 / 4 / 5 | 격자 묶음 (기본으로 꺼져 있음) |
| <code>InsideArea</code> | 6 | 탭 묶음 (제목줄 위에 놓음) |

### WUXWindowGroupTypeEnum

| 이름 | 값 |
| --- | --- |
| <code>GroupAsGrid</code> | 0 |
| <code>GroupAsTab</code> | 1 |

### DockingElement.COLLAPSER_STYLE

전역이 아니라 `DS/Windows/DockingElement` 클래스에 있습니다.

| 이름 | 값 | 설명 |
| --- | --- | --- |
| <code>STANDARD</code> | 0 | 바깥 가장자리의 접기 막대 (기본) |
| <code>FIXED_AREA</code> | 1 | 펼쳐져 있고 탭이 하나 이하면 접기 막대를 숨김 (접혀 있거나 탭이 여럿이면 보임) |
| <code>MOVABLE</code> | 2 | 크기 0 인 움직이는 접기 단추 (Skeleton 이 씀). `pinnedFlag` 가 아니고 내부 조건을 만족할 때만 보임. 로컬 실행에서는 영역 밖(x = -23)에 숨은 채 나타나지 않았음 <Badge type="danger" text="검증 필요" /> |

### TitleBarButtonsEnum <Badge type="warning" text="2026" />

2026 의 `titleBarButtons` 값입니다 (비트 합). 전역이 아니라 `DS/Windows/Window` 클래스의 `Window.TitleBarButtonsEnum` 에 있습니다.

| 이름 | 값 |
| --- | --- |
| <code>None</code> | 0 |
| <code>Close</code> | 1 |
| <code>Maximize</code> | 2 |
| <code>Settings</code> | 8 |
| <code>Information</code> | 16 |
| <code>Help</code> | 32 |
