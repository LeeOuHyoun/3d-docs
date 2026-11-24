# 스키마 이름, 타입·정책·속성 정보 <Badge type="info" text="원본 분석" />

심볼릭 이름과 실제 이름을 서로 바꾸고, 객체 없이 타입, 정책, 속성 같은 관리 객체의 정보를 읽는 메서드입니다. 예제의 `part` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다. util 전체를 하고 싶은 일로 찾으려면 [유틸리티](/jpo/api/utilities) 를 봅니다.

::: info 근거
xerinfra.jar (`PropertyUtil`, `FrameworkUtil`, `mxType`, `mxAttr`, `mxBus`, `UOMUtil`, `DomainObject`, `DomainRelationship`, `com.matrixone.apps.cache` 의 `CacheManager`, `SymbolicNameCache`, `ReverseSymbolicNameCache`, `TypePolicyCache`), eMatrixServletRMI.jar (`BusinessType`, `AttributeType`, `Unit`) class 파일 (javap) 기준. 건수는 OOTB JPO 에서 센 호출 수: `PropertyUtil.getSchemaProperty(` 5,105, `FrameworkUtil.getAliasForAdmin(` 199, `FrameworkUtil.lookupStateName(` 171, `mxType.isOfParentType(` 140, `DomainObject.getAttributeSelect(` 93, `PropertyUtil.getAdminProperty(` 34, `FrameworkUtil.getRanges(` 34, `FrameworkUtil.reverseLookupStateName(` 27, `UOMUtil.isAssociatedWithDimension(` 25, `mxType.getPolicies(` 20, `FrameworkUtil.getPolicyClassification(` 19
:::

샘플: [XX_UtilSchema_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilSchema_mxJPO.java) ([메서드별 설명](#샘플-jpo))

## 한눈에 보기

| 하고 싶은 일 | 쓸 메서드 |
| --- | --- |
| 심볼릭 이름을 실제 이름으로 | [`PropertyUtil.getSchemaProperty(context, "type_MyPart")`](#실제-이름으로-바꾸기) |
| 실제 이름을 심볼릭 이름으로 | [`FrameworkUtil.getAliasForAdmin(context, "type", type, true)`](#심볼릭-이름으로-바꾸기) |
| 심볼릭 상태 이름을 실제 상태 이름으로 | [`PropertyUtil.getSchemaProperty(context, "policy", policy, "state_Release")`](#실제-상태-이름으로-바꾸기) |
| 실제 상태 이름을 심볼릭 상태 이름으로 | [`FrameworkUtil.reverseLookupStateName(context, policy, current)`](#심볼릭-상태-이름으로-바꾸기) |
| 타입, 정책 같은 관리 객체의 property 읽기 | [`PropertyUtil.getAdminProperty(context, "type", typePart, "MyVersioning")`](#관리-객체의-property) |
| 관리 객체의 property 쓰기, 지우기 | [`PropertyUtil.setAdminProperty`, `PropertyUtil.removeAdminProperty`](#관리-객체의-property) |
| 타입 이름만으로 상속 관계 확인 | [`mxType.isOfParentType(context, type, typePart)`](#상속-관계-확인) |
| 타입의 맨 위 타입 | [`FrameworkUtil.getBaseType(context, type, null)`](#맨-위-타입) |
| 타입에 쓸 수 있는 정책 목록 | [`mxType.getPolicies(context, typePart, false)`](#정책-목록) |
| 타입의 기본 정책과 첫 리비전 | [`mxType.getDefaultPolicy(context, typeDocument, false)`](#기본-정책) |
| 타입에 정의된 속성 목록 | [`mxType.getAttributes(context, typePart)`](#속성-목록) |
| 타입에 그 속성이 있는지 | [`FrameworkUtil.isAttributeOnType(context, typePart, attrStatus)`](#속성이-있는지) |
| 속성의 범위 값 | [`FrameworkUtil.getRanges(context, attrStatus)`](#범위-값) |
| 속성 값을 읽는 select 만들기 | [`DomainObject.getAttributeSelect(attrStatus)`](#속성-select-만들기) |
| 속성에 단위가 있는지, 저장 단위 | [`UOMUtil.isAssociatedWithDimension`, `UOMUtil.getDBunit`](#단위가-있는-속성) |
| 값을 저장 단위로 바꾸기 | [`UOMUtil.convertToDefaultUnit(context, attr, value, unit)`](#단위가-있는-속성) |
| 객체의 타입, 상태, 리비전, 속성 값 읽고 바꾸기 | [`obj.getInfo` 같은 DomainObject 메서드](#객체의-값-읽고-바꾸기) |

## 심볼릭 이름과 실제 이름

스키마 이름은 바뀔 수 있어서 코드에는 심볼릭 이름 (`type_MyPart`) 을 적고 실행할 때 실제 이름 (`My Part`) 으로 바꿉니다. 등록 방법과 배경은 [심볼릭 이름](/jpo/domain-object#심볼릭-이름) 에 있습니다.

### 실제 이름으로 바꾸기

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PropertyUtil.getSchemaProperty(context, symbolicName)` | `symbolicName`: 심볼릭 이름 (`type_MyPart`, `relationship_MyPartDocument`, `attribute_MyStatus`, `policy_...`, `role_...`) | `String`. 실제 이름. 등록 안 된 이름이나 `null`, `""` 이면 `""` |

| 호출 | 결과 |
| --- | --- |
| `getSchemaProperty(context, "type_MyPart")` | `My Part` (예시) |
| `getSchemaProperty(context, "type_MyPartt")` (등록 안 된 이름) | `""` |
| `getSchemaProperty(context, null)`, `(context, "")` | `""`. 서버에 가지 않음 |

```java
String typePart = PropertyUtil.getSchemaProperty(context, "type_MyPart");
if (typePart.isEmpty()) {
    throw new FrameworkException("type_MyPart is not registered");
}
```

- 처음 부를 때만 MQL `print program eServiceSchemaVariableMapping.tcl select property[type_MyPart].to dump` 를 부릅니다. 결과 `type My Part` 에서 첫 단어 (관리 객체 종류) 를 뺀 나머지를 돌려주고, 서버 메모리의 캐시에 테넌트별로 담아 둡니다. 다음부터는 서버에 가지 않습니다.
- 등록 안 된 이름은 캐시에 담지 않습니다. 부를 때마다 MQL 을 다시 부르고 늘 `""` 이 옵니다.
- `""` 을 그대로 넘기면 오류 없이 엉뚱한 결과가 나옵니다 (`getRelatedObjects` 는 관계 패턴이 비면 모든 관계를 따라감). 위처럼 비었는지 확인합니다.
- 예외를 던지지 않습니다. 안에서 난 `FrameworkException` 은 스택만 찍고 `""` 을 돌려줍니다.
- 등록을 바꿔도 이미 담긴 값은 그대로입니다. 비우는 메서드 `PropertyUtil.cacheSymbolicNames(context)` 는 OOTB 가 회사 이름을 바꿀 때만 부르는 내부용입니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `PropertyUtil.getSchemaProperty(symbolicName)` <Badge type="warning" text="폐기" /> | 폐기. context 를 받지 않아 안에서 context 를 따로 얻어 (`ContextUtil.runInAnonymousContext`) 같은 캐시를 읽음. OOTB 에 743 건 남아 있음 |

::: info 근거
`getSchemaProperty(Context, String)` 은 `CacheManager.getValue(context, SYMBOLIC_NAMES, [이름])` 이 `null` 이면 `""`, `FrameworkException` 은 `printStackTrace` 뒤 `""`. `CacheManager.getValue` 는 테넌트 (`context.getTenant()`, 없으면 `globalUser`) 별 `ConcurrentHashMap` 에서 찾고, 없으면 `SymbolicNameCache.populateCache` 를 부름. `SymbolicNameCache.isFullCache()` 가 `false` 라 못 찾을 때마다 다시 부름. `populateCache` 는 이름이 비면 아무것도 하지 않고, 아니면 MQL `print program $1 select $2 dump` (`eServiceSchemaVariableMapping.tcl`, `property[이름].to`) 결과를 `split(" ")` 해 두 번째 조각부터 이어 붙여 `trim` 한 값이 비지 않을 때만 캐시에 넣음. `cacheSymbolicNames(Context)` 는 `CacheManager.clearTenant(context, SYMBOLIC_NAMES)`. OOTB 호출은 emxCompanyBase_mxJPO.java, emxPropertyUtilBase_mxJPO.java 뿐. `getSchemaProperty(String)` 은 `Deprecated`, `runInAnonymousContext` 안에서 같은 `CacheManager.getValue` 를 부름
:::

### 심볼릭 이름으로 바꾸기

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.getAliasForAdmin(context, adminType, name, true)` | `adminType`: 관리 객체 종류 (`type`, `relationship`, `attribute`, `policy`, `role` ...). `name`: 실제 이름. `true`: 담아 둔 값을 씀 | `String`. 심볼릭 이름 (`type_MyPart`). 등록 안 됐거나 없는 이름이면 `null`. `adminType`, `name` 이 비어도 `null` |

| 호출 | 결과 |
| --- | --- |
| `getAliasForAdmin(context, "type", "My Part", true)` | `type_MyPart` (예시) |
| `getAliasForAdmin(context, "type", "", true)` | `null`. 서버에 가지 않음 |

```java
Map info = part.getInfo(context, StringList.create(DomainConstants.SELECT_TYPE, DomainConstants.SELECT_POLICY));
String symbolicType = FrameworkUtil.getAliasForAdmin(context, "type", (String) info.get(DomainConstants.SELECT_TYPE), true);
String symbolicPolicy = FrameworkUtil.getAliasForAdmin(context, "policy", (String) info.get(DomainConstants.SELECT_POLICY), true);
```

- 처음 찾을 때 MQL 을 두 번 부릅니다. `list 종류 이름` 으로 있는지 보고, `list property to 종류 이름 on program eServiceSchemaVariableMapping.tcl` 결과의 첫 단어를 돌려줍니다. 찾은 값은 테넌트별 캐시에 담습니다.
- 등록 안 된 이름은 담지 않아 부를 때마다 MQL 두 번입니다.
- 마지막 인자를 `false` 로 주면 담긴 값이 있어도 매번 MQL 로 다시 읽습니다. OOTB 는 `true` 150 건, `false` 35 건입니다.
- MQL 이 실패하면 `System.out` 에 `Can't get symbolic name for 종류 이름` 을 찍고 `null` 을 돌려줍니다.
- 상태 이름은 이 메서드로 바꾸지 못합니다 ([심볼릭 상태 이름으로 바꾸기](#심볼릭-상태-이름으로-바꾸기)).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `PropertyUtil.getAliasForAdmin(context, adminType, name, useCache)` <Badge type="warning" text="폐기" /> | 폐기. `FrameworkUtil.getAliasForAdmin` 이 안에서 그대로 부름 |
| `DomainObject.getAliasForName(context, name, useCache)` <Badge type="warning" text="폐기" /> | 폐기. `getAliasForAdmin(context, "attribute", name, useCache)` 를 그대로 부름 ([getAliasForName](/jpo/api/domain-object#getaliasforname)) |
| `FrameworkUtil.getTypeRegistrationName(context, type, servletContext)` <Badge type="warning" text="폐기" /> | 폐기. `ServletContext` 는 쓰지 않고 `type` 으로 같은 캐시를 읽음. 오류면 `null` |

::: info 근거
`FrameworkUtil.getAliasForAdmin` 은 `PropertyUtil.getAliasForAdmin` (`Deprecated`) 을 그대로 부름. `PropertyUtil.getAliasForAdmin` 은 종류나 이름이 `UIUtil.isNullOrEmpty` 면 `null`, 아니면 `CacheManager.getValue(context, REVERSE_SYMBOLIC_NAMES, !useCache, [종류, 이름])` (세 번째 인자가 `true` 면 캐시를 다시 채움). `ReverseSymbolicNameCache.isFullCache()` 는 `false`. `populateCache` 는 MQL `list $1 $2` 결과가 있으면 `list property to $1 $2 on program $3` 결과를 `split(" ")` 한 첫 조각을 캐시에 넣고, `FrameworkException` 이면 `System.out.println`. `getTypeRegistrationName` 은 `Deprecated`, `CacheManager.getValue(context, REVERSE_SYMBOLIC_NAMES, ["type", 이름])`, `FrameworkException` 이면 `null`. 호출 형태: `getAliasForAdmin(context, "type", ...)` 59, `"policy"` 26, `"attribute"` 5, `"relationship"` 5. `getAliasForAdmin` 은 `UIUtil` 이 `jakarta` 를 찾지 못해 로컬 실행이 막혀 바이트코드로만 확인함
:::

## 상태 이름

상태의 심볼릭 이름 (`state_Release`) 은 정책에 붙은 property (`property state_Release value Release`) 입니다. 그래서 정책 이름과 함께 바꿉니다. 정책마다 상태 이름이 다른 예는 [state 이름과 심볼릭 이름](/schema/policy#state-이름과-심볼릭-이름) 에 있습니다.

### 실제 상태 이름으로 바꾸기

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PropertyUtil.getSchemaProperty(context, "policy", policy, symbolicState)` | `policy`: 정책의 실제 이름 (객체의 `policy` select 값). `symbolicState`: 심볼릭 상태 이름 (`state_Release`) | `String`. 실제 상태 이름. 인자가 하나라도 `null` 이나 `""` 이면 `""`. MQL 이 실패하면 (정책이 없을 때 등) `null`. 등록 안 된 상태 이름이면 MQL 이 돌려준 빈 값 <Badge type="danger" text="검증 필요" /> |

| 호출 | 결과 |
| --- | --- |
| `getSchemaProperty(context, "policy", "My Part Policy", "state_Release")` | `Release` (예시) |
| `getSchemaProperty(context, "policy", "", "state_Release")` | `""`. 서버에 가지 않음 (로컬 실행) |
| `getSchemaProperty(context, "policy", null, "state_Release")` | `""` (로컬 실행) |
| `getSchemaProperty(context, "policy", "My Part Policy", "")` | `""` (로컬 실행) |

```java
Map info = part.getInfo(context, StringList.create(DomainConstants.SELECT_POLICY, DomainConstants.SELECT_CURRENT));
String policy = (String) info.get(DomainConstants.SELECT_POLICY);
String current = (String) info.get(DomainConstants.SELECT_CURRENT);
String release = PropertyUtil.getSchemaProperty(context, "policy", policy, "state_Release");
boolean released = current.equals(release);
```

위 변수의 값 (예시): `policy` 는 `My Part Policy`, `release` 는 `Release`.

- 같은 정책, 같은 상태는 처음 한 번만 MQL `print policy "정책" select property[state_Release].value dump;` 을 부릅니다. 결과는 서버 메모리 (static `Hashtable`) 에 담고 비우지 않습니다. 빈 값도 담으므로 정책에 상태 property 를 나중에 붙였으면 서버를 다시 띄우기 전까지 빈 값이 나옵니다.
- MQL 이 실패한 결과 (`null`) 는 담지 않습니다. 정책 이름이 틀리면 부를 때마다 MQL 을 부르고 `null` 이 옵니다. 그래서 위 예제는 `current.equals(release)` 처럼 `current` 쪽에서 비교합니다.
- 예외를 던지지 않습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `FrameworkUtil.lookupStateName(context, policy, symbolicState)` | 담아 두지 않아 부를 때마다 MQL. 결과가 비면 `null`, 정책이 없으면 `FrameworkException` |
| `PropertyUtil.getSchemaProperty("policy", policy, symbolicState)` <Badge type="warning" text="폐기" /> | 폐기. `context` 를 `null` 로 위 메서드를 부름 (안에서 context 를 따로 얻음) |
| `PropertyUtil.getAdminProperty(context, "policy", policy, symbolicState)` | 같은 `property[이름].value` 를 읽는 일반 메서드 ([관리 객체의 property](#관리-객체의-property)). 상태 이름은 OOTB 대로 `getSchemaProperty` 로 통일 |

::: info 근거
`getSchemaProperty(Context, String, String, String)`: 세 인자 중 하나라도 `null` 이나 길이 0 이면 `""` (로컬 실행으로도 확인). 아니면 `"종류|이름|property"` 키로 static `_TNPPropertyHash` 를 찾고, 없으면 MQL `print $1 "$2" select $3 dump;` (`property[이름].value`) 를 부름 (`context` 가 `null` 이면 `runInAnonymousContext`). 결과가 `null` 이 아니면 넣음. `Exception` 은 잡아서 `null`. `_TNPPropertyHash` 는 static 초기화 말고는 비우는 코드가 없고, 키에 테넌트가 없음. `lookupStateName` 은 정책이나 상태가 `null` 이나 `""` 이면 `null` (로컬 실행으로도 확인), 아니면 MQL `print policy $1 select $2 dump $3` (`property[state_Release]`, `|`) 결과에서 ` value` 뒤 글자를 돌려주고, 결과가 비면 `null`, `MatrixException` 은 `FrameworkException` 으로 던짐. 없는 property 를 select 했을 때 MQL 이 빈 값을 돌려주는지는 서버에서 확인하지 못함 (OOTB `getPolicyClassification` 도 빈 결과를 "property 없음" 으로 처리). 호출 형태: `getSchemaProperty(context, "policy", ...)` 187, `getSchemaProperty("policy", ...)` 103
:::

### 심볼릭 상태 이름으로 바꾸기

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.reverseLookupStateName(context, policy, state)` | `policy`: 정책의 실제 이름. `state`: 실제 상태 이름 (`current` select 값) | `String`. 심볼릭 상태 이름 (`state_Release`). 못 찾거나 인자가 `null` 이면 `null` |

| 호출 | 결과 |
| --- | --- |
| `reverseLookupStateName(context, "My Part Policy", "Release")` | `state_Release` (예시) |
| `reverseLookupStateName(context, null, "Release")` | `null`. 서버에 가지 않음 (로컬 실행) |

```java
Map info = part.getInfo(context, StringList.create(DomainConstants.SELECT_POLICY, DomainConstants.SELECT_CURRENT));
String symbolicState = FrameworkUtil.reverseLookupStateName(context,
        (String) info.get(DomainConstants.SELECT_POLICY), (String) info.get(DomainConstants.SELECT_CURRENT));
```

- 담아 두지 않아 부를 때마다 MQL `print policy 정책 select property dump |` 를 부릅니다. `이름 value 값` 모양의 property 중 값이 `state` 와 같은 첫 property 의 이름을 돌려줍니다. 이름에 `BaselineMapped` 가 들어간 property 는 건너뜁니다.
- 못 찾으면 안에서 예외를 만들지만 던지지 않고 `null` 을 돌려줍니다. 정책이 없어 MQL 이 실패해도 `null` 입니다.
- 상태를 비교할 때는 이 메서드보다 [실제 상태 이름으로 바꾸기](#실제-상태-이름으로-바꾸기) 로 바꾼 이름과 `current` 를 비교하는 쪽이 서버를 덜 다녀옵니다.

::: info 근거
`reverseLookupStateName`: `policy` 나 `state` 가 `null` 이면 `null` (로컬 실행으로도 확인). MQL `print policy $1 select $2 dump $3` (`property`, `|`) 결과를 `StringTokenizer(결과, "|")` 로 나눠, 조각마다 첫 공백 앞을 이름, 첫 공백 위치 + 7 (` value `) 다음을 값으로 봄. 값이 `state` 와 같고 이름에 `BaselineMapped` 가 없으면 그 이름. 결과가 비면 `emxFramework.FrameworkUtil.Error_GetSymbolicName` 문구로 `FrameworkException` 을 던지지만, 같은 메서드의 `MatrixException` catch (`FrameworkException` 은 `MatrixException` 의 하위 클래스) 가 받아 새 `FrameworkException` 을 만들기만 하고 버린 뒤 결과를 돌려줌
:::

## 관리 객체의 property

타입, 정책, 속성, 형식 같은 관리 객체에 붙은 property (`property MyVersioning value false`) 를 읽고 씁니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `PropertyUtil.getAdminProperty(context, adminType, name, property)` | `adminType`: `type`, `policy`, `attribute`, `format` ... (`null` 이면 `person`). `name`: 관리 객체 이름 (`null` 이면 로그인 사용자). `property`: property 이름 | `String`. property 값. 없는 property 면 MQL 이 돌려준 빈 값 <Badge type="danger" text="검증 필요" /> |
| `PropertyUtil.setAdminProperty(context, adminType, name, property, value)` | `value`: 넣을 값. `null` 이면 `NullPointerException`, `"null"` (대소문자 무시) 이면 아무것도 하지 않음 | 없음. 슈퍼유저로 property 를 넣거나 바꿈 |
| `PropertyUtil.removeAdminProperty(context, adminType, name, property)` | 위와 같음 | 없음. property 가 있을 때만 슈퍼유저로 지움 |

```java
String policy = part.getInfo(context, DomainConstants.SELECT_POLICY);
String classification = PropertyUtil.getAdminProperty(context, "policy", policy, "PolicyClassification");
String versioning = PropertyUtil.getAdminProperty(context, "type", typePart, "MyVersioning");
PropertyUtil.setAdminProperty(context, "type", typePart, "MyVersioning", "false");
PropertyUtil.removeAdminProperty(context, "type", typePart, "MyVersioning");
```

- `getAdminProperty` 는 그 관리 객체를 처음 읽을 때 property 를 모두 읽어 (`print 종류 "이름" select property.name property.value dump ~|~`) 서버 메모리에 테넌트, 종류, 이름별로 담아 둡니다. 다음부터는 서버에 가지 않습니다.
- 담아 둔 값은 `setAdminProperty`, `removeAdminProperty` 로 바꿀 때만 고쳐집니다. MQL 로 직접 바꾼 값은 이 서버의 캐시에 들어가지 않습니다.
- 없는 관리 객체면 MQL 오류로 `FrameworkException` 이 납니다.
- `setAdminProperty` 와 `removeAdminProperty` 는 MQL 을 슈퍼유저 (User Agent) 로 실행합니다 (`modify 종류 "이름" property "이름" value "값"`, `modify 종류 "이름" remove property "이름"`). 로그인 사용자의 권한과 관계없이 스키마가 바뀌므로 꼭 필요할 때만 씁니다.
- `setAdminProperty` 는 지금 값과 같으면 아무것도 하지 않습니다. 바꾼 뒤에는 다른 앱 서버의 캐시도 고치도록 `framework.ui.CacheManager` 를 부릅니다. `removeAdminProperty` 는 이 서버의 캐시만 고칩니다.
- 정책의 분류 (`Production` 같은 값) 는 정책의 `PolicyClassification` property 입니다. 위 예제처럼 읽습니다 (OOTB emxECPartBase_mxJPO.java).
- 모든 property 를 한 MQL 결과로 받아 `~|~` 로 나눠 앞 절반을 이름, 뒤 절반을 값으로 짝짓습니다. Java `split` 은 끝의 빈 문자열을 버리므로 마지막 property 의 값이 비어 있으면 짝이 어긋날 수 있습니다 <Badge type="danger" text="검증 필요" />.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `PropertyUtil.getSchemaProperty(context, adminType, name, property)` | 같은 `property[이름].value` 를 읽지만 비우지 않는 별도 캐시에 담아 `setAdminProperty` 로 바꿔도 그대로. 상태 이름에만 씀 |
| `FrameworkUtil.getPolicyClassification(context, policy)` | `PolicyClassification` 만 읽음. 담아 두지 않아 부를 때마다 MQL, 값이 비면 `FrameworkException` |

::: info 근거
`getAdminProperty`: 종류가 `null` 이면 `person`, 이름이 `null` 이면 `context.getUser()`. `loadAdminProperties` 는 키 (`PersonUtil.getTenant(context) + ":" + 종류 + ":" + 이름`) 가 static `_AdminPropertyCache` 에 없으면 MQL `print $1 "$2" select $3 $4 dump $5` (`property.name`, `property.value`, `~|~`) 결과를 `split("~\\|~")` 해 앞 절반을 이름, 뒤 절반을 값으로 담음. 그 Map 에 없는 property 는 `print $1 "$2" select $3 dump` (`property[이름].value`) 결과를 담음. `setAdminProperty` 는 `value.equals(getAdminProperty(...))` 를 먼저 부르고 (그래서 `null` 이면 `NullPointerException`), `"null"` 이면 끝. 다르면 `MqlUtil.mqlCommand(context, "modify $1 \"$2\" property \"$3\" value \"$4\"", true, ...)` (`true` 는 `pushContext` / `popContext`), 캐시를 고치고 `CacheManager.hasAppCacheServerList` 면 `updateRemoteAPPServerAdminCache`, 끝으로 `updateRMIAdminCache`. `removeAdminProperty` 는 슈퍼유저로 `print ... select property[이름] dump` 가 비지 않으면 `modify $1 "$2" remove property "$3"` 뒤 이 서버 캐시에서만 지움. `getPolicyClassification` 은 MQL `print policy $1 select $2 dump` (`property[PolicyClassification].value`) 를 `trim` 한 값이 비면 `emxFramework.FrameworkUtil.Error_GetPolicy` 문구로 `FrameworkException`. OOTB 호출: `setAdminProperty` 2, `removeAdminProperty` 0
:::

## 타입 정보

객체 없이 타입 이름으로 상속 관계, 정책, 속성을 읽습니다. 타입 이름은 실제 이름을 넘깁니다. MQL 에서 보는 방법은 [상속 확인](/schema/admin#상속-확인) 에 있습니다.

### 상속 관계 확인

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `mxType.isOfParentType(context, type, parentType)` | `type`: 확인할 타입. `parentType`: 상위 타입 | `boolean`. `type` 이 `parentType` 이거나 그 하위 타입이면 `true` |

| 호출 | 결과 |
| --- | --- |
| `isOfParentType(context, "My Part", "My Part")` | `true`. 서버에 가지 않음 (로컬 실행) |
| `isOfParentType(context, null, typePart)` | `FrameworkException` (안의 `NullPointerException` 을 감쌈. 로컬 실행) |

```java
String typeSubPart = PropertyUtil.getSchemaProperty(context, "type_MySubPart");
boolean isPart = mxType.isOfParentType(context, typeSubPart, typePart);
```

- 두 이름이 같으면 서버에 가지 않고 `true` 입니다. 다르면 담아 두지 않고 MQL `print type 상위타입 select derivative[타입] dump` 결과가 `TRUE` (대소문자 무시) 인지 봅니다. 반복문에서 같은 타입을 여러 번 물으면 그만큼 MQL 을 부릅니다.
- 상위 타입이 없으면 MQL 오류로 `FrameworkException` 입니다.
- 객체가 있으면 `obj.isKindOf(context, type)` ([isKindOf](/jpo/api/domain-object#iskindof)) 를 쓰거나, 다른 값과 함께 읽을 때 select `type.kindof[타입]` 을 `getInfo` 에 넣습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `obj.isKindOf(context, type)` | 객체가 있을 때 쓰는 DomainObject 메서드 (객체 id 로 MQL). 타입 이름만 있을 때는 쓸 수 없음 |

::: info 근거
`mxType.isOfParentType`: `type.equals(parentType)` 이면 `true` (그래서 `type` 이 `null` 이면 `NullPointerException`, `Exception` catch 가 `FrameworkException` 으로 감쌈), 아니면 MQL `print type $1 select $2 dump` (`parentType`, `derivative[` + type + `]`) 결과를 `equalsIgnoreCase("true")`. OOTB `.isKindOf(` 671, select `type.kindof[` 325
:::

### 맨 위 타입

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.getBaseType(context, type, null)` | `type`: 타입 이름. 세 번째 인자 (`Vault`) 는 쓰지 않음 | `String`. MQL `print type 타입 select kindof dump` 결과. `type` 이 `""` 이면 `""`, `null` 이면 `NullPointerException` |

| 호출 | 결과 |
| --- | --- |
| `getBaseType(context, typeSubPart, null)` | `My Part` (예시) |
| `getBaseType(context, "", null)` | `""`. 서버에 가지 않음 (로컬 실행) |
| `getBaseType(context, null, null)` | `NullPointerException` (로컬 실행) |

```java
String typeSubPart = PropertyUtil.getSchemaProperty(context, "type_MySubPart");
String topType = FrameworkUtil.getBaseType(context, typeSubPart, null);
```

- 결과를 서버 메모리 (static `Hashtable`) 에 담고 비우지 않습니다. 같은 타입은 처음 한 번만 MQL 을 부릅니다.
- OOTB 는 결과를 맨 위 타입 이름 하나로 비교합니다 (emxPartMarkupBase_mxJPO.java 의 `sTopLevelType.equals(TYPE_PART)`, emxQuickFileAccessBase_mxJPO.java 의 `strRootType`). 서버에서 `kindof` 가 값 하나만 돌려주는지는 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.
- 객체가 있으면 select `type.kindof` 를 다른 값과 함께 `getInfo` 로 읽습니다 (OOTB emxExtendedHeaderBase_mxJPO.java).

::: info 근거
`getBaseType`: static `BASE_TYPES` 를 `synchronized` 로 감싸 `get(type)` (그래서 `type` 이 `null` 이면 `Hashtable.get` 의 `NullPointerException`), 없으면 `type` 이 비었을 때 `""` (담지 않음), 아니면 MQL `print type $1 select $2 dump` (`kindof`) 결과를 담음. `MatrixException` 은 `FrameworkException` 으로 던짐. `Vault` 인자는 읽지 않음
:::

### 정책 목록

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `mxType.getPolicies(context, type, false)` | `type`: 타입 이름. `false`: 타입에 걸린 정책 (`BusinessType.getPolicies`) | `MapList`. 정책마다 Map. 키는 `name`, `description`, `revision` (첫 리비전), `formats` (`StringList`), `defaultFormat`, `store` |

```java
MapList policies = mxType.getPolicies(context, typePart, false);
StringList policyNames = new StringList();
for (Object o : policies) {
    Map policy = (Map) o;
    policyNames.add((String) policy.get("name"));
}
```

- 서버를 여러 번 다녀옵니다: 타입을 열고, 정책마다 정책을 열고 MQL 세 번 (`format`, `defaultformat`, `store`). 담아 두지 않습니다.
- 두 번째 인자 `true` 는 `BusinessType.getPoliciesForPerson` 으로 읽습니다. OOTB 는 Program Central 쪽 (emxProjectSpaceBase_mxJPO.java, emxTaskBase_mxJPO.java) 이 씁니다. 결과가 어떻게 다른지는 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.
- 선언된 예외가 `FrameworkException` 이 아니라 `MatrixException` 입니다. 타입이 없으면 열 때 실패합니다.
- 객체에 지정된 정책 하나는 `getInfo(context, DomainConstants.SELECT_POLICY)` 로 읽습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `obj.getPolicies(context)` <Badge type="warning" text="폐기" /> | 폐기. 객체의 타입으로 `mxType.getPolicies(context, type, false)` 를 부름 ([getPolicies](/jpo/api/domain-object#getpolicies)) |
| `FrameworkUtil.getPolicyNames(context, type)` | 이름만 돌려줌 (MQL `print type 타입 select policy dump \|` 를 테넌트별로 담아 둠). OOTB 에서 부르지 않음 |

::: info 근거
`mxType.getPolicies`: `new BusinessType(type, context.getVault())` 를 `open`, `false` 면 `getPolicies(context)`, `true` 면 `getPoliciesForPerson(context, false)`. 정책마다 `Policy.open` 뒤 `name` (`getName`), `description` (`getDescription(context)`), `revision` (`getFirstInSequence`), MQL `print Policy $1 select $2 dump $3` 로 `format` (`FrameworkUtil.split` 한 `StringList`), `defaultformat`, `store`, 그리고 `Policy.close`. `FrameworkUtil.getPolicyNames` 는 `CacheManager.getValue(context, TYPE_POLICY, [type])`, `TypePolicyCache.populateCache` 는 MQL `print type $1 select $2 dump $3` (`policy`, `|`) 를 `StringUtil.split` 해 담음. OOTB 호출: `mxType.getPolicies(..., false)` 12, `(..., true)` 8
:::

### 기본 정책

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `mxType.getDefaultPolicy(context, type, false)` | `type`: 타입 이름 | `Map`. `getPolicies(context, type, false)` 의 첫 행 (키는 위와 같음). 정책이 없으면 `null` |

```java
Map defaultPolicy = mxType.getDefaultPolicy(context, typeDocument, false);
String policy = (String) defaultPolicy.get("name");
String firstRevision = (String) defaultPolicy.get("revision");
```

- 정책 이름은 `get("name")`, 그 정책의 첫 리비전은 `get("revision")` 입니다.
- `getPolicies` 목록의 첫 번째를 고릅니다. 정책을 모두 읽고 첫 번째만 쓰므로 서버에 가는 횟수는 `getPolicies` 와 같습니다.
- 실패하면 `FrameworkException` 입니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `obj.getDefaultPolicy(context, type)` | 안에서 `mxType.getDefaultPolicy(context, type, false)` 의 `name` 만 꺼냄. 객체 변수가 있어야 부를 수 있음 ([getDefaultPolicy](/jpo/api/domain-object#getdefaultpolicy)) |

::: info 근거
`mxType.getDefaultPolicy`: `getPolicies(context, type, boolean)` 이 비지 않으면 `get(0)`, 비면 `null`. `Exception` 은 `FrameworkException` 으로 던짐. `DomainObject.getDefaultPolicy` 는 `mxType.getDefaultPolicy(context, type, false)` 가 `null` 이 아니면 `get("name")`
:::

### 속성 목록

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `mxType.getAttributes(context, type)` | `type`: 타입 이름 | `MapList`. 속성마다 Map. 키는 `name`, `type` (자료형), `description`, `default` (기본값), `choices` (범위 값 `StringList`) |
| `mxType.getAttributes(context, type, true)` | 세 번째 인자 `true`: 자료형이 `string` 인 속성의 multiline 도 읽음 | 위 키에 `multiline` 이 더해짐. `string` 이면 MQL 결과 (`TRUE` / `FALSE`), 아니면 `"false"` |

```java
MapList attributes = mxType.getAttributes(context, typePart);
for (Object o : attributes) {
    Map attribute = (Map) o;
    String name = (String) attribute.get("name");
    String dataType = (String) attribute.get("type");
    StringList choices = (StringList) attribute.get("choices");
}
```

- 안에서 `ContextUtil.startTransaction(context, false)` 와 `commitTransaction` 으로 감쌉니다. 실패하면 abort 하고 `FrameworkException` 입니다.
- 타입을 열고, 속성마다 `AttributeType` 을 열어 읽습니다. 담아 두지 않습니다.
- `type` 값은 서버가 준 자료형 이름입니다 (`AttributeType.getDataType()`).
- 상속받은 속성이 목록에 들어가는지는 서버에서 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `DomainObject.getTypeAttributes(context, type)` <Badge type="tip" text="protected" /> | `protected`. 안에서 `mxType.getAttributes(context, type)` 를 그대로 부름 |
| `DomainObject.getTypeAttributeNames(context, type)` <Badge type="tip" text="protected" /> | `protected`. 이름만 (`BusinessType.getAttributeTypes`) |

::: info 근거
`mxType.getAttributes(Context, String)` 은 `getAttributes(context, type, false)`. 세 인자 형태는 `startTransaction(context, false)`, `BusinessType.open` 뒤 `getAttributeTypes(context)` 를 돌며 `AttributeType.open`, `name` (`toString`), `type` (`getDataType`), `description`, `default` (`getDefaultValue`), `choices` (`getChoices`) 를 넣고, `true` 면 `"string".equalsIgnoreCase(getDataType())` 일 때 MQL `print attribute $1 select $2 dump` (`multiline`), 아니면 `"false"`. 끝에 `BusinessType.close`, `commitTransaction`. `DomainObject.getTypeAttributes` 는 `mxType.getAttributes(context, type)` 만 부름
:::

### 속성이 있는지

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.isAttributeOnType(context, type, attribute)` | `type`: 타입 이름. `attribute`: 속성 이름 | `boolean`. 타입의 속성 목록에 같은 이름 (대소문자 구분) 이 있으면 `true` |

```java
boolean hasStatus = FrameworkUtil.isAttributeOnType(context, typePart, attrStatus);
```

- 담아 두지 않고 MQL `print type 타입 select attribute dump |` 한 번으로 이름만 봅니다. `mxType.getAttributes` 로 목록을 받아 찾는 것보다 서버를 덜 다녀옵니다.
- 타입이 없으면 MQL 오류로 `FrameworkException` 입니다.

::: info 근거
`isAttributeOnType`: MQL `Print type $1 select $2 dump $3` (`attribute`, `|`) 결과가 비지 않으면 `StringTokenizer(결과, "|")` 조각을 `trim` 해 `equals(attribute)`. `MatrixException` 은 `FrameworkException` 으로 던짐. OOTB emxECOBase_mxJPO.java
:::

## 속성 정보

### 범위 값

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `FrameworkUtil.getRanges(context, attribute)` | `attribute`: 속성 이름 | `StringList`. 속성에 정해 둔 범위 값 (`AttributeType.getChoices()`) |

```java
StringList statusRanges = FrameworkUtil.getRanges(context, attrStatus);
```

위 변수의 값 (예시): `[Approved, Rejected]`.

- 담아 두지 않고 부를 때마다 `AttributeType` 을 열어 읽고 닫습니다. 속성이 없으면 `FrameworkException` 입니다.
- [속성 목록](#속성-목록) 의 `choices` 도 같은 `getChoices()` 값입니다.
- 범위 값의 화면 표시 이름은 [설정값, 다국어 문구](/jpo/api/util/resource) 에서 바꿉니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `mxAttr.getChoices(context, attribute)` | 같은 코드 (`AttributeType.open` → `getChoices()` → `close`). 예외가 `FrameworkException` 으로 바뀌지 않고 `MatrixException` 으로 남음 |

::: info 근거
`FrameworkUtil.getRanges`: `new AttributeType(attribute)` 를 `open`, `getChoices()`, `close`. `MatrixException` 은 `FrameworkException` 으로 던짐. `mxAttr.getChoices` 는 같은 세 호출을 `throws MatrixException` 으로. OOTB 호출: `mxAttr.getChoices(` 3
:::

### 속성 select 만들기

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.getAttributeSelect(attribute)` | `attribute`: 속성 이름 | `String`. `attribute[이름]`. `null` 이면 `ERROR: NULL ATTRIBUTE`. 서버에 가지 않음 |

```java
String statusSelect = DomainObject.getAttributeSelect(attrStatus);
Map info = part.getInfo(context, StringList.create(DomainConstants.SELECT_NAME, statusSelect));
String status = (String) info.get(statusSelect);
```

위 변수의 값 (예시): `statusSelect` 는 `attribute[My Status]`, `status` 는 `Approved`.

- 결과 Map 에서 값을 꺼낼 때도 넣은 select 문자열을 그대로 키로 씁니다.
- 자세한 것은 [getAttributeSelect](/jpo/api/domain-object#getattributeselect) 에 있습니다.

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `DomainRelationship.getAttributeSelect(attribute)` | 만드는 문자열이 `attribute[이름].value` 로 다름 (로컬 실행). 섞어 쓰면 결과 Map 의 키가 달라짐 |

::: info 근거
`DomainObject.getAttributeSelect`: `null` 이면 `"ERROR: NULL ATTRIBUTE"`, 아니면 `"attribute[" + 이름 + "]"`. `DomainObject` 는 static 초기화에서 `jakarta.servlet` 을 찾지 못해 로컬 실행이 막혀 바이트코드로만 확인함. `DomainRelationship.getAttributeSelect("My Quantity")` 는 `attribute[My Quantity].value`, `null` 이면 `ERROR: NULL ATTRIBUTE` (로컬 실행으로 확인). OOTB 호출: `DomainObject.getAttributeSelect(` 93, `DomainRelationship.getAttributeSelect(` 63
:::

## 단위가 있는 속성

길이, 무게처럼 단위 (dimension) 가 붙은 속성은 저장 단위와 입력 단위가 따로 있습니다. 로직에 꼭 필요한 다섯 개만 둡니다. 나머지 `UOMUtil` 메서드 (화면 표시 문자열, 사용자 단위계 기준 값) 는 [다루지 않은 메서드](#다루지-않은-메서드) 에 있습니다.

| 쓸 메서드 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `UOMUtil.isAssociatedWithDimension(context, attribute)` | `attribute`: 속성 이름 | `boolean`. 단위가 있는 속성이면 `true`. 없는 속성이어도 예외 없이 `false` |
| `UOMUtil.getDBunit(context, attribute)` | 위와 같음 | `String`. 저장 단위 (dimension 의 기본 단위) 이름. 단위가 없는 속성이면 `""` |
| `UOMUtil.getDimensionUnits(context, attribute)` | 위와 같음 | `StringList`. 쓸 수 있는 단위 이름. 단위가 없으면 빈 목록 |
| `UOMUtil.getInputunit(context, objectId, attribute)` | `objectId`: 객체 id | `String`. 그 객체에 값을 넣을 때 쓴 단위 |
| `UOMUtil.convertToDefaultUnit(context, attribute, value, unit)` | `value`: 숫자 문자열. `unit`: `value` 의 단위 이름 | `String`. 저장 단위로 바꾼 값 |

| 호출 | 결과 |
| --- | --- |
| `convertToDefaultUnit(context, attr, "25", 저장단위)` | `25`. 넘긴 값 그대로 |
| `convertToDefaultUnit(context, attr, "10", "mm")` (`mm` 의 multiplier `0.001`, offset `0` 일 때) | `0.01` |
| 위에서 값이 `"0.1"` 일 때 | `1.0E-4` |
| 위에서 값이 `"1,000"` 일 때 | `0.001` (`,` 를 `.` 로 읽어 `1.0` 으로 봄) |
| `convertToDefaultUnit(context, attr, "25", "없는단위")` | `25`. 예외 없이 그대로 |

```java
String attrLength = PropertyUtil.getSchemaProperty(context, "attribute_MyLength");
if (UOMUtil.isAssociatedWithDimension(context, attrLength)) {
    String dbUnit = UOMUtil.getDBunit(context, attrLength);
    StringList units = UOMUtil.getDimensionUnits(context, attrLength);
    String inputUnit = UOMUtil.getInputunit(context, objectId, attrLength);
    String dbValue = UOMUtil.convertToDefaultUnit(context, attrLength, "25", "mm");
}
```

- 모두 담아 두지 않고 서버에서 속성의 dimension (`AttributeType.getDimension`) 을 읽습니다. `getInputunit` 은 객체를 열어 읽습니다.
- `convertToDefaultUnit` 은 `unit` 이 저장 단위면 `value` 를 그대로 돌려주고, 아니면 그 단위의 `값 × multiplier + offset` 을 계산합니다 (`Unit.normalize`). 결과는 `String.valueOf(double)` 이라 `0.01`, `25.0`, `1.0E-4` 같은 모양입니다.
- `unit` 이 그 속성의 단위가 아니거나 `value` 가 숫자가 아니어도 예외 없이 `value` 를 그대로 돌려줍니다. 바뀌었는지 알려면 결과와 `value` 를 비교합니다.
- OOTB 는 객체의 입력 값을 select `attribute[속성].inputvalue` 로도 읽습니다 (emxPartMarkupBase_mxJPO.java).

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `UOMUtil.getSystemunit(context, null, attribute, null)` | 객체 id 와 단위계가 비면 `getDBunit` 과 같은 기본 단위를 돌려줌. OOTB 화면 코드가 이 형태로 씀 |

::: info 근거
`isAssociatedWithDimension`, `getDBunit`, `getDimensionUnits` 는 private `getDimension` (`new AttributeType(이름).getDimension(context)`, 이름이 빈 dimension 은 `null`, `Exception` 은 버림) 을 씀. `getDBunit` 은 `Dimension.getDefaultUnit(context).getName()`, `getDimensionUnits` 는 `Dimension.getUnits(context, true)` 의 이름. `getInputunit(Context, String, String)` 은 private `getValue` 로 `new BusinessObject(id)` 를 열고 `getAttributesWithUnits(context, [속성], false, "")` 의 `getInputunit()`. `convertToDefaultUnit` 은 `unit.equals(getDBunit(...))` 면 그대로, 아니면 `Dimension.getUnits(context)` 에서 이름이 같은 `Unit` 의 `normalize(value)`, `Exception` 은 버리고 `value`. eMatrixServletRMI.jar `Unit.normalize` 는 `value.replace(',', '.')` 를 `Double.valueOf` 해 `* _dmultiplier + _doffset` 을 `String.valueOf`. 표의 숫자 모양은 같은 계산을 로컬 실행해 확인 (`Unit` 은 서버가 준 값으로만 만들어져 직접 실행하지 못함). `getSystemunit(context, id, 속성, 단위계)` 는 id 가 비고 단위계가 `null`, `""`, `As Entered` 면 `getDefaultUnit(context).getName()`
:::

## 객체의 값 읽고 바꾸기

이 페이지의 클래스 (`FrameworkUtil`, `mxBus`) 에도 객체 하나의 타입, 상태, 리비전, 속성을 읽고 바꾸는 메서드가 있습니다. 모두 DomainObject 에 같은 일을 하는 메서드가 있으므로 그것을 씁니다.

| 쓸 메서드 | 하는 일 |
| --- | --- |
| `obj.getInfo(context, selects)` | 타입, 현재 상태, 리비전, 속성 같은 값을 한 번에 읽음 ([값 읽기](/jpo/api/read#값-읽기-getinfo), [타입, 존재, 리비전](/jpo/api/read#타입-존재-리비전)) |
| `obj.setAttributeValues(context, attrs)`, `obj.setOwner(context, user)` ... | 값을 바꿈 ([값 수정](/jpo/api/write#값-수정)) |

```java
String statusSelect = DomainObject.getAttributeSelect(attrStatus);
Map info = part.getInfo(context, StringList.create(DomainConstants.SELECT_TYPE, DomainConstants.SELECT_CURRENT,
        "last.id", "previous.id", statusSelect));
String current = (String) info.get(DomainConstants.SELECT_CURRENT);
boolean revised = !"".equals(info.get("previous.id"));
```

현재 상태가 정책에서 몇 번째인지는 `state` 목록에서 찾습니다.

```java
Map info = part.getInfo(context, StringList.create("state", DomainConstants.SELECT_CURRENT), StringList.create("state"));
StringList states = (StringList) info.get("state");
int position = states.indexOf(info.get(DomainConstants.SELECT_CURRENT)) + 1;
```

**같은 일을 하는 다른 메서드**

| 메서드 | 쓰지 않는 이유 |
| --- | --- |
| `FrameworkUtil.getType(context, bo)` | `getInfo(context, DomainConstants.SELECT_TYPE)` 과 같은 값. 객체를 열어 `getBusinessType` 으로 읽음 |
| `obj.getType(context)` <Badge type="warning" text="폐기" /> | 폐기. 안에서 `FrameworkUtil.getType` 을 부름 ([getType](/jpo/api/domain-object#gettype)) |
| `FrameworkUtil.getCurrentState(context, bo)`, `mxBus.getCurrentState(context, obj)` | 상태 이름은 `getInfo(context, DomainConstants.SELECT_CURRENT)`. 정책의 상태를 모두 받아 `isCurrent()` 인 `State` 를 찾음 |
| `obj.getCurrentState(context)` <Badge type="warning" text="폐기" /> | 폐기. 같은 방법으로 `State` 를 찾음 ([getCurrentState](/jpo/api/domain-object#getcurrentstate)) |
| `FrameworkUtil.getCurrentStateName(context, objectId)` | `getInfo(context, DomainConstants.SELECT_CURRENT)` 과 같음 (MQL `print bus id select current dump`). id 가 비거나 결과가 비면 `FrameworkException` |
| `FrameworkUtil.getCurrentStateOfBusObj(context, bo)` | 현재 상태가 몇 번째인지 (1 부터). 위 예제처럼 `state` 목록에서 구함. OOTB 에서 부르지 않음 |
| `mxBus.getState(context, obj, stateName)` | 정책의 `State` 객체 하나를 이름으로 찾음 (대소문자 무시). 이름 목록은 select `state` |
| `FrameworkUtil.getLastRevision(context, objectId)` | 마지막 리비전 id. select `last.id` 나 `obj.getLastRevision(context)` 와 같은 값. 리비전 목록이 비면 `NullPointerException` |
| `FrameworkUtil.isRevisedBusObj(context, bo)` | 앞 리비전이 있는지. 위 예제처럼 select `previous.id` 로 봄 |
| `FrameworkUtil.getAttribute(context, bo, attribute)`, `mxBus.getAttributeValue(context, obj, attribute)` | `obj.getAttributeValue(context, attribute)` 와 같음 (`attribute[이름]` select) |
| `FrameworkUtil.getRelAttribute(context, rel, attribute)` | `DomainRelationship.getAttributeValue(context, relId, attribute)` 와 같은 일. 관계를 열고 속성 목록을 돌며 이름이 같은 값을 찾음. 없으면 `""` |
| `mxBus.getAttribute(context, obj, attribute)` | `Attribute` 객체를 돌려줌. 값은 `getAttributeValue`, 정보는 `obj.getAttributeDetails(context)` |
| `mxBus.getAttributeValues(context, obj)` | `obj.getAttributeMap(context)` 와 같은 일. 여러 값 속성도 `getValue()` 하나로 넣음 |
| `mxBus.getAttributeDetails(context, obj)` | `obj.getAttributeDetails(context)` 와 같은 일. 키가 `attributeName`, `attributeValue`, `attributeType`, `attributeChoices`, `attributeMultiline` 로 다름 |
| `mxBus.getBasics(context, obj, names)` | `owner`, `modified` 같은 기본 값을 `String[]` 로. `getInfo(context, selects)` 로 읽음 |
| `mxBus.print(context, obj, selects, multiSelects)` | `obj.getInfo(context, selects, multiSelects)` 와 같은 모양 |
| `mxBus.expand(...)` (2) | `obj.getRelatedObjects(...)` 와 같은 일 (`BusinessObject.expandSelect`) |
| `mxBus.setAttributeValue(context, obj, attribute, value)` | `obj.setAttributeValue(context, attribute, value)` 와 같음 |
| `mxBus.setAttributeValues(context, obj, attrs)` | `obj.setAttributeValues(context, attrs)` 와 같은 일. 값이 `StringList` 면 `ClassCastException` (`mxAttr.createAttributeList` 가 값을 모두 `String` 으로 꺼냄. 로컬 실행) |
| `mxBus.setDescription`, `mxBus.setOwner` | `obj.setDescription`, `obj.setOwner` 와 같음 (`BusinessObject` 의 set 뒤 `update`) |
| `mxBus.change(context, obj, type, name, revision, vault, policy)` | `obj.setName`, `setPolicy`, `setVault` 로 하나씩 바꿈. 안에서 `BusinessObject.change` |
| `mxBus.create(context, type, name, revision, policy, vault)` | `obj.createObject(context, type, name, revision, policy, vault)` 와 같은 일 |
| `mxBus.copy(context, obj, name, revision, vault)` | `obj.cloneObject(...)` 와 같은 일 |
| `mxBus.revise(context, obj, revision, vault)` | `obj.reviseObject(context, revision, true)` 와 같은 일 |
| `mxBus.delete(context, obj)` | `obj.deleteObject(context)` |
| `mxBus.promote`, `mxBus.demote` | `obj.promote(context)`, `obj.demote(context)` |
| `mxBus.gotoTargetState(context, obj, state)` (2) | `obj.setState(context, state)` 와 같은 일 |
| `mxBus.lock`, `mxBus.unlock` | `obj.lock(context)`, `obj.unlock(context)` (`BusinessObject` 메서드) |

::: info 근거
`mxBus` 의 메서드는 모두 `getBusinessObject(obj)` (id 문자열이면 `new BusinessObject(id)`, 아니면 `BusinessObject` 로 변환) 뒤 닫혀 있으면 열고, 같은 일을 하는 `BusinessObject` 메서드를 부른 뒤 자기가 연 것만 닫음. `setAttributeValues` 는 `mxAttr.createAttributeList(map)` (값을 `(String)` 으로 변환) 뒤 `setAttributes`. `FrameworkUtil.toAttributeList` (DomainObject `setAttributeValues` 가 씀) 는 `String`, `StringList` 값만 넣고 나머지는 건너뜀 (로컬 실행: `Integer`, `null` 값은 빠짐). `getAttributeDetails` 의 키는 `mxBus.SEL_ATTRIBUTE_*` 상수. `FrameworkUtil.getCurrentState` 는 `getStates(context)` 를 돌며 `isCurrent()`, `getCurrentStateName` 은 id 가 비거나 결과가 비면 `MatrixException` 을 `FrameworkException` 으로, `getCurrentStateOfBusObj` 는 MQL `print bus $1 select $2 dump $3` (`state.current`, `|`) 에서 `TRUE` 까지 센 수, `getLastRevision` 은 `new BusinessObject(id).getRevisions(context)` 의 마지막 요소의 `getObjectId()`, `isRevisedBusObj` 는 `getPreviousRevision(context).getTypeName()` 이 `""` 이 아닌지, `getRelAttribute` 는 `Relationship.getAttributes(context)` 를 돌며 `getName().equals`. OOTB 호출: `FrameworkUtil.getAttribute(` 5, `getCurrentState(` 8, `mxBus.` 메서드 4 (`expand`, `getCurrentState`, `setAttributeValue`, `setOwner` 각 1)
:::

## 샘플 JPO

[XX_UtilSchema_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/util/XX_UtilSchema_mxJPO.java) 의 메서드입니다. 모든 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map (`programMap`) 에서 값을 읽습니다.

| 메서드 | 받는 값 (`programMap` 의 키) | 돌려주는 값 | 보여 주는 것 |
| --- | --- | --- | --- |
| `toActualName` | `symbolicName` | `String` (등록 안 됐으면 `""`) | `getSchemaProperty` ([실제 이름으로 바꾸기](#실제-이름으로-바꾸기)) |
| `toSymbolicName` | `adminType` (`type` ...), `name` | `String` (등록 안 됐으면 `null`) | `getAliasForAdmin` ([심볼릭 이름으로 바꾸기](#심볼릭-이름으로-바꾸기)) |
| `isReleased` | `objectId` | `Boolean` | 정책의 `state_Release` 를 실제 이름으로 바꿔 `current` 와 비교 ([실제 상태 이름으로 바꾸기](#실제-상태-이름으로-바꾸기)) |
| `getCurrentStateSymbolicName` | `objectId` | `String` (`state_Release` ...) | `reverseLookupStateName` ([심볼릭 상태 이름으로 바꾸기](#심볼릭-상태-이름으로-바꾸기)) |
| `getTypeProperty` | `type`, `property` | `String` | `getAdminProperty` ([관리 객체의 property](#관리-객체의-property)) |
| `isMyPartType` | `type` | `Boolean` | `mxType.isOfParentType` ([상속 관계 확인](#상속-관계-확인)) |
| `getPolicyNames` | `type` | `StringList` | `mxType.getPolicies` 의 `name` ([정책 목록](#정책-목록)) |
| `getDefaultPolicy` | `type` | `Map` (`policy`, `firstRevision`. 정책이 없으면 빈 Map) | `mxType.getDefaultPolicy` ([기본 정책](#기본-정책)) |
| `getTypeAttributes` | `type` | `MapList` | `mxType.getAttributes` ([속성 목록](#속성-목록)) |
| `hasStatusAttribute` | `type` | `Boolean` | `isAttributeOnType` ([속성이 있는지](#속성이-있는지)) |
| `getRanges` | `symbolicAttribute` | `StringList` | `getRanges` ([범위 값](#범위-값)) |
| `getStatus` | `objectId` | `String` | `getAttributeSelect` 로 만든 select 를 `getInfo` 로 읽기 ([속성 select 만들기](#속성-select-만들기)) |
| `getUnitInfo` | `objectId`, `symbolicAttribute` | `Map` (`hasUnit`, `dbUnit`, `units`, `inputUnit`) | 단위 정보 ([단위가 있는 속성](#단위가-있는-속성)) |
| `toDbUnitValue` | `symbolicAttribute`, `value`, `unit` | `String` | `convertToDefaultUnit` ([단위가 있는 속성](#단위가-있는-속성)) |

::: details XX_UtilSchema_mxJPO.java

<<< @/../JPOSample/util/XX_UtilSchema_mxJPO.java

:::

::: warning
샘플의 타입, 관계, 속성 이름은 예시입니다. 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다.
:::

## 다루지 않은 메서드

::: details 펼쳐 보기
| 클래스 | 메서드 | 이유 |
| --- | --- | --- |
| `PropertyUtil` | `cacheSymbolicNames()` <Badge type="warning" text="폐기" />, `cacheSymbolicNames(context)`, `checkSymbolicNamesCache`, `loadSymbolicNamesCache` (2) | 내부용 |
| `PropertyUtil` | `refreshAliasForAdmin` <Badge type="warning" text="폐기" /> (protected) | 내부용 |
| `PropertyUtil` | `loadAdminProperties`, `loadAdmintoProperties`, `updateAdminProperty`, `clearAdminPropertyCache`, `getAdminPropertyCacheKey` | 내부용 |
| `PropertyUtil` | `getAdmintoProperty` | 앱 전용 |
| `FrameworkUtil` | `getTypeApplicationName` | 내부용 |
| `FrameworkUtil` | `lookupObject` (2) | 내부용 |
| `FrameworkUtil` | `getAttributeDataType` | 내부용 |
| `FrameworkUtil` | `toAttributeList`, `toMap(AttributeList)`, `toMap(context, AttributeList)`, `toMap(context, AttributeTypeList)` | 내부용 |
| `FrameworkUtil` | `isAttributeHidden` | 화면용 |
| `FrameworkUtil` | `getTypesList` | OOTB 미사용 |
| `FrameworkUtil` | `getBusinessObjectRevisions` (2) | OOTB 미사용 |
| `FrameworkUtil` | `attributeHasChoicesOnly` | OOTB 미사용 |
| `FrameworkUtil` | `getNonHiddenAttributeNames` | OOTB 미사용 |
| `mxAttr` | `createAttributeList` | OOTB 미사용 |
| `mxBus` | `getAttributes`, `getBusinessObject` (2) (protected) | 내부용 |
| `UOMUtil` | `isUOMAttributeExpression`, `isAttributeExpression`, `isSimpleAttributeExpression`, `getAttrNameFromSelect` | 화면용 |
| `UOMUtil` | `convertToI18nUnitName` (2), `formatDisplayValue` (2), `getInputValue` (2) | 화면용 |
| `UOMUtil` | `getDBValue` (2), `getDBToString` (2), `getInputToString` (2) | OOTB 미사용 |
| `UOMUtil` | `getSystemValue` (4), `getSystemToString` (4), `getSystemunit` (사용자 단위계를 쓰는 4 개) | 환경설정 |
| `UOMUtil` | `getInputunit(context, id, attribute, isRelationship)` | 같은 일 다른 메서드 |
:::
