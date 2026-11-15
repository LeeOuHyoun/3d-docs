# 3DSearch 에서 객체 골라 오기 <Badge type="info" text="원본 분석" />

위젯에서 3DDashboard 의 3DSearch 화면을 열고, 사용자가 고른 객체나 사람을 받아 오는 방법(in-context search)입니다. OOTB 의 "기존 항목 추가", "담당자 지정", "구성원 추가" 단추가 이 방법을 씁니다. 화면 없이 코드로 검색하는 REST(`/federated/search`)는 [끝 절](#화면-없이-검색-federated-search)에 있습니다.

::: info 근거
SNInfraUX/SNInfraUX.js 의 `DS/SNInfraUX/SearchCom` (소켓 껍데기), `DS/SNInfraUX/SearchRessources` (검색 요청 본문, `getFedSearchUrl`), `DS/SNInfraUX/Api/SearchApi`. SNSearchUX/SNSearchUX.js 의 `DS/SNSearchUX/InContextSearch` (`RegisterContext`, `InContextSearch`, `UnregisterContext` 를 받는 쪽), `DS/SNSearchUX/SearchUX_TopBar`. SNResultUX/SNResultUX.js 의 `dispatchInContextSelection`, `_createFurtiveControls`, `buildCompassSelectionData`, `DS/SNResultUX/utils/FederatedSearch_parse`. UWA2/js/Utils/InterCom.js, UWA2/js/Json.js (`prune`). 호출부: ENOUserGroupMgmt, ENORouteMgmt (11 곳), ENXMeetingMgmt, ENXESignConfigApp, `DS/E6WCommonUI/Search`, PADUtils `inAppsSearch`, ENOXSearchUIController, LifecycleWidget, 3DSearch 의 "저장된 검색 수정". 집계: `DS/SNInfraUX/SearchCom` 을 쓰는 앱 15 (2026 16), InterCom 소켓으로 `SearchComServer` 에 직접 붙는 앱 32, in-context 옵션(`app_socket_id`)을 만드는 앱 43, `Selected_Objects_search` 35 파일, `UnregisterContext` 30 파일 46 건. 옵션 표와 받는 키 표의 앱 수는 OOTB 소스에서 센 근사값 (3DSearch 자체 모듈 제외). REST: RDFFoundation `FederatedSearchUtil`, SearchWidget. 2026 SNInfraUX, SNSearchUX, SNResultUX 와 비교함
:::

샘플: [SearchSample.html](https://github.com/LeeOuHyoun/3d-docs/blob/main/WidgetSample/SearchSample.html) (타입을 제한해 3DSearch 열기, 여러 개 선택, 받은 객체 빼기, 사람 고르기, 받은 목록, 등록 해제, 글자로 검색). 3DDashboard 위젯에서만 3DSearch 가 열립니다. 단독 실행에서는 화면만 뜨고 단추를 눌러도 아무 일도 일어나지 않습니다.

## 부르는 법

```js
require(['DS/SNInfraUX/SearchCom'], function (SearchCom) {
  var socketId = 'XX_search_' + widget.id;
  var socket = SearchCom.createSocket({ socket_id: socketId });

  socket.addListener('Selected_Objects_search', function (objects) {
    objects.forEach(function (obj) {
      console.log(obj.id, obj['ds6w:label_value'], obj['ds6w:type_value']);
    });
  });

  var options = {
    app_socket_id: socketId,
    widget_id: socketId,
    mode: 'furtive',
    title: 'Select parts',
    multiSel: true,
    precond: 'flattenedtaxonomies:"types/VPMReference"',
    default_with_precond: true,
    source: ['3dspace']
  };
  socket.dispatchEvent('RegisterContext', options);
  socket.dispatchEvent('InContextSearch', options);
});
```

- 모듈은 클래스가 아니라 **이미 만들어진 객체 하나** 입니다. `createSocket` 이 [InterCom](/uwa/intercom) 소켓을 만들어 대시보드 최상위 창의 서버 `SearchComServer` 에 붙입니다.
- **같은 옵션을 `RegisterContext` 와 `InContextSearch` 에 둘 다 보냅니다.** `RegisterContext` 로 저장한 값이 이 화면의 검색 규칙(조건, 뺄 id, 여러 개 선택, 고르기 모드)이 되고, `InContextSearch` 가 3DSearch 를 열고 첫 검색을 합니다. OOTB 대부분이 이렇게 합니다.
- 결과는 옵션의 `app_socket_id` 소켓으로만 옵니다. `app_socket_id` 는 소켓을 만든 `socket_id` 와 같아야 합니다.
- 사용자가 OK 를 누르면 `Selected_Objects_search` 로 고른 객체 배열이 옵니다. 표시 값은 HTML 이스케이프되어 있으므로 원래 값(`이름_value`)을 씁니다 ([받는 데이터](#받는-데이터)).
- OOTB 앱의 `getRefinementToSnN`, `getPrecondForContentSearch` 는 각 앱이 만든 도우미입니다 (`DS/ENORouteMgmt/Utilities/Search/SearchUtil` 등). `DS/SNInfraUX/SearchUtils` 는 3DSearch 내부 도우미(결과 화면 인자, 사용자 id, SecurityContext)라 위젯에서 부를 일이 거의 없습니다.

## SearchCom

| 메서드 | 설명 |
| --- | --- |
| `createSocket({ socket_id })` | 소켓을 만들어 돌려줌. `socket_id` 가 없으면 `SearchCom: socket_id not found`, 이미 만든 id 면 `SearchCom: socket with id ... already created` 예외 |
| `deleteSocket(socket_id)` | 만든 목록에서 지워 같은 id 로 다시 만들 수 있게 함. 연결은 끊지 않으므로 소켓의 `disconnect()` 도 부름 |

돌려받은 소켓은 InterCom Socket 그대로입니다 (`dispatchEvent`, `addListener`, `removeListener`, `disconnect`). `createSocket` 이 받는 다른 키는 위젯에서 쓰지 않습니다.

| 키 | 쓰는 곳 |
| --- | --- |
| `widgetId` | 3DDashboard 에서 미리보기가 아닌 위젯 id 를 주면 `SearchComServer_<widgetId>` 서버에 붙음. 3DSearch 위젯 자신이 씀. OOTB 앱 위젯은 넘기지 않음 |
| `clientInfo` | 실행 환경 이름. 3DSpace 화면 상단 바(ENOFrameworkSearch)가 `'3DSpace'` 를 넘김 |

::: tip SearchCom 없이 붙기
E6WCommonUI, PADUtils, LifecycleWidget 같은 앱은 `new InterCom.Socket(id, { dispatchRetryInterval: 5 })` 를 만들어 `subscribeServer('SearchComServer')` 합니다. 하는 일은 같습니다. 이 방법은 같은 id 로 다시 만들면 예전 소켓을 끊고 바꿔 끼웁니다 (예외 없음).
:::

### socket_id 와 widget_id

| 앱 | `socket_id`, `app_socket_id` | `widget_id` |
| --- | --- | --- |
| ENORouteMgmt, ENOUserGroupMgmt, ENXMeetingMgmt 등 ENOVIA 앱 | 열 때마다 `UWA.Utils.getUUID()` | 소켓 id 와 같음 |
| PADUtils | `'enopad_' + widget.id` (고정) | `widget.id` |
| E6WCommonUI, ENOXSearchUIController, LifecycleWidget | `'search_socket_' + UUID 또는 시각` | `widget.id` |

- `widget_id` 는 등록 이름입니다. 같은 값으로 다시 `RegisterContext` 하면 바꿔 씁니다. `UnregisterContext` 도 이 값으로 지웁니다.
- ENOVIA 앱처럼 열 때마다 새 소켓을 만들면 소켓과 등록이 계속 쌓이고(해제하는 코드가 없음), 예전 소켓도 모든 소켓에 보내는 이벤트(`onCloseTransient`)를 받습니다. 위젯마다 소켓 하나를 만들어 다시 쓰는 편이 깔끔합니다 (샘플 방식).
- `widget_id` 에 실제 `widget.id` 를 넣고 해제하지 않으면, 그 위젯을 최대화했을 때 상단 검색창이 이 등록 정보로 "앱 안에서 찾기" 를 하는 것으로 보입니다 <Badge type="danger" text="검증 필요" /> (`SearchUX_TopBar.computeSearchBar` 가 최대화된 위젯 id 로 등록 정보를 찾음). 그런 동작이 필요 없으면 소켓 id 를 씁니다.

## 이벤트

위젯이 보내는 것:

| 이벤트 | 데이터 | 하는 일 |
| --- | --- | --- |
| `RegisterContext` | 옵션 | `widget_id` 이름으로 옵션을 저장. 저장한 옵션을 `RegisterContext_Response` 로 돌려줌 |
| `InContextSearch` | 옵션 | 3DSearch 결과 화면(앱 `X3DSEAR_AP`)을 열고 저장된 옵션으로 첫 검색 |
| `UnregisterContext` | `{ widget_id }` 또는 id 문자열 | 저장한 옵션을 지움 |

위젯이 받는 것:

| 이벤트 | 받는 소켓 | 데이터 | 언제 |
| --- | --- | --- | --- |
| `RegisterContext_Response` | `app_socket_id` | 저장된 옵션 | 등록 직후. E6WCommonUI 는 이것을 받은 뒤 `InContextSearch` 를 보냄 |
| `Selected_Objects_search` | `app_socket_id` | 고른 객체 배열 | OK, Apply, 하나만 고르고 더블클릭. **하나도 고르지 않으면 오지 않음** |
| `InContextSearch_Response` | `app_socket_id` | `{ selected_command, selected_objects, selected_data, search_params }` | OK, Apply, 취소 |
| `Selected_global_action` | `app_socket_id` | `{ id, command_data, selected_objects }` | `global_actions` 단추를 누름 |
| `closeTransient` | 모든 소켓 | `{ command_id }` | OK, 취소로 3DSearch 창을 닫을 때 |
| `onCloseTransient` | 모든 소켓 | `{ in_apps_search: 저장된 옵션 }` | 3DSearch 결과 화면이 닫힘 |

OK 를 누르면 `Selected_Objects_search` → `closeTransient` → `InContextSearch_Response` 순서로 오고, 창이 닫히면 `onCloseTransient` 가 옵니다.

::: tip unregisterSearchContext 는 받는 곳이 없습니다
OOTB 16 개 파일(ENOXSearchUIController, LifecycleWidget, ENOChgServices ...)이 정리할 때 `unregisterSearchContext` 를 보냅니다. 3DSearch 쪽에서 받는 이름은 `UnregisterContext` 이고, 3DSpace webapps 의 SNSearchUX, SNResultUX, SNInfraUX 에는 `unregisterSearchContext` 를 받는 곳이 없습니다.
:::

## 옵션

`RegisterContext` 와 `InContextSearch` 에 같은 객체를 보냅니다. 앱 수는 OOTB 에서 그 키를 넣는 앱 수입니다.

| 키 | 앱 수 | OOTB 값 | 설명 |
| --- | --- | --- | --- |
| `app_socket_id` | 43 | 소켓 id | **필수.** 결과를 받을 소켓 |
| `widget_id` | 40 | 소켓 id, `widget.id` | **필수.** 등록 이름 |
| `mode` | 39 | `'furtive'` | 고르기 모드. 결과 아래에 취소 / OK 단추가 생김. 다른 값이면 단추가 없음 |
| `title` | 34 | 앱 글 | 3DSearch 창 제목에 붙는 글 |
| `multiSel` | 27 | `true`, `false` | `true` 여러 개, `false` 한 개. 없으면 여러 개 |
| `precond` | 26 | [precond](#precond) | 모든 검색에 `(검색어) AND (precond)` 로 붙는 조건. 타입 제한에 씀 |
| `default_with_precond` | 38 | `true` | `true` 면 열자마자 `default_search_criteria` 와 `precond` 로 검색. 아니면 검색어 없이 검색창에 포커스만 주는 것으로 보입니다 <Badge type="danger" text="검증 필요" /> |
| `default_search_criteria` | 28 | `''` | 처음 검색어 |
| `show_precond` | 28 | `false` | `false` 면 검색창에 precond 를 보이지 않음 |
| `source` | 19 | `['3dspace']` | 찾을 곳. 문자열 하나도 됨. 사용자 그룹까지는 `['3dspace', 'usersgroup']` |
| `tenant` | 27 | 플랫폼 id | `widget.getValue('x3dPlatformId')` |
| `resourceid_not_in` | 12 | 이미 넣은 객체 id | 결과에서 뺄 id (physicalid). ENOUserGroupMgmt 는 이미 있는 구성원을 뺌 |
| `resourceid_in` | 11 | id 배열 | 이 id 안에서만 찾음. ENORouteMgmt 는 범위 구성원만 |
| `idcard_activated` | 31 | `false` | `false` 면 결과를 눌러도 ID 카드를 열지 않음 |
| `recent_search` | 11 | `{ types: [...] }` | "최근 내용" 메뉴를 이 타입으로 좁힘 |
| `columns` | 7 | 술어 배열 | 결과에 더 받을 속성. 검색 요청의 `select_predicate` 에 더해짐 |
| `global_actions` | 9 | `[{ id: 'incontextHelp', title, icon: 'fonticon fonticon-help', overflow: false }]` | 결과 화면 위 단추. 누르면 `Selected_global_action` |
| `advanced_search` | 9 | ENOUserGroupMgmt `getMemberAdvancedSearch` | 고급 검색 패널 (`showByDefault`, `searchOnInit`, `ds6w:type`, `selection`, `common_predicates` ...) |
| `context_title` | 8 | - | 검색 문맥 이름. PADUtils 는 `context_name` 값을 넣음. 화면에 어떻게 보이는지는 확인하지 않음 |
| `search_delegation` | 10 | `'3dsearch'` | 위젯을 최대화했을 때 상단 검색창 동작. `'3ddashboard'` 면 "이 탭에서 찾기" 쪽으로 가는 것으로 보입니다 <Badge type="danger" text="검증 필요" /> |
| `itemViewClickHandler` | 9 | `''` | 결과 항목을 누를 때 부를 모듈 경로. 그 모듈의 `executeAction({ model })` 을 부름 |
| `furtive_options` | - | `{ min_select: 0 }` | OK 단추가 켜지는 최소 선택 수. 기본 1. 3DSearch 자신만 씀 |
| `showApplyButton`, `applyButton_Nls` | - | - | Apply 단추와 그 툴팁. 누르면 `selected_command` 가 `'in_app_apply'` |
| `nls` | - | - | `{ button_ok_label, button_apply_label }` 단추 글자 |
| `closeTransient` | - | - | `false` 면 OK 뒤 창을 닫지 않는 것으로 보입니다 <Badge type="danger" text="검증 필요" /> |
| `UXOptions` | 2 | - | 결과 화면 기능 끄기: `dnD`, `dblClk`, `localActions`, `menuMyContent`, `menuMyFavorites`, `searchRecentContent`, `globalToolbarActions: { saveAsFavorite, export, print }` |
| `show_banned_types` | - | - | 없으면 관리자가 상단 검색에서 숨긴 타입(`topbar_search_banned_list`)도 보임. `false` 면 숨김 |

넣어도 쓰이지 않는 키가 있습니다. `role` (27 앱, 대부분 `''`), `select_result_max_idcard` (14 앱, `false`), 맨 위의 `types` 는 2025 3DSearch 코드(SNSearchUX, SNResultUX, SNInfraUX)에서 읽는 곳을 찾지 못했습니다. `types` 는 E6WCommonUI 의 자기 옵션으로, 거기서 precond 로 바꿔 넣습니다.

::: warning 등록 없이 InContextSearch 만 보내면
`InContextSearch` 는 3DSearch 를 연 뒤 `widget_id` 로 저장된 옵션을 찾아 고르기 화면을 꾸밉니다. 등록이 없으면 고르기 모드(OK 단추)가 아닌 보통 검색으로 열리고, 대시보드에서 아직 아무도 등록하지 않았으면 최상위 창에서 오류가 납니다 (저장소 `_registered_search_ctx` 가 아직 없음). 검색 조건도 저장된 값이 쓰여서, LifecycleWidget 처럼 등록은 빈 값으로 하고 `InContextSearch` 에만 조건을 주면 첫 검색에만 조건이 걸리는 것으로 보입니다 <Badge type="danger" text="검증 필요" />.
:::

## precond

3DSearch 검색식입니다. [federated search](#화면-없이-검색-federated-search) 의 `query` 와 문법이 같습니다.

| 하고 싶은 일 | precond | OOTB |
| --- | --- | --- |
| 타입 하나 | `flattenedtaxonomies:"types/VPMReference"` | LifecycleWidget, ENOUserGroupMgmt |
| 타입 여러 개 | `flattenedtaxonomies:("types/Document" OR "types/Drawing")` | E6WCommonUI |
| `type` 필드로 | `type:"VPMReference"` | ENOXSearchUIController |
| 3dspace 밖 소스의 타입 | `[ds6w:type]:("pno:Person")` | E6WCommonUI (swym) |
| 타입 빼기 | `(...) AND NOT flattenedtaxonomies:("types/Person")` | E6WCommonUI `excluded_types` |
| 상태 | `current:"Active"`, `[ds6w:status]:(Public)` | ENOUserGroupMgmt, ENORouteMgmt |
| 최신 개정만 | `latestrevision:"TRUE"` | ENORouteMgmt |
| 제목, 이름에 글자 포함 | `[ds6w:label]:(*abc*) OR [ds6w:identifier]:(*abc*)` | ENOUserGroupMgmt |
| 속성 값 | `route_95_template_95_valid:"TRUE"` | ENORouteMgmt |
| 특정 id | `uri:"<id>"` | ENORouteMgmt (사용자 그룹) |

- E6WCommonUI 는 `3dspace`, `drive` 소스에는 `flattenedtaxonomies:"types/타입"`, 다른 소스에는 `[ds6w:type]:"타입"` 을 씁니다. `flattenedtaxonomies` 는 타입 계층을 펼친 값이라 하위 타입도 걸리는 것으로 보입니다 <Badge type="danger" text="검증 필요" />.
- 속성 필드 이름은 소문자이고, 영문자와 숫자가 아닌 글자를 `_글자코드_` 로 바꾼 모양입니다 (`_` → `_95_`, `.` → `_46_`. 예: `plm_95_externalid`, `v_95_partnumber`). OOTB 에서 쓰는 이름을 보고 추정한 규칙입니다 <Badge type="danger" text="검증 필요" />.
- 사용자 입력을 그대로 넣으면 식이 깨질 수 있습니다. SearchWidget 은 입력에서 `"#$@%*,?\<>[]|:` 를 받지 않습니다 (`ForbiddenChar`).

## 받는 데이터

### Selected_Objects_search

고른 결과 배열입니다. 한 항목은 3DSearch 결과 한 줄이고, 키는 이 규칙으로 만들어집니다 (`FederatedSearch_parse`). 소켓을 지나며 JSON 으로 바뀌므로 모델이 아니라 보통 객체입니다.

| 키 | 값 |
| --- | --- |
| `id` | 객체 id. 결과의 `resourceid` 값 (3DSpace 객체는 physicalid) |
| 술어 이름 (`ds6w:label` 등) | 화면 표시 값. 문자열은 **HTML 이스케이프**, 날짜는 표시 형식, 타입은 표시 이름 |
| 술어 이름 + `_value` (`ds6w:label_value` 등) | 원래 값 |
| `/` 가 들어간 이름 (`ds6w:what/ds6w:type`) | 마지막 부분(`ds6w:type`)으로도 한 번 더 들어감 |
| `ds6w` 형식 술어 | 접두어를 뗀 이름(`label`, `type`)으로도 들어감 |

같은 술어에 값이 여러 개면 배열입니다.

| 키 | 내용 | OOTB 에서 읽는 앱 |
| --- | --- | --- |
| `id` | physicalid | 대부분 |
| `ds6w:label`, `ds6w:label_value` | 제목 | 11 |
| `ds6w:identifier`, `ds6w:identifier_value` | 이름(name). 사람이면 로그인 이름 | 9 |
| `ds6w:type`, `ds6w:type_value` | 타입 표시 이름, 내부 이름 (`VPMReference`) | 7, 5 |
| `ds6w:status`, `ds6w:status_value` | 상태 | 2 |
| `ds6w:modified`, `ds6w:created` | 날짜 (표시 형식). 원래 값은 `_value` | 2 ~ 3 |
| `ds6wg:revision` | 개정 | 1 |
| `ds6w:responsible`, `ds6w:project`, `ds6w:description` | 담당자, 프로젝트, 설명 | 0 ~ 2 |
| `resourceid` | `id` 와 같은 값 | 1 |

3DSearch 화면이 기본으로 받는 술어는 `SNInfraUX/assets/SearchView.json` 에 있습니다 (`ds6w:label`, `ds6w:type`, `ds6w:identifier`, `ds6wg:revision`, `ds6w:status`, `ds6w:responsible`, `ds6w:modified`, `ds6w:project`, `ds6w:description`, `ds6w:dataSource`). 더 필요한 속성은 옵션 `columns` 로 더합니다.

::: warning 표시 값은 HTML 이스케이프되어 있습니다
`ds6w:label` 같은 문자열 표시 값은 `&`, `<`, `>`, `"`, `'` 가 `&amp;` 처럼 바뀌어 옵니다 (`SearchUtils.escapeHtml`). 원래 글자가 필요하면 `ds6w:label_value` 처럼 `_value` 키를 씁니다. OOTB 의 ENXMeetingMgmt, ENXESignConfigApp 은 `ds6w:label` 에 `unescapeHTML()` 을 불러 되돌립니다.
:::

### InContextSearch_Response

| 키 | 내용 |
| --- | --- |
| `selected_command` | `'in_app_ok'`, `'in_app_apply'`, `'in_app_cancel'` |
| `selected_objects` | 고른 객체 배열 (위와 같은 모양). 취소에도 들어 있음 |
| `selected_data` | 고른 객체의 [3DXContent](/events/drag-and-drop#_3dxcontent-형식) (`version: '2.1'`, `source: 'X3DSEAR_AP'`, 항목의 `contextId` 는 `''`). 취소에는 없음 |
| `search_params` | 마지막 검색 요청 값 |

PADUtils `inAppsSearch` 가 이 이벤트의 `selected_command` 로 OK, Apply, 취소를 나눕니다.

## 사람 고르기

사람도 같은 방법입니다. 조건과 받은 값을 읽는 키만 다릅니다.

| | 객체 | 사람 |
| --- | --- | --- |
| `precond` | `flattenedtaxonomies:"types/VPMReference"` | `flattenedtaxonomies:"types/Person" AND current:"active"` |
| 그룹도 고르기 | - | 온프레미스 `flattenedtaxonomies:"types/Group"`, 클라우드 `[ds6w:type]:(Group)` 과 `source: ['3dspace', 'usersgroup']` |
| `recent_search` | `{ types: ['VPMReference'] }` | `{ types: ['Person'] }` |
| 받은 값 | `id` (physicalid) | `ds6w:identifier_value` 가 로그인 이름, `ds6w:label_value` 가 표시 이름, `id` 는 Person 객체의 physicalid |

- 조직, 역할로 좁힐 때는 `AND member:"<조직>"` (ENORouteMgmt), `AND userrole:"VPLMAdmin"` (ENXESignConfigApp) 를 더합니다.
- ENXESignConfigApp 은 온프레미스에서 `ds6w:identifier` 를, 클라우드에서 `id` 를 사람 이름으로 씁니다.
- 3DSwym 소스(`swym`)의 사람은 `[ds6w:type]:("pno:Person")` 로 찾고 id 가 `iam:` 으로 시작합니다 (SearchWidget 이 앞 4 글자를 뗌).
- 화면 없이 사람을 찾을 때는 [사람 찾기 (e6w)](/rest-api/e6w#사람-찾기-personsearch) 나 아래 [federated search](#화면-없이-검색-federated-search) 를 씁니다.

## 닫기와 해제

| 상황 | 할 일 |
| --- | --- |
| OK, 취소 | 3DSearch 창은 스스로 닫힘 (상단 바가 `closeTransient` 를 받아 3DSearch 앱 창을 닫음) |
| 창이 닫힘 (`onCloseTransient`) | 모든 소켓에 오므로 `in_apps_search.widget_id` 가 내 값인지 보고 `UnregisterContext` |
| 위젯이 소켓을 버림 | `UnregisterContext` → `SearchCom.deleteSocket(socket_id)` → `socket.disconnect()` |

- 3DSearch 의 "저장된 검색 수정" 이 `onCloseTransient` 에서 이렇게 정리합니다.
- PADUtils `inAppsSearch` 는 `InContextSearch_Response` 를 받으면 바로 `UnregisterContext` 를 보냅니다.
- ENORouteMgmt 같은 ENOVIA 앱은 해제하지 않습니다.

## 동작 방식

```text
내 위젯 (iframe)                                3DDashboard 최상위 창
createSocket({ socket_id }) ─── 붙음 ───────▶   InterCom 서버 SearchComServer
                                                 └ 소켓 incontextsearch_socket_id (InContextSearch 모듈)
RegisterContext ────────────────────────────▶   widget_id 로 옵션 저장
InContextSearch ────────────────────────────▶   3DSearch 결과 화면(X3DSEAR_AP)을 열고 저장된 옵션으로 첫 검색
                                                 사용자가 고르고 OK
Selected_Objects_search ◀── app_socket_id 로만
closeTransient, onCloseTransient ◀── 모든 소켓에
```

- 서버는 받은 이벤트를 보낸 소켓 말고 붙어 있는 모든 소켓에 넘깁니다. 받을 소켓을 정해 보낸 것(`app_socket_id`)만 그 소켓에 갑니다 ([InterCom](/uwa/intercom)).
- 위젯 소켓은 `window.parent` 의 서버에 붙습니다. 3DDashboard 위젯 iframe 의 부모가 대시보드 창이라 이렇게 닿습니다.

| 실행 환경 | 결과 |
| --- | --- |
| 3DDashboard 위젯 | 동작 |
| 단독 실행 (위젯 HTML 을 직접 엶) | `window.parent` 가 자기 창이라 자기 창에 만든 서버에 붙음. 받는 쪽이 없어 **아무 일도 일어나지 않음** (오류도 없음) |
| 3DSpace 기본 화면 | 상단 바(TopFrame, ENOFrameworkSearch)도 `SearchComServer` 를 엶. 소켓은 바로 위 창에만 붙으므로 그 창이 상단 바를 가진 창일 때만 닿는 것으로 보입니다 <Badge type="danger" text="검증 필요" /> |
| 대시보드와 origin 이 다른 iframe | InterCom 이 `postMessage` 대상 origin 을 자기 주소로 정해서 전달되지 않는 것으로 보입니다 <Badge type="danger" text="검증 필요" /> |

::: warning 받는 쪽 코드는 3DDashboard 쪽에 있습니다
받는 쪽(InContextSearch, 상단 3DSearch, 결과 화면)은 3DDashboard 최상위 창과 3DSearch 앱이 불러오는 파일입니다. 이 문서는 3DSpace webapps 에 같은 이름으로 들어 있는 SNSearchUX, SNResultUX, TopFrame 으로 읽었습니다. 대시보드 서버가 다른 판을 쓰면 다를 수 있습니다.
:::

상단 검색창의 글자를 위젯이 직접 받는 방법으로는 `widget` 의 `onSearch` 이벤트가 따로 있습니다 ([widget 객체](/widget/widget-api) 의 이벤트 표).

## 예제

### 소켓 만들기

<<< @/../WidgetSample/SearchSample.html#socket{js}

### 옵션 만들기 (타입 제한, 여러 개 선택, 받은 객체 빼기)

<<< @/../WidgetSample/SearchSample.html#options{js}

`widget.getValue('x3dPlatformId')` 는 [PlatformAPI](/widget/platform-api) 의 `getWidgetTenant()` 와 같은 값입니다 ([플랫폼 id](/widget/space-url#플랫폼-id)).

### 열기

<<< @/../WidgetSample/SearchSample.html#open{js}

### 사람 고르기

<<< @/../WidgetSample/SearchSample.html#person{js}

### 받기

<<< @/../WidgetSample/SearchSample.html#receive{js}

### OK, Apply, 취소 나누기

<<< @/../WidgetSample/SearchSample.html#response{js}

### 닫기와 해제

<<< @/../WidgetSample/SearchSample.html#close{js}

## 화면 없이 검색 (federated search)

3DSearch 화면 없이 검색 결과만 필요하면 3DSearch 서비스의 REST 를 직접 부릅니다. 3DSearch 화면도 같은 REST 를 씁니다 (`SearchRessources.getFetchOptions`).

### 주소

1. [Compass 서비스](/widget/compass-services#getserviceurl) 의 `getServiceUrl({ serviceName: '3DSearch', platformId })` 로 3DSearch 주소를 받습니다.
2. 끝에 `/federated` 가 없으면 붙이고 `/search` 를 붙입니다. 3DSearch 화면 코드(`getFedSearchUrl`)가 이렇게 합니다.
3. `WAFData.authenticatedRequest` 로 JSON 을 POST 합니다 ([JSON 보내기](/uwa/request#json-보내기)). 3DSearch 는 3DSpace 와 다른 서버지만 WAFData 가 로그인 세션을 붙입니다.

- SearchApi, RDFFoundation, SearchWidget 은 Compass 주소에 `/search` 만 붙입니다. Compass 의 3DSearch 주소가 이미 `/federated` 로 끝나는 것으로 보입니다 <Badge type="danger" text="검증 필요" />.
- 같은 서비스에 `/login` (GET, 소스 목록), `/recent` (최근 내용), `/search/source`, `/suggest` 도 있습니다 (3DSearch 화면 코드, RDFFoundation).

### 요청 본문

| 키 | OOTB 값 | 설명 |
| --- | --- | --- |
| `query` | `'[ds6w:label]:(*abc*)'` | 검색식 ([precond](#precond) 와 같은 문법). 전체는 `'#all'` (3DSearch 화면은 `*` 를 이것으로 바꿈) |
| `select_predicate` | `['ds6w:label', 'ds6w:identifier']` | 받을 술어 |
| `source` | `['3dspace']` | 소스 |
| `tenant` | 플랫폼 id | SearchWidget 은 주소 `?tenant=` 에도 붙임 |
| `nresults` | 10, 40, 100 | 한 번에 받을 개수 |
| `start`, `next_start` | `'0'`, 앞 응답의 `infos.next_start` | 다음 쪽 |
| `order_field`, `order_by` | `'relevance'`, `'desc'` | 정렬 |
| `login` | `{ '3dspace': { SecurityContext: 'ctx::...' } }` | 3DSpace 소스에 쓸 SecurityContext. 3DSearch 화면은 `3dspace`, `mbseapps` 소스에 넣음 |
| `label` | `'3DSearch-<사용자>-<기능>-<시각>'` | 요청 이름. 매번 다르게 붙임. 기록용으로 보입니다 <Badge type="danger" text="검증 필요" /> |
| `locale` | `widget.lang` | 언어 |
| `select_file` | `['icon', 'thumbnail_2d']` | 아이콘, 미리보기 그림 |
| `specific_source_parameter` | `{ '3dspace': { additional_query: ' AND ...' } }` | 소스별로 더 붙일 조건 |
| `resourceid_in`, `resourceid_not_in` | id 배열 | in-context 옵션과 같음 |
| `with_indexing_date`, `with_synthesis`, `with_nls` | 3DSearch 화면 `true` | 인덱스 날짜, 태그 집계(`facets`), 번역된 이름을 같이 받는 것으로 보입니다 <Badge type="danger" text="검증 필요" /> |

`login` 을 빼면 사용자 기본 SecurityContext 를 쓰는 것으로 보입니다 <Badge type="danger" text="검증 필요" />. 기본값이 없는 사용자는 `infos.sources[]` 에 `FS_AUTHENTICATION_ERROR` ("User has no preferred Security Context") 가 옵니다 (3DSearch 화면이 이 메시지를 따로 안내함). SecurityContext 얻는 법은 [3DSpace 주소와 SecurityContext](/widget/space-url#securitycontext), 형식은 [보안 컨텍스트](/rest-api/security-context#형식) 를 봅니다.

### 응답

```json
{
  "infos": {
    "nresults": 20,
    "nmatches": 134,
    "next_start": "<다음 쪽 값>",
    "sources": [{ "name": "3dspace" }]
  },
  "results": [
    {
      "attributes": [
        { "name": "resourceid", "value": "<physicalid>" },
        { "name": "ds6w:label", "value": "PRT-0001", "type": "string" },
        { "name": "ds6w:type", "value": "VPMReference" }
      ]
    }
  ]
}
```

| 키 | 내용 |
| --- | --- |
| `results[].attributes[]` | `{ name, value, type, format }`. 같은 `name` 이 여러 번 오면 여러 값 |
| `infos.nresults` | 이번에 온 개수 |
| `infos.nmatches` | 전체 개수. `estimated` 가 `true` 면 3DSearch 화면은 `nhits` 를 씀 |
| `infos.next_start` | 다음 쪽을 받을 때 `next_start` 로 보냄 |
| `infos.sources[]` | 소스별 결과. `error: { errorCode, errorMessage }` 가 있으면 그 소스는 실패 |
| `facets` | 태그 집계 |

- 위 JSON 은 모양을 보이려고 줄인 것입니다. `type` 은 `string`, `date` 같은 값 종류, `format` 은 `ds6w`, `ds6w_facet`, `internal`, `snippet` 같은 값이 보입니다 (FederatedSearch_parse 가 구분하는 값).
- REST 응답의 `value` 는 원래 값입니다. HTML 이스케이프와 `_value` 키는 3DSearch 화면이 결과를 다시 만들 때 생깁니다.
- `resourceid` 는 `select_predicate` 에 넣지 않아도 오는 것으로 보입니다 <Badge type="danger" text="검증 필요" /> (3DSearch 화면 기본 술어에 없지만 결과마다 id 로 씀).

::: warning 요청이 성공해도 소스별 오류가 있을 수 있습니다
HTTP 는 성공(`onComplete`)인데 `infos.sources[i].error` 에 오류가 들어 있을 수 있습니다. 결과가 비었으면 이것부터 봅니다.
:::

### 예제: 주소 얻기

<<< @/../WidgetSample/SearchSample.html#fed-url{js}

### 예제: 검색

<<< @/../WidgetSample/SearchSample.html#fed-search{js}

### 예제: 응답 읽기

<<< @/../WidgetSample/SearchSample.html#fed-parse{js}

### SearchApi 로 부르기

`DS/SNInfraUX/Api/SearchApi` 의 `search(options)` 도 같은 요청을 보냅니다. 3DSearch 내부 모듈(SNSmartQuery, SearchControlCenter 등 6 앱)이 씁니다.

```js
require(['DS/SNInfraUX/Api/SearchApi'], function (SearchApi) {
  SearchApi.search({
    tenant: widget.getValue('x3dPlatformId'),
    query: '[ds6w:label]:(*abc*)',
    select_predicate: ['ds6w:label', 'ds6w:identifier'],
    source: ['3dspace'],
    nresults: 20,
    onComplete: function (response) { console.log(response.results); },
    onFailure: function (error) { console.error(error); }
  });
});
```

- `tenant` 를 꼭 넣습니다. 없으면 주소 대신 플랫폼 목록(배열)을 받아 `onFailure` 로 끝납니다.
- 기본값은 `order_field: 'relevance'`, `order_by: 'desc'`, `select_predicate: ['ds6w:label']`, `nresults: 40` 입니다.
- 응답에서 `facets` 를 지우고, `with_info: true` 를 주지 않으면 `infos` 도 지웁니다.
- Compass 주소에 `/search` 만 붙이고, `login`(SecurityContext)은 넣지 못합니다. 역할을 정해 찾아야 하면 WAFData 로 직접 부릅니다.

## SearchWidget 과 다른 점

`DS/SearchWidget/SearchWidget` (7 앱: ShareWidget, TransferOwnershipWidget, AccessRightWidget, SharingReportWidget, MVAInput, ENOXServicesChangeOwnershipWidget, WidgetServicesUI)은 3DSearch 창을 여는 것이 아니라, **입력창에서 사람, 조직, 협업 공간을 찾아 고르는 컨트롤** 입니다. `SearchWidget.getSearchWidget(SearchWidget.FULL 또는 SearchWidget.RESTRICTED, 옵션)` 으로 만들고, `type` 은 `'all'`, `'user'`, `'collabspace'`, `'org'` (`DS/SearchWidget/SearchWidgetConstant`) 입니다. 안에서 3DSpace 서비스나 federated search 를 부릅니다.

## 2026 변경

| 항목 | 내용 |
| --- | --- |
| `SearchCom` | 같음 |
| `InContextSearch` (받는 쪽) | 같음. X2A 모드(`isx2a` 설정)에서 열릴 때 `webinwin:web:notification` 을 하나 더 보냄 |
| 결과 보내기 | `Selected_Objects_search` 를 `SearchComServer` 서버로만 보냄 (`dispatchEvent(이름, 데이터, [app_socket_id], ['SearchComServer'])`). `createSocket` 에 `widgetId` 를 주지 않는 보통 방식은 영향 없음 |
| 결과 키 | 같음 (표시 값 HTML 이스케이프, `_value`) |
| `SearchApi` | `search` 같음. `getUserGroupMemberDetails` 가 usersgroup 서비스를 직접 부름 |
| 쓰는 앱 | SearchCom 15 → 16 (ENOXPackageCommonUXInfra), SearchWidget 7 → 8 (ManageSharing) |
