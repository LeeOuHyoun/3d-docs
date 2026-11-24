# 문자열, 목록 <Badge type="info" text="원본 분석" />

빈 값을 확인하고, 문자열을 자르고 잇고 바꾸고, `StringList` 와 조회 결과 `MapList` 를 만들고 정렬하는 메서드입니다. 이 페이지의 쓸 메서드는 모두 서버에 가지 않고 넘긴 값만 다룹니다. 예제의 `part` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다. util 전체를 하고 싶은 일로 찾으려면 [유틸리티](/jpo/api/utilities) 를 봅니다.

::: info 근거
xerinfra.jar (`UIUtil`, `StringUtil`, `FrameworkUtil`, `MapList`), eMatrixServletRMI.jar (`StringList`, `matrix.util.List`, `matrix.util.StringUtils`), m1jsystem.jar (`com.matrixone.jsystem.util.StringUtils`) class 파일 (javap) 기준. Context 가 필요 없는 메서드는 OOTB jar 만 클래스패스에 두고 로컬에서 실행해 "호출 → 결과" 를 적었습니다. `UIUtil`, 안에서 `UIUtil` 을 부르는 `stripString`, `MapList` 의 정렬은 로컬에서 클래스를 읽지 못해 바이트코드로만 확인했습니다. 건수는 OOTB JPO 에서 센 호출 수: `UIUtil.isNotNullAndNotEmpty(` 1,324, `UIUtil.isNullOrEmpty(` 945, `FrameworkUtil.split(` 1,077, `StringList.create(` 338, `FrameworkUtil.findAndReplace(` 240, `FrameworkUtil.join(` 110, `.addSortKey(` 89, `.sortStructure(` 19
:::

샘플: [XX_UtilString_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilString_mxJPO.java) ([메서드별 설명](#샘플-jpo))

## 한눈에 보기

| 하고 싶은 일 | 쓸 메서드 |
| --- | --- |
| 값이 비었는지 확인 (`null`, 공백, `"null"`) | [`UIUtil.isNullOrEmpty(s)`](#빈-값-확인) |
| 문자열을 구분자로 자르기 | [`FrameworkUtil.split(s, ",")`](#문자열-자르기) |
| 목록을 구분자로 이어 문자열 하나로 | [`FrameworkUtil.join(list, ", ")`](#문자열-잇기) |
| 찾은 글자를 모두 바꾸기 | [`FrameworkUtil.findAndReplace(s, find, rep)`](#글자-바꾸기) |
| 긴 글을 n 글자로 줄이고 `...` 붙이기 | [`FrameworkUtil.stripString(s, n, type)`](#글자-수로-줄이기) |
| UTF-8 바이트 수에 맞게 자르기 | [`FrameworkUtil.getUtf8TruncatedString(s, maxBytes)`](#utf-8-바이트-수로-자르기) |
| select 목록처럼 값 여러 개로 목록 만들기 | [`StringList.create(a, b, ...)`](#목록-만들기) |
| 목록을 `String[]` 로 바꾸기 | [`list.toStringArray()`](#목록을-배열로-바꾸기) |
| 목록에서 `null`, `""` 빼기 | [`list.filterEmpty()`](#빈-값-빼기) |
| 순서를 지키며 중복 빼기 | [`unique.addAllExclusive(list)`](#중복-빼기) |
| 목록 정렬하기 | [`list.sort()`](#정렬하기) |
| 조회 결과를 한 키로 정렬 | [`mapList.sort(key, dir, type)`](#한-키로-정렬) |
| 조회 결과를 여러 키로 정렬 | [`mapList.addSortKey(...)` + `mapList.sort()`](#여러-키로-정렬) |
| 여러 단계로 펼친 결과를 부모, 자식 순서를 지키며 정렬 | [`mapList.sortStructure(key, dir, type)`](#구조-목록-정렬) |

## 빈 값 확인

넘어온 인자나 읽은 값이 비었는지 확인할 때 씁니다. `null`, 공백뿐인 문자열, `"null"` 문자열을 한 번에 거릅니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `UIUtil.isNullOrEmpty(s)` | `s`: 확인할 문자열 | `boolean`. `null`, 공백뿐인 문자열, `"null"` (대소문자 무시) 이면 `true` |
| `UIUtil.isNotNullAndNotEmpty(s)` | 위와 같음 | 위의 반대. 안에서 `!isNullOrEmpty(s)` 를 돌려줌 |

| 호출 | 결과 |
| --- | --- |
| `UIUtil.isNullOrEmpty(null)`, `UIUtil.isNullOrEmpty("")`, `UIUtil.isNullOrEmpty("   ")` | `true` |
| `UIUtil.isNullOrEmpty("null")`, `UIUtil.isNullOrEmpty("NULL")` | `true` |
| `UIUtil.isNullOrEmpty(" null ")` | `false`. `"null"` 과 비교할 때는 앞뒤 공백을 지우지 않음 |
| `UIUtil.isNullOrEmpty("0")`, `UIUtil.isNullOrEmpty("false")` | `false` |

```java
String description = part.getInfo(context, DomainConstants.SELECT_DESCRIPTION);
if (UIUtil.isNullOrEmpty(description)) {
    description = "-";
}
```

- 요청 값에는 `"null"` 문자열이 들어오는 경우가 있습니다 ([공통](/jpo/args#공통)). 이 메서드가 그것까지 거르므로 `"null".equals` 를 따로 확인하지 않습니다.
- 서버에 가지 않고 넘긴 문자열만 봅니다.
- 가이드의 설명은 [빈 값과 문자열](/jpo/utils#빈-값과-문자열) 에 있습니다.
- `UIUtil` 은 화면용 클래스지만 이 두 메서드는 JPO 에서 그대로 씁니다. 화면용 메서드는 [다루지 않은 메서드](#다루지-않은-메서드) 에 있습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `ChangeUXUtil.isNullOrEmpty(s)` (enterprisechangemgtapp.jar), `ChangeUtil.isNullOrEmpty(s)` (enterprisechangemgt.jar) | 앱 jar 에 있는 같은 이름의 메서드 (앱 전용). `"null"` 만 빈 값으로 보고 `"NULL"` 은 빈 값으로 보지 않음 |
| `BOMMgtUtil`, `PartMgtUtil`, `FormulationUtil`, `BusinessUtil` 같은 앱 클래스의 `isNullOrEmpty`, `isNotNullAndNotEmpty` | 앱마다 따로 둔 같은 이름의 메서드 (앱 전용) |

::: info 근거
xerinfra.jar `UIUtil.isNullOrEmpty(String)`: `null` 이거나 `trim().length() == 0` 이거나 `"null".equalsIgnoreCase(s)` 이면 참 (마지막 비교는 `trim` 하기 전 값으로 함). `isNotNullAndNotEmpty` 는 `!isNullOrEmpty`. `UIUtil` 은 `jakarta.servlet`, `jakarta.json` 을 쓰는 메서드와 한 클래스라 로컬에서 읽지 못해 결과는 바이트코드로 확인. enterprisechangemgtapp.jar `ChangeUXUtil.isNullOrEmpty`, enterprisechangemgt.jar `ChangeUtil.isNullOrEmpty`: `null` 이거나 `"null".equals(s)` 이거나 `trim` 한 길이가 0 이면 참. OOTB 호출: `ChangeUXUtil.isNullOrEmpty(` 132, `ChangeUtil.isNullOrEmpty(` 54, `BOMMgtUtil.isNotNullAndNotEmpty(` 26, `PartMgtUtil.isNotNullAndNotEmpty(` 19
:::

## 문자열 자르기

MQL `dump` 결과나 쉼표로 이어 넘어온 id 처럼, 구분자로 이어진 문자열을 목록으로 자를 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.split(s, delim)` | `s`: 자를 문자열. `delim`: 구분자 한 글자 (`","`, `"\|"`). 정규식이 아님 | `StringList`. `s` 가 `null` 이거나 `""` 이면 빈 목록. `delim` 이 `null` 이면 `"~"` 로 자름 |

| 호출 | 결과 |
| --- | --- |
| `FrameworkUtil.split("a,b,c", ",")` | `["a", "b", "c"]` |
| `FrameworkUtil.split("a,,c", ",")` | `["a", "", "c"]` |
| `FrameworkUtil.split("a,b,", ",")` | `["a", "b", ""]` |
| `FrameworkUtil.split(",a", ",")` | `["a"]`. 맨 앞 구분자 하나는 빈 값을 만들지 않음 |
| `FrameworkUtil.split(",,a", ",")` | `["", "a"]` |
| `FrameworkUtil.split("", ",")`, `FrameworkUtil.split(null, ",")` | `[]` |
| `FrameworkUtil.split(" a , b ", ",")` | `[" a ", " b "]`. 앞뒤 공백은 그대로 |
| `FrameworkUtil.split("a.b.c", ".")` | `["a", "b", "c"]` |
| `FrameworkUtil.split("a~b", null)` | `["a", "b"]` |
| `FrameworkUtil.split("a\|\|b", "\|\|")` | `["a", "\|", "\|", "b"]` |
| `FrameworkUtil.split("a,b;c", ",;")` | `["a", ",", "b", ";", "c"]` |

```java
StringList idList = FrameworkUtil.split("11111.1.1.1,11111.1.1.2", ",");
String out = MqlUtil.mqlCommand(context, "print bus $1 select $2 $3 dump $4", objectId, "name", "current", "|");
StringList nameAndCurrent = FrameworkUtil.split(out, "|");
```

::: warning split 의 구분자는 한 글자만 씁니다
`FrameworkUtil.split` 은 구분자 문자열의 글자 하나하나를 구분자로 봅니다. `"||"` 처럼 두 글자 이상을 주면 구분자 글자가 결과에 섞여 나옵니다. OOTB 도 `"|"` (364 건), `","` (329 건) 같은 한 글자만 씁니다.
:::

- 첫 값이 빈 문자열이면 그 칸이 사라져 뒤 칸이 하나씩 당겨집니다. `FrameworkUtil.split("|A|Release", "|")` 는 `["A", "Release"]` 입니다. MQL `dump` 결과를 칸 위치로 읽을 때는 비지 않는 select (`name`, `id`) 를 맨 앞에 둡니다. 맨 앞이 빌 수 있는 표의 행 값은 [인자, JPO 호출, id](/jpo/api/util/args-id) 에 칸 수로 읽는 방법이 있습니다.
- 빈 칸을 빼려면 `.filterEmpty()` 를 붙입니다 ([빈 값 빼기](#빈-값-빼기)). `String[]` 가 필요하면 `.toStringArray()` 를 붙입니다.
- 여러 객체 `getInfo`, `findObjects` 가 여러 값을 `\u0007` 로 이어 주면 `FrameworkUtil.split(값, "\u0007")` 로 자릅니다 ([값이 여러 개인 select](/jpo/api/read#값이-여러-개인-select)).
- `FrameworkUtil.split(s, fieldSep, recordSep)` 는 `recordSep` 로 먼저 자르고 조각마다 `fieldSep` 로 다시 잘라 `List<StringList>` 를 돌려줍니다. `split("a~b|c~d", "~", "|")` 는 `[["a", "b"], ["c", "d"]]` 입니다 (OOTB 1 건).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `StringUtil.split(s, delim)` | `FrameworkUtil` 이 `StringUtil` 을 상속해서 같은 메서드 |
| `FrameworkUtil.splitString(s, delim)` (`StringUtil.splitString`) | 구분자 문자열 전체로 자르고 조각마다 앞뒤 공백을 지움. 맨 앞 구분자도 빈 값을 만듦 (`",a"` → `["", "a"]`). `delim` 이 `null` 이면 `NullPointerException`, `""` 이면 끝나지 않음 |
| `StringUtils.split(s, regex)` <Badge type="warning" text="폐기" /> (m1jsystem.jar `com.matrixone.jsystem.util`, eMatrixServletRMI.jar `matrix.util` 에 같은 클래스) | 클래스 전체가 폐기. 안에서 `String.split(regex)` 를 그대로 불러 구분자를 정규식으로 봄. `StringUtils.split("a.b.c", ".")` 는 빈 배열, `"\|"` 로 자르면 글자마다 잘림. 끝의 빈 값은 버림 |

::: info 근거
xerinfra.jar `StringUtil.split(String, String)` (`FrameworkUtil extends StringUtil`): 문자열이 `null` 이면 빈 `StringList`, 구분자가 `null` 이면 `"~"`. `new StringTokenizer(s, 구분자, true)` 로 토큰을 받아, 토큰이 구분자와 같고 바로 앞 토큰도 구분자면 `""` 를 넣고, 구분자가 아니면 그 토큰을 넣음. 마지막 토큰이 구분자면 `""` 를 하나 더 넣음. 토큰은 구분자 글자 하나씩이라 두 글자 이상의 구분자와는 같아지지 않음. `splitString` 은 `indexOf(구분자, 시작)` 으로 자르고 조각마다 `trim`, 구분자 길이를 `null` 확인보다 먼저 읽음. 두 `StringUtils` 는 클래스와 메서드 모두 `Deprecated` 이고 `String.split`, `replace`, `replaceAll`, `replaceFirst`, `matches` 를 그대로 부름. 호출 → 결과는 로컬 실행으로 확인 (`splitString("ab", "")` 는 `OutOfMemoryError` 가 날 때까지 돎). OOTB 호출: `FrameworkUtil.split(` 1,077, `StringUtil.split(` 80, `FrameworkUtil.splitString(` 61, 구분자 `"\7"` 17 건
:::

## 문자열 잇기

목록의 값을 구분자로 이어 문자열 하나로 만들 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.join(list, delim)` | `list`: `StringList` 또는 `String[]`. `delim`: 값 사이에 넣을 문자열 (두 글자 이상도 됨) | `String`. 빈 목록이면 `""`, 값이 하나면 그 값. `delim` 이 `null` 이면 `"~"` 로 이음 |

| 호출 | 결과 |
| --- | --- |
| `FrameworkUtil.join(StringList.create("a", "b", "c"), "/")` | `"a/b/c"` |
| `FrameworkUtil.join(StringList.create("a", "b"), ", ")` | `"a, b"` |
| `FrameworkUtil.join(StringList.create("a", "", "c"), ",")` | `"a,,c"` |
| `FrameworkUtil.join(StringList.create("a"), "/")` | `"a"` |
| `FrameworkUtil.join(new StringList(), "/")` | `""` |
| `FrameworkUtil.join(StringList.create("a", "b"), null)` | `"a~b"` |
| `FrameworkUtil.join(new String[] {"a", "b", "c"}, "\|")` | `"a\|b\|c"` |
| `FrameworkUtil.join(list, ",")` (`list` 는 `["a", "b", null, "d"]`) | `"a,b"`. `null` 앞까지만 이음 |
| `FrameworkUtil.join(new String[] {"a", null}, ",")` | `NullPointerException` |
| `FrameworkUtil.join((StringList) null, ",")` | `NullPointerException` |

```java
StringList docNames = part.getInfoList(context, "from[" + relPartDocument + "].to.name");
String names = FrameworkUtil.join(docNames, ", ");
String joined = FrameworkUtil.join(FrameworkUtil.split("11111.1.1.1,11111.1.1.2", ","), "|");
```

- `StringList` 에 `null` 이 섞이면 예외 없이 그 앞까지만 이어 돌려줍니다. 값이 빠져도 알아채기 어려우니 `null` 이 들어갈 수 있는 목록은 먼저 [빈 값 빼기](#빈-값-빼기) 를 합니다.
- Java 의 `String.join(", ", list)` 도 같은 결과를 냅니다. `null` 은 `"null"` 로 들어갑니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `StringUtil.join(list, delim)` | `FrameworkUtil` 이 `StringUtil` 을 상속해서 같은 메서드 |
| `list.join(delim)` (`matrix.util.List`) | 값에 `null` 이 있거나 `delim` 이 `null` 이면 `NullPointerException`. OOTB 는 `FrameworkUtil.join` 을 씀 |

::: info 근거
xerinfra.jar `StringUtil.join(StringList, String)`: 크기가 1 이면 첫 요소를 그대로 돌려주고, 구분자가 `null` 이면 `"~"`. `StringItr` 로 돌며 이음. eMatrixServletRMI.jar `ListItr.next()` 는 지금 요소가 `null` 이면 `false` 를 돌려줘 반복이 거기서 끝남. `join(String[], String)` 은 길이를 미리 더하려고 요소마다 `length()` 를 불러 `null` 에서 예외. `matrix.util.List.join(String)` 은 `stream().map(Object::toString)` 다음 `Collectors.joining(구분자)`. 호출 → 결과는 로컬 실행으로 확인. OOTB 호출: `FrameworkUtil.join(` 110, `StringUtil.join(` 11 (모두 인자 두 개 형태)
:::

## 글자 바꾸기

문자열 안에서 찾은 글자를 모두 다른 글자로 바꿀 때 씁니다. 찾는 글자는 정규식이 아닌 글자 그대로입니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.findAndReplace(s, find, rep)` | `s`: 원래 문자열. `find`: 찾을 글자 (비우지 않음). `rep`: 바꿀 글자 | `String`. 앞에서부터 겹치지 않게 찾은 곳을 모두 바꾼 문자열. 대소문자를 가림 |

| 호출 | 결과 |
| --- | --- |
| `FrameworkUtil.findAndReplace("a-b-c", "-", "/")` | `"a/b/c"` |
| `FrameworkUtil.findAndReplace("a.b", ".", "-")` | `"a-b"` |
| `FrameworkUtil.findAndReplace("xaby", "ab", "Z")` | `"xZy"` |
| `FrameworkUtil.findAndReplace("aaa", "aa", "b")` | `"ba"` |
| `FrameworkUtil.findAndReplace("line1\r\nline2", "\r\n", " ")` | `"line1 line2"` |
| `FrameworkUtil.findAndReplace("Part PART", "part", "X")` | `"Part PART"` |
| `FrameworkUtil.findAndReplace("a,b", ",", "")` | `"ab"` |
| `FrameworkUtil.findAndReplace("a,b", ",", null)` | `"anullb"` |
| `FrameworkUtil.findAndReplace(null, ",", "-")`, `FrameworkUtil.findAndReplace("a,b", null, "-")` | `NullPointerException` |
| `FrameworkUtil.findAndReplace("ab", "", "-")` | 끝나지 않음. 메모리가 찰 때까지 돎 |

```java
String description = part.getInfo(context, DomainConstants.SELECT_DESCRIPTION);
String oneLine = FrameworkUtil.findAndReplace(description, "\n", " ");
```

::: warning 찾을 글자를 비우지 않습니다
`find` 가 `""` 이면 같은 자리를 끝없이 찾으며 결과를 늘립니다. 로컬 실행에서는 `OutOfMemoryError` 로 끝났습니다. 찾을 글자를 변수로 받으면 `UIUtil.isNullOrEmpty` 로 먼저 거릅니다.
:::

- Java 의 `s.replace(find, rep)` 도 같은 결과입니다. `find` 가 `""` 여도 끝나고 (`"ab"` → `"-a-b-"`), `rep` 가 `null` 이면 예외입니다.
- 대소문자를 가리지 않고 바꾸려면 `FrameworkUtil.findAndReplaceWithIgnoreCase(s, find, rep)` 를 씁니다. `("Part PART part", "part", "X")` 는 `"X X X"` 입니다. 빈 `find` 는 같은 문제가 있습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `StringUtil.Replace(s, find, rep)` (`FrameworkUtil.Replace`) | 찾은 자리의 첫 글자만 바꾸고 나머지 글자를 남김. `Replace("xaby", "ab", "Z")` → `"xZby"`, `Replace("aaa", "aa", "b")` → `"bba"`. 한 글자를 찾을 때만 맞음. 빈 `find` 는 끝나지 않음 |
| `StringUtils.replace(s, find, rep)` <Badge type="warning" text="폐기" /> | 폐기. 안에서 `String.replace` 를 그대로 부름 |
| `StringUtils.replaceAll(s, regex, rep)`, `StringUtils.replaceFirst(s, regex, rep)` <Badge type="warning" text="폐기" /> | 폐기. 정규식으로 찾음. `replaceAll("a.b", ".", "-")` → `"---"` |

::: info 근거
xerinfra.jar `FrameworkUtil.findAndReplace(String, String, String)`: `StringBuffer` 에 `indexOf(find, 시작)` 앞부분과 `rep` 를 붙이고 시작을 `찾은 자리 + find.length()` 로 옮김. `find` 가 `""` 이면 시작이 그대로라 끝나지 않음. `rep` 는 `StringBuffer.append` 라 `null` 이 `"null"` 로 들어감. `findAndReplaceWithIgnoreCase` 는 `toLowerCase()` 한 문자열에서 같은 방식으로 찾음. `StringUtil.Replace` 는 찾은 자리마다 `rep` 를 넣고 다음 조각을 `찾은 자리 + 1` 부터 이음. 호출 → 결과는 로컬 실행으로 확인 (빈 `find` 는 `-Xmx64m` 에서 `OutOfMemoryError`). OOTB 호출: `FrameworkUtil.findAndReplace(` 240, `FrameworkUtil.Replace(` 5, `StringUtil.Replace(` 5, `findAndReplaceWithIgnoreCase(` 0
:::

## 길이에 맞게 줄이기

메일이나 알림에 넣을 긴 설명을 줄이거나, 바이트 수가 정해진 자리에 맞게 자를 때 씁니다.

### 글자 수로 줄이기

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.stripString(s, n, type)` | `n`: 남길 글자 수. `type`: `FrameworkUtil.StripStringType.WORD_STRIP` (`n` 글자 뒤 첫 공백에서 자름) 또는 `CHAR_STRIP` (`n` 글자에서 자름) | `String`. `n` 글자보다 길면 자르고 `...` 를 붙임. `n` 글자 이하면 그대로. 빈 값이면 `""` |

| 호출 | 결과 |
| --- | --- |
| `FrameworkUtil.stripString("Hello World Foo", 7, WORD_STRIP)` | `"Hello World..."` |
| `FrameworkUtil.stripString("Hello World Foo", 7, CHAR_STRIP)` | `"Hello W..."` |
| `FrameworkUtil.stripString("Hello World", 7, WORD_STRIP)` | `"Hello ..."`. 7 글자 뒤에 공백이 없으면 6 글자에서 자름 |
| `FrameworkUtil.stripString("Hello", 7, CHAR_STRIP)` | `"Hello"` |
| `FrameworkUtil.stripString(null, 7, CHAR_STRIP)`, `stripString("null", 2, CHAR_STRIP)`, `stripString("  ", 1, CHAR_STRIP)` | `""` |
| `FrameworkUtil.stripString("Hello World", 7, null)` | `""` |

```java
String description = part.getInfo(context, DomainConstants.SELECT_DESCRIPTION);
String shortText = FrameworkUtil.stripString(description, 100, FrameworkUtil.StripStringType.WORD_STRIP);
```

- `WORD_STRIP` 은 단어 중간에서 자르지 않는 대신 결과가 `n` 글자보다 길 수 있습니다. 길이를 꼭 지켜야 하면 `CHAR_STRIP` 을 씁니다.
- `type` 을 `null` 로 주면 값이 있어도 `""` 가 옵니다.
- OOTB 는 경로 (Route) 알림 메일에 넣는 내용 설명을 `WORD_STRIP` 으로 줄입니다 (`emxCommonInitiateRouteBase_mxJPO.java`, `emxCommonCompleteTaskBase_mxJPO.java`).

### UTF-8 바이트 수로 자르기

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.getUtf8TruncatedString(s, maxBytes)` | `maxBytes`: 넘지 않을 바이트 수 | `String`. UTF-8 로 `maxBytes` 바이트를 넘기 전까지 앞에서부터 남긴 글자. 글자 중간에서 자르지 않고 `...` 은 붙이지 않음 |

| 호출 | 결과 |
| --- | --- |
| `FrameworkUtil.getUtf8TruncatedString("abcdef", 4)` | `"abcd"` |
| `FrameworkUtil.getUtf8TruncatedString("가나다", 7)` | `"가나"`. 한글은 한 글자 3 바이트 |
| `FrameworkUtil.getUtf8TruncatedString("a가b", 3)` | `"a"` |
| `FrameworkUtil.getUtf8TruncatedString("", 3)` | `""` |
| `FrameworkUtil.getUtf8TruncatedString(null, 3)` | `NullPointerException` |

```java
String name = part.getInfo(context, DomainConstants.SELECT_NAME);
String subject = FrameworkUtil.getUtf8TruncatedString("[" + name + "] 검토 요청", 250);
```

- OOTB 는 메일 제목을 250 바이트로 자를 때 씁니다 (`emxMailUtilBase_mxJPO.java`).

::: info 근거
xerinfra.jar `FrameworkUtil.stripString(String, int, StripStringType)`: `UIUtil.isNotNullAndNotEmpty(s)` 가 거짓이면 `""`. 길이가 `n` 이하면 그대로. `CHAR_STRIP` 은 `substring(0, n)`, `WORD_STRIP` 은 `substring(n)` 에서 `indexOf(" ")` 를 찾아 `substring(0, n + 찾은 자리)` (못 찾으면 `-1` 이라 `n - 1` 글자). 자른 결과가 원래보다 짧으면 `...` 를 붙임 (문자열 연결 recipe `\u0001...`). 두 형이 아니면 `""`. 안에서 `UIUtil` 을 불러 로컬 실행이 막혀 결과는 바이트코드로 확인. `getUtf8TruncatedString(String, int)`: 글자마다 `0x7F` 이하 1, `0x7FF` 이하 2, 서로게이트 앞쪽 4, 뒤쪽 0, 그 밖 3 바이트로 세어 넘기 전까지 붙임. 결과는 로컬 실행으로 확인. OOTB 호출: `FrameworkUtil.stripString(` 5, `getUtf8TruncatedString(` 1
:::

## 목록 만들기

`getInfo`, `getRelatedObjects` 에 넘길 select 목록처럼 값 여러 개를 담은 `StringList` 를 만들 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `StringList.create(a, b, ...)` | 값 여러 개 또는 `String[]` | `StringList`. 넘긴 순서대로. 배열이 `null` 이면 빈 목록 |
| `StringList.create(collection)` | `List`, `Set` 같은 모음 | `StringList`. 모음을 도는 순서대로. 모음이 `null` 이면 `NullPointerException` |
| `new StringList()` | 없음 | 빈 `StringList`. `add(값)` 으로 채움 |

| 호출 | 결과 |
| --- | --- |
| `StringList.create("a", "b")` | `["a", "b"]` |
| `StringList.create()`, `StringList.create((String[]) null)` | `[]` |
| `StringList.create("a", null)` | `["a", null]`. `null` 도 넣음 |
| `StringList.create(new TreeSet(Arrays.asList("b", "a")))` | `["a", "b"]` |
| `new StringList("a")` | `["a"]` |
| `new StringList((String[]) null)` | `NullPointerException` |

```java
StringList selects = StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME,
        DomainObject.getAttributeSelect(attrStatus));
Map info = part.getInfo(context, selects);

StringList doneStates = new StringList();
doneStates.add("Release");
doneStates.add("Obsolete");
boolean done = doneStates.contains(part.getInfo(context, DomainConstants.SELECT_CURRENT));
```

- `StringList` 는 `java.util.List` 입니다 (`matrix.util.List` 를 상속). `for (String s : list)`, `get(i)`, `size()`, `contains(s)`, `remove(s)` 를 그대로 씁니다. `contains` 는 대소문자를 가립니다.
- `StringList.EMPTY` 는 바꿀 수 없는 빈 목록입니다. `add` 하면 `UnsupportedOperationException` 입니다.
- 내용이 같아도 `ArrayList` 와 `equals` 로 비교하면 `false` 입니다. `StringList` 끼리만 내용을 비교합니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `new StringList(s)`, `new StringList(String[])` | `create` 와 같은 목록. 배열이 `null` 이면 `NullPointerException`, `s` 가 `null` 이면 `[null]` |
| `StringList.asList(...)` | 안에서 `create` 를 그대로 부름 |
| `list.addElement(s)` <Badge type="warning" text="폐기" />, `list.elementAt(i)` <Badge type="warning" text="폐기" />, `list.removeElement(s)` <Badge type="warning" text="폐기" /> | `matrix.util.List` 에서 폐기. `add`, `get`, `remove` 와 같은 일 |

::: info 근거
eMatrixServletRMI.jar `StringList.create(String...)`: 배열이 `null` 이면 길이 0 배열로 `new StringList(String[])`. `create(List)` 는 `list.toArray(...)` 로 `create(String...)` 를 부름. `create(Collection)`, `create(Set)` 은 `stream` 으로 모음. `asList(String...)`, `asList(List)` 는 `create` 를 그대로 부름. `StringList(String)` 은 크기 1 로 만들어 `add`, `StringList(String[])` 은 배열 길이를 먼저 읽음. 상위 클래스 `matrix.util.List` 는 `java.util.List` 를 구현하고 `addElement`, `elementAt`, `removeElement`, `insertElementAt`, `firstElement`, `lastElement` 등 16 개 형태가 `Deprecated`. `List.equals` 는 상대가 `matrix.util.List` 일 때만 내용을 비교. `StringList.EMPTY` 는 바꾸는 메서드가 `UnsupportedOperationException` 을 던지는 `unmodifiableStringList`. 결과는 로컬 실행으로 확인. OOTB 호출: `StringList.create(` 338, `new StringList(` 8,265
:::

## 목록을 배열로 바꾸기

`DomainObject.getInfo(context, ids, selects)`, `DomainObject.deleteObjects`, `DomainRelationship.disconnect` 처럼 `String[]` 를 받는 메서드에 목록을 넘길 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `list.toStringArray()` | 없음 | `String[]`. 목록과 같은 순서. 빈 목록이면 길이 0 배열 |

```java
StringList docIdList = part.getInfoList(context, "from[" + relPartDocument + "].to.id");
MapList docs = DomainObject.getInfo(context, docIdList.toStringArray(), StringList.create(DomainConstants.SELECT_NAME));
```

- `list.toArray(new String[0])` 과 같습니다. `toStringArray` 가 안에서 그대로 부릅니다. OOTB 에는 `toArray(new String[...])` 형태가 더 많습니다 (104 건, `toStringArray()` 11 건).
- 거꾸로 배열을 목록으로 바꿀 때는 `StringList.create(array)` 를 씁니다 ([목록 만들기](#목록-만들기)).

::: info 근거
eMatrixServletRMI.jar `StringList.toStringArray()`: `toArray(new String[0])` 결과를 `String[]` 로 돌려줌. `matrix.util.List.toArray(Object[])` 는 안의 `java.util.List` 에 그대로 넘김. 결과는 로컬 실행으로 확인
:::

## 목록 정리하기

### 빈 값 빼기

자른 결과나 읽은 값에서 `null` 과 `""` 을 뺄 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `list.filterEmpty()` | 없음 | 새 `StringList`. `null`, `""` 을 뺀 나머지. 원래 목록은 그대로 |

| 호출 | 결과 |
| --- | --- |
| `StringList.create("a", null, "", " ", "b").filterEmpty()` | `["a", " ", "b"]`. 공백은 남음 |
| `FrameworkUtil.split("a,,b,", ",").filterEmpty()` | `["a", "b"]` |

```java
StringList values = FrameworkUtil.split("P-001,,P-002,", ",").filterEmpty();
```

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `StringList.createNotNull(a, b, ...)`, `StringList.createNotNull(list)` | `null` 만 빼고 `""` 은 남김. `createNotNull("a", null, "", "b")` → `["a", "", "b"]` |

### 중복 빼기

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `unique.addAllExclusive(list)` | `list`: 더할 목록 | `boolean`. `unique` 에 아직 없는 값만 `list` 의 순서대로 더함. `list` 가 `null` 이면 아무것도 하지 않음 |

| 호출 | 결과 (`unique`) |
| --- | --- |
| 빈 `unique` 에 `["c", "a", "b", "a", "c"]` | `["c", "a", "b"]` |
| `["a", "b"]` 에 `["b", "c"]` | `["a", "b", "c"]` |
| `["a"]` 에 `["A", "b"]` | `["a", "A", "b"]`. 대소문자를 가림 |

```java
StringList unique = new StringList();
unique.addAllExclusive(FrameworkUtil.split("P-003,P-001,P-003", ","));
```

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `list.uniq()` | 중복을 빼지만 `HashSet` 을 거쳐 순서가 바뀜. `["c", "a", "b", "a", "c"]` → `["a", "b", "c"]` |

### 정렬하기

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `list.sort()` | 없음 | 없음 (`void`). 목록을 그 자리에서 정렬. 대소문자를 가리지 않은 글자 순, 같으면 대문자가 앞 |

| 호출 | 결과 |
| --- | --- |
| `["b", "B", "a", "A", "c"]` 의 `sort()` | `["A", "a", "B", "b", "c"]` |
| `["10", "9", "2"]` 의 `sort()` | `["10", "2", "9"]`. 숫자도 글자로 비교 |
| `null` 이 든 목록의 `sort()` | `NullPointerException` |

```java
StringList docNames = part.getInfoList(context, "from[" + relPartDocument + "].to.name");
docNames.sort();
```

- Java 의 `Collections.sort(list)` 는 대소문자를 가려 `["A", "B", "a", "b", "c"]` 가 됩니다.

::: info 근거
eMatrixServletRMI.jar `StringList.filterEmpty()`: `stream` 에서 `null` 이 아니고 `String.isEmpty()` 가 아닌 값만 모아 새 `StringList`. `createNotNull` 은 `null` 만 거름. `matrix.util.List.addAllExclusive(List)`: 인자가 `null` 이면 `true`, 아니면 요소마다 지금 목록에 `contains` 가 거짓일 때만 `add`. `uniq()` 는 `new HashSet(this)` 로 모은 뒤 목록을 비우고 다시 넣음. `sort()` 는 값마다 `toLowerCase() + 원래 값` 을 만들어 `compareTo` (`Sortable` 이면 `getSortString()`). 결과는 로컬 실행으로 확인. OOTB 호출: `.filterEmpty()`, `.addAllExclusive(`, `.uniq()` 0 건
:::

## 조회 결과 정렬

`getRelatedObjects`, `DomainObject.getInfo(context, ids, selects)`, `findObjects` 가 돌려주는 `MapList` 를 행 Map 의 값으로 정렬할 때 씁니다.

- `MapList` 는 `java.util.ArrayList` 이고 요소가 `Map` 입니다. `for (Object o : mapList)` 로 돌면서 `(Map) o` 로 받습니다 ([MapList, StringList](/jpo/domain-object#maplist-stringlist)).
- 조회 메서드가 이미 `MapList` 를 돌려주므로 `FrameworkUtil.toMapList` 로 바꿀 일은 거의 없습니다. `DomainObject.getInfo(context, ids, selects)` 가 안에서 `BusinessObject.getSelectBusinessObjectData` 결과를 `toMapList` 로 바꿔 돌려줍니다 ([여러 객체를 한 번에](/jpo/api/read#여러-객체를-한-번에)).

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `mapList.sort(key, dir, type)` | 아래 표 | 없음 (`void`). 그 자리에서 정렬 |
| `mapList.addSortKey(key, dir, type)` 여러 번 + `mapList.sort()` | 아래 표. 먼저 넣은 키가 우선 | 없음 |
| `mapList.sortStructure(key, dir, type)` | 아래 표. 쉼표로 이어 여러 키를 줄 수 있음 | 없음 |

| 인자 | 넘기는 값 |
| --- | --- |
| `key` | 행 Map 의 키 (`name`, `attribute[My Quantity]` ...) |
| `dir` | `"ascending"` 이면 오름차순, 그 밖의 값은 모두 내림차순 (`"asc"` 도 내림차순). `null` 이면 `"ascending"`. 대소문자 무시 |
| `type` | `"string"` (대소문자 무시 글자 순), `"stringpad"` (짧은 값이 앞, 길이가 같으면 글자 순. `A9` 가 `A10` 앞), `"integer"` (정수), `"real"` (실수), `"date"` (DB 날짜 형식으로 읽어 비교). `null` 이면 `"string"`. 대소문자 무시 |

| 값 | 정렬 결과 |
| --- | --- |
| 비었거나 (`""`) 키가 없는 행 | 오름차순이면 맨 앞, 내림차순이면 맨 뒤 |
| `integer`, `real` 인데 숫자가 아닌 값 | 그 두 값만 글자 순으로 비교 |
| `date` 인데 날짜로 읽지 못하는 값 | `IllegalArgumentException` |
| 값이 `StringList` (값이 여러 개인 select) | `ClassCastException` |
| 위에 없는 `type` (`"number"` 같은 오타) | 그 키로는 정렬하지 않음. 모두 같다고 봄 |
| 키 값이 같은 행 | 원래 순서를 지킴 |

### 한 키로 정렬

```java
MapList children = part.getRelatedObjects(context, relStructure, "*",
        StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_TYPE, DomainConstants.SELECT_NAME,
                DomainConstants.SELECT_MODIFIED),
        StringList.create(DomainConstants.SELECT_RELATIONSHIP_ID), false, true, (short) 1, null, null, 0);
children.sort(DomainConstants.SELECT_MODIFIED, "descending", "date");
```

### 여러 키로 정렬

```java
MapList children = part.getRelatedObjects(context, relStructure, "*",
        StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_TYPE, DomainConstants.SELECT_NAME),
        StringList.create(DomainConstants.SELECT_RELATIONSHIP_ID), false, true, (short) 1, null, null, 0);
children.addSortKey(DomainConstants.SELECT_TYPE, "ascending", "string");
children.addSortKey(DomainConstants.SELECT_NAME, "ascending", "string");
children.sort();
```

- 키는 쌓입니다. 같은 `MapList` 를 다른 키로 다시 정렬하려면 `clearSortKeys()` 를 먼저 부릅니다.
- `sort(key, dir, type)` 은 쌓인 키를 쓰지도 바꾸지도 않습니다.

### 구조 목록 정렬

`getRelatedObjects` 를 여러 단계로 펼친 결과처럼 행마다 `level` 이 있는 목록을, 부모 행 바로 뒤에 그 자식들이 오도록 두고 같은 부모의 자식끼리 정렬합니다.

```java
MapList structure = part.getRelatedObjects(context, relStructure, "*",
        StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME),
        StringList.create(DomainConstants.SELECT_RELATIONSHIP_ID), false, true, (short) 0, null, null, 0);
structure.sortStructure(DomainConstants.SELECT_NAME, "ascending", "string");
```

- `getRelatedObjects` 가 돌려준 순서 (부모 다음에 그 자식) 를 그대로 둔 목록에 씁니다. 행의 부모는 그 행보다 위에 있는 행 가운데 `level` 이 하나 작은 가장 가까운 행으로 정해집니다.
- 첫 행에 `level` 키가 없으면 `sort(key, dir, type)` 과 같습니다.
- 행 Map 마다 비교에 쓴 `sortBy` 키를 넣습니다.
- 여러 키는 `sortStructure("type,name", "ascending,descending", "string,string")` 처럼 쉼표로 잇습니다. `addSortKey` 로 넣은 키로 하려면 `sortStructure()` 를 부릅니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `mapList.sortStructure(context, key, dir, comparatorName)` | 서버의 `emxUITable:sortMapList` JPO 를 불러 표의 정렬 비교기 이름으로 정렬함 (표 화면용) |

::: info 근거
xerinfra.jar `MapList.sort(String, String, String)`, `addSortKey`: 방향이 `null` 이면 `ascending`, 형이 `null` 이면 `string`, 둘 다 `toLowerCase` 로 바꿔 키 Map (`name`, `dir`, `type`) 을 만듦. `addSortKey` 는 `_sortKeys` 에 더하고 `clearSortKeys` 가 비움. `sort()` 는 `_sortKeys` 로, `sort(key, dir, type)` 은 그 키 하나만으로 `Collections.sort`. `MapList$MapComparator.compare`: 방향은 `"ascending".equals(dir)` 일 때만 오름차순. 값을 `(String)` 으로 꺼내 둘 다 비면 0, 앞만 비면 -1, 뒤만 비면 1. `string` 은 `compareToIgnoreCase`, `stringpad` 는 길이 차이 다음 `compareToIgnoreCase`, `integer` / `real` 은 `Long.valueOf` / `Double.valueOf` (`NumberFormatException` 이면 `compareToIgnoreCase`), `date` 는 `eMatrixDateFormat.getJavaDate` (형식으로 못 읽으면 `new Date(String)`). 그 밖의 형은 0. `sortStructure(String, String, String)` 은 쉼표로 나눈 키마다 정렬 키를 만들고, 첫 행에 `level` 이 있으면 행마다 위 단계 행들의 키 값과 순번을 이은 `sortBy` 목록을 넣어 `StructuredMapComparator` (앞부분이 같으면 짧은 쪽이 앞) 로 정렬. `sortStructure(Context, String, String, String)` 은 `JPO.invoke(context, "emxUITable", null, "sortMapList", ...)`. `MapComparator` 를 만들 때 `eMatrixDateFormat` 을 읽어 로컬 실행이 막혀 바이트코드로만 확인. `DomainObject.getInfo(Context, String[], StringList, boolean)` 은 `BusinessObject.getSelectBusinessObjectData` 결과를 `FrameworkUtil.toMapList` 로 바꿈. OOTB 호출: `.addSortKey(` 89, `.sortStructure(` 19, `.clearSortKeys()` 0, `FrameworkUtil.toMapList(` 48
:::

## 샘플 JPO

[XX_UtilString_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilString_mxJPO.java) 의 메서드입니다. 모든 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map (`programMap`) 에서 값을 읽습니다.

| 메서드 | 받는 값 (`programMap` 의 키) | 돌려주는 값 | 보여 주는 것 |
| --- | --- | --- | --- |
| `isEmptyValue` | `value` | `Boolean` | [빈 값 확인](#빈-값-확인) |
| `splitValues` | `values` (쉼표로 이은 값) | `StringList`. `"a,,b,"` 면 `["a", "", "b", ""]` | [문자열 자르기](#문자열-자르기) |
| `getDocumentNames` | `objectId` | `String`. 연결된 문서 이름을 `", "` 로 이은 값 | [문자열 잇기](#문자열-잇기) |
| `toSingleLine` | `text` | `String`. 줄바꿈 (`\r\n`, `\n`) 을 공백으로 바꾼 값 | [글자 바꾸기](#글자-바꾸기) |
| `getShortDescription` | `objectId` | `Map` (`byWord`, `byChar`). 설명을 50 글자로 줄인 두 가지 값 | [글자 수로 줄이기](#글자-수로-줄이기) |
| `cutMailSubject` | `subject` | `String`. 250 바이트 안으로 자른 제목 | [UTF-8 바이트 수로 자르기](#utf-8-바이트-수로-자르기) |
| `getPartBasics` | `objectId` | `Map` (`name`, `revision`, `current`, `id`, `type`) | [목록 만들기](#목록-만들기) 로 만든 select 목록 |
| `getNamesOfIds` | `objectIds` (쉼표로 이은 id) | `MapList` (id 마다 `id`, `name`) | [목록을 배열로 바꾸기](#목록을-배열로-바꾸기). 빈 id 는 거름 |
| `removeEmptyValues` | `values` | `StringList`. `"a,,b,"` 면 `["a", "b"]` | [빈 값 빼기](#빈-값-빼기) |
| `removeDuplicates` | `values` | `StringList`. `"c,a,b,a,c"` 면 `["c", "a", "b"]` | [중복 빼기](#중복-빼기) |
| `sortValues` | `values` | `StringList`. `"b,B,a,A,10,9"` 면 `["10", "9", "A", "a", "B", "b"]` | [정렬하기](#정렬하기) |
| `getChildrenByName` | `objectId` | `MapList` (자식 부품. 이름 오름차순) | [한 키로 정렬](#한-키로-정렬) |
| `getChildrenByTypeAndName` | `objectId` | `MapList` (타입 오름차순, 같은 타입은 이름 내림차순) | [여러 키로 정렬](#여러-키로-정렬) |
| `getSortedStructure` | `objectId` | `MapList` (끝까지 펼친 구조. 같은 부모의 자식끼리 이름 순) | [구조 목록 정렬](#구조-목록-정렬) |

- 부르는 법은 [불러 보기](/jpo/api/#불러-보기) 와 같습니다. 프로그램 이름은 `XX_UtilString` 입니다.
- `splitValues`, `removeEmptyValues`, `removeDuplicates`, `sortValues` 는 Context 없이 로컬에서 실행해 표의 결과를 확인했습니다.

::: details XX_UtilString_mxJPO.java

<<< @/../JPOSample/util/XX_UtilString_mxJPO.java

:::

::: warning
샘플의 관계 이름 (`relationship_MyPartStructure`, `relationship_MyPartDocument`) 은 예시입니다. 실제 스키마의 심볼릭 이름으로 바꿔야 합니다. 데이터를 바꾸는 메서드는 없습니다. 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다.
:::

## 다루지 않은 메서드

::: details 펼쳐 보기
| 클래스 | 메서드 | 이유 |
| --- | --- | --- |
| `UIUtil` | `displayField` <Badge type="warning" text="폐기" /> 2 / 3, `displayUOMTextField`, `displayUOMField`, `displayUOMComboField` (4), `getHTMLForComboFiled` | 화면용 |
| `UIUtil` | `getWindowTitleName` (2), `getLanguageForSpellChecker`, `getObjectCategoryJson`, `getTypeChooserValues`, `addSecureTokeninURL`, `getQuickFileRelPath`, `formatRelPath` | 화면용 |
| `UIUtil` | `logConfigurations` <Badge type="warning" text="폐기" /> | 화면용 |
| `UIUtil` | `setValue` | 화면용 |
| `UIUtil` | `toString`, `getSortedListFromMap` | OOTB 미사용 |
| `StringUtil` | `join(List, String, String)`, `join(StringList, StringList, String, String)` | OOTB 미사용 |
| `StringUtil` | `convertStringList2Vector` | OOTB 미사용 |
| `StringUtil` | `findReplace` | 화면용 |
| `StringUtil` | `encrypt`, `decrypt` | 내부용 |
| `FrameworkUtil` | `trimAll`, `comparePattern`, `getIntersection` | OOTB 미사용 |
| `FrameworkUtil` | `getSum`, `getAverage` (2), `getMedian` (2), `getMinimum`, `getMaximum`, `getStandardDeviation` (2), `getSortedList`, `getFormatedNumber` | 화면용 |
| `FrameworkUtil` | `toMap` (5), `toMapList` (13), `toAttributeList` | 같은 일 다른 메서드 |
| `StringUtils` (m1jsystem.jar, eMatrixServletRMI.jar) | `matches` <Badge type="warning" text="폐기" /> | 같은 일 다른 메서드 |
| `StringList` | `new StringList(int, int)` <Badge type="warning" text="폐기" /> | 같은 일 다른 메서드 |
| `StringList` | `checkForNull`, `copyOf`, `toList`, `unmodifiable`, `unmodifiableCopy`, `create(Stream)`, `compareTo` (2) | OOTB 미사용 |
| `matrix.util.List` | `setElementAt`, `insertElementAt`, `removeElementAt`, `removeAllElements`, `firstElement`, `lastElement`, `copyInto`, `ensureCapacity`, `capacity`, `trimToSize`, `clone` (모두 <Badge type="warning" text="폐기" />) | 같은 일 다른 메서드 |
| `MapList` | `sortStructure(Context, String, String, String, String)` | 화면용 |
:::
