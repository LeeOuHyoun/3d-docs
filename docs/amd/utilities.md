# 공통 유틸 (DS/Utilities, Logger ...) <Badge type="info" text="원본 분석" />

ENOVIA 화면 컨트롤(WUX)이 같이 쓰는 도우미 모듈입니다. DOM 이벤트 등록, 아이콘, 크기 감지, 로그, 마우스와 터치 이벤트, 키 코드, UUID 를 위젯에서도 그대로 불러 씁니다. webapps 주소, 쿠키, `DS/WebappsUtils/Utils` 는 [WebappsUtils](/amd/webapps-utils), 객체 합치기와 타입 검사(`UWA/Core`, `UWA/Utils`)는 [Core 유틸](/uwa/core) 을 봅니다.

::: info 근거
Utilities/Utilities.js (`DS/Utilities/*` 28 개 모듈), Logger/Logger.js (`DS/Logger/Logger`, `DS/Logger/Appenders/Quake`, `GelfHttp`), Core/Core.js (`DS/Core/Core`, `DS/Core/PointerEvents`), ResizeSensor/js/ResizeSensor.js, AmdLoader.js (Logger 의 `logPrefix`), wux.css (`nodeIsRendered`, `iframe.wux-ui-is-resized`). 앱 수는 모듈 문자열이 나오는 webapps 폴더 수 (자기 폴더 제외). 호출 수는 `define` / `require` 의 의존성 위치와 함수 인자 이름을 맞춰 `인자.함수(` 를 센 값 (자기 폴더 제외, 안쪽 함수가 같은 이름을 다시 선언한 곳 제외). 호출부: Controls, DataGridView, CollectionView, Tweakers, CfgBulkTablePresenter, ConfiguratorPanel, ENORouteMgmt, ENOXModelListView, 3DPlay 계열. 2026 은 enovia2026_webapps 의 같은 파일과 비교함
:::

## 한눈에

| 모듈 | 앱 | 많이 쓰는 것 (호출) | 용도 |
| --- | --- | --- | --- |
| `DS/Utilities/Dom` | 105 | `addEventOnElement` 740, `createElement` 297, `generateIcon` 161, `removeEventOnElement` 91, `firstParentWithClass` 79 | DOM 이벤트 등록 / 해제, 아이콘, 요소 찾기, 크기 감지 |
| `DS/Core/PointerEvents` | 102 | `POINTERUP` 238, `POINTERDOWN` 202, `POINTERHIT` 140, `POINTERMOVE` 123, `isTouchEvent` 39 | 마우스와 터치를 같은 이벤트 이름으로 받기 |
| `DS/Logger/Logger` | 97 | `getLogger` 226 | 이름별 수준이 있는 로그 |
| `DS/Utilities/Utils` | 96 | `debounce` 146, `applyMixin` 106, `getUnsafeHTMLContentValueToApply` 33, `getDocumentOwner` 27 | 이것저것 도우미 |
| `DS/Utilities/TouchUtils` | 77 | `getTouchMode` 188 | 터치 모드인지 |
| `DS/Core/Core` | 68 | 의존성으로만 씀 (171 건) | 전역 `WUX`, 위 모듈 초기화, `wux.css` |
| `DS/Utilities/UUID` | 19 | `v4` 42 | UUID 문자열 |
| `DS/Utilities/Array` | 19 | `clear` 30, `optimizedForEach` 25, `pushUnique` 22 | 배열 도우미 |
| `DS/ResizeSensor/js/ResizeSensor` | 18 | `new ResizeSensor` 23 | 요소 크기 변화 |
| `DS/Utilities/KeyboardUtils` | 14 | `ESCAPE` 24, `ENTER` 18, `SPACE` 10 (상수) | 키 코드 상수 |

## 어떻게 부르나

```js
require([
  'DS/Core/Core',
  'DS/Utilities/Dom',
  'DS/Utilities/Utils',
  'DS/Logger/Logger'
], function (WUX, Dom, Utils, Logger) {
  var logger = Logger.getLogger('DS/MyWidget/PartList');
  var onResize = Utils.debounce(function () {
    logger.log('resized');
  }, 200);
});
```

- 모두 정적 함수 묶음입니다. `new` 하지 않습니다. `ResizeSensor` 만 `new` 로 만듭니다.
- `DS/Core/Core` 는 처음 읽힐 때 `PointerEvents.init()`, `TooltipManager.init()`, `TouchUtils.init()` 을 부르고 `wux.css`, `wux-3ds-fonticons.css` 를 읽습니다. 돌려주는 `WUX` 객체는 전역 `window.WUX` 로도 등록됩니다 ([아래](#ds-core-core-의-wux-함수)).
- WUX 컨트롤(`DS/Controls/...`)은 `DS/Core/Core` 를 의존성으로 가집니다. 컨트롤을 쓰는 위젯은 따로 넣지 않아도 됩니다.

::: warning 컨트롤 없이 PointerEvents 만 부르면 이벤트가 오지 않습니다
`DSpointer...` 이벤트는 `PointerEvents.init()` 이 document 에 리스너를 단 뒤에만 생깁니다. `init()` 은 `DS/Core/Core` 만 부릅니다. 같은 이유로 `Dom.generateIcon` 의 글꼴 아이콘(`wux-3ds-fonticons.css`), `Dom.onNodeRendered`, `Dom.addResizeListener` (`wux.css`) 도 Core 가 읽는 CSS 가 있어야 동작합니다. 컨트롤 없이 이 기능만 쓰면 `DS/Core/Core` 를 의존성에 넣습니다.
:::

## Dom

### 이벤트 등록과 해제

```js
Dom.addEventOnElement(this, button, 'click', function (e) {
  this.classList.add('my-clicked');
});
Dom.removeEventOnElement(this, button, 'click');
Dom.removeAllEventsOnElement(this);
```

| 함수 | 호출 | 설명 |
| --- | --- | --- |
| `addEventOnElement(owner, target, 이벤트, fn, options?)` | 740 | `target.addEventListener` 로 등록하고 `owner._WUXEvents` 에 기록. `fn` 의 `this` 는 `target` |
| `removeEventOnElement(owner, target, 이벤트, fn?, options?)` | 91 | 하나 해제. `fn` 을 빼면 그 이벤트의 첫 등록. 등록할 때 `options` 를 줬으면 같은 값을 줘야 찾음. 해제하면 `true` |
| `removeAllEventsOnElement(owner)` | 7 | `owner` 로 등록한 것을 모두 해제. 화면을 없앨 때 부름 |
| `hasEventOnElement(owner, target, 이벤트, fn?)` | 1 | 등록되어 있는지 |

- `owner` 는 등록 목록을 담을 아무 객체입니다. OOTB 는 컴포넌트 자신(`this`)을 넘기고 `destroy` 에서 `removeAllEventsOnElement(this)` 를 부릅니다 (Tree, Tweakers, Spreadsheet).
- `target` 은 DOM 요소, `window`, `document` 외에 `addEventListener` 를 가진 WUX 컨트롤도 됩니다. CfgBulkTablePresenter 는 칩 컨트롤의 `onPostRemove` 를 이렇게 받습니다.
- 진짜 사용자 이벤트(`isTrusted`)면 "사용자 조작 중" 상태로 `fn` 을 부릅니다. 그리드가 이 상태일 때만 편집 값을 받는 이야기는 [커스텀 Tweaker](/tweakers/custom-tweaker) 에 있습니다.
- Firefox 에서 `mousewheel` 은 `DOMMouseScroll` 로 바꿔 등록합니다.

| 함수 | 호출 | 설명 |
| --- | --- | --- |
| `pushUserInteractionContext()` / `popUserInteractionContext()` | 23 / 23 | "사용자 조작 중" 구간을 직접 만듦 |
| `isCurrentlyCalledFromUIInteraction()` | 8 | 지금 사용자 조작 안에서 불렸는지 |
| `callOutsideUserinteraction(fn)` | 2 | `fn` 을 조작 밖으로 보고 부름 (코드가 값을 바꿀 때) |

### 요소 만들기와 찾기

| 함수 | 호출 | 설명 |
| --- | --- | --- |
| `createElement(tag, 속성)` | 297 (14 앱) | `document.createElement` 에 속성을 넣은 DOM 요소. 옵션이 2026 에서 바뀜 (아래) |
| `firstParentWithClass(el, class)` | 79 | 자기 자신부터 위로 올라가며 그 class 를 가진 첫 요소. 없으면 `undefined` |
| `isElementInside(el, container, { lookInPopup }?)` | 26 | `el` 이 `container` 안인지. `lookInPopup` 이면 WUX 팝업은 팝업을 띄운 요소를 따라 올라감 |
| `hasClass(el, class)` | 15 | class 를 가졌는지 |
| `setContent(el, 내용, allowUnsafeHTML?)` | 15 | 문자열은 기본 `textContent` (HTML 해석 안 함), `true` 면 `innerHTML`. 요소면 비우고 넣음 |
| `setStyles(el, { width: 100 })` | 15 | 카멜 이름을 `-` 이름으로, 숫자에 `px` 를 붙여 `cssText` 에 덧붙임 |
| `getDocumentOwner(el)` | 14 | `el.ownerDocument`. 없으면 `document` |
| `computeStringWidth(글자, 크기, 글꼴, bold?, italic?)` | 34 | canvas 로 잰 글자 폭(px). 컬럼 폭 계산에 씀 |
| `createEllipsisText({ label, nbLinesVisible, maxWidth, lineHeight })` | 7 | 줄 수를 넘으면 말줄임표로 자르는 `div` (UWA 요소) |
| `empty(el)` | 3 | `innerHTML = ''` |
| `hyperlinkifyText(글자)` | 1 | URL, `www.`, `mailTo:` 를 `a` 태그로 바꾼 HTML 문자열. 나머지 `<`, `>` 는 escape |

- `setContent` 의 기본값은 `Utils.getUnsafeHTMLContentValueToApply()` 로 정합니다. 브라우저 localStorage `WebUX_XSS` 가 `'unsafe'` 일 때만 기본이 HTML 입니다. WUX 컨트롤(Controls, DataGridView, Tree, Tweakers ...)도 이 함수로 HTML 허용 기본값을 정합니다 (33 건).

::: warning Dom.createElement 옵션은 2025 와 2026 이 다릅니다
2025 는 `{ class: 'x', styles: { ... }, html: '...' }` 처럼 `style` / `styles` 객체, `html`, 그 밖의 속성 이름을 받습니다. 2026 은 `{ className, style, text }` 를 받고 `class` 와 그 밖의 속성에는 콘솔 경고를 냅니다. 2025 에서 `className`, `text` 를 주면 같은 이름의 속성(attribute)이 될 뿐 class 와 글자가 들어가지 않습니다. 위젯 코드는 `UWA.createElement` ([DOM](/uwa/element)) 를 씁니다.
:::

### 아이콘

```js
var fontIcon = Dom.generateIcon('check');
var image = Dom.generateIcon({ iconPath: iconUrl, iconSize: { width: '16px', height: '16px' } });
Dom.generateIcon('close', { target: existingSpan });
container.appendChild(fontIcon);
```

- 문자열에 `.` 이 있거나 `data:image`, `blob:` 이 들어 있으면 이미지(`img`), 아니면 글꼴 아이콘 이름입니다. 글꼴 아이콘은 `span.wux-ui-3ds.wux-ui-3ds-이름` 이 됩니다.
- 객체 형식(`iconPath`, `iconName` + `fontIconFamily`, `iconClasses`, `svgElement`, `userName`, `number`)은 [더 많은 편집기](/tweakers/more-editors) 의 아이콘 설명을 봅니다.
- 둘째 인자 `{ target: 기존요소 }` 면 새로 만들지 않고 그 요소의 class / 배경을 바꿉니다 (이전 아이콘은 `cleanIcon` 으로 지움). `{ roundFlag: true }` 는 둥근 아이콘입니다.
- 돌려주는 값은 UWA 요소입니다 (`img`, `span`, `div`). OOTB 호출 161 건 / 43 앱.

### 그려짐과 크기

| 함수 | 호출 | 설명 |
| --- | --- | --- |
| `onNodeRendered(el, fn, { once }?)` | 27 | 요소가 화면에 그려지면 `fn()`. `wux-ui-is-rendered` class 의 CSS 애니메이션 시작으로 감지 |
| `addResizeListener(el, fn)` / `removeResizeListener(el, fn)` | 11 / 8 | 요소 안에 투명 `iframe` 을 넣고 그 창의 `resize` 로 감지. 한 프레임에 한 번 `fn(el)` |
| `addEndResizeListener(el, fn, ms?)` | 4 | 위 리스너를 debounce (기본 500 ms). 크기 변경이 멈춘 뒤 한 번 |
| `addResizeObserver(ctx, el, fn)` / `removeResizeObserver(ctx, el, fn)` | 10 / 8 | 브라우저 `ResizeObserver` (border-box). 20 ms 모아서 `fn.call(ctx, entries)` |
| `onNodeInserted(el, fn)` / `onNodeRemoved(el, fn)` | 0 / 1 | 그 요소 자신이 DOM 에 추가 / 제거되면 `fn(el)`. 토큰을 돌려주고 `removeOnNodeInserted(el, 토큰)` 으로 해제. 부모째 추가되면 오지 않음 |

- `addResizeListener` 가 넣는 `iframe` 은 `position: absolute` 입니다. 감지할 요소에 `position: relative` 를 줍니다.

## Logger

```js
define('DS/MyWidget/PartList', ['DS/Logger/Logger'], function (Logger) {
  var logger = Logger.getLogger('DS/MyWidget/PartList');
  logger.debug('rows', rows);
  logger.log('loaded');
  logger.warn('no SecurityContext');
  logger.error('request failed', error);
});
```

| 함수 | 설명 |
| --- | --- |
| `Logger.getLogger(이름 또는 객체)` | 로거. 객체 / 함수면 `loggerName` → `id` → `name` → `prototype.name` 순으로 이름을 정하고, 같은 객체에는 같은 로거를 돌려줌 |
| `logger.debug / log / info / warn / error(...인자)` | 로거 수준 이상이면 출력 |
| `logger.setLevel('WARN')` | 이 로거만 수준 변경. 대소문자 무관. 없는 이름이면 오류 |
| `Logger.setEnabled(false)` | 콘솔 출력만 끔 / 켬. 추가한 출력(appender)에는 계속 보냄 |
| `Logger.addAppender(이름, { append: function (entry) { ... } })` | 출력 위치 추가. `entry` 는 `{ timestamp, type, origin, name, messageArgs, stack }` |
| `Logger.levels` | `{ ALL: 10, DEBUG: 23, LOG: 31, INFO: 42, WARN: 51, ERROR: 64, OFF: 100 }` |

OOTB 는 클래스 안에서 `this._logger = Logger.getLogger(this.name)` (2DAnnotation) 이나 `Logger.getLogger(클래스)` (CATFLWidgetProperties) 로 만들어 둡니다. 3DPlay 는 `DS/WebUtils/Logger(이름, 플래그)` 로 감싸서 설정 `3DPlay.Debug` 가 있으면 `log`, 없으면 `warn` 수준으로 만듭니다.

### 로그 수준

- 기본 수준은 `LOG` 입니다. **`debug` 는 기본으로 보이지 않고** `log`, `info`, `warn`, `error` 는 보입니다.
- 수준은 로거를 만들 때 이름으로 찾습니다. `DS/MyWidget/PartList` → `DS/MyWidget` → `DS` → `ROOT` 순 (`/`, `.` 를 경계로 앞부분).
- 설정은 Logger 모듈이 처음 읽힐 때 한 번 읽습니다. sessionStorage 의 `DS/Logger/Logger.loggerConfig` (JSON) 가 있으면 그것, 없으면 로더 설정 `loggerConfig` 입니다. `ROOT` 가 없으면 `{ level: 'LOG' }` 를 넣습니다.

```js
sessionStorage.setItem('DS/Logger/Logger.loggerConfig', JSON.stringify({
  ROOT: { level: 'WARN' },
  'DS/MyWidget': { level: 'DEBUG' }
}));
```

위처럼 넣고 위젯을 다시 읽으면 `DS/MyWidget/...` 로거만 `debug` 까지 나옵니다. sessionStorage 는 위젯 프레임의 출처(origin) 것이어야 하므로 3DDashboard 에서는 개발자 도구 콘솔의 대상 프레임을 위젯으로 바꾸고 넣는 것으로 보입니다 <Badge type="danger" text="검증 필요" />.

| 켜고 끄기 | 방법 |
| --- | --- |
| 로거 하나 | `logger.setLevel('OFF')`, `logger.setLevel('DEBUG')` |
| 이름 단위 | 위 sessionStorage 값을 넣고 다시 읽기 |
| 콘솔 전체 | `Logger.setEnabled(false)` |
| 처음부터 끔 | Logger 를 읽기 전에 `require.config({ config: { 'DS/Logger/Logger': { disableLog: true } } })` |

### 출력 위치

| 출력 | 내용 |
| --- | --- |
| 콘솔 (기본) | `DS/WebappsUtils/Console` 을 거쳐 `console.log` / `warn` / `error` ... 앞에 `logPrefix: 로거이름: ` 이 붙음 |
| `DS/Logger/Appenders/Quake` | 화면 위쪽 절반을 덮는 검은 `pre` 상자. `toggle()` 로 보이기 / 숨기기. 로더 설정 `loggedLevels` (기본 `['warn', 'error', 'info']`) |
| `DS/Logger/Appenders/GelfHttp` | 로더 설정 `logUrl` 로 GELF 형식 JSON 을 POST |

- `logPrefix` 는 AmdLoader 가 정합니다. 위젯 프레임이면 `Widget instance #위젯id` 입니다. 전역 `dsLogPrefix` 가 있으면 그 값입니다.
- 마지막 인자가 Error 면 그 stack 을 같이 남깁니다. 로더 설정 `logStacks: true` 면 모든 로그에 stack 을 붙입니다.

```js
require(['DS/Logger/Logger', 'DS/Logger/Appenders/Quake'], function (Logger, Quake) {
  Logger.addAppender('quake', Quake);
  Quake.toggle();
});
```

화면에 콘솔이 없는 태블릿에서 확인할 때 씁니다. OOTB 에서 `addAppender` 를 부르는 곳은 UWPClientCode 하나입니다.

## PointerEvents

```js
require(['DS/Core/Core', 'DS/Core/PointerEvents'], function (WUX, PointerEvents) {
  container.addEventListener(PointerEvents.POINTERDOWN, onDown);
  container.addEventListener(PointerEvents.POINTERHIT, function (e) {
    if (e.multipleHitCount === 2) {
      openItem(e.target);
    }
  });
});
```

| 상수 | 이벤트 이름 | 마우스 | 터치 |
| --- | --- | --- | --- |
| `POINTERDOWN` | `DSpointerdown` | `mousedown` | `touchstart` |
| `POINTERMOVE` | `DSpointermove` | `mousemove` | `touchmove` |
| `POINTERUP` | `DSpointerup` | `mouseup` | `touchend` |
| `POINTERHIT` | `DSpointerhit` | `click` | `click` |
| `POINTERENTER`, `POINTERLEAVE`, `POINTEROUT` | `DSpointerenter`, `DSpointerleave`, `DSpointerout` | `mouseenter`, `mouseleave`, `mouseout` | 없음 |
| `HOLD` | `DShold` | 누르고 300 ms | 같음 |
| `LONGHOLD` | `DSlonghold` | 누르고 1,000 ms | 같음 |

- document 에 mouse / touch 리스너를 달고, 원래 이벤트의 target 에서 새 `MouseEvent` 를 보냅니다. `bubbles` 등은 원래 값이라 컨테이너 하나에서 받을 수 있습니다. OOTB 는 기본 `addEventListener` (3DPlay2DAnnotation) 와 UWA `addEvent` (ApplicationFrame) 로 받습니다.
- 새 이벤트에는 `originalEvent` (원래 이벤트), `touchFlag` (터치인지) 가 붙습니다. 터치면 `changedTouches`, `touches` 가 복사되고 좌표(`clientX` ...)는 첫 터치 값입니다.
- `preventDefault()`, `stopPropagation()` 은 원래 이벤트에도 전달됩니다 (`DShold`, `DSlonghold` 는 제외).
- `DSpointerhit` 의 `multipleHitCount` 는 첫 클릭부터 500 ms 안에 같은 대상을 누른 횟수입니다. `2` 면 더블 클릭입니다 (ENOXModelListView, CollectionView, DataGridView 가 이렇게 씀).
- `DShold`, `DSlonghold` 는 5 px 넘게 움직이거나 손을 떼면 취소됩니다.
- 같은 위치에서 5 ms 안에 같은 이벤트가 또 오면 한 번만 보냅니다 (터치 뒤 브라우저가 흉내 내는 mouse 이벤트 중복 제거).
- 오른쪽 버튼은 바꾸지 않습니다. 우클릭은 `contextmenu` 로 받습니다.
- 펜은 따로 처리하지 않습니다. 브라우저가 펜 입력을 mouse 이벤트로도 보내므로 마우스와 같은 이벤트로 오는 것으로 보입니다 <Badge type="danger" text="검증 필요" />.

| 함수 | 호출 | 설명 |
| --- | --- | --- |
| `isTouchEvent(e)` | 39 | 터치 이벤트인지. Android, iPhone / iPad (터치가 되는 Mac 포함) 에서는 마우스로 눌러도 `true` |
| `isLastEventTouch(doc?)` | 4 | 마지막으로 보낸 이벤트가 터치였는지 |
| `enable(doc?)` / `disable(doc?)` | 0 | 그 document 의 변환을 켬 / 끔 |
| `registerDocument(doc)` / `unregisterDocument(doc)` | 0 | 다른 document (iframe 안) 도 변환 |

## 크기 변화 받기

| 방법 | 감지 대상 | 콜백 | 비고 |
| --- | --- | --- | --- |
| `widget.addEvent('onResize', fn)` | 위젯 프레임 | 인자 없음 | [widget 객체](/widget/widget-api) |
| `new ResizeObserver(fn)` (브라우저) | 요소 | `entries` | 23 앱이 직접 씀 |
| `Dom.addResizeObserver(ctx, el, fn)` | 요소 | `fn.call(ctx, entries)`, 20 ms 모음 | 지우기 문제 (아래) |
| `Dom.addResizeListener(el, fn)` | 요소 | `fn(el)`, 한 프레임에 한 번 | `iframe` 을 넣음. `position: relative` 필요 |
| `new ResizeSensor(el, fn)` | 요소, 배열, NodeList | 인자 없음, 한 프레임에 한 번 | 18 앱 (ENOX 계열, ENORouteMgmt, ConfiguratorPanel) |

### ResizeSensor

```js
require(['DS/ResizeSensor/js/ResizeSensor'], function (ResizeSensor) {
  var sensor = new ResizeSensor(container, function () {
    onContainerResize(container.offsetWidth, container.offsetHeight);
  });
  sensor.detach();
});
```

- css-element-queries 라이브러리의 ResizeSensor 입니다 (파일 머리말). 요소 안에 숨은 `div.resize-sensor` 를 넣고 그 안의 스크롤 위치 변화로 크기 변화를 알아냅니다.
- 요소의 `position` 이 `static` 이면 **`relative` 로 바꿉니다**.
- 콜백에 인자가 없습니다. 크기는 `offsetWidth`, `offsetHeight` 로 직접 읽습니다.
- 붙일 때는 부르지 않습니다. 붙인 뒤 크기가 바뀔 때만 부릅니다.
- `sensor.detach(fn?)` 또는 `ResizeSensor.detach(el, fn?)` 로 뗍니다. 마지막 콜백이 빠지면 넣은 `div` 도 지웁니다.
- 2026 파일은 2025 와 같습니다.

::: warning Dom.removeResizeObserver 는 첫 번째 리스너를 지웁니다
지울 항목을 찾은 뒤 그 항목 객체로 `splice` 를 불러서 실제로는 배열의 **첫 번째** 리스너가 지워집니다 (2026 도 같음). 한 요소에 `addResizeObserver` 를 여러 개 달고 하나만 지우면 다른 것이 지워집니다. 한 요소에 하나만 달거나 브라우저 `ResizeObserver` 를 직접 씁니다.
:::

## Utils

`DS/Utilities/Utils` 입니다. `DS/WebappsUtils/Utils` ([WebappsUtils](/amd/webapps-utils)) 와 다른 모듈입니다.

| 함수 | 호출 | 설명 |
| --- | --- | --- |
| `debounce(fn, ms, immediate?)` | 146 | 마지막 호출 뒤 `ms` 동안 조용하면 한 번. `ms` 자리에 함수를 주면 부를 때마다 그 값을 씀. `immediate` 면 처음에 바로 |
| `throttle(fn, ms)` | 9 | 첫 호출은 바로, 그 뒤 `ms` 안의 호출은 마지막 것만 `ms` 뒤에 |
| `applyMixin(Target, Mixin)` | 106 (9 앱) | `Mixin.prototype` 의 속성을 `Target.prototype` 에 복사. 둘 다 생성자여야 함 |
| `getUnsafeHTMLContentValueToApply(값)` | 33 | `값` 이 `undefined` 면 localStorage `WebUX_XSS` 가 `'unsafe'` 인지, 아니면 `값` |
| `getDocumentOwner(el)` / `getDocumentWindow(el)` | 27 / 16 | 요소(또는 WUX 컨트롤)의 document / window |
| `getScrollbarWidth(다시재기?)` | 17 | 스크롤바 폭(px). 한 번 재면 기억 |
| `saveDataInLocalPreferences(id, 키, 이름, 값)` | 16 | localStorage `키` 에 `{ id: { 이름: 값 } }` JSON 으로 저장. `id` 는 문자열 또는 `identifier` 를 가진 객체 |
| `getDataFromLocalPreferences(id, 키, 이름)` / `deleteDataFromLocalPreferences(id, 키, 이름?)` | 17 / 4 | 위 값 읽기 / 지우기. Skeleton, Windows 가 패널 폭과 접힘을 이렇게 기억 |
| `compare(a, b)` | 15 | 값이 같은지 비교. 같으면 `0`, 크기를 정할 수 있으면 `1` / `-1`, 객체끼리 다르거나 한쪽이 `undefined` 면 `2` / `-2`. `sort` 의 비교 함수로는 맞지 않음 |
| `clamp(최소, 최대, 값)` | 15 | 범위 안으로 자름. **인자 순서 주의** (아래) |
| `isMouseOverElement(el, { x, y })` | 8 | 좌표가 요소 사각형 안인지 |
| `isFunction`, `isObject`, `isPlainObject`, `isPromise`, `isHTMLElement`, `isNumber` | 3 ~ 12 | 타입 검사 |
| `getDecimalSeparator(locale?)` | 5 | 소수점 문자. [언어와 사용자 환경설정](/widget/i18n-preferences#날짜와-숫자-쓰기) |

::: warning Utils.clamp 는 (최소, 최대, 값) 순서입니다
`clamp(e, t, n)` 은 `Math.min(Math.max(e, n), t)` 입니다. OOTB 는 `Utils.clamp(minValue, maxValue, value)` 로 부릅니다 (ConfiguratorPanel, Controls). lodash 의 `_.clamp(값, 최소, 최대)` 순서로 넣으면 엉뚱한 값이 나옵니다.
:::

## TouchUtils

| 함수 | 호출 | 설명 |
| --- | --- | --- |
| `getTouchMode()` | 188 | 터치 모드면 `true`. iPad, iPhone, Android 에서 `DS/Core/Core` 가 켬. 데스크톱은 `undefined` |
| `setTouchMode(bool, doc?)` | 6 | `touchMode` 를 따로 정하지 않은 WUX 컨트롤을 모두 바꿈. 바뀐 컨트롤이 있으면 document 에 `touchChange` 이벤트 |
| `applyTouch(el, bool)` | 2 | 그 요소 안의 WUX 컨트롤의 `touchMode` 를 정함 |
| `subscribeToTouchChange(el, fn)` / `unsubscribeToTouchChange(id)` | 0 | 바뀔 때 `fn`. 새 값은 인자로 오지 않으므로 `fn` 안에서 `getTouchMode()` 로 읽음 |

OOTB 는 터치일 때 여백과 크기를 키웁니다 (CAT3DAnnotationBaseUI 의 `getTouchMode() ? '8px' : '5px'`, ApplicationFrame 의 class 추가).

## UUID, Array, KeyboardUtils

| 함수 | 호출 | 설명 |
| --- | --- | --- |
| `UUID.v4()` | 42 | `'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'` 형식 (Math.random) |
| `UUID.createUUID()` | 4 | 브라우저에 `crypto.randomUUID` 가 있으면 그것, 없으면 `v4()`. 2026 은 25 건 |
| `Array.clear(arr, null로?)` | 30 | 배열을 그 자리에서 비우고(`length = 0`) **새 빈 배열**을 돌려줌. 둘째 인자가 참이면 `null` |
| `Array.optimizedForEach(arr, fn)` | 25 | `fn(item, index)`. `{ breakForEach: true }` 를 돌려주면 멈춤 |
| `Array.pushUnique(arr, item)` | 22 | 없을 때만 넣고 `true` |
| `Array.getIndexOfElement(arr, 키, 값)` | 6 | `arr[i][키] === 값` 인 첫 위치. 없으면 `-1` |
| `Array.insertInArray(arr, item, 기준, 'before'?, 키?)` | 0 | 기준 항목 뒤(또는 앞)에 넣음 |
| `Array.arrayMove(arr, from, to)`, `array2dMove`, `array2dTranspose(arr2d)` | 0 / 9 / 4 | 항목 이동, 2 차원 배열 행렬 바꾸기 |

```js
input.addEventListener('keydown', function (e) {
  if (e.keyCode === KeyboardUtils.ENTER) {
    save();
  } else if (e.keyCode === KeyboardUtils.ESCAPE) {
    cancel();
  }
});
```

- `KeyboardUtils` 의 상수는 `keyCode` 숫자입니다. `ENTER` 13, `ESCAPE` 27, `SPACE` 32, `TAB` 9, `DELETE` 46, `BACKSPACE` 8, 화살표 `LEFTARROW` 37 ~ `ARROWDOWN` 40, `A` ~ `Z`, `KEY0` ~ `KEY9`, `NUMPAD0` ~ `NUMPAD9`, `F1` ~ `F12` 등이 있습니다. OOTB 는 `e.keyCode === KeyboardUtils.ENTER` 로 비교합니다.
- 판별 함수: `validate(code)` (Space, Enter), `increase` / `decrease` (위·오른쪽 / 아래·왼쪽 화살표), `next` / `previous`, `isArrowKey`, `isFKey`, `isAlphaNumeric`, `isNumber(code, 윗줄숫자도?)`, `isInteractiveKey`.
- `isNumericKey` 는 `KEY1` ~ `KEY9` 만 봐서 `0` 키는 `false` 입니다.

## 그 밖의 DS/Utilities 모듈

| 모듈 | 앱 | 함수 (호출) | 설명 |
| --- | --- | --- | --- |
| `DateUtils` | 4 | `convertToDate(값)` 11, `areDatesEqual(a, b, 'time'?)` 5, `compare(a, b, 'time'/'month'?)` 5, `isDateInBetween(d, from, to, 'day'/'month'/'time')` 3, `addToDate(d, n, 'year'/'month'/'day')` 2, `isLeapYear` | 날짜 비교, 더하기. 글자로 쓰기는 [언어와 사용자 환경설정](/widget/i18n-preferences#날짜와-숫자-쓰기). 2026 은 48 앱 |
| `StringUtils` | 5 | `toComparable(s, { toLowerCase })` 9, `compareLocal(a, b, { lang, sensitivity })` 4, `sortLocal(arr, opts)`, `isEqualLocal` | 악센트 제거, `Intl.Collator` 비교. 빈 값도 처리 |
| `Object` | 6 | `areEquals(a, b)` 3, `deepFreeze(obj)`, `isLiteralObject(v)` 1 | 깊은 비교, 얼리기 |
| `DeviceUtils` | 4 | `isMobileiOS()` 5, `isAndroid()` 3, `isIE()` | 기기 판별 |
| `Debug` | 4 | `log(분류, ...)` 10, `beginMeasure(이름)` / `endMeasure(이름)` 3 | `console.debug`, `console.time` 등으로 출력. `enableLog(분류)`, `enableMeasure()` 또는 localStorage `WEBUI_DEBUG_LOG` (`'["All"]'` 또는 분류 배열), `WEBUI_DEBUG_MEASURE` (`'true'`) 로 켬 |
| `Scroll` | 0 | `getPosition(el)` | `{ x: -scrollLeft, y: -scrollTop }` |
| `DocUtils` | 1 | `getWebUXPropertyDocumentation` | WUX 속성 설명 문서 찾기 (Tweakers 도움말) |
| `Simulate` | 0 | `simulateMouse`, `simulateKeyDown`, `simulateDragDrop`, `simulateChange` ... | 테스트용 가짜 이벤트 |
| `Css`, `Color`, `Data`, `TreeUtils`, `RAFDispatcher`, `WUXUWAUtils`, `Xml`, `MouseEvents`, `ReusableContentsManager` | 1 ~ 10 | | 컨트롤 내부용. ReusableContentsManager 는 [셀 직접 그리기](/datagridview/custom-cell) |

## 같은 일을 하는 함수

| 하고 싶은 일 | 후보 | 쓸 것 |
| --- | --- | --- |
| 요소 만들기 | `UWA.createElement` (약 6,300), `Dom.createElement` (297 / 14 앱), `document.createElement` | `UWA.createElement`. `Dom.createElement` 는 2026 에서 옵션이 바뀜 |
| 이벤트 등록 | `addEventListener`, UWA `addEvent` ([DOM](/uwa/element)), `Dom.addEventOnElement` | 화면을 없앨 때 한꺼번에 풀거나 WUX 편집과 엮을 때 `Dom.addEventOnElement`, 아니면 UWA / 기본 |
| debounce | `DS/Utilities/Utils.debounce` (146), `DS/WebappsUtils/Utils.debounce` | 둘 다 `(fn, ms, immediate)` 로 동작이 같음. Utilities 쪽은 `ms` 에 함수도 받음 |
| throttle | `DS/Utilities/Utils.throttle(fn, ms)`, `DS/WebappsUtils/Utils.throttle(fn, ms, leading, trailing, ctx)` | **동작이 다름**. Utilities 쪽은 첫 호출을 바로 하고 마지막 호출을 뒤에 한 번 더 함. WebappsUtils 쪽은 셋째 인자가 참일 때만 첫 호출을 바로, 넷째 인자가 참일 때만 뒤에 한 번 더 |
| UUID | `UUID.v4()`, `UUID.createUUID()`, `WebappsUtils/Utils.uuid()`, `UWA.Utils.getUUID()`, `crypto.randomUUID()` | 모두 v4 형식 문자열. 의존성을 늘리지 않으려면 `UWA.Utils.getUUID()`, 추측하기 어려워야 하면 `UUID.createUUID()` |
| 깊은 비교 | `UWA.equals`, `DS/Utilities/Object.areEquals`, `Utils.areObjectsEqual` | `UWA.equals` ([Core 유틸](/uwa/core)) |
| 타입 검사 | `UWA.is(v, type)`, `Utils.isPlainObject` 등 | `UWA.is` |
| 로그 | `DS/Logger/Logger`, `console`, `UWA.log`, `WUX.log(msg, level)`, `DS/Utilities/Debug.log`, `DS/WebappsUtils/Console`, `DS/WebUtils/Logger` | `DS/Logger/Logger`. `WUX.log` 는 `WUX.setLogLevel(숫자)` 전에는 아무것도 내지 않음 |
| 요소 크기 변화 | `ResizeObserver`, `Dom.addResizeObserver`, `Dom.addResizeListener`, `ResizeSensor`, `onResize` | [크기 변화 받기](#크기-변화-받기) |
| 터치인지 | `TouchUtils.getTouchMode()`, `PointerEvents.isTouchEvent(e)`, UWA `Client.Features.touch`, `DeviceUtils.isMobileiOS()` | 화면 배치는 `getTouchMode()` (WUX 컨트롤과 맞음), 이벤트 하나는 `isTouchEvent(e)` |
| 키 판별 | `KeyboardUtils.ENTER` (`keyCode`), `event.key` | OOTB 와 맞추려면 KeyboardUtils, 새 코드는 `event.key === 'Enter'` 도 됨 |
| 지역 문자열 비교 | `StringUtils.compareLocal`, `Intl.Collator`, `localeCompare` | 빈 값이 섞이면 `compareLocal(a, b, { lang: widget.lang })` |
| HTML escape | `UWA.String.escapeHTML`, `StringUtils.escapeHTML` <Badge type="warning" text="2026" /> | `UWA.String.escapeHTML` (두 버전 모두 있음) |
| 날짜를 글자로 | `Intl.DateTimeFormat`, `DateUtils.convertToString` <Badge type="tip" text="2025" />, `DateUtils.formatDateToString` <Badge type="warning" text="2026" />, `UWA.Date.strftime` | [언어와 사용자 환경설정](/widget/i18n-preferences#날짜와-숫자-쓰기) |
| 화면 상태 저장 | `Utils.saveDataInLocalPreferences`, `widget.setValue` | 위젯 인스턴스마다 따로면 `widget.setValue` ([위젯 설정](/widget/preferences)) |

## DS/Core/Core 의 WUX 함수

`DS/Core/Core` 를 읽으면 생기는 전역 `WUX` 에도 몇 가지 함수가 있습니다.

| 함수 / 속성 | 호출 | 설명 |
| --- | --- | --- |
| `WUX.currentLanguage`, `WUX.currentFormatsLanguage` <Badge type="tip" text="2025" /> | 1 / 24 | 화면 언어, 날짜 / 숫자 형식 언어. [언어와 사용자 환경설정](/widget/i18n-preferences) |
| `WUX.onFormatsLanguageChange(fn)` / `offFormatsLanguageChange(fn)` <Badge type="tip" text="2025" /> | 4 / 4 | 형식 언어가 바뀔 때 |
| `WUX.setLogLevel(n)`, `WUX.log(msg, level?)`, `WUX.warn(msg, level?)` | 0 / 4 / 1 | `level` 이 `n` 이하일 때만 콘솔 출력. 기본 `n` 은 0 이라 출력 없음 |
| `WUX.enableWUXConsole()`, `getWUXConsole()` | 6 / 6 | 폭 200 px 의 화면 콘솔을 body 에 붙임 |
| `WUX.setFullscreen()` | 6 | 디버그용. 콘솔에 "운영에서 쓰지 말 것" 경고를 냄 |

## WebUtils, WebUAUtils

| 모듈 | 앱 | 내용 |
| --- | --- | --- |
| `DS/WebUtils/*` | 1 ~ 20 | 3DPlay 계열 내부 도구 (`Logger` 래퍼, `StringUtils.sprintf` / `getExtension`, `Vector2`, `GeometryUtils`, `Profiler`, `ContextedSingleton` ...). 일반 위젯은 쓰지 않음 |
| `DS/WebUAUtils/WebUAUtils` | 34 | 앱 도움말(User Assistance) 열기: `getMoreHelpCallback` 10, `getEmbeddedHelpFullPath` 7, `setPanelHelpMode` 6, `setAppid` 6, `getHelpFullPath` 4, `displayHelp` 3. 2026 은 52 앱 |

## 2026 변경

| 모듈 | 2026 |
| --- | --- |
| `Dom` | `createElement` 옵션이 `{ className, style, text }` 로 바뀜 (위), `setAttributes` 없어짐. `generateIcon` 이 `userPresence`, `countryFlag`, `step` 형식을 더 받음. `generateValidityIcon`, `getCountryNameFromSymbol` 추가 |
| `DateUtils` | `convertToString` 없어짐 → `formatDateToString`, `formatDateToRelativeTime`, `getDatePlaceholder`. `convertToDate` 가 `'2024-05-01'` 처럼 날짜만 있는 문자열을 **현지 자정**으로 읽음 (2025 는 `new Date()` 그대로라 UTC 자정) |
| `Utils` | `getStringFromNumber`, `isWidgetPrefActivated`, `triggerEvent` 없어짐. `typeOf`, `equals`, `clone`, `merge`, `extend` 는 `DS/UWAUtils/WUXUWAUtils` 의 것을 붙임. `dynamicRequire2`, `isIconDefinition` 추가 |
| `StringUtils` | `escapeHTML`, `unescapeHTML`, `camelCase`, `extract` 추가 |
| `Array` | `makeUnique` 추가 |
| `KeyboardUtils` | `getKeyCode(이름)` 추가. 키 이름에 맞는 사용자 영역(PUA) 유니코드 글자를 돌려줌 (단축키 표시 글꼴용으로 보임 <Badge type="danger" text="검증 필요" />) |
| `Debug` | `applyVisualDebug`, `applyCheckerboard` 추가 |
| 새 모듈 | `DS/Utilities/LanguageUtils`, `DS/Utilities/NumberUtils` ([언어와 사용자 환경설정](/widget/i18n-preferences#_2026-변경)), `DS/Utilities/LoadingUtils` (`loadScripts`, `loadJSFiles`, `loadCSSFiles`) |
| 옮겨짐 | `DS/Utilities/WUXUWAUtils` → `DS/UWAUtils/WUXUWAUtils` |
| `DS/Core/Core` | `WUX.currentFormatsLanguage`, `onFormatsLanguageChange` 없어짐 → `WUX.regionalFormatLanguage`, `onRegionalFormatLanguageChange` |
| 같음 | Logger, PointerEvents (상수와 함수), ResizeSensor (파일 동일), TouchUtils, UUID |
| 쓰는 앱 (의존성 기준) | Dom 103 → 112, Utils 94 → 110, TouchUtils 77 → 83, DateUtils 4 → 48, UUID 19 → 29 |
