# 객체 삭제, 연결 끊기 <Badge type="info" text="원본 분석" />

OOTB JPO 가 객체를 지우고 관계를 끊을 때 쓰는 메서드입니다. 객체는 id, 관계는 관계 id (`id[connection]`) 로 지정합니다.

::: info 근거
시그니처와 동작은 xerinfra.jar `DomainObject`, `DomainRelationship` class 파일 (javap) 기준. 건수는 jpo 폴더 바로 아래 1,615 개 파일에서 센 호출 수: `DomainRelationship.disconnect(context` 230, `.deleteObject(context` 56, `"disconnect ` 로 시작하는 MQL 29, `"delete bus ` 28, `DomainObject.deleteObjects(context` 23. 호출 예: ENOConfigureApprovalUIBase_mxJPO.java:533-538 (`DomainRelationship.disconnect` 뒤 `deleteObject`), emxChangeBase_mxJPO.java:3072 (`deleteObjects`), ENOQuestionUIBase_mxJPO.java:5550 (`disconnect(context, String[])`), emxAEFUtilBase_mxJPO.java:429 (`delete bus $1`)
:::

샘플: [XX_ObjectApi_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/XX_ObjectApi_mxJPO.java) 의 `deleteParts`, `removeDocuments`

## 객체 삭제

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `obj.deleteObject(context)` | 객체 하나를 지움. 안에서 트랜잭션을 열고 닫음 | `void` |
| `DomainObject.deleteObjects(context, ids)` | 여러 개를 한 트랜잭션으로 지움. 하나라도 실패하면 모두 되돌림 | `void` |
| `MqlUtil.mqlCommand(context, "delete bus $1", id)` | MQL 로 지움 | `String` |

```java
DomainObject.newInstance(context, objectId).deleteObject(context);
DomainObject.deleteObjects(context, idList.toStringArray());
```

- `delete(context)` 는 `deleteObject(context)` 와 같습니다.
- 삭제 권한이 없으면 예외가 납니다. 정책의 삭제 조건이나 트리거로 막힌 경우도 같습니다.
- 바깥에 트랜잭션이 있으면 그 안에 묶여서 바깥이 abort 되면 삭제도 되돌아갑니다 ([트랜잭션](/jpo/transaction-context#트랜잭션)).

::: info 근거
xerinfra.jar `DomainObject.deleteObject(context)` → `deleteObject(context, false)`: `openObject`, `startTransaction(context, true)`, `confirmDelete` 로 지울 목록을 만들고, 목록이 하나면 `remove(context)`, 여럿이면 목록마다 MQL `delete bus $1`, `commitTransaction`. 실패하면 `abortTransaction` 후 예외. `deleteObjects(context, String[])` 은 `ContextUtil.startTransaction` 한 번 안에서 id 마다 `setId` + `deleteObject`, 실패하면 `abortTransaction`
:::

## 연결 끊기

| 메서드 | 하는 일 | 반환 |
| --- | --- | --- |
| `DomainRelationship.disconnect(context, relId)` | 관계 하나를 끊음 | `void` |
| `DomainRelationship.disconnect(context, relIds)` | 여러 관계를 한 트랜잭션으로 끊음 | `void` |
| `DomainRelationship.disconnect(context, relId, preserve)` | MQL `disconnect connection $1` 로 끊음. `preserve` 가 `true` 면 `preserve` 를 붙임 | `void` |
| `MqlUtil.mqlCommand(context, "disconnect bus $1 relationship $2 from $3", toId, rel, fromId)` | 관계 id 없이 두 객체와 관계 이름으로 끊음 | `String` |

끊을 관계 id 는 조회할 때 같이 받아 둡니다.

| 받는 방법 | select |
| --- | --- |
| `getRelatedObjects` 의 관계 select | `DomainConstants.SELECT_RELATIONSHIP_ID` (`id[connection]`). 상대 객체와 짝으로 받음 |
| 객체의 select | `from[관계].id`, `to[관계].id`. 그 관계 전부 |

```java
StringList relIds = part.getInfoList(context, "from[" + relPartDocument + "].id");
if (!relIds.isEmpty()) {
    DomainRelationship.disconnect(context, relIds.toStringArray());
}
```

- 관계를 끊어도 양쪽 객체는 남습니다. 연결된 객체까지 지우려면 끊은 뒤 [객체 삭제](#객체-삭제) 를 따로 부릅니다.
- 관계를 끊지 않고 끝 객체만 바꾸려면 `DomainRelationship.setToObject` / `setFromObject` 를 씁니다 ([관계 수정](/jpo/api/write#관계-수정)).

::: info 근거
xerinfra.jar `DomainRelationship.disconnect(context, relId)` 는 `new DomainRelationship(relId).remove(context)`, `disconnect(context, String[])` 은 `ContextUtil.startTransaction` 안에서 id 마다 `remove`, `disconnect(context, relId, preserve)` 는 `MqlUtil.mqlCommand(context, "disconnect connection $1" 또는 "disconnect connection $1 preserve", relId)`. MQL 예: emxLibraryCentalDocumentSchemaMigrationBase_mxJPO.java:328 (`disconnect bus $1 relationship $2 from $3`)
:::
