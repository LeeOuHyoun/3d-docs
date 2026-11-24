# history <Badge type="info" text="원본 분석" />

객체와 관계의 기록 (history) 을 읽고, 트리거에서 이번 트랜잭션의 기록을 풀고, 기록을 남기거나 잠시 끄는 방법입니다. 예제의 `part`, `relId` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다. util 전체를 하고 싶은 일로 찾으려면 [유틸리티](/jpo/api/utilities) 를 봅니다.

::: info 근거
xerinfra.jar (`framework.ui.UINavigatorUtil`, `TransactionTriggerUtil`, `EnoviaResourceBundle`, `i18nNow`, `DomainRelationship`), eMatrixServletRMI.jar (`BusinessObject`, `Relationship`, `History`, `com.matrixone.jdl.bosHistorySt`, `com.matrixone.jdl.xml.bosBusinessObjectImpl`) class 파일 (javap) 기준. 기록 문자열의 모양은 OOTB JPO 원본의 주석과 그 문자열을 자르는 코드로 확인했고, 서버에서 읽어 보지는 않았습니다. 건수는 OOTB JPO 에서 센 호출 수: `UINavigatorUtil.getHistoryData(` 1, `TransactionTriggerUtil.parseHistory(` 4, `getFilteredHistory(` 1, `getHistoryI18NString(` 0, 문자열 `"modify bus $1 add history $2 comment $3"` 14, `"history off"` 와 `"history off;"` 15, `"history on"` 과 `"history on;"` 18
:::

샘플: [XX_UtilHistory_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilHistory_mxJPO.java) ([메서드별 설명](#샘플-jpo))

## 한눈에 보기

| 하고 싶은 일 | 쓸 메서드 |
| --- | --- |
| 객체의 기록을 모두 읽기 | [`UINavigatorUtil.getHistoryData(context, objectId)`](#객체-기록-모두-읽기) |
| 가장 최근 기록 하나 읽기 | [`getHistoryData` 결과 목록의 마지막 값](#가장-최근-기록-읽기) |
| 한 종류 이벤트 (소유자 변경 ...) 기록만 읽기 | [`obj.getInfoList(context, "history.changeowner")`](#한-종류-이벤트-기록만-읽기) |
| 관계의 기록 읽기 | [`new Relationship(relId).getHistory(context)`](#관계-기록-읽기) |
| 트리거에서 이번 트랜잭션의 기록 풀기 | [`TransactionTriggerUtil.parseHistory(context, args[0])`](#트랜잭션-기록-풀기) |
| 사용자 기록 남기기 | [`MqlUtil.mqlCommand(context, "modify bus $1 add history $2 comment $3", ...)`](#기록-남기기) |
| 기록을 남기지 않고 고치기 | [`history off` … `finally` 에서 `history on`](#기록-잠시-끄기) |
| 이벤트 이름을 사용자 언어로 바꾸기 | [`EnoviaResourceBundle.getHistoryI18NString(context, event, lang)`](#이벤트-이름-번역) |

## 기록 한 줄의 모양

기록 하나는 이벤트, 사용자, 시각, 상태, 설명이 한 줄로 이어진 문자열입니다. OOTB 원본 주석에 남은 값입니다.

```text
modify - user: Test Everything  time: 7/1/2008 4:36:51 PM  state: Preliminary  Design Purchase: Design  was:
change project - user: VPLMAdminUser  time: 6/18/2015 3:41:16 PM  state: Create  project:   was: Default
```

| 부분 | 값 (위 첫 줄) |
| --- | --- |
| 이벤트 | `modify` (` - user:` 앞) |
| 사용자 | `Test Everything` |
| 시각 | `7/1/2008 4:36:51 PM` |
| 상태 | `Preliminary` |
| 설명 | `Design Purchase: Design  was:` (상태 뒤 나머지) |

- 칸 사이는 공백 두 개입니다. OOTB 코드 (`TransactionTriggerUtil`, `UINavigatorUtil`) 도 `- user:`, `time:`, `state:`, 공백 두 개로 자릅니다.
- 위 두 줄은 원본 주석의 값이라 서버마다, 이벤트마다 같은지는 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.

::: info 근거
emxTransactionNotificationUtilBase_mxJPO.java `parseEvents` 의 주석 (`modify - user: Test Everything  time: …`), emxProgramMigrationRestorePOV_mxJPO.java `migrateObjects` 의 주석 (`history.changeproject = change project - user: …  was: Default`). xerinfra.jar `TransactionTriggerUtil.TRANS_HISTORY_EVENT_DELIMITER = "- user:"`, `UINavigatorUtil.getTime` (`time:` 뒤부터 `state:` 앞까지), `getHistoryData(Context, String, String)` 은 공백 두 개 (`"  "`) 로 시각과 설명을 나눔 (상수 값은 class 파일의 상수 풀에서 확인)
:::

## 객체 기록 모두 읽기

객체 하나의 기록을 이벤트, 시각, 사용자, 상태, 설명 목록으로 받습니다. OOTB 이력 화면 (`emxHistorySummary.jsp`) 이 쓰는 메서드입니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `UINavigatorUtil.getHistoryData(context, objectId)` | `objectId`: 객체 id | `HashMap`. 아래 키. 기록이 많으면 최근 `emxFramework.History.maxRecords` 건만 |

| 키 | 값 |
| --- | --- |
| `action` | `StringList`. 기록마다 이벤트 (`history.event`) |
| `time`, `user`, `state`, `description` | `StringList`. 기록마다 하나 (`history.time` ...) |
| `modifiedName` | `String`. 이벤트에 `change name` 이 있으면 지금 이름, 없으면 `""` |
| `MaxRecords` | `String`. 잘랐으면 최대 건수 (`"2000"`), 아니면 `"NoMessage"` |

```java
Map history = UINavigatorUtil.getHistoryData(context, objectId);
StringList actions = (StringList) history.get("action");
StringList times = (StringList) history.get("time");
StringList users = (StringList) history.get("user");
```

- 다섯 목록은 같은 순번이 같은 기록입니다.
- `time`, `user`, `state` 값은 앞에 `time: `, `user: `, `state: ` 가 붙어 오는 것으로 보입니다. OOTB 화면과 `ENOPeriodicReviewUIBase_mxJPO.java` 가 이 앞부분을 잘라 내고 씁니다 <Badge type="danger" text="검증 필요" />. 샘플의 `withoutLabel` 처럼 붙어 있을 때만 잘라 냅니다.
- 최대 건수는 설정 `emxFramework.History.maxRecords` (분석한 값 `2000`, 0 이하면 `2000`) 입니다. 기록 수 (`history.count`) 가 이보다 많으면 `history[기록수 - 최대건수]` 의 시각을 읽어 그 시각 이후의 기록만 읽습니다 (`history.time.between[시각|]` ...).
- 설정 값이 숫자가 아니거나, 객체가 없거나, 읽다가 실패하면 원래 예외 문구를 담은 `MatrixException` 입니다.
- 서버에 가는 일: `BusinessObject.select` 를 두 번 (기록이 많으면 세 번).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `UINavigatorUtil.getHistoryData(context, objectId, select)` | 최대 건수 처리 없이 다섯 목록을 다 읽고, `print bus $1 select <select> dump` 결과를 잘라 뒤에 덧붙임. 덧붙인 기록의 `state` 칸에는 `state:` 글자만 들어감. OOTB 화면이 관계 기록을 함께 볼 때만 씀 |
| `UINavigatorUtil.getHistoryData(context, type, name, revision)` | `print bus $1 $2 $3 select history dump` 결과를 `Vector` 로 돌려줌 (키 `history`, `user`, `time`, `state`). OOTB 미사용 |
| `obj.getInfoList(context, "history.time")` 등 select 다섯 개 | `getHistoryData` 가 안에서 하는 일. 최대 건수 처리가 없음 |
| `bo.getHistory(context)` (`BusinessObject`) | 서버가 준 기록마다 `description` 칸만 모은 `StringList`. 그 칸에 무엇이 오는지 확인 못 함. OOTB 미사용 |
| `bo.getFilteredHistory(context, events, excludeEvents, user, fromDate, toDate, last, state, descending, limit)` | `History` 객체 목록 (`getTime()` 은 `long`, `getUser()`, `getEvent()`, `getState()`, `getInfo()`). 설명 칸이 없음. 인자 이름은 서버로 보내는 XML 이름으로만 확인했고 동작은 확인 못 함 <Badge type="danger" text="검증 필요" />. `limit` 은 0 이하나 5000 초과면 5000 으로 바꿔 보냄. OOTB 1 건 (`emxPlayButtonSummaryBase_mxJPO.java`: `null, false, null, 0, 0, false, null, true, 0`) |

::: info 근거
xerinfra.jar `UINavigatorUtil.getHistoryData(Context, String)`: `EnoviaResourceBundle.getProperty(context, "emxFramework.History.maxRecords")` 를 `Long.parseLong`, 0 이하면 2000. `history.count` 를 select, 많으면 `history[count - max]` (BootstrapMethods `history[\u0001]`) 를 읽어 `getTime` 으로 시각을 꺼내 `eMatrixDateFormat.getEMatrixDateFormat()` 형식으로 바꾼 뒤 `[\u0001|]` 를 붙인 `history.time.between…` 등 다섯 select, 아니면 `history.event`, `history.time`, `history.user`, `history.state`, `history.description`, `name`. 이벤트 목록이 `change name` 을 담으면 `modifiedName` 에 `name`. 예외는 `new MatrixException(e.toString())`. 3 개 인자 형태는 다섯 select 뒤 `print bus $1 select $2 dump |MXDELIM|` 결과를 `~`, `^`, `@` 중 결과에 없는 글자로 바꿔 자르고 `user:`, `time:`, `"  "`, `" -"` 로 나눠 덧붙이며 `state` 에는 `"state:"` 를 넣음. 4 개 인자 형태는 `print bus $1 $2 $3 select $4 dump $5`. eMatrixServletRMI.jar `BusinessObject.getHistory` 는 `bosBusinessObject.history` 결과의 `bosHistorySt.description` 을 모음. `getFilteredHistory` 는 `events` 가 `null` 이면 빈 목록, `limit` 이 `<= 0` 이나 `> 5000` 이면 5000, `fromDate` / `toDate` 는 `Long.toString` 으로 보냄. 인자 이름은 `com.matrixone.jdl.xml.bosBusinessObjectImpl.filteredHistory` 가 쓰는 XML 요소 이름 (`events`, `excludeEvents`, `user`, `fromDate`, `toDate`, `last`, `state`, `descending`, `limit`). `History(bosHistorySt)` 는 `user`, `grantor`, `state`, `event`, `time` (`Long.valueOf`), `xml`, `info` 만 옮김. common/emxHistorySummary.jsp, ENOPeriodicReviewUIBase_mxJPO.java (`user: ` 로 split). properties/emxSystem.properties:3440 (`emxFramework.History.maxRecords=2000`)
:::

## 가장 최근 기록 읽기

가장 최근 기록 1 건만 돌려주는 OOTB util 메서드는 없습니다. [getHistoryData](#객체-기록-모두-읽기) 의 각 목록에서 마지막 값을 씁니다.

```java
Map history = UINavigatorUtil.getHistoryData(context, objectId);
StringList actions = (StringList) history.get("action");
StringList times = (StringList) history.get("time");
String lastAction = actions == null || actions.isEmpty() ? "" : (String) actions.get(actions.size() - 1);
String lastTime = times == null || times.isEmpty() ? "" : (String) times.get(times.size() - 1);
```

- 목록은 오래된 기록부터 최근 기록 순서로 오는 것으로 보입니다. `getHistoryData` 가 기록이 많을 때 뒤쪽 순번을 남기고, OOTB JPO (`emxCPNNotificationUtilBase_mxJPO.java`) 도 마지막 줄을 최근 값으로 씁니다. 서버에서 확인하지는 않았습니다 <Badge type="danger" text="검증 필요" />.
- 기록이 아주 많은 객체에서도 최근 2000 건 (설정 값) 은 읽습니다. 한 종류 이벤트의 최근 값만 필요하면 [한 종류 이벤트 기록만 읽기](#한-종류-이벤트-기록만-읽기) 가 가볍습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `obj.getInfo(context, "history.count")` + `obj.getInfo(context, "history[" + n + "]")` | 순번이 0 부터인지 1 부터인지, 곧 `history[history.count]` 와 `history[history.count - 1]` 중 어느 쪽이 마지막인지 확인 못 함 <Badge type="danger" text="검증 필요" />. OOTB 는 `getHistoryData` 안에서만 씀 |
| `bo.getFilteredHistory(context, null, false, null, 0, 0, false, null, true, 1)` | `descending`, `limit` 이름대로라면 최근 1 건이지만 동작을 확인 못 함 <Badge type="danger" text="검증 필요" /> |
| `history.time.between[시각\|]` 같은 범위 select | 그 시각 이후 기록의 각 값 (여러 값). `\|` 뒤에 끝 시각을 넣을 수 있는지 확인 못 함 <Badge type="danger" text="검증 필요" /> |

::: info 근거
xerinfra.jar `UINavigatorUtil.getHistoryData(Context, String)` (기록이 많으면 `history[count - max]` 의 시각 이후만 읽음). emxCPNNotificationUtilBase_mxJPO.java (`history.changeowner` 결과를 줄로 나눠 마지막 줄 사용)
:::

## 한 종류 이벤트 기록만 읽기

소유자 변경, 연결처럼 한 종류 이벤트의 기록만 읽습니다. 이벤트 select 는 값이 여러 개라 `getInfoList` 로 받습니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getInfoList(context, "history.changeowner")` | `history.` + 이벤트 select 이름 | `StringList`. 그 이벤트의 기록 한 줄씩 ([기록 한 줄의 모양](#기록-한-줄의-모양)) |

| OOTB 가 쓰는 이벤트 select | 쓰는 곳 |
| --- | --- |
| `history.changeowner` | emxCPNNotificationUtilBase_mxJPO.java (직전 소유자) |
| `history.connect` | emxProgramObjectMigrationBase_mxJPO.java |
| `history.checkin` | emxCommonDocumentMigrationBase_mxJPO.java |
| `history.changeproject`, `history.changeorganization` | emxProgramMigrationRestorePOV_mxJPO.java (직전 협업공간, 조직) |
| `history.custom` | enoECMChangeOrderBase_mxJPO.java |

```java
StringList ownerChanges = part.getInfoList(context, "history.changeowner");
String last = ownerChanges.isEmpty() ? "" : (String) ownerChanges.get(ownerChanges.size() - 1);
int index = last.lastIndexOf("was:");
String previousOwner = index < 0 ? "" : last.substring(index + "was:".length()).trim();
```

- `getInfo(context, "history.changeowner")` 는 첫 값만 돌려줍니다 ([getInfoList](/jpo/api/domain-object#getinfolist)).
- 직전 값은 줄 끝의 `was:` 뒤에 있습니다. OOTB 이력 화면용 메서드도 소유자 변경 설명을 `owner: 새값 was: 이전값` 으로 보고 자릅니다. 이벤트마다 줄의 정확한 모양은 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.
- 위 표에 없는 이벤트의 select 이름 (`history.` 뒤) 은 확인하지 못했습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", objectId, "history.changeowner")` | 여러 줄을 한 문자열로 받아 직접 잘라야 함 |
| `ChangeUtil.getHistory(context, objectId, select)` (enterprisechangemgt.jar) | 앱 전용 (ECM). 안에서 `print bus $1 select $2 dump` |

::: info 근거
xerinfra.jar `UINavigatorUtil.getHistoryDescriptionDisplayString(Context, String, String, String)`: 설명에 `owner:` 가 있으면 `owner:` 뒤부터 `was:` 앞까지를 새 값, `was:` 뒤를 이전 값으로 나눔. OOTB 의 `getInfoList(context, "history.connect")` (emxProgramObjectMigrationBase_mxJPO.java), `lastIndexOf("was:")` (emxCPNNotificationUtilBase_mxJPO.java, emxProgramMigrationRestorePOV_mxJPO.java)
:::

## 관계 기록 읽기

관계 하나 (연결) 의 기록을 읽습니다. OOTB JPO 에서 관계 기록을 읽는 곳은 찾지 못했습니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `new Relationship(relId).getHistory(context)` | `relId`: 관계 id | `StringList`. 서버가 준 기록마다 `description` 칸 |

```java
StringList relHistory = new Relationship(relId).getHistory(context);
```

- 원소에 기록 한 줄 전체가 오는지 설명 부분만 오는지 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `rel.getFilteredHistory(context, events, excludeEvents, user, fromDate, toDate, last, descending, limit)` | 객체의 `getFilteredHistory` 에서 `state` 만 뺀 형태. 동작 확인 못 함 |
| `DomainRelationship.SELECT_HISTORY` (`"history[connection]"`) | OOTB JPO 와 xerinfra.jar 안에서 쓰는 곳이 없음. 어디에 쓰는 select 인지 확인 못 함 |

::: info 근거
eMatrixServletRMI.jar `Relationship.getHistory` 는 `bosRelationship.history(session, id)` 결과의 `bosHistorySt.description` 을 모음. `Relationship.getFilteredHistory(Context, StringList, boolean, String, long, long, boolean, boolean, int)` 는 `limit` 을 5000 으로 맞춰 `bosRelationship.filteredHistory` 호출. xerinfra.jar `DomainRelationship.SELECT_HISTORY = "history[connection]"`
:::

## 트랜잭션 기록 풀기

트랜잭션 트리거가 받는 `${TRANSHISTORY}` ([트리거 매크로](/trigger/jpo-args)) 를 객체별 이벤트 목록으로 풉니다. 저장된 기록이 아니라 이번 트랜잭션에서 일어난 일입니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `TransactionTriggerUtil.parseHistory(context, transHistory)` | `transHistory`: `${TRANSHISTORY}` 값 (트리거 인자 `args[0]`) | `HashMap`. 키 `객체id_종류` (`1234.5678.9012.3456_BusinessObject`), 값은 `이벤트_시각` 을 `\|` 로 이은 문자열 |

| 넘긴 값 | 결과 |
| --- | --- |
| `null`, `""` | 빈 `HashMap` |
| `id=` 가 없는 문자열 | 키 `null`, 값 `""` 하나 |
| `id=` 마다 | `type=` 과 `triggerName=` 사이를 종류로, `history=` 뒤를 이벤트로 풂. 이벤트마다 `modify_7/1/2008 4:36:51 PM` 처럼 이벤트 + `_` + 시각 (시각이 없으면 이벤트만) |

```java
HashMap parsed = TransactionTriggerUtil.parseHistory(context, args[0]);
for (Object key : parsed.keySet()) {
    String[] idAndKind = String.valueOf(key).split("_");
    StringList events = FrameworkUtil.split((String) parsed.get(key), "|");
}
```

- 첫 이벤트의 사용자 이름을 전역 환경 변수 `USER` 에 넣습니다 (`PropertyUtil.setGlobalRPEValue`). 다른 코드가 같은 이름을 쓰면 덮어씁니다.
- 시각은 환경 변수 `MX_NORMAL_DATETIME_FORMAT` (없으면 `moy/dom/yr4 h12:min:sec mer`) 의 `:` 개수와 `AM` / `PM` 으로 잘라 냅니다.
- 실패하면 스택을 찍고 같은 예외를 다시 던집니다. 서버에는 환경 변수를 읽고 쓰러 갑니다.
- `${TRANSHISTORY}` 의 정확한 모양은 이 파서와 원본 주석으로만 확인했습니다 <Badge type="danger" text="검증 필요" />.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `TransactionTriggerUtil.parseEvents(context, events)` | `parseHistory` 가 객체마다 부르는 안쪽 메서드. 객체 하나의 `history=` 뒤 부분만 받음 |
| `emxTransactionNotificationUtil_mxJPO.parseHistory`, `parseEvents` | 같은 코드를 JPO 에 따로 둔 것. `emxClassificationTransactionNotificationBase`, `emxLibraryCentralSubscriptionBase` 에도 `parseEvents` 사본이 있음 |
| `MqlUtil.mqlCommand(context, "get env $1", "TRANSHISTORY")` | 풀기 전의 원문을 환경 변수로 읽는 것. 풀기는 직접 해야 함 (`emxCommonDocumentBase_mxJPO.java`) |

::: info 근거
xerinfra.jar `TransactionTriggerUtil.parseHistory`: `isNullOrEmpty` 면 빈 Map. `id=` 마다 `id=` 뒤부터 `type=` 앞까지를 id, `type=` 뒤부터 `triggerName=` 앞까지를 종류로 잘라 `id + "_" + 종류` (BootstrapMethods `\u0001_\u0001`), `history=` 뒤부터 다음 `"\nid="` 앞까지를 `parseEvents` 로 풀어 `FrameworkUtil.join(events, "|")`. 예외면 `printStackTrace` 후 다시 던짐. `parseEvents`: `Environment.getValue(context, "MX_NORMAL_DATETIME_FORMAT")`, `- user:` 마다 앞 줄의 마지막 줄을 이벤트로, 첫 사용자를 `setGlobalRPEValue(context, "USER", …)`, 시각이 있으면 `이벤트_시각`. OOTB 호출 예: emxCommonDocumentBase_mxJPO.java (`strArray[1]` 이 `BusinessObject` 인지 확인), enoProxyObjectUtil_mxJPO.java, ENOQuestionUIBase_mxJPO.java
:::

## 기록 남기기

객체에 사용자 기록 한 줄을 더합니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `MqlUtil.mqlCommand(context, "modify bus $1 add history $2 comment $3", objectId, event, comment)` | `event`: 이벤트 이름 (`"Review"`). `comment`: 설명 | `String` (보통 `""`) |

```java
MqlUtil.mqlCommand(context, "modify bus $1 add history $2 comment $3", objectId, "Review", "Checked by " + context.getUser());
```

- OOTB 는 이 형태를 14 곳에서 그대로 씁니다. 기록 지우기는 `modify bus $1 delete history last`, `modify bus $1 delete history before $2` (각 1 곳) 입니다.
- 어떤 권한이 있어야 기록을 더할 수 있는지는 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />. OOTB 는 슈퍼유저 형태 (`mqlCommand(context, cmd, true, values...)`) 로도 씁니다 ([MQL 실행, 환경 변수](/jpo/api/util/mql)).
- [pushContext](/jpo/api/util/transaction) 안에서 고친 내용은 기록의 사용자가 User Agent 로 남는 것으로 보입니다 <Badge type="danger" text="검증 필요" />. OOTB 는 실제 사용자 이름을 설명에 적은 기록을 따로 남기기도 합니다 (`emxCPNProductDataLifecycleBase_mxJPO.java`).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `"modify bus " + id + " add history '…' comment '…'"` 이어 붙이기 | 값에 따옴표가 있으면 깨지고 MQL 인젝션 위험. OOTB 일부에 남아 있음 |
| `ChangeUtil.addHistory(context, …)` (enterprisechangemgt.jar) | 앱 전용 (ECM) |

## 기록 잠시 끄기

여러 `DomainObject` 메서드로 고치는 동안 기록이 남지 않게 합니다. 명령 하나만이면 [MQL 실행, 환경 변수](/jpo/api/util/mql) 의 히스토리 없이 실행하는 형태를 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `MqlUtil.mqlCommand(context, "history off", false, false)` … `finally` 에서 `"history on"` | 없음 | `String` |

```java
MqlUtil.mqlCommand(context, "history off", false, false);
try {
    part.setAttributeValue(context, attrStatus, "Checked");
} finally {
    MqlUtil.mqlCommand(context, "history on", false, false);
}
```

- 꼭 `finally` 에서 다시 켭니다. OOTB 도 `try` / `finally` 로 짝을 맞춥니다 (`emxCommonInitiateRouteBase_mxJPO.java`).
- 끄는 범위 (그 세션의 뒤 명령 전체인지) 는 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.
- OOTB 의 `mqlCommand(context, "history off;", true)` 와 `mqlCommand(context, "history on;")` 는 폐기 형태입니다 ([MQL 실행, 환경 변수](/jpo/api/util/mql)).

자세히: [트리거 / 히스토리 끄기](/jpo/mql-util#트리거-히스토리-끄기)

## 이벤트 이름 번역

`change owner` 같은 이벤트 이름을 화면에 보일 말로 바꿉니다. 기록을 표로 돌려주는 JPO 에서 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `EnoviaResourceBundle.getHistoryI18NString(context, event, lang)` | `event`: 이벤트 이름. `lang`: 언어 (`ko`, `en` ...) | `String`. `emxFrameworkStringResource` 의 `emxFramework.History.<공백을 _ 로 바꾼 이름>` 값 |

| 호출 | 결과 |
| --- | --- |
| `getHistoryI18NString(context, "change owner", "ko")` | `소유자 변경` |
| `getHistoryI18NString(context, "change owner", "en")` | `Change Owner` |
| `getHistoryI18NString(context, "promote", "ko")` | `진행` |

```java
String label = EnoviaResourceBundle.getHistoryI18NString(context, "change owner", context.getSession().getLanguage());
```

- 결과는 분석한 properties 의 값입니다. 찾은 값이 `""` 나 `null` 이면 넘긴 이름을 그대로 돌려줍니다. 리소스에 없는 이름일 때 `getFrameworkStringResourceProperty` 가 무엇을 돌려주는지는 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `i18nNow.getHistoryI18NString(event, lang)` | 같은 키를 context 없이 `i18nNow.GetString` 으로 찾는 예전 형태 |

::: info 근거
xerinfra.jar `EnoviaResourceBundle.getHistoryI18NString(Context, String, String)` → `getMXI18NString(context, event, "", lang, "History")`: `"emxFramework." + "History" + "." + event` 의 공백을 `_` 로 바꾼 키를 `getFrameworkStringResourceProperty(context, 키, new Locale(lang))` 로 읽어 `""` 나 `null` 이 아니면 그 값, 아니면 `event`. `i18nNow.getHistoryI18NString(String, String)` 은 같은 키를 `GetString("emxFrameworkStringResource", lang, 키)` 로 읽음. 둘 다 폐기 표시 없음. properties/emxFrameworkStringResource.properties:5161 (`emxFramework.History.change_owner=Change Owner`), emxFrameworkStringResource_ko.properties:5174, 5184
:::

## 샘플 JPO

[XX_UtilHistory_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilHistory_mxJPO.java) 의 메서드입니다. 모든 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map (`programMap`) 에서 값을 읽습니다.

| 메서드 | 받는 값 (`programMap` 의 키) | 돌려주는 값 | 보여 주는 것 |
| --- | --- | --- | --- |
| `getHistoryRows` | `objectId` | `MapList` (기록마다 `action`, `time`, `user`, `state`, `description`) | [getHistoryData](#객체-기록-모두-읽기) 의 다섯 목록을 행으로. `time: ` 같은 앞부분은 붙어 있을 때만 뗌 |
| `getLatestHistory` | `objectId` | `Map` (`action`, `time`, `user`, `state`, `description`). 기록이 없으면 값이 `""` | [가장 최근 기록 읽기](#가장-최근-기록-읽기) |
| `getPreviousOwner` | `objectId` | `String` (마지막 소유자 변경 직전의 소유자. 없으면 `""`) | [한 종류 이벤트 기록만 읽기](#한-종류-이벤트-기록만-읽기) |
| `getHistoryByIndex` | `objectId` | `Map` (`count`, `history[count-1]`, `history[count]` 의 원문) | `history[n]` 의 순번을 서버에서 확인하는 용도 ([검증 필요](#가장-최근-기록-읽기)) |
| `addReviewHistory` | `objectId`, `comment` | 없음 | [기록 남기기](#기록-남기기). **기록을 더함** |
| `updateStatusWithoutHistory` | `objectId`, `status` | 없음 | [기록 잠시 끄기](#기록-잠시-끄기). **속성을 바꿈** |

::: details XX_UtilHistory_mxJPO.java

<<< @/../JPOSample/util/XX_UtilHistory_mxJPO.java

:::

::: warning
샘플의 속성 이름 (`attribute_MyStatus`) 은 예시입니다. 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다.
:::

## 다루지 않은 메서드

::: details 펼쳐 보기
| 클래스 | 메서드 | 이유 |
| --- | --- | --- |
| `UINavigatorUtil` | `getHistoryDescriptionDisplayString` (2 형태) | 화면용 |
:::
