# 유틸리티 <Badge type="info" text="원본 분석" />

객체 조회 / 수정 말고 OOTB JPO 가 자주 부르는 보조 메서드입니다. 클래스마다 많이 쓰는 것만 모았습니다. 배경 설명과 주의할 점은 각 절 끝에 링크한 가이드에 있습니다. 예제의 `part`, `attrStatus` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다.

::: info 근거
시그니처와 동작은 xerinfra.jar (`UIUtil`, `FrameworkUtil`, `StringUtil`, `PropertyUtil`, `MqlUtil`, `ContextUtil`, `PersonUtil`, `XSSUtil`, `eMatrixDateFormat`, `EnoviaResourceBundle`, `mxType`, `CacheUtil`, `MapList`), eMatrixServletRMI.jar (`JPO`, `StringList`) class 파일 (javap) 기준. 건수는 jpo 폴더 바로 아래 1,615 개 파일에서 센 호출 수: `JPO.unpackArgs(` 5,783, `PropertyUtil.getSchemaProperty(` 5,149, `EnoviaResourceBundle.getProperty(` 3,862, `MqlUtil.mqlCommand(` 2,365, `XSSUtil.encodeFor` 2,209, `UIUtil.isNotNullAndNotEmpty(` 1,357, `FrameworkUtil.split(` 1,135, `UIUtil.isNullOrEmpty(` 977, `context.getUser()` 742, `StringList.create(` 483, `JPO.packArgs(` 437, `JPO.invoke(` 408, `.addSortKey(` 99
:::

샘플: [XX_ObjectApi_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/XX_ObjectApi_mxJPO.java) 의 `getModifiedLabel`, [XX_MyPartAction_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/XX_MyPartAction_mxJPO.java) (트랜잭션, pushContext, MqlUtil, 다국어)

## 한눈에 보기

| 하고 싶은 일 | 메서드 |
| --- | --- |
| 넘어온 인자 꺼내기, 다른 JPO 부르기 | [`JPO.unpackArgs`, `JPO.invoke`](#인자-jpo) |
| 빈 값 확인, 문자열 자르기 / 붙이기 | [`UIUtil.isNullOrEmpty`, `FrameworkUtil.split`, `join`](#빈-값-문자열) |
| select 목록 만들기, 결과 정렬하기 | [`StringList.create`, `mapList.sort`](#목록-stringlist-maplist) |
| 심볼릭 이름을 실제 이름으로 | [`PropertyUtil.getSchemaProperty`](#스키마-이름) |
| MQL 명령 실행하기 | [`MqlUtil.mqlCommand`](#mql-mqlutil) |
| 권한 없는 작업을 시스템이 대신 하기 | [`ContextUtil.pushContext`](#트랜잭션-권한-contextutil) |
| 로그인 사용자 알아내기 | [`context.getUser()`, `PersonUtil`](#사용자-personutil) |
| 날짜 값 바꾸기 | [`eMatrixDateFormat`](#날짜-ematrixdateformat) |
| HTML 에 값 넣기, 다국어 문구 읽기 | [`XSSUtil`, `EnoviaResourceBundle`](#화면-문자열-xssutil-enoviaresourcebundle) |

## 인자 (JPO)

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `JPO.unpackArgs(args)` | 메서드가 받은 `String[] args` 를 원래 객체로 되돌림 | 보통 `Map`. 키는 부르는 쪽마다 다름 |
| `JPO.packArgs(map)` | Map 을 다른 JPO 에 넘길 `String[]` 로 바꿈 | `String[]` |
| `JPO.invoke(context, "이름", null, "메서드", JPO.packArgs(map), 반환형.class)` | 다른 JPO 의 메서드를 이름으로 부름. 이름에 `_mxJPO` 는 붙이지 않음 | 그 메서드가 돌려준 값 |

```java
Map programMap = (Map) JPO.unpackArgs(args);
String objectId = (String) programMap.get("objectId");

Map params = new HashMap();
params.put("objectId", objectId);
MapList children = (MapList) JPO.invoke(context, "XX_ObjectApi", null, "getChildParts", JPO.packArgs(params), MapList.class);
```

자세히: [호출 종류별 인자 구조](/jpo/args), [다른 곳에서 JPO 부르기](/jpo/#다른-곳에서-jpo-부르기)

## 빈 값, 문자열

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `UIUtil.isNullOrEmpty(s)` | 값이 비었는지 확인 | `boolean` |
| `UIUtil.isNotNullAndNotEmpty(s)` | 위의 반대 | `boolean` |
| `FrameworkUtil.split(s, 구분자)` | 문자열을 구분자로 자름 | `StringList` |
| `FrameworkUtil.join(목록, 구분자)` | `StringList` 나 `String[]` 를 구분자로 이어 붙임 | `String` |
| `FrameworkUtil.findAndReplace(s, 찾을글자, 바꿀글자)` | 찾은 곳을 모두 바꿈 | `String` |

| 호출 | 결과 |
| --- | --- |
| `UIUtil.isNullOrEmpty(null)`, `("")`, `("  ")`, `("null")`, `("NULL")` | `true` |
| `FrameworkUtil.split("a,b,c", ",")` | `a`, `b`, `c` |
| `FrameworkUtil.split("a,,c", ",")` | `a`, 빈 문자열, `c` |
| `FrameworkUtil.split("a,b,", ",")` | `a`, `b`, 빈 문자열 |
| `FrameworkUtil.split(",a", ",")` | `a`. 맨 앞 구분자는 빈 값을 만들지 않음 |
| `FrameworkUtil.split(null, ",")` | 빈 목록 |
| `FrameworkUtil.join(목록, "/")` | `a/b/c` |

```java
StringList idList = FrameworkUtil.split("11111.1.1.1,11111.1.1.2", ",");
String joined = FrameworkUtil.join(idList, "|");
```

::: warning split 의 구분자는 한 글자만 씁니다
`FrameworkUtil.split` 은 구분자 문자열의 글자 하나하나를 구분자로 봅니다. `"||"` 처럼 두 글자 이상을 주면 구분자 글자가 결과에 섞여 나옵니다. OOTB 도 `"|"`, `","` 같은 한 글자만 씁니다.
:::

자세히: [빈 값과 문자열](/jpo/utils#빈-값과-문자열)

::: info 근거
xerinfra.jar `StringUtil.split(String, String)` (`FrameworkUtil` 의 상위 클래스): 문자열이 `null` 이면 빈 `StringList`, 구분자가 `null` 이면 `"~"`. `new StringTokenizer(s, 구분자, true)` 로 토큰을 받아, 토큰이 구분자와 같고 바로 앞 토큰도 구분자면 `""` 를 넣고, 구분자가 아니면 그 토큰을 넣음. 마지막 토큰이 구분자면 `""` 를 하나 더 넣음. 토큰은 구분자 글자 하나씩이라 두 글자 이상의 구분자와는 같아지지 않음. jpo 폴더의 구분자 사용: `"|"` 338 건, `","` 301 건, `":"` 39 건
:::

## 목록 (StringList, MapList)

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `StringList.create(a, b, ...)` | 값 여러 개로 목록을 만듦. select 목록에 많이 씀 | `StringList` |
| `new StringList(s)`, `new StringList(String[])` | 값 하나 / 배열로 목록을 만듦 | `StringList` |
| `list.toStringArray()` | `String[]` 로 바꿈. `deleteObjects`, `disconnect` 에 넘길 때 | `String[]` |
| `mapList.sort(키, 방향, 형)` | 행 Map 의 그 키 값으로 정렬함 | `void` |
| `mapList.addSortKey(키, 방향, 형)` 여러 번 + `mapList.sort()` | 여러 키로 정렬함. 먼저 넣은 키가 우선 | `void` |

| `sort` 의 인자 | 넘기는 값 |
| --- | --- |
| 키 | 행 Map 의 키 (`name`, `attribute[My Quantity]` ...) |
| 방향 | `"ascending"`, `"descending"` |
| 형 | `"string"` (글자 순, 대소문자 구분 없음), `"integer"` (정수), `"real"` (실수), `"date"` (DB 날짜 형식으로 읽어 비교) |

```java
MapList children = part.getRelatedObjects(context, relStructure, "*",
        StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME, DomainConstants.SELECT_MODIFIED),
        StringList.create(DomainConstants.SELECT_RELATIONSHIP_ID), false, true, (short) 1, null, null, 0);
children.sort(DomainConstants.SELECT_MODIFIED, "descending", "date");

children.addSortKey(DomainConstants.SELECT_TYPE, "ascending", "string");
children.addSortKey(DomainConstants.SELECT_NAME, "ascending", "string");
children.sort();
```

- `MapList` 는 `java.util.ArrayList` 이고 요소가 `Map` 입니다. `for (Object o : mapList)` 로 돌면서 `(Map) o` 로 받습니다.
- 방향과 형 문자열은 대소문자를 가리지 않습니다 (`"String"` 도 됨).

자세히: [MapList, StringList](/jpo/domain-object#maplist-stringlist)

::: info 근거
xerinfra.jar `MapList.sort(String, String, String)`: 방향이 `null` 이면 `ascending`, 형이 `null` 이면 `string`, 둘 다 `toLowerCase` 로 바꿔 정렬 키에 넣음. `MapList$MapComparator` 가 형 `string` (`compareToIgnoreCase`), `integer` (`Long.valueOf`), `real` (`Double.valueOf`), `date` (`eMatrixDateFormat.getJavaDate`) 를 나눠 비교. eMatrixServletRMI.jar `StringList` 의 `create(String...)`, `toStringArray()`
:::

## 스키마 이름

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `PropertyUtil.getSchemaProperty(context, "type_MyPart")` | 심볼릭 이름을 실제 이름으로 바꿈 | `String`. 등록 안 된 이름이면 `""` |
| `PropertyUtil.getSchemaProperty(context, "policy", 정책이름, "state_Release")` | 그 정책의 심볼릭 상태 이름을 실제 상태 이름으로 바꿈 | `String` |
| `FrameworkUtil.lookupStateName(context, 정책이름, "state_Release")` | 위와 같은 일 | `String` |
| `FrameworkUtil.getAliasForAdmin(context, "type", 실제이름, true)` | 실제 이름을 심볼릭 이름으로 바꿈 | `String` |
| `DomainObject.getAttributeSelect(속성이름)` | 속성 값을 읽는 select `attribute[속성이름]` 을 만듦 | `String` |
| `mxType.isOfParentType(context, 타입, 상위타입)` | 객체 없이 타입 이름만으로 상속 관계를 확인함 | `boolean` |
| `FrameworkUtil.getRanges(context, 속성이름)` | 속성에 정해 둔 범위 값 목록 | `StringList` |

```java
String typePart = PropertyUtil.getSchemaProperty(context, "type_MyPart");
String policy = part.getInfo(context, DomainConstants.SELECT_POLICY);
String release = PropertyUtil.getSchemaProperty(context, "policy", policy, "state_Release");
String statusSelect = DomainObject.getAttributeSelect(attrStatus);
```

위 변수의 값 (예시): `typePart` 는 `My Part`, `release` 는 `Release`, `statusSelect` 는 `attribute[My Status]`.

자세히: [심볼릭 이름](/jpo/domain-object#심볼릭-이름), [타입과 상태 이름](/jpo/utils#타입과-상태-이름)

## MQL (MqlUtil)

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `MqlUtil.mqlCommand(context, 명령, 값...)` | MQL 명령을 실행함. 명령 안의 `$1`, `$2` ... 를 뒤의 값으로 채움 | `String`. 명령이 출력한 글자 (끝의 줄바꿈 한 글자는 잘려 있음) |
| `MqlUtil.mqlCommand(context, 명령, true, 값...)` | 그 명령 하나만 슈퍼유저 (User Agent) 로 실행함 | `String` |
| `MqlUtil.mqlCommand(context, "notice $1", 문구)` | 사용자 화면에 알림을 띄움 | `String` |

```java
String current = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", objectId, "current");

String both = MqlUtil.mqlCommand(context, "print bus $1 select $2 $3 dump $4", objectId, "name", "current", "|");
StringList nameAndCurrent = FrameworkUtil.split(both, "|");
```

위 변수의 값 (예시): `current` 는 `Release`, `both` 는 `P-0001|Release`.

- 값은 명령 문자열에 이어 붙이지 말고 `$1` 로 넘깁니다. 공백이나 따옴표가 든 값도 안전합니다.
- 실패하면 MQL 오류 문구를 담은 `FrameworkException` 이 납니다.

자세히: [MqlUtil](/jpo/mql-util)

## 트랜잭션, 권한 (ContextUtil)

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `ContextUtil.startTransaction(context, true)` | 쓰기 트랜잭션을 시작함. 이미 열려 있으면 횟수만 셈 | `boolean` |
| `ContextUtil.commitTransaction(context)` | 변경을 확정함. 가장 바깥에서 부른 것만 실제로 확정 | `void` |
| `ContextUtil.abortTransaction(context)` | 변경을 되돌림 | `void` |
| `ContextUtil.isTransactionActive(context)` | 트랜잭션 중인지 | `boolean` |
| `ContextUtil.pushContext(context)` | 지금부터 슈퍼유저 (User Agent) 로 실행함 | `void` |
| `ContextUtil.popContext(context)` | 원래 사용자로 되돌림. 꼭 `finally` 에서 부름 | `void` |

```java
String user = context.getUser();
ContextUtil.pushContext(context);
try {
    part.setAttributeValue(context, attrStatus, "Checked by " + user);
} finally {
    ContextUtil.popContext(context);
}
```

- `pushContext` 안에서는 `context.getUser()` 가 User Agent 를 돌려줍니다. 실제 사용자 이름은 위처럼 push 전에 담아 둡니다.
- 트랜잭션으로 묶는 예는 [여러 변경을 한 묶음으로](/jpo/api/write#여러-변경을-한-묶음으로) 에 있습니다.

자세히: [Context, 트랜잭션, 권한 전환](/jpo/transaction-context)

## 사용자 (PersonUtil)

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `context.getUser()` | 로그인 사용자의 이름 (로그인 id) | `String` |
| `PersonUtil.getPersonObjectID(context)` | 로그인 사용자의 Person 객체 id | `String` |
| `PersonUtil.getFullName(context, 사용자이름)` | 화면에 보이는 이름. 두 번째 인자를 빼면 로그인 사용자 | `String` |
| `PersonUtil.hasAssignment(context, 역할이름)` | 로그인 사용자가 그 역할 (하위 역할 포함) 을 가졌는지 | `boolean` |
| `PersonUtil.getEmail(context, 사용자이름)` | 메일 주소 | `String` |

```java
String user = context.getUser();
String personId = PersonUtil.getPersonObjectID(context);
String fullName = PersonUtil.getFullName(context, user);
```

`getFullName` 이 돌려주는 모양은 시스템 설정 `emxFramework.FullName.Format` 을 따릅니다. 분석한 properties 의 값은 `<First Name> <Last Name>` 입니다.

자세히: [현재 사용자](/jpo/utils#현재-사용자)

::: info 근거
xerinfra.jar `PersonUtil.getFullName(Context, String)` 은 `EnoviaResourceBundle.getProperty(context, "emxFramework.FullName.Format")` 로 읽은 형식을 `getFullName(Context, String, String)` 에 넘김. properties/emxSystem.properties:2419 (`emxFramework.FullName.Format=<First Name> <Last Name>`)
:::

## 날짜 (eMatrixDateFormat)

DB 의 날짜 값은 `MM/dd/yyyy hh:mm:ss a` 형식 문자열입니다 (`"11/23/2026 02:30:00 PM"`).

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `eMatrixDateFormat.getEMatrixDateFormat()` | DB 날짜 형식 문자열 | `String` (`MM/dd/yyyy hh:mm:ss a`) |
| `eMatrixDateFormat.getJavaDate(값)` | DB 날짜 문자열을 `Date` 로 바꿈 | `java.util.Date` |
| `new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US).format(date)` | `Date` 를 DB 날짜 문자열로 바꿈. 날짜 속성에 넣을 값 | `String` |
| `eMatrixDateFormat.getFormattedDisplayDate(값, tz, locale)` | DB 값을 사용자 시간대와 언어의 화면 날짜로 바꿈 | `String` |
| `eMatrixDateFormat.getFormattedInputDate(context, 값, tz, locale)` | 화면에서 입력한 날짜를 DB 형식으로 바꿈 | `String` |

```java
Date modified = eMatrixDateFormat.getJavaDate(part.getInfo(context, DomainConstants.SELECT_MODIFIED));
String dbValue = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US).format(new Date());
```

- `dbValue` 를 날짜 속성에 `setAttributeValue` 로 넣습니다.
- `SimpleDateFormat` 에는 꼭 `Locale.US` 를 줍니다. 형식 끝의 `a` 가 `AM` / `PM` 이라서, 한국어 Locale 이면 `오후` 로 만들어져 DB 가 읽지 못합니다.
- `tz` 는 화면이 넘기는 `timeZone` 값 (`double`), `locale` 은 `localeObj` 입니다.

자세히: [날짜](/jpo/utils#날짜)

## 화면 문자열 (XSSUtil, EnoviaResourceBundle)

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `XSSUtil.encodeForHTML(context, s)` | HTML 태그 사이에 넣을 글자로 바꿈 | `String` |
| `XSSUtil.encodeForHTMLAttribute(context, s)` | `title="..."` 같은 속성 값 자리에 넣을 글자로 바꿈 | `String` |
| `XSSUtil.encodeForURL(context, s)`, `encodeForJavaScript(context, s)`, `encodeForXML(context, s)` | URL 인자, 스크립트 문자열, XML 자리에 넣을 글자로 바꿈 | `String` |
| `EnoviaResourceBundle.getProperty(context, 리소스파일, context.getLocale(), 키)` | 다국어 문구를 읽음 | `String`. 키가 없으면 키를 그대로 돌려줌 |
| `EnoviaResourceBundle.getProperty(context, 키)` | 시스템 설정 값 (`emxSystem.properties` 등) 을 읽음 | `String` |
| `EnoviaResourceBundle.getTypeI18NString(context, 타입, 언어)` | 타입의 화면 표시 이름 | `String` |
| `EnoviaResourceBundle.getRangeI18NString(context, 속성이름, 값, 언어)` | 범위 값의 화면 표시 이름 | `String` |
| `EnoviaResourceBundle.getStateI18NString(context, 정책이름, 상태, 언어)` | 상태의 화면 표시 이름 | `String` |

```java
String name = part.getInfo(context, DomainConstants.SELECT_NAME);
String html = "<span title=\"" + XSSUtil.encodeForHTMLAttribute(context, name) + "\">"
        + XSSUtil.encodeForHTML(context, name) + "</span>";
String label = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),
        "emxFramework.Basic.Policy");
```

- 사용자가 넣은 값 (이름, 설명) 을 HTML 에 그대로 이어 붙이지 않습니다. 넣는 자리에 맞는 메서드로 감쌉니다.
- 언어는 `context.getSession().getLanguage()` (`ko`, `en` ...) 로 얻습니다.

자세히: [HTML 이스케이프](/jpo/utils#html-이스케이프), [다국어](/jpo/i18n-exception#다국어)

## 그 밖에

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `CacheUtil.getCacheObject(context, 키)` | 서버 전체가 같이 쓰는 캐시에서 값을 읽음 | `Object`. 없으면 `null` |
| `CacheUtil.setCacheObject(context, 키, 값)`, `CacheUtil.removeCacheObject(context, 키)` | 캐시에 값을 넣음 / 지움 | `void` / `Object` |
| `PropertyUtil.getRPEValue(context, 이름, global)` | MQL 환경 변수 (RPE) 를 읽음 | `String` |
| `PropertyUtil.setRPEValue(context, 이름, 값, global)` | MQL 환경 변수를 씀 | `void` |

자세히: [서버 전체 캐시](/jpo/utils#서버-전체-캐시-cacheutil), [환경 변수 (RPE)](/jpo/mql-util#환경-변수-rpe)

## 샘플

빈 값 확인, 한 번에 조회, 사용자 이름, 날짜 변환, HTML 이스케이프를 함께 쓰는 메서드입니다. 객체 id 를 받아 `Gildong Hong (2026-11-23)` 처럼 소유자 이름과 수정한 날을 적은 글자를 돌려줍니다.

```java
public String getModifiedLabel(Context context, String[] args) throws Exception {
    Map programMap = (Map) JPO.unpackArgs(args);
    String objectId = (String) programMap.get("objectId");
    if (UIUtil.isNullOrEmpty(objectId)) {
        return DomainConstants.EMPTY_STRING;
    }
    Map info = new DomainObject(objectId).getInfo(context,
            StringList.create(DomainConstants.SELECT_OWNER, DomainConstants.SELECT_MODIFIED));
    String owner = PersonUtil.getFullName(context, (String) info.get(DomainConstants.SELECT_OWNER));
    String modified = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .format(eMatrixDateFormat.getJavaDate((String) info.get(DomainConstants.SELECT_MODIFIED)));
    return XSSUtil.encodeForHTML(context, owner + " (" + modified + ")");
}
```
