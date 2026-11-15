# 다이얼로그 (Windows) <Badge type="info" text="원본 분석" />

`DS/Windows/Dialog` 로 제목, 내용, 아래쪽 버튼이 있는 창을 띄웁니다. `modalFlag: true` 면 뒤 화면을 어둡게 가리고 막습니다. 같은 모듈 묶음에 패널(`Panel`)과 창을 담는 틀(`ImmersiveFrame`)이 있습니다.

샘플: [02_dialog.html](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/ControlsSample/02_dialog.html) · [04_dialog_grid.html](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/ControlsSample/04_dialog_grid.html) (다이얼로그 안에 DataGridView)

::: info 근거
Windows.js 의 Window (publishedProperties, _applyButtons, getDefaultButtonLabel, close, keydown / keyup 처리), Dialog (_applyButtons, _applyCustomButtonsDefinition, _applyCheckboxInfos, _applyModalFlag, _getDefaultPosition), Panel, ImmersiveFrame. CoreBase.js 의 WUXStandardButtonEnum, WUXStandardButtonRoleEnum, WUXCustomButtonRoleEnum. Windows_ko.js 의 번역. 호출부: AuthGenericCommands (AuthGenericDlgProgressbar), DELWebCommonControls (FormV2), CAT3DAnnotationBaseCmds. `e.dsModel.dialog.close()` 111 건. OOTB 패턴: ENXMeetingMgmt (RemoveMeeting, CreateMeetingDialog), ENXDecisionMgmt (RemoveDecision), ENOUserGroupMgmt (RemoveUsersGroup), ENXHistoryUX (HistoryDefaultDialog), ENOCharacteristicsApp (NewDialog). 사용 파일 수(2025): `DS/Windows/Dialog` 148, `DS/Windows/ImmersiveFrame` 134, `DS/UIKIT/Modal` 99, `DS/UIKIT/SuperModal` 83, `DS/TransientWidget/TransientWidget` 54, `UWA/Controls/Overlay` 11, `UWA/Controls/Popup` 10, `UWA/Controls/Lightbox` 10 (UWA 셋은 대부분 UWA 묶음 파일 자신). UIKIT.js 의 Modal / SuperModal 의존 모듈, UWA2/js/Controls/Popup.js, Overlay.js, Lightbox.js
:::

## 무엇으로 띄울까

ENOVIA 에서 "창을 띄우는" 방법은 넷입니다. OOTB 앱의 새 화면은 대부분 `DS/Windows/Dialog` 를 씁니다.

| 방법 | 바탕 | OOTB 에서 쓰는 곳 | 문서 |
| --- | --- | --- | --- |
| `DS/Windows/Dialog` (+ `ImmersiveFrame`) | WebUX (`DS/Windows/Window`) | ENX* / ENO* 앱의 생성, 삭제 확인, 상세 보기 다이얼로그 | 이 페이지 |
| `DS/UIKIT/Modal`, `DS/UIKIT/SuperModal` | **UWA** (`UWA/Controls/Abstract`) | 예전 UIKIT 화면의 모달, `alert` / `confirm` / `prompt` | [UIKIT](/controls/uikit#supermodal) · [UIKIT API](/controls/api/uikit#modal) |
| `DS/TransientWidget/TransientWidget` | PlatformAPI 메시지 | 다른 앱(속성, 변경 조치, 프로필)을 창으로 열기 | [TransientWidget](/widget/transient-widget) |
| `UWA/Controls/Overlay`, `Lightbox` | UWA | OOTB 앱에서는 거의 쓰지 않음 (Coachmark, VCXWebComposer) | 아래 |

### UWA 로 다이얼로그를 띄우는가

**UWA 만으로 만든 다이얼로그 클래스는 OOTB 에서 쓰지 않습니다.** UWA 는 바탕으로만 쓰입니다.

- `DS/UIKIT/Modal`, `SuperModal` 은 `UWA/Controls/Abstract` 를 상속하고 `UWA/Element`, `UWA/Event` 로 화면을 만듭니다. "UWA 기반 모달" 을 쓰려면 이것을 씁니다.
- `DS/Windows/Dialog` 는 WebUX 클래스이고, UWA 는 `UWA/Core` (`UWA.createElement`) 정도만 씁니다. OOTB 호출부도 내용 요소를 `UWA.createElement` 로 만들어 `content` 에 넣습니다.
- `UWA/Controls/Popup` 은 다이얼로그가 아닙니다. `url`, `width`, `height`, `left`, `top`, `toolbar`, `menubar` 같은 옵션으로 **브라우저 새 창(`window.open`)** 을 엽니다.
- `UWA/Controls/Overlay` 는 부모 요소를 덮는 층(`exclusive`, `closeable`, `open()` / `close()` / `toggle()`), `UWA/Controls/Lightbox` 는 Overlay 에 `setContent()` / `addContent()` 를 더한 것입니다. 제목줄, 버튼, Enter / Esc 처리가 없어서 다이얼로그로 쓰려면 직접 만들어야 합니다.

## 모달 다이얼로그 열기

```js
require(['UWA/Core', 'DS/Windows/Dialog', 'DS/Controls/Button'], function (UWA, Dialog, Button) {
  var body = UWA.createElement('div', { styles: { padding: 12 }, text: '선택한 부품 3건을 승인합니다.' });

  var dlg = new Dialog({
    title: '승인',
    content: body,
    modalFlag: true,
    width: 400,
    buttons: {
      Ok: new Button({
        onClick: function (e) {
          console.log('승인 처리');
          e.dsModel.dialog.close();
        }
      }),
      Cancel: new Button({
        onClick: function (e) { e.dsModel.dialog.close(); }
      })
    }
  });

  dlg.addEventListener('close', function () { console.log('닫힘'); });
});
```

- **만들면 바로 보입니다.** `inject` 를 부르지 않아도 `document.body` 에 붙습니다 (`immersiveFrame` 을 주면 그 안에 붙음).
- 위치를 주지 않으면 화면(또는 ImmersiveFrame) 가운데에 놓입니다.
- `content` 는 요소, WebUX 컨트롤의 화면 요소, 문자열을 받습니다. 문자열은 HTML 로 해석되므로 사용자 입력은 요소의 `text` 로 넣습니다.

## 버튼 (buttons)

`buttons` 객체의 **키 이름이 버튼 종류** 입니다. 값에는 `DS/Controls/Button` 을 넣습니다.

| 키 | 기본 글자 (ko) | 역할 | Enter / Esc |
| --- | --- | --- | --- |
| `Ok` | 확인 | Accept | Enter |
| `Open` | 열기 | Accept | Enter |
| `Save` | 저장 | Accept | Enter |
| `SaveAll` | 모두 저장 | Accept | Enter |
| `Retry` | 재시도 | Accept | Enter |
| `Ignore` | 무시 | Accept | Enter |
| `Yes` / `YesToAll` | 예 / 모두 예 | Yes | Enter |
| `No` / `NoToAll` | 아니요 / 모두 아니요 | No | Esc |
| `Cancel` | 취소 | Reject | Esc |
| `Abort` | 중단 | Reject | Esc |
| `Close` | 닫기 | Reject | Esc |
| `Apply` | 적용 | Apply | - |
| `Reset` / `RestoreDefaults` | 재설정 / 기본값 복원 | Reset | - |
| `Discard` | 취소 | Destructive | - |
| `Help` | 도움말 | Help | - |

- 버튼에 `label` 이 없으면 위 글자가 들어갑니다. 원하는 글자는 `new Button({ label: '승인' })` 처럼 줍니다.
- Dialog 가 `emphasize` 를 정합니다. Accept / Yes 역할은 `primary`, 나머지는 `secondary` 입니다. 버튼에 준 `emphasize` 는 덮어써집니다.
- 표시 순서는 넣은 순서가 아니라 위 열거형 값 순서입니다 (`Ok` 가 항상 `Cancel` 앞).
- 역할별 첫 버튼이 Enter / Esc 키에 연결됩니다. 키를 누르면 그 버튼을 클릭한 것과 같습니다.
- 버튼마다 `dialog` 속성이 붙습니다. 핸들러에서 `e.dsModel.dialog` 로 다이얼로그를 얻습니다.
- 목록에 없는 키(`Delete` 등)는 `Error in Window : "Delete" is not a valid standard button` 을 콘솔에 찍고 무시됩니다.

::: warning 버튼을 눌러도 저절로 닫히지 않습니다
`Ok`, `Cancel` 을 포함해 모든 버튼은 클릭 이벤트만 냅니다. 닫으려면 핸들러에서 `e.dsModel.dialog.close()` 를 부릅니다. 버튼에 `onClick` 이 없으면 Esc 로 `Cancel` 을 눌러도 아무 일이 없습니다. 오른쪽 위 X 버튼만 스스로 `close()` 를 부릅니다.
:::

비활성으로 만들려면 `dlg.buttons.Ok.disabled = true` 처럼 버튼 속성을 바꿉니다.

### 표준 이름이 아닌 버튼

`buttons` 를 비우고 `customButtonsDefinition` 에 Button 옵션 배열을 줍니다. `role` 로 Enter / Esc 를 정합니다.

```js
var dlg = new Dialog({
  title: '변경 확인',
  content: body,
  modalFlag: true,
  customButtonsDefinition: [
    { label: '저장 후 닫기', emphasize: 'primary', role: 'validate', onClick: function (e) { saveAndClose(e.dsModel.dialog); } },
    { label: '저장 안 함', onClick: function (e) { e.dsModel.dialog.close(); } },
    { label: '돌아가기', role: 'escape', onClick: function (e) { e.dsModel.dialog.close(); } }
  ]
});
```

| `role` | 뜻 |
| --- | --- |
| `'validate'` (`WUXCustomButtonRoleEnum.Validate`) | Enter 키 |
| `'escape'` (`WUXCustomButtonRoleEnum.Escape`) | Esc 키 |

이 방식은 Dialog 가 `emphasize` 를 덮어쓰지 않습니다. 만들어진 버튼은 `dlg.getCustomButtons()` 로 얻습니다.

### 아래쪽 체크박스

```js
var dlg = new Dialog({
  title: '삭제',
  content: '선택한 문서를 삭제합니다.',
  modalFlag: true,
  checkboxInfos: { label: '다시 묻지 않음', checkFlag: false },
  buttons: { Ok: new Button({ onClick: onOk }), Cancel: new Button({ onClick: onCancel }) }
});

function onOk(e) {
  console.log('다시 묻지 않음', dlg.checkboxInfos.checkFlag);
  e.dsModel.dialog.close();
}
```

체크하면 `checkboxInfos.checkFlag` 가 바뀝니다.

<Badge type="warning" text="2026" /> `identifier` 와 `checkboxInfos.saveCheckStateInPreferences: true` 를 주면 체크 상태가 localStorage 에 저장되고, `Dialog.getCheckBoxInfoStateInPreferences(identifier)` 로 열기 전에 읽을 수 있습니다. 2026 은 `customButtonsDefinition` 항목에 `secondaryActions` 를 주면 분할 버튼이 됩니다. 예제는 [다시 묻지 않음 저장](/controls/window-layout#다시-묻지-않음-저장-2026) 에 있습니다.

## 닫기와 다시 열기

| 방법 | 결과 |
| --- | --- |
| `dlg.close()` | `close` 이벤트 → `autoCloseFlag` 가 `true` (기본) 면 `destroy()` |
| X 버튼 | `close()` 와 같음. `closeButtonFlag: false` 로 숨김 |
| `dlg.visibleFlag = false` | 숨기기만 함. `true` 로 다시 보임 |
| `dlg.destroy()` | `close` 이벤트 없이 정리 |

::: warning 닫은 다이얼로그는 다시 쓰지 않습니다
기본 설정에서는 `close()` 하면 `destroy()` 까지 되어 객체를 다시 쓸 수 없습니다. 같은 창을 여러 번 열려면 열 때마다 새로 만들거나, `autoCloseFlag: false` 로 만들고 `visibleFlag` 로 숨기고 보입니다.
:::

## 자주 쓰는 옵션

| 키 | 기본값 | 설명 |
| --- | --- | --- |
| <code>title</code> | <code>''</code> | 제목 |
| <code>content</code> | <code>null</code> | 내용 |
| <code>modalFlag</code> | <code>false</code> | 뒤 화면을 가리고 막음 |
| <code>buttons</code> | <code>{}</code> | 표준 버튼 |
| <code>width</code> / <code>height</code> | <code>-1</code> | 크기 (px). <code>-1</code> 이면 내용에 맞춤 |
| <code>minWidth</code> / <code>minHeight</code> | <code>150</code> / <code>0</code> | 최소 크기 |
| <code>resizableFlag</code> | <code>false</code> | 오른쪽 아래 끌어서 크기 조절 |
| <code>movableFlag</code> | <code>true</code> | 제목줄을 끌어서 이동 |
| <code>closeButtonFlag</code> | <code>true</code> | X 버튼 |
| <code>maximizeButtonFlag</code> | <code>false</code> | 최대화 버튼 |
| <code>autoCloseFlag</code> | <code>true</code> | <code>close()</code> 때 <code>destroy()</code> |
| <code>immersiveFrame</code> | <code>null</code> | 담을 ImmersiveFrame |
| <code>position</code> | - | <code>{ my: 'center', at: 'center', of: 요소, offsetX: 0, offsetY: 0 }</code> |
| <code>header</code> | <code>null</code> | 제목줄 아래 내용 위에 놓을 요소 |
| <code>titleBarButtons</code> <Badge type="warning" text="2026" /> | <code>Close</code> | 제목줄 버튼 (Close, Maximize, Settings, Information, Help 비트 조합) |

전체 목록은 [Windows API](/controls/api/windows) 에 있습니다.

::: warning className 은 없습니다
실제 코드에 `new Dialog({ className: 'progressBar_dialog', ... })` 가 있지만 Dialog / Window 에 `className` 속성이 없어 무시됩니다. 클래스는 `dlg.getContent().addClassName('...')` 로 붙입니다.
:::

## 이벤트

| 이벤트 | 시점 |
| --- | --- |
| <code>close</code> | <code>close()</code> 또는 X 버튼 (정리 전) |
| <code>visibleStateChange</code> | <code>visibleFlag</code> 변경 |
| <code>activate</code> / <code>deactivate</code> | 맨 앞 창이 됨 / 다른 창이 앞으로 옴 |
| <code>move</code> | 위치 변경 |
| <code>resize</code> | 크기 변경 (<code>width</code> / <code>height</code>, 최대화, 붙는 영역). 안의 컨트롤에도 전달됨 |
| <code>startResize</code> / <code>stopResize</code> | 사용자가 크기 조절 손잡이를 누름 / 놓음 (<code>e.options.side</code>) |
| <code>startWindowDrag</code> / <code>windowDrag</code> / <code>stopWindowDrag</code> | 제목줄 끌기 |
| <code>expand</code> / <code>collapse</code> | 펼침 / 접힘 |
| <code>settingsStateChange</code> / <code>informationStateChange</code> / <code>helpTrigger</code> <Badge type="warning" text="2026" /> | 제목줄 설정 / 정보 / 도움말 버튼 |

전체 이벤트(`visibilityChange`, `prePlaceChange` / `placeChange`, `contentVisibleStateChange` ...)는 [Windows API › Window](/controls/api/windows#window) 에 있습니다.

## 확인 / 취소 묻기

결과를 콜백 하나로 받는 함수로 감싸 두면 편합니다.

```js
function confirmDialog(title, message, callback) {
  var answered = false;
  var dlg = new Dialog({
    title: title,
    content: UWA.createElement('div', { styles: { padding: 12 }, text: message }),
    modalFlag: true,
    buttons: {
      Yes: new Button({ onClick: function (e) { answered = true; e.dsModel.dialog.close(); callback(true); } }),
      No: new Button({ onClick: function (e) { answered = true; e.dsModel.dialog.close(); callback(false); } })
    }
  });
  dlg.addEventListener('close', function () {
    if (!answered) { callback(false); }
  });
}

confirmDialog('삭제', 'PRT-0001 을 삭제할까요?', function (ok) { console.log(ok); });
```

X 버튼으로 닫힌 경우도 `close` 에서 처리합니다. UIKIT 에는 같은 일을 하는 `SuperModal.confirm` 이 있습니다 ([UIKIT](/controls/uikit#supermodal)).

## OOTB 앱의 다이얼로그 패턴

ENX* / ENO* 앱은 다이얼로그 하나를 모듈 하나(`DS/앱/View/Dialog/RemoveXxx` 처럼)로 만들고, 여는 함수 하나만 돌려줍니다. 삭제 확인 다이얼로그(ENXMeetingMgmt 의 RemoveMeeting)를 일반화하면 아래와 같습니다.

```js
define('DS/MyApp/View/Dialog/RemoveItems', [
  'UWA/Core',
  'DS/Windows/Dialog',
  'DS/Windows/ImmersiveFrame',
  'DS/Controls/Button',
  'i18n!DS/MyApp/assets/nls/MyApp'
], function (UWA, WUXDialog, WUXImmersiveFrame, WUXButton, NLS) {
  'use strict';

  var dialog;

  function open(items) {
    var deletable = items.filter(function (item) { return item.canDelete; });
    var list = UWA.createElement('ul');
    items.forEach(function (item) {
      UWA.createElement('li', { text: item.title }).inject(list);
    });

    var content = UWA.createElement('div');
    UWA.createElement('div', { text: NLS.removeWarning }).inject(content);
    list.inject(content);

    var immersiveFrame = new WUXImmersiveFrame();
    immersiveFrame.inject(document.body);

    dialog = new WUXDialog({
      modalFlag: true,
      width: 500,
      height: 200,
      title: NLS.removeHeader.replace('{count}', deletable.length),
      content: content,
      immersiveFrame: immersiveFrame,
      buttons: {
        Ok: new WUXButton({
          label: NLS.Delete,
          disabled: deletable.length < 1,
          onClick: function () { removeConfirmed(deletable); }
        }),
        Cancel: new WUXButton({
          onClick: function (e) { e.dsModel.dialog.close(); }
        })
      }
    });
  }

  function removeConfirmed(items) {
    console.log('삭제', items);
    dialog.close();
  }

  return { open: open };
});
```

- **ImmersiveFrame 을 `document.body` 에 붙이고 `immersiveFrame` 으로 넘깁니다.** 사용 횟수는 `inject(document.body)` 58, `inject(widget.body)` 17 입니다. 위젯은 자기 iframe 안에서 실행되므로 `document.body` 도 위젯 영역입니다.
- 버튼은 표준 키(`Ok`, `Cancel`)를 쓰고 `label` 만 바꿉니다 (`Ok` 에 `NLS.Delete`). 그래서 Enter / Esc 와 primary 강조가 그대로 붙습니다.
- 지울 수 있는 항목이 없으면 `disabled: true` 로 `Ok` 를 막고, 지울 수 없는 항목은 내용에 따로 보여줍니다.
- 0 건 선택은 다이얼로그를 띄우지 않고 알림 메시지(`level: 'warning'`)로 끝냅니다 ([알림 메시지](/controls/notifications)).
- 생성 다이얼로그(CreateMeetingDialog, NewDialog)도 같은 틀에 `content` 로 탭이나 폼을 넣습니다.

::: warning 열 때마다 만든 ImmersiveFrame 은 남습니다
위 패턴(RemoveMeeting)은 열 때마다 `new ImmersiveFrame()` 을 `document.body` 에 붙이고, 다이얼로그를 닫아도 틀은 남겨 둡니다. 따라 만들 때는 위젯에 틀 하나를 만들어 두고 모든 다이얼로그가 같이 씁니다 ([샘플 04](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/ControlsSample/04_dialog_grid.html) 가 이 방식).

ENOSubscriptionMgmt (ViewSubscriptionsDialog) 처럼 `close` 이벤트에서 `immersiveFrame.destroy()` 를 부르는 OOTB 도 있습니다. 다만 `close()` 는 `close` 이벤트를 낸 뒤 다이얼로그를 `destroy()` 하고, 틀의 `destroy()` 도 안의 창을 `destroy()` 하므로 다이얼로그가 두 번 정리됩니다. 문제가 없는지는 확인하지 않았습니다 <Badge type="danger" text="검증 필요" />.
:::

## 다이얼로그 안에 DataGridView 넣기 <Badge type="tip" text="로컬 실행 확인" />

고르기 창, 목록 보기 창처럼 다이얼로그 안에 그리드를 넣을 때입니다. OOTB 는 ENOSubscriptionMgmt 의 ViewSubscriptionsDialog (구독 목록 그리드 + 툴바) 가 이 형태입니다.

샘플: [04_dialog_grid.html](https://github.com/LeeOuHyoun/sampleCodesAndMemos/blob/master/ControlsSample/04_dialog_grid.html) (고르기 창, 찾기 칸 + 그리드)

<<< @/../ControlsSample/04_dialog_grid.html#pick{js}

내용 요소의 CSS 는 아래와 같습니다.

```css
.dlg-grid-body { position: relative; height: 100%; }
.dlg-grid-body .grid-area { position: absolute; top: 0; left: 0; right: 0; bottom: 0; }
```

- **Dialog 에 `height` 를 꼭 줍니다.** `height` 가 있으면 Dialog 가 내용 영역 높이를 `height - (제목줄 + 버튼 줄)` px 로 정합니다. 기본값 `-1` 이면 내용 영역이 `height: auto` 라서 그리드 높이가 0 이 되어 보이지 않습니다 (Window 의 `_updateContentHeight`).
- 내용 요소는 `position: relative; height: 100%`, 그리드는 그 안에 넣습니다. 그리드를 담는 요소에 `position` 이 없으면 그리드가 페이지 전체를 덮습니다 ([기본 그리드](/datagridview/getting-started), [자주 틀리는 점](/guide/faq)).
- `resizableFlag: true` 로 크기를 바꿔도 그리드가 따라 커집니다. 크기를 바꿀 때 Dialog 는 `resize` 이벤트를 냅니다.
- OOTB 는 크기를 위젯 화면 크기의 80% 로 정합니다: `width` 는 `widget.getViewportDimensions().width * 0.8` (800 이 넘으면 810), `height` 는 `widget.getViewportDimensions().height * 0.8`, `minWidth: 675`, `minHeight: 500`.
- 선택된 행은 `treeDocument.getSelectedNodes()` 로 읽고, 선택이 바뀔 때 `getXSO().onPostAdd` / `onPostRemove` 에서 `Ok` 버튼의 `disabled` 를 바꿉니다 ([우클릭 메뉴와 선택](/datagridview/context-menu#선택-변경-받기)).
- 닫을 때 `grid.destroy()` 로 그리드를 정리합니다.
- 그리드 위에 툴바나 찾기 칸을 두려면 그 줄의 높이를 정해 두고 그리드 영역을 `top` 으로 그만큼 내립니다. OOTB 는 그리드 요소에 `style.top = '40px'` 을 줍니다. 샘플의 `#filter` 범위가 이 형태입니다.

로컬 실행(원본 webapps 를 `npm run samples` 로 띄워 Edge 로 확인)으로 확인한 것: 600 × 400 Dialog 안에 그리드가 높이 335px 로 6행 모두 보임, 선택 전 `선택` 버튼 비활성, 행을 누르면 활성, 누르면 고른 행을 돌려받고 그리드가 정리됨, 찾기 칸에 입력하면 행이 걸러짐.

## ImmersiveFrame

다이얼로그와 패널을 담는 틀입니다. 위젯 영역 안에만 창이 뜨게 하거나, 패널을 좌우에 붙이려면 씁니다.

```js
require(['DS/Windows/ImmersiveFrame', 'DS/Windows/Dialog'], function (ImmersiveFrame, Dialog) {
  var frame = new ImmersiveFrame();
  frame.inject(widget.body);   // 부모는 position 이 있고 높이가 있어야 함

  new Dialog({ title: '속성', content: body, immersiveFrame: frame, modalFlag: true });
});
```

- 틀은 `position: absolute; top/left/right/bottom: 0` 으로 부모를 꽉 채웁니다. 부모에 `position: relative` 와 높이를 줍니다.
- `immersiveFrame` 을 준 다이얼로그는 틀의 가운데에 놓이고, 모달 가리개도 틀 안만 가립니다.
- 틀 뒤에 그리드 같은 본 화면을 깔려면 `frame.setContentInBackgroundLayer(el)` 을 씁니다.
- 화면을 닫을 때 `frame.destroy()` 하면 안에 있는 창도 정리됩니다 (`destroyAllWindows()` 는 창만 정리).

| 메서드 | 설명 |
| --- | --- |
| `setContentInBackgroundLayer(el)` | 배경 층에 내용 넣기 |
| `getFreeZone()` | 패널이 붙지 않은 가운데 영역 |
| `addWindow(win)` / `removeWindow(win)` | 창 넣기 / 빼기 (보통 `immersiveFrame` 옵션으로 자동) |
| `getDockingElement(area)` | 좌 / 우 / 위 / 아래 붙는 영역 |
| `dockWindow(win, area)` / `undockWindow(win)` | 창 붙이기 / 떼기 ([창 붙이기](/controls/window-layout#가장자리에-붙이기)) |
| `destroyAllWindows()` | 안의 창 모두 정리 |

이벤트: `addWindow`, `removeWindow`, `freeZoneResize`.

## Panel

`Window` 를 상속한 패널입니다. ImmersiveFrame 의 좌우 / 아래에 붙이거나 떠 있게 둡니다.

```js
var panel = new Panel({
  title: '속성',
  content: propertyEl,
  immersiveFrame: frame,
  currentDockArea: WUXDockAreaEnum.RightDockArea,
  width: 320
});
```

| 키 | 기본값 | 설명 |
| --- | --- | --- |
| <code>currentDockArea</code> | - | 붙는 위치. <code>WUXDockAreaEnum</code> 의 <code>NoneDockArea</code> (떠 있음), <code>LeftDockArea</code>, <code>RightDockArea</code>, <code>TopDockArea</code>, <code>BottomDockArea</code> |
| <code>allowedDockAreas</code> | - | 끌어서 붙일 수 있는 위치 (비트 합) |
| <code>collapsibleFlag</code> | <code>true</code> | 접기 버튼 |
| <code>floatableFlag</code> | <code>true</code> | 떼어서 띄우기 |
| <code>pinnedFlag</code> | <code>false</code> | 고정 |
| <code>showTitleFlag</code> | <code>true</code> | 제목 표시 |

`WUXDockAreaEnum`, `WUXStandardButtonEnum` 같은 열거형은 `DS/Windows/*` 를 불러오면 전역(`window`)에 생깁니다.

Panel 을 탭으로 묶기, `dockWindow` 로 붙이기, `identifier` 로 위치 저장, 떠 있는 도구 창(ImmersiveWindow)은 [창 묶기, 붙이기, 위치 저장](/controls/window-layout) 에 있습니다. 창이 아닌 분할 레이아웃(왼쪽 | 가운데 | 오른쪽)은 [분할 레이아웃 (DockingElement)](/controls/docking-element) 를 봅니다.
