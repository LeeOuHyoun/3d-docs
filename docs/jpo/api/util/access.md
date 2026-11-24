# 소유권 공유, 접근 권한 <Badge type="info" text="원본 분석" />

객체를 다른 조직, 협업공간, 사람과 함께 쓰게 하는 소유권 공유 (multiple ownership) 와, 로그인 사용자나 다른 사용자가 객체에 어떤 권한을 가졌는지 확인하는 메서드입니다. 예제의 `part` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다. util 전체를 하고 싶은 일로 찾으려면 [유틸리티](/jpo/api/utilities) 를 봅니다.

::: info 근거
xerinfra.jar (`DomainAccess`, `AccessUtil`, `FrameworkUtil`, `DomainObject`) class 파일 (javap) 기준. 건수는 OOTB JPO 에서 센 호출 수: `DomainAccess.createObjectOwnership(` 131, `DomainAccess.deleteObjectOwnership(` 57, `DomainAccess.getAccessSummaryList(` 22, `DomainAccess.getLogicalNames(` 21, `DomainAccess.hasObjectOwnership(` 15, `DomainAccess.createObjectOwnershipForUserGroups(` 15, `DomainAccess.getPhysicalAccessMasksForPolicy(` 15, `DomainAccess.getPhysicalAccessMasks(` 9, `DomainAccess.getOwnerAccessName(` 8, `DomainAccess.getReaderAccessName(` 4, `DomainAccess.clearMultipleOwnership(` 3, select `current.access[` 265, `FrameworkUtil.hasAccess(` 6, `AccessUtil.hasAccess(` 4, `.setPrimaryOwnership(` 47, `.revokeAccess(` 5
:::

샘플: [XX_UtilAccess_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilAccess_mxJPO.java) ([메서드별 설명](#샘플-jpo))

## 한눈에 보기

| 하고 싶은 일 | 쓸 메서드 |
| --- | --- |
| 객체를 다른 조직, 협업공간과 공유 | [`DomainAccess.createObjectOwnership(context, id, org, project, access, comment)`](#조직-협업공간과-공유) |
| 객체를 사람 한 명과 공유 | [`DomainAccess.createObjectOwnership(context, id, personId, access, comment)`](#사람과-공유) |
| 다른 객체의 권한을 물려받게 하기 | [`DomainAccess.createObjectOwnership(context, id, fromId, comment)`](#다른-객체의-권한-물려받기) |
| 공유 하나 거두기 | [`DomainAccess.deleteObjectOwnership(...)`](#공유-거두기) |
| 공유했는지 확인 | [`DomainAccess.hasObjectOwnership(...)`](#공유했는지-확인) |
| 공유 목록 읽기 | [`DomainAccess.getAccessSummaryList(context, id)`](#공유-목록-읽기) |
| 공유를 모두 지우기 | [`DomainAccess.clearMultipleOwnership(context, id)`](#공유를-모두-지우기) |
| 줄 수 있는 권한 이름, 그 실제 권한 | [`DomainAccess.getLogicalNames`, `getPhysicalAccessMasks`](#쓸-수-있는-권한-이름) |
| 로그인 사용자가 그 권한을 가졌는지 | [`FrameworkUtil.hasAccess(context, obj, "modify")`](#로그인-사용자의-권한-확인) |
| 다른 사람들이 그 권한을 가졌는지 | [`AccessUtil.hasAccess(context, id, persons, accesses)`](#다른-사용자의-권한-확인) |
| 객체 자체의 조직, 협업공간, 소유자 바꾸기 | [`obj.setPrimaryOwnership`, `obj.TransferOwnership`](#객체의-조직-협업공간-소유자-바꾸기) |
| grant 로 준 권한 거두기 | [`obj.revokeAccess(context, grantor, grantee)`](#위임한-권한-거두기) |

## 먼저 알아 둘 것

객체에 대한 권한은 아래 네 길로 생깁니다. 이 페이지의 `DomainAccess` 는 세 번째 길을 다룹니다.

| 길 | 내용 | 다루는 곳 |
| --- | --- | --- |
| 정책 규칙 | 상태마다 owner, public, user (역할) 규칙. 보안 컨텍스트의 조직, 협업공간 조건을 붙임 | [Policy 권한](/schema/policy#권한-access) |
| 객체의 조직, 협업공간 | 객체마다 하나 (select `organization`, `project`). 위 규칙의 `single organization`, `single project` 조건이 이것과 비교됨 | [객체의 조직, 협업공간, 소유자 바꾸기](#객체의-조직-협업공간-소유자-바꾸기) |
| 소유권 공유 | 객체에 `조직 \| 협업공간 \| comment` 와 권한 목록을 여러 개 더함 (MQL `add ownership`). 사람은 개인 협업공간 `<이름>_PRJ`, 다른 객체를 가리키는 항목도 됨 | 이 페이지 |
| 권한 위임 (grant) | 사람에게 grantor 이름으로 권한을 줌 | [위임한 권한 거두기](#위임한-권한-거두기) |

소유권 공유 항목은 이렇게 생겼습니다.

| 부분 | 값 | 비었을 때 |
| --- | --- | --- |
| 조직 | 조직 이름 (`MyOrg`) | `null`, `""`, `"ADMINISTRATION"` 은 모두 `-` 로 바꿔 넘김 |
| 협업공간 | 협업공간 이름 (`MyCollabSpace`), 사람이면 `<사람 이름>_PRJ` | 위와 같음 |
| comment | 항목을 구별하는 글자. OOTB 상수 `DomainAccess.COMMENT_MULTIPLE_OWNERSHIP` (`Multiple Ownership For Object`) 를 가장 많이 씀 | `null` 은 `""` |
| 권한 | `Read` 같은 논리 이름, 또는 `read,show` 같은 실제 권한 목록 ([쓸 수 있는 권한 이름](#쓸-수-있는-권한-이름)) | |

- 조직, 협업공간이 같아도 comment 가 다르면 다른 항목입니다. 지우거나 확인할 때는 만들 때와 같은 comment 를 넘깁니다.
- 공유 항목이 그 조직, 협업공간으로 일하는 사용자에게 권한을 더한다는 것은 서버 쪽 동작이라 class 파일로 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />. 프로젝트 메모에는 "revoke 로 권한이 제거되더라도 object ownership 을 주면 권한을 줄 수 있음" 이라고 적혀 있습니다 ([revoke](/schema/policy#revoke)).
- `add ownership` 를 실행할 수 있는 권한 (select `current.access[changesov]` 로 보임) 이 없으면 MQL 오류가 날 것으로 보입니다 <Badge type="danger" text="검증 필요" />. 이 페이지의 메서드는 대부분 로그인 사용자로 실행합니다. 슈퍼유저로 해야 하면 `pushContext` 를 받는 형태를 쓰거나 [pushContext](/jpo/api/util/transaction) 로 감쌉니다.

| 상수 (로컬 실행으로 확인) | 값 |
| --- | --- |
| `DomainAccess.COMMENT_MULTIPLE_OWNERSHIP` | `Multiple Ownership For Object` |
| `DomainAccess.COMMENT_OWNER_ACCESS` | `Owner Access` |
| `DomainAccess.COMMENT_GRANTING_ACCESS` | `Granting Access For Object` |
| `DomainAccess.COMMENT_MULTIPLE_OWNERSHIP_GRANTED` | `Access Granted Through` |

## 조직, 협업공간과 공유

객체를 다른 조직이나 협업공간 사람들도 쓸 수 있게 공유 항목을 더합니다. 데이터를 바꿉니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainAccess.createObjectOwnership(context, objectId, organization, project, access, comment)` | `objectId`: 공유할 객체. `organization`, `project`: 조직, 협업공간 이름 (한쪽만 줘도 됨). `access`: 권한 ([권한 이름](#쓸-수-있는-권한-이름)). `comment`: 항목 구별 글자 | `void` |
| `DomainAccess.createObjectOwnership(context, objectId, organization, project, access, comment, replace)` | `replace`: `true` 면 같은 항목이 이미 있을 때 권한을 바꿈. 위 형태는 `false` | `void` |

```java
DomainAccess.createObjectOwnership(context, objectId, "MyOrg", "MyCollabSpace", "Read",
        DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
```

- 실행하는 MQL 은 `modify bus <objectId> add ownership <조직> <협업공간> for <comment> as <권한1>, <권한2> ...` 입니다. 권한은 먼저 객체의 정책으로 실제 권한 목록으로 바꾼 뒤 하나씩 `$5`, `$6` ... 으로 넘깁니다 (`print bus <id> select policy dump` 한 번).
- `replace` 가 `true` 이고 같은 `조직 | 협업공간 | comment` 항목이 있으면, MQL 하나로 지금 권한을 `remove ownership` 하고 새 권한을 `add ownership` 합니다. 이때 `read`, `show` 는 빼고 바꾸므로 원래 있던 `read`, `show` 는 남습니다.
- `replace` 가 `false` 면 항목이 있는지 보지 않고 `add ownership` 합니다.
- 로그인 사용자로 실행하고 트랜잭션은 열지 않습니다. 여러 객체를 함께 바꾸면 [트랜잭션](/jpo/api/util/transaction) 으로 묶습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `DomainAccess.createObjectOwnershipForUserGroups(context, objectId, group, access, comment)` | 안에서 `createObjectOwnership(context, objectId, null, group, access, comment, false)` 를 그대로 부름 (사용자 그룹 이름을 협업공간 자리에 넣음) |
| `DomainAccess.createObjectOwnershipMultiple(context, objectId, rows, replace, policy, ownerships)` | 여러 항목을 MQL 하나로 더함. 행 Map 의 키는 `org`, `proj`, `comment`, `access`. OOTB 1 건 |
| `DomainAccess.createObjectOwnership(context, objectId, organization, project, access, comment, replace, policy, ownerships)` | 정책 이름과 지금 항목 목록을 미리 넘겨 조회를 줄이는 형태. OOTB 1 건 |
| `obj.setPrimaryOwnership(context, project, organization)` | 공유를 더하지 않고 객체 자체의 협업공간, 조직을 바꿈 ([setPrimaryOwnership](/jpo/api/domain-object#setprimaryownership)) |

::: info 근거
xerinfra.jar `DomainAccess.createObjectOwnership(Context, String, String, String, String, String)` 은 같은 인자에 `false` 를 붙여 7 개 형태를 부름. 7 개 형태: 조직, 협업공간이 `null`, `""`, `"ADMINISTRATION"` 이면 `-`, comment 가 `null` 이면 `""`. `getPhysicalAccessMasks(context, objectId, access)` 로 실제 권한을 얻고, private `getObjectOwnership` 으로 `ownership[조직|협업공간|comment]` 와 `...access` 를 읽음. `replace` 가 참이고 앞의 값이 `true` 면 private `deleteAndCreateObjectOwnership` (`modify bus $1` + ` remove ownership $2 $3 for $4 as ...` + ` add ownership ... as ...`, 양쪽 권한에서 private `removeReadShowAccess` 로 `read`, `show` 를 뺌). 아니면 `"modify bus $1 add ownership $2 $3 for $4 as"` 에 권한마다 ` $n` 과 `,` 를 붙여 (recipe `\u0001 $\u0001`, `\u0001,`) `MqlUtil.mqlCommand(context, 명령, 인자 List)`. `createObjectOwnershipForUserGroups` 는 `createObjectOwnership(context, objectId, null, group, access, comment, false)`. 로컬 실행: `COMMENT_*` 상수 값. OOTB 예: emxDomainAccessBase_mxJPO.java, emxRouteTemplateBase_mxJPO.java
:::

## 사람과 공유

객체를 사람 한 명과 공유합니다. 담당자, 검토자에게 권한을 줄 때 OOTB 가 가장 많이 쓰는 형태입니다. 데이터를 바꿉니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainAccess.createObjectOwnership(context, objectId, personId, access, comment)` | `personId`: 사람의 Person 객체 id ([Person 객체 id](/jpo/api/util/person)). 나머지는 [위](#조직-협업공간과-공유) 와 같음 | `void` |
| `DomainAccess.createObjectOwnership(context, objectId, personId, access, comment, replace, pushContext)` | `replace`: 위와 같음. `pushContext`: `true` 면 슈퍼유저로 실행하고 끝나면 되돌림 | `void` |

```java
String personId = PersonUtil.getPersonObjectID(context, "gdhong");
DomainAccess.createObjectOwnership(context, objectId, personId, "Read", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
```

- 안에서 Person 객체의 이름을 읽어, 조직 없이 협업공간 `<이름>_PRJ` 로 [조직, 협업공간과 공유](#조직-협업공간과-공유) 의 형태를 부릅니다. 그래서 지우거나 확인할 때는 협업공간에 `<이름>_PRJ` 를 넘깁니다 ([공유 거두기](#공유-거두기)).
- OOTB 에는 지우기 전에 `<이름>` 과 `<이름>_PRJ` 를 둘 다 확인하는 곳이 있습니다 (emxDomainAccessBase_mxJPO.java).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `DomainAccess.createObjectOwnership(context, objectId, personId, access, comment, replace)` | `pushContext` 없는 형태. 위 7 개 인자 형태가 `false` 일 때 이것을 부름 |
| `DomainAccess.createObjectOwnership(context, new String[] {objectId, personId, access, comment})` | 배열로 넘기는 형태. 안에서 같은 일 |
| `DomainAccess.createObjectOwnership(context, objectId, "", userName + "_PRJ", access, comment)` | 결과가 같음. 사람 이름을 이미 알 때 Person 조회를 한 번 줄임 |

::: info 근거
xerinfra.jar `DomainAccess.createObjectOwnership(Context, String, String, String, String)` 과 `(…, boolean)`: `DomainObject.newInstance(context, personId).getInfo(context, "name") + "_PRJ"` (recipe `\u0001_PRJ`) 를 협업공간, `null` 을 조직으로 `createObjectOwnership(context, objectId, null, 이름_PRJ, access, comment, replace)`. `(…, boolean, boolean)` 은 마지막이 참이면 `ContextUtil.pushContext` 뒤 6 개 형태를 부르고 `popContext` (예외 때도). `(Context, String[])` 은 길이가 4 일 때만 같은 일. OOTB 예: emxDomainAccessBase_mxJPO.java, emxProgramCentralUtilBase_mxJPO.java
:::

## 다른 객체의 권한 물려받기

폴더 안 문서처럼, 한 객체가 다른 객체 (`fromId`) 를 가리키는 공유 항목을 더합니다. 데이터를 바꿉니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainAccess.createObjectOwnership(context, objectId, fromId, comment)` | `objectId`: 권한을 받을 객체. `fromId`: 권한을 줄 객체. `comment`: 항목 구별 글자 (`null` 은 `""`) | `void` |
| `DomainAccess.createObjectOwnership(context, objectId, fromId, comment, runAsUserAgent)` | `runAsUserAgent`: `true` 면 그 MQL 만 슈퍼유저로. 위 형태는 `false` | `void` |

```java
DomainAccess.createObjectOwnership(context, doc.getObjectId(context), objectId, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
```

- 실행하는 MQL 은 `modify bus <objectId> add ownership bus <fromId> for <comment>` 입니다. `objectId` 의 정책에 `DomainAccess` 페이지의 `accessMasksInheritanceLimit` 값이 있으면 끝에 `as <그 값>` 을 붙입니다.
- 넘어오는 권한의 범위는 서버 쪽 동작이라 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.
- 4 개 인자 형태의 세 번째 값은 **객체 id** 입니다. 같은 이름의 5 개 인자 형태 (사람과 공유) 와 인자 수만 다르니 섞지 않습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `DomainAccess.createObjectOwnershipwithPolicy(context, objectId, fromId, comment, policy)` | 정책 이름을 넘겨 `print bus ... select policy` 를 줄인 형태. 안에서 `createObjectOwnership(context, objectId, fromId, comment, false, policy)` 를 부름 |

::: info 근거
xerinfra.jar `DomainAccess.createObjectOwnership(Context, String, String, String)` 은 `false` 를 붙여 5 개 형태를 부름. 5 개 형태: protected `getAccessMasksInheritanceLimit(context, objectId)` (`print bus $1 select policy dump` 를 슈퍼유저로 읽고 `DomainAccess` 페이지 설정의 `accessMasksInheritanceLimit`) 가 비지 않으면 `"modify bus $1 add ownership bus $2 for $3 as $4"` (recipe `\u0001 as $4`), 아니면 `"modify bus $1 add ownership bus $2 for $3"` 를 `MqlUtil.mqlCommand(context, 명령, runAsUserAgent, ...)`. 페이지 설정은 `print page $1 select content dump` (`DomainAccess`) 의 XML 을 읽어 `CacheUtil` (`policy_logical_mappings`) 에 담아 둠. OOTB 예: emxDomainAccessBase_mxJPO.java
:::

## 공유 거두기

공유 항목 하나를 지웁니다. 데이터를 바꿉니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainAccess.deleteObjectOwnership(context, objectId, organization, project, comment)` | 만들 때와 같은 조직, 협업공간, comment. 사람이면 조직 `""`, 협업공간 `<이름>_PRJ` | `void` |
| `DomainAccess.deleteObjectOwnership(context, objectId, fromId, comment)` | [물려받기](#다른-객체의-권한-물려받기) 로 만든 항목. 만들 때와 같은 `fromId`, comment | `void` |

```java
DomainAccess.deleteObjectOwnership(context, objectId, "", "gdhong_PRJ", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
DomainAccess.deleteObjectOwnership(context, doc.getObjectId(context), objectId, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
```

- 먼저 [공유했는지 확인](#공유했는지-확인) 으로 항목이 있는지 보고, 있을 때만 `modify bus <id> remove ownership <조직> <협업공간> for <comment>` / `modify bus <id> remove ownership bus <fromId> for <comment>` 를 실행합니다. 없으면 아무것도 하지 않습니다 (예외 없음).
- 두 형태 모두 마지막에 `boolean` 을 더한 형태가 있습니다. `true` 면 그 MQL 만 슈퍼유저로 실행합니다. 위 형태는 `false` 입니다.
- 물려받은 항목을 지우는 형태는 MQL 결과를 `System.out` 에 찍습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `obj.removePrimaryOwnership(context)` | 공유 항목이 아니라 객체 자체의 조직, 협업공간을 비움 ([removePrimaryOwnership](/jpo/api/domain-object#removeprimaryownership)) |

::: info 근거
xerinfra.jar `DomainAccess.deleteObjectOwnership(Context, String, String, String, String)` 은 `false` 를 붙여 부름. 6 개 형태: 조직, 협업공간, comment 를 만들 때처럼 바꾼 뒤 `hasObjectOwnership(context, objectId, 조직, 협업공간, comment)` 가 참이면 `mqlCommand(context, "modify bus $1 remove ownership $2 $3 for $4", boolean, ...)`. `(Context, String, String, String)` 은 `false` 를 붙여 부르고, 5 개 형태는 `hasObjectOwnership(context, objectId, fromId, comment)` 가 참이면 `System.out.println(mqlCommand(context, "modify bus $1 remove ownership bus $2 for $3", boolean, ...))`. 예외는 앞의 두 형태가 `Exception`, 뒤의 두 형태가 `FrameworkException`
:::

## 공유했는지 확인

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainAccess.hasObjectOwnership(context, objectId, organization, project, comment)` | 만들 때와 같은 조직, 협업공간, comment | `boolean`. 그 항목이 있으면 `true` |
| `DomainAccess.hasObjectOwnership(context, objectId, fromId, comment)` | 물려받기 항목의 `fromId`, comment | `boolean` |

```java
boolean shared = DomainAccess.hasObjectOwnership(context, objectId, "", "gdhong_PRJ",
        DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
```

- 실행하는 MQL 은 `print bus <id> select ownership[<조직>|<협업공간>|<comment>] dump` (물려받기는 `ownership[<fromId>|<comment>]`) 이고, 결과가 `true` (대소문자 무시) 인지 봅니다. 조직, 협업공간은 만들 때처럼 `-` 로 바꿔 넣습니다.
- 그 항목의 권한까지 보려면 select `ownership[<조직>|<협업공간>|<comment>].access` 를 읽습니다.
- 로그인 사용자로 읽습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `DomainAccess.getObjectOwnershipAccess(context, objectId, organization, project, comment)` | 있는지가 아니라 위 `.access` select 값을 돌려줌. OOTB 미사용 |

::: info 근거
xerinfra.jar `DomainAccess.hasObjectOwnership(Context, String, String, String, String)`: 조직, 협업공간이 `null`, `""`, `"ADMINISTRATION"` 이면 `-`, comment `null` 은 `""`. `mqlCommand(context, "print bus $1 select $2 dump", objectId, "ownership[" + 조직 + "|" + 협업공간 + "|" + comment + "]")` (recipe `ownership[\u0001|\u0001|\u0001]`) 가 `"true".equalsIgnoreCase`. 4 개 형태는 `ownership[\u0001|\u0001]`. `getObjectOwnershipAccess` 는 `ownership[\u0001|\u0001|\u0001].access`
:::

## 공유 목록 읽기

객체에 붙은 공유 항목을 화면의 공유 (Access) 표와 같은 모양으로 읽습니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainAccess.getAccessSummaryList(context, objectId)` | `objectId`: 객체 id | `MapList`. 항목마다 Map 하나. 객체가 없거나 보이지 않으면 (`exists` 가 `true` 가 아니면) 빈 목록 |

| 행 Map 의 키 | 값 |
| --- | --- |
| `id` | `객체id:조직:협업공간:comment`. `:` 로 잘라 [deleteObjectOwnership](#공유-거두기) 에 넘길 수 있음 (OOTB 가 이렇게 씀) |
| `org`, `project` | 조직, 협업공간 이름 |
| `orgTitle`, `projectTitle` | 화면 표시용 이름 |
| `name` | 화면 표시용 이름 (조직과 협업공간을 이은 것) |
| `access` | 권한. 정책의 논리 이름으로 바꿀 수 있으면 논리 이름, 아니면 실제 권한 목록 |
| `comment` | comment |
| `TYPE`, `username`, `inheritedId`, `inheritedName`, `inheritedAccess`, `level`, `hasChildren`, `disableSelection`, `accessLen`, `accessId` | 표 그리기용 값 |

```java
MapList rows = DomainAccess.getAccessSummaryList(context, objectId);
for (Object o : rows) {
    Map row = (Map) o;
    String org = (String) row.get(DomainAccess.KEY_ACCESS_ORG);
    String project = (String) row.get(DomainAccess.KEY_ACCESS_PROJECT);
    String access = (String) row.get(DomainAccess.KEY_ACCESS_GRANTED);
}
```

- 키 이름은 상수 `DomainAccess.KEY_ACCESS_ID` (`id`), `KEY_ACCESS_ORG` (`org`), `KEY_ACCESS_PROJECT` (`project`), `KEY_ACCESS_GRANTED` (`access`), `KEY_ACCESS_COMMENT` (`comment`) 로도 씁니다 (로컬 실행으로 값 확인).
- 객체 자신의 조직, 협업공간도 comment `Primary`, 권한 `All` 인 항목으로 목록 앞에 넣고 시작합니다. 같은 이름의 행을 합치는 처리가 있어 행 수가 항목 수와 다를 수 있습니다.
- 표를 그리는 메서드라 행마다 `temp query bus` 로 그룹, 협업공간 객체를 찾고, 사람 항목은 [getFullName](/jpo/api/util/person) 을 부릅니다. 값만 필요하면 MQL `print bus <id> select ownership dump ,` 가 가볍습니다. 결과는 항목마다 `조직|협업공간|comment` (물려받기는 `fromId|comment`) 를 `,` 로 이은 것입니다 ([공유를 모두 지우기](#공유를-모두-지우기) 가 이렇게 읽음).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `DomainAccess.getAccessSummaryList(context, objectId, boolean)` | 위 형태가 `false` 로 부르는 형태. `true` 면 물려받은 항목을 합치는 처리가 더 있음 |
| `DomainAccess.getOwnershipAccessBasedOnOneLevel(context, objectId)` | 물려받은 항목을 한 단계만 펼쳐 읽는 다른 목록. OOTB 2 건 |

::: info 근거
xerinfra.jar `DomainAccess.getAccessSummaryList(Context, String, boolean)`: `readXMLPolicyDomainAccessSettings` 뒤 private `getAccessSummaryList` 결과를 `name` 오름차순, `accessLen` 내림차순으로 정렬해 같은 이름의 행을 합침. private 형태: `print bus $1 select exists dump` 가 `true` 일 때 `print bus $1 select type  name revision policy owner organization project $2 ... $10 dump $11` (`ownership.businessobject`, `id`, `ownership.businessobjectcount`, `ownership.organizationcount`, `ownership.organization`, `ownership.project`, `ownership.access`, `ownership.comment`, `ownership.project.isaprojectgroup`, `|`) 를 읽고, 객체 자신의 조직, 협업공간을 `All`, `Primary` 로 목록 0 번에 넣음. 행마다 `id` 는 recipe `\u0001:\u0001:\u0001:\u0001` (객체 id, 조직, 협업공간, comment), `access` 는 `getLogicalName(context, 정책, 권한, ...)` 의 결과 Map (`access`, `accessId`) 을 `putAll`. emxDomainAccessBase_mxJPO.java 는 표의 행 id 를 `:` 로 잘라 `deleteObjectOwnership(context, busId, organization, project, comment)` 에 넘김
:::

## 공유를 모두 지우기

객체의 공유 항목 (물려받기 포함) 을 모두 지웁니다. 데이터를 바꿉니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainAccess.clearMultipleOwnership(context, objectId)` | `objectId`: 객체 id | `void` |

```java
DomainAccess.clearMultipleOwnership(context, objectId);
```

- 트랜잭션을 열고 `print bus <id> select ownership dump ,` 의 항목마다 칸이 셋 이상이면 `deleteObjectOwnership(context, id, 조직, 협업공간, comment)`, 둘이면 `deleteObjectOwnership(context, id, fromId, comment)` 를 부릅니다. 하나라도 실패하면 모두 되돌리고 `FrameworkException` 입니다.
- 객체 자신의 조직, 협업공간은 그대로 둡니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `DomainAccess.clearInheritedOwnership(context, objectId)` | 물려받기 항목 (칸이 둘) 만 지움 |

::: info 근거
xerinfra.jar `DomainAccess.clearMultipleOwnership(Context, String)`: `ContextUtil.startTransaction(context, true)`, `mqlCommand(context, "print bus $1 select ownership dump $2", objectId, ",")` 를 `StringUtil.split(값, ",")`, 항목마다 `StringUtil.split(항목, "|")` 의 크기가 3 이상이면 5 개 인자, 2 이상이면 4 개 인자 `deleteObjectOwnership`. 끝에 `commitTransaction`, 예외면 `abortTransaction` 후 `FrameworkException`. `clearInheritedOwnership` 은 크기가 2 인 항목만
:::

## 쓸 수 있는 권한 이름

공유할 때 넘기는 권한은 보통 `Read`, `Full`, `Project Lead` 같은 **논리 이름** 입니다. 논리 이름과 실제 권한 (`read,show,...`) 의 짝은 정책마다 다르고, 페이지 객체 `DomainAccess` 의 XML 에 들어 있습니다 (분석한 원본에는 이 페이지가 없음).

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainAccess.getLogicalNames(context, objectId)` | `objectId`: 객체 id | `StringList`. 그 객체의 정책에 정해 둔 논리 이름 목록 (정책이 없으면 `Default` 의 목록) |
| `DomainAccess.getReaderAccessName(context, objectId)` | 위와 같음 | `String`. 위 목록의 첫 이름 (가장 약한 권한으로 씀) |
| `DomainAccess.getOwnerAccessName(context, objectId)` | 위와 같음 | `String`. 위 목록의 마지막 이름 (가장 강한 권한으로 씀) |
| `DomainAccess.getPhysicalAccessMasks(context, objectId, access)` | `access`: 논리 이름 | `String`. 실제 권한 목록 (`read,show,...`). 그 정책에 없는 이름이면 넘긴 글자에서 `", "` 를 `","` 로 바꿔 그대로 돌려줌 |

```java
String readerAccess = DomainAccess.getReaderAccessName(context, objectId);
String masks = DomainAccess.getPhysicalAccessMasks(context, objectId, readerAccess);
DomainAccess.createObjectOwnership(context, objectId, "MyOrg", "MyCollabSpace", readerAccess, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
```

- 논리 이름 대신 실제 권한 목록 (`"read,show"`) 을 넘겨도 됩니다. 정책에 없는 이름은 실제 권한 목록으로 보기 때문입니다. OOTB 도 `"read,show"` 를 넘기는 곳이 있습니다 (emxCommonIssueBase_mxJPO.java).
- 매핑 XML 은 처음 한 번 `print page DomainAccess select content dump` 로 읽어 서버 전체 캐시 (`CacheUtil`) 에 담아 둡니다.
- Workspace, Document 계열 정책에서는 예전 이름을 새 이름으로 바꿔 찾습니다. 그 변환 결과는 아래와 같습니다 (로컬 실행으로 확인).

| 넘긴 이름 | 바꿔 찾는 이름 |
| --- | --- |
| `Full` | `Owner` |
| `Read Write`, `Add`, `Remove`, `Add Remove` | `Author` |
| `Read` | `Reader` |
| `Basic` | `Viewer` |
| 그 밖 (`Project Lead`, `read,show`, `FULL` ...) | 그대로 |

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `DomainAccess.getObjectLogicalAccessRange(context, objectId)` | 같은 목록. 정책을 MQL 로 읽고, 목록이 없으면 빈 `ArrayList` |
| `DomainAccess.getLogicalNamesForPolicy(context, policy)` | 정책 이름으로 받는 형태. `getLogicalNames` 가 안에서 부름 |
| `DomainAccess.getPhysicalAccessMasksForPolicy(context, policy, access)`, `getPhysicalAccessMasks(context, objectId, access, policy)` | 정책 이름으로 받는 형태. `getPhysicalAccessMasks` 가 안에서 부름 |
| `DomainAccess.getLogicalName(context, objectId, masks)` | 반대 방향 (실제 권한 목록 → 논리 이름) |

::: info 근거
xerinfra.jar `DomainAccess.getLogicalNames(Context, String)` 은 `DomainObject.newInstance(context, objectId).getInfo(context, "policy")` 를 `getLogicalNamesForPolicy` 에 넘김. 그 메서드는 private `readXMLPolicyDomainAccessSettings` (Map) 에서 정책 → 없으면 `Default` 의 `logicalNamesList`. `getReaderAccessName` 은 `get(0)`, `getOwnerAccessName` 은 `get(size() - 1)` (목록이 비거나 없으면 예외). `getPhysicalAccessMasks(Context, String, String)` 은 `print bus $1 select policy dump` 뒤 `getPhysicalAccessMasksForPolicy`: 정책 → 없으면 `Default` 의 Map 에서 이름을 찾고, 없으면 `access.replaceAll(", ", ",")` (상수 바이트 `2c 20`). 정책이 `Workspace`, `Workspace Vaults`, `Workspace Template`, `Template Workspace Vault`, `Document Release`, `Document` 또는 페이지의 `Controlled Folder` 묶음이면 먼저 `updateLogicalMappingforDefaultTypes`. 로컬 실행: `DomainConstants.DOMAIN_ACCESS_MASK` 의 `toString()` 이 `Basic`, `Read`, `Read Write`, `Add`, `Remove`, `Add Remove`, `Full`, `Viewer`, `Reader`, `Author`, `Owner` 이고, `updateLogicalMappingforDefaultTypes` 가 위 표의 값을 돌려줌. OOTB 예: emxDomainAccessBase_mxJPO.java (`getReaderAccessName`, `getLogicalNames`), emxProjectFolderBase_mxJPO.java (`getOwnerAccessName`)
:::

## 로그인 사용자의 권한 확인

로그인 사용자가 지금 상태에서 그 객체에 그 권한 (`modify`, `fromconnect` ...) 을 가졌는지 봅니다. 권한 이름은 [Policy 권한 목록](/schema/policy#권한-목록) 의 이름입니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.hasAccess(context, obj, access)` | `obj`: 객체 (`DomainObject` 도 됨). `access`: 권한 이름 하나 (`"modify"`) | `boolean` |
| select `current.access[modify]` | 목록을 읽을 때 select 에 더함 | `TRUE` / `FALSE` 문자열 |

```java
boolean canModify = FrameworkUtil.hasAccess(context, part, "modify");

MapList children = part.getRelatedObjects(context, relStructure, "*",
        StringList.create(DomainConstants.SELECT_ID, "current.access[modify]"), null, false, true, (short) 1, null, null, 0);
```

- `FrameworkUtil.hasAccess` 는 MQL `print bus <id> select current.access[<권한>] dump` 하나를 실행하고 `TRUE` 인지 봅니다. 권한 이름은 소문자로 바꿔 넣습니다.
- 객체 하나는 `hasAccess`, 여러 객체를 읽는 [getRelatedObjects](/jpo/api/domain-object#getrelatedobjects), [getInfo](/jpo/api/domain-object#getinfo) 에서는 select 를 더해 서버 호출을 늘리지 않습니다. 값은 대소문자를 가리지 않고 `"true".equalsIgnoreCase(값)` 으로 봅니다.
- `pushContext` 안에서는 User Agent 의 권한이 나옵니다. 실제 사용자의 권한은 push 전에 봅니다.
- 객체 id 가 없는 객체 (타입, 이름, 리비전으로 만든 것) 면 한 번 실패한 뒤 객체를 열어 다시 묻습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `FrameworkUtil.hasAccess(context, obj, accessList)` | 권한 목록을 `,` 로 이어 `current.access[read,modify]` 하나로 물음. 여러 권한의 판정 방식은 <Badge type="danger" text="검증 필요" /> |
| `FrameworkUtil.hasAccessForStateName(context, obj, state, access)` | 지금 상태가 아닌 그 상태 (`state[상태].access`) 의 권한. OOTB 미사용 |
| `AccessUtil.hasReadAccess(obj.getAccessMask(context))` 등 (`hasReadWriteAccess`, `hasAddAccess`, `hasRemoveAccess`, `hasAddRemoveAccess`, `hasWorkspaceLeadAccess`, `hasContentRemoveAccess`) | `matrix.db.Access` 의 권한이 Workspace 권한 묶음을 모두 가졌는지. 예: `hasReadWriteAccess` 는 `read`, `show`, `checkout`, `checkin`, `modify`, `lock` |
| `AccessUtil.checkAccess(access)` | 위 묶음 가운데 가장 큰 것의 이름 (`Workspace Lead`, `Add Remove`, `Remove`, `Add`, `Read Write`, `Read`, `None`) |

::: info 근거
xerinfra.jar `FrameworkUtil.hasAccess(Context, BusinessObject, String)`: `mqlCommand(context, "print bus $1 select $2 dump", bo.getObjectId(), "current.access[" + access.toLowerCase() + "]")` (recipe `current.access[\u0001]`), `MatrixException` 이면 `bo.open` 뒤 다시, `trim().equalsIgnoreCase("TRUE")`. `StringList` 형태는 `StringUtil.join(list, ",")`. `hasAccessForStateName` 은 `state[\u0001].access` 를 읽어 `All` 이나 그 권한이 있는지. `AccessUtil.hasReadAccess(Access)` 는 `hasReadAccess`, `hasShowAccess`, `hasCheckoutAccess`, `hasReadWriteAccess` 는 거기에 `hasCheckinAccess`, `hasModifyAccess`, `hasLockAccess`. `checkAccess(Access)` 는 큰 묶음부터 차례로 확인. OOTB 예: emxCommonPartBase_mxJPO.java (`FrameworkUtil.hasAccess`), emxLibraryCentralCommonBase_mxJPO.java (`AccessUtil.hasReadWriteAccess(doObj.getAccessMask(context))`)
:::

## 다른 사용자의 권한 확인

알림을 보내기 전처럼, 여러 사람이 객체에 그 권한을 가졌는지 한 번에 봅니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `AccessUtil.hasAccess(context, objectId, persons, accesses)` | `persons`: 사람 이름 `ArrayList`. `accesses`: 권한 이름 `ArrayList` (`"read"` ...) | `ArrayList`. 권한이 **없는** 사람 이름. 모두 가졌으면 빈 목록 |

```java
ArrayList persons = new ArrayList(FrameworkUtil.split("gdhong,kslee", ","));
ArrayList accesses = new ArrayList();
accesses.add("read");
ArrayList withoutRead = AccessUtil.hasAccess(context, objectId, persons, accesses);
```

- 사람마다 그 사람으로 `pushContext` 한 뒤 `print bus <id> select exists dump |` 가 `FALSE` 면 권한 없음, `current.access` 가 `all` 이면 권한 있음, 아니면 `current.access` 글자에 각 권한 이름이 들어 있는지 (`indexOf`) 봅니다. 끝나면 `popContext` 합니다.
- 권한 이름을 글자 포함으로 찾으므로, 다른 권한 이름의 일부가 되는 이름을 넘기지 않습니다.
- 사람 수만큼 context 를 바꾸고 MQL 을 두 번씩 실행합니다.

::: info 근거
xerinfra.jar `AccessUtil.hasAccess(Context, String, ArrayList, ArrayList)`: `ContextUtil.pushContext(context)` 뒤 사람마다 `pushContext(context, 사람, "", context.getVault().getName())`, `mqlCommand(context, "print bus $1 select $2 dump $3", false, objectId, "exists", "|")` 가 `FALSE` 면 목록에 더함. 아니면 `current.access` 가 `all` (trim, 대소문자 무시) 이면 통과, 아니면 권한마다 `indexOf` 가 0 보다 작으면 목록에 더함. 사람마다 `popContext`, 끝에 한 번 더 `popContext`. 예외는 `FrameworkException(e.toString())`. OOTB 예: emxCommonSubscriptionBase_mxJPO.java, emxSubscriptionUtilBase_mxJPO.java
:::

## 객체의 조직, 협업공간, 소유자 바꾸기

공유 항목을 더하지 않고 객체 자체의 조직, 협업공간 (primary ownership) 이나 소유자를 바꾸는 일은 `DomainObject` 메서드가 합니다. 데이터를 바꿉니다.

| 하고 싶은 일 | 쓸 메서드 | MQL |
| --- | --- | --- |
| 조직, 협업공간 바꾸기 | [`obj.setPrimaryOwnership(context, project, organization)`](/jpo/api/domain-object#setprimaryownership) | `mod bus $1 project $2 organization $3` (슈퍼유저로) |
| 조직, 협업공간 비우기 | [`obj.removePrimaryOwnership(context)`](/jpo/api/domain-object#removeprimaryownership) | 위 명령에 `""`, `""` |
| 소유자, 협업공간, 조직을 한 번에 | [`obj.TransferOwnership(context, owner, project, organization)`](/jpo/api/domain-object#transferownership) | `mod bus $1 project $2 organization $3 owner $4` |

```java
String user = context.getUser();
part.setPrimaryOwnership(context, PersonUtil.getDefaultProject(context, user), PersonUtil.getDefaultOrganization(context, user));
```

- 인자 순서는 협업공간이 먼저입니다. 값은 [지금 보안 컨텍스트](/jpo/api/util/person), [기본 보안 컨텍스트](/jpo/api/util/person) 에서 얻습니다.
- 지금 값은 select `organization`, `project` (`DomainAccess.SELECT_PROJECT` 는 `project`) 로 읽습니다.

## 위임한 권한 거두기

소유권 공유와 다른 길인 grant (grantor 가 grantee 에게 권한을 줌) 로 준 권한은 `DomainObject.revokeAccess` 로 거둡니다. 데이터를 바꿉니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| [`obj.revokeAccess(context, grantor, grantee)`](/jpo/api/domain-object#revokeaccess) | `grantor`, `grantee`: 준 사람, 받은 사람. 하나만 넘기거나 둘 다 `null` 도 됨 | `void`. 거둘 권한이 없으면 아무것도 안 함 |

- grant 를 주는 일은 OOTB JPO 가 `BusinessObject.grantAccessRights` (21 건) 로 합니다. 이 페이지에서는 다루지 않습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `AccessUtil.revokeAccess(context, objects, grantors)` | 슈퍼유저로 `BusinessObject.revokeAccessRights` 를 부름. OOTB 미사용 |
| `AccessUtil.grantAccess(context, objects, accessList, grantor)` | 주는 쪽. `grantor` 로 (비면 슈퍼유저로) `BusinessObject.grantAccessRights`. OOTB 미사용 |

::: info 근거
xerinfra.jar `AccessUtil.revokeAccess(Context, BusinessObjectList, StringList)` 는 `pushContext` 뒤 `BusinessObject.revokeAccessRights`, `popContext`. `grantAccess(Context, BusinessObjectList, AccessList, String)` 은 grantor 가 있으면 `pushContext(context, grantor, "", vault)`, 없으면 `pushContext(context)` 뒤 `BusinessObject.grantAccessRights`. `DomainObject` 의 세 메서드는 [소유권, 접근 권한](/jpo/api/domain-object#소유권-접근-권한) 의 근거 그대로
:::

## 샘플 JPO

[XX_UtilAccess_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilAccess_mxJPO.java) 의 메서드입니다. 모든 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map (`programMap`) 에서 값을 읽습니다. comment 는 모두 `DomainAccess.COMMENT_MULTIPLE_OWNERSHIP` 입니다.

| 메서드 | 받는 값 (`programMap` 의 키) | 돌려주는 값 | 보여 주는 것 |
| --- | --- | --- | --- |
| `shareWithCollabSpace` | `objectId`, `organization`, `project`, `access` | 없음 | [조직, 협업공간과 공유](#조직-협업공간과-공유). 데이터를 바꿈 |
| `shareWithPerson` | `objectId`, `userName`, `access` | 없음 | [사람과 공유](#사람과-공유). Person 객체 id 를 구해 넘김. 데이터를 바꿈 |
| `inheritAccess` | `objectId`, `fromId` | 없음 | [다른 객체의 권한 물려받기](#다른-객체의-권한-물려받기). 데이터를 바꿈 |
| `unshareWithCollabSpace` | `objectId`, `organization`, `project` | 없음 | [공유 거두기](#공유-거두기). 데이터를 바꿈 |
| `unshareWithPerson` | `objectId`, `userName` | 없음 | 협업공간 `<이름>_PRJ` 로 거두기. 데이터를 바꿈 |
| `stopInheritingAccess` | `objectId`, `fromId` | 없음 | 물려받기 항목 거두기. 데이터를 바꿈 |
| `isSharedWithPerson` | `objectId`, `userName` | `Boolean` | [공유했는지 확인](#공유했는지-확인) |
| `getSharingList` | `objectId` | `MapList` | [공유 목록 읽기](#공유-목록-읽기) |
| `clearSharing` | `objectId` | 없음 | [공유를 모두 지우기](#공유를-모두-지우기). 데이터를 바꿈 |
| `getAccessNames` | `objectId` | `Map` (`names`, `reader`, `owner`, `ownerMasks`) | [쓸 수 있는 권한 이름](#쓸-수-있는-권한-이름) |
| `canModify` | `objectId` | `Boolean` | [로그인 사용자의 권한 확인](#로그인-사용자의-권한-확인) (`FrameworkUtil.hasAccess`) |
| `getChildPartsWithAccess` | `objectId` | `MapList` (`id`, `name`, `current.access[modify]`) | 목록을 읽으면서 select 로 권한도 읽기 |
| `findUsersWithoutRead` | `objectId`, `userNames` (쉼표로 이은 이름) | `ArrayList` (읽을 수 없는 사람) | [다른 사용자의 권한 확인](#다른-사용자의-권한-확인) |
| `moveToCollabSpace` | `objectId`, `project`, `organization` | 없음 | [객체의 조직, 협업공간, 소유자 바꾸기](#객체의-조직-협업공간-소유자-바꾸기) (`setPrimaryOwnership`). 데이터를 바꿈 |

::: details XX_UtilAccess_mxJPO.java

<<< @/../JPOSample/util/XX_UtilAccess_mxJPO.java

:::

::: warning
샘플의 관계 이름 (`relationship_MyPartStructure`), 조직, 협업공간, 권한 이름은 예시입니다. `share...`, `unshare...`, `inheritAccess`, `stopInheritingAccess`, `clearSharing`, `moveToCollabSpace` 는 데이터를 바꿉니다. 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다.
:::

## 다루지 않은 메서드

::: details 펼쳐 보기
| 클래스 | 메서드 | 이유 |
| --- | --- | --- |
| DomainAccess | `init`, `createSecurityContext`, `needOperationalOrg`, `deleteSecurityContextObjectifConnectedToOrg`, `allowTypeInheritOwnership`, `updateLogicalMappingforDefaultTypes` | 내부용 |
| DomainAccess | `getObjectLogicalAccessRangeNLS` | 화면용 |
| DomainAccess | `getReportedAgainstObject` | 앱 전용 |
| DomainAccess | `inheritObjectOwnership`, `createRelObjectOwnership`, `deleteRelObjectOwnership`, `copyDirectSOV`, `updateOwnershipOnClone`, `getLogicalNameForPolicy` | OOTB 미사용 |
| DomainAccess | `getDefaultProject` | 같은 일 다른 메서드 ([사용자, 조직, 보안 컨텍스트](/jpo/api/util/person)) |
| AccessUtil | `isOwnerWorkspaceLead`, `setRead`, `getReadAccess`, `setAccess`, `setMemberRead`, `getMemberReadAccess`, `setReadWrite`, `getReadWriteAccess`, `setAdd`, `getAddAccess`, `setRemove`, `getRemoveAccess`, `setAddRemove`, `getAddRemoveAccess`, `setWorkspaceMember`, `setWorkspaceLead`, `getWorkspaceLeadAccess`, `setGlobalRead`, `setNoGlobalRead`, `setNoneAccess`, `getAccessList`, `compareAccess` | 앱 전용 |
| FrameworkUtil | `isAuthorized` | OOTB 미사용 |
:::
