# 메뉴 (DS/Menu) <Badge type="tip" text="로컬 실행 확인" />

`DS/Menu/Menu` 는 3DEXPERIENCE 화면 공통의 팝업 메뉴입니다. 인스턴스를 만들지 않고 `Menu.show(항목 배열, 설정, 콜백)` 으로 엽니다. 오른쪽 클릭 메뉴는 `DS/Menu/ContextualMenu` 로 요소에 연결합니다. DataGridView 우클릭 메뉴, TabBar, Breadcrumb, Flyout, SelectionChips 의 메뉴도 이 모듈이 그립니다.

샘플: [08_menu.html](https://github.com/LeeOuHyoun/3d-docs/blob/main/ControlsSample/08_menu.html) (버튼 아래 메뉴, 항목 종류, 체크 / 라디오, 하위 메뉴, 열어 둔 채 체크 바꾸기, 오른쪽 클릭 메뉴)

::: info 근거
Menu/Menu.js (2025, 2026) 의 `DS/Menu/Menu` (`show`, `hide`, `buildView` 의 항목 검사, 위치 계산 `show`, `updateSize`, `onKeyDown`, 항목 실행 함수, `updateModel`, `MENU_STATE_*`)와 `DS/Menu/ContextualMenu` (`addEventListener`, `removeEventListener`, `setGlobalConfig`, `buildAndDisplayContextualMenu`, 메뉴를 모으는 contextmenu 처리 함수). DataGridView.js 의 `_manageContextualMenu`, Utilities.js 의 `Utils.isAsyncFunction`, `Utils.attachSimpleCallback`. 호출부: ENXMeetingMgmt, ENOUserGroupMgmt (`WUXMenu.show(menu, { position })`), RevisionFamily (`submenu`), ENXSBToolbarClient (`submenu: 'outside'`), FolderEditor, ENOChgGovernanceUX (`buildAndDisplayContextualMenu`, `action.module`). 집계: `DS/Menu/Menu` 52 개 앱, `DS/Menu/ContextualMenu` 28 개 앱. `xxxMenu.show(` 79 회 중 `WUXMenu.show(` 가 30 회이고, 나머지에는 UIKIT DropdownMenu 인스턴스의 `show()` 가 섞여 있습니다. 항목 `type` 이 나오는 앱 폴더 수: `PushItem` 125, `SeparatorItem` 47, `CheckItem` 35, `RadioItem` 21, `TitleItem` 9, `CollectionItem` 0. 로컬 실행: 2025, 2026 webapps 를 `npm run samples` 로 띄워 Edge 로 확인
:::

## 메뉴 열기 (Menu.show)

<<< @/../ControlsSample/08_menu.html#show{js}

| 인자 | 내용 |
| --- | --- |
| 첫째 `items` | 항목 배열 ([항목](#항목)). 비어 있으면 `Invalid content, menu must contain at least one item.` 오류 |
| 둘째 `config` | 아래 설정. 생략하면 화면 왼쪽 위(0, 0)에 열림 |
| 셋째 `callbacks` | `{ onShow(), onHide(byAction) }` |

| 설정 키 | 기본값 | 설명 |
| --- | --- | --- |
| `position` | `{ x: 0, y: 0 }` | 메뉴 왼쪽 위 좌표(px). `document.body` 기준이라 스크롤된 페이지에서는 `getBoundingClientRect()` 값에 스크롤 양을 더함 |
| `layout` | - | `position` 에 메뉴의 어느 모서리를 맞출지. `'topleft'` / `'topright'` / `'bottomleft'` / `'bottomright'`. <code>'bottomleft&#124;topleft'</code> 처럼 이어 쓰면 화면 안에 들어가는 첫 값을 씀 |
| `submenu` | `'auto'` | 하위 메뉴를 여는 방식 ([하위 메뉴](#하위-메뉴)) |
| `doNotHideOnAction` | `false` | `true` 면 항목을 눌러도 닫히지 않음 |
| `display` | - | `{ paddingLeft, paddingTop, paddingRight, paddingBottom }`. 화면 가장자리에서 이만큼 안쪽에만 놓음 |
| `targetDocument` | `document` | 메뉴를 붙일 문서 (다른 창, iframe) |
| `forceTouchMode` | `false` | 터치용 큰 모양 |

- 기준 요소를 받는 옵션은 없습니다. 버튼 아래에 열려면 버튼의 `getBoundingClientRect()` 로 `left`, `bottom` 을 구합니다. OOTB 도 `{ position: { x: pos.left, y: pos.top + 20 } }` 처럼 계산합니다.
- 메뉴는 한 번에 하나입니다. 열린 채로 `Menu.show` 를 또 부르면 앞 메뉴를 닫고(앞 콜백의 `onHide`) 새로 엽니다.
- 메뉴는 `document.body` 끝에 붙는 `div.wux-menu` (`position: absolute`, `z-index: 100000000`)입니다. 화면 밖으로 나가면 안으로 옮기고, 높이가 모자라면 위아래 스크롤 화살표가 생깁니다.
- 폭은 내용에 맞추되 2025 는 160 ~ 320px, 2026 은 90 ~ 320px 입니다.
- `Menu.show` 는 열린 메뉴의 `getBoundingClientRect()` 값을 돌려줍니다. 코드로 닫을 때는 `Menu.hide()` 를 부릅니다.
- 2025 의 메뉴 모양(CSS)은 모듈이 직접 불러옵니다. 2026 은 `Core/wux.css` 에 들어 있어서, WebUX 컨트롤을 하나라도 쓰는 화면(`DS/Core/Core` 가 `wux.css` 를 불러옴)이면 따로 할 일이 없습니다.

로컬 실행으로 확인한 것: 버튼(왼쪽 12px, 아래 93px) 아래에 메뉴가 `left 12, top 93` 으로 열림. `layout: 'topright'` 와 버튼 오른쪽 좌표를 주면 메뉴 오른쪽 끝이 버튼 오른쪽 끝(227px)과 맞음. 페이지를 100px 스크롤한 뒤 `position.y = 400` 으로 열면 `style.top` 은 400px, 화면상 위치는 300px.

### 닫힐 때 (onHide)

| 닫히는 때 | `onHide` 인자 |
| --- | --- |
| 항목을 누르거나 Enter 로 실행 | `true` (항목의 `action` 콜백보다 **먼저** 불림) |
| 메뉴 밖을 누름 (오른쪽 클릭 포함) | `undefined` |
| Esc | `undefined` |
| 창 크기가 바뀜, 창이 포커스를 잃음 | `undefined` |
| 다른 메뉴가 열림 (`Menu.show`) | `undefined` |
| `Menu.hide(값)` | 넘긴 값 |

- 페이지 스크롤로는 닫히지 않습니다. 메뉴가 페이지와 같이 움직입니다.
- 메뉴가 열려 있는 동안은 문서의 `contextmenu` 기본 동작(브라우저 메뉴)을 막습니다. 메뉴 위를 오른쪽 클릭해도 아무 일이 없습니다.

로컬 실행으로 확인한 것: "복사" 를 누르면 `onShow` → `onHide (true)` → `실행: copy` 순서. 밖을 누르거나 Esc 를 누르면 `onHide (false)`. 창 `resize` 이벤트로 닫힘. 2025, 2026 같음.

## 항목

<<< @/../ControlsSample/08_menu.html#items{js}

| 키 | 값 | 설명 |
| --- | --- | --- |
| `type` | `'PushItem'` / `'CheckItem'` / `'RadioItem'` / `'SeparatorItem'` / `'TitleItem'` / `'CollectionItem'` | 꼭 넣음. 없으면 `Invalid item, 'type' property is mandatory.` 오류 |
| `title` | 문자열 | 글자 |
| `icon` | 문자열, `{ iconName, fontIconFamily }`, `{ iconPath }`, `{ iconClasses }` | 아이콘. 규칙은 [WebUX 아이콘](/controls/webux-controls#아이콘) 과 같음 |
| `fonticon` | `{ content, family }` | `content` 는 클래스 이름(`'wux-ui-3ds wux-ui-3ds-trash'`, 공백으로 구분) 또는 `'&#...;'` 문자. `icon` 과 같이 넣으면 오류 |
| `state` | `'disabled'`, `'selected'`, <code>'selected&#124;disabled'</code> | 대소문자 무시, <code>&#124;</code> 로 이음. `'enabled'`, `'unselected'` 는 아무 효과 없음 (안 넣은 것과 같음) |
| `accelerator` | 문자열 | 오른쪽에 회색으로 보이는 글자. 단축키를 등록하지는 않음 |
| `tooltip` | 문자열 또는 툴팁 옵션 객체 | 툴팁. 없으면 글자가 잘릴 때만 `title` 을 툴팁으로 보여 줌 |
| `action` | `{ callback, context, this }` | 누르면 `callback.call(this, { context })` |
| `action` | `{ module, func, argument, context }` | 모듈을 불러와 `func` 실행 (OOTB 는 ENOChgGovernanceUX 한 곳) |
| `submenu` | 항목 배열, 함수, `async` 함수 | 하위 메뉴 ([하위 메뉴](#하위-메뉴)) |
| `badge` | `inject(el)` 가 있는 컨트롤 | `title` 자리에 그 컨트롤을 넣음 (PushItem) |
| `items` | 배열 | CollectionItem 의 아이콘 버튼들 (`icon` 문자열, `fonticon`, `state`, `tooltip`, `action.callback`) |

| `type` | 모양 | 누를 수 있나 |
| --- | --- | --- |
| `PushItem` | 일반 항목 | 예 |
| `CheckItem`, `RadioItem` | `state` 에 `selected` 가 있으면 왼쪽에 파란 막대 | 예. 눌러도 `state` 는 바뀌지 않음 |
| `SeparatorItem` | 가로 구분선 | 아니오 |
| `TitleItem` | 굵은 글자 제목 | 아니오. 키보드 이동도 건너뜀 |
| `CollectionItem` | 아이콘 버튼 한 줄 (잘라내기, 복사 같은 묶음) | 버튼마다 |

- `state: 'disabled'` 항목은 흐리게 보이고, 눌러도 메뉴가 그대로 있으며, 키보드 이동에서 빠집니다. 툴팁은 보입니다.
- Menu 가 읽지 않는 키(OOTB 가 넣는 `name`, `data` 등)는 그대로 무시됩니다.

로컬 실행으로 확인한 것: 위 항목으로 제목, 아이콘 두 개, 흐린 "잠금", 구분선, "삭제" 가 그려지고, "잠금" 위에 마우스를 올리면 "권한이 없습니다" 툴팁이 뜸. `icon` 과 `fonticon` 을 같이 주면 `Invalid item, 'icon' and 'fonticon' properties cannot be set together.` 오류. CollectionItem 은 아이콘 버튼 한 줄로 그려지고 버튼을 누르면 그 `action` 이 실행되고 닫힘.

## 체크와 라디오 항목

<<< @/../ControlsSample/08_menu.html#check{js}

::: warning 체크 항목을 눌러도 표시가 바뀌지 않습니다
Menu 는 `CheckItem` / `RadioItem` 의 `state` 를 바꾸지 않고, 라디오 묶음도 없습니다. 눌렀을 때 `action` 콜백만 부르고 닫힙니다. 값은 앱이 갖고 있다가, 메뉴를 열 때마다 그 값으로 `state` 를 정해 항목을 새로 만듭니다 (OOTB 도 열 때마다 배열을 새로 만듦). 열어 둔 채 표시를 바꾸려면 아래 `Menu.updateModel` 을 씁니다.
:::

### 열어 둔 채 체크 바꾸기

<<< @/../ControlsSample/08_menu.html#keepOpen{js}

| 함수 | 하는 일 |
| --- | --- |
| `Menu.updateModel([{ title, state }])` | 열린 메뉴에서 `title` 이 같은 `CheckItem` 의 `state` 와 표시를 바꿈. RadioItem, PushItem 은 바꾸지 않음 |
| `Menu.getItemState(item)` | `state` 글자를 비트 값으로 (`Menu.MENU_STATE_SELECTED` = 1, `MENU_STATE_DISABLED` = 2, 없음 0) |
| `Menu.setItemState(item, bits)` | 비트 값을 `state` 글자로 바꿔 항목에 넣음 |
| `Menu.translateStateStringToBitField(s)` / `translateStateBitFieldToString(bits)` | 위 두 변환만 |

로컬 실행으로 확인한 것: "썸네일 보기" 를 누르면 닫히고 로그에 `썸네일: false`, 다시 열면 막대가 없음. "수정일순" 을 누른 뒤 다시 열면 막대가 "수정일순" 으로 옮겨 감. `doNotHideOnAction` 메뉴에서 "담당자" 를 두 번 누르면 메뉴가 열린 채 막대가 생겼다 없어짐.

## 하위 메뉴

<<< @/../ControlsSample/08_menu.html#submenu{js}

| `submenu` 값 | 동작 |
| --- | --- |
| 항목 배열 | 마우스를 올리면 오른쪽(자리가 없으면 왼쪽)에 열림. 빈 배열이면 오류 |
| 함수 | 마우스를 올릴 때마다 불러 돌려준 배열로 엶 |
| `async` 함수 | 위와 같고, 결과가 올 때 엶. 기다리는 동안 표시는 없음 |
| `action` 과 같이 넣음 | 글자 부분은 `action` 실행, 오른쪽 화살표 부분에 마우스를 올리면 하위 메뉴 |

`Menu.show` 설정의 `submenu` 로 여는 방식을 정합니다.

| 설정 `submenu` | 동작 |
| --- | --- |
| `'auto'` (기본) | 옆에 엶. 메뉴 폭 + 320px 이 화면 폭보다 크면 `'replace'` 로 바뀜 |
| `'outside'` | 늘 옆에 엶 |
| `'replace'` | 눌러야 열리고, 부모 메뉴 자리를 하위 메뉴가 대신함. 위에 `<` 버튼과 부모 제목 줄이 생김. Esc 나 `<` 로 돌아감 |

::: warning 하위 메뉴를 비동기로 만들 때는 async function 을 씁니다
Menu 는 함수가 `async function` 인지(`Symbol.toStringTag === 'AsyncFunction'`)로 기다릴지를 정합니다. `Promise` 를 돌려주는 보통 함수를 주면 Promise 를 항목 배열로 여겨 `Invalid content, menu must contain at least one item.` 오류가 나고 하위 메뉴가 열리지 않습니다. 2025, 2026 같습니다.
:::

::: warning 2025: 함수 하위 메뉴 항목을 바로 누르면 오류가 납니다
2025 는 `submenu` 가 함수인 항목을 하위 메뉴가 만들어지기 전에 누르면(마우스를 올리고 50ms 안, 또는 `async` 결과가 오기 전) 콘솔에 `ReferenceError: item is not defined` 가 나고 그 항목의 `action` 은 실행되지 않습니다. 하위 메뉴는 결과가 오면 열립니다. 2026 에서 고쳐졌습니다. 함수 하위 메뉴 항목에는 `action` 을 같이 두지 않습니다.
:::

로컬 실행으로 확인한 것: "내보내기" 에 마우스를 올리면 오른쪽에 CSV, Excel 메뉴. "최근 문서" 는 300ms 뒤에 열림. "공유" 는 글자 위에서는 하위 메뉴가 안 열리고 화살표 위에서 열리며, 글자를 누르면 `실행: share`. `submenu: 'replace'` 는 마우스를 올려도 안 열리고 누르면 부모 자리에 "내보내기" 제목 줄과 함께 열림. 화면 폭을 420px 로 줄이면 설정 없이도 replace 방식으로 열림.

## 키보드

메뉴가 열려 있는 동안 문서의 `keydown` 을 받습니다. 첫 글자로 찾아가기, Home / End 는 없습니다.

| 키 | 2025 | 2026 |
| --- | --- | --- |
| 열렸을 때 | 아무 항목도 고르지 않음 | 첫 항목을 고르고 포커스를 줌 |
| ↓ / ↑ | 다음 / 이전 항목. 끝에서 처음으로 돌아감. 흐린 항목, 제목, 구분선은 건너뜀 | 같음 |
| → | 하위 메뉴를 엶 (고른 항목은 부모에 남고, 다음 ↓ 부터 하위 메뉴 안을 움직임) | 하위 메뉴를 열고 그 첫 항목으로 감 |
| ← | 하위 메뉴를 닫음 | 같음 |
| Enter | 고른 항목 실행 (하위 메뉴가 있으면 엶) | 같음 |
| Space | 없음 | Enter 와 같음 |
| Esc | 하위 메뉴면 그것만, 아니면 전체를 닫음 | 같음 |

- `submenu` 가 함수인 항목은 마우스를 올리기 전에는 → 로 열리지 않습니다 (2026 은 → 를 누르면 다음 항목으로 갑니다).

::: warning 2025: 버튼으로 연 메뉴를 Enter 로 고르면 메뉴가 다시 열립니다
2025 는 메뉴가 열려도 포커스가 메뉴로 가지 않습니다. WebUX Button 을 눌러 메뉴를 열고(버튼에 포커스가 남음) Enter 로 항목을 고르면 항목은 실행되지만, 같은 Enter 가 버튼에도 가서 메뉴가 다시 열립니다. 2026 은 포커스가 메뉴 항목으로 옮겨 가서 생기지 않습니다. 2025 에서 키보드 사용을 고려하면 메뉴를 열기 전에 버튼의 포커스를 빼 둡니다 (`document.activeElement.blur()`) <Badge type="danger" text="검증 필요" />.
:::

로컬 실행으로 확인한 것: 2025 는 열었을 때 고른 항목이 없고 ↓ 네 번에 열기 → 복사 → 삭제 → 열기 ("잠금", 제목은 건너뜀). Enter 로 "삭제" 가 실행된 뒤 메뉴가 다시 열림. 2026 은 열자마자 "열기" 가 골라져 있고 Enter 로 실행된 뒤 다시 열리지 않음. Space 도 실행됨. 하위 메뉴에서 2026 은 → 한 번에 "CSV" 로 감.

## 오른쪽 클릭 메뉴 (ContextualMenu)

<<< @/../ControlsSample/08_menu.html#contextual{js}

`ContextualMenu.addEventListener(요소, params)` 로 등록합니다. 요소 대신 `window` 도 됩니다.

| `params` 키 | 설명 |
| --- | --- |
| `callback(e)` | 꼭 넣음. 항목 배열 또는 그 배열을 주는 Promise 를 돌려줌. 빈 배열이나 `undefined` 면 그 등록은 빠짐 |
| `context` | `e.context` 로 그대로 넘어옴 |
| `this` | `callback` 안의 `this` |
| `onHideCB(byAction)` | 메뉴가 닫힐 때. 아래 주의 참고 |
| `noDOMEvents` | `true` 면 `contextmenu` / 길게 누르기 리스너를 붙이지 않음 (`buildAndDisplayContextualMenu` 로만 열 때) |

| `callback` 이 받는 `e` | 내용 |
| --- | --- |
| `e.context` | 등록할 때 준 `context` |
| `e.info.target` | 실제로 눌린 요소 (등록한 요소의 자식일 수 있음) |
| `e.info.position` | 등록한 요소 기준 좌표 `{ x, y }` |
| `e.data.event` | 원래 `contextmenu` 이벤트 |
| `e.data.listener` | 등록한 요소 |
| `e.data.windowPosition` / `e.data.targetPosition` | 창 기준 / 눌린 요소 기준 좌표 |

| 함수 | 하는 일 |
| --- | --- |
| `ContextualMenu.addEventListener(el, params)` | 등록. 번호를 돌려줌 |
| `ContextualMenu.removeEventListener(el, 번호)` | 해제. 없는 번호면 오류 (`Invalid uid.`) |
| `ContextualMenu.buildAndDisplayContextualMenu(el, { clientX, clientY })` | 오른쪽 클릭 없이 `el` 에서 오른쪽 클릭한 것처럼 메뉴를 엶. 위치는 `clientX`, `clientY` |
| `ContextualMenu.setGlobalConfig({ display, submenu })` | 이후 모든 오른쪽 클릭 메뉴에 쓸 `display`, `submenu` (`'auto'` / `'outside'` / `'replace'`) |

- 오른쪽 클릭한 요소부터 `window` 까지 올라가며 등록된 `callback` 을 모두 부르고, 결과를 안쪽 것부터 구분선으로 이어 붙여 한 메뉴로 엽니다. 메뉴는 마우스 위치(`pageX`, `pageY`)에 열립니다.
- 터치 기기에서는 길게 누르기로 열립니다.
- 메뉴를 만들 항목이 있거나 `input`, `textarea`, 편집 가능한 요소가 아니면 브라우저 기본 메뉴를 막습니다.
- 메뉴가 열려 있는 동안 오른쪽 클릭한 요소에 `is-displaying-menu` 속성이 붙습니다. CSS `[is-displaying-menu]` 로 강조할 수 있습니다.
- `onHideCB` 는 등록한 요소 **자체**를 오른쪽 클릭했을 때만 불립니다. 자식 요소를 오른쪽 클릭하면 메뉴는 열리지만 `onHideCB` 는 불리지 않습니다.

::: warning 부모와 자식 요소에 따로 등록하면 callback 이 두 번 불립니다
등록한 요소마다 `contextmenu` 리스너가 붙고, 리스너마다 맨 아래 요소부터 모든 `callback` 을 다시 모읍니다. 자식과 부모에 각각 등록하면 오른쪽 클릭 한 번에 두 `callback` 이 모두 두 번 불리고, 첫 메뉴가 닫히며(`onHideCB(false)`) 같은 메뉴가 다시 열립니다. 서버를 부르는 `callback` 이면 요청이 두 번 갑니다. 한 화면에서는 바깥 요소 한 곳에만 등록하고 `e.info.target` 으로 항목을 나눕니다 (샘플 방식). 그리드처럼 스스로 등록하는 컨트롤을 감싼 요소에 또 등록할 때도 같습니다.
:::

로컬 실행으로 확인한 것: "PRT-0002" 상자를 오른쪽 클릭하면 "PRT-0002 열기", 구분선, "새로 고침" 메뉴가 마우스 위치에 열리고 `callback` 은 한 번. 바깥 영역에서는 "새로 고침" 만. 메뉴가 열린 동안 영역에 `is-displaying-menu` 속성이 붙음. `onHideCB` 는 바깥 영역 자체를 눌렀을 때만 불림. "코드로 열기" 는 상자 왼쪽 아래(21, 231)에 같은 메뉴를 엶. 상자와 바깥 영역에 각각 등록해 보면 메뉴는 "안쪽 항목, 구분선, 바깥 항목" 으로 합쳐지지만 두 `callback` 이 두 번씩 불림 (2025, 2026 같음).

## DataGridView 우클릭 메뉴와의 관계

DataGridView 는 자기 영역에 `ContextualMenu.addEventListener` 를 등록하고, 그 `callback` 안에서 그리드 옵션 `onContextualEvent` 를 부릅니다. 그래서 `onContextualEvent` 가 돌려주는 배열이 그대로 `Menu.show` 로 갑니다.

- 항목 형식은 이 페이지의 [항목](#항목) 과 같습니다. 그리드 쪽 설명은 [우클릭 메뉴와 선택](/datagridview/context-menu#메뉴-항목) 에 있습니다.
- `onContextualEvent` 도 Promise 를 돌려줄 수 있습니다 (ContextualMenu 가 기다림).
- 그리드를 감싼 요소에 또 `ContextualMenu.addEventListener` 를 하면 위 주의처럼 두 번 불립니다.
- TabBar, Breadcrumb, Flyout (`extendedView.menuModel`), SelectionChips, PathExplorer 도 안에서 이 모듈을 씁니다. 2026 DropdownButton 의 `secondaryActions` 는 `{ label, onClick, disabled }` 형식을 받아 Menu 항목으로 바꿉니다 ([레이아웃 컨트롤](/controls/layout#dropdownbutton)).

로컬 실행으로 확인한 것: `onContextualEvent` 가 200ms 뒤에 항목을 주는 Promise 를 돌려줘도 행을 오른쪽 클릭하면 메뉴가 열림 (2025).

## UIKIT DropdownMenu 와 고르기

| | `DS/Menu/Menu` | `DS/UIKIT/DropdownMenu` |
| --- | --- | --- |
| 만들기 | 인스턴스 없음. 열 때마다 `Menu.show(배열, 설정)` | `new DropdownMenu({ target, items })` 를 만들어 둠 |
| 여닫기 | 위치를 직접 계산해 엶 | `target` 을 누르면 열고 닫음 (`bound`) |
| 항목 | `type`, `title`, `action.callback` | `text`, `name`, `handler`, 메뉴의 `onClick(e, item)` |
| 체크 | `CheckItem` / `RadioItem` (값은 앱이 관리) | `selectable`, `selected`, `getSelectedItems()` |
| 하위 메뉴 | `submenu` (배열, `async` 함수), 좁으면 replace | `items` |
| 오른쪽 클릭 | `ContextualMenu` | `contextual: true` |
| 같이 쓰는 것 | DataGridView, TabBar, Breadcrumb, WebUX 컨트롤 | UIKIT Input/Button 의 `dropdown`, Iconbar |
| 앱 수 | 52 (+ ContextualMenu 28) | 66 |

- WebUX 컨트롤이나 DataGridView 로 만드는 화면은 `DS/Menu` 를 씁니다. 그리드 우클릭 메뉴와 모양, 항목 형식, 키보드가 같습니다.
- UIKIT 으로 만든 화면(UIKIT 버튼의 `dropdown`, Iconbar)은 [DropdownMenu](/controls/uikit#dropdownmenu) 를 씁니다.
- 버튼에 메뉴를 붙이고 여닫기를 맡기고 싶을 때 2026 은 [DropdownButton](/controls/layout#dropdownbutton) 이 있습니다.

## 2026 변경

| 항목 | 2025 | 2026 |
| --- | --- | --- |
| CSS | 모듈이 `Menu/assets/css/Menu.css` 를 불러옴 | `Core/wux.css` 에 포함. 모듈은 CSS 를 불러오지 않음 |
| 열었을 때 | 고른 항목 없음, 포커스는 그대로 | 첫 항목을 고르고 포커스를 줌 (항목에 `tabindex="0"`) |
| Space | 없음 | Enter 처럼 실행 |
| → | 하위 메뉴만 엶 | 하위 메뉴를 열고 첫 항목으로 이동 |
| `Menu.show(Promise)` | 오류 (`Invalid content`) | Promise 가 준 배열로 엶 (돌려주는 값은 `undefined`) |
| 함수 하위 메뉴 항목을 바로 누름 | `ReferenceError` | 고쳐짐 |
| 메뉴 폭 | 160 ~ 320px | 90 ~ 320px |
| `tooltip` | `DS/Core/TooltipModel` 로 만들어 붙임 | 객체를 그대로 붙임 |
| 웹 기록(WebRecord) `recId` | 지원 | 없앰 |
| `action.module` / `func` 의 `context` | 객체면 `argument` 에 합침 | `argument.context` 로 넘김 |
| 창 포커스 잃음 | 닫힘 | 포커스가 메뉴로 옮겨 가는 경우는 닫지 않음 |

`ContextualMenu` 는 두 버전의 사용법이 같습니다 (2026 은 웹 기록 대기 처리만 빠짐).
