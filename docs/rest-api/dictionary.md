# 타입, 속성 정보 (dictionary) <Badge type="info" text="원본 분석" />

화면에 타입 이름, 속성 이름을 사용자 언어로 보이거나, 속성 값 목록(range), 하위 타입, 붙일 수 있는 확장을 보일 때 쓰는 방법을 모읍니다. 화면 코드는 주로 `DS/FedDictionaryAccess/FedDictionaryAccessAPI` 를 부르고, 타입 계층과 속성 정의는 `/resources/dictionary/...` REST 를 직접 부릅니다.

::: info 근거
FedDictionaryAccess/FedDictionaryAccess.js (`FedDicoUtils` 5행, `RequestUtils` 544행, `FedDictionaryAccess3DSpace` 1226행, `FedDictionaryAccessAPI` 1855행~), SixwDictionaryAccess.js (`SixwDicoUtils.getServiceBaseURL`, assets/SixwDicoSettings.json), SearchDictionaryAccess.js (`getElementsNLSNames`, `getAttributesNlsValues`). 호출부: ENOChgServices (`fetchCASubTypes`, `fetchNLSForSubType`), ENOX3DSpaceAttributesMgt (`dictionaryServices`), ENOXGridCusto (`WSAdmin.getTypeInfo`, `DictionaryAccessAPI`), DocumentCommonInfra, IssueServices / IssueComponents (`getSubTypesAllAttributes`), DELPPWDicoServices (`_retrieveDicoType`), DMUwebR3Mapp, EditPropWidget, E6WCommonApps (`fetchParentTypes`, `loadSubTypes`), ENORouteTemplateMgmt, ParameterizationSkeleton. 집계: `FedDictionaryAccessAPI` 를 쓰는 앱 37 개, `dictionary/getTypeInfo` 27 회 18 개 모듈 (`/resources/` 로 시작하는 것 21 회), `collabServices/attributes/op/read` 16 회 13 개 모듈. 2026 은 enovia2026_webapps 같은 방식
:::

## 무엇을 쓸지

| 하고 싶은 일 | 방법 | 자세히 |
| --- | --- | --- |
| 타입 이름을 사용자 언어로 | `getNlsOfPropertiesValues({ 'ds6w:type': [타입...] })` | [값 번역](#값-번역-getnlsofpropertiesvalues) |
| 상태 이름을 사용자 언어로 | `getNlsOfPropertiesValues({ 'ds6w:status': ['정책.상태'] })` | 같은 곳 |
| 하위 타입 목록 | `POST /resources/dictionary/getTypeInfo` 본문 `["타입"]` | [getTypeInfo](#하위-타입-gettypeinfo) |
| 부모 타입 체인, 타입 아이콘 | 같은 요청에 `?parentInfo=true&$select=IconName` | 같은 곳 |
| 하위 타입 이름과 번역을 한 번에 | `/resources/v3/e6w/service/TypeDerivatives?typePattern=타입` | [e6w 타입 서비스](#e6w-타입-서비스-v3) |
| 타입의 속성 정의 (타입, 기본값, 값 목록, 번역) | `GET /resources/dictionary/class/{타입}/attributes?inherited=true&info=true` | [타입 속성](#타입-속성-dictionary-class) |
| 여러 타입, 확장의 속성 목록 | `getPropertiesForClass('ds6wg:타입,ds6wg:확장')` | [API 함수](#api-함수) |
| 속성, 타입 이름(라벨)만 번역 | `getResourcesInfo('ds6wg:...')` | 같은 곳 |
| 속성 값의 번역 | `getNlsOfPropertiesValues({ 속성URI: [값...] })` | [값 번역](#값-번역-getnlsofpropertiesvalues) |
| 타입에 붙일 수 있는 확장 | `getExtensionsForClass('타입')` 또는 `POST /resources/dictionary/class/interfaces` | [타입 속성](#타입-속성-dictionary-class) |
| 객체 하나의 속성 값과 정의를 같이 | `POST /resources/v1/collabServices/attributes/op/read` | [객체 속성 읽기](#객체-속성-읽기-collabservices) |

- 객체의 속성 **값** 만 필요하면 [e6w ObjectInfo](/rest-api/e6w) 나 업무 서비스(`$fields`)를 씁니다. 이 페이지는 타입과 속성의 **정의** 쪽입니다.
- 서버(JPO)에서 객체의 타입, 속성을 select 로 읽는 방법은 [DomainObject 와 select](/jpo/domain-object#select-문법) 를 봅니다.

## URI 표기

FedDictionaryAccessAPI 는 타입과 속성을 짧은 URI(`curi`)로 다룹니다.

| 모양 | 뜻 | 예 (소스) |
| --- | --- | --- |
| `ds6wg:<타입>` | 3DSpace 타입 | `ds6wg:VPMReference`, `ds6wg:Part` |
| `ds6wg:<타입 또는 확장>.<속성>` | 3DSpace 속성 | `ds6wg:DMUValidationUser.V_ValidationUser_Contribution` |
| `ds6w:<술어>` | 6W 어휘(검색 공통 속성) | `ds6w:type`, `ds6w:status`, `ds6w:label`, `ds6w:modified` |
| 그 밖의 접두어 | 다른 서비스 어휘 | `swym:`, `pno:`, `dsplan:` |

- `getResourcesInfo` 는 접두어 없는 이름을 `ds6wg:` 로 보고 처리합니다 (2025 소스의 주석 `SDM-2025/02/07`).
- 번역이 없는 속성은 `label` 이 URI 에서 `ds6wg:` 를 뗀 값과 같게 옵니다. ENOXGridCusto 는 이때 `.` 뒤만 잘라 속성 이름으로 보입니다.

## FedDictionaryAccessAPI

```js
require(['DS/FedDictionaryAccess/FedDictionaryAccessAPI'], function (FedDictionaryAccessAPI) {
  FedDictionaryAccessAPI.getResourcesInfo('ds6wg:VPMReference,ds6w:modified', {
    tenantId: widget.getValue('x3dPlatformId'),
    lang: widget.lang,
    apiVersion: 'R2019x',
    onComplete: function (res) {
      res.forEach(function (item) { console.log(item.curi, item.label); });
    },
    onFailure: function (err) { console.log(err); }
  });
});
```

모든 함수가 마지막 인자로 같은 모양의 객체(`cbFuncs`)를 받고, 결과는 `onComplete` 로 줍니다. Promise 를 돌려주지 않습니다.

### cbFuncs 키

| 키 | 필수 | 설명 |
| --- | --- | --- |
| `onComplete` | O | 결과를 받는 함수 |
| `onFailure` | O | 실패. `getResourcesInfo` 는 있는지 확인하지 않고 부르므로 늘 넣습니다 |
| `tenantId` | O | 플랫폼 id (`widget.getValue('x3dPlatformId')`, 온프레미스는 `'OnPremise'`). Compass 서비스 목록에서 이 id 로 3DSpace 주소를 찾습니다 |
| `lang` | - | 결과 언어. 함수마다 기본값이 `widget.lang` 이거나 `'en'` 이라서 **늘 넣습니다** |
| `apiVersion` | - | 결과 모양. 기본 `'R2018x'`, 새 모양은 `'R2019x'`. 새로 만들 때는 `'R2019x'` 를 씁니다 |
| `serviceName` | - | 기본 `'3DSpace'`. `'3DSwym'` 등은 서비스별 어휘 |
| `extensions` | - | `getPropertiesForClass` 만. `false` 면 확장 속성을 빼고 요청 (`&extensions=true` 를 안 붙임) |
| `includeInstances` | - | `getVocabularyClasses`, `getServiceClasses` 에서 인스턴스 타입도 받기 |
| `view` | - | `'search'`(기본) 또는 `'public'`. RDF 서비스에서만 뜻이 있음 |

- 웹 팝업이나 Web-in-Win 처럼 Compass 가 없는 곳에서는 먼저 `setConfigForMyApps({ myAppsBaseUrl, userId, lang })` 를 부릅니다 (ENORIPE_Relations, ENOAEFCore).
- 3DSpace 주소는 `i3DXCompassPlatformServices.getPlatformServices` 의 결과에서 `platformId === tenantId` 인 항목의 `3DSpace` 값을 씁니다. 주소 얻기는 [3DSpace 주소](/widget/space-url) 를 봅니다.

### API 함수

| 함수 | 입력 | 결과 (`R2019x`) | 쓰는 곳 |
| --- | --- | --- | --- |
| `getResourcesInfo(uris, cb)` | 쉼표로 이은 URI | `[{ curi, label, metaType, dataType }]` | 53 회, 20 개 앱 |
| `getNlsOfPropertiesValues(obj, cb)` | `{ URI: [값...] }` (객체 또는 JSON 문자열) | `{ URI: { 값: 번역 } }` | 51 회, 25 개 앱 |
| `getPropertiesForClass(uris, cb)` | 쉼표로 이은 타입, 확장 | `[{ classInfo: { curi, uri }, properties: [...] }]` | 31 회, 19 개 앱 |
| `getNlsOfPropertiesValuesBySource(obj, cb)` | `{ URI: [{ value, source: ['3DSpace'] }] }` | `{ URI: { 값: 번역 } }` | 14 회, 8 개 앱 |
| `getExtensionsForClass(type, cb)` | 타입 하나 | `{ extensions: [{ curi, uri, label }] }` | 10 회, 7 개 앱 |
| `setConfigForMyApps(config)` | `{ myAppsBaseUrl, userId, lang }` | 없음 | 8 회, 5 개 앱 |
| `getFedProperties(cb)` | - | 어휘별 6W 속성 목록 | SNInfraUX, SearchControlCenter |
| `getPropertiesRoots(uris, cb)` | 6W 속성 | `[{ curi, lineage }]` (뿌리 술어, 예 `ds6w:who`) | SNInfraUX, SearchControlCenter |
| `getVocabularyClasses(voc, cb)` | `'ds6wg:'` 같은 어휘 | 어휘의 클래스 목록 | SNNavigationMap, SNResultUX |
| `getServiceClasses(services, cb)` | `['3DSpace']` | `[{ service, vocabularies: [{ vocabularyInfo, classes }] }]` | SNInfraUX |
| `getRangeValues(uri, cb)` | 6W 술어 | `{ propertyInfo, values: [{ value, nlsValue }] }` | TagNavigator |
| `getVocabularyProperties(voc, cb)` | `'ds6w:'` | 어휘의 속성 목록 | SNInfraUX |

- "쓰는 곳" 숫자는 FedDictionaryAccessAPI 를 불러오는 파일에서 `.함수이름(` 이 나온 횟수입니다.
- `addServiceSettings` (사용 중지 표시), `getFedVocabularies`, `getVocabularies`, `getProperties` 는 호출부가 없습니다.
- `getRangeValues` 는 6W 술어(`ds6w:...`) 전용입니다. 3DSpace 속성의 값 목록은 `getPropertiesForClass` 결과의 `rangeValues` 나 [타입 속성](#타입-속성-dictionary-class) 의 `AuthorizedValues` 로 받습니다.

### 결과 모양

`getResourcesInfo`

| `apiVersion` | 모양 |
| --- | --- |
| `R2019x` | `[{ curi, label, metaType, dataType }]`. `metaType` 은 `'Class'`, `'Predicate'` (RDF 는 `'Property'`) |
| `R2018x` (기본) | `{ vocabularyElementNLSInfo: [{ uri, type, nlsName, dataType }] }` (EditPropWidget 이 `uri`, `nlsName` 을 읽음) |

`getPropertiesForClass` 의 `properties[]` (`R2019x`)

| 키 | 설명 |
| --- | --- |
| `curi`, `uri` | 속성 URI |
| `label`, `description` | 번역된 이름 (둘 다 같은 값) |
| `dataType` | 값 타입 |
| `rangeValues` | 값 목록 `[{ value, nlsValue }]`. 값 목록이 있는 속성만 |
| `dimension`, `manipulationUnit` | 단위가 있는 실수 속성 |
| `originCuri` | 원래 속성 (`ds6wg:` + 서버의 `name`) |
| `selectable` | MQL 속성으로 바꿀 때 쓰는 select 식 (ENOChgServices 가 컬럼의 `select` 로 씀) |
| `nature`, `userAccess` | 속성 종류, 접근 (예 `'ReadOnly'`) |

`R2018x` 는 서버 응답 그대로 `{ classPredicates: [{ className, vocabularyPredicateInfo: [{ uri, name, nlsName, range: { dataTypes }, rangeValues: { literalInfo }, selectable, userAccess, dimension, manipulationUnit, nature }] }] }` 입니다. IssueComponents, ENOChgServices 가 이 모양을 읽습니다.

### 값 번역 (getNlsOfPropertiesValues)

```js
FedDictionaryAccessAPI.getNlsOfPropertiesValues(JSON.stringify({
  'ds6w:type': ['VPMReference', 'Drawing'],
  'ds6w:status': ['VPLM_SMB_Definition.IN_WORK', 'VPLM_SMB_Definition.RELEASED']
}), {
  tenantId: widget.getValue('x3dPlatformId'),
  lang: widget.lang,
  apiVersion: 'R2019x',
  onComplete: function (res) {
    var nls = typeof res === 'string' ? JSON.parse(res) : res;
    console.log(nls['ds6w:type'].VPMReference, nls['ds6w:status']['VPLM_SMB_Definition.IN_WORK']);
  },
  onFailure: function () {}
});
```

- 상태 값은 `정책.상태` 로 씁니다 (DMUwebR3Mapp). DMUwebR3Mapp 은 값에 `.DEFAULT` 가 들어 있는 상태의 번역을 빈 글자로 둡니다.
- 번역이 없는 값은 결과에 빠집니다. 호출부는 원래 값으로 채웁니다 (`nls[uri][v] || v`).
- 날짜 모양 값, `ds6w:who`, `ds6w:modified`, `ds6w:created` 같은 사람, 날짜, 수량 술어와 이름에 `cost` 가 들어간 술어는 서버에 묻지 않고 값을 그대로 돌려줍니다 (`needsToBeTranslated`).
- `getNlsOfPropertiesValuesBySource` 는 값마다 `source` (예 `['3DSpace']`, `['usersgroup']`) 를 정해 여러 서비스 값을 한 번에 번역합니다. 안에서 `SearchDictionaryAccessAPI.getNlsOfSearchValuesBySource` 를 그대로 부르고, ENOXGridCusto 는 이 함수를 직접 불러 `ds6w:type` 을 번역합니다.

### 캐시

결과는 브라우저 `sessionStorage` 에 남습니다. 같은 세션에서 다시 부르면 서버를 부르지 않습니다.

| 키 | 함수 |
| --- | --- |
| `dicoElements_<언어>` | `getResourcesInfo` |
| `dicoPropValues_<언어>` | `getNlsOfPropertiesValues` (서비스별로 나눠 저장) |
| `dicoExtensions` | `getExtensionsForClass` (언어 구분 없음) |
| `dicoPropRoots` | `getPropertiesRoots` |

### 안에서 부르는 REST

설정 파일 `SixwDicoSettings.json` 의 `RDFFedVocabulariesOn3DSpace` 가 `"true"` 라서 (2025, 2026 같음) 3DSpace 의 `/resources/6WVocab/access/` 아래를 부릅니다. 직접 부를 일은 드물지만 브라우저 네트워크 탭에서 보이는 이름입니다.

| 함수 | 요청 |
| --- | --- |
| `getPropertiesForClass` | `POST 6WVocab/access/PredicatesRaw?tenant=&extensions=true`, 본문 텍스트 `ds6wg:Part,ds6wg:...` (`Content-Type: text/plain`) |
| `getResourcesInfo` | `ds6wg:` 는 `POST 6WVocab/access/ElementsNLSNames`, 6W 는 `POST 6WVocab/access/Elements6WNLSNames`. 본문은 쉼표로 이은 텍스트 |
| `getNlsOfPropertiesValues` | `POST 6WVocab/access/PredicateValue?tenant=`, 본문 JSON `{ URI: [값] }` |
| `getExtensionsForClass` | `POST /resources/dictionary/class/interfaces?tenant=`, 본문 `{ classes: [타입] }` |
| `getVocabularyClasses` | `GET 6WVocab/access/SpaceClasses?name=ds6wg` |
| `getRangeValues` | `GET 6WVocab/access/PredicateRangeValues?uri=` |

- 헤더는 `Accept-Language` 만 넣고 `SecurityContext` 는 넣지 않습니다 (`RequestUtils.sendRequest` 가 `WAFData.authenticatedRequest` 를 그대로 부름).
- `getPropertiesForClass` 는 이어 붙인 타입을 한 요청으로 보냅니다. ENOXGridCusto 는 100 개씩 나눠 부릅니다.

## 하위 타입 (getTypeInfo)

```
POST <3DSpace>/resources/dictionary/getTypeInfo?tenant=<플랫폼 id>
```

| 항목 | 값 |
| --- | --- |
| 본문 | JSON 배열 `["Change Action"]`, `["Document"]`. 여러 개 가능 (`["Change Action", "Change Order"]`) |
| `Content-Type` | `application/json` |
| `parentInfo=true` | 항목마다 `parent` (바로 위 타입 이름) 를 넣음 |
| `$select=IconName` | 항목마다 `IconName` 을 넣음 |
| `$filter=isOOTB+eq+false` | 고객이 만든 타입만 (DocumentCommonInfra) |
| CSRF | 넣지 않습니다. 조회용 POST 입니다 |

응답은 **타입 이름을 키로 하는 객체** 입니다. 보낸 타입 자신과 모든 하위 타입이 키로 옵니다.

```json
{
  "Change Action": { "IconName": "..." },
  "MyChangeAction": { "parent": "Change Action", "IconName": "..." },
  "MyChangeActionSub": { "parent": "MyChangeAction", "IconName": "..." }
}
```

| 읽는 법 | 호출부 |
| --- | --- |
| `Object.keys(res)` 가 하위 타입 목록. 길이가 1 이면 하위 타입 없음 | ENOChgServices (`fetchCASubTypes`), PIMwebViewModel, DMUwebR3Mapp |
| `res[t].parent` 를 따라 올라가 상속 체인을 만듦. 보낸 타입에서 `parent` 가 없어 멈춤 | DELPPWDicoServices (`_retrieveDicoType`) |
| `<3DSpace>/snresources/images/icons/small/<IconName>.png` 가 타입 아이콘 | ENOXGridCusto, ENOX3DSpaceAttributesMgt |

```js
spaceRequest(spaceUrl, '/resources/dictionary/getTypeInfo?parentInfo=true&$select=IconName&tenant=' + platformId, {
  method: 'POST',
  body: ['Change Action'],
  securityContext: securityContext
}).then(function (res) {
  Object.keys(res).forEach(function (typeName) {
    var info = res[typeName];
    var icon = info.IconName ? spaceUrl + '/snresources/images/icons/small/' + info.IconName + '.png' : '';
    console.log(typeName, info.parent, icon);
  });
});
```

`spaceRequest` 는 [공통 호출 함수 예](/rest-api/#공통-호출-함수-예) 입니다.

- 호출부는 이 응답에서 **타입 번역을 읽지 않고 따로 받습니다.** 키 목록을 `getNlsOfPropertiesValues({ 'ds6w:type': keys })` 로 번역하거나 (ENOX3DSpaceAttributesMgt), 타입마다 `class/{타입}/attributes` 의 `NLSName` 을 읽습니다 (ENOChgServices).
- ENOXGridCusto, ENOX3DSpaceAttributesMgt 는 값이 빈 객체(`{}`)인 항목을 지우고 씁니다. 어떤 타입이 빈 객체로 오는지는 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />
- `SecurityContext` 헤더를 넣는 호출부(ENOChgServices, DMU3DReviewController)와 넣지 않는 호출부(DocumentCommonInfra, IssueServices, ENOXGridCusto)가 다 있습니다.

## 타입 속성 (dictionary/class)

### 타입 하나의 속성 정의

```
GET <3DSpace>/resources/dictionary/class/<타입>/attributes?inherited=true&info=true
```

| 파라미터 | 설명 |
| --- | --- |
| `inherited` | `true` 면 부모 타입에서 받은 속성도. IssueComponents 는 `true` 로 Issue 와 하위 타입을 각각 받아 Issue 의 속성을 빼서 하위 타입만의 속성을 구함 |
| `info` | `true` 면 속성마다 정의(타입, 값 목록 ...)를 넣음 |

응답 (IssueComponents, ENOChgServices 가 읽는 키)

| 키 | 설명 |
| --- | --- |
| `NLSName` | 타입의 번역 이름. 없거나 빈 글자면 호출부는 타입 이름을 씀 |
| `Attributes[]` | 속성 목록 |
| `Attributes[].Name` | 속성 이름 |
| `Attributes[].NLS` | 속성 번역 이름 |
| `Attributes[].Type` | 값 타입. 호출부는 소문자로 바꿔 `date`, `double` 을 가림 |
| `Attributes[].DefaultValue` | 기본값 |
| `Attributes[].MultiValuated`, `MultiLine` | `'Yes'` / `'No'` |
| `Attributes[].HasRangeDefined` | 값 목록이 있으면 `'Yes'` |
| `Attributes[].AuthorizedValues[]` | 값 목록 `{ Name, NLS }` (값, 번역) |
| `Attributes[].UIAccess` | 화면 접근 |

```js
spaceRequest(spaceUrl, '/resources/dictionary/class/' + encodeURIComponent('Change Action') + '/attributes?inherited=true&info=true&tenant=' + platformId, {
  securityContext: securityContext
}).then(function (res) {
  console.log(res.NLSName);
  res.Attributes.forEach(function (a) {
    var range = a.HasRangeDefined === 'Yes' ? a.AuthorizedValues.map(function (v) { return v.Name + '=' + v.NLS; }) : [];
    console.log(a.Name, a.NLS, a.Type, a.DefaultValue, range.join(', '));
  });
});
```

- `MultiValuated`, `HasRangeDefined` 는 불(boolean)이 아니라 `'Yes'` 문자열입니다.
- 비슷한 키(`Type`, `HasRangeDefined`, `AuthorizedValues[].NLS`, `ManipulationUnit`, `Magnitude`)를 `POST /resources/dictionary/properties` 도 `[{ info: [...] }]` 로 돌려줍니다 (DELPPWDicoServices, DELWebPPRController).

### 여러 타입의 확장, 속성 이름

| 요청 | 본문 | 응답 | 호출부 |
| --- | --- | --- | --- |
| `POST /resources/dictionary/class/interfaces` | `{ classes: ["Document", ...] }` | `{ results: [{ name, interfaces: [{ name, nlsName, automatic }] }] }` | DocumentCommonInfra, CfgBaseUX, EditPropWidget, ENOXGridCusto 등 11 개 |
| `POST /resources/dictionary/class/properties?$select=name,nlsName,protection` | `{ classes: [타입..., 확장...] }` | `{ results: [{ properties: [{ name, nlsName, protection }] }] }` | DocumentCommonInfra |
| `GET /resources/dictionary/interface/<확장>/attributes?info=true` | - | 확장의 속성 정의 | CfgBaseUX |
| `POST /resources/dictionary/interfaces?customerOnly=false` | `{ classes: [...] }` | `{ results: [{ name, interfaces }] }` | DELPPWDicoServices (100 개씩 나눠 보냄) |

- `automatic` (키가 `Automatic` 인 경우도 있음) 이 `'Yes'` 인 확장은 자동으로 붙는 것입니다. `getExtensionsForClass` 는 이것을 결과에서 빼고, ENOXGridCusto, ENOX3DSpaceAttributesMgt 는 이것과 이름이 `XP_` 로 시작하고 `_Ext` 로 끝나는 것을 "자동 확장" 으로 따로 둡니다.
- 오류는 `{ error: { Message, MessageException } }` 으로 옵니다 (EditPropWidget, EditPropExtensions).

### 그 밖의 dictionary 경로

| 경로 | 횟수 | 내용 | 쓰는 모듈 (예) |
| --- | --- | --- | --- |
| `dictionary/dimensions` | 19 | 단위(치수) 사전. 응답 `results` | EditPropWidget, DELPPWDicoServices 등 13 개 |
| `dictionary/dimensions/convertValues`, `convertValue` | 7 | 단위 변환 | EditPropWidget, LibraryEditor |
| `dictionary/object/interfaces`, `relation/interfaces` | 5 | 객체, 관계에 붙은 확장. 본문 `{ objects }` / `{ connections }` | EditPropExtensions, DELWebInfrastructureControls |
| `dictionary/object/addInterfaces`, `removeInterfaces`, `relation/...` 같은 이름 | 4 | 확장 붙이기, 떼기 (쓰기) | EditPropExtensions, DELWebInfrastructureControls |
| `dictionary/Dictionary`, `DictionaryCUSTO`, `DictionaryOOTB` | 10 | 사전 전체를 GET. 번역이 필요하면 `lang=` 을 붙임 (IPTableMePreferences `getFullDictionary`) | IPTableMePreferences, PADServices, DocumentCreateNew, CompareWidget2, XSRCommonComponents |
| `dictionary/AttributeCreate`, `AttributeModify`, `AttributeDelete`, `ExportData`, `ImportData` | 6 | 속성 관리 (관리자 앱) | IPClassifyUtil, ClassificationEditor |

## e6w 타입 서비스 (v3)

응답 공통 규칙은 [e6w](/rest-api/e6w) 를 봅니다. v3 는 `data[0]` 바로 아래에서 값을 읽습니다.

| 요청 | 결과 | 호출부 |
| --- | --- | --- |
| `POST /resources/v3/e6w/service/TypeDerivatives?typePattern=<타입>` | `data[0].derivatives` (하위 타입 이름 배열) | E6WCommonApps (`loadSubTypes`) |
| `GET .../TypeDerivatives?typePattern=<타입>&$fields=all` <Badge type="warning" text="2026" /> | `data[0].derivatives`, `data[0].derivatives_NLS` (같은 순서의 번역) | XSRCommonComponents |
| `GET /resources/v3/e6w/service/ParentTypes/<객체 id>` | `data[0].parentTypes` (그 객체 타입의 부모 타입들) | E6WCommonApps (`fetchParentTypes`) |
| `POST /resources/v3/e6w/service/TypeAttributes/fetch` | `data[0].attributes[]` | ENORouteTemplateMgmt |

- `ParentTypes` 는 **타입 이름이 아니라 객체 id** 를 받습니다. E6WCommonApps 는 결과를 타입별로 기억해 두고 `parentTypes.indexOf('VPMReference')` 처럼 씁니다.
- `TypeAttributes/fetch` 본문은 `{ csrf: { name, value }, data: [{ name: '<타입>' }] }`, `Content-Type: application/ds-json` 입니다. CSRF 는 `/resources/v1/application/E6WFoundation/CSRF` 로 먼저 받습니다 ([CSRF 토큰](/uwa/request#csrf-토큰-3dspace-쓰기-요청)).
- `attributes[]` 는 `{ name, label, type, range, rangeLabel }` 입니다. `type` 은 `string`, `boolean`, `integer`, `real`, `date`. `range` 와 `rangeLabel` 은 같은 순서의 배열(값, 번역)입니다. 번역이 없으면 `label` 이 `emxFramework.Attribute...` 같은 키 문자열이라서 ENORouteTemplateMgmt 는 이때 `name` 을 보입니다.

## 객체 속성 읽기 (collabServices)

속성 패널(EditPropWidget)이 객체의 속성 값과 정의를 한 번에 받는 요청입니다. 요청 본문과 저장은 [속성 패널 - 저장과 REST](/controls/properties-panel#저장과-rest) 에 있고, 여기서는 정의 쪽 응답 키만 적습니다.

```
POST <3DSpace>/resources/v1/collabServices/attributes/op/read
본문 { "lIds": [], "busIDs": ["<physicalid>"], "relIDs": [], "attributes": "true", "plmparameters": "false", "navigateToMain": "true", "readonly": "false", "debug_properties": "" }
```

- 업무 객체는 `busIDs`, 관계는 `relIDs` 에 넣습니다. 켜고 끄는 값(`attributes`, `readonly` ...)은 문자열 `"true"` / `"false"` 입니다 (CfgBaseUX, LifecycleServices, IssueTimeline).
- 인덱스 모드(위젯 값 `index_mode`)에서 EditPropWidget 은 `op/readIndex` 에 `{ lIds, indexattributes: "true" }` 를 보냅니다.
- 같은 서비스의 `op/update` (6 회), `op/history`, `op/facets/available` (각 2 회)를 합치면 `collabServices/attributes` 문자열은 27 회입니다. 저장과 탭은 [속성 패널](/controls/properties-panel#저장과-rest) 을 봅니다.
- 응답은 `{ results: [ 객체마다 하나 ] }` 입니다.

| `results[i]` 키 | 설명 |
| --- | --- |
| `basicData[]` | 기본 속성 |
| `data[]` | 그 밖의 속성 |
| `groupData[]` | 묶음으로 보이는 속성 |
| `extensions` | 확장 정보 `{ 확장이름: { name, nameNLS, deploymentExtension } }` |
| `type_icon_url`, `type_icon_large_url`, `preview_url` | 아이콘, 미리 보기 |
| `modifyAccess`, `success` | 수정 권한, 성공 여부 |

`data[]`, `basicData[]` 의 항목 (EditPropWidget 이 읽는 키)

| 키 | 설명 |
| --- | --- |
| `name`, `nls` | 속성 이름, 번역 |
| `type` | 값 타입 (`boolean` 등) |
| `value` | 현재 값. 여러 값 속성은 배열 |
| `range`, `rangeNLS` | 값 목록과 번역. 배열이거나 쉼표로 이은 문자열 |
| `multivalue`, `multiline`, `mandatory` | 여러 값, 여러 줄, 필수 (`mandatory` 는 `'Yes'` 또는 `true`) |
| `readOnly` | `'No'` 또는 `false` 면 고칠 수 있음 |
| `maxlength`, `minval`, `maxval` | 입력 제한 |
| `dimensionType`, `dimension`, `unitName` | 단위가 있는 속성 |
| `extension`, `group`, `groupNLS`, `UIPosition` | 속한 확장, 묶음, 보이는 순서 |
| `selectable`, `path` | 저장 때 쓰는 속성 경로 |

## dsfl/metaTypes

ParameterizationSkeleton 하나만 씁니다.

```
POST <3DSpace>/resources/v1/modeler/dsfl/metaTypes?tenant=<플랫폼 id>
본문 ["RFLVPMLogicalReference", "VPMReference"]
```

응답 `{ Types: [{ Name, NLS, PackageName, Package, IconName, Abstract }] }`. 호출부는 `PackageName` 별로 묶어 타입 고르기 트리를 만듭니다. 보낸 타입 아래의 하위 타입 목록으로 보입니다 <Badge type="danger" text="검증 필요" />

## 2026 변경

| 항목 | 2025 | 2026 |
| --- | --- | --- |
| `getFedVocabularies`, `getVocabularies` | FedDictionaryAccessAPI 에 있음 | 없음. 어휘 목록은 `SixwDictionaryAccessAPI.get6wVocabularies` |
| 6W 어휘 요청 주소 | `/resources/6WVocab/access/` | `/resources/6WVocab2/access/` (Vocabularies, 6WPredicates, PredicatesRoots, PredicateRangeValues, PredicatesNLSNames, Vocabulary6WClasses, Elements6WNLSNames) <Badge type="warning" text="2026" /> |
| `getVocabularyClasses` 요청 | `SpaceClasses?name=` | `VocabularyClasses?name=` |
| `ds6wg:` 번역 요청 (`FedDictionaryAccess3DSpace.getElementsNLSNames`) | 세 번째 인자가 없으면 `TermsNls`, 있으면 `ElementsNLSNames` (`getResourcesInfo` 의 3DSpace 경로는 늘 `ElementsNLSNames`) | 늘 `ElementsNLSNames` |
| `PredicatesRaw`, `PredicateValue`, `AttributeNlsValues` | `/resources/6WVocab/access/` | 그대로 |
| `dictionary/getTypeInfo` | 27 회, 18 개 모듈 | 23 회, 19 개 모듈 (DELPPWDicoServices 빠짐, DELWebInfrastructureControls, ENOXCustomTableEditor 더해짐) |
| `collabServices/attributes/op/read` | 16 회, 13 개 모듈 | 18 회, 15 개 모듈 |
| `e6w/service/TypeDerivatives` | E6WCommonApps 1 곳 (POST) | 3 곳. XSRCommonComponents 가 GET `$fields=all` 로 `derivatives_NLS` 를 받음 |
| `i3DXSNDictionaryAPI` (`DS/i3DXSNDictionaryAPI/3DXSearchDictionaryAccess`) <Badge type="tip" text="2025" /> | 있음. 다른 앱에서 부르지 않음 | 없음 |

FedDictionaryAccessAPI 를 통해 부르면 주소 변화는 신경 쓰지 않아도 됩니다. 6WVocab 주소를 직접 부르는 코드만 고칩니다.

## 주의

::: warning 하위 타입 목록에 자기 자신이 들어 있습니다
`getTypeInfo` 응답 키에는 보낸 타입도 들어 있습니다. "하위 타입이 있는지" 는 키가 2 개 이상인지로 봅니다 (ENOChgServices). 화면 목록에서 기준 타입을 빼려면 키에서 직접 지웁니다 (DELPPWDicoServices 는 `"Resource" !== e` 로 거름).
:::

- `lang` 을 빼면 함수에 따라 영어로 옵니다. `tenantId`, `lang`, `apiVersion: 'R2019x'` 를 늘 같이 넣습니다.
- 결과가 문자열로 오는 경우를 대비해 호출부는 `typeof res === 'string' ? JSON.parse(res) : res` 로 받습니다 (ENOX3DSpaceAttributesMgt, ENOXGridCusto, DocumentCommonInfra).
- IssueServices, ENOChgServices 는 `getPropertiesForClass`, `getExtensionsForClass` 의 실패도 Promise 의 resolve 로 넘깁니다. 그래서 받는 쪽에서 `Error` 인지(ENOChgServices), 기대한 키(`classPredicates`)가 있는지(IssueComponents) 다시 봅니다.
- `getExtensionsForClass` 캐시는 언어를 가리지 않습니다. 한 세션에서 언어를 바꾸면 이전 언어의 라벨이 남을 수 있습니다.
- 타입 이름에 `ds6wg:` 를 붙이는 호출부(DocumentCommonInfra)와 붙이지 않는 호출부(ENOChgServices 의 `getExtensionsForClass`, `getPropertiesForClass`)가 다 있습니다. 서버가 두 모양을 다 받는 것으로 보입니다 <Badge type="danger" text="검증 필요" />
