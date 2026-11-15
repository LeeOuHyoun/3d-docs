# 객체 정보, 사람과 객체 찾기 (e6w) <Badge type="info" text="원본 분석" />

`/resources/v2/e6w/service/...` 는 타입에 상관없이 객체의 기본 정보를 읽고, 사람과 객체를 이름으로 찾는 3DSpace 범용 서비스입니다. 경로 목록은 [엔드포인트 목록 - e6w 서비스](/rest-api/catalog#e6w-서비스), 응답의 기본 모양은 [응답 형식 A](/rest-api/#응답-형식-a-업무-객체) 를 봅니다. 이 페이지는 요청 파라미터, `$fields` 로 받는 키, `dataelements` 키, 찾기 조건, 결과 수, 오류를 적습니다.

::: info 근거
Foundation/Foundation.js (`FoundationAjax` 197~496행 `getParams`, `setupEnoviaServer`, `calculateFinalUrl`, `ajaxRequest`, `FoundationData.loadWidget` 1198행, `tempURI`, `setRowPref`), Foundation2/Foundation2.js (`_serviceURL` 1170행, `loadServiceData` 3111행, `getParams` 3193행, `ReferenceModel.typeAheadCallback`, `syncRead`, `syncFromServer`, ObjectInfo 묶음 요청 6939행), DocumentManagement (`ObjectInfoService`, `IndependentServiceBase._getObjects`), RDFFoundation (`ObjectProxyUtil.sendRequest`, `convertServiceResponseToFederatedFormat`), WEB-INF/resources/param/ChangeOrder_Parameterization.xml (`ParameterizationExpressionImpl`). 호출부: ENOChgServices (`getAdditionObjectInfo`, `getObjectInfo`, `getAdditionSwymPersonInfo`, `getExpressionValue`), IssueServices, CfgBaseUX, VCXWebPlatform, CAT3DExpModel, ENOGantt, CollaborativeTasks, ENOXCADManageScope, FolderEditor, FolderUpload, ENOProjectMgmtModels, IssueManagement. 집계: 2025 `e6w/service/` 문자열 `ObjectInfo` 8, `PersonSearch` 5, `ExpresssionSearch` 4, `ApplicabilityTypeInfo` 3, `ObjectSearch`, `ObjectSearchByName`, `PersonSearchByName` 각 1 (Foundation2 처럼 이름만 넘겨 조립하는 호출은 빠짐). `Foundation2/FoundationV2Data` 를 쓰는 앱 6 개, `Foundation/FoundationData` 3 개
:::

샘플: [02_drag_drop.html](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/EventsSample/02_drag_drop.html) (놓인 객체를 `GET v2/e6w/service/ObjectInfo/{id}` 로 읽기)

## 버전과 주소

| 버전 | 주소 | 응답을 읽는 법 | 쓰는 곳 |
| --- | --- | --- | --- |
| v1 | `/resources/e6w/service/json/<이름>`, 저장은 `POST /resources/e6w/service` (form) | Foundation 위젯 정의 | Foundation (`FoundationData.tempURI`, `applyUpdates`) |
| v2 | `/resources/v2/e6w/service/<이름>` | `data[i].dataelements.<키>` ([응답 형식 A](/rest-api/#응답-형식-a-업무-객체)) | 대부분 |
| v3 | `/resources/v3/e6w/service/<이름>` | 호출부가 `data[i].name`, `data[0].parentTypes` 처럼 `dataelements` 없이 읽음 | DocumentManagement, RDFFoundation, FolderUpload, ENOProjectMgmtModels, E6WCommonApps |

- Foundation2 의 `_serviceURL(name)` 은 이름에 `/resources` 가 없으면 `/resources/v2/e6w/service/` 를 앞에 붙입니다. 그래서 `loadServiceData('ObjectInfo', ...)` 처럼 이름만 넘기는 호출도 v2 입니다.
- v3 는 `dataelements` 로 감싸지 않은 모양으로 보입니다 (FolderUpload, RDFFoundation 이 바로 읽음) <Badge type="danger" text="검증 필요" />

## 공통 파라미터

| 이름 | 넣는 곳 | 예 | 설명 |
| --- | --- | --- | --- |
| `tenant` | 쿼리 | `OnPremise` | 플랫폼 id. Foundation 은 늘 붙입니다 |
| `SecurityContext` | 헤더 또는 쿼리 | `ctx::역할.조직.협업공간` | FoundationAjax 는 헤더에 `encodeURIComponent` 해서 넣고, ENORouteMgmt 는 쿼리에 붙입니다 ([보안 컨텍스트](/rest-api/security-context)) |
| `e6w-lang` | 쿼리 | `ko` | 번역(`typeNLS`, `stateNLS`) 언어. FoundationAjax 는 `zh` 를 `zh_CN` 으로 바꿔 보냄 |
| `e6w-timezone` | 쿼리 | `-540` | `new Date().getTimezoneOffset()` (분) |
| `$language`, `$timezone` | 쿼리 | | Foundation2 `loadServiceData` 가 위 두 값을 이 이름으로 바꿔 보냄 |
| `e6w-objLimit` | 쿼리 | `100` | 결과 수 ([결과 수](#결과-수)) |
| `$fields` | 쿼리 또는 form 본문 | `ownerFullname,policy`, `all,islast` | 더 받을 키 ([ObjectInfo](#objectinfo)) |
| `$include` | 쿼리 | `versions`, `none` | 같이 받을 `relateddata` |
| `$definition` | 쿼리 | `true` | 필드 정의. Foundation2 `getWidgetConstants` 가 씀 |
| `$ids` | form 본문 | `id1,id2` | 여러 객체. `Content-Type: application/x-www-form-urlencoded` |
| `$simpleJSON` | 쿼리 | `true` | v3 ExpresssionSearch (ENOProjectMgmtModels) |
| `Accept-Language` | 헤더 | `widget.lang` | FoundationAjax 가 없으면 넣음 |

- 조회는 GET 이거나 form 본문의 POST 이고 **CSRF 를 넣지 않습니다.** 예외로 `TypeAttributes/fetch` 는 조회지만 JSON 본문에 `csrf` 를 넣습니다 ([CSRF 토큰](/uwa/request#csrf-토큰-3dspace-쓰기-요청)).
- 3DSpace 주소는 [3DSpace 주소와 SecurityContext](/widget/space-url) 로 얻습니다.

## ObjectInfo

| 하는 일 | 요청 | 호출부 |
| --- | --- | --- |
| 객체 하나 | `GET /resources/v2/e6w/service/ObjectInfo/<id>` | VCXWebPlatform, CAT3DExpModel |
| 키를 더 받기 | `GET .../ObjectInfo/<id>?$fields=objectId` | ENOGantt |
| 여러 객체 | `POST .../ObjectInfo?$fields=<키,키>`, 본문 `$ids=id1,id2` | ENOChgServices, IssueServices, CfgBaseUX |
| `$fields` 를 본문에 | `POST .../ObjectInfo`, 본문 `$ids=id1,id2&$fields=name,title` | ENOChgServices (`getObjectInfo`, 기본 `name`) |
| 여러 모델을 한 번에 | `loadServiceData('ObjectInfo', cb, true, '$ids=...', 'application/x-www-form-urlencoded', '$fields=islast')` | Foundation2 모델 |
| v3 | `POST /resources/v3/e6w/service/ObjectInfo/?$fields=all,islast`, 본문 `$ids=` | RDFFoundation (DocumentManagement `ObjectInfoService`) |
| v3 버전, 부모 기준 | 같은 요청에 `$include=versions`, `parentRelName=...&parentDirection=from` | DocumentManagement `ObjectInfoService.getObjects` |

`<id>` 는 physicalid 입니다. 한 개를 요청해도 결과는 `data[0]` 입니다.

### 받는 키 (v2)

`data[i]` 바로 아래는 `id`, `type` 입니다. 나머지는 `dataelements` 에 있습니다.

| `dataelements` 키 | 받는 법 | 설명 |
| --- | --- | --- |
| `name` | `$fields` 없이 읽음 | 이름. 사람이면 로그인 |
| `title`, `revision` | `$fields` 없이 읽음 | 제목, 리비전 |
| `typeNLS`, `stateNLS` | `$fields` 없이 읽음 | 타입, 상태 번역 |
| `image` | `$fields` 없이 읽음 (VCXWebPlatform) | 미리 보기 주소 |
| `fileExtension` | `$fields` 없이 읽음 | 문서 파일 확장자 |
| `firstname`, `lastname` | `$fields` 없이 읽음 | 사람일 때 |
| `icon` | `$fields=icon` | 타입 아이콘 주소 |
| `policy` | `$fields=policy` | 정책 |
| `ownerFullname` | `$fields=ownerFullname` | 소유자 이름 |
| `collabSpace`, `collabSpaceTitle` | `$fields=` 같은 이름 | 협업공간 이름, 표시 이름 |
| `organization`, `organizationTitle` | `$fields=` 같은 이름 | 조직 |
| `synopsis`, `functionalname` | `$fields=` 같은 이름 | 요약, 기능 이름 |
| `islast` | `$fields=islast` | 마지막 리비전인지 |
| `objectId` | `$fields=objectId` | ENOGantt 가 요청하지만 결과를 읽는 코드는 없음 |

- "`$fields` 없이 읽음" 은 호출부가 `$fields` 에 넣지 않고 읽는 키라는 뜻입니다. ENOChgServices, IssueServices 는 `$fields=ownerFullname,collabSpace,collabSpaceTitle,policy,icon,image,synopsis,functionalname,organization,organizationTitle` 로 요청하고 위 표의 키를 모두 읽습니다.
- `$fields=all` 은 모든 키, `none,<키>` 는 기본 키를 빼고, `!<키>` 는 제외입니다 ([쿼리 파라미터](/rest-api/#쿼리-파라미터)).

### 받는 키 (v3)

RDFFoundation 이 v3 결과 `data[i]` 에서 바로 읽는 키입니다 (`$fields=all,islast`).

| 키 | 설명 |
| --- | --- |
| `id`, `type`, `name`, `title`, `revision`, `description` | 기본 |
| `stateNLS`, `ownerFullname` | 상태 번역, 소유자 |
| `icon`, `image`, `bigIcon` | 아이콘, 미리 보기 |
| `extension`, `kind`, `linkURL`, `hasfiles` | 파일 확장자, 종류, 링크 주소, 파일이 있는지 |
| `modified`, `created`, `islast` | 날짜, 마지막 리비전인지 |

### 여러 객체 예

```js
WAFData.authenticatedRequest(spaceUrl + '/resources/v2/e6w/service/ObjectInfo?tenant=' + platformId + '&$fields=ownerFullname,policy,icon', {
  method: 'POST',
  type: 'json',
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
    'Accept-Language': widget.lang,
    SecurityContext: securityContext
  },
  data: '$ids=' + ids.join(','),
  onComplete: function (res) {
    if (!res || res.success === false) { console.log(res && res.error); return; }
    res.data.forEach(function (o) {
      var de = o.dataelements;
      console.log(o.id, o.type, de.name, de.title, de.revision, de.typeNLS, de.stateNLS, de.ownerFullname);
    });
  },
  onFailure: function (error, data) { console.log(error.message, data); }
});
```

- 요청한 id 가 결과에 없을 수 있습니다. RDFFoundation 은 빠진 id 를 `POST /resources/v1/application/E6WFoundation/CheckObjectExistence` (본문 `{ data: [{ id }] }`) 로 다시 물어 `exists === 'FALSE'` 면 지워진 객체로 표시하고, Foundation2 모델은 빠지면 `onFailure` 로 보냅니다.
- VCXWebGUI 는 `image` 주소에 `enovia` 가 들어 있으면 미리 보기로 쓰지 않습니다. 미리 보기가 없는 객체는 타입 기본 그림 주소가 오는 것으로 보입니다 <Badge type="danger" text="검증 필요" />

## 사람 찾기 (PersonSearch)

| 요청 | 뜻 | 호출부 |
| --- | --- | --- |
| `GET /resources/v2/e6w/service/PersonSearch?searchStr=<글자>*` | 글자로 찾기 (`*` 는 와일드카드) | CollaborativeTasks, InstantMessagingW |
| `GET .../PersonSearch?searchStr=<로그인>` | 별표 없이 | FolderEditor, FolderEditorCustomization, ENOXCADManageScope |
| `GET .../PersonSearchByName?searchStr=<로그인>,<로그인>` | 로그인 여러 개 | ENOChgServices (`getAdditionSwymPersonInfo`), Foundation2 (`syncRead`) |
| `/resources/v3/e6w/service/PersonSearch?tenant=...&searchStr=<글자>*&typeStr=Person` | v3 | ENOProjectMgmtModels |

결과 `data[]` 에서 읽는 키: `id`, `type` (`'Person'`), `dataelements.name` (로그인), `dataelements.firstname`, `dataelements.lastname`. 표시 이름은 호출부가 `firstname + ' ' + lastname` 으로 만듭니다.

```js
function searchPerson(text) {
  return new Promise(function (resolve, reject) {
    WAFData.authenticatedRequest(spaceUrl + '/resources/v2/e6w/service/PersonSearch?tenant=' + platformId + '&searchStr=' + encodeURIComponent(text) + '*', {
      method: 'GET',
      type: 'json',
      headers: { Accept: 'application/json', 'Accept-Language': widget.lang, SecurityContext: securityContext },
      onComplete: function (res) {
        if (!res || !res.success || !res.data) { resolve([]); return; }
        resolve(res.data.filter(function (p) { return p.dataelements; }).map(function (p) {
          var de = p.dataelements;
          return { id: p.id, login: de.name, label: (de.firstname || '') + ' ' + (de.lastname || '') };
        }));
      },
      onFailure: function (error) { reject(error); }
    });
  });
}
```

- 사람 사진은 `<3DSwym 주소>/api/user/getpicture/login/<로그인>/format/mini` (큰 것은 `/format/normal`) 입니다 (E6WCommonUI `UIHelper.userPictureURL`).
- 자동완성 컨트롤에 붙이는 방법은 [자동완성](/controls/autocomplete), 3DSearch 로 찾기는 [3DSearch 에서 골라 오기](/widget/search) 를 봅니다.

::: warning PersonSearchByName 결과에 다른 사람이 섞입니다
`PersonSearchByName` 은 검색 가능한 칸에 그 로그인이 들어 있는 사람도 돌려줍니다. Foundation2 는 결과를 `dataelements.name === 로그인` 으로 다시 거른 뒤 씁니다. 로그인으로 한 사람을 정할 때는 이렇게 거릅니다.
:::

## 객체 찾기 (ObjectSearch)

| 요청 | 결과에서 읽는 키 | 호출부 |
| --- | --- | --- |
| `GET /resources/v2/e6w/service/ObjectSearch?searchStr=<글자>*&typeStr=<타입>` | `id`, `type`, `dataelements.name`, `title`, `image` (사람이면 `firstname`, `lastname`) | Foundation2 `ReferenceModel.typeAheadCallback` |
| `GET /resources/v3/e6w/service/ObjectSearch?tenant=...&searchStr=...&typeStr=...` | 모델로 읽음 | ENOProjectMgmtModels (`SearchObjects`) |
| `GET /resources/v3/e6w/service/ObjectSearchByName?typeStr=Workspace&searchStr=<이름>` | `data[].name`, `data[].id` | FolderUpload (같은 이름 폴더가 있는지) |

- Foundation2 의 `typeAheadCallback` 에는 "쓰지 않는 것 같다, 지워야 한다" 는 주석이 있습니다. 새로 만든다면 객체 찾기는 [3DSearch (federated search)](/widget/search) 를 먼저 봅니다.
- FolderUpload 는 결과 중 `name` 이 정확히 같은 것만 씁니다. 이름 찾기도 부분 일치 결과가 섞일 수 있는 것으로 보입니다 <Badge type="danger" text="검증 필요" />

### 찾기 조건

| 파라미터 | 모양 | 예 | 근거 |
| --- | --- | --- | --- |
| `searchStr` | `<글자>*` | `kim*` | `*` 는 와일드카드. CollaborativeTasks, InstantMessagingW |
| `searchStr` | `<글자>` | 로그인 그대로 | FolderEditor, ENOXCADManageScope |
| `searchStr` | `<글자>*  NOT <타입 조건>` | `NOT (flattenedtaxonomies:("types/Person"))` | 타입 빼기. Foundation2, ENOProjectMgmtModels (`Persons.typeAheadCallback`, v3 `PersonSearch` 로 보냄) |
| `searchStr` (ByName) | 쉼표로 이은 로그인 | `user01,user02` | ENOChgServices |
| `typeStr` | 타입, `*`, `Person` | `Workspace` | 찾을 타입. Foundation2 는 지정이 없으면 `*`, 사람 칸이면 `Person` 을 넣음 |

- 타입 조건은 E6WCommonUI `Search.buildSearchTypeQuery` 가 만듭니다. 3DSpace 소스는 `flattenedtaxonomies:("types/<타입>")`, 그 밖의 소스는 `[ds6w:type]:("<타입>")` 를 `OR` 로 잇습니다. 3DSearch 와 같은 문법이라 e6w 찾기도 인덱스를 쓰는 것으로 보입니다 <Badge type="danger" text="검증 필요" />
- 글자는 `encodeURIComponent` 로 감쌉니다. Foundation2 는 감싸고, CollaborativeTasks 는 감싸지 않습니다.

## 설정 값 읽기 (ExpresssionSearch)

이름에 Search 가 있지만 객체 찾기가 아닙니다. 서버에 정의된 설정 값을 이름 패턴으로 읽습니다. 경로의 `Expresssion` 은 `s` 가 세 개인 것이 실제 이름입니다.

| 요청 | 결과 | 호출부 |
| --- | --- | --- |
| `GET /resources/v2/e6w/service/ExpresssionSearch/CHG*,ECM*` | `data[].dataelements` 가 `{ 설정이름: 값 }` | ENOChgServices, ENOChangeActionUX |
| `GET .../ExpresssionSearch/ISU*` | 같음 (`ISU_AUTO_DATES`) | IssueServices → IssueManagement |
| `POST /resources/v3/e6w/service/ExpresssionSearch?tenant=...&$simpleJSON=true`, 본문 `$ids=<이름>,<이름>` | 모델에서 `get(이름)` | ENOProjectMgmtCommonComponents (`getConfigurationCenterSettings`) |

- ENOChgServices 는 `CHG_CHANGE_MATURITY`, `ECMEnableDefaultChangeAnalysis` 같은 이름을 받아 화면 기능을 켜고 끕니다.
- 값은 서버의 Expression 관리 객체(MQL `expression`)에서 옵니다. 예를 들어 `ECMEnableDefaultChangeAnalysis` 는 Configuration Center 설정 정의(`WEB-INF/resources/param/ChangeOrder_Parameterization.xml`)에서 `ParameterizationExpressionImpl` 과 `<Constant id="expression" value="ECMEnableDefaultChangeAnalysis" />` 로 같은 이름의 expression 에 저장됩니다. `CHG_CHANGE_MATURITY`, `ISU_AUTO_DATES` 는 이 설정 정의 파일들에 없어 어디서 만들어지는지는 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />

## 그 밖의 e6w 서비스

| 이름 | 횟수 | 내용 | 호출부 |
| --- | --- | --- | --- |
| `v2/.../ApplicabilityTypeInfo/<id>` | 3 | `data[0].dataelements.applicabilityType` | ENOChgServices, CfgEvolutionUX |
| `v2/.../TSK_WIDGET_CONSTANTS` | 2 | Foundation2 `getWidgetConstants` 로 읽는 서버 상수 (`REFRESH_TIMEOUT` 등) | CollaborativeTasks |
| `v3/.../TypeAttributes/fetch`, `TypeDerivatives`, `ParentTypes` | 3 | 타입 정보 | [타입, 속성 정보](/rest-api/dictionary#e6w-타입-서비스-v3) |
| `v3/.../RiskContextInfo/<id>` | 1 | 컨텍스트 객체 정보 (`fetchSource` 가 `ContextInfo` 일 때) | ENOProjectMgmtModels |
| `v2/.../Tags` | 1 | 태그 (`$simpleJSON=true`) | ENOProjectMgmtModels |
| `/resources/e6w/service/SMA_SimExeOption?objectId=` | 1 | v1 | WebServicesUtils |
| `/resources/e6w/servicetagdata`, `.../search` | 2 | v1 태그 데이터 | Foundation (`WidgetTagNavInit`) |

## 결과 수

- Foundation 은 `e6w-objLimit=<줄 수>` 를 붙입니다. 값은 위젯 설정 `totalrows` (선택지 10, 20, 50, 100, 500, 1000, 기본 100) 입니다 (`setRowPref`, `loadBPS`).
- 직접 부르는 사람 찾기 호출부(CollaborativeTasks, ENOXCADManageScope, FolderEditor)는 개수를 정하지 않습니다. 이때 서버의 기본 개수는 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />
- ENOProjectMgmtModels 의 상수에 `$top=` 이 있지만 e6w 요청에 붙이는 코드는 없습니다.

## Foundation 이 감싸는 방식

ENOVIA 의 예전 위젯(CollaborativeTasks, ENOGantt, E6WCommonApps)은 e6w 를 직접 부르지 않고 Foundation 을 거칩니다.

| 모듈 | 함수 | 하는 일 |
| --- | --- | --- |
| `DS/Foundation/FoundationAjax` | `setupEnoviaServer(options)` | 전역 `window.enoviaServer` 를 만듭니다 (`tenant`, `space`, `rows`, `language`, `timezone`, `preferences`, `computeUrl`) |
| | `ajaxRequest(req, noParamsExceptTenant)` | 주소를 완성하고 `WAFData.authenticatedRequest` 로 보냅니다 |
| `DS/Foundation/FoundationData` | `ajaxRequest(...)` | 대시보드 안(`window.isIFWE`)이면 FoundationAjax, 아니면 jQuery |
| | `loadWidget(name, cb, isPost, postData, headers)` | v1 `/resources/e6w/service/json/<이름>`. `postData` 가 있으면 form POST |
| | `applyUpdates(data, cb)` | 바뀐 객체를 `POST /resources/e6w/service` (form) |
| `DS/Foundation2/FoundationV2Data` | `loadServiceData(name, cb, isPost, postData, contentType, urlParams)` | v2 `/resources/v2/e6w/service/<이름>` 로 `loadWidget` |
| | `getWidgetConstants(name, cb)` | `$definition=true` 로 받은 필드의 `label` 을 `window.WidgetConstants.str` 에 넣음 |
| `DS/Foundation2/Models/FoundationV2Model` | 모델 `fetch` | 같은 때 요청된 모델을 모아 `ObjectInfo` 를 한 번에 POST |

`FoundationAjax.ajaxRequest` 가 하는 일

| 단계 | 내용 |
| --- | --- |
| 주소 | 프로토콜이 있는 주소는 그대로. 아니면 `enoviaServer.computeUrl(url)` 로 앞에 서버 주소를 붙임 |
| 쿼리 | `enoviaServer.getParams()` 의 `tenant`, `e6w-objLimit`, `e6w-lang`, `e6w-timezone`, `preferences` 의 `키=값`. `noParamsExceptTenant` 면 `tenant` 만 |
| 헤더 | `Accept-Language`, `SecurityContext` (`enoviaServer.space` 를 `encodeURIComponent`). `noSecurityContext: true` 면 빼고, 헤더를 직접 주면 그 값을 씀 |
| WAFData 옵션 | `proxy: 'passport'`, `timeout: 500000`, `cache: 3600` |
| 실패 | `onFailure` 를 주지 않으면 `callback({ error: 'Network Error: Unable to load widget due to network or authentication error.' })` |

- `callback` 은 성공과 실패를 같은 함수로 받습니다. `res.success` 와 `res.error` 로 가릅니다. `loadWidget` 은 `success` 가 아니면 `console.error(error + ' :: ' + 이름)` 을 남깁니다.
- `setupEnoviaServer` 직후의 서버 주소(`storageUrl`)는 myApps 주소입니다. Foundation 위젯은 플랫폼 서비스 목록을 읽은 뒤 고른 플랫폼의 3DSpace 주소와 `tenant`, `swym` 등으로 바꿉니다 (`loadBPS`). Foundation 을 빌려 쓸 때는 DocumentManagement 처럼 **완전한 주소** 를 넘기는 편이 안전합니다.
- 새 위젯은 Foundation 없이 [WAFData](/uwa/request) 로 직접 부르는 편이 단순합니다. 위 표의 쿼리와 헤더를 직접 넣으면 같은 요청이 됩니다.

## 오류

| 상황 | 모양 | 처리 |
| --- | --- | --- |
| 서비스 오류 | HTTP 200, `{ success: false, error: '...' }` | `onComplete` 에서 `success` 를 먼저 봄 ([공통 규칙 - 오류](/rest-api/#오류)) |
| 네트워크, 인증 | `onFailure(error, data)` | Foundation 은 `{ error: 'Network Error: ...' }` 로 바꿔 `callback` 에 넘김 |
| 시간 초과 | `onTimeout` | Foundation 은 `{ error: 'Network Error: Unable to load widget due to timeout error.' }` |
| 취소 | `onCancel` | Foundation 은 `{ error: 'Application Error: Request canceled.' }` |
| 없는 id | 오류 없이 결과에서 빠짐 | [ObjectInfo](#여러-객체-예) 의 CheckObjectExistence |

E6WCommonApps 는 `e.success ? e.data[0].derivatives : e.error` 처럼, ParentTypes 는 `e && !e.error && e.data` 로 가립니다.

## 2026 변경

| 항목 | 2025 | 2026 |
| --- | --- | --- |
| `e6w/service/` 경로 종류 | 위 표 | 같음. 새 서비스 없음 |
| `ObjectInfo` 문자열 | 8 | 9. ENOChgServices 가 분류 탭에서 만든 객체를 `POST ObjectInfo`, 본문 `$ids=<id>&$fields=<select 목록 또는 name>` 으로 다시 읽음 |
| `TypeDerivatives` | 1 (E6WCommonApps POST) | 3. XSRCommonComponents 가 `GET ...?typePattern=<타입>&$fields=all` ([타입, 속성 정보](/rest-api/dictionary#e6w-타입-서비스-v3)) |
| Foundation `getParams`, `_serviceURL` | 위 설명 | 같음 |

## 주의

- 여러 객체 조회는 **form 본문 POST** 입니다. `Content-Type` 을 `application/json` 으로 보내면 `$ids` 가 읽히지 않는 것으로 보입니다 <Badge type="danger" text="검증 필요" />
- v2 와 v3 는 결과를 읽는 위치가 다릅니다 (`data[i].dataelements.name` 과 `data[i].name`). 경로의 버전을 바꿀 때는 읽는 코드도 바꿉니다.
- 사람 찾기의 `id` 는 Person 객체의 physicalid 이고, 로그인은 `dataelements.name` 입니다. 다른 API 에 사람을 넘길 때 어느 값을 받는지 확인합니다.
- 서버의 설정 값은 ExpresssionSearch 로 받습니다. 철자를 고쳐 `ExpressionSearch` 로 부르면 안 됩니다.
