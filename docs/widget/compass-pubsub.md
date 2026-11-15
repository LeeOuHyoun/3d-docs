# Compass 에 선택 객체 알리기 (i3DXCompassPubSub) <Badge type="info" text="원본 분석" />

`DS/i3DXCompassServices/i3DXCompassPubSub` 는 위젯과 3DDashboard 의 **Compass(앱 나침반)** 사이 메시지 통로입니다. 위젯은 지금 고른 객체를 Compass 에 알리고(`setObject`, `setX3DContent`), 앱 실행을 부탁하고(`launchApp`), 그 응답을 받습니다. 사용자가 Compass 에서 앱을 누르면 Compass 는 알려 둔 객체를 그 앱에 넘깁니다.

::: info 근거
i3DXCompassServices/i3DXCompassServices.js (`DS/i3DXCompassServices/i3DXCompassPubSub`: `publish`, `subscribe`, `unsubscribe`), i3DXCompass/i3DXCompass.js (Compass 쪽: `initialize` 의 `subscribe("setObject" ...)`, `ae.setObject`, `ae.setTypes`, `ae.setX3DContent`, `ae.launchApp`, `handleLaunchAppEvent`, `Model/App` 의 `launch`, `_launchTransient`), MessageBus/MessageBus.js, i3DXCompassPlatformServices.js (`OpenWith._launchApp`), TopFrame/TopFrame.js (`_initCompass`). 모듈을 쓰는 앱 41 개. 호출부: W3DDrive, Foundation, FolderEditor, E6WCommonUI (`CompassUtil`), ENORIPE_Relations, ENOChgServices, CollaborativeTasks, HistoryExplorer, LifecycleWidget, SNInfraUX, SNNavigationMap. 모듈을 쓰는 파일 안의 `.publish("주제"` 집계: `launchApp` 24, `setObject` 9, `setX3DContent` 9, `setTypes` 8, `resetX3DContent` 5, `setStructure` 4, `resetObject` 4, `resetTypes` 3. `.subscribe(`: `setTypesCallback` 7, `launchAppCallback` 7
:::

샘플: [CompassSample.html](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/WidgetSample/CompassSample.html) (행을 고르면 `setObject` / `setX3DContent`, 모두 풀면 `resetObject` / `resetX3DContent`, `launchApp` 과 응답). 3DDashboard 에서만 동작합니다. 단독 실행에서는 받는 Compass 가 없어 아무 일도 일어나지 않습니다.

## 부르는 법

```js
require(['DS/i3DXCompassServices/i3DXCompassPubSub'], function (CompassPubSub) {
  CompassPubSub.publish('setObject', {
    envId: widget.getValue('x3dPlatformId'),
    serviceId: '3DSpace',
    contextId: securityContext,
    objectId: physicalId,
    objectType: 'VPMReference'
  });

  CompassPubSub.publish('launchApp', { appId: 'XXXXX_AP', widgetId: widget.id }, function (result) {
    console.log(result.response);
  });
});
```

모듈은 클래스가 아니라 객체 하나입니다. `new` 하지 않습니다.

| 메서드 | 설명 |
| --- | --- |
| `publish(topic, data?, callback?)` | 채널 `com.ds.compass` 로 보냄. `callback` 을 주면 `topic + 'Callback'` 을 구독하고 `data._PUB_SUB_ID` 에 UUID 를 넣어, 같은 id 의 응답이 오면 한 번 부르고 그 구독을 해제 |
| `subscribe(topic, fn)` | 받기. `fn(data)`. 돌려주는 값이 없음 |
| `unsubscribe(topic)` | 그 주제의 구독 해제. **다른 위젯 것까지** ([주의](#주의)) |

## 위젯이 보내는 주제

| 주제 | 데이터 | Compass 가 하는 일 | OOTB |
| --- | --- | --- | --- |
| `setObject` | 객체 하나 ([형식](#객체-형식)) | 보관. 이전 값과 다르면 Compass 에 "Your selection has been updated" 를 5 초 보임. 걸려 있던 `setTypes`, `setStructure` 해제. 앱을 실행할 때 씀 ([아래](#compass-가-객체로-하는-일)) | W3DDrive, Foundation, FolderEditor, E6WCommonUI, HistoryExplorer |
| `resetObject` | 없음 | 보관한 객체를 지움 | W3DDrive, Foundation, LifecycleWidget |
| `setX3DContent` | 3DXContent ([형식](#_3dxcontent)) | 보관하고 같은 값을 `setX3DContentCallback` 으로 보냄. 앱을 실행할 때 씀 | W3DDrive, E6WCommonUI, CollaborativeTasks, ENORIPE_Relations, SNResultUX |
| `resetX3DContent` | 없음 | 비움 | W3DDrive, LifecycleWidget, Issue3DReview |
| `launchApp` | `{ appId, widgetId, origin, silent, fileName, fileContent }` | 그 앱 실행, 결과를 `launchAppCallback` 으로 ([앱 실행](#앱-실행-launchapp)) | CollaborativeTasks, SNInfraUX, FolderEditor 등 24 건 |
| `setTypes` | 아무 객체. OOTB 는 `{ widgetId }` | 보관, 걸려 있던 객체 / 구조 해제. 사용자가 Compass 에서 **네이티브 앱** 을 실행하면 `setTypesCallback` 을 보냄 ([네이티브 앱](#네이티브-앱에-선택-넘기기-settypes)) | ENOChgServices, FolderEditor, ENORIPE_Relations, SNNavigationMap |
| `resetTypes` | 없음 | 해제 | ENORIPE_Relations, SNNavigationMap, ENXNav |
| `setStructure` / `resetStructure` | `{ structure: 문자열 }` | 보관 / 해제 (걸려 있던 객체, 타입 해제) | ENO3DView, ENOPAD3DViewer, PADUtils, IPClassifyUXUtil |
| `installApp`, `detectInstallApp` | `{ appId, widgetId, silent }` | 설치 / 설치 확인, 결과를 `installAppCallback`, `detectInstallAppCallback` 으로 | W3DDrive |
| `search` | `{ query }` | Compass 를 열고 검색창에 넣음 | SNInfraUX |

Compass 는 이 밖에 `resetQuadrant`, `activatePlay`, `resetPlay` 도 받습니다.

## 위젯이 받는 주제

| 주제 | 데이터 | 언제 |
| --- | --- | --- |
| `launchAppCallback` | `{ widgetId, appId, response, origin, _PUB_SUB_ID }` | `launchApp` 처리 뒤. `response` 는 `'COMPLETE'` 또는 오류 이름 |
| `setTypesCallback` | `{ appId, ...setTypes 로 보낸 값 }` | `setTypes` 가 걸린 동안 사용자가 Compass 에서 네이티브 앱을 실행한 뒤 |
| `setX3DContentCallback` | 보낸 3DXContent | `setX3DContent` 를 받은 직후 |
| `installAppCallback`, `detectInstallAppCallback` | 보낸 값 (+ 실패 정보) | 설치 처리 뒤 |
| `compassPanelOnShow` / `compassPanelOnHide` | `{}` | Compass 창이 열림 / 닫힘 |

## 객체 형식

`setObject` 에 넣는 객체입니다. 3DXContent 의 `items` 항목도 같은 모양입니다 ([OpenWith](/widget/compass-services#openwith) 의 항목과 같음).

| 키 | Compass 가 씀 | 설명 |
| --- | --- | --- |
| `objectId` | O | physicalid. 없으면 Compass 가 앱 목록에 넘기지 않음 (로그 `Object is not valid for setObject`) |
| `envId` | O | 플랫폼 id (`widget.getValue('x3dPlatformId')`, [플랫폼 id](/widget/space-url)). `cstorage` 로 주면 `envId` 로 옮김 |
| `serviceId` | O | `'3DSpace'`, `'3DDrive'` ... 없으면 콘솔 경고 (`setObject without key: serviceId is deprecated`) |
| `contextId` | O | SecurityContext. `cspace` 로 주면 `contextId` 로 옮김. Foundation 은 `ctx::` 를 뗀 값을 넣음 |
| `objectType` | O | 타입 이름 |
| `displayName`, `displayType`, `displayIcon`, `displayPreview`, `objectRootId` | | 보관만 함 (W3DDrive 가 넣음) |

<<< @/../WidgetSample/CompassSample.html#item{js}

### 3DXContent

```js
{
  protocol: '3DXContent',
  version: '2.0',
  source: widget.getValue('x3dAppId'),
  widgetId: widget.id,
  data: { items: [item1, item2] }
}
```

W3DDrive 는 3DSpace 위젯일 때 `version: '1.0'`, `source: 'X3DCSMA_AP'`, 아니면 `version: '2.0'`, `source: widget.data.appId` 를 씁니다. E6WCommonUI (`CompassUtil.openCompassWithModel`) 는 3DXContent 하나로 `setObject(items[0])` 과 `setX3DContent(content)` 를 같이 보냅니다.

## Compass 가 객체로 하는 일

사용자가 Compass 에서 앱을 누를 때(또는 `launchApp` 을 받을 때) 보관한 값을 씁니다 (i3DXCompass.js `Model/App`).

| 실행하는 앱 | Compass 동작 |
| --- | --- |
| 여러 플랫폼에 있는 앱 | `envId` 플랫폼의 실행 정보를 고름 |
| 네이티브 앱 | 실행 명령에 `-Ctx "contextId"`, `-object objectId`, `-serviceId serviceId`, `-objectType "objectType"`, `-e CATUNILoadFromWebCmdHdr Open` 을 붙임. `setObject` 값이 없으면 3DXContent 의 첫 항목을 씀 |
| 3DSpace 웹 앱 (`_launchTransient`) | 주소에 `SecurityContext=contextId&objectId=objectId` 를 붙임 |
| 3DDashboard 위젯 앱 | 주소 끝에 `#content:X3DContentId=` + 3DXContent JSON 을 붙임 (3DXContent 가 있을 때). 열린 앱은 위젯 값 `X3DContentId` 로 받는 것으로 보입니다 <Badge type="danger" text="검증 필요" /> ([위젯 설정](/widget/preferences#플랫폼이-넣어-주는-값)) |

- Compass 는 앱 목록을 객체 타입으로 거르지 않습니다. i3DXCompass.js 에서 `setObject` 값으로 목록을 거르거나 표시를 바꾸는 코드는 찾지 못했습니다. 이 객체를 열 수 있는 앱만 보여 주려면 [OpenWith](/widget/compass-services#openwith) 를 씁니다.
- 그래서 `setObject` 는 "사용자가 Compass 를 열어 앱을 누르면 이 객체를 넘겨 달라" 는 뜻입니다. 위젯이 앱을 고르지 않습니다.

## 선택이 바뀔 때

<<< @/../WidgetSample/CompassSample.html#select{js}

- W3DDrive 가 이 순서입니다 (`inodePaneView:onChangeSelection`: 선택이 없으면 `resetObject` + `resetX3DContent`, 있으면 `setObject` + `setX3DContent`).
- Foundation 은 행을 고를 때마다 그 행을 `setObject` 하고, 행을 풀면 남은 행 중 가장 나중에 고른 행으로 다시 `setObject` 합니다. 모두 풀면 `resetObject` 입니다.
- 선택이 풀릴 때 `resetObject` 를 보내지 않으면 Compass 는 예전 객체를 계속 들고 있다가 다음 앱 실행 때 넘깁니다.

## 앱 실행 (launchApp)

<<< @/../WidgetSample/CompassSample.html#launch{js}

| 키 | 설명 |
| --- | --- |
| `appId` | 필수. 사용자의 Compass 앱 목록에 있는 앱 id |
| `widgetId` | 응답에 그대로 돌아옴. 내 요청의 응답인지 가릴 때 씀 |
| `origin` | 응답에 그대로 돌아옴. OpenWith 는 `'openWith'` |
| `silent` | 설치 정보를 확인할 때 넘기는 값. 안내 없이 처리하는 것으로 보입니다 <Badge type="danger" text="검증 필요" /> |
| `fileName`, `fileContent` | 둘 다 있으면 네이티브 앱을 띄울 때 파일로 넘김 (`startV6install` 의 `file`). OOTB 는 `fileName: '-file'` 과 JSON 문자열 |

- 객체를 열며 실행하려면 `setX3DContent` (와 `setObject`)를 먼저 보냅니다. CollaborativeTasks 는 `setX3DContent` 뒤에 `launchApp({ appId })` 를 보냅니다.
- 응답 `response`: `'COMPLETE'` 외에 `no_app_id`, `no_app` (목록에 없는 앱), `app_is_a_role`, `no_mode_selected`, `no_launch_infos`, `no_install_selected`, `no_install_compatible_online_installation`, `no_install_compatible_no_online_installation`, `mandatory_update_online_installation`, `ios_app_not_installed`, `android_app_not_installed`, `applet_error`, `unable_install_data`.
- SNInfraUX 는 `result.response === 'COMPLETE'` 로 성공 / 실패 알림을 냅니다.

## 네이티브 앱에 선택 넘기기 (setTypes)

선택을 파일 내용으로 만들어 네이티브 앱(CATIA 같은 데스크톱 앱)에 넘기는 방식입니다. ENOChgServices, ENORIPE_Relations, FolderEditor, SNNavigationMap 이 씁니다.

```text
위젯: subscribe('setTypesCallback', fn)  (한 번만)
위젯: 선택이 있으면 publish('setTypes', { widgetId: widget.id }), 없으면 publish('resetTypes')
사용자: Compass 에서 네이티브 앱을 누름
Compass: 앱을 실행하고 publish('setTypesCallback', { appId, widgetId })
위젯 fn: widgetId 가 내 것이면 publish('launchApp', { appId, widgetId, fileName: '-file', fileContent })
```

```js
CompassPubSub.subscribe('setTypesCallback', function (e) {
  if (e.widgetId !== widget.id) { return; }
  CompassPubSub.publish('launchApp', {
    appId: e.appId,
    widgetId: e.widgetId,
    fileName: '-file',
    fileContent: JSON.stringify({
      version: '2.1',
      results: [{ id: '1', phid: physicalId }],
      structures: [{ structure: [{ id: '1', show: true, select: true, children: [] }] }]
    })
  });
});

CompassPubSub.publish('setTypes', { widgetId: widget.id });
```

- `setTypesCallback` 은 Compass 가 네이티브 앱을 실행했을 때만 보냅니다. 웹 앱이나 위젯 앱을 누르면 오지 않습니다.
- `fileContent` 형식은 받는 네이티브 앱이 정합니다. OOTB 는 `version` `'2.1'` (ENORIPE_Relations), `'2.4'` + `completionOnCAD: 'yes'` (ENOChgServices, SNNavigationMap) 의 `results` / `structures` 모양을 씁니다. 키 전체의 뜻은 webapps 에 없습니다 <Badge type="danger" text="검증 필요" />.

## OpenWith 와의 관계

[OpenWith](/widget/compass-services#openwith) 는 이 PubSub 위에 만든 메뉴 도구입니다.

| OpenWith 동작 | 안에서 보내는 것 |
| --- | --- |
| 호환 앱 항목을 누름 | `setObject(items[0])` (`objectType` 이 배열이면 그 앱과 맞는 하나) + `setX3DContent(content)` + `launchApp({ appId, origin: 'openWith' }, callback)`. callback 은 응답에 `error` 가 있으면 Compass 를 열고 안내하는데, Compass 응답은 `error` 가 아니라 `response` 키라서 이 안내는 나오지 않는 것으로 보입니다 <Badge type="danger" text="검증 필요" /> |
| "다른 앱" 을 누름 | Compass 를 열고 `setObject` + `setX3DContent` (types 모드면 `setTypes` 도) |
| `set3DXContent(content, true)` 후 네이티브 앱 항목을 누름 | `setTypes(content)` + `setTypesCallback({ appId, ...content })` 를 직접 보냄 |

| 하고 싶은 일 | 쓸 것 |
| --- | --- |
| 객체를 열 수 있는 앱 메뉴를 만들기 | [OpenWith](/widget/compass-services#openwith) |
| 사용자가 Compass 를 열었을 때 고른 객체로 앱 실행 | `setObject` + `setX3DContent` (선택이 풀리면 reset) |
| 정해진 앱을 바로 실행 | `setX3DContent` + `launchApp` |
| 네이티브 앱에 선택을 파일로 넘기기 | `setTypes` + `setTypesCallback` + `launchApp` (`fileContent`) |
| 다른 앱을 지금 화면 위에 창으로 | [TransientWidget](/widget/transient-widget) |

## 동작 방식

```text
publish(topic, data, callback)
  → top 창의 DS/MessageBus/MessageBus 를 top.require 로 꺼냄 (한 번 꺼내면 보관)
  → callback 이 있으면 subscribe(topic + 'Callback'), data._PUB_SUB_ID = UUID
  → MessageBus.publish({ channel: 'com.ds.compass', topic, data })
  → 데스크톱 앱 안 브라우저(dscef 가 있음)면 0.5 초 뒤 dscef.sendString(topic, JSON) 도 보냄
Compass (top 창. TopFrame 이 initialize)
  → 같은 MessageBus 에서 setObject, launchApp ... 을 subscribe 해 둠
```

- MessageBus 가 데이터를 `JSON.stringify` 하고 받는 쪽마다 `JSON.parse` 하므로 함수는 전달되지 않습니다. 넘긴 객체의 속성 값이 UWA Model / Collection 이면 그 자리에서 `toJSON()` 결과로 바꿔 넣습니다.
- `callback` 을 주면 **넘긴 객체 자체에** `_PUB_SUB_ID` 가 붙습니다. 같은 객체를 다시 보낼 때는 새로 만들어 보냅니다.
- 받는 함수는 비동기(`setTimeout 0`)로 불립니다.
- Compass 는 같은 이름의 InterCom 서버(`com.ds.compass`)도 엽니다. 받는 이벤트 이름은 `onSetObject`, `onResetObject`, `onLaunchApp`, `onSetX3DContent`, `onResetX3DContent`, `onSetStructure`, `onResetStructure`, `onShowAccordion`, `onHideAccordion` 입니다. ENORIPE_Relations, HistoryExplorer 는 이 서버에 [InterCom](/uwa/intercom) 소켓으로 보냅니다 (`subscribeServer('com.ds.compass', window.parent)`). 소켓은 실행 결과를 `launchApp` 이벤트로 받습니다 (HistoryExplorer).

::: info Compass 코드는 webapps 에 있습니다
Compass 자체(i3DXCompass)와 그것을 띄우는 최상위 창 코드(TopFrame)는 webapps 에 있어 위 동작을 원본으로 확인했습니다. 다만 TopFrame 에 넘기는 설정(대시보드 주소, 사용자 정보)은 3DDashboard 본체가 주는 것이라 webapps 에 없습니다.
:::

## 주의

::: warning 주제는 대시보드 전체가 같이 씁니다
- 모든 위젯과 Compass 가 top 창의 MessageBus 하나를 같이 씁니다. `setTypesCallback`, `launchAppCallback` 에는 다른 위젯 요청의 응답도 옵니다. `widgetId` 가 내 것인지 꼭 확인합니다 (OOTB 는 모두 확인함).
- `unsubscribe(topic)` 은 그 채널에서 이름이 맞는 구독을 **모두** 지웁니다 (`MessageBus.unsubscribe({ channel, topic })`). 다른 위젯의 구독도 사라집니다. `callback` 을 준 `publish` 도 응답을 받은 뒤 같은 방식으로 `topic + 'Callback'` 구독을 지웁니다.
- `subscribe` 는 해제용 객체를 돌려주지 않습니다. 위젯이 닫혀도 top 창에 구독이 남습니다. 오래 듣는 구독은 꼭 필요한 것만 둡니다.
:::

- top 창에 접근할 수 없으면(위젯이 다른 도메인에서 열림) `publish` / `subscribe` 는 아무 일도 하지 않고 오류도 내지 않습니다 (`top.document` 접근이 되는지 먼저 봄). 이때는 위의 InterCom 방식을 씁니다.
- 단독 실행에서는 top 이 자기 창이라 MessageBus 는 있지만 Compass 가 없어 아무도 받지 않습니다. `callback` 도 오지 않습니다.
- `setObject` 에 `objectId` 가 없으면 Compass 는 값을 보관만 하고 앱에는 넘기지 않습니다.
- 이름이 비슷하지만 이 모듈이 아닌 것:

| 이름 | 실제 |
| --- | --- |
| `onObjectSelected`, `selectedObjectsInContent`, `SelectFromSearch` | FolderEditor, LibraryEditor 가 `DS/IPClassifyUtil/IPClassifyCommunication` 으로 데스크톱 앱(`dscef.sendString`)에 보내는 메시지 |
| `com.ds.compass:onGrantedRole`, `com.ds.compass:massiveSynchroFinished`, `com.ds.compass:onExportFinished`, `com.ds.compass:onExportRolesFinished` | [PlatformAPI](/widget/platform-api) 주제 (i3DXCompass, PlatformManagement 가 `PlatformAPI.subscribe`) |

## 2026 변경

| 항목 | 내용 |
| --- | --- |
| `i3DXCompassPubSub` | 같음 (변수 이름만 다름). 여전히 `top` 창을 직접 씀 |
| 같은 파일의 `DS/i3DXCompassServices/i3DXCompassServices` | `getPlatformServices`, `getServiceUrl` (`caa: true` 로 부름), `getCompatibleApps({ content, onComplete })` 를 가진 도우미 (2025, 2026 모두 있음). 2026 은 최상위 창을 찾을 때 `window.dsDashboardWindow.top` 을 먼저 씀 <Badge type="warning" text="2026" /> ([Compass 서비스](/widget/compass-services) 와 같은 변경) |
| Compass 가 받는 주제와 처리 | 같음 (`setObject`, `launchApp`, `setTypes` ... 처리 코드 동일) |

## 관련 문서

- 서비스 주소, OpenWith: [Compass 서비스](/widget/compass-services)
- 위젯 사이 메시지 일반: [PlatformAPI](/widget/platform-api), [InterCom](/uwa/intercom), [PubSub](/events/pubsub)
- 같은 창 안 선택 목록: [선택 (CSOManager)](/events/selection)
- 6W 태그 창과 연동: [6W 태그 연동](/widget/tagger)
