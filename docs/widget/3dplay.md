# 3D / 2D 미리 보기 (3DPlay) <Badge type="info" text="원본 분석" />

`DS/3DPlayHelper/3DPlayHelper` 로 위젯 안의 요소에 3DPlay 뷰어를 만들어 부품(3D), 도면(2D), 문서 파일을 보여 줍니다. OOTB 의 속성 패널(EditPropWidget), 검색 결과(SNResultUX), 첨부 미리 보기가 같은 방식입니다. 3DPlay 앱의 전체 기능이 필요하면 [TransientWidget](/widget/transient-widget) 으로 3DPlay 앱(`X3DPLAW_AP`)을 엽니다.

::: info 근거
3DPlayHelper/3DPlayHelper.js (전역 함수 `Player3DPlayWeb`, `Launch3DPlayWeb`, `DS/3DPlayHelper/3DPlayWebPlayer`, `DS/3DPlayHelper/SplashScreen`), 3DPlay/3DPlay.js (`DS/3DPlay/3DPlay` 의 `createExperience`, `addCallbacks`, `publish` / `subscribe`), 3DPlayConnectors/3DPlayConnectors.js (`DS/3DPlayConnectors/3DPlayInputPreProcessor`), 3DPlaySupport/3DPlaySupport.js (`SupportList` 의 usage 표와 타입별 experience, `AssetSupport.checkSupport`), 3DPlayExperienceModule.js (`ASSET/LOADINGFINISHED` 등 publish), 3DPlayHelper/3DPlaySyndication.html, 3DPlayWidget/3DPlayWidget.js (`W3DPlayPrefsCtrl` 의 `X3DContentId`), 3DPlayApp/assets/json/CAT3DPLAW_AP.idcard.json, 2026 의 3DPlayAppV2/3DPlayAppV2.js 와 WebAfr/Applications/X3DPLAW_AP.json. 호출부: EditPropWidget (`facets/3DPlay/3DPlayFacet`), SNResultUX, ENOAttachmentsPreviewWebInWin, DocumentPreview, ENODODisplay, FolderEditor (2026), DMUPlayMarker 와 CAT3DAnnotationUtils (TransientWidget). 집계: `"DS/3DPlay/3DPlay"` 를 쓰는 폴더 23 개 (2025, 2026), `"DS/3DPlayHelper/3DPlayHelper"` 17 개 (2025) / 16 개 (2026). 세는 방법은 [들어 있는 외부 라이브러리](/amd/libraries) 와 같음
:::

샘플: [3DPlaySample.html](https://github.com/LeeOuHyoun/3d-docs/blob/main/WidgetSample/3DPlaySample.html) (physicalid 로 미리 보기, 닫기, 3DPlay 앱으로 열기). 3DSpace 주소, 플랫폼 id, 창 열기를 대시보드가 맡으므로 3DDashboard 에 올린 위젯에서만 확인할 수 있습니다.

## 방법 고르기

| 방법 | 화면이 뜨는 곳 | OOTB |
| --- | --- | --- |
| `DS/3DPlayHelper/3DPlayHelper` (`Player3DPlayWeb`) | 내 위젯 안의 요소 | EditPropWidget, SNResultUX, ENOAttachmentsPreviewWebInWin, FolderEditor <Badge type="warning" text="2026" /> |
| `3DPlayHelper/3DPlaySyndication.html` iframe | 내 위젯 안의 iframe | DocumentPreview |
| TransientWidget 로 3DPlay 앱(`X3DPLAW_AP`) 열기 | 대시보드의 창. 3DPlay 앱 전체 기능 | DMUPlayMarker, CAT3DAnnotationUtils |

## 미리 보기 만들기

3DSpace 객체를 3DPlay 입력(asset)으로 만듭니다.

<<< @/../WidgetSample/3DPlaySample.html#asset{js}

담을 요소와 입력을 넘겨 만듭니다. 다른 객체로 바꿀 때는 이전 것을 정리하고 새로 만듭니다 (EditPropWidget 과 같은 순서).

<<< @/../WidgetSample/3DPlaySample.html#create{js}

- `DS/3DPlayHelper/3DPlayHelper` 는 함수 `Player3DPlayWeb` 을 돌려줍니다. 같은 이름의 전역 함수이기도 합니다. 함수가 객체를 돌려주므로 OOTB 처럼 `new` 로 불러도 되고 그냥 불러도 같습니다.
- 돌려받는 객체는 바로 오지만 실제 화면(experience)은 비동기로 만들어집니다. 화면을 다루는 코드는 `ExperienceReady` 콜백 뒤에 둡니다.
- 3DSpace 주소(`serverurl`)는 [3DSpace 주소와 SecurityContext](/widget/space-url#서버-주소-3dspace) 의 방법으로 얻습니다 (샘플의 `getSpaceUrl`).

### 넘기는 값

| 키 | 설명 |
| --- | --- |
| `container` | 화면을 넣을 요소 또는 요소 id |
| `input` | 볼 대상 `{ asset, usage, experience }`. 파일 주소 문자열 하나만 줘도 됨 (`provider: 'FILE'` 로 바뀜) |
| `options` | 불러오는 방식, 콜백, 스플래시 ([options](#options)) |
| `ctx` | 3DPlay 컨텍스트 id. 없으면 `options.ctx`, 그것도 없으면 새로 만듦 |
| `lang` | 언어. 없으면 `widget.lang` |

### input.asset (3DSpace 객체)

| 키 | 필수 | 값 |
| --- | --- | --- |
| `provider` | O | `'EV6'`. `'3DSPACE'`, `'EV5'`, `'GEOVIA'` 도 같은 규칙으로 처리 |
| `physicalid` | O | 객체의 physicalid (문자열) |
| `serverurl` | O | 3DSpace 주소 (문자열) |
| `dtype` | 권장 | 타입 이름. 열 화면을 고르는 데 씀 ([타입별로 열리는 화면](#타입별로-열리는-화면)). 없으면 `DeFaUltDtYpE` 로 바꾸고 3D 로 봄 |
| `tenant` | 권장 | 플랫폼 id (`widget.getValue('x3dPlatformId')`) |
| `contextId` | - | SecurityContext. OOTB 는 앱 설정의 값을 넣음 |
| `requiredAuth`, `requestsOptions` | - | OOTB 는 `'passport'`, `{ authentication: 'passport' }` |

- `physicalid` 나 `serverurl` 이 비었거나 문자열이 아니면 `BadUrl` 오류가 됩니다 (3DPlayInputPreProcessor).
- 다른 provider: `'FILE'` (`filename` 에 파일 주소. `format` 이 없으면 확장자로, 확장자도 없으면 3dxml 로 봄), `'BUFFER'` (`dataFormat` 필수), `'3DSwym'`, `'3DDrive'` 등. ENODODisplay 는 파생 문서를 `provider: '3DSPACE'` 와 `mimetype`, `doticketurl` 로 보여 줍니다.

### input.usage

| 값 | 고르는 화면 |
| --- | --- |
| `'preview'` | 가벼운 미리 보기 (3D 는 `3DPlayModelViewer`). EditPropWidget, SNResultUX, FolderEditor 가 씀 |
| 없음 또는 `'collaborate'` | 기본 화면 (3D 는 `3DPlayCollaborativeExperience`) |
| `'NR'`, `'SimReview'` | 시뮬레이션용 화면 |
| `'3DSwYm'`, `'3DWhiteboard'` | 요소 안에 `3DPlaySyndication.html` iframe 을 만들어 그 안에서 띄움 |

### 타입별로 열리는 화면

| `dtype` | `usage: 'preview'` | 기본 |
| --- | --- | --- |
| `VPMReference`, `VPMRepReference`, `CATPart`, `CATProduct`, `3DShape` | 3DPlayModelViewer | 3DPlayCollaborativeExperience |
| `CATDrawing` | 3DPlay2DExperiencePreview | 3DPlaySVGExperienceForDrawing |
| `DIFSheet`, `DIFLayout` | 3DPlayUDLExperiencePreview | 3DPlayUDLExperience |
| `Document`, `DOCUMENTS` | 3DSpaceDocumentConnector 가 문서의 파일을 보고 다시 고름 | 같음 |
| 목록에 없는 타입 | `tenant` 가 있으면 부모 타입을 찾아 다시 고름 | 같음 |

표는 SupportList 의 일부입니다. 아래 두 행은 연결 모듈 이름과 코드 흐름으로 본 동작입니다 <Badge type="danger" text="검증 필요" />. 볼 수 있는 타입인지 미리 알고 싶으면 [볼 수 있는지 먼저 확인하기](#볼-수-있는지-먼저-확인하기) 를 봅니다.

### options

| 키 | 값 | 설명 |
| --- | --- | --- |
| `loading` | `'autoplay'` | 스플래시를 누르지 않고 바로 불러옴. OOTB 대부분 |
| `loading` | 없음 | 스플래시(3DPlay 로고)를 보이고 누르면 불러옴 |
| `loading` | `'preload'` | 스플래시를 둔 채 화면을 미리 만듦 <Badge type="danger" text="검증 필요" /> |
| `callbacks` | `{ experience: {...}, asset: {...} }` | [이벤트](#이벤트-options-callbacks) |
| `splashscreen` | `{ customLogoSrc, customAppName, customBgColor, logoDisplay, custom }` | 스플래시 모양. `custom` 은 HTML 문자열이나 요소 |
| `tenant` | 플랫폼 id | SNResultUX 가 넣음 |
| `ctx` | 컨텍스트 id | 최상위 `ctx` 와 같음 |

## 담는 요소

3DPlay 는 받은 요소 안에 스플래시와 화면을 넣습니다. 요소에 크기가 있어야 합니다. OOTB(EditPropWidget)는 담는 요소에 `position: relative; width: 100%; height: 100%` 를 주고 부모에서 높이를 정합니다. 샘플은 `position: relative; height: 420px` 입니다.

## 돌려받은 객체

| 메서드 | 설명 |
| --- | --- |
| `isReady()` | `ExperienceReady` 뒤 `true`. 만든 지 10 초가 지나면 준비와 상관없이 `true` 가 됨 |
| `getExperience()` | experience 객체. 준비 전에는 `undefined` |
| `getCtx()` | 컨텍스트 id. 2025 는 experience 가 생긴 뒤에만 값이 있음 |
| `load({ input, options })` | 다른 대상으로 바꾸기 ([바꾸기와 닫기](#다른-객체로-바꾸기와-닫기)) |
| `loadAsset(input, { clearSceneGraph })` | 지금 화면에 대상을 더 불러옴 (XCT3DPlayIntegratability). 둘째 인자에 `true` / `false` 를 주던 옛 방식은 경고를 찍음 |
| `removeAsset(대상)` | 불러온 대상 빼기 (experience 의 `remove` 를 부름) |
| `informActiveState(true 또는 false)` | 화면이 보이는지 알림. 패널을 보이거나 숨기고 `3DPLAY/3DPLAYPREVIEW/ENABLE` / `DISABLE` 를 publish. E6WCommonAppsLightbox 가 열 때 true, 닫을 때 false |
| `dispose()` | 정리 (아래 주의) |
| `loadAnnotations({ annotation }, { clean })`, `getAnnotations()` | 주석 불러오기, JSON 으로 받기 |

experience 객체에는 `play`, `pause`, `getScreenShot`, `clearView`, `getScenarioManager` 같은 메서드가 있지만 화면 종류마다 달라 이 페이지에서는 다루지 않습니다.

## 이벤트 (options.callbacks)

| 묶음 | 이름 | 언제, 인자 |
| --- | --- | --- |
| `experience` | `ExperienceReady` | 화면 준비 끝. 인자는 experience 객체 |
| `experience` | `ExperienceSplashScreen` | 스플래시 상태가 바뀔 때 |
| `experience` | `Error` | 오류 |
| `experience` | `Notif` | 알림. `{ event, msg }` (`event` 는 `'fatal'`, `'error'`, `'warning'` ...) |
| `experience` | `Dispose` | 정리될 때 |
| `asset` | `LoadingStarted` | 불러오기 시작 |
| `asset` | `LoadingProgress` | 진행. `{ model, progress }` |
| `asset` | `LoadingFinished` | 다 불러옴. `{ model, time, ctx, loadAssetInfo }` (`time` 은 ms) |
| `asset` | `LoadingError` | 실패. `{ ctx, ... }` |

- 이름 목록은 3DPlaySyndication.html 의 디버그용 콜백(`debugplayer`)과 같습니다.
- 3DPlay 는 키를 대문자로 바꿔 `DS/CoreEvents/Events` 에 `/<ctx>/EXPERIENCE/EXPERIENCEREADY/` 같은 이름으로 구독합니다. 그래서 대소문자는 가리지 않습니다. `getCtx()` 로 ctx 를 얻으면 [Events](/events/pubsub) 로 직접 구독해도 됩니다.
- OOTB 일부(SNResultUX, FolderEditor)는 `model: { LoadingError }` 도 넘기지만, 일반 화면에서 `MODEL/LOADINGERROR` 를 내는 코드는 찾지 못했습니다. 불러오기 실패는 `asset.LoadingError` 로 받습니다.
- 3DPlay 는 `3DPLAYREADY`, `3DPLAYERROR`, `3DPLAYLOADINGFINISHED` CustomEvent 도 `window.top.document` 에 보냅니다 (`detail.helper`). 최상위 창의 출처가 다르면 try/catch 로 건너뛰므로 위젯에서는 콜백을 씁니다.

## 다른 객체로 바꾸기와 닫기

| 방법 | 동작 | OOTB |
| --- | --- | --- |
| `dispose()` → 요소 비우기 → 새로 만들기 | 가장 단순. 샘플도 이 방식 | EditPropWidget, FolderEditor |
| `load({ input, options })` | 같은 experience 로 볼 수 있으면 그 안에서 바꾸고, 아니면 정리한 뒤 같은 요소에 다시 만듦. 준비 전(`isReady()` 가 false)이면 input 만 바꿔 둠 | SNResultUX, ENOAttachmentsPreviewWebInWin |

::: warning 준비 전에 dispose 하면 정리되지 않습니다
`dispose()` 는 `isReady()` 가 true 이고 experience 가 있을 때만 experience 를 정리합니다. 그 전에 부르면 콘솔에 `Experience is not ready` 를 찍고 스플래시만 지웁니다. 사용자가 객체를 빠르게 바꾸는 화면이면 `ExperienceReady` 를 받은 뒤 다음 것을 열고, 위젯을 닫을 때도 준비가 끝난 player 만 `dispose()` 합니다.
:::

## 볼 수 있는지 먼저 확인하기

SNResultUX 는 3D 화면을 보이기 전에 `DS/3DPlaySupport/AssetSupport` 로 볼 수 있는 타입인지 확인합니다.

```js
require(['DS/3DPlaySupport/AssetSupport'], function (AssetSupport) {
  AssetSupport.checkSupport(asset, {}, function (supported) {
    if (supported) {
      showPreview(state, viewer, asset);
    } else {
      viewer.setText('3D 로 볼 수 없는 타입입니다');
    }
  });
});
```

- `asset` 은 `Player3DPlayWeb` 에 넘길 것과 같은 모양입니다 (SNResultUX 는 `provider`, `tenant`, `serverurl`, `physicalid`, `dtype`, `requiredAuth`, `requestsOptions`).
- 결과는 콜백으로 옵니다. 타입에 맞는 experience 를 찾으면 `true` 입니다. `requestsOptions.authentication` 이 `'passport'` 이고 타입이 Office 형식(DOCX, XLSX, PPTX ...)이면 바로 `false` 입니다.

## iframe 으로 띄우기 (3DPlaySyndication.html)

DocumentPreview 는 `Player3DPlayWeb` 을 직접 만들지 않고 3DPlay 전용 페이지를 iframe 으로 엽니다.

```js
const params = { input: { asset: asset }, options: { loading: 'autoplay' } };
const base = WebappsUtils.getProxifiedWebappsBaseUrl() || WebappsUtils.getWebappsBaseUrl();
UWA.createElement('iframe', {
  src: base + '3DPlayHelper/3DPlaySyndication.html?params=' + encodeURIComponent(JSON.stringify(params)),
  styles: { position: 'absolute', width: '100%', height: '100%', border: '0' }
}).inject(container);
```

- 페이지가 AmdLoader 와 UWA standalone 을 스스로 읽고, `params` 를 JSON 으로 풀어 페이지 안의 `canvas-div` 에 `Player3DPlayWeb` 을 만듭니다.
- 주소에 `autoplay=true` 를 붙이면 `loading: 'autoplay'` 와 같습니다. `params` 에 `debugplayer: true` 를 넣으면 모든 콜백을 콘솔에 찍습니다.
- 함수는 JSON 으로 넘길 수 없으므로 콜백을 받을 수 없습니다. 보여 주기만 할 때 씁니다.
- 부모 창에 `widget` 이 있으면 `lang`, `x3dPlatformId`, `appId` 를 iframe 쪽 widget 에 복사합니다.
- 주소는 [WebappsUtils](/amd/webapps-utils) 로 만듭니다. `getWebappsBaseUrl()` 은 끝에 `/` 가 붙어 있습니다.

## 3DPlay 앱으로 열기 (TransientWidget)

3DPlay 앱을 대시보드 창으로 열고 볼 객체를 넘깁니다. 3DSpace 주소는 넘기지 않아도 됩니다.

<<< @/../WidgetSample/3DPlaySample.html#transient{js}

- `X3DPLAW_AP` 는 3DPlay 웹 앱의 앱 id 입니다 (3DPlayApp/assets/json/CAT3DPLAW_AP.idcard.json). 비슷한 `X3DPLAY_AP` 는 iOS 기기에서 3DPlay 네이티브 앱을 띄울 때 쓰는 id 입니다 (3DPlayNativePlayer).
- 3DPlay 앱(`DS/3DPlayWidget/W3DPlayPrefsCtrl`)은 `widget.getValue('X3DContentId')` 를 JSON 으로 풀어 `data` 가 있으면 그 내용을 열고 값을 지웁니다. 이미 저장된 3DPlay 내용(`W3DPlayContent` 설정)이 있으면 쓰지 않습니다.
- 3DXContent 의 키는 [3DXContent 형식](/events/drag-and-drop#_3dxcontent-형식) 을 봅니다. OOTB 는 `version: '0.1'`, `source` 에 자기 앱 id 를 넣습니다.
- DMUPlayMarker, CAT3DAnnotationUtils 는 `x3dSharedId`, `x3dPlatformId` 도 자기 위젯 값으로 같이 넘깁니다. 3DPlay 쪽에서 이 두 값을 어떻게 쓰는지는 확인하지 않았습니다 <Badge type="danger" text="검증 필요" />.
- 창 모양 옵션(`mode`, `width`, `height`)과 닫기는 [TransientWidget](/widget/transient-widget) 과 같습니다.

## 3DDashboard 밖에서

| 기능 | 단독 실행 |
| --- | --- |
| `Player3DPlayWeb` | `widget` 이 없어도 만들어짐 (`typeof widget` 으로 확인). `serverurl`, `tenant` 는 직접 넣어야 함. 3DSpace 데이터를 passport 인증으로 읽는 부분까지 동작하는지는 확인하지 않음 <Badge type="danger" text="검증 필요" /> |
| 3DPlaySyndication.html | AmdLoader 와 UWA standalone 을 스스로 읽는 페이지. 대시보드 밖에서 그려지는지는 위와 같음 <Badge type="danger" text="검증 필요" /> |
| TransientWidget 로 3DPlay 앱 열기 | 메시지를 받을 대시보드가 없어 아무 일도 없음 ([동작 방식](/widget/transient-widget#동작-방식)) |
| `window.top` 으로 보내는 CustomEvent | try/catch 안이라 오류 없이 건너뜀 |

## 같이 보이는 모듈

| 모듈 | 내용 | 직접 쓸 일 |
| --- | --- | --- |
| `DS/3DPlay/3DPlay` | 3DPlay 커널 싱글톤. `createExperience`, `getExperience(ctx)`, `publish(ctx, 이름, 값)`, `subscribe(ctx, 이름, fn)`, `getVersion()`. 23 개 앱 | ctx 단위 이벤트를 직접 구독할 때 |
| `DS/3DPlaySupport/AssetSupport`, `DS/3DPlaySupport/SupportList` | 지원 여부, 타입별 experience 목록 | [지원 확인](#볼-수-있는지-먼저-확인하기) |
| `DS/3DPlayConnectors/3DPlayInputPreProcessor` | input 정리 (provider 기본값, 필수값 검사) | 없음. `Player3DPlayWeb` 이 부름 |
| `DS/3DPlayExperienceModule/PlayExperience3D` | experience 의 기반 클래스 (16 개 앱이 씀) | 없음 |
| `DS/3DPlayWidget/Widget3DPlay` | 3DPlay 앱 화면 전체 (모델, 뷰, 컨트롤러) | 없음. 3DPlayApp, 3DPlayAppV2 가 씀 |
| `DS/3DPlayUtils/*` | 3DPlay 화면 보조 (`ContextualBar`, `FullScreen`, `PanelManager`, `ServiceURL` ...) | 없음 |
| `DS/3DPlayWidgetViews/W3DPlayLightboxView` | vuekit 의 `vu-lightbox` 로 만든 3DPlay 라이트박스 ([Vue](/amd/libraries#vue)) | 없음 |

## 2026 에서 달라진 것

| 항목 | 2025 | 2026 |
| --- | --- | --- |
| `Player3DPlayWeb` 사용법 | | 같음. `getCtx()` 가 experience 가 생기기 전에도 ctx 를 돌려줌. 의존성에 `DS/3DPlayWAFRMigration/WAFR2Utils`, `DS/3DPlaySupport/ExperiencesList` 가 더해짐 |
| `DS/3DPlayHelper/3DPlayHelper` 호출부 | 17 개 앱 | 16 개 앱. 3DPlayApp, InstantMessagingW 가 빠지고 FolderEditor 가 들어옴 |
| 3DPlay 앱 본체 | `3DPlayApp/3DPlayApp.js` 한 파일 (약 390 KB, 여러 모듈 묶음) | `3DPlayApp.js` 는 약 8 KB 의 껍데기(`DS/WAfrContainer/App` 기반). 새 폴더 `3DPlayAppV2` (`DS/3DPlayAppV2/3DPlayAppAFR`, `DS/WAfrAppManager/App` 기반) |
| 앱 정의 | `CAT3DPLAW_AP.idcard.json` (afrCode `DS/3DPlayApp/3DPlayApp`) | 같은 idcard 에 더해 `WebAfr/Applications/X3DPLAW_AP.json` (afrCode `DS/3DPlayAppV2/3DPlayAppAFR`, experience 별 specialization 5 개) |
| 앱 id, 여는 방법 | `X3DPLAW_AP`, `X3DContentId` | 같음 (DMUPlayMarker, CAT3DAnnotationUtils 도 그대로) |
| 3DPlaySyndication.html | | iOS 앱 설정(`myAppConfig.appId` 가 `'3DPlay_IOS'`)이면 라이트박스를 씌움 |
| 새 폴더 | | 3DPlayWAFRMigration, 3DPlayMagicSysMLv2Experience |

2026 의 두 앱 정의 중 대시보드가 실제로 어느 쪽을 쓰는지는 확인하지 않았습니다 <Badge type="danger" text="검증 필요" />. 위젯에서 `Player3DPlayWeb` 을 쓰거나 `X3DPLAW_AP` 를 여는 코드는 두 버전에서 바꿀 것이 없습니다.
