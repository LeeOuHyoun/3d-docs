# HTML, URL, JSON <Badge type="info" text="원본 분석" />

JPO 가 HTML, URL, XML 글자를 만들어 돌려줄 때 값을 넣는 자리에 맞게 바꾸는 (이스케이프) 방법과, JSON 문자열을 만들고 읽는 방법입니다. 예제의 `part` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다. util 전체를 하고 싶은 일로 찾으려면 [유틸리티](/jpo/api/utilities) 를 봅니다.

::: info 근거
xerinfra.jar (`XSSUtil`, `FrameworkUtil`, `UIUtil`, `UINavigatorUtil`, `BPSJsonObjectBuilder`), framework.jar (`UIRTEUtil`), esapi.jar 2.5.3.1 (`org.owasp.esapi.reference.DefaultEncoder`, `DefaultSecurityConfiguration`), eMatrixServletRMI.jar (`com.matrixone.json.JSONObject`) class 파일 (javap) 기준. `XSSUtil`, `UINavigatorUtil` 은 jakarta.servlet 이 없어 로컬에서 실행하지 않았습니다. 인코딩 결과는 `XSSUtil` 이 그대로 넘기는 ESAPI `DefaultEncoder` 를 OOTB `properties/ESAPI.properties` 로 로컬 실행해 확인했고, `FrameworkUtil` 의 인코딩 메서드는 직접 로컬 실행했습니다. 건수는 OOTB JPO 에서 센 호출 수: `XSSUtil.encodeForHTML(` 592, `encodeForJavaScript(` 577, `encodeForHTMLAttribute(` 337, `encodeForURL(` 290, `encodeForXML(` 235, `decodeFromURL(` 60, `FrameworkUtil.encodeURL(` 32, `Json.createObjectBuilder()` 128, `Json.createArrayBuilder()` 83, `BPSJsonObjectBuilder.createJsonObjectBuilder(` 12, `FrameworkUtil.toJsonObject(` 8
:::

샘플: [XX_UtilHtml_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilHtml_mxJPO.java) ([메서드별 설명](#샘플-jpo))

배경 설명은 가이드의 [HTML 이스케이프](/jpo/utils#html-이스케이프) 에 있습니다.

## 한눈에 보기

| 하고 싶은 일 | 쓸 메서드 |
| --- | --- |
| HTML 태그 사이에 값 넣기 | [`XSSUtil.encodeForHTML(context, s)`](#html-에-값-넣기) |
| HTML 속성 값 (`title="..."`) 자리에 값 넣기 | [`XSSUtil.encodeForHTMLAttribute(context, s)`](#html-에-값-넣기) |
| 스크립트 문자열 (`onclick`, `<script>`) 안에 값 넣기 | [`XSSUtil.encodeForJavaScript(context, s)`](#html-에-값-넣기) |
| URL 인자 값 넣기 | [`XSSUtil.encodeForURL(context, s)`](#url-인자-값-넣기) |
| URL 인자 값 되돌리기 | [`XSSUtil.decodeFromURL(s)`](#url-인자-값-되돌리기) |
| XML 에 값 넣기 | [`XSSUtil.encodeForXML(context, s)`](#xml-에-값-넣기) |
| JPO 가 돌려줄 JSON 문자열 만들기 | [`BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder())` → `build().toString()`](#json-문자열-만들기) |
| JSON 문자열 읽기 | [`FrameworkUtil.toJsonObject(s)`](#json-문자열-읽기) |

## HTML 에 값 넣기

`programHTMLOutput` 열이나 필드처럼 JPO 가 HTML 을 만들어 돌려줄 때 씁니다. 사용자가 넣은 값 (이름, 설명) 을 HTML 에 그대로 이어 붙이지 않고, 넣는 자리에 맞는 메서드로 감쌉니다.

| 넣는 자리 | 쓸 메서드 | 바꾸는 글자 |
| --- | --- | --- |
| 태그 사이의 글자 | `XSSUtil.encodeForHTML(context, s)` | 영문, 숫자, 공백, `,` `.` `-` `_` 밖의 모든 글자를 `&lt;`, `&#x2f;` 같은 문자 참조로. 한글도 `&#xac00;` 로 바뀜 (브라우저에는 그대로 보임) |
| 속성 값 (`title="..."`, `value="..."`) | `XSSUtil.encodeForHTMLAttribute(context, s)` | 위와 같고 공백도 `&#x20;` |
| 스크립트 문자열 (`onclick`, `<script>` 안의 `'...'`) | `XSSUtil.encodeForJavaScript(context, s)` | 영문, 숫자, `,` `.` `_` 밖의 모든 글자를 `\x3C`, `가` 같은 이스케이프로 |

| 호출 | 결과 |
| --- | --- |
| `encodeForHTML(context, "Tom & \"Jerry\" <b>")` | `Tom &amp; &quot;Jerry&quot; &lt;b&gt;` |
| `encodeForHTML(context, "O'Reilly")` | `O&#x27;Reilly` |
| `encodeForHTML(context, "가나 1/2")` | `&#xac00;&#xb098; 1&#x2f;2` |
| `encodeForHTML(context, "P-0001_A.1")` | `P-0001_A.1` |
| `encodeForHTML(context, "line1\nline2")` | `line1&#xa;line2` |
| `encodeForHTML(context, "a&amp;b")` (이미 바꾼 값) | `a&amp;amp&#x3b;b` |
| `encodeForHTMLAttribute(context, "Tom & \"Jerry\" <b>")` | `Tom&#x20;&amp;&#x20;&quot;Jerry&quot;&#x20;&lt;b&gt;` |
| `encodeForJavaScript(context, "O'Reilly")` | `O\x27Reilly` |
| `encodeForJavaScript(context, "</script><script>alert(1)")` | `\x3C\x2Fscript\x3E\x3Cscript\x3Ealert\x281\x29` |
| `encodeForJavaScript(context, "P-0001")` | `P\x2D0001` |
| `encodeForJavaScript(context, "가나")` | `가나` |
| 세 메서드 모두 `null`, `""`, `"null"`, `"  "` | 넘긴 값 그대로 (`null` 은 `null`) |

```java
String name = part.getInfo(context, DomainConstants.SELECT_NAME);
String html = "<span title=\"" + XSSUtil.encodeForHTMLAttribute(context, name) + "\">"
        + XSSUtil.encodeForHTML(context, name) + "</span>";
String link = "<a href=\"javascript:void(0)\" onclick=\"alert('" + XSSUtil.encodeForJavaScript(context, name) + "')\">"
        + XSSUtil.encodeForHTML(context, name) + "</a>";
```

- 감싸지 않은 값을 이어 붙이지 않습니다. 이름에 `<` 나 `"` 가 들면 화면이 깨지거나 스크립트가 실행됩니다.
- 한 값은 한 번만 감쌉니다. 두 번 감싸면 `&amp;amp;` 처럼 그대로 보입니다.
- 값이 `null` 이면 `null` 이 그대로 와서, 이어 붙이면 `null` 글자가 들어갑니다.
- ESAPI 설정 오류 등으로 예외가 나면 콘솔에 `Unable to encode the value, ...` 를 찍고 **바꾸지 않은 값** 을 돌려줍니다.
- 내보내기, 인쇄 중에는 HTML 이 아니라 글자만 돌려줍니다 ([표 열 function](/jpo/args#표-열-function)). 직접 만든 JSP 에서 예외 문구를 `alert` 로 보여 줄 때도 `encodeForJavaScript` 를 씁니다 ([예외](/jpo/i18n-exception#예외)).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `XSSUtil.encodeForHTML(context, s, false)` | 서식 있는 글 (RTE) 의 지원 태그 (`<b>` 등) 를 바꾼 뒤 다시 살림. 2 인자 형태가 `true` 로 이것을 부름. 일반 값에는 2 인자 형태 |
| `UINavigatorUtil.htmlEncode(s)` <Badge type="warning" text="폐기" /> | `&`, `<`, `>`, `"` 네 글자만 바꿈 (`'` 는 그대로) |

::: info 근거
xerinfra.jar `XSSUtil.encodeForHTML(Context, String)` → `encodeForHTML(context, s, true)`. 모든 `encodeFor*` 는 `UIUtil.isNullOrEmpty(s)` 면 `s` 를 그대로 돌려주고, 아니면 `ESAPI.encoder().encodeForHTML(s)` (`encodeForHTMLAttribute`, `encodeForJavaScript`, `encodeForXML`, `encodeForURL`, `encodeForCSS` 도 같은 이름의 ESAPI 메서드) 를 부름. `Exception` 이 나면 `System.out` 에 `Unable to encode the value, either the configuration for ESAPI is wrong or missing...` 를 찍고 `s` 를 돌려줌. `false` 면 framework.jar `UIRTEUtil.containsRTETags` 가 참일 때 `decodeRTESupportedTags` 로 지원 태그를 되돌림. esapi.jar `DefaultEncoder.encodeForHTML` 은 `HTMLEntityCodec` 과 `IMMUNE_HTML`, `encodeForHTMLAttribute` 는 `IMMUNE_HTMLATTR`, `encodeForJavaScript` 는 `JavaScriptCodec` 과 `IMMUNE_JAVASCRIPT`. 표의 결과는 ESAPI 2.5.3.1 `DefaultEncoder` 를 OOTB `properties/ESAPI.properties` (`ESAPI.Encoder=org.owasp.esapi.reference.DefaultEncoder`) 로 로컬 실행해 확인. `UINavigatorUtil.htmlEncode(String)` 은 `replaceAll` 로 네 글자만 바꾸고 `Deprecated: true`. OOTB 예: emxExtendedHeaderBase_mxJPO.java (`onclick` 안의 `encodeForJavaScript`)
:::

## URL 인자 값 넣기

링크 (`href`) 를 만들 때 `?` 뒤의 **인자 값 하나하나** 를 감쌉니다. URL 전체를 넘기면 `?`, `=`, `&`, `/` 까지 바뀌어 주소가 깨집니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `XSSUtil.encodeForURL(context, s)` | `s`: 인자 값 하나 | `String`. `URLEncoder.encode(s, "UTF-8")` 과 같은 결과 (공백은 `+`, 한글은 UTF-8 바이트의 `%XX`). `null`, `""`, `"null"`, 공백뿐이면 그대로 |

| 호출 | 결과 |
| --- | --- |
| `encodeForURL(context, "a=b&c d")` | `a%3Db%26c+d` |
| `encodeForURL(context, "가나 1/2")` | `%EA%B0%80%EB%82%98+1%2F2` |
| `encodeForURL(context, "100% (x+y)")` | `100%25+%28x%2By%29` |
| `encodeForURL(context, "P-0001_A.1")` | `P-0001_A.1` |

```java
String name = part.getInfo(context, DomainConstants.SELECT_NAME);
String url = "../common/emxTree.jsp?objectId=" + XSSUtil.encodeForURL(context, objectId)
        + "&name=" + XSSUtil.encodeForURL(context, name);
String html = "<a href=\"" + url + "\">" + XSSUtil.encodeForHTML(context, name) + "</a>";
```

- `java.net.URLEncoder.encode(s, "UTF-8")` 와 결과가 같습니다. 다른 점은 `XSSUtil` 은 `null`, `""` 를 그대로 돌려주고 (`URLEncoder` 는 `null` 이면 `NullPointerException`), 예외를 던지지 않는다는 것입니다.
- 문자 인코딩은 ESAPI 설정 `Encryptor.CharacterEncoding` 을 따릅니다. OOTB 값은 `UTF-8` 입니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `XSSUtil.encodeForURL(s)` | Context 형태가 안에서 이것을 그대로 부름. 결과 같음 |
| `FrameworkUtil.encodeURL(s)` | 안에서 `XSSUtil.encodeForURL(s)` 를 그대로 부름 |
| `FrameworkUtil.encodeURL(s, "UTF-8")` | 자체 구현. `/ ? & = : ; @ $ , + * . - _` 를 그대로 둬서 값 안의 `&`, `=`, `+` 가 인자를 깨뜨림 (`"a=b&c d"` → `a=b&c%20d`). 빈 문자열이면 `null`. 문자셋 이름이 틀리면 바꿀 글자마다 `null` 이 들어감 (`"a b"` → `anullb`) |
| `FrameworkUtil.encodeNonAlphaNumeric(s, "UTF-8")` | 영문, 숫자 밖은 모두 소문자 `%xx` (`.`, `-`, `_` 도). 빈 문자열이면 `null` |
| `FrameworkUtil.encodeURLParamValues(url)`, `UINavigatorUtil.encodeURL(url)`, `UINavigatorUtil.encodeURL(context, url)` | URL 전체를 받아 `?` 뒤를 `&`, `=` 로 잘라 값 (`FrameworkUtil` 은 이름도) 을 `XSSUtil.encodeForURL` 로 바꿈. 값 안에 이미 `&`, `=` 가 있으면 잘못 자름 |
| `XSSUtil.encodeURLForServer(context, url)`, `XSSUtil.encodeURLwithParsing(context, query)` | 위와 같이 잘라 이름과 값을 바꿈. `?` 도 `&` 도 없으면 전체를 `encodeForURL` 로 바꿈 |

::: info 근거
xerinfra.jar `XSSUtil.encodeForURL(Context, String)` → `encodeForURL(String)` → `ESAPI.encoder().encodeForURL(s)`. esapi.jar `DefaultEncoder.encodeForURL` 은 `URLEncoder.encode(input, ESAPI.securityConfiguration().getCharacterEncoding())`, `DefaultSecurityConfiguration.getCharacterEncoding()` 은 `Encryptor.CharacterEncoding` (기본 `UTF-8`). properties/ESAPI.properties:292 (`Encryptor.CharacterEncoding=UTF-8`). `FrameworkUtil.encodeURL(String)` → `XSSUtil.encodeForURL(String)`. `FrameworkUtil.encodeURL(String, String)` 은 `a-z A-Z 0-9 / ? & = : . ; @ $ , + - _ *` 를 그대로 두고 나머지를 `encodeChar` (그 문자셋의 바이트마다 `%` + 소문자 hex, 문자셋을 못 찾으면 `null`) 로 바꿈. 결과가 비면 `null`. `encodeNonAlphaNumeric` 은 영문, 숫자만 그대로. `encodeURLParamValues`, `UINavigatorUtil.encodeURL` 은 `StringTokenizer(…, "&")` 와 `indexOf("=")` 로 자름. 표의 결과는 ESAPI `DefaultEncoder`, `URLEncoder`, `FrameworkUtil.encodeURL(String, String)`, `encodeNonAlphaNumeric` 을 로컬 실행해 확인. OOTB 예: emxCommonDocumentUIBase_mxJPO.java (`href` 의 `objectId=` 값). OOTB JPO: `XSSUtil.encodeForURL(context` 212 건 / 290 건
:::

## URL 인자 값 되돌리기

URL 인코딩된 값을 받아 원래 글자로 되돌릴 때 씁니다. OOTB 는 URL 로 넘어온 검색 필터를 되돌린 뒤 JSON 으로 읽습니다 (emxAEFFullSearchBase_mxJPO.java).

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `XSSUtil.decodeFromURL(s)` | `s`: URL 인코딩된 값 | `String`. `URLDecoder.decode(s, "UTF-8")` 의 결과 (`+` 는 공백). `null`, `""`, `"null"`, 공백뿐이면 그대로. 되돌리다 예외가 나면 콘솔에 찍고 넘긴 값 그대로 |

| 호출 | 결과 |
| --- | --- |
| `decodeFromURL("a+b%20c")` | `a b c` |
| `decodeFromURL("%EA%B0%80%EB%82%98")` | `가나` |
| `decodeFromURL("100%")` | `100%`. `URLDecoder` 는 `IllegalArgumentException` 이지만 잡아서 그대로 돌려줌 |

```java
String filter = XSSUtil.decodeFromURL("%EA%B0%80%EB%82%98+1%2F2");
```

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `FrameworkUtil.decodeURL(s, "UTF-8")` | 자체 구현. `%xx`, `+` 밖의 글자를 1 바이트로 잘라 넣어 한글이 그대로 든 값은 깨짐 (`"가+나"` → 깨진 글자). 빈 문자열이면 `null` |
| `java.net.URLDecoder.decode(s, "UTF-8")` | 결과는 같음. 잘못된 `%` 에서 `IllegalArgumentException`, `null` 을 그대로 받지 못함 |

::: info 근거
xerinfra.jar `XSSUtil.decodeFromURL(String)`: `UIUtil.isNullOrEmpty(s)` 면 그대로, 아니면 `URLDecoder.decode(s, ESAPI.securityConfiguration().getCharacterEncoding())`, `Exception` 이면 콘솔에 찍고 `s`. `FrameworkUtil.decodeURL(String, String)`: `%` 다음 두 글자가 16 진수면 1 바이트, `+` 는 공백, 그 밖의 글자는 `(byte)` 로 바꿔 모은 뒤 `new String(bytes, 0, n, 문자셋)`. 모은 바이트가 없으면 `null`, 예외는 `DebugUtil.debug` 후 `null`. 표의 결과는 `URLDecoder.decode` 와 `FrameworkUtil.decodeURL` 을 로컬 실행해 확인 (`decodeURL("100%")` 은 `100%`, `URLDecoder` 는 `IllegalArgumentException`)
:::

## XML 에 값 넣기

JPO 가 XML 글자를 만들어 돌려줄 때 요소 내용과 속성 값을 감쌉니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `XSSUtil.encodeForXML(context, s)` | `s`: 넣을 값 | `String`. 영문, 숫자, 공백, `.` `-` `_` 밖의 글자를 `&#x26;` 같은 16 진 문자 참조로. `null`, `""`, `"null"`, 공백뿐이면 그대로 |

| 호출 | 결과 |
| --- | --- |
| `encodeForXML(context, "Tom & \"Jerry\" <b>")` | `Tom &#x26; &#x22;Jerry&#x22; &#x3c;b&#x3e;` |
| `encodeForXML(context, "가나 1/2")` | `&#xac00;&#xb098; 1&#x2f;2` |

```java
String name = part.getInfo(context, DomainConstants.SELECT_NAME);
String xml = "<part id=\"" + XSSUtil.encodeForXML(context, objectId) + "\"><name>"
        + XSSUtil.encodeForXML(context, name) + "</name></part>";
```

::: info 근거
xerinfra.jar `XSSUtil.encodeForXML(Context, String)` → `ESAPI.encoder().encodeForXML(s)` (null / 빈 값 / 예외 처리는 [HTML 에 값 넣기](#html-에-값-넣기) 와 같음). esapi.jar `DefaultEncoder.encodeForXML` 은 `XMLEntityCodec` 과 `IMMUNE_XML`. 표는 로컬 실행으로 확인. OOTB 예: ENOPeriodicReviewUIBase_mxJPO.java
:::

## JSON 문자열 만들기

JPO 가 JSON 문자열을 돌려줘야 할 때 (위젯, AJAX 호출 등) 씁니다. 표준 JSON API (`jakarta.json`) 의 builder 를 `BPSJsonObjectBuilder` 로 감싸 값을 넣고, `build().toString()` 으로 문자열을 만듭니다. OOTB `emxTypeAheadFullSearchBase_mxJPO.java` 의 `getQueryField` 도 이렇게 돌려줍니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder())` | 감쌀 builder. `null` 이면 `IllegalArgumentException` (`builder is NULL`) | `JsonObjectBuilder` |
| `json.add(이름, 값)` | 값: `String`, `int`, `long`, `double`, `boolean`, `BigInteger`, `BigDecimal`, `JsonValue`, `JsonObjectBuilder`, `JsonArrayBuilder` | 같은 builder. 객체 형 값이 `null` 이면 `addNull(이름)` 으로 넣음 (JSON 의 `null`) |
| `json.build().toString()` | 없음 | `String`. JSON 글자 |

```java
JsonObjectBuilder json = BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder());
json.add("id", objectId);
json.add("name", part.getInfo(context, DomainConstants.SELECT_NAME));
json.add("status", part.getAttributeValue(context, attrStatus));
String text = json.build().toString();
```

목록은 `Json.createArrayBuilder()` 에 객체 builder 를 하나씩 `add` 합니다.

```java
MapList children = part.getRelatedObjects(context, relStructure, "*",
        StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME),
        StringList.create(DomainConstants.SELECT_RELATIONSHIP_ID), false, true, (short) 1, null, null, 0);
JsonArrayBuilder rows = Json.createArrayBuilder();
for (Object o : children) {
    Map row = (Map) o;
    JsonObjectBuilder item = BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder());
    item.add("id", (String) row.get(DomainConstants.SELECT_ID));
    item.add("name", (String) row.get(DomainConstants.SELECT_NAME));
    rows.add(item);
}
JsonObjectBuilder result = BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder());
result.add("children", rows);
String text = result.build().toString();
```

- JSON 글자 안의 따옴표, 줄바꿈 같은 이스케이프는 JSON API 가 합니다. 값을 미리 `encodeForJavaScript` 로 감싸지 않습니다.
- `int`, `long`, `double`, `boolean` 은 감싸개를 거치지 않고 그대로 넘깁니다.
- 감싸지 않은 `Json.createObjectBuilder()` 에 `null` 값을 넣을 때의 동작과 `toString()` 의 정확한 모양 (공백, 키 순서) 은 서버의 JSON 구현을 따르며 로컬에서 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.
- `import jakarta.json.Json;`, `jakarta.json.JsonObjectBuilder`, `jakarta.json.JsonArrayBuilder` 가 필요합니다. OOTB JPO 도 같은 패키지를 씁니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `Json.createObjectBuilder()` 만 쓰기 | 값이 `null` 일 때를 따로 처리하지 않음. 감싸면 `addNull` 로 바뀜 |
| `FrameworkUtil.getJsonObjectFromMap(map)` | 값이 `String`, `Double`, `Boolean`, `null` 일 때만. `Integer`, `Long`, `StringList` 같은 값이면 `RuntimeException` (`Unsupported type ... in Json conversion`). OOTB 호출 0 |
| `UIUtil.getJSONString(jsonObject)` | 손으로 이어 붙임. 값마다 뒤에 `,` 를 붙이고 `}` 로 닫아 끝에 쉼표가 남고 (`{"a":"x",}`), `toolbarCode` 키는 뺌. OOTB 호출 0 |
| `UIUtil.getUnescapedJsonString(jsonObject)` | `toString()` 결과의 `\\x`, `\\u` 를 `\x`, `\u` 로 되돌림 (`encodeForJavaScript` 한 값을 화면 스크립트에 넘길 때). OOTB 호출 0 |
| `UINavigatorUtil.toJSONString(s)` | 문자열 하나를 따옴표로 감싼 JSON 값으로 바꿈 (`"`, `\`, 제어 문자, `</` 의 `/`). 손으로 JSON 을 이을 때만 필요. `null`, `""` 는 `""`. OOTB 호출 0 |
| `com.matrixone.json.JSONObject`, `JSONArray` (eMatrixServletRMI.jar) | 클래스 전체 폐기 (`@Deprecated(forRemoval=true)`) |

::: info 근거
xerinfra.jar `BPSJsonObjectBuilder.createJsonObjectBuilder(JsonObjectBuilder)`: `null` 이면 `IllegalArgumentException("builder is NULL")`, 아니면 감싼 객체를 돌려줌. `add(String, String)`, `add(String, JsonValue)`, `add(String, BigInteger)`, `add(String, BigDecimal)`, `add(String, JsonObjectBuilder)`, `add(String, JsonArrayBuilder)` 는 값이 `null` 이면 `builder.addNull(name)`, 아니면 `builder.add(name, value)`. `int`, `long`, `double`, `boolean` 형태와 `addNull`, `build` 는 그대로 넘김. `FrameworkUtil.getJsonObjectFromMap(Map)`: `Json.createObjectBuilder()` 에 `null` → `addNull`, `String`, `Double`, `Boolean` → `add`, 그 밖은 `new RuntimeException("Unsupported type " + 클래스이름 + " in Json conversion")`. `UIUtil.getJSONString(JsonObject)`: `StringBuilder("{")` 에 키마다 `"키":값,` 을 붙이고 `}` 를 붙임 (`toolbarCode` 건너뜀). `UIUtil.getUnescapedJsonString(JsonObject)`: `toString().replaceAll("\\\\x", "\\x").replaceAll("\\\\u", "\\u")` (class 파일의 상수 바이트로 확인). `UINavigatorUtil.toJSONString(String)`: `"` 로 감싸고 `"` `\` 앞에 `\`, `<` 다음의 `/` 앞에 `\`, `\b \t \n \f \r`, 0x20 미만, 0x80~0x9F, U+2000~U+20FF 는 `\uXXXX`. eMatrixServletRMI.jar `com.matrixone.json.JSONObject`, `JSONArray` 의 class 속성 `Deprecated: true`, `@Deprecated(forRemoval=true)`. jakarta.json jar 가 로컬에 없어 위 메서드는 실행하지 않음. OOTB 예: emxTypeAheadFullSearchBase_mxJPO.java (`getQueryField`: `BPSJsonObjectBuilder` → `add` → `build().toString()`), emxAEFFullSearchBase_mxJPO.java (`build().toString()` 반환)
:::

## JSON 문자열 읽기

다른 JPO 나 화면이 넘긴 JSON 문자열에서 값을 꺼낼 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.toJsonObject(s)` | `s`: `{...}` 모양의 JSON 문자열 | `jakarta.json.JsonObject`. `json.getString("키")`, `json.getInt("키")`, `json.getJsonArray("키")` 로 꺼냄 |

```java
JsonObject json = FrameworkUtil.toJsonObject("{\"name\":\"P-0001\",\"qty\":2}");
String name = json.getString("name");
int qty = json.getInt("qty");
```

- `[...]` 모양 (배열) 은 `FrameworkUtil.toJsonArray(s)` 로 읽습니다. 같은 방식으로 `readArray()` 를 부릅니다.
- 형식이 틀린 문자열, 없는 키를 `getString` 할 때의 예외는 서버의 JSON 구현을 따르며 로컬에서 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `Json.createReader(new StringReader(s)).readObject()` | `toJsonObject` 가 안에서 그대로 부르고 `close()` 까지 함 |
| `UINavigatorUtil.readJsonObject(json, key)` | `json.getString(key)` 를 감싸 예외면 stack trace 를 찍고 `""`. OOTB 호출 0 |
| `new com.matrixone.json.JSONObject(s)` (eMatrixServletRMI.jar) | 클래스 전체 폐기 (`forRemoval=true`) |

::: info 근거
xerinfra.jar `FrameworkUtil.toJsonObject(String)`: `Json.createReader(new StringReader(s))` → `readObject()` → `close()`. `toJsonArray(String)` 은 `readArray()`. `UINavigatorUtil.readJsonObject(JsonObject, String)`: `getString(key)`, `Exception` 이면 `printStackTrace()` 후 `""`. OOTB 예: emxAEFFullSearchBase_mxJPO.java (`FrameworkUtil.toJsonObject(XSSUtil.decodeFromURL(...))`)
:::

## 샘플 JPO

[XX_UtilHtml_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilHtml_mxJPO.java) 의 메서드입니다. 모든 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map (`programMap`) 에서 값을 읽습니다.

| 메서드 | 받는 값 (`programMap` 의 키) | 돌려주는 값 | 보여 주는 것 |
| --- | --- | --- | --- |
| `getNameHtml` | `objectId` | `String` (`<span title="...">...</span>`) | 태그 사이와 속성 값 ([HTML 에 값 넣기](#html-에-값-넣기)) |
| `getAlertLink` | `objectId` | `String` (`onclick` 이 든 `<a>`) | 스크립트 문자열 ([HTML 에 값 넣기](#html-에-값-넣기)) |
| `getTreeLink` | `objectId` | `String` (`href` 가 든 `<a>`) | URL 인자 값 ([URL 인자 값 넣기](#url-인자-값-넣기)) |
| `decodeUrlValue` | `value` (URL 인코딩된 값) | `String` | URL 인자 값 되돌리기 ([URL 인자 값 되돌리기](#url-인자-값-되돌리기)) |
| `getPartXml` | `objectId` | `String` (`<part>` XML) | XML 에 값 넣기 ([XML 에 값 넣기](#xml-에-값-넣기)) |
| `getPartJson` | `objectId` | `String` (JSON 객체) | JSON 문자열 만들기 ([JSON 문자열 만들기](#json-문자열-만들기)) |
| `getChildrenJson` | `objectId` | `String` (`children` 배열이 든 JSON) | 목록을 JSON 배열로 ([JSON 문자열 만들기](#json-문자열-만들기)) |
| `readJsonValues` | `json` (`{"name":"...","status":"..."}`) | `Map` (`name`, `status`) | JSON 문자열 읽기 ([JSON 문자열 읽기](#json-문자열-읽기)) |

::: details XX_UtilHtml_mxJPO.java

<<< @/../JPOSample/util/XX_UtilHtml_mxJPO.java

:::

::: warning
샘플의 타입, 관계, 속성 이름은 예시입니다. 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다. `jakarta.json` 은 로컬에 jar 가 없어, OOTB class 파일이 부르는 `jakarta.json` 시그니처만으로 만든 대역 (stub) 으로 컴파일했습니다.
:::

## 다루지 않은 메서드

::: details 펼쳐 보기
| 클래스 | 메서드 | 이유 |
| --- | --- | --- |
| `XSSUtil` | `encodeForCSS` | OOTB 미사용 |
| `XSSUtil` | `canonicalize` (2), `strip_tags`, `getInstance` | 화면용 |
| `XSSUtil` | `getDomainName` | OOTB 미사용 |
| `FrameworkUtil` | `encodeHref` | 화면용 |
| `FrameworkUtil` | `encodeBase64` | OOTB 미사용 |
| `FrameworkUtil` | `getBuilderObjectClone` (2), `getBuilderArrayClone`, `remove` | 내부용 |
| `UINavigatorUtil` | `htmlDecode` | 앱 전용 |
| `com.matrixone.json` | `JSONTokenizer`, `JSONUtil` | 내부용 |
:::
