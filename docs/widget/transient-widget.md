# 다른 앱을 창으로 열기 (TransientWidget) <Badge type="info" text="원본 분석" />

`DS/TransientWidget/TransientWidget` 는 **다른 3DDashboard 앱(위젯)을 지금 화면 위에 창으로 여는** 모듈입니다. OOTB 에서 "속성 보기", "변경 조치 열기", "사용자 프로필 보기" 처럼 다른 앱의 화면을 잠깐 띄울 때 씁니다. 내용을 직접 그리는 다이얼로그가 필요하면 [다이얼로그](/controls/dialog) 를 씁니다.

::: info 근거
TransientWidget/TransientWidget.js, Controller/Transient.js, Tool/Mapping.js, Tool/TransientMessaging.js (2025, 2026 모두 난독화되지 않은 원본). 모듈 이름 사용 54 파일. 호출부: ENOChangeActionUX (`ENOWCHA_AP`, `ENOWCHG_AP`), ENORouteMgmt (`ENXROUT_AP`), LifecycleControls (`ENOLCMT_AP`), CollaborativeTasks (`ENXBPPY_AP`), E6WCommonUI / NotifAlert / SocialToolbar (`X3DPRFL_AP`), ENOChgServices (`widget.getValue("TransientCAPid")`). 옵션 집계: `mode: "panel"` 15, `mode: "transient"` 12, `height` 800 / 700, `width` 650 ~ 800
:::

샘플: [TransientSample.html](https://github.com/LeeOuHyoun/3d-docs/blob/main/WidgetSample/TransientSample.html) (다른 앱 열기 / 닫기 / 숨기기, 자기 자신을 창으로 열기, 넘어온 값 읽기). 3DDashboard 에서만 창이 열립니다. 단독 실행에서는 화면만 뜨고 `showWidget` 은 아무 일도 하지 않습니다.

## 열기

```js
require(['DS/TransientWidget/TransientWidget'], function (TransientWidget) {
  TransientWidget.showWidget('ENOLCMT_AP', '', {
    SelectedItem: [{ objectId: physicalId, metatype: 'businessobject', source: '3DSpace', tenant: platformId }],
    x3dPlatformId: platformId,
    isTransient: 'true',
    loadData: true
  }, {
    mode: 'panel',
    width: 650,
    height: 800
  });
});
```

- 모듈은 클래스가 아니라 **이미 만들어진 객체 하나** 를 돌려줍니다. `new` 하지 않습니다.
- 첫 인자는 앱 ID(`XXXXX_AP`) 또는 위젯 주소입니다. OOTB 호출은 모두 앱 ID 입니다.
- 셋째 인자(`widgetData`)는 열리는 앱의 **위젯 설정 값** 이 됩니다. 열린 앱은 `widget.getValue('키')` 로 읽습니다 (ENOChangeActionUX 가 `{ TransientCAPid: id }` 로 열고 ENOChgServices 가 `widget.getValue("TransientCAPid")` 로 읽음).
- 무엇을 넘길지는 **열리는 앱이 정합니다.** OOTB 앱마다 키가 다르므로(`SelectedItem`, `TransientCAPid`, `login` / `url` ...) 같은 앱을 여는 OOTB 호출부를 찾아 키를 맞춥니다.
- 위 예의 `ENOLCMT_AP` (OOTB 정보 앱)는 `objectId` 키를 읽지 않고 `SelectedItem` 배열과 `X3DContentId` 만 봅니다 (EditPropWidget.js 의 시작 코드, LifecycleControls `EditPropDialog._launchTransientWidget`). 넘기는 값 표와 더 쉬운 방법(`EditPropCmdBase().launchProperties`)은 [OOTB 정보 창으로 열기](/controls/properties-panel#ootb-정보-창으로-열기) 에 있습니다.
- 3DPlay 앱(`X3DPLAW_AP`)은 `{ X3DContentId: JSON.stringify(3DXContent) }` 로 엽니다 (DMUPlayMarker, CAT3DAnnotationUtils). [3DPlay 앱으로 열기](/widget/3dplay#_3dplay-앱으로-열기-transientwidget) 를 봅니다.

| 인자 | 타입 | 설명 |
| --- | --- | --- |
| <code>widgetUrl</code> | <code>string</code> | 앱 ID 또는 위젯 주소 |
| <code>widgetTitle</code> | <code>string</code> / <code>{ widgetTitle }</code> | 제목. 대부분 <code>''</code> 로 두고 앱 제목을 씀 |
| <code>widgetData</code> | <code>object</code> / <code>string</code> | 열리는 앱의 설정 값, 또는 설정을 받을 주소 |
| <code>options</code> | <code>object</code> | 여는 모양 (아래 표) |

| 옵션 | OOTB 에서 쓰는 값 | 설명 |
| --- | --- | --- |
| <code>mode</code> | <code>'panel'</code>, <code>'transient'</code> | <code>'panel'</code> 은 크기를 준 창, <code>'transient'</code> 는 기본 미리보기 창으로 보입니다 <Badge type="danger" text="검증 필요" /> |
| <code>width</code> / <code>height</code> | 650 ~ 800 / 700, 800 | <code>mode: 'panel'</code> 과 같이 씀 (px) |
| <code>closeSearch</code> | 직접 주지 않음 | 주지 않으면 <code>showWidget</code> 이 채움 (아래) |

::: warning 실제로 창을 그리는 코드는 webapps 에 없습니다
`showWidget` 은 PlatformAPI 로 `ShowWidget` 메시지를 보내기만 합니다. 메시지를 받아 창을 그리는 쪽(3DDashboard 최상위 창의 TopFrameManager)은 3DSpace webapps 에 없어서, `mode` 값의 전체 목록과 창 모양은 원본으로 확인할 수 없습니다. 위 표는 OOTB 호출부에서 쓰는 값만 적었습니다.
:::

## 동작 방식

```text
showWidget(appId, title, data, options)
  → 사용자 설정을 읽어 options.closeSearch 를 정함
  → PlatformAPI.publish('ShowWidget', { appId, title, data, options })
  → TopFrameManager 가 아직 없으면 PubSub 'loadedTopFrameManager' 때 한 번 더 보냄
```

- 메시지 이름은 `Tool/Mapping.js` 에 있습니다: `ShowWidget`, `CloseWidget`, `Hide`, `Show`, `ResetWidget`, `AddModule(s)`.
- `options.closeSearch` 를 주지 않으면 사용자가 "새 탭에서 열기(전체 화면)" 설정을 켰는지로 정해집니다 (켜져 있지 않으면 `true`). 준 값은 그대로 둡니다 (`UWA.merge`). 2025 는 Me 설정 `OpenInNewTab = FullScreenExperience`, 2026 은 `DS/Dashboard/Utils.getAppUserPreferences({ appId })` 의 `preferences.openInNewTab` 을 봅니다. <Badge type="warning" text="2026" />
- Me 설정을 위젯에서 읽는 방법은 [언어와 사용자 환경설정](/widget/i18n-preferences#betaoptions-getmepreference) 에 있습니다.
- 3DDashboard 밖(단독 실행)에서는 메시지를 받을 곳이 없어 아무것도 열리지 않습니다.

## 닫기, 숨기기, 열려 있는지 확인

| 메서드 | 설명 |
| --- | --- |
| <code>closeWidget(appId?)</code> | 그 앱의 창을 닫음. 없으면 열린 창 |
| <code>hide()</code> / <code>show()</code> | 닫지 않고 숨김 / 다시 보임. 숨겨도 <code>isWidgetOpen()</code> 은 <code>true</code> |
| <code>isWidgetOpen(appId?)</code> | 창이 열려 있는지 |
| <code>isOpen</code> | 하나라도 열려 있는지 (위젯 안에서 부르면 최상위 창의 값) |

`isWidgetOpen` 은 부르는 위치에 따라 판단 방법이 다릅니다.

- 대시보드 최상위 창: `showWidget` / `closeWidget` 으로 기록한 앱 ID 목록을 봅니다.
- 위젯 안: **자기 자신이 미리보기 창인지** 를 봅니다 (`widget.id` 에 `preview` 가 들어 있는지). `appId` 를 주면 `widget.getValue('appId')` 가 같을 때만 `true` 입니다.

::: tip 이미 창 안에서 열린 경우
OOTB(E6WCommonUI 의 프로필 보기)는 자기 앱이 이미 미리보기 창으로 떠 있으면(`isWidgetOpen(widget.data.appId)`) 창을 또 열지 않고 `window.open(주소, '_blank')` 로 새 탭에 엽니다.
:::

## 예제

### 다른 앱 열기

<<< @/../WidgetSample/TransientSample.html#open{js}

### 창 안에서 또 열지 않기

<<< @/../WidgetSample/TransientSample.html#guard{js}

`widget.data.appId` 는 3DDashboard 에 앱으로 등록된 위젯에서 자기 앱 ID 입니다 (E6WCommonUI 가 같은 값을 씀). 등록하지 않고 Run Your App 으로 연 위젯에서 값이 있는지는 확인하지 않았습니다 <Badge type="danger" text="검증 필요" />.

### 창으로 열린 쪽에서 값 읽기

넘긴 값은 열린 앱의 위젯 설정 값이 됩니다. 받는 위젯은 `widget:preferences` 에 같은 이름의 `hidden` 설정을 정의해 두고 `widget.getValue` 로 읽습니다.

```html
<widget:preferences>
  <widget:preference name="sampleObjectId" type="hidden" defaultValue=""></widget:preference>
  <widget:preference name="sampleMessage" type="hidden" defaultValue=""></widget:preference>
</widget:preferences>
```

<<< @/../WidgetSample/TransientSample.html#receive{js}

설정에 정의하지 않은 키도 `getValue` 로 읽히는지는 확인하지 않았습니다 <Badge type="danger" text="검증 필요" />. OOTB 의 `TransientCAPid` 처럼 받는 앱이 키를 정해 두는 것이 안전합니다.

### 내용을 직접 그리는 창이 필요하면

TransientWidget 은 등록된 앱을 여는 것이라 DataGridView 같은 컨트롤을 직접 넣을 수 없습니다. 그리드를 넣은 창은 [다이얼로그 안에 DataGridView 넣기](/controls/dialog#다이얼로그-안에-datagridview-넣기) 를 봅니다.

## Dialog 와 고르는 기준

| 하고 싶은 일 | 쓸 것 |
| --- | --- |
| 다른 OOTB 앱 화면(속성, 변경 조치, 프로필)을 그대로 띄우기 | TransientWidget |
| 내 위젯을 다른 화면에서 창으로 띄우기 | TransientWidget (내 위젯이 3DDashboard 에 앱으로 등록되어 있어야 함 <Badge type="danger" text="검증 필요" />) |
| 확인 / 입력 / 목록 선택처럼 내용을 직접 그리기 | [Dialog](/controls/dialog) |
| OOTB 속성 화면을 창이 아니라 내 위젯 안에 넣기 | [속성 패널 (EditPropWidget)](/controls/properties-panel) |
| 성숙도를 바꾸는 OOTB 창 | TransientWidget 이 아니라 MaturityCmd ([성숙도 변경, 개정](/rest-api/lifecycle)) |
| 내 위젯에서 고른 객체로 다른 앱 실행 | [Compass 에 선택 객체 알리기](/widget/compass-pubsub) |
