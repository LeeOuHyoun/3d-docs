# 객체 조회 <Badge type="info" text="원본 분석" />

OOTB JPO 가 객체와 관계의 값을 읽을 때 쓰는 메서드입니다. 무엇을 읽을지는 select 문자열 (`name`, `attribute[Title]`, `from[EBOM].to.id` ...) 로 정합니다. 문법은 [select 문법](/jpo/domain-object#select-문법) 에 있습니다.

::: info 근거
시그니처와 동작은 xerinfra.jar `DomainObject`, `DomainRelationship`, eMatrixServletRMI.jar `BusinessObject` class 파일 (javap) 기준. 건수는 jpo 폴더 바로 아래 1,615 개 파일에서 센 호출 수: `DomainObject.newInstance(` 3,957, `new DomainObject(` 2,331, `.getInfo(context` 4,127 (그중 여러 id 형태 375), `.getRelatedObjects(context` 1,369, `.isKindOf(context` 678, `.getAttributeValue(context` 496, `DomainObject.findObjects(context` 360, `.getInfoList(context` 305, `getSelectBusinessObjectData(context` 188, `DomainRelationship.getInfo(context` 89
:::

샘플: [XX_ObjectApi_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/XX_ObjectApi_mxJPO.java) 의 `getPartInfo`, `getChildParts`, `getObjectsInfo`, `findLatestParts`

## 객체 얻기

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `DomainObject.newInstance(context, id)` | id 의 객체. 타입에 등록된 하위 클래스 (`Part` ...) 로 만들려고 객체를 한 번 읽음 | `DomainObject` |
| `new DomainObject(id)` | 타입을 읽지 않고 바로 만듦. `DomainObject` 메서드만 쓸 때 | `DomainObject` |
| `DomainObject.newInstance(context)` | 빈 객체. [생성](/jpo/api/write#생성) 할 때 | `DomainObject` |
| `obj.setId(id)` | 같은 인스턴스를 다른 id 로 다시 씀 | `void` |
| `DomainRelationship.newInstance(context, relId)`, `new DomainRelationship(relId)` | 관계 하나 | `DomainRelationship` |

```java
DomainObject part = DomainObject.newInstance(context, objectId);
DomainObject obj = new DomainObject();
for (String id : ids) {
    obj.setId(id);
    obj.setAttributeValue(context, attrStatus, "Done");
}
```

## select 로 값 읽기

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `obj.getInfo(context, select)` | select 하나 | `String` |
| `obj.getInfo(context, selects)` | select 여러 개. 키는 넣은 select 문자열 그대로 | `Map` |
| `obj.getInfo(context, selects, multiSelects)` | `multiSelects` 에 넣은 select 는 값이 하나여도 `StringList` | `Map` |
| `obj.getInfoList(context, select)` | 값이 여러 개인 select 하나 (`from[X].to.id` ...) | `StringList` |
| `DomainObject.getInfo(context, ids, selects)` | 여러 객체를 한 번에. 결과는 `ids` 와 같은 순서로 받아 씀 | `MapList` |
| `BusinessObject.getSelectBusinessObjectData(context, ids, selects)` | 위와 같은 일을 하는 하위 API. 값은 `getElement(i).getSelectData(select)` | `BusinessObjectWithSelectList` |
| `obj.getAttributeValue(context, 속성)` | 속성 하나 | `String` |
| `obj.getAttributeMap(context)` | 모든 속성 (속성 이름 → 값) | `Map` |
| `obj.getAttributeMap(context, true)` | 위와 같고 여러 값 속성만 `StringList` 로 | `Map` |

- 값은 모두 문자열입니다. 날짜는 `MM/dd/yyyy hh:mm:ss a` 형식입니다 ([날짜](/jpo/utils#날짜)). 참 / 거짓을 돌려주는 select (`type.kindof[X]`, `from[관계]` ...) 도 `TRUE`, `True` 같은 문자열이라 `"true".equalsIgnoreCase(값)` 으로 비교합니다.
- 값이 여러 개일 수 있는 select 는 `getInfoList` 나 세 번째 인자 `multiSelects` 로 받습니다. 그래야 항상 `StringList` 입니다 ([여러 값 select](/jpo/domain-object#여러-값-select)).
- `getName(context)`, `getType(context)` 같은 getter 도 있지만 부를 때마다 객체를 엽니다. 여러 값은 `getInfo(context, selects)` 한 번으로 읽습니다.
- 열 function 처럼 행이 많을 때는 행마다 `getInfo` 를 부르지 말고 `DomainObject.getInfo(context, ids, selects)` 를 한 번 부릅니다 ([표 열 function](/jpo/args#표-열-function)).

```java
String attrStatus = PropertyUtil.getSchemaProperty(context, "attribute_MyStatus");
Map info = part.getInfo(context, StringList.create(DomainConstants.SELECT_NAME, DomainConstants.SELECT_CURRENT,
        DomainObject.getAttributeSelect(attrStatus)));
String name = (String) info.get(DomainConstants.SELECT_NAME);
String status = (String) info.get("attribute[" + attrStatus + "]");
StringList docIds = part.getInfoList(context, "from[" + relPartDocument + "].to.id");
MapList rows = DomainObject.getInfo(context, ids, StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME));
```

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
| `SELECT_LAST_ID` | `last.id` | 최신 리비전 id |
| `SELECT_ORGANIZATION`, `SELECT_PROJECT` | `organization`, `project` | 조직, 협업공간 |
| `SELECT_RELATIONSHIP_ID`, `SELECT_RELATIONSHIP_NAME` | `id[connection]`, `name[connection]` | 관계 id, 관계 이름 (`getRelatedObjects` 의 관계 select) |
| `SELECT_FROM_ID`, `SELECT_TO_ID` | `from.id`, `to.id` | 관계 양 끝 객체 (관계 select) |
| `DomainObject.getAttributeSelect(속성)` | `attribute[속성]` | 속성 값 select 를 만들어 줌 |

## 타입, 존재, 리비전

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `obj.isKindOf(context, 타입)` | 그 타입이거나 하위 타입인지 | `boolean` |
| `obj.exists(context)` | 객체가 있는지 | `boolean` |
| `obj.getLastRevision(context)` | 최신 리비전 | `BusinessObject` |
| `obj.getPreviousRevision(context)`, `getNextRevision(context)` | 이전, 다음 리비전 | `BusinessObject` |
| `obj.isLastRevision(context)` | 최신 리비전인지 | `boolean` |
| `obj.getRevisionsInfo(context, selects, new StringList())` | 모든 리비전의 select 값. 세 번째 인자는 OOTB 도 빈 목록을 넘김 | `MapList` |

- 받은 `BusinessObject` 의 id 는 `getObjectId(context)` 로 꺼냅니다.
- 타입 이름만 있고 객체가 없으면 `mxType.isOfParentType(context, 타입, 상위타입)` 을 씁니다 ([타입과 상태 이름](/jpo/utils#타입과-상태-이름)).
- select 로도 읽을 수 있습니다: `type.kindof[Part]`, `last.id`, `previous.id`, `next.id`, `revisions`.

## 관계 따라가기

### getRelatedObjects

```java
MapList list = obj.getRelatedObjects(context, relPattern, typePattern, objectSelects, relSelects,
        getTo, getFrom, (short) 1, objectWhere, relWhere, 0);
```

| 인자 | 형 | 값 |
| --- | --- | --- |
| `relPattern`, `typePattern` | `String` | 관계, 타입의 실제 이름. 여러 개는 쉼표, 전부는 `"*"` |
| `objectSelects`, `relSelects` | `StringList` | 결과 Map 에 담을 객체 값, 관계 값 |
| `getTo` | `boolean` | `true` 면 이 객체가 to 쪽인 관계를 따라감 → 부모 |
| `getFrom` | `boolean` | `true` 면 이 객체가 from 쪽인 관계를 따라감 → 자식 |
| 단계 | `short` | `1` 바로 아래, `0` 끝까지 |
| `objectWhere`, `relWhere` | `String` | 조건식 (`"current==Release"`). 없으면 `null` |
| 개수 제한 | `int` | `0` 이면 제한 없음 |

결과는 `MapList` 이고 행 Map 의 키는 아래와 같습니다.

| 키 | 값 |
| --- | --- |
| 넣은 select 문자열 | 그 값 (`id`, `name`, `id[connection]` ...) |
| `level` | 시작 객체에서 몇 단계인지 (`"1"`, `"2"` ...) |
| `relationship` | 관계 이름 |

```java
MapList children = part.getRelatedObjects(context, relStructure, "*",
        StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME),
        StringList.create(DomainConstants.SELECT_RELATIONSHIP_ID, DomainRelationship.getAttributeSelect(attrQty)),
        false, true, (short) 1, null, null, 0);
for (Object o : children) {
    Map row = (Map) o;
    String childId = (String) row.get(DomainConstants.SELECT_ID);
    String relId = (String) row.get(DomainConstants.SELECT_RELATIONSHIP_ID);
}
```

다른 오버로드와 패턴 규칙은 [관계 따라가기](/jpo/domain-object#관계-따라가기-getrelatedobjects) 에 있습니다.

### 그 밖의 형태

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `obj.getRelatedObject(context, rel, isFrom, objectSelects, relSelects)` | 바로 아래 / 위 한 단계에서 첫 번째 하나. `isFrom` 이 `true` 면 자식 쪽, `false` 면 부모 쪽 | `Map` (없으면 `null`) |
| `obj.getInfoList(context, "from[관계].to.id")` | 연결된 객체 id 만 필요할 때 | `StringList` |

::: info 근거
xerinfra.jar `DomainObject.getRelatedObject` 는 `getRelatedObjects(context, rel, "*", objectSelects, relSelects, !isFrom, isFrom, (short) 1, null, null)` 결과의 첫 행을 돌려주고, 행이 없으면 `null`. getRelatedObjects 호출 예: emxCommonDocumentUIBase_mxJPO.java:142-160
:::

## 검색 (findObjects)

타입, 이름, 조건으로 객체를 찾습니다. MQL `temp query` 와 같습니다.

| 형태 | 인자 |
| --- | --- |
| 5 개 | `findObjects(context, type, vault, where, selects)` |
| 9 개 | `findObjects(context, type, name, revision, owner, vault, where, expandType, selects)` |
| 11 개 | `findObjects(context, type, name, revision, owner, vault, where, queryName, expandType, selects, limit)` |

| 인자 | 값 |
| --- | --- |
| `type`, `name`, `revision`, `owner` | 패턴. `null` 이면 `"*"` |
| `vault` | `null` 이면 검색용 vault 설정 (`VaultUtil.getSearchVaultPattern`) |
| `where` | 조건식 (`"current==Release"`, `"revision==last"`). 없으면 `""` 또는 `null` |
| `expandType` | `true` 면 하위 타입 포함. 5 개 형태는 항상 `true` |
| `limit` | 최대 개수 (`short`). `0` 이면 제한 없음. 5, 9 개 형태는 `0` |
| `queryName` | 저장된 query 이름. 보통 `null` |

반환은 `MapList` 이고 행 Map 의 키는 `selects` 에 넣은 select 문자열입니다.

```java
MapList found = DomainObject.findObjects(context, typePart, "P-*", "*", "*", null, "revision==last", true,
        StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME));
```

::: tip where 를 꼭 좁힙니다
5 개, 9 개 형태는 개수 제한이 없습니다. 결과가 많을 수 있는 타입이면 where 로 좁히거나 11 개 형태의 `limit` 을 줍니다.
:::

::: info 근거
xerinfra.jar `DomainObject.findObjects` 오버로드 11 개는 모두 `(Context, type, name, revision, owner, vault, where, queryName, expandType, selects, limit, ...)` 형태로 모임. 5 개 형태는 name / revision / owner / queryName 에 `null`, expandType 에 `true`, limit 에 `0` 을 넘김. 안에서 `null` 패턴은 `"*"`, vault 가 `null` 이면 `VaultUtil.getSearchVaultPattern(context)` 를 쓰고 `matrix.db.Query` 로 실행
:::

## 관계 값 읽기

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `DomainRelationship.getInfo(context, relIds, selects)` | 여러 관계를 한 번에 (`from.id`, `to.id`, `attribute[X]` ...) | `MapList` |
| `DomainRelationship.getAttributeValue(context, relId, 속성)` | 관계 속성 하나 | `String` |
| `rel.getAttributeMap(context)` | 관계 속성 전부 | `Map` |
| `DomainRelationship.getAttributeSelect(속성)` | 관계 select 용 `attribute[속성]` | `String` |

```java
String qty = DomainRelationship.getAttributeValue(context, relId, attrQty);
MapList rels = DomainRelationship.getInfo(context, relIds, StringList.create(DomainConstants.SELECT_FROM_ID, DomainConstants.SELECT_TO_ID));
```

MQL 로 읽을 때는 `MqlUtil.mqlCommand(context, "print connection $1 select $2 dump", relId, "to.id")` 를 씁니다 ([MqlUtil](/jpo/mql-util)).
