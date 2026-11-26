# objectInfoPanel 사용법 (객체 정보 패널)

physicalid 하나만 넘기면 화면 오른쪽에 ENOVIA 의 정보 패널을 띄워 주는 모듈입니다. 패널 자체는 OOTB 의 `EditPropWidget` 이고, 이 모듈은 그것을 Triptych 오른쪽 칸에 붙이고 여닫는 처리를 대신합니다.

- 탭은 속성, 첨부, Relations, 코멘트, 이력 다섯 개입니다.
- 파일은 `objectInfoPanel.js` 와 `objectInfoPanel.css` 두 개입니다. 실제로 쓰는 예는 같은 저장소의 `Recipes/seedWidgetLayout` 에 있습니다.
- enovia 2025, 2026 원본 webapps 를 PC 에서 띄워 동작을 확인했습니다. 3DSpace 응답은 가짜로 주었고 실제 서버에서는 확인하지 못했습니다 ([확인 상태](#6-확인-상태)).

## 한눈에 보기

| 하고 싶은 일 | 방법 | 절 |
| --- | --- | --- |
| 화면 틀 (Triptych) 을 만들면서 정보 패널까지 붙이기 | `objectInfoPanel.initTriptych(triptych, options, left, main)` | 3.1 |
| 객체 정보를 패널에 띄우기 | `objectInfoPanel.show(physicalId)` | 3.2 |
| 이미 만든 Triptych 에 패널만 붙이기 | `objectInfoPanel.create({ modelEvents, container })` | 3.3 |
| 툴바의 i 버튼으로 패널 여닫기 | `PageToolBar` 에 같은 `modelEvents` 와 `withInformationButton: true` | 2 |
| 코드로 패널 여닫기 | `modelEvents.publish({ event: 'information-panel-open' })` | 4.2 |
| 탭 구성 바꾸기 | `objectInfoPanel.js` 의 `FACETS` | 4.1 |

## 1. 넣는 곳

두 파일을 `<3DSpace>/webapps/Recipes/objectInfoPanel/` 에 넣습니다. 모듈 이름은 `DS/Recipes/objectInfoPanel/objectInfoPanel` 입니다.

| 이 모듈이 불러오는 모듈 | 비고 |
| --- | --- |
| `DS/lodash-4.18.1/lodash` | lodash. 원본 webapps 에는 없습니다. 프로젝트에 넣어 둔 lodash 의 모듈 이름으로 맞춥니다 |
| `DS/EditPropWidget/EditPropWidget`, `DS/ENOXAttachmentsUX/AttachmentsPane` 등 | 원본 모듈 |

- 다른 폴더에 두면 `objectInfoPanel.js` 안의 모듈 이름 세 곳 (`define` 두 개의 이름, `FACETS` 의 `require`) 과 `css!` 경로를 폴더에 맞게 바꿉니다.
- 위젯 안에서 써야 합니다. 전역 `widget` 의 `x3dPlatformId` 값을 읽습니다.

## 2. 가장 짧은 예

그리드의 "정보 보기" 를 누르면 그 행의 객체 정보가 오른쪽 패널에 뜨는 화면입니다. `initTriptych` 한 번, `show` 한 번이 전부입니다.

```js
define('DS/MyApp/PartList', [
  'UWA/Core',
  'DS/CoreEvents/ModelEvents',
  'DS/ENOXTriptych/js/ENOXTriptych',
  'DS/ENOXPageToolBar/js/PageToolBar',
  'DS/DataGridView/DataGridView',
  'DS/TreeModel/TreeDocument',
  'DS/TreeModel/TreeNodeModel',
  'DS/Recipes/objectInfoPanel/objectInfoPanel',
  'UWA/Element'
], (UWA, ModelEvents, ENOXTriptych, PageToolBar, DataGridView, TreeDocument, TreeNodeModel, objectInfoPanel) => {
  'use strict';

  const ROWS = [
    { label: 'PRT-0001', grid: { title: '브라켓', physicalid: 'AAAA1111AAAA1111AAAA1111AAAA1111' } },
    { label: 'PRT-0002', grid: { title: '샤프트', physicalid: 'BBBB2222BBBB2222BBBB2222BBBB2222' } }
  ];

  const render = (parent) => {
    const modelEvents = new ModelEvents();
    const frame = UWA.createElement('div', { styles: { position: 'relative', width: '100%', height: '100%' } }).inject(parent);
    const main = UWA.createElement('div', { styles: { display: 'flex', flexDirection: 'column', height: '100%' } });
    const toolbarHost = UWA.createElement('div', { styles: { flex: 'none', height: '40px' } }).inject(main);
    const gridHost = UWA.createElement('div', { styles: { position: 'relative', flex: '1 1 0' } }).inject(main);

    objectInfoPanel.initTriptych(new ENOXTriptych(), { modelEvents, container: frame, withtransition: true }, null, main);

    new PageToolBar({ modelEvents, withInformationButton: true }).render().inject(toolbarHost);

    const treeDocument = new TreeDocument();
    treeDocument.prepareUpdate();
    ROWS.forEach((row) => treeDocument.addRoot(new TreeNodeModel(row)));
    treeDocument.pushUpdate();

    new DataGridView({
      treeDocument,
      columns: [
        { text: '번호', dataIndex: 'tree', width: 150 },
        { text: '이름', dataIndex: 'title', width: 150 },
        {
          text: '정보',
          dataIndex: 'info',
          width: 120,
          typeRepresentation: 'url',
          getCellValue: () => ({
            label: '정보 보기',
            func: ({ context }) => objectInfoPanel.show(context.nodeModel.getAttributeValue('physicalid'))
          })
        }
      ]
    }).inject(gridHost);
  };

  return { render };
});
```

위젯 html 에서는 `onLoad` 때 `render(widget.body)` 를 부릅니다 (`Recipes/seedWidgetLayout/seedWidgetLayout.html` 과 같은 방식).

- `modelEvents` 하나를 Triptych, 툴바, 정보 패널이 함께 씁니다. 툴바에 다른 `modelEvents` 를 주면 i 버튼이 패널과 연결되지 않습니다.
- 그리드가 아니어도 됩니다. 버튼의 클릭 처리에서 `objectInfoPanel.show(physicalId)` 만 부르면 됩니다.

## 3. 함수

### 3.1 initTriptych(triptych, options, left, main, right)

`triptych.init(options, left, main, right)` 를 부르던 자리에 그대로 바꿔 넣습니다. Triptych 를 초기화하고 오른쪽 칸에 정보 패널을 붙입니다.

| 넘기는 값 | 내용 |
| --- | --- |
| `triptych` | `new ENOXTriptych()` 로 만든 것. 아직 `init` 하지 않은 상태 |
| `options` | `ENOXTriptych` 의 `init` 옵션 그대로 (`container`, `left`, `right`, `withtransition`, `borderLeft` ...) |
| `options.modelEvents` | 툴바와 함께 쓸 `ModelEvents`. 안 넘기면 안에서 새로 만들지만 밖에서 받을 방법이 없으므로 넘기는 쪽을 권합니다 |
| `left`, `main` | 왼쪽 칸, 가운데 칸에 넣을 요소. 왼쪽 칸이 없으면 `null` |
| `right` | 생략할 수 있습니다. 생략하면 높이 100% 인 `div` 를 만들어 오른쪽 칸에 넣고 그 안에 패널을 붙입니다 |

- 돌려받는 값: 넘긴 `triptych`.
- `options.right` 를 안 주면 너비 400, 최소 너비 250 으로 엽니다. 준 값이 있으면 그 값이 우선합니다.

```js
objectInfoPanel.initTriptych(new ENOXTriptych(), {
  modelEvents,
  container: frame,
  withtransition: true,
  right: { originalSize: 500 }
}, null, main);
```

### 3.2 show(physicalId)

그 객체를 패널에 넣고 오른쪽 칸을 엽니다.

| 넘기는 값 | 내용 |
| --- | --- |
| `physicalId` | 비즈니스 객체의 physicalid 문자열. 관계 (connection) 의 id 는 받지 않습니다 |

- 부를 때마다 속성 탭부터 보여 줍니다. 다른 탭을 보던 중이어도 속성 탭으로 돌아옵니다.
- 패널이 이미 열려 있으면 내용만 바뀝니다.
- `initTriptych` 나 `create` 를 부르기 전이면 아무 일도 하지 않습니다.
- 패널은 조금 늦게 만들어집니다 ([4.4](#44-보안-컨텍스트)). `show` 는 패널이 준비될 때까지 기다렸다가 실행하므로 `initTriptych` 바로 다음 줄에서 불러도 됩니다. 돌려받는 값은 그때 끝나는 `Promise` 입니다.

### 3.3 create({ modelEvents, container })

Triptych 를 이미 직접 만들어 쓰는 화면에서 패널만 붙일 때 씁니다. `initTriptych` 가 안에서 부르는 함수입니다.

| 넘기는 값 | 내용 |
| --- | --- |
| `modelEvents` | Triptych 와 툴바가 쓰는 `ModelEvents` |
| `container` | 패널을 넣을 요소. Triptych 의 오른쪽 칸에 넣은 요소 |

```js
const right = UWA.createElement('div', { styles: { height: '100%' } });
triptych.init({ modelEvents, container: frame, withtransition: true }, null, main, right);
objectInfoPanel.create({ modelEvents, container: right });
```

- 돌려받는 값은 없습니다.
- 모듈은 마지막에 만든 패널 하나만 기억합니다. `show` 는 그 패널에 띄웁니다.

## 4. 동작

### 4.1 탭

| 탭 | 내용 | 보이는 조건 |
| --- | --- | --- |
| 속성 | 속성 보기와 편집 (연필 버튼) | 항상 |
| 첨부 | 그 객체에 `Reference Document` 관계로 붙은 문서 목록. 업로드, 기존 문서 붙이기 버튼 | 플랫폼에 3DSpace 서비스가 있으면 항상 |
| Relations | 연결된 객체 | 서버가 그 객체에 허용할 때 |
| 코멘트 | 3DSwym 코멘트 | 서버가 허용하고 플랫폼에 `comment` 서비스가 있을 때 |
| 이력 | 객체 history | 서버가 허용하고 3DSpace 서비스가 있을 때 |

- 첨부를 뺀 네 탭은 OOTB 에 들어 있는 탭입니다. 첨부 탭은 OOTB 의 `DS/ENOXAttachmentsUX/AttachmentsPane` 을 이 모듈이 탭으로 감싼 것입니다 (`objectInfoPanel.js` 앞부분의 `AttachmentsFacet`).
- 속성 탭은 `show` 때, 나머지 탭은 그 탭을 열 때 내용을 불러옵니다.
- 탭을 빼거나 더하려면 `objectInfoPanel.js` 의 `FACETS` 를 고칩니다. 넣을 수 있는 이름은 `DS/EditPropWidget/constants/EditPropConstants` 의 `FACET_...` 상수입니다. 속성과 첨부를 뺀 탭은 이름을 넣어도 서버가 그 객체에 허용해야 보입니다.

### 4.2 여닫기

툴바 (`PageToolBar`) 의 i 버튼과 Triptych 사이를 이 모듈이 이어 줍니다. 같은 이벤트를 직접 보내면 코드로도 여닫을 수 있습니다.

| 이벤트 | 뜻 | 누가 보내나 |
| --- | --- | --- |
| `information-panel-open` | 패널 열기 | i 버튼, `show`, 직접 |
| `information-panel-close` | 패널 닫기 | i 버튼, 직접 |
| `information-panel-visible` | 패널이 열렸음 | 이 모듈 (i 버튼이 눌린 모양으로 바뀜) |
| `information-panel-hidden` | 패널이 닫혔음 | 이 모듈 (i 버튼이 풀림) |

```js
modelEvents.publish({ event: 'information-panel-open' });
modelEvents.publish({ event: 'information-panel-close' });
modelEvents.subscribe({ event: 'information-panel-hidden' }, () => console.log('닫힘'));
```

- 객체를 고르기 전에 i 버튼으로 열면 "아이템을 선택하지 않음" 이 보입니다.

### 4.3 닫기 버튼

패널 오른쪽 위의 X 는 Triptych 가 그리는 버튼입니다. 객체를 고르기 전의 빈 패널에도 보입니다. OOTB 의 `ENOXEngineer` 와 같은 구성입니다.

- X 는 패널 위에 겹쳐 그려지므로, 그 아래에 놓이는 머리글의 접기 버튼을 왼쪽으로 25px 비켜 둡니다. `objectInfoPanel.css` 의 규칙 하나가 그 일을 합니다 (`ENOXEngineer` 의 `ENOXProperties.css` 와 같은 값).
- X 를 없애려면 `options.right` 에 `withClose: false` 를 줍니다. 그러면 비켜 두는 규칙도 적용되지 않습니다. 이때 닫는 방법은 i 버튼뿐입니다.

### 4.4 보안 컨텍스트

패널을 만들기 전에 사용자의 기본 보안 컨텍스트를 한 번 받아 `EditPropWidget` 에 넘깁니다. 속성, 첨부를 비롯한 패널의 요청이 모두 그 값으로 나갑니다.

- 받는 곳은 `SecurityContextServices.getSecurityContext` (3DSpace 의 `/resources/pno/person/getsecuritycontext`) 입니다.
- 이 조회가 끝난 뒤에 패널이 만들어지므로 `initTriptych` 직후에는 오른쪽 칸이 잠깐 비어 있습니다.
- 조회에 실패하면 값을 넘기지 않고 패널을 만듭니다. 이때는 `EditPropWidget` 의 기본 동작대로 사용자에게 할당된 첫 번째 자격을 씁니다.
- 위젯에 자격을 고르는 설정이 따로 있다면 그 값을 넘기도록 `objectInfoPanel.js` 의 `getSecurityContext` 를 바꿉니다. `{ SecurityContext: 'ctx::역할.조직.협업공간' }` 모양으로 끝나는 `Promise` 를 돌려주면 됩니다.

## 5. 주의

- 패널에서 속성을 편집할 수 있습니다 (`readOnly: false`). 보기만 하게 하려면 `objectInfoPanel.js` 의 `readOnly` 를 `true` 로 바꿉니다.
- 화면을 다시 그리면서 `initTriptych` 를 다시 부르면 새 패널이 만들어지고 `show` 는 새 패널에 띄웁니다. 앞의 패널은 지우지 않으므로 앞의 화면 요소는 부르는 쪽에서 비웁니다.
- 첨부 탭이 쓰는 플랫폼 주소와 보안 컨텍스트는 페이지에서 한 번만 받습니다. 처음 받을 때 실패하면 새로 고칠 때까지 첨부 탭이 빈 채로 남습니다.
- 탭 이름 "첨부" 는 한글로 고정되어 있습니다. 나머지 탭 이름은 OOTB 번역을 따릅니다.

## 6. 확인 상태

enovia 2025, 2026 원본 webapps 를 PC 에서 띄우고, 플랫폼 정보와 3DSpace 응답을 가짜로 주어 실제 OOTB 코드를 실행했습니다.

| 확인한 것 | 결과 |
| --- | --- |
| 2 절 예제, 3 절과 4.2 절의 코드 | 그대로 실행됨 |
| `show` | 오른쪽 칸이 열리고 속성 탭이 선택됨. 다른 탭을 보다가 불러도 속성 탭으로 돌아옴 |
| 첨부 탭 | 탭을 열 때 그 객체의 문서 조회 요청이 나감. 객체를 바꾸면 새 객체로 다시 조회 |
| i 버튼, X | 여닫힘과 i 버튼의 눌린 모양이 맞게 따라감. 빈 패널에서도 X 로 닫힘 |
| X 와 접기 버튼 | 겹치지 않음 (10px 떨어짐) |
| 보안 컨텍스트 | 속성, 첨부, 이력 요청이 모두 기본 보안 컨텍스트로 나감. 조회 실패 때는 첫 번째 할당 자격으로 나감 |
| 화면을 다시 그린 뒤 | 새 패널에서 속성, 첨부 탭이 정상 동작 |

실제 서버에서 확인이 필요한 것:

- 객체 타입마다 Relations, 코멘트, 이력 탭이 실제로 보이는지와 그 내용.
- 첨부 탭의 업로드, 내려받기, 문서 붙이기.
- 자격이 여러 개인 사용자에게 기본 보안 컨텍스트가 기대한 값으로 오는지.
- 3DDashboard 안에서의 동작. 확인은 위젯을 단독으로 띄운 화면에서만 했습니다.

## 7. 근거 (원본 위치)

| 내용 | 원본 |
| --- | --- |
| 패널 옵션, 객체 넘기기 (`initDatas`), 탭 정의 | `EditPropWidget/EditPropWidget.js` 의 `EditPropWidget`, `EditPropMainController`, `IDCardSkeleton`, `ConstantsMapping` |
| 같은 구성의 OOTB 화면 | `ENOXEngineer` 의 `PropertiesPanel` (패널 옵션, `alwaysVisible` 탭 추가, 이벤트 연결), `ENORouteMgmt` 의 `ContentInfoPropWidget` |
| 닫기 버튼과 25px | `ENOXTriptych/ENOXTriptych.js` 의 `withClose`, `ENOXEngineer/components/PropertiesView/ENOXProperties.css` |
| i 버튼 이벤트 | `ENOXPageToolBar/ENOXPageToolBar.js` |
| 첨부 탭 | `ENOXAttachmentsUX/ENOXAttachmentsUX.js` 의 `AttachmentsPane`, `AttachmentsFacet` |
| 보안 컨텍스트 | `WidgetServices/WidgetServices.js` 의 `SecurityContextServices` |
