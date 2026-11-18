# 6W 태그 연동 (TagNavigatorProxy) <Badge type="info" text="원본 분석" />

`DS/TagNavigatorProxy/TagNavigatorProxy` 는 위젯 목록의 행마다 6W 태그(무엇 / 누가 / 언제 / 어디서 ...)를 붙여 3DDashboard 의 **태그 창(6W Tagger)** 에 보이게 하고, 사용자가 태그를 고르면 그 결과를 위젯에 알려 주는 모듈입니다. 위젯은 받은 결과로 자기 목록을 거릅니다.

::: info 근거
TagNavigatorProxy/TagNavigatorProxy.js (위젯 쪽: `TagNavigatorProxy.createProxy`, `AbstractTagger6WProxy`, `Tagger6WProxy`, `Tagger6WProxyWithFilteringServices`), TagNavigator/TagNavigator.js (태그 창 쪽: InterCom 서버 `com.ds.tagger6w`, `TaggerProxy`, `TaggerProxyWithFilteringServices`, `Predicate`, `TagValue`), TopFrame/TopFrame.js (`_initTagNavigator`). 모듈을 쓰는 앱 27 개. 호출부: Foundation / Foundation2 (`WidgetTagNavInit`, `TaggerAggregator`, `TNProxyFilterOnServerHelper`), ENOProjectMgmtCommonComponents, ENOPartMgt (`PartContentsCollection`), W3DContentApp, ENXSBGridConnector (`6WTag.js`), IPClassifyUX, ENOFilterBIUX, SNResultUX, 3DPlayUtils. 문자열 집계 (webapps 전체, 같은 이름의 다른 객체 포함): `TagNavigatorProxy.createProxy(` 10, `.setSubjectsTags(` 31, `.addSubjectsTags(` 7, `"onFilterChange"` 31, `"onFilterSubjectsChange"` 18. `filteringMode` 값: `"WithFilteringServices"` 13 폴더, `"FilteringOnServer"` 11 폴더
:::

샘플: [TaggerSample.html](https://github.com/LeeOuHyoun/3d-docs/blob/main/WidgetSample/TaggerSample.html) (작은 목록에 태그 붙이기, 태그 창에서 고르면 목록 거르기, activate / deactivate, die). 3DDashboard 에서만 동작합니다. 단독 실행에서는 태그 창이 없어 `proxyIsReady` 도, 필터 이벤트도 오지 않습니다.

## 만들기

```js
require(['DS/TagNavigatorProxy/TagNavigatorProxy'], function (TagNavigatorProxy) {
  const proxy = TagNavigatorProxy.createProxy({
    widgetId: widget.id,
    filteringMode: 'WithFilteringServices'
  });
});
```

- 모듈은 객체 하나이고 `createProxy(options)` 로 proxy 를 만듭니다. `new` 하지 않습니다.
- `filteringMode` 가 두 값 중 하나가 아니면 `undefined` 를 돌려줍니다 (콘솔 `Invalid input Args`).
- `widgetId` 도 `contextId` 도 문자열이 아니면 바로 예외입니다 (`If in 3DD context: give a widgetId. Else give a contextId.`).
- 상수: `TagNavigatorProxy.filteringMode.WithFilteringServices`, `TagNavigatorProxy.filteringMode.FilteringOnServer`.
- 돌려받는 proxy 는 UWA Model 입니다. `addEvent`, `listenTo` 로 이벤트를 받고, `get('hasFilter')`, `get('isFocused')` 로 상태를 읽습니다.

### 거르는 방식 두 가지

| `filteringMode` | 누가 거르나 | 위젯이 보내는 것 | 위젯이 받는 이벤트 | OOTB |
| --- | --- | --- | --- | --- |
| `'WithFilteringServices'` | 태그 창 (브라우저) | 행마다 태그 (`setSubjectsTags`) | `onFilterSubjectsChange`: 남길 행 id 목록 | Foundation, ENOProjectMgmtCommonComponents, ENOPartMgt, W3DContentApp (3DDrive), 3DPlayUtils |
| `'FilteringOnServer'` | 위젯이 서버에 다시 검색 | 태그 요약과 개수 (`setTags`) | `onFilterChange`: 고른 태그 | SNResultUX (3DSearch 결과), IPClassifyUX, ENOFilterBIUX, W3DContentApp |

목록 전체를 이미 가지고 있으면 `'WithFilteringServices'`, 서버에서 페이지 단위로 받는 목록이면 `'FilteringOnServer'` 를 씁니다.

### 옵션

| 옵션 | 설명 |
| --- | --- |
| `widgetId` | 3DDashboard 에서 필수. `widget.id` 그대로. 태그 창이 이 값으로 위젯이 있는 context(탭으로 보임 <Badge type="danger" text="검증 필요" />)를 찾습니다. 대시보드가 모르는 id 면 태그 창 쪽에서 `The widget id is unknown by 3DDashboard.` 오류가 나고 위젯에는 아무것도 오지 않습니다 |
| `contextId` | 3DDashboard 밖(3DSpace 단독 화면)에서 `widgetId` 대신. OOTB 는 `'context1'` (Foundation, ENOPartMgt, ENOProjectMgmtCommonComponents) |
| `filteringMode` | 필수. 위 표 |
| `tenant` | 플랫폼 id. 태그 창이 이 값으로 3DSpace / 6WTags 서비스 주소를 찾습니다. OOTB 는 `'onpremise'` 이면 넣지 않습니다 |
| `pod` | `'6WTags'` 또는 `'3DSpace'`. 태그 서버를 고릅니다 (Foundation, W3DContentApp, 3DPlayUtils 는 `'6WTags'`). 사용자에게 보이는 차이는 확인하지 않았습니다 <Badge type="danger" text="검증 필요" /> |
| `events` | 콜백 묶음. 키 이름이 정해져 있습니다 ([이벤트](#이벤트)) |
| `appId` | 앱 id (`XXXXX_AP`). 관리자가 앱별로 정한 분류 설정을 읽을 때 씁니다 (ENOFilterBIUX) |
| `colorize` | `true` 면 태그로 색칠 기능 (`colorize` / `unColorize` 이벤트) |
| `exclude` | `true` 면 태그 제외 기능 (IPClassifyUX) |
| `forceFilterChangeEvent` | `'FilteringOnServer'` 에서 등록되자마자 필터가 없어도 `onFilterChange` 한 번 |
| `rightParentWindow` | 태그 창이 있는 창을 직접 지정. 없으면 부모 창을 따라 올라가며 `create6WTaggerView` 가 있는 창을 찾습니다 |

그 밖에 `enableGrouping`, `freeInput`, `preciseCount`, `predicateSortOrder`, `enableTagsOnDemand`, `embeddedTagger`, `predicateSummary` 같은 키도 태그 창이 읽지만 이 문서에서는 다루지 않습니다.

## 태그 데이터

### 행마다 태그 (setSubjectsTags)

<<< @/../WidgetSample/TaggerSample.html#tags{js}

키는 행 id(주제, subject), 값은 그 행의 태그 배열입니다.

| 태그 키 | 필수 | 설명 |
| --- | --- | --- |
| `sixw` | O | 분류 경로 `ds6w:<W>/<분류 id>`. 첫 조각은 `ds6w:what`, `ds6w:who`, `ds6w:when`, `ds6w:where`, `ds6w:why`, `ds6w:how` 중 하나. 마지막 조각이 분류 id, 가운데 조각은 상위 분류 |
| `object` | O | 값. 문자열, 또는 계층 값이면 배열. 숫자나 빈 값은 오류 ([주의](#주의)) |
| `dispValue` | | 태그 창에 보일 값. 없으면 `object` |
| `type` | | `'string'` (기본), `'integer'`, `'double'`, `'float'`, `'boolean'`, `'date'`, `'tree'`, `'daterange'`. 앞의 `xsd:` 는 떼어 냅니다. 모르는 값은 `'string'` |
| `field` | | 값을 꺼낸 속성 이름 같은 표시. 고른 태그의 `field` 배열로 돌아옵니다. `'explicit'` 이면 사용자 태그로 취급합니다 |

- 분류 이름: 분류 id 가 플랫폼 어휘(vocabulary)에 있으면 태그 창이 서버에서 받은 번역 이름을 보여 주고, 없으면 id 를 그대로 보여 줍니다. ENOPartMgt 는 `'ds6w:what/' + NLS 이름` 처럼 어휘에 없는 id 를 씁니다.
- 날짜: `type: 'date'` 에 `'YYYY/MM/DD'`. 태그 창에는 연 → 월 → 일 계층으로 보입니다. 보여 줄 때는 `-` 도 `/` 로 바꾸고 앞 10 글자만 쓰지만, 화면에서 거르기(`'WithFilteringServices'`)는 넣은 문자열 그대로 앞부분을 비교합니다. 처음부터 `/` 로 넣습니다.
- 계층 값: `type: 'tree'` 에 `'A/B/C'`, 또는 `object` 를 배열로 줍니다.

자주 쓰는 `sixw` (webapps 문자열 수, 3DSearch 용도 포함):

| `sixw` | 수 | 뜻 |
| --- | --- | --- |
| `ds6w:what/ds6w:type` | 116 | 타입 |
| `ds6w:what/ds6w:status` | 79 | 상태(성숙도) |
| `ds6w:who/ds6w:responsible` | 55 | 담당자 |
| `ds6w:when/ds6w:modified`, `ds6w:when/ds6w:created` | 31, 28 | 수정일, 작성일 (`type: 'date'`) |
| `ds6w:what/ds6w:kind` | 27 | 종류 |
| `ds6w:where/ds6w:context/ds6w:project` | 23 | 프로젝트(협업 공간) |
| `ds6w:what/ds6w:policy` | 20 | 정책 |
| `ds6w:where/ds6w:source/ds6w:dataSource` | 18 | 출처 |
| `ds6w:who/ds6w:responsible/ds6w:organizationResponsible` | 17 | 담당 조직 |

### 행 id (주제)

- 키는 위젯이 정하는 문자열입니다. 태그 창은 같은 값을 `filteredSubjectList` 로 돌려주므로, 그 값으로 행을 다시 찾을 수 있으면 됩니다.
- OOTB 는 3DSpace 객체면 `'pid://' + physicalid` (Foundation, ENOProjectMgmtCommonComponents, W3DContentApp), 3DDrive 파일이면 `'driveid:' + id` (W3DContentApp) 를 씁니다.
- 태그 창은 이 id 로 태그 서버에서 사용자가 직접 붙인 태그(사용자 태그)를 찾아 합치기도 합니다 (proxy 가 `hasPlatformData` 가 아닐 때, `TaggerProxyWithFilteringServices.getExplicitTags`). 사용자 태그까지 쓰려면 OOTB 형식을 따르는 것이 안전합니다 <Badge type="danger" text="검증 필요" />.

## 고른 태그 받기

### 화면에서 거르기 (WithFilteringServices)

<<< @/../WidgetSample/TaggerSample.html#proxy{js}

<<< @/../WidgetSample/TaggerSample.html#filter{js}

`onFilterSubjectsChange` 로 오는 값:

| 키 | 설명 |
| --- | --- |
| `filteredSubjectList` | 남길 행 id 배열. **필터가 없으면 모든 행** |
| `allfilters` | 고른 태그. `{ sixw: [ { object, type, disptext, field } ] }` |
| `localfilters` | 이 proxy 에서 고른 태그 |
| `hasFiltersTEMP` | 필터가 있는지 (이름으로 보아 임시 키) |

- 같은 분류 안에서 고른 값은 하나만 맞으면 되고, 분류가 여럿이면 모든 분류에 맞아야 남습니다 (분류마다 맞은 행을 세어 고른 분류 수 이상인 행만 남김).
- `date`, `tree`, 배열 값은 앞부분이 같으면 맞습니다. `2025` 를 고르면 `2025/10/30` 행이 남습니다.
- 사용자가 태그를 고르거나 풀 때 옵니다. 이미 필터가 걸린 상태에서 `setSubjectsTags` 를 부르면 바로 한 번 옵니다. 필터가 없을 때는 `setSubjectsTags` 해도 오지 않습니다.
- 필터가 있는지는 `filteredSubjectList` 길이가 아니라 `allfilters` 로 봅니다 (W3DContentApp 방식).

받는 방법은 넷 중 하나입니다.

| 방법 | OOTB |
| --- | --- |
| `proxy.addEvent('onFilterSubjectsChange', fn)` | Foundation2, ENOProjectMgmtCommonComponents, 3DPlayUtils |
| `listenTo(proxy, { onFilterSubjectsChange: fn })` | W3DContentApp |
| `proxy.addFilterSubjectsListener(fn, scope)` (하나만 보관, 다시 부르면 바뀜) | Foundation, ENOPartMgt, ENXSBGridConnector |
| 옵션 `events: { addFilterSubjectsListener: fn }` | |

### 서버로 거르기 (FilteringOnServer)

```js
const proxy = TagNavigatorProxy.createProxy({
  widgetId: widget.id,
  filteringMode: 'FilteringOnServer'
});

proxy.addEvent('onFilterChange', function (filters) {
  searchAgain(filters.allfilters);
});

proxy.setTags(null, [
  { sixw: 'ds6w:what/ds6w:type', object: 'Part', dispValue: 'Part', count: 12 },
  { sixw: 'ds6w:what/ds6w:status', object: 'IN_WORK', dispValue: 'In Work', count: 5 }
], { count: 40 });
```

| 인자 | 설명 |
| --- | --- |
| `tagsdata` | 행마다 태그 (`setSubjectsTags` 와 같은 모양). 사용자가 행에 태그를 붙이게 할 때만 줍니다 (Foundation2 주석). 없으면 `null` |
| `summaryData` | 태그 요약 배열 `{ sixw, object, dispValue, type, count }`. 비어 있으면 `tagsdata` 로 개수를 셉니다 |
| `options` | `{ count: 전체 결과 수, hideCounts: true/false, predicateOptions }` |

- `onFilterChange` 값은 `{ allfilters, localfilters }` 입니다. Foundation2 주석 예: `{ allfilters: { 'ds6w:when/ds6w:starts': [{ object: '2013', type: 'date' }] } }`.
- 등록될 때(`proxyIsReady`) 이미 필터가 있으면 `onFilterChange` 가 한 번 옵니다.
- 결과가 없으면 `setTags({}, [])` 로 비웁니다.

## 메서드

| 메서드 | 방식 | 설명 |
| --- | --- | --- |
| `setSubjectsTags(tags)` | 화면 | 행 태그 전체를 바꿈 |
| `addSubjectsTags(tags)` | 화면 | 이미 보낸 태그에 덧붙임 |
| `setTags(tagsdata, summaryData, options)` | 서버 | 위 표 |
| `unsetTags()` | 둘 다 | 태그를 모두 비우고 `unfocus` |
| `activate()` / `deactivate()` | 둘 다 | 이 proxy 의 태그를 태그 창에 보이게 / 빼기. 비활성 proxy 는 표시되지 않고 필터 변경도 받지 않습니다 |
| `die()` | 둘 다 | 태그 창에서 proxy 를 지움. 응답(`isDead`)이 오면 연결을 끊음 |
| `getCurrentFilter()` | 둘 다 | 마지막으로 받은 필터의 복사본 (`allfilters`, `localfilters` ...) |
| `setLocalFilter(filter, warn)`, `addLocalFilter`, `removeLocalFilter`, `clearLocalFilter(refine)` | 둘 다 | 코드로 태그 고르기 / 풀기. `filter` 는 `{ sixw: [{ object, type, disptext, field }] }` (ENOProjectMgmtCommonComponents) |
| `focusOnSubjects(ids, persistTags, dataSources, options)` / `unfocus()` | 둘 다 | 고른 행 id 를 태그 편집 대상(`subjectsInEdition`)으로 넘김 / 풂. 사용자가 그 행에 태그를 붙일 때 쓰는 것으로 보입니다 <Badge type="danger" text="검증 필요" />. Foundation 은 선택한 행 id 를 넘기고 선택이 없으면 `unfocus` |
| `configurePredicates(config)` | 둘 다 | 분류별 설정. `{ 'ds6w:created': { freeInputOnly: true } }`. 키: `weight` (1 미만이면 숨김), `relevant`, `expanded`, `hideCount`, `allowFreeInput`, `freeInputOnly` |
| `toggleProxyFocus(bool)` | 둘 다 | 이 위젯 태그만 보기 |
| `setInfoMessage(data)` / `clearInfoMessage()` | 둘 다 | 태그 창 안 안내 글 |

### 이벤트

| 이벤트 (`addEvent`) | 받는 값 | 언제 |
| --- | --- | --- |
| `onFilterSubjectsChange` | [위 표](#화면에서-거르기-withfilteringservices) | 화면 방식에서 필터가 바뀜 |
| `onFilterChange` | `{ allfilters, localfilters }` | 서버 방식에서 필터가 바뀜 |
| `proxyIsReady` | `{ filters, lang, forceFilterChangeEvent }` | 태그 창이 proxy 를 등록함 |
| `colorize` / `unColorize` | 색 정보 | `colorize: true` 일 때 |
| `onChange:hasFilter`, `onChange:isFocused` | (Model 속성) | 필터 / 포커스 상태가 바뀜 |

옵션 `events` 에서 읽는 키는 `addFilterSubjectsListener`, `addFilterChangeListener`, `addTagCreationListener` / `addTagDeletionListener` (사용자 태그를 만들거나 지움. 화면 방식에서 `{ predicate, object, subjects }`), `addUnfocusSubjectsListener`, `addOpenTaggerListener` / `addCloseTaggerListener` (서버 방식, 태그 창 열림 / 닫힘), `addColorizeListener`, `addFocusChangeListener` 들입니다. `events: { onFilterSubjectsChange: fn }` 처럼 이벤트 이름을 넣으면 **읽지 않습니다** (ENOPartMgt 는 넣지만 따로 `addFilterSubjectsListener` 를 부릅니다).

## 동작 방식

```text
createProxy(options)
  → 부모 창을 따라 올라가며 create6WTaggerView 가 있는 창을 찾음 (없으면 top)
  → InterCom 소켓으로 그 창의 서버 com.ds.tagger6w 에 getProxy
  → 태그 창: widgetId 로 context 를 찾고 그 context 의 태그 창에 proxy 등록
  → proxyIsReady (현재 필터, 언어)
setSubjectsTags, setTags, focusOnSubjects, unfocus, unsetTags, configurePredicates
  → top 창에 접근할 수 있으면 top 의 DS/TagNavigator/TagNavigator 를 직접 부름 (데이터는 복사)
  → 접근할 수 없으면 InterCom 메시지
사용자가 태그를 고름
  → 그 context 의 활성 proxy 마다 결과 계산
  → onFilterSubjectsChange (화면 방식) / onFilterChange (서버 방식)
```

- 등록되기 전에 보낸 `setSubjectsTags`, `setTags`, `activate`, `deactivate`, `focusOnSubjects`, `unfocus`, `unsetTags`, `clearLocalFilter`, `configurePredicates` 는 태그 창이 쌓아 두었다가 등록되면 실행합니다 (콘솔 `stacking a proxy call`). 그래서 `createProxy` 바로 뒤에 `setSubjectsTags` 해도 됩니다. `addSubjectsTags`, `setLocalFilter`, `setColorMap` 같은 나머지는 쌓지 않으므로 `proxyIsReady` 뒤에 부릅니다.
- 같은 context 의 위젯들은 태그 창 하나를 같이 씁니다. 고른 태그는 그 context 의 활성 proxy 모두에 적용됩니다. 그래서 다른 위젯에만 있는 분류의 태그를 고르면 내 위젯의 `filteredSubjectList` 는 빈 배열이 됩니다.
- 주소에 `proxyLogs` 나 `6WTaggerLogs` 가 있으면 proxy 로그를 콘솔에 남기고, `TN_activateInterCom` 이 있으면 직접 호출 대신 InterCom 만 씁니다.

::: info 태그 창 코드는 webapps 에 있습니다
태그 창 자체(TagNavigator), 그것을 띄우는 최상위 창 코드(TopFrame)는 webapps 에 있습니다. 하지만 TopFrame 에 넘겨 주는 `getContextIdFromWidgetId` (위젯 id → context), `getTabTitleFromTabId` 같은 함수는 3DDashboard 본체가 주는 것이라 webapps 에 없습니다. context 가 대시보드 탭이라는 것은 이 함수 이름으로 본 추정입니다 <Badge type="danger" text="검증 필요" />.
:::

## 숨길 때, 닫을 때

<<< @/../WidgetSample/TaggerSample.html#lifecycle{js}

| 상황 | OOTB 처리 |
| --- | --- |
| 목록을 가리고 다른 화면(상세 보기)을 보일 때 | `deactivate()`, 돌아오면 `activate()` (ENOPartMgt `PartSkeleton`, ENOChgServices) |
| 위젯 안 탭을 바꿀 때 | `clearLocalFilter()` + `unsetTags()` + `deactivate()` (IPClassifyUX) |
| 대시보드 탭을 바꿀 때 | 위젯이 따로 하는 일 없음. 태그 창이 context 마다 따로 있는 것으로 보입니다 <Badge type="danger" text="검증 필요" /> |
| 위젯이 닫히거나 다시 읽힐 때 | `window.onunload` 에서 `die()` (Foundation2, ENOProjectMgmtCommonComponents). ENXSBGridConnector 는 `beforeunload` |
| 보여 줄 대상이 바뀔 때 | `die()` 후 새 proxy (W3DContentApp 이 드라이브를 바꿀 때) |

- `onLoad` 는 새로고침 때 다시 불릴 수 있습니다 ([onRefresh](/widget/widget-api#onrefresh)). proxy 를 그때마다 만들지 않도록 한 번만 만들어 둡니다 (샘플은 `if (!proxy)`).
- `die()` 한 proxy 는 다시 쓰지 않고 새로 만듭니다.

## 주의

::: warning sixw 와 object 형식을 지킵니다
- `sixw` 첫 조각이 `ds6w:what`, `ds6w:who`, `ds6w:when`, `ds6w:where`, `ds6w:why`, `ds6w:how` 가 아니면 분류를 만들지 못해 오류가 납니다 (`Fail to create a predicate with invalid arg`). 그 태그는 제대로 보이지 않고 개수도 어긋날 수 있습니다.
- `object` 가 숫자이거나 빈 값이면 태그 값을 만들지 못합니다. 필터가 없을 때 보낸 `setSubjectsTags` 는 개수를 세는 단계(`_computeTagsValues`)에서 오류가 나 그 호출의 태그가 **하나도** 보이지 않습니다. 숫자는 `String(n)` 으로 바꿔 넣습니다.
:::

- 같은 분류에서 값을 둘 이상 고르고 다른 분류도 고르면, 앞 분류 값을 둘 다 가진 행은 뒤 분류와 맞지 않아도 남을 수 있습니다. 맞은 개수로 판단하는 계산 때문입니다.
- `widgetId` 에는 `widget.id` 외의 값을 넣지 않습니다. 대시보드가 모르는 id 면 아무 이벤트도 오지 않습니다.
- 3DDashboard 전용입니다. 단독 실행에서는 `proxyIsReady` 가 오지 않습니다. 화면은 태그 없이 그대로 두는 것이 안전합니다.

## 2026 변경

| 항목 | 내용 |
| --- | --- |
| TagNavigatorProxy.js | 2025 와 같은 파일 (내용 같음) |
| 태그 창이 받는 메시지 | 38 개 이름 같음 (`setSubjectsTags`, `setTags`, `activate` ...) |
| `configurePredicates` <Badge type="warning" text="2026" /> | 분류별 `viewMode` 키 추가. 처음 보일 모양을 정하는 것으로 보입니다 (`'tag'`, `'people'`, `'freeInput'` 값이 보임) <Badge type="danger" text="검증 필요" /> |
| `i3dxUtils6W` 폴더 <Badge type="warning" text="2026" /> | 새 모듈 `DS/i3dxUtils6W/i3dxUtils`. `i3dx:도메인!id` 형태 주소를 나누고(`split3dx`), 3DSpace `/resources/domains/v1/registry/items` 의 도메인 목록으로 도메인별 fetch 주소를 찾는 도우미입니다. RDFFoundation, E6WCommonApps 가 쓰고, TagNavigatorProxy / TagNavigator 는 쓰지 않습니다 |

## 관련 문서

- 선택한 객체를 Compass 에 알리기: [Compass 에 선택 객체 알리기](/widget/compass-pubsub)
- 상단 바 메뉴에 명령 넣기: [대시보드 상단 바](/widget/top-bar)
- 창 사이 메시지 구조: [InterCom](/uwa/intercom)
- 플랫폼 id 얻기: [3DSpace 주소와 SecurityContext](/widget/space-url)
