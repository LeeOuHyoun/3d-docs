# MQL 실행, 환경 변수 <Badge type="info" text="원본 분석" />

JPO 안에서 MQL 명령을 실행하고, MQL 환경 변수 (RPE) 를 읽고 쓰는 방법입니다. 예제의 `part` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다. util 전체를 하고 싶은 일로 찾으려면 [유틸리티](/jpo/api/utilities) 를 봅니다. 언제 `DomainObject` 대신 MQL 을 쓰는지, 자주 쓰는 명령 목록 같은 배경은 가이드 [MqlUtil](/jpo/mql-util) 에 있습니다.

::: info 근거
xerinfra.jar (`MqlUtil`, `PropertyUtil`, `ContextUtil`, `FrameworkException`), eMatrixServletRMI.jar (`MQLCommand`, `MQLExecParams`, `com.matrixone.jdl.xml.bosMQLCommandImpl`) class 파일 (javap) 기준. 건수는 OOTB JPO 에서 센 호출 수: `MqlUtil.mqlCommand(` 2,266, `PropertyUtil.setGlobalRPEValue(` 87, `PropertyUtil.getGlobalRPEValue(` 77, `PropertyUtil.setRPEValue(` 36, `PropertyUtil.getRPEValue(` 35, `PropertyUtil.unsetRPEValue(` 2, `PropertyUtil.unsetGlobalRPEValue(` 2, `MQLCommand.exec(` 2, `MqlUtil.getMQL(` 0, `MqlUtil.mqlCommandStream(` 0
:::

샘플: [XX_UtilMql_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilMql_mxJPO.java) ([메서드별 설명](#샘플-jpo))

## 한눈에 보기

| 하고 싶은 일 | 쓸 메서드 |
| --- | --- |
| MQL 명령 실행하기 (값은 `$1` 로) | [`MqlUtil.mqlCommand(context, cmd, values...)`](#mql-명령-실행하기) |
| 값 개수가 실행 때 정해지는 명령 실행하기 | [`MqlUtil.mqlCommand(context, cmd, list)`](#mql-명령-실행하기) |
| 값이 없는 명령 실행하기 | [`MqlUtil.mqlCommand(context, cmd, false, false)`](#값이-없는-명령-실행하기) |
| 명령 하나만 슈퍼유저로 실행하기 | [`MqlUtil.mqlCommand(context, cmd, true, values...)`](#명령-하나만-슈퍼유저로-실행하기) |
| 트리거, 히스토리 없이 명령 하나 실행하기 | [`MqlUtil.mqlCommand(context, historyOff, triggerOff, cmd, push, values...)`](#트리거-히스토리-없이-실행하기) |
| 처음 보는 `mqlCommand` 형태 읽기 | [mqlCommand 13 가지 형태](#mqlcommand-13-가지-형태) |
| 사용자에게 알림, 경고, 오류 문구 띄우기 | [`MqlUtil.mqlCommand(context, "notice $1", msg)`](#사용자에게-메시지-띄우기) |
| 환경 변수 (RPE) 읽기 | [`PropertyUtil.getRPEValue(context, name, global)`](#환경-변수-읽고-쓰기) |
| 환경 변수 쓰기 | [`PropertyUtil.setRPEValue(context, name, value, global)`](#환경-변수-읽고-쓰기) |
| 환경 변수 지우기 | [`PropertyUtil.unsetRPEValue(context, name, global)`](#환경-변수-읽고-쓰기) |

## MQL 명령 실행하기

`DomainObject` 에 없는 일 (관계 하나의 정보, 정책, 역할 목록 ...) 을 하거나 결과를 문자열로 바로 받고 싶을 때 씁니다. 명령 안의 `$1`, `$2` ... 자리에 뒤의 값이 차례로 들어갑니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `MqlUtil.mqlCommand(context, cmd, values...)` | `cmd`: `$1`, `$2` ... 를 넣은 MQL 명령. `values`: `$1` 부터 차례로 들어갈 값 | `String`. 명령이 출력한 글자에서 마지막 한 글자를 뺀 것. 출력이 없으면 `""` |
| `MqlUtil.mqlCommand(context, cmd, list)` | `list`: 값을 담은 `List<String>`. 값 개수가 실행 때 정해질 때 | 위와 같음 |

| 호출 (예시) | 결과 (예시) |
| --- | --- |
| `mqlCommand(context, "print bus $1 select $2 dump", objectId, "current")` | `Release` |
| `mqlCommand(context, "print bus $1 select $2 $3 dump $4", objectId, "name", "current", "\|")` | `P-0001\|Release` |

위 결과는 값의 모양을 보이려는 예시입니다. 서버에서 실행한 결과가 아닙니다.

```java
String current = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", objectId, "current");

String both = MqlUtil.mqlCommand(context, "print bus $1 select $2 $3 dump $4", objectId, "name", "current", "|");
StringList nameAndCurrent = FrameworkUtil.split(both, "|");

List<String> values = new ArrayList<String>();
values.add(objectId);
values.add("owner");
String owner = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", values);
```

- 값은 명령 문자열에 이어 붙이지 않고 `$1` 로 넘깁니다. 값은 명령과 따로 서버에 넘어갑니다. 이어 붙이면 이름의 공백이나 따옴표 때문에 명령이 깨지고 MQL 인젝션 위험이 있습니다. OOTB 도 2011 년 무렵 이어 붙인 명령을 이 형태로 고쳤습니다 (`Mql Injection:parameterized Mql` 주석 88 곳).
- 결과는 마지막 한 글자를 잘라서 돌려줍니다. MQL 이 끝에 붙이는 줄바꿈을 빼려는 것인데, 그 글자가 줄바꿈인지는 보지 않습니다.
- 결과가 여러 줄 (`temp query bus`, `list role` ...) 이면 줄 사이의 `\n` 은 남습니다. 줄바꿈으로 자른 뒤 다시 구분자로 자릅니다 ([문자열, 목록](/jpo/api/util/string-list)).
- 명령이 실패하면 `FrameworkException` 이 납니다. `getMessage()` 는 MQL 오류 문구에서 마지막 한 글자를 뺀 것입니다.
- 명령마다 서버에 한 번 갑니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `MqlUtil.mqlCommand(context, cmd)` <Badge type="warning" text="폐기" /> | 값 자리 없이 명령 문자열만 받아 값을 이어 붙이게 됨. 값이 없는 명령은 [값이 없는 명령 실행하기](#값이-없는-명령-실행하기) |
| `MqlUtil.mqlCommand(context, mql, cmd)` <Badge type="warning" text="폐기" />, `MqlUtil.getMQL(context)` <Badge type="warning" text="폐기" /> | `getMQL` 은 `new MQLCommand()` 를 돌려줄 뿐이고, 그 객체를 넘겨도 하는 일은 `mqlCommand(context, cmd, false, false)` 와 같음 |
| `new MQLCommand()` + `mql.executeCommand(context, cmd, values...)` + `mql.getResult()` | `executeCommand`, `getResult`, `getError` 가 모두 폐기. 성공 여부를 `boolean` 으로 받아 오류를 직접 꺼내야 하고 결과 끝 줄바꿈도 남음. `MqlUtil` 이 안에서 이렇게 부름 |
| `MQLCommand.exec(context, cmd, values...)` (eMatrixServletRMI.jar) | OOTB JPO 2 건. `MatrixException` 을 던지고 슈퍼유저 실행 인자가 없음. 결과가 줄바꿈으로 끝날 때만 자름 |
| `MqlUtil.mqlCommandStream(context, cmd)`, `mqlCommandStream(context, cmd, value, values...)` | 결과를 `Reader` 로 받음. OOTB 0 건. 실패해도 예외 없이 `null` 을 돌려줌 |

::: info 근거
`mqlCommand(Context, String, String...)` → `mqlCommand(context, cmd, false, values)` → `mqlCommand(context, false, cmd, false, values)` → private `mqlCommand(Context, String, boolean, boolean, boolean, String...)`. 이 메서드는 `new MQLCommand().executeCommand(context, historyOff, triggerOff, cmd, values)` 로 값을 명령과 따로 넘기고 (→ `bosMQLCommand.executeCmdWithFormat(session, ..., cmd, values)`), 실패하면 `getError()`, 성공하면 `getResult()` 를 받아 길이가 0 보다 크면 `substring(0, length - 1)`. 오류는 `new FrameworkException(오류문구)` 를 같은 메서드의 `catch (Exception)` 이 `new FrameworkException(e)` 로 한 번 더 감쌈. 감싸도 `getMessage()` 는 안쪽 문구와 같음 (로컬 실행으로 확인: `new FrameworkException(new FrameworkException("Error: #1900068: ..."))` 의 `getMessage()`, `toString()` 이 안쪽 문구 그대로). `mqlCommand(Context, String, List)` 는 `list.toArray(new String[0])` 로 같은 길을 감. `MQLCommand.getResult()` 는 서버가 준 결과 조각을 이어 붙이기만 함 (`processResults`). `MQLCommand.exec` → `MQLExecParams.exec`: 값이 없으면 `executeCommand(context, cmd, historyOff, triggerOff)`, 있으면 format 형태로 실행하고 결과가 줄바꿈으로 끝날 때만 그만큼 자름. `mqlCommandStream` 은 `MQLCommand.runCommandStream` 이 `MatrixException` 을 던지면 `new FrameworkException(e)` 를 만들기만 하고 (`throw` 없음) `null` 을 돌려줌. `getMQL` 은 `return new MQLCommand()`. OOTB 예: emxControlledFolderBase_mxJPO (`Mql Injection:parameterized Mql` 주석)
:::

## 값이 없는 명령 실행하기

`print transaction`, `trigger off` 처럼 값이 없는 명령도 있습니다. 이때 `MqlUtil.mqlCommand(context, "print transaction")` 로 쓰면 폐기된 `mqlCommand(Context, String)` 가 불립니다. 같은 일을 하는 폐기 아닌 형태는 `mqlCommand(context, cmd, false, false)` 입니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `MqlUtil.mqlCommand(context, cmd, false, false)` | `cmd`: 값 자리 (`$1`) 가 없는 명령. 첫 `false`: 슈퍼유저로 실행하지 않음 (`true` 면 슈퍼유저). 두 번째 `false`: 서버에 `allowMultipleOverride` 로 넘어가는 값 | `String`. [MQL 명령 실행하기](#mql-명령-실행하기) 와 같음 |

```java
String transaction = MqlUtil.mqlCommand(context, "print transaction", false, false);
```

- 폐기된 `mqlCommand(context, cmd)` 와 이 형태는 안에서 같은 메서드를 같은 값으로 부릅니다. 결과도, 실패할 때도 같습니다.
- 두 번째 값의 뜻은 서버로 넘기는 이름 (`allowMultipleOverride`, 한 문자열의 여러 명령 허용으로 보임) 으로만 확인했습니다 <Badge type="danger" text="검증 필요" />. 보통 `false` 를 넣습니다.
- 값이 있는 명령에는 쓰지 않습니다. 값을 이어 붙이게 됩니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `MqlUtil.mqlCommand(context, cmd)` <Badge type="warning" text="폐기" /> | 폐기. 안에서 `mqlCommand(context, cmd, false, false)` 와 같은 일을 함 |
| `MqlUtil.mqlCommand(context, cmd, true)` <Badge type="warning" text="폐기" /> | 폐기. `mqlCommand(context, cmd, true, false)` 와 같은 일을 함 |
| `MQLCommand.exec(context, cmd)` (eMatrixServletRMI.jar) | OOTB JPO 에서 거의 쓰지 않음 (2 건). `MatrixException` 을 던짐 |

::: info 근거
`mqlCommand(Context, String)` → `mqlCommand(context, cmd, false)` → `mqlCommand(context, new MQLCommand(), cmd, false)` → private `mqlCommand(Context, MQLCommand, String, boolean push, boolean, boolean)` 에 `(false, false, false)`. `mqlCommand(Context, String, boolean, boolean)` 은 같은 private 메서드에 `(첫 값, 두 번째 값, false)`. private 메서드는 `executeCommand(context, cmd, 두 번째 값, 세 번째 값, false)` → `bosMQLCommand.executeCmd(session, cmd, allowMultipleOverride, historyOff, triggerOff)` (이름은 `com.matrixone.jdl.xml.bosMQLCommandImpl` 이 XML 로 보낼 때 쓰는 요소 이름). xerinfra.jar `ContextUtil.isTransactionActive` 도 `print transaction` 을 폐기 형태 `mqlCommand(Context, String)` 로 부름. OOTB 의 폐기 형태 `(context, cmd)` 는 185 건, 그중 따옴표 안 상수 명령이 59 건 (`"verb on"`, `"history on;"`, `"get env EVENT"` ...), `(context, cmd, true)` 는 92 건 (`"trigger off"` 16, `"trigger on"` 15 ...)
:::

## 명령 하나만 슈퍼유저로 실행하기

로그인 사용자에게 권한이 없는 명령 하나만 User Agent 로 실행합니다. 명령 문자열 바로 뒤에 `true` 를 넣습니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `MqlUtil.mqlCommand(context, cmd, true, values...)` | `true`: 실행 전에 `ContextUtil.pushContext(context)`, 끝나면 (실패해도) `ContextUtil.popContext(context)`. `false` 면 지금 사용자 | `String`. [MQL 명령 실행하기](#mql-명령-실행하기) 와 같음 |

```java
String owner = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", true, objectId, "owner");
```

- 그 명령 하나만 권한 검사를 건너뜁니다. 사용자 권한으로 해야 하는 수정 (`modify bus ...`) 에 습관처럼 넣지 않습니다. OOTB 에는 이 형태가 350 건 있어 따라 쓰기 쉽습니다.
- 값을 `List` 로 넘기는 `mqlCommand(context, cmd, true, list)` 도 같은 일을 합니다.
- 명령 여러 개를 슈퍼유저로 하려면 `ContextUtil.pushContext` 로 감쌉니다 ([트랜잭션, 권한 전환](/jpo/api/util/transaction)).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `MqlUtil.mqlCommand(context, cmd, true)` <Badge type="warning" text="폐기" /> | 폐기. 값 자리가 없는 형태. 같은 일은 `mqlCommand(context, cmd, true, false)` |
| `MqlUtil.mqlCommand(context, mql, cmd, true)` <Badge type="warning" text="폐기" /> | 폐기. `MQLCommand` 객체를 받을 뿐 `mqlCommand(context, cmd, true, false)` 와 같음 |
| `ContextUtil.pushContext(context)` + `mqlCommand(context, cmd, values...)` + `finally` 의 `popContext(context)` | 명령 하나에는 길기만 함. `true` 형태가 안에서 이렇게 함 |

::: info 근거
`mqlCommand(Context, String, boolean, String...)` → `mqlCommand(context, false, cmd, push, values)` → private 메서드: push 가 참이면 `ContextUtil.pushContext(context)` 를 부르고 finally 에서 `ContextUtil.popContext(context)`. push 자체가 실패해도 `FrameworkException` 으로 감쌈. `ContextUtil.pushContext(context)` 는 `person_UserAgent` 로 바꿈. OOTB 인자 모양: `(context, cmd, true, 값...)` 350 건, `(context, cmd, false, 값...)` 23 건
:::

## 트리거, 히스토리 없이 실행하기

명령 하나를 트리거나 히스토리 기록 없이 실행하라고 서버에 알립니다. 명령 문자열 **앞** 에 boolean 두 개를 넣습니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `MqlUtil.mqlCommand(context, historyOff, triggerOff, cmd, push, values...)` | `historyOff`: `true` 면 히스토리를 남기지 말라고 넘김. `triggerOff`: `true` 면 트리거를 돌리지 말라고 넘김. `push`: 명령 뒤의 boolean. 슈퍼유저로 실행할지 | `String`. [MQL 명령 실행하기](#mql-명령-실행하기) 와 같음 |

```java
MqlUtil.mqlCommand(context, false, true, "modify bus $1 $2 $3", false, objectId, attrStatus, "Approved");
```

- 두 값은 그 명령을 실행할 때 서버에 `historyOff`, `triggerOff` 로 넘어갑니다. 그 명령에만 적용되는지는 서버에서 확인하지 않았습니다 <Badge type="danger" text="검증 필요" />.
- 세션 명령 `trigger off` / `trigger on` 으로 끄는 방법도 있습니다 (OOTB 50 / 51 건). 그때는 [가이드](/jpo/mql-util#트리거-히스토리-끄기) 처럼 꼭 `finally` 에서 다시 켭니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `MqlUtil.mqlCommand(context, historyOff, cmd, push, values...)` | 트리거 값을 받지 않음. 위 형태의 `triggerOff` 에 `false` 를 넣은 것과 같음 |
| `MqlUtil.mqlCommand(context, historyOff, cmd, push, list)` | 위와 같은데 값이 `List` |
| `MqlUtil.mqlCommand(context, historyOff, cmd, push)` | 값 자리가 없는 형태. 트리거 값을 받지 않음 |
| `mqlCommand(context, "trigger off", false, false)` ... `"trigger on"` | 끄고 켜는 두 명령을 `finally` 로 짝 맞춰야 함 |

::: info 근거
`mqlCommand(Context, boolean, boolean, String, boolean, String...)` 은 private 메서드에 `(cmd, push = 네 번째 값, historyOff = 첫째, triggerOff = 둘째, values)` 를 넘김. private 메서드는 `MQLCommand.executeCommand(context, historyOff, triggerOff, cmd, values)` → `executeCommand(context, historyOff, triggerOff, false, cmd, values)` → `bosMQLCommand.executeCmdWithFormat(session, historyOff, triggerOff, resultToMyCaller, cmd, values)` (이름은 `com.matrixone.jdl.xml.bosMQLCommandImpl` 이 보내는 XML 요소 이름). `mqlCommand(Context, boolean, String, boolean, String...)` 은 `triggerOff` 자리에 `false`. `mqlCommand(Context, boolean, String, boolean)` 은 폐기된 private 메서드에 `historyOff` 만 넘김. OOTB 인자 모양: `(context, 참/거짓, 참/거짓, cmd, 참/거짓, 값...)` 60 건 (그중 `true, true, cmd, true` 49 건), `(context, true, cmd, true, 값...)` 8 건
:::

## mqlCommand 13 가지 형태

`mqlCommand` 는 public 형태가 13 개입니다. boolean 의 뜻은 자리로 정해집니다.

- 명령 문자열 **바로 뒤** 의 boolean: 슈퍼유저로 실행할지 (`push`)
- 명령 문자열 **앞** 의 boolean: 첫째 히스토리 끄기 (`historyOff`), 둘째 트리거 끄기 (`triggerOff`)
- 값 자리가 없는 `(context, cmd, push, b)` 의 두 번째 boolean: `allowMultipleOverride`

| 형태 | 이 페이지에서 |
| --- | --- |
| `mqlCommand(context, cmd, values...)` | [쓸 메서드](#mql-명령-실행하기) |
| `mqlCommand(context, cmd, list)` | [쓸 메서드](#mql-명령-실행하기). 값이 `List` |
| `mqlCommand(context, cmd, push, values...)` | [쓸 메서드](#명령-하나만-슈퍼유저로-실행하기) |
| `mqlCommand(context, cmd, push, list)` | 위와 같음. 값이 `List` |
| `mqlCommand(context, historyOff, triggerOff, cmd, push, values...)` | [쓸 메서드](#트리거-히스토리-없이-실행하기) |
| `mqlCommand(context, historyOff, cmd, push, values...)` | 위 형태의 `triggerOff` 가 `false` |
| `mqlCommand(context, historyOff, cmd, push, list)` | 위와 같음. 값이 `List` |
| `mqlCommand(context, cmd, push, allowMultipleOverride)` | [값이 없는 명령](#값이-없는-명령-실행하기) |
| `mqlCommand(context, historyOff, cmd, push)` | 값이 없는 명령 + 히스토리 끄기 |
| `mqlCommand(context, cmd)` <Badge type="warning" text="폐기" /> | `mqlCommand(context, cmd, false, false)` 와 같음 |
| `mqlCommand(context, cmd, push)` <Badge type="warning" text="폐기" /> | `mqlCommand(context, cmd, push, false)` 와 같음 |
| `mqlCommand(context, mql, cmd)` <Badge type="warning" text="폐기" /> | `MQLCommand` 객체를 받음. `mqlCommand(context, cmd, false, false)` 와 같음 |
| `mqlCommand(context, mql, cmd, push)` <Badge type="warning" text="폐기" /> | `mqlCommand(context, cmd, push, false)` 와 같음 |

- `mqlCommand(context, "trigger off", true)` 처럼 값 없이 `true` 하나만 넣으면 폐기 형태가 불립니다. Java 는 가변 인자 (`values...`) 형태보다 인자 수가 딱 맞는 형태를 먼저 고릅니다.
- 값 자리가 있는 형태 (`values...`, `list`) 는 값을 명령과 따로 서버에 넘기고, 값 자리가 없는 형태는 명령 문자열을 그대로 넘깁니다.

::: info 근거
xerinfra.jar `MqlUtil` 의 public `mqlCommand` 13 개 중 `Deprecated: true` 는 `(Context, String)`, `(Context, MQLCommand, String)`, `(Context, String, boolean)`, `(Context, MQLCommand, String, boolean)` 4 개. 값 자리가 없는 형태는 모두 폐기된 private `mqlCommand(Context, MQLCommand, String, boolean, boolean, boolean)` (→ `executeCmd`) 로, 값 자리가 있는 형태는 모두 private `mqlCommand(Context, String, boolean, boolean, boolean, String...)` (→ `executeCmdWithFormat`) 로 모임
:::

## 사용자에게 메시지 띄우기

트리거나 화면에서 부른 JPO 가 사용자에게 알림, 경고, 오류 문구를 보여 줄 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `MqlUtil.mqlCommand(context, "notice $1", msg)` | `msg`: 보여 줄 문구. 경고는 `"warning $1"`, 오류는 `"error $1"` | `String` |

```java
MqlUtil.mqlCommand(context, "notice $1", "My Status must be Approved.");
```

- 문구는 바로 뜨지 않고 context 의 클라이언트 작업 목록에 쌓입니다. 화면 JSP 가 요청이 끝날 때 꺼내 보여 줍니다 ([가이드](/jpo/mql-util#사용자에게-메시지-보내기)).
- 트리거에서 진행을 막을 때는 `notice` 로 이유를 남기고 `1` 을 돌려줍니다 ([트리거](/trigger/)).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `emxContextUtil_mxJPO.mqlNotice(context, msg)` | 다른 JPO 의 static 메서드. 안에서 `MqlUtil.mqlCommand(context, "notice $1", msg)` 를 그대로 부름 |
| `emxContextUtil_mxJPO.mqlWarning(context, msg)`, `mqlError(context, msg)` | 위와 같이 `"warning $1"`, `"error $1"` 을 그대로 부름 |

::: info 근거
emxContextUtilBase_mxJPO.java (`mqlNotice`, `mqlWarning`, `mqlError` 가 `MqlUtil.mqlCommand` 로 `"notice $1"`, `"warning $1"`, `"error $1"` 실행). eMatrixServletRMI.jar `MQLCommand.processResults`: 서버 결과의 `tasks` 를 `context.addClientTasks(new ClientTaskList(tasks))` 로 context 에 넣음. 건수: 문자열 `"notice $1"` 128, `emxContextUtil_mxJPO.mqlNotice(` 229, `emxContextUtilBase_mxJPO.mqlNotice(` 76
:::

## 환경 변수 읽고 쓰기

MQL 환경 변수 (RPE) 는 한 요청 안의 JPO, 트리거, MQL 프로그램이 문자열 값을 주고받는 곳입니다. 다른 코드에 신호를 보낼 때 (`MX_SKIP_PART_PROMOTE_CHECK` 를 `true` 로 두고 트리거가 읽음) 와 트리거 매크로 (`EVENT`, `OBJECTID` ...) 를 읽을 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PropertyUtil.getRPEValue(context, name, global)` | `name`: 변수 이름. `global`: `true` 면 global 변수 | `String`. MQL `get env` 의 결과 ([MQL 명령 실행하기](#mql-명령-실행하기) 처럼 마지막 한 글자를 뺀 것) |
| `PropertyUtil.setRPEValue(context, name, value, global)` | `value`: 넣을 값. `null` 이나 `""` 이면 아무것도 하지 않음 | `void` |
| `PropertyUtil.unsetRPEValue(context, name, global)` | `name`, `global` | `void` |

| 호출 | 실행하는 MQL |
| --- | --- |
| `getRPEValue(context, name, false)` / `(…, true)` | `get env $1` / `get env global $1` |
| `setRPEValue(context, name, value, false)` / `(…, true)` | `set env $1 $2` / `set env global $1 $2` |
| `unsetRPEValue(context, name, false)` / `(…, true)` | `unset env $1` / `unset env global $1` |

```java
PropertyUtil.setRPEValue(context, "XX_SKIP_STATUS_CHECK", "true", false);
try {
    part.setAttributeValue(context, attrStatus, "Approved");
} finally {
    PropertyUtil.unsetRPEValue(context, "XX_SKIP_STATUS_CHECK", false);
}

String skip = PropertyUtil.getRPEValue(context, "XX_SKIP_STATUS_CHECK", false);
String event = PropertyUtil.getRPEValue(context, "EVENT", false);
```

- `global` 이 `true` 면 MQL 명령에 `global` 을 붙입니다. OOTB 는 프로그램이 부른 쪽에 값을 돌려줄 때 (주석 `return value through global RPE`) 와, `pushContext` 가 바꾸기 전 사용자 이름 (`MX_LOGGED_IN_USER_NAME`) 을 남길 때 global 을 씁니다. global 이 아닌 변수가 어디까지 보이는지 (그 프로그램 안, 요청 전체) 는 서버에서 확인하지 않았습니다 <Badge type="danger" text="검증 필요" />. 쓸 때와 읽을 때 같은 `global` 값을 줍니다.
- 위 예제처럼 쓰는 쪽은 `finally` 에서 지우고, 읽는 쪽 (트리거) 은 `"true".equalsIgnoreCase(값)` 으로 봅니다. OOTB 의 `MX_SKIP_PART_PROMOTE_CHECK` 가 이 모양입니다 (global 아님).
- `setRPEValue` 에 `""` 를 넣어도 지워지지 않고 앞 값이 남습니다. 지울 때는 `unsetRPEValue` 를 씁니다. OOTB 에도 `""` 로 지우려다 아무 일도 하지 않는 곳이 있습니다 (emxTaskBase_mxJPO, `updateConstraintDate_NoticeGiven`).
- 설정하지 않은 이름을 읽어도 오류는 나지 않는 것으로 보입니다 (OOTB 트리거가 평소에는 설정되지 않은 이름을 이 방법으로 읽음). 그때 돌려받는 값 (`""` 인지) 은 서버에서 확인하지 않았습니다 <Badge type="danger" text="검증 필요" />.
- 값은 문자열뿐입니다. 읽기, 쓰기, 지우기마다 서버에 한 번 갑니다.
- 서버 설정값 (`MATRIXINSTALL` 같은 ini 변수) 은 RPE 가 아닙니다 ([이름 채번, 캐시, 로그, 서버 환경](/jpo/api/util/etc)).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `PropertyUtil.getGlobalRPEValue(context, name)` | 안에서 `getRPEValue(context, name, true)` 를 그대로 부름 |
| `PropertyUtil.setGlobalRPEValue(context, name, value)` | 안에서 `setRPEValue(context, name, value, true)` 를 그대로 부름 |
| `PropertyUtil.unsetGlobalRPEValue(context, name)` | 안에서 `unsetRPEValue(context, name, true)` 를 그대로 부름 |
| `MqlUtil.mqlCommand(context, "get env $1", name)`, `"set env $1 $2"`, `"unset env $1"` (`global` 을 붙인 것 포함) | `PropertyUtil` 이 안에서 실행하는 명령 그대로. `global` 과 이름, 값 순서를 문자열로 맞춰야 함 (OOTB emxECRBase_mxJPO 에 이름과 값이 바뀐 `set env global $1 $2` 호출이 있음) |

::: info 근거
xerinfra.jar `PropertyUtil.getRPEValue(Context, String, boolean)`: 참이면 `MqlUtil.mqlCommand(context, "get env global $1", name)`, 아니면 `"get env $1"` 의 결과를 그대로 돌려줌. `setRPEValue`: 값이 `null` 이 아니고 `""` 와 다를 때만 `"set env global $1 $2"` / `"set env $1 $2"`. `unsetRPEValue`: `"unset env global $1"` / `"unset env $1"`. `getGlobalRPEValue`, `setGlobalRPEValue`, `unsetGlobalRPEValue` 는 같은 메서드에 `true`. 모두 예외를 `new FrameworkException(e)` 로 감쌈. global 사용 예: emxCPNProductDataBase_mxJPO (`"set env global emxOrganization $1"`, 주석 `return value through global RPE`). 신호 예: emxECOBase_mxJPO (`setRPEValue(context, "MX_SKIP_PART_PROMOTE_CHECK", "true", false)`), emxECPartBase_mxJPO 의 트리거 `ensureECOConnected` (`getRPEValue(context, "MX_SKIP_PART_PROMOTE_CHECK", false)`). 트리거 매크로 읽기: emxCommonDocumentBase_mxJPO (`MqlUtil.mqlCommand(context, "get env $1", "EVENT")`). 건수: 문자열 `"get env $1"` 62, `"set env global $1 $2"` 16, `"unset env $1"` 12, `"set env $1 $2"` 10
:::

## 샘플 JPO

[XX_UtilMql_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilMql_mxJPO.java) 의 메서드입니다. `checkStatus` 를 뺀 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map (`programMap`) 에서 값을 읽습니다.

| 메서드 | 받는 값 (`programMap` 의 키) | 돌려주는 값 | 보여 주는 것 |
| --- | --- | --- | --- |
| `getCurrentState` | `objectId` | `String` (현재 상태) | `$1` 로 값 넘기기 ([MQL 명령 실행하기](#mql-명령-실행하기)) |
| `getNameAndState` | `objectId` | `Map` (`name`, `current`) | `dump $4` 의 구분자로 여러 값 받아 자르기 ([MQL 명령 실행하기](#mql-명령-실행하기)) |
| `getSelectValues` | `objectId`, `selects` (쉼표로 이은 select. 값이 하나인 select 만) | `Map` (select → 값) | 값 개수가 실행 때 정해질 때 `List` 로 넘기기 ([MQL 명령 실행하기](#mql-명령-실행하기)) |
| `getTransactionStatus` | 없음 | `String` (`print transaction` 결과) | 값이 없는 명령 ([값이 없는 명령 실행하기](#값이-없는-명령-실행하기)) |
| `getOwnerAsSystem` | `objectId` | `String` (소유자) | 명령 하나만 슈퍼유저로 ([명령 하나만 슈퍼유저로 실행하기](#명령-하나만-슈퍼유저로-실행하기)) |
| `setStatusWithoutTrigger` | `objectId`, `status` | 없음 | **데이터를 바꿈.** 트리거 없이 속성 바꾸기 ([트리거, 히스토리 없이 실행하기](#트리거-히스토리-없이-실행하기)) |
| `setStatusWithFlag` | `objectId`, `status` | 없음 | **데이터를 바꿈.** 환경 변수로 트리거에 신호를 주고 `finally` 에서 지우기 ([환경 변수 읽고 쓰기](#환경-변수-읽고-쓰기)) |
| `checkStatus` | 없음 (My Status 속성 수정 check 트리거로 붙임) | `int` (`0` 통과, `1` 막음) | 환경 변수 읽기, `notice` 로 막는 이유 남기기 ([사용자에게 메시지 띄우기](#사용자에게-메시지-띄우기)) |

::: details XX_UtilMql_mxJPO.java

<<< @/../JPOSample/util/XX_UtilMql_mxJPO.java

:::

::: warning
샘플의 타입, 관계, 속성, 환경 변수 이름은 예시입니다. 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다.
:::

## 다루지 않은 메서드

::: details 펼쳐 보기
| 클래스 | 메서드 | 이유 |
| --- | --- | --- |
| `MQLCommand` (eMatrixServletRMI.jar) | `execList`, `lExec`, `lExecList`, `execWithBind`, `execWithBindList`, `execWithStreaming`, `execWithArgs`, `execWithArgsExpanded`, `runCommandStream`, `stringToMyCaller`, `buildSpec`, `getKernelDateString`, `open`, `close`, `setResult`, `setError` | OOTB 미사용 |
:::
