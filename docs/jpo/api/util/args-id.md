# 인자, JPO 호출, id <Badge type="info" text="원본 분석" />

JPO 가 받은 `args` 에서 값을 꺼내고, 다른 JPO 를 부르고, 표 화면에서 넘어온 행 값과 객체 id 를 다루는 메서드입니다. 예제의 `part` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다. util 전체를 하고 싶은 일로 찾으려면 [유틸리티](/jpo/api/utilities) 를 봅니다.

::: info 근거
eMatrixServletRMI.jar (`JPO`, `JPOSupport`, `JPOInvocationParams`, `LocalJPO`, `ThreadMap`), xerinfra.jar (`FrameworkUtil`, `UIUtil`) class 파일 (javap) 기준. `JPO.packArgs`, `JPO.unpackArgs`, `FrameworkUtil` 의 행 값, id 모양 메서드는 OOTB jar 만 클래스패스에 두고 로컬에서 실행해 결과를 적었습니다 (서버에 가는 `JPO.invoke`, `getPIDfromOID` 류와 `UIUtil` 은 바이트코드로만 확인). 화면이 만드는 행 값은 OOTB JSP, JavaScript 로 확인했습니다. 건수는 OOTB JPO 에서 센 호출 수: `JPO.unpackArgs(` 5,294, `JPO.packArgs(` 376, `JPO.invoke(` 374, `FrameworkUtil.isObjectId(` 25, `JPO.invokeLocal(` 7, `FrameworkUtil.getPIDfromOID(` 5, `FrameworkUtil.getSplitTableRowIds(` 3, `FrameworkUtil.getOIDfromPID(` 2
:::

샘플: [XX_UtilArgs_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilArgs_mxJPO.java) ([메서드별 설명](#샘플-jpo))

## 한눈에 보기

| 하고 싶은 일 | 쓸 메서드 |
| --- | --- |
| 받은 `args` 를 Map 으로 되돌리기 | [`JPO.unpackArgs(args)`](#넘어온-인자-꺼내기) |
| 다른 JPO 의 메서드를 이름으로 부르기 | [`JPO.invoke(context, "이름", null, "메서드", JPO.packArgs(map), 반환형.class)`](#다른-jpo-부르기) |
| 표, 구조 화면에서 고른 행의 objectId 꺼내기 | [`FrameworkUtil.getSplitTableRowIds(rowIds)`](#objectid-꺼내기) |
| 고른 행의 관계 id, 부모 id, 행 위치 꺼내기 | [`FrameworkUtil.split(rowId, "\|")` 를 칸 수로 읽기](#관계-id-부모-id-까지-꺼내기) |
| 문자열이 객체 id 모양인지 확인 | [`FrameworkUtil.isObjectId(context, s)`](#id-모양-확인) |
| 객체 id 를 physicalid 로 | [`FrameworkUtil.getPIDfromOID(context, id)`](#physicalid-와-id-바꾸기) |
| physicalid 를 객체 id 로 | [`FrameworkUtil.getOIDfromPID(context, pid)`](#physicalid-와-id-바꾸기) |
| 관계 physicalid 를 관계 id 로 | [`FrameworkUtil.getRelIDfromPID(context, pid)`](#physicalid-와-id-바꾸기) |

## 넘어온 인자 꺼내기

JPO 메서드는 모두 `(Context context, String[] args)` 를 받습니다. 화면이나 다른 JPO 가 Map 을 `JPO.packArgs` 로 싸서 넘기므로 받는 쪽은 `JPO.unpackArgs` 로 되돌립니다. 어떤 키가 들어 있는지는 부르는 쪽마다 다릅니다 ([호출 종류별 인자 구조](/jpo/args)).

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `JPO.unpackArgs(args)` | `args`: 메서드가 받은 `String[]` 그대로 | 싸서 넘긴 객체. 보통 `Map` (`HashMap`). 캐스팅해서 받음 |

| 호출 | 결과 |
| --- | --- |
| `JPO.unpackArgs(JPO.packArgs(map))` | `map`. 같은 스레드에서는 같은 객체 (`==` 가 `true`) |
| `JPO.unpackArgs(new String[0])`, `JPO.unpackArgs(new String[] {"1.2.3.4"})` | `Exception`: `The argument was not properly packed, count != 2` |
| `JPO.unpackArgs(new String[] {"1.2.3.4", "Part"})` | `ClassNotFoundException: 1.2.3.4`. 첫 칸을 클래스 이름으로 읽음 |
| `JPO.unpackArgs(null)` | `NullPointerException` |

```java
Map programMap = (Map) JPO.unpackArgs(args);
String objectId = (String) programMap.get("objectId");
boolean hasObject = UIUtil.isNotNullAndNotEmpty(objectId);
```

- 서버에 가지 않습니다.
- 값은 대부분 `String` 이고 없는 키는 `null` 입니다. `"null"` 문자열이 오기도 하니 [빈 값 확인](/jpo/api/util/string-list) 의 `UIUtil.isNullOrEmpty` 로 거릅니다.
- 트리거, MQL `execute program` (`mxMain`), `program[...]` select 로 불린 메서드의 `args` 는 싸지 않은 위치 인자입니다. `unpackArgs` 를 부르면 위 예외가 나므로 `args[0]` 처럼 바로 읽습니다 ([트리거](/trigger/), [MQL 에서 실행](/jpo/#mql-에서-실행-mxmain)).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `JPO.unpackArgs(args, retain)` | `unpackArgs(args)` 가 환경 변수 `MX_RETAIN_OBJECT_AFTER_UNPACK` 값 (없으면 `true`) 을 넣어 그대로 부름. `false` 면 꺼내면서 저장소에서 지워 같은 `args` 를 다시 꺼낼 수 없음 |
| `UIUtil.getValue(map, key)` | `null` 을 `""` 로 바꾸고 앞뒤 공백을 지움. `"null"` 문자열은 그대로라 `isNullOrEmpty` 확인이 또 필요함. 값이 `String` 이 아니면 `ClassCastException`. OOTB 는 검색 화면 JPO 에서만 씀 |

::: info 근거
eMatrixServletRMI.jar `JPO.unpackArgs(String[])` 는 `unpackArgs(args, RETAIN_AFTER_UNPACK)` (`RETAIN_AFTER_UNPACK` 은 `Environment.getValueBoolean(..., "MX_RETAIN_OBJECT_AFTER_UNPACK", true)`). `unpackArgs(String[], boolean)`: 길이가 2 가 아니면 `Exception("The argument was not properly packed, count != 2")`. `args[0]` 이 `$$MXRIP$$|` 로 시작하면 `JPOSupport.getReference(DEFAULT, args[1], retain)` (지금 스레드의 `ThreadMap` 에서 꺼냄. `retain` 이 거짓이면 꺼내면서 지움), 아니면 `Class.forName(args[0])` 다음 `args[1]` 을 Mime64 로 풀어 역직렬화. xerinfra.jar `UIUtil.getValue(Map, String)`: `(String) map.get(key)` 가 `null` 이면 `""`, 아니면 `trim()`. 결과는 로컬 실행으로 확인 (`UIUtil.getValue` 는 바이트코드). OOTB: `JPO.unpackArgs(` 5,294 (모두 인자 하나 형태), `UIUtil.getValue(` 29 (`emxTypeAheadFullSearchBase_mxJPO.java`, `emxAEFFullSearchBase_mxJPO.java`)
:::

## 다른 JPO 부르기

다른 JPO 의 메서드를 이름으로 부를 때 씁니다. 넘길 값은 Map 에 담아 `JPO.packArgs` 로 싸고, 받는 쪽은 [넘어온 인자 꺼내기](#넘어온-인자-꺼내기) 처럼 꺼냅니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `JPO.invoke(context, "이름", null, "메서드", JPO.packArgs(map), 반환형.class)` | `"이름"`: 프로그램 이름. 클래스 이름에서 `_mxJPO` 를 뺀 것 (`"XX_UtilArgs"`). 세 번째: 생성자에 넘길 `String[]` (보통 `null`). `"메서드"`: 부를 메서드 이름. 다섯 번째: 메서드에 넘길 `String[]` | 그 메서드가 돌려준 객체. `반환형.class` 는 읽지 않으므로 받는 쪽에서 캐스팅함. 서버가 돌려준 결과가 비면 `null` |
| `JPO.packArgs(map)` | `map`: 넘길 값. 보통 `HashMap` | `String[]` 두 칸. 첫 칸은 `$$MXRIP$$\|` 와 클래스 이름, 둘째 칸은 저장소 열쇠 |

| 호출 | 결과 |
| --- | --- |
| `JPO.packArgs(map)` | `["$$MXRIP$$\|java.util.HashMap", "113786060"]`. 열쇠 숫자는 부를 때마다 다름 |
| `JPO.packArgs(null)` | `NullPointerException` |
| 다른 스레드에서 그 결과로 `JPO.unpackArgs` | `null` |

```java
Map params = new HashMap();
params.put("objectId", objectId);
MapList children = (MapList) JPO.invoke(context, "XX_ObjectApi", null, "getChildParts", JPO.packArgs(params), MapList.class);
```

- 서버 (커널) 에 호출을 보내 그 JPO 를 실행합니다. `packArgs` 는 서버에 가지 않습니다.
- 받은 `context` 를 그대로 넘깁니다. 사용자와 트랜잭션은 [다른 사용자 / 다른 JPO 호출과 context](/jpo/transaction-context#다른-사용자-다른-jpo-호출과-context) 를 봅니다.
- 부른 메서드가 예외를 던지면 그 문구를 담은 `MatrixException` 이 납니다. 부른 메서드가 남긴 `notice` 같은 메시지는 context 의 클라이언트 작업에 더해집니다.
- 받는 쪽이 표나 폼에서 불리는 메서드면 화면이 넘기는 Map 모양 (`objectList`, `paramList`, `paramMap` ...) 을 그대로 만들어야 합니다 ([다른 곳에서 JPO 부르기](/jpo/#다른-곳에서-jpo-부르기)).
- `packArgs` 는 Map 을 복사하지 않고 지금 스레드의 저장소에 맡긴 뒤 열쇠만 담습니다. 같은 스레드에서 꺼내면 같은 객체라서 받는 쪽에서 Map 을 고치면 부른 쪽 Map 도 바뀔 수 있습니다 <Badge type="danger" text="검증 필요" />. context 가 로컬이 아니면 `invoke` 가 보내기 전에 그 객체를 직렬화합니다.
- OOTB 는 프로그램 이름에 `_mxJPO` 를 붙이지 않습니다 (이름을 문자열로 준 294 건 중 293 건).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `JPO.invoke(context, "이름", null, "메서드", args)` (반환형 인자 없음) | 서버 처리 결과 `int` (`status`) 만 돌려줌. `mxMain` 처럼 `int` 를 돌려주는 메서드용. OOTB 33 건 |
| `JPO.invoke(context, 클래스.class, 인자...)`, `JPO.invoke(context, 클래스.class, 반환형)`, `JPO.invoke(context, JPOInvocationParams)` | 클래스나 `JPOInvocationParams` 로 부르는 형태. 앞의 둘은 `mxMain` 을 부름. OOTB 0 건 |
| `JPO.invokeLocal(...)` (3 형태) | 서버 커널을 거치지 않고 지금 JVM 에서 JPO 객체를 만들어 부름. `throws Throwable` 로 선언되어 있음. OOTB 7 건 |
| `JPO.packArgsRemote(map)` | Map 을 바로 직렬화한 복사본을 만듦. context 가 로컬이 아니면 `invoke` 가 `packArgs` 값을 알아서 직렬화하므로 쓸 일이 없음. OOTB 0 건 |
| `new XX_Other_mxJPO(context, args).method(context, args)`, `XX_Other_mxJPO.method(...)` | 컴파일할 때 그 클래스가 있어야 함 ([다른 JPO 에서](/jpo/#다른-jpo-에서)) |

::: info 근거
eMatrixServletRMI.jar `JPO.packArgs(Object)`: `["$$MXRIP$$|" + 클래스 이름, JPOSupport.setObject(DEFAULT, o)]`. `setObject` 는 `ThreadMap.getCurrentThreadMap(bucket).setObject(o)` 로 지금 스레드의 Map 에 넣고 열쇠 문자열을 돌려줌. `invoke(Context, String, String[], String, String[], Class)`: `JPOInvocationParams` 에 클래스 이름, 메서드 이름, 생성자 인자, 메서드 인자를 넣고 `exec(context)` → `JPO.invoke(Context, JPOInvocationParams)`. 마지막 `Class` 인자는 읽지 않음. `invokePrivate` 는 context 가 로컬이 아니면 `$$MXRIP$$|` 로 시작하는 인자를 `writeObject` 로 직렬화해 바꾸고 (`fixArgs`), `bosInterface.invokeClass` 를 부른 뒤 클라이언트 작업을 context 에 더하고 오류 문구가 있으면 `MatrixException(오류)`. 결과 문자열에서 `$$JPORET:` 뒤를 `trim` 해 비면 `null`, 로컬이면 `JPOSupport.getResult`, 아니면 역직렬화. 반환형 없는 `invoke` 는 결과의 `status`. `invoke(Context, Class, String...)` 은 메서드 이름 `mxMain`. `LocalJPO.invokeLocal(Context, JPOInvocationParams)`: `JPOSupport.newInstance(클래스 이름, ...)` 로 객체를 만들고 `JPOSupport.invokeObject` 로 부름. 로컬 실행: `packArgs` 결과, 같은 스레드의 `unpackArgs` 가 같은 객체 (`==`), 다른 스레드는 `null`, `packArgsRemote` 를 푼 결과는 복사본. OOTB `JPO.invoke(` 374 (반환형까지 인자 6 개 340, 5 개 33), `JPO.invokeLocal(` 7 (`emxMemberListBase_mxJPO.java`, `enoFormulationProcessBase_mxJPO.java` 등), `JPO.packArgsRemote(` 0
:::

## 표, 구조 화면에서 고른 행 나누기

표나 구조 화면 (Structure Browser) 에서 행을 고르고 명령을 누르면 JPO 는 고른 행마다 `emxTableRowId` 값을 받습니다. 값 하나에 관계 id, 객체 id, 부모 id, 행 위치가 `|` 로 이어져 있습니다.

### 행 값의 모양

| 화면 | 값 | 예 |
| --- | --- | --- |
| 표 (`emxTable.jsp`), 관계 없는 행 | `objectId` | `1.2.3.4` |
| 표, 관계로 연결된 행 | `relId\|objectId` | `5.6.7.8\|1.2.3.4` |
| 구조 화면 | `relId\|objectId\|parentId\|행위치` | `5.6.7.8\|1.2.3.4\|9.9.9.9\|0,1` |
| 구조 화면의 맨 위 행 | `\|objectId\|\|0` | `\|1.2.3.4\|\|0` |

- 구조 화면은 없는 관계 id 와 부모 id 를 `""` 로 채워 언제나 네 칸을 만듭니다.
- 행 위치는 `0` (맨 위 행), `0,0`, `0,1` ... 처럼 위 행 번호를 쉼표로 이은 값입니다. OOTB 는 행 위치가 `0` 이면 맨 위 행을 고른 것으로 봅니다.
- 값은 `programMap.get("emxTableRowId")` 로 꺼냅니다. 부르는 JSP 에 따라 고른 행 모두가 `String[]` 로 오기도 하고 첫 행만 `String` 으로 오기도 합니다 (OOTB 는 `(String[])` 28 건, `(String)` 22 건). `RequestValuesMap` 안에 `String[]` 로 들어 있기도 합니다 ([공통](/jpo/args#공통)).

### objectId 꺼내기

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.getSplitTableRowIds(rowIds)` | `rowIds`: `emxTableRowId` 값 배열 | `String[]`. 행과 같은 순서의 objectId. `rowIds` 가 `null` 이거나 빈 배열이면 빈 배열 |

| 행 값 | 결과 |
| --- | --- |
| `1.2.3.4` | `1.2.3.4` |
| `5.6.7.8\|1.2.3.4` | `1.2.3.4` |
| `\|1.2.3.4\|\|0` | `1.2.3.4` |
| `\|1.2.3.4\|9.9.9.9\|0,0` | `1.2.3.4` |
| `5.6.7.8\|1.2.3.4\|9.9.9.9\|0,1` | `1.2.3.4` |
| `\|1.2.3.4` | `\|1.2.3.4`. 받은 값 그대로 |
| `a\|b\|c\|d\|e` | `a\|b\|c\|d\|e`. 받은 값 그대로 |

```java
Map programMap = (Map) JPO.unpackArgs(args);
String[] rowIds = (String[]) programMap.get("emxTableRowId");
String[] selectedIds = FrameworkUtil.getSplitTableRowIds(rowIds);
MapList selected = DomainObject.getInfo(context, selectedIds, StringList.create(DomainConstants.SELECT_NAME));
```

- 서버에 가지 않습니다. 고른 행의 객체가 있는지는 보지 않습니다.
- 화면이 만들지 않는 모양 (`|1.2.3.4` 처럼 두 칸인데 앞이 빈 값) 은 그대로 돌려주므로 결과도 id 가 아닙니다.

### 관계 id, 부모 id 까지 꺼내기

관계를 끊거나 부모를 알아야 할 때는 행 값을 `FrameworkUtil.split(rowId, "|")` 로 잘라 칸 수로 읽습니다. `split` 은 맨 앞의 빈 칸 하나를 버리므로 관계 id 가 없는 구조 행은 세 칸이 됩니다 ([문자열 자르기](/jpo/api/util/string-list)).

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.split(rowId, "\|")` | `rowId`: 행 값 하나 | `StringList`. 칸 수에 따라 아래 표처럼 읽음 |

| 칸 수 | 행 | `[0]` | `[1]` | `[2]` | `[3]` |
| --- | --- | --- | --- | --- | --- |
| 1 | 표, 관계 없음 | objectId | | | |
| 2 | 표, 관계 있음 | relId | objectId | | |
| 3 | 구조, 관계 없음 (맨 위 행 등) | objectId | parentId (맨 위 행은 `""`) | 행 위치 | |
| 4 | 구조, 관계 있음 | relId | objectId | parentId | 행 위치 |

| 호출 | 결과 |
| --- | --- |
| `FrameworkUtil.split("1.2.3.4", "\|")` | `["1.2.3.4"]` |
| `FrameworkUtil.split("5.6.7.8\|1.2.3.4", "\|")` | `["5.6.7.8", "1.2.3.4"]` |
| `FrameworkUtil.split("\|1.2.3.4\|\|0", "\|")` | `["1.2.3.4", "", "0"]` |
| `FrameworkUtil.split("\|1.2.3.4\|9.9.9.9\|0,0", "\|")` | `["1.2.3.4", "9.9.9.9", "0,0"]` |
| `FrameworkUtil.split("5.6.7.8\|1.2.3.4\|9.9.9.9\|0,1", "\|")` | `["5.6.7.8", "1.2.3.4", "9.9.9.9", "0,1"]` |

```java
String rowId = "5.6.7.8|1.2.3.4|9.9.9.9|0,1";
StringList parts = FrameworkUtil.split(rowId, "|");
String rowRelId = "";
if (parts.size() == 2 || parts.size() == 4) {
    rowRelId = parts.get(0);
}
```

- OOTB 도 같은 방식으로 읽습니다 (`emxAEFUtilBase_mxJPO.java` 의 `getObjectIds`: 세 칸이면 `[0]`, 아니면 `[1]`. `checkIsRootNodeSelected`: 세 칸이면 `[2]`, 아니면 `[3]` 이 `"0"` 인지).
- Java 의 `rowId.split("\\|", -1)` 은 빈 칸을 버리지 않아 구조 행이 언제나 네 칸 (`["", "1.2.3.4", "", "0"]`) 입니다.
- 샘플의 `splitTableRowIds` 가 이 표대로 행마다 `relId`, `objectId`, `parentId`, `rowPosition` 을 담은 Map 을 만듭니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `FrameworkUtil.getTableRowRelId(rowId)` | 관계 id 가 없는 행이면 받은 값을 그대로 돌려줌. `"\|1.2.3.4\|\|0"` → `"\|1.2.3.4\|\|0"`, `"1.2.3.4"` → `"1.2.3.4"` (객체 id). 빈 값 대신 엉뚱한 값이 옴 |
| `UIUtil.parseRelAndObjectIds(context, rowIds, true)` | 첫 `\|` 에서만 나눔. 구조 행이면 objectId 자리에 `objectId\|parentId\|행위치` 가 들어감. `\|` 가 없는 행은 관계 id 목록에 넣지 않아 두 목록의 순서가 어긋남. 세 번째 인자가 `false` 면 관계 id 배열 뒤쪽이 `null` |
| 앱의 `TableRowId` (ENOQuestionaire.jar, ENOControlledPrinting.jar, periodicreview_classic.jar), `EmxTableRowId` (ENOContacts.jar, ENODownloadPackage.jar, ENOGeographyConfiguration.jar) | 앱마다 따로 만든 같은 기능 (앱 전용). questionnaire 의 것은 안에서 `FrameworkUtil.split(값, "\|")` 의 칸 수 1, 3, 4 로 나누고 두 칸이면 예외 |

::: info 근거
xerinfra.jar `FrameworkUtil.getSplitTableRowIds(String[])`: `null` 이거나 길이 0 이면 빈 배열. 행마다 `split(행, "|")` 의 크기가 3 이면 `[0]`, 4 면 `[1]`, 2 면 `[1]`, 그 밖이면 행 값 그대로. `getTableRowRelId(String)`: 크기가 4 나 2 면 `[0]`, 그 밖이면 받은 값. `UIUtil.parseRelAndObjectIds(Context, String[], boolean)`: context 를 쓰지 않음. 값마다 `indexOf("|")` 가 있으면 앞을 `relIds`, 뒤 전체를 `objectIds` 에 넣고, 없으면 `objectIds` 에만 넣음. 세 번째 인자가 거짓이면 입력 길이의 `String[]` 에 `toArray` 로 옮김. 화면이 만드는 값: common/emxTableDataInclude.jsp (관계 id 가 비면 `objectId`, 아니면 `relId|objectId`), common/scripts/emxUIFreezePane.js (`relid + "|" + oid + "|" + parentId + "|" + id`, 비면 `""`, 행 `id` 는 `r[@id='0,0']` 모양). ENOQuestionaire.jar `com.dassault_systemes.enovia.questionnaire.TableRowId(String)`. 호출 → 결과는 로컬 실행으로 확인 (`parseRelAndObjectIds` 는 바이트코드). OOTB `getSplitTableRowIds(` 3, `getTableRowRelId(` 0, `parseRelAndObjectIds(` 0, `new TableRowId(` 38
:::

## id 모양 확인

넘어온 문자열이 객체 id 인지, 이름이나 타입 같은 다른 값인지 나눌 때 씁니다. 객체가 실제로 있는지는 보지 않습니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.isObjectId(context, s)` | `s`: 확인할 문자열 | `boolean`. `.` 으로 나눈 네 칸이 모두 정수이거나, 정수가 아닌 칸이 32 자 이상의 16 진수 (physicalid 모양) 이면 `true` |

| 호출 | 결과 |
| --- | --- |
| `FrameworkUtil.isObjectId(context, "1234.5678.9012.3456")` | `true` |
| `FrameworkUtil.isObjectId(context, "1234.5678.9012")`, `FrameworkUtil.isObjectId(context, "1.2.3.4.5")` | `false`. 칸이 네 개가 아님 |
| `FrameworkUtil.isObjectId(context, "A1B2C3D4E5F60718293A4B5C6D7E8F90")` | `true`. 32 자 16 진수 |
| `FrameworkUtil.isObjectId(context, "A1B2C3D4E5F60718293A4B5C6D7E8F9")` | `false`. 31 자 |
| `FrameworkUtil.isObjectId(context, "My Part")`, `FrameworkUtil.isObjectId(context, "")` | `false` |
| `FrameworkUtil.isObjectId(context, "1234.5678.9012.3456:x")` | `true`. `:` 뒤는 보지 않음 |
| `FrameworkUtil.isObjectId(context, "5.6.7.8\|1.2.3.4")` | `false` |
| `FrameworkUtil.isObjectId(context, null)` | `FrameworkException` (`NullPointerException` 을 감쌈) |

```java
String value = "1234.5678.9012.3456";
DomainObject target = null;
if (UIUtil.isNotNullAndNotEmpty(value) && FrameworkUtil.isObjectId(context, value)) {
    target = DomainObject.newInstance(context, value);
}
```

- 서버에 가지 않습니다. `context` 를 읽지 않으므로 모양만 맞으면 없는 id 도 `true` 입니다. 있는지는 `obj.exists(context)` 로 확인합니다 ([타입, 존재, 리비전](/jpo/api/read#타입-존재-리비전)).
- `DomainObject.newInstance(context, s)` 도 안에서 이 메서드로 id 인지 타입 이름인지 나눕니다 ([newInstance](/jpo/api/domain-object#newinstance)).
- `null` 을 넘기면 예외이므로 `UIUtil.isNullOrEmpty` 로 먼저 거릅니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `FrameworkUtil.isObjectIdOnly(context, s)` | 첫 글자 뒤에 `.` 이 있는지만 봄. `"a.b"` 도 `true`, physicalid 는 `false`. OOTB 0 건 |
| `FrameworkUtil.isRelId(context, type, id)` | 서버에 `temp query bus 타입 * * where "id ==값"` 을 보내 찾은 객체가 없거나 오류면 `true`. 없는 값, 잘못된 값도 `true`. OOTB 0 건 |

::: info 근거
xerinfra.jar `FrameworkUtil.isObjectId(Context, String)`: context 를 쓰지 않음. `:` 앞만 남기고 (`undecorateOid`) `split(값, ".")` 의 칸마다 `Long.parseLong`. 정수가 아닌 칸이 나오면 `\p{XDigit}*` 에 맞고 길이가 32 이상일 때만 `true`, 아니면 `false`. 모두 정수면 칸 수가 4 일 때 `true`. 예외는 `FrameworkException` 으로 감쌈. `isObjectIdOnly`: `indexOf(".") > 0`. `isRelId(Context, String, String)`: `MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 where $4 select $5 dump", 타입 (비면 "*"), "*", "*", "id ==" + 값, "name")` 결과가 비거나 예외면 참. 호출 → 결과는 로컬 실행으로 확인 (`context` 는 `null` 로 넘김). OOTB `FrameworkUtil.isObjectId(` 25
:::

## physicalid 와 id 바꾸기

객체 id (`1234.5678.9012.3456` 모양) 와 physicalid (32 자 16 진수) 를 서로 바꿀 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.getPIDfromOID(context, id)` | `id`: 객체 id | `String`. 그 객체의 `physicalid` |
| `FrameworkUtil.getOIDfromPID(context, pid)` | `pid`: 객체의 physicalid | `String`. 그 객체의 `id` |
| `FrameworkUtil.getRelIDfromPID(context, pid)` | `pid`: 관계의 physicalid | `String`. 그 관계의 `id` |

```java
String physicalId = FrameworkUtil.getPIDfromOID(context, objectId);
String sameId = FrameworkUtil.getOIDfromPID(context, physicalId);
MapList rows = DomainObject.getInfo(context, ids, StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_PHYSICAL_ID));
```

- 셋 다 MQL 을 한 번 실행합니다 (`print bus $1 select physicalid dump`, `print bus $1 select id dump`, `print connection $1 select id dump`). 실패하면 `FrameworkException` 입니다.
- id 여러 개를 바꾸거나 다른 값도 함께 읽으면 select 에 `DomainConstants.SELECT_PHYSICAL_ID` (`physicalid`) 를 넣어 [여러 객체를 한 번에](/jpo/api/read#여러-객체를-한-번에) 읽습니다. 서버에 한 번만 갑니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `FrameworkUtil.getOIDfromPIDifExist(context, pid)` | 실패하면 예외 대신 받은 `pid` 를 그대로 돌려줌. 없는 객체를 알아챌 수 없음. OOTB 0 건 |
| `DomainObject.newInstance(context, id).getInfo(context, DomainConstants.SELECT_PHYSICAL_ID)` | 같은 값을 읽지만 `newInstance(context, id)` 가 타입을 읽으려고 서버에 한 번 더 감 ([newInstance](/jpo/api/domain-object#newinstance)). 다른 값과 함께 읽을 때만 씀 |
| `FrameworkUtil.getPhysicalIdfromUUId(context, uuid)` | 사용자 그룹 URI 로 그룹 객체의 physicalid 를 찾는 앱 전용 메서드. 찾은 값을 `System.out` 에 찍음 |

::: info 근거
xerinfra.jar `FrameworkUtil.getPIDfromOID(Context, String)`: `MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", id, "physicalid")`, 예외는 `FrameworkException`. `getOIDfromPID` 는 같은 명령에 `id`, `getRelIDfromPID` 는 `print connection $1 select $2 dump` 에 `id`. `getOIDfromPIDifExist` 는 예외가 나면 받은 값을 돌려줌. `getPhysicalIdfromUUId` 는 `GroupProxyService.getGroupByURI(context, uuid, [id, physicalid])` 결과를 `System.out.println` 으로 찍고 physicalid (비면 id) 를 돌려줌. 서버에 가는 메서드라 로컬에서 실행하지 않음. OOTB `getPIDfromOID(` 5, `getPhysicalIdfromUUId(` 6, `getOIDfromPID(` 2, `getOIDfromPIDifExist(` 0, `getRelIDfromPID(` 0
:::

## 샘플 JPO

[XX_UtilArgs_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilArgs_mxJPO.java) 의 메서드입니다. 모든 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map (`programMap`) 에서 값을 읽습니다.

| 메서드 | 받는 값 (`programMap` 의 키) | 돌려주는 값 | 보여 주는 것 |
| --- | --- | --- | --- |
| `getArgs` | `objectId`, `mode` | `Map` (`objectId`, `hasObjectId`, `mode`). `mode` 가 비면 `"view"` | [넘어온 인자 꺼내기](#넘어온-인자-꺼내기) |
| `getChildIds` | `objectId` | `StringList` (자식 부품 id) | `countChildren` 이 부르는 메서드 |
| `countChildren` | `objectId` | `Integer` (자식 부품 수) | [다른 JPO 부르기](#다른-jpo-부르기). 같은 클래스의 `getChildIds` 를 `JPO.invoke` 로 부름 |
| `getSelectedObjectIds` | `emxTableRowId` (`String[]`) | `StringList` (고른 행의 objectId) | [objectId 꺼내기](#objectid-꺼내기) |
| `splitTableRowIds` | `emxTableRowId` (`String[]`) | `MapList` (행마다 `relId`, `objectId`, `parentId`, `rowPosition` 중 있는 것) | [관계 id, 부모 id 까지 꺼내기](#관계-id-부모-id-까지-꺼내기) |
| `isObjectId` | `value` | `Boolean`. 비면 `false` | [id 모양 확인](#id-모양-확인) |
| `toPhysicalId` | `objectId` | `String` (physicalid) | [physicalid 와 id 바꾸기](#physicalid-와-id-바꾸기) |
| `toObjectId` | `physicalId` | `String` (객체 id) | [physicalid 와 id 바꾸기](#physicalid-와-id-바꾸기) |

- 부르는 법은 [불러 보기](/jpo/api/#불러-보기) 와 같습니다. 프로그램 이름은 `XX_UtilArgs` 입니다.
- `splitTableRowIds`, `getSelectedObjectIds` 는 `emxTableRowId` 를 `String[]` 로 받는 경우입니다. `String` 하나로 오는 화면이면 `new String[] {값}` 으로 감싸 씁니다.
- `splitTableRowIds`, `getSelectedObjectIds` 는 Context 없이 로컬에서 실행해 결과를 확인했습니다. `"|1.2.3.4||0"` 은 `{objectId=1.2.3.4, parentId=, rowPosition=0}` 입니다.

::: details XX_UtilArgs_mxJPO.java

<<< @/../JPOSample/util/XX_UtilArgs_mxJPO.java

:::

::: warning
샘플의 관계 이름 (`relationship_MyPartStructure`) 은 예시입니다. 실제 스키마의 심볼릭 이름으로 바꿔야 합니다. 데이터를 바꾸는 메서드는 없습니다. 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다.
:::

## 다루지 않은 메서드

::: details 펼쳐 보기
| 클래스 | 메서드 | 이유 |
| --- | --- | --- |
| `JPO` | `getString` | 내부용 |
| `UIUtil` | `toQueryMap`, `toQueryString` | 화면용 |
| `FrameworkUtil` | `getObjectName` | 같은 일 다른 메서드 |
| `FrameworkUtil` | `getObjectNameWithRevision` | 화면용 |
| `FrameworkUtil` | `isNewBusObj` | 같은 일 다른 메서드 |
| `FrameworkUtil` | `validateMethodBeforeInvoke` (3) | 내부용 |
| `FrameworkUtil` | `invokeMethodInClass`, `invokeMethodInObject` | OOTB 미사용 |
:::
