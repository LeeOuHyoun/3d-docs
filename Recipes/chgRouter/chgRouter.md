# ChgRouter 로 Change Action 상세 화면 열기 (조사 메모)

Change 앱(`ENOChgGovernanceUX`) 위에 만든 위젯에서, 그리드의 링크로 Change Action(커스텀 하위 type `sdxECChangeAction` 포함) 상세 화면으로 넘어가는 방법을 조사한 메모입니다.

- 근거: enovia2025 / 2026 webapps 원본 (`ENOChgRouter5`, `ENOChgGovernanceUX`, `ENOChgServices`). 2025 와 2026 이 같습니다.
- **실제 화면에서는 돌려 보지 않았습니다.** 확인할 것은 [6 절](#6-확인-필요) 에 있습니다.

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

- 싱글턴이라 어느 모듈에서 불러도 같은 라우터입니다. 단 Change 앱이 `initialize` 하기 전에는 `navigate` · `getDependency` 가 실패합니다. **Change 앱 위젯 안에서만** 씁니다.

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

## 6. 확인 필요

- `GET /resources/v1/chguxservices/changeaction/<id>` 가 `sdxECChangeAction` 도 돌려주는지 (Network 에서 200 과 `data`).
- 넘기는 `id` 가 physicalid 인지.
- 위젯 화면에서 Console 실행 대상을 위젯 프레임으로 바꾸고 아래가 상세 화면을 여는지. 열리면 그리드 링크도 동작합니다.

  ```js
  require(['DS/ENOChgGovernanceUX/scripts/util/changeAction/ChgChangeActionUtil'], (u) => u.displayObjectInfoInsideIDcard('<physicalid>'));
  ```

- 기존 소스가 `getDependency('xChangeApp').openIDCard(id)` 로 `sdxECChangeAction` 을 이미 열고 있다면, 서버가 하위 type 도 `"Change Action"` 으로 돌려준다는 뜻이므로 그 방식도 됩니다.
