# 독립 실행 이관 프로그램 <Badge type="info" text="원본 분석" />

대량 데이터를 넣는 Java 프로그램입니다. 서버 안에서 돌지 않고, 웹앱의 class 와 jar 를 클래스패스로 잡아 `main` 으로 따로 실행합니다. 실행 명령은 [서버 반영 절차](/deploy/#독립-실행-프로그램-이관) 에 있고, 여기서는 프로그램의 구조를 정리합니다.

| 층 | class | 역할 |
| --- | --- | --- |
| 실행 | `...MigrationApp` | `main`. 인자 확인, Context 만들기, 트랜잭션, 결과 출력, 종료 코드 |
| 처리 | `...Migration` / `...MigrationService` | 입력 파일을 읽고 항목마다 객체를 만들어 결과를 남김 |
| 입력 | `...Input`, `...Data` | 항목 하나 / 항목 묶음. 항목은 `Migratable` 을 구현 |
| 읽기 | `...JsonParser`, `...ExcelParser` | 입력 파일(JSON, 엑셀)을 입력 class 로 바꿈 |
| 결과 | `MigrationResult`, `MigrationItemResult`, `MigrationError`, `MigrationStatistics` | 전체 결과, 항목 결과, 오류, 통계 |
| 종류 | `MigrationType` | 이관 종류 enum (첫 번째 인자) |

::: info 근거
프로젝트 `src/` 의 migration 패키지 (38 개 파일: 실행 class 5 개, 처리 class 6 개, 결과 class 5 개, 입력 / 읽기 class), `resources/custom/migration/` 의 실행 bat 5 개, 입력 JSON 1 개와 엑셀. 계층 구조 이관(JSON)과 표 형태 이관(엑셀) 두 갈래가 있습니다. 이름은 일반적인 이름으로 바꿔 적었습니다
:::

## 실행 class

```java
public static void main(String[] args) {
    if (args.length < 3) {
        logger.error("Usage: java MyMigrationApp <type> <jsonFile> <commit|abort>");
        System.exit(1);
    }
    System.exit(migration(args[0], args[1], args[2]));
}

private static int migration(String type, String sourceFile, String commit) {
    Context context = null;
    String migrationId = type + "-" + commit + "-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
    try {
        context = MyContextUtil.getContext(
                MyConfig.values.get("ENCRYPTED_IF_USERNAME"),
                MyConfig.values.get("ENCRYPTED_IF_PASSWORD"),
                "ctx::VPLMProjectLeader.Company Name.MyCompany");
        ContextUtil.startTransaction(context, true);

        MigrationResult<MyData> result = new MyMigration(sourceFile).migrate(context, migrationId);
        printMigrationResult(result);

        if ("commit".equalsIgnoreCase(commit) && result.isFullySuccessful()) {
            ContextUtil.commitTransaction(context);
            return 0;
        }
        ContextUtil.abortTransaction(context);
        return result.isFullySuccessful() ? 0 : 1;
    } catch (Exception e) {
        logger.error("Error: {}", e.getMessage(), e);
        if (context != null) {
            ContextUtil.abortTransaction(context);
        }
        return 1;
    } finally {
        if (context != null) {
            context.close();
        }
    }
}
```

- 인자는 `<종류> <입력 파일> <commit|abort>` 세 개입니다. 하나라도 빠지면 사용법을 로그로 남기고 종료 코드 1 로 끝납니다.
- `migrationId` 는 `종류-모드-시각` 입니다. 로그에서 실행 한 번을 찾는 열쇠입니다.
- Context 는 서버 밖이므로 직접 만듭니다 ([서버에서 Context 만들기](/server-java/#서버에서-context-만들기)). 계정은 설정 로더의 `ENCRYPTED_` 키에서 읽습니다.
- **`commit` 이면서 실패 항목이 하나도 없을 때만** 저장합니다. 하나라도 실패하면 `commit` 을 줘도 되돌립니다.
- 종료 코드는 성공 0, 실패 1 입니다. bat 이 이 값으로 성공 / 실패를 출력합니다.

::: warning 계정을 코드에 적지 않습니다
원본 실행 class 중 하나는 관리자 계정과 비밀번호를 `getContext("계정", "비밀번호", ...)` 로 코드에 그대로 적었습니다. 다른 class 처럼 설정 로더의 암호화 키에서 읽습니다.
:::

## 처리 class

계층 구조(분류 체계) 이관의 처리 순서입니다.

| 순서 | 내용 |
| --- | --- |
| 1 | 입력 JSON 을 읽음. 먼저 파일 경로로 읽고, 실패하면 클래스패스 리소스(`custom/migration/...`)로 다시 읽음 |
| 2 | 최상위(`level: 1`) 항목을 만듦 |
| 3 | `level` 2, 3, ... 순서로 그 깊이의 항목을 만듦. 부모가 먼저 만들어지므로 하위 항목은 부모를 이름으로 찾아 연결 |
| 4 | 실패 항목이 0 개면 `overallSuccess = true` |

항목 하나는 이렇게 처리합니다.

```java
MigrationItemResult result = new MigrationItemResult(item.getIdentifier(), item.getDisplayName());
result.markInProgress();
try {
    String exists = MqlUtil.mqlCommand(context, "print bus $1 $2 $3 select $4 dump", TYPE, item.getName(), "-", "exists");
    if ("TRUE".equalsIgnoreCase(exists)) {
        String id = MqlUtil.mqlCommand(context, "print bus $1 $2 $3 select $4 dump", TYPE, item.getName(), "-", "id");
        result.markSuccess(id);
    } else {
        DomainObject object = DomainObject.newInstance(context);
        object.createObject(context, TYPE, item.getName(), "-", POLICY, VAULT);
        object.setAttributeValues(context, attributes);
        object.addFromObject(context, new RelationshipType(REL_TO_PARENT), parentId);
        result.markSuccess(object.getInfo(context, "id"));
    }
} catch (Exception e) {
    result.markFailure(new MigrationError.Builder()
            .errorCode("CREATE_FAILED")
            .errorMessage(e.getMessage())
            .itemIdentifier(item.getIdentifier())
            .itemDisplayName(item.getDisplayName())
            .severity(MigrationError.ErrorSeverity.ERROR)
            .build());
}
```

- **이미 있으면 그 id 로 성공 처리합니다.** 중간에 멈춘 이관을 처음부터 다시 돌려도 이미 만든 항목은 건너뜁니다.
- 항목 하나가 실패해도 예외를 밖으로 던지지 않고 결과에 남긴 뒤 다음 항목으로 갑니다. 전체가 멈추는 것은 입력 파일을 못 읽었을 때뿐입니다 (`MigrationResult.failure`).

입력 JSON 은 항목마다 `name`, `level`, `parentName`, `title`, `children` 을 둡니다.

```json
{
  "data": [
    {
      "name": "ROOT",
      "level": 1,
      "parentName": "",
      "title": "분류 체계",
      "children": [
        { "name": "A", "level": 2, "parentName": "ROOT", "title": "대분류 A", "children": [] }
      ]
    }
  ]
}
```

## 결과 class

| class | 주요 메서드 | 내용 |
| --- | --- | --- |
| `MigrationItemResult` | `markInProgress()`, `markSuccess(id)`, `markFailure(error)`, `markSkipped(reason)` | 항목 하나의 상태. `PENDING` → `IN_PROGRESS` → `SUCCESS` / `FAILED` / `SKIPPED`. 성공이면 만든 객체 id 를 가짐 |
| `MigrationError` | `Builder().errorCode().errorMessage().severity().build()` | 오류 코드, 메시지, 항목, stack trace, 심각도 (`WARNING` 계속 가능 / `ERROR` 그 항목 실패 / `FATAL` 전체 중단) |
| `MigrationResult<T>` | `isOverallSuccess()`, `isFullySuccessful()`, `getFailedResults()`, `getAllErrors()`, `getStatistics()` | 전체 결과. `isFullySuccessful()` 은 전체 성공이면서 실패 항목 0 개. `getAllErrors()` 는 전체 오류(`GLOBAL_ERROR`, `FATAL`)와 항목 오류를 모음 |
| `MigrationStatistics` | `toString()` | `Total: 10, Success: 9 (90.0%), Failed: 1, Skipped: 0, Pending: 0, Duration: 1234ms` 와 오류 코드별 건수 |

`markSkipped` 는 오류 코드 `SKIPPED`, 심각도 `WARNING` 인 오류를 남기므로 `getAllErrors()` 에도 나옵니다. 건너뛴 항목은 실패로 세지 않아 `isFullySuccessful()` 에는 영향이 없습니다.

## 엑셀 입력

표 형태 이관은 엑셀을 읽어 같은 결과 class 로 처리합니다. 엑셀 이관 중 하나는 끝나면 **입력 엑셀의 정해진 열에 행마다 결과(성공 / 오류 메시지)를 다시 적습니다.** 시트 이름, 결과 열, 데이터 시작 행은 코드에 고정되어 있습니다. 입력 파일이 바뀌므로 복사본으로 실행합니다.

## 실행 bat

`resources/custom/migration/*.bat` 은 이관 종류마다 하나입니다. 하는 일은 같습니다.

| 순서 | 내용 |
| --- | --- |
| 1 | `chcp 65001`, `-Dfile.encoding=UTF-8` 로 한글 로그가 깨지지 않게 함 |
| 2 | 네 번째 인자(`development` 기본 / `production`)로 TomEE 와 웹앱 경로를 고름 |
| 3 | `TomEE/bin/setenv.bat` 을 불러 서버와 같은 환경 변수를 씀 |
| 4 | `WEB-INF/classes`, `WEB-INF/lib/*`, `TomEE/lib/*` 를 클래스패스로 잡음. 폴더가 없으면 종료 코드 1 |
| 5 | `java -Xmx1024m ... <App class> %1 "%2" %3` 실행 후 종료 코드를 그대로 돌려줌 |

::: danger 표 형태(엑셀) 이관은 abort 가 동작하지 않습니다
엑셀 이관 실행 class 4 개와 처리 class 4 개에는 살아 있는 트랜잭션 코드(`startTransaction`, `commitTransaction`, `abortTransaction`)가 없습니다 (하나는 주석 처리, 나머지는 처음부터 없음). 세 번째 인자를 `abort` 로 줘도 **처리하는 즉시 저장됩니다.** 종료 코드도 4 개 중 3 개는 결과와 상관없이 0 입니다. 이 bat 들은 `abort` 기본값 줄도 주석 처리되어 있습니다. 미리 돌려 보려면 테스트 서버에서 실행하거나 트랜잭션 코드를 되살립니다. `abort` 기본값과 "실패가 하나라도 있으면 되돌리기" 가 살아 있는 것은 계층 구조 이관(분류 체계) 하나입니다.
:::

::: warning 항목마다 트랜잭션을 다시 엽니다
계층 구조 이관은 실행 class 가 트랜잭션을 연 상태에서, 처리 class 가 항목마다 `ContextUtil.startTransaction` / `commitTransaction` 을 다시 부르고 실패하면 `abortTransaction` 을 부릅니다. 바깥의 `abort` 가 이미 만든 항목까지 되돌리는지, 항목 하나의 `abortTransaction` 이 바깥 트랜잭션까지 끝내는지는 `ContextUtil` 의 중첩 처리에 달려 있습니다 <Badge type="danger" text="검증 필요" />. 테스트 서버에서 `abort` 로 돌린 뒤 객체가 남는지 확인합니다.
:::
