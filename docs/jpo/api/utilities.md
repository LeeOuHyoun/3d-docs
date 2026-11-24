# 유틸리티 <Badge type="info" text="원본 분석" />

JPO 안에서 쓰는 공통 기능 (util) 을 기능별로 정리한 페이지 모음의 첫 페이지입니다. ENOVIA 에는 같은 일을 하는 util 메서드가 클래스마다 따로 있는 경우가 많아서, 일마다 **쓸 메서드를 하나로 정하고** 나머지는 "같은 일을 하는 다른 메서드" 로 정리했습니다. 객체를 읽고 쓰는 메서드는 [객체 조회](/jpo/api/read), [객체 생성, 수정](/jpo/api/write), [DomainObject 메서드 사양](/jpo/api/domain-object) 에 있습니다.

::: info 근거
enovia2025 `3dspace/WEB-INF/lib` 의 xerinfra.jar (`com.matrixone.apps.domain.util`, `com.matrixone.apps.framework.ui`, `com.matrixone.apps.framework.lifecycle`), eMatrixServletRMI.jar (`matrix.db.JPO`, `matrix.util.StringList`), m1jsystem.jar (`StringUtils`) class 파일을 javap 로 읽어 확인했습니다. JSP 화면용 (화면 그리기, 화면 환경설정), 서버 관리, 라이선스, 특정 앱 전용 메서드는 뺐고, 뺀 메서드는 각 페이지 끝 "다루지 않은 메서드" 에 이름만 남겼습니다. Context 가 필요 없는 메서드 일부는 OOTB jar 로 로컬 실행해 결과를 확인했습니다. 서버에서 실행해 보지는 않았습니다.
:::

## 페이지

| 페이지 | 다루는 일 |
| --- | --- |
| [문자열, 목록](/jpo/api/util/string-list) | 빈 값 확인, 자르기 / 잇기 / 바꾸기, `StringList`, `MapList` 정렬 |
| [인자, JPO 호출, id](/jpo/api/util/args-id) | 넘어온 인자 꺼내기, 다른 JPO 부르기, 표의 행 id 나누기, physicalid |
| [스키마 이름, 타입·정책·속성 정보](/jpo/api/util/schema) | 심볼릭 이름 ↔ 실제 이름, 상태 이름, 관리 객체 property, 타입 / 정책 / 속성 정보 |
| [설정값, 다국어 문구](/jpo/api/util/resource) | 시스템 설정 읽기, 다국어 문구, 타입 / 상태 / 범위 값의 화면 이름, 언어 |
| [MQL 실행, 환경 변수](/jpo/api/util/mql) | `MqlUtil`, 환경 변수 (RPE) |
| [트랜잭션, 권한 전환](/jpo/api/util/transaction) | 트랜잭션으로 묶기, 시스템 권한으로 바꾸기 |
| [사용자, 조직, 보안 컨텍스트](/jpo/api/util/person) | 로그인 사용자, Person 객체, 회사 / 조직 / 협업공간, 역할 |
| [소유권 공유, 접근 권한](/jpo/api/util/access) | 다른 조직 / 협업공간 / 사람과 객체 함께 쓰기, 권한 확인 |
| [날짜](/jpo/api/util/date) | DB 날짜 값 ↔ `Date`, 화면 날짜, 입력 날짜 |
| [HTML, URL, JSON](/jpo/api/util/html-json) | HTML / URL / 스크립트에 넣을 글자, JSON 만들기 |
| [메일, 알림](/jpo/api/util/mail) | 메일과 알림 보내기 |
| [history](/jpo/api/util/history) | history 읽기, 최근 기록, 트리거 안의 트랜잭션 기록, 기록 추가 |
| [이름 채번, 캐시, 로그, 서버 환경](/jpo/api/util/etc) | 채번기 이름, 서버 전체 캐시, 로그, 설치된 앱 확인 |

## 쓸 메서드를 고른 기준

1. 폐기 (`Deprecated`) 가 아닌 것
2. 바이트코드로 본 동작이 바르고 함정이 적은 것
3. 다른 후보들이 안에서 부르는 쪽 (원래 구현)
4. OOTB JPO 에서 많이 쓰는 것

## 하고 싶은 일로 찾기

### 문자열, 목록

| 하고 싶은 일 | 쓸 메서드 | 절 |
| --- | --- | --- |
| 값이 비었는지 확인 (`null`, 공백, `"null"`) | `UIUtil.isNullOrEmpty(s)` | [빈 값 확인](/jpo/api/util/string-list#빈-값-확인) |
| 문자열을 구분자로 자르기 | `FrameworkUtil.split(s, ",")` | [문자열 자르기](/jpo/api/util/string-list#문자열-자르기) |
| 목록을 구분자로 이어 문자열 하나로 | `FrameworkUtil.join(list, ", ")` | [문자열 잇기](/jpo/api/util/string-list#문자열-잇기) |
| 찾은 글자를 모두 바꾸기 | `FrameworkUtil.findAndReplace(s, find, rep)` | [글자 바꾸기](/jpo/api/util/string-list#글자-바꾸기) |
| 긴 글을 n 글자로 줄이고 `...` 붙이기 | `FrameworkUtil.stripString(s, n, type)` | [글자 수로 줄이기](/jpo/api/util/string-list#글자-수로-줄이기) |
| UTF-8 바이트 수에 맞게 자르기 | `FrameworkUtil.getUtf8TruncatedString(s, maxBytes)` | [UTF-8 바이트 수로 자르기](/jpo/api/util/string-list#utf-8-바이트-수로-자르기) |
| select 목록처럼 값 여러 개로 목록 만들기 | `StringList.create(a, b, ...)` | [목록 만들기](/jpo/api/util/string-list#목록-만들기) |
| 목록을 `String[]` 로 바꾸기 | `list.toStringArray()` | [목록을 배열로 바꾸기](/jpo/api/util/string-list#목록을-배열로-바꾸기) |
| 목록에서 `null`, `""` 빼기 | `list.filterEmpty()` | [빈 값 빼기](/jpo/api/util/string-list#빈-값-빼기) |
| 순서를 지키며 목록의 중복 빼기 | `unique.addAllExclusive(list)` | [중복 빼기](/jpo/api/util/string-list#중복-빼기) |
| 목록 정렬하기 | `list.sort()` | [정렬하기](/jpo/api/util/string-list#정렬하기) |
| 조회 결과 (MapList) 를 한 키로 정렬 | `mapList.sort(key, dir, type)` | [한 키로 정렬](/jpo/api/util/string-list#한-키로-정렬) |
| 조회 결과를 여러 키로 정렬 | `mapList.addSortKey(key, dir, type)` + `mapList.sort()` | [여러 키로 정렬](/jpo/api/util/string-list#여러-키로-정렬) |
| 여러 단계로 펼친 결과를 부모, 자식 순서를 지키며 정렬 | `mapList.sortStructure(key, dir, type)` | [구조 목록 정렬](/jpo/api/util/string-list#구조-목록-정렬) |

### 인자, JPO 호출, id

| 하고 싶은 일 | 쓸 메서드 | 절 |
| --- | --- | --- |
| 받은 `args` 를 Map 으로 되돌리기 | `JPO.unpackArgs(args)` | [넘어온 인자 꺼내기](/jpo/api/util/args-id#넘어온-인자-꺼내기) |
| 다른 JPO 의 메서드를 이름으로 부르기 | `JPO.invoke(context, "이름", null, "메서드", JPO.packArgs(map), 반환형.class)` | [다른 JPO 부르기](/jpo/api/util/args-id#다른-jpo-부르기) |
| 표, 구조 화면에서 고른 행의 objectId 꺼내기 | `FrameworkUtil.getSplitTableRowIds(rowIds)` | [objectId 꺼내기](/jpo/api/util/args-id#objectid-꺼내기) |
| 고른 행의 관계 id, 부모 id, 행 위치 꺼내기 | `FrameworkUtil.split(rowId, "\|")` | [관계 id, 부모 id 까지 꺼내기](/jpo/api/util/args-id#관계-id-부모-id-까지-꺼내기) |
| 문자열이 객체 id 모양인지 확인 | `FrameworkUtil.isObjectId(context, s)` | [id 모양 확인](/jpo/api/util/args-id#id-모양-확인) |
| 객체 id 를 physicalid 로 | `FrameworkUtil.getPIDfromOID(context, id)` | [physicalid 와 id 바꾸기](/jpo/api/util/args-id#physicalid-와-id-바꾸기) |
| physicalid 를 객체 id 로 | `FrameworkUtil.getOIDfromPID(context, pid)` | [physicalid 와 id 바꾸기](/jpo/api/util/args-id#physicalid-와-id-바꾸기) |
| 관계 physicalid 를 관계 id 로 | `FrameworkUtil.getRelIDfromPID(context, pid)` | [physicalid 와 id 바꾸기](/jpo/api/util/args-id#physicalid-와-id-바꾸기) |

### 스키마 이름, 타입·정책·속성 정보

| 하고 싶은 일 | 쓸 메서드 | 절 |
| --- | --- | --- |
| 심볼릭 이름을 실제 이름으로 | `PropertyUtil.getSchemaProperty(context, "type_MyPart")` | [실제 이름으로 바꾸기](/jpo/api/util/schema#실제-이름으로-바꾸기) |
| 실제 이름을 심볼릭 이름으로 | `FrameworkUtil.getAliasForAdmin(context, "type", type, true)` | [심볼릭 이름으로 바꾸기](/jpo/api/util/schema#심볼릭-이름으로-바꾸기) |
| 심볼릭 상태 이름을 실제 상태 이름으로 | `PropertyUtil.getSchemaProperty(context, "policy", policy, "state_Release")` | [실제 상태 이름으로 바꾸기](/jpo/api/util/schema#실제-상태-이름으로-바꾸기) |
| 실제 상태 이름을 심볼릭 상태 이름으로 | `FrameworkUtil.reverseLookupStateName(context, policy, current)` | [심볼릭 상태 이름으로 바꾸기](/jpo/api/util/schema#심볼릭-상태-이름으로-바꾸기) |
| 타입, 정책 같은 관리 객체의 property 읽기 | `PropertyUtil.getAdminProperty(context, "type", typePart, "MyVersioning")` | [관리 객체의 property](/jpo/api/util/schema#관리-객체의-property) |
| 관리 객체의 property 쓰기, 지우기 | `PropertyUtil.setAdminProperty`, `PropertyUtil.removeAdminProperty` | [관리 객체의 property](/jpo/api/util/schema#관리-객체의-property) |
| 타입 이름만으로 상속 관계 확인 | `mxType.isOfParentType(context, type, typePart)` | [상속 관계 확인](/jpo/api/util/schema#상속-관계-확인) |
| 타입의 맨 위 타입 | `FrameworkUtil.getBaseType(context, type, null)` | [맨 위 타입](/jpo/api/util/schema#맨-위-타입) |
| 타입에 쓸 수 있는 정책 목록 | `mxType.getPolicies(context, typePart, false)` | [정책 목록](/jpo/api/util/schema#정책-목록) |
| 타입의 기본 정책과 첫 리비전 | `mxType.getDefaultPolicy(context, typeDocument, false)` | [기본 정책](/jpo/api/util/schema#기본-정책) |
| 타입에 정의된 속성 목록 | `mxType.getAttributes(context, typePart)` | [속성 목록](/jpo/api/util/schema#속성-목록) |
| 타입에 그 속성이 있는지 | `FrameworkUtil.isAttributeOnType(context, typePart, attrStatus)` | [속성이 있는지](/jpo/api/util/schema#속성이-있는지) |
| 속성의 범위 값 | `FrameworkUtil.getRanges(context, attrStatus)` | [범위 값](/jpo/api/util/schema#범위-값) |
| 속성 값을 읽는 select 만들기 | `DomainObject.getAttributeSelect(attrStatus)` | [속성 select 만들기](/jpo/api/util/schema#속성-select-만들기) |
| 속성에 단위가 있는지, 저장 단위 | `UOMUtil.isAssociatedWithDimension`, `UOMUtil.getDBunit` | [단위가 있는 속성](/jpo/api/util/schema#단위가-있는-속성) |
| 값을 저장 단위로 바꾸기 | `UOMUtil.convertToDefaultUnit(context, attr, value, unit)` | [단위가 있는 속성](/jpo/api/util/schema#단위가-있는-속성) |
| 객체의 타입, 상태, 리비전, 속성 값 읽기 | `obj.getInfo(context, selects)` | [객체의 값 읽고 바꾸기](/jpo/api/util/schema#객체의-값-읽고-바꾸기) |

### 설정값, 다국어 문구

| 하고 싶은 일 | 쓸 메서드 | 절 |
| --- | --- | --- |
| 시스템 설정 값 읽기 (`emxSystem.properties` 등) | `EnoviaResourceBundle.getProperty(context, key)` | [설정 값 읽기](/jpo/api/util/resource#설정-값-읽기) |
| 쉼표로 이어진 설정 값을 목록으로 읽기 | `FrameworkProperties.getTokenizedProperty(context, key, ",")` | [목록으로 읽기](/jpo/api/util/resource#목록으로-읽기) |
| 사용자 언어로 문구 읽기 | `EnoviaResourceBundle.getProperty(context, resourceFile, locale, key)` | [다국어 문구 읽기](/jpo/api/util/resource#다국어-문구-읽기) |
| 문구의 `{0}`, `{1}` 자리에 값 넣기 | `MessageUtil.substituteMessageArgs(context, text, args, locale)` | [문구에 값 넣기](/jpo/api/util/resource#문구에-값-넣기) |
| 타입, 속성, 정책 이름을 화면 이름으로 | `EnoviaResourceBundle.getAdminI18NString(context, kind, name, language)` | [타입, 속성, 정책 이름](/jpo/api/util/resource#타입-속성-정책-이름) |
| 상태 이름을 화면 이름으로 | `EnoviaResourceBundle.getStateI18NString(context, policy, state, language)` | [상태 이름](/jpo/api/util/resource#상태-이름) |
| 속성 범위 값을 화면 이름으로 | `EnoviaResourceBundle.getRangeI18NString(context, attribute, value, language)` | [범위 값](/jpo/api/util/resource#범위-값) |
| 로그인 사용자의 언어 얻기 | `context.getLocale()` | [언어 얻기](/jpo/api/util/resource#언어-얻기) |
| `"ko"` 같은 언어 문자열을 Locale 로 | `MessageUtil.getLocale(language)` | [언어 문자열을 Locale 로](/jpo/api/util/resource#언어-문자열을-locale-로) |

### MQL 실행, 환경 변수

| 하고 싶은 일 | 쓸 메서드 | 절 |
| --- | --- | --- |
| MQL 명령 실행하기 (값은 `$1` 로) | `MqlUtil.mqlCommand(context, cmd, values...)` | [MQL 명령 실행하기](/jpo/api/util/mql#mql-명령-실행하기) |
| 값 개수가 실행 때 정해지는 MQL 명령 실행하기 | `MqlUtil.mqlCommand(context, cmd, list)` | [MQL 명령 실행하기](/jpo/api/util/mql#mql-명령-실행하기) |
| 값이 없는 MQL 명령 실행하기 | `MqlUtil.mqlCommand(context, cmd, false, false)` | [값이 없는 명령 실행하기](/jpo/api/util/mql#값이-없는-명령-실행하기) |
| MQL 명령 하나만 슈퍼유저로 실행하기 | `MqlUtil.mqlCommand(context, cmd, true, values...)` | [명령 하나만 슈퍼유저로 실행하기](/jpo/api/util/mql#명령-하나만-슈퍼유저로-실행하기) |
| 트리거, 히스토리 없이 MQL 명령 하나 실행하기 | `MqlUtil.mqlCommand(context, historyOff, triggerOff, cmd, push, values...)` | [트리거, 히스토리 없이 실행하기](/jpo/api/util/mql#트리거-히스토리-없이-실행하기) |
| `mqlCommand` 의 boolean 인자 읽기 | `mqlCommand` 13 가지 형태 | [mqlCommand 13 가지 형태](/jpo/api/util/mql#mqlcommand-13-가지-형태) |
| 사용자에게 알림, 경고, 오류 문구 띄우기 | `MqlUtil.mqlCommand(context, "notice $1", msg)` | [사용자에게 메시지 띄우기](/jpo/api/util/mql#사용자에게-메시지-띄우기) |
| MQL 환경 변수 (RPE) 읽기 | `PropertyUtil.getRPEValue(context, name, global)` | [환경 변수 읽고 쓰기](/jpo/api/util/mql#환경-변수-읽고-쓰기) |
| MQL 환경 변수 쓰기 | `PropertyUtil.setRPEValue(context, name, value, global)` | [환경 변수 읽고 쓰기](/jpo/api/util/mql#환경-변수-읽고-쓰기) |
| MQL 환경 변수 지우기 | `PropertyUtil.unsetRPEValue(context, name, global)` | [환경 변수 읽고 쓰기](/jpo/api/util/mql#환경-변수-읽고-쓰기) |

### 트랜잭션, 권한 전환

| 하고 싶은 일 | 쓸 메서드 | 절 |
| --- | --- | --- |
| 여러 변경을 한 트랜잭션으로 묶기 | `ContextUtil.startTransaction(context, true)`, `commitTransaction`, `abortTransaction` | [여러 변경을 한 트랜잭션으로 묶기](/jpo/api/util/transaction#여러-변경을-한-트랜잭션으로-묶기) |
| 트랜잭션 중인지 확인하기 | `ContextUtil.isTransactionActive(context)` | [트랜잭션 중인지 확인하기](/jpo/api/util/transaction#트랜잭션-중인지-확인하기) |
| 트랜잭션 안에서 일부만 되돌리기 | `ContextUtil.setSavePoint(context, name)`, `abortSavePoint` | [트랜잭션 안에서 일부만 되돌리기](/jpo/api/util/transaction#트랜잭션-안에서-일부만-되돌리기) |
| 권한 없는 작업을 시스템 사용자 (User Agent) 로 하기 | `ContextUtil.pushContext(context)`, `popContext` | [권한 없이 시스템으로 바꾸기](/jpo/api/util/transaction#권한-없이-시스템으로-바꾸기) |
| 정해 둔 다른 사용자로 바꿔 실행하기 | `ContextUtil.pushContext(context, user, null, null)` | [다른 사용자로 바꾸기](/jpo/api/util/transaction#다른-사용자로-바꾸기) |
| 시스템으로 바꾼 뒤 실제 로그인 사용자 알아내기 | `PropertyUtil.getRPEValue(context, ContextUtil.MX_LOGGED_IN_USER_NAME, true)` | [바꾸기 전 사용자 알아내기](/jpo/api/util/transaction#바꾸기-전-사용자-알아내기) |
| context 를 받지 못한 코드에서 실행하기 | `ContextUtil.runInAnonymousContext(callable)` | [context 없이 실행하기](/jpo/api/util/transaction#context-없이-실행하기) |

### 사용자, 조직, 보안 컨텍스트

| 하고 싶은 일 | 쓸 메서드 | 절 |
| --- | --- | --- |
| 로그인 사용자 이름 알아내기 | `context.getUser()` | [로그인 사용자 이름](/jpo/api/util/person#로그인-사용자-이름) |
| 사용자의 Person 객체 id 얻기 | `PersonUtil.getPersonObjectID(context, user)` | [Person 객체 id](/jpo/api/util/person#person-객체-id) |
| Person 객체의 속성, 연결 읽기 | `Person.getPerson(context, user)` | [Person 객체의 값 읽기](/jpo/api/util/person#person-객체의-값-읽기) |
| 화면에 보이는 사람 이름 만들기 | `PersonUtil.getFullName(context, user)` | [화면에 보이는 이름](/jpo/api/util/person#화면에-보이는-이름) |
| 사람의 메일 주소 읽기 | `PersonUtil.getEmail(context, user)` | [메일 주소](/jpo/api/util/person#메일-주소) |
| 로그인 사용자의 회사 id | `PersonUtil.getUserCompanyId(context)` | [회사](/jpo/api/util/person#회사) |
| 사람이 Member 로 속한 조직 | `PersonUtil.getMemberOrganizations(context, user)` | [소속 조직](/jpo/api/util/person#소속-조직) |
| 지금 보안 컨텍스트와 그 조직, 협업공간 | `PersonUtil.getActiveSecurityContext(context)`, `getActiveOrganization`, `getActiveProject` | [지금 보안 컨텍스트](/jpo/api/util/person#지금-보안-컨텍스트) |
| 기본 보안 컨텍스트와 그 조직, 협업공간 | `PersonUtil.getDefaultSecurityContext(context, user)`, `getDefaultOrganization`, `getDefaultProject` | [기본 보안 컨텍스트](/jpo/api/util/person#기본-보안-컨텍스트) |
| 가진 보안 컨텍스트, 조직의 협업공간 목록 | `PersonUtil.getSecurityContextsNames(context, user)`, `getProjects(context, user, org)` | [가진 보안 컨텍스트 목록](/jpo/api/util/person#가진-보안-컨텍스트-목록) |
| 로그인 사용자가 그 역할을 가졌는지 | `PersonUtil.hasAssignment(context, role)` | [역할을 가졌는지 확인](/jpo/api/util/person#역할을-가졌는지-확인) |
| 사용자가 가진 역할, 그룹 목록 | `PersonUtil.getAssignments(context, user)` | [가진 역할 목록](/jpo/api/util/person#가진-역할-목록) |
| 보안 컨텍스트가 관리자, 읽기 역할인지 | `PersonUtil.isVPLMAdmin(context, sc)`, `isVPLMReader` ... | [보안 컨텍스트의 역할 확인](/jpo/api/util/person#보안-컨텍스트의-역할-확인) |
| 그 역할을 가진 사람 목록 | `PersonUtil.getPersonFromRole(context, role)` | [역할을 가진 사람 목록](/jpo/api/util/person#역할을-가진-사람-목록) |
| 사용자 그룹에 든 사람 목록 | `FrameworkUtil.getProjectGroupAssignees(context, group)` | [사용자 그룹의 사람 목록](/jpo/api/util/person#사용자-그룹의-사람-목록) |
| 로그인 vault, 검색할 vault | `context.getVault().getName()`, `VaultUtil.getSearchVaultPattern(context)` | [vault](/jpo/api/util/person#vault) |

### 소유권 공유, 접근 권한

| 하고 싶은 일 | 쓸 메서드 | 절 |
| --- | --- | --- |
| 객체를 다른 조직, 협업공간과 공유 | `DomainAccess.createObjectOwnership(context, id, org, project, access, comment)` | [조직, 협업공간과 공유](/jpo/api/util/access#조직-협업공간과-공유) |
| 객체를 사람 한 명과 공유 | `DomainAccess.createObjectOwnership(context, id, personId, access, comment)` | [사람과 공유](/jpo/api/util/access#사람과-공유) |
| 다른 객체의 권한을 물려받게 하기 | `DomainAccess.createObjectOwnership(context, id, fromId, comment)` | [다른 객체의 권한 물려받기](/jpo/api/util/access#다른-객체의-권한-물려받기) |
| 공유 하나 거두기 | `DomainAccess.deleteObjectOwnership(context, id, org, project, comment)` | [공유 거두기](/jpo/api/util/access#공유-거두기) |
| 공유했는지 확인 | `DomainAccess.hasObjectOwnership(context, id, org, project, comment)` | [공유했는지 확인](/jpo/api/util/access#공유했는지-확인) |
| 객체의 공유 목록 읽기 | `DomainAccess.getAccessSummaryList(context, id)` | [공유 목록 읽기](/jpo/api/util/access#공유-목록-읽기) |
| 객체의 공유를 모두 지우기 | `DomainAccess.clearMultipleOwnership(context, id)` | [공유를 모두 지우기](/jpo/api/util/access#공유를-모두-지우기) |
| 줄 수 있는 권한 이름, 그 실제 권한 목록 | `DomainAccess.getLogicalNames(context, id)`, `getPhysicalAccessMasks(context, id, access)` | [쓸 수 있는 권한 이름](/jpo/api/util/access#쓸-수-있는-권한-이름) |
| 로그인 사용자가 객체에 그 권한을 가졌는지 | `FrameworkUtil.hasAccess(context, obj, "modify")` | [로그인 사용자의 권한 확인](/jpo/api/util/access#로그인-사용자의-권한-확인) |
| 다른 사람들이 객체에 그 권한을 가졌는지 | `AccessUtil.hasAccess(context, id, persons, accesses)` | [다른 사용자의 권한 확인](/jpo/api/util/access#다른-사용자의-권한-확인) |
| 객체 자체의 조직, 협업공간, 소유자 바꾸기 | `obj.setPrimaryOwnership(context, project, org)`, `obj.TransferOwnership` | [객체의 조직, 협업공간, 소유자 바꾸기](/jpo/api/util/access#객체의-조직-협업공간-소유자-바꾸기) |
| grant 로 준 권한 거두기 | `obj.revokeAccess(context, grantor, grantee)` | [위임한 권한 거두기](/jpo/api/util/access#위임한-권한-거두기) |

### 날짜

| 하고 싶은 일 | 쓸 메서드 | 절 |
| --- | --- | --- |
| DB 날짜 형식 알아내기 | `eMatrixDateFormat.getEMatrixDateFormat()` | [DB 날짜 형식](/jpo/api/util/date#db-날짜-형식) |
| DB 날짜 값을 `Date` 로 | `eMatrixDateFormat.getJavaDate(value)` | [DB 날짜 값을 Date 로](/jpo/api/util/date#db-날짜-값을-date-로) |
| `Date` 를 DB 날짜 값으로 (날짜 속성에 넣을 값) | `new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US).format(date)` | [Date 를 DB 날짜 값으로](/jpo/api/util/date#date-를-db-날짜-값으로) |
| 사용자 시간대, Locale 얻기 | `requestMap.get("timeZone")`, `requestMap.get("localeObj")` | [시간대와 Locale 얻기](/jpo/api/util/date#시간대와-locale-얻기) |
| DB 날짜 값을 사용자 화면 날짜 (시간) 로 | `eMatrixDateFormat.getFormattedDisplayDateTime(context, value, showTime, format, tz, locale)` | [DB 날짜 값을 화면 날짜로](/jpo/api/util/date#db-날짜-값을-화면-날짜로) |
| 화면에서 입력한 날짜를 DB 값으로 | `eMatrixDateFormat.getFormattedInputDate(context, value, tz, locale)` | [화면 입력을 DB 날짜 값으로](/jpo/api/util/date#화면-입력을-db-날짜-값으로) |
| 화면에서 입력한 날짜와 시간을 DB 값으로 | `eMatrixDateFormat.getFormattedInputDateTime(context, date, time, tz, locale)` | [화면 입력을 DB 날짜 값으로](/jpo/api/util/date#화면-입력을-db-날짜-값으로) |
| 토, 일을 뺀 일수 세기 | `DateUtil.computeDuration(start, finish)` | [토, 일을 뺀 일수](/jpo/api/util/date#토-일을-뺀-일수) |
| 토, 일을 건너뛰어 끝나는 날 구하기 | `DateUtil.computeFinishDate(start, days * 86400000L)` | [토, 일을 뺀 일수](/jpo/api/util/date#토-일을-뺀-일수) |

### HTML, URL, JSON

| 하고 싶은 일 | 쓸 메서드 | 절 |
| --- | --- | --- |
| HTML 태그 사이에 값 넣기 | `XSSUtil.encodeForHTML(context, s)` | [HTML 에 값 넣기](/jpo/api/util/html-json#html-에-값-넣기) |
| HTML 속성 값 (`title="..."`) 자리에 값 넣기 | `XSSUtil.encodeForHTMLAttribute(context, s)` | [HTML 에 값 넣기](/jpo/api/util/html-json#html-에-값-넣기) |
| 스크립트 문자열 (`onclick`, `<script>`) 안에 값 넣기 | `XSSUtil.encodeForJavaScript(context, s)` | [HTML 에 값 넣기](/jpo/api/util/html-json#html-에-값-넣기) |
| URL 인자 값 넣기 | `XSSUtil.encodeForURL(context, s)` | [URL 인자 값 넣기](/jpo/api/util/html-json#url-인자-값-넣기) |
| URL 인자 값 되돌리기 | `XSSUtil.decodeFromURL(s)` | [URL 인자 값 되돌리기](/jpo/api/util/html-json#url-인자-값-되돌리기) |
| XML 에 값 넣기 | `XSSUtil.encodeForXML(context, s)` | [XML 에 값 넣기](/jpo/api/util/html-json#xml-에-값-넣기) |
| JPO 가 돌려줄 JSON 문자열 만들기 | `BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder())` → `build().toString()` | [JSON 문자열 만들기](/jpo/api/util/html-json#json-문자열-만들기) |
| JSON 문자열 읽기 | `FrameworkUtil.toJsonObject(s)` | [JSON 문자열 읽기](/jpo/api/util/html-json#json-문자열-읽기) |

### 메일, 알림

| 하고 싶은 일 | 쓸 메서드 | 절 |
| --- | --- | --- |
| 알림 (IconMail) 과 메일 보내기 (제목, 본문을 그대로) | `MailUtil.sendMessage(context, toList, ccList, bccList, subject, message, objectIdList)` | [알림 보내기](/jpo/api/util/mail#알림-보내기) |
| 받는 사람 언어의 문구로 알림 보내기 | `MailUtil.sendNotification(context, toList, ..., objectIdList, companyName, bundle)` | [받는 사람 언어로 알림 보내기](/jpo/api/util/mail#받는-사람-언어로-알림-보내기) |
| 알림의 보내는 사람 정하기 | `MailUtil.setAgentName(context, name)` | [보내는 사람 정하기](/jpo/api/util/mail#보내는-사람-정하기) |
| 메일 본문에 넣을 객체 링크 만들기 | `MailUtil.getObjectLinkURI(context, MailUtil.getBaseURL(context), objectId)` | [객체 링크 주소 만들기](/jpo/api/util/mail#객체-링크-주소-만들기) |

### history

| 하고 싶은 일 | 쓸 메서드 | 절 |
| --- | --- | --- |
| 객체의 기록 (history) 을 모두 읽기 | `UINavigatorUtil.getHistoryData(context, objectId)` | [객체 기록 모두 읽기](/jpo/api/util/history#객체-기록-모두-읽기) |
| 가장 최근 기록 하나 읽기 | `getHistoryData` 결과 목록의 마지막 값 | [가장 최근 기록 읽기](/jpo/api/util/history#가장-최근-기록-읽기) |
| 한 종류 이벤트 (소유자 변경 ...) 기록만 읽기 | `obj.getInfoList(context, "history.changeowner")` | [한 종류 이벤트 기록만 읽기](/jpo/api/util/history#한-종류-이벤트-기록만-읽기) |
| 관계의 기록 읽기 | `new Relationship(relId).getHistory(context)` | [관계 기록 읽기](/jpo/api/util/history#관계-기록-읽기) |
| 트리거에서 이번 트랜잭션 기록 (`${TRANSHISTORY}`) 풀기 | `TransactionTriggerUtil.parseHistory(context, args[0])` | [트랜잭션 기록 풀기](/jpo/api/util/history#트랜잭션-기록-풀기) |
| 객체에 사용자 기록 남기기 | `MqlUtil.mqlCommand(context, "modify bus $1 add history $2 comment $3", ...)` | [기록 남기기](/jpo/api/util/history#기록-남기기) |
| 기록을 남기지 않고 고치기 | `history off` … `finally` 에서 `history on` | [기록 잠시 끄기](/jpo/api/util/history#기록-잠시-끄기) |
| 기록 이벤트 이름을 사용자 언어로 | `EnoviaResourceBundle.getHistoryI18NString(context, event, lang)` | [이벤트 이름 번역](/jpo/api/util/history#이벤트-이름-번역) |

### 이름 채번, 캐시, 로그, 서버 환경

| 하고 싶은 일 | 쓸 메서드 | 절 |
| --- | --- | --- |
| 채번기로 이름을 받아 객체 만들기 | `FrameworkUtil.autoName(context, "type_MyPart", "policy_MyPartPolicy")` | [채번기로 객체 만들기](/jpo/api/util/etc#채번기로-객체-만들기) |
| 채번기로 이름만 받기 | `DomainObject.getAutoGeneratedName(context, "type_MyPart", "")` | [채번기로 이름만 받기](/jpo/api/util/etc#채번기로-이름만-받기) |
| 서버 전체가 같이 쓰는 캐시에 값 넣기, 읽기, 지우기 | `CacheUtil.setCacheObject`, `getCacheObject`, `removeCacheObject` | [서버 전체 캐시](/jpo/api/util/etc#서버-전체-캐시) |
| 로그 남기기 | slf4j `LoggerFactory.getLogger(...)` | [로그 남기기](/jpo/api/util/etc#로그-남기기) |
| 서버에 앱이 설치되어 있는지 알아보기 | `FrameworkUtil.isSuiteRegistered(context, "appVersionProgramCentral", false, null, null)` | [앱이 설치되어 있는지](/jpo/api/util/etc#앱이-설치되어-있는지) |
| 클라우드 서버인지 알아보기 | `UINavigatorUtil.isCloud(context)` | [클라우드 서버인지](/jpo/api/util/etc#클라우드-서버인지) |
| JPO 메서드를 백그라운드로 돌리기 | `new BackgroundProcess().submitJob(context, program, method, args, (String) null)` | [백그라운드로 실행하기](/jpo/api/util/etc#백그라운드로-실행하기) |

## 같은 일을 하는 메서드 정리

일마다 쓸 메서드와, 같은 일을 하지만 쓰지 않는 메서드입니다. 쓰지 않는 이유는 각 페이지의 "같은 일을 하는 다른 메서드" 표에 있습니다.

### 문자열, 목록 쪽

[문자열, 목록](/jpo/api/util/string-list)

| 같은 일 | 쓸 것 | 쓰지 않는 것 |
| --- | --- | --- |
| 빈 값 확인 | `UIUtil.isNullOrEmpty` | `ChangeUXUtil.isNullOrEmpty`, `ChangeUtil.isNullOrEmpty`, 앱 클래스 (`BOMMgtUtil`, `PartMgtUtil` ...) 의 `isNullOrEmpty` |
| 문자열 자르기 | `FrameworkUtil.split` | `StringUtil.split`, `FrameworkUtil.splitString`, `StringUtils.split` (폐기) |
| 문자열 잇기 | `FrameworkUtil.join` | `StringUtil.join`, `list.join` |
| 글자 바꾸기 | `FrameworkUtil.findAndReplace` | `StringUtil.Replace`, `FrameworkUtil.Replace`, `StringUtils.replace` (폐기), `StringUtils.replaceAll` (폐기), `StringUtils.replaceFirst` (폐기) |
| 목록 만들기 | `StringList.create` | `new StringList(s)`, `new StringList(String[])`, `StringList.asList`, `addElement` (폐기) |
| 목록에서 빈 값 빼기 | `list.filterEmpty` | `StringList.createNotNull` |
| 목록에서 중복 빼기 | `addAllExclusive` | `uniq` |
| 구조 목록 정렬 | `mapList.sortStructure(key, dir, type)` | `mapList.sortStructure(context, key, dir, comparatorName)` |
| 여러 객체 값을 MapList 로 받기 | `DomainObject.getInfo(context, ids, selects)` | `FrameworkUtil.toMapList`, `FrameworkUtil.toMap` |

### 인자, JPO 호출, id 쪽

[인자, JPO 호출, id](/jpo/api/util/args-id)

| 같은 일 | 쓸 것 | 쓰지 않는 것 |
| --- | --- | --- |
| 넘어온 인자 꺼내기 | `JPO.unpackArgs(args)` | `JPO.unpackArgs(args, retain)`, `UIUtil.getValue` |
| 다른 JPO 부르기 | `JPO.invoke(context, "이름", null, "메서드", args, 반환형.class)` | `JPO.invoke` (반환형 없는 형태, `클래스.class` 형태, `JPOInvocationParams` 형태), `JPO.invokeLocal`, `JPO.packArgsRemote` |
| 고른 행 값에서 관계 id 꺼내기 | `FrameworkUtil.split(rowId, "\|")` | `FrameworkUtil.getTableRowRelId`, `UIUtil.parseRelAndObjectIds`, 앱의 `TableRowId`, `EmxTableRowId` |
| 객체 id 모양 확인 | `FrameworkUtil.isObjectId` | `FrameworkUtil.isObjectIdOnly`, `FrameworkUtil.isRelId` |
| physicalid 를 객체 id 로 | `FrameworkUtil.getOIDfromPID` | `FrameworkUtil.getOIDfromPIDifExist` |
| 객체 id 를 physicalid 로 | `FrameworkUtil.getPIDfromOID` | `DomainObject.newInstance(context, id).getInfo(context, "physicalid")`, `FrameworkUtil.getPhysicalIdfromUUId` |

### 스키마 이름, 타입·정책·속성 정보 쪽

[스키마 이름, 타입·정책·속성 정보](/jpo/api/util/schema)

| 같은 일 | 쓸 것 | 쓰지 않는 것 |
| --- | --- | --- |
| 심볼릭 이름을 실제 이름으로 | `PropertyUtil.getSchemaProperty(context, symbolicName)` | `PropertyUtil.getSchemaProperty(symbolicName)` (폐기) |
| 실제 이름을 심볼릭 이름으로 | `FrameworkUtil.getAliasForAdmin(context, adminType, name, true)` | `PropertyUtil.getAliasForAdmin` (폐기), `DomainObject.getAliasForName` (폐기), `FrameworkUtil.getTypeRegistrationName` (폐기) |
| 심볼릭 상태 이름을 실제 상태 이름으로 | `PropertyUtil.getSchemaProperty(context, "policy", policy, symbolicState)` | `FrameworkUtil.lookupStateName`, `PropertyUtil.getSchemaProperty("policy", policy, symbolicState)` (폐기) |
| 관리 객체의 property 읽기 | `PropertyUtil.getAdminProperty(context, adminType, name, property)` | `PropertyUtil.getSchemaProperty(context, adminType, name, property)` (상태 이름 말고), `FrameworkUtil.getPolicyClassification` |
| 타입 이름만으로 상속 관계 확인 | `mxType.isOfParentType(context, type, parentType)` | `obj.isKindOf(context, type)` (객체가 있을 때) |
| 타입의 정책 목록 | `mxType.getPolicies(context, type, false)` | `obj.getPolicies(context)` (폐기), `FrameworkUtil.getPolicyNames` |
| 타입의 기본 정책 | `mxType.getDefaultPolicy(context, type, false)` | `obj.getDefaultPolicy(context, type)` |
| 타입의 속성 목록 | `mxType.getAttributes(context, type)` | `DomainObject.getTypeAttributes` (protected), `DomainObject.getTypeAttributeNames` (protected) |
| 속성의 범위 값 | `FrameworkUtil.getRanges(context, attribute)` | `mxAttr.getChoices` |
| 속성 값 select 만들기 | `DomainObject.getAttributeSelect(attribute)` | `DomainRelationship.getAttributeSelect` (`attribute[이름].value` 를 만듦) |
| 속성의 저장 단위 | `UOMUtil.getDBunit(context, attribute)` | `UOMUtil.getSystemunit(context, null, attribute, null)` |
| 객체의 타입, 상태 읽기 | `obj.getInfo(context, selects)` | `FrameworkUtil.getType`, `getCurrentState`, `getCurrentStateName`, `getCurrentStateOfBusObj`, `mxBus.getCurrentState`, `mxBus.getState`, `obj.getType` (폐기), `obj.getCurrentState` (폐기) |
| 객체의 리비전 확인 | select `last.id`, `previous.id` (`obj.getInfo`) | `FrameworkUtil.getLastRevision`, `FrameworkUtil.isRevisedBusObj` |
| 객체, 관계의 속성 읽기 | `obj.getAttributeValue`, `obj.getAttributeMap`, `obj.getAttributeDetails`, `DomainRelationship.getAttributeValue` | `FrameworkUtil.getAttribute`, `FrameworkUtil.getRelAttribute`, `mxBus.getAttribute`, `mxBus.getAttributeValue`, `mxBus.getAttributeValues`, `mxBus.getAttributeDetails`, `mxBus.getBasics`, `mxBus.print` |
| 객체 만들기, 바꾸기, 지우기 | `DomainObject` 메서드 (`createObject`, `cloneObject`, `reviseObject`, `setAttributeValues`, `setOwner`, `setState`, `promote`, `deleteObject` ...) | `mxBus.create`, `copy`, `revise`, `change`, `setAttributeValue`, `setAttributeValues`, `setDescription`, `setOwner`, `gotoTargetState`, `promote`, `demote`, `delete`, `lock`, `unlock`, `expand` |

### 설정값, 다국어 문구 쪽

[설정값, 다국어 문구](/jpo/api/util/resource)

| 같은 일 | 쓸 것 | 쓰지 않는 것 |
| --- | --- | --- |
| 설정 값 읽기 | `EnoviaResourceBundle.getProperty(context, key)` | `FrameworkProperties.getProperty` (폐기), `UINavigatorUtil.getSystemProperty`, `PersonUtil.getDefault`, `EnoviaResourceBundle.getBundle` |
| 설정 값을 목록으로 읽기 | `FrameworkProperties.getTokenizedProperty(context, key, delimiter)` | `FrameworkProperties.getTokenizedProperty(key, delimiter)` (폐기) |
| 다국어 문구 읽기 | `EnoviaResourceBundle.getProperty(context, resourceFile, locale, key)` | `EnoviaResourceBundle.getProperty(context, suite, key, language)`, `EnoviaResourceBundle.getFrameworkStringResourceProperty`, `MessageUtil.getString`, `MessageUtil.getTranslatedMessage`, `i18nNow.getI18nString` (폐기), `i18nNow.GetString` (폐기), `UINavigatorUtil.getI18nString` (폐기), `UINavigatorUtil.getI18NstringForSuite` (폐기), `FrameworkUtil.i18nStringNow`, `ComponentsUtil.i18nStringNow`, `FrameworkStringResource.getString` (폐기), `EnoviaResourceBundle.getBundle` |
| 문구에 값 넣기 | `MessageUtil.substituteMessageArgs(context, text, args, locale)` | `MessageFormat.format`, `MessageUtil.getMessage`, `StringResource.format`, `MessageUtil.substituteValues`, `FrameworkUtil.findAndReplace`, `ComponentsUIUtil.getI18NString` |
| 타입, 속성, 정책 이름 번역 | `EnoviaResourceBundle.getAdminI18NString(context, kind, name, language)` | `EnoviaResourceBundle.getTypeI18NString`, `getAttributeI18NString`, `getRoleI18NString`, `getFormatI18NString`, `getBasicI18NString`, `getMXI18NString`, `getAdminI18NStringList`, `getI18NForSymbolicTypes`, `getI18NVaultNames`, `i18nNow.getTypeI18NString` 외 같은 이름 메서드, `UINavigatorUtil.getAdminI18NString`, `UINavigatorUtil.getAdminI18NStringList` |
| 상태 이름 번역 | `EnoviaResourceBundle.getStateI18NString(context, policy, state, language)` | `EnoviaResourceBundle.getStateI18NStringList`, `i18nNow.getStateI18NString`, `i18nNow.getStateI18NStringList`, `UINavigatorUtil.getStateI18NString`, `UINavigatorUtil.getStateI18NStringList` |
| 범위 값 번역 | `EnoviaResourceBundle.getRangeI18NString`, `EnoviaResourceBundle.getAttrRangeI18NStringList` | `i18nNow.getRangeI18NString`, `i18nNow.getAttrRangeI18NStringList`, `UINavigatorUtil.getAttrRangeI18NString`, `UINavigatorUtil.getAttrRangeI18NStringList` |
| 로그인 사용자의 언어 얻기 | `context.getLocale()` | `context.getSession().getLanguage()`, `MessageUtil.getLocale(context)`, `UIUtil.getLanguage`, `PersonUtil.getLanguage` |
| 언어 문자열을 Locale 로 | `MessageUtil.getLocale(language)` | `i18nNow.getLocale`, `new Locale(language)` |

### MQL 실행, 환경 변수 쪽

[MQL 실행, 환경 변수](/jpo/api/util/mql)

| 같은 일 | 쓸 것 | 쓰지 않는 것 |
| --- | --- | --- |
| MQL 명령 실행하기 | `MqlUtil.mqlCommand(context, cmd, values...)` | `MqlUtil.mqlCommand(context, cmd)` (폐기), `MqlUtil.mqlCommand(context, mql, cmd)` (폐기), `MqlUtil.getMQL` (폐기), `new MQLCommand().executeCommand` (폐기), `MQLCommand.exec`, `MqlUtil.mqlCommandStream` |
| 값이 없는 MQL 명령 실행하기 | `MqlUtil.mqlCommand(context, cmd, false, false)` | `MqlUtil.mqlCommand(context, cmd)` (폐기), `MqlUtil.mqlCommand(context, cmd, true)` (폐기), `MQLCommand.exec(context, cmd)` |
| MQL 명령 하나만 슈퍼유저로 실행하기 | `MqlUtil.mqlCommand(context, cmd, true, values...)` | `MqlUtil.mqlCommand(context, cmd, true)` (폐기), `MqlUtil.mqlCommand(context, mql, cmd, true)` (폐기), `pushContext` + `mqlCommand` + `popContext` |
| 트리거, 히스토리 없이 MQL 명령 실행하기 | `MqlUtil.mqlCommand(context, historyOff, triggerOff, cmd, push, values...)` | `MqlUtil.mqlCommand(context, historyOff, cmd, push, values...)`, `MqlUtil.mqlCommand(context, historyOff, cmd, push)`, `trigger off` / `trigger on` 명령 |
| 사용자에게 메시지 띄우기 | `MqlUtil.mqlCommand(context, "notice $1", msg)` | `emxContextUtil_mxJPO.mqlNotice`, `mqlWarning`, `mqlError` |
| MQL 환경 변수 읽기 / 쓰기 / 지우기 | `PropertyUtil.getRPEValue` / `setRPEValue` / `unsetRPEValue` | `PropertyUtil.getGlobalRPEValue`, `setGlobalRPEValue`, `unsetGlobalRPEValue`, `MqlUtil.mqlCommand(context, "get env $1", name)` 등 |

### 트랜잭션, 권한 전환 쪽

[트랜잭션, 권한 전환](/jpo/api/util/transaction)

| 같은 일 | 쓸 것 | 쓰지 않는 것 |
| --- | --- | --- |
| 트랜잭션 시작 / 확정 / 되돌리기 | `ContextUtil.startTransaction(context, true)`, `commitTransaction`, `abortTransaction` | `DomainObject.startTransaction` / `commitTransaction` / `abortTransaction` (폐기), `ContextUtil.startTransaction(context, update, historyOff)`, `context.start` / `commit` / `abort`, MQL `start transaction` 등 |
| 트랜잭션 중인지 확인하기 | `ContextUtil.isTransactionActive(context)` | `context.isTransactionActive()` |
| 저장점 만들고 되돌리기 | `ContextUtil.setSavePoint`, `abortSavePoint` | `DomainObject.setSavePoint` (폐기), `MqlUtil.mqlCommand(context, "set transaction savepoint $1", name)` |
| 시스템 사용자 (User Agent) 로 바꾸기 | `ContextUtil.pushContext(context)`, `popContext` | `DomainObject.pushContext` / `popContext` (폐기), `ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), "", "")`, `ContextUtil.pushContext(context, null, null, null)`, MQL `push context`, `emxContextUtil_mxJPO.pushContext` |
| 다른 사용자로 바꾸기 | `ContextUtil.pushContext(context, user, null, null)` | `DomainObject.pushContext(context, user, password, vault)` (폐기), `context.resetContext` |
| 바꾸기 전 사용자 알아내기 | `PropertyUtil.getRPEValue(context, ContextUtil.MX_LOGGED_IN_USER_NAME, true)` | `PropertyUtil.getGlobalRPEValue(context, ContextUtil.MX_LOGGED_IN_USER_NAME)` |
| context 없이 실행하기 | `ContextUtil.runInAnonymousContext(callable)` | `ContextUtil.getAnonymousContext` (폐기), `PropertyUtil.getAnonymousContext`, `PropertyUtil.shutDownAnonymousContext` |

### 사용자, 조직, 보안 컨텍스트 쪽

[사용자, 조직, 보안 컨텍스트](/jpo/api/util/person)

| 같은 일 | 쓸 것 | 쓰지 않는 것 |
| --- | --- | --- |
| 로그인 사용자 이름 | `context.getUser()` | `Person.getPerson(context).getName()` |
| Person 객체 id | `PersonUtil.getPersonObjectID(context, user)` | `Person.getPerson(context, user).getObjectId()`, `PersonUtil.getPersonObject(context, user).getObjectId()` |
| Person 객체 얻기 | `Person.getPerson(context, user)` | `PersonUtil.getPersonObject(context, user)`, `Person.getPerson(context, user, false)`, `PersonUtil.getPersonProperty` (Person 을 읽지 않는 캐시) |
| 화면에 보이는 이름 | `PersonUtil.getFullName(context, user)` | `Person.getDisplayName(context, user)` |
| 회사 | `PersonUtil.getUserCompanyId(context)` | `Person.getCompanyId(context)`, `PersonUtil.getCompanyNameFromCompanyID` (`Company ID` 속성으로 찾음), `FrameworkUtil.getOrganization` |
| 지금 보안 컨텍스트 | `PersonUtil.getActiveSecurityContext(context)` | `context.getRole()` (`ctx::` 붙음), `PersonUtil.getActiveProjectTitle` |
| 기본 보안 컨텍스트 | `PersonUtil.getDefaultSecurityContext`, `getDefaultOrganization`, `getDefaultProject` | `PersonUtil.getDefaultSCValues`, `getDefaultSCRole`, `DomainAccess.getDefaultProject` (사용자 기본값이 아님) |
| 가진 보안 컨텍스트 목록 | `PersonUtil.getSecurityContextsNames(context, user)` | `PersonUtil.getSecurityContexts`, `hasSecurityContext`, `getSecurityContextAssignments`, `isValidSecurityContext`, `getProjectsWithTitleAndName` |
| 역할을 가졌는지 확인 | `PersonUtil.hasAssignment(context, role)` | `PersonUtil.getAssignments(context).contains(role)`, `PersonUtil.isLoggedInUserCRorAdmin` |
| 가진 역할 목록 | `PersonUtil.getAssignments(context, user)` | `PersonUtil.getUserRoles`, `getAllUserAssignments`, `getAssignmentsMap`, `Person.getRoleAssignments` |
| 사용자 그룹의 사람 | `FrameworkUtil.getProjectGroupAssignees(context, group)` | `FrameworkUtil.getPersonsMailListFromUserGroup` (메일이 아니라 이름) |
| 로그인 vault | `context.getVault().getName()` | `PersonUtil.getDefaultVault`, `Person.getVaultName` (회사 vault) |
| 검색할 vault | `VaultUtil.getSearchVaultPattern(context)` | `PersonUtil.getSearchVaults`, `PersonUtil.getSearchDefaultVaults`, `VaultUtil.getSearchVaults`, `VaultUtil.getVaults`, `VaultUtil.getLocalVaults` |

### 소유권 공유, 접근 권한 쪽

[소유권 공유, 접근 권한](/jpo/api/util/access)

| 같은 일 | 쓸 것 | 쓰지 않는 것 |
| --- | --- | --- |
| 조직, 협업공간과 공유 | `DomainAccess.createObjectOwnership(context, id, org, project, access, comment)` | `DomainAccess.createObjectOwnershipForUserGroups`, `createObjectOwnershipMultiple`, 9 개 인자 `createObjectOwnership` |
| 사람과 공유 | `DomainAccess.createObjectOwnership(context, id, personId, access, comment)` | `createObjectOwnership(context, String[])`, `createObjectOwnership(context, id, "", 이름 + "_PRJ", access, comment)` |
| 다른 객체의 권한 물려받기 | `DomainAccess.createObjectOwnership(context, id, fromId, comment)` | `DomainAccess.createObjectOwnershipwithPolicy` |
| 공유했는지 확인 | `DomainAccess.hasObjectOwnership(...)` | `DomainAccess.getObjectOwnershipAccess` |
| 공유 목록 읽기 | `DomainAccess.getAccessSummaryList(context, id)` | `DomainAccess.getOwnershipAccessBasedOnOneLevel` |
| 공유를 모두 지우기 | `DomainAccess.clearMultipleOwnership(context, id)` | `DomainAccess.clearInheritedOwnership` (물려받기만) |
| 줄 수 있는 권한 이름 | `DomainAccess.getLogicalNames(context, id)` | `DomainAccess.getObjectLogicalAccessRange`, `getLogicalNamesForPolicy` |
| 논리 이름 → 실제 권한 | `DomainAccess.getPhysicalAccessMasks(context, id, access)` | `DomainAccess.getPhysicalAccessMasksForPolicy`, 4 개 인자 `getPhysicalAccessMasks` |
| 로그인 사용자의 권한 확인 | `FrameworkUtil.hasAccess(context, obj, access)` (목록이면 select `current.access[...]`) | `FrameworkUtil.hasAccess(context, obj, StringList)`, `hasAccessForStateName`, `AccessUtil.hasReadAccess` 등, `AccessUtil.checkAccess` |
| grant 로 준 권한 거두기 | `obj.revokeAccess(context, grantor, grantee)` | `AccessUtil.revokeAccess` |

### 날짜 쪽

[날짜](/jpo/api/util/date)

| 같은 일 | 쓸 것 | 쓰지 않는 것 |
| --- | --- | --- |
| DB 날짜 값을 `Date` 로 | `eMatrixDateFormat.getJavaDate(value)` | `eMatrixDateFormat.getJavaDate(value, locale)`, `new SimpleDateFormat(...).parse(value)`, `DateFormatUtil.parseGeneric` |
| `Date` 를 DB 날짜 값으로 | `new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US).format(date)` | `new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat())` (Locale 없음), `DateFormatUtil.formatDate` |
| DB 날짜 값을 화면 날짜로 | `eMatrixDateFormat.getFormattedDisplayDateTime(context, value, showTime, format, tz, locale)` | `getFormattedDisplayDateTime` (Context 없는 3 형태, user 를 받는 형태), `getFormattedDisplayDate` (2), `getDateValue` |
| 화면 입력을 DB 날짜 값으로 | `eMatrixDateFormat.getFormattedInputDate(context, value, tz, locale)`, `getFormattedInputDateTime(context, date, time, tz, locale)` | `getFormattedInputDate` (나머지 6 형태, 폐기 2), `getFormattedInputDateTime` (나머지 6 형태, 폐기 2) |
| 연, 월 더하기 | `Calendar.add` | `DateUtil.computeFinishDate(date, years, months)` |

### HTML, URL, JSON 쪽

[HTML, URL, JSON](/jpo/api/util/html-json)

| 같은 일 | 쓸 것 | 쓰지 않는 것 |
| --- | --- | --- |
| HTML 태그 사이에 값 넣기 | `XSSUtil.encodeForHTML(context, s)` | `XSSUtil.encodeForHTML(context, s, false)`, `UINavigatorUtil.htmlEncode` (폐기) |
| URL 인자 값 넣기 | `XSSUtil.encodeForURL(context, s)` | `XSSUtil.encodeForURL(s)`, `FrameworkUtil.encodeURL` (2), `FrameworkUtil.encodeNonAlphaNumeric`, `FrameworkUtil.encodeURLParamValues`, `UINavigatorUtil.encodeURL` (2), `XSSUtil.encodeURLForServer`, `XSSUtil.encodeURLwithParsing`, `URLEncoder.encode` |
| URL 인자 값 되돌리기 | `XSSUtil.decodeFromURL(s)` | `FrameworkUtil.decodeURL`, `URLDecoder.decode` |
| JSON 문자열 만들기 | `BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder())` | `Json.createObjectBuilder()` 만 쓰기, `FrameworkUtil.getJsonObjectFromMap`, `UIUtil.getJSONString`, `UIUtil.getUnescapedJsonString`, `UINavigatorUtil.toJSONString`, `com.matrixone.json.JSONObject` (폐기) |
| JSON 문자열 읽기 | `FrameworkUtil.toJsonObject(s)` | `Json.createReader(...).readObject()`, `UINavigatorUtil.readJsonObject`, `com.matrixone.json.JSONObject` (폐기) |

### 메일, 알림 쪽

[메일, 알림](/jpo/api/util/mail)

| 같은 일 | 쓸 것 | 쓰지 않는 것 |
| --- | --- | --- |
| 알림, 메일 보내기 | `MailUtil.sendMessage` | `emxNotificationUtil_mxJPO.sendJavaMail`, `emxNotificationUtil_mxJPO.objectNotification`, `emxMailUtil_mxJPO.sendMail`, `new IconMail()` + `send`, `MxMessage.sendJavaMail`, MQL `send mail` |
| 다국어 문구로 알림 보내기 | `MailUtil.sendNotification(…, companyName, bundle)` | `MailUtil.sendNotification(…, emailAlso)`, `emxMailUtil_mxJPO.sendNotification`, `emxMailUtil_mxJPO.sendNotificationToUser` |
| 객체 링크 주소 만들기 | `MailUtil.getObjectLinkURI` | `emxNotificationUtil_mxJPO.getObjectLink`, `emxNotificationUtil_mxJPO.getObjectLinkHTML` |

### history 쪽

[history](/jpo/api/util/history)

| 같은 일 | 쓸 것 | 쓰지 않는 것 |
| --- | --- | --- |
| 객체 기록 모두 읽기 | `UINavigatorUtil.getHistoryData(context, id)` | `getHistoryData(context, id, select)`, `getHistoryData(context, type, name, rev)`, `obj.getInfoList(context, "history.time")` 등, `BusinessObject.getHistory`, `BusinessObject.getFilteredHistory` |
| 가장 최근 기록 읽기 | `getHistoryData` 끝 값 | `history.count` + `history[n]`, `getFilteredHistory(…, true, 1)`, `history.time.between[시각\|]` |
| 한 종류 이벤트 기록 읽기 | `obj.getInfoList(context, "history.<이벤트>")` | `print bus $1 select $2 dump`, `ChangeUtil.getHistory` |
| 관계 기록 읽기 | `Relationship.getHistory` | `Relationship.getFilteredHistory`, `DomainRelationship.SELECT_HISTORY` |
| 트랜잭션 기록 풀기 | `TransactionTriggerUtil.parseHistory` | `TransactionTriggerUtil.parseEvents`, `emxTransactionNotificationUtil_mxJPO.parseHistory`, `get env TRANSHISTORY` |
| 이벤트 이름 번역 | `EnoviaResourceBundle.getHistoryI18NString` | `i18nNow.getHistoryI18NString` |

### 이름 채번, 캐시, 로그, 서버 환경 쪽

[이름 채번, 캐시, 로그, 서버 환경](/jpo/api/util/etc)

| 같은 일 | 쓸 것 | 쓰지 않는 것 |
| --- | --- | --- |
| 채번기로 객체 만들기 | `FrameworkUtil.autoName(context, type, policy)` | `autoName` 의 다른 7 형태, `FrameworkUtil.autoRevision` |
| 채번기로 이름만 받기 | `DomainObject.getAutoGeneratedName` | `autoName(…, true, false)`, `obj.getUniqueName`, `obj.getShortUniqueName` |
| 서버 전체 캐시 | `CacheUtil.getCacheObject`, `setCacheObject`, `removeCacheObject` | `CacheUtil.getCacheString`, `getCacheMap`, `getCacheMapList`, `setCacheMap`, `clearCache` |
| 로그 남기기 | slf4j `Logger` | `DebugUtil.debug`, `DebugUtil.setDebug`, `System.out.println`, `MatrixWriter`, `DomainObject.debug` |
| 클라우드인지 알아보기 | `UINavigatorUtil.isCloud` | `FrameworkUtil.isOnPremise` |
| 백그라운드로 실행 | `BackgroundProcess.submitJob(context, program, method, args, (String) null)` | `submitJob` 의 다른 2 형태, `invokeInBackground`, `Job.createAndSubmit` |
