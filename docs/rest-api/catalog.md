# 엔드포인트 목록 <Badge type="info" text="원본 분석" />

enovia2025 webapps 의 `.js` 에서 찾은 3DSpace REST 경로입니다. 공통 규칙(헤더, 응답, 오류)은 [공통 규칙](/rest-api/) 을 봅니다.

- **횟수**: 소스에 그 경로 문자열이 나온 횟수입니다. `'/resources/v1/modeler/' + 이름` 처럼 조립하는 코드는 빠지므로 실제보다 적습니다.
- **메서드**: 경로 근처 코드에서 본 HTTP 메서드입니다. 빠진 메서드가 있을 수 있습니다.
- **계열**: A = 업무 객체(Foundation 형식, `data[].dataelements`), B = ds 서비스(`member[]`). [두 가지 계열](/rest-api/#두-가지-계열) 참고.
- 경로의 `{id}` 는 대부분 physicalid 입니다.

::: info 근거
`grep -o "resources/v1/modeler/[^\"' ]*"` 결과 299 건 (서비스 43 종), 그 밖의 `resources/...` 문자열 집계. 메서드는 경로 앞뒤 10~25 행의 `method:`, `type:`, `.GET(` 등을 집계. 2026 은 enovia2026_webapps 같은 방식
:::

샘플: [WidgetSample.js](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/WidgetSample/WidgetSample.js) (modeler/pno/person), [02_drag_drop.html](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/EventsSample/02_drag_drop.html) (v2/e6w/service/ObjectInfo)

## 공통 / 사용자

| 경로 | 횟수 | 메서드 | 설명 | 쓰는 모듈 (예) |
| --- | --- | --- | --- | --- |
| `/resources/v1/application/E6WFoundation/CSRF` | 60 | GET | CSRF 토큰. 응답 `{ csrf: { name, value } }` | DocumentManagement, ENOCharacteristicsApp 등 22 개 |
| `/resources/v1/application/CSRF` | 19 | GET | 위와 같음 | IssueServices 등 |
| `/resources/modeler/pno/person?current=true&select=...` | 59 | GET | 현재 사용자, 선호 컨텍스트, 협업공간 목록 | 45 개 모듈 |
| `/resources/pno/person/getsecuritycontext` | 38 | GET | `{ SecurityContext }` | 24 개 모듈 |
| `/resources/bps/cspaces` | 9 | GET | `{ cspaces: [...] }` 협업공간 목록 | ENORouteMgmt, ENXMeetingMgmt, Foundation |
| `/resources/enocsmrest/collabspaces?tenant=` | 25 | GET | 협업공간 목록 | W3DSpaceApp, CAT3DWInfra |
| `/resources/bps/physicalids?oid_list=` | 5 | GET | objectId → physicalid 변환 | ENOAEFCore |

자세한 것: [보안 컨텍스트](/rest-api/security-context)

## e6w 서비스

모든 타입에 쓰는 범용 서비스입니다. 응답은 v2 가 A 계열 모양이고, v3 는 `dataelements` 없이 `data[i].name` 처럼 읽습니다. 요청 파라미터, `$fields`, 찾기 조건은 [객체 정보, 사람과 객체 찾기 (e6w)](/rest-api/e6w) 를 봅니다.

| 경로 | 메서드 | 설명 | 쓰는 모듈 (예) |
| --- | --- | --- | --- |
| `/resources/v2/e6w/service/ObjectInfo/{id}` | GET | 객체 하나의 기본 정보 → `data[0]` | VCXWebPlatform, CAT3DExpModel, ENOGantt |
| `/resources/v2/e6w/service/ObjectInfo?$fields=...` | POST | 본문 `$ids=id1,id2` (form). 여러 객체 기본 정보 | ENOChgServices, IssueServices, CfgBaseUX |
| `/resources/v3/e6w/service/ObjectInfo/` | POST | v3. 본문 `$ids=` (form). [상세](/rest-api/e6w#받는-키-v3) | DocumentManagement `ObjectInfoService`, RDFFoundation |
| `/resources/v2/e6w/service/PersonSearch`, `PersonSearchByName` | GET | 사람 찾기 (`searchStr=`). [상세](/rest-api/e6w#사람-찾기-personsearch) | CollaborativeTasks, FolderEditor, ENOChgServices |
| `/resources/v2/e6w/service/ObjectSearch` | GET | 객체 찾기 (`searchStr=`, `typeStr=`). [상세](/rest-api/e6w#객체-찾기-objectsearch) | Foundation2, ENOProjectMgmtModels (v3) |
| `/resources/v3/e6w/service/ObjectSearchByName` | GET | 이름으로 객체 찾기 | FolderUpload |
| `/resources/v2/e6w/service/ExpresssionSearch/<이름 패턴>` | GET | 객체 찾기가 아니라 서버 설정 값 읽기. `Expresssion` 오타가 실제 경로. [상세](/rest-api/e6w#설정-값-읽기-expresssionsearch) | ENOChgServices, IssueServices |
| `/resources/v3/e6w/service/TypeAttributes/fetch` | POST | 타입의 속성 목록. 본문에 `csrf`, `application/ds-json`. [상세](/rest-api/dictionary#e6w-타입-서비스-v3) | ENORouteTemplateMgmt |
| `/resources/v3/e6w/service/TypeDerivatives?typePattern=` | POST (2026 GET) | 하위 타입 이름. [상세](/rest-api/dictionary#e6w-타입-서비스-v3) | E6WCommonApps, XSRCommonComponents (2026) |
| `/resources/v3/e6w/service/ParentTypes/{객체 id}` | GET | 그 객체 타입의 부모 타입들. 타입 이름이 아니라 객체 id 를 받음. [상세](/rest-api/dictionary#e6w-타입-서비스-v3) | E6WCommonApps |
| `/resources/v2/e6w/service/<이름>` | - | Foundation2 는 서비스 이름에 `/resources` 가 없으면 이 앞부분을 붙임 | Foundation2 `_serviceURL` |

v2 `ObjectInfo` 응답의 `dataelements` 에서 읽는 키: `name`, `title`, `revision`, `typeNLS`, `icon`, `image`, `fileExtension`, `firstname`, `lastname`. `$fields` 로 `ownerFullname,collabSpace,collabSpaceTitle,policy,organization,organizationTitle` 등을 더 받습니다.

## modeler: 업무 객체 서비스 (A 계열)

| 서비스 | 횟수 | 메서드 | 경로 예 | 쓰는 모듈 (예) |
| --- | --- | --- | --- | --- |
| `documents` | 80 | GET, POST, PUT, DELETE | `documents/{id}`, `documents/{id}/files`, `documents/files/CheckinTicket` | DocumentManagement, ENOXAttachmentsUX 등 20 개. [상세](/rest-api/documents) |
| `tasks` | 20 | GET, POST, PUT | `tasks?$terse=true&tenant=`, `tasks/{id}`, `tasks/tasksPreference`. 여러 건은 POST `$ids=` | CollaborativeTasks, ENOGantt, RouteWebAppUX |
| `routes` | 7 | GET, POST, PUT | `routes`, `routes/{id}`, `routes?whereUsed=` | ENORouteMgmt, ENOChgServices, RouteWebAppUX |
| `routetemplates` | 1 | - | `routetemplates` | ENORouteMgmt |
| `subscriptions` | 13 | GET, POST, PUT | `subscriptions`, `subscriptions/viewSubscriptions`, `subscriptions/getSubscriptionsbyPost`, `subscriptions/createPushSubscription`, `subscriptions/unsubscribeUserAndGroup` | ENOSubscriptionMgmt |
| `ESignRecord` | 7 | GET, PUT | `ESignRecord?ESignObjectRef=`, `ESignRecord/delegateAuthSuccess` | ENXESignature, ENORouteMgmt |
| `esign` | 6 | GET | `esign/configs`, `esign/configs/adoptEsigService`, `esign/configs/getTypeMaturity?type=` | ENXESignConfigApp |
| `meetings` | 2 | - | `meetings` | ENXMeetingMgmt |
| `decisions` | 2 | - | `decisions` | ENXDecisionMgmt |
| `projects` | 4 | GET (+ 동기화) | `projects/{id}/dpmgantt` | ENOGantt |
| `workspaces` | 2 | GET, POST | `workspaces/{id}?$include=all,folders`, POST `workspaces` 본문 `{ csrf, data }` | FolderUpload |
| `kanban` | 5 | GET, POST, PATCH | `kanban/boards?tenant=`, `kanban/boards/{id}`, `kanban/boards/searchByProjectId?projectId=` | E6WCommonApps, EditPropWidget |
| `commondocuments` | 1 | - | `commondocuments/documentaction/replacebyrevision` | DocumentCommands |
| `images` | 1 | DELETE | `images/{id}` | ImageManagement |
| `conversionAdmin` | 6 | - | `conversionAdmin/derivedformatmanagement/declarative`, `.../derivedoutputrule` | ENODerivedFormatManagement, UnifiedExportCommand |
| `characteristics` | 19 | GET, POST, PUT | `characteristics/performancespec/{id}/...`, `characteristics/product/{id}/performancechar/...`, `characteristics/perfspec/addextensions` | ENOCharacteristicsApp, XSRCommonComponents |
| `performancecharacteristic` | 1 | - | `performancecharacteristic/dimensions_and_units/dimensions` | ENOCharacteristicsApp |
| `specmanagement` | 1 | - | `specmanagement/types` | XSRUtilityCore |
| `savescopecontent` | 1 | POST | `savescopecontent?tenant=` | ENOXCADManageScope |
| `e6wproxy` | 2 | - | `e6wproxy/...` | LifecycleServices, OSLCConsumer |

## modeler: ds 서비스 (B 계열)

| 서비스 | 횟수 | 메서드 | 경로 예 | 쓰는 모듈 (예) |
| --- | --- | --- | --- | --- |
| `dseng` | 3 | (참조용) | `dseng/dseng:EngItem/{id}` | TerminologySelector (다른 요청의 `relativePath` 로 씀) |
| `dslib` | 13 | GET, POST, PATCH | `dslib/dslib:Library/search?$mask=dslib:LibraryDetailsMask`, `dslib:Library/{id}?$mask=dslib:ExpandClassesDetailsMask&$depth=20`, `dslib:Class/{id}?$mask=dslib:ClassAttributesMask`, `dslib:ClassifiedItem/{id}` (PATCH), `dslib:ClassifiedItem` (POST 분류), `dslib:ClassifiedItem/remove` | IssueServices, ENOCustomAttributeUtils, ENOProjectMgmtModels |
| `dslc` | 7 | POST | `dslc/maturity/changeState`, `dslc/maturity/getNextStates`, `dslc/ownership/transfer`, `dslc/version/create`. 응답은 B 계열의 `member[]` 가 아니라 `{ data }` / `{ results }`, 실패는 `errorReport[]` | FolderEditor, TeachersBookmarkEditor, ENONextGenFilterUX, DELWKOModelServices, DELPPWModelServices. [상세](/rest-api/lifecycle#dslc-caa-성숙도-api) |
| `dsprcs` | 15 | GET, POST | `dsprcs/dsprcs:MfgProcess/{id}`, `dsprcs/invoke/dsprcs:expand`, `dsprcs:DataCollectPlan/{id}/dsprcs:DataCollectRow` | DELWKOModelServices, DELWebPPRController |
| `dspfl` | 15 | GET, POST | `dspfl/dspfl:Unit/{id}?$mask=dsmvpfl:UnitBaseMask`, `dspfl:ModelVersion/{id}/dspfl:ProductConfiguration/{id}`, `dspfl/invoke/dspfl:getVariabilityDefinition` | ConfiguratorPanel, CfgFilterUX, DELWKOModelServices |
| `dsmfg` | 8 | GET, POST, PUT | `dsmfg/dsmfg:MfgItem/{id}`, `dsmfg/invoke/dsmfg:getMfgItemInstallDependencies` | DELPPWModelServices, DELWKOModelServices |
| `dswo` | 12 | GET, POST | `dswo/invoke/dswo:CreateWorkOrderFromProcess?$mask=dsmvwo:WorkOrderMask.DetailedStructure`, `dswo/productionplan`, `dswo/workorder?productionPlanId=` | DELWKOModelServices, DELWKOIRPCModels |
| `dscstr` | 8 | GET, POST | `dscstr/dscstr:CstrItem/{id}`, `dscstr:CstrItem/search?$searchStr=`, `dscstr:CstrItem/bulkfetch` (POST, id 배열), `dscstr:CstrItem/{id}/dscstr:CstrInstance?$mask=dsmvcstr:CstrItemMask.Instance` | CAT3DWebStoreyComponents |
| `dsspec` | 7 | GET | `dsspec/dsspec:PhysicalProduct/{id}`, `dsspec/invoke/dsspec:getCustomAttributes` | DMUBaseUI, DMUReadPersistence, ENOMadeFromUX |
| `dsxcad` | 4 | POST | `dsxcad/dsxcad:Template/`, `dsxcad/CheckinTicket` | ENOXCADTemplate, ENOXCADAdminSettingUI |
| `dssourcing` | 4 | - | `dssourcing/qualifications/...` | ENOXMEIQualification |
| `dssrc` | 1 | - | `dssrc/dssrc:ManufacturerEquivalentItem/` | ENOXMEIQualification |
| `dsrsc` | 1 | POST | `dsrsc/dsrsc:Resource/` | DELWKOModelServices |
| `dsmsr` | 3 | GET | `dsmsr/dsmsr:Simulation/{id}` | SPYLoader |
| `dslog` | 2 | - | `dslog/dslog:LogItemPRIVATE/` | CATFLCockpitWidget, ENOXArchitectureCommon |
| `dscfg` | 2 | POST | `dscfg/invoke/dscfg:getEffectivityContent` | DELWebOpen |
| `dsadvfilter` | 2 | GET, POST | `dsadvfilter/dsadvfilter:AdvFilter/` | ENONextGenFilterUX, LifecycleServices |
| `dsbsd` | 2 | POST, PATCH | `dsbsd/dsbsd:BrowsingStructure/`, `dsbsd:BrowsingNode/` | TerminologyEditor |
| `dsbks` | 1 | POST | `dsbks/dsbks:Bookmark?$mask=dsbks:BksMask.Details` | FolderActive |
| `dsfl` | 1 | POST | `dsfl/metaTypes?tenant=` | ParameterizationSkeleton. [상세](/rest-api/dictionary#dsfl-metatypes) |
| `dsmbse` | 1 | POST | `dsmbse/dsmbse:Model/search?tenant=` | 3DPlayMagicReportExperience |
| `mbse` | 1 | - | `mbse/i3dx/fetch` | RDFFoundation |
| `dssyseng` | 1 | - | `dssyseng/version` | RDFFoundation |

### 2026 에서 새로 보이는 서비스

| 서비스 | 횟수 (2026) | 경로 예 | 쓰는 모듈 |
| --- | --- | --- | --- |
| `dstdp` <Badge type="warning" text="2026" /> | 58 | `dstdp/packages/`, `dstdp/publications/` | ENOXPackageManagement 등 패키지 앱 |
| `dsdo` <Badge type="warning" text="2026" /> | - | `dsdo/dsdo:DerivedOutputs/`, `.../Locate` | ENOXPackageManagement, VCXWebComposer3DPlayExperience |
| `dseno` <Badge type="warning" text="2026" /> | - | `dseno/invoke/dseno:getInfo` | CATFLCockpitWidget |
| `dsmsrprotected` <Badge type="warning" text="2026" /> | - | `dsmsrprotected/dsmsr:Simulation/` (2025 의 `dsmsr` 대신) | SPYLoader |
| `dsodm` <Badge type="warning" text="2026" /> | - | `dsodm/orderdata?$mask=detailed_structure` | DELWKOModelServices |
| `dsserial` <Badge type="warning" text="2026" /> | - | `dsserial/serializeditem/` | DELWKOModelServices |
| `dstrs` <Badge type="warning" text="2026" /> | - | `dstrs/dstrs:Language`, `dstrs:Translation/bulkfetch` | DELWebOERCommonUtils |
| `dsvcp` <Badge type="warning" text="2026" /> | - | `dsvcp/publications` | ENOXPackageCommands |
| `exchangejobs` <Badge type="warning" text="2026" /> | - | `exchangejobs/files/checkinend` | UnifiedImport |
| `ipec` <Badge type="warning" text="2026" /> | - | `ipec/assets/classes` | ENOXPackageManagement |
| `quc` <Badge type="warning" text="2026" /> | - | `quc/printreasons`, `quc/filecategories/upload` | ParameterizationSkeleton |

2026 소스에는 `dsmsr`, `dssyseng`, `mbse` 문자열이 보이지 않습니다.

## 그 밖에 자주 보이는 3DSpace 경로

`modeler` 아래가 아닌 경로입니다. 응답 모양이 제각각이라 쓰기 전에 호출부를 직접 확인합니다.

| 경로 | 횟수 | 메서드 | 설명 | 쓰는 모듈 (예) |
| --- | --- | --- | --- | --- |
| `/resources/v1/FolderManagement/Folder` | 51 | - | 폴더(북마크) | FolderEditor, FolderActive |
| `/resources/modeler/change/changeaction` | 47 | - | 변경 조치 | ENOChgServices, CfgAuthoringContextUX |
| `/resources/v1/chguxservices/changeaction`, `changerequest`, `changeorder`, `investigationRequest` | 24~26 | - | 변경 관리 화면용 | ENOChgServices |
| `/resources/v1/collabServices/attributes` | 27 | POST, PUT, GET | 속성 정보. 대부분 `POST op/read`. 그 밖에 `PUT op/update`, `GET op/history`, `POST op/facets/available`. [상세](/rest-api/dictionary#객체-속성-읽기-collabservices) | 16 개 모듈 |
| `/resources/dictionary/getTypeInfo` | 21 | POST | 타입 정보 (본문 예 `'["Change Action"]'`). [상세](/rest-api/dictionary#하위-타입-gettypeinfo) | CfgBaseUX 등 15 개 |
| `/resources/enorelnav/v2/navigate` | 20 | POST | 관계 탐색. JSON 본문. 하위 경로 `getEcosystem`, `getRelationCount`, `getProfiles`, `setPreferences` 등. [상세](/rest-api/navigate) | ENORIPE_Relations, PIMwebUX 등 7 개 |
| `/resources/v1/dslc/versiongraph` | 14 | GET, POST | 버전 그래프. [상세](/rest-api/lifecycle#개정에-쓰는-다른-경로) | LifecycleServices, IssueServices |
| `/resources/lifecycle/maturity/getStateGraph` | 9 | POST | 상태(성숙도) 그래프. [상세](/rest-api/lifecycle#상태-그래프-읽기-getstategraph) | ChangeMaturityWidget, DocumentCommonInfra |
| `/resources/lifecycle/maturity/getStatesAttributes` | 11 | POST | state 이름과 색. 본문 `{ policies: [...] }`. [상세](/rest-api/lifecycle#갈-수-있는-state) | ENOChgServices, IPClassifyUXUtil, ENOXVersionExplorerController 등 9 개 |

## 3DSpace 가 아닌 서비스

아래는 이름이 비슷하지만 3DSpace 주소가 아닌 다른 서비스 주소로 부릅니다.

| 경로 | 서비스 | 비고 |
| --- | --- | --- |
| `/federated/search` | 3DSearch | 전체 검색. 응답 `results[].attributes[]`. [3DSearch 에서 골라 오기](/widget/search#화면-없이-검색-federated-search) |
| `/resources/3ddrive/v1/...` | 3DDrive | CSRF 헤더가 `X-DS-CSRFTOKEN` |
| `/api/authenticated/cas/transient` | 3DPassport | 임시 ticket |

서비스 주소 얻기는 [Compass 서비스의 getServiceUrl](/widget/compass-services#getserviceurl) 을 봅니다.
