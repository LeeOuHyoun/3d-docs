# Slack 알림 <Badge type="info" text="원본 분석" />

서버 코드에서 Slack 메시지를 보내는 방법입니다. 프로젝트는 Slack 앱의 봇 토큰으로 [Slack Web API](https://api.slack.com/web) 를 직접 부르는 class 를 두었습니다. [메일](/server-java/mail) 과 같은 모양으로 발송, 문구, 업무 알림 class 를 나눕니다.

| 층 | class | 역할 |
| --- | --- | --- |
| 발송 | `MySlackSender` | Web API 호출. 채널 메시지, 이메일로 사용자를 찾아 DM. **예외를 던지지 않음** |
| 문구 | `MySlackMessage` | 제목 한 줄 + 항목 줄 + 바로가기 링크. 문구는 properties |
| 업무 알림 | `MyRequestSlackNotifier` | 받을 사람을 찾아 문구를 만들고 발송 class 를 부름 |

::: info 근거
프로젝트 `src/` 의 slack 패키지 (발송 class, 문구 class, 업무 알림 class 1 개), `resources/custom/` 의 Slack 문구 properties 와 설정 properties 의 `slack.*` 키, 업무 알림을 부르는 JPO 1 곳 (지금은 주석 처리). class 이름과 문구는 일반적인 이름으로 바꿔 적었습니다
:::

## 설정

[설정 로더](/server-java/#설정-로더) 가 읽는 properties 에 둡니다.

```properties
slack.enabled=true
ENCRYPTED_slack.bot.token=<암호화한 봇 토큰>
slack.default.channel=dev-noti
```

| 키 | 내용 |
| --- | --- |
| `slack.enabled` | `true` 일 때만 보냅니다. 아니면 info 로그만 남기고 `false` 를 돌려줍니다 |
| `ENCRYPTED_slack.bot.token` | 봇 토큰 (`xoxb-...`). 암호화한 값이 우선이고, 없으면 평문 `slack.bot.token` 을 읽습니다 |
| `slack.default.channel` | 채널을 비우고 보낼 때의 채널 |

- 봇 토큰에는 메시지 보내기(`chat:write`)와 이메일로 사용자 찾기(`users:read.email`) 권한이 있어야 합니다. 원본에는 적혀 있지 않고 Slack API 의 메서드별 권한 기준입니다.
- 평문 키는 테스트용입니다. 주석으로라도 설정 파일에 토큰을 남기지 않습니다. 남겼다면 Slack 에서 토큰을 다시 발급합니다.

## 보내기

```java
String text = MySlackMessage.of("REQUEST", "CREATE")
        .add("Name", String.join(", ", itemNames))
        .add("Requester", requesterName)
        .link(spaceUrl + "/common/emxNavigator.jsp?objectId=" + projectId)
        .text();

MySlackSender.sendDirectMessages(emails, text);
```

| 메서드 | 반환 | 내용 |
| --- | --- | --- |
| `postMessage(channel, text)` | `boolean` | 채널(`dev-noti`) 또는 Slack 사용자 ID(`U...`)로 보냄. 비우면 `slack.default.channel` |
| `sendDirectMessage(email, text)` | `boolean` | 이메일로 Slack 사용자를 찾아 DM |
| `sendDirectMessages(emails, text)` | 보낸 건수 | 여러 명에게 같은 DM. 같은 주소는 한 번만 |
| `findUserIdByEmail(email)` | 사용자 ID 또는 `null` | `users.lookupByEmail` 로 찾음 |

### 부르는 Web API

| API | 방식 | 보내는 값 | 받는 값 |
| --- | --- | --- | --- |
| `https://slack.com/api/users.lookupByEmail` | GET `?email=...` | - | `user.id` |
| `https://slack.com/api/chat.postMessage` | POST JSON | `{ "channel": ..., "text": ... }` | `ok`, `error` |

두 요청 모두 머리글에 `Authorization: Bearer <토큰>` 을 넣습니다. DM 은 사용자 ID 를 `channel` 에 넣어 보냅니다.

::: warning 실패해도 HTTP 200 입니다
Slack 은 토큰이 틀려도, 사용자를 못 찾아도 HTTP 200 에 `{"ok": false, "error": "..."}` 로 답합니다. 응답 본문의 `ok` 를 봐야 합니다. PLM 의 이메일과 Slack 계정 이메일이 다르면 `users_not_found` 가 옵니다.
:::

### 예외를 던지지 않는 이유

알림이 실패했다고 원래 업무(요청 등록, 결재)를 되돌리면 안 되기 때문입니다. 발송 class 는 모든 오류를 `false` 반환과 error 로그로만 남깁니다.

- 연결 3 초, 읽기 5 초로 시간 제한을 짧게 둡니다. 트리거 안에서 불려도 트랜잭션을 오래 잡지 않게 하려는 것입니다.
- 업무 알림 class 는 **commit 다음에** 부릅니다. 원본 JPO 도 `ContextUtil.commitTransaction(context)` 바로 다음 줄에서 부르게 되어 있습니다 (지금은 주석 처리). 롤백된 일을 알리지 않기 위해서입니다.

## 문구 (properties)

문구 class 는 `ResourceBundle` 로 `custom/Slack.properties` 를 읽습니다.

```properties
COMMON.Label.Link=바로가기
COMMON.Label.Reason=사유
REQUEST.Label.Name=대상
REQUEST.Label.Requester=요청자
REQUEST.CREATE.Title=[PLM] 새 요청
```

| 키 | 내용 |
| --- | --- |
| `{도메인}.{이벤트}.Title` | 첫 줄 제목. 이벤트마다 하나 |
| `{도메인}.Label.{항목}` | `add(항목, 값)` 줄의 이름 |
| `COMMON.Label.{항목}` | 도메인에 이름이 없을 때 대신 찾는 공용 이름 |

위 예제로 만들어지는 본문입니다.

```text
[PLM] 새 요청
대상: A-01, A-02
요청자: 홍길동
<https://.../common/emxNavigator.jsp?objectId=...|바로가기>
```

- 값이 빈 항목은 줄을 만들지 않습니다.
- 링크는 Slack 링크 표기(`<주소|보이는 글자>`)로 붙여서 긴 주소 대신 "바로가기" 만 보입니다.
- 키가 없으면 제목은 이벤트 이름, 항목 이름은 항목 키 그대로 쓰고 warn 로그를 남깁니다. 문구가 빠져도 알림은 나갑니다.
- 원본 properties 는 한글을 `\uXXXX` 로 적습니다 (native2ascii). `ResourceBundle` 이 캐시하므로 문구를 바꾸면 재시작해야 합니다.

## 업무 알림 class

요청을 여러 건 한 번에 등록하면 프로젝트별로 묶어 프로젝트마다 한 통씩 보냅니다. 같은 프로젝트면 받을 사람(담당 부서 Group 의 owner)도 같기 때문입니다.

| 순서 | 내용 |
| --- | --- |
| 1 | 등록된 객체 id 목록에서 각 객체의 프로젝트 id 를 relationship 으로 읽어 프로젝트별로 이름을 모음 |
| 2 | 프로젝트에 연결된 Group 의 `owner` 를 읽음 |
| 3 | `PersonUtil.getEmail(context, 사용자)` 로 이메일을 찾음. 이메일이 없는 사용자는 warn 로그 후 건너뜀 |
| 4 | 문구를 만들고 `sendDirectMessages` 로 보냄 |

전체를 `try` 로 감싸 오류는 로그로만 남깁니다.

::: warning 원본의 테스트 수신자 스위치는 항상 켜져 있습니다
`sendDirectMessages` 의 설명은 "`slack.test.receiver` 가 설정돼 있으면 그 주소로만 보낸다" 인데, 원본 코드는 `boolean bTest = true;` 로 고정되어 있어 **모든 DM 이 고정된 테스트 주소로 갑니다.** 설정 파일에도 `slack.test.receiver` 키가 없습니다. 운영에 쓰려면 아래처럼 설정값을 읽게 고칩니다.

```java
String testReceiver = MyConfig.values.get("slack.test.receiver");
boolean test = testReceiver != null && !testReceiver.isEmpty();
```

수신자를 테스트 주소로 바꾼 **뒤에** 중복을 지우므로, 테스트 중에는 받을 사람이 여러 명이어도 한 통만 갑니다.
:::
