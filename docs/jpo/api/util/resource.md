# 설정값, 다국어 문구 <Badge type="info" text="원본 분석" />

시스템 설정 값 (`emxSystem.properties` 등) 과 사용자 언어의 문구 (`*StringResource*.properties`) 를 읽고, 타입, 정책, 상태 같은 관리 객체 이름을 화면 이름으로 바꾸는 메서드입니다. 예제의 `part` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다. util 전체를 하고 싶은 일로 찾으려면 [유틸리티](/jpo/api/utilities) 를 봅니다.

::: info 근거
xerinfra.jar (`EnoviaResourceBundle`, `Tenant`, `FrameworkProperties`, `MessageUtil`, `i18nNow`, `UINavigatorUtil`, `UIUtil`, `PersonUtil`, `PropertyUtil`, `FrameworkUtil`, `FrameworkStringResource`), eMatrixServletRMI.jar (`matrix.db.Context`, `MatrixSession`, `matrix.util.StringResource`, `LoginServlet`, `ServletUtil`), pnocommon.jar (`ComponentsUtil`, `ComponentsUIUtil`) class 파일 (javap) 기준. `MessageUtil` 에서 Context 를 쓰지 않는 메서드와 `StringResource.format` 은 로컬 실행으로 확인했습니다. `EnoviaResourceBundle`, `i18nNow` 는 로컬에서 초기화되지 않아 (jakarta.servlet 없음) 바이트코드로만 봤습니다. 건수는 OOTB JPO 에서 센 호출 수: `EnoviaResourceBundle.getProperty(` 3,657, `context.getLocale()` 1,477, `context.getSession().getLanguage()` 1,164, `ComponentsUtil.i18nStringNow(` 140, `FrameworkProperties.getProperty(` 137, `MessageUtil.getMessage(` 133, `i18nNow.getI18nString(` 130, `EnoviaResourceBundle.getFrameworkStringResourceProperty(` 121, `i18nNow.getAdminI18NString(` 91, `i18nNow.getRangeI18NString(` 88, `EnoviaResourceBundle.getStateI18NString(` 36, `EnoviaResourceBundle.getTypeI18NString(` 32, `EnoviaResourceBundle.getAdminI18NString(` 30, `EnoviaResourceBundle.getRangeI18NString(` 29, `EnoviaResourceBundle.getAttrRangeI18NStringList(` 14, `MessageUtil.getLocale(` 11, `FrameworkProperties.getTokenizedProperty(` 6
:::

샘플: [XX_UtilResource_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilResource_mxJPO.java) ([메서드별 설명](#샘플-jpo))

## 한눈에 보기

| 하고 싶은 일 | 쓸 메서드 |
| --- | --- |
| 시스템 설정 값 읽기 (`emxSystem.properties` 등) | [`EnoviaResourceBundle.getProperty(context, key)`](#설정-값-읽기) |
| 쉼표로 이어진 설정 값을 목록으로 읽기 | [`FrameworkProperties.getTokenizedProperty(context, key, ",")`](#목록으로-읽기) |
| 사용자 언어로 문구 읽기 | [`EnoviaResourceBundle.getProperty(context, resourceFile, locale, key)`](#다국어-문구-읽기) |
| 문구의 `{0}`, `{1}` 자리에 값 넣기 | [`MessageUtil.substituteMessageArgs(context, text, args, locale)`](#문구에-값-넣기) |
| 타입, 속성, 정책 이름을 화면 이름으로 | [`EnoviaResourceBundle.getAdminI18NString(context, kind, name, language)`](#타입-속성-정책-이름) |
| 상태 이름을 화면 이름으로 | [`EnoviaResourceBundle.getStateI18NString(context, policy, state, language)`](#상태-이름) |
| 속성 범위 값을 화면 이름으로 | [`EnoviaResourceBundle.getRangeI18NString(context, attribute, value, language)`](#범위-값) |
| 로그인 사용자의 언어 얻기 | [`context.getLocale()`](#언어-얻기) |
| `"ko"` 같은 언어 문자열을 Locale 로 | [`MessageUtil.getLocale(language)`](#언어-문자열을-locale-로) |

## 설정 값 읽기

언어와 상관없는 시스템 설정 (`emxSystem.properties` 같은 파일의 키) 을 읽을 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `EnoviaResourceBundle.getProperty(context, key)` | `key`: 설정 키 (`emxFramework.FullName.Format`) | `String`. 앞뒤 공백을 뗀 값. 키가 없으면 `FrameworkException` (문구 `Missing resource for key '키'`) |

| 키 | 분석한 properties 의 값 |
| --- | --- |
| `emxFramework.FullName.Format` | `<First Name> <Last Name>` |
| `eServiceSuiteFramework.StringResourceFileId` | `emxFrameworkStringResource` |
| `emxFramework.Routes.ShowUserNameForFDA` | `true` (문자열) |

```java
String format = EnoviaResourceBundle.getProperty(context, "emxFramework.FullName.Format");
String checkTypes;
try {
    checkTypes = EnoviaResourceBundle.getProperty(context, "emxMyApp.Release.CheckTypes");
} catch (FrameworkException e) {
    checkTypes = "";
}
```

- 없을 수 있는 키는 `FrameworkException` 을 잡아 기본값을 씁니다. OOTB 도 `emxFramework.smallIcon.<타입>` 이 없으면 이렇게 잡아서 `emxFramework.smallIcon.defaultType` 을 읽습니다.
- 값은 모두 문자열입니다. 참 / 거짓은 `"true".equalsIgnoreCase(값)` 으로 비교합니다.
- 값은 DB 에 올린 properties page (`list page *.properties` 에 나오는 page) 에서 읽습니다. 테넌트마다 처음 한 번 page 를 모두 읽어 서버 메모리에 담고, 다음부터는 메모리에서 꺼냅니다. page 를 고친 뒤에는 서버 캐시 (`UICache.clearTenantCache`) 를 비우기 전까지 예전 값이 나옵니다. page 에 없으면 서버 클래스패스의 `emxSystem.properties` 등에서 찾습니다.
- 로그인 사용자의 회사가 있으면 `키.회사이름` (공백은 `_`) 을 먼저 찾고, 없으면 `키` 를 읽습니다. 회사 이름은 사용자마다 처음 한 번 Person 객체의 `to[Employee].from.name` 을 읽어 기억해 둡니다.
- 문구처럼 언어마다 다른 값은 [다국어 문구 읽기](#다국어-문구-읽기) 를 씁니다.
- `PropertyUtil.getEnvironmentProperty(context, name)` 는 properties 설정이 아니라 서버 커널의 환경 값을 읽는 다른 메서드입니다 (처음 읽은 값을 서버가 다시 시작할 때까지 기억).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `FrameworkProperties.getProperty(context, key)` <Badge type="warning" text="폐기" /> | 안에서 `EnoviaResourceBundle.getProperty(context, key)` 를 그대로 부름 |
| `FrameworkProperties.getProperty(key)` <Badge type="warning" text="폐기" /> | context 를 `null` 로 넘김. DB 의 properties page 와 회사별 값을 보지 않고 서버 클래스패스 파일만 읽음 |
| `FrameworkProperties.getProperty(key, value)` <Badge type="warning" text="폐기" /> | 두 번째 값을 쓰지 않고 위와 똑같이 부름 |
| `UINavigatorUtil.getSystemProperty(context, key)` | 안에서 `EnoviaResourceBundle.getProperty(context, key)` 를 부르고, 키가 없으면 예외 대신 `""` (예외는 서버 표준 출력에 찍음) |
| `PersonUtil.getDefault(context, key)` | 안에서 `EnoviaResourceBundle.getProperty(context, key)` 를 부르고, 키가 없거나 공백뿐이면 `""` |
| `EnoviaResourceBundle.getBundle(context, "emxSystem", language)` | 서버 클래스패스의 설정 파일을 모은 `ResourceBundle`. DB 의 page 와 회사별 값을 보지 않음 |

::: info 근거
xerinfra.jar `EnoviaResourceBundle.getProperty(Context, String)` 은 `getProperty(context, key, true)` 를 부름. 이 메서드가 `getCompanyName` 으로 회사 이름을 얻어 `키.회사이름` 을 만들고 `getConfigurationProperty` 를 부름. `getConfigurationProperty` 는 `Tenant.getConfigProperty` (회사 키, 키 순서) 에서 찾고, `null` 이면 `ResourceBundle.getBundle("Configuration_globalUser", new EnoviaResourceBundleControl())` 에서 찾음. 이것도 없으면 `MissingResourceException` 을 `FrameworkException("Missing resource for key '키'")` 로 바꿔 던지고, 바깥에서 한 번 더 `FrameworkException` 으로 감쌈 (감싼 뒤의 문구가 그대로인 것은 로컬 실행으로 확인). `Tenant`: 처음 조회 때 MQL `list page *.properties` 와 `print page '<이름>' select content dump` 로 읽어 `UICache._tenantContent` 에 담고, 있으면 다시 읽지 않음. `UICache.clearTenantCache` 가 이 캐시를 비움. `getCompanyName`: `PersonUtil.getPersonProperty` 에 없으면 `getPersonObjectID` 와 `getInfo(to[Employee].from.name)` 로 읽어 `setPersonProperty` 로 저장. `FrameworkProperties`, `UINavigatorUtil.getSystemProperty`, `PersonUtil.getDefault`, `EnoviaResourceBundle.getBundle` 바이트코드. `PropertyUtil.getEnvironmentProperty` 는 `matrix.db.Environment.getValue` (커널 `getEnvValue`) 를 static `HashMap` 에 담음. OOTB 예: emxCommonFileUIBase_mxJPO.java, ENOQuestionUIBase_mxJPO.java (`smallIcon` 키를 `FrameworkException` 으로 잡음). properties/emxSystem.properties (`emxFramework.FullName.Format`, `eServiceSuiteFramework.StringResourceFileId`, `emxFramework.Routes.ShowUserNameForFDA`)
:::

### 목록으로 읽기

`a, b, c` 처럼 구분자로 이어 둔 설정 값을 목록으로 받을 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkProperties.getTokenizedProperty(context, key, delimiter)` | `key`: 설정 키. `delimiter`: 구분자 한 글자 (`","`) | `StringList`. 항목마다 앞뒤 공백을 뗌. 빈 항목은 `""` 로 남음. 키가 없으면 `FrameworkException` |

| 키 | 분석한 properties 의 값 | 돌려받는 목록 |
| --- | --- | --- |
| `emxFramework.Preferences.Language.Choices` | `en, ja, it, fr, de, zh_cn` | `[en, ja, it, fr, de, zh_cn]` |

```java
StringList languages = FrameworkProperties.getTokenizedProperty(context, "emxFramework.Preferences.Language.Choices", ",");
```

- 값은 [설정 값 읽기](#설정-값-읽기) 와 같은 메서드로 읽은 뒤 자르므로, 읽는 곳과 키가 없을 때의 동작이 같습니다.
- 자르는 규칙은 `FrameworkUtil.split` 과 같습니다 ([문자열, 목록](/jpo/api/util/string-list)). 로컬 실행: `" a , b ,, c "` 를 `","` 로 자르고 다듬으면 `[a, b, , c]`.
- `FrameworkProperties` 의 다른 메서드는 폐기지만 이 형태 (context 를 받는 것) 는 폐기가 아닙니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `FrameworkProperties.getTokenizedProperty(key, delimiter)` <Badge type="warning" text="폐기" /> | context 없이 `FrameworkProperties.getProperty(key)` 로 읽음 (서버 클래스패스 파일만) |

::: info 근거
xerinfra.jar `FrameworkProperties.getTokenizedProperty(Context, String, String)`: `EnoviaResourceBundle.getProperty(context, key)` → `StringUtil.split(값, 구분자)` → `FrameworkUtil.trimAll(목록)`. `trimAll` 결과는 로컬 실행 (`FrameworkUtil.trimAll(FrameworkUtil.split(" a , b ,, c ", ","))` → `[a, b, , c]`). OOTB 예: emxAEFFullSearchBase_mxJPO.java (`getTokenizedProperty(context, "emxFramework.FullTextSearch.Libraries", ",")`). properties/emxSystem.properties (`emxFramework.Preferences.Language.Choices`)
:::

## 다국어 문구 읽기

오류 메시지, 메일 제목처럼 사람이 읽는 글자를 사용자 언어로 읽을 때 씁니다. 배경은 [다국어](/jpo/i18n-exception#다국어) 에 있습니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `EnoviaResourceBundle.getProperty(context, resourceFile, locale, key)` | `resourceFile`: 리소스 파일 이름. 언어 꼬리와 `.properties` 를 뗀 것 (`emxFrameworkStringResource`). `null` 이나 `""` 이면 `emxFrameworkStringResource`. `locale`: 보통 `context.getLocale()`. `key`: 문구 키 | `String`. 사용자 언어에 없으면 영어 값, 어디에도 없으면 **키 문자열 그대로**. 키가 없어도 예외는 나지 않음. `locale` 이나 `key` 가 `null` 이면 `NullPointerException` |

| 넘기는 값 | 돌려받는 것 (분석한 properties 와 바이트코드 기준) |
| --- | --- |
| `("emxFrameworkStringResource", Locale.KOREAN, "emxFramework.Basic.Policy")` | `정책` |
| `("emxFrameworkStringResource", Locale.ENGLISH, "emxFramework.Basic.Policy")` | `Policy` |
| `("emxFrameworkStringResource", Locale.KOREAN, "emxFramework.Common.NotUniqueMsg")` | `개체 유형 이름 개정이 고유하지 않습니다.` |
| `("emxFrameworkStringResource", locale, "emxMyApp.NoSuchKey")` | `emxMyApp.NoSuchKey` (키 그대로) |

```java
String label = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),
        "emxFramework.Basic.Policy");
String msg = EnoviaResourceBundle.getProperty(context, "emxMyAppStringResource", context.getLocale(),
        "emxMyApp.Error.CannotRelease");
```

위 변수의 값 (예시, 한국어 사용자): `label` 은 `정책`.

- 찾는 순서: DB 의 page `<파일>_<언어>` (`emxFrameworkStringResource_ko`) → 사용자 언어가 `en` 이 아니면 `<파일>_en` → 서버 클래스패스의 파일 (`ResourceBundle`) → 키. page 는 파일과 언어마다 처음 한 번 읽어 서버 메모리에 둡니다 ([설정 값 읽기](#설정-값-읽기) 와 같은 캐시).
- 화면에 `emxMyApp.Error.NoPart` 같은 키가 그대로 보이면 키 이름이나 리소스 파일 이름이 틀린 것입니다. 코드에서 확인하려면 돌려받은 값을 키와 비교합니다 (OOTB 도 `contains("emxFramework.Attribute.")` 로 확인).
- 리소스 파일은 `properties/` 에 언어별로 있습니다 (`emxFrameworkStringResource_ko.properties`). 값은 `정책` 같은 유니코드 이스케이프로 적혀 있습니다.
- 읽은 문구로 오류를 알리는 법은 [사용자에게 오류 보여 주기](/jpo/i18n-exception#사용자에게-오류-보여-주기) 에 있습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `EnoviaResourceBundle.getProperty(context, suite, key, language)` | suite 이름 (`"Framework"`, `"ProgramCentral"`) 을 설정 `<suite>.StringResourceFileId` (없으면 `eServiceSuite<suite>.StringResourceFileId`) 로 리소스 파일 이름으로 바꾼 뒤 같은 순서로 찾음. 설정을 한 번 더 읽고, 리소스 파일 이름을 넣으면 설정을 찾지 못함. `language` 가 `null` 이면 `NullPointerException` |
| `EnoviaResourceBundle.getProperty(context, suite, key, locale)` | 위와 같음 (언어를 `Locale` 로 받음) |
| `EnoviaResourceBundle.getFrameworkStringResourceProperty(context, key, locale)` | `emxFrameworkStringResource` 만 읽음. 회사 이름을 붙인 키를 먼저 찾음 |
| `MessageUtil.getString(context, resourceFile, key, value, locale)` | 안에서 `EnoviaResourceBundle.getProperty(context, resourceFile, locale, key)` 를 부름. 네 번째 값은 쓰지 않음 |
| `MessageUtil.getString(resourceFile, key, value, locale)`, `MessageUtil.getString(key, value, locale)` | context 를 `null` 로 위 메서드를 부름. DB 의 page 를 보지 않고 서버 클래스패스 파일만 읽음 |
| `i18nNow.getI18nString(key, resourceFile, language)` <Badge type="warning" text="폐기" /> | context 없이 서버 클래스패스 파일만 읽음. 키가 없으면 키 |
| `new i18nNow().GetString(resourceFile, language, key)` <Badge type="warning" text="폐기" /> | 위와 같이 읽고, 키가 없으면 `""` |
| `UINavigatorUtil.getI18nString(key, resourceFile, language)` <Badge type="warning" text="폐기" /> | `i18nNow.getI18nString` 과 같은 코드 |
| `UINavigatorUtil.getI18NstringForSuite(suite, key, language)` <Badge type="warning" text="폐기" /> | context 를 `null` 로 suite 형태 `getProperty` 를 부름 |
| `FrameworkUtil.i18nStringNow(key, language)` | `emxFrameworkStringResource` 를 폐기된 `GetString` 으로 읽음. 키가 없으면 키 |
| `ComponentsUtil.i18nStringNow(key, language)` (pnocommon.jar) | `emxComponentsStringResource` 를 폐기된 `GetString` 으로 읽음. 키가 없으면 `""` |
| `FrameworkStringResource.getString(key, value, locale)` <Badge type="warning" text="폐기" /> | context 를 `null` 로 `getFrameworkStringResourceProperty` 를 부름 |
| `MessageUtil.getTranslatedMessage(resourceFile, language, key)` | 폐기된 `GetString` 을 그대로 부름 |
| `EnoviaResourceBundle.getBundle(context, resourceFile, language)` | 서버 클래스패스 파일로 `ResourceBundle` 을 만듦. DB 의 page 를 보지 않고, 키가 없으면 `getString` 이 `MissingResourceException` |
| `UINavigatorUtil.getStringResourceFileId(context, suite)` | suite 이름을 리소스 파일 이름으로 바꾸기만 함. 파일 이름을 바로 넘기면 필요 없음 |
| `MessageUtil.getBundleName(context)` | 늘 `"emxFrameworkStringResource"` 를 돌려줌 (로컬 실행) |

::: info 근거
xerinfra.jar `EnoviaResourceBundle.getProperty(Context, String, Locale, String)`: 파일 이름이 비면 `emxFrameworkStringResource`, `UIUtil.getLanguage(locale.getLanguage())` 로 언어를 정해 `Tenant.getStringResourceProperties` (page 캐시) 에서 찾고, 없으면 `en` 으로 한 번 더, 그래도 없으면 `ResourceBundle.getBundle(파일, Locale).getString(키)`. 이 마지막 단계의 예외는 잡아서 키를 돌려줌. `locale.getLanguage()` 와 page 조회는 try 밖이라 `null` 이면 `NullPointerException`. suite 형태 `getProperty(Context, String, String, Locale)` 는 `getProperty(context, suite + ".StringResourceFileId")`, 실패하면 `"eServiceSuite" + suite + ".StringResourceFileId"` 를 읽고 둘 다 실패하면 파일 이름을 `""` 로 둠. `i18nNow.GetString` 은 `getProperty(null, ...)` 를 부르고 결과가 키와 같으면 `""`. `UINavigatorUtil.getI18nString`, `FrameworkUtil.i18nStringNow`, `MessageUtil.getString`, `FrameworkStringResource.getString` 바이트코드. pnocommon.jar `ComponentsUtil.i18nStringNow`. OOTB 예: emxECPartBase_mxJPO.java (`getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(), ...)`), emxLibraryCentralClassificationAttributesBase_mxJPO.java (`contains("emxFramework.Attribute.")` 로 키가 돌아왔는지 확인). properties/emxFrameworkStringResource_ko.properties, _en.properties (`emxFramework.Basic.Policy`, `emxFramework.Common.NotUniqueMsg`)
:::

## 문구에 값 넣기

`{0}`, `{1}` 자리가 있는 문구에 값을 넣을 때 씁니다. 문구는 [다국어 문구 읽기](#다국어-문구-읽기) 로 읽습니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `MessageUtil.substituteMessageArgs(context, text, args, locale)` | `text`: `{0}`, `{1}` 자리가 있는 문구. `args`: 넣을 값 (`Object[]`). 0 번째가 `{0}`. `locale`: 숫자 값을 적는 형식의 언어. `context` 는 쓰지 않음 | `String`. 값이 모자라면 그 자리는 `{1}` 처럼 그대로. `args` 가 `null` 이면 문구 그대로. `text` 가 `null` 이면 `NullPointerException` |

| 호출 (로컬 실행) | 결과 |
| --- | --- |
| `MessageUtil.substituteMessageArgs(null, "{0} 부품이 {1} 상태입니다.", new Object[] {"P-0001", "릴리스됨"}, Locale.KOREAN)` | `P-0001 부품이 릴리스됨 상태입니다.` |
| `MessageUtil.substituteMessageArgs(null, "Can't release {0}", new Object[] {"P-0001"}, Locale.ENGLISH)` | `Can't release P-0001` |
| `MessageUtil.substituteMessageArgs(null, "'{0}' is locked", new Object[] {"P-0001"}, Locale.ENGLISH)` | `'P-0001' is locked` |
| `MessageUtil.substituteMessageArgs(null, "Qty {0}", new Object[] {Integer.valueOf(1234)}, Locale.ENGLISH)` | `Qty 1,234` |
| `MessageUtil.substituteMessageArgs(null, "Qty {0}", new Object[] {"1234"}, Locale.ENGLISH)` | `Qty 1234` |
| `MessageUtil.substituteMessageArgs(null, "{0} and {1}", new Object[] {"A"}, Locale.ENGLISH)` | `A and {1}` |
| `MessageUtil.substituteMessageArgs(null, "{0}{0}", new Object[] {"A"}, null)` | `AA` |
| `MessageUtil.substituteMessageArgs(null, "Total {COUNT} for {0}", new Object[] {"A"}, Locale.ENGLISH)` | `IllegalArgumentException` (`can't parse argument number: COUNT`) |

```java
Locale locale = context.getLocale();
String text = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale,
        "emxFramework.Policy.InvalidPolicyMsg");
String message = MessageUtil.substituteMessageArgs(context, text, new Object[] {"My Policy"}, locale);
```

위 변수의 값 (예시, 한국어 사용자): `text` 는 `"{0}" 정책이 존재하지 않습니다.`, `message` 는 `"My Policy" 정책이 존재하지 않습니다.` (넣는 부분은 로컬 실행으로 확인).

- 문구 안의 `'` 는 그대로 나옵니다. 안에서 `'` 를 `''` 로 바꾼 뒤 `java.text.MessageFormat` 으로 넣기 때문입니다.
- `{` 뒤에 숫자가 아닌 글자가 오면 (`{COUNT}`) `IllegalArgumentException` 이 납니다. 직접 만드는 문구에는 숫자 자리만 씁니다.
- `Integer`, `Double` 같은 숫자 객체는 `locale` 의 형식으로 적힙니다 (`1,234`). 그대로 넣으려면 문자열로 바꿔 넘깁니다.
- OOTB 문구에는 `{0}` 말고 `<NAME>` 자리나 `$<name>` 자리를 쓰는 것도 있습니다. 그 문구는 아래 표의 `MessageUtil.getMessage` 형태가 채웁니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `MessageFormat.format(text, args...)` (Java 표준) | `'` 를 따옴표 기호로 봄. `"Can't release {0}"` 를 넘기면 `Cant release {0}` (로컬 실행) |
| `MessageUtil.getMessage(context, objectId, key, args, null, locale, resourceFile)` (값 배열 형태) | 읽기와 넣기를 한 번에 함. 안에서 `getString` → `substituteValues` → `substituteMessageArgs`. `substituteValues` 를 거치며 문구의 `$` 가 빠짐 (`Cost: $5` → `Cost: 5`, 로컬 실행) |
| `MessageUtil.getMessage(context, key, names, values, null[, resourceFile])` 외 이름 / 값 배열 형태 | `<이름>` 자리를 바꿈 (`StringResource.format`). 같은 자리가 두 번 있으면 첫 번째만 바뀜 |
| `StringResource.format(text, names, values)` (eMatrixServletRMI.jar) | `<이름>` 자리의 첫 번째만 바꿈. `"<NAME> rev <REV> (<NAME>)"` → `P-0001 rev A (<NAME>)` (로컬 실행) |
| `MessageUtil.substituteValues(context, text, objectId, language)` | `$<select>` 를 그 객체의 값으로, `${이름}` 을 MQL `get env global` 결과로 채움. `{0}` 은 그대로이고 `$` 글자가 빠짐. `objectId` 를 넘기면 `getInfo` 로 서버에 감 |
| `FrameworkUtil.findAndReplace(text, "{0}", value)` | 자리마다 한 번씩 불러야 함 |
| `ComponentsUIUtil.getI18NString(context, ...)` (pnocommon.jar) | 안에서 `MessageUtil.getMessage` (값 배열 형태) 를 부름. 파일은 `emxComponentsStringResource` |

::: info 근거
xerinfra.jar `MessageUtil.substituteMessageArgs(Context, String, Object[], Locale)`: `new MessageFormat("")`, `text.replaceAll("'", "''")`, `setLocale(locale)`, `applyPattern`, `format(args)`. context 는 쓰지 않음. `getMessage(Context, String, String, Object[], String, Locale, String)` 은 `getString` → `substituteValues(context, 문구, objectId, 언어)` → 값 배열이 있으면 `substituteMessageArgs`. `getMessage` 의 이름 / 값 배열 형태는 `StringResource.format`. eMatrixServletRMI.jar `StringResource.format(String, String[], String[])`: 이름마다 `"<" + 이름 + ">"` 의 첫 위치만 바꿈. `substituteValues` 는 `$` 로 자른 뒤 `<`, `{`, `[` 를 매크로로 처리하고 `{` 는 `MqlUtil.mqlCommand(context, "get env global $1", 이름)`. 로컬 실행 결과는 위 표와 각 행. OOTB 예: ENOCriteriaBase_mxJPO.java (`MessageUtil.getMessage(context, null, 키, 값배열, null, context.getLocale(), 파일)`), emxECOBase_mxJPO.java (`substituteValues`). properties/emxFrameworkStringResource_ko.properties, _en.properties (`emxFramework.Policy.InvalidPolicyMsg`)
:::

## 관리 객체 이름 번역

타입, 속성, 정책, 상태, 범위 값의 화면 이름은 `emxFrameworkStringResource` 에 `emxFramework.<종류>.<이름>` 키로 들어 있습니다. 메서드는 넘긴 이름의 공백을 `_` 로 바꿔 키를 만들고 [다국어 문구 읽기](#다국어-문구-읽기) 와 같은 곳에서 읽습니다.

### 타입, 속성, 정책 이름

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `EnoviaResourceBundle.getAdminI18NString(context, kind, name, language)` | `kind`: `"Type"`, `"Attribute"`, `"Relationship"`, `"Policy"`, `"Role"`, `"Interface"`, `"Format"`, `"Vault"`, `"Basic"` (`Owner`, `Policy` 같은 기본 항목). `name`: 실제 이름 (`Part`). `language`: `"ko"` 같은 언어 문자열 ([언어 얻기](#언어-얻기)) | `String`. 화면 이름. 번역 값이 비었거나 `language` 가 `null` 이면 넘긴 이름. 번역 키가 없으면 키 문자열 (`emxFramework.Type.My_Part`) <Badge type="danger" text="검증 필요" /> |

| 넘기는 값 | 읽는 키 | 분석한 properties 의 값 (`ko` / `en`) |
| --- | --- | --- |
| `("Type", "Part", ...)` | `emxFramework.Type.Part` | `부품` / `Part` |
| `("Attribute", "Originator", ...)` | `emxFramework.Attribute.Originator` | `작성자` / `Originator` |
| `("Policy", "EC Part", ...)` | `emxFramework.Policy.EC_Part` | `EC 부품` / `EC Part` |
| `("Role", "Employee", ...)` | `emxFramework.Role.Employee` | `직원` / `Employee` |
| `("Basic", "Policy", ...)` | `emxFramework.Basic.Policy` | `정책` / `Policy` |
| `("Type", "My Part", ...)` | `emxFramework.Type.My_Part` | 없음. 리소스에 이 키를 넣어야 번역됨 |

```java
String language = context.getLocale().getLanguage();
String typeLabel = EnoviaResourceBundle.getAdminI18NString(context, "Type", "Part", language);
String attrLabel = EnoviaResourceBundle.getAdminI18NString(context, "Attribute", attrStatus, language);
```

위 변수의 값 (예시, 한국어 사용자): `typeLabel` 은 `부품`.

- 이 메서드들은 `emxFrameworkStringResource` 만 읽습니다. 로그인 사용자의 회사가 있으면 `키.회사이름` 을 먼저 찾습니다.
- 번역 키가 없을 때 키 문자열이 오면 화면에 `emxFramework.Type.My_Part` 가 그대로 보입니다. 직접 만든 타입, 속성, 정책에는 이 모양의 키를 넣어 둡니다.
- 돌려받은 화면 이름을 select 나 MQL 에 다시 넣지 않습니다. 실제 이름이 아닙니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `EnoviaResourceBundle.getTypeI18NString(context, type, language)` | `getAdminI18NString(context, "Type", type, language)` 와 같은 키를 읽음 |
| `EnoviaResourceBundle.getAttributeI18NString`, `getRoleI18NString`, `getFormatI18NString`, `getBasicI18NString` | 종류를 `Attribute`, `Role`, `Format`, `Basic` 으로 정해 둔 같은 메서드 |
| `EnoviaResourceBundle.getMXI18NString(context, name, middle, language, kind)` | 위 메서드들이 안에서 부르는 메서드. 키를 직접 조립함 |
| `EnoviaResourceBundle.getAdminI18NStringList(context, kind, names, language)` | `getAdminI18NString` 을 이름마다 부른 목록 |
| `EnoviaResourceBundle.getI18NForSymbolicTypes(context, "type_Part,type_Document", language)` | 심볼릭 이름을 실제 이름으로 바꾼 뒤 `getAdminI18NString(context, "Type", ...)` 을 부른 목록 |
| `EnoviaResourceBundle.getI18NVaultNames(context, "vault1,vault2", language)` | `getAdminI18NString(context, "Vault", ...)` 결과를 `, ` 로 이어 붙임 |
| `i18nNow.getTypeI18NString(type, language)` 외 `i18nNow` 의 같은 이름 메서드 (`getAdminI18NString`, `getAttributeI18NString`, `getRoleI18NString`, `getAdminI18NStringList`, `getMXI18NString`, `getViewerI18NString` ...) | context 없이 폐기된 `GetString` 으로 서버 클래스패스 파일만 읽음. 번역이 없으면 넘긴 이름 |
| `UINavigatorUtil.getAdminI18NString(context, kind, name, language)`, `getAdminI18NStringList(context, ...)` | 폐기된 `UINavigatorUtil.getMXI18NString` 을 부름. 키가 돌아오면 넘긴 이름으로 바꿈 |
| `UINavigatorUtil.getAdminI18NString(kind, name, language)`, `getAdminI18NStringList(kind, names, language)` | context 없이 폐기된 `getMXI18NString` → `getI18nString` (서버 클래스패스 파일만) |

::: info 근거
xerinfra.jar `EnoviaResourceBundle.getMXI18NString(Context, String name, String middle, String language, String kind)`: `"emxFramework." + kind + "." + (middle 이 "" 가 아니면 middle + ".") + name` 의 공백을 `_` 로 바꿔 `getFrameworkStringResourceProperty(context, 키, new Locale(language))` 를 부르고, 결과가 `null` 이나 `""` 가 아니면 그 값, 아니면 넘긴 이름. 예외 (`language` 가 `null` 인 경우 포함) 도 넘긴 이름. `getAdminI18NString`, `getTypeI18NString` 등은 이 메서드에 종류만 넘김. `getFrameworkStringResourceProperty` 는 page 에 키가 없으면 서버 클래스패스의 `emxFrameworkStringResource` 를 `ResourceBundle.getBundle` 로 열어 찾고, `getString` 이 실패하면 키를 돌려줌. 이 파일이 클래스패스에 없으면 `getBundle` 의 예외가 `getMXI18NString` 까지 올라가 넘긴 이름이 옴. 서버 클래스패스에 이 파일이 있는지는 분석한 소스로 확인하지 못함 <Badge type="danger" text="검증 필요" />. `UINavigatorUtil.getMXI18NString` 은 결과가 키와 같으면 넘긴 이름으로 바꿈 (`getSuiteKeyValue`). `i18nNow.getMXI18NString` 은 `GetString("emxFrameworkStringResource", language, 키)` 가 `""` 이면 넘긴 이름. OOTB 예: emxAEFFullSearchBase_mxJPO.java (`getAdminI18NString(context, "Type", ...)`). properties/emxFrameworkStringResource_ko.properties, _en.properties (`emxFramework.Type.Part`, `emxFramework.Attribute.Originator`, `emxFramework.Policy.EC_Part`, `emxFramework.Role.Employee`, `emxFramework.Basic.Policy`)
:::

### 상태 이름

상태 이름은 정책마다 따로 번역되어 있어서 정책 이름을 함께 넘깁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `EnoviaResourceBundle.getStateI18NString(context, policy, state, language)` | `policy`: 정책 실제 이름. `state`: 상태 실제 이름 (`current` 의 값) | [타입, 속성, 정책 이름](#타입-속성-정책-이름) 과 같음 |

| 넘기는 값 | 읽는 키 | 분석한 properties 의 값 (`ko` / `en`) |
| --- | --- | --- |
| `("EC Part", "Preliminary", ...)` | `emxFramework.State.EC_Part.Preliminary` | `작업 중` / `In Work` |
| `("EC Part", "Release", ...)` | `emxFramework.State.EC_Part.Release` | `릴리스됨` / `Released` |

```java
Map info = part.getInfo(context, StringList.create(DomainConstants.SELECT_POLICY, DomainConstants.SELECT_CURRENT));
String stateLabel = EnoviaResourceBundle.getStateI18NString(context, (String) info.get(DomainConstants.SELECT_POLICY),
        (String) info.get(DomainConstants.SELECT_CURRENT), context.getLocale().getLanguage());
```

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `EnoviaResourceBundle.getStateI18NStringList(context, policies, states, language)` | `getStateI18NString` 을 상태마다 부른 목록 |
| `EnoviaResourceBundle.getStateI18NStringList(policy, states, language)` | context 없이 `i18nNow.getStateI18NString` 을 상태마다 부름 |
| `i18nNow.getStateI18NString(policy, state, language)`, `i18nNow.getStateI18NStringList` | context 없이 폐기된 `GetString` 으로 서버 클래스패스 파일만 읽음 |
| `UINavigatorUtil.getStateI18NString(policy, state, language)`, `UINavigatorUtil.getStateI18NStringList` | context 없이 폐기된 `getMXI18NString` 을 부름 |

::: info 근거
xerinfra.jar `EnoviaResourceBundle.getStateI18NString(Context, String, String, String)` 은 `getMXI18NString(context, state, policy, language, "State")`. `getStateI18NStringList(Context, StringList, StringList, String)` 는 두 번째 목록 (상태) 을 돌며 같은 순서의 첫 번째 목록 (정책) 과 짝지어 부름. OOTB 예: ENOCharacteristicMasterUIBase_mxJPO.java, emxDecisionBase_mxJPO.java. properties/emxFrameworkStringResource_ko.properties, _en.properties (`emxFramework.State.EC_Part.Preliminary`, `emxFramework.State.EC_Part.Release`)
:::

### 범위 값

속성에 정해 둔 범위 값 (선택 목록) 의 화면 이름입니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `EnoviaResourceBundle.getRangeI18NString(context, attribute, value, language)` | `attribute`: 속성 실제 이름. `value`: 범위 값 | [타입, 속성, 정책 이름](#타입-속성-정책-이름) 과 같음 |
| `EnoviaResourceBundle.getAttrRangeI18NStringList(context, attribute, values, language)` | `values`: 범위 값 목록 (`StringList`) | `StringList`. 같은 순서의 화면 이름. `values` 가 `null` 이면 빈 목록 |

| 넘기는 값 | 읽는 키 | 분석한 properties 의 값 (`ko` / `en`) |
| --- | --- | --- |
| `("Unit of Measure", "EA (each)", ...)` | `emxFramework.Range.Unit_of_Measure.EA_(each)` | `EA(낱개)` / `EA (each)` |

```java
String language = context.getLocale().getLanguage();
String statusLabel = EnoviaResourceBundle.getRangeI18NString(context, attrStatus,
        part.getAttributeValue(context, attrStatus), language);
StringList values = FrameworkUtil.getRanges(context, attrStatus);
StringList labels = EnoviaResourceBundle.getAttrRangeI18NStringList(context, attrStatus, values, language);
```

- 범위 값 목록은 `FrameworkUtil.getRanges` 로 읽습니다 ([스키마 이름, 타입·정책·속성 정보](/jpo/api/util/schema)).
- `labels` 는 화면에 보일 이름, `values` 는 저장할 값입니다. 속성에는 `values` 쪽을 넣습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `i18nNow.getRangeI18NString(attribute, value, language)`, `i18nNow.getAttrRangeI18NStringList(attribute, values, language)` | context 없이 폐기된 `GetString` 으로 서버 클래스패스 파일만 읽음 |
| `UINavigatorUtil.getAttrRangeI18NString(attribute, value, language)`, `UINavigatorUtil.getAttrRangeI18NStringList(attribute, values, language)` | context 없이 폐기된 `getMXI18NString` 을 부름 |
| `UINavigatorUtil.getAttrRangeI18NStringList(context, attribute, values, language)` | 폐기된 `UINavigatorUtil.getMXI18NString` 을 값마다 부름 |

::: info 근거
xerinfra.jar `EnoviaResourceBundle.getRangeI18NString(Context, String, String, String)` 은 `getMXI18NString(context, value, attribute, language, "Range")`, `getAttrRangeI18NStringList` 는 값마다 같은 호출 (목록이 `null` 이면 빈 `StringList`). OOTB 예: emxRouteBase_mxJPO.java (`getRangeI18NString`), ENOCriteriaUIBase_mxJPO.java (`getAttrRangeI18NStringList`). properties/emxFrameworkStringResource_ko.properties, _en.properties (`emxFramework.Range.Unit_of_Measure.EA_(each)`)
:::

## 언어 얻기

번역 메서드에 넘길 로그인 사용자의 언어를 얻습니다. 배경은 [언어 얻기](/jpo/i18n-exception#언어-얻기) 에 있습니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `context.getLocale()` | 없음 | `Locale`. 웹 로그인 때 브라우저의 `Accept-Language` 로 정해짐 (`ko-KR,...` → `ko_KR`). 정한 적 없는 context 는 서버 JVM 기본 Locale. [다국어 문구 읽기](#다국어-문구-읽기), [문구에 값 넣기](#문구에-값-넣기) 에 넘김 |
| `context.getLocale().getLanguage()` | 없음 | 언어 문자열 (`ko`, `en`). [관리 객체 이름 번역](#관리-객체-이름-번역) 처럼 언어 문자열을 받는 메서드에 넘김 |

```java
Locale locale = context.getLocale();
String language = locale.getLanguage();
```

- `context.getLocale()` 과 `context.getSession().getLanguage()` 는 세션에 따로 저장됩니다. `context.setLocale(locale)` 은 둘 다 바꾸고, `context.setLanguage(s)` 는 언어 문자열만 바꿉니다.
- 웹 로그인 서블릿은 언어 문자열에 `Accept-Language` 헤더를 그대로 넣습니다 (`ko-KR,ko;q=0.9,en-US;q=0.8` 같은 모양). 언어를 직접 비교할 때는 `context.getLocale().getLanguage()` 를 씁니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `context.getSession().getLanguage()` | 웹 로그인 서블릿이 `Accept-Language` 헤더를 그대로 넣어 `ko-KR,ko;q=0.9,...` 같은 값일 수 있음 (뒤에 `setLocale` 이 불리면 `ko`). 비었으면 JVM 기본 언어 |
| `MessageUtil.getLocale(context)` | `context.getSession().getLanguage()` 를 `MessageUtil.getLocale(문자열)` 로 다시 풀어 Locale 을 만듦 |
| `UIUtil.getLanguage(s)` | 사용자 언어를 얻는 메서드가 아님. 쉼표 앞 첫 값만 돌려줌 (`"ko-KR,ko;q=0.9"` → `"ko-KR"`). 비면 `"en"`, `null` 이면 `NullPointerException` |
| `PersonUtil.getLanguage(context)` | 로그인 언어가 아님. person 의 property `IconMailLanguagePreference` (없으면 설정 `emxFramework.Preferences.Language.Default`) |

::: info 근거
eMatrixServletRMI.jar `Context.getLocale()` 은 `getSession().getLocale()`. `MatrixSession` 생성자가 `_locale = Locale.getDefault()`, `_language = null` 로 두고, `getLanguage()` 는 `_language` 가 `null` 이면 `Locale.getDefault().getLanguage()`. `Context.setLocale` 은 세션 Locale 과 `setLanguage(locale.getLanguage())` 를 함께 바꿈. `LoginServlet.allowUser`, `ServletUtil.checkAuthentication` 은 `setLocale(Framework.getLocale(Accept-Language))` 뒤에 `setLanguage(Accept-Language 헤더)` 를 부름. 3dspace/common/emxCommonAppInitialization.jsp, emxCommonAppInitializationInclude.inc 는 `context.setLocale(i18nNow.getLocale(Accept-Language))`. xerinfra.jar `UIUtil.getLanguage(String)` (`split(",")[0]`, 비면 `"en"`), `PersonUtil.getLanguage(Context)` (`PropertyUtil.getAdminProperty(context, "person", 사용자, "IconMailLanguagePreference")`), `MessageUtil.getLocale(Context)` 바이트코드
:::

### 언어 문자열을 Locale 로

`"ko"` 같은 언어 문자열만 있을 때 (받는 사람의 언어로 메일을 쓸 때 등) Locale 로 바꿉니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `MessageUtil.getLocale(language)` | `language`: `"ko"`, `"ko-KR"`, `Accept-Language` 모양 (`"ko-KR,ko;q=0.9"`) | `Locale`. 비었거나 `null` 이면 `en_US` |

| 호출 (로컬 실행) | 결과 |
| --- | --- |
| `MessageUtil.getLocale("ko")` | `ko` (나라 없음) |
| `MessageUtil.getLocale("ko-KR")` | `ko_KR` |
| `MessageUtil.getLocale("ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")` | `ko_KR` |
| `MessageUtil.getLocale("en-US,en;q=0.9")` | `en_US` |
| `MessageUtil.getLocale("")`, `MessageUtil.getLocale((String) null)` | `en_US` |

```java
Locale locale = MessageUtil.getLocale("ko");
String label = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Basic.Policy");
```

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `i18nNow.getLocale(s)` | 비었거나 `null` 이면 빈 Locale (`MessageUtil.getLocale` 은 `en_US`). 클래스를 처음 쓸 때 설정을 읽음 |
| `new Locale(s)` (Java 표준) | `"ko-KR,ko;q=0.9"` 를 넘기면 language 가 `ko-kr,ko;q=0.9` 가 됨 (로컬 실행) |

::: info 근거
xerinfra.jar `MessageUtil.getLocale(String)`: 첫 토큰을 `-`, `,`, `;` 위치로 나눠 `new Locale(언어, 나라)`, 예외면 `new Locale("en", "US")`. 결과는 로컬 실행. `i18nNow.getLocale(String)` 은 `initLocale` 에서 `null`, `""`, `"null"` 이면 언어와 나라를 `""` 로 둔 채 `new Locale("", "")`. `i18nNow` 의 static 초기화가 `FrameworkProperties.getProperty("emxFramework.Automation.Test")` 를 부름 (로컬에서는 이 단계에서 `NoClassDefFoundError`)
:::

## 샘플 JPO

[XX_UtilResource_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilResource_mxJPO.java) 의 메서드입니다. 모든 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map (`programMap`) 에서 값을 읽습니다. 데이터를 바꾸는 메서드는 없습니다.

| 메서드 | 받는 값 (`programMap` 의 키) | 돌려주는 값 | 보여 주는 것 |
| --- | --- | --- | --- |
| `readSetting` | `key`, `defaultValue` | `String` | 설정 값. 키가 없으면 `defaultValue` ([설정 값 읽기](#설정-값-읽기)) |
| `readSettingList` | `key` | `StringList` | 쉼표로 이어진 설정 값을 목록으로 ([목록으로 읽기](#목록으로-읽기)) |
| `readMessage` | `key` | `String` | 로그인 사용자 언어의 문구 ([다국어 문구 읽기](#다국어-문구-읽기)) |
| `readMessageInLanguage` | `key`, `language` (`ko` ...) | `String` | 정한 언어의 문구 ([언어 문자열을 Locale 로](#언어-문자열을-locale-로)) |
| `getDuplicateNameMessage` | `objectId` | `String` | 문구의 `{0}` 에 타입 화면 이름 넣기 ([문구에 값 넣기](#문구에-값-넣기)) |
| `getDisplayNames` | `objectId` | `Map` (`type`, `policy`, `current`) | 타입, 정책, 상태 이름 번역 ([관리 객체 이름 번역](#관리-객체-이름-번역)) |
| `getStatusLabel` | `objectId` | `String` | 속성 범위 값 하나 번역 ([범위 값](#범위-값)) |
| `getStatusChoices` | 없음 | `Map` (`values`, `labels`) | 범위 값 목록과 화면 이름 ([범위 값](#범위-값)) |
| `getUserLanguage` | 없음 | `Map` (`locale`, `language`) | 로그인 사용자의 언어 ([언어 얻기](#언어-얻기)) |

::: details XX_UtilResource_mxJPO.java

<<< @/../JPOSample/util/XX_UtilResource_mxJPO.java

:::

::: warning
샘플의 타입, 관계, 속성 이름은 예시입니다. 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다.
:::

## 다루지 않은 메서드

::: details 펼쳐 보기
| 클래스 | 메서드 | 이유 |
| --- | --- | --- |
| `EnoviaResourceBundle` | `getHelpProperty` | 화면용 |
| `EnoviaResourceBundle` | `getViewerI18NString` | 화면용 |
| `EnoviaResourceBundle` | `getAttributeTypeI18NString` | 화면용 |
| `EnoviaResourceBundle` | `getCalendarI18NString`, `getHistoryI18NString` | OOTB 미사용 |
| `EnoviaResourceBundle` | `getRoleDescriptionI18NString`, `getAdminDescriptionI18NString`, `getDefaultAttributeValueI18NString` | OOTB 미사용 |
| `EnoviaResourceBundle` | `getDimensionI18NString`, `getDimensionI18NStringList` | OOTB 미사용 |
| `EnoviaResourceBundle` | `getProperties`, `getI18nKey` | OOTB 미사용 |
| `EnoviaResourceBundle` | `getAutomationString`, `getAutoTestString`, `getCompanyName`, `getProperty(Context, String, boolean)` | 내부용 |
| `FrameworkProperties` | `getProperty(Context, String, String)`, `getProperty(String, String, String)` | 화면용 |
| `FrameworkProperties` | `getCompanyName`, `getSuiteKeyFromSuiteDir`, `populateSuiteKeyCache` | 내부용 |
| `i18nNow` | `setkey`, `setbundle`, `setlocal`, `getLocale()`, `getAutomationString`, `getAutoTestString` | 내부용 |
| `i18nNow` | `getKey` | OOTB 미사용 |
| `MessageUtil` | `replaceSymbolicTokens`, `lookupSymbolicToken` | 내부용 |
| `MessageUtil` | `searchDiscussionsOnKeywords`, `getTempQueryResult` | 앱 전용 |
| `UINavigatorUtil` | `getI18NstringForSuite(ServletContext, ...)`, `getSystemProperty(ServletContext, String)`, `getStringResourceFileId(ServletContext, String)` | 화면용 |
| `UINavigatorUtil` | `getStringResourceFileId(String)` | 같은 일 다른 메서드 |
| `UINavigatorUtil` | `getTypeIconProperty`, `getRegisteredDirectory`, `getDirectoryProperty` | 화면용 |
| `UINavigatorUtil` | `notificationLanguages` | OOTB 미사용 |
| `UINavigatorUtil` | `getI18nKey` | 내부용 |
| `UIUtil` | `getLanguageForSpellChecker` | 화면용 |
| `PersonUtil` | `getLanguageDefault`, `getLanguageChoices`, `setLanguage` | 환경설정 |
| `PersonUtil` | `getDefault(ServletContext, String)` | 화면용 |
| `PropertyUtil` | `getProperty(String)` | OOTB 미사용 |
| `Context` | `setLocale`, `setLanguage` | 내부용 |
| `EngineeringUtil` | `i18nStringNow` | 앱 전용 |
| `ProgramCentralUtil` | `getLocale` | 앱 전용 |
:::
