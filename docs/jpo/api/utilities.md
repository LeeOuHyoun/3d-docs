# 유틸리티 <Badge type="info" text="원본 분석" />

OOTB JPO 가 객체 조회 / 수정 말고 자주 부르는 보조 메서드입니다. 클래스마다 많이 쓰는 것만 모았고, 쓰는 법과 주의할 점은 각 표 아래 링크한 가이드에 있습니다.

::: info 근거
시그니처는 xerinfra.jar (`UIUtil`, `FrameworkUtil`, `PropertyUtil`, `MqlUtil`, `ContextUtil`, `PersonUtil`, `XSSUtil`, `eMatrixDateFormat`, `EnoviaResourceBundle`, `mxType`, `CacheUtil`, `MapList`), eMatrixServletRMI.jar (`JPO`, `StringList`) class 파일 (javap) 기준. 건수는 jpo 폴더 바로 아래 1,615 개 파일에서 센 호출 수: `JPO.unpackArgs(` 5,783, `PropertyUtil.getSchemaProperty(` 5,149, `EnoviaResourceBundle.getProperty(` 3,862, `MqlUtil.mqlCommand(` 2,365, `XSSUtil.encodeFor` 2,209, `UIUtil.isNotNullAndNotEmpty(` 1,357, `FrameworkUtil.split(` 1,135, `UIUtil.isNullOrEmpty(` 977, `context.getUser()` 742, `StringList.create(` 483, `JPO.packArgs(` 437, `JPO.invoke(` 408, `.addSortKey(` 99
:::

샘플: [XX_ObjectApi_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/XX_ObjectApi_mxJPO.java) 의 `getModifiedLabel`, [XX_MyPartAction_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/XX_MyPartAction_mxJPO.java) (트랜잭션, pushContext, MqlUtil, 다국어)

## 인자 (JPO)

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `JPO.unpackArgs(args)` | 메서드가 받은 `String[] args` 를 Map 으로 되돌림. 키는 호출 종류마다 다름 | `Map` |
| `JPO.packArgs(map)` | Map 을 다른 JPO 에 넘길 `String[]` 로 | `String[]` |
| `JPO.invoke(context, "이름", null, "메서드", JPO.packArgs(map), 반환형.class)` | 다른 JPO 의 메서드를 이름으로 부름. 이름에 `_mxJPO` 는 붙이지 않음 | 반환형 |

자세히: [호출 종류별 인자 구조](/jpo/args), [다른 곳에서 JPO 부르기](/jpo/#다른-곳에서-jpo-부르기)

## 빈 값, 문자열

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `UIUtil.isNullOrEmpty(s)` | `null`, 공백뿐, `"null"` (대소문자 무시) 이면 `true` | `boolean` |
| `UIUtil.isNotNullAndNotEmpty(s)` | 위의 반대 | `boolean` |
| `FrameworkUtil.split(s, 구분자)` | 문자열을 자름 | `StringList` |
| `FrameworkUtil.join(list, 구분자)` | `StringList` 나 `String[]` 를 이어 붙임 | `String` |
| `FrameworkUtil.findAndReplace(s, 찾을것, 바꿀것)` | 모두 바꿈 | `String` |

자세히: [빈 값과 문자열](/jpo/utils#빈-값과-문자열)

## 목록 (StringList, MapList)

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `StringList.create(a, b, ...)` | 값 여러 개로 목록을 만듦. select 목록에 많이 씀 | `StringList` |
| `new StringList(s)`, `new StringList(String[])` | 값 하나 / 배열로 만듦 | `StringList` |
| `list.toStringArray()` | `String[]` 로 바꿈 (`deleteObjects`, `disconnect` 에 넘길 때) | `String[]` |
| `mapList.sort(키, "ascending", "String")` | 행 Map 의 키로 정렬. 방향 `ascending` / `descending`, 형 `String` / `integer` / `date` | `void` |
| `mapList.addSortKey(키, 방향, 형)` 여러 번 + `mapList.sort()` | 여러 키로 정렬 | `void` |

자세히: [MapList, StringList](/jpo/domain-object#maplist-stringlist)

## 스키마 이름

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `PropertyUtil.getSchemaProperty(context, "type_Part")` | 심볼릭 이름 → 실제 이름. 등록 안 된 이름이면 `""` | `String` |
| `PropertyUtil.getSchemaProperty(context, "policy", 정책, "state_Release")` | 심볼릭 상태 이름 → 실제 상태 이름 | `String` |
| `FrameworkUtil.lookupStateName(context, 정책, "state_Release")` | 위와 같은 일 | `String` |
| `FrameworkUtil.getAliasForAdmin(context, "type", 실제이름, true)` | 실제 이름 → 심볼릭 이름 | `String` |
| `DomainObject.getAttributeSelect(속성)` | `attribute[속성]` select 를 만듦 | `String` |
| `mxType.isOfParentType(context, 타입, 상위타입)` | 객체 없이 타입 상속 확인 | `boolean` |
| `FrameworkUtil.getRanges(context, 속성)` | 속성의 범위 값 목록 | `StringList` |

자세히: [심볼릭 이름](/jpo/domain-object#심볼릭-이름), [타입과 상태 이름](/jpo/utils#타입과-상태-이름)

## MQL (MqlUtil)

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", id, "current")` | MQL 실행. `$1`, `$2` ... 를 뒤의 값으로 채움 | `String` (끝 한 글자를 자른 결과) |
| `MqlUtil.mqlCommand(context, cmd, true, 값...)` | 그 명령 하나만 슈퍼유저로 실행 | `String` |
| `MqlUtil.mqlCommand(context, "notice $1", msg)` | 사용자에게 알림 | `String` |

자세히: [MqlUtil](/jpo/mql-util)

## 트랜잭션, 권한 (ContextUtil)

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `ContextUtil.startTransaction(context, true)` | 쓰기 트랜잭션 시작. 이미 열려 있으면 카운트만 올림 | `boolean` |
| `ContextUtil.commitTransaction(context)` / `abortTransaction(context)` | 확정 / 되돌림. 가장 바깥 한 쌍만 실제로 처리 | `void` |
| `ContextUtil.isTransactionActive(context)` | 트랜잭션 중인지 | `boolean` |
| `ContextUtil.pushContext(context)` / `popContext(context)` | User Agent (슈퍼유저) 로 바꿈 / 되돌림. pop 은 `finally` 에서 | `void` |

자세히: [Context, 트랜잭션, 권한 전환](/jpo/transaction-context)

## 사용자 (PersonUtil)

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `context.getUser()` | 로그인 사용자 이름. `pushContext` 중이면 User Agent | `String` |
| `PersonUtil.getPersonObjectID(context)` | 로그인 사용자의 Person 객체 id | `String` |
| `PersonUtil.getFullName(context, 사용자)` | 화면에 보이는 이름. 인자가 없으면 로그인 사용자 | `String` |
| `PersonUtil.hasAssignment(context, 역할)` | 그 역할 (하위 역할 포함) 을 가졌는지 | `boolean` |
| `PersonUtil.getEmail(context, 사용자)` | 메일 주소 | `String` |

자세히: [현재 사용자](/jpo/utils#현재-사용자)

## 날짜 (eMatrixDateFormat)

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `eMatrixDateFormat.getEMatrixDateFormat()` | DB 날짜 형식 (`MM/dd/yyyy hh:mm:ss a`) | `String` |
| `eMatrixDateFormat.getJavaDate(값)` | DB 날짜 문자열 → `Date` | `Date` |
| `new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US).format(date)` | `Date` → DB 날짜 문자열 (속성에 쓸 값) | `String` |
| `eMatrixDateFormat.getFormattedDisplayDate(값, tz, locale)` | DB 값 → 사용자 시간대의 화면 날짜 | `String` |
| `eMatrixDateFormat.getFormattedInputDate(context, 값, tz, locale)` | 화면에서 입력한 날짜 → DB 형식 | `String` |

자세히: [날짜](/jpo/utils#날짜)

## 화면 문자열 (XSSUtil, EnoviaResourceBundle)

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `XSSUtil.encodeForHTML(context, s)` | HTML 태그 사이 글자로 안전하게 | `String` |
| `XSSUtil.encodeForHTMLAttribute(context, s)` | `title="..."` 같은 속성 값 자리 | `String` |
| `XSSUtil.encodeForURL(context, s)`, `encodeForJavaScript(context, s)`, `encodeForXML(context, s)` | URL 인자, 스크립트 문자열, XML 자리 | `String` |
| `EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), 키)` | 다국어 문구. 키가 없으면 키를 그대로 돌려줌 | `String` |
| `EnoviaResourceBundle.getProperty(context, 키)` | 시스템 설정 값 (`emxSystem.properties` 등) | `String` |
| `EnoviaResourceBundle.getTypeI18NString(context, 타입, 언어)` | 타입의 화면 이름 | `String` |
| `EnoviaResourceBundle.getRangeI18NString(context, 속성, 값, 언어)` | 범위 값의 화면 이름 | `String` |
| `EnoviaResourceBundle.getStateI18NString(context, 정책, 상태, 언어)` | 상태의 화면 이름 | `String` |

자세히: [HTML 이스케이프](/jpo/utils#html-이스케이프), [다국어](/jpo/i18n-exception#다국어)

## 그 밖에

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `CacheUtil.getCacheObject(context, 키)` / `setCacheObject(context, 키, 값)` / `removeCacheObject(context, 키)` | 서버 전체가 같이 쓰는 캐시 | `Object` / `void` / `Object` |
| `PropertyUtil.getRPEValue(context, 이름, global)` / `setRPEValue(context, 이름, 값, global)` | MQL 환경 변수 (RPE) 읽기 / 쓰기 | `String` / `void` |

자세히: [서버 전체 캐시](/jpo/utils#서버-전체-캐시-cacheutil), [환경 변수 (RPE)](/jpo/mql-util#환경-변수-rpe)

## 샘플

빈 값 확인, 한 번에 조회, 사용자 이름, 날짜 변환, HTML 이스케이프를 함께 쓰는 열 / 필드용 메서드입니다.

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
