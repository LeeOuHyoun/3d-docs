# 메일, 알림 <Badge type="info" text="원본 분석" />

JPO 에서 사용자에게 3DEXPERIENCE 알림 (IconMail) 과 메일을 보내는 방법입니다. 받는 사람, 제목, 본문, 객체 링크를 넘기는 법과 보내는 사람을 정하는 법을 다룹니다. 예제의 `part` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다. util 전체를 하고 싶은 일로 찾으려면 [유틸리티](/jpo/api/utilities) 를 봅니다. 화면에 경고창 문구를 띄우는 `notice` 는 메일이 아니라 [MQL 실행, 환경 변수](/jpo/api/util/mql) 에 있습니다.

::: info 근거
xerinfra.jar (`MailUtil`, `MessageUtil`, `framework.ui.UINavigatorUtil`), eMatrixServletRMI.jar (`IconMail`, `com.matrixone.util.MxMessageBase`, `matrix.util.StringResource`) class 파일 (javap) 기준. `MailUtil` 은 대부분 OOTB JPO `emxMailUtil` 을 `JPO.invoke` 로 부르기만 하므로, 실제로 보내는 일은 OOTB JPO 원본 `emxMailUtilBase_mxJPO.java`, `emxNotificationUtilBase_mxJPO.java` 로 확인했습니다. 건수는 OOTB JPO 에서 센 호출 수: `MailUtil.getObjectLinkURI(` 23, `MailUtil.setAgentName(` 13, `MailUtil.sendMessage(` 12, `MailUtil.sendNotification(` 10, `MailUtil.setTreeMenuName(` 9, `MailUtil.getBaseURL(` 4, `MailUtil.getAgentName(` 1, `MailUtil.setBaseURL(` 1, `emxMailUtil_mxJPO.sendNotification(` 15, `emxMailUtil_mxJPO.sendNotificationToUser(` 15, `emxNotificationUtil_mxJPO.getObjectLinkHTML(` 37, `emxNotificationUtil_mxJPO.objectNotification(` 24, `emxNotificationUtil_mxJPO.sendJavaMail(` 9
:::

샘플: [XX_UtilMail_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilMail_mxJPO.java) ([메서드별 설명](#샘플-jpo))

## 한눈에 보기

| 하고 싶은 일 | 쓸 메서드 |
| --- | --- |
| 제목, 본문을 그대로 넘겨 알림 보내기 | [`MailUtil.sendMessage(context, toList, ccList, bccList, subject, message, objectIdList)`](#알림-보내기) |
| 받는 사람마다 그 사람 언어의 문구로 보내기 | [`MailUtil.sendNotification(context, toList, ..., objectIdList, companyName, bundle)`](#받는-사람-언어로-알림-보내기) |
| 보내는 사람 정하기 | [`MailUtil.setAgentName(context, name)`](#보내는-사람-정하기) |
| 본문에 넣을 객체 링크 주소 만들기 | [`MailUtil.getObjectLinkURI(context, MailUtil.getBaseURL(context), objectId)`](#객체-링크-주소-만들기) |
| 알림이 어디로 가는지 (알림, 메일) 알기 | [알림이 가는 곳](#알림이-가는-곳) |

## 알림이 가는 곳

`MailUtil.sendMessage`, `sendNotification` 은 마지막에 OOTB JPO `emxMailUtil` 의 `sendMail` 로 모입니다. 서버가 클라우드인지 ([`UINavigatorUtil.isCloud`](/jpo/api/util/etc)) 와 `emailAlso` 값에 따라 보내는 길이 다릅니다.

| 서버 | `emailAlso` | 하는 일 |
| --- | --- | --- |
| 온프레미스 | `true` (기본) | 3DEXPERIENCE 알림 (`IconMail`) 을 만들어 `send(context, subject, true)`. 세 번째 값은 메일도 함께 보낼지로 쓰는 값 (아래) |
| 온프레미스 | `false` | 위와 같고 `send(context, subject, false)` |
| 클라우드 | `true` | 알림 없이 메일만 보냄 (`MxMessage.sendJavaMail(context, false)`, 서버의 SMTP 설정 사용) |
| 클라우드 | `false` | 아무것도 보내지 않고 끝남 (예외 없음) |

- `IconMail.send` 의 세 번째 값은 서버 쪽 `bosIconMail.send` 로 그대로 넘어갑니다. OOTB 는 메일을 따로 보낼 때 이 값에 `false` 를 넣습니다 (`MxMessageBase.sendJavaMail` 은 알림을 `false` 로 보낸 뒤 메일을 따로 보냄). 그래서 "메일도 함께" 로 보이지만 서버 동작은 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.
- 메일 주소는 받는 사람 Person 의 메일 주소를 씁니다. 메일 서버 설정은 서버 쪽 (`MxMessageSupport.getSendMailInfo`) 에서 읽습니다.
- 이 프로젝트처럼 SMTP 로 직접 보내는 방법은 [메일](/server-java/mail) 에 있습니다.

::: info 근거
emxMailUtilBase_mxJPO.java `sendMail(…, emailAlso)`: 처음에 `isCloud && !emailAlso` 면 `return`. `new IconMail().create(context)` 뒤 받는 사람, 객체, 본문을 넣고, `isCloud` 면 `msg.sendJavaMail(context, false)`, 아니면 `mail.send(context, subject, emailAlso)`. eMatrixServletRMI.jar `IconMail.send(Context, String, boolean)` 은 `bosIconMail.send(subject, boolean)` 호출, `send(Context, String)` 은 `true` 로 부름. `MxMessageBase.sendJavaMail(Context, boolean)`: 값이 `true` 면 `IconMail` 을 만들어 `send(context, subject, false)` 한 뒤, 언제나 `sendJavaMail(context)` (SMTP 정보는 `getSendMailInfo`). emxNotificationUtilBase_mxJPO.java `sendJavaMail` 의 주석 `//Send mail only using IconMail` 뒤 `iconMail.send(context, subject, false)`
:::

## 알림 보내기

제목과 본문 글자를 그대로 넘겨 보냅니다. 문구를 코드에서 만들 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `MailUtil.sendMessage(context, toList, ccList, bccList, subject, message, objectIdList)` | `toList`: 받는 사람 이름 (로그인 id) 의 `StringList`. `ccList`, `bccList`: 참조, 숨은 참조. 없으면 `null`. `subject`: 제목. `message`: 본문 (글자 그대로). `objectIdList`: 알림에 붙일 객체 id 의 `StringList`. 없으면 `null` | 없음 (`void`) |
| `MailUtil.sendMessage(context, toList, ccList, bccList, subject, message, objectIdList, emailAlso)` | `emailAlso`: `false` 면 메일 없이 알림만 ([알림이 가는 곳](#알림이-가는-곳)). 앞 형태는 `true` | 없음 |

`objectIdList` 를 주면 객체를 알림에 붙이고, 본문 끝에 객체마다 링크를 한 줄씩 붙입니다. 링크 앞부분은 [getBaseURL](#객체-링크-주소-만들기) 값입니다.

| `message` 와 `objectIdList` | 실제 본문 (예시) |
| --- | --- |
| `"Please review."`, `[1234.5678.9012.3456]` | `Please review.` 다음 빈 줄, 그다음 `https://plm.example.com/3dspace/common/emxNavigator.jsp?objectId=1234.5678.9012.3456` |
| 위와 같고 `getBaseURL` 이 `""` | `Please review.` (링크 없음) |

주소는 모양을 보이려는 예시입니다. 서버에서 보낸 결과가 아닙니다.

```java
StringList toList = FrameworkUtil.split("user01,user02", ",");
MailUtil.setAgentName(context, context.getUser());
MailUtil.sendMessage(context, toList, null, null, "Review request: P-0001", "Please review.", new StringList(objectId));
```

- 제목이 `null` 이나 `""` 이면 아무것도 보내지 않고 끝납니다. 예외도 나지 않습니다.
- 제목이 UTF-8 로 255 byte 를 넘으면 250 byte 로 자르고 `...` 를 붙입니다.
- 보내는 동안 User Agent 로 바꿔서 실행합니다 (`pushContext`, 끝나면 `finally` 에서 `popContext`). 그래서 [보내는 사람](#보내는-사람-정하기) 을 정하지 않으면 User Agent 가 보낸 것으로 보입니다. OOTB 는 바로 앞에 `MailUtil.setAgentName(context, context.getUser())` 를 부릅니다.
- `toList` 의 이름마다 MQL `print User $1 select isaperson dump` 로 사람인지 보고, 사람이면 `iconmailenabled` 를 읽습니다. 메일 주소가 없고 `iconmailenabled` 가 `FALSE` 인 사람은 받는 사람에서 뺍니다. `ccList`, `bccList` 는 거르지 않습니다.
- `objectIdList` 의 객체를 하나씩 엽니다. 없는 id 가 있으면 예외입니다.
- 테넌트 값이 있는 서버에서는 링크 끝에 `&tenant=...` 가 두 번 붙습니다 (`getObjectLinkURI` 가 붙이고 JPO 가 한 번 더 붙임).
- 실패하면 원래 예외를 담은 `FrameworkException` 입니다. [보내는 사람](#보내는-사람-정하기) 을 정해 둔 경우, JPO 는 보내는 사람으로 바꾼 context 를 `finally` 없이 되돌리므로 보내다 실패하면 바뀐 채로 남습니다. 메일 실패를 잡고 계속 진행하는 코드는 그 뒤 `context.getUser()` 가 달라져 있을 수 있습니다.
- 서버에 가는 일: JPO `emxMailUtil` 실행, MQL `get env global MX_TREE_MENU`, 받는 사람마다 `print User`, `print person`, 객체 열기, 알림 만들기와 보내기.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `emxNotificationUtil_mxJPO.sendJavaMail(context, toList, ccList, bccList, subject, text, html, fromAgent, replyTo)` | JPO 클래스의 static 메서드라 서버 JPO 로만 컴파일됨. 안에서 난 예외를 `System.out` 에 찍고 버려서 실패를 알 수 없음. HTML 본문이 꼭 필요할 때만 |
| `emxNotificationUtil_mxJPO.objectNotification(context, objectId, notificationName, payload)` | 관리자가 만든 `Notification` 객체 (받는 사람, 제목, 본문을 속성으로 둠) 가 있어야 함. 받는 사람과 문구를 코드에서 정하지 않음 |
| `emxMailUtil_mxJPO.sendMail(context, args)` | 원본 주석이 `@deprecated` (대신 `sendNotificationToUser`). 안에서 `sendMessage` 를 부름 |
| `new IconMail()` + `create`, `setToList`, `send` | `MailUtil` 이 JPO 안에서 하는 일. 받는 사람 거르기, 링크, 보내는 사람 바꾸기를 직접 해야 함 |
| `new MxMessage()` + `sendJavaMail(context, false)` | 메일만 보냄. 클라우드에서 JPO 가 쓰는 길 |
| MQL `send mail` | OOTB JPO 에서 쓰는 곳이 없음 (주석에만 나옴). 문법과 동작은 확인 못 함 |
| `MessageUtil` | 보내는 메서드가 없음. 문구 만들기 (`getMessage`, `getString`) 만 있음 |

::: info 근거
xerinfra.jar `MailUtil.sendMessage(7 개)` → `(8 개, true)`. 8 개 형태: `ContextUtil.pushContext(context)` 뒤 `toList`, `ccList`, `bccList`, `subject`, `message`, `objectIdList`, `emailAlso` 를 Map 에 담아 `JPO.invoke(context, "emxMailUtil", null, "sendMessage", JPO.packArgs(map))`, 예외는 `new FrameworkException(e)`, `finally` 에서 `popContext`. emxMailUtilBase_mxJPO.java `sendMessage(…, emailAlso)`: 제목이 `null`/`""` 면 `return`, `MX_TREE_MENU` 를 읽고 지움, `getBaseURL` 과 `objectIdList` 가 있으면 `"\n"` 뒤 id 마다 `"\n" + MailUtil.getObjectLinkURI(...)` (+ `&treeMenu=`, + `&tenant=`). `sendMail`: `print User $1 select $2 dump` (`isaperson`), `print person $1 select $2 dump` (`iconmailenabled`), `PersonUtil.getEmail`, 객체마다 `new BusinessObject(id).open(context)`, 제목 `getBytes("UTF-8").length > 255` 면 `FrameworkUtil.getUtf8TruncatedString(subject, 250) + "..."`, 보내는 사람 `pushContext` 뒤 보내고 `popContext` (`try`/`finally` 없음). OOTB 호출 예: emxCommonTaskBase_mxJPO.java (`setAgentName(context, context.getUser())` 다음 `sendMessage`), emxLifecycleBase_mxJPO.java (8 개 형태, `emxFramework.Routes.EnableMails` 값을 `emailAlso` 로)
:::

## 받는 사람 언어로 알림 보내기

제목과 본문을 문자열 리소스 키로 넘깁니다. 받는 사람마다 언어 설정을 읽어 언어별로 따로 번역해 보냅니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `MailUtil.sendNotification(context, toList, ccList, bccList, subjectKey, subjectKeys, subjectValues, messageKey, messageKeys, messageValues, objectIdList, companyName, bundle)` | `subjectKey`, `messageKey`: 리소스 키. 없는 키면 그 글자를 그대로 씀. `subjectKeys` / `subjectValues`: 문구 안 `<키>` 를 바꿀 이름과 값 (같은 길이의 `String[]`. 바꿀 것이 없으면 `null`). `messageKeys` / `messageValues`: 본문용. `companyName`: 넘기지만 문구를 찾을 때 쓰지 않음. 보통 `null`. `bundle`: 리소스 파일 이름 (`emxMyAppStringResource`). 나머지는 [sendMessage](#알림-보내기) 와 같음 | 없음 (`void`) |
| `MailUtil.sendNotification(context, toList, ..., objectIdList, companyName)` | `bundle` 없이 `emxFrameworkStringResource` 에서 찾음 | 없음 |

문구의 `<키>` 는 `matrix.util.StringResource.format` 이 바꿉니다.

| 리소스 문구 / 키 / 값 | 결과 |
| --- | --- |
| `Route <Name> can not be started` / `Name` / `R-0001` | `Route R-0001 can not be started` |
| `<type> <name> was released` / `type`, `name` / `My Part`, `P-0001` | `My Part P-0001 was released` |
| `<Name> / <Name>` / `Name` / `P-0001` | `P-0001 / <Name>`. 키마다 처음 한 곳만 바꿈 |
| `<NAME> was released` / `name` / `P-0001` | `<NAME> was released`. 대소문자를 가림 |
| `<name>` / `name` / `null` | `NullPointerException` |

```text
emxMyApp.Mail.ReviewSubject=Review request: <name>
emxMyApp.Mail.ReviewBody=Please review <name> by <date>.
```

```java
String name = part.getInfo(context, DomainConstants.SELECT_NAME);
MailUtil.sendNotification(context, new StringList("user01"), null, null,
        "emxMyApp.Mail.ReviewSubject", new String[] {"name"}, new String[] {name},
        "emxMyApp.Mail.ReviewBody", new String[] {"name", "date"}, new String[] {name, "2026-12-01"},
        new StringList(objectId), null, "emxMyAppStringResource");
```

본문은 받는 사람 언어마다 아래 순서로 만듭니다.

1. `objectIdList` 가 있으면 `emxFramework.IconMail.ObjectDetails.CheckBusinessObjects` 문구 (`Please check following Business Object(s):`) 와 객체마다 `"타입 표시 이름" "이름" "리비전"` 한 줄
2. `messageKey` 문구 (`<키>` 를 바꾼 것)
3. 객체마다 링크 한 줄 (`getBaseURL` 값 + `?objectId=` + id, 테넌트가 있으면 `&tenant=`)

- 받는 사람의 언어는 사용자 설정 (`IconMailLanguagePreference`) 이고, 없으면 `PersonUtil.getLanguageDefault(context)` 입니다. 언어가 같은 사람끼리 묶어 한 번씩 보냅니다.
- `sendMessage` 와 달리 User Agent 로 바꾸지 않습니다. 객체 정보를 읽는 일과 보내는 일이 부른 사용자 권한으로 됩니다. 보내는 사람은 [보내는 사람 정하기](#보내는-사람-정하기) 를 따릅니다.
- 제목이 비면 보내지 않는 것, 받는 사람 거르기, 제목 255 byte 자르기는 [sendMessage](#알림-보내기) 와 같습니다 (같은 `sendMail` 로 모임).
- `objectIdList` 에 Inbox Task 가 있으면 그 작업과 뒤 객체의 링크는 붙지 않고, 작업 안내 문구가 붙습니다.
- `emailAlso` 를 고르려면 `bundle` 대신 마지막에 `boolean` 을 넣는 형태를 씁니다. 그때 리소스 파일은 `emxFrameworkStringResource` 입니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `MailUtil.sendNotification(context, toList, ..., objectIdList, companyName, emailAlso)` | `bundle` 을 받지 않음. 메일을 빼야 할 때만 |
| `emxMailUtil_mxJPO.sendNotification(context, ...)` (static) | `MailUtil.sendNotification` 이 `JPO.invoke` 로 부르는 그 JPO 메서드. 서버 JPO 로만 컴파일됨 |
| `emxMailUtil_mxJPO.sendNotificationToUser(context, args)` | 쉼표로 이은 받는 사람, 키와 값 개수를 `String[]` 에 차례로 담아 넘기는 형태. 안에서 `sendNotification` 을 부름 |
| `MessageUtil.getMessage(...)` 로 만든 문구 + [sendMessage](#알림-보내기) | 받는 사람 언어를 고르지 않음. 한 언어로 보내도 될 때는 이쪽이 단순함 |

::: info 근거
xerinfra.jar `MailUtil.sendNotification(12 개)` → `(13 개, true)`. `(…, boolean)` 과 `(…, String)` 형태는 키 이름 `toList`, `ccList`, `bccList`, `subjectKey`, `subjectKeys`, `subjectValues`, `messageKey`, `messageKeys`, `messageValues`, `objectIdList`, `companyName` 에 `emailAlso` 또는 `basePropFileName` 을 더해 `JPO.invoke(context, "emxMailUtil", null, "sendNotification", ...)` (push 없음). emxMailUtilBase_mxJPO.java `sendNotification`: `basePropFileName` 이 있으면 그 파일, 없으면 `MessageUtil.getBundleName(context)` (`emxFrameworkStringResource`). 받는 사람마다 `PropertyUtil.getAdminProperty(context, "user", 이름, PersonUtil.PREFERENCE_ICON_MAIL_LANGUAGE)` (값 `IconMailLanguagePreference`), 비면 `PersonUtil.getLanguageDefault`. 제목은 `MessageUtil.getMessage(context, null, subjectKey, subjectKeys, subjectValues, companyName, locale, bundle)`: `getString` → `EnoviaResourceBundle.getProperty(context, bundle, locale, key)` (`companyName` 은 쓰지 않음, 예외면 키), `substituteValues`, `StringResource.format(문구, keys, values)`. `StringResource.format` 은 키마다 `indexOf("<" + 키 + ">")` 한 번만 찾아 바꿈. 위 `<키>` 표는 로컬 실행으로 확인 (eMatrixServletRMI.jar `StringResource.format`). 링크는 `baseurl + "?objectId=" + id` (+ `&tenant=`), Inbox Task 면 `break` 뒤 `getInboxTaskMailMessage`. properties/emxFrameworkStringResource.properties (`emxFramework.Routes.Mail.Subject = Route <Name> can not be started`). OOTB 호출 예: emxCommonIssueBase_mxJPO.java (`messageKeys = {"type", "name"}`), enoECMChangeUtilBase_mxJPO.java (키 대신 만든 문구를 넘김)
:::

## 보내는 사람 정하기

알림의 보내는 사람을 바꿉니다. `sendMessage` 는 User Agent 로 보내므로, 실제 사용자 이름으로 보이게 하려면 먼저 부릅니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `MailUtil.setAgentName(context, name)` | `name`: 보내는 사람의 사용자 이름. `null` 이나 `""` 면 정해 둔 값을 지움 | 없음 |
| `MailUtil.getAgentName(context)` | 없음 | `String`. 정해 둔 값, 없으면 설정 `emxFramework.NotificationAgent` 값 |

```java
MailUtil.setAgentName(context, context.getUser());
String agent = MailUtil.getAgentName(context);
```

- 값은 `context` 의 custom data (`emxMailUtilBase.NotificationAgentName`) 에 들어갑니다. 지우지 않으면 같은 context 로 보내는 다음 알림에도 쓰입니다.
- 보낼 때 이 값이 있고 지금 사용자와 다르면 그 사용자로 바꿔 (`pushContext`) 보내고 되돌립니다.
- 분석한 `emxSystem.properties` 의 `emxFramework.NotificationAgent` 는 빈 값입니다. 그래서 정하지 않으면 지금 context 사용자 (`sendMessage` 는 User Agent) 가 보낸 것으로 보입니다.
- 클라우드에서는 설정 `emxFramework.NotificationAgent` 에 값이 있으면 이 값보다 그 값을 먼저 씁니다.

::: info 근거
xerinfra.jar `MailUtil.setAgentName` / `getAgentName` 은 `JPO.invoke(context, "emxMailUtil", null, "setAgentName" / "getAgentName", ...)`. emxMailUtilBase_mxJPO.java `setAgentName`: 값이 `null`/`""` 가 아니면 `context.setCustomData("emxMailUtilBase.NotificationAgentName", 값)`, 아니면 `removeFromCustomData`. `getAgentName`: custom data, 없으면 `EnoviaResourceBundle.getProperty(context, "emxFramework.NotificationAgent")` (`mailConfigurationsLoaded` 는 `true` 로 바뀌는 곳이 없음). `sendMail`: `agentName` 이 비지 않고 `context.getUser()` 와 다르면 `emxContextUtil.pushContext(context, {agentName})`. properties/emxSystem.properties (`emxFramework.NotificationAgent =`). OOTB 호출 예: emxCommonTaskBase_mxJPO.java, emxProjectHoldAndCancelBase_mxJPO.java (`getAgentName` 으로 읽어 다시 `setAgentName`)
:::

## 객체 링크 주소 만들기

본문에 넣을 객체 화면 주소를 만듭니다. 앞부분 (base URL) 은 서버가 기억해 둔 값을 읽습니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `MailUtil.getBaseURL(context)` | 없음 | `String`. 링크 앞부분 (`…/common/emxNavigator.jsp`). 아직 없으면 `""` |
| `MailUtil.getObjectLinkURI(context, baseURL, objectId)` | `baseURL`: 링크 앞부분. `objectId`: 객체 id. 비면 `?objectId=` 를 붙이지 않음 | `String`. 서버에 가지 않음 |

| 호출 (예시) | 결과 |
| --- | --- |
| `getObjectLinkURI(context, "https://plm.example.com/3dspace/common/emxNavigator.jsp", "1234.5678.9012.3456")`, 테넌트 없음 | `https://plm.example.com/3dspace/common/emxNavigator.jsp?objectId=1234.5678.9012.3456` |
| 위와 같고 테넌트 `OI000000001` | `…emxNavigator.jsp?objectId=1234.5678.9012.3456&tenant=OI000000001` |
| `objectId` 가 `""`, 테넌트 `OI000000001` | `…emxNavigator.jsp?tenant=OI000000001` |
| `baseURL` 이 `""` | `?objectId=1234.5678.9012.3456`. 쓸 수 없는 주소 |

```java
String baseURL = MailUtil.getBaseURL(context);
String link = UIUtil.isNullOrEmpty(baseURL) ? "" : MailUtil.getObjectLinkURI(context, baseURL, objectId);
```

- base URL 은 서버 (JVM) 의 static Map 에 테넌트별로 들어 있습니다. 사용자가 화면으로 처음 들어올 때 `common/emxClientSideInfoProcessing.jsp` 가 `MailUtil.setMailConfigurations` 로 한 번 넣습니다. 서버를 다시 띄운 뒤 아무도 화면에 들어오지 않았으면 `""` 이므로, 스케줄러처럼 화면 없이 도는 JPO 에서는 비어 있을 수 있습니다.
- 넣는 값은 `UINavigatorUtil.notificationURL` 이 만든 `<서버 주소><페이지 경로>common/emxNavigator.jsp` 입니다. 설정 `emxFramework.EnableURLNotification` 이 `false` 면 `""` 입니다 (분석한 값은 `true`).
- 위 표의 서버 주소와 테넌트 값은 예시입니다. 결과는 바이트코드의 문자열 연결로 확인했습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `emxNotificationUtil_mxJPO.getObjectLink(context, objectId)` | 안에서 `getBaseURL` 이 비지 않을 때 `MailUtil.getObjectLinkURI` 를 그대로 부름. JPO 클래스 |
| `emxNotificationUtil_mxJPO.getObjectLinkHTML(context, text, objectId)` | 위 링크를 `<a href='…'>text</a>` 로 감쌈. `text` 를 HTML 이스케이프하지 않음 |
| 주소를 `"…?objectId=" + id` 로 직접 이어 붙이기 | 테넌트 값을 빠뜨림 |

::: info 근거
xerinfra.jar `MailUtil.getObjectLinkURI`: `uri = baseURL`, `objectId` 가 `isNotNullAndNotEmpty` 면 `uri + "?objectId=" + objectId`, `context.getTenant()` 가 비지 않으면 `uri` 에 `?` 가 있을 때 `"&tenant="`, 없을 때 `"?tenant="` 를 붙임 (BootstrapMethods `\u0001?objectId=\u0001`, `\u0001&tenant=\u0001`, `\u0001?tenant=\u0001`). `getBaseURL` → `JPO.invoke(…, "getBaseURL", null, String.class)`. emxMailUtilBase_mxJPO.java `getBaseURL` → `getTenantProperty(context, 1)`: static `_TENANT_URL` 에 테넌트 (`context.getTenant()`, 비면 `_GLOBAL_NO_TENANT_`) 가 없으면 `""`. `setMailConfigurations` 는 테넌트 값이 아직 없을 때만 넣고, `setBaseURL` 은 이미 있을 때만 바꿈. common/emxClientSideInfoProcessing.jsp (세션의 `emxCommonAppInitialized` 가 거짓일 때 `UINavigatorUtil.notificationURL(context, request, null, null, false)` 로 만든 주소를 `setMailConfigurations`). xerinfra.jar `UINavigatorUtil.notificationURL`: `emxFramework.EnableURLNotification` 이 `false` 면 `""`, 아니면 `ematrix.proxy.server` (없으면 요청의 서버 경로) + `ematrix.page.path` + `eServiceSuiteFramework.CommonDirectory` (`common`) + `eServiceSuiteFramework.ApplicationStartPage` (`emxNavigator.jsp`). emxNotificationUtilBase_mxJPO.java `getObjectLink`, `getObjectLinkHTML`
:::

## 샘플 JPO

[XX_UtilMail_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilMail_mxJPO.java) 의 메서드입니다. 모든 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map (`programMap`) 에서 값을 읽습니다.

| 메서드 | 받는 값 (`programMap` 의 키) | 돌려주는 값 | 보여 주는 것 |
| --- | --- | --- | --- |
| `sendReviewRequest` | `objectId`, `toUsers` (쉼표로 이은 사용자 이름), `message` | 없음 | 보내는 사람을 로그인 사용자로 정하고 [sendMessage](#알림-보내기). **알림을 보냄** |
| `sendReviewRequestByKey` | `objectId`, `toUsers` | 없음 | 리소스 키와 `<name>` 값으로 [sendNotification](#받는-사람-언어로-알림-보내기). **알림을 보냄** |
| `getObjectLink` | `objectId` | `String` (링크. base URL 이 없으면 `""`) | [getBaseURL, getObjectLinkURI](#객체-링크-주소-만들기) |
| `getMailSettings` | 없음 | `Map` (`baseURL`, `agentName`) | 지금 서버의 base URL 과 보내는 사람 ([보내는 사람 정하기](#보내는-사람-정하기)) |

::: details XX_UtilMail_mxJPO.java

<<< @/../JPOSample/util/XX_UtilMail_mxJPO.java

:::

::: warning
샘플의 타입, 리소스 키 (`emxMyApp.Mail.ReviewSubject` ...) 는 예시입니다. `sendReviewRequestByKey` 는 `emxMyAppStringResource` 에 두 키가 있어야 문구가 번역됩니다 (없으면 키 글자가 그대로 나감). 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다.
:::

## 다루지 않은 메서드

::: details 펼쳐 보기
| 클래스 | 메서드 | 이유 |
| --- | --- | --- |
| `MailUtil` | `setBaseURL` | 내부용 |
| `MailUtil` | `setMailConfigurations` | 내부용 |
| `MailUtil` | `setLanguages`, `getLanguages` | 내부용 |
| `MailUtil` | `setTreeMenuName`, `unsetTreeMenuName`, `getTreeMenuName` | 화면용 |
| `MailUtil` | `sendNotificationWhenObjectBlockedFromPromoting` | 앱 전용 |
| `UINavigatorUtil` | `notificationURL`, `notificationAgentName`, `notificationLanguages` | 화면용 |
:::
