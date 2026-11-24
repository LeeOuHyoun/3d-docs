# JPO API 레퍼런스 <Badge type="info" text="원본 분석" />

OOTB JPO 가 객체를 읽고, 만들고, 고치고, 지울 때 실제로 쓰는 메서드를 **하는 일, 넘기는 값, 돌려받는 값** 으로 정리한 사양서입니다. 왜 그렇게 쓰는지와 배경 설명은 가이드 ([DomainObject 와 select](/jpo/domain-object), [MqlUtil](/jpo/mql-util), [자주 쓰는 유틸리티](/jpo/utils)) 에 있습니다.

::: info 근거
enovia2025 `jpo/` 바로 아래 `*_mxJPO.java` 1,615 개에서 호출 수를 세어 많이 쓰는 메서드를 골랐습니다. 시그니처와 동작은 `3dspace/WEB-INF/lib` 의 xerinfra.jar, eMatrixServletRMI.jar class 파일을 `javap` 로 읽어 확인했습니다. 이 섹션의 Java 예제와 샘플은 같은 jar 로 컴파일해 확인했고, 서버에서 실행해 보지는 않았습니다.
:::

## 페이지

| 페이지 | 찾을 수 있는 것 |
| --- | --- |
| [객체 조회](/jpo/api/read) | 값 읽기, 연결된 객체 목록, 검색, 관계 값 |
| [객체 생성, 수정](/jpo/api/write) | 만들기, 개정, 값과 상태 바꾸기, 연결, 트랜잭션 |
| [객체 삭제, 연결 끊기](/jpo/api/delete) | 객체 지우기, 관계 끊기 |
| [유틸리티](/jpo/api/utilities) | 인자, 문자열, 목록, 이름 변환, MQL, 사용자, 날짜, 화면 문자열 |
| [DomainObject 메서드 사양](/jpo/api/domain-object) | `DomainObject` 에 선언된 메서드 전부 (폐기 메서드와 대체 포함), 샘플 `XX_DomainObjectApi` |

## 먼저 알아 둘 다섯 가지

### 1. 객체는 id 로, 관계는 관계 id 로 가리킵니다

| 가리키는 것 | 넘기는 값 | 얻는 곳 |
| --- | --- | --- |
| 객체 | 객체 id (`id` select 의 값) | 화면이 넘기는 `objectId`, 조회 결과의 `id` |
| 관계 (연결 하나) | 관계 id (`id[connection]` select 의 값) | `getRelatedObjects` 결과의 `id[connection]`, 객체의 `from[관계].id` |

관계 하나하나도 id 와 속성을 가집니다. 관계 속성을 고치거나 관계를 끊을 때는 객체 id 가 아니라 관계 id 를 넘깁니다.

### 2. 읽을 값은 select 문자열로 정합니다

| select | 뜻 |
| --- | --- |
| `name`, `revision`, `current`, `owner` | 이름, 리비전, 현재 상태, 소유자 |
| `attribute[Title]` | Title 속성의 값 |
| `from[EBOM].to.id` | EBOM 관계로 연결된 자식 객체의 id. 여러 개일 수 있음 |

MQL `print bus ... select` 에 쓰는 것과 같은 문법입니다. 전체 표는 [select 문법](/jpo/domain-object#select-문법) 에 있습니다.

### 3. 관계에는 방향이 있습니다

```text
from 쪽 (부모)  ────  관계  ────▶  to 쪽 (자식)
조립품 A        ────  EBOM  ────▶  부품 B
```

| 표현 | 뜻 |
| --- | --- |
| `A → B` | 이 문서의 표기. A 가 from 쪽 (부모), B 가 to 쪽 (자식) |
| `from[EBOM].to.id` | 내가 from 쪽인 EBOM 관계의 to 쪽 객체 id. 곧 자식 |
| `to[EBOM].from.id` | 내가 to 쪽인 EBOM 관계의 from 쪽 객체 id. 곧 부모 |

방향을 반대로 주면 조회는 오류 없이 빈 결과가 나오고, 연결은 거꾸로 만들어지거나 실패합니다.

### 4. 이름은 실제 이름으로 넘깁니다

타입, 관계, 속성, 상태 이름은 화면에 보이는 이름이 아니라 스키마의 실제 이름입니다. 코드에는 심볼릭 이름 (`type_Part`) 을 적고 실행할 때 실제 이름으로 바꿉니다.

```java
String typePart = PropertyUtil.getSchemaProperty(context, "type_Part");
```

등록되지 않은 심볼릭 이름이면 빈 문자열이 옵니다. 그 값을 그대로 넘기면 오류 없이 엉뚱한 결과가 나옵니다 ([심볼릭 이름](/jpo/domain-object#심볼릭-이름)).

### 5. 값은 모두 문자열입니다

| 값 | 형식 |
| --- | --- |
| 숫자 | 숫자를 적은 문자열 (`"2"`) |
| 날짜 | `MM/dd/yyyy hh:mm:ss a` 형식 문자열 (`"11/23/2026 02:30:00 PM"`). 변환은 [날짜](/jpo/api/util/date) |
| 참 / 거짓 | `"TRUE"`, `"True"` 같은 문자열. `"true".equalsIgnoreCase(값)` 으로 비교 |
| 값이 여러 개 | 읽는 메서드에 따라 다름. [값이 여러 개인 select](/jpo/api/read#값이-여러-개인-select) |

## 하고 싶은 일로 찾기

| 하고 싶은 일 | 메서드 | 문서 |
| --- | --- | --- |
| id 로 객체 얻기 | `DomainObject.newInstance(context, id)` | [객체 얻기](/jpo/api/read#객체-얻기) |
| 타입, 이름, 리비전으로 id 알아내기 | `print bus $1 $2 $3 select $4 dump` | [타입, 이름, 리비전으로 얻기](/jpo/api/read#타입-이름-리비전으로-얻기) |
| 객체의 값 읽기 | `obj.getInfo` | [값 읽기](/jpo/api/read#값-읽기-getinfo) |
| 값이 여러 개인 select 읽기 | `obj.getInfoList` | [값이 여러 개인 select](/jpo/api/read#값이-여러-개인-select) |
| 여러 객체의 값을 한 번에 읽기 | `DomainObject.getInfo(context, ids, selects)` | [여러 객체를 한 번에](/jpo/api/read#여러-객체를-한-번에) |
| 자식, 부모 객체 목록 받기 | `obj.getRelatedObjects` | [관계 따라가기](/jpo/api/read#관계-따라가기-getrelatedobjects) |
| 타입, 이름, 조건으로 찾기 | `DomainObject.findObjects` | [검색](/jpo/api/read#검색-findobjects) |
| 하위 타입인지, 최신 리비전인지 확인 | `obj.isKindOf`, `obj.isLastRevision` | [타입, 존재, 리비전](/jpo/api/read#타입-존재-리비전) |
| 관계 속성 읽기 | `DomainRelationship.getAttributeValue` | [관계 값 읽기](/jpo/api/read#관계-값-읽기) |
| 객체 만들기 | `obj.createObject` | [생성](/jpo/api/write#생성) |
| 만들면서 바로 연결하기 | `obj.createAndConnect` | [생성](/jpo/api/write#생성) |
| 다음 리비전, 복사본 만들기 | `obj.reviseObject`, `obj.cloneObject` | [생성](/jpo/api/write#생성) |
| 속성, 설명, 소유자 바꾸기 | `obj.setAttributeValues`, `obj.setDescription`, `obj.setOwner` | [값 수정](/jpo/api/write#값-수정) |
| 상태 바꾸기 | `obj.promote`, `obj.setState` | [상태 바꾸기](/jpo/api/write#상태-바꾸기) |
| 두 객체 연결하기 | `DomainRelationship.connect` | [연결](/jpo/api/write#연결) |
| 관계 속성, 관계 끝 객체 바꾸기 | `DomainRelationship.setAttributeValue`, `setToObject` | [관계 수정](/jpo/api/write#관계-수정) |
| 여러 변경을 함께 되돌릴 수 있게 묶기 | `ContextUtil.startTransaction` | [여러 변경을 한 묶음으로](/jpo/api/write#여러-변경을-한-묶음으로) |
| 객체 지우기 | `obj.deleteObject`, `DomainObject.deleteObjects` | [객체 삭제](/jpo/api/delete#객체-삭제) |
| 관계 끊기 | `DomainRelationship.disconnect` | [연결 끊기](/jpo/api/delete#연결-끊기) |
| 넘어온 인자 꺼내기 | `JPO.unpackArgs` | [인자](/jpo/api/util/args-id) |
| 빈 값 확인, 문자열 자르기 | `UIUtil.isNullOrEmpty`, `FrameworkUtil.split` | [빈 값, 문자열](/jpo/api/util/string-list) |
| 결과 목록 정렬하기 | `mapList.sort` | [목록](/jpo/api/util/string-list) |
| 심볼릭 이름을 실제 이름으로 | `PropertyUtil.getSchemaProperty` | [스키마 이름](/jpo/api/util/schema) |
| MQL 명령 실행하기 | `MqlUtil.mqlCommand` | [MQL](/jpo/api/util/mql) |
| 로그인 사용자 알아내기 | `context.getUser()`, `PersonUtil` | [사용자](/jpo/api/util/person) |
| 날짜 값 바꾸기 | `eMatrixDateFormat` | [날짜](/jpo/api/util/date) |
| HTML 에 값 넣기, 다국어 문구 읽기 | `XSSUtil`, `EnoviaResourceBundle` | [화면 문자열](/jpo/api/util/html-json) |

## 예제에서 쓰는 변수

이 섹션의 예제는 아래 스키마와 변수를 그대로 씁니다. 스키마 이름은 예시입니다.

```text
My Part  ────  My Part Structure (관계 속성 My Quantity)  ────▶  My Part        부품 → 하위 부품
My Part  ────  My Part Document                           ────▶  My Document    부품 → 문서
```

```java
String typePart = PropertyUtil.getSchemaProperty(context, "type_MyPart");
String typeDocument = PropertyUtil.getSchemaProperty(context, "type_MyDocument");
String relStructure = PropertyUtil.getSchemaProperty(context, "relationship_MyPartStructure");
String relPartDocument = PropertyUtil.getSchemaProperty(context, "relationship_MyPartDocument");
String attrStatus = PropertyUtil.getSchemaProperty(context, "attribute_MyStatus");
String attrQty = PropertyUtil.getSchemaProperty(context, "attribute_MyQuantity");
DomainObject part = DomainObject.newInstance(context, objectId);
```

| 변수 | 내용 |
| --- | --- |
| `context` | JPO 메서드가 첫 인자로 받는 `Context` |
| `objectId`, `part` | 부품 하나의 객체 id 와 그 객체 |
| `ids` | 객체 id 배열 (`String[]`) |
| `relId`, `relIds` | 관계 id 와 관계 id 배열 (`String[]`) |
| `doc`, `docIds` | 문서 객체 (`DomainObject`) 와 문서 id 배열 (`String[]`) |

## 샘플 JPO

[XX_ObjectApi_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/XX_ObjectApi_mxJPO.java) 하나에 이 섹션의 패턴을 메서드별로 담았습니다. 모든 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map (`programMap`) 에서 값을 읽습니다. 여기 없는 `DomainObject` 메서드 (`isKindOf`, `getRelatedObject`, `addRelatedObjects`, `cloneObject` ...) 는 [XX_DomainObjectApi_mxJPO.java](/jpo/api/domain-object#샘플-jpo) 에 있습니다.

| 메서드 | 받는 값 (`programMap` 의 키) | 돌려주는 값 | 보여 주는 것 |
| --- | --- | --- | --- |
| `getPartInfo` | `objectId` | `Map` | `getInfo` 와 `multiSelects` ([값 읽기](/jpo/api/read#값-읽기-getinfo)) |
| `getChildParts` | `objectId` | `MapList` | `getRelatedObjects`, 정렬 ([관계 따라가기](/jpo/api/read#관계-따라가기-getrelatedobjects)) |
| `getObjectsInfo` | `objectIds` (쉼표로 이은 id) | `MapList` | 여러 객체 `getInfo` ([여러 객체를 한 번에](/jpo/api/read#여러-객체를-한-번에)) |
| `findLatestParts` | `name` (이름 패턴. 비면 전체) | `MapList` | `findObjects` ([검색](/jpo/api/read#검색-findobjects)) |
| `createDocument` | `objectId`, `title` | `Map` (`id`, `relId`) | 트랜잭션, `createAndConnect` ([생성](/jpo/api/write#생성)) |
| `updatePart` | `objectId`, `status`, `description` | 없음 | 트랜잭션, 값 수정 ([값 수정](/jpo/api/write#값-수정)) |
| `updateQuantity` | `relId`, `quantity` | 없음 | 관계 속성 수정 ([관계 수정](/jpo/api/write#관계-수정)) |
| `releasePart` | `objectId` | `Integer` (움직인 단계 수) | `setState` ([상태 바꾸기](/jpo/api/write#상태-바꾸기)) |
| `revisePart` | `objectId` | `String` (새 리비전의 id) | `reviseObject` ([생성](/jpo/api/write#생성)) |
| `deleteParts` | `objectIds` (쉼표로 이은 id) | 없음 | `deleteObjects` ([객체 삭제](/jpo/api/delete#객체-삭제)) |
| `removeDocuments` | `objectId` | 없음 | 관계 id 를 읽어 `disconnect` ([연결 끊기](/jpo/api/delete#연결-끊기)) |
| `getModifiedLabel` | `objectId` | `String` | 빈 값 확인, 사용자 이름, 날짜, HTML 이스케이프 ([유틸리티](/jpo/api/util/date)) |

### 불러 보기

JSP 나 다른 JPO 에서 Map 을 만들어 넘깁니다.

```java
Map args = new HashMap();
args.put("objectId", objectId);
Map info = (Map) JPO.invoke(context, "XX_ObjectApi", null, "getPartInfo", JPO.packArgs(args), Map.class);
```

- 프로그램 이름은 클래스 이름에서 `_mxJPO` 를 뺀 `XX_ObjectApi` 입니다.
- 표나 폼 설정으로 화면에서 직접 부르는 메서드는 받는 Map 의 모양이 다릅니다 ([호출 종류별 인자 구조](/jpo/args)).

### 전체 소스

::: details XX_ObjectApi_mxJPO.java

<<< @/../JPOSample/XX_ObjectApi_mxJPO.java

:::

::: warning
샘플의 타입, 관계, 속성 (`type_MyPart`, `relationship_MyPartDocument` ...) 은 예시 이름입니다. 실제 스키마의 심볼릭 이름으로 바꿔야 합니다. 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다.
:::
