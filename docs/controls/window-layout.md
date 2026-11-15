# 창 묶기, 붙이기, 위치 저장 <Badge type="tip" text="로컬 실행 확인" />

ImmersiveFrame 안의 Panel 을 탭으로 묶거나 가장자리에 붙이고, 사용자가 옮긴 위치를 다음에도 쓰게 하는 방법입니다. 떠 있는 도구 창(ImmersiveWindow), 활성 창(WindowManager), 2026 의 "다시 묻지 않음" 저장도 여기에 있습니다. 다이얼로그 자체의 사용법은 [다이얼로그](/controls/dialog), 속성 전체는 [Windows API](/controls/api/windows) 에 있습니다.

샘플: [05_window_group.html](https://github.com/LeeOuHyoun/3d-docs/blob/main/ControlsSample/05_window_group.html)

::: info 근거
Windows.js 의 Window (attachToWindow, _attachToWindow, _applyWindowGroup, _canBeSnapSource / Target, _saveDataInPreferences, _readWindowPreferences, deleteWindowPreferences, 드래그 종료 처리), Panel (_canBeSnapSource / Target, supportsGroupType, _createWindowGroup), WindowGroup (attachWindow, _detachWindow, _updateTitle, close), WindowGroupAsTab (currentWindows, getDisplayedWindows), ImmersiveFrame (dockWindow, undockWindow, `__acceptsSnapping__`, _computeUniqueIdentifier), ImmersiveWindow, WindowManager. Utilities.js 의 saveDataInLocalPreferences. 2026 Dialog 의 _applyCheckboxInfos, getCheckBoxInfoStateInPreferences, _applyCustomButtonsDefinition (secondaryActions). OOTB 앱은 WindowGroup 계열과 ImmersiveWindow 를 직접 부르지 않음 (0 파일). 로컬 실행: 2025, 2026 webapps 를 `npm run samples` 로 띄워 Edge 로 확인
:::

## 탭으로 묶기

Panel 제목줄을 다른 Panel 제목줄 위로 끌어 놓으면 두 창이 탭 하나로 묶입니다. 코드로는 `attachToWindow` 를 씁니다.

<<< @/../ControlsSample/05_window_group.html#group{js}

- `attachToWindow(대상, WUXSnapAreaEnum.InsideArea)` 는 대상과 **탭 묶음(WindowGroupAsTab)** 을 만듭니다. 결과는 성공 1, 실패 -1 입니다.
- 묶인 창의 `windowGroup` 이 묶음입니다. 셋째 창은 묶음(또는 묶인 창)에 같은 방법으로 붙입니다.
- 묶음의 제목은 `[부품, 문서, 변경]` 처럼 창 제목을 이어 붙인 것입니다.
- 보이는 탭은 `group.currentWindows = [창]` 으로 바꿉니다. 지금 보이는 창은 `group.getDisplayedWindows()` 입니다.
- `win.windowGroup = null` 로 풀면 그 창이 떨어져 나옵니다. 창이 하나만 남으면 묶음은 스스로 없어지고 남은 창도 풀립니다.

로컬 실행으로 확인한 것: 문서를 부품에 묶으면 결과 1, 종류 `GroupAsTab`, 제목 `[부품, 문서]`. 변경까지 묶으면 3개. `currentWindows = [부품]` 으로 부품 탭이 보이고 문서 탭이 숨음. 문서를 풀면 `[부품, 변경]` 이 남음.

::: warning 묶이는 것은 Panel 뿐이고, 격자 묶음은 꺼져 있습니다
- Window, Dialog 는 묶이지 않습니다 (`_canBeSnapSource` 가 항상 `false`). Panel 끼리, 같은 ImmersiveFrame 안에서, `snappableFlag: true` (Panel 기본값) 일 때만 묶입니다. ImmersiveWindow 는 `snappableFlag` 기본값이 `false` 입니다.
- 위아래 / 좌우로 붙이는 **격자 묶음(WindowGroupAsGrid)** 은 ImmersiveFrame 의 내부 값 `__acceptsSnapping__` 이 `true` 일 때만 됩니다. 원본(2025, 2026) 어디에서도 이 값을 켜지 않으므로 `TopArea` / `BottomArea` / `LeftArea` / `RightArea` 로 `attachToWindow` 하면 -1 이 돌아옵니다.

로컬 실행으로 확인한 결과: `BottomArea` -1, `RightArea` -1, `snappableFlag: false` 인 Panel -1, Dialog -1, `InsideArea` 1, `panel.supportsGroupType(GroupAsGrid)` 은 `false`.
:::

이미 있는 묶음에 넣을 때는 `win.windowGroup = 묶음` 으로도 됩니다. 묶음의 마지막 탭 뒤에 들어갑니다 (`InsideArea` 로 마지막 창에 붙임).

## 가장자리에 붙이기

<<< @/../ControlsSample/05_window_group.html#dock{js}

- `frame.dockWindow(창, WUXDockAreaEnum.RightDockArea)` 로 붙이고 `frame.undockWindow(창)` 로 뗍니다. `currentDockArea` 를 바꿔도 같습니다.
- 붙일 수 있는 곳은 창의 `allowedDockAreas` (비트 합)가 정합니다.
- 묶음 안의 창을 붙이면 묶음에서 빠져서 붙습니다 (로컬 실행에서 확인: 2개 묶음에서 하나를 붙이자 묶음이 없어짐).
- 그 가장자리에 붙은 창 목록은 `frame.getWindowsInDockingElement(area)`, 가장자리 영역은 `frame.getDockingElement(area)` (DockingElementForImmersiveFrame) 입니다.
- 붙이고 뗄 때 창에서 `prePlaceChange` → `placeChange` → `resize` / `move` 순으로 이벤트가 납니다.

## 위치 저장

창에 `identifier` 를 주면 사용자가 옮기거나 크기를 바꾼 값이 localStorage 에 저장되고, 같은 `identifier` 로 다시 만들 때 복원됩니다.

<<< @/../ControlsSample/05_window_group.html#saved{js}

- 저장 키는 `wux-windows-window` 하나이고 값은 `{ "identifier": { position, ... } }` 입니다. 위치, 크기, 붙은 곳, 붙은 크기, 최대화, 탭 묶음을 저장합니다.
- **사용자가 직접 바꾼 값만 저장합니다.** 제목줄 끌기를 놓을 때, 크기 조절을 놓을 때, 최대화, 끌어서 붙이기 / 묶기 때입니다. 코드로 `position`, `width` 를 바꾸는 것은 저장하지 않습니다.
- `identifier` 는 생성 옵션으로만 줍니다 (Abstract 의 읽기 전용 속성). `'1'`, `'2'` 처럼 0 이상의 정수 문자열은 저장에 쓰이지 않습니다. ImmersiveFrame 이 identifier 없는 창에 붙이는 값이기 때문입니다. `'myapp-parts-panel'` 처럼 앱 이름을 넣은 문자열을 씁니다.
- `win.deleteWindowPreferences()` 로 이 창의 저장 값을 지웁니다. 저장 값을 복원했는지는 `win.arePreferencesRestored()` 로 봅니다.
- localStorage 는 브라우저와 사용자 PC 마다 따로입니다. 다른 PC 에서는 기본 위치로 열립니다.

로컬 실행으로 확인한 것: 창 제목줄을 (200, 80) 만큼 끌어 놓자 `{ "sample-saved-panel": { "position": { "my": "top left", "at": "top left", "offsetX": 200, "offsetY": 80 } } }` 가 저장됨. 페이지를 새로 고치고 다시 열자 같은 자리(213, 354)에 열림. 코드로 만든 직후에는 저장 값이 없음.

## 다시 묻지 않음 저장 (2026)

<Badge type="warning" text="2026" /> 2026 Dialog 는 아래쪽 체크박스 상태를 저장할 수 있습니다. 확인 다이얼로그의 "다시 묻지 않음" 에 씁니다.

<<< @/../ControlsSample/05_window_group.html#dontask{js}

- `identifier` 와 `checkboxInfos.saveCheckStateInPreferences: true` 를 같이 주면, 사용자가 체크박스를 누를 때 `wux-windows-window` 의 `[identifier].checkBoxInfoState` 에 저장됩니다.
- 열기 전에 정적 함수 `Dialog.getCheckBoxInfoStateInPreferences(identifier)` 로 저장 값을 읽어 다이얼로그를 건너뜁니다. 값이 없으면 `undefined` 입니다.
- 다시 열면 저장된 값으로 체크된 채 열립니다.
- `customButtonsDefinition` 항목에 `secondaryActions: [{ label, onClick }]` 를 주면 그 버튼이 분할 버튼(`DS/Controls/DropdownButton`)이 됩니다. 이 버튼의 `onClick` 은 `e.dsModel.dialog` 가 아니라 다이얼로그 변수로 닫습니다.
- 2025 에는 `getCheckBoxInfoStateInPreferences` 가 없습니다. 샘플은 함수가 있는지 먼저 확인합니다.

로컬 실행(2026 webapps)으로 확인한 것: 체크 후 `삭제` 를 누르면 `{ "sample-delete-confirm": { "checkBoxInfoState": true } }` 가 저장되고, 다음에는 다이얼로그 없이 바로 진행됨. `삭제` 버튼이 `wux-controls-dropdownbutton` 으로 만들어짐.

## 떠 있는 도구 창 (ImmersiveWindow)

<<< @/../ControlsSample/05_window_group.html#immersive{js}

- Panel 을 상속하지만 **붙지 않습니다** (`allowedDockAreas = NoneDockArea`). 3D 화면 위 도구 상자 같은 용도입니다.
- `useBackgroundFlag: false` 로 배경을 없애고, `decorationFlag: false` 로 테두리를 없애고, `opacity` 로 투명도를 줍니다.
- `useGripFlag: true` 면 위쪽에 끌기 손잡이가 생기고, `backgroundDraggableFlag: true` 면 내용 빈 곳을 끌어도 움직입니다.
- `snappableFlag`, `collapsibleFlag` 기본값이 `false` 입니다 (Panel 은 `true`).

## 활성 창, 맨 앞으로 (WindowManager)

<<< @/../ControlsSample/05_window_group.html#active{js}

- 활성 창(`activeFlag: true`)은 페이지에 하나입니다. `WindowManager.getActiveWindow()` 로 얻습니다.
- `WindowManager.bringToFront(창)` 은 같은 층의 떠 있는 창보다 `z-index` 를 높입니다. 붙어 있거나 최대화된 창에는 아무 일도 하지 않습니다. `win.bringToFront()` 도 같습니다.
- 창을 누르면 그 창이 활성이 되고 `activate` 이벤트가 납니다.

## 창 이벤트

샘플은 창마다 아래 이벤트를 로그에 남깁니다.

| 이벤트 | 언제 나는지 (로컬 실행) |
| --- | --- |
| `move` | 처음 놓일 때, 붙이고 뗄 때 |
| `resize` | 묶일 때(대상 창), 붙이고 뗄 때, `width` / `height` 변경 |
| `startResize` / `stopResize` | 사용자가 크기 조절 손잡이를 누름 / 놓음 |
| `prePlaceChange` / `placeChange` | 묶이고 풀릴 때, 붙이고 뗄 때 |
| `activate` | 창을 누르거나 묶음에 들어가 앞에 보일 때 |
| `close` | X 버튼, `close()` |

전체 목록은 [Windows API › Window](/controls/api/windows#window) 에 있습니다.
