# JPO 의 history 구현 정리 (조사 메모)

JPO 에서 객체 history 를 읽고, 해석하고, 쓰는 OOTB 구현을 조사한 메모와 예제입니다. 가장 최근 기록을 읽는 방법을 찾다가 시작했습니다. VitePress 문서에 옮기기 전 단계라서 서버에서 확인하지 못한 항목이 남아 있습니다. 정리본은 VitePress 문서의 JPO > API 레퍼런스 > 유틸리티 > history 에 있습니다.

- 예제: `XX_HistoryUtil_mxJPO.java` (같은 폴더). enovia2025 jar 로 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다.
- 근거: enovia2025 `3dspace/WEB-INF/lib` 의 jar 를 `javap` 로 읽은 바이트코드와 `jpo/` 의 OOTB JPO (`emx*`, `eno*`, `ENO*`).

## 한눈에 보기

| 하고 싶은 일 | 방법 | 절 |
| --- | --- | --- |
| 객체 history 전체 (또는 최근 `maxRecords` 건) 읽기 | `UINavigatorUtil.getHistoryData(context, id)` | 1.1 |
| history 를 `History` 객체로 읽기 | `BusinessObject.getFilteredHistory(...)` | 1.1 |
| 한 종류 이벤트 기록만 읽기 | `obj.getInfoList(context, "history.changeowner")` | 1.1 |
| 트리거에서 지금 트랜잭션의 기록 해석 | `TransactionTriggerUtil.parseHistory` | 1.2 |
| 기록 추가, 삭제, 잠시 끄기 | `modify bus $1 add history ...`, `history off` | 1.3 |
| 가장 최근 기록 1 건 읽기 | 목록의 마지막 값 | 2 |
| 기록 수, n 번째 기록, 어느 시각 이후 기록 읽기 | `history.count`, `history[n]`, `history.time.between[시각\|]` | 3 |

## 1. 구현 목록

### 1.1 history 읽기

| 구현 | 내용 |
| --- | --- |
| `UINavigatorUtil.getHistoryData(context, id)` (xerinfra.jar) | `HashMap` 으로 돌려줌. 기록이 많으면 최근 기록만 읽음 (아래) |
| `UINavigatorUtil.getHistoryData` 3, 4 개 인자 형태 | 3 개 형태의 세 번째 인자는 select 식. 다섯 select (`history.time` ...) 를 최대 건수 처리 없이 모두 읽고, `print bus $1 select <select> dump` 결과를 잘라 덧붙임. 키는 `time`, `user`, `action`, `state`, `description`. 4 개 형태는 타입, 이름, 리비전으로 `print bus $1 $2 $3 select $4 dump $5` 를 부름 |
| `BusinessObject.getHistory(context)` (eMatrixServletRMI.jar) | `StringList` |
| `BusinessObject.getFilteredHistory(context, StringList, boolean, String, long, long, boolean, String, boolean, int)` | `HistoryList`. 원소 `matrix.db.History` 는 `getTime()`, `getUser()`, `getEvent()`, `getState()`, `getInfo()`. 사용 예 `emxPlayButtonSummaryBase_mxJPO.java` (`null, false, null, 0, 0, false, null, true, 0`). 인자는 서버로 보내는 XML 이름으로 `events, excludeEvents, user, fromDate, toDate, last, state, descending, limit` (`limit` 은 0 이하나 5000 초과면 5000). 실제 동작은 확인 못 함 |
| `Relationship.getHistory`, `getFilteredHistory` | 관계의 history. select 는 `DomainRelationship.SELECT_HISTORY` = `"history[connection]"` |
| 이벤트별 select | `history.changeowner`, `history.connect`, `history.checkin`, `history.custom`, `history.changeproject`, `history.changeorganization` 을 `getInfoList` 나 `print bus ... dump` 로 읽음 |
| `ChangeUtil.getHistory(context, id, select)` (enterprisechangemgt.jar) | 안에서 `print bus $1 select $2 dump` |
| `AuditTrailService.getHistory(context, ids, 시작, 끝, boolean, String)` (ENOAuditTrail.jar) | 기간으로 거른 감사 이력 (`ENOAuditTrailsBase_mxJPO.java`) |
| `ChangeOrder.getCustomHistory`, `getFilteredHistoryBasedOnAction` | ECM 의 소유권 변경, 보류, 재개, 취소 이력 (`enoECMChangeUXBase_mxJPO.java`) |
| `HistoryUtill.getLinkHistory(context, String[], String)` (ENOHistoryService.jar) | 이름으로 보아 링크 이력. 동작은 확인 못 함 |

`UINavigatorUtil.getHistoryData(context, id)` 의 동작:

1. 최대 건수 `emxFramework.History.maxRecords` 를 읽음 (분석한 `emxSystem.properties` 는 `2000`. 0 이하면 `2000`).
2. `history.count` 로 기록 수를 읽음.
3. 기록 수가 최대 건수보다 많으면 `history[기록수 - 최대건수]` 를 select 해 그 기록의 시각을 꺼내고, `history.time.between[시각|]`, `history.event.between[시각|]` ... 로 그 시각 이후의 기록만 읽음 ([3 절](#3-순번과-범위-select)).
4. 많지 않으면 `history.time`, `history.event`, `history.user`, `history.state`, `history.description` 을 모두 읽음.

| 돌려주는 `HashMap` 의 키 | 값 |
| --- | --- |
| `time`, `user`, `action`, `state`, `description` | `StringList`. 기록마다 하나 (`action` 은 `history.event`) |
| `modifiedName` | `String`. 이벤트에 `change name` 이 있을 때만 현재 `name` |

- `history.xxx` 는 값이 여러 개인 select 입니다. `getInfo(context, select)` 는 **첫 값만** 주므로 전부 받으려면 `getInfoList` 나 `print bus ... dump` 를 씁니다.

### 1.2 트리거 안의 현재 트랜잭션 history

저장된 history 가 아니라 지금 트랜잭션에서 일어난 일을 해석합니다.

| 구현 | 내용 |
| --- | --- |
| `TransactionTriggerUtil.parseHistory(context, String)`, `parseEvents` (xerinfra.jar) | 트리거 인자로 받은 트랜잭션 history 문자열 (`history=`, `- user:` ... 구분자) 을 해석 |
| `emxTransactionNotificationUtilBase_mxJPO.java` `parseHistory` | JPO 쪽 같은 일 |
| `MqlUtil.mqlCommand(context, "get env $1", "TRANSHISTORY")` | 트랜잭션 history 를 env 로 읽음 (`emxCommonDocumentBase_mxJPO.java`) |

### 1.3 쓰기와 기타

| 구현 | 내용 |
| --- | --- |
| `modify bus $1 add history $2 comment $3` | 사용자 기록 추가 (OOTB 14 건). ECM 에는 `ChangeUtil.addHistory(context, String, String, String)` 도 있음 |
| `modify bus $1 delete history last`, `delete history before $2` | 기록 삭제 |
| `history off;` / `history on;` | 기록을 잠시 끔 |
| `i18nNow.getHistoryI18NString(String, String)` | history 문구 번역으로 보임. 동작은 확인 못 함 |

## 2. 가장 최근 기록 읽기

- **가장 최근 1 건만 돌려주는 OOTB util 메서드는 없습니다.** OOTB 는 history 를 목록으로 읽고 **마지막 항목** 을 최근 기록으로 씁니다.
- 목록은 오래된 것부터 최신 순으로 오는 것으로 보입니다. `getHistoryData` 가 기록이 많을 때 뒤쪽 순번을 남기고, OOTB JPO 도 마지막 줄을 최근 값으로 씁니다. 서버에서 확인하지는 않았습니다.

| OOTB 위치 | 방법 |
| --- | --- |
| `emxCPNNotificationUtilBase_mxJPO.java` | `print bus $1 select history.changeowner dump` 결과를 `"\n"` 으로 자르고 **마지막 줄** 에서 `lastIndexOf("was:")` 뒤를 직전 소유자로 씀 |
| `emxProgramMigrationRestorePOV_mxJPO.java` | `history.changeproject`, `history.changeorganization` 값에서 `lastIndexOf("was")` 뒤를 직전 협업공간, 조직으로 씀 |
| `UINavigatorUtil.getHistoryData(context, id)` | 최근 `maxRecords` 건만 읽음. 결과 목록의 마지막 값이 최근 기록 |

- 다섯 값 (`time`, `user`, `action`, `state`, `description`) 이 모두 필요하면 `getHistoryData` 결과의 마지막 값을 씁니다 (예제 `getLatestHistory`).
- 한 종류 이벤트의 최근 값만 필요하면 이벤트 select 하나만 읽는 쪽이 가볍습니다 (예제 `getLatestEventHistory`, `getPreviousOwner`).

## 3. 순번과 범위 select

`getHistoryData` 가 쓰는 select 입니다. 다른 OOTB JPO 에서는 쓰는 곳을 찾지 못했습니다.

| select | 값 |
| --- | --- |
| `history.count` | 기록 수 |
| `history[n]` | n 번째 기록 하나의 원문. 순번이 0 부터인지 1 부터인지는 확인 못 함 |
| `history.time.between[시각\|]`, `history.event.between[시각\|]`, `history.user.between[...]`, `history.state.between[...]`, `history.description.between[...]` | 그 시각 이후 기록의 각 값 (여러 값). 시각은 `eMatrixDateFormat.getEMatrixDateFormat()` 형식 |

- `getHistoryData` 는 `history[n]` 원문에서 시각을 꺼내 `[시각|]` 를 만듭니다. `|` 뒤를 비운 것은 끝 시각을 두지 않는다는 뜻으로 보입니다.
- 순번이 확인되면 `history[마지막 순번]` 한 번으로 최근 기록을 읽을 수 있습니다. 확인용 예제가 `getHistoryByIndex` 입니다.

## 4. 예제 (`XX_HistoryUtil_mxJPO.java`)

모든 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map 에서 값을 읽습니다.

| 메서드 | 받는 값 | 돌려주는 값 | 방법 |
| --- | --- | --- | --- |
| `getLatestHistory` | `objectId` | `Map` (`time`, `user`, `action`, `state`, `description`). 기록이 없으면 값이 `""` | `UINavigatorUtil.getHistoryData` 결과 목록마다 마지막 값 |
| `getLatestEventHistory` | `objectId`, `event` (`changeowner`, `connect` ...) | `String`. 그 이벤트의 마지막 기록 원문. 없으면 `""` | `getInfoList(context, "history." + event)` 의 마지막 값 |
| `getPreviousOwner` | `objectId` | `String`. 마지막 소유자 변경 직전의 소유자. 없으면 `""` | `history.changeowner` 마지막 기록의 `was:` 뒤 (`emxCPNNotificationUtilBase` 와 같은 방식) |
| `getHistoryByIndex` | `objectId` | `Map` (`count`, `history[count-1]`, `history[count]` 의 원문) | `history.count` 와 순번 select. 서버에서 어느 쪽이 마지막 기록인지 확인하는 용도 |

## 5. 확인 필요

- `history[n]` 의 순번이 0 부터인지 1 부터인지. 곧 `history[history.count]` 와 `history[history.count - 1]` 중 어느 쪽이 마지막 기록인지 (`getHistoryByIndex` 로 확인).
- history 목록이 오래된 순인지 (서버에서 `print bus <id> select history.time dump` 로 확인).
- `history.xxx.between[시각|]` 의 `|` 뒤에 끝 시각을 넣을 수 있는지.
- `history.changeowner` 같은 이벤트 select 원문의 정확한 모양 (`was:` 앞뒤).
- `getFilteredHistory` 인자 (특히 마지막 `boolean`, `int`) 가 정렬이나 개수 제한인지.
