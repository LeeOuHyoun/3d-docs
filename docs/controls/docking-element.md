# 분할 레이아웃 (DockingElement) <Badge type="tip" text="로컬 실행 확인" />

`DS/Windows/DockingElement` 는 영역을 **한쪽 영역(docking zone) + 나머지 영역(free zone)** 으로 나누는 컨트롤입니다. 경계를 끌어 크기를 바꾸고, 접기 막대로 접습니다. "왼쪽 목록 | 가운데 그리드 | 오른쪽 속성" 같은 화면을 만들 때 씁니다. 창(Window)이 아니라 레이아웃 컨트롤이라 ImmersiveFrame 이 필요 없습니다.

샘플: [06_docking_element.html](https://github.com/LeeOuHyoun/3d-docs/blob/main/ControlsSample/06_docking_element.html)

::: info 근거
Windows.js 의 DockingElement (publishedProperties, addContent, replaceContent, getResizeInfos, __shouldCollapserBeVisible, _saveCollapseDockingZoneFlagInPreferences, 경계 끌기 / 접기 막대 처리, `fire("dockStartResize" / "dockIsResizing" / "dockResize" / "collapse" / "expand" / "dimensionsChanged")`, COLLAPSER_STYLE). 호출부: Skeleton (`DS/Skeleton/SkeletonLayout`. 왼쪽 / 오른쪽 DockingElement 를 겹쳐 3단 화면), SNResultUX (오른쪽 정보 패널), SpecTree (위쪽), IPClassifyActive, CollectionView, Tree, VCXWebTimeline (7 파일). 로컬 실행: 2025, 2026 webapps 를 `npm run samples` 로 띄워 Edge 로 확인
:::

## 3단 레이아웃

DockingElement 하나는 둘로만 나눕니다. 셋으로 나누려면 하나의 나머지 영역에 다른 DockingElement 를 넣습니다. OOTB `DS/Skeleton/SkeletonLayout` 이 이 방식입니다. OOTB ENX 앱의 3단 화면은 `DS/ENOXTriptych` 를 씁니다. 고르는 기준은 [ENOVIA 공용 화면 틀](/controls/enox-components#고르는-기준) 을 봅니다.

<<< @/../ControlsSample/06_docking_element.html#layout{js}

```css
#layout { position: relative; height: 420px; }
.pane { height: 100%; overflow: auto; box-sizing: border-box; }
.center { position: relative; height: 100%; }
```

- `side` 는 한쪽 영역의 위치(`WUXDockAreaEnum.LeftDockArea` / `RightDockArea` / `TopDockArea` / `BottomDockArea`)입니다. 읽기 전용이라 **생성 옵션으로만** 줍니다. 기본값은 `TopDockArea` 입니다.
- `dockingZoneContent` 에 한쪽 영역 요소, `freeZoneContent` 에 나머지 영역 요소를 넣습니다. 겹칠 때는 안쪽 DockingElement 의 `getContent()` 를 바깥의 `freeZoneContent` 에 넣습니다.
- 바깥 DockingElement 만 `inject` 합니다. 담는 요소에는 높이와 `position` 을 줍니다.
- `dockingZoneSize` 가 한쪽 영역 크기(px)입니다. 사용자가 끌 때는 `minSize` ~ `maxSize` 안에서만 바뀝니다.
- 가운데에 DataGridView 를 넣으려면 가운데 요소에 `position: relative; height: 100%` 를 줍니다 ([기본 그리드](/datagridview/getting-started)).
- 오른쪽 영역에 OOTB 속성 화면을 넣으려면 그 영역 요소에 [속성 패널 (EditPropWidget)](/controls/properties-panel) 을 넣습니다 (영역 요소에 높이 100%).

로컬 실행으로 확인한 것: 1222 × 422 영역에서 왼쪽 179px, 가운데 800px, 오른쪽 219px. 왼쪽 목록을 누르면 가운데 그리드가 바뀌고, 그리드 행을 고르면 오른쪽에 속성이 보임.

## 이벤트

<<< @/../ControlsSample/06_docking_element.html#events{js}

| 이벤트 | 시점 |
| --- | --- |
| `dockStartResize` / `dockIsResizing` / `dockResize` | 사용자가 경계를 누름 / 끄는 중 / 놓음. 코드로 `dockingZoneSize` 를 바꾸면 나지 않음 |
| `collapse` / `expand` | 접힘 / 펼침. 코드로 `collapseDockingZoneFlag` 를 바꿔도 남 |
| `dimensionsChanged` | 접기 / 펼치기 애니메이션이 끝나 나머지 영역 크기가 정해진 뒤 |

크기를 직접 계산해 그리는 내용이 있으면 `dockResize` 와 `dimensionsChanged` 에서 다시 계산합니다.

## 코드로 접기, 크기 바꾸기, 숨기기

<<< @/../ControlsSample/06_docking_element.html#control{js}

| 속성 | 하는 일 |
| --- | --- |
| `collapseDockingZoneFlag` | `true` 면 접힘. 접힌 영역은 나머지 영역에 자리를 내줌 |
| `dockingZoneSize` | 한쪽 영역 크기. 코드로 줘도 `minSize` / `maxSize` 를 넘으면 그 값으로 맞춰짐. `'100%'` 도 받음 |
| `visibleDockingZoneFlag` | `false` 면 한쪽 영역과 접기 막대를 모두 숨김 (접기와 달리 막대도 없음) |
| `collapsibleFlag` / `visibleCollapserFlag` | 접을 수 있는지 / 접기 막대를 보일지 |
| `autoExpandFlag` | `true` (기본) 면 접힌 영역에 마우스를 올릴 때 잠시 펼침 |
| `overlayDockingZoneFlag` | `true` 면 한쪽 영역이 나머지 영역을 밀지 않고 위에 겹침 |
| `resizableFlag` | `false` 면 경계를 끌 수 없음 (SNResultUX 는 고정된 위젯에서만 `true`) |

로컬 실행으로 확인한 것: 왼쪽을 접으면 `collapse` 가 나고 가운데가 800 → 980px. 오른쪽 `dockingZoneSize` +40 이면 오른쪽 259px. `visibleDockingZoneFlag = false` 면 오른쪽 0px, 가운데 1040px.

## 접힘 상태 저장

`identifier` 를 주면 **사용자가 접기 막대를 눌러 바꾼** 접힘 상태가 localStorage 에 저장되고, 다음에 같은 `identifier` 로 만들 때 복원됩니다.

- 키는 `side` 에 따라 `wux-docking-element-left` / `-right` / `-top` / `-bottom` 이고, 값은 `{ "identifier": { "collapseDockingZoneFlag": { "collapseDockingZoneFlag": true } } }` 입니다.
- 코드로 `collapseDockingZoneFlag` 를 바꾸는 것은 저장하지 않습니다.
- 영역 크기(`dockingZoneSize`)는 저장하지 않습니다. ImmersiveFrame 가장자리 영역은 붙은 창의 `identifier` 로 크기를 저장합니다 ([Windows API](/controls/api/windows#dockingelementforimmersiveframe)).

로컬 실행으로 확인한 것: 왼쪽 접기 막대를 누르자 `{ "sample-left-docking": { "collapseDockingZoneFlag": { "collapseDockingZoneFlag": true } } }` 가 저장되고, 새로 고친 뒤에도 접힌 채 열림 (가운데 960px).

## 한쪽 영역을 탭으로

`addContent` 에 둘째 인자를 주면 한쪽 영역이 탭으로 바뀌고, 탭마다 크기를 따로 가집니다.

```js
docking.addContent({ label: '속성', content: propertyEl }, { dockingZoneSize: 240 });
docking.addContent({ label: '이력', content: historyEl }, { dockingZoneSize: 320 });
docking.activateTab(historyEl);
```

- 탭 막대는 접기 막대 자리에 놓입니다. 탭이 하나면 탭 막대를 숨깁니다 (`hideTabBarWhenOnlyOnePageFlag`).
- `removeContent(el)`, `replaceContent(old, new)`, `updateContentVisibility(el, flag)`, `isContentActive(el)` 로 탭을 다룹니다.
- 이 방식은 샘플로 실행하지 않았습니다 <Badge type="danger" text="검증 필요" />.

## 접기 막대 모양

`collapserStyle` 은 생성 옵션으로만 줍니다. 값은 `DockingElement.COLLAPSER_STYLE` 입니다.

| 값 | 모양 |
| --- | --- |
| `STANDARD` (0, 기본) | 바깥 가장자리의 20px 막대. 샘플이 쓰는 방식 |
| `FIXED_AREA` (1) | 펼쳐져 있고 탭이 하나 이하면 막대를 숨김 |
| `MOVABLE` (2) | 크기 0 의 움직이는 단추. Skeleton 이 `autoExpandFlag: false` 와 같이 씀 |

::: warning MOVABLE 접기 단추가 보이지 않았습니다
로컬 실행에서 `MOVABLE` 로 만든 접기 단추는 영역 밖(x = -23)에 숨은 채 마우스를 가까이 가져가도 나타나지 않았습니다. Skeleton 의 CSS 나 다른 조건이 필요한 것으로 보입니다 <Badge type="danger" text="검증 필요" />. 직접 만드는 화면은 `STANDARD` 를 씁니다.
:::

속성, 메서드 전체는 [Windows API › DockingElement](/controls/api/windows#dockingelement) 에 있습니다.
