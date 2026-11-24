# 사용자, 조직, 보안 컨텍스트 <Badge type="info" text="원본 분석" />

로그인 사용자와 그 Person 객체, 회사와 조직, 보안 컨텍스트 (`역할.조직.협업공간`), 역할, vault 를 알아내는 메서드입니다. 예제의 `part` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다. util 전체를 하고 싶은 일로 찾으려면 [유틸리티](/jpo/api/utilities) 를 봅니다. 로그인 사용자 정보를 다루는 배경은 [현재 사용자](/jpo/utils#현재-사용자) 에 있습니다.

::: info 근거
xerinfra.jar (`PersonUtil`, `VaultUtil`, `FrameworkUtil`, `DomainAccess`), pnocommon.jar (`com.matrixone.apps.common.Person`), eMatrixServletRMI.jar (`matrix.db.Context`) class 파일 (javap) 기준. 건수는 OOTB JPO 에서 센 호출 수: `context.getUser()` 659, `Person.getPerson(` 218, `PersonUtil.getPersonObjectID(` 215, `PersonUtil.getFullName(` 150, `PersonUtil.hasAssignment(` 48, `PersonUtil.getSearchVaults(` 46, `PersonUtil.getAssignments(` 34, `PersonUtil.getPersonObject(` 28, `PersonUtil.getUserCompanyId(` 20, `PersonUtil.getDefaultOrganization(` 19, `PersonUtil.getActiveSecurityContext(` 19, `PersonUtil.getActiveProject(` 16, `PersonUtil.getActiveOrganization(` 12, `PersonUtil.getDefaultSecurityContext(` 12, `PersonUtil.getUserRoles(` 12, `PersonUtil.getDefaultVault(` 10, `PersonUtil.getPersonFromRole(` 9, `PersonUtil.isVPLMAdmin(` 8, `FrameworkUtil.getProjectGroupAssignees(` 8, `PersonUtil.getDefaultProject(` 7, `PersonUtil.getEmail(` 4, `PersonUtil.getMemberOrganizations(` 4, `PersonUtil.hasAnyAssignment(` 4, `PersonUtil.getProjects(` 3, `PersonUtil.getSecurityContextsNames(` 2
:::

샘플: [XX_UtilPerson_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilPerson_mxJPO.java) ([메서드별 설명](#샘플-jpo))

## 한눈에 보기

| 하고 싶은 일 | 쓸 메서드 |
| --- | --- |
| 로그인 사용자 이름 | [`context.getUser()`](#로그인-사용자-이름) |
| 사용자의 Person 객체 id | [`PersonUtil.getPersonObjectID(context, user)`](#person-객체-id) |
| Person 객체의 속성, 연결 읽기 | [`Person.getPerson(context, user)`](#person-객체의-값-읽기) |
| 화면에 보이는 이름 (`Gildong Hong`) | [`PersonUtil.getFullName(context, user)`](#화면에-보이는-이름) |
| 메일 주소 | [`PersonUtil.getEmail(context, user)`](#메일-주소) |
| 로그인 사용자의 회사 | [`PersonUtil.getUserCompanyId(context)`](#회사) |
| Member 로 속한 조직 | [`PersonUtil.getMemberOrganizations(context, user)`](#소속-조직) |
| 지금 보안 컨텍스트와 그 조직, 협업공간 | [`PersonUtil.getActiveSecurityContext(context)`](#지금-보안-컨텍스트) |
| 기본 보안 컨텍스트와 그 조직, 협업공간 | [`PersonUtil.getDefaultSecurityContext(context, user)`](#기본-보안-컨텍스트) |
| 가진 보안 컨텍스트, 조직의 협업공간 목록 | [`PersonUtil.getSecurityContextsNames(context, user)`](#가진-보안-컨텍스트-목록) |
| 그 역할을 가졌는지 확인 | [`PersonUtil.hasAssignment(context, role)`](#역할을-가졌는지-확인) |
| 가진 역할, 그룹 목록 | [`PersonUtil.getAssignments(context, user)`](#가진-역할-목록) |
| 보안 컨텍스트가 관리자, 읽기 역할인지 | [`PersonUtil.isVPLMAdmin(context, sc)`](#보안-컨텍스트의-역할-확인) |
| 그 역할을 가진 사람 목록 | [`PersonUtil.getPersonFromRole(context, role)`](#역할을-가진-사람-목록) |
| 사용자 그룹에 든 사람 목록 | [`FrameworkUtil.getProjectGroupAssignees(context, group)`](#사용자-그룹의-사람-목록) |
| 로그인 vault, 검색할 vault | [`context.getVault().getName()`, `VaultUtil.getSearchVaultPattern(context)`](#vault) |

## 로그인 사용자 이름

로그인한 사람의 이름 (로그인 id) 입니다. 소유자, 메일 받는 사람을 정할 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `context.getUser()` | 없음 | `String`. 사용자 이름 (`gdhong` ...). Context 의 세션에 담긴 이름 |

```java
String user = context.getUser();
```

- [pushContext](/jpo/api/util/transaction) 안에서는 User Agent 이름이 나옵니다. 이 페이지에서 사용자를 받지 않는 형태 (`getPersonObjectID(context)`, `getFullName(context)`, `getAssignments(context)`, `hasAssignment` ...) 도 모두 이 값으로 사용자를 정하므로 같습니다. 실제 사용자는 push 전에 변수에 담아 둡니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `Person.getPerson(context).getName()` | Person 객체를 찾아 열고 이름을 읽음. 같은 값을 서버를 거쳐 얻음 |

::: info 근거
eMatrixServletRMI.jar `Context.getUser()` 는 `getSession().getUserName()`. xerinfra.jar `PersonUtil` 의 사용자를 받지 않는 형태 (`getPersonObjectID(Context)`, `getPersonObject(Context)`, `getFullName(Context)`, `getEmail(Context)`, `getAssignments(Context)`, `getDefaultSecurityContext(Context)`, `getMemberOrganizations(Context)`) 는 모두 `context.getUser()` 로 사용자를 받는 형태를 부름. `hasAssignment` 는 `getAssignments(context)` 를 씀
:::

## Person 객체 id

사용자 이름으로 그 사람의 Person 객체 (타입 `Person`) id 를 얻습니다. 사람과 연결하거나 [사람과 공유](/jpo/api/util/access) 할 때 넘깁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PersonUtil.getPersonObjectID(context, user)` | `user`: 사용자 이름 | `String`. Person 객체 id |
| `PersonUtil.getPersonObjectID(context)` | 없음. 로그인 사용자 | 위와 같음. 로그인 사용자 이름이 비었으면 `""` |

```java
String personId = PersonUtil.getPersonObjectID(context, context.getUser());
```

- 처음 한 번 MQL `temp query bus` 로 타입 `Person`, 이름이 `user` 인 객체를 하나 찾고, 그 id 를 서버 메모리 (static Map, 테넌트와 사용자별) 에 담아 둡니다. 같은 사용자를 다시 물으면 서버에 가지 않습니다.
- Person 객체가 없으면 `FrameworkException` 입니다. 문구는 `emxFramework.PersonUtil.Error_NoPersonBOFound` (`Error: No person business object found with name ==`) 뒤에 사용자 이름을 붙인 것입니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `Person.getPerson(context, user).getObjectId()` | 안에서 `getPersonObjectID` 로 id 를 얻은 뒤 객체를 열기까지 함 |
| `PersonUtil.getPersonObject(context, user).getObjectId()` | 같은 캐시로 id 를 얻어 `DomainObject` 를 열기까지 함 |

::: info 근거
xerinfra.jar `PersonUtil.getPersonObjectID(Context, String)`: `getTenantPersonMap(context, user)` (static `_TENANT_PERSONS` 에서 테넌트, 사용자로 꺼낸 `HashMap`) 의 `id` 가 비었으면 `getPersonObject(context, user).getObjectId(context)` 를 넣어 둠. 실패하면 `EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.PersonUtil.Error_NoPersonBOFound")` 와 사용자 이름을 이은 문구 (recipe `\u0001\u0001`) 로 `FrameworkException`. `getPersonObject(Context, String)` 은 캐시가 비었으면 `MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 limit 1 select $4 dump $5", Person 타입, user, "*", "id", "|")` 결과를 `|` 로 잘라 마지막 칸을 id 로 씀. properties/emxFrameworkStringResource.properties (`Error: No person business object found with name ==`)
:::

## Person 객체의 값 읽기

Person 객체의 속성이나 연결 (회사 등) 은 객체를 얻어 [getInfo](/jpo/api/domain-object#getinfo) 로 읽습니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `Person.getPerson(context, user)` | `user`: 사용자 이름 | `com.matrixone.apps.common.Person` (pnocommon.jar). `DomainObject` 의 하위 클래스. 열린 상태로 옴 |
| `Person.getPerson(context)` | 없음. 로그인 사용자 | 위와 같음 |

```java
Person person = Person.getPerson(context, context.getUser());
String companySelect = "to[" + DomainConstants.RELATIONSHIP_EMPLOYEE + "].from.name";
Map info = person.getInfo(context, StringList.create(DomainConstants.SELECT_NAME, companySelect));
String companyName = (String) info.get(companySelect);
```

- 안에서 `PersonUtil.getPersonObjectID(context, user)` 로 id 를 얻고 (위의 캐시) `new Person(id)` 를 엽니다.
- Person 객체가 없으면 `FrameworkException` 입니다 (문구 `emxComponents.Person.NoPersonObject`, `Error: No person business object found.`).
- import 는 `com.matrixone.apps.common.Person` 입니다. `matrix.db.Person` (사용자 admin 객체) 과 이름이 같으니 섞지 않습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `PersonUtil.getPersonObject(context, user)` | 같은 일 (같은 캐시의 id 로 `DomainObject` 를 만들어 엶). OOTB 28 건 |
| `Person.getPerson(context, user, false)` | `false` 면 캐시 없이 `temp query bus` 로 찾음. 결과는 같음 |
| `PersonUtil.getPersonProperty(context, user, key)` | 이름과 달리 Person 객체를 읽지 않음. 서버 메모리에 담아 둔 값 (`id`, `full name`, `Email` ...) 만 꺼내고 없으면 `null` |

::: info 근거
pnocommon.jar `Person.getPerson(Context)` → `getPerson(context, context.getUser())`. `getPerson(Context, String)`: `PersonUtil.getPersonObjectID(context, user)` → `new Person(id)` → `open(context)`, 실패하면 `EnoviaResourceBundle.getProperty(context, "emxPersonOrgModelStringResource", 세션 언어, "emxComponents.Person.NoPersonObject")` 로 `FrameworkException`. `getPerson(Context, String, boolean)` 은 `false` 면 `temp query bus $1 $2 $3 limit $4 select $5 dump $6` 로 찾음. xerinfra.jar `PersonUtil.getPersonObject(Context, String)` 은 `new DomainObject(id)` 를 `open`. `getPersonProperty(Context, String, String)` 은 `getTenantPersonMap(context, user).get(key)` 만 함. properties/emxPersonOrgModelStringResource.properties (`Error: No person business object found.`)
:::

## 화면에 보이는 이름

사람 이름을 `Gildong Hong` 처럼 화면용으로 만듭니다. 메일 본문, 이력 문구에 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PersonUtil.getFullName(context, user)` | `user`: 사용자 이름 | `String`. 시스템 설정 `emxFramework.FullName.Format` 모양의 이름. Person 객체가 없으면 `user` 그대로, `user` 가 비면 `""` |
| `PersonUtil.getFullName(context)` | 없음. 로그인 사용자 | 위와 같음 |

형식의 `<First Name>`, `<Last Name>`, `<User Name>` 을 값으로 바꿔 넣을 뿐이라, 형식에 따라 결과가 이렇게 달라집니다 (First Name `Gildong`, Last Name `Hong`, 사용자 `gdhong`).

| `emxFramework.FullName.Format` | 결과 |
| --- | --- |
| `<First Name> <Last Name>` (분석한 properties 의 값) | `Gildong Hong` |
| `<Last Name> <First Name>` | `Hong Gildong` |
| `<Last Name>, <First Name> (<User Name>)` | `Hong, Gildong (gdhong)` |

```java
String owner = part.getInfo(context, DomainConstants.SELECT_OWNER);
String ownerName = PersonUtil.getFullName(context, owner);
```

- 처음 한 번 `temp query bus Person <user> - limit 1 select id attribute[First Name] attribute[Last Name] dump |` 로 읽고 결과를 서버 메모리에 담아 둡니다. OOTB 는 Person 의 `First Name`, `Last Name` 속성이 바뀔 때 이 캐시를 고칩니다 (emxPersonBase_mxJPO.java).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `Person.getDisplayName(context, user)` | 안에서 `PersonUtil.getFullName(context, user)` 를 그대로 부름 |

::: info 근거
xerinfra.jar `PersonUtil.getFullName(Context, String)` 은 `EnoviaResourceBundle.getProperty(context, "emxFramework.FullName.Format")` 를 private `getFullName(Context, String, String)` 에 넘김. private 형태: `user` 가 비면 `""`. 캐시 `full name` 이 비었으면 `mqlCommand(context, "temp query bus $1 $2 $3 limit 1 select $4 $5 $6 dump $7", Person 타입, user, "-", "id", "attribute[First Name]", "attribute[Last Name]", "|")`, 결과를 `|` 로 잘라 형식의 `<First Name>` 에 5 번째, `<Last Name>` 에 6 번째, `<User Name>` 에 2 번째 칸을 `String.replace`. 결과가 비면 `user`. pnocommon.jar `Person.getDisplayName(Context, String)` → `PersonUtil.getFullName`. properties/emxSystem.properties:2419 (`emxFramework.FullName.Format=<First Name> <Last Name>`)
:::

## 메일 주소

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PersonUtil.getEmail(context, user)` | `user`: 사용자 이름 | `String`. 메일 주소. 메일이 비었으면 `""`. `user` 가 사람이 아니면 (역할 이름 등) `null` |
| `PersonUtil.getEmail(context)` | 없음. 로그인 사용자 | 위와 같음 |

```java
String mail = PersonUtil.getEmail(context, part.getInfo(context, DomainConstants.SELECT_OWNER));
```

- Person 객체의 속성이 아니라 사용자 (admin person) 의 `email` 을 읽습니다: `list user <user> select isaperson dump` 가 `TRUE` 일 때만 `print person <user> select email dump`. 결과는 서버 메모리에 담아 둡니다. OOTB 는 Person 의 `Email Address` 속성이 바뀔 때 이 캐시를 고칩니다 (emxPersonBase_mxJPO.java).
- 실패하면 `FrameworkException` 인데, 문구는 이름 이야기인 `emxFramework.PersonUtil.Error_GetPersonFullName` (`Error: Unable to get Person Full Name.`) 입니다. 메일 문제로 읽지 않게 주의합니다.
- 메일 보내기는 [메일, 알림](/jpo/api/util/mail) 을 봅니다.

::: info 근거
xerinfra.jar `PersonUtil.getEmail(Context, String)`: 캐시 `Email` 이 비었으면 `mqlCommand(context, "list user $1 select $2 dump", user, "isaperson")` 가 `TRUE` (대소문자 무시) 일 때 `mqlCommand(context, "print Person $1 select $2 dump", user, "email")`, `null` 이면 `""` 로 넣어 둠. `TRUE` 가 아니면 캐시 값 (없으면 `null`) 을 돌려줌. 예외는 `emxFramework.PersonUtil.Error_GetPersonFullName` 문구. `getEmail(Context)` 는 `context.getUser()` 가 비면 `""`
:::

## 회사

로그인 사용자가 Employee 로 속한 회사 (Company) 객체의 id 입니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PersonUtil.getUserCompanyId(context)` | 없음. 로그인 사용자 | `String`. 회사 객체 id (Person 의 `to[Employee].from.id`). 연결이 없으면 `null` |

```java
String companyId = PersonUtil.getUserCompanyId(context);
String companyName = DomainObject.newInstance(context, companyId).getInfo(context, DomainConstants.SELECT_NAME);
```

- 회사 이름만 필요하면 Person 객체에서 select `to[Employee].from.name` 을 바로 읽는 편이 한 번에 끝납니다 ([Person 객체의 값 읽기](#person-객체의-값-읽기)).
- 다른 사용자의 회사는 `Person.getPerson(context, user).getCompanyId(context)` 로 읽습니다 (같은 select).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `Person.getPerson(context).getCompanyId(context)` | 같은 select (`to[Employee].from.id`). 로그인 사용자면 위 메서드로 충분 |
| `PersonUtil.getCompanyNameFromCompanyID(context, companyId)` | 회사 객체 id 가 아니라 회사의 `Company ID` 속성 값으로 찾음 (`attribute[Company ID] ==값`). 결과가 1 개가 아니면 예외. OOTB 에 객체 id 를 넘기는 곳이 있음 (emxChangeBase_mxJPO.java) |
| `FrameworkUtil.getOrganization(context, bo, rel)` | 관계 `rel` 의 from 객체를 열어 `BusinessObject` 로 돌려주는 일반 메서드. OOTB 미사용 |

::: info 근거
xerinfra.jar `PersonUtil.getUserCompanyId(Context)` 는 `getPersonObject(context).getInfo(context, "to[" + relationship_Employee + "].from.id")` (recipe `to[\u0001].from.id`). `getCompanyNameFromCompanyID(Context, String)` 은 static `_companyMap` 에 없으면 `DomainObject.findObjects(context, TYPE_COMPANY, "*", "-", "*", "*", "attribute[Company ID] ==" + 값, false, [name])`, 1 건이 아니면 `"Many companies with same CompanyID " + 값` 으로 `FrameworkException`. pnocommon.jar `Person.getCompanyId(Context)` 는 `getCompany(context)` 가 `Person.SELECT_COMPANY_ID` (`to[Employee].from.id`) 로 만든 `Company` 의 id. `FrameworkUtil.getOrganization(Context, BusinessObject, String)` 은 `to[rel].from.id` 를 읽어 `new BusinessObject(id)` 를 open / close
:::

## 소속 조직

사람이 Member 관계로 들어 있는 조직 (회사, 부서 ...) 객체 id 목록입니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PersonUtil.getMemberOrganizations(context, user)` | `user`: 사용자 이름 | `String[]`. 조직 객체 id (Person 의 `to[Member].from.id`). `user` 가 비면 `null` |
| `PersonUtil.getMemberOrganizations(context)` | 없음. 로그인 사용자 | 위와 같음 |

```java
String[] orgIds = PersonUtil.getMemberOrganizations(context, context.getUser());
```

- 결과를 서버 메모리에 담아 두고, 다음부터는 **그 배열 자체** 를 돌려줍니다. 받은 배열의 값을 바꾸지 않습니다.
- 관계 이름이 심볼릭 이름이 아니라 `Member` 로 박혀 있습니다.

::: info 근거
xerinfra.jar `PersonUtil.getMemberOrganizations(Context, String)`: 캐시 `memberorganization` 이 없으면 `new DomainObject(getPersonObjectID(context, user)).getInfoList(context, "to[Member].from.id")` 를 `String[]` 로 옮겨 넣어 두고 돌려줌. `user` 가 `null` 이거나 비면 캐시 값 (없으면 `null`)
:::

## 지금 보안 컨텍스트

로그인 사용자가 지금 일하는 보안 컨텍스트 (`역할.조직.협업공간`, [형식](/rest-api/security-context#형식)) 와 그 조직, 협업공간입니다. 새 객체의 조직, 협업공간을 정하거나 권한을 판단할 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PersonUtil.getActiveSecurityContext(context)` | 없음 | `String`. `context.getRole()` 에서 앞의 `ctx::` 를 뗀 값 |
| `PersonUtil.getActiveOrganization(context)` | 없음 | `String`. 그 보안 컨텍스트의 조직 이름. 보안 컨텍스트가 없거나 못 찾으면 `""` |
| `PersonUtil.getActiveProject(context)` | 없음 | `String`. 그 보안 컨텍스트의 협업공간 이름. 위와 같음 |

| `context.getRole()` | `getActiveSecurityContext` 결과 |
| --- | --- |
| `ctx::VPLMProjectLeader.MyOrg.MyCollabSpace` | `VPLMProjectLeader.MyOrg.MyCollabSpace` |
| `ctx::` 로 시작하지 않는 값 | 그 값 그대로 |
| `null`, `""` | `""` |

```java
String sc = PersonUtil.getActiveSecurityContext(context);
String organization = PersonUtil.getActiveOrganization(context);
String project = PersonUtil.getActiveProject(context);
part.setPrimaryOwnership(context, project, organization);
```

- `getActiveSecurityContext` 는 `context.getRole()` 만 읽습니다. 조직과 협업공간은 문자열을 자르지 않고, 이름이 그 보안 컨텍스트인 `Security Context` 객체를 검색 (`DomainObject.findObjects`) 해 연결된 조직, 협업공간 이름을 읽습니다. 부를 때마다 검색합니다.
- 객체의 조직, 협업공간을 바꾸는 `setPrimaryOwnership` 은 [소유권 공유, 접근 권한](/jpo/api/util/access) 을 봅니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `context.getRole()` | 앞에 `ctx::` 가 붙은 원래 값. 위 메서드가 이것을 읽어 뗌 |
| `PersonUtil.getActiveProjectTitle(context)` | 협업공간의 `Title` 속성 (비었으면 이름). 화면 표시용 |

::: info 근거
xerinfra.jar `PersonUtil.getActiveSecurityContext(Context)`: `context.getRole()` 이 비면 `""`, `ctx::` 로 시작하면 `substring(5)`, 아니면 그대로. `getActiveOrganization` / `getActiveProject` / `getActiveProjectTitle` 은 private `getPartOfActiveSecurityContext(context, 부분, title)`: 보안 컨텍스트가 비면 `""`, 아니면 `DomainObject.findObjects(context, type_SecurityContext, sc, "*", "*", "*", null, sc, true, selects, 0)` 의 첫 행에서 `from[Security Context Organization].to.name` / `from[Security Context Project].to.name` (title 이면 `...to.attribute[Title]`) 을 읽음. OOTB 예: emxAEFUtilBase_mxJPO.java
:::

## 기본 보안 컨텍스트

사용자가 기본으로 정해 둔 보안 컨텍스트와 그 조직, 협업공간입니다. 다른 사용자를 대신해 일할 때 그 사람의 보안 컨텍스트로 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PersonUtil.getDefaultSecurityContext(context, user)` | `user`: 사용자 이름. `(context)` 형태는 로그인 사용자 | `String`. 사용자 설정 `preference_VPLMIntegVPLMContext` 의 값. 그 사용자가 지금 가진 보안 컨텍스트가 아니면 `""` |
| `PersonUtil.getDefaultOrganization(context, user)` | `user`: 사용자 이름 | `String`. 기본 보안 컨텍스트의 조직 이름. 못 찾으면 `""` |
| `PersonUtil.getDefaultProject(context, user)` | `user`: 사용자 이름 | `String`. 기본 보안 컨텍스트의 협업공간 이름. 못 찾으면 `""` |

```java
String user = context.getUser();
String defaultSc = PersonUtil.getDefaultSecurityContext(context, user);
if (UIUtil.isNotNullAndNotEmpty(defaultSc)) {
    String organization = PersonUtil.getDefaultOrganization(context, user);
    String project = PersonUtil.getDefaultProject(context, user);
}
```

- 값은 MQL `print person "<user>" select property[preference_VPLMIntegVPLMContext].value dump` 로 읽고, 슈퍼유저로 `print person <user> select assignment` 를 읽어 `ctx::` 를 붙인 그 이름이 있을 때만 돌려줍니다.
- 저장된 값은 `ctx::` 없이 들어 있는 것으로 보입니다. OOTB 는 `context.resetRole("ctx::" + 값)` 처럼 앞에 붙여 씁니다 (emxSubscriptionReplyHandlerBase_mxJPO.java) <Badge type="danger" text="검증 필요" />
- `getDefaultOrganization`, `getDefaultProject` 는 각각 위 두 MQL 을 다시 실행한 뒤 `Security Context` 객체를 검색합니다. 기본 보안 컨텍스트가 `""` 여도 막지 않고 이름 `""` 으로 검색하므로, 위 예처럼 먼저 비었는지 봅니다 (이때의 검색 결과는 <Badge type="danger" text="검증 필요" />).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `PersonUtil.getDefaultSCValues(context)` | 로그인 사용자만. 역할, 조직, 협업공간을 검색 한 번으로 `Role`, `Org`, `CS` 키 Map 에 담음. OOTB JPO 에서 직접 부르는 곳 없음 |
| `PersonUtil.getDefaultSCRole(context, user)` | 역할 부분만 같은 방법으로 읽음 |
| `DomainAccess.getDefaultProject(context)` | 이름과 달리 사용자의 기본 협업공간이 아님. 심볼릭 이름 `role_GLOBAL` 의 실제 이름을 돌려줌 |

::: info 근거
xerinfra.jar `PersonUtil.getDefaultSecurityContext(Context, String)`: `mqlCommand(context, "print $1 \"$2\" select $3 dump", "person", user, "property[preference_VPLMIntegVPLMContext].value")` 를 private `isValidSecurityContext(context, 값, user)` 가 참일 때만 돌려줌. private 형태는 값이 비면 거짓, `ctx::` 가 없으면 붙이고 (recipe `ctx::\u0001`), `mqlCommand(context, "print person $1 select $2 dump;", true, user, "assignment")` 를 `,` 로 잘라 들어 있는지 봄. 없으면 그룹으로 받은 보안 컨텍스트 (`assignment.roleassignment`) 도 봄. `getDefaultOrganization` / `getDefaultProject` / `getDefaultSCRole` 은 `getDefaultSecurityContext` 결과를 이름으로 `findObjects(context, type_SecurityContext, 이름, "*", "*", "*", null, 이름, true, ["from[관계].to.name"], 0)`, 첫 행의 값. `getDefaultSCValues` 는 세 select 를 한 번에. `DomainAccess.getDefaultProject(Context)` 는 `PropertyUtil.getSchemaProperty(context, "role_GLOBAL")` 만 돌려줌
:::

## 가진 보안 컨텍스트 목록

사용자가 고를 수 있는 보안 컨텍스트 전부와, 조직별 협업공간 목록입니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PersonUtil.getSecurityContextsNames(context, user)` | `user`: 사용자 이름 | `StringList`. 보안 컨텍스트 이름 (`역할.조직.협업공간`, `ctx::` 없음). 직접 받은 것과 그룹으로 받은 것을 합쳐 중복을 뺌. 순서는 정해져 있지 않음 |
| `PersonUtil.getProjects(context, user, organization)` | `organization`: 조직 이름. 비우면 모든 조직 | `StringList`. 그 조직의 보안 컨텍스트에 있는 협업공간 이름 (중복 없음, 순서 없음) |
| `PersonUtil.getOrganizations(context, user, project)` | `project`: 협업공간 이름. 비우면 모든 협업공간 | `StringList`. 조직 이름 (중복 없음, 순서 없음) |

```java
String user = context.getUser();
StringList contexts = PersonUtil.getSecurityContextsNames(context, user);
boolean canUse = contexts.contains("VPLMProjectLeader.MyOrg.MyCollabSpace");
StringList projects = PersonUtil.getProjects(context, user, PersonUtil.getDefaultOrganization(context, user));
```

- 세 메서드 모두 Person 객체 (캐시한 id) 를 `getInfo` 한 번으로 읽습니다. 보안 컨텍스트는 Person 에서 `from[Assigned Security Context].to` 와 `to[Group Member].from.from[Group Assigned Security Context].to` 두 길로 찾습니다.
- `getProjects` 는 `TransferOwnership` 이 새 소유자의 협업공간을 확인할 때도 씁니다 ([TransferOwnership](/jpo/api/domain-object#transferownership)).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `PersonUtil.getSecurityContexts(context, user, selects)` | 보안 컨텍스트마다 역할, 조직, 협업공간을 담은 `MapList`. 키가 `from[Security Context Project].to.name` 같은 select 문자열. 넘긴 `selects` 에 기본 select 를 더해 그 목록을 바꿈 |
| `PersonUtil.hasSecurityContext(context, user)` | 안에서 `getSecurityContextsNames` 를 불러 비었는지만 봄 |
| `PersonUtil.getSecurityContextAssignments(context)` | 로그인 사용자의 assignment 가운데 `ctx::` 로 시작하는 것 (`ctx::` 붙음). OOTB 미사용 |
| `PersonUtil.isValidSecurityContext(context, sc)` | 로그인 사용자의 assignment 에 `ctx::` 를 붙인 그 이름이 있는지. OOTB 미사용 |
| `PersonUtil.getProjectsWithTitleAndName(context, user, organization)` | `getProjects` 와 같은 검색에 Title 을 `\|` 로 이어 붙임. 화면 표시용 |

::: info 근거
xerinfra.jar `PersonUtil.getSecurityContextsNames(Context, String)`: `getPersonObject(context, user).getInfo(context, selects, selects)` 로 `from[Assigned Security Context].to.name`, `to[Group Member].from.from[Group Assigned Security Context].to.name` (recipe `from[\u0001].to.name`, `to[\u0001].from.from[\u0001].to.name`) 을 읽어 stream `flatMap` 후 `Collectors.toSet()` 을 `new StringList(new ArrayList(set))`. `getProjects` / `getOrganizations` 는 private `getSecurityContextSet` (같은 두 길로 `from[Security Context Project].to.name`, `from[Security Context Organization].to.name`, `from[Security Context Role].to.name` 과 두 Title 을 읽어 보안 컨텍스트마다 Map) 을 조직 / 협업공간으로 거르고 (비었으면 거르지 않음) 협업공간 / 조직 이름을 `toSet()`. `getSecurityContexts` 는 같은 Set 을 `MapList` 로. `hasSecurityContext` 는 `getSecurityContextsNames` 크기가 0 보다 큰지. OOTB 예: emxDomainAccessBase_mxJPO.java (`getProjects`), enoECMChangeOrderBase_mxJPO.java (`getSecurityContextsNames`)
:::

## 역할을 가졌는지 확인

로그인 사용자가 어떤 역할을 가졌는지 봅니다. 명령 표시, 접근 함수 (access function), 트리거의 권한 판단에 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PersonUtil.hasAssignment(context, role)` | `role`: 역할의 **실제 이름** (`PropertyUtil.getSchemaProperty(context, "role_...")` 로 바꾼 값) | `boolean`. 로그인 사용자가 그 역할이나 그 하위 역할을 가지면 `true` |
| `PersonUtil.hasAnyAssignment(context, "role_A,role_B")` | 역할의 **심볼릭 이름** 을 쉼표로 이은 문자열 | `boolean`. 하나라도 가지면 `true` |
| `Person.getPerson(context, user).hasRole(context, role)` | 로그인 사용자가 아닌 사람. `role` 은 실제 이름 | `boolean`. 그 사람이 그 역할이나 그 하위 역할을 가지면 `true` |

```java
boolean isLeader = PersonUtil.hasAssignment(context, PropertyUtil.getSchemaProperty(context, "role_MyTeamLeader"));
boolean isReviewer = PersonUtil.hasAnyAssignment(context, "role_MyTeamLeader,role_MyReviewer");
```

- `hasAssignment` 는 [가진 역할 목록](#가진-역할-목록) 에 그 이름이 있는지 보고, 없으면 그 역할의 하위 역할 (MQL `print role <역할> select child`, 끝까지 내려감) 중 하나라도 있는지 봅니다. 하위 역할 목록도 서버 메모리에 담아 둡니다.
- `hasAnyAssignment` 에 실제 이름을 넘기면 `getSchemaProperty` 가 `""` 를 돌려줘 오류 없이 `false` 가 됩니다. 두 메서드의 넘기는 값이 다르니 주의합니다.
- `pushContext` 안에서는 User Agent 의 역할로 판단합니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `PersonUtil.getAssignments(context).contains(role)` | 하위 역할을 보지 않음 |
| `PersonUtil.isLoggedInUserCRorAdmin(context)` | 지금 보안 컨텍스트가 VPLMAdmin 이거나, 기본 보안 컨텍스트의 역할이 회사 대표 (`role_CompanyRepresentative`, `role_OrganizationManager`) 인지. 조직 관리용 |

::: info 근거
xerinfra.jar `PersonUtil.hasAssignment(Context, String)`: `getAssignments(context).contains(role)` 가 참이면 참, 아니면 `getChildrenUser(context, role)` 의 이름마다 `contains`. `getChildrenUser` 는 캐시 `childUsers` 가 없으면 `list user $1 select isarole dump` 가 `TRUE` 일 때 `getChildrenRole` (MQL `print role  $1 select child dump |`, 자식마다 재귀) 결과를 넣어 둠. `hasAnyAssignment(Context, String)` 은 `StringUtil.split(값, ",")` 의 항목마다 `PropertyUtil.getSchemaProperty(context, 항목)` 을 `hasAssignment` 에 넘김. pnocommon.jar `Person.hasRole(Context, String)` 은 `loadRoles` (`matrix.db.Person.getAssignments` 의 이름과 `RoleUtil.getParentRoles` 의 상위 역할, `print role|group <이름> select ancestor`) 에 들어 있는지. OOTB 예: emxMEPAltSubExtentionBase_mxJPO.java (`hasAnyAssignment`)
:::

## 가진 역할 목록

사용자에게 할당된 역할, 그룹 이름을 모두 받습니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PersonUtil.getAssignments(context, user)` | `user`: 사용자 이름. 비우면 로그인 사용자 | `Vector` (`String`). 직접 할당된 역할, 그룹, 보안 컨텍스트 역할 (`ctx::역할.조직.협업공간`) 과, 맨 끝에 사용자 이름 자신 |
| `PersonUtil.getAssignments(context)` | 없음. 로그인 사용자 | 위와 같음 |

```java
StringList assignments = new StringList(PersonUtil.getAssignments(context, context.getUser()));
```

- 하위, 상위 역할은 넣지 않습니다. 역할 상속까지 보려면 [역할을 가졌는지 확인](#역할을-가졌는지-확인) 을 씁니다.
- 결과를 서버 메모리에 담아 두고 **그 `Vector` 자체** 를 돌려줍니다. 값을 더하거나 빼려면 위처럼 복사해서 씁니다.
- 그룹을 거쳐 받은 보안 컨텍스트 역할은 `user` 가 아니라 로그인 사용자 (`context.getUser()`) 기준으로 더해집니다. 다른 사용자의 목록에 로그인 사용자의 것이 섞일 수 있습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `PersonUtil.getUserRoles(context, user)` | 같은 목록을 캐시 없이 매번 읽음. `getAssignments` 가 처음 한 번 이것을 부름 |
| `PersonUtil.getAllUserAssignments(context, user)` | 상위 역할까지 넣은 `Set`. 직접 할당은 `matrix.db.User` 객체로, 상위 역할은 이름 (`String`) 으로 섞여 들어감 |
| `PersonUtil.getAssignmentsMap(context, user, assignments)` | 넘긴 이름마다 종류 (`role`, `group`, `org`, `projectgroup`) 를 단 Map. 종류를 나눌 때만 |
| `Person.getRoleAssignments(context)` | 역할만, `ctx::` 를 빼고, 심볼릭 이름이 등록된 것만 |

::: info 근거
xerinfra.jar `PersonUtil.getAssignments(Context, String)`: 캐시 `assignment` 가 비었으면 `getUserRoles(context, user)` 를 넣고, 캐시의 `Vector` 를 그대로 돌려줌. `getUserRoles` → private `getUserAssignedRoles`: `new matrix.db.Person(user).open(context)`, `getAssignments(context)` 의 이름에 `getGroupAssignedSCRoles(context)` (`print person $1 select $2 $3 dump` 를 `context.getUser()` 로, `assignment.roleassignment`, `assignment.roleassignment.isaprojectgroup`) 를 더하고, 끝에 `Person.getName()`. `getAllUserAssignments` 는 `Set.addAll(UserList)` 뒤 Role 마다 `getAllParentRoles`. `getAssignmentsMap` 은 `print person $1 select assignment.isarole assignment.isagroup assignment.isaprojectgroup assignment.isanorg` 결과에서 이름마다 종류를 찾음. pnocommon.jar `Person.getRoleAssignments` 는 `ctx::` 가 아니고 `FrameworkUtil.getAliasForAdmin(context, "role", 이름, true)` 가 비지 않은 Role 만
:::

## 보안 컨텍스트의 역할 확인

보안 컨텍스트 문자열의 역할 부분이 관리자, 리더, 읽기 전용 같은 VPLM 역할인지 봅니다. 서버에 묻지 않고 글자 앞부분만 비교합니다.

| 쓸 메서드 | `true` 인 경우 (`sc` 가 이 글자로 시작) |
| --- | --- |
| `PersonUtil.isVPLMAdmin(context, sc)` | `role_VPLMAdmin` 의 실제 이름 + `.` |
| `PersonUtil.isVPLMLeader(context, sc)` | `role_VPLMProjectLeader` + `.` |
| `PersonUtil.isVPLMOwner(context, sc)` | `role_VPLMProjectAdministrator` + `.`, `role_3DSRestrictedOwner` + `.` |
| `PersonUtil.isVPLMAuthor(context, sc)` | `role_VPLMCreator` + `.` |
| `PersonUtil.isVPLMContributor(context, sc)` | `role_VPLMExperimenter` + `.`, `role_3DSRestrictedContributor` + `.` |
| `PersonUtil.isVPLMReader(context, sc)` | `role_VPLMViewer` + `.`, `role_3DSRestrictedReader` + `.`, `role_VPLMSecuredCrossAccess` + `.` |
| `PersonUtil.isVPLMAdminInHostCompany(context)` | 지금 보안 컨텍스트가 `<role_VPLMAdmin>.<role_CompanyName>.Default` 와 같을 때 |

```java
String sc = PersonUtil.getActiveSecurityContext(context);
boolean isAdmin = PersonUtil.isVPLMAdmin(context, sc);
boolean isReader = PersonUtil.isVPLMReader(context, sc);
```

- 두 번째 인자는 사용자 이름이 아니라 보안 컨텍스트 문자열입니다. `ctx::` 가 붙은 `context.getRole()` 을 그대로 넘기면 늘 `false` 입니다. `getActiveSecurityContext` 나 `getDefaultSecurityContext` 결과를 넘깁니다. `null` 을 넘기면 `NullPointerException` 입니다.
- OOTB 에는 `loggedInRole.contains(역할이름)` 으로 보는 곳도 있습니다 (emxAEFUtilBase_mxJPO.java). 글자 포함이라 조직, 협업공간 이름에 역할 이름이 들어 있어도 참이 됩니다. 위 메서드처럼 앞부분으로 봅니다.

::: info 근거
xerinfra.jar `PersonUtil.isVPLMAdmin(Context, String)` 등: `PropertyUtil.getSchemaProperty(context, 역할 심볼릭 이름)` 에 `.` 를 붙여 (recipe `\u0001.`) `sc.startsWith`. `isVPLMAdminInHostCompany(Context)` 는 `role_VPLMAdmin` 과 `role_CompanyName` 의 실제 이름으로 `\u0001.\u0001.Default` 를 만들어 `getActiveSecurityContext(context)` 와 `equals`. OOTB 예: emxJobBase_mxJPO.java (`isVPLMAdmin(context, PersonUtil.getActiveSecurityContext(context))`), emxExtendedHeaderBase_mxJPO.java (`getDefaultSecurityContext` 결과를 넘김)
:::

## 역할을 가진 사람 목록

그 역할을 가진 사람 모두에게 알림을 보내거나 담당자를 고를 때 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PersonUtil.getPersonFromRole(context, role)` | `role`: 역할의 실제 이름 | `StringList`. 그 역할과 그 하위 역할 (끝까지) 에 할당된 사람 이름. 중복 없음 |

```java
StringList leaders = PersonUtil.getPersonFromRole(context, PropertyUtil.getSchemaProperty(context, "role_MyTeamLeader"));
```

- 역할마다 `matrix.db.Role` 을 열어 하위 역할을 따라 내려가며 할당된 사용자 가운데 사람 (`matrix.db.Person`) 만 모읍니다. 하위 역할이 많으면 그만큼 서버에 갑니다. 캐시하지 않습니다.
- 실패하면 `emxFramework.PersonUtil.Error_GetPersonFromRole` (`Error: Unable to get people from Role.`) 문구로 `FrameworkException` 입니다.

::: info 근거
xerinfra.jar `PersonUtil.getPersonFromRole(Context, String)`: `new Role(role).open(context)`, `hasChildren()` 이면 `getChildren()` 마다 자신을 재귀 호출해 없는 이름만 더함. `getAssignments(context)` 가운데 `instanceof matrix.db.Person` 인 이름을 없는 것만 더하고 `close`. OOTB 예: emxProgramCentralUtilBase_mxJPO.java
:::

## 사용자 그룹의 사람 목록

사용자 그룹 (Group Proxy 객체) 에 든 사람 목록입니다. 라우트 담당자가 그룹일 때 OOTB 가 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.getProjectGroupAssignees(context, group)` | `group`: 그룹 객체 id 또는 그룹 이름 | `MapList`. 사람마다 `name`, `id`. 그룹을 못 찾거나 읽다 실패하면 빈 목록 (예외 없음) |

```java
MapList members = FrameworkUtil.getProjectGroupAssignees(context, "MyReviewGroup");
StringList names = new StringList();
for (Object o : members) {
    names.add((String) ((Map) o).get(DomainConstants.SELECT_NAME));
}
```

- 객체 id 가 아니면 `temp query bus "Group Proxy" <이름> * select id dump |` 로 id 를 찾습니다. 그다음 그룹에서 `Group Member` 관계로 연결된 `Person` 을 한 단계 읽습니다. 타입과 관계 이름이 심볼릭 이름이 아니라 실제 이름으로 박혀 있습니다.
- 이름이 사용자 그룹인지만 보려면 `FrameworkUtil.isUserGroupObject(context, name)` 을 씁니다 (`type_GroupProxy` 이고 리비전 `-` 인 객체가 있으면 `true`).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `FrameworkUtil.getPersonsMailListFromUserGroup(context, group)` | 안에서 위 메서드를 불러 사람 **이름** 을 쉼표로 이음. 이름과 달리 메일 주소가 아님 |

::: info 근거
xerinfra.jar `FrameworkUtil.getProjectGroupAssignees(Context, String)`: `isObjectId` 가 거짓이면 `mqlCommand(context, "temp query bus $1 $2 $3 select $4 dump $5", "Group Proxy", 값, "*", "id", "|")` 의 4 번째 칸을 id 로. `new DomainObject(id).getRelatedObjects(context, "Group Member", TYPE_PERSON, [name, id], null, false, true, 1, null, null)`, `Exception` 은 잡고 빈 `MapList`. `getPersonsMailListFromUserGroup` 은 결과의 `name` 을 `,` 로 이음. `isUserGroupObject` 는 `temp query bus $1 $2 $3 select $4 dump` (`type_GroupProxy`, 이름, `-`, `id`) 결과가 비지 않으면 참. OOTB 예: emxInboxTaskBase_mxJPO.java, emxRouteBase_mxJPO.java
:::

## vault

새 객체를 둘 vault 와 검색할 vault 입니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `context.getVault().getName()` | 없음 | `String`. 로그인 context 의 vault 이름 |
| `VaultUtil.getSearchVaultPattern(context)` | 없음 | `String`. 검색에 쓸 vault 이름을 쉼표로 이은 것. `findObjects` 의 vault 자리에 넣는 모양 |

```java
String vault = context.getVault().getName();
String searchVaults = VaultUtil.getSearchVaultPattern(context);
```

- `DomainObject.findObjects` 는 vault 를 `null` 로 넘기면 안에서 `getSearchVaultPattern` 을 씁니다 ([findObjects](/jpo/api/domain-object#findobjects)). 검색에는 `null` 을 넘기면 됩니다.
- 값: 프로그램 `emxKweTypesBL` 이 있으면 그 `getSearchVaultsForType` 결과 (`0|a|b` 면 `a,b`), 없으면 서버 캐시 (`CacheManager` 의 `SEARCH_VAULTS`) 의 vault 이름을 이은 것입니다.
- 객체를 만들 때 vault 를 정하는 일은 [getDefaultVault](/jpo/api/domain-object#getdefaultvault) 를 봅니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `PersonUtil.getDefaultVault(context)` | 안에서 `context.getVault().getName()` 을 그대로 부름 |
| `PersonUtil.getSearchVaults(context, boolean, option)` | 안에서 `VaultUtil.getSearchVaultPattern(context)` 를 그대로 부름. 두 인자는 쓰지 않음 (OOTB 는 `ALL_VAULTS` 같은 값을 넘기지만 결과가 같음) |
| `PersonUtil.getSearchDefaultVaults(context)` | 위와 같음 |
| `VaultUtil.getSearchVaults(context)` | 같은 값을 `,` 로 잘라 `List` 로 |
| `VaultUtil.getVaults(context)` | 모든 vault 의 `name`, `description` (`MapList`). vault 마다 열어 설명을 읽음 |
| `VaultUtil.getLocalVaults(context)` | `list server * select active` 로 활성 서버의 vault 를 모음. 서버가 없으면 모든 vault |
| `Person.getPerson(context).getVaultName(context)` | 로그인 vault 가 아니라 회사 객체의 vault |

::: info 근거
xerinfra.jar `PersonUtil.getDefaultVault(Context)` 는 `context.getVault().getName()`. `getSearchVaults(Context, boolean, String)`, `getSearchDefaultVaults(Context)` 는 `VaultUtil.getSearchVaultPattern(context)` 만 부름. `VaultUtil.getSearchVaultPattern`: `CacheUtil` 의 `isemxKweTypesBLExist` (없으면 `list program $1` 로 확인) 가 `true` 면 `JPO.invoke(context, "emxKweTypesBL", ..., "getSearchVaultsForType", ...)`, 결과가 `0` 이면 `""`, `0|` 로 시작하면 `|` 를 `,` 로. 비면 `MatrixException` 을 던져 잡고 `CacheManager.getValue(context, SEARCH_VAULTS, ...)` 를 `FrameworkUtil.join(..., ",")`. `getSearchVaults` 는 `split(",")` 의 `Arrays.asList`. `getVaults(Context)` 는 `Vault.getVaults(context, true, false)` 를 돌며 `open` / `getDescription` / `close`. pnocommon.jar `Person.getVaultName` → `getCompanyVaultName` → `getCompany(context).getVault()`. 로컬 실행: `VaultUtil` 은 static 초기화에서 `jakarta.servlet` 이 없어 `NoClassDefFoundError` 라 실행하지 않음
:::

## 샘플 JPO

[XX_UtilPerson_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilPerson_mxJPO.java) 의 메서드입니다. 모든 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map (`programMap`) 에서 값을 읽습니다. `userName` 을 비우면 로그인 사용자 (`context.getUser()`) 로 봅니다. 데이터를 바꾸는 메서드는 없습니다.

| 메서드 | 받는 값 (`programMap` 의 키) | 돌려주는 값 | 보여 주는 것 |
| --- | --- | --- | --- |
| `getLoginUserInfo` | 없음 | `Map` (`userName`, `personId`, `fullName`, `email`) | [로그인 사용자 이름](#로그인-사용자-이름), [Person 객체 id](#person-객체-id), [화면에 보이는 이름](#화면에-보이는-이름), [메일 주소](#메일-주소) |
| `getPersonDetails` | `userName` | `Map` (`name`, `firstName`, `company`) | [Person 객체의 값 읽기](#person-객체의-값-읽기). 회사 이름을 select 로 함께 읽음 |
| `getCompanyId` | 없음 | `String` (회사 객체 id) | [회사](#회사) |
| `getMemberOrganizationIds` | `userName` | `StringList` (조직 객체 id) | [소속 조직](#소속-조직). `null` 이면 빈 목록 |
| `getCurrentSecurityContext` | 없음 | `Map` (`securityContext`, `organization`, `project`) | [지금 보안 컨텍스트](#지금-보안-컨텍스트) |
| `getDefaultSecurityContext` | `userName` | `Map` (같은 키) | [기본 보안 컨텍스트](#기본-보안-컨텍스트). 비었으면 조직, 협업공간을 찾지 않음 |
| `getSecurityContextNames` | `userName` | `StringList` | [가진 보안 컨텍스트 목록](#가진-보안-컨텍스트-목록) |
| `getProjectsOfOrganization` | `userName`, `organization` (비우면 모든 조직) | `StringList` (협업공간 이름) | `getProjects` ([가진 보안 컨텍스트 목록](#가진-보안-컨텍스트-목록)) |
| `hasTeamLeaderRole` | 없음 | `Boolean` | `hasAssignment` 에 실제 이름 ([역할을 가졌는지 확인](#역할을-가졌는지-확인)) |
| `hasAnyReviewRole` | 없음 | `Boolean` | `hasAnyAssignment` 에 심볼릭 이름 |
| `isTeamLeader` | `userName` | `Boolean` | 다른 사용자의 역할 (`Person.hasRole`) |
| `getUserAssignments` | `userName` | `StringList` | [가진 역할 목록](#가진-역할-목록). 캐시의 `Vector` 를 복사 |
| `isAdminContext` | 없음 | `Boolean` | [보안 컨텍스트의 역할 확인](#보안-컨텍스트의-역할-확인) |
| `getTeamLeaders` | 없음 | `StringList` (사람 이름) | [역할을 가진 사람 목록](#역할을-가진-사람-목록) |
| `getGroupMembers` | `groupName` (그룹 이름이나 id) | `MapList` (`name`, `id`) | [사용자 그룹의 사람 목록](#사용자-그룹의-사람-목록) |
| `getVaults` | 없음 | `Map` (`loginVault`, `searchVaults`) | [vault](#vault) |

::: details XX_UtilPerson_mxJPO.java

<<< @/../JPOSample/util/XX_UtilPerson_mxJPO.java

:::

::: warning
샘플의 역할 이름 (`role_MyTeamLeader`, `role_MyReviewer`) 은 예시입니다. 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다.
:::

## 다루지 않은 메서드

::: details 펼쳐 보기
| 클래스 | 메서드 | 이유 |
| --- | --- | --- |
| PersonUtil | `checkFDAAuthentication`, `getAssignmentsForDisplay`, `getCollaborativeSpaceCommands_New`, `getCompanyNameFromTitle`, `getCompanyTitleFromName`, `getHomePageURL`, `getJsonMxTableMenuSelection`, `getLastQuery`, `getMXTableProgramSelection`, `getOrganizationsForDisplay`, `getOrganizationsForDisplayForSCSelection`, `getProjectTitle`, `getSecurityContextAssignmentsForDisplay`, `getSecurityContextsNamesForDisplay`, `getUserRolesTitles`, `setAssignmentsForDisplay`, `setLastQuery`, `setLastQueryProperty`, `setSecurityContext`, `updateMxTableMenuSelection` | 화면용 |
| PersonUtil | `getActualTimeZonePreference`, `getAssignedTaskNotification`, `getCaseSensitivePreference`, `getChangeOwnerNotification`, `getChannelReports`, `getChoices`, `getCommand`, `getCurrency`, `getDateFormat`, `getDayLightTimeZoneValue`, `getDaylightSavingPreference`, `getDecimalSymbol`, `getDefault`, `getDiscussionSort`, `getDiscussionView`, `getEmailNotificationsPreference`, `getExportFormat`, `getExportFormatChoices`, `getExportFormatDefault`, `getFeatureNameDisplayPreference`, `getFieldSeparator`, `getFieldSeparatorChoices`, `getFieldSeparatorDefault`, `getImagesForms`, `getImagesFormsPreference`, `getImagesTables`, `getImagesTablesPreference`, `getLanguage`, `getLanguageChoices`, `getLanguageDefault`, `getLastContainedInSearchedTypePreference`, `getLastSearchedTypePreference`, `getLateTaskNotification`, `getMenu`, `getPagination`, `getPaginationPreference`, `getPaginationRange`, `getPaginationRangePreference`, `getPreferenceDateFormatString`, `getPreferenceDateFormatValue`, `getPreferenceDisplayTime`, `getPreferenceDisplayTimeValue`, `getProductConfigurationModePreference`, `getRecordSeparator`, `getRejectedTimesheetNotification`, `getRemoveCarriageReturnChoices`, `getRemoveCarriageReturns`, `getRemoveCarriageReturnsDefault`, `getRuleDisplay`, `getRuleDisplayPreference`, `getSearchDefaultSelection`, `getSelectedLayout`, `getSubmittedTimesheetNotification`, `getTimeDisplay`, `getTimeZonePreference`, `getToolbarPreference`, `getTreeDisplayPreference`, `getUnitOfMeasure`, `setAssignedTaskNotificationPreference`, `setCaseSensitivePreference`, `setChannelReports`, `setCommand`, `setCurrency`, `setDateFormat`, `setDaylightSavingPreference`, `setDefaultSecurityContext`, `setDiscussionSort`, `setDiscussionView`, `setEmailNotificationsPreference`, `setExportFormat`, `setFeatureNameDisplayPreference`, `setFieldSeparator`, `setImagesFormsPreference`, `setImagesTablesPreference`, `setLanguage`, `setLastContainedInSearchedTypePreference`, `setLastSearchedTypePreference`, `setLateTaskNotificationPreference`, `setMenu`, `setPaginationPreference`, `setPaginationRangePreference`, `setProductConfiguratorModePreference`, `setRecordSeparator`, `setRejectedTimesheetNotificationPreference`, `setRemoveCarriageReturns`, `setRuleDisplayPreference`, `setSearchDefaultVaults`, `setSelectedLayout`, `setSubmittedTimesheetNotificationPreference`, `setTimeDisplay`, `setTimeZonePreference`, `setToolbarPreference`, `setTreeNameDisplayPreference`, `setUnitOfMeasure` | 환경설정 |
| PersonUtil | `clearCache`, `clearPersonCache`, `clearTenantCache`, `clearUserCacheProperty`, `getChildrenUser`, `getPersonMap`, `isPersonObjectSchemaExist`, `refreshUserAssignments`, `setAssignments`, `setFullName`, `setPersonProperty`, `updateLastLogin`, `updateUserAssignments` | 내부용 |
| PersonUtil | `getLicenseProductsBySuiteKey` | 라이선스 |
| PersonUtil | `addSecurityContextAssignment`, `removeSecurityContextAssignment`, `updateSecurityContextAssignments`, `getActiveSCRole`, `getPersonObjectPhysicalID`, `getRoles`, `getSCUserRoles`, `getSecurityContext`, `removeDuplicates`, `isLoggedInUserCR`, `getVPLMChildrenRoleList` <Badge type="warning" text="폐기" /> | OOTB 미사용 |
| PersonUtil | `isEnterpriseScopeVisible`, `isRaceProject` | 앱 전용 |
| Person (pnocommon.jar) | `getPersonsFromNames`, `getPersonsFromIds` | 같은 일 다른 메서드 |
| VaultUtil | `isMultiValutEnabled` | 내부용 |
:::
