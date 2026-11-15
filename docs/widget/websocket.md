# 실시간 통신 (WebSocket) <Badge type="info" text="원본 분석" /> <Badge type="tip" text="로컬 실행 확인" />

위젯은 브라우저에서 도는 스크립트이므로 외부 WebSocket 서버에 연결할 수 있습니다. 브라우저 기본 `WebSocket` 을 쓰거나, 플랫폼에 들어 있는 socket.io-client 4.7.4 (`DS/DSNodeSocketioClient_4.7.4/DSNodeSocketioClient_4.7.4`) 를 씁니다. OOTB 의 알림(3DNotification)과 실시간 협업(RTDriver)이 socket.io 로 연결합니다.

다만 **연결할 서버는 따로 있어야 합니다.** 3DSpace 커스터마이징 소스(Java, JPO)에는 WebSocket 서버 쪽 코드가 없고, OOTB 가 연결하는 곳도 3DSpace 가 아니라 플랫폼의 별도 서버(3DNotification, RTC)입니다.

::: info 근거
NotificationManager/NotificationManager.js (`activeConnection` 의 연결 옵션과 `wss://` 붙이기, `connect` 뒤 `WAFData.authenticatedRequest(passportUrl + "/login?service=INTF")` 로 `access_token` 을 받아 `emit("new_user", ...)`, `getServiceUrl({ serviceName: "3dnotifications" })`, `listenVisibilityEvent`, `listenNetworkEvent`, `socketIdentifier` / `isElectedSocket`), RTDriver/RTDriver.js (`connectionParams` 의 `path: "/socketio.rtc"` / `"/socketio.conv"`, `getServiceTicket` 의 `"/login?service=3DSIM"`, `serverLogin` 의 `emit("login", ...)`), DSNodeSocketioClient_4.7.4/DSNodeSocketioClient_4.7.4.js (전역 `io` 가 있으면 그것, 없으면 `VENNodeSocketioClient_4.7.4/socket.io.min` 을 읽음). `enovia2025/src`, `enovia2025/jpo` 에서 `javax.websocket`, `@ServerEndpoint`, `WebSocket` 검색 결과 0 건. webapps 전체에서 `new WebSocket(` 을 쓰는 곳은 세지 않았습니다
:::

샘플: [WebSocketSample.html](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/WidgetSample/WebSocketSample.html) (브라우저 WebSocket / socket.io 고르기, 3DPassport 토큰 보내기, 다시 연결, 정리). 로컬 시험 서버(ws 에코, socket.io 4.7 에코)에 연결해 주고받기, 끊긴 뒤 다시 연결, 끊기를 Edge 로 확인했습니다. 3DDashboard 안에서 연결과 토큰 받기는 확인하지 않았습니다.

## 고르는 기준

| 방법 | 모듈 | 다시 연결 | 맞는 서버 |
| --- | --- | --- | --- |
| 브라우저 `WebSocket` | 없음 (브라우저 기본) | 직접 만듦 | 아무 WebSocket 서버 (Java `@ServerEndpoint`, Node `ws`, Spring 등) |
| socket.io-client 4.7.4 | `DS/DSNodeSocketioClient_4.7.4/DSNodeSocketioClient_4.7.4` | socket.io 가 함 | **socket.io 서버만** (Node `socket.io` 4.x 등). 일반 WebSocket 서버에는 붙지 않음 |

- 서버를 새로 만든다면 서버 쪽 기술에 맞춰 고릅니다. Node 로 만들면 socket.io, Java 로 만들면 브라우저 `WebSocket` 이 간단합니다.
- 폴더 이름에 버전이 없는 `DS/DSNodeSocketioClient/DSNodeSocketioClient` 도 같은 4.7.4 이지만 OOTB 가 쓰지 않습니다. 버전이 붙은 쪽을 씁니다 ([들어 있는 외부 라이브러리](/amd/libraries#통신-미디어-입력)).

## 주소

<<< @/../WidgetSample/WebSocketSample.html#url{js}

- 3DDashboard 와 3DSpace 는 https 이므로 `ws://` 는 혼합 콘텐츠로 막힙니다. **`wss://`** 로 연결합니다. OOTB 둘 다 받은 주소에서 `http(s)://` 를 떼고 `wss://` 를 붙입니다.
- 서버 주소를 코드에 넣지 않고 위젯 설정(`<widget:preference type="text">`)으로 받으면 운영, 시험 서버를 바꾸기 쉽습니다 ([위젯 설정](/widget/preferences)).
- OOTB 는 서버 주소를 Compass 에서 서비스 이름으로 받습니다 (`getServiceUrl({ serviceName: '3dnotifications' })`). 커스텀 서버는 Compass 에 등록되어 있지 않으므로 설정 값으로 받습니다.

## 브라우저 WebSocket

<<< @/../WidgetSample/WebSocketSample.html#native{js}

- 브라우저 `WebSocket` 은 끊기면 다시 연결하지 않습니다. 샘플은 1, 2, 4 ... 초(최대 30 초) 간격으로 10 번까지 다시 엽니다.
- 주고받는 값은 문자열(또는 바이너리)입니다. 종류를 나누려면 `{ type, data }` 같은 JSON 으로 감쌉니다.
- `close(1000, ...)` 로 닫은 것인지, 끊긴 것인지(`1006` 등)를 `onclose` 의 `code` 로 구분합니다. 샘플은 직접 닫은 경우 다시 연결하지 않습니다.

## socket.io-client 4.7.4

<<< @/../WidgetSample/WebSocketSample.html#socketio{js}

| 옵션 | OOTB 값 | 설명 |
| --- | --- | --- |
| `transports` | `['websocket']` (두 곳 모두) | 처음부터 WebSocket 으로 연결. 기본값(`polling` 먼저)은 HTTP 요청이 먼저 나감 |
| `path` | 3DNotification 은 기본(`/socket.io`), RTDriver 는 `/socketio.rtc`, `/socketio.conv` | 서버의 `path` 와 같아야 함. 프록시 뒤에서 경로를 나눌 때 바꿈 |
| `reconnection`, `reconnectionAttempts`, `reconnectionDelayMax` | 켬, 재시도 횟수 설정 값, 2 분 남짓 (NotificationManager) | 끊기면 socket.io 가 다시 연결 |
| `timeout` | 10 초 (NotificationManager), 15 초 (RTDriver) | 연결 제한 시간 |

- 모듈은 `io` 함수를 돌려줍니다. OOTB 는 `io.connect(url, options)` 로 부르고 샘플은 `io(url, options)` 로 부릅니다. 같은 함수입니다.
- 위젯 HTML 에 socket.io 를 따로 `<script>` 로 넣어 전역 `io` 가 있으면 모듈은 그 전역 `io` 를 돌려줍니다. 버전이 섞이지 않게 모듈 하나만 씁니다.

::: warning 서버가 끊으면 socket.io 는 다시 연결하지 않습니다
서버가 `socket.disconnect()` 로 끊으면 `disconnect` 이유가 `'io server disconnect'` 이고 자동 다시 연결을 하지 않습니다 (socket.io 사양, 로컬 시험에서 확인). 네트워크가 끊긴 경우(`'transport close'` 등)는 다시 연결합니다. 서버가 끊은 뒤에도 다시 붙어야 하면 `socket.connect()` 를 직접 부릅니다. 샘플은 탭이 다시 보일 때 `ensure()` 에서 부릅니다.
:::

## 인증

WebSocket 연결에는 `WAFData` 처럼 로그인 정보가 자동으로 붙지 않습니다. 서버가 누구인지 알아야 하면 연결한 뒤 토큰을 보냅니다. OOTB 는 두 곳 모두 같은 순서입니다.

| 순서 | 3DNotification | RTDriver |
| --- | --- | --- |
| 1. 연결 | `connect` 이벤트 | `connect` 이벤트 |
| 2. 토큰 받기 | `WAFData.authenticatedRequest(<3DPassport>/login?service=INTF, { type: 'json' })` → `access_token` | `<3DPassport>/login?service=3DSIM` → `access_token` |
| 3. 보내기 | `emit('new_user', { pseudo: 로그인, pass: 토큰, language, tenant })` | `emit('login', { login, username, st: 토큰, tenant, ... })` |
| 4. 서버 | 3DPassport 로 토큰을 확인하는 것으로 보입니다 <Badge type="danger" text="검증 필요" /> | 같음 <Badge type="danger" text="검증 필요" /> |

<<< @/../WidgetSample/WebSocketSample.html#token{js}

- 3DPassport 주소는 `PlatformAPI.getApplicationConfiguration('app.urls.passport')` 로 얻습니다 ([PlatformAPI](/widget/platform-api#getapplicationconfiguration-키)). 3DDashboard 밖에서는 비어 있습니다.
- `service=` 뒤의 이름은 3DPassport 에 등록된 서비스여야 하는 것으로 보입니다 <Badge type="danger" text="검증 필요" />. `INTF`, `3DSIM` 은 플랫폼 서비스용이므로, 커스텀 서버용 서비스를 3DPassport 에 등록할 수 있는지와 서버에서 토큰을 확인하는 방법은 플랫폼 관리자에게 확인합니다.
- 다시 연결될 때마다 토큰을 새로 받아 보냅니다. OOTB 도 `connect` 이벤트마다 받습니다.
- 3DPassport 서비스 등록이 어렵다면, 3DSpace 에 로그인 세션으로만 부를 수 있는 REST(JPO) 를 하나 두고 거기서 짧게 쓰는 서명 토큰을 만들어 WebSocket 서버가 확인하는 방법도 있습니다. 소스에 사례는 없는 설계안입니다.

## 연결, 정리

<<< @/../WidgetSample/WebSocketSample.html#connect{js}

| 일 | 하는 방법 | OOTB |
| --- | --- | --- |
| 탭이 다시 보이면 끊긴 연결 다시 열기 | `document` 의 `visibilitychange` | NotificationManager `listenVisibilityEvent` |
| 네트워크가 돌아오면 다시 열기 | `window` 의 `online` / `offline` | NotificationManager `listenNetworkEvent` |
| 위젯을 지우거나 페이지를 떠날 때 닫기 | `pagehide` (또는 `unload`) | 다른 위젯 연동도 창 `unload` 에서 정리 ([6W 태그 연동](/widget/tagger)) |
| 설정을 바꾸면 다시 연결 | `endEdit` 에서 닫고 새로 열기 | - |

::: tip 연결 수
위젯마다, 탭마다 연결이 하나씩 생깁니다. 같은 위젯을 대시보드 여러 탭에 두면 연결도 그만큼 늘어납니다. NotificationManager 는 여러 곳 중 하나만 소켓을 열도록 고르는 값(`socketIdentifier`, `isElectedSocket`)을 둡니다. 연결 수가 문제가 되면 한 위젯만 연결하고 [PlatformAPI publish / subscribe](/widget/platform-api) 로 다른 위젯에 나눠 주는 방법을 생각합니다.
:::

## 서버와 네트워크

| 확인할 것 | 내용 |
| --- | --- |
| 인증서 | `wss://` 이므로 브라우저가 믿는 인증서가 필요합니다. 사설 인증서면 연결이 조용히 실패합니다 (`onerror` 뒤 `1006`) |
| 리버스 프록시 | 3DSpace 앞의 프록시(Apache, Nginx 등) 뒤에 둘 때는 `Upgrade`, `Connection` 헤더를 넘기고 읽기 시간 제한을 늘려야 합니다 |
| Origin | WebSocket 은 CORS 를 적용받지 않으므로 서버가 `Origin` 헤더를 직접 확인합니다. 3DDashboard 위젯 iframe 에서 연결할 때 `Origin` 이 어떤 주소가 되는지는 확인하지 않았습니다 <Badge type="danger" text="검증 필요" /> |
| 콘텐츠 보안 정책 (CSP) | 대시보드나 3DSpace 가 `connect-src` 를 제한하면 막힙니다. webapps 소스로는 확인할 수 없습니다 <Badge type="danger" text="검증 필요" /> |
| 3DSpace 안에 서버 두기 | 커스터마이징 소스에 사례가 없습니다. 별도 서버(Node, Spring 등)로 두는 편이 OOTB 구조와 같습니다 |

## 로컬에서 시험하기

샘플은 `ws://` 로 시작하는 주소는 그대로 씁니다. [샘플 서버](/guide/#샘플-소스) (`npm run samples`, http) 로 `WebSocketSample.html` 을 열고, 같은 PC 에 시험용 서버를 띄워 연결합니다.

| 클라이언트 | 시험 서버 | 설정의 WebSocket server |
| --- | --- | --- |
| 브라우저 WebSocket | Node `ws` 로 받은 JSON 의 `data` 를 그대로 돌려주는 서버 | `ws://127.0.0.1:<포트>/<경로>` |
| socket.io | Node `socket.io` 4.x 서버, `message` 이벤트를 그대로 돌려줌 | `ws://127.0.0.1:<포트>` (path 는 기본 `/socket.io`) |

단독 실행에서는 3DPassport 주소가 없으므로 `passportService` 설정을 비워 둡니다.
