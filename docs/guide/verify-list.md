# 검증 대기 목록

본문에 <Badge type="danger" text="검증 필요" /> 배지가 붙은 추정 사양 227 곳을 모은 목록입니다. 실무에 쓰기 전에 서버에서 확인합니다.

- 각 줄 앞의 링크는 그 문장이 있는 절로 갑니다.
- 확인했으면 원래 페이지에서 배지를 지우고 문장을 단정형으로 고칩니다. 다르게 동작하면 내용을 고칩니다.
- 이 페이지는 `npm run docs:verify-list` 로 다시 만듭니다. 직접 고치지 않습니다.

## 데이터 모델 / MQL (21)

### Attribute, Type, Relationship, Role (3)

- [자료형](/schema/admin#자료형) : `timestamp` : `date` 와 저장 정밀도가 어떻게 다른지는 확인하지 못했습니다
- [자료형](/schema/admin#자료형) : 키워드는 대소문자를 가리지 않는 것으로 보입니다.
- [나중에 고치기](/schema/admin#나중에-고치기) : `mod type ... remove attribute` 를 하면 그 type 의 기존 객체에 있던 값도 사라지는 것으로 보입니다.

### mql 폴더 작성 규칙 (5)

- [본문 순서](/schema/conventions#본문-순서) : 운영에 이미 반영된 객체는 `mod` 만 다시 실행하면 되기 때문으로 보입니다.
- [본문 순서](/schema/conventions#본문-순서) : 정의를 지우면 그 attribute 값이나 연결이 함께 사라지는 것으로 보입니다.
- [OOTB 수정 규칙](/schema/conventions#ootb-수정-규칙) : 같은 이름의 사본이 있으면 `copy` 가 실패하기 때문으로 보입니다.
- [데이터 이관 (98.migration)](/schema/conventions#데이터-이관-98-migration) : 엑셀 같은 원본에서 한 줄 = 명령 하나로 만들어 낸 것으로 보입니다.
- [데이터 이관 (98.migration)](/schema/conventions#데이터-이관-98-migration) : 이관한 번호와 채번기의 `eService Next Number` 가 겹치지 않도록 번호 구간을 나누거나 다음 번호를 맞춰 두어야 하는 것으로 보입니다.

### 데이터 모델 개요 (1)

- [심볼릭 이름](/schema/#심볼릭-이름) : `getSchemaProperty` 는 `eServiceSchemaVariableMapping.tcl` 에 붙은 property 를 보고 실제 이름을 찾는 것으로 보입니다

### MQL 문법과 조회 (7)

- [기본 규칙](/schema/mql#기본-규칙) : 대소문자 : 키워드는 섞어 써도 동작하는 것으로 보입니다
- [business object 명령](/schema/mql#business-object-명령) : 대상 객체의 수정 일시를 바꾸지 않는 옵션으로 보입니다.
- [temp query bus](/schema/mql#temp-query-bus) : `T N R` : type 의 자식 type 도 함께 찾는 것으로 보입니다
- [expand bus](/schema/mql#expand-bus) : `terse` : type / name / revision 대신 id 위주로 짧게 출력하는 것으로 보입니다
- [expand bus](/schema/mql#expand-bus) : 출력은 한 줄에 연결 하나이고, 단계, 관계, 방향, 반대편 type / name / revision, select 값이 차례로 나오는 것으로 보입니다.
- [실행 환경](/schema/mql#실행-환경) : `set context user ... role 'ctx::역할.조직.협업공간'` : 권한 검사와 객체의 조직 / 협업공간에 영향을 주는 것으로 보입니다
- [Java 에서 MQL 부르기](/schema/mql#java-에서-mql-부르기) : 공백이 있는 이름도 따옴표 없이 그대로 넘기는 것으로 보아 따옴표 처리를 대신해 주는 것으로 보입니다.

### Policy (상태와 권한) (5)

- [조건](/schema/policy#조건) : `single organization` : 객체의 조직과 사용자 로그인 컨텍스트의 조직이 같을 때로 보입니다
- [조건](/schema/policy#조건) : `single project` : 객체의 협업공간(project)과 로그인 컨텍스트의 협업공간이 같을 때로 보입니다
- [trigger](/schema/policy#trigger) : `Override` : 이벤트의 기본 동작 대신 실행되는 것으로 보입니다
- [문법에는 있지만 프로젝트에서 안 쓰는 것](/schema/policy#문법에는-있지만-프로젝트에서-안-쓰는-것) : `enforcereserveaccess` : 예약(reserve)한 사용자만 고칠 수 있게 하는 policy 설정으로 보임
- [문법에는 있지만 프로젝트에서 안 쓰는 것](/schema/policy#문법에는-있지만-프로젝트에서-안-쓰는-것) : 프로젝트의 결재는 이 기능 대신 Route(`Object Route` 관계)와 trigger 로 처리하는 것으로 보입니다.

## UI 구성 요소 (26)

### command, menu (4)

- [command 정의](/ui-components/command-menu#command-정의) : `nothidden` / `hidden` : 화면 동작과는 무관한 것으로 보입니다
- [어디에 열까: Target Location](/ui-components/command-menu#어디에-열까-target-location) : 프레임 이름 비교는 대소문자를 가리는 것으로 보입니다.
- [창 크기](/ui-components/command-menu#창-크기) : `가로x세로` 형식(`1500x1000`)은 `emxFramework.PopupSize` 목록에 등록된 것만 쓰는 것으로 보입니다.
- [보일지 말지: 접근 setting](/ui-components/command-menu#보일지-말지-접근-setting) : `Access Expression` : 현재 객체 기준으로 평가하는 것으로 보입니다.

### portal, channel (1)

- [channel 정의](/ui-components/portal-channel#channel-정의) : `setting 'Height'` : 높이는 행 단위로 정해지는 것으로 보입니다

### settings 레퍼런스 (10)

- [settings 레퍼런스](/ui-components/settings) : `Input Type1`, `On Change Handler1`, `OnFocus Handler1` : 이름을 바꿔 임시로 끈 것으로 보입니다
- [command](/ui-components/settings#command) : `Mode` : 구조 브라우저의 보기 / 편집 중 어느 쪽 툴바에 보일지로 보입니다
- [command](/ui-components/settings#command) : `Range Values`, `Range Display Values` : 툴바 combobox 선택지를 직접 적는 것으로 보입니다
- [command](/ui-components/settings#command) : `Pull Right` : 하위 메뉴를 오른쪽으로 펼치는 것으로 보입니다
- [menu (툴바, 트리)](/ui-components/settings#menu-툴바-트리) : `Tree Scope ID` : 트리 노드에 붙일 범위 id 로 보입니다
- [menu (툴바, 트리)](/ui-components/settings#menu-툴바-트리) : `ImageDrop Relationship` : 트리에 이미지를 끌어 놓을 때 쓸 관계로 보입니다
- [표시](/ui-components/settings#표시) : `Group By` : 행 묶기 기준 열로 보입니다
- [편집](/ui-components/settings#편집) : `Type Ahead Mapping` : 자동 완성 결과에서 칸에 넣을 값으로 보입니다
- [편집](/ui-components/settings#편집) : `Validate Type` : 기본 금지 문자 검사로 보입니다
- [편집](/ui-components/settings#편집) : `Type Ahead Validate` : 자동 완성 결과 값만 허용하는 것으로 보입니다

### table (목록, 구조 브라우저) (3)

- [table 정의](/ui-components/table#table-정의) : 붙이지 않으면 사용자 개인 table 로 보입니다.
- [열 종류: Column Type, Field Type](/ui-components/table#열-종류-column-type-field-type) : 이 둘은 `Field Type` 쪽 값으로 보입니다.
- [편집 가능한 열](/ui-components/table#편집-가능한-열) : `Field Type` `attribute` 열은 `Update ...` 가 없어도 attribute 에 바로 저장되는 것으로 보입니다.

### URL 파라미터 (8)

- [공통](/ui-components/url-parameters#공통) : `header`, `subHeader` : `UINavigatorUtil.parseHeader` 가 `objectId` 와 함께 풀어서, 키 값 안의 `$<select식>` 도 바뀌는 것으로 보입니다
- [행과 열](/ui-components/url-parameters#행과-열) : `type` : 행 추가 / 필터에서 쓰는 타입으로 보입니다
- [행과 열](/ui-components/url-parameters#행과-열) : `objectBased` : 행이 객체가 아닌 데이터일 때 쓰는 것으로 보입니다
- [행과 열](/ui-components/url-parameters#행과-열) : `connectionProgram` : 행을 끼워 넣을 때 연결을 처리하는 것으로 보입니다
- [모양](/ui-components/url-parameters#모양) : `pageSize` : 한 번에 그릴 행 수로 보입니다
- [모양](/ui-components/url-parameters#모양) : 그래서 주소에 소문자만 주면 칸은 줄바꿈되지만 툴바에는 값이 가지 않아, 줄바꿈 버튼이 "켜기" 아이콘으로 보일 수 있습니다
- [편집](/ui-components/url-parameters#편집) : `insertNewRow` : 새 행 추가 끄기로 보입니다
- [편집](/ui-components/url-parameters#편집) : `editRelationship`, `resequenceRelationship` : 행 추가 / 순서 바꾸기에 쓸 관계로 보입니다

## JPO (9)

### 호출 종류별 인자 구조 (3)

- [구조 펼치기 program](/jpo/args#구조-펼치기-program) : JPO 가 단계를 직접 처리했다는 표시로 보입니다.
- [셀 스타일 (Style Function)](/jpo/args#셀-스타일-style-function) : 열 설정에 붙으므로 그 열의 셀에 적용되는 것으로 보입니다.
- [postProcessJPO](/jpo/args#postprocessjpo) : `void` 는 `JPO.invoke` 가 `null` 을 돌려주므로 편집 폼에서도 실패하지 않는 것으로 보입니다.

### DomainObject 와 select (2)

- [연결, 끊기](/jpo/domain-object#연결-끊기) : `DomainRelationship.connect(context, fromId, relName, toId, preserve)` : id 로 연결. `preserve=true` 면 MQL `connect ... preserve` 로 실행 (수정 일자를 바꾸지 않는 옵션으로 보입니다
- [관계](/jpo/domain-object#관계) : `attribute[없는이름]` 처럼 이름을 틀리면 예외 없이 `null` / 빈 값이 오는 것으로 보입니다.

### JPO 개요 (1)

- [Base / 파생 구조](/jpo/#base-파생-구조) : Base 를 직접 고치면 업그레이드 때 사라지는 것으로 보입니다.

### MqlUtil (1)

- [트리거 / 히스토리 끄기](/jpo/mql-util#트리거-히스토리-끄기) : `trigger off` 는 같은 context (세션) 의 이후 명령 전체에 적용되는 것으로 보입니다.

### Context, 트랜잭션, 권한 전환 (2)

- [UI 가 열어 주는 트랜잭션](/jpo/transaction-context#ui-가-열어-주는-트랜잭션) : 트리거는 객체를 바꾸는 MQL 명령의 트랜잭션 안에서 실행되는 것으로 보입니다.
- [슈퍼유저로 바꾸기 (pushContext)](/jpo/transaction-context#슈퍼유저로-바꾸기-pushcontext) : 이력 (history) 의 사용자도 User Agent 로 남는 것으로 보입니다.

## 트리거 (8)

### 채번기 (Object Generator) (2)

- [속성](/trigger/generator#속성) :  : 기본 경로(슈퍼유저로 생성)는 `eServicecommonGenerateObject.tcl` 에 넘겨 처리하며 이 프로그램은 원본에 없음
- [번호가 만들어지는 방식](/trigger/generator#번호가-만들어지는-방식) : `FrameworkUtil.autoName` 도 같은 방식으로 보입니다.

### 트리거 개요 (2)

- [이벤트](/trigger/#이벤트) : type 의 Connect / Disconnect 는 그 type 객체에 관계가 연결 / 해제될 때 실행되는 것으로 보입니다.
- [Check, Action, Override](/trigger/#check-action-override) : `Override` : 이벤트의 기본 동작 대신 실행되는 것으로 보입니다

### 트리거 JPO 와 인자 (1)

- [macro 치환](/trigger/jpo-args#macro-치환) : 없는 변수 : 빈 값으로 보입니다

### 라이프사이클과 promote (2)

- [promote 한 번에 일어나는 일](/trigger/lifecycle#promote-한-번에-일어나는-일) : Action 이 예외를 던지면 state 변경도 남지 않는 것으로 보입니다.
- [서명과 자동 promote](/trigger/lifecycle#서명과-자동-promote) : `${AUTOPROMOTE}` : 트리거 안에서 자동 promote 인지 구분하는 RPE 변수로 보입니다

### 트리거 등록 (1)

- [2. admin 에 붙이기](/trigger/register#_2-admin-에-붙이기) : 내 이름만 적으면 OOTB 트리거가 빠지는 것으로 보입니다.

## properties (3)

### 문자열 리소스 (다국어) (1)

- [파일 규칙](/properties/string-resource#파일-규칙) : `이름_koNative.properties` : 사람이 읽는 원본으로 보입니다.

### 시스템 설정과 page 객체 (2)

- [앱 등록 키](/properties/system-settings#앱-등록-키) : `.Directory` : command href 의 `${SUITE_DIR}` 가 이 폴더로 바뀌는 것으로 보입니다
- [앱 등록 키](/properties/system-settings#앱-등록-키) : 프로젝트처럼 Custom 에만 앱을 등록했을 때 앱 설정 파일의 키가 어느 경로로 읽히는지는 서버에서 확인해야 합니다.

## 서버 Java (18)

### 외부 DB (MyBatis) (1)

- [서버에 넣기](/server-java/external-db#서버에-넣기) : jar : 분석한 `WEB-INF/lib` 에는 없어서 실제 위치는 확인하지 못했습니다.

### 서버 Java 개요 (3)

- [서버에서 Context 만들기](/server-java/#서버에서-context-만들기) : 닫지 않으면 서버에 세션이 남는 것으로 보입니다.
- [서버에서 Context 만들기](/server-java/#서버에서-context-만들기) : 객체를 만드는 배치는 소유 조직과 협업공간이 여기서 정해지므로 반드시 지정합니다.
- [JPO 에서 요청 객체 쓰기](/server-java/#jpo-에서-요청-객체-쓰기) : 분석한 `web.xml` 에는 이 등록이 없어서 실제 등록 형태는 확인하지 못했습니다.

### 메일 (3)

- [시스템 오류 메일](/server-java/mail#시스템-오류-메일) : 원본에는 같은 오류를 묶거나 횟수를 제한하는 처리가 보이지 않습니다.
- [서버에 넣기](/server-java/mail#서버에-넣기) : jar : 분석한 `WEB-INF/lib` 에는 없어서 실제 위치는 확인하지 못했습니다.
- [서버에 넣기](/server-java/mail#서버에-넣기) : 원본 설정에는 템플릿 캐시를 켜는 키가 없어서 바로 반영되는 것으로 보입니다.

### 독립 실행 이관 프로그램 (1)

- [실행 bat](/server-java/migration#실행-bat) : 바깥의 `abort` 가 이미 만든 항목까지 되돌리는지, 항목 하나의 `abortTransaction` 이 바깥 트랜잭션까지 끝내는지는 `ContextUtil` 의 중첩 처리에 달려 있습니다

### 커스텀 REST 서비스 (6)

- [Modeler](/server-java/rest-service#modeler) : `@ApplicationPath` 어노테이션을 서버가 찾아서 등록하는 것으로 보입니다.
- [로그인 사용자의 Context](/server-java/rest-service#로그인-사용자의-context) : 뜻은 확인하지 못했습니다.
- [로그인 사용자의 Context](/server-java/rest-service#로그인-사용자의-context) : OOTB 서비스와 같은 틀을 쓰므로 필요한 것으로 보입니다.
- [응답 형식](/server-java/rest-service#응답-형식) : Context 를 얻지 못한 경우(로그인 만료)도 같은 형태로 오는 것으로 보입니다.
- [외부 시스템이 부르는 주소](/server-java/rest-service#외부-시스템이-부르는-주소) : 외부 시스템이 어떤 방식으로 CAS 인증을 통과해 부르는지는 확인하지 못했습니다.
- [위젯에서 부르기](/server-java/rest-service#위젯에서-부르기) : 원본 webapps 폴더에는 이 서비스를 부르는 위젯 소스가 없어서 위 호출은 OOTB 서비스를 부르는 방식([서버 호출](/uwa/request))을 그대로 적용한 것입니다.

### 스케줄러 (4)

- [Job](/server-java/scheduler#job) : 몇 분마다 도는 Job 이므로 `finally` 에서 닫는 편이 안전한 것으로 보입니다.
- [Job](/server-java/scheduler#job) : 원본에는 붙어 있지 않습니다.
- [서버에 넣기](/server-java/scheduler#서버에-넣기) : 분석한 `web.xml` 에는 이 등록이 없어서 위 형태는 서블릿 표준에 따른 추정입니다.
- [서버에 넣기](/server-java/scheduler#서버에-넣기) : 원본에는 이에 대한 처리가 보이지 않습니다.

## 서버 반영 (10)

### 서버 반영 절차 (10)

- [한눈에 보기](/deploy/#한눈에-보기) : JPO : 필요 없는 것으로 보입니다
- [MQL (스키마, UI 정의)](/deploy/#mql-스키마-ui-정의) : `-del` 은 되돌릴 때 쓰는 삭제 묶음으로 보입니다.
- [JPO](/deploy/#jpo) : 일반적으로 알려진 MQL 은 아래 형태입니다.
- [JPO](/deploy/#jpo) : 컴파일 결과 메시지를 꼭 확인합니다.
- [JPO](/deploy/#jpo) : 다른 JPO 가 `${CLASS:MyPartUI}` 로 참조하는 JPO 를 고쳤으면 참조하는 쪽도 다시 컴파일해야 하는 것으로 보입니다.
- [캐시 하나만 비우기 (JPO)](/deploy/#캐시-하나만-비우기-jpo) : `emxUtil` `loadFrameworkProperty` : 다시 읽기를 일으키는 용도로 보임
- [문자열 리소스까지 비우기](/deploy/#문자열-리소스까지-비우기) : JDK 버전이 바뀌면 필드 이름이나 접근 제한 때문에 실패할 수 있는 것으로 보입니다.
- [커스텀 Java (src/)](/deploy/#커스텀-java-src) : 서블릿이나 필터를 직접 추가하면 제품 업데이트 때 다시 만들어지면서 사라질 수 있으므로 추가한 블록을 따로 보관해 둡니다.
- [커스텀 리소스 (resources/custom/)](/deploy/#커스텀-리소스-resources-custom) : 메일 템플릿 (`.vm`) : 엔진 캐시 설정에 따름
- [반영 순서](/deploy/#반영-순서) : 이 순서는 참조 관계에서 정리한 것이고 원본에 반영 절차서가 있는 것은 아닙니다.

## AMD 로딩 (5)

### 들어 있는 외부 라이브러리 (1)

- [React](/amd/libraries#react) : 한 창에서 둘을 같이 부르면 먼저 읽힌 React 가 쓰이는 것으로 보입니다

### 공통 유틸 (DS/Utilities, Logger ...) (3)

- [로그 수준](/amd/utilities#로그-수준) : sessionStorage 는 위젯 프레임의 출처(origin) 것이어야 하므로 3DDashboard 에서는 개발자 도구 콘솔의 대상 프레임을 위젯으로 바꾸고 넣는 것으로 보입니다
- [PointerEvents](/amd/utilities#pointerevents) : 브라우저가 펜 입력을 mouse 이벤트로도 보내므로 마우스와 같은 이벤트로 오는 것으로 보입니다
- [2026 변경](/amd/utilities#_2026-변경) : `KeyboardUtils` : 키 이름에 맞는 사용자 영역(PUA) 유니코드 글자를 돌려줌 (단축키 표시 글꼴용으로 보임

### WebappsUtils (1)

- [로드될 때 하는 일](/amd/webapps-utils#로드될-때-하는-일) : 3DDashboard 위젯 프레임에서는 AmdLoader 를 프레임이 먼저 읽으므로 webapps 주소도 프레임이 정하는 것으로 보입니다.

## 위젯과 플랫폼 (66)

### 3D / 2D 미리 보기 (3DPlay) (6)

- [타입별로 열리는 화면](/widget/3dplay#타입별로-열리는-화면) : 아래 두 행은 연결 모듈 이름과 코드 흐름으로 본 동작입니다
- [options](/widget/3dplay#options) : `loading` : 스플래시를 둔 채 화면을 미리 만듦
- [3DPlay 앱으로 열기 (TransientWidget)](/widget/3dplay#_3dplay-앱으로-열기-transientwidget) : 3DPlay 쪽에서 이 두 값을 어떻게 쓰는지는 확인하지 않았습니다
- [3DDashboard 밖에서](/widget/3dplay#_3ddashboard-밖에서) : `Player3DPlayWeb` : 3DSpace 데이터를 passport 인증으로 읽는 부분까지 동작하는지는 확인하지 않음
- [3DDashboard 밖에서](/widget/3dplay#_3ddashboard-밖에서) : 3DPlaySyndication.html : 대시보드 밖에서 그려지는지는 위와 같음
- [2026 에서 달라진 것](/widget/3dplay#_2026-에서-달라진-것) : 2026 의 두 앱 정의 중 대시보드가 실제로 어느 쪽을 쓰는지는 확인하지 않았습니다

### Compass 에 선택 객체 알리기 (i3DXCompassPubSub) (4)

- [Compass 가 객체로 하는 일](/widget/compass-pubsub#compass-가-객체로-하는-일) : 3DDashboard 위젯 앱 : 열린 앱은 위젯 값 `X3DContentId` 로 받는 것으로 보입니다
- [앱 실행 (launchApp)](/widget/compass-pubsub#앱-실행-launchapp) : `silent` : 안내 없이 처리하는 것으로 보입니다
- [네이티브 앱에 선택 넘기기 (setTypes)](/widget/compass-pubsub#네이티브-앱에-선택-넘기기-settypes) : 키 전체의 뜻은 webapps 에 없습니다
- [OpenWith 와의 관계](/widget/compass-pubsub#openwith-와의-관계) : 호환 앱 항목을 누름 : callback 은 응답에 `error` 가 있으면 Compass 를 열고 안내하는데, Compass 응답은 `error` 가 아니라 `response` 키라서 이 안내는 나오지 않는 것으로 보입니다

### Compass 서비스 (i3DXCompassPlatformServices) (1)

- [OpenWith](/widget/compass-services#openwith) : 열리는 앱은 이 `content` 를 위젯 값 `X3DContentId` (JSON 문자열)로 받는 것으로 보입니다

### 3DDashboard 에 위젯 올리기 (12)

- [배포 위치와 주소](/widget/dashboard-register#배포-위치와-주소) : 반영 : 정적 파일이라 서버를 다시 시작하지 않아도 되는 것으로 보입니다
- [배포 위치와 주소](/widget/dashboard-register#배포-위치와-주소) : 서버가 여러 대이면 모든 서버의 같은 위치에 넣어야 하는 것으로 보입니다.
- [Run Your App 으로 열기](/widget/dashboard-register#run-your-app-으로-열기) : Run Your App 이 앱 목록에 보이는지는 부여된 역할에 따라 다른 것으로 보입니다.
- [동작](/widget/dashboard-register#동작) : 바뀐 뒤에는 새 위젯의 설정 항목(`<widget:preferences>`)이 적용되고, 탭을 다시 열어도 바뀐 위젯으로 남는 것으로 보입니다.
- [Additional App 으로 등록](/widget/dashboard-register#additional-app-으로-등록) : 등록 버튼이 보이려면 플랫폼 관리자 권한이 있어야 하는 것으로 보입니다.
- [입력 항목](/widget/dashboard-register#입력-항목) : 위젯은 `Widget` 을 고르는 것으로 보입니다.
- [입력 항목](/widget/dashboard-register#입력-항목) : 위젯 설정 초기값을 담은 파일 주소인 것으로 보입니다.
- [입력 항목](/widget/dashboard-register#입력-항목) : Object Type Compatibility 는 그 type 의 객체에서 "다른 앱으로 열기" 를 했을 때 이 앱이 후보로 나오게 하는 값으로 보입니다.
- [저장 요청](/widget/dashboard-register#저장-요청) : Platform Management 의 다른 요청이 Compass(My Apps) 주소 뒤에 `/resources/AppsMngt/` 를 붙여 쓰므로 전체 주소는 아래와 같은 것으로 보입니다.
- [저장 요청](/widget/dashboard-register#저장-요청) : 수정은 `id` 를 넣은 같은 본문을 보내는 것으로 보입니다.
- [Internal 저장](/widget/dashboard-register#internal-저장) : 3DSpace `webapps/` 의 AMD 모듈을 상대 경로로 부르는 위젯은 주소가 달라져 모듈을 찾지 못하므로 External 로 등록하는 편이 안전한 것으로 보입니다.
- [등록한 뒤](/widget/dashboard-register#등록한-뒤) : 구성원은 Compass 에서 Short Name 으로 앱을 찾아 대시보드 탭에 올리는 것으로 보입니다.

### 언어와 사용자 환경설정 (I18n, MePreferences) (4)

- [지금 언어 얻기](/widget/i18n-preferences#지금-언어-얻기) : 3DDashboard 가 위젯 프레임에 Me 메뉴의 언어를 넣어 주는지는 webapps 원본에 없는 대시보드 쪽 일이라 확인하지 못했습니다
- [OOTB 가 읽는 저장소와 키](/widget/i18n-preferences#ootb-가-읽는-저장소와-키) : `LengthDisplay` 는 화면 표시용 자릿수로 보이나 `...CalcDisplay` 와의 차이는 원본에서 확인하지 못했습니다
- [값이 바뀔 때](/widget/i18n-preferences#값이-바뀔-때) : 3DDashboard 또는 서버 알림으로 보이며, 다른 탭에서 바꾼 경우에도 오는지는 확인하지 못했습니다
- [2026 변경](/widget/i18n-preferences#_2026-변경) : `getAppUserPreferences` 가 `openInNewTab` 말고 무엇을 돌려주는지는 확인하지 못했습니다

### 위젯 (2)

- [실행 환경](/widget/#실행-환경) : 3DDashboard : 대시보드의 위젯 프레임 (iframe, 주소에 `/api/widget/frame` 이 들어간 것으로 보임
- [실행 환경](/widget/#실행-환경) : 3DDashboard 는 위젯 HTML 을 읽어 제목, 메타, 설정 항목, 본문, 스크립트를 뽑아 다시 실행하는 것으로 보입니다

### PlatformAPI (1)

- [publish / subscribe](/widget/platform-api#publish-subscribe) : `targets` : 데스크톱 앱 안 위젯(Web-in-Win)용으로 보입니다

### 위젯 설정 (preferences) (2)

- [getValue 가 값을 찾는 순서](/widget/preferences#getvalue-가-값을-찾는-순서) : 대시보드에서는 프레임이 값을 대시보드로 보내 위젯마다 저장하는 것으로 보입니다.
- [설정 창 흐름](/widget/preferences#설정-창-흐름) : 2 : 대시보드가 `getPreferences()` 결과로 그리는 것으로 보임

### 3DSearch 에서 객체 골라 오기 (14)

- [socket_id 와 widget_id](/widget/search#socket-id-와-widget-id) : `widget_id` 에 실제 `widget.id` 를 넣고 해제하지 않으면, 그 위젯을 최대화했을 때 상단 검색창이 이 등록 정보로 "앱 안에서 찾기" 를 하는 것으로 보입니다
- [옵션](/widget/search#옵션) : `default_with_precond` : 아니면 검색어 없이 검색창에 포커스만 주는 것으로 보입니다
- [옵션](/widget/search#옵션) : `search_delegation` : `'3ddashboard'` 면 "이 탭에서 찾기" 쪽으로 가는 것으로 보입니다
- [옵션](/widget/search#옵션) : `closeTransient` : `false` 면 OK 뒤 창을 닫지 않는 것으로 보입니다
- [옵션](/widget/search#옵션) : 검색 조건도 저장된 값이 쓰여서, LifecycleWidget 처럼 등록은 빈 값으로 하고 `InContextSearch` 에만 조건을 주면 첫 검색에만 조건이 걸리는 것으로 보입니다
- [precond](/widget/search#precond) : `flattenedtaxonomies` 는 타입 계층을 펼친 값이라 하위 타입도 걸리는 것으로 보입니다
- [precond](/widget/search#precond) : OOTB 에서 쓰는 이름을 보고 추정한 규칙입니다
- [동작 방식](/widget/search#동작-방식) : 3DSpace 기본 화면 : 소켓은 바로 위 창에만 붙으므로 그 창이 상단 바를 가진 창일 때만 닿는 것으로 보입니다
- [동작 방식](/widget/search#동작-방식) : 대시보드와 origin 이 다른 iframe : InterCom 이 `postMessage` 대상 origin 을 자기 주소로 정해서 전달되지 않는 것으로 보입니다
- [주소](/widget/search#주소) : Compass 의 3DSearch 주소가 이미 `/federated` 로 끝나는 것으로 보입니다
- [요청 본문](/widget/search#요청-본문) : `label` : 기록용으로 보입니다
- [요청 본문](/widget/search#요청-본문) : `with_indexing_date`, `with_synthesis`, `with_nls` : 인덱스 날짜, 태그 집계(`facets`), 번역된 이름을 같이 받는 것으로 보입니다
- [요청 본문](/widget/search#요청-본문) : `login` 을 빼면 사용자 기본 SecurityContext 를 쓰는 것으로 보입니다
- [응답](/widget/search#응답) : `resourceid` 는 `select_predicate` 에 넣지 않아도 오는 것으로 보입니다

### 3DSpace 주소와 SecurityContext (2)

- [플랫폼 id](/widget/space-url#플랫폼-id) : 온프레미스 환경의 플랫폼 id 는 `OnPremise` 로 보입니다.
- [목록과 기본값 받기](/widget/space-url#목록과-기본값-받기) : 3DSpace 가 여러 플랫폼을 서비스하는 환경에서 필요한 것으로 보입니다.

### 6W 태그 연동 (TagNavigatorProxy) (7)

- [옵션](/widget/tagger#옵션) : `widgetId` : 태그 창이 이 값으로 위젯이 있는 context(탭으로 보임
- [옵션](/widget/tagger#옵션) : `pod` : 사용자에게 보이는 차이는 확인하지 않았습니다
- [행 id (주제)](/widget/tagger#행-id-주제) : 사용자 태그까지 쓰려면 OOTB 형식을 따르는 것이 안전합니다
- [메서드](/widget/tagger#메서드) : `focusOnSubjects(ids, persistTags, dataSources, options)` / `unfocus()` : 사용자가 그 행에 태그를 붙일 때 쓰는 것으로 보입니다
- [동작 방식](/widget/tagger#동작-방식) : context 가 대시보드 탭이라는 것은 이 함수 이름으로 본 추정입니다
- [숨길 때, 닫을 때](/widget/tagger#숨길-때-닫을-때) : 대시보드 탭을 바꿀 때 : 태그 창이 context 마다 따로 있는 것으로 보입니다
- [2026 변경](/widget/tagger#_2026-변경) : `configurePredicates` <Badge type="warning" text="2026" /> : 처음 보일 모양을 정하는 것으로 보입니다 (`'tag'`, `'people'`, `'freeInput'` 값이 보임)

### 대시보드 상단 바 (TopBarProxy) (5)

- [메서드](/widget/top-bar#메서드) : `addOnSubMenuDisplayedEvent({ 칸 id: fn })` : 그 칸의 메뉴가 열릴 때 부를 함수로 보입니다
- [동작 방식](/widget/top-bar#동작-방식) : 지금 보이는 위젯의 채널(`widget.id`)을 활성으로 하는 것으로 보입니다
- [주의](/widget/top-bar#주의) : 상단 바에는 채널의 proxy 를 한꺼번에 지우는 `_removeProvider(채널 id)` 도 있지만 webapps 안에서 부르는 곳이 없어, 대시보드가 위젯을 지울 때 대신 정리해 주는지는 확인하지 않았습니다
- [주의](/widget/top-bar#주의) : 같은 위젯에서 proxy 를 또 만들면 상단 바 쪽 등록이 초기화되거나 항목이 겹칠 수 있습니다
- [2026 변경](/widget/top-bar#_2026-변경) : `enovTasks` 칸 <Badge type="warning" text="2026" /> : 위젯 항목을 넣으면 보이는지는 확인하지 않았습니다

### 다른 앱을 창으로 열기 (TransientWidget) (4)

- [열기](/widget/transient-widget#열기) : <code>mode</code> : <code>'panel'</code> 은 크기를 준 창, <code>'transient'</code> 는 기본 미리보기 창으로 보입니다
- [창 안에서 또 열지 않기](/widget/transient-widget#창-안에서-또-열지-않기) : 등록하지 않고 Run Your App 으로 연 위젯에서 값이 있는지는 확인하지 않았습니다
- [창으로 열린 쪽에서 값 읽기](/widget/transient-widget#창으로-열린-쪽에서-값-읽기) : 설정에 정의하지 않은 키도 `getValue` 로 읽히는지는 확인하지 않았습니다
- [Dialog 와 고르는 기준](/widget/transient-widget#dialog-와-고르는-기준) : 내 위젯을 다른 화면에서 창으로 띄우기 : TransientWidget (내 위젯이 3DDashboard 에 앱으로 등록되어 있어야 함

### widget 객체 (2)

- [이벤트](/widget/widget-api#이벤트) : `onDestroy` : 앱 코드나 대시보드가 보내는 것으로 보입니다
- [endEdit](/widget/widget-api#endedit) : 3DDashboard 의 설정 창은 대시보드가 그리는 것으로 보입니다.

## 화면 컨트롤 (26)

### 레이아웃 컨트롤 API (1)

- [Tab](/controls/api/layout#tab) : <code>responsiveFlag</code> <Badge type="warning" text="2026" /> : 폭이 모자라면 탭 글자를 숨기고 아이콘만 남기는 것으로 보입니다

### Windows API (1)

- [DockingElement.COLLAPSER_STYLE](/controls/api/windows#dockingelement-collapser-style) : <code>MOVABLE</code> : 로컬 실행에서는 영역 밖(x = -23)에 숨은 채 나타나지 않았음

### 목록, 트리, 타일 뷰 (1)

- [펼치기, 찾기](/controls/collection-views#펼치기-찾기) : `node.expandAll()` 은 이름과 달리 두 단계까지만 펼치는 것으로 보입니다

### 확장 컨트롤 (ControlsEx) (1)

- [툴바 버튼과 접기](/controls/controls-ex#툴바-버튼과-접기) : `titleIcon` 은 크기를 16px 로 덧붙여 `Dom.generateIcon` 으로 그립니다. 앱(ENOProjectMgmtCommonComponents)은 아이콘 주소 문자열을 그대로 넣지만, 소스가 이 값에 `Object.assign` 으로 크기 옵션을 덧붙이므로 `{ iconPath: '주소' }` 객체로 주는 편이 안전한 것으로 보입니다

### 다이얼로그 (Windows) (1)

- [OOTB 앱의 다이얼로그 패턴](/controls/dialog#ootb-앱의-다이얼로그-패턴) : 문제가 없는지는 확인하지 않았습니다

### 분할 레이아웃 (DockingElement) (2)

- [한쪽 영역을 탭으로](/controls/docking-element#한쪽-영역을-탭으로) : 이 방식은 샘플로 실행하지 않았습니다
- [접기 막대 모양](/controls/docking-element#접기-막대-모양) : Skeleton 의 CSS 나 다른 조건이 필요한 것으로 보입니다

### ENOVIA 공용 화면 틀 (ENOX) (6)

- [옵션](/controls/enox-components#옵션) : `pinOptions` / `pinCallback` : `DS/W3DDComponents/Views/PinView` 옵션(`widgetAppId`, `widgetData` 등)으로 대시보드에 위젯을 고정하는 것으로 보입니다
- [옵션](/controls/enox-components#옵션) : `appNamePosition` : 1 이면 줄이지 않음
- [LinkManager API](/controls/enox-components#linkmanager-api) : 3DDashboard 밖에서는 실행하지 않았습니다
- [LinkManager API](/controls/enox-components#linkmanager-api) : PAD 앱이 아니면 연결 뒤 동기화 단계가 끝나지 않는 것으로 보입니다
- [WidgetLink 직접 쓰기](/controls/enox-components#widgetlink-직접-쓰기) : 커스텀 위젯이 대시보드에서 연결 대상으로 잡히려면 연결 능력(`x3dLinkability`)이 서로 맞아야 하는데, 이 조건은 대시보드 쪽(`DS/Dashboard/WidgetLinkManager`, webapps 에 없음)에 있어 확인하지 못했습니다
- [Skeleton](/controls/enox-components#skeleton) : 서버 목록(Collection 의 `url`, `fetch`)이 있어야 화면이 채워집니다

### 화면 컨트롤 개요 (1)

- [무엇을 쓸까](/controls/#무엇을-쓸까) : UIKIT 은 UWA 컨트롤(`UWA/Controls/Abstract`) 위에 만든 예전 계열로 보입니다.

### 입력 컨트롤 (숫자, 칩, 검색, 파일) (1)

- [직접 바꾸기와 보여 주기 (customValidator, customFormatter)](/controls/input-controls#직접-바꾸기와-보여-주기-customvalidator-customformatter) : 2026 은 화면 글자를 `NumberUtils.formatNumberToString` 으로 만들어 지역 형식이 늘 적용되는 것으로 보입니다

### 메뉴 (DS/Menu) (1)

- [키보드](/controls/menu#키보드) : 2025 에서 키보드 사용을 고려하면 메뉴를 열기 전에 버튼의 포커스를 빼 둡니다 (`document.activeElement.blur()`)

### 속성 패널 (EditPropWidget) (10)

- [보이는 모양 (typeOfDisplay)](/controls/properties-panel#보이는-모양-typeofdisplay) : `ONLY_IDENTITY_CARD` : 이름과 달리 탭 내용을 숨기는 코드는 찾지 못했습니다
- [선택을 따라갈지 (selectionType)](/controls/properties-panel#선택을-따라갈지-selectiontype) : `RESTRICTED_SELECTION` : 실제로 어떤 메시지가 통과하는지는 확인하지 않았습니다
- [객체 넣기 (initDatas)](/controls/properties-panel#객체-넣기-initdatas) : 직접 만든 탭을 넣었으면 샘플처럼 `initDatas(models, EditPropConstants.FACET_PROPERTIES)` 로 처음 탭을 줍니다
- [직접 만든 탭](/controls/properties-panel#직접-만든-탭) : 이 샘플 탭은 서버에서 실행해 보지 않았습니다
- [OOTB 정보 창으로 열기](/controls/properties-panel#ootb-정보-창으로-열기) : 이 중 무엇이 꼭 있어야 하는지는 확인하지 않았습니다
- [OOTB 정보 창으로 열기](/controls/properties-panel#ootb-정보-창으로-열기) : 브라우저로 `<3DSpace>/webapps/EditPropWidget/EditPropWidget.html?selected=<physicalid>` 를 열어도 되는지는 확인하지 않았습니다
- [팝업과 도킹 창](/controls/properties-panel#팝업과-도킹-창) : 이 두 모양은 실행해 보지 않았습니다
- [고르는 기준](/controls/properties-panel#고르는-기준) : 저장 뒤 내 화면 갱신 : 다른 위젯이므로 PlatformAPI `DS/PADUtils/PADCommandProxy/refresh` 구독
- [3DDashboard 밖에서](/controls/properties-panel#_3ddashboard-밖에서) : 단독 실행에서는 Compass 콜백이 오지 않을 수 있어([Compass 서비스](/widget/compass-services)), 그러면 탭과 속성이 끝내 나오지 않을 것으로 보입니다
- [주의](/controls/properties-panel#주의) : 없는 모듈은 그 탭만 비게 됩니다

## 이벤트, 선택, 끌어 놓기 (4)

### 끌어 놓기 (DataDragAndDrop, 3DXContent) (1)

- [items[] 항목](/events/drag-and-drop#items-항목) : 3DDashboard 셸 같은 webapps 밖에서 검사하는지는 확인하지 못했지만, 실제로는 필수 키만 지키면 통하는 것으로 보입니다.

### 이벤트, 선택, 끌어 놓기 개요 (1)

- [어떤 것을 쓰나](/events/#어떤-것을-쓰나) : 대시보드의 위젯은 보통 각자 iframe 에서 돌기 때문에, 위젯 A 에서 `Events.publish` 해도 위젯 B 의 `Events.subscribe` 는 받지 못하는 것으로 보입니다.

### 선택 (CSOManager, HSOManager, PSOManager) (2)

- [선택 (CSOManager, HSOManager, PSOManager)](/events/selection) : `DS/Selection/HSOManager` : 강조(highlight) 목록으로 보입니다
- [선택 (CSOManager, HSOManager, PSOManager)](/events/selection) : `DS/Selection/PSOManager` : 미리 선택(마우스 올림) 목록으로 보입니다

## 3DSpace REST API (29)

### 타입, 속성 정보 (dictionary) (3)

- [하위 타입 (getTypeInfo)](/rest-api/dictionary#하위-타입-gettypeinfo) : 어떤 타입이 빈 객체로 오는지는 확인하지 못했습니다
- [dsfl/metaTypes](/rest-api/dictionary#dsfl-metatypes) : 보낸 타입 아래의 하위 타입 목록으로 보입니다
- [주의](/rest-api/dictionary#주의) : 서버가 두 모양을 다 받는 것으로 보입니다

### 객체 정보, 사람과 객체 찾기 (e6w) (7)

- [버전과 주소](/rest-api/e6w#버전과-주소) : v3 는 `dataelements` 로 감싸지 않은 모양으로 보입니다 (FolderUpload, RDFFoundation 이 바로 읽음)
- [여러 객체 예](/rest-api/e6w#여러-객체-예) : 미리 보기가 없는 객체는 타입 기본 그림 주소가 오는 것으로 보입니다
- [객체 찾기 (ObjectSearch)](/rest-api/e6w#객체-찾기-objectsearch) : 이름 찾기도 부분 일치 결과가 섞일 수 있는 것으로 보입니다
- [찾기 조건](/rest-api/e6w#찾기-조건) : 3DSearch 와 같은 문법이라 e6w 찾기도 인덱스를 쓰는 것으로 보입니다
- [설정 값 읽기 (ExpresssionSearch)](/rest-api/e6w#설정-값-읽기-expresssionsearch) : `CHG_CHANGE_MATURITY`, `ISU_AUTO_DATES` 는 이 설정 정의 파일들에 없어 어디서 만들어지는지는 확인하지 못했습니다
- [결과 수](/rest-api/e6w#결과-수) : 이때 서버의 기본 개수는 확인하지 못했습니다
- [주의](/rest-api/e6w#주의) : `Content-Type` 을 `application/json` 으로 보내면 `$ids` 가 읽히지 않는 것으로 보입니다

### 3DSpace REST API 공통 규칙 (2)

- [URL 구성](/rest-api/#url-구성) : `tenant=` : 온프레미스에서는 생략해도 되는 것으로 보입니다
- [쿼리 파라미터](/rest-api/#쿼리-파라미터) : `$terse` : 서버가 어떤 객체를 줄여 보내는지는 확인하지 못했습니다

### 성숙도 변경, 개정 (lifecycle) (10)

- [갈 수 있는 state](/rest-api/lifecycle#갈-수-있는-state) : state 이름과 색만 필요하면 `POST /resources/lifecycle/maturity/getStatesAttributes` 본문 `{ policies: ['<policy 이름>'] }` 를 부르고 `policies[].policyName`, `policies[].states[]` (`stateSysName`, `stateUserName`, `activeColor`, `inactiveColor`) 를 읽습니다 (IPClassifyUXUtil, ENOXVersionExplorerController, ENOChgServices). ENONextGenFilterUX 는 `{ objectids: [id] }` 로 보내고 `policies` 를 객체 하나로 읽습니다
- [dslc (CAA) 성숙도 API](/rest-api/lifecycle#dslc-caa-성숙도-api) : 서버는 현재 state 와 policy 그래프로 promote 인지 demote 인지와 `tostate`, `fromstate`, `signature` 를 정한 뒤 lifecycle 과 같은 내부 함수(`maturityChange_internal`)를 부르는 것으로 보입니다
- [dslc (CAA) 성숙도 API](/rest-api/lifecycle#dslc-caa-성숙도-api) : `nextStates` 항목이 문자열인지 객체인지는 확인하지 못했습니다
- [오류 코드](/rest-api/lifecycle#오류-코드) : 코드 앞 세 자리가 HTTP 상태와 같은 것으로 보입니다
- [소유자 넘기기](/rest-api/lifecycle#소유자-넘기기) : 샘플의 이 버튼은 서버에서 실행해 보지 않았습니다
- [끝났을 때 알림](/rest-api/lifecycle#끝났을-때-알림) : 다른 OOTB 위젯이 새로 고치게 하려면 같은 모양으로 `Lifecycle.Modification` 을 보내면 될 것으로 보입니다
- [서버에서 일어나는 일 (트리거)](/rest-api/lifecycle#서버에서-일어나는-일-트리거) : 서버 클래스(`LifecycleServices_ChangeMaturity_PLMReference_default`, `_BusinessObject`)의 문자열로 보면 한 트랜잭션 안에서 아래 검사를 하고, 하나라도 실패하면 abort 하는 것으로 보입니다
- [서버에서 일어나는 일 (트리거)](/rest-api/lifecycle#서버에서-일어나는-일-트리거) : 서버 클래스에 context 의 client task(notice)를 읽어 메시지에 붙이는 문자열(`getClientTasks`, `UXMessage`, `#FSMP#`)이 있어서 `report[].error` 에 들어오는 것으로 보입니다
- [서버에서 일어나는 일 (트리거)](/rest-api/lifecycle#서버에서-일어나는-일-트리거) : dslc `changeState` 는 같은 실패를 `errorReport` (`LCERROR500002`, `Maturity Failed :` 뒤에 원인)로 돌려주는 것으로 보입니다
- [서버에서 일어나는 일 (트리거)](/rest-api/lifecycle#서버에서-일어나는-일-트리거) : 트리거 실패 때 실제로 어떤 문자열이 오는지는 서버에서 확인하지 않았습니다

### 관계 탐색 (enorelnav) (6)

- [getEcosystem 요청](/rest-api/navigate#getecosystem-요청) : `restrictedMode` : 뜻은 확인하지 못함
- [getEcosystem 요청](/rest-api/navigate#getecosystem-요청) : `trueREST` : 뜻은 확인하지 못함
- [getEcosystem 요청](/rest-api/navigate#getecosystem-요청) : `redirectQueryToSGI` : 인덱스 쪽으로 질의를 돌리는 옵션으로 보입니다
- [관계 패턴](/rest-api/navigate#관계-패턴) : 타입에 맞는 패턴을 서버가 고르는 것으로 보입니다
- [주의](/rest-api/navigate#주의) : 같은 `widgetId` 를 다른 화면이나 다른 용도에 쓰면 맡긴 설정이 섞일 수 있습니다
- [주의](/rest-api/navigate#주의) : 쓸 수 있는 패턴은 역할, 앱 설정에 따라 다른 것으로 보입니다 (`getProfiles` 가 `roles` 를 받음)

### 보안 컨텍스트 (SecurityContext) (1)

- [헤더에 넣기](/rest-api/security-context#헤더에-넣기) : 두 방식이 모두 동작하는 것으로 보입니다.

## DataGridView (2)

### 사용자 뷰와 화면 상태 저장 (1)

- [저장 위치](/datagridview/custom-views#저장-위치) : 위젯 설정에 저장한 값은 대시보드가 위젯마다 보관하므로 다른 브라우저에서 열어도 유지되는 것으로 보입니다.

### 서버 사이드 데이터 (1)

- [정렬, 필터, 찾기는 어떻게 되나](/datagridview/server-side#정렬-필터-찾기는-어떻게-되나) : 그 밖에 그룹 행의 키(제목, 합계 등)를 어떤 모양으로 넣어야 하는지는 호출 예가 없어 확인하지 못했습니다.
