# DomainObject 메서드 사양 <Badge type="info" text="원본 분석" />

`com.matrixone.apps.domain.DomainObject` 에 선언된 메서드를 모두 모아 **하는 일, 넘기는 값, 돌려받는 것** 으로 정리한 사양서입니다. 자주 쓰는 메서드를 하고 싶은 일 순서로 보려면 [객체 조회](/jpo/api/read), [객체 생성, 수정](/jpo/api/write), [객체 삭제, 연결 끊기](/jpo/api/delete) 를 먼저 봅니다. 예제의 `part`, `relStructure` 같은 변수는 [예제에서 쓰는 변수](/jpo/api/#예제에서-쓰는-변수) 에 있습니다.

::: info 근거
enovia2025 `3dspace/WEB-INF/lib/xerinfra.jar` 의 `DomainObject` class 파일을 `javap` 로 읽어 시그니처, 폐기 표시 (`Deprecated`), 동작을 확인했습니다. 다룬 범위는 이 클래스에 직접 선언된 public, protected 메서드와 생성자 (이름 123 개, 형태 194 개) 입니다. 상속받은 `BusinessObject` 메서드는 자주 쓰는 `promote`, `demote` 만 넣었습니다. 예제는 같은 jar 로 컴파일해 확인했고, 서버에서 실행해 보지는 않았습니다.
:::

샘플: [XX_DomainObjectApi_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/XX_DomainObjectApi_mxJPO.java) ([메서드별 설명](#샘플-jpo))

## 먼저 알아 둘 것

### 클래스 모양

| 항목 | 내용 |
| --- | --- |
| 패키지 | `com.matrixone.apps.domain` (xerinfra.jar) |
| 상속 | `matrix.db.BusinessObject` 를 상속. `promote`, `demote`, `open`, `close` 같은 메서드는 여기서 옴 |
| 구현 | `DomainConstants`, `DomainSymbolicConstants`, `ImageHolder`. 그래서 `DomainObject.SELECT_NAME` 처럼 상수를 바로 씀 |
| 예외 | 대부분 `FrameworkException` (`matrix.util.MatrixException` 의 하위 클래스). 몇몇은 `Exception`, `MatrixException` |

### 표시

| 표시 | 뜻 |
| --- | --- |
| <Badge type="warning" text="폐기" /> | class 파일에 `Deprecated` 가 붙은 메서드 (61 개 형태). 지금은 동작하지만 새 코드에는 쓰지 않고, 함께 적은 대체 메서드를 씁니다 |
| <Badge type="tip" text="protected" /> | `DomainObject` 를 상속한 클래스 안에서만 부를 수 있음. JPO 에서 직접 부르지 않습니다 |
| <Badge type="info" text="BusinessObject" /> | `DomainObject` 가 아니라 상속받은 `BusinessObject` 에 선언된 메서드 |
| <Badge type="danger" text="검증 필요" /> | 원본으로 확인하지 못한 동작 |

### 표 읽는 법

- **형태** 열의 `obj` 는 `DomainObject` 인스턴스, `DomainObject.` 으로 시작하면 static 메서드입니다.
- 이름 하나에 인자가 다른 형태 (오버로드) 가 여러 개면 한 표에 행으로 모았습니다.
- 값은 모두 문자열이고, 타입 / 관계 / 속성 / 상태 이름은 화면 이름이 아니라 실제 이름을 넘깁니다 ([먼저 알아 둘 다섯 가지](/jpo/api/#먼저-알아-둘-다섯-가지)).

## 하고 싶은 일로 찾기

| 하고 싶은 일 | 메서드 | 사양 |
| --- | --- | --- |
| id 로 타입에 맞는 클래스 (`Part` ...) 의 객체 얻기 | `DomainObject.newInstance(context, id)` | [newInstance](#newinstance) |
| 애플리케이션 클래스 (`engineering.Part` ...) 로 객체 얻기 | `DomainObject.newInstance(context, id, app)` | [newInstance](#newinstance) |
| 타입을 알 때 객체를 열지 않고 하위 클래스 객체 만들기 | `DomainObject.newInstance(context, type, app)` | [newInstance](#newinstance) |
| 타입 조회 없이 id 만 담은 객체 만들기 | `new DomainObject(id)` | [new DomainObject](#new-domainobject) |
| 객체 변수 하나를 다른 id 로 다시 쓰기 | `obj.setId(id)` | [setId](#setid) |
| 객체 id 꺼내기 (타입, 이름, 리비전으로 만든 객체 포함) | `obj.getId(context)` | [getId](#getid) |
| 연결된 Image Holder 객체 얻기 | `obj.getImageObject(context)` | [getImageObject](#getimageobject) |
| 값 하나 읽기 | `obj.getInfo(context, select)` | [getInfo](#getinfo) |
| 값 여러 개를 한 번에 읽기 | `obj.getInfo(context, selects)` | [getInfo](#getinfo) |
| 값이 여러 개인 select 를 다른 값과 함께 목록으로 읽기 | `obj.getInfo(context, selects, multiSelects)` | [getInfo](#getinfo) |
| 여러 객체의 값을 한 번에 읽기 | `DomainObject.getInfo(context, ids, selects)` | [getInfo](#getinfo) |
| 값이 여러 개인 select 하나를 목록으로 읽기 | `obj.getInfoList(context, select)` | [getInfoList](#getinfolist) |
| 그 타입이거나 하위 타입인지 확인 | `obj.isKindOf(context, type)` | [isKindOf](#iskindof) |
| 속성 하나 읽기 | `obj.getAttributeValue(context, name)` | [getAttributeValue](#getattributevalue) |
| 모든 속성을 이름 → 값으로 읽기 | `obj.getAttributeMap(context, true)` | [getAttributeMap](#getattributemap) |
| 속성의 타입, 선택값까지 읽기 | `obj.getAttributeDetails(context)` | [getAttributeDetails](#getattributedetails) |
| 속성 값 select 만들기 | `DomainObject.getAttributeSelect(name)` | [getAttributeSelect](#getattributeselect) |
| 타입, 이름 패턴, 조건으로 객체 찾기 | `DomainObject.findObjects(context, type, name, revision, ...)` | [findObjects](#findobjects) |
| 개수를 제한하고 정렬해서 찾기 | `DomainObject.findObjects(context, type, vault, where, selects, limit, orderBy)` | [findObjects](#findobjects) |
| 연결하기 전에 순환 구조가 되는지 확인하기 | `DomainObject.multiLevelRecursionCheck(context, fromId, toId, relPattern, checkRevisions)` | [multiLevelRecursionCheck](#multilevelrecursioncheck) |
| 연결된 객체 목록 받기 (자식, 부모, 여러 단계) | `obj.getRelatedObjects(context, ..., limit)` | [getRelatedObjects](#getrelatedobjects) |
| 펼친 결과를 타입, 관계, 값 패턴으로 한 번 더 거르기 | `obj.getRelatedObjects(context, ..., limit, includeType, includeRel, includeMap)` | [getRelatedObjects](#getrelatedobjects) |
| 바로 연결된 객체 하나만 받기 | `obj.getRelatedObject(context, relPattern, isFrom, objectSelects, relSelects)` | [getRelatedObject](#getrelatedobject) |
| 그 관계로 연결된 것이 있는지 확인 | `obj.hasRelatedObjects(context, rel, isFrom)` | [hasRelatedObjects](#hasrelatedobjects) |
| 이 객체 → 다른 객체 (id) 로 연결하기 | `obj.addToObject(context, relType, id)` | [addToObject](#addtoobject) |
| 다른 객체 (id) → 이 객체로 연결하기 | `obj.addFromObject(context, relType, id)` | [addFromObject](#addfromobject) |
| 방향을 boolean 으로 골라 하나 연결하기 | `obj.addRelatedObject(context, relType, isFrom, id)` | [addRelatedObject](#addrelatedobject) |
| 여러 객체를 한 트랜잭션으로 연결하기 | `obj.addRelatedObjects(context, relType, isFrom, ids)` | [addRelatedObjects](#addrelatedobjects) |
| 이름, 정책까지 정해 만들면서 바로 연결하기 | `obj.createAndConnect(context, type, name, revision, policy, vault, rel, other, isFrom)` | [createAndConnect](#createandconnect) |
| 하나만 두는 관계의 상대 바꾸기, 끊기 | `obj.setRelatedObject(context, rel, isFrom, id)` | [setRelatedObject](#setrelatedobject) |
| 새 객체 만들기 (비운 인자는 기본값) | `obj.createObject(context, type, name, revision, policy, vault)` | [createObject](#createobject) |
| 타입의 기본 정책 이름 알아내기 | `obj.getDefaultPolicy(context, type)` | [getDefaultPolicy](#getdefaultpolicy) |
| 정책의 첫 리비전 알아내기 | `obj.getDefaultRevision(context, policy)` | [getDefaultRevision](#getdefaultrevision) |
| 겹치지 않게 붙일 이름, 리비전 문자열 만들기 | `obj.getUniqueName(prefix)` | [getUniqueName](#getuniquename) |
| 채번기로 이름 받기 | `DomainObject.getAutoGeneratedName(context, generator, series)` | [getAutoGeneratedName](#getautogeneratedname) |
| 객체 복사본 만들기 | `obj.cloneObject(context, newName, null, null, false, false)` | [cloneObject](#cloneobject) |
| 속성 하나 바꾸기 | `obj.setAttributeValue(context, attr, value)` | [setAttributeValue](#setattributevalue) |
| 속성 여러 개를 한 번에 바꾸기 | `obj.setAttributeValues(context, attrs)` | [setAttributeValues](#setattributevalues) |
| 설명 바꾸기 | `obj.setDescription(context, text)` | [setDescription](#setdescription) |
| 이름 바꾸기 | `obj.setName(context, name)` | [setName](#setname) |
| 소유자 바꾸기 | `obj.setOwner(context, user)` | [setOwner](#setowner) |
| 정책 바꾸기 | `obj.setPolicy(context, policy)` | [setPolicy](#setpolicy) |
| vault 바꾸기 | `obj.setVault(context, vault)` | [setVault](#setvault) |
| 마지막 리비전인지 확인하기 | `obj.isLastRevision(context)` | [isLastRevision](#islastrevision) |
| 마지막 리비전 얻기 | `obj.getLastRevision(context)` | [getLastRevision](#getlastrevision) |
| 바로 앞 리비전 얻기 | `obj.getPreviousRevision(context)` | [getPreviousRevision](#getpreviousrevision) |
| 모든 리비전의 값을 한 번에 읽기 | `obj.getRevisionsInfo(context, selects, multiSelects)` | [getRevisionsInfo](#getrevisionsinfo) |
| 리비전 이름을 정해 다음 리비전 만들기 | `obj.reviseObject(context, revision, withFiles)` | [reviseObject](#reviseobject) |
| 목표 상태까지 한 번에 옮기기 | `obj.setState(context, state)` | [setState](#setstate) |
| 다음 상태로 한 단계 올리기 | `obj.promote(context)` | [promote, demote](#promote-demote) |
| 이전 상태로 한 단계 내리기 | `obj.demote(context)` | [promote, demote](#promote-demote) |
| 두 상태 사이 서명과 승인 여부 읽기 | `obj.getSignaturesDetails(context, fromState, toState)` | [getSignaturesDetails](#getsignaturesdetails) |
| 객체 하나 지우기 | `obj.deleteObject(context)` | [deleteObject](#deleteobject) |
| 여러 객체를 한 트랜잭션으로 지우기 | `DomainObject.deleteObjects(context, ids)` | [deleteObjects](#deleteobjects) |
| 체크인된 파일 이름과 포맷 목록 받기 | `obj.getAllFormatFiles(context)` | [getAllFormatFiles](#getallformatfiles) |
| 서버 workspace 의 파일을 체크인하기 | `obj.checkinFromServer(context, unlock, append, format, store, fileNames)` | [checkinFromServer](#checkinfromserver) |
| 협업공간, 조직 바꾸기 | `obj.setPrimaryOwnership(context, project, organization)` | [setPrimaryOwnership](#setprimaryownership) |
| 협업공간, 조직 비우기 | `obj.removePrimaryOwnership(context)` | [removePrimaryOwnership](#removeprimaryownership) |
| 소유자, 협업공간, 조직을 한 번에 바꾸기 | `obj.TransferOwnership(context, owner, project, organization)` | [TransferOwnership](#transferownership) |
| 부여된 접근 권한 거두기 | `obj.revokeAccess(context, grantor, grantee)` | [revokeAccess](#revokeaccess) |

## 이름으로 찾기

DomainObject 에 선언된 메서드 123 개 (생성자 포함) 입니다. **형태** 는 오버로드 개수, 폐기 숫자는 폐기된 형태 수 / 전체 형태 수입니다.

| 메서드 | 형태 | 구분 | 묶음 |
| --- | --- | --- | --- |
| [abortTransaction](#aborttransaction) | 1 | <Badge type="warning" text="폐기" /> | [트랜잭션, 권한 전환](#트랜잭션-권한-전환) |
| [addFromObject](#addfromobject) | 1 |  | [연결](#연결) |
| [addFromObjects](#addfromobjects) | 2 | <Badge type="warning" text="폐기" /> 1 / 2 | [연결](#연결) |
| [addRelatedObject](#addrelatedobject) | 1 |  | [연결](#연결) |
| [addRelatedObjects](#addrelatedobjects) | 2 | <Badge type="warning" text="폐기" /> 1 / 2 | [연결](#연결) |
| [addToObject](#addtoobject) | 1 |  | [연결](#연결) |
| [addToObjects](#addtoobjects) | 2 | protected 1, <Badge type="warning" text="폐기" /> 1 / 2 | [연결](#연결) |
| [backupFile](#backupfile) | 1 | protected | [파일](#파일) |
| [changeOwner](#changeowner) | 1 | <Badge type="warning" text="폐기" /> | [값 수정](#값-수정) |
| [changePolicy](#changepolicy) | 1 | <Badge type="warning" text="폐기" /> | [값 수정](#값-수정) |
| [changeVault](#changevault) | 1 | <Badge type="warning" text="폐기" /> | [값 수정](#값-수정) |
| [checkinFromServer](#checkinfromserver) | 2 |  | [파일](#파일) |
| [clear](#clear) | 1 |  | [객체 얻기](#객체-얻기) |
| [cloneObject](#cloneobject) | 4 |  | [생성, 복제](#생성-복제) |
| [cloneWithFileMove](#clonewithfilemove) | 1 |  | [생성, 복제](#생성-복제) |
| [closeObject](#closeobject) | 1 |  | [객체 얻기](#객체-얻기) |
| [commitTransaction](#committransaction) | 1 | <Badge type="warning" text="폐기" /> | [트랜잭션, 권한 전환](#트랜잭션-권한-전환) |
| [confirmDelete](#confirmdelete) | 1 | <Badge type="warning" text="폐기" /> | [삭제](#삭제) |
| [connect](#connect) | 1 | <Badge type="warning" text="폐기" /> | [연결](#연결) |
| [connectFrom](#connectfrom) | 1 | <Badge type="warning" text="폐기" /> | [연결](#연결) |
| [connectTo](#connectto) | 1 | <Badge type="warning" text="폐기" /> | [연결](#연결) |
| [createAndConnect](#createandconnect) | 3 |  | [연결](#연결) |
| [createObject](#createobject) | 2 |  | [생성, 복제](#생성-복제) |
| [createRevision](#createrevision) | 1 | <Badge type="warning" text="폐기" /> | [리비전](#리비전) |
| [debug](#debug) | 3 | protected, <Badge type="warning" text="폐기" /> | [그 밖에](#그-밖에) |
| [delete](#delete) | 2 | <Badge type="warning" text="폐기" /> | [삭제](#삭제) |
| [deleteObject](#deleteobject) | 2 |  | [삭제](#삭제) |
| [deleteObjects](#deleteobjects) | 1 | static | [삭제](#삭제) |
| [demoteWithBL](#demotewithbl) | 1 |  | [상태, 서명](#상태-서명) |
| [new DomainObject](#new-domainobject) | 4 |  | [객체 얻기](#객체-얻기) |
| [editBasicInfo](#editbasicinfo) | 1 | <Badge type="warning" text="폐기" /> | [값 수정](#값-수정) |
| [expandSelect](#expandselect) | 3 | <Badge type="warning" text="폐기" /> | [관계 따라가기](#관계-따라가기) |
| [findObjects](#findobjects) | 11 | static, <Badge type="warning" text="폐기" /> 1 / 11 | [검색](#검색) |
| [getAliasForName](#getaliasforname) | 1 | static, <Badge type="warning" text="폐기" /> | [값 읽기](#값-읽기) |
| [getAllFiles](#getallfiles) | 1 |  | [파일](#파일) |
| [getAllFormatFiles](#getallformatfiles) | 1 |  | [파일](#파일) |
| [getApprovalsInfo](#getapprovalsinfo) | 1 | <Badge type="warning" text="폐기" /> | [상태, 서명](#상태-서명) |
| [getAttribute](#getattribute) | 1 | <Badge type="warning" text="폐기" /> | [속성](#속성) |
| [getAttributeDetails](#getattributedetails) | 1 |  | [속성](#속성) |
| [getAttributeMap](#getattributemap) | 2 |  | [속성](#속성) |
| [getAttributeNames](#getattributenames) | 1 | protected | [속성](#속성) |
| [getAttributeSelect](#getattributeselect) | 1 | static | [속성](#속성) |
| [getAttributeValue](#getattributevalue) | 1 |  | [속성](#속성) |
| [getAutoGeneratedName](#getautogeneratedname) | 1 | static | [생성, 복제](#생성-복제) |
| [getBasicInfo](#getbasicinfo) | 1 | <Badge type="warning" text="폐기" /> | [기본 정보](#기본-정보) |
| [getBusinessObjectData](#getbusinessobjectdata) | 1 |  | [값 읽기](#값-읽기) |
| [getBusSelectList](#getbusselectlist) | 1 | <Badge type="warning" text="폐기" /> | [값 읽기](#값-읽기) |
| [getCachedData](#getcacheddata) | 1 | protected, <Badge type="warning" text="폐기" /> | [그 밖에](#그-밖에) |
| [getCurrentState](#getcurrentstate) | 1 | <Badge type="warning" text="폐기" /> | [기본 정보](#기본-정보) |
| [getDecoratedOid](#getdecoratedoid) | 1 |  | [객체 얻기](#객체-얻기) |
| [getDefaultName](#getdefaultname) | 1 | protected | [생성, 복제](#생성-복제) |
| [getDefaultPolicy](#getdefaultpolicy) | 1 |  | [생성, 복제](#생성-복제) |
| [getDefaultRevision](#getdefaultrevision) | 1 |  | [생성, 복제](#생성-복제) |
| [getDefaultVault](#getdefaultvault) | 1 |  | [생성, 복제](#생성-복제) |
| [getErrorMessage](#geterrormessage) | 1 | static | [기본 정보](#기본-정보) |
| [getFirstRevision](#getfirstrevision) | 1 | protected | [리비전](#리비전) |
| [getId](#getid) | 2 | <Badge type="warning" text="폐기" /> 1 / 2 | [객체 얻기](#객체-얻기) |
| [getImageObject](#getimageobject) | 1 |  | [기본 정보](#기본-정보) |
| [getInfo](#getinfo) | 10 | static | [값 읽기](#값-읽기) |
| [getInfoList](#getinfolist) | 2 |  | [값 읽기](#값-읽기) |
| [getLastRevision](#getlastrevision) | 1 |  | [리비전](#리비전) |
| [getLicenseReservedAPPS](#getlicensereservedapps) | 1 |  | [그 밖에](#그-밖에) |
| [getMajorRevisions](#getmajorrevisions) | 1 |  | [리비전](#리비전) |
| [getName](#getname) | 1 | <Badge type="warning" text="폐기" /> | [기본 정보](#기본-정보) |
| [getNextStateSignature](#getnextstatesignature) | 1 |  | [상태, 서명](#상태-서명) |
| [getObject](#getobject) | 3 | static, <Badge type="warning" text="폐기" /> | [객체 얻기](#객체-얻기) |
| [getObjectDetails](#getobjectdetails) | 1 | static | [기본 정보](#기본-정보) |
| [getObjects](#getobjects) | 1 | <Badge type="warning" text="폐기" /> | [관계 따라가기](#관계-따라가기) |
| [getObjectSelectList](#getobjectselectlist) | 1 |  | [값 읽기](#값-읽기) |
| [getParentInfo](#getparentinfo) | 1 | protected, <Badge type="warning" text="폐기" /> | [관계 따라가기](#관계-따라가기) |
| [getPolicies](#getpolicies) | 1 | <Badge type="warning" text="폐기" /> | [기본 정보](#기본-정보) |
| [getPreviousRevision](#getpreviousrevision) | 1 |  | [리비전](#리비전) |
| [getRelatedObject](#getrelatedobject) | 1 |  | [관계 따라가기](#관계-따라가기) |
| [getRelatedObjects](#getrelatedobjects) | 9 | <Badge type="warning" text="폐기" /> 3 / 9 | [관계 따라가기](#관계-따라가기) |
| [getRelationship](#getrelationship) | 1 | protected, <Badge type="warning" text="폐기" /> | [관계 따라가기](#관계-따라가기) |
| [getRelationshipSelectList](#getrelationshipselectlist) | 1 |  | [값 읽기](#값-읽기) |
| [getRevision](#getrevision) | 1 | <Badge type="warning" text="폐기" /> | [기본 정보](#기본-정보) |
| [getRevisions](#getrevisions) | 1 | <Badge type="warning" text="폐기" /> | [리비전](#리비전) |
| [getRevisionsInfo](#getrevisionsinfo) | 2 |  | [리비전](#리비전) |
| [getRouteInfoForBranches](#getrouteinfoforbranches) | 1 |  | [상태, 서명](#상태-서명) |
| [getRouteInfoForStates](#getrouteinfoforstates) | 1 | <Badge type="warning" text="폐기" /> | [상태, 서명](#상태-서명) |
| [getShortUniqueName](#getshortuniquename) | 3 | <Badge type="warning" text="폐기" /> 1 / 3 | [생성, 복제](#생성-복제) |
| [getSignaturesDetails](#getsignaturesdetails) | 2 |  | [상태, 서명](#상태-서명) |
| [getState](#getstate) | 1 | protected, <Badge type="warning" text="폐기" /> | [기본 정보](#기본-정보) |
| [getType](#gettype) | 1 | <Badge type="warning" text="폐기" /> | [기본 정보](#기본-정보) |
| [getTypeAttributeNames](#gettypeattributenames) | 1 | static, protected | [속성](#속성) |
| [getTypeAttributes](#gettypeattributes) | 1 | static, protected | [속성](#속성) |
| [getUniqueName](#getuniquename) | 4 | <Badge type="warning" text="폐기" /> 1 / 4 | [생성, 복제](#생성-복제) |
| [gotoState](#gotostate) | 1 | <Badge type="warning" text="폐기" /> | [상태, 서명](#상태-서명) |
| [hasObjects](#hasobjects) | 1 | protected, <Badge type="warning" text="폐기" /> | [관계 따라가기](#관계-따라가기) |
| [hasRelatedObjects](#hasrelatedobjects) | 1 |  | [관계 따라가기](#관계-따라가기) |
| [isKindOf](#iskindof) | 1 |  | [값 읽기](#값-읽기) |
| [isLastRevision](#islastrevision) | 1 |  | [리비전](#리비전) |
| [multiLevelRecursionCheck](#multilevelrecursioncheck) | 1 | static | [검색](#검색) |
| [newInstance](#newinstance) | 8 | static | [객체 얻기](#객체-얻기) |
| [openObject](#openobject) | 1 |  | [객체 얻기](#객체-얻기) |
| [popContext](#popcontext) | 1 | static, protected, <Badge type="warning" text="폐기" /> | [트랜잭션, 권한 전환](#트랜잭션-권한-전환) |
| [promoteWithBL](#promotewithbl) | 1 |  | [상태, 서명](#상태-서명) |
| [pushContext](#pushcontext) | 2 | static, protected, <Badge type="warning" text="폐기" /> | [트랜잭션, 권한 전환](#트랜잭션-권한-전환) |
| [querySelect](#queryselect) | 2 | static, <Badge type="warning" text="폐기" /> | [검색](#검색) |
| [removePrimaryOwnership](#removeprimaryownership) | 1 |  | [소유권, 접근 권한](#소유권-접근-권한) |
| [resetAttributes](#resetattributes) | 1 |  | [값 수정](#값-수정) |
| [reviseObject](#reviseobject) | 2 |  | [리비전](#리비전) |
| [revokeAccess](#revokeaccess) | 1 |  | [소유권, 접근 권한](#소유권-접근-권한) |
| [setAttributeValue](#setattributevalue) | 1 |  | [값 수정](#값-수정) |
| [setAttributeValues](#setattributevalues) | 1 |  | [값 수정](#값-수정) |
| [setCachedData](#setcacheddata) | 1 | protected, <Badge type="warning" text="폐기" /> | [그 밖에](#그-밖에) |
| [setDebug](#setdebug) | 1 | static, <Badge type="warning" text="폐기" /> | [그 밖에](#그-밖에) |
| [setDescription](#setdescription) | 1 |  | [값 수정](#값-수정) |
| [setId](#setid) | 1 |  | [객체 얻기](#객체-얻기) |
| [setLicenseReservedAPPS](#setlicensereservedapps) | 1 |  | [그 밖에](#그-밖에) |
| [setName](#setname) | 1 |  | [값 수정](#값-수정) |
| [setOwner](#setowner) | 1 |  | [값 수정](#값-수정) |
| [setPolicy](#setpolicy) | 1 |  | [값 수정](#값-수정) |
| [setPrimaryOwnership](#setprimaryownership) | 2 |  | [소유권, 접근 권한](#소유권-접근-권한) |
| [setRelatedObject](#setrelatedobject) | 1 |  | [연결](#연결) |
| [setRelationship](#setrelationship) | 1 | protected, <Badge type="warning" text="폐기" /> | [연결](#연결) |
| [setSavePoint](#setsavepoint) | 1 | static, <Badge type="warning" text="폐기" /> | [트랜잭션, 권한 전환](#트랜잭션-권한-전환) |
| [setState](#setstate) | 2 |  | [상태, 서명](#상태-서명) |
| [setVault](#setvault) | 1 |  | [값 수정](#값-수정) |
| [startTransaction](#starttransaction) | 1 | <Badge type="warning" text="폐기" /> | [트랜잭션, 권한 전환](#트랜잭션-권한-전환) |
| [TransferOwnership](#transferownership) | 1 |  | [소유권, 접근 권한](#소유권-접근-권한) |
| [validateSelectedAttributes](#validateselectedattributes) | 2 |  | [속성](#속성) |

## 객체 얻기

객체 변수를 만들고, 다른 객체를 가리키게 하고, id 를 꺼내고, 열고 닫는 메서드입니다. 대부분 서버에 가지 않고 변수에 id 를 담아 두기만 합니다. 어떤 것을 쓸지는 [객체 얻기](/jpo/api/read#객체-얻기) 에도 정리되어 있습니다.

### new DomainObject

서버에 가지 않고 객체 변수를 만듭니다. 타입을 읽지 않으므로 클래스는 언제나 `DomainObject` 입니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `new DomainObject()` | 없음 | id 가 없는 빈 객체 |
| `new DomainObject(id)` | `id`: 객체 id | 그 id 를 담은 객체 |
| `new DomainObject(decoratedOid)` | `decoratedOid`: `DecoratedOid`. 안의 id (`getOID()`) 만 씀 | 그 id 를 담은 객체 |
| `new DomainObject(bo)` | `bo`: `BusinessObject` | `bo` 의 id, 이름, 타입, 리비전, vault 를 복사한 객체. 열린 상태는 복사하지 않음 |

- 객체가 있는지, id 가 맞는지 확인하지 않습니다. 이 단계에서는 없는 id 여도 예외가 나지 않습니다.
- `DomainObject(String)`, `DomainObject(DecoratedOid)` 는 `throws Exception` 으로 선언되어 있어 `try` 나 `throws Exception` 이 필요합니다.
- `new DomainObject(id)` 는 이름 칸에도 id 를 넣어 둡니다. 열기 전에 context 없는 `getName()` 을 부르면 이름이 아니라 id 가 옵니다.
- `Part`, `Document` 같은 하위 클래스가 필요하면 [newInstance](#newinstance) 를 씁니다.

```java
DomainObject plain = new DomainObject(objectId);
String name = plain.getInfo(context, DomainConstants.SELECT_NAME);
DomainObject lastPart = new DomainObject(part.getLastRevision(context));
```

### newInstance

id 로 그 타입에 등록된 클래스 (`Part`, `Document` ...) 의 객체를 만듭니다. id 대신 타입 이름을 넘기면 그 타입 클래스의 빈 객체를 만듭니다. OOTB 가 가장 많이 쓰는 형태는 `newInstance(context, id)` 입니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.newInstance(context)` | 없음 | id 가 없는 빈 `DomainObject`. [생성](/jpo/api/write#생성) 전에 씀 |
| `DomainObject.newInstance(context, id)` | `id`: 객체 id. id 모양이 아니면 타입 이름으로 봄 | id 를 담은, 타입에 맞는 클래스의 객체. 타입 이름이면 그 클래스의 빈 객체 |
| `DomainObject.newInstance(context, id, app)` | `app`: 애플리케이션 이름 (`DomainConstants.ENGINEERING` = `"Engineering"`, `PROGRAM` = `"Program"` ...). 대소문자 무관. `null` 이면 주지 않은 것과 같음 | 위와 같고, 그 애플리케이션의 매핑 파일에서도 클래스를 찾음 |
| `DomainObject.newInstance(context, id, app, intf)` | `intf`: interface 이름. 객체에 이 interface 가 붙어 있으면 타입 대신 interface 로 클래스를 찾음 (매핑 파일의 `interface_` 항목) | 위와 같음 |
| `DomainObject.newInstance(context, bo)` | `bo`: `BusinessObject` | `bo` 의 id 를 담은, 타입에 맞는 클래스의 객체 |
| `DomainObject.newInstance(context, bo, app)` | `bo`, `app`: 위와 같음 | 위와 같고 `app` 의 매핑 파일에서도 찾음 |
| `DomainObject.newInstance(context, decoratedOid)` | `decoratedOid`: `DecoratedOid` (`"type:타입#id"` 를 푼 것) | 그 안의 타입으로 찾은 클래스의 객체. 타입이 비어 있으면 `DomainObject` |
| `DomainObject.newInstance(context, decoratedOid, app)` | `decoratedOid`, `app`: 위와 같음 | 위와 같고 `app` 의 매핑 파일에서도 찾음 |

객체를 여는지는 형태마다 다릅니다.

| 형태 | 서버에서 하는 일 |
| --- | --- |
| `newInstance(context)`, 타입 이름을 넘긴 형태, `DecoratedOid` 형태 | 객체를 열지 않음 |
| `newInstance(context, id ...)` | 타입을 읽으려고 `new BusinessObject(id)` 를 따로 열고 닫음. `intf` 를 주면 MQL `print bus` 를 한 번 더 부름 |
| `newInstance(context, bo ...)` | `bo` 가 닫혀 있으면 열고, 타입을 읽은 뒤 닫음. 열려 있었으면 그대로 둠 |

- 어느 형태든 돌려받은 객체는 열려 있지 않고 id 만 담겨 있습니다 (`bo` 형태도 이름, 리비전은 복사하지 않음).
- 문자열이 `11111.22222.33333.44444` 처럼 숫자 네 조각이거나, 32 자 이상 16 진수 조각 (physicalid) 이 있으면 id 로 봅니다. 그 밖의 문자열은 타입 이름으로 봐서 id 없는 객체가 옵니다. 잘못된 id 를 넘겨도 예외 없이 빈 객체가 올 수 있습니다.
- `id` 로 보인 값의 객체가 없으면 타입을 읽을 때 `FrameworkException` 이 납니다. `null` 을 넘기면 `NullPointerException` 입니다.
- 타입을 이미 알면 `newInstance(context, type)` 나 `newInstance(context, type, app)` 로 클래스만 정하고 `setId(id)` 로 id 를 넣어 객체 열기를 건너뜁니다 (OOTB `emxGenericDocumentBase_mxJPO.java`, `emxECPartBase_mxJPO.java`).

클래스는 매핑 파일 (`.properties`) 에서 아래 순서로 찾습니다.

1. `emxFramework.BeanMapping.PreliminarySearchFiles` 의 파일 (분석한 `emxSystem.properties` 는 `emxIntegrationsBeanMappingFile`, `emxmxclearcaseMappingFile`)
2. `app` 을 주면 `emx<app>MappingFile` (예: `emxEngineeringMappingFile`). `emxFramework.ApplicationList` 에 있는 애플리케이션만 읽음
3. `emxCommonMappingFile`

- 파일의 항목은 `type_Part=com.matrixone.apps.common.Part` 처럼 심볼릭 이름 = 클래스 이름입니다. 타입이 없으면 같은 파일에서 상위 타입을 차례로 찾고, 그래도 없으면 다음 파일로 갑니다.
- 끝까지 없으면 `DomainObject` 입니다. 찾은 클래스가 `DomainObject` 하위가 아니면 `FrameworkException` 입니다.

| 부른 형태 (타입이 `Part` 인 id) | 받는 클래스 |
| --- | --- |
| `DomainObject.newInstance(context, id)` | `com.matrixone.apps.common.Part` |
| `DomainObject.newInstance(context, id, DomainConstants.ENGINEERING)` | `com.matrixone.apps.engineering.Part` |
| 어느 매핑 파일에도 없는 타입 | `com.matrixone.apps.domain.DomainObject` |

```java
DomainObject empty = DomainObject.newInstance(context);
DomainObject engPart = DomainObject.newInstance(context, objectId, DomainConstants.ENGINEERING);
DomainObject typed = DomainObject.newInstance(context, typePart);
typed.setId(objectId);
DomainObject prevPart = DomainObject.newInstance(context, part.getPreviousRevision(context));
```

### getObject

<Badge type="warning" text="폐기" /> 대신 `newInstance` 를 씁니다. (안에서 같은 인자로 `newInstance` 만 부름)

같은 인자로 [newInstance](#newinstance) 를 불러 돌려줍니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.getObject(context, id)` | `id`: 객체 id | `newInstance(context, id)` 결과 |
| `DomainObject.getObject(context, bo)` | `bo`: `BusinessObject` | `newInstance(context, bo)` 결과 |
| `DomainObject.getObject(context, decoratedOid)` | `decoratedOid`: `DecoratedOid` | `newInstance(context, decoratedOid.getOID())` 결과. 안의 타입은 쓰지 않고 객체를 열어 다시 읽음 |

### setId

같은 객체 변수가 다른 id 를 가리키게 합니다. 반복문에서 객체 변수 하나를 다시 쓸 때 씁니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.setId(id)` | `id`: 객체 id | 없음 (`void`) |

- 지금 id 가 있고 새 id 와 다르면 먼저 [clear](#clear) 를 부릅니다 (닫고 캐시를 비움). 같은 id 면 비우지 않습니다.
- 서버에 가지 않고 타입도 다시 읽지 않습니다. 클래스는 그대로라서 `Part` 객체에 문서 id 를 넣어도 `Part` 객체입니다.
- 이름, 타입, 리비전 칸은 바꾸지 않습니다. context 없는 `getName()`, `getTypeName()`, `getRevision()` 은 다시 열기 전까지 이전 객체의 값을 돌려줍니다. `getInfo` 처럼 안에서 객체를 여는 메서드는 새 id 로 읽습니다.
- `null`, `""` 도 그대로 넣습니다. 예외 없이 디버그 경고만 남깁니다 ([setDebug](#setdebug) 로 켠 때만 출력).
- id 가 비면 `BusinessObject.open` 은 남아 있는 타입, 이름, 리비전, vault 로 엽니다. 이전 객체를 열었던 변수에 빈 id 를 넣으면 다음 `getInfo`, `getId(context)` 가 이전 객체를 다시 열려고 하므로 빈 id 는 넣기 전에 거릅니다.

```java
DomainObject obj = DomainObject.newInstance(context);
for (String id : ids) {
    obj.setId(id);
    String name = obj.getInfo(context, DomainConstants.SELECT_NAME);
}
```

### getId

객체 id 를 꺼냅니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getId(context)` | 없음 | `String`. 객체 id. 닫혀 있으면 열어서 읽고 다시 닫음 |
| `obj.getId()` <Badge type="warning" text="폐기" /> | 없음 | `String`. 변수에 담긴 id. 서버에 가지 않음 |

- 둘 다 id 가 없으면 `FrameworkException` (`emxFramework.Beans.DomainObject.MissingObjectId` 문구) 입니다.
- `getId(context)` 는 열면서 id 를 받아 오므로 타입, 이름, 리비전, vault 만 담긴 객체 (`new BusinessObject(type, name, revision, vault)` 로 만든 것) 에서도 id 가 나옵니다. `getId()` 는 이런 객체에서 예외입니다.
- id 를 담은 객체 (`newInstance(context, id)`, `new DomainObject(id)`) 라도 닫혀 있으면 `getId(context)` 는 서버를 한 번 다녀옵니다.
- `getId()` 대신 `getObjectId()` (`BusinessObject` 메서드) 를 씁니다. 같은 칸을 읽고, id 가 없으면 예외 대신 `null` 을 돌려줍니다.
- 새로 만든 객체의 id 는 [생성](/jpo/api/write#생성) 처럼 `createObject` 뒤에 `getId(context)` 로 꺼냅니다.

```java
DomainObject byTnr = new DomainObject(new BusinessObject(typePart, "P-0001", "A", context.getVault().getName()));
String id = byTnr.getId(context);
String partId = part.getObjectId();
```

### getDecoratedOid

타입과 id 를 함께 담은 `DecoratedOid` 를 만듭니다. OOTB JPO 에서는 쓰지 않습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getDecoratedOid(context)` | 없음 | `DecoratedOid`. `toString()` 은 `"type:타입#id"` |

- 타입은 `getType(context)` (객체를 열어 읽음), id 는 `getId()` 로 채웁니다. 이 값을 `newInstance(context, decoratedOid)` 에 넘기면 타입을 다시 읽지 않습니다.

### openObject

객체가 닫혀 있으면 엽니다. 돌려받은 값을 [closeObject](#closeobject) 에 넘겨 자기가 연 것만 닫습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.openObject(context)` | 없음 | `boolean`. 이번에 열었으면 `true`, 이미 열려 있었으면 `false` |

- 이미 열려 있으면 서버에 가지 않습니다. 열다 실패하면 `FrameworkException` 입니다.
- 열면 id, 타입, 이름, 리비전, vault 가 채워져 context 없는 `getName()`, `getTypeName()`, `getRevision()` 으로 꺼낼 수 있습니다 ([open / close](/jpo/api/read#ootb-에-남아-있는-방식-open-close)).
- `getName(context)`, `getId(context)`, `getInfo` 같은 메서드도 안에서 이 짝으로 열고 닫습니다. 새 코드는 [getInfo](#getinfo) 로 읽습니다.

```java
boolean opened = part.openObject(context);
try {
    String name = part.getName();
    String revision = part.getRevision();
} finally {
    part.closeObject(context, opened);
}
```

### closeObject

두 번째 인자가 `true` 일 때만 객체를 닫습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.closeObject(context, close)` | `close`: `true` 면 닫음, `false` 면 아무것도 안 함. 보통 `openObject` 결과를 넣음 | 없음 |

- 닫을 때 난 `MatrixException` 은 버립니다. 닫기는 서버에 가지 않고 변수의 열린 상태만 지웁니다.
- `closeObject(context, true)` 는 다른 코드가 열어 둔 것도 닫습니다.

### clear

객체 변수에 담아 둔 캐시를 비우고 닫습니다. 가리키는 객체는 바뀌지 않습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.clear()` | 없음 | 없음 |

- 비우는 것: `setCachedData` 캐시, `getRevisions(context, selects, true)` 가 담아 둔 리비전 목록, `getInfo` 등이 다시 쓰는 select 목록 (`getObjectSelectList`, `getRelationshipSelectList`). 그리고 닫음.
- id, 이름, 타입, 리비전은 그대로입니다.
- id 가 바뀔 때는 `setId` 가 알아서 부르므로 `clear()` 다음 `setId(id)` (OOTB `emxResourcePlanTemplateBase_mxJPO.java`) 의 `clear()` 는 같은 id 를 다시 넣을 때만 차이가 납니다.
- OOTB 의 `.clear()` 는 대부분 `StringList`, `MapList`, `Map` 의 것입니다.

::: info 근거
xerinfra.jar `DomainObject` 바이트코드. 생성자는 `BusinessObject` 생성자에 넘기고 캐시 필드를 `null`, `LICENSE_RESERVED_APPS` 를 빈 배열로 둠. eMatrixServletRMI.jar `BusinessObject(String)` 은 id 를 `_objectId` 와 이름 칸에 넣고, `BusinessObject(BusinessObject)` 는 id, 이름, 타입, 리비전, vault 를 복사. `newInstance` 는 모두 private `newInstance(context, id, type, app, interface)` 로 모임: 문자열을 `DecoratedOid` 로 풀어 타입이 있으면 그대로 쓰고, 없으면 `FrameworkUtil.isObjectId` 가 참일 때만 `FrameworkUtil.getType(context, new BusinessObject(id))` (open → `getBusinessType` → close), 거짓이면 그 문자열을 타입으로 씀. interface 를 주면 `print bus $1 select $2 dump` 로 `interface[이름]` 이 `TRUE` 인지 봄. 클래스는 `FrameworkUtil.lookupObject(context, "type", 타입, DomainObject.class, app)` → `lookupClassName` (위 순서, 파일마다 `BusinessType.getParent` 로 상위 타입, 없으면 `com.matrixone.apps.domain.DomainObject`). id 가 있으면 `String` 생성자, 없으면 인자 없는 생성자로 만듦. `getId()` 는 `getObjectId()` 가 `null` 이면 예외, `getId(Context)` 는 `openObject` → `getObjectId()` → `closeObject`. `setId` 는 id 가 다르면 `clear()` 뒤 `setObjectId`. `clear()` 는 `_cacheMap`, `_revisions`, `_objectSelectList`, `_relationshipSelectList` 를 `null` 로 하고 `close`. `BusinessObject.close` 는 연결 핸들만 `null` 로 함. `getObject` 는 `newInstance(Context, String)` / `newInstance(Context, BusinessObject)` 만 부름. 매핑: properties `emxCommonMappingFile.properties` (`type_Part=com.matrixone.apps.common.Part`), engineering.jar `emxEngineeringMappingFile.properties` (`type_Part=com.matrixone.apps.engineering.Part`). OOTB 예: emxCPNProductDataLifecycleBase_mxJPO.java (`BusinessObject` 형태), emxProjectTemplateBase_mxJPO.java (인자 4 개), emxECPartBase_mxJPO.java (`openObject` / `closeObject`)
:::

## 기본 정보

이름, 타입, 상태 같은 기본 값을 읽는 메서드입니다. 이름, 타입, 리비전, 상태를 읽는 getter 는 모두 폐기되었고 [getInfo](#getinfo) 로 대신합니다.

### getName

<Badge type="warning" text="폐기" /> 대신 `getInfo(context, DomainConstants.SELECT_NAME)` 을 씁니다. (안에서 객체를 열어 열 때 받은 이름을 돌려줌)

객체의 이름을 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getName(context)` | 없음 | `String`. 이름 |

- `getInfo` 를 부르지 않고 `openObject` → `getName()` → `closeObject` 로 읽습니다. 닫혀 있으면 부를 때마다 엽니다.

### getType

<Badge type="warning" text="폐기" /> 대신 `getInfo(context, DomainConstants.SELECT_TYPE)` 을 씁니다. (안에서 객체를 열어 타입 이름을 돌려줌)

객체의 타입 이름을 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getType(context)` | 없음 | `String`. 타입 이름 |

- `openObject` → `FrameworkUtil.getType(context, this)` → `closeObject` 로 읽습니다.

### getRevision

<Badge type="warning" text="폐기" /> 대신 `getInfo(context, DomainConstants.SELECT_REVISION)` 을 씁니다. (안에서 객체를 열어 열 때 받은 리비전을 돌려줌)

객체의 리비전을 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getRevision(context)` | 없음 | `String`. 리비전 |

- `openObject` → `getRevision()` → `closeObject` 로 읽고, 실패하면 `FrameworkException` 입니다.

### getCurrentState

<Badge type="warning" text="폐기" /> 상태 이름만 필요하면 대신 `getInfo(context, DomainConstants.SELECT_CURRENT)` 를 씁니다. (OOTB `emxECRBase_mxJPO.java` 도 `getCurrentState(context).getName()` 을 이렇게 바꿔 둠)

현재 상태를 `State` 객체로 받습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getCurrentState(context)` | 없음 | `matrix.db.State`. 현재 상태. 못 찾으면 `null` |

- 객체를 열고 `getStates(context)` (정책의 상태 목록) 에서 `isCurrent()` 인 것을 고릅니다. `State` 객체가 꼭 필요하면 `getStates(context)` 를 직접 씁니다.

### getState

<Badge type="warning" text="폐기" /> 대신 `getStates(context)` 에서 이름으로 고릅니다. (안에서 하는 일과 같음)
<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

정책의 상태 하나를 이름으로 찾습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `getState(context, stateName)` | `stateName`: 상태 이름. 대소문자 무관 | `matrix.db.State`. 없으면 `null` |

### getPolicies

<Badge type="warning" text="폐기" /> 대신 `mxType.getPolicies(context, type, false)` 를 씁니다. (안에서 이 객체의 타입으로 이것만 부름. OOTB `emxChangeBase_mxJPO.java` 도 이렇게 바꿔 둠)

이 객체의 **타입** 에 쓸 수 있는 정책 목록을 받습니다. 객체에 지정된 정책 하나는 `getInfo(context, DomainConstants.SELECT_POLICY)` 로 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getPolicies(context)` | 없음 | `MapList`. 정책마다 Map 하나. 키는 `name`, `description`, `revision` (첫 리비전), `formats` (`StringList`), `defaultFormat`, `store` |

### getObjectDetails

id 하나로 physicalid, 타입, 이름, Title, V_Name 을 한 번에 읽습니다. OOTB JPO 에서는 쓰지 않습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.getObjectDetails(context, id)` | `id`: 객체 id | `Map`. 키는 `physicalid`, `type`, `name`, `attribute[Title]`, `attribute[V_Name]`, `id`. 값은 `String` |

- `new DomainObject(id).getInfo(context, selects)` 와 같습니다. 두 속성 키는 `DomainConstants.SELECT_ATTRIBUTE_TITLE`, `SELECT_ATTRIBUTE_VNAME` (`attribute_Title`, `attribute_V_Name` 의 실제 이름) 이고, `id` 는 `getInfo` 가 더한 것입니다.
- `throws Exception` 으로 선언되어 있습니다.

### getBasicInfo

<Badge type="warning" text="폐기" /> 대신 `getInfo(context, selects)` 와 [getAttributeMap](#getattributemap) 을 씁니다. (안에서 MQL `print bus` 로 기본 항목을, `getAttributes` 로 속성을 읽음)

기본 항목과 속성을 두 행짜리 `MapList` 로 받습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getBasicInfo(context)` | 없음 | `MapList` 두 행. 0 번 행: `type`, `name`, `revision`, `owner`, `description`. 1 번 행: 속성 이름 → 속성 정보 Map (`name`, `value`, `type`, `choices`, `multiline`) |

- 0 번 행은 MQL `print bus $1 select type name revision owner description dump` 결과를 `|~|` 로 잘라 채웁니다. 자를 때 `StringTokenizer` 를 써서 `|` 와 `~` 를 각각 구분 문자로 보고 빈 값을 건너뜁니다. 리비전이 비어 있거나 설명에 `|`, `~` 가 있으면 값이 한 칸씩 밀립니다.
- 1 번 행의 `type` 은 `string`, `integer`, `real`, `timestamp`, `boolean`, `undefined` 중 하나, `choices` 는 `StringList`, `multiline` 은 `"true"` / `"false"` 입니다.
- id 는 `getId()` 로 읽어서 id 가 없으면 `FrameworkException` 입니다.

### getImageObject

이 객체에 연결된 Image Holder 객체를 받습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getImageObject(context)` | 없음 | `com.matrixone.apps.domain.Image`. Image Holder 가 없으면 `null` |

- `DomainObject.SELECT_IMAGE_HOLDER_ID` (`to[relationship_ImageHolder 의 실제 이름].from.id`) 를 `getInfo` 로 읽어 `newInstance(context, id)` 로 만듭니다.
- `emxCommonMappingFile` 에 `type_ImageHolder=com.matrixone.apps.domain.Image` 로 등록되어 있어 `Image` 로 옵니다. 다른 클래스가 나오면 `FrameworkException` 입니다.

### getErrorMessage

받은 예외에서 화면에 보여 줄 오류 문구를 꺼냅니다. OOTB JPO 에서는 쓰지 않습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.getErrorMessage(context, e)` | `e`: 받은 예외 | `String`. 오류 문구 |

아래 순서로 정합니다.

1. `context` 에 쌓인 client task 를 읽고 비웁니다 (서버가 사용자에게 보내려고 모아 둔 메시지로 보입니다 <Badge type="danger" text="검증 필요" />).
2. 예외 메시지 (`getLocalizedMessage()`) 에 `1500028` 이나 `1500019` 가 있으면 영어 고정 문구 `User does not have necessary privileges or selected object is not in correct maturity state for this operation.`
3. client task 가 있었으면 마지막 task 의 내용 (`getTaskData()`)
4. 없으면 메시지를 정규식 `" [A-Za-z]+:"` 로 자른 조각: 조각이 다섯 개 이상이면 세 번째, 메시지에 `System Error` 가 있으면 두 번째 (그 안에 `java.lang.Exception:` 이 있으면 그 뒤), 아니면 첫 번째. 자르다 실패하면 메시지 그대로

- 메시지가 `null` 이면 `""` 로 봅니다. `throws MatrixException` 으로 선언되어 있습니다.

::: info 근거
xerinfra.jar `DomainObject` 바이트코드. `getName(Context)`, `getRevision(Context)` 는 `openObject` → `BusinessObject.getName()` / `getRevision()` → `closeObject`, `getType(Context)` 는 `openObject` → `FrameworkUtil.getType` → `closeObject`. `getCurrentState` / `getState` 는 `openObject` 뒤 `getStates(context)` 를 돌며 `State.isCurrent()` / `getName().equalsIgnoreCase`. `getPolicies` 는 `mxType.getPolicies(context, getTypeName(), false)` (키는 xerinfra.jar `mxType` 바이트코드). `getObjectDetails` 는 `new DomainObject(id).getInfo(context, [physicalid, type, name, SELECT_ATTRIBUTE_TITLE, SELECT_ATTRIBUTE_VNAME])`. `getBasicInfo` 는 `"print bus $1 select $2 $3 $4 $5 $6 dump $7"` 를 `new StringTokenizer(결과, "|~|")` 로 자르고, 속성은 `FrameworkUtil.toMap(Context, AttributeList)`. `getImageObject` 는 `getInfo(context, SELECT_IMAGE_HOLDER_ID)` 가 `null` 이 아니면 `(Image) newInstance(context, id)`. `getErrorMessage` 는 `Context.updateClientTasks` / `getClientTasks` / `clearClientTasks` 뒤 위 순서. OOTB 예: emxImageManagerBase_mxJPO.java, emxCPDUtilBase_mxJPO.java (`getImageObject`), emxENCActionLinkAccessBase_mxJPO.java (`getCurrentState` 를 `getInfo` 로 바꾼 주석)
:::

## 값 읽기

select 문자열로 이 객체 (또는 여러 객체) 의 값을 읽는 메서드와, select 목록을 만드는 메서드입니다. 쓰는 법과 결과 모양은 [객체 조회](/jpo/api/read#값-읽기-getinfo) 에도 있습니다.

### getInfo

select 로 값을 읽습니다. 인스턴스 형태는 이 객체 하나, static 형태는 id 배열의 객체들을 한 번에 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getInfo(context, select)` | `select`: 읽을 select 하나 (`name`, `attribute[속성이름]` ...) | `String`. 값이 여러 개면 첫 값. 서버가 값을 주지 않으면 `null` |
| `obj.getInfo(context, select, substitute)` | `substitute`: 서버에 그대로 넘기는 플래그. 위 형태는 `true` | 위와 같음 |
| `obj.getInfo(context, selects)` | `selects`: 읽을 select 목록 (`StringList`). `null` 이면 `id`, `type` 만 읽음 | `Map` (select → 값). `id`, `type` 이 늘 함께 들어 있음. 값은 `String` (여러 개면 첫 값). `NEW_MULTI_VALUE_LIST` 에 있는 select (`state` ...) 만 `StringList` |
| `obj.getInfo(context, selects, substitute)` | `substitute`: 위와 같음 | 위와 같음 |
| `obj.getInfo(context, selects, multiSelects)` | `multiSelects`: 값을 목록으로 받을 select (`Collection`. `StringList` 를 넘겨도 됨). `null` 이면 모든 select 가 첫 값 `String` | `Map`. `multiSelects` 와 `NEW_MULTI_VALUE_LIST` 에 있는 select 는 `StringList`, 나머지는 `String`. `id`, `type` 은 더하지 않음 |
| `obj.getInfo(context, selects, multiSelects, substitute)` | `substitute`: 위와 같음 | 위와 같음 |
| `DomainObject.getInfo(context, ids, selects)` | `ids`: 읽을 객체 id 배열 (`String[]`). `selects`: 읽을 select. `null` 이면 빈 목록으로 부름 | `MapList`. 객체마다 Map 하나. 넣은 select 만 들어 있음. 값이 여러 개면 `\u0007` 로 이어 붙인 `String`. `NEW_MULTI_VALUE_LIST` 에 있는 select 는 `StringList` |
| `DomainObject.getInfo(context, ids, selects, substitute)` | `substitute`: 위와 같음 | 위와 같음 |
| `DomainObject.getInfo(context, ids, selects, multiSelects)` | `multiSelects`: 목록으로 받을 select. `null` 이면 `NEW_MULTI_VALUE_LIST` 의 select 까지 모두 `String` | `MapList`. `multiSelects` 와 `NEW_MULTI_VALUE_LIST` 에 있는 select 는 `StringList`, 나머지는 `String` |
| `DomainObject.getInfo(context, ids, selects, multiSelects, substitute)` | `substitute`: 위와 같음 | 위와 같음 |

- 인스턴스 형태는 객체가 열려 있지 않으면 `open` 하고, 읽은 뒤 자기가 연 것만 `close` 합니다. 트랜잭션은 열지 않습니다. static 형태는 객체를 열지 않고 `BusinessObject.getSelectBusinessObjectData` 로 한 번에 읽습니다.
- 인스턴스 형태는 서버 값을 모두 목록으로 받은 뒤, 목록으로 받을 select 가 아니면 첫 값 (`get(0)`) 만 남깁니다. 그래서 `from[관계].to.id` 처럼 값이 여러 개여도 `String` 하나만 옵니다. 전부 받으려면 `multiSelects` 에 넣거나 [getInfoList](#getinfolist) 를 씁니다.
- `NEW_MULTI_VALUE_LIST` 에는 `state`, `format.file.name`, `format.file.size`, `format.file.format`, `format.file.modified`, `grantor`, `grantee`, `granteeaccess` 와 몇몇 앱 select 가 들어 있습니다. `getInfo(context, selects)` 결과에서 이 값을 `(String)` 으로 꺼내면 `ClassCastException` 이 납니다. 값 하나 형태 `getInfo(context, "state")` 는 첫 값 `String` 입니다.
- 넘긴 `selects` 는 바뀌지 않습니다 (`getInfo(context, selects)` 는 복사본에 `id`, `type` 을 더함). `getInfo(context, selects, multiSelects)` 에 `selects` 를 `null` 로 넘기면 예외입니다.
- 서버가 돌려주지 않은 select (연결이 없는 `from[관계].to.id` 등) 는 결과 Map 에 키가 없어 `get` 이 `null` 입니다.
- 마지막 `boolean` (`substitute`) 은 인스턴스 형태는 `BusinessObject.select(context, selects, substitute)`, static 형태는 `QueryParams.setSubstitute(substitute)` 로 서버에 그대로 넘어갑니다. 서버 쪽 처리라 이 값으로 결과가 어떻게 달라지는지는 class 파일로 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />. boolean 없는 형태는 모두 `true` 를 넘기므로 보통은 그 형태를 씁니다.

```java
String name = part.getInfo(context, DomainConstants.SELECT_NAME);
String docSelect = "from[" + relPartDocument + "].to.id";
Map info = part.getInfo(context, StringList.create(DomainConstants.SELECT_CURRENT, docSelect),
        StringList.create(docSelect));
String current = (String) info.get(DomainConstants.SELECT_CURRENT);
StringList attachedDocIds = (StringList) info.get(docSelect);
MapList rows = DomainObject.getInfo(context, ids,
        StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME));
```

`info` 의 모양 (값은 예시). `multiSelects` 를 준 형태라 `id`, `type` 이 없습니다.

```text
{current=Release, from[My Part Document].to.id=[11111.22222.33333.77777, 11111.22222.33333.88888]}
```

### getInfoList

값이 여러 개일 수 있는 select 하나를 목록으로 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getInfoList(context, select)` | `select`: 읽을 select (`from[관계].to.id` ...) | `StringList`. 값이 하나여도 목록. 값이 없으면 빈 목록 |
| `obj.getInfoList(context, select, substitute)` | `substitute`: [getInfo](#getinfo) 의 마지막 `boolean` 과 같음. 위 형태는 `true` | 위와 같음 |

- 안에서 `getInfo(context, [select], [select], substitute)` 를 부르고 그 select 의 값을 꺼냅니다. 객체 `open` / `close` 도 `getInfo` 와 같습니다.
- 결과에 그 select 키가 없으면 빈 목록입니다. 단, 서버가 다른 키로 값을 줬으면 키마다 `"키 = 값"` 문자열을 담은 목록이 옵니다.

```java
StringList childIds = part.getInfoList(context, "from[" + relStructure + "].to.id");
StringList parentIds = part.getInfoList(context, "to[" + relStructure + "].from.id");
```

### getBusinessObjectData

select 마다 MQL 을 한 번씩 불러 값을 목록으로 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getBusinessObjectData(context, selects)` | `selects`: 읽을 select 목록 | `Hashtable`. select → `StringList` (값이 하나여도 목록) |

- select 마다 `print bus <id> select <select> dump |MXDELIM|` 을 부르고 결과를 `|MXDELIM|` 로 잘라 담습니다. select 수만큼 서버를 다녀오므로 보통은 `getInfo(context, selects, multiSelects)` 를 씁니다.
- 선언된 예외가 `FrameworkException` 이 아니라 `Exception` 입니다.

### isKindOf

이 객체의 타입이 주어진 타입이거나 그 하위 타입인지 봅니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.isKindOf(context, type)` | `type`: 타입의 실제 이름. `null` 이면 서버에 가지 않고 `false` | `boolean`. `type` 이거나 그 하위 타입이면 `true` |

- MQL `print bus <id> select type.kindof[<type>] dump` 결과가 `TRUE` (대소문자 무시) 인지 봅니다. 객체를 `open` 하지 않고 MQL 한 번입니다.
- 다른 값도 함께 읽을 때는 같은 select `type.kindof[타입]` 을 `getInfo` 의 `selects` 에 넣으면 서버를 한 번 덜 다녀옵니다.

```java
boolean isPart = part.isKindOf(context, typePart);
String kindOfPart = "type.kindof[" + typePart + "]";
Map info = part.getInfo(context, StringList.create(DomainConstants.SELECT_NAME, kindOfPart));
boolean samePart = "TRUE".equalsIgnoreCase((String) info.get(kindOfPart));
```

### getBusSelectList

<Badge type="warning" text="폐기" /> 대신 `getObjectSelectList(size)` 를 씁니다. (안에서 `getObjectSelectList(size)` 만 부름)

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getBusSelectList(size)` | `size`: 처음 잡아 둘 크기 | `SelectList`. [getObjectSelectList](#getobjectselectlist) 와 같은 목록 |

### getObjectSelectList

이 객체 변수가 들고 있는 객체 select 목록 (`SelectList`) 을 비워서 돌려줍니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getObjectSelectList(size)` | `size`: 처음 잡아 둘 크기. 내용에는 영향 없음 | `SelectList` (`StringList` 의 하위 클래스). 늘 빈 목록 |

- 객체 변수마다 목록이 하나뿐이고, 부를 때마다 같은 목록을 비워서 줍니다. 같은 변수로 `getInfo(context, select)`, `getInfoList`, `getAttributeValue`, `getInfo(context, (StringList) null)` 등을 부르면 안에서 이 목록을 다시 비우고 써서, 받아 둔 목록의 내용이 바뀝니다. 채운 뒤 바로 넘기고 다시 쓰지 않습니다.
- `SelectList` 에는 `addName()` (`name`), `addId()` (`id`), `addCurrentState()` (`current`), `addAttribute(속성이름)` (`attribute[속성이름]`) 처럼 select 를 넣는 메서드가 있습니다. OOTB 는 이렇게 채워 `expandSelect`, `getRevisions` 에 넘깁니다.
- 새 코드는 `StringList.create(...)` 로 만들어도 됩니다.

```java
SelectList selects = part.getObjectSelectList(3);
selects.addName();
selects.addCurrentState();
selects.addAttribute(attrStatus);
Map info = part.getInfo(context, selects);
```

### getRelationshipSelectList

이 객체 변수가 들고 있는 관계 select 목록 (`SelectList`) 을 비워서 돌려줍니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getRelationshipSelectList(size)` | `size`: 처음 잡아 둘 크기. 내용에는 영향 없음 | `SelectList`. 늘 빈 목록 |

- [getObjectSelectList](#getobjectselectlist) 와 따로 있는 목록이고, 부를 때마다 같은 목록을 비워서 주는 것은 같습니다.
- OOTB 는 `expandSelect` 의 관계 select 를 만들 때 씁니다.

### getAliasForName

<Badge type="warning" text="폐기" /> 대신 `FrameworkUtil.getAliasForAdmin(context, "attribute", name, useCache)` 를 씁니다. (안에서 이 메서드를 그대로 부름)

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.getAliasForName(context, name, useCache)` | `name`: 속성의 실제 이름. `useCache`: `true` 면 캐시에서 찾음, `false` 면 캐시를 다시 채운 뒤 찾음 | `String`. 속성의 심볼릭 이름 (`attribute_...`). 없거나 `name` 이 비었으면 `null` |

::: info 근거
xerinfra.jar `DomainObject` 바이트코드. `getInfo(Context, String)` → `getInfo(Context, String, true)` → `getObjectSelectList(1)` 에 select 를 넣고 `getInfo(Context, StringList, null, boolean)`. `getInfo(Context, StringList, boolean)` 은 `selects` 가 `null` 이면 `getObjectSelectList(2)`, 아니면 `new StringList(selects)` 에 `id`, `type` 을 더하고 `multiSelects` 자리에 `NEW_MULTI_VALUE_LIST` 를 넘김. `getInfo(Context, StringList, Collection, boolean)` 은 `openObject` → `select(context, selects, substitute)` → `getHashtable()` (eMatrixServletRMI.jar `BusinessObjectWithSelect` 는 `_list = true` 라 값이 모두 `SelectConstants.parseToStringList` 의 `StringList`) → `multiSelects` 가 있으면 `NEW_MULTI_VALUE_LIST` 를 더한 목록에 없는 키를 `get(0)` 으로 바꿈 → `closeObject`. static `getInfo` 는 `BusinessObject.getSelectBusinessObjectData(context, ids, selects, substitute)` (→ `QueryParams.setSubstitute`, `bosQuerySt.substituteIndxName`) 뒤 `FrameworkUtil.toMapList`. 3 개 인자 형태는 `toMapList(list)` = `toMapList(list, NEW_MULTI_VALUE_LIST)`, `multiSelects` 형태는 `multiSelects` 가 `null` 이면 `toMapList(list, null)` 이라 모두 `getSelectData` (`\u0007` 로 이어진 `String`). `NEW_MULTI_VALUE_LIST` 는 `DomainConstants.getMulValList()`. `getInfoList` 는 `getInfo(context, selectList, [select], substitute)` 의 값이 `null` 이면 빈 목록에 `"키 = 값"` 을 더함. `getBusinessObjectData` 는 `FrameworkUtil.getSelectData(context, id, selects, false)` 로 select 마다 `print $1 $2 select $3 dump $4` (`bus`, id, select, `|MXDELIM|`). `isKindOf` 는 `type == null` 이면 `false`, 아니면 `MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", getId(), "type.kindof[" + type + "]")` 가 `TRUE` 인지 (`equalsIgnoreCase`). `getObjectSelectList`, `getRelationshipSelectList` 는 필드 `_objectSelectList`, `_relationshipSelectList` 를 처음에 `new SelectList(size)` 로 만들고 이후엔 `clear()` + `ensureCapacity(size)` (`SelectList(int)` 는 `StringList()` 만 부름). `getBusSelectList` 는 `getObjectSelectList(size)`. `getAliasForName` 은 `FrameworkUtil.getAliasForAdmin(context, "attribute", name, useCache)` → `PropertyUtil.getAliasForAdmin` → `CacheManager.getValue(context, REVERSE_SYMBOLIC_NAMES, !useCache, ...)` (그 boolean 이 `true` 면 캐시를 다시 채움). OOTB 사용 예: `getInfo(context, select, true)` ENOPeriodicReviewUIBase_mxJPO.java, `getObjectSelectList` emxECRBase_mxJPO.java, emxECPartBase_mxJPO.java, `getRelationshipSelectList` emxECRBase_mxJPO.java
:::

## 속성

이 객체의 속성 값과 속성 정보를 읽는 메서드, 속성 select 를 만드는 메서드입니다. 속성 이름은 모두 실제 이름 (`PropertyUtil.getSchemaProperty` 로 바꾼 이름) 을 넘깁니다.

### getAttributeValue

속성 하나의 값을 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getAttributeValue(context, name)` | `name`: 속성 이름 | `String`. 값이 여러 개인 속성은 첫 값 |

- `getInfo(context, DomainObject.getAttributeSelect(name))` 를 부르는 것과 같습니다 ([getInfo](#getinfo)). 객체 `open` / `close` 도 같습니다.
- 속성 여러 개를 읽을 때는 [getAttributeSelect](#getattributeselect) 로 select 를 만들어 `getInfo(context, selects)` 로 한 번에 읽습니다.

```java
String status = part.getAttributeValue(context, attrStatus);
```

### getAttribute

<Badge type="warning" text="폐기" /> 대신 `getAttributeDetails(context)` 를 씁니다. (같은 `getAttributes(context)` 결과에서 속성 정보를 꺼냄. 값만 필요하면 [getAttributeValue](#getattributevalue))

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getAttribute(context, name)` | `name`: 속성 이름. 대소문자 무시 | `matrix.db.Attribute`. 없으면 `null` |

### getAttributeMap

이 객체의 모든 속성을 이름 → 값 Map 으로 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getAttributeMap(context)` | | `Map`. 키는 속성 이름 (`My Status`), 값은 `String`. 여러 값 속성은 `StringList` |
| `obj.getAttributeMap(context, useValues)` | `useValues`: `false` 면 위 형태와 같음. `true` 면 `BusinessObject.getAttributeValues(context, true)` 로 읽음 | 위와 같음 |

- 두 형태 모두 여러 값 속성은 `StringList`, 나머지는 `String` 을 넣습니다. 결과 모양은 같고 서버에서 읽는 호출만 다릅니다.
- 위 형태는 `BusinessObject.getAttributes(context)` 로 읽습니다. 두 호출 모두 서버에 `boolean` 을 하나 넘기는데, 위 형태는 `MX_DEPRECATE_HIDDEN` 설정 (context 의 custom data 나 환경 변수) 에 따라 정하고 `true` 형태는 늘 `true` 입니다. 이 값으로 결과가 어떻게 달라지는지는 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />. OOTB 는 `true` 형태를 주로 씁니다.
- 키는 `attribute[My Status]` 가 아니라 속성 이름입니다. 객체를 `open` 하고 읽은 뒤 자기가 연 것만 `close` 합니다.

```java
Map attrs = part.getAttributeMap(context, true);
Object value = attrs.get(attrStatus);
StringList values = value instanceof StringList ? (StringList) value : StringList.create((String) value);
```

`attrs` 의 모양 (값은 예시. `My Tags` 는 여러 값 속성):

```text
{My Status=Approved, My Tags=[A, B]}
```

### getAttributeDetails

이 객체의 모든 속성을 값과 함께 타입, 선택값까지 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getAttributeDetails(context)` | | `Map`. 키는 속성 이름, 값은 그 속성의 정보 `Map` |

- 속성 정보 Map 의 키: `name` (이름), `value` (값 `String`), `type` (`string`, `integer`, `real`, `timestamp`, `boolean`. 그 밖은 `undefined` 나 빈 문자열), `choices` (선택값 `StringList`), `multiline` (`"true"` / `"false"`).
- OOTB 는 결과에 그 속성 키가 있는지 (`containsKey`) 로 객체에 속성이 있는지 확인하는 데 씁니다.

```text
{My Status={name=My Status, value=Approved, type=string, choices=[Approved, Rejected], multiline=false}}
```

### getAttributeNames

<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

이 객체의 모든 속성 이름을 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getAttributeNames(context)` | | `StringList`. 속성 이름 |

### getAttributeSelect

속성 값을 읽는 select 문자열을 만듭니다. 서버를 다녀오지 않습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.getAttributeSelect(name)` | `name`: 속성 이름 | `String`. `attribute[name]`. `name` 이 `null` 이면 `ERROR: NULL ATTRIBUTE` |

```java
String statusSelect = DomainObject.getAttributeSelect(attrStatus);
Map info = part.getInfo(context, StringList.create(DomainConstants.SELECT_NAME, statusSelect));
String status = (String) info.get(statusSelect);
```

### getTypeAttributeNames

<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

타입에 정의된 속성 이름을 읽습니다. 객체가 없어도 됩니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.getTypeAttributeNames(context, type)` | `type`: 타입의 실제 이름 | `StringList`. 그 타입의 속성 이름 |

### getTypeAttributes

<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

타입에 정의된 속성의 정보를 읽습니다. 안에서 public 인 `mxType.getAttributes(context, type)` 을 그대로 부릅니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.getTypeAttributes(context, type)` | `type`: 타입의 실제 이름 | `MapList`. 속성마다 Map (`name`, `type`, `description`, `default`, `choices`) |

### validateSelectedAttributes

속성 이름 목록에서 타입에 없는 속성을 빼고, 빠진 것이 있으면 걸러진 목록을 이 객체의 속성에 씁니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.validateSelectedAttributes(context, names, attrName, type)` | `names`: 속성 이름 목록 (`StringList`). `attrName`: 걸러진 목록을 저장할 이 객체의 속성. `type`: 기준 타입의 실제 이름 | `StringList`. `names` 중 `type` 에 정의된 속성만 |
| `obj.validateSelectedAttributes(context, rows, attrName, type)` | `rows`: `StringList` 의 `List`. 행마다 첫 값은 속성의 심볼릭 이름 (`attribute_...`). 요소가 2 개 이상인 행만 봄 | `List`. 첫 값의 실제 이름이 `type` 에 있는 행만 |

- 거른 뒤 `ContextUtil.pushContext(context)` 로 super user 가 되고, 줄어들었으면 `StringList` 형태는 `~` 로, `List` 형태는 행 안을 `~`, 행 사이를 `;` 로 이어 붙여 `attrName` 에 씁니다. 끝에 `popContext` 합니다.

::: info 근거
xerinfra.jar `DomainObject` 바이트코드. `getAttributeValue` 는 `getInfo(context, getAttributeSelect(name))`. `getAttributeSelect` 는 `null` 이면 `"ERROR: NULL ATTRIBUTE"`, 아니면 `"attribute[" + name + "]"`. `getAttribute` 는 `openObject` → `getAttributes(context).getAttributes()` 를 돌며 `getName().equalsIgnoreCase(name)` → `closeObject`. `getAttributeMap(Context)` 는 `getAttributes(context).getAttributes()`, `getAttributeMap(Context, true)` 는 `getAttributeValues(context, true)` 의 `AttributeList` 를 돌며 `isMultiVal()` 이면 `getValueList()`, 아니면 `getValue()` 를 `getName()` 키로 넣음. `getAttributeMap(Context, false)` 는 `getAttributeMap(Context)`. eMatrixServletRMI.jar `BusinessObject.getAttributes(Context)` 는 `KNLDeprecated.isDeprecated(context, MX_DEPRECATE_HIDDEN)` 를, `getAttributeValues(Context, StringList, boolean)` 은 `boolean || isDeprecated(...)` 를 서버에 넘김. `getAttributeDetails` 는 `FrameworkUtil.toMap(Context, AttributeList)` 로 속성마다 `type` (`getAttributeDataType`), `name`, `choices`, `multiline`, `value` 를 넣음. `getAttributeNames` 는 `getAttributes(context)` 의 이름. `getTypeAttributeNames` 는 `new BusinessType(type, context.getVault())` 를 `open` 해 `getAttributeTypes` 의 이름. `getTypeAttributes` 는 `mxType.getAttributes(context, type)` (안에서 `ContextUtil.startTransaction(context, false)` 뒤 `BusinessType` 의 `AttributeType` 마다 `name`, `type`, `description`, `default`, `choices`, 끝에 commit). `validateSelectedAttributes` 는 `getTypeAttributeNames(context, type)` 에 있는 것만 남기고 (`List` 형태는 `PropertyUtil.getSchemaProperty(context, 첫 값)`), `pushContext` 뒤 크기가 다르면 `FrameworkUtil.join` 결과로 `setAttributeValue(context, attrName, ...)`, `finally` 에서 `popContext`. OOTB 사용 예: `getAttributeMap(context, true)` emxProjectTemplateBase_mxJPO.java, emxMilestonesBase_mxJPO.java, `getAttributeDetails` emxCommonDocumentBase_mxJPO.java, `getTypeAttributeNames` emxProjectTemplateBase_mxJPO.java
:::

## 검색

타입, 이름, 조건으로 객체를 찾는 static 메서드입니다. 자주 쓰는 세 형태와 결과 모양은 [검색 (findObjects)](/jpo/api/read#검색-findobjects) 에 있고, 여기서는 모든 형태의 인자 순서와 기본값을 정리합니다.

### findObjects

MQL `temp query` 처럼 조건에 맞는 객체를 찾아 select 값을 `MapList` 로 돌려줍니다. 형태는 11 개이고 모두 맨 아래 15 개 인자 형태로 모입니다. 형태마다 몇 개를 빼고 받을 뿐 인자 순서는 같고, 8 개 인자 형태만 `orderBy` 와 `multiSelects` 의 순서가 바뀝니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.findObjects(context, type, vault, where, selects)` | 빠진 인자: `name`, `revision`, `owner` 는 `"*"`, `expandType` 은 `true`, `limit` 은 `0`, `multiSelects` 는 기본 목록 | `MapList`. 찾은 객체 하나가 행 Map 하나 |
| `DomainObject.findObjects(context, type, vault, where, selects, multiSelects)` | 5 개 형태에 `multiSelects` 를 더함 | 같음 |
| `DomainObject.findObjects(context, type, vault, where, selects, limit, orderBy)` | `name`, `revision`, `owner` 는 `"*"`, `expandType` 은 `true`, `multiSelects` 는 기본 목록 | 같음 |
| `DomainObject.findObjects(context, type, vault, where, selects, limit, orderBy, multiSelects)` | `name`, `revision`, `owner` 는 `"*"`, `expandType` 은 `true`. **`orderBy` 가 `multiSelects` 앞** | 같음 |
| `DomainObject.findObjects(context, type, name, revision, owner, vault, where, expandType, selects)` | `limit` 은 `0`, `multiSelects` 는 기본 목록 | 같음 |
| `DomainObject.findObjects(context, type, name, revision, owner, vault, where, expandType, selects, multiSelects)` | `limit` 은 `0` | 같음 |
| `DomainObject.findObjects(context, type, name, revision, owner, vault, where, queryName, expandType, selects, limit)` | `multiSelects` 는 기본 목록 | 같음 |
| `DomainObject.findObjects(context, type, name, revision, owner, vault, where, queryName, expandType, selects, limit, multiSelects)` | 11 개 형태에 `multiSelects` 를 더함 | 같음 |
| `DomainObject.findObjects(context, type, name, revision, owner, vault, where, queryName, expandType, selects, limit, searchFormat, searchText)` <Badge type="warning" text="폐기" /> | `multiSelects` 는 기본 목록 | 같음 |
| `DomainObject.findObjects(context, type, name, revision, owner, vault, where, queryName, expandType, selects, limit, searchFormat, searchText, multiSelects)` | 13 개 형태에 `multiSelects` 를 더함 | 같음 |
| `DomainObject.findObjects(context, type, name, revision, owner, vault, where, queryName, expandType, selects, limit, searchFormat, searchText, multiSelects, orderBy)` | 모든 인자를 직접 넘김 | 같음 |

표에서 빠졌다고 적지 않은 인자는 이렇게 채워집니다: `queryName` 은 `null`, `searchFormat` 은 `"*"`, `searchText` 는 `""`, `orderBy` 는 빈 목록 (정렬 없음).

| 인자 | 형 | 넘기는 값 |
| --- | --- | --- |
| `type` | `String` | 타입 이름 패턴. 여러 개는 쉼표로 이음 (`"A,B"`). `null` 이면 `"*"` |
| `name`, `revision`, `owner` | `String` | 이름, 리비전, 소유자 패턴 (`"P-*"`). `null` 이면 `"*"` |
| `vault` | `String` | vault 패턴. `null` 이면 검색용 vault 설정 (`VaultUtil.getSearchVaultPattern(context)`) |
| `where` | `String` | 조건식 (`"current==Release"`). `null` 이면 `""` (조건 없음) |
| `queryName` | `String` | 저장된 query 이름. 주면 그 query 를 열지만 타입, 이름, where, 개수 같은 조건은 이 메서드의 인자로 다시 넣습니다. 보통 `null` |
| `expandType` | `boolean` | `true` 면 하위 타입까지 찾음 |
| `selects` | `StringList` | 찾은 객체에서 읽을 select. `null` 이면 빈 목록. `type` 이 없으면 더해서 읽음 |
| `limit` | `short` | 찾을 최대 개수. `(short) 0` 은 `matrix.db.Query` 의 기본값과 같은 값이고, OOTB 는 개수를 제한하지 않을 때 넘김. `short` 이므로 32767 까지 |
| `searchFormat`, `searchText` | `String` | 파일 내용 검색 (전문 검색) 에 쓸 포맷과 찾을 글자. `null` 이면 `"*"`, `""`. OOTB 는 거의 `null` <Badge type="danger" text="검증 필요" /> |
| `multiSelects` | `Collection` | 값을 `StringList` 로 받을 select. 넘긴 목록에 기본 목록 (`DomainConstants.NEW_MULTI_VALUE_LIST`) 을 더해 씀. `null` 이면 기본 목록도 쓰지 않아 모든 값이 `String` ([값이 여러 개인 select](/jpo/api/read#값이-여러-개인-select)) |
| `orderBy` | `StringList` | 정렬에 쓸 select. OOTB 는 `"-modified"`, `"-originated"` 처럼 앞에 `-` 를 붙여 넘김 (`-` 는 내림차순으로 보입니다 <Badge type="danger" text="검증 필요" />) |

결과의 행 Map 키는 `selects` 에 넣은 select 문자열과 `type` 입니다. `id` 도 `selects` 에 넣어야 들어옵니다.

- 트랜잭션이 없으면 읽기 트랜잭션 (`startTransaction(context, false)`) 을 열고, 끝나면 commit 합니다. 실패하면 직접 연 경우에만 abort 하고 `FrameworkException` 을 던집니다.
- 개수 제한이 없는 형태 (`limit` 이 `0`) 는 where 로 꼭 좁힙니다 ([where 를 꼭 좁힙니다](/jpo/api/read#검색-findobjects)).
- 폐기된 13 개 인자 형태 대신 14 개 인자 형태에 `multiSelects` 로 `DomainConstants.NEW_MULTI_VALUE_LIST` 를 넘깁니다. 13 개 형태는 그 값을 넣어 14 개 형태를 부르기만 합니다.

::: warning 8 개 인자 형태만 순서가 다릅니다
`findObjects(context, type, vault, where, selects, limit, orderBy, multiSelects)` 는 `orderBy` 가 `multiSelects` 보다 앞이고, 15 개 인자 형태는 `multiSelects` 가 `orderBy` 보다 앞입니다. `StringList` 는 `Collection` 이기도 해서 두 값을 바꿔 넣어도 컴파일됩니다.
:::

```java
StringList selects = StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME, DomainConstants.SELECT_REVISION);
MapList latest = DomainObject.findObjects(context, typePart, "P-*", "*", "*", null, "revision==last", null, true,
        selects, (short) 100);
MapList recent = DomainObject.findObjects(context, typePart, null, "current==Release", selects, (short) 50,
        StringList.create("-modified"));
for (Object o : recent) {
    Map row = (Map) o;
    String id = (String) row.get(DomainConstants.SELECT_ID);
}
```

`recent` 의 모양 (값은 예시):

```text
[
  {id=11111.22222.33333.55555, name=P-0002, revision=A, type=My Part},
  {id=11111.22222.33333.44444, name=P-0001, revision=B, type=My Part}
]
```

값이 여러 개인 select 는 `multiSelects` 에도 넣어 `StringList` 로 받습니다.

```java
String selectDocIds = "from[" + relPartDocument + "].to.id";
MapList parts = DomainObject.findObjects(context, typePart, null, "revision==last",
        StringList.create(DomainConstants.SELECT_ID, selectDocIds), StringList.create(selectDocIds));
for (Object o : parts) {
    Map row = (Map) o;
    StringList partDocIds = (StringList) row.get(selectDocIds);
}
```

### querySelect

<Badge type="warning" text="폐기" /> 대신 [findObjects](#findobjects) 를 씁니다. 두 형태 모두 뒤의 `MapList`, `boolean` 인자는 쓰지 않고 같은 값으로 `findObjects` 를 부르기만 합니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.querySelect(context, type, vault, where, selects, mapList, flag)` | 앞 5 개는 `findObjects(context, type, vault, where, selects)` 와 같음. `mapList`, `flag` 는 쓰지 않음 | `MapList`. 5 개 인자 `findObjects` 의 결과 |
| `DomainObject.querySelect(context, type, name, revision, owner, vault, where, expandType, selects, mapList, flag)` | 앞 9 개는 `findObjects(context, type, name, revision, owner, vault, where, expandType, selects)` 와 같음. `mapList`, `flag` 는 쓰지 않음 | `MapList`. 9 개 인자 `findObjects` 의 결과 |

### multiLevelRecursionCheck

`fromId → toId` 로 관계를 만들면 순환 (자기 자신이나 자기 아래에 있는 객체를 다시 자식으로 넣는 것) 이 되는지 확인합니다. 연결은 하지 않고 확인만 합니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.multiLevelRecursionCheck(context, fromId, toId, relPattern, checkRevisions)` | `fromId`: 부모 (from 쪽) 로 둘 객체 id. `toId`: 자식 (to 쪽) 으로 둘 객체 id. `relPattern`: 따라갈 관계 이름 (쉼표로 여러 개). 심볼릭 이름 (`relationship_...`) 이면 실제 이름으로 바꿈. `checkRevisions`: `true` 면 id 대신 모든 리비전 id (`revisions.id`) 로 비교 | `boolean`. 순환이 되면 `true` |

- 두 가지를 봅니다. (1) `fromId` 와 `toId` 의 값 (`id` 또는 `revisions.id`) 이 같으면 `true`. (2) `toId` 에서 `relPattern` 을 자식 쪽으로 끝까지 펼쳐 (`getRelatedObjects(..., false, true, (short) 0, ...)`), 나온 객체 중 `fromId` 와 값이 같은 것이 있으면 `true`.
- `fromId`, `toId`, `relPattern` 중 하나라도 `null` 이나 `""` 면 `FrameworkException` (`emxFramework.ErrorMessage.InvalidArgument` 문구) 을 던집니다.
- `checkRevisions` 가 `true` 면 (2) 의 비교가 맞지 않을 수 있습니다. `fromId` 쪽 `revisions.id` 는 `\u0007` 로 이어진 문자열인데, `getRelatedObjects` 쪽은 값이 여럿이면 `StringList` 로 바뀌기 때문입니다 <Badge type="danger" text="검증 필요" />.
- OOTB 는 EBOM 을 연결하기 전에 부르고, `true` 면 오류를 던집니다 (emxENCFullSearchBase_mxJPO.java, emxBOMPartManagementBase_mxJPO.java).

```java
String childId = ids[0];
if (DomainObject.multiLevelRecursionCheck(context, objectId, childId, relStructure, false)) {
    throw new FrameworkException("Recursion: " + childId);
}
DomainRelationship.connect(context, part, relStructure, new DomainObject(childId));
```

::: info 근거
xerinfra.jar `DomainObject.findObjects`: 10 개 형태가 값을 채워 15 개 인자 형태 `(Context, type, name, revision, owner, vault, where, queryName, expandType, selects, limit, searchFormat, searchText, multiSelects, orderBy)` 를 부름 (14 개 형태를 거치는 것 포함, 그때 `orderBy` 는 `new StringList()`). 15 개 형태는 `ContextUtil.isTransactionActive` 가 `false` 면 `startTransaction(context, false)`, `null` 인 type / name / revision / owner / searchFormat 은 `"*"`, where / searchText 는 `""`, vault 는 `VaultUtil.getSearchVaultPattern(context)`, selects 에 `type` 이 없으면 더함. `queryName` 이 있으면 `new Query(queryName)`, 없으면 `new Query()` 를 `open` 한 뒤 `setBusinessObjectType`, `setBusinessObjectName`, `setBusinessObjectRevision`, `setOwnerPattern`, `setVaultPattern`, `setWhereExpression`, `setExpandType`, `setObjectLimit`, `setSearchFormat`, `setSearchText` (eMatrixServletRMI.jar `QueryParamsBase` 에서 `expandTypes`, `limit`, `fullTextFormat`, `fullTextSearch` 로 들어감. `limit` 초기값 `0`). `multiSelects` 가 있으면 `NEW_MULTI_VALUE_LIST` 를 더해 `getIterator(context, selects, (short) 1000, orderBy)` → `FrameworkUtil.toMapList(QueryIterator, Collection)` (목록에 있는 키만 `getSelectDataList`) → `Query.close`, commit. 13 개 인자 형태에만 `Deprecated`. `querySelect` 두 형태도 `Deprecated` 이고 `mapList`, `boolean` 을 버리고 13 개 인자 `findObjects` 를 부름. `multiLevelRecursionCheck`: `getInfo(context, {fromId, toId}, [id 또는 revisions.id])` 두 행의 `values()` 를 `retainAll`, 이어서 `newInstance(context, toId).getRelatedObjects(context, rel, "*", selects, null, false, true, (short) 0, null, null, 0)` 의 행마다 `retainAll`. `rel` 은 `PropertyUtil.getSchemaProperty` 결과가 비지 않으면 그것을 씀. emx / ENO 파일의 `findObjects` 호출: 5 개 인자 122, 9 개 73, 11 개 69, 13 개 18, 그 밖 15. 예: emxProductBase_mxJPO.java (쉼표로 이은 타입), emxDashboardDocumentsBase_mxJPO.java (7 개 형태, `-originated`, `-modified`), emxECPartBase_mxJPO.java (15 개 형태, `-modified`), emxAppContentBase_mxJPO.java (13 개 형태, 포맷 다음 검색 글자)
:::

## 관계 따라가기

이 객체에 연결된 객체를 목록, 한 개, 있는지 여부로 받는 메서드입니다. 형태가 많지만 모두 인자 18 개짜리 `getRelatedObjects` 하나로 모입니다.

### getRelatedObjects

이 객체에서 관계를 따라 연결된 객체를 `MapList` 로 받습니다. 연결 하나가 행 Map 하나이고, 객체 값과 관계 값이 한 Map 에 같이 담깁니다. 방향 고르기와 기본 예제는 [관계 따라가기 (getRelatedObjects)](/jpo/api/read#관계-따라가기-getrelatedobjects) 에 있습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getRelatedObjects(context, relPattern, typePattern, objectSelects, relSelects, getTo, getFrom, level, objectWhere, relWhere, limit)` | 기본 형태 (인자 11 개) | `MapList`. 없으면 빈 `MapList` |
| `obj.getRelatedObjects(context, relPattern, typePattern, objectSelects, relSelects, getTo, getFrom, level, objectWhere, relWhere)` <Badge type="warning" text="폐기" /> | 11 개에서 `limit` 를 뺀 것 (`limit` 는 `0`) | 같음 |
| `obj.getRelatedObjects(context, relPattern, typePattern, objectSelects, relSelects, getTo, getFrom, level, objectWhere, relWhere, limit, includeType, includeRel, includeMap)` | 11 개 + 받은 행을 한 번 더 거르는 `Pattern` 두 개와 `Map` | 같음 |
| `obj.getRelatedObjects(context, relPattern, typePattern, objectSelects, relSelects, getTo, getFrom, level, objectWhere, relWhere, includeType, includeRel, includeMap)` <Badge type="warning" text="폐기" /> | 위에서 `limit` 를 뺀 것 (`limit` 는 `0`) | 같음 |
| `obj.getRelatedObjects(context, relPattern, typePattern, getTo, getFrom, level, objectSelects, relSelects, objectWhere, relWhere, limit, includeRel, includeType, includeMap)` | 순서가 다른 형태. `getTo`, `getFrom`, `level` (`int`) 이 select 앞에 오고, 거르는 패턴이 `String` 이며 관계, 타입 순서 | 같음. `id`, `type`, `id[connection]` 이 늘 들어감 |
| `obj.getRelatedObjects(context, relPattern, typePattern, getTo, getFrom, level, objectSelects, relSelects, objectWhere, relWhere, includeRel, includeType, includeMap)` <Badge type="warning" text="폐기" /> | 위에서 `limit` 를 뺀 것 (`limit` 는 `0`) | 같음 |
| `obj.getRelatedObjects(context, relPattern, typePattern, objectSelects, relSelects, getTo, getFrom, level, objectWhere, relWhere, limit, checkHidden, preventDuplicates, pageSize, includeType, includeRel, includeMap, relKeyPrefix)` | 모든 형태가 모이는 형태 (인자 18 개). `limit` 가 `short` | 같음 |
| `obj.getRelatedObjects(context, relPattern, typePattern, objectSelects, relSelects, getTo, getFrom, level, objectWhere, relWhere, limit, checkHidden, preventDuplicates, pageSize, includeType, includeRel, includeMap, relKeyPrefix, effectivity)` | 18 개 + effectivity 필터 식 | 같음 |
| `obj.getRelatedObjects(context, relPattern, typePattern, objectSelects, relSelects, getTo, getFrom, level, objectWhere, relWhere, limit, checkHidden, preventDuplicates, pageSize, includeType, includeRel, includeMap, relKeyPrefix, effectivity, effectivityOn)` | 19 개 + 필터를 붙일 where | 같음 |

인자마다 넣는 값입니다.

| 인자 | 형 | 넘기는 값 |
| --- | --- | --- |
| `relPattern` | `String` | 따라갈 관계 이름. 쉼표로 여러 개, 전부는 `"*"`. `null` / `""` 면 `"*"` 로 바뀜 |
| `typePattern` | `String` | 받을 객체의 타입 이름. 쉼표, `"*"`. 하위 타입 포함 설정 (`setDerivedTypes(true)`) 으로 넘김. `null` / `""` 는 `emxFramework.enableTypePattern_NULL_as_ALL` 이 `true` 일 때만 `"*"` 로 바뀌므로 `"*"` 를 직접 넣음 |
| `objectSelects` | `StringList` | 연결된 객체에서 읽을 select. `null` 이면 빈 목록. `type` 은 없으면 더해짐 |
| `relSelects` | `StringList` | 관계에서 읽을 select. `null` 이면 빈 목록 |
| `getTo`, `getFrom` | `boolean` | `getTo` 는 부모 쪽, `getFrom` 은 자식 쪽. 둘 다 `true` 면 양쪽 |
| `level` | `short` (순서 다른 형태는 `int`) | 몇 단계까지. `1` 은 바로 연결된 것만, `0` 은 끝까지 |
| `objectWhere`, `relWhere` | `String` | 조건식. `null` 이면 `""` (조건 없음) |
| `limit` | `int` (18 ~ 20 개는 `short`) | 서버에서 받을 최대 행 수. `0` 이면 제한 없음. 안에서 `short` 로 바꾸므로 32767 을 넘기지 않음 |
| `includeType` | `Pattern` (순서 다른 형태는 `String`) | 받은 행 중 `type` 값이 맞는 행만 남김. `null` (`String` 은 `""` 도) 이면 거르지 않음 |
| `includeRel` | `Pattern` (순서 다른 형태는 `String`) | 받은 행 중 관계 이름이 맞는 행만 남김. 비우는 규칙은 `includeType` 과 같음 |
| `includeMap` | `Map` | 키는 행의 select 키, 값은 패턴. 모든 키가 맞는 행만 남김. `null` 이면 거르지 않음 |
| `checkHidden` | `boolean` | `ExpandParams.setCheckHidden` 에 넘김. 18 개보다 짧은 형태는 `true` |
| `preventDuplicates` | `boolean` | `ExpandParams.setPreventDuplicates` 에 넘김. 짧은 형태는 `false` |
| `pageSize` | `short` | `ExpandParams.setPageSize` 에 넘김. 짧은 형태는 `1000` |
| `relKeyPrefix` | `String` | 결과 Map 에서 관계 select 키 앞에 붙일 글자. `null` / `"null"` 이면 붙이지 않음. 짧은 형태는 `null` |
| `effectivity` | `String` | effectivity 필터 식 (OOTB 는 컴파일된 식을 넘김). `null` / `"null"` / `""` 이면 필터 없음 |
| `effectivityOn` | `short` | 필터를 붙일 곳. `0` 객체 where 와 관계 where 둘 다, `1` 관계 where, `2` 객체 where. 범위 밖이면 `0`. 19 개 형태는 `0` |

결과 `MapList` 의 행 Map 에는 아래 키가 들어갑니다.

| 행 Map 의 키 | 값 |
| --- | --- |
| `objectSelects`, `relSelects` 에 넣은 select | 그 값. 관계 select 는 `relKeyPrefix` 가 있으면 그 글자를 앞에 붙인 키 |
| `type` | 연결된 객체의 타입. 늘 들어감 |
| `level` | 시작 객체에서 몇 단계인지 (`"1"`, `"2"` ...). 늘 들어감 |
| `relationship` | 관계 이름. 늘 들어감 |
| `id`, `id[connection]` | select 에 넣었을 때만. 순서 다른 형태 (13, 14 개 `String`) 만 넣지 않아도 들어감 |

- 폐기 형태는 `limit` 자리에 `0` 을 넣어 다른 형태를 부르기만 합니다. 10 개 형태 대신 11 개 형태에 `limit` `0`, 13 개 `Pattern` 형태 대신 14 개 `Pattern` 형태에 `limit` `0`, 13 개 `String` 형태 대신 14 개 `String` 형태에 `limit` `0` 을 씁니다.
- 트랜잭션이 없으면 읽기 트랜잭션 (`startTransaction(context, false)`) 을 직접 열고 끝나면 commit, 오류면 abort 합니다. 이미 트랜잭션 안이면 열지도 닫지도 않습니다. 오류는 `FrameworkException` (메시지는 원래 예외의 `toString()`) 으로 던집니다.
- 11, 14 개 형태 (와 그 폐기 형태) 는 객체를 open 했다가 닫습니다. 18 ~ 20 개 형태는 open 하지 않습니다.
- 객체 값을 먼저 넣고 관계 값을 넣으므로 키가 같으면 관계 값이 남습니다. 겹치지 않게 하려면 18 개 형태의 `relKeyPrefix` 를 씁니다.
- `includeType`, `includeRel`, `includeMap` 은 서버에서 다 받은 뒤 하나씩 걸러 냅니다. 그래서 `limit` 는 거르기 전 행 수에 적용되고, 걸러진 행 아래 단계의 행은 그대로 남습니다.
- 패턴은 쉼표로 여러 개를 넣고 `*` (아무 글자 여럿), `?` (아무 글자 하나) 를 씁니다. `includeType`, `includeRel` 은 대소문자를 구분하고, `includeMap` 은 양쪽을 대문자로 바꿔 비교하므로 구분하지 않습니다. `includeType` 은 타입 이름 글자만 비교하므로 하위 타입도 남기려면 그 이름을 쉼표로 더 넣습니다.
- `includeMap` 의 키는 select 에 넣은 키여야 합니다. 행에 없는 키나 값이 여러 개 (`StringList`) 인 키를 넣으면 예외 (`NullPointerException`, `ClassCastException`) 가 `FrameworkException` 으로 나옵니다.
- 순서 다른 형태 (14 개 `String`) 는 넘긴 `objectSelects` 목록에 `id`, `type` 을 직접 더합니다 (넘긴 목록이 바뀜). 거르는 패턴은 `includeRel`, `includeType` 순서라 `Pattern` 형태 (`includeType`, `includeRel`) 와 반대입니다.
- `checkHidden`, `preventDuplicates`, `pageSize` 는 `ExpandParams` 에 그대로 넘기기만 합니다. 서버에서 숨김 객체 확인, 중복 막기, 한 번에 받는 행 수로 동작하는지는 이름으로만 짐작합니다 <Badge type="danger" text="검증 필요" />.
- 19, 20 개 형태의 effectivity 필터는 `appVersionEffectivityFramework` 가 등록되어 있을 때만 붙습니다. 아니면 18 개 형태와 같습니다.

구조를 끝까지 펼친 뒤 `My Part` 타입이고 상태가 `Release` 인 행만 남깁니다. `typePattern` 이 `"*"` 이므로 다른 타입을 거쳐 내려간 행까지 받은 다음에 거릅니다.

```java
Map<String, String> includeMap = new HashMap<>();
includeMap.put(DomainConstants.SELECT_CURRENT, "Release");
MapList released = part.getRelatedObjects(context, relStructure, "*",
        StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_CURRENT),
        StringList.create(DomainConstants.SELECT_RELATIONSHIP_ID),
        false, true, (short) 0, null, null, 0,
        new Pattern(typePart), null, includeMap);
```

### getRelatedObject

바로 연결된 객체 중 첫 번째 하나를 `Map` 으로 받습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getRelatedObject(context, relPattern, isFrom, objectSelects, relSelects)` | `relPattern`: 관계 이름 (쉼표, `"*"`). `isFrom`: `true` 면 자식 쪽, `false` 면 부모 쪽. `objectSelects`, `relSelects`: [getRelatedObjects](#getrelatedobjects) 와 같음 | `Map`. 모양은 `getRelatedObjects` 의 한 행과 같음. 없으면 `null` |

- 안에서 `getRelatedObjects(context, relPattern, "*", objectSelects, relSelects, !isFrom, isFrom, (short) 1, null, null)` 를 부르고 첫 행을 돌려줍니다.
- 여러 개가 연결되어 있어도 예외 없이 첫 행만 돌려줍니다. 순서를 정하는 처리는 없으므로 하나만 연결되는 관계에 씁니다.
- 타입 `"*"`, 1 단계, 조건 없음이 고정입니다. 타입이나 조건으로 거르려면 [getRelatedObjects](#getrelatedobjects) 를 씁니다.

```java
Map docRow = part.getRelatedObject(context, relPartDocument, true,
        StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME), null);
String docId = docRow == null ? null : (String) docRow.get(DomainConstants.SELECT_ID);
```

### getRelationship

<Badge type="warning" text="폐기" /> 대신 `getRelatedObject` 를 씁니다. (같은 인자를 `getRelatedObject` 에 그대로 넘기기만 함)

<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

바로 연결된 객체 중 첫 번째 하나를 받음.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getRelationship(context, relPattern, isFrom, objectSelects, relSelects)` | [getRelatedObject](#getrelatedobject) 와 같음 | `Map`. 없으면 `null` |

### hasRelatedObjects

이 객체에 그 관계로 연결된 것이 하나라도 있는지 봅니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.hasRelatedObjects(context, rel, isFrom)` | `rel`: 관계 이름 하나. `isFrom`: `true` 면 이 객체가 from 쪽인 연결 (자식 쪽), `false` 면 to 쪽인 연결 (부모 쪽) | `boolean`. select 값이 `TRUE` (대소문자 무시) 면 `true` |

- `isFrom` 이 `true` 면 `from[rel]`, `false` 면 `to[rel]` 을 select 해서 값이 `TRUE` 인지만 봅니다. `rel` 은 select 안에 그대로 들어갑니다.
- 객체를 open 했다가 닫습니다. 연결된 객체의 id 나 개수가 필요하면 [getRelatedObjects](#getrelatedobjects) 나 `getInfoList(context, "from[관계].to.id")` 를 씁니다.

```java
boolean hasChildren = part.hasRelatedObjects(context, relStructure, true);
boolean isUsed = part.hasRelatedObjects(context, relStructure, false);
```

### hasObjects

<Badge type="warning" text="폐기" /> 대신 `hasRelatedObjects` 를 씁니다. (같은 인자를 `hasRelatedObjects` 에 그대로 넘기기만 함)

<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

그 관계로 연결된 것이 있는지 봄.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.hasObjects(context, rel, isFrom)` | [hasRelatedObjects](#hasrelatedobjects) 와 같음 | `boolean` |

### expandSelect

<Badge type="warning" text="폐기" /> 대신 `getRelatedObjects` 를 씁니다. (세 형태 모두 조회는 `getRelatedObjects` 에 `limit` `0` 으로 맡김)

연결된 객체를 `MapList` 로 받음.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.expandSelect(context, relPattern, typePattern, objectSelects, relSelects, getTo, getFrom, level, objectWhere, relWhere, cache, useCache)` | 앞 10 개는 [getRelatedObjects](#getrelatedobjects) 와 같음. `cache` (`MapList`), `useCache` (`boolean`) 는 쓰지 않음 | `MapList` |
| `obj.expandSelect(context, relPattern, typePattern, objectSelects, relSelects, getTo, getFrom, level, objectWhere, relWhere, includeType, includeRel, includeMap, cache, useCache)` | 앞 13 개는 `getRelatedObjects` 와 같음. `cache`, `useCache` 는 쓰지 않음 | `MapList` |
| `obj.expandSelect(context, relPattern, typePattern, objectSelects, relSelects, getTo, getFrom, level, objectWhere, relWhere, cache, useCache, relKeyPrefix)` | `useCache` 가 `true` 이고 `cache` 가 `null` 이 아니면 조회 없이 `cache` 를 그대로 돌려줌. 아니면 18 개 형태로 조회 | `MapList` |

- 12 개 형태 대신 `getRelatedObjects(context, relPattern, typePattern, objectSelects, relSelects, getTo, getFrom, level, objectWhere, relWhere, 0)` 를 씁니다.
- 15 개 형태 대신 `getRelatedObjects(context, relPattern, typePattern, objectSelects, relSelects, getTo, getFrom, level, objectWhere, relWhere, 0, includeType, includeRel, includeMap)` 를 씁니다.
- 13 개 형태 대신 `getRelatedObjects(context, relPattern, typePattern, objectSelects, relSelects, getTo, getFrom, level, objectWhere, relWhere, (short) 0, true, false, (short) 1000, null, null, null, relKeyPrefix)` 를 씁니다. `cache` 를 돌려주는 동작은 대체가 없으므로 부르는 쪽에서 처리합니다.

### getObjects

<Badge type="warning" text="폐기" /> 대신 `getRelatedObjects(context, relPattern, typePattern, getTo, getFrom, level, objectSelects, relSelects, objectWhere, relWhere, 0, includeRel, includeType, includeMap)` 를 씁니다. (앞 13 개 인자를 폐기된 13 개 `String` 형태에 넘기기만 하고, 그 형태는 `limit` `0` 으로 이 형태를 부름)

연결된 객체를 `MapList` 로 받음.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getObjects(context, relPattern, typePattern, getTo, getFrom, level, objectSelects, relSelects, objectWhere, relWhere, includeRel, includeType, includeMap, cache, useCache)` | 앞 13 개는 순서 다른 형태의 [getRelatedObjects](#getrelatedobjects) 와 같음. `cache`, `useCache` 는 쓰지 않음 | `MapList` |

### getParentInfo

<Badge type="warning" text="폐기" /> 대신 `getRelatedObjects(context, relPattern, "*", selects, null, true, false, (short) level, null, null, 0)` 를 씁니다. (안에서 폐기된 10 개 형태를 같은 값으로 부르기만 함)

<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

부모 쪽으로 연결된 객체를 `MapList` 로 받음.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getParentInfo(context, level, selects, relPattern)` | `level`: 몇 단계까지 (`int`). `selects`: 객체 select. `relPattern`: 관계 이름 | `MapList`. 부모 쪽 객체 행 |

::: info 근거
xerinfra.jar `DomainObject.getRelatedObjects` 9 개 형태의 호출 관계: 10 개 → 13 개 `Pattern` (`null, null, null`) → 14 개 `Pattern` (`limit` `0`), 11 개 → 14 개 `Pattern` (`null, null, null`), 13 개 `String` → 14 개 `String` (`limit` `0`), 14 개 `String` → 18 개, 14 개 `Pattern` → 18 개 (`(short) limit, true, false, (short) 1000, includeType, includeRel, includeMap, null`), 19 개 → 20 개 (`(short) 0`), 20 개 → 18 개. 14 개 `String` 은 `objectSelects` 에 `id`, `type`, 복사한 `relSelects` 에 `id[connection]` 을 더하고 패턴 문자열을 `new Pattern(...)` 으로 바꾸는데, 12 번째 (`includeRel`) 를 18 개 형태의 16 번째, 13 번째 (`includeType`) 를 15 번째에 넘김. 18 개 형태는 `objectSelects` 를 복사해 `type` 을 더하고 `ContextUtil.isTransactionActive` 가 아니면 `startTransaction(context, false)`, `BusinessObject.getExpansionIterator(..., limit, checkHidden, preventDuplicates, pageSize, false)` → `ExpandParams` 의 `setLimit`, `setCheckHidden`, `setPreventDuplicates`, `setPageSize`, `setIsList(false)`, `setDerivedTypes(true)`. eMatrixServletRMI.jar `ExpandParams.RECURSE_TO_ALL = 0`, 생성자의 `limit` 기본값 `0`. `FrameworkUtil.toMapList(ExpansionIterator, short, Pattern, Pattern, Map, String)` 은 `limit` 가 `0` 이면 결과 목록 초기 크기를 `10000` 으로 잡고, 관계 이름 (`getTypeName()`) 을 `includeRel`, 행의 `type` 값을 `includeType` 으로 `Pattern.match`, 관계 값을 `relKeyPrefix + 키` 로 넣은 뒤 `level`, `relationship` 을 넣고 `includeMap` 값과 행 값을 `toUpperCase()` 해서 비교. `matrix.util.Pattern.match` 는 쉼표로 나눠 하나라도 맞으면 `true`, `*`, `?` 지원, 대소문자 구분 기본값 `true`. 20 개 형태는 `FrameworkUtil.isSuiteRegistered(context, "appVersionEffectivityFramework", ...)` 일 때 `exec program ConfigFiltering_InitFilter`, `execute[ConfigFiltering_ApplyFilter ...]` 식을 where 앞에 `&&` 로 붙이고 끝나면 `ConfigFiltering_ReleaseFilter`. `getRelatedObject` 는 결과 `size()` 가 `0` 보다 크면 `get(0)`, 아니면 `null`. `hasRelatedObjects` 는 `"from[" + rel + "]"` / `"to[" + rel + "]"` 를 `mxBus.print` (`BusinessObject.select`) 로 읽어 `"TRUE".equalsIgnoreCase`. `expandSelect` (13 개) 는 `useCache && cache != null` 이면 `cache` 를 돌려줌. 폐기 표시는 `Deprecated: true` 기준 (`getRelatedObjects` 10 개, 13 개 두 형태, `expandSelect` 3 개, `getObjects`, `getParentInfo`, `getRelationship`, `hasObjects`). 18 개 형태 인자 주석 (`check hidden`, `prevent duplicates`, `page size`, `includeType, includeRelationship, postPatterns, relKeyPrefix`): emxProgramObjectMigrationBase_mxJPO.java. 19 개 형태에 컴파일된 effectivity 식을 넘기는 예: emxECPartBase_mxJPO.java. 20 개 형태 예: emxProductLineBase_mxJPO.java
:::

## 연결

이 객체와 다른 객체 사이에 관계를 만드는 메서드입니다. 방향 표기 `A → B` 는 A 가 from 쪽 (부모), B 가 to 쪽 (자식) 이라는 뜻입니다 ([관계의 방향](/jpo/api/#_3-관계에는-방향이-있습니다)). 두 객체를 잇는 기본 방법인 `DomainRelationship.connect(context, from, rel, to)` 는 [연결](/jpo/api/write#연결) 에 있습니다.

boolean 인자로 방향을 고르는 메서드는 `true` 의 뜻이 서로 다릅니다.

| 형태 | `true` 일 때 | `false` 일 때 | `true` 의 뜻 |
| --- | --- | --- | --- |
| `obj.addRelatedObject(context, relType, isFrom, id)` | `id → obj` | `obj → id` | 넘긴 상대가 from |
| `obj.createAndConnect(context, type, rel, other, isFrom)` | `other → 새 객체` | `새 객체 → other` | 넘긴 상대가 from |
| `obj.connect(context, rel, other, isFrom)` <Badge type="warning" text="폐기" /> | `other → obj` | `obj → other` | 넘긴 상대가 from |
| `obj.addRelatedObjects(context, rel, other, isFrom)` <Badge type="warning" text="폐기" /> | `other → obj` | `obj → other` | 넘긴 상대가 from |
| `obj.addRelatedObjects(context, relType, isFrom, ids)` | `obj → 각 id` | `각 id → obj` | 이 객체가 from |
| `obj.setRelatedObject(context, rel, isFrom, id)` | `obj → id` | `id → obj` | 이 객체가 from |
| `DomainRelationship.connect(context, obj, rel, isFrom, ids)` | `obj → 각 id` | `각 id → obj` | 이 객체가 from |

::: warning id 하나와 id 배열은 방향이 반대입니다
`addRelatedObject(..., true, id)` 는 `id → obj` 인데 `addRelatedObjects(..., true, ids)` 는 `obj → 각 id` 입니다. 이름도 같은 함정이 있어서 `addFromObject(context, relType, id)` 는 `id → obj`, `addFromObjects(context, relType, ids)` 는 `obj → 각 id` 입니다. 헷갈리면 방향이 이름과 인자 순서에 그대로 드러나는 [addToObject](#addtoobject) (`obj → id`), [addFromObject](#addfromobject) (`id → obj`), `DomainRelationship.connect(context, from, rel, to)` 를 씁니다.
:::

### addToObject

이 객체를 from 쪽에 두고 `id` 의 객체로 관계를 만듭니다 (`obj → id`).

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.addToObject(context, relType, id)` | `relType`: 관계 (`new RelationshipType(관계이름)`). `id`: to 쪽에 둘 객체 id | `DomainRelationship`. 만든 관계. 관계 id 는 `getName()` |

- `DomainRelationship.connect(context, obj, relType, new DomainObject(id))` 와 같습니다. 두 객체를 열어 트랜잭션 안에서 연결하고, 실패하면 abort 한 뒤 `FrameworkException` 을 던집니다.
- 바깥에 트랜잭션이 있으면 그 안에 묶입니다 ([중첩 호출](/jpo/transaction-context#중첩-호출은-참조-카운트로-처리)).

```java
DomainRelationship rel = part.addToObject(context, new RelationshipType(relStructure), ids[0]);
rel.setAttributeValue(context, attrQty, "2");
String newRelId = rel.getName();
```

### addFromObject

`id` 의 객체를 from 쪽에 두고 이 객체로 관계를 만듭니다 (`id → obj`).

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.addFromObject(context, relType, id)` | `relType`: 관계 (`new RelationshipType(관계이름)`). `id`: from 쪽에 둘 객체 id | `DomainRelationship`. 만든 관계 |

- `DomainRelationship.connect(context, new DomainObject(id), relType, obj)` 와 같습니다. 트랜잭션과 예외는 [addToObject](#addtoobject) 와 같습니다.

```java
DomainObject childPart = DomainObject.newInstance(context, ids[0]);
DomainRelationship rel = childPart.addFromObject(context, new RelationshipType(relStructure), objectId);
```

```text
part ── My Part Structure ──▶ childPart
```

### addToObjects

이름은 "to 쪽 객체 더하기" 지만 두 형태의 방향이 반대입니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.addToObjects(context, rel, other)` <Badge type="warning" text="폐기" /> | `rel`: 관계 이름. `other`: to 쪽에 둘 객체 (`DomainObject`) | `DomainRelationship`. `obj → other` 로 만든 관계 |
| `obj.addToObjects(context, relType, ids)` <Badge type="tip" text="protected" /> | `relType`: 관계. `ids`: 상대 객체 id 배열 | `Map`. 키는 `ids` 의 id, 값은 관계 id. **`각 id → obj`** |

- 폐기 형태는 `DomainRelationship.connect(context, obj, rel, other)` 만 부릅니다. 대신 그것을 씁니다.
- protected 형태는 하위 클래스에서만 부를 수 있습니다. `DomainRelationship.connect(context, obj, relType, false, ids)` 와 같고, 밖에서는 같은 일을 하는 [addRelatedObjects](#addrelatedobjects) (`isFrom` = `false`) 를 씁니다.

### addFromObjects

이름은 "from 쪽 객체 더하기" 지만 두 형태의 방향이 반대입니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.addFromObjects(context, rel, other)` <Badge type="warning" text="폐기" /> | `rel`: 관계 이름. `other`: from 쪽에 둘 객체 (`DomainObject`) | `DomainRelationship`. `other → obj` 로 만든 관계 |
| `obj.addFromObjects(context, relType, ids)` | `relType`: 관계. `ids`: 상대 객체 id 배열 | `Map`. 키는 `ids` 의 id, 값은 관계 id. **`obj → 각 id`** |

- 폐기 형태는 `DomainRelationship.connect(context, other, rel, obj)` 만 부릅니다. 대신 그것을 씁니다.
- 배열 형태는 이름과 달리 이 객체가 from 쪽입니다. `DomainRelationship.connect(context, obj, relType, true, ids)` 와 같고, 트랜잭션과 결과 Map 은 [addRelatedObjects](#addrelatedobjects) 와 같습니다.

### addRelatedObject

`isFrom` 으로 방향을 골라 `id` 의 객체와 관계를 만듭니다. `true` 면 [addFromObject](#addfromobject) (`id → obj`), `false` 면 [addToObject](#addtoobject) (`obj → id`) 를 그대로 부릅니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.addRelatedObject(context, relType, isFrom, id)` | `relType`: 관계 (`new RelationshipType(관계이름)`). `isFrom`: `true` 면 **상대** 를 from 쪽에 둠. `id`: 상대 객체 id | `DomainRelationship`. 만든 관계 |

```java
DomainRelationship toDoc = part.addRelatedObject(context, new RelationshipType(relPartDocument), false, docIds[0]);
DomainObject childPart = DomainObject.newInstance(context, ids[0]);
DomainRelationship fromParent = childPart.addRelatedObject(context, new RelationshipType(relStructure), true, objectId);
```

```text
part ── My Part Document  ──▶ docIds[0] 문서
part ── My Part Structure ──▶ childPart
```

### addRelatedObjects

`isFrom` 으로 방향을 골라 관계를 만듭니다. id 배열을 받는 형태는 `true` 의 뜻이 [addRelatedObject](#addrelatedobject) 와 반대입니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.addRelatedObjects(context, rel, other, isFrom)` <Badge type="warning" text="폐기" /> | `rel`: 관계 이름. `other`: 상대 객체 (`DomainObject`). `isFrom`: `true` 면 **상대** 를 from 쪽에 둠 | `DomainRelationship`. `true` 면 `other → obj`, `false` 면 `obj → other` |
| `obj.addRelatedObjects(context, relType, isFrom, ids)` | `relType`: 관계. `isFrom`: `true` 면 **이 객체** 를 from 쪽에 둠. `ids`: 상대 객체 id 배열 | `Map`. 키는 `ids` 의 id, 값은 만든 관계 id. `true` 면 `obj → 각 id`, `false` 면 `각 id → obj` |

- 폐기 형태 대신 `true` 면 `DomainRelationship.connect(context, other, rel, obj)`, `false` 면 `DomainRelationship.connect(context, obj, rel, other)` 를 씁니다. 안에서 `addFromObjects` / `addToObjects` 를 거쳐 이것만 부릅니다.
- 배열 형태는 `DomainRelationship.connect(context, obj, relType, isFrom, ids)` 와 같습니다 (`true` 면 [addFromObjects](#addfromobjects), `false` 면 [addToObjects](#addtoobjects) 를 거침).
- 배열 형태는 id 전체를 **한 트랜잭션** 으로 연결합니다. 하나라도 실패하면 abort 하고 `FrameworkException` 을 던지므로, 바깥 트랜잭션이 없으면 앞서 만든 연결도 함께 되돌아갑니다.
- 상대 객체는 열지 않고 id 만 바꿔 가며 연결합니다. 빈 배열이면 빈 Map 이 옵니다.

```java
Map relIdByDocId = part.addRelatedObjects(context, new RelationshipType(relPartDocument), true, docIds);
String firstRelId = (String) relIdByDocId.get(docIds[0]);
```

```text
part ── My Part Document ──▶ docIds 의 각 문서

{22222.33333.44444.11111=99999.22222.33333.11111, 22222.33333.44444.22222=99999.22222.33333.22222}
```

### connect

<Badge type="warning" text="폐기" /> 대신 `DomainRelationship.connect(context, from, rel, to)` 를 씁니다. (안에서 `addRelatedObjects` → `addFromObjects` / `addToObjects` 를 거쳐 이 메서드만 부름)

`isFrom` 으로 방향을 골라 `other` 와 관계를 만듭니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.connect(context, rel, other, isFrom)` | `rel`: 관계 이름. `other`: 상대 객체 (`DomainObject`). `isFrom`: `true` 면 **상대** 를 from 쪽에 둠 | `DomainRelationship`. `true` 면 `other → obj`, `false` 면 `obj → other` |

- `true` 면 `DomainRelationship.connect(context, other, rel, obj)`, `false` 면 `DomainRelationship.connect(context, obj, rel, other)` 와 같습니다.

### connectTo

<Badge type="warning" text="폐기" /> 대신 `DomainRelationship.connect(context, obj, rel, other)` 를 씁니다. (안에서 `addToObjects(context, rel, other)` 를 거쳐 이 메서드만 부름)

이 객체를 from 쪽에 두고 `other` 로 관계를 만듭니다 (`obj → other`).

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.connectTo(context, rel, other)` | `rel`: 관계 이름. `other`: to 쪽에 둘 객체 (`DomainObject`) | `DomainRelationship`. 만든 관계 |

### connectFrom

<Badge type="warning" text="폐기" /> 대신 `DomainRelationship.connect(context, other, rel, obj)` 를 씁니다. (안에서 `addFromObjects(context, rel, other)` 를 거쳐 이 메서드만 부름)

`other` 를 from 쪽에 두고 이 객체로 관계를 만듭니다 (`other → obj`).

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.connectFrom(context, rel, other)` | `rel`: 관계 이름. `other`: from 쪽에 둘 객체 (`DomainObject`) | `DomainRelationship`. 만든 관계 |

### createAndConnect

새 객체를 만들고 바로 `other` 와 연결합니다. [빈 객체](/jpo/api/read#객체-얻기) (`DomainObject.newInstance(context)`) 에서 부르고, 만드는 규칙은 [createObject](#createobject) 와 같습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.createAndConnect(context, type, rel, other, isFrom)` | `type`: 만들 타입. `rel`: 관계 이름. `other`: 연결할 상대 (`DomainObject`). `isFrom`: `true` 면 `other → 새 객체`, `false` 면 `새 객체 → other` | `DomainRelationship`. 만든 관계 |
| `obj.createAndConnect(context, type, name, rel, other, isFrom)` | `name`: 만들 이름. 나머지는 위와 같음 | 위와 같음 |
| `obj.createAndConnect(context, type, name, revision, policy, vault, rel, other, isFrom)` | `revision`, `policy`, `vault`: 리비전, 정책, vault. 나머지는 위와 같음 | 위와 같음 |

짧은 두 형태는 빠진 값을 `null` 로 채워 9 인자 형태를 부릅니다. `null` 로 둔 값은 아래 기본값을 씁니다.

| 인자 | `null` 일 때 |
| --- | --- |
| `name` | 고유 이름 (`""` 이나 공백만 있어도 같음) |
| `revision` | 정책의 첫 리비전 |
| `policy` | 타입의 기본 정책 |
| `vault` | **`other` 의 vault**. `other` 도 `null` 이면 context 의 vault |

- `revision`, `policy`, `vault` 는 `null` 일 때만 기본값을 씁니다. `""` 는 그대로 넘어갑니다.
- 만들기와 연결을 한 트랜잭션으로 묶습니다. 연결이 실패하면 abort 하고 `FrameworkException` 을 던지므로, 바깥 트랜잭션이 없으면 만든 객체도 되돌아갑니다.
- `rel` 이나 `other` 가 `null` 이면 객체만 만들고 `null` 을 돌려줍니다.
- 끝나면 `obj` 가 새 객체를 가리킵니다. 새 id 는 `obj.getId(context)`, 관계 id 는 돌려받은 관계의 `getName()` 입니다.

```java
DomainObject newDoc = DomainObject.newInstance(context);
DomainRelationship rel = newDoc.createAndConnect(context, typeDocument, relPartDocument, part, true);
String newDocId = newDoc.getId(context);
String newRelId = rel.getName();

DomainObject childPart = DomainObject.newInstance(context);
childPart.createAndConnect(context, typePart, "P-0100", null, null, null, relStructure, part, true);
```

```text
part ── My Part Document  ──▶ newDoc
part ── My Part Structure ──▶ childPart (이름 P-0100, 리비전과 정책은 기본값, vault 는 part 의 vault)
```

### setRelatedObject

이 객체에 그 관계로 바로 붙은 상대를 `id` 의 객체 하나로 맞춥니다. 상대가 하나만 있어야 하는 관계에 씁니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.setRelatedObject(context, rel, isFrom, id)` | `rel`: 관계 이름. `isFrom`: `true` 면 **이 객체** 가 from 쪽 (`obj → 상대`), `false` 면 to 쪽 (`상대 → obj`). `id`: 새 상대의 객체 id. `null` 이면 끊기 | `DomainRelationship`. 남은 관계나 만든 관계. 끊었거나 한 일이 없으면 `null` |

지금 붙은 상대는 [getRelatedObject](#getrelatedobject) 로 찾고, 그 결과에 따라 아래처럼 동작합니다.

| 지금 상대 | `id` | 하는 일 | 돌려받는 것 |
| --- | --- | --- | --- |
| 있음 | `null` | 그 관계를 끊음 | `null` |
| 있음 | 지금 상대와 같음 | 아무것도 안 함 | 그 관계 |
| 있음 | 다른 id | 관계는 두고 끝 객체만 `id` 로 바꿈 (MQL `modify connection`). 관계 id 는 그대로 | 그 관계 |
| 없음 | 있음 | `isFrom` 방향으로 새로 연결함 | 만든 관계 |
| 없음 | `null` | 아무것도 안 함 | `null` |

- 기존 관계를 끊고 다시 연결하지 않습니다. 끝 객체 바꾸기는 `DomainRelationship.setToObject` / `setFromObject` 와 같은 MQL 입니다 ([관계 수정](/jpo/api/write#관계-수정)).
- 상대가 여러 개 붙어 있으면 찾은 결과의 첫 하나만 바꾸거나 끊고, 나머지는 그대로 둡니다.
- 안에서 트랜잭션을 열지 않습니다. 찾기와 바꾸기를 함께 되돌리려면 바깥에서 묶습니다.
- 실패하면 `FrameworkException` 을 던집니다.

```java
DomainRelationship rel = part.setRelatedObject(context, relPartDocument, true, docIds[0]);
part.setRelatedObject(context, relPartDocument, true, null);
```

첫 줄은 `part → 문서` 연결이 없으면 `part → docIds[0]` 을 만들고, 있으면 그 관계의 to 쪽을 `docIds[0]` 로 바꿉니다. 둘째 줄은 그 연결을 끊습니다.

### setRelationship

<Badge type="warning" text="폐기" /> 대신 `setRelatedObject(context, rel, isFrom, id)` 를 씁니다. (같은 인자를 그대로 넘겨 그 메서드만 부름)
<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

상대를 `id` 의 객체 하나로 맞춥니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.setRelationship(context, rel, isFrom, id)` | [setRelatedObject](#setrelatedobject) 와 같음 | `DomainRelationship`. [setRelatedObject](#setrelatedobject) 와 같음 |

::: info 근거
xerinfra.jar `DomainObject` 바이트코드:
- `addToObject(context, relType, id)` → `DomainRelationship.connect(context, this, relType, new DomainObject(id))`, `addFromObject` → `connect(context, new DomainObject(id), relType, this)`. 폐기 `addToObjects(context, rel, other)` → `connect(context, this, rel, other)`, 폐기 `addFromObjects(context, rel, other)` → `connect(context, other, rel, this)`.
- `addFromObjects(context, relType, ids)` → `DomainRelationship.connect(context, this, relType, true, ids)`, protected `addToObjects(context, relType, ids)` → 같은 메서드에 `false`.
- `addRelatedObject` / `addRelatedObjects` 는 `isFrom` 이면 `addFrom...`, 아니면 `addTo...` 를 부름. `connect(context, rel, other, isFrom)` → `addRelatedObjects(context, rel, other, isFrom)`, `connectTo` → `addToObjects`, `connectFrom` → `addFromObjects`, `setRelationship` → `setRelatedObject`.
- `DomainRelationship.connect(context, from, relType, to)`: 두 객체 `openObject`, `ContextUtil.startTransaction(context, true)`, `from.connect(context, relType, true, to)`, `commitTransaction`, `closeObject`. `connect(context, obj, relType, isFrom, ids)`: `startTransaction` 한 번 안에서 id 마다 `obj.connect(context, relType, isFrom, 상대)` 후 `HashMap` 에 `id → Relationship.getName()`. 예외면 `abortTransaction` 후 `FrameworkException`. `DomainRelationship.connect` 형태에는 폐기 표시가 없음.
- eMatrixServletRMI.jar `BusinessObject.connect(context, relType, isFrom, other)` 는 `isFrom` 을 `ConnectParameters.setFrom` 으로 넘기고, 돌려받은 `Relationship` 생성자가 `isFrom` 이 `true` 면 호출한 객체를 `_from`, 상대를 `_to` 로 둠. 이름 (`getName()`) 에는 서버가 돌려준 관계 id 가 들어감.
- `createAndConnect` 5 / 6 인자 → 9 인자 `(context, type, name, revision, policy, vault, rel, other, isFrom)`: `startTransaction`, vault 가 `null` 이면 `getDefaultVault(context, other)` (`other.getVault()`, 비면 `other` 를 열어 읽음. `other` 가 `null` 이면 `context.getVault()`), `createObject(context, type, name, revision, policy, vault)`, `other` 와 `rel` 이 둘 다 있으면 `connect(context, rel, other, isFrom)`, `commitTransaction`. `createObject` 는 이름이 `null` 이거나 trim 후 `""` 면 `getDefaultName`, policy / revision / vault 는 `null` 일 때만 `getDefaultPolicy` / `getDefaultRevision` / `getDefaultVault`.
- `setRelatedObject`: `getRelatedObject(context, rel, isFrom, [id], [id[connection]])` 로 찾음. 있으면 새 id 가 `null` 일 때 `DomainRelationship.disconnect(context, relId)`, 다를 때 `DomainRelationship.setFromToObject(context, relId, !isFrom, new DomainObject(id))` (`modify connection $1 to $2` / `modify connection $1 from $2`). 없으면 `connect(context, new RelationshipType(rel), isFrom, new DomainObject(id))` (`BusinessObject`). 트랜잭션 호출 없음.
- OOTB 호출 예: emxCPDUtilBase_mxJPO.java (`createAndConnect` 5 인자, `false`), emxBuildBase_mxJPO.java (6 인자), emxCommonDownloadBase_mxJPO.java (9 인자), emxPartMasterMigrationBase_mxJPO.java (`addRelatedObjects(context, relType, true, toObjectIds)`), emxBOMPartManagementBase_mxJPO.java (`addRelatedObject(..., true, id)`), emxLocationBase_mxJPO.java (`setRelatedObject(..., true, null)` 뒤 새 id)
:::

## 생성, 복제

빈 객체 변수로 새 객체를 만들거나 지금 객체의 복사본을 만드는 메서드와, 그때 쓰는 이름, 정책, 리비전, vault 기본값을 정하는 메서드입니다. 쓰는 법 요약은 [생성](/jpo/api/write#생성) 에 있습니다.

### createObject

이 객체 변수로 새 객체를 만듭니다. 끝나면 이 변수가 새 객체를 가리킵니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.createObject(context, type, name, revision, policy, vault)` | `type`: 타입의 실제 이름. `name`: 이름. `null`, `""`, 공백뿐이면 [getDefaultName](#getdefaultname) 으로 만듦 (`auto_...`). `revision`: 리비전. `null` 이면 정책의 첫 리비전 ([getDefaultRevision](#getdefaultrevision)). `policy`: 정책의 실제 이름. `null` 이면 [getDefaultPolicy](#getdefaultpolicy). `vault`: `null` 이면 로그인 context 의 vault | `void`. 새 id 는 `obj.getId(context)` |
| `obj.createObject(context, type, name, revision, policy, vault, attrs)` | 앞 형태와 같음. `attrs` (`Map`) 는 **쓰지 않고 버림** | `void` |

- 기본값은 `null` 일 때만 씁니다. `revision`, `policy`, `vault` 에 `""` 를 넘기면 `""` 그대로 서버에 넘어갑니다. `name` 만 `""` 와 공백도 빈 값으로 봅니다 (앞뒤 공백은 잘라 냄).
- 기본값은 이름, 정책, 리비전, vault 순서로 정합니다. 리비전은 넘긴 정책 (없으면 기본 정책) 의 첫 리비전입니다.
- 안에서 트랜잭션을 열고 `BusinessObject.create` 뒤 확정합니다. 실패하면 되돌리고 `FrameworkException` 을 던집니다. 바깥에 트랜잭션이 있으면 그 안에 묶입니다 ([여러 변경을 한 묶음으로](/jpo/api/write#여러-변경을-한-묶음으로)).
- `create` 가 새 객체의 id, 타입, 이름, 리비전, vault 를 이 변수에 넣습니다. id 가 있는 변수에서 부르면 그 변수도 새 객체로 바뀌므로 [빈 객체](/jpo/api/read#객체-얻기) (`DomainObject.newInstance(context)`) 에서 부릅니다.
- `Map` 을 받는 형태는 앞 형태를 그대로 부릅니다. 속성은 만든 뒤 [setAttributeValues](#setattributevalues) 로 넣습니다. 이 형태는 예외를 메시지 문자열만 담은 `FrameworkException` 으로 바꿔 던집니다.

```java
Map attrs = new HashMap();
attrs.put(attrStatus, "Draft");
DomainObject newDoc = DomainObject.newInstance(context);
ContextUtil.startTransaction(context, true);
try {
    newDoc.createObject(context, typeDocument, null, null, null, null);
    newDoc.setAttributeValues(context, attrs);
    ContextUtil.commitTransaction(context);
} catch (Exception e) {
    ContextUtil.abortTransaction(context);
    throw e;
}
String newId = newDoc.getId(context);
```

### getDefaultName

<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

`createObject`, `cloneObject` 가 이름이 비었을 때 부르는 이름 생성기입니다. [getUniqueName](#getuniquename)`(context)` 를 그대로 부릅니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getDefaultName(context)` | 없음 | `String`. `getUniqueName(context)` 의 결과 |

### getDefaultPolicy

타입에 걸린 정책 중 첫 번째 정책의 이름을 읽습니다. `createObject` 가 `policy` 가 `null` 일 때 씁니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getDefaultPolicy(context, type)` | `type`: 타입의 실제 이름 | `String`. 정책의 실제 이름. 타입에 정책이 없으면 `null` |

- `mxType.getDefaultPolicy(context, type, false)` 가 돌려주는 Map 의 `name` 입니다. 그 안에서는 `BusinessType.getPolicies` 목록의 첫 번째를 고릅니다 (사용자별 정책 목록이 아님).
- 이 객체 변수의 값은 쓰지 않습니다. 빈 객체에서 불러도 됩니다.

```java
DomainObject newDoc = DomainObject.newInstance(context);
String policy = newDoc.getDefaultPolicy(context, typeDocument);
String firstRevision = newDoc.getDefaultRevision(context, policy);
```

### getDefaultRevision

정책의 리비전 순서에서 첫 값을 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getDefaultRevision(context, policy)` | `policy`: 정책의 실제 이름. `null` 이면 이 객체의 지금 정책 | `String`. 첫 리비전 (순서가 `A,B,C,...` 인 정책이면 `A`) |

- 정책을 `Policy.open` 으로 열어 `getFirstInSequence` 를 읽고 닫습니다.
- `policy` 가 `null` 이면 이 객체를 열어 정책을 읽으므로 id 가 있는 객체에서만 그렇게 부릅니다. [cloneObject](#cloneobject) 가 `newRev` 가 `null` 일 때 이 방법으로 원본 정책의 첫 리비전을 씁니다.

### getDefaultVault

새 객체를 둘 vault 이름을 정합니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getDefaultVault(context, bo)` | `bo`: vault 를 따를 객체 (`BusinessObject`, `DomainObject`). `null` 이면 로그인 context 의 vault | `String`. vault 이름 |

- `bo` 가 있으면 `bo.getVault()` 를 씁니다. 아직 읽지 않아 `null` 이면 `bo` 를 열어 읽고 닫습니다.
- 이 객체 변수의 값은 쓰지 않습니다.
- `createAndConnect` 는 `vault` 가 `null` 이면 연결할 상대 객체를 이 메서드에 넘겨, 상대와 같은 vault 에 만듭니다.

### getUniqueName

접두어 + 카운터 + 지금 시각 (밀리초) 을 이어 붙인 문자열을 만듭니다. OOTB 는 이름이나 리비전을 겹치지 않게 붙일 때 씁니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getUniqueName(prefix)` | `prefix`: 앞에 붙일 문자열. `""` 이면 숫자만 | `String`. `prefix` + 카운터 + 시각 전체 (13 자리) |
| `obj.getUniqueName(prefix, length)` | `length`: 시각을 뒤에서 몇 자리만 쓸지. `0` 이면 전부 | `String`. `prefix` + 카운터 + 시각 뒤 `length` 자리 |
| `obj.getUniqueName(context)` | 없음. 접두어는 설정 `emxFramework.<클래스 이름>.autoName` 값 | `String`. 설정을 못 읽으면 접두어 `auto_` |
| `obj.getUniqueName()` <Badge type="warning" text="폐기" /> | 없음 | `String`. `getUniqueName(context)` 와 같은 규칙 |

- 카운터는 모든 객체가 함께 쓰는 `static` 값입니다. 부를 때마다 1 씩 늘고 127 다음에 0 으로 돌아가므로 1 ~ 3 자리입니다. 시각은 `new Date().getTime()` 입니다.
- DB 에서 겹치는지 확인하지 않습니다. 다른 서버 (JVM) 에서 같은 때 부르면 같은 값이 나올 수 있습니다.
- `synchronized` 는 `(prefix, length)` 형태에만 있고 나머지 형태도 이 형태를 부릅니다. 객체 변수마다 따로 잠기므로 다른 변수에서 동시에 부르면 카운터가 겹칠 수 있습니다.
- `getUniqueName(context)` 의 `<클래스 이름>` 은 이 변수의 실제 클래스 이름 (패키지 제외) 입니다. `DomainObject` 면 `emxFramework.DomainObject.autoName` 입니다. 값이 없거나 `"null"` 이거나 읽다가 예외가 나면 `auto_` 를 씁니다. enovia2025 `properties` 폴더에는 이 키가 없습니다.
- 폐기된 `getUniqueName()` 은 `getUniqueName((Context) null)` 을 부릅니다. 대신 `getUniqueName(context)` 를 씁니다.

```java
String revision = part.getUniqueName("");
String versionName = part.getUniqueName("VD_");
String folderName = part.getUniqueName("Folder_", 12);
String autoName = part.getUniqueName(context);
```

값은 예시입니다 (카운터 3 ~ 6, 시각 `1764052800123` 무렵).

```text
revision    = 31764052800123
versionName = VD_41764052800124
folderName  = Folder_5764052800125
autoName    = auto_61764052800126
```

### getShortUniqueName

[getUniqueName](#getuniquename) 과 같고 시각은 뒤 10 자리만 씁니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getShortUniqueName(prefix)` | `prefix`: 앞에 붙일 문자열 | `String`. `getUniqueName(prefix, 10)` 과 같음 |
| `obj.getShortUniqueName(context)` | 없음. 접두어는 `getUniqueName(context)` 와 같은 설정 | `String`. 설정을 못 읽으면 `auto_` + 카운터 + 시각 뒤 10 자리 |
| `obj.getShortUniqueName()` <Badge type="warning" text="폐기" /> | 없음 | `String` |

- 폐기된 `getShortUniqueName()` 은 `getShortUniqueName((Context) null)` 을 부릅니다. 대신 `getShortUniqueName(context)` 를 씁니다.

### getAutoGeneratedName

채번기 (eService Object Generator) 로 이름을 받습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.getAutoGeneratedName(context, generator, series)` | `generator`: 채번기 이름. 보통 심볼릭 타입 이름 (`"type_Company"`). `series`: 채번기의 리비전 (시리즈). 보통 `""`. `null` 이면 `""` | `String`. 만든 이름 |

- MQL `execute program $1 $2 $3 $4 $5 $6 $7 $8 $9` 에 `eServicecommonNumberGenerator.tcl`, `generator`, `series`, 공백 (`" "`) 5 개, `Yes` 를 넘겨 실행합니다.
- 결과를 `|` 로 나눠 첫 값이 `1` 이면 `emxFramework.Error.CreatingObjectName` 문구 (뒤에 `generator`) 로, 결과가 비면 `emxFramework.Error.NoResultsFound` 문구로 예외를 던집니다 (`FrameworkException`). 아니면 두 번째 값을 돌려줍니다.
- 실제 타입 이름만 있으면 `FrameworkUtil.getAliasForAdmin(context, "type", 실제이름, true)` 로 심볼릭 이름을 얻어 넘깁니다. 채번기 설정은 [채번기](/trigger/generator) 에 있습니다.
- `FrameworkUtil.autoName` 도 같은 프로그램을 부르고, 공백 자리에 정책과 vault 를 넘깁니다. 이 메서드로 부를 때 프로그램이 객체를 만들지 않는지는 프로그램 원본이 없어 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />. OOTB 는 이름만 받는 용도로 씁니다.

```java
String name = DomainObject.getAutoGeneratedName(context, "type_MyDocument", "");
DomainObject newDoc = DomainObject.newInstance(context);
newDoc.createObject(context, typeDocument, name, null, null, null);
```

### cloneObject

이 객체의 복사본을 만듭니다. 이 변수는 원본을 그대로 가리키고, 복사본은 돌려받는 값으로 받습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.cloneObject(context, newName)` | `newName`: 복사본 이름. `null`, `""` 이면 [getDefaultName](#getdefaultname) (`auto_...`). 리비전, vault 는 기본값. 파일도 복사 | `BusinessObject` (복사본) |
| `obj.cloneObject(context, newName, newRev, vault)` | `newRev`: 복사본 리비전. `null` 이면 원본 정책의 첫 리비전. `vault`: `null` 이면 로그인 context 의 vault. 파일도 복사 | `BusinessObject` |
| `obj.cloneObject(context, newName, newRev, vault, copyFiles)` | `copyFiles`: `true` 면 파일도 복사, `false` 면 복사 안 함 | `BusinessObject` |
| `obj.cloneObject(context, newName, newRev, vault, copyFiles, autoName)` | `autoName`: `true` 면 타입 설정에 따라 이름을 채번기 이름으로 바꿈 (아래). 앞 세 형태는 모두 `true` | `BusinessObject` |

- `autoName` 이 `true` 이고 타입의 admin property `AutoGeneratedObjectName` 이 `true` 면, **넘긴 `newName` 이 있어도** [getAutoGeneratedName](#getautogeneratedname) 이 만든 이름으로 바꿉니다. 이름을 꼭 지켜야 하면 6 개 인자 형태에 `false` 를 넘깁니다.
- 기본값은 `null` 일 때만 씁니다. `newName` 만 `""` 도 빈 값으로 봅니다.
- 원본을 (열려 있지 않으면) 열고, 트랜잭션 안에서 `BusinessObject.clone(context, null, name, rev, vault, copyFiles)` 를 부릅니다. 이력 (history) 은 복사하지 않습니다. 실패하면 되돌리고 `FrameworkException` 을 던집니다.

```java
BusinessObject copy = part.cloneObject(context, null);
DomainObject copyPart = new DomainObject(copy);

BusinessObject named = part.cloneObject(context, "P-0001-COPY", null, null, false, false);
String namedId = named.getObjectId(context);
```

### cloneWithFileMove

새 객체를 만들고 이 객체의 파일을 그 객체로 옮깁니다. 이 객체의 속성은 복사하지 않습니다. OOTB JPO 에서는 부르지 않습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.cloneWithFileMove(context, type, name, revision, policy, vault, store, attrs)` | `type`: 새 객체 타입. `null`, `""` 이면 `DomainConstants.TYPE_VERSION_DOCUMENT`. `policy`: `null`, `""` 이면 `DomainConstants.POLICY_VERSION_DOCUMENT`. `name`, `revision`, `vault`: [createObject](#createobject) 와 같음. `store`: 파일을 넣을 store (아래 두 번째 경우만 씀). `attrs`: 새 객체에 넣을 속성 Map. `null` 이나 빈 Map 이면 건너뜀 | `DomainObject` (새 객체) |

- 한 트랜잭션 안에서 `createObject`, `setAttributeValues` 뒤 파일을 옮깁니다. 클래스 `com.matrixone.fcs.http.HttpPreCheckin` 을 찾으면 MQL `modify businessobject $1 move from $2 all` (새 객체 id, 이 객체 id) 로 모두 옮기고, 못 찾으면 이 객체의 첫 파일 하나만 서버 임시 폴더로 checkout 해 새 객체에 checkin 합니다 (이 객체의 파일은 남음).

::: info 근거
xerinfra.jar `DomainObject` 바이트코드 (javap). `createObject(6 개)`: `name` 을 trim 하고 `null` / `""` 면 `getDefaultName`, `policy == null` 이면 `getDefaultPolicy(context, type)`, `revision == null` 이면 `getDefaultRevision(context, policy)`, `vault == null` 이면 `getDefaultVault(context, null)`, 그 뒤 `startTransaction(context, true)` → `BusinessObject.create(context, type, name, revision, policy, vault)` → `commitTransaction`, 예외면 `abortTransaction`. `createObject(7 개)` 는 7 번째 `Map` 을 읽지 않고 6 개 형태를 부르며 예외를 `new FrameworkException(e.toString())` 으로 바꿈. eMatrixServletRMI.jar `BusinessObject.create(Context, CreateParameters)` 가 결과의 `objectid`, `name`, `revision`, `vault` 를 이 변수의 필드에 넣음. `getDefaultName` → `getUniqueName(context)`. `getDefaultPolicy` → `mxType.getDefaultPolicy(context, type, false)` 의 `"name"` (`mxType.getPolicies` 의 `BusinessType.getPolicies` 첫 항목). `getDefaultRevision` → `new Policy(policy).open` → `getFirstInSequence`. `getDefaultVault` → `bo.getVault()` 또는 `context.getVault().getName()`. `getUniqueName(String, int)`: `static volatile byte counter` 를 1 늘리고 음수면 0, `length == 0` 이면 `prefix + counter + new Date().getTime()`, 아니면 시각 문자열의 뒤 `length` 자리. `getUniqueName(String)` → `(prefix, 0)`, `getShortUniqueName(String)` → `(prefix, 10)`, `getUniqueName(Context)` / `getShortUniqueName(Context)` 는 `EnoviaResourceBundle.getProperty(context, "emxFramework." + 클래스이름 + ".autoName")`, 기본 `auto_`. 인자 없는 두 형태는 `Deprecated` 이고 `(Context) null` 을 넘김. `getAutoGeneratedName`: `MqlUtil.mqlCommand(context, "execute program $1 ... $9", [eServicecommonNumberGenerator.tcl, generator, series, " " x5, Yes])`, `|` 로 자른 첫 값이 `1` 이면 `CreatingObjectName`, 두 번째 값 반환. `cloneObject(context, name)` → `(name, null, null, true)` → `(…, true, true)`, `(name, rev, vault)` → `(…, true)` → `(…, true, true)`, `(…, copyFiles)` → `(…, copyFiles, true)`. 6 개 형태: 마지막 인자가 `true` 면 `PropertyUtil.getAdminProperty(context, "type", 타입, "AutoGeneratedObjectName")` 이 `true` 일 때 `UICache.getObjectGenerator(context, 심볼릭타입, "")` 로 채번기를 찾아 `getAutoGeneratedName(context, 채번기, "")` 로 이름을 덮어씀. 복사는 `BusinessObject.clone(context, null, name, rev, vault, copyFiles)` 이고 안에서 `CloneParameters.setCopyFiles(copyFiles)`, `setCopyHistory(false)`. `cloneWithFileMove` → `newInstance(context).createObject(…)`, `setAttributeValues`, FCS 클래스가 있으면 MQL `move from`, 없으면 `backupFile` (`checkout bus $1 server format $2 file $3 directory $4`, `checkin bus $1 server $2 $3 $4 $5` 에 `store "…"`). OOTB 호출 예: emxCPNProductDataBase_mxJPO.java (`getAutoGeneratedName(context, typeAlias, "A")` 뒤 `cloneObject(context, name, revision, vault, copyFiles)`), emxBuildBase_mxJPO.java (`getUniqueName("DF_")`), emxProjectFolderBase_mxJPO.java (`getUniqueName(이름 + "_", 12)`), emxCommonDocumentUIBase_mxJPO.java (`getShortUniqueName`), emxLibraryCentralCreateInterfaceBase_mxJPO.java (`new DomainObject().getDefaultRevision(context, 정책)`), emxPersonBase_mxJPO.java (`getDefaultVault(context, person)`)
:::

## 값 수정

이미 있는 객체의 속성, 설명, 이름, 소유자, 정책, vault 를 바꾸는 메서드입니다. 쓰는 법 요약과 예제는 [값 수정](/jpo/api/write#값-수정) 에 있습니다.

이 절의 메서드는 모두 객체가 열려 있지 않으면 열고, 자기가 연 경우에만 끝나고 닫습니다. 트랜잭션은 열지 않습니다. 여러 변경을 함께 되돌리려면 [여러 변경을 한 묶음으로](/jpo/api/write#여러-변경을-한-묶음으로) 처럼 바깥에서 묶습니다. 실패하면 `FrameworkException` 을 던집니다.

### setAttributeValue

속성 하나를 바꿉니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.setAttributeValue(context, attr, value)` | `attr`: 속성의 실제 이름. `value`: 새 값 (문자열) | `void` |

- `Attribute` 하나를 담은 `AttributeList` 로 `BusinessObject.setAttributes` 를 한 번 부릅니다.
- 속성 둘 이상은 [setAttributeValues](#setattributevalues) 로 한 번에 넘깁니다. 날짜 값 형식은 [날짜](/jpo/api/util/date) 에 있습니다.

### setAttributeValues

속성 여러 개를 한 번에 바꿉니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.setAttributeValues(context, attrs)` | `attrs`: 속성의 실제 이름 → 값 `Map`. 값은 `String`, 값이 여러 개인 속성은 `StringList` | `void` |

- `FrameworkUtil.toAttributeList(attrs)` 로 바꾼 뒤 `BusinessObject.setAttributes` 를 한 번 부릅니다. MQL `modify bus` 가 아니라 서버 API (`setAttributeValues`) 한 번으로 모든 속성을 넘깁니다.
- 값이 `String` 도 `StringList` 도 아닌 항목 (`null` 포함) 은 오류 없이 빠집니다.
- 빈 Map 이어도 건너뛰지 않고 빈 목록으로 서버를 부릅니다. `attrs` 가 `null` 이면 `FrameworkException` 입니다.

### resetAttributes

이 객체의 모든 속성을 속성 정의의 기본값으로 되돌립니다. OOTB JPO 에서는 부르지 않습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.resetAttributes(context)` | 없음 | `void` |

- 객체의 속성마다 속성 정의의 기본값을 읽고, 기본값이 `""` 이고 범위 (choices) 가 있으면 범위의 첫 값을 씁니다. 모아서 `setAttributes` 한 번으로 넣습니다.

### setDescription

설명을 바꿉니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.setDescription(context, text)` | `text`: 새 설명 | `void` |

- `BusinessObject.setDescription` 으로 기본 정보에 넣고 `update(context)` 로 저장합니다.

### setName

이름을 바꿉니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.setName(context, name)` | `name`: 새 이름 | `void` |

- `name` 이 `null` 이면 `emxFramework.Beans.DomainObject.ObjectNameCantBeNull` 문구로 `FrameworkException` 을 던집니다. `""` 는 확인하지 않고 그대로 넘깁니다.
- 지금 이름과 같으면 아무것도 하지 않습니다.
- 다르면 `BusinessObject.change(context, type, 새이름, revision, vault, policy)` 로 이름만 바꾸고 (나머지는 지금 값), 이 변수의 이름 값도 바꿉니다.

### setOwner

소유자를 바꿉니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.setOwner(context, user)` | `user`: 새 소유자의 사용자 이름 | `void` |

- `BusinessObject.setOwner` 로 기본 정보에 넣고 `update(context)` 로 저장합니다.
- 폐기된 [changeOwner](#changeowner) 는 이 메서드를 그대로 부릅니다.

### setPolicy

정책을 바꿉니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.setPolicy(context, policy)` | `policy`: 새 정책의 실제 이름 | `void` |

- `BusinessObject.change(context, type, name, revision, vault, 새정책)` 으로 정책만 바꿉니다.
- 폐기된 [changePolicy](#changepolicy) 는 이 메서드를 그대로 부릅니다.

### setVault

vault 를 바꿉니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.setVault(context, vault)` | `vault`: 새 vault 이름 | `void` |

- `BusinessObject.change(context, type, name, revision, 새vault, policy)` 로 vault 만 바꿉니다.
- 폐기된 [changeVault](#changevault) 는 이 메서드를 그대로 부릅니다.

### changeOwner

<Badge type="warning" text="폐기" /> 대신 `setOwner(context, user)` 를 씁니다. (안에서 `setOwner` 만 부름)

소유자를 바꿉니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.changeOwner(context, user)` | `user`: 새 소유자의 사용자 이름 | `void` |

### changePolicy

<Badge type="warning" text="폐기" /> 대신 `setPolicy(context, policy)` 를 씁니다. (안에서 `setPolicy` 만 부름)

정책을 바꿉니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.changePolicy(context, policy)` | `policy`: 새 정책의 실제 이름 | `void` |

### changeVault

<Badge type="warning" text="폐기" /> 대신 `setVault(context, vault)` 를 씁니다. (안에서 `setVault` 만 부름)

vault 를 바꿉니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.changeVault(context, vault)` | `vault`: 새 vault 이름 | `void` |

### editBasicInfo

<Badge type="warning" text="폐기" /> 대신 `setDescription(context, text)` 와 `setAttributeValues(context, attrs)` 를 씁니다. (안에서 `description` 키는 `setDescription` 을 부르고, 나머지 키는 `setAttributeValues` 와 같은 `setAttributes` 한 번으로 넣음)

설명과 속성을 한 번에 바꿉니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.editBasicInfo(context, info)` | `info`: 키 → 값 `Map`. 키가 `description` (대소문자 무시) 이면 설명, 그 밖의 키는 모두 속성의 실제 이름으로 봄 (`name`, `owner` 도 속성 이름). 값은 `String` | `void` |

::: info 근거
xerinfra.jar `DomainObject` 바이트코드 (javap). 모두 `openObject` / `closeObject` 로 감싸고 `startTransaction` 은 부르지 않음. `setAttributeValue` → `new AttributeList(1)` 에 `Attribute(new AttributeType(attr), value)` 를 담아 `setAttributes`. `setAttributeValues(Context, Map)` → `FrameworkUtil.toAttributeList(map)` (값이 `String` 이면 `Attribute(type, String)`, `StringList` 면 `Attribute(type, StringList)`, 그 밖은 건너뜀) → `LatticeObject.setAttributeValues(Context, AttributeList)` → `BusinessObject.setAttributes` (eMatrixServletRMI.jar, `bosBusinessObject.setAttributeValues` 한 번, 크기 검사 없음). `resetAttributes` → `getAttributes(context).getAttributes()` 를 돌며 `AttributeType.open` / `getDefaultValue`, `""` 이고 `getChoices` 가 있으면 첫 값, `setAttributes`. `setDescription` / `setOwner` → `BusinessObject.setDescription` / `setOwner` (기본 정보 필드에 넣기) → `update(context)`. `setName` → `null` 이면 `ObjectNameCantBeNull`, `getName()` 과 같으면 건너뜀, 아니면 `change(context, getTypeName(), 새이름, getRevision(), getVault(), getPolicy(context).getName())` 뒤 `setName(String)`. `setPolicy` → `change(context, type, name, revision, vault, 새정책)`, `setVault` → `change(context, type, name, revision, 새vault, policy)`. `changeOwner` / `changePolicy` / `changeVault` (`Deprecated`) → `setOwner` / `setPolicy` / `setVault`. `editBasicInfo` (`Deprecated`) → 키가 `description` (`equalsIgnoreCase`) 면 `setDescription`, 아니면 `AttributeList` 에 담아 `setAttributeValues(context, AttributeList)`. OOTB 호출 예: emxCommonPartBase_mxJPO.java (`setPolicy`), emxECSearchMassUpdateBase_mxJPO.java (`setVault`), emxChangeBase_mxJPO.java (`changeOwner`)
:::

## 리비전

같은 객체의 리비전 (A, B, C ...) 을 찾거나 새 리비전을 만드는 메서드입니다. 리비전 목록은 상속받은 `BusinessObject.getRevisions(context)` 에서 오고, 아래 메서드는 그 목록의 첫 요소를 첫 리비전, 마지막 요소를 마지막 리비전으로 씁니다.

### isLastRevision

이 객체가 마지막 리비전인지 확인합니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.isLastRevision(context)` | 없음 | `boolean`. 마지막 리비전의 id 가 이 객체의 id 와 같으면 `true` |

- 안에서 `pushContext` 로 사용자를 바꿔 [getLastRevision](#getlastrevision) 을 부르고 되돌립니다. 그래서 로그인 사용자의 권한과 관계없이 마지막 리비전과 비교합니다 ([pushContext](/jpo/transaction-context#슈퍼유저로-바꾸기-pushcontext)).
- 이 객체의 id 는 서버에 묻지 않고 변수에 든 값 (`getObjectId()`) 을 씁니다. id 로 만든 객체 (`newInstance(context, id)`, `new DomainObject(id)`) 에서 부릅니다.

### getLastRevision

마지막 리비전을 받습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getLastRevision(context)` | 없음 | `BusinessObject`. 마지막 리비전. id 는 `getObjectId(context)`. 이 객체가 마지막이면 이 객체와 같은 객체 |

- 객체를 열고 (`openObject`) `getRevisions(context)` 목록의 마지막 요소를 꺼낸 뒤 닫습니다. 목록이 비면 `null` 입니다.
- id 만 필요하면 select `last.id` 로 다른 값과 함께 읽을 수 있습니다 ([타입, 존재, 리비전](/jpo/api/read#타입-존재-리비전)).

```java
String lastId = objectId;
if (!part.isLastRevision(context)) {
    BusinessObject last = part.getLastRevision(context);
    lastId = last.getObjectId(context);
}
```

### getPreviousRevision

바로 앞 리비전을 받습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getPreviousRevision(context)` | 없음 | `BusinessObject`. 앞 리비전. `null` 은 오지 않음 |

- 객체를 열고 상속받은 `BusinessObject.getPreviousRevision(context)` 를 부른 뒤 닫습니다. 서버가 준 값으로 늘 새 `BusinessObject` 를 만들어 돌려주므로 `null` 이 아닙니다.
- 첫 리비전에서 부르면 빈 객체가 오는 것으로 보입니다 <Badge type="danger" text="검증 필요" />. OOTB 는 `getObjectId(context)` 가 `""` 인지 (emxCPNProductDataLifecycleBase_mxJPO.java), `toString()` 이 `".."` 인지 (emxChangeBase_mxJPO.java), `exists(context)` 로 (emxPartMarkupBase_mxJPO.java) 확인합니다.
- 다음 리비전은 상속받은 `getNextRevision(context)` 로 받습니다.

```java
BusinessObject previous = part.getPreviousRevision(context);
String previousId = previous.getObjectId(context);
if (UIUtil.isNotNullAndNotEmpty(previousId)) {
    DomainObject previousPart = DomainObject.newInstance(context, previousId);
}
```

### getFirstRevision

<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

첫 리비전을 받습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getFirstRevision(context)` | 없음 | `BusinessObject`. `getRevisions(context)` 목록의 첫 요소. 목록이 비면 `null` |

- 밖에서는 같은 목록을 주는 `BusinessObject.getRevisions(context)` 의 첫 요소를 씁니다.

### getRevisions

<Badge type="warning" text="폐기" /> 대신 [getRevisionsInfo](#getrevisionsinfo) 를 씁니다. 같은 리비전 목록의 select 값을 한 번에 읽습니다.

모든 리비전의 select 값을 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getRevisions(context, selects, useCache)` | `selects`: 리비전마다 읽을 select. `null` 이면 빈 목록. `useCache`: `true` 면 이 변수에 남아 있는 지난 결과를 그대로 돌려줌 | `MapList`. 리비전마다 Map 하나. 키는 `selects` 의 select |

- 리비전마다 따로 열어 읽습니다 (`mxBus.print`). `DomainConstants.NEW_MULTI_VALUE_LIST` 에 없는 select 는 첫 값만 옵니다.
- 대체인 `getRevisionsInfo` 는 행에 `type`, `name`, `revision`, `id` 를 늘 넣고, 정책에 major 순서만 있으면 major 리비전 목록을 쓰는 점이 다릅니다.
- 인자가 `context` 하나인 `getRevisions(context)` 는 상속받은 `BusinessObject` 메서드이고 폐기가 아닙니다. `BusinessObjectList` 를 돌려줍니다.

### getMajorRevisions

major 리비전마다 객체 하나 (`bestsofar`) 를 받습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getMajorRevisions(context)` | 없음 | `BusinessObjectList`. 요소는 `DomainObject` |

- MQL `PRINT BUS $1 SELECT $2 DUMP $3` 에 이 객체 id, `majorids[].bestsofar.id`, `~` 를 넘기고, 받은 값을 `~` 로 잘라 id 마다 `DomainObject.newInstance(context, id)` 를 만듭니다.

### getRevisionsInfo

모든 리비전의 select 값을 한 번에 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getRevisionsInfo(context, selects, multiSelects)` | `selects`: 리비전마다 읽을 select. 값은 `String`. `multiSelects`: 값을 `StringList` 로 받을 select. 없으면 `new StringList()` | `MapList`. 리비전마다 Map 하나 |
| `obj.getRevisionsInfo(context, selects, multiSelects, substitute)` | 위와 같음. `substitute`: `BusinessObject.getSelectBusinessObjectData` 의 마지막 인자 (`QueryParams.setSubstitute`) 로 그대로 넘김. 3 개 인자 형태는 `true` (기본값과 같음) <Badge type="danger" text="검증 필요" /> | 같음 |

| 행 Map 의 키 | 값 |
| --- | --- |
| `type`, `name`, `revision`, `id` | 그 리비전의 타입, 이름, 리비전, id. 넣지 않아도 들어 있음 |
| `selects` 의 select | `String`. 값이 여러 개면 `\u0007` 로 이어짐 |
| `multiSelects` 의 select | `StringList` |

- `selects`, `multiSelects` 는 `null` 로 두지 않습니다. 처음에 바로 `size()` 를 불러 `NullPointerException` 이 납니다. 비울 때는 `new StringList()` 를 넘깁니다.
- `DomainConstants.NEW_MULTI_VALUE_LIST` 를 쓰지 않습니다. `state` 같은 select 도 `StringList` 로 받으려면 `multiSelects` 에 넣습니다.
- 리비전 목록은 정책에 minor 순서가 없고 major 순서만 있으면 [getMajorRevisions](#getmajorrevisions), 아니면 `getRevisions(context)` 입니다.
- 모든 리비전을 `BusinessObject.getSelectBusinessObjectData` 한 번으로 읽습니다.

```java
String selectDocIds = "from[" + relPartDocument + "].to.id";
MapList revisions = part.getRevisionsInfo(context, StringList.create(DomainConstants.SELECT_CURRENT),
        StringList.create(selectDocIds));
for (Object o : revisions) {
    Map row = (Map) o;
    String revision = (String) row.get(DomainConstants.SELECT_REVISION);
    String current = (String) row.get(DomainConstants.SELECT_CURRENT);
    StringList revisionDocIds = (StringList) row.get(selectDocIds);
}
```

`revisions` 의 모양 (값은 예시):

```text
[
  {type=My Part, name=P-0001, revision=A, id=11111.22222.33333.44444, current=Release,
   from[My Part Document].to.id=[11111.22222.33333.77777]},
  {type=My Part, name=P-0001, revision=B, id=11111.22222.33333.55555, current=Preliminary,
   from[My Part Document].to.id=[11111.22222.33333.77777, 11111.22222.33333.88888]}
]
```

### createRevision

<Badge type="warning" text="폐기" /> 대신 `reviseObject(context, true)` 를 씁니다. 둘 다 마지막 리비전에서 정책의 다음 리비전을 파일을 이어 받아 만듭니다.

다음 리비전을 만들고 그 값을 Map 으로 돌려줍니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.createRevision(context)` | 없음 | `Map`. 키는 `type`, `name`, `revision`, `id` (새 리비전의 값) |

- vault 는 context 의 vault (`context.getVault()`) 를 씁니다. [reviseObject](#reviseobject) 는 이 객체의 vault 를 씁니다.
- 트랜잭션을 열고 닫습니다. 실패하면 abort 하고 `FrameworkException` 을 던집니다.

### reviseObject

마지막 리비전의 다음 리비전을 만듭니다. 이 객체가 마지막 리비전이 아니어도 마지막 리비전에서 만듭니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.reviseObject(context, withFiles)` | `withFiles`: `true` 면 파일을 새 리비전으로 이어 받음 | `BusinessObject`. 새 리비전. id 는 `getObjectId(context)` |
| `obj.reviseObject(context, revision, withFiles)` | `revision`: 새 리비전 이름 (`"B"`). `null` 이면 정책의 다음 순서 | 같음 |

- `revision` 이 `null` 일 때만 마지막 리비전의 `getNextSequence(context)` 를 씁니다. `""` 는 그대로 넘깁니다.
- vault 는 이 객체의 vault 입니다. 변수에 vault 가 없으면 객체를 열어 읽습니다.
- 트랜잭션을 열고 닫습니다. 바깥에 트랜잭션이 있으면 그 안에 묶입니다 ([중첩 호출](/jpo/transaction-context#중첩-호출은-참조-카운트로-처리)). 실패하면 abort 하고 `FrameworkException` 을 던집니다.
- 생성 쪽 설명은 [생성](/jpo/api/write#생성) 에 있습니다.

```java
BusinessObject next = part.reviseObject(context, false);
String nextId = next.getObjectId(context);
BusinessObject docNext = doc.reviseObject(context, "B", true);
```

::: info 근거
xerinfra.jar `DomainObject`: `isLastRevision` 은 `getObjectId()` 를 읽고 `ContextUtil.pushContext` → `getLastRevision` → `newInstance(context, BusinessObject).getId()` 와 `equals` → `popContext`. `getLastRevision`, `getFirstRevision` 은 `openObject` → `getRevisions(context)` 의 마지막 / 첫 요소 (비면 `null`) → `closeObject`. `getPreviousRevision` 은 `openObject` → `super.getPreviousRevision(context)` → `closeObject` 이고, eMatrixServletRMI.jar `BusinessObject.getPreviousRevision` 은 서버가 준 값으로 `new BusinessObject(...)` 를 돌려줌. `getRevisions(Context, StringList, boolean)` 은 `Deprecated`, boolean 이 `true` 이고 필드 `_revisions` 가 있으면 그것을 돌려주고, 아니면 리비전마다 `mxBus.print(context, bo, selects, NEW_MULTI_VALUE_LIST)`. `getMajorRevisions` 는 `MqlUtil.mqlCommand(context, "PRINT BUS $1 SELECT $2 DUMP $3", id, "majorids[].bestsofar.id", "~")`. `getRevisionsInfo(context, selects, multiSelects)` 는 4 개 형태에 `true`. 4 개 형태는 `selects` + `multiSelects` 를 합치고 `getPolicy(context)` 의 `hasMinorSequence` 가 `false` 이고 `hasMajorSequence` 가 `true` 면 `getMajorRevisions`, 아니면 `getRevisions(context)`. 각 리비전 id 로 `getSelectBusinessObjectData(context, ids, 합친목록, boolean)` 을 한 번 부르고, 행마다 `type` / `name` / `revision` / `id` (`getTypeName()`, `getName()`, `getRevision()`, `getObjectId()`) 와 `selects` 는 `getSelectData`, `multiSelects` 는 `getSelectDataList` 를 넣음. `createRevision` 은 `Deprecated`, `getLastRevision` → `getNextSequence` → `getDefaultVault(context, null)` (= `context.getVault().getName()`) → `revise(context, 다음리비전, vault)` → `getInfo(context, [type, name, revision, id])`. `reviseObject(context, withFiles)` 는 `reviseObject(context, null, withFiles)`. 3 개 형태: `getLastRevision`, `revision` 이 `null` 이면 `getNextSequence` (eMatrixServletRMI.jar 에서 `nextMinorSequence`), `getDefaultVault(context, this)`, `startTransaction(context, true)`, `revise(context, null, revision, vault, withFiles)` (`ReviseParameters.setInheritFiles(withFiles)`), `commitTransaction`. `revise(context, rev, vault)` 는 `setInheritFiles(true)` 로 이어짐. emx / ENO 파일 호출: `getLastRevision` 33, `getRevisionsInfo` 31 (모두 3 개 인자), `getPreviousRevision` 19, `isLastRevision` 10, `getRevisions` (3 개 인자) 7, `reviseObject` 6 (모두 2 개 인자), `createRevision` 1, `getMajorRevisions` 0
:::

## 상태, 서명

정책의 상태를 옮기고, 상태 사이의 서명과 객체에 걸린 Route 를 읽는 메서드입니다. 상태 이름은 모두 실제 이름입니다 (심볼릭 이름이 아님).

### setState

목표 상태가 될 때까지 promote 나 demote 를 되풀이합니다. 앞으로도 뒤로도 옮깁니다. 상태 이름을 얻는 법은 [상태 바꾸기](/jpo/api/write#상태-바꾸기) 에 있습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.setState(context, state)` | `state`: 목표 상태의 실제 이름. 대소문자는 가리지 않음 | `int`. 목표 위치 - 처음 위치. 올렸으면 양수, 내렸으면 음수, 이미 그 상태면 `0` |
| `obj.setState(context, state, skipRouteCheck)` | `skipRouteCheck`: `false` 면 promote 할 때마다 먼저 끝나지 않은 Route 를 검사함. `true` 면 검사 안 함 (`setState(context, state)` 와 같음) | 위와 같음 |

- 정책의 상태 목록에서 처음 상태와 목표 상태의 위치를 찾고, 그 차이만큼 `promote` (앞으로) 나 `demote` (뒤로) 를 부릅니다. 돌려주는 값은 움직이기 전에 계산한 이 위치 차이입니다.
- 정책에 없는 상태면 `emxFramework.Beans.DomainObject.StateNotFound` 문구로 `FrameworkException` 이 납니다.
- 한 단계마다 `current` 를 다시 읽어 목표에 닿으면 멈춥니다. 목표를 지나쳐 버리면 `emxFramework.LifeCycle.PromotionFailed.ErrorMessage` (내릴 때는 `DemotionFailed`) 예외입니다.
- 서명이나 check 를 건너뛰는 처리는 없습니다. 한 단계 한 단계가 그냥 [promote, demote](#promote-demote) 라서, promote 를 막는 것은 `setState` 도 막습니다.
- `skipRouteCheck` 가 `false` 일 때의 검사: 이 객체에 `Object Route` 로 연결된 Route 중 현재 상태에 걸려 있고 아직 `Define` / `In Process` 인 것이 있으면 `emxFramework.Lifecycle.Promote.InCompleteRouteStateBlock` 문구로 `FrameworkException` 입니다. demote 할 때는 검사하지 않습니다.
- 객체를 열고 `ContextUtil.startTransaction(context, true)` 안에서 움직입니다. 실패하면 `abortTransaction` 후 `FrameworkException` 으로 감싸 던지므로, 가장 바깥 트랜잭션이면 앞서 움직인 단계까지 되돌아갑니다.

```java
String policy = part.getInfo(context, DomainConstants.SELECT_POLICY);
String release = PropertyUtil.getSchemaProperty(context, "policy", policy, "state_Release");
int moved = part.setState(context, release, false);
```

### gotoState

<Badge type="warning" text="폐기" /> 대신 `setState(context, state)` 를 씁니다. (안에서 `setState(context, state)` 를 부르고 돌려받은 값만 버림)

목표 상태까지 promote 나 demote 를 되풀이합니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.gotoState(context, state)` | `state`: 목표 상태의 실제 이름 | `void` |

### promoteWithBL

BL 은 Business Logic 입니다. 성숙도 (maturity) 비즈니스 로직 `PLMCheckMaturity`, `PLMActionMaturity`, `PLMPostCheckMaturity` 를 promote 앞뒤로 부르면서 한 단계 올립니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.promoteWithBL(context)` | 없음 | `void`. 실패하면 `MatrixException` |

- 순서는 아래와 같습니다. 비즈니스 로직은 `com.dassault_systemes.lifecycle.services.BusinessLogic` 을 리플렉션으로 불러 실행합니다.

| 단계 | 하는 일 | 실패하면 |
| --- | --- | --- |
| 1 | `current`, `policy`, `type`, `name`, Title 을 읽고 [getNextStateSignature](#getnextstatesignature) 로 다음 상태와 서명을 구함 | 예외 |
| 2 | `PLMCheckMaturity` 실행 (트랜잭션 밖) | 예외. promote 하지 않음 |
| 3 | `ContextUtil.startTransaction(context, true)` 후 `promote` | `abortTransaction` 후 예외 |
| 4 | `PLMActionMaturity` 실행 | **`commitTransaction` 후** 예외. promote 는 남음 |
| 5 | `PLMPostCheckMaturity` 실행 후 `commitTransaction` | `abortTransaction` 후 예외. promote 도 되돌아감 |

- 예외 메시지는 `BL Execution Failed <Title, 없으면 이름>-:<원래 메시지>` 형태입니다 (4 단계는 `BL Execution Failed On Post ...`).
- OOTB `emx*`, `ENO*` JPO 에서는 부르는 곳이 없습니다.

### demoteWithBL

[promoteWithBL](#promotewithbl) 과 같은 순서로 비즈니스 로직을 부르고, `promote` 대신 `demote` 로 한 단계 내립니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.demoteWithBL(context)` | 없음 | `void`. 실패하면 `MatrixException` |

- 내릴 때도 비즈니스 로직에 넘기는 서명과 상태는 [getNextStateSignature](#getnextstatesignature) 가 구한 다음 상태 쪽 값입니다.

### getNextStateSignature

객체의 현재 상태에서 나가는 분기 가운데 승인된 서명이 있는 분기를 찾아, 그 서명 이름과 대상 상태를 Map 에 넣습니다. [promoteWithBL](#promotewithbl) 이 안에서 씁니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getNextStateSignature(context, target, info, current)` | `target`: 상태를 볼 객체. `info`: 결과를 채울 `Map`. `current`: 쓰지 않음 | `void`. `info` 에 `BLSignature`, `BLToState` 를 넣음 |

- `target.getCurrentState(context)` 의 분기를 차례로 보며, 승인된 (approved) 서명이 처음 나온 분기에서 멈춥니다. `BLSignature` 는 그 서명 이름, `BLToState` 는 그 분기의 대상 상태 이름입니다.
- 승인된 서명이 없으면 `BLSignature` 는 `""`, `BLToState` 는 마지막으로 본 분기의 대상 상태입니다. 분기가 없으면 둘 다 `""` 입니다.
- 부른 객체 (`obj`) 와 네 번째 인자는 쓰지 않습니다. `target` 의 상태만 봅니다.

### getSignaturesDetails

`fromState` 에서 `toState` 로 넘어갈 때 필요한 서명을 하나씩 Map 으로 돌려줍니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getSignaturesDetails(context, fromState, toState)` | `fromState`, `toState`: 상태의 실제 이름 | `MapList`. 서명 하나에 Map 하나. 서명이 없으면 빈 목록 |
| `obj.getSignaturesDetails(context, from, to)` | `from`, `to`: `matrix.db.State`. 이름만 꺼내 위 형태를 부름 | 위와 같음 |

Map 하나의 키:

| 키 | 값 |
| --- | --- |
| `name` | 서명 이름 |
| `signer` | 서명한 사람 |
| `comment` | 서명할 때 남긴 의견 |
| `approved`, `rejected`, `ignored`, `signed` | 승인됨, 거부됨, 무시됨, 서명됨. `"TRUE"` 또는 `"FALSE"` |
| `hasapprove`, `hasreject`, `hasignore` | select `state[fromState].signature[서명이름].hasapprove` 등을 `getInfo` 로 읽은 값. 읽은 결과가 비면 이 세 키는 없음 |

- `hasapprove`, `hasreject`, `hasignore` 는 지금 사용자가 그 서명을 승인 / 거부 / 무시할 수 있는지를 나타내는 것으로 보입니다. <Badge type="danger" text="검증 필요" />
- 분기가 여럿이면 OOTB 처럼 정책의 상태를 하나씩 `toState` 로 넣어 봅니다 (emxLifecycleBase_mxJPO.java).

```java
String current = part.getInfo(context, DomainConstants.SELECT_CURRENT);
String policy = part.getInfo(context, DomainConstants.SELECT_POLICY);
String release = PropertyUtil.getSchemaProperty(context, "policy", policy, "state_Release");
MapList signatures = part.getSignaturesDetails(context, current, release);
StringList waiting = new StringList();
for (Object o : signatures) {
    Map sig = (Map) o;
    if (!"TRUE".equals(sig.get("approved"))) {
        waiting.add((String) sig.get("name"));
    }
}
```

### getApprovalsInfo

<Badge type="warning" text="폐기" /> 대신 쓸 메서드가 따로 없습니다. 서명은 [getSignaturesDetails](#getsignaturesdetails), Route 는 [getRouteInfoForBranches](#getrouteinfoforbranches) 로 나눠 읽습니다.

정책의 모든 상태 쌍 (양쪽 방향) 마다 서명과 그 상태에 걸린 Route 를 Map 으로 모으고, 끝에 Ad Hoc Route 를 담은 Map 을 하나 더 붙입니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getApprovalsInfo(context)` | 없음 | `MapList`. 상태 쌍마다 `name`, `signature`, `signers`, `status`, `comments`, `routes`. 마지막 Map 은 `name` 이 `"Ad Hoc Routes"` |

### getRouteInfoForStates

<Badge type="warning" text="폐기" /> 대신 `getRouteInfoForBranches(context, adHocState)` 를 씁니다. (같은 Route 를 같은 규칙으로 읽고, Route Target State 가 빈 Route 는 같은 키가 나옴)

이 객체에 걸린 Route 의 기준 상태를 키로 모읍니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getRouteInfoForStates(context, adHocState)` | `adHocState`: Route Base State 가 이 값과 같으면 이름을 바꾸지 않고 그대로 씀 | `Map`. 키는 상태 이름, 값은 `"true"` |

### getRouteInfoForBranches

이 객체에 `Object Route` 로 연결된 Route 를 보고, Route 가 걸린 상태 (와 목표 상태) 를 키로 모읍니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getRouteInfoForBranches(context, adHocState)` | `adHocState`: Route Base State 가 이 값과 같으면 이름을 바꾸지 않고 그대로 씀 | `Map`. 키는 `상태` 또는 `상태#TO#목표상태`, 값은 `"true"`. Route 가 없으면 빈 Map |

- `Object Route` 관계의 속성 Route Base Policy, Route Base State, Route Target State 를 읽고, 심볼릭 이름으로 보고 `PropertyUtil.getSchemaProperty`, `FrameworkUtil.lookupStateName` 으로 실제 이름으로 바꿉니다.
- Route Base Policy 가 이 객체의 정책과 다르거나, 정책이나 상태가 비면 그 Route 는 건너뜁니다.
- OOTB `emx*`, `ENO*` JPO 에서는 부르는 곳이 없습니다.

### promote, demote

<Badge type="info" text="BusinessObject" /> 상속받은 메서드입니다.

`matrix.db.BusinessObject` 의 메서드로, 한 단계 올리거나 내립니다. DomainObject 는 다시 정의하지 않습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.promote(context)` | 없음 | `void`. 실패하면 `MatrixException` |
| `obj.demote(context)` | 없음 | `void`. 실패하면 `MatrixException` |

- 객체 id 로 서버에 promote / demote 를 요청하고, 돌아온 메시지를 context 의 client task 에 쌓습니다. Java 쪽에서 트랜잭션을 따로 열지 않습니다.
- 정책의 상태 트리거가 걸립니다 ([라이프사이클과 promote](/trigger/lifecycle)). 여러 단계를 한 번에 옮기려면 [setState](#setstate) 를 씁니다.

::: info 근거
xerinfra.jar `DomainObject.setState(context, state)` → `setState(context, state, true)`. 세 인자 형태: `getStates` 에서 `equalsIgnoreCase` 로 목표 위치, `isCurrent()` 로 처음 위치, 차이가 양수면 (세 번째 인자가 `false` 일 때 `LifeCycleUtil.checksToPromoteObject` 뒤) `promote`, 음수면 `demote`, 단계마다 `getInfo(context, "current")` 확인, 반환은 위치 차이. `checksToPromoteObject` 는 `getRelatedObjects(Object Route, Route)` 에 관계 where (Route Base Policy / State 가 지금 정책, 상태의 심볼릭 이름. 속성 `emxFramework.AdHocRoutesBlockLifecycle` 이 참이면 Ad Hoc 도) 와 객체 where (`current` 가 `Define` 또는 `In Process`) 를 주고 결과가 있으면 예외. `promoteWithBL` / `demoteWithBL` → private `promoteDemoteWithBL(context, "Promote" / "Demote")`: `BLInput(context, 객체, current, BLSignature, BLToState)` 로 `BusinessLogic.invokeBLCheck` / `invokeBLAction` 을 리플렉션 호출. `getNextStateSignature` 는 `getCurrentState().getBranches()` 의 `StateBranch.getSignatures()` 에서 `isApproved()` 를 찾음 (eMatrixServletRMI.jar `StateBranch.getSignatures` 는 `getSignatures(context, 부모 상태, 분기 이름)`). `getSignaturesDetails` 는 `BusinessObject.getSignatures(context, from, to)`. `getRouteInfoForStates` / `Branches` 는 `expandSelect(Object Route, Route, from 쪽, 1 단계)` 의 관계 속성으로 키를 만듦. `BusinessObject.promote` / `demote` 는 서버 호출 결과를 `Context.addClientTasks`. 호출 예: emxLifecycleBase_mxJPO.java (`getSignaturesDetails`), emxWorkspaceMdlRelationBase_mxJPO.java (`setState(context, state, false)`), emxECOBase_mxJPO.java (`gotoState`, `getApprovalsInfo`)
:::

## 삭제

객체를 지우는 메서드입니다. 관계 끊기와 함께 쓰는 법은 [객체 삭제, 연결 끊기](/jpo/api/delete) 에 있습니다.

### delete

<Badge type="warning" text="폐기" /> 대신 `deleteObject` 를 씁니다. (같은 인자로 `deleteObject(context)`, `deleteObject(context, recursive)` 를 그대로 부름)

객체를 지웁니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.delete(context)` | 없음 | `void` |
| `obj.delete(context, recursive)` | `recursive`: [deleteObject](#deleteobject) 의 두 번째 인자와 같음 | `void` |

### deleteObject

이 객체를 지웁니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.deleteObject(context)` | 없음 | `void`. `deleteObject(context, false)` 와 같음 |
| `obj.deleteObject(context, recursive)` | `recursive`: 지울 목록을 만드는 [confirmDelete](#confirmdelete) 로 그대로 넘기는 값 | `void` |

- 객체를 열고 트랜잭션을 시작한 뒤, `confirmDelete` 로 지울 목록을 만듭니다. 목록이 하나면 `remove(context)`, 둘 이상이면 하나씩 MQL `delete bus $1` 로 지우고 commit 합니다.
- `DomainObject` 의 `confirmDelete` 는 자기 자신만 목록에 넣으므로 `recursive` 는 결과에 영향이 없습니다. `confirmDelete` 를 다시 정의한 하위 클래스에서만 뜻이 있습니다 (`com.matrixone.apps.common.Organization` 은 `false` 이고 하위 Business Unit 이 있으면 예외, `true` 면 하위 것까지 지울 목록에 넣음).
- 지우다 실패하면 `abortTransaction` 후 원래 예외의 **메시지만 담은 `java.lang.Exception`** 을 던집니다. `catch (MatrixException e)`, `catch (FrameworkException e)` 로는 잡히지 않습니다. 객체를 열지 못한 경우 (없는 id 등) 만 그 전 단계라 `FrameworkException` 그대로 나옵니다.
- 바깥 트랜잭션이 있으면 그 안에 묶입니다 ([여러 변경을 한 묶음으로](/jpo/api/write#여러-변경을-한-묶음으로)).

```java
doc.deleteObject(context);
```

### deleteObjects

여러 객체를 한 트랜잭션으로 지웁니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.deleteObjects(context, ids)` | `ids`: 지울 객체 id 배열 (`String[]`) | `void` |

- `ContextUtil.startTransaction(context, true)` 한 번 안에서 `DomainObject` 하나에 id 를 차례로 `setId` 하며 `deleteObject(context)` 를 부릅니다. MQL 을 직접 쓰지 않습니다.
- 하나라도 실패하면 `abortTransaction` 후 메시지만 담은 `java.lang.Exception` 을 던집니다. 가장 바깥 트랜잭션이면 앞서 지운 것도 되돌아갑니다.
- 빈 배열이면 아무것도 지우지 않고 끝납니다.

```java
DomainObject.deleteObjects(context, ids);

StringList docIdList = part.getInfoList(context, "from[" + relPartDocument + "].to.id");
DomainObject.deleteObjects(context, docIdList.toStringArray());
```

### confirmDelete

<Badge type="warning" text="폐기" /> 대신 쓸 메서드가 따로 없습니다. [deleteObject](#deleteobject) 가 지울 목록을 만들 때 안에서 부르는 메서드입니다.

`list` 에 이 객체를 넣습니다. 아무것도 지우지 않습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.confirmDelete(context, recursive, list)` | `recursive`: 쓰지 않음. `list`: 지울 객체를 모으는 `BusinessObjectList` | `void` |

::: info 근거
xerinfra.jar `DomainObject.delete(context)` → `deleteObject(context)`, `delete(context, b)` → `deleteObject(context, b)`, `deleteObject(context)` → `deleteObject(context, false)`. `deleteObject(context, b)`: `openObject`, `startTransaction(context, true)`, `confirmDelete(context, b, list)`, `list.size() > 1` 이면 목록마다 `MqlUtil.mqlCommand(context, "delete bus $1", id)`, 아니면 `remove(context)`, `commitTransaction`, `closeObject`. 예외는 `abortTransaction` 후 `new Exception(e.getMessage())`. `deleteObjects(context, String[])`: `ContextUtil.startTransaction(context, true)`, `new DomainObject()` 에 `setId` + `deleteObject(context)` 반복, `ContextUtil.commitTransaction`, 예외면 `ContextUtil.abortTransaction` 후 `new Exception(e.getMessage())`. `confirmDelete` 는 `list.add(this)` 만 함. 하위 클래스: pnocommon.jar `Organization.confirmDelete` (`getBusinessUnits` 결과가 있고 `b` 가 `false` 면 `BusinessUnitContainsBusinessUnits` 예외). 호출 예: emxCommonDocumentBase_mxJPO.java (`deleteObject(context, true)`), emxChangeBase_mxJPO.java (`deleteObjects`)
:::

## 트랜잭션, 권한 전환

DomainObject 에 남아 있는 트랜잭션, 사용자 전환 메서드입니다. 모두 폐기이고 `ContextUtil` 의 같은 이름 메서드를 같은 인자로 부릅니다. 쓰는 법은 [트랜잭션](/jpo/transaction-context#트랜잭션), [슈퍼유저로 바꾸기](/jpo/transaction-context#슈퍼유저로-바꾸기-pushcontext) 에 있습니다.

### startTransaction

<Badge type="warning" text="폐기" /> 대신 `ContextUtil.startTransaction(context, update)` 를 씁니다. (인자를 그대로 넘겨 부름)

트랜잭션을 시작합니다. 이미 `ContextUtil` 로 열려 있으면 횟수만 올립니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.startTransaction(context, update)` | `update`: `true` 면 쓰기, `false` 면 읽기 전용 | `boolean`. 시작했거나 횟수를 올렸으면 `true`, 다른 곳에서 연 트랜잭션이 있어 아무것도 안 했으면 `false` |

### commitTransaction

<Badge type="warning" text="폐기" /> 대신 `ContextUtil.commitTransaction(context)` 를 씁니다. (그대로 부름)

트랜잭션을 확정합니다. 가장 바깥에서 부른 것만 실제로 확정합니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.commitTransaction(context)` | 없음 | `void` |

### abortTransaction

<Badge type="warning" text="폐기" /> 대신 `ContextUtil.abortTransaction(context)` 를 씁니다. (그대로 부름)

트랜잭션을 되돌립니다. 횟수를 줄여 0 이 될 때만 실제로 되돌립니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.abortTransaction(context)` | 없음 | `void`. 예외를 던지지 않음 (안에서 난 예외는 표준 오류에 출력) |

### setSavePoint

<Badge type="warning" text="폐기" /> 대신 `ContextUtil.setSavePoint(context, name)` 을 씁니다. (그대로 부름)

트랜잭션 안에 저장점을 만듭니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.setSavePoint(context, name)` | `name`: 저장점 이름 | `void` |

- MQL `set transaction savepoint $1` 을 실행합니다. 그 지점까지 되돌리려면 `ContextUtil.abortSavePoint(context, name)` (MQL `abort transaction $1`) 를 씁니다.

### pushContext

<Badge type="warning" text="폐기" /> 대신 `ContextUtil.pushContext` 를 씁니다. (같은 인자로 그대로 부름)

<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

지금 사용자를 쌓아 두고 다른 사용자로 바꿉니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.pushContext(context)` | 없음 | `void`. User Agent 로 바꿈 |
| `DomainObject.pushContext(context, user, password, vault)` | `user`, `password`, `vault`: 바꿀 사용자. `user` 가 비면 User Agent, `vault` 가 `null` 이면 지금 vault | `void` |

### popContext

<Badge type="warning" text="폐기" /> 대신 `ContextUtil.popContext(context)` 를 씁니다. (그대로 부름)

<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

[pushContext](#pushcontext) 전의 사용자로 돌아갑니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.popContext(context)` | 없음 | `void` |

::: info 근거
xerinfra.jar `DomainObject`: `startTransaction(context, b)` → `ContextUtil.startTransaction(context, b)`, `commitTransaction` → `ContextUtil.commitTransaction`, `abortTransaction` → `ContextUtil.abortTransaction` (throws 없음), static `setSavePoint` → `ContextUtil.setSavePoint`, protected static `pushContext(context)` / `pushContext(context, user, password, vault)` / `popContext` → `ContextUtil` 의 같은 메서드. `ContextUtil.startTransaction(context, b)` → `(context, b, false)`: 횟수가 0 보다 크면 올리고 `true`, 아니면 `isTransactionActive` 가 참일 때 `false`, 아니면 `Context.start` 후 횟수 1 과 `true`. `abortTransaction` 은 횟수를 줄여 0 일 때 `Context.abort`, 예외는 잡아서 `printStackTrace(System.err)`. `setSavePoint` 는 MQL `set transaction savepoint $1`, `abortSavePoint` 는 `abort transaction $1`. `pushContext(context, user, password, vault)` 는 `user` 가 비면 `person_UserAgent`, `vault` 가 `null` 이면 `context.getVault()`
:::

## 파일

객체에 체크인된 파일 목록을 읽거나, 서버에 있는 파일을 객체에 체크인하는 메서드입니다.

### getAllFiles

파일 목록을 포맷별로 묶어 받습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getAllFiles(context)` | 없음 | `MapList`. 포맷 하나가 Map 하나. 키 `format` 에 포맷 이름, 키 `"1"`, `"2"` ... 에 파일 이름 |

- 상속받은 `getFiles(context)` 의 파일을 차례로 보면서, 앞 파일과 포맷이 다를 때마다 새 Map 을 만듭니다.
- 번호는 Map 마다 `"1"` 부터 붙고, 이름이 빈 파일은 건너뜁니다. 파일이 없으면 빈 `MapList` 입니다.
- 실패하면 `FrameworkException` 이 아니라 `MatrixException` 을 던집니다.

```text
[
  {format=generic, 1=spec.pdf, 2=spec.docx},
  {format=Image, 1=photo.jpg}
]
```

### getAllFormatFiles

파일 하나를 Map 하나로 받습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getAllFormatFiles(context)` | 없음 | `MapList`. 파일 하나가 Map 하나. 키는 `format` (포맷 이름), `filename` (파일 이름) |

- 이름이 빈 파일은 건너뜁니다. 파일이 없으면 빈 `MapList` 입니다. 실패하면 `MatrixException` 을 던집니다.
- select 로는 `format.file.name`, `format.file.format` 을 읽습니다. 둘 다 기본 목록 (`DomainConstants.NEW_MULTI_VALUE_LIST`) 에 있어 `getInfo(context, selects)` 에서 `StringList` 로 옵니다 ([값이 여러 개인 select](/jpo/api/read#값이-여러-개인-select)).

```java
MapList files = doc.getAllFormatFiles(context);
for (Object o : files) {
    Map file = (Map) o;
    String format = (String) file.get("format");
    String fileName = (String) file.get("filename");
}
```

`files` 의 모양 (값은 예시):

```text
[
  {format=generic, filename=spec.pdf},
  {format=generic, filename=spec.docx},
  {format=Image, filename=photo.jpg}
]
```

### checkinFromServer

서버의 workspace 폴더에 있는 파일을 이 객체에 체크인합니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.checkinFromServer(context, unlock, append, format, store, fileNames)` | `unlock`: 체크인 뒤 잠금을 풀지. `append`: `true` 면 이미 있는 파일에 더함. `format`: 포맷 이름. `null` 이면 `DomainConstants.FORMAT_GENERIC`. `store`: store 이름. `null` 이면 `""`. `fileNames`: `context.createWorkspace()` 폴더 안의 파일 이름 목록 (`StringList`) | `void` |
| `obj.checkinFromServer(context, unlock, append, format, fileNames)` | store 를 넘기지 않음. 로그인 사용자 회사의 store (`DomainConstants.SELECT_COMPANY_STORE` 를 사람 객체에서 읽어 실제 이름으로 바꾼 값) 를 씀 | `void` |

- 파일 경로는 `context.createWorkspace() + "/" + 파일이름` 입니다. 먼저 같은 workspace 에 파일을 써 둡니다. OOTB (emxCommonDocumentBase_mxJPO.java) 도 `context.createWorkspace()` 폴더에 이미지를 만든 뒤 부릅니다. 이 방식은 같은 context 에서 `createWorkspace()` 가 같은 폴더를 돌려준다는 전제입니다 <Badge type="danger" text="검증 필요" />.
- FCS 로 체크인합니다. store 가 FCS 체크인을 받지 않으면 `"Non suported on non-fcs stores"` 로 실패합니다. 예외는 `FrameworkException` 으로 감싸 던집니다.
- `DomainObject` 쪽에서는 트랜잭션을 열지 않습니다.
- OOTB 는 6 개 인자 형태만 씁니다 (22 건, `store` 에 `null` 을 넘기는 곳이 많음).

```java
String workspace = context.createWorkspace();
String fileName = "report.txt";
Files.write(Paths.get(workspace, fileName), "P-0001".getBytes(StandardCharsets.UTF_8));
doc.checkinFromServer(context, true, true, DomainConstants.FORMAT_GENERIC, null, StringList.create(fileName));
Files.deleteIfExists(Paths.get(workspace, fileName));
```

### backupFile

<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

이 객체의 첫 파일 하나를 다른 객체로 복사합니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.backupFile(context, target, store)` | `target`: 파일을 넣을 객체. `store`: store 이름. `null` 이나 `""` 면 store 를 정하지 않음 | `void` |

- `format.file.name` 의 첫 값과 `format.file.format` 의 파일을 서버 임시 폴더 (`java.io.tmpdir` 아래 `backup`) 로 체크아웃한 뒤 `target` 에 체크인하고 임시 파일을 지웁니다. 파일이 없으면 예외가 납니다.

## 소유권, 접근 권한

객체의 협업공간 (project), 조직 (organization), 소유자를 바꾸거나 부여된 접근 권한을 거두는 메서드입니다. 모두 MQL `mod bus` 를 부릅니다.

### setPrimaryOwnership

객체의 협업공간과 조직을 바꿉니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.setPrimaryOwnership(context, project, organization)` | `project`: 협업공간 이름 (project 인 role). `organization`: 조직 이름 (organization 인 role) | `void` |
| `obj.setPrimaryOwnership(context, project, organization, asSuperUser)` | `asSuperUser`: `true` 면 `mod bus` 를 [pushContext](/jpo/transaction-context#슈퍼유저로-바꾸기-pushcontext) 로 실행. `false` 면 로그인 사용자로. 3 개 인자 형태는 `true` | `void` |

- 인자 순서는 `project` 가 먼저입니다. OOTB 도 `setPrimaryOwnership(context, 협업공간, 조직)` 순으로 넘깁니다 (emxECPartBase_mxJPO.java, emxCommonTaskBase_mxJPO.java).
- 먼저 `print role $1 select $2 dump;` 로 `organization` 의 `isanorg`, `project` 의 `isaproject` 가 `TRUE` 인지 봅니다. 아니면 `"Invalid ORGANIZATION"`, `"Invalid PROJECT"` 로 실패합니다. 이 확인은 늘 pushContext 로 실행합니다.
- 그다음 `mod bus $1 project $2 organization $3` 을 실행합니다.
- 트랜잭션을 열고 닫습니다. 실패하면 abort 하고 `FrameworkException` 을 던집니다.
- 지금 값은 select `project`, `organization` (`SELECT_PROJECT`, `SELECT_ORGANIZATION`) 으로 읽습니다.

```java
String user = context.getUser();
String project = PersonUtil.getDefaultProject(context, user);
String organization = PersonUtil.getDefaultOrganization(context, user);
part.setPrimaryOwnership(context, project, organization);
doc.removePrimaryOwnership(context);
```

### removePrimaryOwnership

객체의 협업공간과 조직을 비웁니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.removePrimaryOwnership(context)` | 없음 | `void` |

- `mod bus $1 project $2 organization $3;` 에 `""`, `""` 를 넘겨 로그인 사용자로 실행합니다. 트랜잭션과 예외는 [setPrimaryOwnership](#setprimaryownership) 과 같습니다.

### TransferOwnership

소유자, 협업공간, 조직을 한 번에 바꿉니다. 이름이 대문자 `T` 로 시작합니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.TransferOwnership(context, owner, project, organization)` | `owner`: 새 소유자 (사람 이름). `project`, `organization`: 새 협업공간, 조직 | `void` |

- `owner` 의 보안 컨텍스트 중 `organization` 에 속한 협업공간 (`PersonUtil.getProjects(context, owner, organization)`) 에 `project` 가 없으면 `emxFramework.Invalid.Ownership` 문구로 실패합니다.
- 그다음 [setPrimaryOwnership](#setprimaryownership) 과 같이 조직과 협업공간을 확인하고, `mod bus $1 project $2 organization $3 owner $4` 를 로그인 사용자로 실행합니다.
- 트랜잭션을 열고 닫습니다. 실패하면 abort 하고 원래 예외의 메시지로 `FrameworkException` 을 던집니다.

### revokeAccess

`grant` 로 부여된 접근 권한을 거둡니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.revokeAccess(context, grantor, grantee)` | `grantor`: 권한을 준 사람. `grantee`: 권한을 받은 사람. 하나만 넘기거나 둘 다 `null` 도 됨 | `void` |

넘긴 값에 따라 먼저 select 하나를 읽고, 그 값이 `true` 일 때만 MQL 을 실행합니다.

| 넘긴 값 | 먼저 읽는 select | 실행하는 MQL |
| --- | --- | --- |
| `grantor`, `grantee` 둘 다 | `grant[grantor,grantee]` | `mod bus $1 revoke grantor $2 grantee $3` |
| `grantor` 만 | `grantor[grantor]` | `mod bus $1 revoke grantor $2` |
| `grantee` 만 | `grantee[grantee]` | `mod bus $1 revoke grantee $2` |
| 둘 다 `null` | `grant` | `mod bus $1 revoke all` |

- 거둘 권한이 없으면 아무것도 하지 않고 끝납니다 (예외 없음).
- select 는 로그인 사용자로 읽고, MQL 은 [pushContext](/jpo/transaction-context#슈퍼유저로-바꾸기-pushcontext) 로 실행합니다.
- `null` 만 "넘기지 않음" 으로 봅니다. `""` 는 그대로 select 와 MQL 에 들어갑니다.
- 객체를 열고 닫습니다. 트랜잭션은 열지 않습니다.

::: info 근거
xerinfra.jar `DomainObject`: `getAllFiles` 는 `getFiles(context)` 를 돌며 `File.getFormat()` 이 앞 파일과 다르면 새 `HashMap` 에 `format` 을 넣고, `getName()` 이 비지 않으면 `String.valueOf(번호)` 키에 이름을 넣음 (번호는 포맷마다 1 부터). `getAllFormatFiles` 는 이름이 비지 않은 파일마다 `format`, `filename`. 둘 다 예외를 `MatrixException(e.toString())` 으로 던짐. `checkinFromServer` 5 개 형태: format 이 `null` 이면 `FORMAT_GENERIC`, `PersonUtil.getPersonObject(context).getInfo(context, SELECT_COMPANY_STORE)` 가 비지 않으면 `PropertyUtil.getSchemaProperty` 로 바꿔 6 개 형태를 부름. 6 개 형태: store 가 `null` 이면 `""`, 파일 이름마다 `new matrix.db.File(context.createWorkspace() + "/" + 이름, format)` 을 `FileList` 에 담아 `BusinessObject.checkinFromServer(context, unlock, append, format, store, FileList)` 를 부름 (이 상속 메서드에는 `Deprecated` 가 붙어 있음). eMatrixServletRMI.jar 쪽은 `FcsSupport.isCheckinEligible` 이 `false` 면 `"Non suported on non-fcs stores"`, 파일마다 `new BusinessObjectProxy(id, format, 이름, append, unlock)` (필드 `_append`, `_lockUnlock`) 를 만들어 `CheckinStart.doItFromServer` → `FcsClient.checkin` → `CheckinEnd.doIt`. `backupFile` 은 `File.createTempFile("domain", null)` 의 상위 폴더 아래 `backup/<이름>` 을 만들고 `getInfo(context, [format.file.name, format.file.format])`, `checkout bus $1 server format $2 file $3 directory $4`, store 가 있으면 `store "<store>"` 를 넣어 `checkin bus $1 server $2 $3 $4 $5`. `setPrimaryOwnership(context, project, organization)` 은 4 개 형태에 `true`. 4 개 형태: `ContextUtil.startTransaction(context, true)`, `MqlUtil.mqlCommand(context, "print role $1 select $2 dump;", true, organization, "isanorg")`, 같은 명령으로 `project`, `"isaproject"`, `mqlCommand(context, "mod bus $1 project $2 organization $3", asSuperUser, id, project, organization)`. `MqlUtil.mqlCommand(Context, String, boolean, String...)` 의 boolean 은 `true` 면 `ContextUtil.pushContext` 뒤 실행. `removePrimaryOwnership` 은 `mqlCommand(context, "mod bus $1 project $2 organization $3;", id, "", "")`. `TransferOwnership` 은 private `isValidOwnership` (`PersonUtil.getProjects(context, owner, organization).contains(project)`) 뒤 role 확인, `mqlCommand(context, "mod bus $1 project $2 organization $3 owner $4", false, id, project, organization, owner)`. `revokeAccess` 는 `openObject` 뒤 위 표의 select 를 `getInfo(context, select)` 로 읽어 `"true"` 면 `mqlCommand(context, 명령, true, ...)`, `closeObject`. emx / ENO 파일 호출: `setPrimaryOwnership` 34 (3 개 인자 29, 4 개 인자 5, 4 개 인자는 모두 `false`), `checkinFromServer` 22 (모두 6 개 인자), `removePrimaryOwnership` 11, `revokeAccess` 5, `getAllFormatFiles` 4, `getAllFiles` 1, `TransferOwnership` 1 (emxDomainAccessBase_mxJPO.java)
:::

## 그 밖에

라이선스 확인용 목록, 디버그 출력, 객체별 캐시입니다. OOTB JPO 에서는 쓰지 않습니다.

### getLicenseReservedAPPS

객체에 붙은 라이선스 확인용 애플리케이션 목록을 읽습니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.getLicenseReservedAPPS()` | 없음 | `String[]`. public 필드 `LICENSE_RESERVED_APPS` 의 값. 생성자가 빈 배열을 넣어 둠 |

- `DomainObject` 자신은 이 값을 읽지 않습니다. 생성 폼 (framework.jar `UIFormCommon.createObject`) 이 `newInstance(context, type, app)` 로 만든 객체에서 읽어, 비어 있지 않으면 `FrameworkLicenseUtil.checkLicenseReserved(context, apps)` 로 라이선스를 확인합니다.

### setLicenseReservedAPPS

라이선스 확인용 애플리케이션 목록을 바꿉니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `obj.setLicenseReservedAPPS(apps)` | `apps`: 애플리케이션 이름 배열 | 없음 |

### debug

<Badge type="warning" text="폐기" /> 대신 `DebugUtil.debug(...)` 를 씁니다. (인자 1 ~ 3 개 형태와 출력 방식이 같고, 켜는 값만 `DebugUtil.setDebug` 로 다름)
<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

디버그가 켜져 있으면 `System.out` 에 한 줄 출력합니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `debug(msg)` | `msg`: 출력할 문자열 | 없음 |
| `debug(msg1, msg2)` | 이어 붙여 한 줄로 출력할 문자열 | 없음 |
| `debug(msg1, msg2, msg3)` | 이어 붙여 한 줄로 출력할 문자열 | 없음 |

- [setDebug](#setdebug) 로 켠 때만 출력합니다. `setId` 의 `null` / `""` 경고가 이것으로 나갑니다.

### setDebug

<Badge type="warning" text="폐기" /> 대신 `DebugUtil.setDebug(on)` 을 씁니다. (같은 방식으로 `DebugUtil` 쪽 출력을 켬)

`DomainObject` 의 [debug](#debug) 출력을 켜거나 끕니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `DomainObject.setDebug(on)` | `on`: `true` 면 켬 | 없음 |

- static 값이라 그 서버의 모든 `DomainObject` 에 적용됩니다. OOTB 의 `.setDebug(` 는 `DebugUtil` 이나 다른 클래스의 것입니다.

### getCachedData

<Badge type="warning" text="폐기" /> 대신 쓸 메서드가 따로 없습니다.
<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

객체 변수에 담아 둔 값을 꺼냅니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `getCachedData(key)` | `key`: 넣을 때 쓴 키 | `Object`. 없으면 `null` |

### setCachedData

<Badge type="warning" text="폐기" /> 대신 쓸 메서드가 따로 없습니다.
<Badge type="tip" text="protected" /> 하위 클래스에서만 부를 수 있습니다.

객체 변수에 값을 담아 둡니다.

| 형태 | 넘기는 값 | 돌려받는 것 |
| --- | --- | --- |
| `setCachedData(key, value)` | `key`, `value`: 담을 키와 값 | 없음 |

- 객체 변수마다 따로 있는 `HashMap` 입니다. 서버에 저장되지 않고, [clear](#clear) 와 id 가 바뀌는 [setId](#setid) 가 비웁니다.

::: info 근거
xerinfra.jar `DomainObject` 바이트코드. `get/setLicenseReservedAPPS` 는 `LICENSE_RESERVED_APPS` 필드를 읽고 쓰기만 함. 이 필드를 읽는 다른 곳은 lib 폴더 jar 중 framework.jar `UIFormCommon.createObject` 하나 (`getLicenseReservedAPPS()` 가 비어 있지 않으면 `FrameworkLicenseUtil.checkLicenseReserved`). `debug` 3 개는 static `_debug` 가 `true` 일 때 `System.out.print` / `println`, `setDebug` 는 `_debug` 에 대입. xerinfra.jar `DebugUtil.debug` 1 ~ 3 개, `DebugUtil.setDebug` 는 같은 모양이고 폐기 표시 없음. `getCachedData` / `setCachedData` 는 `_cacheMap` (`HashMap`, 처음 넣을 때 만듦) 의 `get` / `put`
:::

## 샘플 JPO

[XX_DomainObjectApi_mxJPO.java](https://github.com/LeeOuHyoun/3d-docs/blob/main/JPOSample/XX_DomainObjectApi_mxJPO.java) 에 이 페이지의 메서드 가운데 [XX_ObjectApi_mxJPO.java](/jpo/api/#샘플-jpo) 에 없는 것을 메서드별로 담았습니다. 모든 메서드는 `JPO.unpackArgs(args)` 로 꺼낸 Map (`programMap`) 에서 값을 읽습니다.

| 메서드 | 받는 값 (`programMap` 의 키) | 돌려주는 값 | 보여 주는 것 |
| --- | --- | --- | --- |
| `getPartSummary` | `objectId` | `Map` (`isPart`, `isLastRevision`, `hasDocument`, `isUsed`, `attributes`) | [isKindOf](#iskindof), [isLastRevision](#islastrevision), [hasRelatedObjects](#hasrelatedobjects), [getAttributeMap](#getattributemap) |
| `getNames` | `objectIds` (쉼표로 이은 id) | `MapList` (id 마다 `name`, `current`, `id`, `type`) | 객체 변수 하나를 [setId](#setid) 로 다시 쓰기. 빈 id 는 거름 |
| `getIdByName` | `name`, `revision` | `String` (객체 id) | 타입, 이름, 리비전으로 만든 객체에서 [getId](#getid) |
| `getLatestRevisionId` | `objectId` | `String` (마지막 리비전의 id) | [isLastRevision](#islastrevision), [getLastRevision](#getlastrevision) |
| `getRevisionHistory` | `objectId` | `MapList` (리비전마다 Map 하나) | [getRevisionsInfo](#getrevisionsinfo). 문서 id 는 `StringList` 로 받음 |
| `getFirstDocument` | `objectId` | `Map`. 문서가 없으면 `null` | [getRelatedObject](#getrelatedobject) |
| `connectDocuments` | `objectId`, `documentIds` (쉼표로 이은 id) | `Map` (문서 id → 관계 id) | [addRelatedObjects](#addrelatedobjects). `true` 라서 `부품 → 각 문서` |
| `replaceDocument` | `objectId`, `documentId` (비우면 끊기) | `String` (남은 관계 id. 끊었으면 `""`) | [setRelatedObject](#setrelatedobject) |
| `createNumberedDocument` | 없음 | `String` (새 문서 id) | [getAutoGeneratedName](#getautogeneratedname) 으로 받은 이름으로 [createObject](#createobject) |
| `copyPart` | `objectId`, `name` | `String` (복사본 id) | [cloneObject](#cloneobject) 6 개 인자. 이름을 지키고 파일은 복사하지 않음 |
| `renamePart` | `objectId`, `name` | 없음 | [setName](#setname). 빈 이름은 미리 막음 |
| `getUnsignedSignatures` | `objectId` | `StringList` (아직 승인되지 않은 서명 이름) | 현재 상태에서 Release 상태까지의 [getSignaturesDetails](#getsignaturesdetails) |

- 부르는 법은 [불러 보기](/jpo/api/#불러-보기) 와 같습니다. 프로그램 이름은 `XX_DomainObjectApi` 입니다.
- `connectDocuments` 의 `addRelatedObjects(..., true, ids)` 는 id 하나를 받는 `addRelatedObject(..., true, id)` 와 방향이 반대입니다 ([연결](#연결)).
- `createNumberedDocument` 는 `type_MyDocument` 이름의 채번기가 있어야 합니다. 없으면 예외가 납니다.

::: details XX_DomainObjectApi_mxJPO.java

<<< @/../JPOSample/XX_DomainObjectApi_mxJPO.java

:::

::: warning
샘플의 타입, 관계 (`type_MyPart`, `relationship_MyPartDocument` ...) 는 예시 이름입니다. 실제 스키마의 심볼릭 이름으로 바꿔야 합니다. `connectDocuments`, `replaceDocument`, `createNumberedDocument`, `copyPart`, `renamePart` 는 데이터를 바꿉니다. 컴파일은 확인했고 서버에서 실행해 보지는 않았습니다.
:::
