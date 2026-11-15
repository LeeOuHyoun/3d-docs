# 속성 패널 (EditPropWidget) <Badge type="info" text="원본 분석" />

`DS/EditPropWidget/EditPropWidget` 는 3DEXPERIENCE 의 "정보(Information)" 화면, 곧 객체 머리글(ID 카드), 탭(속성, 관계, 이력 ...), 속성 보기 / 편집 / 저장을 **내 위젯 안의 요소에 그대로 넣는** 컨트롤입니다. physicalid 만 넘기면 속성을 서버에서 읽고, 고친 값을 저장하는 REST 까지 패널이 부릅니다.

::: info 근거
EditPropWidget/EditPropWidget.js (2025 15,125 줄, 2026 16,770 줄) 의 `DS/EditPropWidget/EditPropWidget` (init, initDatas, addFacets, removeFacets, selectFacet, setReadonlyStatus, onInnerResize, destroyComponent), `controllers/EditPropMainController` (defaultOptions, `_setup`, `setModels`, `_setModels`, `_onSelectionChange`), `UI/EditPropSkeleton`, `UI/IDCardSkeleton` (idcardInit, `_loadFacet`, `_manageFacetVisibility`, `_getCurrentSelectedFacet`, checkChangeAuthorized, destroyComponent), `UI/IDCardManager` (setOccurrences), `constants/EditPropConstants`, `constants/ConstantsMapping` (getMapping, getDefaultFacets), `models/EditPropModel`, `models/EditPropModelMerge`, `facets/Common/FacetsCore`, `FacetsBase`, `facets/Properties/EditPropController` (onSave, `_commitModels`, `_onCompleteCommit`), `ajaxRequests/3DSpaceAjaxRequests`, `ServicesAjaxRequests` (createRequest, `_createJSONModifiedAttribute`), `EditPropWidgetHTML`, `EditPropComponent`, `factory/EditPropComponentFactory`, EditPropWidget.html, EditPropWidget.css. WidgetServices.js 의 `commandProxy/CommandProxy`, `getSourceUrlAsync`, `getServicesUrl`, `getGrantedRoles`. ErrorWidget.js. LifecycleControls.js 의 `EditPropDialog` (`launchProperties`, `_launchTransientWidget`). 호출부: ENORouteMgmt (ContentInfoPropWidget, RouteInfoPropWidget, RoutePropertiesPanel, RouteHistory 탭), ENOUserGroupMgmt, ENXDecisionMgmt, ENOPartMgt (PropertiesView), XSRCommonComponents (XSpecProperties), FolderEditor, IssueManagement, LifecycleWidget, DocumentPreview, W3DSpaceApp, ENOChgGovernanceUX, IPClassifyUX, ENOXAttachmentsUX (EditPropCmdBase). 모듈 문자열이 나오는 webapps 폴더 수 (자기 폴더 제외): `EditPropConstants` 61, `FacetsBase` 55, `EditPropModel` 37, `EditPropWidget` 32, `EditPropUtils` 16, `EditPropCmdBase` 14, `EditPropComponent` 10. EditProp 모듈을 부르는 62 개 폴더의 파일에서 센 값: `typeOfDisplay` ALL 39 / ONLY_EDIT_PROPERTIES 6 / ONLY_IDENTITY_CARD 1, `selectionType` NO_SELECTION 38 / ALL_SELECTION 5 / RESTRICTED_SELECTION 2, `extraNotif:` 35, `editMode:` 34, `.initDatas(` 75 (35 폴더), `.addFacets(` 57, `.removeFacets(` 48 (`FACET_VIEWS` 15, `FACET_DERIVEDOUTPUT` 11), `"onSave"` 12, `action_close_panel` 19
:::

샘플: [07_properties_panel.html](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/ControlsSample/07_properties_panel.html) (physicalid 입력 → 패널, typeOfDisplay / readOnly / editMode 바꾸기, 이벤트 로그, 다시 읽기, 탭 고르기, 정리, 직접 만든 탭, OOTB 정보 창으로 열기). 3DSpace 데이터를 읽으므로 3DDashboard 나 3DSpace 서버(`<3DSpace>/webapps/ControlsSample/`)에서만 동작합니다. `npm run samples` 로 띄운 로컬 서버에는 3DSpace REST 가 없어 데이터가 나오지 않습니다.

## 한눈에

```js
require([
  'DS/EditPropWidget/EditPropWidget',
  'DS/EditPropWidget/constants/EditPropConstants',
  'DS/EditPropWidget/models/EditPropModel'
], function (EditPropWidget, EditPropConstants, EditPropModel) {
  var panel = new EditPropWidget({
    typeOfDisplay: EditPropConstants.ALL,
    selectionType: EditPropConstants.NO_SELECTION,
    readOnly: false,
    extraNotif: true
  });
  var model = new EditPropModel({ objectId: physicalId, metatype: 'businessobject', source: '3DSpace', tenant: platformId });
  panel.initDatas([model]);
  panel.inject(containerEl);
});
```

| 단계 | 하는 일 |
| --- | --- |
| 1 | `new EditPropWidget(옵션)` 으로 패널을 만듭니다. 옵션은 만들 때만 정합니다 |
| 2 | 보일 객체를 `EditPropModel` 배열로 `initDatas` 에 줍니다 |
| 3 | `inject(요소)` 로 붙입니다. 요소에는 **높이** 를 줍니다 |
| 4 | 패널이 보일 탭을 묻고, 속성을 읽고, 저장 버튼을 누르면 저장합니다 |

- 모듈은 클래스입니다. `new` 로 만듭니다. `UWA/Controls/Abstract` 를 상속합니다.
- OOTB 는 `panel.inject(el)` (IssueManagement, LifecycleWidget, DocumentPreview, FolderEditor) 와 `panel.elements.container.inject(el)` (ENORouteMgmt, ENOPartMgt, ENOUserGroupMgmt, XSRCommonComponents) 을 둘 다 씁니다. 같은 결과입니다.
- 화면 구성: 위에서부터 ID 카드(그림, 제목, 소유자, 날짜, 리비전, `actions` 버튼), 탭 막대, 탭 버튼 막대(속성 탭은 "편집", "표시 재설정"), 탭 내용, 맨 아래 오른쪽 버튼 줄(저장, 확장 추가, 탭이 넣은 버튼, 닫기)입니다.

## 패널 만들기

<<< @/../ControlsSample/07_properties_panel.html#create{js}

```css
.panel-area { position: relative; height: 560px; }
```

### 생성 옵션

| 옵션 | 기본값 | OOTB 에서 쓰는 값 | 설명 |
| --- | --- | --- | --- |
| `typeOfDisplay` | `ALL` | `ALL` 39, `ONLY_EDIT_PROPERTIES` 6, `ONLY_IDENTITY_CARD` 1 | 무엇을 보일지 ([아래 표](#보이는-모양-typeofdisplay)) |
| `selectionType` | `ALL_SELECTION` | `NO_SELECTION` 38, `ALL_SELECTION` 5, `RESTRICTED_SELECTION` 2 | 다른 앱의 선택을 따라갈지 ([아래 표](#선택을-따라갈지-selectiontype)) |
| `readOnly` | **읽기 전용** | `false` 가 대부분. 상태 / 권한에 따라 `true` | `false` 를 줘야 편집할 수 있습니다 |
| `editMode` | `false` | `false` | `true` 면 처음부터 편집 상태. `false` 면 "편집"(연필) 버튼으로 바꿈 |
| `extraNotif` | `false` | `true` 35 | `true` 면 저장 뒤 "수정 성공" 메시지 |
| `facets` | OOTB 탭 40 개 전부 | `[FACET_PROPERTIES]` 가 가장 많음 | 탭 후보 ([탭](#탭-facet)) |
| `context` | 없음 | `{ getSecurityContext: function () { return { SecurityContext: 'ctx::...' }; } }` | 읽기 / 저장 요청의 SecurityContext. 사용자에게 부여된 값인지 서버로 확인한 뒤 씀 ([WidgetServices](/widget/widget-services#securitycontextservices)) |
| `actions` | 없음 | `action_close_panel` 19 | ID 카드의 버튼 `[{ name, text, icon, handler, after }]`. `after: true` 면 다른 버튼 뒤에 놓임. `action_close_panel` 은 저절로 `after: true` |
| `events` | 없음 | `onNotification` | [이벤트](#이벤트) |
| `multiselection` | `MULTISEL_MERGE` | `MULTISEL_MERGE`, `false` | 여러 객체를 넣었을 때 ([여러 객체](#여러-객체)) |
| `warningOnUnsaved` | `true` | `false` 3 | 저장 안 한 값이 있을 때 다른 객체로 바꾸거나 닫으면 확인 창 ("일부 탭을 저장해야 합니다 ...") |
| `disableAddExtensionButton` | `false` | `true` 6 | 편집 상태의 "확장 추가"(인터페이스 붙이기) 버튼을 숨김 |
| `defaultFacetName` | `'properties'` | 1 | 처음 고를 탭. `initDatas` 둘째 인자로도 줌 |
| `showIdCard` | `ONLY_EDIT_PROPERTIES` 가 아니면 `true` | 1 | ID 카드를 보일지 |
| `showTopBar` | `true` | 1 | `false` 면 아래 버튼 줄(저장, 닫기)을 숨김. 팝업형은 이 줄(`controller.getTopBar()`)을 창 아래로 옮기려고 `false` 를 씀 |
| `setCloseButton` | `true` | | 속성 탭의 편집 중 "취소" 버튼을 보일지 |
| `noThumbnail`, `facetLabel` | `false`, `true` | | ID 카드 그림을 숨길지, 탭에 글자를 보일지 (`false` 면 아이콘만) |
| `className` | `''` | `'enxpropertiestab'` 등 | 패널 바깥 요소에 붙는 클래스 |
| `appId` / `sourceAppId` | 없음 | `'ENXWDOC_AP'`, `'X3DCSMA_AP'` 등 | 보일 탭을 물을 때 서버에 `sourceAppId` 로 보냄 |
| `displayType` | `STANDARD` | | EditPropWidget 은 `STANDARD`. 팝업 / 도킹 창은 [다른 모듈](#팝업과-도킹-창) |
| `closeButton` <Badge type="warning" text="2026" /> | `false` | | `true` 면 `STANDARD` 에서도 아래 버튼 줄에 닫기 버튼 ([2026 변경](#_2026-변경)) |
| `setEditButton`, `collapseTabs` | | 21, 10 폴더가 줌 | EditPropWidget 원본에 읽는 곳이 없음 (효과 없음) |

- 생성자의 둘째 인자로 `widget` 을 주면 패널 안의 `WidgetServices` 가 그 위젯 객체를 씁니다 (`setWidgetObject`). 주지 않으면 전역 `widget` 을 씁니다.
- 만들 때 단위 사전(`GET /resources/dictionary/dimensions`)을 한 번 읽습니다. 객체 속성은 `initDatas` 뒤에 읽습니다.

::: warning readOnly 를 주지 않으면 읽기 전용입니다
패널은 `readOnly !== false` 일 때 읽기 전용으로 만듭니다. 옵션을 빼면 "편집" 버튼과 저장이 나오지 않습니다. 편집하게 하려면 `readOnly: false` 를 줍니다. 그래도 서버가 고칠 수 없다고 한 속성(권한, 상태)은 읽기 전용입니다.
:::

::: warning 담는 요소에 높이를 줍니다
패널 바깥 요소는 `height: 100%` 이고, 탭 내용 영역은 남은 높이를 `position: absolute` 로 채웁니다. 높이가 없는 요소에 붙이면 ID 카드만 보이고 탭 내용 높이가 0 이 됩니다. OOTB 정보 앱은 위젯 본문 높이를 직접 정하고, LifecycleWidget 은 `height: 95%` 요소에 붙입니다. ID 카드 그림은 패널 높이가 300px 이상일 때만 보입니다.
:::

### 보이는 모양 (typeOfDisplay)

| 상수 | 값 | 보이는 것 |
| --- | --- | --- |
| `ALL` | `'editProp_all'` | ID 카드 + 탭 막대 + 탭 내용. OOTB 정보 창과 같은 모양 |
| `ONLY_EDIT_PROPERTIES` | `'editProp_onlyProperties'` | 탭 하나만. ID 카드와 탭 막대 없음 (ENOUserGroupMgmt, ENXDecisionMgmt, IssueManagement) |
| `ONLY_IDENTITY_CARD` | `'editProp_onlyIC'` | ID 카드 + 탭 하나. 탭 막대 없음 (ENOAttachmentsWebInWin). 이름과 달리 탭 내용을 숨기는 코드는 찾지 못했습니다 <Badge type="danger" text="검증 필요" /> |

- 탭 막대가 없는 두 모양은 `facets` 의 첫 탭을 보입니다. 단, `alwaysVisible: true` 인 탭이 있으면 그 탭을 먼저 보입니다. 그래서 OOTB 는 `facets: [FACET_PROPERTIES]` 하나만 줍니다.
- `selectFacet` 은 탭 막대가 있는 `ALL` 에서만 됩니다.

::: warning ALL 이 아니면 메시지가 화면에 나오지 않습니다
패널은 저장 성공, 서버 오류 같은 메시지를 패널 맨 위에 붙이는데, 붙일 자리는 `ALL` 에서만 정해집니다. `ONLY_EDIT_PROPERTIES` / `ONLY_IDENTITY_CARD` 에서는 메시지를 붙일 곳이 없어 보이지 않습니다. `events.onNotification` 에서 직접 띄웁니다 (ENXDecisionMgmt, IssueManagement 가 이 방식). "아이템을 선택하지 않음" 같은 큰 안내 문구는 모든 모양에서 보입니다.
:::

### 선택을 따라갈지 (selectionType)

| 상수 | 값 | 동작 |
| --- | --- | --- |
| `NO_SELECTION` | `'noSelection'` | 선택을 듣지 않습니다. `initDatas` 로만 내용이 바뀝니다. 내 화면에 넣는 패널은 거의 이것 |
| `ALL_SELECTION` (기본) | `'allSelection'` | PlatformAPI 주제 `DS/PADUtils/PADCommandProxy/select` 를 구독해 다른 앱이 알린 선택(`data.paths`, physicalid 경로 배열)을 따라갑니다. 메시지의 `appid` 가 내 앱 id 이면 무시 |
| `RESTRICTED_SELECTION` | `'restrictSelection'` | 같은 주제에서 `metadata.originWidgetId` 가 내 `widget.id` 인 메시지만 받습니다 (IPClassifyUX, IPClassifyActive, SNResultUX, 도킹형 정보 명령처럼 내 앱이 PADCommandProxy 로 선택을 알리는 경우). 실제로 어떤 메시지가 통과하는지는 확인하지 않았습니다 <Badge type="danger" text="검증 필요" /> |

- `stopSelection(true)` 로 잠시 선택을 무시하고, `stopSelection(false)` 로 다시 받습니다.
- [선택 (CSOManager)](/events/selection) 은 창 하나 안의 목록이라 이 패널과 상관없습니다. 내 그리드에서 고른 행을 보이려면 그리드의 [선택 변경](/datagridview/context-menu#선택-변경-받기) 에서 `initDatas` 를 부릅니다.

::: warning selectionType 을 빼면 다른 위젯의 선택으로 내용이 바뀝니다
기본값이 `ALL_SELECTION` 입니다. 내 화면 전용 패널이면 `selectionType: EditPropConstants.NO_SELECTION` 을 꼭 줍니다. OOTB 는 38 곳 (31 폴더) 이 이렇게 줍니다.
:::

## 객체 넣기 (initDatas)

<<< @/../ControlsSample/07_properties_panel.html#data{js}

`initDatas(models, defaultFacetName, infoForFacet)` 의 `models` 항목은 `EditPropModel` 이나 같은 키를 가진 일반 객체입니다 (일반 객체는 패널이 `EditPropModel` 로 바꿈. XSRCommonComponents 가 이 방식). 모델에 이벤트를 달려면 `EditPropModel` 로 만듭니다.

| 키 | 기본값 | 설명 |
| --- | --- | --- |
| `objectId` | `''` | **physicalid**. 저장 요청의 `model/bus/<physicalid>` 가 이 값입니다 |
| `metatype` | `'unknown'` | `'businessobject'` (OOTB 30 곳), `'relationship'`. 읽기 요청에서 `busIDs` / `relIDs` / `lIds` 중 어디에 넣을지 정함 |
| `source` | `'3DSpace'` | 어느 서비스에서 읽을지. `'3DDrive'`, `'3DSwym'`, `'www'` 등도 있음 |
| `tenant` | 모듈을 불러온 때의 `x3dPlatformId` | 플랫폼 id. 요청 주소에 `?tenant=` 로 붙음. 온프레미스 OOTB 는 `'OnPremise'` 를 쓰기도 함 (LifecycleWidget, ENOUserGroupMgmt) |
| `isTransient` | `false` | `true` 면 서버에서 읽지 않고 모델에 넣은 속성만 보임 |
| `type` | 없음 | 타입 이름. 알면 넣음 (LifecycleWidget) |
| `instrefid` | `''` | `relationship` 일 때 같이 읽을 객체 id. 읽기 요청의 `busIDs` 와 `mains` 에 들어감 |
| `i3dx` | `''` | 3DSearch(인덱스 모드) 객체 id |

- `initDatas` 둘째 인자는 처음 고를 탭 이름, 셋째는 탭에 넘길 정보(`infoForFacet`)입니다. FolderEditor 는 `initDatas(models, 'Classification')`, `initDatas(models, 'relations')` 로 엽니다.
- `initDatas([])` 면 "아이템을 선택하지 않음" 을 보입니다 (DocumentPreview).
- `initData` 는 같은 함수의 다른 이름입니다.
- 트리 노드(`getID()`, `getInstID()` 를 가진 객체)에서 모델을 만들 때는 `DS/EditPropWidget/utils/EditPropUtils` 의 `getModelFromSelectedNode(node)` 를 씁니다 (14 폴더).
- 저장 안 한 값이 있으면 `initDatas` 가 먼저 확인 창을 띄우고, 사용자가 확인해야 바뀝니다 (`warningOnUnsaved`).
- 패널은 첫 객체의 `tenant` 를 위젯 값 `x3dPlatformId` 에 씁니다. 사용자가 편집 / 보기를 바꾸면 위젯 값 `editProperties_editMode` 도 씁니다.

::: tip 직접 만든 탭이 처음 탭이 될 수 있습니다
처음 그릴 때 아직 고른 탭이 없으면 패널은 `alwaysVisible: true` 인 첫 탭을 고르고, 이 이름이 기본 탭(`'properties'`)보다 앞섭니다. 직접 만든 탭을 넣었으면 샘플처럼 `initDatas(models, EditPropConstants.FACET_PROPERTIES)` 로 처음 탭을 줍니다 <Badge type="danger" text="검증 필요" />.
:::

### 모델에서 값 읽기

| 메서드 | 결과 |
| --- | --- |
| `panel.getListModel()` | 지금 모델 배열. 여러 객체면 합친 모델이 맨 앞 |
| `panel.getTitle()` | 첫 객체 제목 (`label`) |
| `model.get('속성 id')` | 속성 모델. `.value` (보이는 값), `.valueDB` (저장 값), `.name`, `.label`, `.readOnly`, `.mandatory`. IssueManagement 는 `model.get('Title').value`, `model.get('type').value` 를 읽음 |
| `model.getChangedValues()` | 저장 전 편집 중인 속성 `{ 속성 id: 속성 모델 }` |
| `model.getAllAttributes()` | 모든 값. ENORouteMgmt 는 `getAllAttributes().type.dbValues.value` 로 타입을 읽음 |

## 이벤트

| `events` 옵션 | 언제 | 인자 |
| --- | --- | --- |
| `onNotification` | 패널이 메시지를 낸 뒤 (저장 성공, 서버 오류, 경고) | `{ eventID: 'success' / 'info' / 'warning' / 'error', msg }` |
| `onTitleChanged` | 첫 객체의 제목이 정해지거나 바뀐 뒤. 오류로 비우면 `''` | 제목 문자열 (HTML 이 섞일 수 있음) |
| `onDataLoaded` | 속성 탭이 처음 다 그려진 뒤. 패널 하나에 **한 번만** | 없음 |
| `onCancel` | 아래 버튼 줄의 닫기 버튼 (확인 창 통과 뒤). 2025 의 `STANDARD` 에는 닫기 버튼이 없음 | 없음 |
| `onPrimaryChange` | ID 카드 그림(대표 이미지)을 바꾼 뒤 | `{ id, image }` |

| 모델 이벤트 (`model.addEvent`) | 언제 | 인자 |
| --- | --- | --- |
| `onSave` | 저장이 끝나고 서버 값으로 모델을 고친 뒤 (ENORouteMgmt, ENOUserGroupMgmt, ENXDecisionMgmt, ENOChgGovernanceUX 는 여기서 자기 화면을 다시 읽음) | 없음 |
| `onChangeAttributes` | 값을 읽어 올 때, 편집할 때, 저장 결과를 반영할 때 | `(model, [속성 모델 ...])` |
| `onReset:속성 id` | 그 속성 값을 되돌렸을 때 | 없음 |

- 패널을 닫을 때(닫기 버튼, 팝업 닫기) `onCancel` 다음에 패널이 스스로 `destroyComponent()` 를 부릅니다.
- IssueManagement 는 `onChangeAttributes` 에서 날짜 순서를 검사하고 틀리면 속성 모델의 `resetInitialValues()` 로 되돌립니다. DocumentPreview 는 여기서 마지막에 바뀐 속성을 기억했다가 `onSave` 에서 씁니다.
- `onSave` 를 한 번만 받으려면 `model.listenToOnce(model, 'onSave', fn)` (IssueManagement).

## 저장과 REST

```text
initDatas([model])
  → POST /resources/v1/collabServices/attributes/op/facets/available   보일 탭
  → Compass 에 서비스 목록, 부여된 역할을 물음                          탭의 services / roles 조건
  → 탭 모듈을 require 로 불러와 만듦 (속성 탭은 늘 만듦)
  → POST /resources/v1/collabServices/attributes/op/read               속성 값, 정의, 쓰기 권한
값을 고치면 → 아래 버튼 줄에 "저장" 버튼
"저장" → 보이는 탭마다 onSave
  → (속성 탭) 업무 규칙(UX BL)이 배포되어 있으면 그 검사, 입력 값 검사
  → PUT /resources/v1/collabServices/attributes/op/update?propertiesFacet=true
  → 응답 값으로 모델을 고침 → 모델 onSave → extraNotif 면 "수정 성공"
  → PlatformAPI 'DS/PADUtils/PADCommandProxy/refresh' 로 고친 physicalid 를 다른 앱에 알림
```

| 시점 | 요청 | 내용 |
| --- | --- | --- |
| 만들 때 | `GET /resources/dictionary/dimensions` | 단위 사전 |
| `initDatas` | `POST .../attributes/op/facets/available` | 본문 `{ id, facets: [{ name, inheritance }], sourceAppId }`. 여러 객체면 `ids` |
| 속성 탭 | `POST .../attributes/op/read` | 본문 `{ lIds, busIDs, relIDs, attributes, plmparameters, navigateToMain, readonly, runUXBL: 'true', ... }`. metatype 이 `EBOM` 이면 `/resources/v1/proto/attributes/op/read`. 응답 키는 [타입, 속성 정보](/rest-api/dictionary#객체-속성-읽기-collabservices) |
| 저장 | `PUT .../attributes/op/update?propertiesFacet=true` | 아래 본문 |
| 옛 이력 탭 (`FACET_ACTIVITY`) | `GET .../attributes/op/history?id=...&nbResults=20` | `FACET_GENERALHISTORY` 탭은 `DS/ENXHistoryUX/View/HistoryFacet` 에 맡김 |
| 확장 추가 | `POST /resources/dictionary/class/interfaces?tenant=...` | 붙일 수 있는 인터페이스 ([dictionary/class](/rest-api/dictionary#타입-속성-dictionary-class)) |

- `...` 는 `/resources/v1/collabServices` 입니다. 3DSpace 주소는 `WidgetServices.getSourceUrlAsync` 로 구하고, 모든 주소 끝에 `tenant=<tenant>` 가 붙습니다.
- 요청은 [WAFData](/uwa/request) `handleRequest` (`proxy: 'passport'`) 이고 헤더에 `SecurityContext`, `Accept-Language` 를 넣습니다. 변경 조치(Change)가 정해져 있으면 그 헤더도 붙습니다. 헤더 규칙은 [REST 공통 규칙](/rest-api/#헤더) 과 같습니다.

저장 본문 (객체마다 한 요청, 여러 객체면 `transactionid` 로 묶음):

```json
{
  "requests": [
    {
      "path": "model/bus/<physicalid>",
      "method": "PATCH",
      "type": "<type>",
      "body": [{ "op": "replace", "path": "<속성 path>", "value": "<새 값>" }],
      "queryParams": { "select": ["physicalid", "modified", "<속성 select>"] }
    }
  ]
}
```

- 관계를 가리키는 속성은 `op: 'connect'` / `'disconnect'` 로 보내고, PLM 파라미터는 `model/plmparameter/<id>` 경로로 보냅니다.
- 응답의 결과마다 `status` 가 200 대가 아니면 "업데이트 중 오류" 를 보이고, 속성마다 오류가 있으면 그 칸에 표시합니다.
- 편집 중 "취소" 는 값만 되돌리고 서버를 부르지 않습니다.

## 탭 (facet)

`facets` 의 항목은 세 가지입니다.

| 형식 | 예 | 뜻 |
| --- | --- | --- |
| 상수 문자열 | `EditPropConstants.FACET_PROPERTIES` | OOTB 탭 |
| `{ id, ... }` | `{ id: EditPropConstants.FACET_CHANGE, options: { readOnly: true } }` | OOTB 탭 정의에 키를 덮어씀 (XSRCommonComponents) |
| 탭 정의 객체 | `{ require, alwaysVisible, facet: { name, text, icon }, options }` | 직접 만든 탭 ([아래](#직접-만든-탭)) |

- 넣은 탭 중 **서버가 그 객체에 쓸 수 있다고 한 탭** 만 보입니다 (`facets/available`). 탭이 하나면 탭 막대를 숨깁니다.
- `facets` 에 속성 탭이 없으면 패널이 보이지 않는 속성 탭을 더합니다 (ID 카드 값을 이 탭이 읽음).
- `addFacets([...])` 로 나중에 더하고(`position` 으로 위치), `removeFacets(['views'])` 로 뺍니다. 기본 탭 전부를 쓰는 OOTB 는 `removeFacets([FACET_VIEWS])` 15 곳, `removeFacets([FACET_DERIVEDOUTPUT])` 11 곳입니다.

자주 쓰는 OOTB 탭 (호출부의 상수 사용 순):

| 상수 | 이름 | 탭 | 모듈 | 조건 |
| --- | --- | --- | --- | --- |
| `FACET_PROPERTIES` | `properties` | 속성 | `DS/EditPropWidget/facets/Properties/EditPropController` | 늘 만듦, 여러 객체 가능 |
| `FACET_VIEWS` | `views` | 보기 (3D) | `DS/EditPropWidget/facets/3DPlay/3DPlayFacet` | |
| `FACET_SWYMCOMMENTS` | `social` | 코멘트 | `DS/EditPropWidget/facets/SwymComments/SwymComments` | 서비스 `comment` |
| `FACET_DERIVEDOUTPUT` | `derivedoutputs` | 파생 출력 | `DS/ENODODisplay/commands/facets/DerivedOutputFacet` | 역할 CSV, InternalDS |
| `FACET_RELATIONS` | `relations` | 관계 | `DS/ENORIPE_Relations/views/RelationsTab` | |
| `FACET_CHANGE` | `Change` | 변경 | `DS/ENOChgActionProp/facets/ChangePropertiesFacet` | |
| `FACET_SHARING` | `sharing` | 공유 | `DS/ENOSharingWebUI/ENOSharingWebUI` | |
| `FACET_GENERALHISTORY` | `history` | 이력 | `DS/EditPropWidget/facets/GeneralHistory/History` (안은 `DS/ENXHistoryUX/View/HistoryFacet`) | 서비스 `3DSpace` |
| `FACET_INSTANCE` | `instance` | 인스턴스 | `DS/EditPropWidget/facets/Instance/InstanceFacet` | 여러 객체 가능 |
| `FACET_REVISIONS` | `revisions` | 리비전 | `DS/ENOXVersionExplorerUtils/RevisionsTabFacet` | |
| `FACET_EFFECTIVITY` | `Effectivity` | 유효성 | `DS/CfgWebAppEffectivityUX/facets/EffectivityFacet` | 역할 CSV, InternalDS |
| `FACET_FILES` | `Files` | 파일 | `DS/DocumentFiles/FilesFacet` | |
| `FACET_CLASSIFICATIONS` | `Classification` | 분류 | `DS/ClassificationsFacet/ClassificationFacet` | |
| `FACET_IPPROTECTION` | `IPProtection` | IP 보호 | `DS/ENOIPProtection/IPProtection/IPProtectionController` | 역할 IPA, IPS, InternalDS |

나머지 상수: `FACET_ACTIVITY` (`activity`, 옛 이력), `FACET_DOMAINPROPERTIES`, `FACET_iPROPERTIES`, `FACET_iINSTANCE`, `FACET_iCHANGE` (3DSearch 인덱스 모드용), `FACET_CONSTRUCTION`, `FACET_EBOM`, `FACET_SPECIFICATIONS`, `FACET_Variability`, `FACET_ALTERNATE`, `FACET_PROXY`, `FACET_SWYM_CONTENT`, `FACET_SWYM_COMMUNITY_PROPERTIES`, `FACET_SWYM_USER_PROPERTIES`, `FACET_SWYM_SURVEY_PROPERTIES`, `FACET_DOCUMENTS`, `FACET_EXPORTED_DOCUMENTS`, `FACET_RICHCONTENT`, `FACET_COVERAGELINKS`, `FACET_PC_CONTENT`, `FACET_FL_SPECIFICATION`, `FACET_CONTENTS`, `FACET_MEMBERS`, `FACET_ATTACHMENT`, `FACET_ADMIN`, `FACET_POSTERRORANALYSIS`, `FACET_PHYSICSSIMULATION`, `FACET_NONE`.

3D 미리 보기 탭(`facets/3DPlay/3DPlayFacet`)은 [3D / 2D 미리 보기](/widget/3dplay) 의 `Player3DPlayWeb` 을 씁니다.

### 직접 만든 탭

`DS/EditPropWidget/facets/Common/FacetsBase` (55 폴더) 를 상속한 모듈을 만들고 탭 정의의 `require` 에 그 이름을 줍니다. ENORouteMgmt 의 `RouteHistory` 와 같은 모양입니다.

<<< @/../ControlsSample/07_properties_panel.html#facet{js}

| 탭 정의 키 | 설명 |
| --- | --- |
| `require` | 탭 모듈 이름. 탭이 처음 필요할 때 `require` 로 불러옴 |
| `facet` | `{ name, text, icon }`. `name` 은 탭 id (`selectFacet`, `removeFacets` 에 씀), `icon` 은 3DS 글꼴 아이콘 이름 (`'info'`, `'clock'`, `'object-related'` ...) |
| `alwaysVisible` | `true` 면 서버의 탭 확인 없이 보임. 서버는 직접 만든 탭을 모르므로 꼭 줌 |
| `options` | 탭 모듈의 `this.options`. 패널이 `tabId`, `events` 를 더하고, 비어 있지 않으면 `infoForFacet`, `tenant` 도 더함 |
| `position` | `addFacets` 에서 끼울 위치 |
| `services` / `roles` | 이 서비스가 플랫폼에 있어야 / 이 역할 중 하나가 있어야 보임 (`['3DSpace']`, `['CSV']`) |
| `supportMerge` | `true` 가 아니면 여러 객체일 때 서버에 묻지 않음 |
| `alwaysCalled` | 지금 고른 탭이 아니어도 미리 만듦 (속성 탭이 이것) |
| `actions` | 탭 아래 버튼 `[{ name, text, icon }]`. 누르면 탭의 `onAction(name)` |

| 탭 모듈 메서드 | 패널이 부르는 때 |
| --- | --- |
| `init(options, sharedOptions)` | 처음 만들 때. `this.elements.container` 를 만듦 |
| `updateFacet()` | 보일 객체가 바뀐 뒤 탭이 보일 때. 같은 객체면 다시 부르지 않음 |
| `onRefresh()` | 다시 읽기 (기본은 `updateFacet()`) |
| `onSave()`, `nothingToSave()` | "저장" 버튼 / 저장할 것이 있는지 |
| `getButtons()` | 아래 버튼 줄에 넣을 요소 |
| `onAction(name)`, `isActionAvailable(name)` | 탭 `actions` 버튼 |
| `onResize()`, `onClose()`, `destroyComponent()` | 크기 변경, 패널 닫힘, 정리 |

- 탭 안에서는 `this.getModel()` (지금 객체), `this.isReadOnly()`, `this.switchSaveButton(true)` (저장 버튼 보이기), `this.showLoader()` / `hideLoader()`, `this._raiseNotification(msg, 'success')` 를 씁니다 (`FacetsCore`).
- 여러 객체를 넣으면 `getModel()` 이 합친 모델입니다. RouteHistory 는 `isMergedModel()` 이면 아무것도 하지 않습니다.
- 이 샘플 탭은 서버에서 실행해 보지 않았습니다 <Badge type="danger" text="검증 필요" />.

## 다시 읽기, 읽기 전용, 크기, 정리

<<< @/../ControlsSample/07_properties_panel.html#control{js}

| 메서드 | 설명 | OOTB |
| --- | --- | --- |
| `onRefresh()` | 지금 객체를 비웠다가 다시 넣어 서버에서 다시 읽음 | W3DSpaceApp |
| `setReadonlyStatus(flag)` | 읽기 전용을 바꾸고 다시 그림 | FolderEditor (CATIA 세션과 타입에 따라) |
| `selectFacet(name)` | 보이는 탭으로 바꿈 (`ALL` 만) | |
| `addFacets(defs)` / `removeFacets(names)` | 탭 더하기 / 빼기 | 26 / 17 폴더 |
| `stopSelection(flag)` | 선택 따라가기를 멈춤 / 다시 | |
| `onInnerResize()` | 크기가 바뀐 뒤 다시 맞춤. `onResize()` 는 비어 있음 | |
| `getContent()` | 안쪽 화면 객체(EditPropSkeleton). DOM 요소는 `panel.elements.container.getContent()` (FolderEditor) | |
| `getIdentityCardUI()`, `getMainTable()` | ID 카드 영역 / 탭 내용 영역 요소 | |
| `destroyComponent()` | 탭마다 `destroyComponent`, 탭 막대 정리, 선택 구독 해제. **화면 요소는 남음** | IssueManagement |
| `show(models)`, `centerIt()` | 팝업 / 도킹형 창을 보이고 `initDatas` / 팝업형 창을 가운데로. 일반 패널에서는 둘 다 아무것도 안 함 | |

- 정리 순서는 IssueManagement 와 같습니다: 모델 `destroy()`, `destroyComponent()`, 담는 요소 비우기. OOTB 정보 앱은 창 `unload` 때 `destroyComponent()` 를 부릅니다.

::: warning ALL 이 아닌 패널의 destroyComponent 는 오류가 납니다
`destroyComponent` 는 탭 막대를 꼭 지우는데, 탭 막대는 `ALL` 에서만 만듭니다. `ONLY_EDIT_PROPERTIES` / `ONLY_IDENTITY_CARD` 패널에서 부르면 `Cannot read properties of undefined (reading 'destroy')` 가 납니다 (2025, 2026 같음). 앞쪽 탭 정리는 이미 끝난 뒤이므로 IssueManagement 처럼 `try / catch` 로 감싸고 담는 요소를 비웁니다.
:::

## 여러 객체

`initDatas([m1, m2, ...])` 로 여러 개를 넣으면 패널이 합친 모델(`EditPropModelMerge`)을 만들어 공통 속성을 한 화면에서 고치게 합니다.

- 값이 객체마다 다르면 여러 값으로 보이고, 저장하면 `transactionid` 하나로 묶은 PUT 한 번으로 보냅니다. 각 객체 모델에 `onSave` 가 납니다.
- 같은 physicalid 가 둘이면 하나만 씁니다.
- `supportMerge` 가 없는 탭은 숨습니다. OOTB 에서 여러 객체를 받는 탭은 속성, EBOM, 인스턴스, 제품 구성 내용 정도입니다.
- tenant 나 source 가 다르면 "속성 보기는 다른 테넌트(소스)에서 사용할 수 없음" 을 보입니다.
- `multiselection: EditPropConstants.MULTISEL_NO` 면 둘 이상일 때 "너무 많은 개체가 선택됨" 을 보입니다.

## 상수 (EditPropConstants)

| 묶음 | 상수와 값 |
| --- | --- |
| 모양 | `ALL` `'editProp_all'`, `ONLY_EDIT_PROPERTIES` `'editProp_onlyProperties'`, `ONLY_IDENTITY_CARD` `'editProp_onlyIC'` |
| 선택 | `ALL_SELECTION` `'allSelection'`, `RESTRICTED_SELECTION` `'restrictSelection'`, `NO_SELECTION` `'noSelection'` |
| 띄우는 방식 (`displayType`) | `STANDARD` `'editProp_standard'`, `POPUP` `'editProp_popup'`, `DOCKABLE` `'editProp_dockable'`, `TRANSIENT` `'editProp_transient'`, `PINNED` `'editProp_pinned'` <Badge type="warning" text="2026" /> |
| 여러 객체 | `MULTISEL_STD` `'yes'`, `MULTISEL_NO` `'no'`, `MULTISEL_MERGE` `'merge'` |
| 편집 강제 (탭 initData 의 `edition`) | `FORCE_READWRITE` `'force_readWrite'`, `FORCE_READONLY` `'force_readonly'`, `FORCE_DEFAULT` `'force_default'`, `NO_CHANGES` `'no_changes'` |
| metatype | `METATYPE_BUS` `'businessobject'`, `METATYPE_REL` `'relationship'`, `METATYPE_PATH` `'path'`, `METATYPE_UNKNOWN` `'unknown'` |
| 탭 아래 버튼 | `ACTION_SWITCH` `'pencil'` (편집), `ACTION_RESET` `'reset'` (표시 재설정), `ACTION_REFRESH` `'cw'`, `ACTION_SHARING_*`, `ACTION_SWYM_*` |
| 탭 | `FACET_*` ([탭](#탭-facet)) |
| 기타 | `MVA_SEP` <code>'&#124;'</code> (여러 값 구분), `MODEL_INIT` `'_Model_init'`, `MODEL_DS_ATTR` `'_DS_ATTR_'`, `TYPES` (`integer`, `real`, `timestamp`, `person`, `boolean`, `enum`), `COLLAPSE_IC_PREFERENCE` (위젯 설정 `editProperty_collapseIC`) |

## OOTB 정보 창으로 열기

내 화면에 넣지 않고 3DDashboard 의 OOTB 정보 창을 띄울 수도 있습니다. OOTB "정보(Information)" 명령이 쓰는 `DS/ENOCollabSharingCmds/commands/EditPropCmd/EditPropCmdBase` (14 폴더, `DS/LifecycleControls/EditPropDialog` 를 그대로 물려받음) 의 `launchProperties` 에 선택 객체를 주는 context 를 넘깁니다 (ENOXAttachmentsUX 방식).

<<< @/../ControlsSample/07_properties_panel.html#ootb-window{js}

```text
launchProperties(context)
  → context.getSelectedNodes() 를 EditPropUtils.getModelFromSelectedNode 로 모델로 바꿈
  → 이 위젯이 미리보기 창 안 / Web-in-Win / 데스크톱 앱 안이면 위젯 안 팝업 (EditPropComponent, POPUP)
  → 대시보드에 정보 창이 이미 떠 있으면 새로 열지 않고 그 창을 다시 보이며 CommandProxy.publishUpdate 를 보냄
  → 아니면 TransientWidget.showWidget('ENOLCMT_AP', 'ENOVIA - Information ', 넘길 값, { mode: 'panel', width: 650, height: 800 })
```

`ENOLCMT_AP` 가 EditPropWidget.html (OOTB 정보 앱)입니다. 이 앱은 `selectionType: ALL_SELECTION` 이라 열린 뒤 다른 앱의 선택도 따라갑니다. TransientWidget 으로 직접 열 때 넘기는 값은 OOTB 명령이 만드는 것과 맞춥니다.

| 키 | 값 |
| --- | --- |
| `SelectedItem` | 객체 배열 `[{ objectId, metatype, tenant, source }]` (`EditPropModel.toJSON()`) |
| `x3dPlatformId` | 플랫폼 id |
| `isTransient` | `'true'`. 정보 창의 닫기 버튼이 `TransientWidget.closeWidget()` 을 부름 |
| `loadData` | `true` |
| `securityContext` | `'ctx::...'` |
| `readOnly` | 위젯 값 `editProperties_readOnly` 가 있으면 그 값 |
| `parent_x3dAppId` | 연 쪽 앱 id |

- 이 중 무엇이 꼭 있어야 하는지는 확인하지 않았습니다 <Badge type="danger" text="검증 필요" />. OOTB 명령을 쓰는 편이 안전합니다.
- 정보 앱은 주소 파라미터도 읽습니다: `selected` (physicalid), `meta`, `tenant`, `source`, `sourceName`, `readonly`, `editMode`, Web-in-Win 에서는 `defaultFacetName` 도. 브라우저로 `<3DSpace>/webapps/EditPropWidget/EditPropWidget.html?selected=<physicalid>` 를 열어도 되는지는 확인하지 않았습니다 <Badge type="danger" text="검증 필요" />.

::: warning ENOLCMT_AP 는 objectId 키를 읽지 않습니다
정보 앱은 넘어온 값 중 `SelectedItem` (과 `X3DContentId`) 만 보고 객체를 정합니다. `showWidget('ENOLCMT_AP', '', { objectId: id })` 처럼 열면 "아이템을 선택하지 않음" 이 보입니다. `SelectedItem` 배열로 넘기거나 위의 `launchProperties` 를 씁니다.
:::

## 팝업과 도킹 창

`DS/EditPropWidget/factory/EditPropComponentFactory` 의 `createFactory(options)` 는 `displayType` 으로 만들 모듈을 고릅니다. 옵션과 `initDatas` 는 EditPropWidget 과 같습니다.

| `displayType` | 모듈 | 모양 | OOTB |
| --- | --- | --- | --- |
| `STANDARD` (기본) | `DS/EditPropWidget/EditPropWidget` | 내 요소 안 | 이 페이지 |
| `POPUP` | `DS/EditPropWidget/EditPropComponent` (10 폴더) | 위젯 안 떠 있는 창 (`ENOFloatingPanel`, 제목 "ENOVIA - Information", 고정(pin) 버튼은 정보 앱으로 엶) | LifecycleControls (정보 명령), ENOChgServices, ENORIPE_Relations |
| `DOCKABLE` | `DS/EditPropWidget/dockablecomponent/EditPropDockableComponent` | ImmersiveFrame 오른쪽에 붙은 `DS/Windows/Panel` | ENOCollabSharingCmds (DockableInformationCmd) |
| `TRANSIENT` | 없음 (`undefined`) | 정보 앱 안에서 쓰는 값 | EditPropWidget.html |

- 팝업과 도킹형은 `component.show(models)` 로 창을 보이며 `initDatas` 까지 합니다. 창의 닫기는 `onCancel` 을 냅니다. 도킹형은 `immersiveFrameFromApp` 에 앱의 ImmersiveFrame 을 줄 수 있습니다.
- 이 두 모양은 실행해 보지 않았습니다 <Badge type="danger" text="검증 필요" />. 창 자체는 [Windows API](/controls/api/windows) 를 봅니다.

## 고르는 기준

| 하고 싶은 일 | 쓸 것 |
| --- | --- |
| 내 화면 오른쪽에 고른 객체의 속성을 늘 보이기 | EditPropWidget (`NO_SELECTION`) 을 [DockingElement](/controls/docking-element) 오른쪽 영역에 넣음 |
| 버튼 하나로 OOTB 정보 창을 띄우기 (탭, 편집, 저장 모두 OOTB) | `EditPropCmdBase().launchProperties(context)` → `ENOLCMT_AP` ([TransientWidget](/widget/transient-widget)) |
| 위젯 안에 떠 있는 속성 창 | `EditPropComponent` (`POPUP`) |
| 확인, 짧은 입력처럼 내용을 직접 그리기 | [Dialog](/controls/dialog) + WebUX 입력 컨트롤 |
| 객체 머리글(배너, 제목, 명령)만 | [IDCard](/controls/controls-ex#idcard) |

| | EditPropWidget (내장) | OOTB 정보 창 (`ENOLCMT_AP`) |
| --- | --- | --- |
| 위치 | 내 위젯 안 요소 | 3DDashboard 위의 별도 창 (650 × 800) |
| 코드 | 만들기, `initDatas`, `inject`, 정리 | `launchProperties` 한 번 |
| 탭과 버튼 | `facets`, `actions`, 직접 만든 탭 | OOTB 기본 탭 |
| 저장 뒤 내 화면 갱신 | 모델 `onSave` 로 바로 | 다른 위젯이므로 PlatformAPI `DS/PADUtils/PADCommandProxy/refresh` 구독 <Badge type="danger" text="검증 필요" /> |
| 3DDashboard 밖 | [아래](#_3ddashboard-밖에서) | 창이 열리지 않음 |

## 3DDashboard 밖에서

- 패널 안 여러 곳이 전역 `widget` 의 `getValue`, `id` 를 바로 씁니다. 전역 `widget` 이 없으면 WidgetServices 가 빈 `UWA/Widget` 을 만들어 `window.widget` 에 넣지만 설정 값(`x3dPlatformId` 등)이 비어 있습니다. 단독으로 열 페이지는 샘플처럼 `UWA_Standalone_Alone.js` 를 넣은 위젯 HTML 로 만듭니다 ([위젯 실행 환경](/widget/#실행-환경)).
- 3DSpace 주소는 Compass 로 구하고, 실패하거나 10 초 안에 답이 없으면 `<protocol>//<hostname>/3DSpace` (포트 없음)를 씁니다 ([get3DSpaceUrlAsync](/widget/widget-services#get3dspaceurlasync)).
- 보일 탭을 정할 때 Compass 의 서비스 목록(`getPlatformServices`)과 부여된 역할(`getGrantedRoles`)을 시간 제한 없이 기다립니다. 단독 실행에서는 Compass 콜백이 오지 않을 수 있어([Compass 서비스](/widget/compass-services)), 그러면 탭과 속성이 끝내 나오지 않을 것으로 보입니다 <Badge type="danger" text="검증 필요" />.
- 그래서 이 패널은 3DDashboard 에서 쓰는 것을 전제로 합니다. 대시보드에 내 위젯을 올리는 방법은 [3DDashboard 에 위젯 올리기](/widget/dashboard-register) 에 있습니다.

## 2026 변경

| 항목 | 2025 | 2026 |
| --- | --- | --- |
| 닫기 버튼 | `displayType` 이 `STANDARD` 가 아니면 | `closeButton: true` 이거나 `STANDARD` / `PINNED` 가 아니면 <Badge type="warning" text="2026" />. 닫기 버튼이 있으면 속성 탭의 "취소" 버튼은 없음 |
| `displayType` | | `PINNED` (`'editProp_pinned'`) 추가. OOTB 정보 앱의 기본값이 `STANDARD` → `PINNED`, 주소 파라미터 `closeButton` 추가 <Badge type="warning" text="2026" /> |
| `FACET_DOMAINPROPERTIES` 값 | `'Domain_centric_Properties'` <Badge type="tip" text="2025" /> | `'properties_domain'`. 이름을 문자열로 쓰지 말고 상수를 씀 |
| 새 탭 상수 | | `FACET_CONSTRUCTION_CUSTOM_ATTR`, `FACET_FL_ATTACHEMENT`, `FACET_MBSE_USEDMODEL`, `FACET_MBSE_USINGMODEL`, `FACET_EQUIVALENTITEMS`, `FACET_ADVFILTER` (기본 탭 46 개) <Badge type="warning" text="2026" /> |
| 도메인 속성 탭 | 탭 하나 | 서버 layout 의 facet 마다 탭을 더 만듦 (`facets/DomainProperties/RuntimeFacetController`) <Badge type="warning" text="2026" /> |
| `context.getSecurityContext` | 패널에 옮겨 붙여 부름 (함수 안 `this` 가 context 가 아닐 때가 있음) | context 에 묶어(`bind`) 부름. 두 버전 모두 함수 안에서 `this` 를 쓰지 않는 편이 안전 |
| 인덱스 모드 읽기 | `/attributes/op/readIndex` <Badge type="tip" text="2025" /> | 부르지 않음 (콘솔에 "더 이상 없음") |
| 공개 메서드, 옵션 이름, 읽기 / 저장 REST | | 같음 |
| 호출 앱 | 32 | 36 (ENOXEngineer_260603, ENOXPackageCommonUXInfra, ENXMeetingMgmt, ProductReferential, XSRUtilityCore 추가. 2026 webapps 에 ENOPartMgt 폴더가 없음) |

## 주의

- 옵션은 만들 때만 읽습니다. `typeOfDisplay`, `facets`, `editMode` 를 바꾸려면 정리하고 새로 만듭니다 (읽기 전용만 `setReadonlyStatus`).
- `actions` 는 ID 카드에 붙으므로 ID 카드가 없는 `ONLY_EDIT_PROPERTIES` 에서는 보이지 않습니다.
- `events` 객체는 패널이 안에서 고쳐 씁니다 (`onNotification` 등을 바꿔 끼움). 같은 `events` 객체를 다른 패널에 다시 넘기지 않습니다.
- 탭 모듈은 처음 보일 때 불러오므로 OOTB 탭이 들어 있는 폴더(ENORIPE_Relations, ENOSharingWebUI ...)가 같은 서버에 있어야 합니다. 없는 모듈은 그 탭만 비게 됩니다 <Badge type="danger" text="검증 필요" />.
