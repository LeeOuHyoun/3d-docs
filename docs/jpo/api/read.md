# 객체 조회 <Badge type="info" text="원본 분석" />

객체와 관계의 값을 읽는 메서드입니다. 무엇을 읽을지는 select 문자열로 정하고, 값은 모두 문자열로 옵니다. 예제의 `part`, `relStructure` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다.

::: info 근거
시그니처와 동작은 xerinfra.jar `DomainObject`, `DomainRelationship`, `FrameworkUtil`, eMatrixServletRMI.jar `BusinessObject`, `ObjectWithSelectDataHolder` class 파일 (javap) 기준. 건수는 jpo 폴더 바로 아래 1,615 개 파일에서 센 호출 수: `DomainObject.newInstance(` 3,957, `new DomainObject(` 2,331, `.getInfo(context` 4,127 (그중 여러 id 형태 375), `.getRelatedObjects(context` 1,369, `.isKindOf(context` 678, `.getAttributeValue(context` 496, `DomainObject.findObjects(context` 360, `.getInfoList(context` 305, `BusinessObject.getSelectBusinessObjectData(` 168, `DomainRelationship.getInfo(context` 89
:::

샘플: [XX_ObjectApi_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/XX_ObjectApi_mxJPO.java) 의 `getPartInfo`, `getChildParts`, `getObjectsInfo`, `findLatestParts` ([메서드별 설명](/jpo/api/#샘플-jpo))

## 한눈에 보기

| 하고 싶은 일 | 메서드 | 돌려받는 것 |
| --- | --- | --- |
| 값 하나 읽기 | `obj.getInfo(context, select)` | `String` |
| 값 여러 개를 한 번에 읽기 | `obj.getInfo(context, selects)` | `Map` (select → 값) |
| 값이 여러 개인 select 읽기 | `obj.getInfoList(context, select)` | `StringList` |
| 여러 객체의 값을 한 번에 읽기 | `DomainObject.getInfo(context, ids, selects)` | `MapList` (객체마다 Map 하나) |
| 연결된 객체 목록 받기 | `obj.getRelatedObjects(...)` | `MapList` (연결마다 Map 하나) |
| 조건에 맞는 객체 찾기 | `DomainObject.findObjects(...)` | `MapList` (객체마다 Map 하나) |
| 관계의 값 읽기 | `DomainRelationship.getInfo(context, relIds, selects)` | `MapList` (관계마다 Map 하나) |

## 객체 얻기

값을 읽거나 고치려면 먼저 id 로 객체 변수를 만듭니다. 이 단계는 id 를 담아 두는 것이고, 값은 아래 메서드를 부를 때 서버에서 읽습니다.

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.newInstance(context, id)` | id 로 객체를 얻음. 타입에 맞는 클래스 (`Part` ...) 로 만들려고 객체를 한 번 읽음. OOTB 가 가장 많이 쓰는 형태 | `DomainObject` |
| `new DomainObject(id)` | 객체를 읽지 않고 바로 얻음. `getInfo`, `setAttributeValue` 같은 `DomainObject` 메서드만 쓸 때 | `DomainObject` |
| `DomainObject.newInstance(context)` | id 가 없는 빈 객체. [생성](/jpo/api/write#생성) 할 때 | `DomainObject` |
| `obj.setId(id)` | 같은 변수가 다른 객체를 가리키게 함. 반복문에서 객체 변수를 다시 쓸 때 | `void` |
| `DomainRelationship.newInstance(context, relId)`, `new DomainRelationship(relId)` | 관계 id 로 관계 변수를 얻음 | `DomainRelationship` |

```java
DomainObject part = DomainObject.newInstance(context, objectId);
DomainObject plain = new DomainObject(objectId);
```

### 타입, 이름, 리비전으로 얻기

id 를 모르고 타입, 이름, 리비전만 알 때는 먼저 id 를 읽습니다.

| 방법 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `MqlUtil.mqlCommand(context, "print bus $1 $2 $3 select $4 dump", 타입, 이름, 리비전, "id")` | 타입, 이름, 리비전으로 id 를 읽음. 그런 객체가 없으면 예외 | `String` (객체 id) |
| `new BusinessObject(타입, 이름, 리비전, vault)` 를 `open` 한 뒤 `getObjectId()` | 같은 일을 하는 예전 방식 ([open / close](#ootb-에-남아-있는-방식-open-close)) | `String` (객체 id) |
| `DomainObject.findObjects(...)` 에 이름과 리비전을 넘김 | 이름을 패턴으로 찾을 때. 없으면 빈 목록 | `MapList` ([검색](#검색-findobjects)) |

```java
String id = MqlUtil.mqlCommand(context, "print bus $1 $2 $3 select $4 dump", typePart, "P-0001", "A", "id");
DomainObject found = new DomainObject(id);
```

## 값 읽기 (getInfo)

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getInfo(context, select)` | select 하나의 값을 읽음 | `String` |
| `obj.getInfo(context, selects)` | select 여러 개 (`StringList`) 를 한 번에 읽음 | `Map`. 키는 넣은 select 문자열, 값은 `String`. 넣지 않아도 `id`, `type` 이 함께 들어 있음 |
| `obj.getInfo(context, selects, multiSelects)` | 위와 같고, `multiSelects` 에도 넣은 select 는 값을 목록으로 받음 | `Map`. `multiSelects` 와 `NEW_MULTI_VALUE_LIST` 의 select 는 `StringList`, 나머지는 `String`. `id`, `type` 은 더하지 않음 |
| `obj.getInfoList(context, select)` | 값이 여러 개인 select 하나를 읽음 | `StringList`. 값이 없으면 빈 목록 |

```java
StringList selects = StringList.create(DomainConstants.SELECT_NAME, DomainConstants.SELECT_CURRENT,
        DomainObject.getAttributeSelect(attrStatus));
Map info = part.getInfo(context, selects);
String name = (String) info.get(DomainConstants.SELECT_NAME);
String status = (String) info.get(DomainObject.getAttributeSelect(attrStatus));
```

`info` 의 모양 (값은 예시):

```text
{name=P-0001, current=Release, attribute[My Status]=Approved, id=11111.22222.33333.44444, type=My Part}
```

- 값을 여러 개 읽을 때는 `getInfo(context, select)` 를 여러 번 부르지 말고 `selects` 로 한 번에 읽습니다. 부를 때마다 서버를 다녀옵니다.
- 연결이 없는 `from[관계].to.id` 처럼 값이 없을 수 있는 select 는 결과가 `null` 일 수 있으니 확인하고 씁니다.
- `getName(context)`, `getType(context)` 같은 getter 는 <Badge type="warning" text="폐기" /> 되었고 부를 때마다 객체를 엽니다. 대신 `getInfo(context, DomainConstants.SELECT_NAME)` 처럼 읽고, 값이 둘 이상 필요하면 `getInfo(context, selects)` 를 씁니다 ([기본 정보](/jpo/api/domain-object#기본-정보)).

### 값이 여러 개인 select

`from[관계].to.id` 처럼 값이 여러 개일 수 있는 select 는 **읽는 메서드에 따라 결과가 다릅니다**.

| 읽는 방법 | 값이 하나일 때 | 값이 여러 개일 때 |
| --- | --- | --- |
| `obj.getInfo(context, select)`, `obj.getInfo(context, selects)` | `String` | `String`. **첫 값만** 옴 |
| `obj.getInfoList(context, select)` | `StringList` | `StringList` |
| `obj.getInfo(context, selects, multiSelects)` 에서 `multiSelects` 에 넣은 select | `StringList` | `StringList` |
| `DomainObject.getInfo(context, ids, selects)`, `findObjects`, `DomainRelationship.getInfo` | `String` | `String`. 값들을 `\u0007` 문자로 이어 붙임 |
| 바로 위 메서드에 `multiSelects` 인자를 더 준 형태에서 `multiSelects` 에 넣은 select | `StringList` | `StringList` |
| `getRelatedObjects` | `String` | `StringList` |

전부 받으려면 `getInfoList` 를 쓰거나 `multiSelects` 에 그 select 를 넣습니다.

```java
String select = "from[" + relPartDocument + "].to.id";
StringList docIdList = part.getInfoList(context, select);

Map info = part.getInfo(context, StringList.create(DomainConstants.SELECT_NAME, select), StringList.create(select));
StringList sameDocIds = (StringList) info.get(select);

MapList rows = DomainObject.getInfo(context, ids, StringList.create(select), StringList.create(select));
StringList docIdsOfFirst = (StringList) ((Map) rows.get(0)).get(select);
```

- `\u0007` 로 이어진 문자열을 받았으면 `FrameworkUtil.split(값, SelectConstants.cSelectDelimiter)` 로 자릅니다.
- `getRelatedObjects` 결과처럼 `String` 과 `StringList` 가 섞여 오는 값은 `instanceof StringList` 로 나눠 받습니다 ([여러 값 select](/jpo/domain-object#여러-값-select)).
- `state`, `format.file.name`, `grantee` 같은 몇몇 select 는 `multiSelects` 에 넣지 않아도 `getInfo(context, selects)`, 여러 객체 `getInfo`, `findObjects` 에서 `StringList` 로 옵니다. `DomainConstants.NEW_MULTI_VALUE_LIST` 에 미리 들어 있기 때문입니다.

::: info 근거
서버가 준 값은 `ObjectWithSelectDataHolder` 에 select → 문자열로 들어가고, 값이 여러 개면 `\u0007` 로 이어져 있음 (`getSelectData` 는 그대로, `getSelectDataList` 는 `SelectConstants.parseToStringList` 로 자름). `DomainObject.getInfo(Context, StringList, Collection, boolean)` 은 `multiSelects` 와 `NEW_MULTI_VALUE_LIST` 에 없는 키의 값을 목록의 첫 요소 (`get(0)`) 로 바꾸고 (`multiSelects` 가 `null` 이면 `NEW_MULTI_VALUE_LIST` 도 합치지 않음), `getInfo(Context, String)` 은 `multiSelects` 자리에 `null` 을 넘김. `getInfo(Context, StringList)` 는 select 목록에 `id`, `type` 을 더해 부름. `FrameworkUtil.toMap(BusinessObjectWithSelect, Collection)` (여러 객체 `getInfo`) 과 `toMapList(QueryIterator, Collection)` (`findObjects`) 은 `multiSelects` 에 있으면 `getSelectDataList`, 없으면 `getSelectData`. `getRelatedObjects` 는 `ExpandParams.setIsList(false)` 로 펼쳐서 `getHashtable` 이 `\u0007` 이 든 값만 `StringList` 로 바꿈. `getInfoList` 는 결과가 `null` 이면 `new StringList()`. OOTB 가 `FrameworkUtil.split(값, "\7")` 로 자르는 곳 17 건
:::

### 속성 읽기

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getAttributeValue(context, 속성이름)` | 속성 하나의 값. `getInfo(context, "attribute[속성이름]")` 과 같음 | `String` |
| `obj.getAttributeMap(context)` | 객체의 모든 속성 | `Map` (속성 이름 → 값. 여러 값 속성은 `StringList`) |
| `obj.getAttributeMap(context, true)` | 위와 같음. 서버에서 읽는 호출 (`getAttributeValues(context, true)`) 만 다름 ([getAttributeMap](/jpo/api/domain-object#getattributemap)) | 위와 같음 |

```java
String status = part.getAttributeValue(context, attrStatus);
Map attrs = part.getAttributeMap(context);
String sameStatus = (String) attrs.get(attrStatus);
```

`getAttributeMap` 의 키는 `attribute[My Status]` 가 아니라 속성 이름 `My Status` 입니다.

### 여러 객체를 한 번에

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.getInfo(context, ids, selects)` | id 배열의 객체들을 한 번에 읽음. 결과 행은 `ids` 순서 | `MapList`. 행마다 select → 값 Map |
| `BusinessObject.getSelectBusinessObjectData(context, ids, selects)` | 같은 일을 하는 낮은 수준 메서드. OOTB 168 건 | `BusinessObjectWithSelectList`. 값은 `getElement(i).getSelectData(select)`, 여러 값은 `getSelectDataList(select)` |

```java
MapList rows = DomainObject.getInfo(context, ids, StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME));
for (Object o : rows) {
    Map row = (Map) o;
    String name = (String) row.get(DomainConstants.SELECT_NAME);
}
```

`rows` 의 모양 (값은 예시):

```text
[
  {id=11111.22222.33333.44444, name=P-0001},
  {id=11111.22222.33333.55555, name=P-0002}
]
```

- 결과 Map 에는 넣은 select 만 들어 있습니다. id 가 필요하면 `SELECT_ID` 를 넣습니다.
- 표의 열 function 처럼 행이 많을 때는 행마다 `getInfo` 를 부르지 말고 이 메서드를 한 번 부릅니다 ([표 열 function](/jpo/args#표-열-function)).

### 자주 쓰는 select 상수

`DomainConstants` 의 상수와 그 값입니다.

| 상수 | 값 | 내용 |
| --- | --- | --- |
| `SELECT_ID`, `SELECT_PHYSICAL_ID` | `id`, `physicalid` | 객체 id |
| `SELECT_TYPE`, `SELECT_NAME`, `SELECT_REVISION` | `type`, `name`, `revision` | 타입, 이름, 리비전 |
| `SELECT_CURRENT`, `SELECT_STATES` | `current`, `state` | 현재 상태, 정책의 상태 목록 (여러 값) |
| `SELECT_POLICY`, `SELECT_OWNER`, `SELECT_VAULT` | `policy`, `owner`, `vault` | 정책, 소유자, vault |
| `SELECT_ORIGINATED`, `SELECT_MODIFIED` | `originated`, `modified` | 생성, 수정 일시 |
| `SELECT_DESCRIPTION` | `description` | 설명 |
| `SELECT_LAST_ID` | `last.id` | 최신 리비전의 id |
| `SELECT_ORGANIZATION`, `SELECT_PROJECT` | `organization`, `project` | 조직, 협업공간 |
| `SELECT_RELATIONSHIP_ID`, `SELECT_RELATIONSHIP_NAME` | `id[connection]`, `name[connection]` | 관계 id, 관계 이름. `getRelatedObjects` 의 관계 select 에 씀 |
| `SELECT_FROM_ID`, `SELECT_TO_ID` | `from.id`, `to.id` | 관계의 from 쪽, to 쪽 객체 id. 관계 select 에 씀 |
| `DomainObject.getAttributeSelect(속성이름)` | `attribute[속성이름]` | 속성 값 select 를 만들어 주는 메서드 |

## 타입, 존재, 리비전

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `obj.isKindOf(context, 타입)` | 그 타입이거나 그 하위 타입인지 | `boolean` |
| `obj.exists(context)` | 객체가 있는지 | `boolean` |
| `obj.isLastRevision(context)` | 최신 리비전인지 | `boolean` |
| `obj.getLastRevision(context)` | 최신 리비전 | `BusinessObject` |
| `obj.getPreviousRevision(context)`, `obj.getNextRevision(context)` | 이전, 다음 리비전 | `BusinessObject` |
| `obj.getRevisionsInfo(context, selects, new StringList())` | 모든 리비전의 select 값. 세 번째 인자는 값을 `StringList` 로 받을 select 이고, 없으면 빈 목록을 넘김 (`null` 이면 예외). 정책에 major 순서만 있으면 major 리비전마다 한 행 ([getRevisionsInfo](/jpo/api/domain-object#getrevisionsinfo)) | `MapList` |

```java
if (part.isKindOf(context, typePart) && !part.isLastRevision(context)) {
    String lastId = part.getLastRevision(context).getObjectId(context);
}
```

- 받은 `BusinessObject` 의 id 는 `getObjectId(context)` 로 꺼냅니다.
- 객체 없이 타입 이름만 있으면 `mxType.isOfParentType(context, 타입, 상위타입)` 을 씁니다 ([스키마 이름](/jpo/api/util/schema)).
- select 로도 읽을 수 있습니다: `type.kindof[타입]`, `last.id`, `previous.id`, `next.id`, `revisions`.

::: info 근거
xerinfra.jar `DomainObject.isKindOf(Context, String)` 은 타입이 `null` 이면 `false`, 아니면 MQL `print bus $1 select $2 dump` 로 `type.kindof[타입]` 을 읽어 `TRUE` 인지 봄. `getLastRevision` 은 `getRevisions(context)` 목록의 마지막 요소
:::

## OOTB 에 남아 있는 방식: open / close

OOTB 코드에는 객체를 `open` 한 뒤 `context` 없는 getter 로 읽고 `close` 하는 형태가 많이 남아 있습니다. OOTB 코드를 읽을 때 알아 두고, 새 코드는 `getInfo(context, selects)` 를 씁니다.

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `bo.open(context)` | 서버에서 객체의 id, 타입, 이름, 리비전, vault 를 읽어 변수에 담아 둠 | `void` |
| `bo.getObjectId()`, `bo.getTypeName()`, `bo.getName()`, `bo.getRevision()` | 열어 둔 객체에서 값을 꺼냄. `open` 전에는 비어 있을 수 있음 | `String` |
| `bo.close(context)` | 연 것을 닫음 | `void` |

```java
BusinessObject last = part.getLastRevision(context);
last.open(context);
String lastId = last.getObjectId();
String lastRevision = last.getRevision();
last.close(context);

BusinessObject bo = new BusinessObject(typePart, "P-0001", "A", context.getVault().getName());
bo.open(context);
String id = bo.getObjectId();
bo.close(context);
```

`DomainObject` 의 `getName(context)`, `getId(context)` 는 이 open → getter → close 를 한 번에 해 주는 메서드입니다.

::: info 근거
jpo 폴더 `.open(context)` 679 건 (`Query`, `Role` 등을 여는 것 포함), `new BusinessObject(` 523 건 (그중 타입, 이름, 리비전, vault 를 넘기는 형태 72 건), `"print bus $1 $2 $3 select` 30 건. eMatrixServletRMI.jar `BusinessObject.open(Context)`: id 가 있으면 `open`, 없으면 `openTNRV(session, type, name, revision, vault, ...)` 를 서버에 부르고 받은 name, type, revision, vault, objectid 를 필드에 넣음. 예: emxECPartBase_mxJPO.java:7692-7697 (`getLastRevision` 뒤 `open`, `getObjectId()`, `getRevision()`, `close`), emxClassificationBase_mxJPO.java:151 (`new BusinessObject(type, name, revision, context.getVault().getName())`). xerinfra.jar `DomainObject.getName(Context)` 은 `openObject` → `getName()` → `closeObject`
:::

## 관계 따라가기 (getRelatedObjects)

이 객체에 연결된 객체를 목록으로 받습니다. 연결된 객체의 값과 관계의 값을 한 번에 가져옵니다.

```java
MapList list = obj.getRelatedObjects(context, relPattern, typePattern, objectSelects, relSelects,
        getTo, getFrom, level, objectWhere, relWhere, limit);
```

| 인자 | 형 | 넘기는 값 |
| --- | --- | --- |
| `relPattern` | `String` | 따라갈 관계의 실제 이름. 여러 개는 쉼표 (`"A,B"`), 전부는 `"*"` |
| `typePattern` | `String` | 받을 객체의 타입 이름. 여러 개는 쉼표, 전부는 `"*"`. `null` 로 두지 않음 |
| `objectSelects` | `StringList` | 연결된 객체에서 읽을 select |
| `relSelects` | `StringList` | 관계에서 읽을 select (`id[connection]`, `attribute[속성이름]` ...) |
| `getTo` | `boolean` | `true` 면 부모 쪽으로 따라감 |
| `getFrom` | `boolean` | `true` 면 자식 쪽으로 따라감 |
| `level` | `short` | 몇 단계까지. `(short) 1` 은 바로 연결된 것만, `(short) 0` 은 끝까지 |
| `objectWhere`, `relWhere` | `String` | 조건식 (`"current==Release"`). 없으면 `null` |
| `limit` | `int` | 최대 개수. `0` 이면 제한 없음. 안에서 `short` 로 바꾸므로 32767 까지 |

방향은 아래처럼 고릅니다.

```text
부모 (from 쪽)  ◀── getTo = true ──  이 객체  ── getFrom = true ──▶  자식 (to 쪽)
```

| 받고 싶은 것 | `getTo` | `getFrom` |
| --- | --- | --- |
| 자식 목록 (BOM 의 하위 부품, 붙은 문서) | `false` | `true` |
| 부모 목록 (Where Used) | `true` | `false` |

결과는 `MapList` 이고 연결 하나가 행 Map 하나입니다.

| 행 Map 의 키 | 값 |
| --- | --- |
| `objectSelects`, `relSelects` 에 넣은 select 문자열 | 그 값. 하나면 `String`, 여러 개면 `StringList` |
| `type` | 연결된 객체의 타입. 넣지 않아도 들어 있음 |
| `level` | 시작 객체에서 몇 단계인지 (`"1"`, `"2"` ...) |
| `relationship` | 관계의 이름 |

```java
MapList children = part.getRelatedObjects(context, relStructure, "*",
        StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME),
        StringList.create(DomainConstants.SELECT_RELATIONSHIP_ID, DomainRelationship.getAttributeSelect(attrQty)),
        false, true, (short) 1, null, null, 0);
for (Object o : children) {
    Map row = (Map) o;
    String childId = (String) row.get(DomainConstants.SELECT_ID);
    String relId = (String) row.get(DomainConstants.SELECT_RELATIONSHIP_ID);
    String qty = (String) row.get(DomainRelationship.getAttributeSelect(attrQty));
}
```

`children` 의 모양 (값은 예시):

```text
[
  {id=11111.22222.33333.55555, name=P-0002, type=My Part, id[connection]=99999.22222.33333.11111,
   attribute[My Quantity]=2, level=1, relationship=My Part Structure},
  {id=11111.22222.33333.66666, name=P-0003, type=My Part, id[connection]=99999.22222.33333.22222,
   attribute[My Quantity]=4, level=1, relationship=My Part Structure}
]
```

- 객체 값과 관계 값은 한 Map 에 같이 담깁니다. 키가 같으면 관계 값이 덮어쓰므로 관계의 id 와 이름은 `id[connection]`, `name[connection]` 으로 읽습니다.
- `typePattern` 에는 `"*"` 를 넣습니다. `null` 이나 `""` 를 전체로 바꿔 주는 처리는 `emxFramework.enableTypePattern_NULL_as_ALL` 설정이 `true` 일 때만 하는데, 분석한 properties 에는 이 설정이 없습니다. `relPattern` 은 `null` 이나 `""` 면 `"*"` 로 바뀝니다.
- OOTB 에 가장 많은 10 개 인자 형태 (`limit` 없음) 는 <Badge type="warning" text="폐기" /> 되었습니다. 11 개 형태에 `limit` `0` 을 넘깁니다.
- 패턴으로 결과를 한 번 더 거르는 14 개 인자 형태와 나머지 형태는 [getRelatedObjects](/jpo/api/domain-object#getrelatedobjects) 에 있습니다.

### 한 개만, id 만

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getRelatedObject(context, rel, isFrom, objectSelects, relSelects)` | 바로 연결된 것 중 첫 번째 하나. `isFrom` 이 `true` 면 자식 쪽, `false` 면 부모 쪽 | `Map`. 없으면 `null` |
| `obj.getInfoList(context, "from[관계].to.id")` | 자식 id 만 필요할 때. 부모는 `to[관계].from.id` | `StringList` |

::: info 근거
xerinfra.jar `DomainObject.getRelatedObjects` 는 인자 개수가 다른 형태 9 개가 모두 18 개 인자 형태로 모임. 그 안에서 `objectSelects` 에 `type` 이 없으면 더하고 `getExpansionIterator` 로 펼친 뒤 `FrameworkUtil.toMapList(ExpansionIterator, ...)` 로 바꿈. `toMapList` 는 객체 값 Hashtable 에 관계 값을 `put` 하고 `level` (`Integer.toString(getLevel())`), `relationship` (`getTypeName()`) 을 넣음. `relPattern` 이 `null` / `""` 면 `"*"`, `typePattern` 은 static `ENABLE_TYPE_PATTERN_NULL_AS_ALL` (`emxFramework.enableTypePattern_NULL_as_ALL`) 이 `true` 일 때만 `"*"`. `limit` 은 `short` 로 바꿔 `ExpandParams.setLimit`. `getRelatedObject` 는 `getRelatedObjects(context, rel, "*", objectSelects, relSelects, !isFrom, isFrom, (short) 1, null, null)` 결과의 첫 행이고, 행이 없으면 `null`. 호출 예: emxCommonDocumentUIBase_mxJPO.java:142-160
:::

## 검색 (findObjects)

타입, 이름, 조건으로 객체를 찾습니다. MQL `temp query` 와 같습니다. 인자 개수가 다른 세 형태를 많이 씁니다.

| 형태 | 인자 |
| --- | --- |
| 5 개 | `findObjects(context, type, vault, where, selects)` |
| 9 개 | `findObjects(context, type, name, revision, owner, vault, where, expandType, selects)` |
| 11 개 | `findObjects(context, type, name, revision, owner, vault, where, queryName, expandType, selects, limit)` |

| 인자 | 형 | 넘기는 값 |
| --- | --- | --- |
| `type`, `name`, `revision`, `owner` | `String` | 패턴 (`"P-*"`). `null` 이면 `"*"` |
| `vault` | `String` | vault 이름 패턴. `null` 이면 검색용 vault 설정을 씀 |
| `where` | `String` | 조건식 (`"current==Release"`, `"revision==last"`). 없으면 `""` 또는 `null` |
| `expandType` | `boolean` | `true` 면 하위 타입까지 찾음. 5 개 형태는 항상 `true` |
| `selects` | `StringList` | 찾은 객체에서 읽을 select |
| `limit` | `short` | 최대 개수. 5 개, 9 개 형태는 `0`. `0` 은 `matrix.db.Query` 의 기본값과 같은 값이고 OOTB 는 개수를 제한하지 않을 때 넘김. 32767 까지 |
| `queryName` | `String` | 저장된 query 이름. 보통 `null` |

결과는 `MapList` 이고 찾은 객체 하나가 행 Map 하나입니다. 키는 `selects` 에 넣은 select 문자열이고, `type` 은 넣지 않아도 들어 있습니다.

```java
MapList found = DomainObject.findObjects(context, typePart, "P-*", "*", "*", null, "revision==last", true,
        StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME));
```

`found` 의 모양 (값은 예시):

```text
[
  {id=11111.22222.33333.44444, name=P-0001, type=My Part},
  {id=11111.22222.33333.55555, name=P-0002, type=My Part}
]
```

::: tip where 를 꼭 좁힙니다
5 개, 9 개 형태는 개수 제한이 없습니다. 결과가 많을 수 있는 타입이면 where 로 좁히거나 11 개 형태의 `limit` 을 줍니다.
:::

::: info 근거
xerinfra.jar `DomainObject.findObjects` 는 인자 개수가 다른 형태 11 개가 모두 `(Context, type, name, revision, owner, vault, where, queryName, expandType, selects, limit, ...)` 형태로 모임. 5 개 형태는 name / revision / owner / queryName 에 `null`, expandType 에 `true`, limit 에 `0` 을 넘김. 안에서 `null` 패턴은 `"*"`, vault 가 `null` 이면 `VaultUtil.getSearchVaultPattern(context)`, `selects` 에 `type` 이 없으면 더하고 `matrix.db.Query` 의 `getIterator` 로 실행한 뒤 `FrameworkUtil.toMapList(QueryIterator, Collection)` 으로 바꿈
:::

## 관계 값 읽기

| 메서드 | 하는 일 | 돌려받는 것 |
| --- | --- | --- |
| `DomainRelationship.getInfo(context, relIds, selects)` | 관계 id 배열의 관계들을 한 번에 읽음 (`from.id`, `to.id`, `attribute[속성이름]` ...) | `MapList`. 행마다 select → 값 Map |
| `DomainRelationship.getAttributeValue(context, relId, 속성이름)` | 관계 속성 하나 | `String` |
| `rel.getAttributeMap(context)` | 관계의 모든 속성 | `Map` (속성 이름 → 값) |
| `DomainRelationship.getAttributeSelect(속성이름)` | 관계 select 에 넣을 `attribute[속성이름]` 을 만듦 | `String` |

```java
String qty = DomainRelationship.getAttributeValue(context, relId, attrQty);
MapList rels = DomainRelationship.getInfo(context, relIds,
        StringList.create(DomainConstants.SELECT_FROM_ID, DomainConstants.SELECT_TO_ID));
```

`rels` 의 모양 (값은 예시):

```text
[
  {from.id=11111.22222.33333.44444, to.id=11111.22222.33333.55555}
]
```

MQL 로 읽을 때는 `MqlUtil.mqlCommand(context, "print connection $1 select $2 dump", relId, "to.id")` 를 씁니다 ([MQL](/jpo/api/util/mql)).
