# 날짜 <Badge type="info" text="원본 분석" />

DB 에 문자열로 들어 있는 날짜 값을 `Date` 로 읽고, `Date` 를 DB 값으로 만들고, DB 값과 사용자 화면의 날짜를 서로 바꾸는 방법입니다. 예제의 `part` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다. util 전체를 하고 싶은 일로 찾으려면 [유틸리티](/jpo/api/utilities) 를 봅니다.

::: info 근거
xerinfra.jar (`eMatrixDateFormat`, `DateUtil`, `PersonUtil`, `PropertyUtil`, `UINavigatorUtil`, `FormatUtil`, `FrameworkUtil`), framework.jar (`UITableCommon`, `UIFormCommon`, `UISearch`), eMatrixServletRMI.jar (`matrix.util.DateFormatUtil`) class 파일 (javap) 기준. `eMatrixDateFormat` 은 jakarta.servlet 이 없어 로컬에서 실행하지 않았고, 안에서 부르는 JDK 의 `SimpleDateFormat`, `DateFormat` 과 `DateUtil` 은 로컬 실행으로 확인했습니다 (JDK 21, 시간대 `Asia/Seoul`). 건수는 OOTB JPO 에서 센 호출 수: `eMatrixDateFormat.getJavaDate(` 263, `getEMatrixDateFormat(` 104, `getFormattedInputDate(` 97, `getFormattedDisplayDateTime(` 62, `getEMatrixDisplayDateFormat(` 56, `getFormattedInputDateTime(` 43, `getFormattedDisplayDate(` 28, `DateUtil.computeDuration(` 24, `get("timeZone")` 145, `get("localeObj")` 91
:::

샘플: [XX_UtilDate_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilDate_mxJPO.java) ([메서드별 설명](#샘플-jpo))

배경 설명은 가이드의 [날짜](/jpo/utils#날짜) 에 있습니다.

## 한눈에 보기

| 하고 싶은 일 | 쓸 메서드 |
| --- | --- |
| DB 날짜 형식 알아내기 | [`eMatrixDateFormat.getEMatrixDateFormat()`](#db-날짜-형식) |
| DB 날짜 값을 `Date` 로 | [`eMatrixDateFormat.getJavaDate(value)`](#db-날짜-값을-date-로) |
| `Date` 를 DB 날짜 값으로 (날짜 속성에 넣을 값) | [`new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US).format(date)`](#date-를-db-날짜-값으로) |
| 사용자 시간대, Locale 얻기 | [`requestMap.get("timeZone")`, `requestMap.get("localeObj")`](#시간대와-locale-얻기) |
| DB 날짜 값을 사용자 화면 날짜로 | [`eMatrixDateFormat.getFormattedDisplayDateTime(context, value, false, format, tz, locale)`](#db-날짜-값을-화면-날짜로) |
| DB 날짜 값을 사용자 화면 날짜와 시간으로 | [`eMatrixDateFormat.getFormattedDisplayDateTime(context, value, true, format, tz, locale)`](#db-날짜-값을-화면-날짜로) |
| 화면에서 입력한 날짜를 DB 값으로 | [`eMatrixDateFormat.getFormattedInputDate(context, value, tz, locale)`](#화면-입력을-db-날짜-값으로) |
| 화면에서 입력한 날짜와 시간을 DB 값으로 | [`eMatrixDateFormat.getFormattedInputDateTime(context, date, time, tz, locale)`](#화면-입력을-db-날짜-값으로) |
| 토, 일을 뺀 일수 세기 | [`DateUtil.computeDuration(start, finish)`](#토-일을-뺀-일수) |
| 토, 일을 건너뛰어 끝나는 날 구하기 | [`DateUtil.computeFinishDate(start, days * 86400000L)`](#토-일을-뺀-일수) |

## DB 날짜 형식

DB 의 날짜 값은 날짜가 아니라 서버 설정 형식의 **문자열** 입니다 (`"11/23/2026 02:30:00 PM"`). 형식은 `emxSystem.properties` 에서 읽습니다.

| 설정 | 값 (분석한 properties) | 읽는 메서드 |
| --- | --- | --- |
| `eServiceSuites.eMatrixDateFormat` | `MM/dd/yyyy hh:mm:ss a` | `eMatrixDateFormat.getEMatrixDateFormat()`. DB 날짜 형식 |
| `eServiceSuites.eMatrixInputDateFormat` | `MM/dd/yyyy hh:mm:ss a` | `eMatrixDateFormat.getInputDateFormat()`. 화면 입력을 DB 값으로 만들 때 쓰는 형식 |
| `emxFramework.DateTime.DisplayFormat` | `MEDIUM` | `eMatrixDateFormat.getEMatrixDisplayDateFormat()`. `java.text.DateFormat` 의 상수 (`FULL` 0, `LONG` 1, `MEDIUM` 2, `SHORT` 3). 그 밖의 값이면 2 |
| `emxFramework.DateTime.DisplayTime` | `false` | `eMatrixDateFormat.isTimeDisplay()` |

```java
String dbFormat = eMatrixDateFormat.getEMatrixDateFormat();
int displayFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
```

위 변수의 값 (분석한 설정일 때): `dbFormat` 은 `MM/dd/yyyy hh:mm:ss a`, `displayFormat` 은 `2` (`DateFormat.MEDIUM`).

- 처음 부를 때 `EnoviaResourceBundle.getProperty(context, 키)` 로 네 설정을 읽어 static 필드에 담아 둡니다. 서버 (JVM) 안의 모든 JPO 가 같은 값을 씁니다.
- `isTimeDisplay()` 는 설정을 읽지 않고 담아 둔 값만 돌려줍니다. 다른 메서드가 설정을 읽기 전이면 처음 값 `true` 입니다.
- `setEMatrixDateFormat(형식)`, `setInputDateFormat(형식)` 은 이 static 필드를 바꿔 서버 전체의 형식을 바꿉니다. JPO 에서 부르지 않습니다.

::: info 근거
properties/emxSystem.properties:1278-1279, 1290-1291. xerinfra.jar `eMatrixDateFormat.getEMatrixDateFormat()`, `getInputDateFormat()`: static 필드가 `null` 이거나 빈 문자열이면 `setEMatrixDateFormat((Context) null)` 을 부르고 필드를 돌려줌. `getEMatrixDisplayDateFormat()` 은 필드가 0 일 때 같은 일을 함. `setEMatrixDateFormat(Context)` 는 네 설정을 `EnoviaResourceBundle.getProperty(context, 키)` 로 읽어 `SHORT` 3, `MEDIUM` 2, `LONG` 1, `FULL` 0, 그 밖은 2 로 넣고, `DisplayTime` 이 `false` 면 `displayTime` 을 거짓으로 둠 (읽다 난 `Exception` 은 콘솔에 찍고 넘어감). static 초기값: 두 형식 `""`, `displayTime` 참, `iDateTimeDisplayFormat` 0
:::

## DB 날짜 값을 Date 로

날짜 속성, `modified` 같은 DB 값을 비교하거나 계산하려면 `java.util.Date` 로 바꿉니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `eMatrixDateFormat.getJavaDate(value)` | `value`: DB 날짜 문자열 | `java.util.Date`. 서버 JVM 의 시간대로 읽음 |

안에서 `new SimpleDateFormat(DB 형식, Locale.US).parse(value, new ParsePosition(0))` 로 읽고, 못 읽으면 `new Date(value)` (Java 의 예전 날짜 해석) 로 한 번 더 읽습니다.

| 넘기는 값 | 결과 (서버 시간대가 한국일 때) |
| --- | --- |
| `"11/23/2026 02:30:00 PM"` | 2026-11-23 14:30:00 |
| `"1/5/2026 12:00:00 PM"` | 2026-01-05 12:00:00. 월, 일 앞의 0 은 없어도 됨 |
| `"11/23/2026"` | 2026-11-23 00:00:00. 첫 해석은 실패하고 `new Date("11/23/2026")` 이 읽음 |
| `"13/45/2026 02:30:00 PM"` | 2027-02-14 14:30:00. 범위를 넘는 월, 일을 오류 없이 넘겨 셈 |
| `"2026-11-23"`, `""` | `IllegalArgumentException` |
| `null` | `NullPointerException` |

```java
String attrDueDate = PropertyUtil.getSchemaProperty(context, "attribute_MyDueDate");
Date modified = eMatrixDateFormat.getJavaDate(part.getInfo(context, DomainConstants.SELECT_MODIFIED));
String dueValue = part.getAttributeValue(context, attrDueDate);
Date due = null;
if (UIUtil.isNotNullAndNotEmpty(dueValue)) {
    due = eMatrixDateFormat.getJavaDate(dueValue);
}
```

- 빈 값은 예외가 나므로 먼저 확인합니다.
- 결과 `Date` 는 서버 시간대 기준입니다. 사용자에게 보여 줄 글자는 [화면 날짜로](#db-날짜-값을-화면-날짜로) 바꿉니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `eMatrixDateFormat.getJavaDate(value, locale)` | 그 Locale 로 먼저 읽음. `Locale.US` 가 아니면 `PM` 을 못 읽어 `yyyy/MM/dd'@'HH:mm:ss:z` (GMT) 형식, 그다음 `new Date(value)` 로 넘어감 |
| `new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US).parse(value)` | `getJavaDate` 가 안에서 같은 형식으로 읽음. 시간 없는 값 (`"11/23/2026"`) 은 `ParseException` |
| `matrix.util.DateFormatUtil.parseGeneric(value)` (eMatrixServletRMI.jar) | DB 형식이 아니라 `yyyy/MM/dd'@'HH:mm:ss:z` (GMT) 형식을 읽음. OOTB JPO 호출 0 |

::: info 근거
xerinfra.jar `eMatrixDateFormat.getJavaDate(String)`: `new SimpleDateFormat(getEMatrixDateFormat(), Locale.US).parse(value, new ParsePosition(0))`, 결과가 `null` 이면 `new Date(value)` 를 `Calendar.getInstance(Locale.US)` 에 넣었다 꺼내 돌려줌. `getJavaDate(String, Locale)` 은 그 Locale 의 `SimpleDateFormat` → `DateFormatUtil.getGenericDateFormat()` (eMatrixServletRMI.jar, `yyyy/MM/dd'@'HH:mm:ss:z`, `Locale.US`, GMT) → `new Date(value)` 순서. 표의 결과는 같은 JDK 호출 (`SimpleDateFormat.parse(value, new ParsePosition(0))`, `new Date(value)`) 을 같은 값으로 로컬 실행해 확인
:::

## Date 를 DB 날짜 값으로

날짜 속성에 넣을 값은 DB 날짜 형식의 문자열로 만듭니다. `eMatrixDateFormat` 에는 `Date` 를 DB 형식으로 바꾸는 메서드가 없어서 `SimpleDateFormat` 을 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US).format(date)` | `date`: `java.util.Date` | `String`. 서버 시간대의 DB 날짜 문자열 |

| 호출 (`date` 는 2026-11-23 14:30) | 결과 |
| --- | --- |
| `new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.US).format(date)` | `11/23/2026 02:30:00 PM` |
| `new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.KOREA).format(date)` | `11/23/2026 02:30:00 오후` |
| `new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.KOREA).parse("11/23/2026 02:30:00 PM", new ParsePosition(0))` | `null` (못 읽음) |

```java
String attrDueDate = PropertyUtil.getSchemaProperty(context, "attribute_MyDueDate");
SimpleDateFormat dbFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
part.setAttributeValue(context, attrDueDate, dbFormat.format(new Date()));
```

::: warning SimpleDateFormat 은 Locale.US 로 만듭니다
형식 끝의 `a` 는 `AM` / `PM` 입니다. Locale 을 주지 않으면 서버 기본 Locale 을 써서, 한국어 Locale 서버에서는 `오후` 가 만들어지고 `PM` 이 든 DB 값은 읽지 못합니다. OOTB 도 대부분 `Locale.US` 를 줍니다 (60 건, Locale 없이 12 건).
:::

- 화면에서 고른 날짜 (사용자 시간대) 를 저장할 때는 직접 만들지 말고 [화면 입력을 DB 날짜 값으로](#화면-입력을-db-날짜-값으로) 를 씁니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat())` | 서버 기본 Locale 을 씀. 한국어 Locale 서버에서 `오후` 가 들어감 |
| `matrix.util.DateFormatUtil.formatDate(context, date)` (eMatrixServletRMI.jar) | DB 형식 설정이 아니라 MQL 환경 변수 `MX_NORMAL_DATETIME_FORMAT` 을 읽어 만듦 (값이 없으면 `EEE MMM d, yyyy hh:mm:ss a z`). 부를 때마다 환경 변수를 읽음. OOTB JPO 호출 0 |

::: info 근거
xerinfra.jar `eMatrixDateFormat` 에 `Date` 를 받아 DB 형식 문자열을 돌려주는 public 메서드가 없음 (`getDateValue` 는 화면 날짜를 돌려줌). eMatrixServletRMI.jar `DateFormatUtil.formatDate(Context, Date)` → `initDateFormat(context)` → `Environment.getValue(context, "MX_NORMAL_DATETIME_FORMAT")`. 표의 결과는 로컬 실행으로 확인. OOTB JPO: `new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US)` 60 건, Locale 없이 12 건, 다른 Locale 5 건
:::

## 시간대와 Locale 얻기

화면 날짜와 DB 값을 바꾸는 메서드는 사용자의 시간대 (`tz`) 와 Locale 을 받습니다. 화면에서 부른 JPO 는 받은 Map 에서 꺼냅니다.

| 값 | 꺼내는 곳 | 형 | 예 |
| --- | --- | --- | --- |
| `timeZone` | 폼 필드 function, 생성 / 편집 처리: `requestMap`. 표 열 function: `paramList`. `programMap` 바로 아래에 있는 호출도 있음 ([호출 종류별 인자 구조](/jpo/args)) | `String`. `Double.parseDouble` 로 바꿈 | 한국 `"-9"`, 미국 동부 `"5"` |
| `localeObj` | `requestMap`, `paramList` | `Locale` | `ko_KR` |

- `timeZone` 은 로그인할 때 브라우저가 보낸 `getTimezoneOffset()/60` 값입니다 (일광 절약 시간 중이면 1 을 더함). 세션 속성 `timeZone` 에 담겨 화면마다 넘어옵니다.
- `eMatrixDateFormat` 은 이 값에 `-3600000` 을 곱해 UTC 와의 차이 (밀리초) 로 씁니다. `-9` 이면 `GMT+9:00` 입니다.
- 사용자가 환경설정에서 시간대를 골라 두었으면 Context 를 받는 메서드는 `tz` 보다 그 설정 (Person 의 property `preference_TimeZone`) 을 먼저 씁니다.
- `localeObj` 가 없으면 `context.getLocale()` (세션의 Locale) 을 씁니다. `common/emxCreate.jsp` 는 `requestMap` 에 `localeObj` 를 문자열 (`request.getLocale().toString()`) 로 다시 넣기도 하므로, 형을 확신할 수 없으면 `instanceof Locale` 로 확인합니다 (아래 예제).

::: info 근거
common/scripts/emxClientSideInfo.js:31-35 (`x.getTimezoneOffset()/60`, 일광 절약 시간이면 `+= 1`, `timeZoneOffset` 으로 전송). xerinfra.jar `UINavigatorUtil.processClientSideData` 가 받은 XML 의 `timeZoneOffset` 을 Map 에 넣어 `initializeUserSession` 을 부르고, `initializeUserSession` 이 세션 속성 `timeZone` 에 넣음. common/emxCreate.jsp:83, 173-174 (세션 `timeZone`, `localeObj` 에 `request.getLocale()`), 297 (`requestMap.put("localeObj", request.getLocale().toString())`). eMatrixServletRMI.jar `Context.getLocale()` 은 `getSession().getLocale()`. OOTB JPO: `requestMap.get("timeZone")` 79 건, `programMap` 20 건, `paramMap` 13 건, `paramList` 9 건, `requestMap.get("localeObj")` 52 건, `paramList` 13 건
:::

## DB 날짜 값을 화면 날짜로

표 열이나 폼 필드에서 날짜를 사용자 시간대와 언어로 보여 줄 때 씁니다. 프레임워크의 표, 폼 화면 (`UITableCommon`, `lzDate` 태그 ...) 도 같은 메서드로 날짜를 그립니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `eMatrixDateFormat.getFormattedDisplayDateTime(context, value, showTime, format, tz, locale)` | `value`: DB 날짜 문자열. `showTime`: `true` 면 시간까지. `format`: `DateFormat.SHORT` ~ `FULL`, 보통 `eMatrixDateFormat.getEMatrixDisplayDateFormat()` (0~3 밖이면 이 값). `tz`, `locale`: [시간대와 Locale](#시간대와-locale-얻기) | `String`. `value` 가 `null` 이나 `""` 이면 `""` |

시간대는 이 순서로 정합니다.

1. 로그인 사용자 (`context.getUser()`) 의 시간대 설정 `preference_TimeZone` 이 있으면 그 시간대. 이때 `tz` 는 쓰지 않습니다. 일광 절약 설정 `preference_DaylightSaving` 이 `No` 면 일광 절약 시간이 없는 고정 시간대로 바꿉니다 (`America/Los_Angeles` → `Etc/GMT+8`).
2. 없으면 `tz` 로 `GMT+9:00` 같은 시간대를 만듭니다.

| `format` (시간대 `GMT+9:00`) | `Locale.US` | `Locale.KOREA` |
| --- | --- | --- |
| `DateFormat.SHORT` (3) | `11/23/26` | `26. 11. 23.` |
| `DateFormat.MEDIUM` (2) | `Nov 23, 2026` | `2026. 11. 23.` |
| `DateFormat.LONG` (1) | `November 23, 2026` | `2026년 11월 23일` |
| `DateFormat.FULL` (0) | `Monday, November 23, 2026` | `2026년 11월 23일 월요일` |
| `DateFormat.MEDIUM`, `showTime` 이 `true` | `Nov 23, 2026, 2:30:00 PM` | `2026. 11. 23. 오후 2:30:00` |

```java
public String getModifiedDisplay(Context context, String[] args) throws Exception {
    Map programMap = (Map) JPO.unpackArgs(args);
    Map requestMap = (Map) programMap.get("requestMap");
    double tz = Double.parseDouble((String) requestMap.get("timeZone"));
    Locale locale = context.getLocale();
    if (requestMap.get("localeObj") instanceof Locale) {
        locale = (Locale) requestMap.get("localeObj");
    }
    String modified = DomainObject.newInstance(context, (String) requestMap.get("objectId"))
            .getInfo(context, DomainConstants.SELECT_MODIFIED);
    return eMatrixDateFormat.getFormattedDisplayDateTime(context, modified, false,
            eMatrixDateFormat.getEMatrixDisplayDateFormat(), tz, locale);
}
```

- 폼 필드 function 의 예입니다. 표 열 function 은 `paramList` 에서 꺼냅니다 ([표 열 function](/jpo/args#표-열-function)).
- 시간대 설정은 `PersonUtil.getActualTimeZonePreference` 로 읽습니다. 사람마다 처음 한 번 MQL (`print person ... select property.name property.value dump`) 로 읽어 서버 캐시에 둡니다.
- `pushContext` 안에서 부르면 `context.getUser()` 가 User Agent 라서 User Agent 의 시간대 설정을 봅니다. 실제 사용자 기준으로 보여 주려면 push 전에 부릅니다 ([pushContext](/jpo/transaction-context#슈퍼유저로-바꾸기-pushcontext)).
- 표의 모양은 서버 JDK 의 Locale 데이터를 따릅니다 (표는 로컬 JDK 21 결과). JDK 21 의 영어 시간에서 `PM` 앞의 공백은 U+202F 입니다.
- 형식을 못 맞춘 예외는 `FrameworkException` (`eMatrixDateFormat:Date Formating Error: ...`) 으로 바뀌어 나옵니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `getFormattedDisplayDateTime(context, user, value, showTime, format, tz, locale)` | 위 메서드가 `context.getUser()` 를 넣어 이것을 부름. 다른 사용자 (push 전의 실제 사용자 등) 의 시간대 설정으로 보여 줄 때만 |
| `getFormattedDisplayDateTime(value, showTime, format, tz, locale)` | 시간대 설정을 보지 않고 `tz` 만 씀. Context 형태가 설정이 없을 때 부르는 것 |
| `getFormattedDisplayDateTime(value, showTime, tz, locale)`, `getFormattedDisplayDateTime(value, tz, locale)` | 위 형태에 설정 형식, `showTime` `true` 를 넣어 부름. 시간대 설정을 보지 않음 |
| `getFormattedDisplayDate(value, tz, locale)` | `getFormattedDisplayDateTime(value, false, tz, locale)` 를 그대로 부름. 시간대 설정을 보지 않음 |
| `getFormattedDisplayDate(value, tz)` | 위와 같고 Locale 이 `Locale.US` 로 고정 |
| `getDateValue(context, millis, tz, locale)` | 밀리초 문자열을 DB 형식으로 만든 뒤 쓸 메서드를 날짜만 (`false`) 으로 부름. `tz` 도 문자열. 실패하면 콘솔에 찍고 `""` |

::: info 근거
xerinfra.jar `eMatrixDateFormat.getFormattedDisplayDateTime(Context, String, boolean, int, double, Locale)` 은 `context.getUser()` 를 넣어 `(Context, String user, ...)` 를 부름. 그 메서드는 `PersonUtil.getActualTimeZonePreference(context, user)` 가 비면 Context 없는 5 인자 형태로 넘기고, 있으면 `DateFormat.getDateTimeInstance(format, format, locale)` (`showTime` 거짓이면 `getDateInstance(format, locale)`) 에 `TimeZone.getTimeZone(설정)` 을 넣어 `getJavaDate(value, locale)` 을 format. 5 인자 형태는 `tz * -3600000.0` 으로 `GMT+h:mm` 시간대를 만들어 format (서버 시간대의 일광 절약 시간만큼 보정). `PersonUtil.getActualTimeZonePreference` → `getTimeZonePreference` (`PropertyUtil.getAdminProperty(context, "person", user, "preference_TimeZone")`), `getDaylightSavingPreference` 가 `No` 면 `getDayLightTimeZoneValue`. `PropertyUtil.loadAdminProperties` 가 `MqlUtil.mqlCommand` 로 `print person "..." select property.name property.value dump` 를 읽어 static Map 에 둠. framework.jar `UITableCommon`, `TableService`, `lzDate`, `UIChart`, `RenderPDF` 가 이 6 인자 형태를 부름. 표의 모양은 `DateFormat.getDateInstance` / `getDateTimeInstance` 를 로컬 실행해 확인. OOTB JPO 는 `getFormattedDisplayDateTime(context, ...)` 50 건 / 전체 62 건
:::

## 화면 입력을 DB 날짜 값으로

사용자가 화면 (날짜 고르기) 에서 넣은 날짜를 날짜 속성에 저장할 때 씁니다. 프레임워크의 폼 저장 (`UIFormCommon`) 도 같은 메서드로 날짜 필드 값을 바꿉니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `eMatrixDateFormat.getFormattedInputDate(context, value, tz, locale)` | `value`: 사용자 화면 형식의 날짜. `DateFormat.getDateInstance(getEMatrixDisplayDateFormat(), locale)` 로 읽음 (`Locale.US` `MEDIUM` 이면 `Nov 23, 2026`). `tz`, `locale`: [시간대와 Locale](#시간대와-locale-얻기) | `String`. DB 입력 형식 (`M/d/yyyy hh:mm:ss a`) 의 문자열 |
| `eMatrixDateFormat.getFormattedInputDateTime(context, date, time, tz, locale)` | `date`: 위와 같음. `time`: `2:30:00 PM` 처럼 시:분:초 AM/PM | `String`. 위와 같음 |

- 날짜만 받는 형태는 시간을 정오 (`12:00:00 PM`) 로 채웁니다. 사용자 시간대의 그날 정오를 서버 시간대로 옮긴 값이 됩니다.
- 시간대 설정 (`preference_TimeZone`) 이 있으면 `tz` 대신 그 시간대로 읽습니다.
- 월, 일 앞에 0 을 붙이지 않습니다. 사용자와 서버가 모두 한국 시간이면 `"Nov 23, 2026"` (`Locale.US`) → `"11/23/2026 12:00:00 PM"`, `"Jan 5, 2026"` → `"1/5/2026 12:00:00 PM"` 입니다.

| 넘기는 값 (`Locale.US`, `MEDIUM`) | 날짜 해석 결과 |
| --- | --- |
| `"Nov 23, 2026"` | 2026-11-23 |
| `"11/23/2026"`, `"2026-11-23"`, `""` | `ParseException` |
| `null` | `NullPointerException` |
| `"2026. 11. 23."` (`Locale.KOREA`) | 2026-11-23 |

```java
public Boolean updateDueDate(Context context, String[] args) throws Exception {
    Map programMap = (Map) JPO.unpackArgs(args);
    Map paramMap = (Map) programMap.get("paramMap");
    Map requestMap = (Map) programMap.get("requestMap");
    double tz = Double.parseDouble((String) requestMap.get("timeZone"));
    String dbValue = eMatrixDateFormat.getFormattedInputDate(context, (String) paramMap.get("New Value"), tz,
            context.getLocale());
    DomainObject.newInstance(context, (String) paramMap.get("objectId")).setAttributeValue(context,
            PropertyUtil.getSchemaProperty(context, "attribute_MyDueDate"), dbValue);
    return Boolean.TRUE;
}
```

- OOTB 의 Update Function (`emxCurrencyConversionBase_mxJPO.java` 의 `updateCurrencyConvEffectivityDate`) 도 `New Value` 를 이렇게 바꿔 저장합니다 ([저장 (Update Function)](/jpo/args#저장-update-function)).
- 폼 저장 (`UIFormCommon.commitFields`, 생성 폼의 `updateFiledValues`) 은 날짜 필드 값을 먼저 `getFormattedInputDate` 로 바꿉니다. 화면마다 Update Function 이 받는 `New Value` 가 화면 형식인지 DB 형식인지는 끝까지 따라가지 못했습니다 <Badge type="danger" text="검증 필요" />. 받은 값의 모양을 한 번 찍어 보고 정합니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `getFormattedInputDate(value, tz, locale)` | 시간대 설정을 보지 않음. Context 형태가 설정이 없을 때 부르는 것과 같은 계산 |
| `getFormattedInputDate(context, value, tz)` | Locale 을 서버 기본 (`Locale.getDefault()`) 으로 씀 |
| `getFormattedInputDate(value, tz)` | 값을 화면 형식이 아니라 DB 입력 형식 (`MM/dd/yyyy`) 으로 읽음 (`parseCalendarInputDateTime`, `Locale.US`) |
| `getFormattedInputDate(value, int)`, `(context, value, int)` <Badge type="warning" text="폐기" /> | 시간대가 `int`. 값을 `/` 로 잘라 읽는 예전 방식 |
| `getFormattedInputDate(value, int, locale)`, `(context, value, int, locale)` | 시간대가 `int` (30 분 단위 시간대를 못 넣음). 앞의 것은 폐기 형태를 그대로 부름 |
| `getFormattedInputDateTime(context, date, time, format, tz, locale)` | 날짜 형식을 직접 넘김. 쓸 메서드가 설정 형식을 넣어 이것을 부름 |
| `getFormattedInputDateTime(date, time, format, tz, locale)`, `(date, time, tz, locale)` | 시간대 설정을 보지 않음 |
| `getFormattedInputDateTime(context, date, time, tz)` | Locale 을 서버 기본으로 씀 |
| `getFormattedInputDateTime(date, time, tz)` | 날짜를 `/` 로 잘라 연도만 고치고 DB 입력 형식으로 읽음 (화면 형식이 아님) |
| `getFormattedInputDateTime(date, time, int)`, `(context, date, time, int)` <Badge type="warning" text="폐기" /> | 시간대가 `int` |

::: info 근거
xerinfra.jar `eMatrixDateFormat.getFormattedInputDate(Context, String, double, Locale)` → `getFormattedInputDateTime(context, value, adjustTimeStringForInputFormat(""), getEMatrixDisplayDateFormat(), tz, locale)`. `adjustTimeStringForInputFormat("")` 은 입력 형식에 `hh` / `HH` 가 있을 때 `a` 가 있으면 `12:00:00 PM`, 없으면 `12:00:00`. `getFormattedInputDateTime(Context, String, String, int, double, Locale)` 은 `PersonUtil.getActualTimeZonePreference(context)` 가 비면 Context 없는 형태로, 있으면 그 시간대의 `DateFormat.getDateInstance(format, locale).parse(date)` 에 시, 분, 초를 더해 서버 시간대 `Calendar` 로 꺼냄. Context 없는 형태는 `getDateInstance(format, locale).parse(date)` 에 12 시간을 더하고 `tz * -3600000.0` 의 `SimpleTimeZone` 과 서버 시간대의 `getRawOffset()` 으로 옮김. 결과는 `월/일/연` (`\u0001/\u0001/\u0001`, 입력 형식이 `dd` 로 시작하면 `일/월/연`, `yyyy` 로 시작하면 `연/월/일`) + 공백 + `hh:mm:ss AM` (10 미만은 앞에 0) 을 이어 붙임. 폐기 표시는 `javap -v` 의 `Deprecated: true`. framework.jar `UIFormCommon.commitFields`, `updateFiledValues` 가 `isDateField` 면 `getFormattedInputDate(Context, String, double, Locale)` 를 부르고, `UISearch` 가 `getFormattedInputDateTime(Context, String, String, double, Locale)` 을 부름. 날짜 해석 표는 `DateFormat.getDateInstance(DateFormat.MEDIUM, locale).parse` 를 로컬 실행해 확인. OOTB JPO 는 `getFormattedInputDate(context, ...)` 81 건 / 97 건, `getFormattedInputDateTime(context, ...)` 38 건 / 43 건
:::

## 토, 일을 뺀 일수

`DateUtil` 은 근무일 달력이 아니라 **토요일, 일요일만 빼는** 고정 규칙으로 셉니다. 공휴일, 회사 달력은 보지 않습니다. OOTB 에서는 주로 Program Central (작업, 프로젝트, 시간 기록) 이 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DateUtil.computeDuration(start, finish)` | `start`, `finish`: `Date` | `long`. 두 날을 포함한 월~금의 날 수. 1 보다 작으면 1 |
| `DateUtil.computeFinishDate(start, days * 86400000L)` | `start`: `Date`. 둘째 인자: 일수를 밀리초로 | `Date`. `start` 를 첫날로 세어 그 일수째 되는 월~금. 1 일 이하이면 `start` 와 같은 날 |

| 호출 (2026 년, 시각은 09:00) | 결과 |
| --- | --- |
| `computeDuration(11/23 (월), 11/27 (금))` | `5` |
| `computeDuration(11/23 (월), 11/30 (월))` | `6` |
| `computeDuration(11/23 (월), 11/23 (월))` | `1` |
| `computeDuration(11/21 (토), 11/23 (월))` | `1`. 넘긴 `start` 객체가 11/23 으로 바뀜 |
| `computeDuration(11/27 (금), 11/29 (일))` | `1`. 넘긴 `finish` 객체가 11/27 로 바뀜 |
| `computeDuration(11/23 (월), 12/25 (금))` | `25`. 성탄절도 셈 |
| `computeDuration(11/27 (금), 11/23 (월))` | `1`. 순서가 거꾸로여도 예외 없음 |
| `computeFinishDate(11/23 (월), 5 * 86400000L)` | 11/27 (금) |
| `computeFinishDate(11/23 (월), 6 * 86400000L)` | 11/30 (월) |
| `computeFinishDate(11/23 (월), 86400000L)`, `(…, 0)` | 11/23 (월) |
| `computeFinishDate(11/23 (월), 36 * 3600000L)` (1.5 일) | 11/23 (월). 하루 단위로 버림 |
| `computeFinishDate(11/21 (토), 86400000L)` | 11/23 (월). 넘긴 `start` 객체도 11/23 으로 바뀜 |

```java
String attrStartDate = PropertyUtil.getSchemaProperty(context, "attribute_MyStartDate");
String attrDueDate = PropertyUtil.getSchemaProperty(context, "attribute_MyDueDate");
Date start = eMatrixDateFormat.getJavaDate(part.getAttributeValue(context, attrStartDate));
Date due = eMatrixDateFormat.getJavaDate(part.getAttributeValue(context, attrDueDate));
long workingDays = DateUtil.computeDuration(new Date(start.getTime()), new Date(due.getTime()));
Date finish = DateUtil.computeFinishDate(new Date(start.getTime()), 5 * 86400000L);
```

- 넘긴 `Date` 가 토, 일이면 그 객체 자체를 월요일 (끝 날짜는 금요일) 로 바꿉니다. 원래 값을 다시 쓸 거라면 위처럼 복사본을 넘깁니다.
- 끝나는 날에서 시작일을 거꾸로 구할 때는 `DateUtil.computeStartDate(finish, days * 86400000L)` 입니다. 같은 규칙입니다 (금 11/27 에서 5 일 → 월 11/23).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `DateUtil.computeFinishDate(date, years, months)` | 토, 일을 보지 않고 연, 월만 더함 (`Calendar.add`). 0 이하는 무시. 1/31 에 1 달이면 2/28 |

::: info 근거
xerinfra.jar `DateUtil.computeDuration(Date, Date)`: `Calendar.DAY_OF_WEEK` 로 시작이 토 (7) 면 +2 일, 일 (1) 이면 +1 일, 끝이 일이면 -2 일, 토면 -1 일을 넘긴 `Date` 에 `setTime` 으로 넣고, 날 수 차이에서 7 일마다 2 일과 남은 날의 주말을 빼서 1 을 더함 (0 이하면 1). `computeFinishDate(Date, long)` / `computeStartDate(Date, long)`: 밀리초가 86400000 보다 클 때만 일수로 나누고, 5 일마다 2 일과 남은 주말을 더해 `(일수 - 1) × 하루` 를 더하거나 뺌. `computeFinishDate(Date, int, int)` 는 양수일 때만 `Calendar.add(YEAR / MONTH)`. 표는 로컬 실행 (시간대 `Asia/Seoul`) 으로 확인. 공휴일이나 달력 객체를 읽는 코드는 없음. OOTB 예: emxTaskBase_mxJPO.java, emxWeeklyTimeSheetBase_mxJPO.java
:::

## 예: 소유자와 수정한 날

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

- 날짜는 서버 시간대 기준입니다. 사용자 시간대와 화면 형식으로 보여 주려면 [화면 날짜로](#db-날짜-값을-화면-날짜로) 의 메서드를 씁니다.
- 사용자 이름은 [사용자, 조직, 보안 컨텍스트](/jpo/api/util/person), HTML 이스케이프는 [HTML, URL, JSON](/jpo/api/util/html-json) 에 있습니다.
- 같은 메서드가 [XX_ObjectApi_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/XX_ObjectApi_mxJPO.java) 와 이 페이지의 샘플에 있습니다.

## 샘플 JPO

[XX_UtilDate_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilDate_mxJPO.java) 의 메서드입니다. 모든 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map (`programMap`) 에서 값을 읽습니다.

| 메서드 | 받는 값 (`programMap` 의 키) | 돌려주는 값 | 보여 주는 것 |
| --- | --- | --- | --- |
| `readModifiedDate` | `objectId` | `Map` (`modified`, `modifiedDate`) | DB 값을 `Date` 로 ([DB 날짜 값을 Date 로](#db-날짜-값을-date-로)) |
| `setDueDateNow` | `objectId` | `String` (넣은 DB 값) | `Date` 를 DB 값으로 만들어 속성에 넣음. **데이터를 바꿈** ([Date 를 DB 날짜 값으로](#date-를-db-날짜-값으로)) |
| `getDisplayModified` | `objectId`, `timeZone` (`"-9"`), `localeObj` (없으면 세션 Locale) | `Map` (`date`, `dateTime`) | DB 값을 화면 날짜, 날짜와 시간으로 ([화면 날짜로](#db-날짜-값을-화면-날짜로)) |
| `saveDueDateInput` | `objectId`, `dueDate` (화면 형식 날짜), `timeZone`, `localeObj` | `String` (넣은 DB 값) | 화면 입력을 DB 값으로 바꿔 속성에 넣음. **데이터를 바꿈** ([화면 입력을 DB 날짜 값으로](#화면-입력을-db-날짜-값으로)) |
| `countWorkingDays` | `objectId` | `Map` (`startDate`, `dueDate`, `workingDays`) | 두 날짜 속성 사이의 월~금 일수 ([토, 일을 뺀 일수](#토-일을-뺀-일수)) |
| `getModifiedLabel` | `objectId` | `String` | 소유자 이름과 수정한 날 ([예](#예-소유자와-수정한-날)) |

::: details XX_UtilDate_mxJPO.java

<<< @/../JPOSample/util/XX_UtilDate_mxJPO.java

:::

::: warning
샘플의 타입, 관계, 속성 이름은 예시입니다. 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다.
:::

## 다루지 않은 메서드

::: details 펼쳐 보기
| 클래스 | 메서드 | 이유 |
| --- | --- | --- |
| `eMatrixDateFormat` | `setEMatrixDateFormat(String)`, `setEMatrixDateFormat(Context)`, `setInputDateFormat` | 내부용 |
| `eMatrixDateFormat` | `setEMatrixDateFormat()` <Badge type="warning" text="폐기" /> | 내부용 |
| `eMatrixDateFormat` | `convertToEMatrixDateFormatNumber` | OOTB 미사용 |
| `eMatrixDateFormat` | `adjustTimeStringForInputFormat` | 내부용 |
| `eMatrixDateFormat` | `parseCalendarInputDateTime` | OOTB 미사용 |
| `eMatrixDateFormat` | `getFormattedDisplayInputDateTime(String, double)`, `getFormattedDisplayInputDateTime(String, int)` <Badge type="warning" text="폐기" /> | OOTB 미사용 |
| `DateUtil` | `computeIntervalDates` | OOTB 미사용 |
| `DateUtil` | `getMilliseconds` (2), `compareLocaleDates` (2) | 화면용 |
| `DateUtil` | `getJavaScriptUTCDate` | 화면용 |
| `matrix.util.DateFormatUtil` | `initDateFormat` (2), `parseDateIntoGeneric`, `getGenericDateFormat` | 내부용 |
| `FormatUtil` | `formatNumber`, `parseFloat`, `setDefaultDecimalSymbol` | OOTB 미사용 |
| `FrameworkUtil` | `getFormatedNumber`, `hasDigitSeparator` | OOTB 미사용 |
:::
