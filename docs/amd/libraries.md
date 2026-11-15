# 들어 있는 외부 라이브러리 <Badge type="info" text="원본 분석" />

webapps 에는 DS 코드 말고도 오픈소스(일부 상용) 라이브러리가 들어 있습니다. 대부분 DS 가 만든 작은 AMD 래퍼(`DS/폴더/모듈`)로 감싸져 있어서 위젯에서 `require` 로 바로 부를 수 있습니다. 이 페이지는 위젯에서 부를 이름, 버전, OOTB 에서 쓰는 앱 수, 주의할 점을 쓰임별로 정리합니다. 모듈 이름 규칙과 `require.config` 는 [AMD 로딩](/amd/) 에 있습니다.

::: info 근거
래퍼 파일: Handlebars/Handlebars.js, CKEditor/CKEditor.js (2026 VENCKEditor/4.25.1/ckeditor.js 의 `licenseKey` 검사), vue/vue.js, vuex/vuex.js, Vuetify/Vuetify.js, vuekit/vuekit.js, vuekit3/VUEKIT.umd.js, ReactLoader/ReactLoader.js, React18Loader/React18Loader.js, ReactRedux/ReactRedux.js, ReactRouterDom/ReactRouterDom.js, ENO6WPlugins/ENO6WPlugins.js, ENOjquery/ENOjquery.js, ENOjqueryUI/jQueryUI.js, WebjQueryLib/WebjQueryLib.js, 3DXHighcharts/3DXHighcharts.js, VENHighChart/UIHighCharts.js, SMAHighcharts/SMAHighcharts.js, PDFjs/PDFjs.js, PDFLib/PDFLib.js, ZipJS2/ZipJS2.js, D3js_external, D3Cola, DagreShim (LayoutLib/1.0.0/layout.min.js), Leaflet/LeafletLoader.js, VENMathjs/VENMathjs.js, VENMathjs2/VENMathjs2.js, ENODOMPurify/ENODOMPurify.js, DSNodeSocketioClient_4.7.4, DSVideoJS, WUXRichText/WUXRichText.js. 버전은 각 라이브러리 파일의 머리 주석과 `version` 문자열에서 읽었습니다. 사용 예: TopBar, 3DPlayWidgetViews (Vue 2 + vuekit), SNSearchUX, MPFView (Vue 3 `createApp`), DELReactAppBooter (React 18), ENORouteMgmt (CKEditor), 3DPlay2DExperience (PDF.js), SMAMpaResultsChartsWebUI (Plotly). 앱 수는 `"DS/폴더/..."` 큰따옴표 문자열이 나오는 .js 파일을 webapps 최상위 폴더로 묶어 센 폴더 수입니다 (자기 폴더는 빼고, vuekit 이나 vuex 같은 래퍼 폴더도 1 로 셈). 2025 는 enovia2025 webapps, 2026 은 enovia2026_webapps 에서 같은 방식으로 셌습니다
:::

::: warning OOTB 라이브러리는 업그레이드 때 바뀝니다
webapps 의 라이브러리는 DS 가 자기 앱을 위해 넣은 것입니다. 2025 와 2026 을 비교하면 같은 이름 안의 버전이 바뀌거나 폴더가 통째로 없어진 경우가 있습니다. 커스텀 위젯이 여기에 기대면 업그레이드 뒤 바로 깨질 수 있습니다.

| 바뀐 것 | 2025 | 2026 |
| --- | --- | --- |
| jQuery | `DS/ENO6WPlugins/jQuery` (jQuery 2.1.1, 30 개 앱) | **ENO6WPlugins, VENENO6WPlugins 폴더 없음**. OOTB 는 `DS/ENOjquery/ENOjquery` (3.7.1, 37 개 앱)로 옮김 (LifecycleControls, ENOChangeActionUX 등) |
| jQuery UI | `DS/ENO6WPlugins/jQueryUI` (1.10.3) | 폴더 없음. `DS/ENOjqueryUI/jQueryUI` (1.14.1) |
| CKEditor | `DS/CKEditor/CKEditor` 가 4.18.0 을 읽음 | 같은 이름이 4.25.1-lts 를 읽음 (CKEditor 4 LTS) |
| DOMPurify | `VENCDDOMPurify/latest` 안이 3.1.7 | 같은 폴더 안이 3.3.0 |
| JSZip | `DS/JSZipTools/JSZip` (3.2.1) | JSZipTools 폴더 없음 |
| Highcharts 6 | `SPYTempVenHC/6.0.7` | 폴더 없음 |
| Math.js | SMAApproximationExperience 가 `DS/VENMathjs/VENMathjs` (5.4.2) 를 씀 | `DS/VENMathjs2/VENMathjs2` (15.1.0) 로 옮김 |
| Vue 3.5 | - | `vue-3.5.13` 폴더는 있지만 `vue.js` 가 없음 (빌드 기록 파일만 있음) |

없어진 폴더의 모듈을 부르면 묶음 파일과 개별 파일이 모두 404 가 되어 `require` 의 오류 콜백이 불립니다 ([오류 메시지](/amd/#오류-메시지)). 버전이 중요하면 라이브러리를 내 위젯 폴더에 넣어 씁니다 ([커스텀 위젯에서 쓸 때](#커스텀-위젯에서-쓸-때)).
:::

## 래퍼가 하는 일

래퍼는 대부분 같은 모양입니다. 전역 변수가 이미 있으면 그것을 모듈로 내놓고, 없으면 `require.toUrl` 로 VEN 폴더(원본 라이브러리 폴더)의 실제 파일 경로를 만들어 `require.config({ paths, shim })` 을 부른 뒤 그 경로에 기대는 `DS/...` 모듈을 정의합니다.

| 래퍼 동작 | 위젯에 주는 영향 |
| --- | --- |
| 전역 변수(`Highcharts`, `CKEDITOR`, `jQuery`, `PDFJS`, `io`, `mathjs` ...)가 이미 있으면 그것을 돌려줌 | 같은 창(window)에 다른 버전이 먼저 올라와 있으면 **그 버전이 옴**. 버전까지 비교해 맞는 버전을 주는 래퍼는 VENHighChart (11.4.4), ENO6WPlugins (2.1.1, 3.3.1) 뿐 |
| `require.config({ paths, shim })` 을 부름 | 로더 전역 설정이 바뀜. `vue`, `vu-kit`, `react`, `react-dom`, `highcharts/highcharts`, `reduxjs` 처럼 `DS/` 없는 이름도 생김 |
| 실제 버전은 래퍼 안에 적혀 있음 | 래퍼 이름은 그대로인데 업그레이드 때 버전만 바뀜 (CKEditor 4.18.0 → 4.25.1) |
| 디버그 빌드 전환 | `UWA.debug` (vue, vuex, Vuetify, vuekit), `localStorage` 의 `ReactDebug` / `ReactDOMDebug` (ReactLoader), `MathjsDebug` (VENMathjs), `window.$DRWAF_DEBUG` (React18Loader, ReactRedux, ReactRouterDom) |
| 모듈 값을 돌려주지 않는 것도 있음 | `DS/FabricJS/FabricJS` 는 파일 안에 `define` 이 없어서 값이 `undefined`. OOTB(2DAnnotation)는 전역 `window.fabric` 을 씀 |

3DDashboard 위젯은 위젯마다 iframe 에서 돌므로([실행 환경](/widget/#실행-환경)) "같은 창" 은 내 위젯 iframe 입니다. 내 위젯이 부른 OOTB 모듈이 같은 라이브러리를 먼저 읽어 두는 경우가 겹치는 경우입니다.

CSS 가 필요한 라이브러리는 [css!](/amd/plugins#css) 로 같이 부릅니다. 래퍼가 CSS 까지 불러 주는 것은 vuekit, DSVideoJS 정도입니다 (각 표의 "주의" 칸 참고).

## 템플릿 (Handlebars)

| 모듈 | 버전 | 쓰는 앱 (2025 / 2026) | 설명 |
| --- | --- | --- | --- |
| `DS/Handlebars/Handlebars` | Handlebars 4.7.8 | 51 / 58 | 아래 `Handlebars4` 와 같은 객체. `registerHelper` 를 감싸서 이미 있는 이름이면 Error 를 던짐 |
| `DS/Handlebars/Handlebars4` | 4.7.8 (`VENHandlebars/handlebars-v4.7.8.min.js`) | 37 / 40 | 원래 Handlebars. 전역 `Handlebars` 가 있으면 그것을 돌려줌 |

`DS/Handlebars/` 를 쓰는 폴더는 81 개 (2025) / 89 개 (2026) 로, webapps 에서 가장 많이 쓰는 외부 라이브러리입니다. `VENHandlebars/handlebars-v1.3.0.min.js` 도 있지만 부르는 곳은 없습니다. 4.7.8 은 AMD 로 읽혀서 전역 `Handlebars` 를 만들지 않습니다.

템플릿 파일(.html, .handlebars)은 [text!](/amd/plugins#text) 로 읽어 `compile` 합니다. OOTB(ENOChangeActionUX 등)도 같은 방식입니다.

```js
require([
  'DS/Handlebars/Handlebars',
  'text!DS/MyWidget/assets/templates/PartCard.html'
], function (Handlebars, source) {
  var template = Handlebars.compile(source);
  widget.body.setHTML(template({ name: 'PRT-0001', revision: 'A' }));
});
```

::: warning registerHelper 는 창 전체에 하나입니다
Handlebars 객체는 같은 창의 모든 모듈이 같이 씁니다. `DS/Handlebars/Handlebars` 는 이 공용 객체의 `registerHelper` 를 바꿔서, 같은 이름을 두 번 등록하면 `Handlebars helper '이름' has already been registered!` 오류를 던집니다. 한 번 바뀌면 `Handlebars4` 로 부른 쪽에도 똑같이 적용됩니다. 도우미 이름에는 앱 접두어(`myWidgetDate` 등)를 붙이고, 모듈이 처음 로드될 때 한 번만 등록합니다.
:::

## UI 프레임워크

### Vue

| 모듈 | 버전 | 쓰는 앱 (2025 / 2026) | 주의 |
| --- | --- | --- | --- |
| `DS/vue/vue` | Vue 2.6.10 (`vuejs/2.6.10/vue.min.js`) | 8 / 7 | Vue 2 생성자. 템플릿 컴파일러가 들어 있는 전체 빌드. 로더에 `vue` 라는 이름도 정의함. 8 중 3 은 래퍼(vuekit, vuex, Vuetify) |
| `DS/vuekit/vuekit` | vu-kit (DS 의 Vue 2 화면 부품 모음) | 4 / 3 | `Vue.use` 까지 해 주고 `vu-kit.css` 도 불러옴. `vu-btn`, `vu-input`, `vu-modal`, `vu-lightbox` 등 컴포넌트 35 개를 전역 등록 |
| `DS/vuex/vuex` | Vuex 3.6.2 | 0 / 0 | `Vue.use(Vuex)` 까지 해 줌 |
| `DS/Vuetify/Vuetify` | Vuetify 2.7.1 | 0 / 0 | `Vue.use(Vuetify)` 까지 해 줌. CSS(`vuetify-2.7.1/dist/vuetify.min.css`)는 따로 |
| `DS/vue-3.4.15/vue` | Vue 3.4.15 | 2 / 3 | Vue 3 전역 빌드를 AMD 로 감싼 것. SNSearchUX, vuekit3, MPFView <Badge type="warning" text="2026" /> |
| `DS/vue-3.3.4/vue` | Vue 3.3.4 | 0 / 0 | |
| `DS/vuekit3/VUEKIT.umd` | vu-kit 의 Vue 3 판 (컴포넌트 68 / 70 개) | 0 / 0 | `DS/vue-3.4.15/vue` 에 기댐. `app.use(VUEKIT, { lang, country })`. `VUEKIT.css` 는 스스로 불러오지 않음 |
| `vue-3.5.13` <Badge type="warning" text="2026" /> | - | - | 폴더에 `vue.js` 가 없음. 원본 빌드는 `vuejs/3.5.13/dist/` 에만 있음 |

vuekit 은 webapps 폴더 이름(`Vuekit\vuekit.mweb` 빌드)과 컴포넌트 이름(`vu-*`)으로 보아 DS 가 만든 Vue 화면 부품 모음입니다. 외부 라이선스 표기는 없습니다. TopBar, 3DPlayWidgetViews, W3DDrive 가 씁니다 (3DPlayApp 은 2025 만).

Vue 2 는 OOTB(3DPlayWidgetViews 의 W3DPlayLightboxView)처럼 `new Vue({ el, template, data, methods })` 로 붙입니다.

```js
require(['DS/vue/vue', 'DS/vuekit/vuekit'], function (Vue) {
  var mount = UWA.createElement('div').inject(widget.body);
  new Vue({
    el: mount,
    template: '<div><vu-btn @click="save">저장</vu-btn> <span v-text="message"></span></div>',
    data: function () { return { message: '' }; },
    methods: {
      save: function () { this.message = '저장했습니다'; }
    }
  });
});
```

Vue 3 은 `createApp` 으로 만들고 `mount` 합니다. OOTB(SNSearchUX, MPFView)는 template 문자열을 쓸 때 `compilerOptions.runtimeGlobalName` 을 모듈 이름으로 바꿔 줍니다.

```js
require(['DS/vue-3.4.15/vue'], function (Vue) {
  var app = Vue.createApp({
    template: '<button @click="count++" v-text="count"></button>',
    data: function () { return { count: 0 }; }
  });
  app.config.compilerOptions = { runtimeGlobalName: "require('DS/vue-3.4.15/vue')" };
  app.mount(UWA.createElement('div').inject(widget.body));
});
```

- 이 모듈은 Vue 를 전역 `Vue` 로 내놓지 않습니다. `runtimeGlobalName` 을 바꾸지 않으면 런타임에 컴파일한 template 이 전역 `Vue` 를 찾아 실패합니다. 컴파일러 기본값이 `runtimeGlobalName: "Vue"` 이고, 컴파일 결과(`const _Vue = Vue` 로 시작)를 `new Function` 으로 실행하므로 래퍼 안의 지역 변수 `Vue` 가 아니라 전역을 봅니다 (vue-3.4.15/vue.js).
- 다 쓴 뒤에는 `app.unmount()` 를 부릅니다 (MPFView 의 VueComponentWrapper).

### React

| 모듈 | 버전 | 쓰는 앱 (2025 / 2026) | 주의 |
| --- | --- | --- | --- |
| `DS/React18Loader/React` <Badge type="warning" text="2026" /> | React 18.2.0 | - / 28 | `window.$DRWAF_DEBUG` 가 참이면 development 빌드 |
| `DS/React18Loader/ReactDomClient` <Badge type="warning" text="2026" /> | react-dom 18.2.0 (`client`) | - / 6 | `createRoot` |
| `DS/React18Loader/ReactDom` <Badge type="warning" text="2026" /> | react-dom 18.2.0 | - / 2 | |
| `DS/React18Loader/FastDeepEqual` <Badge type="warning" text="2026" /> | fast-deep-equal 3.1.3 | - / 6 | |
| `DS/React18Loader/ReactRefresh`, `ReactDomTestUtils`, `ReactTestingLibrary` <Badge type="warning" text="2026" /> | react-refresh 0.14.0, testing-library 13.3.0 | - / 1, 0, 0 | 개발, 테스트용 |
| `DS/ReactLoader/React`, `DS/ReactLoader/ReactDom` | React 17.0.2 | 0 / 0 | localStorage `ReactDebug`, `ReactDOMDebug` 가 `'true'` 면 development. 전역 변수 `react`, `reactDOM` 에도 넣음 |
| `DS/ReactRedux/ReactRedux` | react-redux 8.0.2 | 0 / 5 | |
| `DS/ReactRedux/Toolkit` | @reduxjs/toolkit 1.8.3 | 1 / 8 | |
| `DS/ReactRedux/Redux`, `ReduxThunk`, `ReduxMockStore` | redux 4.2.0, redux-thunk 2.4.1, redux-mock-store 1.5.3 | 0 / 0 | |
| `DS/ReactRouterDom/ReactRouterDom` <Badge type="warning" text="2026" /> | react-router-dom 6.3.0 (history 5.3.0) | - / 3 | 전역 변수 `reactRouterDOM` 에도 넣음 |

`DS/React18Loader/` 를 쓰는 폴더는 2026 에 31 개입니다 (DELMIA 계열 DEL*, VCX*, XCT* 앱). React18Loader 는 `react`, `react/jsx-runtime`, `react-dom`, `react-dom/client`, `lodash.clonedeep`, `lodash.debounce`, `fast-deep-equal` 경로와 `immer` 패키지도 같이 등록합니다.

위젯에 붙이는 법은 DELReactAppBooter 와 같습니다. 위젯 `onLoad` 에서 `createRoot(요소).render(...)` 를 부릅니다.

```js
widget.addEvent('onLoad', function () {
  require([
    'DS/React18Loader/React',
    'DS/React18Loader/ReactDomClient'
  ], function (React, ReactDomClient) {
    var root = ReactDomClient.createRoot(widget.body);
    root.render(React.createElement('div', { className: 'my-panel' }, 'My widget'));
  });
});
```

- 위젯 HTML 에서 바로 쓰려면 JSX 대신 `React.createElement` 로 씁니다. JSX 는 빌드 도구로 미리 바꿔 둬야 합니다.
- development 빌드가 필요하면 로더를 부르기 **전에** `window.$DRWAF_DEBUG = true` 를 둡니다 (DELReactAppBooter 가 이 순서).
- ReactLoader(17) 와 React18Loader(18)는 둘 다 `react` 라는 같은 경로 이름을 씁니다. 한 창에서 둘을 같이 부르면 먼저 읽힌 React 가 쓰이는 것으로 보입니다 <Badge type="danger" text="검증 필요" />. 2025 에는 React18Loader 가 없으므로 React 18 이 필요하면 직접 넣어야 합니다.

### jQuery

| 모듈 | 버전 | 쓰는 앱 (2025 / 2026) | 주의 |
| --- | --- | --- | --- |
| `DS/ENO6WPlugins/jQuery` <Badge type="tip" text="2025" /> | jQuery 2.1.1 (`VENENO6WPlugins/plugins/jquery/latest`) | 30 / - | `noConflict()` 로 `$` 를 돌려놓음 |
| `DS/ENO6WPlugins/jQuery_3.3.1` <Badge type="tip" text="2025" /> | 3.3.1 | 3 / - | |
| `DS/ENO6WPlugins/jQueryUI` <Badge type="tip" text="2025" /> | jQuery UI 1.10.3 | 8 / - | |
| `DS/ENOjquery/ENOjquery` <Badge type="warning" text="2026" /> | 3.7.1 (`VENCDjquery/latest/dist`) | - / 37 | 전역 `jQuery` 가 이미 있으면 **버전과 상관없이** 그 전역 값을 돌려줌 (그 전역이 3.7.1 이 아니면 3.7.1 은 `DS/ENOPlugins/ENOjquery` 로 따로 정의됨. 전역 `jQuery` 가 없으면 이 모듈이 3.7.1 을 읽어 돌려줌) |
| `DS/ENOjqueryUI/jQueryUI` <Badge type="warning" text="2026" /> | jQuery UI 1.14.1 (`VENCDjqueryUI/latest`) | - / 6 | CSS(`jquery-ui.min.css`)는 불러오지 않음 |
| `DS/WebjQueryLib/WebjQuery` | 3.5.1 (`VENWebjQueryLibExt`) | 0 / 0 | 2025, 2026 모두 있음 |

- `VENENOFrameworkPlugins` 에는 Fancytree 같은 jQuery 플러그인이 들어 있고 ENOFrameworkPlugins 가 씁니다. 그 안의 `jQuery.js` 는 2026 에 없습니다.
- 새 위젯은 UWA / WebUX 컨트롤로 충분한 경우가 많습니다. jQuery 가 꼭 필요하면 2025, 2026 에 같은 이름으로 있는 것이 `DS/WebjQueryLib/WebjQuery` 뿐이라는 점을 감안합니다.

### 그 밖의 UI 라이브러리

| 폴더 | 내용 | 쓰는 곳 |
| --- | --- | --- |
| `Polymer`, `Polymer-0.5.5`, `Polymer-1.1.4`, `Polymer-1.6.1`, `Polymer-1.11.3`, `webcomponentsjs-2.2.7` | Polymer 0.x ~ 1.11.3, Web Components polyfill | 부르는 모듈 없음 |
| `Sencha` | Ext JS 7.1.0 (`ext-all.js`) | ENOGantt (`Ven/Ext.js`) |
| `Bryntum` | Bryntum Gantt Pro 6.1.7 (`gnt-all.js`) | ENOGantt (`Ven/Bryntum.js`) |

Ext JS 와 Bryntum 은 머리 주석에 상용 라이선스 안내가 있습니다. ENOGantt 전용으로 보고 커스텀 위젯에서는 쓰지 않는 것이 안전합니다.

## 차트

| 모듈 | 버전 | 쓰는 앱 (2025 / 2026) | 주의 |
| --- | --- | --- | --- |
| `DS/3DXHighcharts/3DXHighcharts` | Highcharts 4.0.3 (`VEN3DXHighcharts/4.0.3/highcharts-all`) | 5 / 4 | 전역 `Highcharts` 가 있으면 버전과 상관없이 그것. 4.0.3 은 shim 으로 읽혀 전역 `Highcharts` 를 만듦. Foundation, SNResultUX, W3DXComponents |
| `DS/HighChartsModules/HighCharts_drilldown` | 4.0.3 drilldown 모듈 | 1 / 1 | 값 없음. SNResultUX |
| `DS/VENHighChart/UIHighCharts` | Highcharts 11.4.4 (+ highcharts-more, pattern-fill) | 2 / 3 | AMD 로 읽혀 전역을 만들지 않음. 로더에 `highcharts/highcharts` 이름을 만듦. ENOProjectMgmtCommonComponents, VIOCtrlCenterApp, DELReactControls <Badge type="warning" text="2026" /> |
| `DS/SMAHighcharts/Highcharts` | Highcharts 11.4.1 (`SMAVenHighcharts/11.4.1`) | 0 / 0 | 전역 `Highcharts` 가 있으면 그것 |
| `SPYTempVenHC/6.0.7` <Badge type="tip" text="2025" /> | Highcharts 6.0.7 | SPYHC, SPYExtUI | 2026 에 없음 |
| `PlotlyJS-1.46.1` ~ `PlotlyJS-3.0.1` <Badge type="warning" text="2026" /> | plotly.js 1.46.1, 1.52.3, 1.55.2, 1.58.4, 2.6.2, 3.0.1 | - / 1 | 래퍼 없음. SMAMpaResultsChartsWebUI 가 경로를 직접 잡음 (아래) |
| `DS/D3js_external/D3js` | d3 3.5.17 (`VEND3js/3.5.17`) | 1 / 1 | 전역 `d3`. SNNavigationMap |
| `DS/VEND3js/5.7.0/d3.min` | d3 5.7.0 | 1 / 1 | 래퍼 없이 파일 경로로 부름. SocialGlobeAPI. VEND3js 에는 3.4.10, 3.4.11, 3.5.8, 4.1.1 도 있음 |
| `DS/D3Cola/D3Cola` | WebCola 3.0.0 | 1 / 1 | SNNavigationMap |
| `DS/DagreShim/DagreShim` | dagre (`LayoutLib/1.0.0/layout.min`, dagre + graphlib UMD 묶음) | 1 / 1 | 그래프 배치 라이브러리. `DS/LayoutLib` 를 그대로 돌려줌 (전역 `layout` 객체가 이미 있으면 그것을 `DS/LayoutLib` 로 씀). shim 설정의 exports 이름은 `layout` 이지만 파일은 AMD 로 스스로 정의되고, AMD 가 없을 때 만드는 전역은 `dagre`. SNNavigationMap |

```js
require(['DS/VENHighChart/UIHighCharts'], function (Highcharts) {
  var el = UWA.createElement('div', { styles: { height: '300px' } }).inject(widget.body);
  Highcharts.chart(el, {
    title: { text: 'My chart' },
    series: [{ type: 'column', data: [3, 5, 2] }]
  });
});
```

- Highcharts 는 머리 주석에 `License: www.highcharts.com/license` 가 있습니다 (상용 라이선스 제품). 커스텀 위젯에서 쓰기 전에 라이선스를 확인합니다.
- 4.0.3 과 11 은 API 가 많이 다릅니다. 같은 위젯에서 `3DXHighcharts` 와 `UIHighCharts` 를 섞지 않습니다.

래퍼가 없는 Plotly 는 SMAMpaResultsChartsWebUI 처럼 경로와 shim 을 직접 잡습니다. 경로 이름은 창 전체에 걸리므로 겹치지 않게 짓습니다.

```js
var base = require.toUrl('DS/PlotlyJS-3.0.1/').split('?')[0];
require.config({
  paths: { 'MyWidget-plotly': base + 'plotly.min' },
  shim: { 'MyWidget-plotly': { exports: 'Plotly' } }
});
require(['MyWidget-plotly'], function (Plotly) {
  Plotly.newPlot(chartEl, [{ x: [1, 2, 3], y: [2, 1, 3] }]);
});
```

## 그래프와 트리 그리기 (egraph, etree)

| 모듈 | 쓰는 앱 (2025 / 2026) | 설명 |
| --- | --- | --- |
| `DS/egraph/core`, `DS/egraph/views`, `DS/egraph/iact`, `DS/egraph/utils` | 11 / 11, 11 / 11, 9 / 8, 12 / 11 | 노드와 연결선 그래프 그리기. CSS 는 `css!DS/egraph/views.css` (9 곳), `views_default.css` |
| `DS/egraph/overview` | 2 / 2 | 전체 보기 |
| `DS/etree/syncwidget`, `DS/etree/tree`, `DS/etree/iact`, `DS/etree/syncviewsFactory`, `DS/etree/widget` | 9 / 9, 3 / 2, 4 / 3, 2 / 2, 2 / 1 | egraph 위의 트리 배치. CSS 는 `overviewui.css`, `syncviews.css` |

- 외부 오픈소스가 아니라 DS 의 WebUX 라이브러리입니다 (빌드 기록 `.egraph.index.mk` 의 묶음 이름이 `WebUX\egraph.mweb`, 라이선스 표기 없음). `DS/` 없는 별칭(`egraph/core`, `etree/tree` ...)도 정의됩니다.
- 쓰는 곳: GraphListView, HistoryExplorer, ENOXVersionExplorerThumbnailGraphView, SNResultUX, EPSSchematicsUI, ENXDataGrid, ENXNav 등. API 는 이 페이지에서 다루지 않습니다.

## 편집기

| 모듈 | 버전 | 쓰는 앱 (2025 / 2026) | 주의 |
| --- | --- | --- | --- |
| `DS/CKEditor/CKEditor` | CKEditor 4.18.0 / **4.25.1-lts** | 14 / 14 | 전역 `CKEDITOR` 를 돌려줌 (전역 `CKEDITOR_BASEPATH` 도 정함). 스킨, 언어, 플러그인은 CKEditor 가 basepath 에서 스스로 읽음. `VENCKEditor/4.16.0` 도 있지만 래퍼는 안 씀 |
| `DS/WUXRichText/CKEditor` <Badge type="warning" text="2026" /> | CKEditor 5 47.1.0 (`ckeditor5/`) | - / 0 | WebUX 컨트롤. 만들 때 ckeditor5 의 CSS 와 ES 모듈 js 를 불러와 `ClassicEditor` 를 만듦. `value` 속성, `ready`, `change`, `uncommittedChange` 이벤트 |
| CodeMirror 5.47.0 (`CodeMirrorV5.47/`) | 5.47.0 | - | [ControlsEx 의 CodeMirror](/controls/controls-ex#codemirror) 가 감싸서 씀. 직접 부를 AMD 이름은 없음 |
| CodeMirror 5.15.2 (`CodeMirror/`) | 5.15.2 | - | EPSSchematicsUI 가 `../CodeMirror/lib/codemirror.js` 를 스크립트로 직접 읽음 |
| `DS/jsBeautify/javascript/index` | js-beautify (버전 표기 없음) | 1 / 1 | `css/index`, `html/index` 도 있음 |
| `DS/VENHtmlDiff/HTMLDiff` | HTML 비교 | 2 / 2 | DELCompareIntegration, HTMLDiffModule |
| `marked/marked.js` | marked (머리 주석 2011-2020) | 0 / 0 | 부르는 곳 없음 |

CKEditor 4 는 textarea 를 바꿔 끼우는 방식입니다 (ENORouteMgmt 의 검토 의견 칸).

```js
require(['DS/CKEditor/CKEditor'], function (CKEDITOR) {
  var textarea = UWA.createElement('textarea', { name: 'myComment' }).inject(widget.body);
  var editor = CKEDITOR.replace(textarea, {
    toolbar: [{ name: 'basicstyles', items: ['Bold', 'Italic'] }],
    height: 120
  });
  editor.on('change', function () {
    console.log(editor.getData());
  });
});
```

- 4.25.1 은 "CKEditor 4 LTS (Extended Support Model)" 판이고, 같은 폴더의 LICENSE.md 는 4.23.0 이상을 상용 라이선스 조건으로 안내합니다. 4.18.0 과 라이선스 조건이 다릅니다.
- 4.25.1 (2026) 에는 `config.licenseKey` 를 검사하는 코드가 있습니다. 키는 ckeditor.js 의 기본 설정(`CKEDITOR.config.licenseKey`)에 들어 있고 이 키는 검사를 통과하므로, OOTB 는 따로 넣지 않습니다. 키를 빈 값이나 다른 값으로 덮어쓰면 에디터가 만들어진 직후(`instanceLoaded`) 콘솔에 `[CKEDITOR]: The license key is missing or invalid.` 를 찍고 에디터를 `destroy()` 합니다.
- ckeditor5 는 ES 모듈 파일이라 AMD `require` 로 부르지 않습니다. WUXRichText 는 2026 의 `DS/Utilities/LoadingUtils.loadScripts` (`<script type="module">` 로 읽음, [LoadingUtils](/amd/utilities#loadingutils))를 씁니다. 화면 컨트롤 쪽에서 고르는 표는 [컨트롤 개요](/controls/#무엇을-쓸까) 에 있습니다. OOTB 의 Vue 앱(SocialToolbarVue)은 CKEditor 5 41.4.2 를 자기 파일 안에 묶어 넣어 따로 씁니다.

## 문서와 파일

| 모듈 | 버전 | 쓰는 앱 (2025 / 2026) | 주의 |
| --- | --- | --- | --- |
| `DS/PDFjs/PDFjs` | PDF.js. 기본 1.9.426, `window.PDFJS_VERSION` 으로 1.4.257, 2.6.347, 2.13.216, 3.11.174 | 4 / 4 | 2.x 이상이면 `GlobalWorkerOptions.workerSrc` 를 맞춰 줌. 전역 `PDFJS` 가 있으면 그것 |
| `DS/PDFLib/PDFLib` | pdf-lib 1.17.1 | 1 / 2 | 전역 `PDFLib` 가 있으면 그것. DMUPersistence, MMPrint <Badge type="warning" text="2026" /> |
| `DS/ZipJS2/ZipJS2` | @zip.js/zip.js 2.6.5 + pako 2.0.4 | 10 / 10 | zip.js 2 의 API 객체를 돌려줌. `DS/ZipJS2/pako` 로 pako 만 부를 수도 있음 |
| `DS/ZipJS/zip`, `zip-fs`, `inflate`, `deflate` | zip.js (2013 판, DS 수정) | 10 / 10, 6 / 6, 5 / 8, 5 / 8 | `DS/Visualization/*` 에 기댐. 3D 데이터 읽기(WebVisualization, ModelLoader 계열)가 씀 |
| `DS/JSZipTools/JSZip` <Badge type="tip" text="2025" /> | JSZip 3.2.1 | 0 / - | 2026 에 없음. 같은 내용의 `DS/AdminUI_JSZipTools/JSZip` (1 / 1, DELMCBUserDefAdmin)은 두 버전 모두 있음 |
| `DS/EasySax/EasySax` | EasySAX (XML SAX 파서) | 5 / 5 | Workers, XCADLoader 등 |
| `DS/VENWebHtml2Canvas/Html2Canvas` | html2canvas 1.0.0-rc.5 | 2 / 2 | UWA 클래스. `new Html2Canvas().htmlToCanvas(요소, 옵션, 콜백)` 이 Promise 를 돌려줌 |
| `DS/VENWebCanvg/CanvgTools` | canvg (SVG 를 canvas 로) | 2 / 1 | |
| `DS/FabricJS/FabricJS` | Fabric.js 1.7.9 | 1 / 1 | 모듈 값 없음. 전역 `window.fabric` 을 씀 (2DAnnotation) |
| `DS/Venfflate/fflate` <Badge type="warning" text="2026" /> | fflate | - / 1 | i3DxMdSimulationWeb |
| `DS/VenCBORX/VenCBORX` <Badge type="warning" text="2026" /> | cbor-x (`cborxJS-1.6.0`) | - / 1 | SPY |

```js
window.PDFJS_VERSION = '2.13.216';
require(['DS/PDFjs/PDFjs'], function (pdfjs) {
  pdfjs.getDocument({ data: new Uint8Array(arrayBuffer), isEvalSupported: false }).promise
    .then(function (pdf) {
      console.log(pdf.numPages);
    });
});
```

::: warning DS/PDFjs/PDFjs 의 기본 버전은 1.9.426 입니다
`window.PDFJS_VERSION` 이 없으면 오래된 1.9.426 을 읽습니다. OOTB(3DPlay2DExperience, DMUPersistence, WebDocumentVisualization)는 `window.PDFJS_VERSION = '2.13.216'` 을 먼저 넣고 부릅니다. 버전은 이 모듈이 그 창에서 **처음 로드될 때** 정해지므로, 다른 모듈이 먼저 다른 버전으로 불렀으면 그 버전이 옵니다. 3DPlay2DExperience 는 다 쓴 뒤 `window.PDFJS_VERSION = undefined` 로 되돌립니다.
:::

## 지도와 공간

| 모듈 | 버전 | 쓰는 앱 (2025 / 2026) | 주의 |
| --- | --- | --- | --- |
| `DS/Leaflet/LeafletLoader` | Leaflet 0.7.7 | 1 / 1 | CSS 는 `css!DS/Leaflet/Leaflet.css` 로 따로. MPFLabMap |
| `DS/Leaflet/MarkerClusterLoader` | Leaflet.markercluster | 0 / 0 | CSS `Leaflet/assets/plugin/MarkerCluster/MarkerCluster.Default.css` |
| `DS/Leaflet/MPMapView` | Leaflet 위에 만든 DS 지도 화면 | 1 / 1 | 위 CSS 를 스스로 부름. MPFLabMap |
| `DS/Supercluster/supercluster.min` | supercluster | 1 / 1 | SocialGlobe |
| `DS/VENTurfJS/turf.min` | Turf.js | 1 / 2 | MapDemoWidget, SocialGlobe <Badge type="warning" text="2026" /> |
| `VENProj4js`, `VENProj4js-2.7.2` | proj4js (좌표계 변환) | 0 / 0 | |
| `libtess/libtess.min` | libtess (다각형 분할) | 0 / 0 | |

## 날짜와 수학

| 모듈 | 버전 | 쓰는 앱 (2025 / 2026) | 주의 |
| --- | --- | --- | --- |
| `DS/VENMomentJS-2.23.0/moment-timezone` | moment-timezone 0.5.23 + Moment 2.23.0 | 1 / 3 | DS 가 고쳐서 `DS/VENMomentJS-2.23.0/moment` 에 기댐. DELWebInfrastructureControls, DELPPWProcessValidationModule, DELPXPGanttWOView <Badge type="warning" text="2026" /> |
| `DS/VENMomentJS-2.23.0/moment` | Moment 2.23.0 | 0 / 0 | |
| `DS/VENMomentJS-2.30.1/moment`, `moment-timezone` <Badge type="warning" text="2026" /> | Moment 2.30.1 + moment-timezone 0.5.48 | - / 0 | |
| `VENMomentJS-2.10.3/moment.js` | **실제 내용은 2.23.0** | 0 / 0 | 폴더 이름과 버전이 다름. 2.10.3 은 `VENMomentJSLibs/moment.js` 에 있음 |
| `DS/VENMathjs/VENMathjs` | Math.js 5.4.2 | 3 / 3 | 전역 `mathjs` 가 있으면 그것. KnowledgeWebCore, KnowledgeWebUtils, ControlsEx <Badge type="warning" text="2026" /> |
| `DS/VENMathjs2/VENMathjs2` <Badge type="warning" text="2026" /> | Math.js 15.1.0 | - / 1 | SMAApproximationExperience 가 VENMathjs 에서 옮겨 옴 |

폴더 이름의 버전만 믿지 않습니다. 쓰기 전에 파일의 `version` 값(Moment 는 `moment.version`, Math.js 는 `math.version`)을 확인합니다.

## 보안과 데이터 처리

| 모듈 | 버전 | 쓰는 앱 (2025 / 2026) | 주의 |
| --- | --- | --- | --- |
| `DS/ENODOMPurify/ENODOMPurify` | DOMPurify 3.1.7 / 3.3.0 (`VENCDDOMPurify/latest/dist/purify.min`) | 2 / 3 | `DOMPurify.sanitize(html)`. CollaborativeTasks, E6WCommonUI, ENORouteMgmt <Badge type="warning" text="2026" /> |
| `ajv` <Badge type="warning" text="2026" /> | ajv 6.7.0 (JSON Schema 검사) | - / 0 | 래퍼 없음 |
| `lodash.clonedeep-4.5.0`, `lodash.debounce-4.0.8` <Badge type="warning" text="2026" /> | lodash 함수 하나씩 | - / 1 | React18Loader 가 `lodash.clonedeep`, `lodash.debounce` 경로 이름을 만듦 |
| `immer-10.1.1` <Badge type="warning" text="2026" /> | immer 10.1.1 | - / 0 | React18Loader 가 `immer` 패키지로 등록 |
| `fast-deep-equal-3.1.3` <Badge type="warning" text="2026" /> | fast-deep-equal 3.1.3 | - / 1 | `DS/React18Loader/FastDeepEqual` 로 부름 |

서버에서 받은 서식 있는 글(HTML)을 화면에 넣을 때는 DOMPurify 로 거릅니다.

```js
require(['DS/ENODOMPurify/ENODOMPurify'], function (DOMPurify) {
  descriptionEl.innerHTML = DOMPurify.sanitize(htmlFromServer);
});
```

## 통신, 미디어, 입력

| 모듈 | 버전 | 쓰는 앱 (2025 / 2026) | 주의 |
| --- | --- | --- | --- |
| `DS/DSNodeSocketioClient_4.7.4/DSNodeSocketioClient_4.7.4` | socket.io-client 4.7.4 | 2 / 2 | 전역 `io` 가 있으면 그것. NotificationManager, RTDriver. 위젯에서 쓰는 법은 [실시간 통신 (WebSocket)](/widget/websocket) |
| `DS/DSNodeSocketioClient/DSNodeSocketioClient` | socket.io-client 4.7.4 | 0 / 0 | 폴더 이름에 버전이 없는 쪽. 내용은 같은 4.7.4 |
| `DS/DSVideoJS/DSVideoJS` | video.js 6.6.0 (`VENBrightcoveVideoJS/6.6.0`) | 1 / 1 | CSS 도 같이 부름. 플러그인 `DSVideoResolutionSwitcherPlugin`, `DSVideoThumbnailPlugin`. SwymUIComponents |
| `DS/VENHammer/2.0.8/hammer` | Hammer.js 2.0.8 (터치 제스처) | 1 / 1 | UIKIT 이 씀 |
| `DS/VENHowler/2.2.3/dist/howler` | howler.js 2.2.3 (소리 재생) | 1 / 1 | XCTWebCreativeAppPlay |
| `VENNippleJS/0.6.8`, `VENNippleJSLib/0.6.8` | nipplejs 0.6.8 (화면 조이스틱) | 1 / 1 | WebInfraUI, VirtualDevice |
| `VENMediaSoupClient` <Badge type="warning" text="2026" /> | mediasoup-client | - / 0 | |

## 커스텀 위젯에서 쓸 때

| 기준 | 이유 |
| --- | --- |
| 래퍼 이름(`DS/...`)으로 부름 | VEN 폴더 안 버전 경로는 업그레이드 때 바뀌거나 없어짐 (VENENO6WPlugins) |
| 2025, 2026 에 같은 이름으로 있고 OOTB 가 많이 쓰는 것을 고름 | Handlebars, CKEditor, PDFjs, ZipJS2, ENODOMPurify 는 두 버전 모두 같은 이름 |
| 버전이 중요하면 내 위젯 폴더에 직접 넣음 | 래퍼 이름은 그대로 두고 안의 버전만 바뀔 수 있음 (CKEditor). OOTB(SocialToolbarVue)도 CKEditor 5 를 자기 파일에 묶어 씀 |
| 전역 변수를 만드는 라이브러리는 한 창에 한 버전만 둠 | 먼저 올라온 전역 값이 래퍼에서 그대로 나옴 (Highcharts, jQuery, CKEditor) |
| 라이선스를 확인 | Highcharts, CKEditor 4 LTS, Ext JS, Bryntum 은 머리 주석에 상용 라이선스 안내가 있음 |

내 폴더에 넣은 UMD 파일은 모듈 이름으로 바로 부르면 됩니다. 전역 변수만 만드는 파일은 OOTB 래퍼처럼 shim 을 줍니다.

```js
require.config({
  paths: { 'MyWidget-chartlib': require.toUrl('DS/MyWidget/lib/chartlib.min').split('?')[0] },
  shim: { 'MyWidget-chartlib': { exports: 'ChartLib' } }
});
require(['MyWidget-chartlib'], function (ChartLib) {
  ChartLib.draw(chartEl);
});
```

- 경로 이름에 `DS/` 를 붙이지 않으면 [묶음 파일](/amd/#로더가-묶음-파일을-찾는-방법) 확인 요청이 생기지 않습니다. 대신 창 전체에서 겹치지 않는 이름(`MyWidget-...`)을 씁니다.
- `require.toUrl` 이 붙이는 `?` 뒤 값은 OOTB 래퍼처럼 떼고 넣습니다.
