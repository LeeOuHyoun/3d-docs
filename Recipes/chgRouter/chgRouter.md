# ChgRouter 로 Change Action 상세 화면 열기 (조사 메모)

Change 앱(`ENOChgGovernanceUX`) 위에 만든 위젯에서, 그리드의 링크로 Change Action(커스텀 하위 type `sdxECChangeAction` 포함) 상세 화면으로 넘어가는 방법을 조사한 메모입니다. Change 앱이 아닌 **다른 위젯** 의 링크로 Change 앱의 상세 화면을 여는 방법은 [6 절](#6-다른-위젯에서-change-앱의-상세-화면-열기) 입니다.

- 근거: enovia2025 / 2026 webapps 원본 (`ENOChgRouter5`, `ENOChgGovernanceUX`, `ENOChgServices`, `ENOChangeActionUX`). 2025 와 2026 이 같습니다.
- **실제 화면에서는 돌려 보지 않았습니다.** 확인할 것은 [7 절](#7-확인-필요) 에 있습니다.

## 1. ChgRouter 는 무엇인가

`DS/ENOChgRouter5/scripts/ChgRouter`. Change 앱이 위젯 안의 화면 전환을 관리하는 OOTB 싱글턴입니다. 경로 라이브러리 Router5 를 감쌌습니다. 권한 검사나 데이터 조회는 하지 않습니다.

| 메서드 | 하는 일 |
| --- | --- |
| `initialize(name, path)` | 라우터 생성. 기본 경로는 `home` (`/home`) |
| `addRoute(routes)` | 화면 경로 등록. 예: `{ name: 'home.CAInfo.CADetails', path: '/CADetails' }` |
| `addRouteMethods(name, { activate, deactivate })` | 그 화면에 들어올 때 · 나갈 때 함수 등록 |
| `start(path)` | 라우터 시작 |
| `navigate(name, params, options)` | 화면 전환. 들어오는 화면의 `activate` 가 `params` 를 받음 |
| `setDependency(name, obj)` / `getDependency(name)` | 화면끼리 공유할 객체를 이름으로 넣고 꺼냄 |
| `getRouter()` | 안의 Router5 객체 |

- `navigate` 하면 떠나는 화면의 `deactivate`, 들어오는 화면의 `activate` 가 불립니다. 실제로 화면을 그리는 것은 `activate` 입니다.
- 이름이 `home.CAInfo.CADetails` 처럼 계층이라, 하위 화면으로 가면 아직 활성이 아닌 상위 화면(`home.CAInfo`) 의 `activate` 도 불립니다.
- 등록 안 된 이름은 전환되지 않습니다 (`allowNotFound: false`).
- Change 앱이 시작할 때 넣어 두는 공유 객체:

  | 이름 | 내용 |
  | --- | --- |
  | `applicationChannel` | 앱 전체의 ModelEvents |
  | `xChangeApp` | 앱 객체 (`showIDCard`, `openIDCard`, `clearContent`, `deactivateWP` ...) |
  | `landingpage` | 첫 화면 컨테이너 |

- 싱글턴이라 어느 모듈에서 불러도 같은 라우터입니다. 단 Change 앱이 `initialize` 하기 전에는 `navigate` · `getDependency` 가 실패합니다. **Change 앱 위젯 안에서만** 씁니다. 다른 위젯에서는 [6 절](#6-다른-위젯에서-change-앱의-상세-화면-열기) 처럼 앱을 열어 넘깁니다.

## 2. Change Action 상세 라우트

| 상수 (`DS/ENOChgGovernanceUX/scripts/config/ChgGovAppGlobalConfig`) | 이름 |
| --- | --- |
| `ROUTE_CO_DETAILS` | `home.COInfo.CODetails` |
| `ROUTE_CR_DETAILS` | `home.CRInfo.CRDetails` |
| `ROUTE_CA_DETAILS` | `home.CAInfo.CADetails` |
| `ROUTE_IR_DETAILS` | `home.IRInfo.IRDetails` |

`ROUTE_CA_DETAILS` 의 `activate` 가 받는 값:

| 값 | 쓰는 곳 | 빼면 |
| --- | --- | --- |
| `nodeModel` | `getDependency('xChangeApp').showIDCard(nodeModel, mode)` 로 화면을 그림 | 화면이 안 그려짐 |
| `mode` | `showIDCard` 에 넘김. OOTB 는 바깥에서 열 때 `'outsideCall'` | — |
| `id` | `setRouteInfo` 로 저장 (`changeRequestId`). 위젯 새로고침 때 이 값으로 다시 엶 | 새로고침하면 복원 안 됨 |
| `name` | 브레드크럼 링크 글자 (`changeRequestName`) | 브레드크럼이 빈 글자 |
| `title` | 라우트 쪽에서 읽지 않음 | 영향 없음 (OOTB 는 늘 같이 넘김) |

`nodeModel` 은 `{ model }` 모양이고, `showIDCard` 는 `model.isKindOfType`, `model.id`, `model.attributes`, `model.maturity` 를 읽습니다. **id 만으로는 만들 수 없고** 서버에서 상세 정보를 받아 만들어야 합니다 (3 절).

## 3. id 로 Change Action 상세 열기

OOTB `ChgChangeActionUtil.displayObjectInfoInsideIDcard(id)` 가 하는 일을 풀어 쓴 것입니다.

```js
define('DS/MyApp/openChangeAction', [
  'DS/ENOChgRouter5/scripts/ChgRouter',
  'DS/ENOChgServices/scripts/services/ChgDataService',
  'DS/ENOChgServices/scripts/services/ChgDataProcess',
  'DS/ENOChgGovernanceUX/scripts/util/changeAction/ChgChangeActionUtil',
  'DS/ENOChgGovernanceUX/scripts/config/ChgGovAppGlobalConfig'
], (ChgRouter, ChgDataService, ChgDataProcess, ChgChangeActionUtil, ChgGovAppGlobalConfig) => {
  'use strict';

  return (physicalId) => ChgDataService.getChangeActionInfo(physicalId)
    .then((response) => ChgChangeActionUtil.createHeaderFieldsJSON({ model: ChgDataProcess.processChangeActionDetails(response)[0] }))
    .then((model) => ChgRouter.navigate(ChgGovAppGlobalConfig.ROUTE_CA_DETAILS, {
      id: model.id,
      name: model.name,
      title: model.title,
      nodeModel: { model },
      mode: 'outsideCall'
    }, { reload: true }));
});
```

| 단계 | 모듈 | 내용 |
| --- | --- | --- |
| 1 | `ChgDataService.getChangeActionInfo` | `GET /resources/v1/chguxservices/changeaction/<id>` |
| 2 | `ChgDataProcess.processChangeActionDetails` | 화면용 값으로 바꿈. `isKindOfType` 은 `"ChangeAction"` 고정 |
| 3 | `ChgChangeActionUtil.createHeaderFieldsJSON` | 머리글 칸 (소유자 · 협업공간 · 조직 이름 등) |
| 4 | `ChgRouter.navigate` | 상세 라우트로 전환 |

- 직접 쓸 이유가 없으면 OOTB 한 줄 `ChgChangeActionUtil.displayObjectInfoInsideIDcard(id)` 를 씁니다. 위 단계에 더해 실패 시 화면 가림 해제, Native 모드의 주소 · 브레드크럼 처리까지 합니다.
- 다른 객체는 같은 이름의 함수가 있는 모듈을 씁니다: `.../util/ChgChangeOrderHomeUtil` (CO), `.../util/changeRequest/ChgChangeRequestUtil` (CR), `.../util/investigationRequest/ChgInvestigationRequestUtil` (IR).

### 같은 일을 하는 다른 방법: `openIDCard`

`ChgRouter.getDependency('xChangeApp').openIDCard(id)` 도 상세 화면을 엽니다. 차이:

| | `displayObjectInfoInsideIDcard(id)` | `xChangeApp.openIDCard(id)` |
| --- | --- | --- |
| type 확인 | 안 함 (Change Action 고정) | 서버에 물음 (`/resources/v2/e6w/service/ApplicabilityTypeInfo/<id>`). `"Change Action"` 이어야 열림 |
| 라우터 | `navigate` 함 (화면 경로 · 브레드크럼 갱신) | 안 함 (그리기만) |
| OOTB 용도 | 목록 · 끌어 놓기 · 바깥에서 열기 | 새로고침, 같은 경로 재진입 |

커스텀 하위 type 에는 type 을 따지지 않는 `displayObjectInfoInsideIDcard` 쪽이 안전합니다.

## 4. 커스텀 하위 type (`sdxECChangeAction`)

- 상세 화면으로 가는 경로는 type 이름을 보지 않습니다 (`isKindOfType` 고정). 하위 type 도 Change Action 상세로 갑니다.
- 상세 화면 안 일부 기능은 type 이름을 `"Change Action"` 과 직접 비교합니다. 원래 type 이름을 받으면 그 분기를 건너뜁니다. 링크와 상관없이 OOTB 목록에서 열어도 같습니다.
  - 영향 분석 적용 범위 (`ChgChangeImpactsGridModel.setApplicabilityData`)
  - CO · CR 오케스트레이션 표 (`ChgOrchestrationDataGridModel`, `ChgCROrchestrationDataGridModel`)

## 5. DataGridView 링크 컬럼

`url` 셀의 값에 무엇을 넣느냐로 동작이 갈립니다 (셀 동작은 3d-docs 셀 타입 문서에서 로컬 실행으로 확인).

| 값 | 동작 |
| --- | --- |
| `{ label, url, parameters }` | `<a href="url + parameters">`. `hyperlinkTarget` 기본 `_blank` (새 탭). `_self` 는 위젯 iframe 이 통째로 바뀌고, `_top` 은 3DDashboard 전체가 바뀜 |
| `{ label, func }` | 누르면 `func({ context })`. `context.nodeModel` 이 누른 행 |

위젯 안 상세 화면으로 가려면 `func` 를 씁니다.

```js
{
  text: '설계변경',
  dataIndex: 'openChange',
  typeRepresentation: 'url',
  getCellValue: (cellInfos) => ({
    label: cellInfos.nodeModel.getAttributeValue('name'),
    func: ({ context }) => ChgChangeActionUtil.displayObjectInfoInsideIDcard(context.nodeModel.getAttributeValue('id'))
  })
}
```

## 6. 다른 위젯에서 Change 앱의 상세 화면 열기

### 6.1 직접 부를 수 없습니다

위젯마다 iframe 이 따로라서, 다른 위젯에서는 `ChgRouter` 나 `displayObjectInfoInsideIDcard` 를 부를 수 없습니다. 모듈을 `require` 해도 그 위젯 안에서 만든 화면이 없는 빈 객체를 받습니다. 부르면 아래처럼 실패합니다 (`ENOChgGovernanceUX.js`).

| 실패 지점 | 이유 |
| --- | --- |
| `ChgGovPropertiesPanel.start` 의 `n[1].children` | `document.querySelectorAll('.gov-triptych-wrapper')` 가 비어 있음 (3단 화면은 앱 첫 화면이 만듦) |
| `buildIDCardViewForCA` 의 `appendChild` | `.cogov-content-wrapper` 가 없음 |
| `displayObjectInfoInsideIDcard` | 앱이 `initialize` 한 라우터가 없음 |

### 6.2 앱을 열면서 id 를 넘깁니다

OOTB 도 이 방식입니다. `ENOChangeActionUX` 에서 대상 Change Action 링크(`.Realized-change-targetCA-value-span`)를 누르면 실행되는 코드입니다.

```js
require(['DS/TransientWidget/TransientWidget'], (TransientWidget) => {
  const open = (appId, physicalId) => {
    const platformId = widget.getValue('x3dPlatformId');
    TransientWidget.showWidget(appId, '', {
      X3DContentId: JSON.stringify({
        protocol: '3DXContent',
        source: widget.name,
        data: { items: [{ envId: platformId, serviceId: '3DSpace', objectId: physicalId }] }
      }),
      x3dPlatformId: platformId
    }, { mode: 'panel', width: 650, height: 800 });
  };

  open('ENOWCHG_AP', '<physicalid>');
});
```

| 값 | 설명 |
| --- | --- |
| `appId` | 열 앱의 3DDashboard 앱 ID. `ENOWCHG_AP` 는 Change Governance, `ENOWCHA_AP` 는 Change Execution |
| `platformId` | `widget.getValue('x3dPlatformId')`. 3DDashboard 가 넣어 주는 값이라 찾을 필요 없음. 온프레미스는 `'OnPremise'` 로 보임 ([플랫폼 id](../../docs/widget/space-url.md)). OOTB 는 `ChgServiceGlobalVariable` 의 `tenant` |
| `objectId` | physicalid |
| `mode: 'panel'`, `width`, `height` | OOTB 값 그대로 (창으로 뜸). `showWidget` 자체는 [TransientWidget](../../docs/widget/transient-widget.md) 에 있음 |

> **창이 뜨지 않고 CORS 에러만 날 때**
>
> 개발 서버에서 `showWidget` 을 불렀더니 화면에 변화가 없고, 콘솔에 `.../ENOWCHG_AP.json?... blocked by CORS policy` 와 `net::ERR_FAILED` 가 났습니다. 3DDashboard 가 창을 열기 전에 앱 설정 파일 `3dspace/webapps/config/<앱 ID>.json` 을 읽는데, 대시보드와 3DSpace 의 주소가 달라 브라우저가 막은 것으로 보입니다. 이 요청을 보내는 대시보드 코드는 webapps 에 없어 추정입니다. 코드로는 고칠 수 없고 서버 설정 문제입니다. 같은 서버에서도 [6.4 주소로 열기](#64-주소로-열기) 는 됐습니다.
>
> - `ENOWCHG_AP`, `ENOWCHA_AP` 는 이 설정 파일이 있고, `ENOLCMT_AP` 는 없습니다.
> - 콘솔의 `TopFrame#_onRightPanelStateChange is a @private and a @deprecated method.` 는 대시보드가 남기는 경고로, 이 문제와 상관없습니다.
> - Chrome 확장 프로그램(Allow CORS)으로는 풀리지 않았습니다.

### 6.3 받는 쪽이 하는 일

`ENOChgGovernanceUX` 의 `loadGovWidget` 이 앱을 시작할 때 처리합니다.

1. 위젯 값 `X3DContentId` 를 읽어 `data.items` 의 **마지막 항목** `objectId` 를 꺼냅니다.
2. `getObjectTypeInformation(id)` (`ENOChgServices`)로 type 을 서버에 묻습니다 (`/resources/v2/e6w/service/ApplicabilityTypeInfo/<id>` 응답의 `applicabilityType`).
3. 값이 `"Change Order"`, `"Change Request"`, `"Change Action"`, `"Investigation Request"` 중 하나면 `openWithIDCard` 가 `launchPropertyPanelContext({ context: id })` 와 그 type 의 `displayObjectInfoInsideIDcard(id)` 를 부릅니다.
4. 그 밖의 값이면 "접근할 수 없는 객체" 알림과 함께 첫 화면으로 갑니다.

Change Execution 앱(`ENOWCHA_AP`)은 2025 에서 `X3DContentId` 의 마지막 항목 또는 `TransientCAPid` 값을 읽고 `IsChangeAction(id)` 가 참이면 ID 카드를 엽니다. 2026 에서는 이 받는 코드를 찾지 못했습니다.

### 6.4 주소로 열기

OOTB 의 **Copy Link** 가 만드는 주소입니다. Change Governance 목록에서 행을 오른쪽 클릭하면 나오는 메뉴입니다 (`ENOChgServices` 의 `getCopyLink`, `makeCopyLinkObject`). 복사한 주소를 새 브라우저 탭에서 열면 그 객체의 ID 카드가 열리는 것을 개발 서버에서 확인했습니다.

```text
<3DDashboard 주소>#app:<앱 ID>/content:X3DContentId=<encodeURIComponent(JSON)>
```

JSON 은 `{ protocol: '3DXContent', data: { items: [{ envId, objectId, openLink: true }] } }` 입니다. 내 위젯의 링크에서는 같은 주소를 만들어 새 탭으로 엽니다.

```js
require(['DS/i3DXCompassPlatformServices/i3DXCompassPlatformServices'], (CompassPlatformServices) => {
  const buildLink = (dashboardUrl, appId, physicalId) => `${dashboardUrl}#app:${appId}/content:X3DContentId=${encodeURIComponent(JSON.stringify({
    protocol: '3DXContent',
    data: { items: [{ envId: widget.getValue('x3dPlatformId'), objectId: physicalId, openLink: true }] }
  }))}`;

  const openByLink = (appId, physicalId) => {
    if (widget.widgetDomain) {
      window.open(buildLink(widget.widgetDomain, appId, physicalId), '_blank');
      return;
    }
    CompassPlatformServices.getPlatformServices({
      platformId: widget.getValue('x3dPlatformId'),
      onComplete: (platform) => window.open(buildLink(platform['3DDashboard'], appId, physicalId), '_blank')
    });
  };

  openByLink('<앱 ID>', '<physicalid>');
});
```

- `<앱 ID>` 에는 열 앱의 화면에서 Copy Link 한 주소의 `#app:` 뒤 값을 넣습니다. OOTB 앱이면 `ENOWCHG_AP`, 복사해서 등록한 커스텀 앱이면 등록할 때 붙은 ID 입니다 (6.5). 사용자에게 부여된 앱이어야 합니다.
- 대시보드 주소는 OOTB 와 같이 `widget.widgetDomain` 을 먼저 쓰고, 없으면 Compass 에 `3DDashboard` 주소를 묻습니다.
- Compass 응답을 기다린 뒤 `window.open` 을 부르면 클릭과 떨어져 있어 브라우저가 팝업으로 막을 수 있습니다. 그때는 주소를 미리 받아 둡니다.

Compass 의 "다른 앱으로 열기" 도 같은 `X3DContentId` 를 붙여 엽니다 ([Compass 에 선택 객체 알리기](../../docs/widget/compass-pubsub.md#compass-가-객체로-하는-일)).

### 6.5 복사해서 만든 커스텀 앱 (예: `sdxENOChgGovernanceUX`)

OOTB 앱을 복사해 이름만 바꾼 앱은 위 방법에서 앱 ID 만 바꾸면 같은 흐름으로 동작합니다. 받는 쪽 `loadGovWidget` 을 OOTB 그대로 두었다면 고칠 것이 없습니다.

- 커스텀 앱이 **3DDashboard 에 앱으로 등록되어 앱 ID 가 있어야** 합니다. 등록한 앱의 ID 는 OOTB ID 가 아니라 등록할 때 붙는 값(`MAP-XXXXXXXX` 모양)이라 서버마다 다를 수 있습니다. 그 앱 화면에서 Copy Link 를 하면 주소의 `#app:` 뒤에서 ID 를 볼 수 있습니다 (Copy Link 는 지금 위젯의 `widget.getValue('appId')` 를 씀).
- `ENOWCHG_AP` 로 열면 OOTB 앱을 찾습니다. 사용자에게 그 앱이 부여되지 않았으면 새 탭이 원래 대시보드 화면으로 열리고 "앱을 검색하는 중 오류가 발생했습니다. 유효한 라이선스가 없습니다." 가 뜹니다. 개발 서버에서 겪었고, 등록한 앱 ID 로 바꾸자 ID 카드가 열렸습니다.
- 복사본의 `openWithIDCard` 가 OOTB 와 같으면 `launchPropertyPanelContext` 에 `type` 을 넘기지 않습니다. 정보 패널이 하위 type(`sdxECChangeAction`)을 알아야 한다면 복사본에서 `{ context: id, type: '<하위 type>' }` 로 고칩니다.
- 앱 ID 를 직접 비교하는 OOTB 코드는 커스텀 앱 ID 를 모릅니다.

| 위치 | 비교 | 커스텀 앱 ID 일 때 |
| --- | --- | --- |
| `ENOChgServices` 의 라이선스 확인 | `appId == 'ENOWCHA_AP'` 면 `CExecution`, 아니면 `CGovernance` | Governance 로 확인 |
| `ENOChangeActionUX` 의 링크 | `appId == 'ENOWCHG_AP'` 면 그 앱, 아니면 `ENOWCHA_AP` 를 엶 | OOTB Change Execution 앱이 열림 |
| `ENOChgGovernanceUX` 시작 화면 제목 | `appId == 'ENOWCHA_AP'` 면 Change Execution 제목 | 기본 제목 (Change Governance) |
| `ENOChgProbes` 사용 기록 | `ENOWCHA_AP` / `ENOWCHG_AP` 로 이름을 정함 | 둘 다 아니라서 이름이 비어 있음 |

## 7. 확인 필요

- 운영 서버에 등록한 커스텀 앱의 ID (개발 서버와 다를 수 있음). 6.4 주소는 개발 서버에서 등록한 앱 ID 로 ID 카드가 열림을 확인함 ([6 절](#6-다른-위젯에서-change-앱의-상세-화면-열기)).
- 6.2 의 CORS 에러가 서버 설정을 바꾸면 없어지는지.
- `sdxECChangeAction` 객체의 `ApplicabilityTypeInfo/<id>` 응답 `applicabilityType` 이 `"Change Action"` 인지. 다른 값이면 6.3 의 4 번으로 갑니다.
- 복사본의 `loadGovWidget`, `openWithIDCard` 가 OOTB 와 같은지 (난독화되어 눈으로 비교하기 어려우면 `getValue("X3DContentId")` 를 읽는지만 봄).
- `GET /resources/v1/chguxservices/changeaction/<id>` 가 `sdxECChangeAction` 도 돌려주는지 (Network 에서 200 과 `data`).
- 넘기는 `id` 가 physicalid 인지.
- 위젯 화면에서 Console 실행 대상을 위젯 프레임으로 바꾸고 아래가 상세 화면을 여는지. 열리면 그리드 링크도 동작합니다.

  ```js
  require(['DS/ENOChgGovernanceUX/scripts/util/changeAction/ChgChangeActionUtil'], (u) => u.displayObjectInfoInsideIDcard('<physicalid>'));
  ```

- 기존 소스가 `getDependency('xChangeApp').openIDCard(id)` 로 `sdxECChangeAction` 을 이미 열고 있다면, 서버가 하위 type 도 `"Change Action"` 으로 돌려준다는 뜻이므로 그 방식도 됩니다.
