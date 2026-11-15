# 관계 탐색 (enorelnav) <Badge type="info" text="원본 분석" />

`/resources/enorelnav/v2/navigate/...` 는 객체 하나에서 출발해 관계로 이어진 객체를 돌려주는 3DSpace 서비스입니다. 하위 구조, 상위(사용처, where-used), 첨부 문서, 변경 조치처럼 관계 종류를 이름(패턴)으로 골라 받습니다. 3DEXPERIENCE 의 "관계" 앱(ENORIPE_AP)과 관계 탐색 그래프(SNNavigationMap, 요청 이름 `RelationalExplorer`)가 이것으로 그립니다.

::: info 근거
ENORIPE_Relations/ENORIPE_Relations.js (`setup` 의 getEcosystem 주소, `_getDetail`, `_prepareURI`, `_getNlsData`, `_getProfiles`, `_parseRelations`, `_setPreferences`, `createPromise`), SNNavigationMap/SNNavigationMap.js (`getRelationsCount`, `getEcosystem`, `setPreferences`, `getDetails`, `clear`, `getPatterns`, `getProfiles`, `getSynchroStatus`, `_getEcosystemByLevel`, `switchRelationsDirection`, `createPromise`), ENORIPE_Columns (`WSRelations.getCount`, `getDetail`, `getrelationprofiles`, `_buildNode`, assets/ColumnsDefinition.json), PIMwebUX (`getEnoviaModelRelations`), DMUwebR3Mapp / DMUwebR3Mctrl, PlatformServicesData, CompareWidget2 (`Ecosystem`), SNNavigationMap/assets/nls (패턴 번역). 집계: 2025 `enorelnav/v2/navigate` 20 회 7 개 모듈, 예전 `enorelnav/navigate` 8 회 6 개 모듈. 2026 은 enovia2026_webapps 같은 방식
:::

## 엔드포인트

앞에는 `<3DSpace>/resources/enorelnav/` 가 붙고 끝에 `?tenant=<플랫폼 id>` 가 붙습니다. **모두 POST 이고 본문은 JSON** 입니다.

| 경로 | 횟수 | 하는 일 | 쓰는 모듈 |
| --- | --- | --- | --- |
| `v2/navigate/getEcosystem` | 8 | 이어진 객체 받기 (가장 많이 씀) | ENORIPE_Relations, ENORIPE_Columns, ENORIPE_RelationsColumns, SNNavigationMap, PIMwebUX, DMUwebR3Mapp, DMUwebR3Mctrl |
| `v2/navigate/getRelationCount` | 3 | 패턴별 개수만 | ENORIPE_Columns, ENORIPE_RelationsColumns, SNNavigationMap |
| `v2/navigate/getProfiles` | 3 | 역할에 맞는 프로필(패턴 묶음) 목록 | ENORIPE_Columns, ENORIPE_RelationsColumns, SNNavigationMap |
| `v2/navigate/setPreferences` | 2 | `widgetId` 에 쓸 패턴, 속성을 서버에 맡김 | ENORIPE_Relations, SNNavigationMap |
| `v2/navigate/getPatterns` | 1 | 쓸 수 있는 패턴 목록 | SNNavigationMap |
| `v2/navigate/clear` | 1 | `widgetId` 에 맡긴 상태 지우기 | SNNavigationMap |
| `v2/navigate/getSynchroStatus`, `fullImport` | 1, 1 | 인덱스 동기화 상태, 전체 가져오기 | SNNavigationMap |
| `navigate/getecosystem` (v1) | 4 | 예전 방식 | CompareWidget2, ENOFilterBIUX, PADServices, PlatformServicesData |
| `navigate/setpreferences`, `navigate/detail`, `navigate/getProfiles` (v1) | 1, 2, 1 | 예전 방식 | PlatformServicesData, SNNavigationMap, ENORIPE_Relations |

- 3DSearch 결과의 `action_RelatedObjects` 동작(`DS/ENORIPE_Search/WhereUsed`)은 REST 를 부르지 않고 관계 앱(ENORIPE_AP)을 열어 객체를 넘깁니다. 화면 없이 관계를 받으려면 이 페이지의 REST 를 직접 부릅니다.
- 즐겨찾기(`/resources/enorelnavfav/favorite/...`), 관계 설정(`/resources/enorelcc/cc_services/getrelsettings`, GET)은 다른 서비스입니다.

## 헤더

| 헤더 | 값 |
| --- | --- |
| `Content-Type`, `Accept` | `application/json` |
| `SecurityContext` | 보안 컨텍스트. 모든 v2 호출부가 넣습니다 ([보안 컨텍스트](/rest-api/security-context)) |

조회용 POST 라서 CSRF 토큰은 넣지 않습니다. v1 을 부르는 PlatformServicesData 는 `SecurityContext` 도 넣지 않습니다.

## 두 가지 쓰는 방법

| 방법 | 순서 | 쓰는 곳 |
| --- | --- | --- |
| **한 번에** | `getEcosystem` 본문에 `relations`, `direction`, `responseMode` 를 다 넣음 | PIMwebUX, DMUwebR3Mapp, DMUwebR3Mctrl, ENORIPE_Columns |
| **세션** | `getProfiles` 로 패턴을 고르고 → `setPreferences` 로 `widgetId` 에 맡기고 → `getEcosystem` 은 `ids` 만 → 끝나면 `clear` | ENORIPE_Relations, SNNavigationMap |

새 위젯에서 정해진 관계 하나 둘을 받을 때는 "한 번에" 가 단순합니다. 세션 방식은 사용자가 프로필을 바꾸는 탐색 화면용입니다.

## getEcosystem 요청

| 키 | 값 | 설명 |
| --- | --- | --- |
| `ids` | `["<physicalid>", ...]` | 시작 객체 id. ENORIPE_Relations, PIMwebUX |
| `items` | `[{ id, serviceId, type }]` | `ids` 대신 객체 정보로. DMUwebR3Mapp, SNNavigationMap |
| `id` | `"<physicalid>"` | 하나만. ENORIPE_Columns `getDetail` |
| `relations` | `["v6_structure_from"]` | 받을 관계 패턴 ([관계 패턴](#관계-패턴)). 없으면 `setPreferences` 로 맡긴 값 |
| `direction` | `"in"`, `"out"`, `"both"` | 방향. SNNavigationMap 기본 `"both"` |
| `responseMode` | `"ObjectsAndLinks"`, `"objectsByPatterns"` | 응답 모양 ([응답](#getecosystem-응답)) |
| `widgetId` | 문자열 | 서버에 맡긴 설정을 찾는 열쇠. PIMwebUX, ENORIPE_Columns 는 호출마다 시각을 붙인 새 값을 씀 (`'Reveal-' + widget.id + '-' + Date.now()`) |
| `label` | 문자열 | 호출 이름. ENORIPE_Relations 는 `getLabelAPI()` 값, ENORIPE_Columns 는 `'DGV-RelationColumns-<시각>'`, SNNavigationMap 은 모든 요청을 `'RelationalExplorer'` 로 덮어씀 |
| `lang` | `"en"` | 언어 |
| `attributesForView` | `["ds6w:label", "ds6w:modified", ...]` | 객체마다 더 받을 속성 (ENORIPE_Columns) |
| `computeWithInstances` | `false` | 인스턴스까지 셀지. 호출부는 대부분 `false` (SNNavigationMap 만 설정에 따라 `true`) |
| `restrictedMode` | `false` | 뜻은 확인하지 못함 <Badge type="danger" text="검증 필요" /> |
| `trueREST` | `false` (v2), `true` (v1) | 뜻은 확인하지 못함 <Badge type="danger" text="검증 필요" /> |
| `excludedRelations` | 패턴 배열 | 뺄 패턴 (SNNavigationMap) |
| `fullImportMode` | `true` | 여러 단계 펼치기에서 씀 (SNNavigationMap `_getEcosystemByLevel`) |
| `redirectQueryToSGI` | `true` | 인덱스 쪽으로 질의를 돌리는 옵션으로 보입니다 <Badge type="danger" text="검증 필요" /> |

- 타입으로 거르는 키와 깊이를 정하는 키는 호출부에서 보이지 않습니다. 받은 뒤 `type` 이나 `ds6w:type` 으로 거르고 (PIMwebUX 는 `Issue`, `Task` 만, DMUwebR3Mapp 은 `Change Action` 을 뺌), 여러 단계는 다시 부릅니다 ([다른 탐색 방법과 고르기](#다른-탐색-방법과-고르기)).

## 관계 패턴

패턴 이름은 `<관계>_to` 와 `<관계>_from` 쌍입니다. `_to` 는 시작 객체가 가리키는 쪽(하위, 첨부한 것), `_from` 은 시작 객체를 가리키는 쪽(상위, 사용처)입니다. 호출부는 `_from` 을 `direction: "in"`, `_to` 를 `"out"` 과 같이 씁니다.

| 패턴 | 화면 이름 (한국어 NLS) | 쓰는 곳 |
| --- | --- | --- |
| `v6_structure_to` / `v6_structure_from` | 하위 / 상위 | PlatformServicesData (Children / Parents) |
| `v6_structure_roots_from` | 루트 | SNNavigationMap (`excludedRelations` 에 넣어 뺌) |
| `v6_annexdoc_to` / `v6_annexdoc_from` | 첨부 파일 / 다음에 대한 첨부 | PIMwebUX (`in` + `_from`: 이 객체를 첨부로 가진 Issue, Task) |
| `v6_specdoc_to` | 사양 문서 | ENORIPE_Columns (`ColumnsDefinition.json`) |
| `v6_drawing_from`, `simulation_from` | 도면, 시뮬레이션 | PlatformServicesData |
| `v6_cad_document_drawing_to`, `..._process_to`, `..._analysis_to` | 다음에 대한 도면, 프로세스, 분석 | ENORIPE_Columns |
| `changeaction_to` / `changeaction_from` | Change Action | (NLS 만 확인) |
| `issue_to` / `issue_from` | 영향을 받은 아이템 / 영향 | (NLS 만 확인) |
| `carealizedwhere_to` | 변경 조치에서 실현 대상으로 이어진 객체 | DMUwebR3Mapp (Change Action 에서 `out`) |
| `rvw_validated_from` | 이 객체를 대상으로 한 검증 (`DMUValidationValidation`) | DMUwebR3Mapp (`in`) |

- 번역은 `DS/SNNavigationMap/assets/nls/SNNavigationMap_<묶음>` (예 `SNNavigationMap_V6Design`, `SNNavigationMap_ECM`, `SNNavigationMap_Issue`) 에 패턴 이름을 키로 들어 있습니다. 같은 파일에 프로필 이름(`WhereUsed` "사용된 위치 관계", `Designer` "제품 구조 관계", `ProjectManager` "프로젝트 관계")도 있습니다.
- 사용자 환경에서 쓸 수 있는 패턴은 `getProfiles` 결과의 `profiles[].relations[].name` 으로 확인합니다.
- `setPreferences` 의 `relations: ["use-auto-by-type-relations"]` 와 `allRelationsMode: true` 는 "모든 관계" (`AutoModeProfile`) 입니다. 타입에 맞는 패턴을 서버가 고르는 것으로 보입니다 <Badge type="danger" text="검증 필요" />

## getEcosystem 응답

| `responseMode` | 모양 | 읽는 곳 |
| --- | --- | --- |
| `ObjectsAndLinks` | `{ objects: [객체], links: [링크], csl_links }` | PIMwebUX, DMUwebR3Mapp, SNNavigationMap |
| `objectsByPatterns` | `{ objectsByPatterns: { <패턴>: [객체] } }` | ENORIPE_Relations, ENORIPE_Columns |

객체 (호출부가 읽는 키)

| 키 | 설명 |
| --- | --- |
| `id`, `physicalid` | 객체 id. 다른 서비스 객체는 `id` 가 없을 수 있음 |
| `name`, `type`, `revision` | 이름, 타입, 리비전 |
| `serviceId` | `'3DSpace'` 또는 다른 서비스 |
| `ds6w:type`, `ds6w:label`, `ds6w:identifier`, `ds6w:modified`, `ds6w:i3dx` | 6W 속성. **번역되지 않은 값** |
| `type_icon_url`, `preview_url`, `thumbnail` | 아이콘, 미리 보기 |
| `relation` | 이어 준 관계의 id (`objectsByPatterns`) |
| `relations[]` | `{ source, direction }`. `source` 는 시작 객체 id |
| `instances[]` | 인스턴스 목록. 있으면 ENORIPE_Relations 는 `relations` 의 길이(방향이 `in` 이면 1)를 "n 인스턴스" 로 보임 |
| `attributesForView` 에 넣은 키 | 요청한 속성 |

링크 (`ObjectsAndLinks`)

| 키 | 설명 |
| --- | --- |
| `source`, `target` | 양 끝 객체 id |
| `type` | 패턴 이름 (`..._to`, `..._from`) |
| `direction` | `"in"` / `"out"`. SNNavigationMap 은 `"in"` 이면 `_from` 을 `_to` 로 바꾸고 `source`, `target` 을 맞바꿔 그림 |
| `uniqueId`, `isReflexive` | 링크 구분 값, 자기 자신으로 가는 링크 |

- `ds6w:` 값은 번역되어 오지 않습니다. ENORIPE_Relations 는 응답의 `ds6w:` 키를 모아 `FedDictionaryAccessAPI.getNlsOfPropertiesValues` 로 번역한 뒤 그립니다 ([타입, 속성 정보](/rest-api/dictionary#값-번역-getnlsofpropertiesvalues)).
- `id` 가 없고 `serviceId` 가 3DSpace 가 아닌 객체는 다른 서비스 객체입니다. ENORIPE_Relations, SNNavigationMap 은 `ds6w:i3dx` 로 3DSearch 에 다시 물어 채웁니다 (`_prepareURI`).
- `type: 'json'` 을 주지 않으면 응답이 문자열로 옵니다. ENORIPE_Relations, ENORIPE_Columns 는 `JSON.parse` 합니다.

## 예: 사용처(상위) 받기

시작 객체를 가리키는 객체를 한 번에 받습니다. PIMwebUX `getEnoviaModelRelations` 와 같은 본문입니다.

```js
function getWhereUsed(spaceUrl, platformId, securityContext, objectId) {
  return new Promise(function (resolve, reject) {
    WAFData.authenticatedRequest(spaceUrl + '/resources/enorelnav/v2/navigate/getEcosystem?tenant=' + platformId, {
      method: 'POST',
      type: 'json',
      headers: { Accept: 'application/json', 'Content-Type': 'application/json', SecurityContext: securityContext },
      data: JSON.stringify({
        ids: [objectId],
        relations: ['v6_structure_from'],
        direction: 'in',
        responseMode: 'ObjectsAndLinks',
        restrictedMode: false,
        trueREST: false,
        label: 'MyWhereUsed',
        widgetId: 'MyWhereUsed-' + widget.id + '-' + Date.now()
      }),
      onComplete: function (res) {
        resolve((res.objects || []).filter(function (o) { return o.id !== objectId; }));
      },
      onFailure: function (error, data) { reject({ error: error, data: data }); }
    });
  });
}
```

- 하위를 받으려면 `relations: ['v6_structure_to']`, `direction: 'out'` 입니다.
- `objects` 에는 시작 객체도 들어 있습니다. DMUwebR3Mapp 은 시작 객체(Change Action)를 `ds6w:type` 으로 걸러 이름만 꺼내고 나머지를 결과로 씁니다. 위 예는 `id` 로 뺍니다.

## 개수만 받기 (getRelationCount)

```json
{
  "widgetId": "MyCount-<위젯 id>-<시각>",
  "label": "MyCount",
  "ids": ["<physicalid>", "<physicalid>"],
  "relations": ["v6_structure_to", "v6_specdoc_to"],
  "computeWithInstances": false
}
```

응답 `{ counts: [{ source, name, count }] }`. `source` 는 시작 객체 id, `name` 은 패턴, `count` 는 **문자열** 이라 ENORIPE_Columns 는 `parseInt` 합니다. 개수가 0 인 패턴은 오지 않는 것으로 보고 0 으로 먼저 채워 둡니다. SNNavigationMap 은 `ids` 대신 `items` 를 보냅니다.

## 세션 방식 요청

| 요청 | 본문 | 응답에서 읽는 것 |
| --- | --- | --- |
| `v2/navigate/getProfiles` | `{ widgetId, roles, lang, label }` | `profiles[]` 의 `name`, `type` (`'custom'`), `nls`, `isAppSpecific`, `relations[].name` |
| `v2/navigate/getPatterns` | `{ widgetId, roles, lang, label }` | 패턴 목록 |
| `v2/navigate/setPreferences` | `{ widgetId, relations, allRelationsMode, publicationsMode, computeWithInstances, attributesForView, excludedRelations, lang, ghostMode, label }` | 없음 |
| `v2/navigate/getEcosystem` | `{ widgetId, ids, responseMode: 'objectsByPatterns', label }` | [응답](#getecosystem-응답) |
| `v2/navigate/clear` | `{ widgetId }` | 없음 |
| `v2/navigate/getSynchroStatus` | `{ widgetId, ids }` | 동기화 상태 |

- `roles` 는 사용자 역할 목록입니다. ENORIPE_Relations 는 `getrelsettings` 의 `relation_sets` (역할, 사용자 그룹별 패턴 묶음) 를 받아 프로필에 합칩니다 (`_mergeSettings`).
- 프로필 이름의 화면 글자는 `type` 이 `'custom'` 이면 `nls`, 아니면 NLS 파일에서 찾습니다.
- 예전 v1 은 `navigate/setpreferences` (`{ widgetId, relations, countmanagement: false }`) → `navigate/getecosystem` (`{ widgetId, ids }`) → `navigate/detail` (`{ widgetId, id }`) 세 번을 부르고, `detail` 응답의 `newstructure.<패턴>` 배열을 읽습니다 (PlatformServicesData).

## v1 getecosystem 응답

v1 을 한 번에 부르면 (`{ ids, relations, trueREST: true, countmanagement: false, debug: false, widgetId }`) 응답이 v2 와 다릅니다.

```json
{
  "New": {
    "Objects": [ { "id": "<physicalid>", "ds6w:type": "..." } ],
    "Links": [ { "source": "<id>", "target": "<id>" } ]
  }
}
```

CompareWidget2 는 `New.Links[].target` 을 모아 이어진 객체 id 로 씁니다. 새로 만들 때는 v2 를 씁니다.

## 다른 탐색 방법과 고르기

| 방법 | 받는 것 | 고르는 기준 |
| --- | --- | --- |
| enorelnav `getEcosystem` (이 페이지) | 관계 패턴 이름으로 이어진 객체, 링크, 개수 | 타입이 섞인 관계(첨부, 사용처, 변경, 이슈)를 화면에 보일 때. 관계 이름(MQL relationship)을 몰라도 됨 |
| A 계열 `relateddata` | `$include=` 로 정해진 관계 객체 | 문서의 파일, 태스크의 담당자처럼 그 서비스가 정한 관계만 필요할 때 ([응답 형식 A](/rest-api/#응답-형식-a-업무-객체), [문서 - 부모 기준 조회](/rest-api/documents#엔드포인트)) |
| B 계열 ds 서비스 | `$mask`, `$depth`, `invoke/<서비스>:expand` (예 `dsprcs/invoke/dsprcs:expand?$mask=...`) | 제품, 공정 구조를 깊이를 정해 펼칠 때 ([쿼리 파라미터](/rest-api/#쿼리-파라미터), [엔드포인트 목록](/rest-api/catalog#modeler-ds-서비스-b-계열)) |
| `/cvservlet/progressiveexpand/v2`, `/cvservlet/multiexpand` | 구조 펼치기 | 3D 구조 화면(ENOFilterBIUX)용. 응답 형식은 이 문서에서 다루지 않음 |
| 서버 JPO `getRelatedObjects` | MQL 관계 이름, 방향, 깊이로 | 서버에서 처리할 때 ([DomainObject - 관계 따라가기](/jpo/domain-object#관계-따라가기-getrelatedobjects)) |

- 호출부에서 깊이를 정하는 키는 보이지 않습니다. SNNavigationMap 은 여러 단계를 받을 때 앞 단계 결과로 `getEcosystem` 을 다시 부릅니다 (`_getEcosystemByLevel`, `fullImportMode: true`).
- 개수만 필요하면 `getRelationCount` 가 가볍습니다. ENORIPE_Columns 는 그리드 칸에 개수를 먼저 보이고, 펼칠 때 `getEcosystem` 을 부릅니다.

## 2026 변경

| 항목 | 2025 | 2026 |
| --- | --- | --- |
| `v2/navigate/i3dx/getecosystem`, `i3dx/getrelationcount` <Badge type="warning" text="2026" /> | 없음 | SNNavigationMap 이 다른 서비스 객체까지 이어 볼 때(`isCrossServicesNavigation`) 씀. `setPreferences` 에 보낸 값을 브라우저에 두었다가 이 요청 본문에 합쳐 보냄 |
| `v2/navigate/getAllDefaultProfiles` <Badge type="warning" text="2026" /> | 없음 | SNNavigationMap. 본문 `{ label }` |
| ENORIPE_Relations 의 프로필 요청 | v1 `navigate/getProfiles` (`profileVersion: 2`) | v2 `getProfiles`, 본문에 `application`, `userGroups` 를 더함 |
| `redirectQueryToSGI` | 설정 값이 있으면 그 값 | `is3DSpaceIUExistingAndTenantAllowed()` 이고 `ActivateOptimizedIndexNavigation` 또는 `RedirectQueryToSGI` 설정이 켜져 있으면 `true` |
| `enorelnav/v2/navigate` 문자열 | 20 회 | 25 회 (같은 7 개 모듈) |
| v1 `navigate/getecosystem` | CompareWidget2 등 4 곳 | 5 곳. CompareCommon 이 `attributes_for_detailview`, `displayTime` 을 넣어 부름 |

## 주의

::: warning widgetId 는 호출마다 새로 만듭니다
세션 방식은 `setPreferences` 에 보낸 패턴을 `widgetId` 로 다시 찾습니다 (ENORIPE_Relations 의 `getEcosystem` 본문에는 `relations` 가 없음). 같은 `widgetId` 를 다른 화면이나 다른 용도에 쓰면 맡긴 설정이 섞일 수 있습니다 <Badge type="danger" text="검증 필요" />. 한 번에 방식으로 부를 때 PIMwebUX, ENORIPE_Columns 는 `widgetId` 에 시각을 붙여 호출마다 다른 값을 쓰고, 세션 방식은 끝날 때 `clear` 를 부릅니다 (SNNavigationMap).
:::

- 쓸 수 있는 패턴은 역할, 앱 설정에 따라 다른 것으로 보입니다 (`getProfiles` 가 `roles` 를 받음) <Badge type="danger" text="검증 필요" />. 하드코딩하기 전에 `getProfiles` 결과에 있는지 확인합니다.
- ENORIPE_Relations 는 `getEcosystem` 에 설정 시간의 2 배(`2 * getTimeout()`)를 줍니다. 결과가 많으면 시간 초과로 끝날 수 있으니 `onTimeout` 을 처리합니다.
- 모든 호출이 POST 라서 브라우저 캐시를 타지 않습니다. 같은 객체를 자주 펼치면 화면 쪽에서 결과를 기억해 둡니다.
