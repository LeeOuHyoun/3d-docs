# 성숙도 변경, 개정 (lifecycle) <Badge type="info" text="원본 분석" />

위젯에서 객체의 state(성숙도)를 바꾸고(promote / demote), 갈 수 있는 state 를 보여 주고, 새 revision 을 만들고(개정), 소유자를 넘기는 방법입니다. OOTB 성숙도 창을 그대로 띄우는 길과 REST 를 직접 부르는 길이 있습니다. state 가 바뀔 때 서버에서 도는 트리거는 [라이프사이클과 promote](/trigger/lifecycle), state 정의는 [Policy](/schema/policy) 에 있습니다.

::: info 근거
- webapps (2025): LifecycleCmd/LifecycleCmd.js (`MaturityCmd`, `ReviseCmd`, `CommandBase`, `CommandMgr.executeCmd`, `SelectionUtil._createSelObj4Node`, `MessageMediator`), MaturityWidget/MaturityWidget.js (`_changeMaturity`, `_getMaturityGraph`, `_showNotification`, `_refresh`), ChangeMaturityWidget/ChangeMaturityWidget.js (`setStateUI`, `_changeMaturity`, `_getMaturityState`), ReviseWidget/ReviseWidget.js (`_executeRevise`, `reviseServiceRequest`), LifecycleServices/LifecycleServices.js (`LifecycleServicesSettings`, `LifecycleServices`, `LifecycleAlert`, `Refresh`), LifecycleControls `EditPropDialog`, ENOCollabSharingCmds `ChangeCommandBase`, NewBranchWidget/NewBranchWidget.js (`notificationTimeout`)
- REST 호출부: ENONextGenFilterUX (`_onSetMaturity`, `_initMaturityStateGraph`, `_reviseFilter`, `_changeOwner`), DELWKOModelServices / DELPPWModelServices (`syncMaturity`, `getCurrentStates`), FolderEditor / TeachersBookmarkEditor (`transferOwnership`), DocumentCommands (`V2/Actions/Maturity`, `NewRevision`), DocumentCommonInfra, DMUPersistence, ENOChgServices (`promoteChangeAction`, `getStatesAttributes`), IPClassifyUXUtil, IssueServices, ENOXVersionExplorerModelWebServices
- OOTB 명령 호출부: `new MaturityCmd().executeAsync([{ physicalid }])` ENXMeetingMgmt, ENXDecisionMgmt, ENORouteMgmt, RouteWebAppUX, DocumentCommands / `executeCompactViewAsync` ENOXEngineer, ENOChgGovernanceUX, IssueManagement / `begin()` ENONextGenFilterUX, ENOXCADTemplate
- 서버: 3dspace/WEB-INF/lib 의 DSLCRestWebServices.jar, ENOLifecycleWebServices.jar 클래스 상수 문자열 (`DSLCCAAWebServiceBase`, `MaturityCAAWebservices`, `VersionCAAWebservices`, `TransferCAAWebServices`, `CAAWebServicesUtil`, `ApplicationConfig`, `MaturityResource`, `ReviseResource`, `LifecycleServices_ChangeMaturity_*`), properties/emxENOLifecycleServicesStringResource.properties. 소유자 넘기기 응답: `TransferCAAWebServices.changeOwnership_CAA` 바이트코드 (항목 `Status` 로 200 / 207 / 400 을 고르고, 200 일 때만 `results` 로 감쌈)
- 집계: 모듈 문자열이 나오는 폴더 수 `LifecycleServicesSettings` 46, `LifecycleServices` 35, `MaturityCmd` 24, `LifecycleAlert` 21, `MessageMediator` 16. 파일 수 `app_initialization(` 46, `getSecurityContextPromise(` 27, `executeAsync(` 13, `executeCompactViewAsync(` 3. 2026 은 enovia2026_webapps 의 같은 폴더와 비교
:::

샘플: [LifecycleSample.html](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/WidgetSample/LifecycleSample.html) (physicalid 로 상태 그래프 읽기 → 갈 수 있는 state 골라 promote / demote → 결과와 오류 표시, dslc getNextStates / changeState, 개정, 소유자 넘기기, OOTB 성숙도 창 / 작은 성숙도 표시 / 개정 창 / 소유자 변경 창). 3DDashboard 에서만 동작합니다.

## 고르는 기준

| 하고 싶은 일 | 쓸 것 | CSRF 토큰 |
| --- | --- | --- |
| OOTB 와 같은 성숙도 창 띄우기 | `MaturityCmd` 의 `executeAsync` | 신경 쓰지 않음 (모듈이 부름) |
| 현재 state 배지와 다음 state 버튼을 내 화면에 넣기 | `MaturityCmd` 의 `executeCompactViewAsync` | 신경 쓰지 않음 |
| 갈 수 있는 state 를 직접 그리고 바꾸기 | `lifecycle/maturity/getStateGraph` + `promote` / `demote` | 보내지 않음 |
| 화면 없이 state 이름만 주고 바꾸기 | `v1/modeler/dslc/maturity/changeState` | 필요 |
| 개정 창 띄우기 | `ReviseCmd` | 신경 쓰지 않음 |
| 바로 개정 | `lifecycle/revise/major` | 보내지 않음 |
| 소유자 넘기기 | `v1/modeler/dslc/ownership/transfer`, OOTB 창은 `ActionBar_TransferOwnershipCmd` | 필요 / 신경 쓰지 않음 |

OOTB 창은 알림, 결재 Route, 하위 구조 포함, 서명 같은 처리를 다 해 주지만 모양은 바꿀 수 없습니다. 화면을 직접 그리려면 REST 를 부릅니다. 샘플처럼 섞어 써도 됩니다.

## REST 묶음

주소 앞부분이 다른 세 묶음이 있습니다. 응답 모양과 CSRF 처리가 다르므로 섞지 않습니다.

| 묶음 | 주소 | 서버 | 성공 / 실패 | CSRF | 쓰는 곳 |
| --- | --- | --- | --- | --- | --- |
| lifecycle | `/resources/lifecycle/...` | `ApplicationConfig` (`/resources/lifecycle`) 의 `MaturityResource`, `ReviseResource` 등 | HTTP 200 + `status: 'success'` / `'failure'` | 보내지 않음 | MaturityWidget, ChangeMaturityWidget, ReviseWidget, DocumentCommands, ENOChgServices, DMUPersistence |
| dslc (CAA) | `/resources/v1/modeler/dslc/...` | `DSLCCAAWebServiceBase` 의 `MaturityCAAWebservices`, `VersionCAAWebservices`, `TransferCAAWebServices` | 실패하면 HTTP 4xx / 5xx + `errorReport[]` | 헤더 `ENO_CSRF_TOKEN` 필요 | ENONextGenFilterUX, FolderEditor, TeachersBookmarkEditor, DELWKOModelServices, DELPPWModelServices |
| dslc (버전) | `/resources/v1/dslc/...` | 확인하지 못함 | 경로마다 다름 (`errorID`, `errorMessage`) | 보내지 않음 | LifecycleServices, ReviseWidget, ENOXVersionExplorer 계열, IssueServices |

- 성숙도, 개정, 소유자 요청은 모두 **POST + JSON 본문** 입니다. 읽기(`getStateGraph`, `getNextStates`)도 POST 입니다.
- 헤더는 `SecurityContext` ([보안 컨텍스트](/rest-api/security-context)), `Content-Type: application/json`, `Accept-Language` 입니다. OOTB 성숙도 화면은 `X-Requested-With: XMLHttpRequest` 도 넣습니다.
- lifecycle 요청에는 OOTB 가 항상 `?tenant=<플랫폼 id>` 를 붙입니다 (`get3DSpaceWSUrl`, 값이 없으면 `OnPremise`). dslc (CAA) 호출부(ENONextGenFilterUX, DELWKO / DELPPW)는 붙이지 않습니다.
- lifecycle 서버 클래스(`MaturityResource`, `ReviseResource`, `OwnershipService`)에는 CSRF 를 다루는 문자열이 없고, 확인한 OOTB 호출부(ChangeMaturityWidget, MaturityWidget, ReviseWidget, DocumentCommands, ENOChgServices, DMUPersistence)도 토큰을 넣지 않습니다. dslc (CAA) 는 공통 함수 `CAAWebServicesUtil` 이 `ENO_CSRF_TOKEN` 헤더를 검사하고(`validateCSRFKey`), OOTB 호출부도 모두 넣습니다. 토큰 받기는 [CSRF 토큰](/uwa/request#csrf-토큰-3dspace-쓰기-요청) 을 봅니다.
- 변경 조치 아래에서 일하는 중(Work Under)이면 OOTB 는 `DS-Change-Authoring-Context` 헤더를 붙입니다 ([헤더](/rest-api/#헤더)).
- WAFData 기본 시간 제한은 25 초입니다. OOTB 는 성숙도 요청에 1 시간, 개정과 dslc 버전 요청에 10 분을 줍니다.

샘플의 요청 함수입니다.

<<< @/../WidgetSample/LifecycleSample.html#request{js}

::: warning lifecycle 과 dslc 는 CSRF 처리가 다릅니다
같은 일을 하는 요청이라도 `/resources/v1/modeler/dslc/` 는 헤더 `ENO_CSRF_TOKEN` 이 없으면 서버가 거절합니다. `/resources/lifecycle/` 는 OOTB 가 토큰 없이 부릅니다. dslc 만 실패하면 토큰부터 확인합니다.
:::

## 상태 그래프 읽기 (getStateGraph)

OOTB 성숙도 창(MaturityWidget)과 작은 성숙도 표시(ChangeMaturityWidget)가 처음에 부르는 요청입니다. 객체의 현재 state 와 그 policy 의 state, transition 목록을 한 번에 줍니다.

```text
POST <3DSpace>/resources/lifecycle/maturity/getStateGraph?tenant=<플랫폼 id>
{ "data": [ { "physicalid": "<id>" } ] }
```

| 응답 키 | 내용 |
| --- | --- |
| `status` | `{ code, errorMsg }`. 성공은 `errorMsg` 가 `null` 이고 `code` 가 0 |
| `data.objects[]` | 요청한 객체. `physicalid`, `policy`, `current` (state 이름), `currentStateUserName` (번역 이름), `name`, `displayName`, `revision`, `type`, `typeDisplayName`, `imageUrl`, `organizationTitle`, `projectTitle`, `routes` (결재 Route), `isAggregateRep`, `isOnceInstantiable`, `usage` |
| `data.policies[]` | `name` (policy 이름) 과 `maturityGraph` |
| `maturityGraph.states[]` | `stateSysName` (실제 이름), `stateUserName` (번역 이름), `visuorder` (순서), `isCurrent`, `activeColor`, `inactiveColor`, `tooltip` |
| `maturityGraph.transitions[]` | `sourceState`, `targetState`, `signatures[]` (`name`, `username` ...), `hasRoute` |

여러 객체를 한 번에 물으면 `objects`, `policies` 가 여러 개 옵니다. 객체의 `policy` 와 `name` 이 같은 policy 를 찾아 씁니다. `policies` 가 비어 있으면 성숙도 그래프가 없는 객체입니다.

<<< @/../WidgetSample/LifecycleSample.html#state-graph{js}

### 갈 수 있는 state

ChangeMaturityWidget 의 드롭다운은 이렇게 만듭니다.

| 대상 | 보내는 요청 |
| --- | --- |
| 현재 state 에서 나가는 transition 의 `targetState` | `promote` |
| `visuorder` 가 바로 앞인 state (그 state 로 가는 transition 이 없을 때) | `demote` |

<<< @/../WidgetSample/LifecycleSample.html#next-states{js}

- MaturityWidget 의 여러 객체 화면은 `coretype` 이 `Entity` 인 객체에서 대상 state 의 `visuorder` 가 현재보다 작으면 demote 로 봅니다. 화면마다 규칙이 조금 다르므로, 뒤로 가는 transition 이 있는 policy 는 결과를 확인합니다.
- `objects[].routes[0].status` (또는 `routes[0].routestatuses[0].status`)가 `Block`, `BlockedByRequiredRouteRule` 이면 결재 Route 가 막고 있습니다. ChangeMaturityWidget 은 다음 state 버튼에 Route 아이콘과 안내 문구를 붙입니다.
- 현재 state 만 필요하면 `POST /resources/lifecycle/maturity/state` 본문 `{ data: [{ physicalid }] }` 를 부르고 `data[].physicalid`, `data[].current` 를 읽습니다 (DELPPWModelServices `getCurrentStates`).
- state 이름과 색만 필요하면 `POST /resources/lifecycle/maturity/getStatesAttributes` 본문 `{ policies: ['<policy 이름>'] }` 를 부르고 `policies[].policyName`, `policies[].states[]` (`stateSysName`, `stateUserName`, `activeColor`, `inactiveColor`) 를 읽습니다 (IPClassifyUXUtil, ENOXVersionExplorerController, ENOChgServices). ENONextGenFilterUX 는 `{ objectids: [id] }` 로 보내고 `policies` 를 객체 하나로 읽습니다 <Badge type="danger" text="검증 필요" />.

## promote / demote

```text
POST <3DSpace>/resources/lifecycle/maturity/promote?tenant=<플랫폼 id>
POST <3DSpace>/resources/lifecycle/maturity/demote?tenant=<플랫폼 id>
```

| 본문 `data[]` 키 | 필수 | 설명 |
| --- | --- | --- |
| `physicalid` | O | 객체 id |
| `tostate` | O | 갈 state 의 `stateSysName`. 표시 이름이 아님 |
| `fromstate` | - | 현재 state. ENOChgServices 는 빼고 보냄 |
| `signature` | - | 서명이 걸린 transition 의 서명 이름. MaturityWidget 이 promote 때 넣음 |
| `type`, `name`, `revision`, `policy` | - | MaturityWidget, DMUPersistence 가 같이 보냄 |

| 응답 키 | 내용 |
| --- | --- |
| `status` | `'success'` 또는 `'failure'` |
| `results[]` | 바뀐 객체. `physicalid`, `current` (새 state), `cestamp`, `modified` |
| `report[]` | 실패한 객체. `physicalids` (배열), `error` (문구) |

- 권한이 없거나, 갈 수 없는 state 이거나, 트리거가 막아도 **HTTP 200** 으로 옵니다. `onComplete` 안에서 `status` 를 먼저 봅니다.
- 여러 개를 보내면 일부만 성공할 수 있습니다. ChangeMaturityWidget 은 `results` 를 먼저 반영한 뒤 `status === 'failure'` 면 `report` 를 10 개까지 보여 줍니다.
- `demote` 도 본문이 같고 `tostate` 에 이전 state 를 넣습니다 (MaturityWidget `_demote`, ChangeMaturityWidget).
- MaturityWidget 은 본문 최상위에 `notificationTimeout: 3600` 을 넣습니다. 오래 걸리면 결과를 백그라운드 알림으로 돌리는 기준(초)입니다. MaturityWidget 은 요청 시간 제한도 같은 1시간(`36e5` ms)으로 두고, 시간이 지나면 오류 대신 "작업이 진행 중이며 끝나면 3DNotification Center 로 알림이 간다" 는 안내(`maturityTimeoutInfo`)를 띄웁니다. NewBranchWidget 도 같은 방식(`notificationTimeout: 600`, 시간 제한 600초, `notificationTimeoutInfo`)입니다.

<<< @/../WidgetSample/LifecycleSample.html#change{js}

### 오류 문구 찾기

| 상황 | 받는 곳 | 문구 위치 |
| --- | --- | --- |
| lifecycle 업무 실패 | `onComplete` | `report[i].error` |
| dslc 실패 | `onFailure(error, data)` | `data.errorReport[i].errorCode`, `errorMessage` |
| 그 밖의 HTTP 오류 | `onFailure(error, data)` | `data.error.message`, `data.message`, `data.errorMessage`. 상태 코드는 `error.message` 끝의 `ResponseCode with value "500"` |
| 시간 초과 | `onTimeout` | - |

LifecycleServices 의 `parseWebServiceError(error, data)` 는 `data.error.message` → `data.message` → `data.errorMessage` 순으로 문구를 고르고, 401 / 403 / 504 / 0 (연결 없음)은 정해진 번역 문구로 바꿉니다. 샘플은 여기에 `report`, `errorReport` 를 먼저 봅니다.

<<< @/../WidgetSample/LifecycleSample.html#errors{js}

## dslc (CAA) 성숙도 API

`/resources/v1/modeler/dslc/` 는 서버 클래스 이름에 CAA 가 붙은 웹 서비스입니다. 서버에는 `/maturity/getNextStates`, `/maturity/changeState`, `/version/create`, `/version/getGraph`, `/ownership/transfer` 가 있고, 같은 묶음에 잠금(reservation), 공유(sharing), 복제(duplicate) 서비스도 있습니다.

| 경로 | 본문 | 응답 |
| --- | --- | --- |
| `maturity/getNextStates` | `{ data: [{ id }] }` | `data[]` 에 `id`, `maturityState` (현재 state). 서버 상수에 `nextStates` 도 있음 |
| `maturity/changeState` | `{ data: [{ id, nextState }] }` | OOTB 호출부는 응답을 읽지 않음 |

- 객체는 `id` 대신 참조 객체 `{ identifier, type, source: '3DSpace', relativePath }` 로도 줄 수 있습니다. DELWKOModelServices 는 `relativePath: '/resources/v1/modeler/dsmfg/dsmfg:MfgItem/<id>'` 를 넣습니다.
- `nextState` 는 state 이름입니다. 호출부에는 `'RELEASED'` (제조 항목), `'Active'` (북마크) 가 보입니다.
- 서버는 현재 state 와 policy 그래프로 promote 인지 demote 인지와 `tostate`, `fromstate`, `signature` 를 정한 뒤 lifecycle 과 같은 내부 함수(`maturityChange_internal`)를 부르는 것으로 보입니다 <Badge type="danger" text="검증 필요" />. 그렇다면 promote 와 demote 를 고르기 어려울 때 이쪽이 편합니다.
- `nextStates` 항목이 문자열인지 객체인지는 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />. 샘플은 응답을 그대로 보여 줍니다.

<<< @/../WidgetSample/LifecycleSample.html#dslc{js}

### 오류 코드

실패하면 HTTP 4xx / 5xx 와 함께 `{ "errorReport": [{ "errorCode": "...", "errorMessage": "...", "errorType": "..." }] }` 가 옵니다. 코드 앞 세 자리가 HTTP 상태와 같은 것으로 보입니다 <Badge type="danger" text="검증 필요" />. 문구는 `emxENOLifecycleServicesStringResource.properties` 의 `lifecycle.CAAMaturity.error.<코드>` 입니다 (개정은 `CAAVersion`, 소유자는 `CAATransfer`).

| 코드 | 문구 (CAAMaturity) |
| --- | --- |
| `LCERROR400001` | Bad Payload |
| `LCERROR400002` | Bad Payload : No Data |
| `LCERROR400003` | Bad Payload : Null or Empty id Value |
| `LCERROR400004` | Bad Payload : Null or Empty nextState Value |
| `LCERROR400006`, `LCERROR400007` | id key not found, nextState key not found |
| `LCERROR400008` | Business Object's concrete type is not public |
| `LCERROR400009`, `LCERROR400010` | Invalid Id, Invalid Source |
| `LCERROR401000` | Missing or incorrect security context (서버 클래스 상수) |
| `LCERROR403000` | Forbidden |
| `LCERROR404000` | Not Found : Business object does not exist |
| `LCERROR500001`, `LCERROR500002` | Maturity Failed, `Maturity Failed :` 뒤에 원인 |
| `LCERROR500003` | 처리 객체 수가 한도를 넘음 |

## 개정 (revise)

```text
POST <3DSpace>/resources/lifecycle/revise/major?tenant=<플랫폼 id>
{ "data": [ { "physicalid": "<id>" } ] }
```

| 본문 키 | 설명 |
| --- | --- |
| `data[].physicalid` | 개정할 객체 |
| `data[].title` | 새 revision 의 제목 (ENOXVersionExplorer, ReviseWidget) |
| `data[].modifiedAttributes` | 바꿀 값. ReviseWidget 은 `revision` (제안 번호), `description`, `PLMReference.V_versionComment` 를 넣음 |
| `folderid` | 새 revision 을 넣을 폴더 (ReviseWidget) |

| 응답 키 | 내용 |
| --- | --- |
| `status` | `'success'` / `'failure'` |
| `results[]` | `physicalid` (새 revision), `derivedfromphysicalid` 또는 `attribute[PLMReference.V_DerivedFrom]` (원본), `folders` |
| `report[]` | 실패 문구 `error` |

<<< @/../WidgetSample/LifecycleSample.html#revise{js}

### 개정에 쓰는 다른 경로

| 경로 | 메서드 | 본문 → 응답 | 쓰는 곳 |
| --- | --- | --- | --- |
| `/resources/v1/modeler/dslc/version/create` | POST + CSRF | `{ data: [{ source: '3DSpace', identifier, type, relativePath }] }` → `results[0].id`. 실패 `errorReport[]` | ENONextGenFilterUX `_reviseFilter` |
| `/resources/v1/dslc/versiongraph/{id}` | GET | `?withThumbnail=1&withIsLastVersion=1&withAttributes=1&withCopyFrom=1` | ENOXVersionExplorerModelWebServices, LifecycleServices (2025) |
| `/resources/v1/dslc/versiongraph` | POST | `{ graphRequests: [{ id, attributes: ['revision', 'policy'] }] }` → `graphs[].versions[]` (`id`, `code` (revision), `isLastVersion`, `maturity`, `ancestors[]` 의 `id`, `semantic`) | IssueServices, DMUReadPersistence, AuthGenericCommands |
| `/resources/v1/dslc/getversionnumberproposal` | POST | `{ addRequests: [{ copyId, ancestors: [{ semantic: 'LAST', id }] }] }` → `addRequests[].code` (다음 revision 제안) | ReviseWidget |
| `/resources/v1/dslc/getallowedsemantics` | POST | `{ allowedSemantics: [{ id }], output: { infos: 'version' } }` → `allowedSemantics[]` (`semantic`, `mode`) | LifecycleServices `getAllowedSemanticsPromise`, `getRevisionMode` |
| `/resources/v1/dslc/addversions` | POST | `{ addRequests: [{ copyId, code, ancestors: [{ id, semantic }], attributes: [{ name, value }] }] }` → `addRequests[].id`, `code`. `semantic` 은 `E`, `LAST`, `FIRST`, `FORE` | ReviseWidget (`useFIRST`, `foreIntent` 일 때), LifecycleServices `createFirstConfigurationRevisionPromise` |
| `/resources/v1/dslc/getlastversions` | POST | 최신 revision | DocumentCommands |
| `/resources/lifecycle/revise/prepare_revise_checkavailability`, `prepare_revise_maskattributes` | POST | 개정 창을 열기 전 확인, 입력 칸 정보 | ReviseWidget |
| `/resources/lifecycle/revise/from` | POST | 다른 객체로부터 개정 | ReviseFromWidget, CompareMergePlugin |
| `/resources/lifecycle/newbranch/execute_newbranch` | POST | 새 branch | NewBranchWidget |
| `/resources/lifecycle/revision/family`, `revision/versionFamilyData` | - | revision 계열 | RevisionFamily, ENOXVersionExplorerModelWebServices |

ENOXVersionExplorer 는 버전 그래프를 부를 때 `DS-Change-Authoring-Context` 헤더를 지웁니다.

## 소유자 넘기기

```text
POST <3DSpace>/resources/v1/modeler/dslc/ownership/transfer
헤더 ENO_CSRF_TOKEN
{ "owner": "<로그인 이름>", "organization": "<조직>", "collabspace": "<협업공간>", "data": [ { "id": "<id>" } ] }
```

| 본문 키 | 설명 |
| --- | --- |
| `owner` | 새 소유자. FolderEditor 는 이것과 `data` 만 보냄 |
| `organization`, `collabspace` | 새 조직, 협업공간. ENONextGenFilterUX 는 SecurityContext 의 조직, 협업공간을 넣음 |
| `data[]` | `{ id }` 또는 참조 객체. 한 번에 1,000 개까지 (서버 상수 `Only 1000 objects allowed to Transfer`) |

- `owner`, `organization`, `collabspace` 가 모두 비면 `Empty Transfer Arguments` 오류입니다 (서버 상수).
- 응답 항목은 `owner`, `organization`, `collabspace`, `id`, `status` (숫자) 입니다. 실패한 항목에는 `errorReport` 가 붙고, 원래 오류에 `errorCode` 와 `errorMessage` 가 모두 있으면 `errorReport: { errorMessage }` 만 남깁니다.
- 응답 모양은 항목 결과에 따라 달라집니다.
  - 모두 `200`: HTTP 200, `{ results: [...] }`
  - `200` 과 `400` 이 섞임: HTTP 207, `results` 없이 **항목 배열이 최상위**
  - 모두 `400` 이거나 `500` 이 하나라도 있음: HTTP 400, 항목 배열이 최상위
- TeachersBookmarkEditor 는 `results[]` 만 읽고, 항목의 `status` 가 `200` 이면 성공, 그 밖의 값이면 실패로 세어 개수만 알립니다. 일부만 실패하면 응답에 `results` 가 없으므로 최상위 배열도 처리해야 합니다.
- 실패 문구는 `onFailure` 의 `data.message` 또는 `data.errorMessage` 에 있습니다 (FolderEditor `formatError`).

<<< @/../WidgetSample/LifecycleSample.html#ownership{js}

OOTB 소유자 변경 창은 `DS/ENOCollabSharingCmds/commands/TransferOwnershipCmd/ActionBar_TransferOwnershipCmd` 입니다 (9 개 폴더). 대상 노드는 `context.getSelectedNodes()` 로 넘기고 노드에는 `getID()`, `getLabel()` 이 꼭 있어야 합니다. `DS/Selection/CSOManager` 에 선택이 있으면 그것을 먼저 씁니다. 끝난 것은 PlatformAPI topic `DS/PADUtils/PADCommandProxy/refresh` 로 압니다 (ENONextGenFilterUX). 샘플의 이 버튼은 서버에서 실행해 보지 않았습니다 <Badge type="danger" text="검증 필요" />.

<<< @/../WidgetSample/LifecycleSample.html#ootb-owner{js}

| 명령 | 창에 넘기는 종류 | 쓰는 곳 |
| --- | --- | --- |
| `ActionBar_TransferOwnershipCmd` | `Person` (사람) | ENONextGenFilterUX "소유자 변경", DocumentCommands, ENORouteMgmt 등 |
| `ActionBar_ChangeOwnerCmd` (8 개 폴더) | `full` | ENONextGenFilterUX "이동(Move To)" |
| `ActionBar_ChangeCSCmd` | `CollabSpace` (협업공간) | ENOChgGovernanceUX |

## OOTB 창 그대로 쓰기

### 성숙도 창 (MaturityCmd)

`DS/LifecycleCmd/MaturityCmd` 는 Collaborative Lifecycle 앱의 성숙도 명령입니다. 모듈 이름이 다른 폴더 24 개에 나오고, 회의, 결정, Route, 문서 앱이 이 명령으로 같은 창을 띄웁니다. `new MaturityCmd(options)` 로 만들고 아래 메서드를 부릅니다.

| 메서드 | 설명 |
| --- | --- |
| `executeAsync(targets)` | 성숙도 창(MaturityWidget)을 엶. 창이 닫히거나 변경이 끝나면 **값 없이** resolve |
| `execute(targets, onDone)` | 위와 같고 콜백으로 받음 |
| `executeCompactViewAsync(widget, container, options, physicalId)` | 작은 성숙도 표시 (아래) |
| `begin()` | ApplicationFrame 명령으로 시작. 대상은 `context.getSelectedNodes()` 에서 읽음 (ENONextGenFilterUX `ID: 'MaturityHdr'`, ENOXCADTemplate) |

| `targets[]` 키 | 필수 | 설명 |
| --- | --- | --- |
| `physicalid` | O | `id`, `nodeID`, `physicalId` 도 읽음 |
| `displayName`, `revision` | - | 창 제목 ("성숙도 - 이름 revision") |
| `type`, `typeDisplayName`, `icon` | - | 표시용 |
| `current`, `currentDisplayName`, `policy` | - | 없어도 됨. 창이 `getStateGraph` 로 다시 읽음 |
| `serviceurl` | - | 다른 3DSpace 의 `.../resources/lifecycle/maturity/` 주소 (DocumentCommands) |
| `maturityTitle` | - | 창 제목에서 이름 대신 씀 |

| `options.context` 키 | 설명 |
| --- | --- |
| `getSecurityContext()` | `{ SecurityContext: 'ctx::...' }` 를 돌려줌. 없으면 위젯 설정 `xPref_CREDENTIAL` → `pad_security_ctx` → `securityContext` → 서버 기본값(`pno/person/getsecuritycontext`) 순으로 찾음 |
| `displayNotification(n)` | 창이 내는 알림 `{ eventID, msg, sticky }` 를 받음. 없으면 LifecycleAlert 가 화면 알림으로 띄움 |
| `getSelectedNodes()` | `begin()` 때 대상. 노드는 `getID()`, `getLabel()`, `getType()`, `getDisplayName()`, `object.revision` (ENONextGenFilterUX) |
| `getUUID()` | `Lifecycle.Modification` 알림의 `OriginID` 가 됨 |

<<< @/../WidgetSample/LifecycleSample.html#ootb-dialog{js}

- 창은 내 위젯의 `widget.body` 안에 그려집니다 (`inject(widget.body)`). 크기는 지난번 크기를 쓰고 최소 650 x 305 입니다. 위젯이 작으면 창도 잘립니다.
- `executeAsync` 는 바꿨는지, 취소했는지 알려 주지 않습니다. ENXMeetingMgmt 처럼 끝나면 다시 읽습니다.
- 창이 알아서 결재 Route, 서명, 하위 구조 포함(체크박스), Work Under 헤더를 처리합니다.
- 명령 목록에서 쓸 때(`isTypesSupported`)는 `CATPart`, `CATProduct`, `CATDrawing` 이 빠집니다. `executeAsync` 는 이 검사를 하지 않습니다.

### 작은 성숙도 표시 (ChangeMaturityWidget)

현재 state 배지, 다음 state 버튼, 다른 state 드롭다운(맨 끝에 "성숙도" 창 열기)을 내 요소 안에 그립니다. ENOXEngineer, ENOChgGovernanceUX, IssueManagement 의 ID 카드가 씁니다.

| 인자 | 설명 |
| --- | --- |
| `widget` | 위젯 객체 |
| `container` | 그릴 요소 |
| `options` | `SecurityContext` (`ctx::...`), `serviceurl` (다른 3DSpace 의 `.../resources/lifecycle/maturity/`), `EnableApproval` (결재 상태 표시) |
| `physicalId` | 객체 id |

<<< @/../WidgetSample/LifecycleSample.html#ootb-compact{js}

- promise 는 **첫 변경 시도**(성공, 실패, 시간 초과)가 끝나면 resolve 됩니다. ENOChgGovernanceUX 는 그 뒤 표시를 새로 만듭니다.
- 끝난 뒤 `cmd.changeMaturityWidget.current` (state 이름), `currentState` (번역 이름)를 읽을 수 있습니다 (ENOChgGovernanceUX).

::: warning 작은 성숙도 표시의 CSS 는 직접 불러옵니다
ChangeMaturityWidget 모듈은 자기 CSS 를 부르지 않습니다. 쓰는 앱 셋 모두 `css!DS/ChangeMaturityWidget/ChangeMaturityWidget` 을 따로 require 합니다. 빼면 배지와 버튼이 모양 없이 나옵니다. MaturityWidget, ReviseWidget 은 자기 CSS 를 부릅니다.
:::

### 개정 창 (ReviseCmd)

`DS/LifecycleCmd/ReviseCmd` 는 개정 창(ReviseWidget)을 엽니다. `executeAsync` 가 없으므로 끝은 `addEvent('ExecCompleted', fn)` 로 받습니다 (CommandFactory 와 같은 방식). DocumentCommands 는 `context.displayNotification` 에서 `eventID === 'primary'` 를 성공으로 봅니다.

<<< @/../WidgetSample/LifecycleSample.html#ootb-revise{js}

- 대상에 `current`, `policy` 가 없으면 `/resources/lifecycle/product/attributeList` 로 다시 읽고, 이때 로그에 `widget.data.appId` 를 씁니다. 샘플은 둘 다 넘깁니다.
- 같은 방식의 다른 명령: `HistoryCmd` (revision 목록, 9 개 폴더), `DuplicateCmd` (복제, 5), `DeleteCmd` (삭제, 4). ENONextGenFilterUX 가 `ID` 를 `RevisionFromHdr`, `DuplicateHdr` 로 줍니다.

### 끝났을 때 알림

| 방법 | 범위 | 데이터 |
| --- | --- | --- |
| `executeAsync().then()` | 이 호출 | 없음 |
| `context.displayNotification(n)` | 이 호출 | `{ eventID, msg, sticky }` |
| `MessageMediator.addEvent('onModification', fn)` | 같은 위젯 안 | `{ created, modified, deleted, context }`. 항목은 `{ physicalid, operation: 'Promote', state, policy }`, 개정은 `created` 에 `{ physicalid (새), sourceObjectId, operation: 'Revise' }` |
| PlatformAPI `Lifecycle.Modification` | 대시보드 전체 | JSON **문자열**. `{ From: 'Lifecycle', Refresh: (위 객체), AppID, OriginID, tenant, WidgetID }` |
| PlatformAPI `DS/PADUtils/PADCommandProxy/refresh` | 대시보드 전체 | `data.authored.modified` 에 바뀐 id 배열 (ENOCharacteristicsApp) |

`MessageMediator` 의 `onModification` 을 `DS/LifecycleServices/Refresh` 가 받아서 OOTB 편집기를 새로 고치고(`PADCommandProxy.refresh`) `Lifecycle.Modification` 을 보냅니다. 받는 쪽(DocumentFiles, ENOChangeActionUX, ENOChgGovernanceUX 등)은 문자열이면 `JSON.parse` 합니다. PlatformAPI 사용법은 [PlatformAPI](/widget/platform-api) 를 봅니다.

<<< @/../WidgetSample/LifecycleSample.html#notify{js}

REST 를 직접 불러 바꾼 경우에는 이 알림이 나가지 않습니다. 다른 OOTB 위젯이 새로 고치게 하려면 같은 모양으로 `Lifecycle.Modification` 을 보내면 될 것으로 보입니다 <Badge type="danger" text="검증 필요" />.

### TransientWidget 으로 여는 앱

[TransientWidget](/widget/transient-widget) 의 LifecycleControls 예(`ENOLCMT_AP`)는 **속성(Information) 창** 입니다 (`EditPropDialog._launchTransientWidget`, `mode: 'panel'`, 650 x 800). 열리는 EditPropWidget 소스에는 성숙도 관련 코드가 없습니다. Collaborative Lifecycle 앱(`ENOLCMI_AP`)은 3DXContent 로 객체를 넘겨 열 수 있지만([OpenWith](/widget/compass-services#openwith)) 자동 실행 명령(`commandId`)은 `LifecycleDelete` 만 처리합니다. 성숙도를 바꾸는 창은 `MaturityCmd` 로 엽니다.

## 모듈

| 모듈 | 폴더 수 | 하는 일 |
| --- | --- | --- |
| `DS/LifecycleServices/LifecycleServicesSettings` | 46 | 3DSpace 주소, tenant, 요청 헤더 (싱글톤) |
| `DS/LifecycleServices/LifecycleServices` | 35 | SecurityContext, CSRF, 오류 문구, 개정 방식 조회 (싱글톤) |
| `DS/LifecycleCmd/MaturityCmd` | 24 | 성숙도 창, 작은 성숙도 표시 |
| `DS/LifecycleServices/LifecycleAlert` | 21 | 알림 `displayNotification({ eventID, msg, title, subtitle, hasHtml, sticky })`. [알림](/controls/notifications) 의 NotificationsManagerUXMessages 를 씀 |
| `DS/LifecycleCmd/MessageMediator` | 16 | 명령 사이 이벤트 (`onModification`) |
| `DS/LifecycleControls/CommandDialog` | 12 | 명령 창 틀 (MaturityWidget 이 씀) |
| `DS/LifecycleControls/EditPropDialog` | 9 | 속성 창 (`ENOLCMT_AP` 를 TransientWidget 으로 엶) |
| `DS/LifecycleCmd/HistoryCmd`, `ReviseCmd`, `DuplicateCmd`, `DeleteCmd` | 9, 3, 5, 4 | revision 목록, 개정, 복제, 삭제 창 |
| `DS/MaturityWidget/MaturityWidget` | 8 | 성숙도 창 본체 (MaturityCmd 가 만듦) |
| `DS/ReviseWidget/ReviseWidget` | 3 | 개정 창 본체 |
| `DS/ChangeMaturityWidget/ChangeMaturityWidget` | 2 | 작은 성숙도 표시 본체 |
| `DS/LifecycleWidget/LifecycleWidget` | - | Collaborative Lifecycle 앱 자체. 다른 앱이 부르지 않음 |

### LifecycleServicesSettings

| 함수 | 설명 |
| --- | --- |
| `app_initialization(callback, platformServices?)` | 플랫폼 서비스 목록을 한 번 읽고(`getPlatformServices`) tenant 를 정한 뒤 `callback()`. 둘째 인자로 `[{ '3DSpace': 주소, platformId }]` 를 주면 Compass 를 부르지 않음 (ENOXCADTemplate) |
| `setOption('platform_services', list)` | 위 목록을 직접 넣기 (ENOChgGovernanceUX) |
| `getTenant()`, `setTenant(id)` | tenant. 기본은 `x3dPlatformId`, 없으면 첫 플랫폼 |
| `get3DSpaceWSUrl(tenant, path, query?)` | `<3DSpace>` + path + `?tenant=` (+ query). tenant 가 비면 `x3dPlatformId`, 그것도 없으면 `OnPremise` |
| `getHeaders(securityContext?)` | `Accept`, `Content-Type: application/json`, `Accept-Language: widget.lang`, `SecurityContext` (`encodeURIComponent`). Work Under 중이면 `DS-Change-Authoring-Context`, `DS-Configuration-Authoring-Context` 도 넣음. CSRF 는 넣지 않음 |

### LifecycleServices

| 함수 | 설명 |
| --- | --- |
| `getSecurityContextPromise(tenant, context?)` | `context.getSecurityContext().SecurityContext` → 위젯 설정 `xPref_CREDENTIAL` (`ctx::` 를 붙임), `pad_security_ctx`, `securityContext` → `pno/person/getsecuritycontext` 순 |
| `getCsrfKeyPromise(tenant)` | `E6WFoundation/CSRF` 의 `csrf` (`{ name, value }`). 한 번 받으면 보관 |
| `parseWebServiceError(error, data)` | 오류 문구 하나 |
| `getAllowedSemanticsPromise(ids, context, options)` | `dslc/getallowedsemantics` |
| `getRevisionMode(id, allowedSemantics)` | 위 응답에서 개정 방식 (`effectivity_based`, `effectivity_based_reused`, `effectivity_based_model` 또는 `mode`) |
| `getVersionNumberProposalPromise(ids, semantic, prefix, context)` | `dslc/getversionnumberproposal` |

이 모듈들은 OOTB 앱끼리 쓰는 모듈이라 버전이 바뀌면 동작을 다시 확인합니다. 내 위젯은 주소와 헤더를 직접 만들고(샘플), OOTB 창을 띄울 때만 `MaturityCmd` 를 쓰면 바뀜에 덜 흔들립니다.

## 서버에서 일어나는 일 (트리거)

lifecycle `promote` / `demote` 는 서버에서 객체의 promote / demote 를 실행합니다 (CID 객체는 MQL `promote branch`, `demote branch`). 그래서 policy 에 붙인 Promote Check / Action 트리거가 다른 화면에서 바꿀 때와 똑같이 돕니다 ([라이프사이클과 promote](/trigger/lifecycle), [Policy 의 trigger](/schema/policy#trigger)). dslc `changeState` 도 같은 내부 함수를 거치는 것으로 보입니다 (위). 서버 클래스(`LifecycleServices_ChangeMaturity_PLMReference_default`, `_BusinessObject`)의 문자열로 보면 한 트랜잭션 안에서 아래 검사를 하고, 하나라도 실패하면 abort 하는 것으로 보입니다 <Badge type="danger" text="검증 필요" />.

| 검사 | 실패하면 `report[].error` 에 오는 문구 |
| --- | --- |
| 결재 Route 가 끝나지 않음 (`hasIncompleteRoutes`) | You cannot change the maturity of ... because this object is part of a route. Verify that this route is in the Completed state. |
| 갈 수 없는 state (`isValidNextState`) | Cannot transition to ... state from current state / Target state ... does not exist |
| 권한 | No access privilege for operation in current state. |
| 객체가 없음 | Object has been deleted or is no longer accessible |
| PLM 업무 규칙 (`PLMCheckMaturity`, `PLMActionMaturity`, `PLMPostCheckMaturity`) | 규칙이 낸 문구 |
| promote 와 policy 트리거 (Check → state 변경 → Action) | 트리거 문구 (아래) |

- 트리거가 `notice` 로 남긴 문구([막기와 메시지](/trigger/jpo-args#막기와-메시지)): 서버 클래스에 context 의 client task(notice)를 읽어 메시지에 붙이는 문자열(`getClientTasks`, `UXMessage`, `#FSMP#`)이 있어서 `report[].error` 에 들어오는 것으로 보입니다 <Badge type="danger" text="검증 필요" />.
- Action 트리거가 1 을 돌려주면 `emxTriggerManager` 가 "Trigger program failed" 예외를 던지고 promote 도 실패합니다. 원인을 보여 주려면 Action 에서는 메시지를 담아 예외를 던집니다.
- classic UI (emxExtendedPageHeaderAction.jsp) 는 promote 예외 문구에 `Cannot promote - all requirements not satisfied` 가 있으면 "Promotion Failed" 문구로 바꿔 보여 줍니다. 서버가 이 문자열을 낼 수 있다는 뜻입니다.
- dslc `changeState` 는 같은 실패를 `errorReport` (`LCERROR500002`, `Maturity Failed :` 뒤에 원인)로 돌려주는 것으로 보입니다 <Badge type="danger" text="검증 필요" />.

::: warning OOTB 성숙도 창은 트리거 문구를 바꿔서 보여 줄 수 있습니다
MaturityWidget 은 `report[].error` 에 `1500168` 이나 `all requirements not satisfied` 가 있으면 "성숙도를 변경하는 데 필요한 허가가 없습니다" (`MATURITY_FAIL_PERMISSION`) 로 바꿔 보여 줍니다. 트리거가 남긴 이유가 사용자에게 보이지 않으면 REST 를 직접 부르고 `report[].error` 를 그대로 표시해 봅니다. 트리거 실패 때 실제로 어떤 문자열이 오는지는 서버에서 확인하지 않았습니다 <Badge type="danger" text="검증 필요" />.
:::

## 2026 변경

| 항목 | 2025 | 2026 |
| --- | --- | --- |
| `MaturityCmd`, `CommandBase`, `MessageMediator`, `HistoryCmd` | | 같음 |
| ChangeMaturityWidget.js | | 파일이 같음 |
| `LifecycleServicesSettings`, `LifecycleAlert` | | 같음 |
| MaturityWidget 의 요청 경로, 오류 처리 (`1500168`) | | 같음 |
| LifecycleServices 버전 그래프 (`_getAncestorsPromise`) | `GET /resources/v1/dslc/versiongraph/{id}` | `POST /resources/v1/dslc/versiongraph/` 본문 `{ graphRequests: [{ id }] }` <Badge type="warning" text="2026" /> |
| LifecycleServices `navigatePromise` | 없음 | `/resources/lifecycle/navigate/federated` 를 부름 <Badge type="warning" text="2026" /> |
| `ReviseCmd.isTypesSupported` | `ENOCLG_LibraryReference` 만 막음 | `VPMReferenceExtPos`, `AxisSystemReference` 도 POE 역할이 없으면 막음 <Badge type="warning" text="2026" /> |
| `ReviseFromCmd`, `NewBranchCmd`, `DuplicateCmd` 의 `isTypesSupported` | POE 역할 검사 없음 | 같은 두 타입을 POE 역할(`GrantedRolesCache.isEnabled('POE')`)이 없으면 막음 <Badge type="warning" text="2026" />. 2026 에서 이 명령들이 비활성으로 보이면 사용자 역할을 확인 |
| `CommandMgr` | | `DS/PADServices/utils/GrantedRolesCache` 를 초기화 <Badge type="warning" text="2026" /> |
| dslc/maturity 호출부 | DELWKOModelServices 에도 있음 (`changeState` 6 건, `getNextStates` 4 건) <Badge type="tip" text="2025" /> | DELWKOModelServices 에서 빠지고 FolderEditor `changeMaturityState` 추가 (`changeState` 5 건, `getNextStates` 2 건) |
| LifecycleControls | | `DS/LifecycleControls/CredentialSelector` 추가 <Badge type="warning" text="2026" /> |

## 주의

- `tostate`, `nextState` 에는 표시 이름이 아니라 state 이름(`stateSysName`)을 넣습니다. 같은 뜻이라도 policy 마다 이름이 다를 수 있습니다 ([state 이름과 심볼릭 이름](/schema/policy#state-이름과-심볼릭-이름)).
- lifecycle 응답은 실패해도 HTTP 200 입니다. `status` 를 먼저 봅니다 ([onComplete 에서도 success 를 확인합니다](/rest-api/#오류) 와 같은 이유).
- OOTB 창과 REST 는 같은 서버 처리를 거칩니다. 화면에서 막고 싶은 규칙은 위젯이 아니라 트리거에 둡니다 ([화면이 아니라 트리거에서 막습니다](/trigger/jpo-args#막기와-메시지)).
- ENONextGenFilterUX 의 `_changeOwner` 는 CSRF 토큰을 받기 전에 정의되지 않은 변수 `token` 을 헤더에 넣습니다. OOTB 코드를 베낄 때는 샘플처럼 토큰을 받은 뒤 넣습니다.
