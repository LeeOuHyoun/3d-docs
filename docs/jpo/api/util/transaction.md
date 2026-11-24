# 트랜잭션, 권한 전환 <Badge type="info" text="원본 분석" />

여러 변경을 한 트랜잭션으로 묶고, 로그인 사용자에게 권한이 없는 작업을 잠깐 시스템 사용자 (User Agent) 로 실행하는 방법입니다. 예제의 `part` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다. util 전체를 하고 싶은 일로 찾으려면 [유틸리티](/jpo/api/utilities) 를 봅니다. Context 에서 꺼내는 값, 화면이 열어 주는 트랜잭션 같은 배경은 가이드 [Context, 트랜잭션, 권한 전환](/jpo/transaction-context) 에 있습니다.

::: info 근거
xerinfra.jar (`ContextUtil`, `DomainObject`, `PropertyUtil`, `MqlUtil`, `UIUtil`), eMatrixServletRMI.jar (`matrix.db.Context`, `JPOSupport`, `ContextHolder`) class 파일 (javap) 기준. 건수는 OOTB JPO 에서 센 호출 수: `ContextUtil.popContext(` 686, `ContextUtil.pushContext(` 659, `ContextUtil.abortTransaction(` 315, `ContextUtil.commitTransaction(` 306, `ContextUtil.startTransaction(` 296, `ContextUtil.isTransactionActive(` 11, `ContextUtil.getAnonymousContext(` 7, `ContextUtil.restoreContext(` 3, `ContextUtil.setAttribute(` 2, `ContextUtil.setSavePoint(` 1, `ContextUtil.abortSavePoint(` 1, `ContextUtil.runInAnonymousContext(` 0, `ContextUtil.getAttribute(` 0, `ContextUtil.removeAttribute(` 0, `ContextUtil.clearAgentCache(` 0
:::

샘플: [XX_UtilTransaction_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilTransaction_mxJPO.java) ([메서드별 설명](#샘플-jpo))

## 한눈에 보기

| 하고 싶은 일 | 쓸 메서드 |
| --- | --- |
| 여러 변경을 한 트랜잭션으로 묶기 | [`ContextUtil.startTransaction(context, true)`, `commitTransaction`, `abortTransaction`](#여러-변경을-한-트랜잭션으로-묶기) |
| 트랜잭션 중인지 확인하기 | [`ContextUtil.isTransactionActive(context)`](#트랜잭션-중인지-확인하기) |
| 트랜잭션 안에서 일부만 되돌리기 | [`ContextUtil.setSavePoint(context, name)`, `abortSavePoint`](#트랜잭션-안에서-일부만-되돌리기) |
| 권한 없는 작업을 시스템 사용자로 하기 | [`ContextUtil.pushContext(context)`, `popContext`](#권한-없이-시스템으로-바꾸기) |
| 정해 둔 다른 사용자로 바꿔 실행하기 | [`ContextUtil.pushContext(context, user, null, null)`](#다른-사용자로-바꾸기) |
| 시스템으로 바꾼 뒤 실제 사용자 알아내기 | [`PropertyUtil.getRPEValue(context, ContextUtil.MX_LOGGED_IN_USER_NAME, true)`](#바꾸기-전-사용자-알아내기) |
| context 를 받지 못한 코드에서 실행하기 | [`ContextUtil.runInAnonymousContext(callable)`](#context-없이-실행하기) |

## 여러 변경을 한 트랜잭션으로 묶기

값을 둘 이상 바꾸거나, 만들고 연결하는 것처럼 여러 변경이 함께 성공하거나 함께 취소되어야 할 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `ContextUtil.startTransaction(context, true)` | `true`: 쓰기 트랜잭션. `false`: 읽기 전용 | `boolean`. 시작했거나 횟수를 올렸으면 `true`. `ContextUtil` 밖에서 연 트랜잭션이 이미 있어 아무것도 안 했으면 `false` |
| `ContextUtil.commitTransaction(context)` | 없음 | `void`. 가장 바깥에서 부른 한 번만 실제로 확정 |
| `ContextUtil.abortTransaction(context)` | 없음 | `void`. 가장 바깥에서 부른 한 번만 실제로 되돌림. 예외를 던지지 않음 |

```java
ContextUtil.startTransaction(context, true);
try {
    part.setAttributeValue(context, attrStatus, "Approved");
    doc.setAttributeValue(context, attrStatus, "Approved");
    ContextUtil.commitTransaction(context);
} catch (Exception e) {
    ContextUtil.abortTransaction(context);
    throw e;
}
```

`ContextUtil` 은 트랜잭션을 몇 번 열었는지 세어 두고, 가장 바깥 한 쌍만 서버에 보냅니다.

| 상황 | `startTransaction` | `commitTransaction` | `abortTransaction` |
| --- | --- | --- | --- |
| 트랜잭션 없음 | 서버에서 시작, 횟수 1 | 횟수 1 → 0, 서버에서 확정 | 횟수 1 → 0, 서버에서 되돌림 |
| `ContextUtil` 로 연 트랜잭션 안 (횟수 n) | 횟수만 n+1 | 횟수만 1 줄임 | 횟수만 1 줄임 |
| `ContextUtil` 밖에서 연 트랜잭션 안 (횟수 0) | 아무것도 안 하고 `false` | 아무것도 안 함 | 아무것도 안 함 |

- 예외가 나면 `catch` 에서 `abortTransaction` 을 부르고 예외를 **다시 던집니다**. 안쪽의 abort 는 횟수만 줄이므로, 바깥이 예외를 받아 abort 해야 실제로 되돌아갑니다. 바깥에서 예외를 삼키고 commit 하면 안쪽 변경까지 확정될 수 있습니다.
- 가장 바깥 `commitTransaction` 은 먼저 `print transaction` 으로 쓰기 트랜잭션이 되돌리는 중 (`aborting`) 인지 봅니다. 그렇다면 `Failed to commit` (`emxFramework.ContextUtil.ErrorCommit`) 문구로 `FrameworkException` 을 던지고 **횟수를 줄이지 않습니다**. 위 예제처럼 `catch` 에서 `abortTransaction` 을 불러야 횟수가 0 이 되고 실제로 되돌아갑니다.
- 이미 열린 트랜잭션 안에서는 `true` / `false` 를 다시 보지 않습니다. 바깥이 읽기 전용 (`false`) 이면 안쪽에서 `true` 로 불러도 횟수만 올라갑니다.
- 횟수는 그 `Context` 객체에 붙어 있습니다. 같은 `context` 로 시작하고 끝냅니다.
- 화면 처리 JSP 나 트리거처럼 이미 트랜잭션이 열린 상태에서 JPO 가 불리는 경우는 [가이드](/jpo/transaction-context#ui-가-열어-주는-트랜잭션) 를 봅니다.
- 서버에 가는 것: 가장 바깥 `startTransaction` 은 `print transaction` 한두 번과 시작, 가장 바깥 `commitTransaction` 은 `print transaction` 한 번과 확정, 가장 바깥 `abortTransaction` 은 되돌리기. 안쪽 호출은 서버에 가지 않습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| [`part.startTransaction(context, update)`](/jpo/api/domain-object#starttransaction), [`commitTransaction`](/jpo/api/domain-object#committransaction), [`abortTransaction`](/jpo/api/domain-object#aborttransaction) <Badge type="warning" text="폐기" /> | 폐기. `DomainObject` 인스턴스 메서드로, 안에서 `ContextUtil` 의 같은 메서드를 그대로 부름 |
| `ContextUtil.startTransaction(context, update, historyOff)` | 세 번째 값을 서버 트랜잭션의 히스토리 끄기로 넘김. OOTB 0 건 |
| `context.start(true)`, `context.commit()`, `context.abort()` | 횟수를 거치지 않고 서버에 바로 보냄. `ContextUtil` 로 연 트랜잭션 안에서 부르면 횟수와 실제 상태가 어긋남 (OOTB 10 / 16 / 18 건) |
| MQL `start transaction`, `commit transaction`, `abort transaction` | 위와 같이 횟수에 잡히지 않음. OOTB 0 건 |

::: info 근거
`ContextUtil.startTransaction(Context, boolean)` → `startTransaction(context, update, false)`. 이 메서드: 횟수 (`ContextUtil.getAttribute(context, "transaction-reference-count")`) 가 0 보다 크면 1 올리고 `true`. 아니면 `ContextUtil.isTransactionActive(context)` 가 참일 때 `false`. 아니면 `Context.start(update, historyOff, false)` (→ `TransactionParameters.setUpdate`, `setHistoryOff`, `setTriggerOff`) 뒤 횟수 1, `true`. `commitTransaction`: 횟수가 0 이하면 아무것도 안 함. 1 이면 private `isTransactionUpdateAndAborting` (`MQLCommand.exec(context, "print transaction")` 결과에 `inactive` 가 없고 `aborting` 과 `update` 가 있으면 참) 이 참일 때 `EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.ContextUtil.ErrorCommit")` 문구로 `FrameworkException` (횟수 그대로). 아니면 횟수를 1 줄이고 0 이면 `Context.commit()`. `abortTransaction`: 횟수가 0 보다 크면 1 줄이고 0 이면 `Context.abort()`. 예외는 `printStackTrace(System.err)` 하고 삼킴 (`throws` 없음). 횟수는 `ContextUtil` 의 static `WeakHashMap` 에 `Context` 객체를 키로 둠 (`Context` 는 `equals` 를 바꾸지 않음). `Context.commit()` 은 서버 확정이 실패하면 트랜잭션이 살아 있을 때 `abort()` 하고 `MatrixExceptionOnCommit` 을 던짐. `DomainObject.startTransaction` / `commitTransaction` / `abortTransaction` 은 `Deprecated: true` 이고 `ContextUtil` 의 같은 메서드를 부름. properties/emxFrameworkStringResource.properties (`emxFramework.ContextUtil.ErrorCommit=Failed to commit`). OOTB 인자 모양: `startTransaction(context, true)` 236 건, `(context, false)` 59 건
:::

## 트랜잭션 중인지 확인하기

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `ContextUtil.isTransactionActive(context)` | 없음 | `boolean`. 서버에 트랜잭션이 열려 있으면 `true`. 누가 열었는지는 보지 않음 |

```java
boolean inTransaction = ContextUtil.isTransactionActive(context);
```

- `startTransaction` 이 안에서 이 확인을 하므로, 시작 전에 따로 부를 필요는 없습니다.
- 서버에 `print transaction` 을 한두 번 보냅니다. 실패하면 `FrameworkException` 입니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `context.isTransactionActive()` | `ContextUtil.isTransactionActive` 가 안에서 먼저 부르는 같은 확인. 거짓이면 `ContextUtil` 이 `print transaction` 결과가 `transaction active` 로 시작하는지 한 번 더 봄 |

::: info 근거
`ContextUtil.isTransactionActive`: `context.isTransactionActive()` 가 참이면 `true`, 아니면 `MqlUtil.mqlCommand(context, "print transaction")` 결과가 `transaction active` 로 시작하는지. 예외는 `FrameworkException` 으로 감쌈. `Context.isTransactionActive()` 는 `getTransactionInfo()` (`print transaction` 결과에 `inactive`, `aborting`, `read`, `update` 가 있는지로 나눔) 가 `INACTIVE` 가 아닌지
:::

## 트랜잭션 안에서 일부만 되돌리기

트랜잭션 안에 저장점을 만들고, 나중에 그 저장점 뒤의 변경만 되돌립니다. [여러 변경을 한 트랜잭션으로 묶기](#여러-변경을-한-트랜잭션으로-묶기) 예제의 `try` 안에서 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `ContextUtil.setSavePoint(context, name)` | `name`: 저장점 이름 | `void`. MQL `set transaction savepoint $1` 실행 |
| `ContextUtil.abortSavePoint(context, name)` | `name`: 되돌아갈 저장점 이름 | `void`. MQL `abort transaction $1` 실행 |

```java
ContextUtil.setSavePoint(context, "BeforeDocument");
doc.setAttributeValue(context, attrStatus, "Approved");
ContextUtil.abortSavePoint(context, "BeforeDocument");
```

- 저장점은 `ContextUtil` 의 횟수와 상관없습니다. 마지막에 `commitTransaction` 을 불러야 나머지 변경이 확정됩니다.
- OOTB (emxTaskBase_mxJPO) 는 `abortSavePoint` 뒤에도 같은 트랜잭션에서 다음 작업을 이어 갑니다.
- 실패하면 MQL 오류로 `FrameworkException` 이 납니다 ([MQL 실행, 환경 변수](/jpo/api/util/mql)).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| [`DomainObject.setSavePoint(context, name)`](/jpo/api/domain-object#setsavepoint) <Badge type="warning" text="폐기" /> | 폐기. 안에서 `ContextUtil.setSavePoint` 를 그대로 부름 |
| `MqlUtil.mqlCommand(context, "set transaction savepoint $1", name)`, `"abort transaction $1"` | `ContextUtil` 이 안에서 실행하는 명령 그대로 |

::: info 근거
`setSavePoint` → `MqlUtil.mqlCommand(context, "set transaction savepoint $1", name)`, `abortSavePoint` → `MqlUtil.mqlCommand(context, "abort transaction $1", name)`. 둘 다 횟수를 건드리지 않음. OOTB 예: emxTaskBase_mxJPO (`setSavePoint(context, "PROMOTE")` 뒤 이미 목표 상태면 `abortSavePoint(context, "PROMOTE")`, 아니면 `setState`). 생성 화면 JSP 도 `"CreateObject"` 저장점을 씀 ([가이드](/jpo/transaction-context#ui-가-열어-주는-트랜잭션))
:::

## 권한 없이 시스템으로 바꾸기

로그인 사용자에게 권한이 없는 작업 (시스템 속성 갱신, 다른 사람 객체 연결 ...) 을 잠깐 User Agent 로 실행합니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `ContextUtil.pushContext(context)` | 없음 | `void`. 지금 사용자를 쌓아 두고 User Agent (`person_UserAgent`) 로 바꿈 |
| `ContextUtil.popContext(context)` | 없음 | `void`. 바로 전 사용자로 되돌림. 꼭 `finally` 에서 부름 |

```java
String user = context.getUser();
ContextUtil.pushContext(context);
try {
    part.setAttributeValue(context, attrStatus, "Checked by " + user);
} finally {
    ContextUtil.popContext(context);
}
```

- push 와 pop 은 같은 수만큼 짝을 맞춥니다. 두 번 push 하면 두 번 pop 해야 처음 사용자로 돌아옵니다.
- push 안에서는 `context.getUser()` 가 User Agent 를 돌려줍니다. 실제 사용자 이름은 위처럼 push 전에 담아 둡니다. `PersonUtil` 처럼 `context.getUser()` 로 사용자를 정하는 메서드도 User Agent 기준이 됩니다 ([사용자, 조직, 보안 컨텍스트](/jpo/api/util/person)).
- push 없이 pop 하면 `The context has not been saved -use pushContext prior to calling popContext method` 문구로 `FrameworkException` 이 납니다. pop 안에서 난 다른 오류도 모두 이 문구로 바뀌고 원래 오류는 남지 않습니다. properties 의 한국어 문구는 앞부분이 `컨텍스트가 저장되었습니다.` 로 뜻이 반대로 번역되어 있습니다.
- push 안에서 부른 다른 JPO, MQL 도 User Agent 로 실행됩니다 (같은 `context`).
- MQL 명령 하나만 바꿔 실행하려면 `MqlUtil.mqlCommand(context, cmd, true, values...)` 를 씁니다 ([MQL 실행, 환경 변수](/jpo/api/util/mql)).
- 서버에 가는 것: push 마다 `get env global` 한 번 (비어 있으면 `set env global` 한 번 더) 과 사용자 전환 (`Context.resetContext`) 한두 번, pop 마다 사용자 전환 한두 번.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| [`DomainObject.pushContext(context)`](/jpo/api/domain-object#pushcontext), [`DomainObject.popContext(context)`](/jpo/api/domain-object#popcontext) <Badge type="warning" text="폐기" /> | 폐기, protected. 안에서 `ContextUtil` 의 같은 메서드를 그대로 부름 |
| `ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING)` | 같은 User Agent 로 바뀜. 인자만 길고 vault 자리에 `""` 를 그대로 넘김. OOTB 에 흔함 (117 건) |
| `ContextUtil.pushContext(context, null, null, null)` | 같은 User Agent 로 바뀜 (31 건) |
| MQL `push context`, `pop context` | `ContextUtil` 이 쌓는 목록에 남지 않아 `popContext` 로 되돌릴 수 없음 (OOTB 2 건) |
| `emxContextUtil_mxJPO` 의 `pushContext(context, args)`, `popContext(context, args)` | MQL 프로그램에서 부르는 JPO 메서드. 안에서 `ContextUtil.pushContext(context, user, password, vault)`, `ContextUtil.popContext(context)` 를 부름 |

::: info 근거
`pushContext(Context)` → `pushContext(context, getSuperUserName(context), getSuperUserPassword(context), null)`. `getSuperUserName` 은 `PropertyUtil.getSchemaProperty(context, "person_UserAgent")`, `getSuperUserPassword` 는 프로그램 `emxcommonSessionInfo` 의 내용을 `FrameworkUtil.decrypt` 로 두 번 푼 값 (`CacheUtil` 에 담아 둠). `pushContext(Context, String, String, String)`: `MX_LOGGED_IN_USER_NAME` 처리 ([바꾸기 전 사용자 알아내기](#바꾸기-전-사용자-알아내기)), 지금 `{user, password, vault}` 를 `ContextUtil.getAttribute(context, "context-stack")` 의 `Stack` 에 push, `Context.resetContext`, 세션 id 별 목록에도 push. `context.getUser()` 는 `MatrixSession.getUserName()` 이고 `resetContext` 가 이 값을 바꿈. `popContext`: 두 목록에서 pop, 꺼낸 비밀번호가 비었으면 먼저 User Agent 로 바꾼 뒤 `resetContext(user, password, vault)`. 안에서 어떤 예외가 나도 `emxFramework.ContextUtil.ContextNotSaved` 와 `emxFramework.ContextUtil.PushContext` 문구를 이어 붙인 `new FrameworkException(문구)` (원래 예외는 넣지 않음). properties/emxFrameworkStringResource.properties, emxFrameworkStringResource_ko.properties. emxContextUtilBase_mxJPO.java (`pushContext`, `popContext`). OOTB 인자 모양: `pushContext(context)` 398 건
:::

## 다른 사용자로 바꾸기

User Agent 가 아닌 정해 둔 사용자로 바꿔 실행합니다. OOTB 는 접근 권한을 줄 때 기록에 남을 사용자 (`DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR` 같은 grantor) 로 바꾸는 데 씁니다 ([소유권 공유, 접근 권한](/jpo/api/util/access)).

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `ContextUtil.pushContext(context, user, null, null)` | `user`: 바꿀 사용자 이름. 비우면 User Agent. 비밀번호와 vault 는 보통 `null` | `void`. 되돌릴 때는 같은 `ContextUtil.popContext(context)` |

| 넘긴 값 | 동작 |
| --- | --- |
| `password` 가 비었음 (`null`, `""`, 공백, `"null"`) | 먼저 User Agent 로 바꾼 뒤, `user` 가 User Agent 가 아니면 그 사용자로 비밀번호 없이 다시 바꿈 |
| `password` 가 있음 | 그 사용자와 비밀번호로 바로 바꿈 |
| `user` 가 비었음 | User Agent 와 그 비밀번호로 바꿈. 넘긴 `password` 는 쓰지 않음 |
| `vault` 가 `null` | 지금 context 의 vault 이름을 씀 (비밀번호가 비어 User Agent 를 먼저 거칠 때 그 첫 전환에는 `null` 을 그대로 넘김) |

```java
ContextUtil.pushContext(context, DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR, null, null);
try {
    part.setAttributeValue(context, attrStatus, "Reviewed");
} finally {
    ContextUtil.popContext(context);
}
```

- 비밀번호 없이 다른 사용자로 바꿀 수 있는 것은 User Agent 로 먼저 바꾼 뒤 그 권한으로 바꾸기 때문입니다.
- 쌓기와 되돌리기, 예외 문구는 [권한 없이 시스템으로 바꾸기](#권한-없이-시스템으로-바꾸기) 와 같습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| [`DomainObject.pushContext(context, user, password, vault)`](/jpo/api/domain-object#pushcontext) <Badge type="warning" text="폐기" /> | 폐기, protected. 안에서 그대로 부름 |
| `context.resetContext(user, password, vault)` | 바꾸기 전 사용자를 쌓지 않아 `popContext` 로 돌아갈 수 없음 |

::: info 근거
`pushContext(Context, String, String, String)`: `UIUtil.isNullOrEmpty(password)` (`null`, `trim` 해서 빈 값, `"null"` 대소문자 무시) 이면 `resetContext(UserAgent, UserAgent비밀번호, vault)` 를 먼저 부름. 그 뒤 `vault` 가 `null` 이면 `context.getVault().getName()`, `user` 가 비면 User Agent 이름과 비밀번호. 앞에서 User Agent 로 바꿨고 `user` 가 User Agent 면 (`equalsIgnoreCase`) 다시 바꾸지 않고, 아니면 `resetContext(user, password, vault)`. OOTB 예: emxECOBase_mxJPO (`pushContext(context, DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR, null, null)` 뒤 `BusinessObject.grantAccessRights`), 이 형태 11 건
:::

## 바꾸기 전 사용자 알아내기

시스템 사용자로 실행 중인 코드 (push 한 뒤 불린 트리거, 다른 JPO) 에서 실제 로그인 사용자를 알아야 할 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PropertyUtil.getRPEValue(context, ContextUtil.MX_LOGGED_IN_USER_NAME, true)` | `ContextUtil.MX_LOGGED_IN_USER_NAME`: 상수 `"MX_LOGGED_IN_USER_NAME"`. `true`: global 환경 변수 | `String`. `pushContext` 가 남긴 사용자 이름 |

```java
String loggedInUser = PropertyUtil.getRPEValue(context, ContextUtil.MX_LOGGED_IN_USER_NAME, true);
```

- `pushContext` 는 이 global 환경 변수가 비어 있을 때 (`null`, `""`, `"null"`) 만 바꾸기 전 사용자 이름을 넣습니다. `popContext` 는 지우지 않습니다. 한 번 들어간 뒤의 push 는 값을 바꾸지 않습니다.
- 그래서 처음 push 한 때의 사용자입니다. 처음 push 가 이미 User Agent 상태에서 일어났으면 User Agent 가 들어 있을 수 있어, OOTB 는 값이 User Agent 인 경우를 따로 처리합니다 (emxCommonCompleteTaskBase_mxJPO, emxRouteBase_mxJPO).
- 같은 메서드 안이라면 push 전에 `context.getUser()` 를 변수에 담아 두는 쪽이 확실합니다.
- 환경 변수 읽기 자체는 [MQL 실행, 환경 변수](/jpo/api/util/mql) 를 봅니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `PropertyUtil.getGlobalRPEValue(context, ContextUtil.MX_LOGGED_IN_USER_NAME)` | 안에서 `getRPEValue(context, name, true)` 를 그대로 부름. OOTB 가 주로 쓰는 모양 |

::: info 근거
`ContextUtil.pushContext(Context, String, String, String)` 첫 부분: `PropertyUtil.getGlobalRPEValue(context, "MX_LOGGED_IN_USER_NAME")` 이 `null`, `""`, `"null"` 이면 `PropertyUtil.setGlobalRPEValue(context, "MX_LOGGED_IN_USER_NAME", context.getUser())` (바꾸기 전 사용자). `popContext` 에는 이 이름이 없음. 상수 `ContextUtil.MX_LOGGED_IN_USER_NAME = "MX_LOGGED_IN_USER_NAME"`. OOTB 의 `ContextUtil.MX_LOGGED_IN_USER_NAME` 사용 22 건
:::

## context 없이 실행하기

Context 를 인자로 받지 못한 코드 (표 정렬용 비교 클래스의 기본 생성자 같은 곳) 에서 MQL 이나 조회를 해야 할 때 씁니다. JPO 메서드는 받은 `context` 를 쓰면 되므로 쓸 일이 거의 없습니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `ContextUtil.runInAnonymousContext(callable)` | `callable`: `ContextUtil.Callable` 구현. `call(Context ctx)` 안에서 넘겨받은 `ctx` 로 일을 함 | `Object`. `call` 이 돌려준 값. 예외는 `FrameworkException` 으로 감쌈 |

```java
String runAs = (String) ContextUtil.runInAnonymousContext(new ContextUtil.Callable() {
    public Object call(Context ctx) throws Exception {
        return ctx.getUser();
    }
});
```

- JPO 를 실행 중인 스레드에서는 새 context 를 만들지 않고 **그 JPO 가 받은 context** 를 넘깁니다. 이름과 달리 로그인 사용자 그대로입니다.
- 그 밖의 스레드에서는 사용자 없이 `new Context("", host)` 로 서버에 연결하고, `call` 이 끝나면 (예외가 나도) 연결을 닫습니다. 사용자 없는 context 로 무엇을 할 수 있는지는 확인하지 않았습니다 <Badge type="danger" text="검증 필요" />.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `ContextUtil.getAnonymousContext()` <Badge type="warning" text="폐기" /> | 폐기. 얻은 context 를 돌려주기만 하고 닫지 않음. JPO 스레드에서는 JPO 의 context 를 돌려주므로 다 쓰고 닫으면 JPO 의 context 가 닫힘 |
| `PropertyUtil.getAnonymousContext()` | 안에서 폐기된 `ContextUtil.getAnonymousContext()` 를 부르고, 예외는 출력만 하고 `null` 을 돌려줌 |
| `PropertyUtil.shutDownAnonymousContext(ctx)` | `ctx.shutdown()` 만 하고 `null` 을 돌려줌. JPO 의 context 를 넘기면 그것을 닫음 |

::: info 근거
`runInAnonymousContext`: `JPOSupport.isJPOThread()` 가 거짓이면 `PropertyUtil.getProperty("ematrix.server.host")` 를 host 로 씀. `Context.getExistingContext()` (→ `JPOSupport.getContext()`: 지금 스레드에 등록된 context) 가 있고 `isConnected()` 면 그것을 쓰고, 아니면 `new Context("", host)` + `connect()` 하고 `call` 뒤 finally 에서 `shutdown()` (→ `close()`). `JPOSupport.invokeObject`, `newInstance` 는 JPO 를 부를 때 넘긴 context 를 스레드에 등록 (`registerThread`) 하고 끝나면 해제. `getAnonymousContext()` (`Deprecated: true`) 는 같은 순서로 얻은 context 를 돌려주고 닫지 않음 (연결 여부도 보지 않음). `PropertyUtil.getAnonymousContext()` 는 `FrameworkException` 을 `printStackTrace` 하고 삼킴. OOTB `getAnonymousContext` 7 건은 모두 정렬 비교 클래스의 기본 생성자 (emxSortPersonFullNameBase_mxJPO 등)
:::

## 샘플 JPO

[XX_UtilTransaction_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilTransaction_mxJPO.java) 의 메서드입니다. 모든 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map (`programMap`) 에서 값을 읽습니다.

| 메서드 | 받는 값 (`programMap` 의 키) | 돌려주는 값 | 보여 주는 것 |
| --- | --- | --- | --- |
| `setStatusTogether` | `objectId`, `documentId`, `status` | 없음 | **데이터를 바꿈.** 두 객체의 속성을 한 트랜잭션으로 바꾸고 실패하면 되돌리기 ([여러 변경을 한 트랜잭션으로 묶기](#여러-변경을-한-트랜잭션으로-묶기)) |
| `isInTransaction` | 없음 | `Boolean` | 트랜잭션 중인지 ([트랜잭션 중인지 확인하기](#트랜잭션-중인지-확인하기)) |
| `setCheckedByAsSystem` | `objectId` | 없음 | **데이터를 바꿈.** 실제 사용자 이름을 담아 두고 User Agent 로 속성 바꾸기 ([권한 없이 시스템으로 바꾸기](#권한-없이-시스템으로-바꾸기)) |
| `getUserDuringPush` | `userName` (비우면 User Agent) | `Map` (`before`, `during`, `loggedIn`, `after`) | push 전, 중, 후의 `context.getUser()` 와 `MX_LOGGED_IN_USER_NAME` ([다른 사용자로 바꾸기](#다른-사용자로-바꾸기), [바꾸기 전 사용자 알아내기](#바꾸기-전-사용자-알아내기)) |

::: details XX_UtilTransaction_mxJPO.java

<<< @/../JPOSample/util/XX_UtilTransaction_mxJPO.java

:::

::: warning
샘플의 타입, 관계, 속성 이름은 예시입니다. 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다.
:::

## 다루지 않은 메서드

::: details 펼쳐 보기
| 클래스 | 메서드 | 이유 |
| --- | --- | --- |
| `ContextUtil` | `setAttribute`, `getAttribute`, `removeAttribute` | 내부용 |
| `ContextUtil` | `restoreContext` | 내부용 |
| `ContextUtil` | `clearAgentCache` | 내부용 |
| `ContextUtil` | `getSuperUserName` (protected, 폐기) | 내부용 |
:::
