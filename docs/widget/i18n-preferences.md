# 언어와 사용자 환경설정 (I18n, MePreferences) <Badge type="info" text="원본 분석" />

위젯에서 지금 언어를 얻는 방법, 날짜와 숫자를 사용자 언어로 쓰는 방법, 사용자가 3DDashboard 의 "Me" 메뉴에서 정한 환경설정(Me preferences)을 읽는 방법을 정리합니다. 번역 문자열 파일(`i18n!`)은 [플러그인](/amd/plugins), REST 요청의 언어 / 시간대 값은 [REST API](/rest-api/), 위젯 자신의 설정 값은 [위젯 설정](/widget/preferences) 을 봅니다.

::: info 근거
UWPClientCode/UWPClientCode.js 의 `DS/UWPClientCode/I18n`, AmdLoader.js 의 `i18n` 플러그인 (`getLocale`, `getSupportedLanguages`), UWA2/js/Utils/Client.js (`Locale`), Widget.js (`lang`), Core/Core.js (`WUX.currentLanguage`, `currentFormatsLanguage`), Utilities.js 의 `DS/Utilities/DateUtils`, `Utils.getDecimalSeparator`, MePreferencesClientAPI/MePreferencesClientAPI.js, MePreferencesClientUtilities/*.js, MePreferencesClientCacheMgr/*.js, MePreferencesUIElements/MePreferencesModalDialog.js, WebappsUtils.js 의 `DS/WebappsUtils/BetaOptions`, TransientWidget/Controller/Transient.js, WebSystem/WebSystem.js 의 `DS/WebSystem/Nls`. 2026: Utilities.js 의 `DS/Utilities/LanguageUtils`, `NumberUtils`, `DateUtils`. 앱 수 (모듈 문자열이 나오는 폴더): I18n 22, Nls 21, MePreferencesClientAPI 14. 호출 수 (의존성 위치와 인자 이름을 맞춰 셈): I18n `replace` 44, `getCurrentLanguage` 42, `setCurrentLanguage` 2, `getSupportedLanguages` 1. 이 모듈에는 `get`, `compose`, `bind` 가 없으므로 문자열 집계의 `I18n.get(` 등은 다른 I18n 객체입니다. MePreferencesClientAPI `readPreferences` 8, `getMePreferencesDialog` 5 (문자열 `readPreferences(` 는 40 앱. 3D 앱은 CATWebUXPreferences 같은 감싼 모듈로 부름). 호출부: CAT3DWInfra, CfgBaseUX, EffectivityWebExternalJS, ENORouteMgmt, PIMwebPropertiesTabs, DELWebOERCommonUtils, ENO3DUtils, VisuMePreferences, ENOXPackageCommonUXInfra (2026)
:::

## 한눈에

| 하고 싶은 일 | 쓸 것 | 절 |
| --- | --- | --- |
| 지금 언어 (앞 2 글자) | `widget.lang` | [지금 언어 얻기](#지금-언어-얻기) |
| NLS 파일과 같은 언어 이름 (`zh-TW`) | `I18n.getCurrentLanguage()` | [DS/UWPClientCode/I18n](#ds-uwpclientcode-i18n) |
| 날짜, 숫자를 사용자 형식으로 | 2025 `Intl` + `widget.lang`, 2026 `DateUtils.formatDateToString` | [날짜와 숫자 쓰기](#날짜와-숫자-쓰기) |
| Me 설정 값 읽기 | `MePreferencesClientAPI.readPreferences` | [Me 환경설정 읽기](#me-환경설정-읽기) |
| Me 설정이 바뀌면 다시 그리기 | `PlatformAPI.subscribe('com.ds.mep:onPrefUpdate', ...)` | [값이 바뀔 때](#값이-바뀔-때) |
| 내 앱의 Me 설정 창 열기 | `MePreferencesClientAPI.getMePreferencesDialog` | [Me 설정 창 열기](#me-설정-창-열기) |

## 지금 언어 얻기

| 방법 | 값 예 | 정하는 곳 | 사용 |
| --- | --- | --- | --- |
| `widget.lang` | `'ko'`, `'zh'` | 위젯을 만들 때 UWA 가 브라우저 첫 언어의 앞부분을 넣음 (`Client.Locale.lang`) | 700 건 / 175 앱 |
| `WidgetServices.getLanguage()` | `'ko'` | `COMPASS_CONFIG.lang` → `widget.lang` → `widget.getValue('lang')` → 최상위 창 `dsLang` → `'en'` ([WidgetServices](/widget/widget-services)) | |
| `I18n.getCurrentLanguage()` | `'ko'`, `'zh-TW'`, `'pt-BR'` | `i18n!` 플러그인이 정한 언어. 로더 설정 `i18n.locale` → 브라우저 언어 → `'en'` ([언어 정하기](/amd/plugins#언어-정하기)) | 42 건 / 12 앱 |
| `nls._lang` | `I18n` 과 같음 | `i18n!` 로 읽은 객체. 언어별 묶음으로 읽었을 때만 값이 있음 ([돌려받는 객체](/amd/plugins#돌려받는-객체)) | |
| `WUX.currentLanguage` | `'ko'` | `DS/Core/Core` 를 읽을 때 `widget.lang` → `navigator.language` | 컨트롤 내부 |
| `window.dsLang` | `'ko'` | 앱이 직접 넣는 전역 (CAT3DWInfra, BksStandalone, MPFClickAndBuyComponent) | 29 앱 (읽기 포함) |
| `swymlang` 쿠키 | `'ko'` | Me 메뉴에서 언어를 바꿀 때 `I18n.setCurrentLanguage` 가 씀 | 22 앱 (ENORouteMgmt 등이 날짜 형식에 씀) |
| `LanguageUtils.language` <Badge type="warning" text="2026" /> | `'ko'` | `widget.lang` → `dsLang` → `navigator.language` 의 앞부분 → `'en'` | 컨트롤 내부 |

- `widget.lang`, `LanguageUtils.language` 는 앞부분만입니다 (`zh-TW` → `zh`). NLS 파일 이름(`_zh-TW.js`, `_pt-BR.js`)이나 언어 목록과 맞출 때는 `I18n.getCurrentLanguage()` 를 씁니다.
- UWA 는 `widget.lang` 을 브라우저 언어로 채웁니다. 3DDashboard 가 위젯 프레임에 Me 메뉴의 언어를 넣어 주는지는 webapps 원본에 없는 대시보드 쪽 일이라 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.
- NLS 언어를 위젯 언어에 맞추려면 첫 `i18n!` 전에 `require.config({ config: { i18n: { locale: widget.lang } } })` 를 부릅니다 (CAT3DWInfra 등 7 앱).

위젯이 없는 단독 페이지까지 고려한 OOTB 형태입니다 (CfgBaseUX 는 이 값을 `Accept-Language` 헤더로 보냄, EffectivityWebExternalJS 는 `window.widget.lang` 이 없으면 이 값으로 채움).

```js
require(['DS/UWPClientCode/I18n'], function (I18n) {
  const lang = window.widget && widget.lang ? widget.lang : I18n.getCurrentLanguage();
});
```

## DS/UWPClientCode/I18n

```js
require(['DS/UWPClientCode/I18n'], function (I18n) {
  const lang = I18n.getCurrentLanguage();
  const korean = I18n.getSupportedLanguages().ko;
  const text = I18n.replace('{count} items', { count: 3 });
});
```

| 함수 | 호출 | 설명 |
| --- | --- | --- |
| `getCurrentLanguage()` | 42 | 지금 언어. 모듈을 읽을 때 `i18n!` 플러그인의 언어로 정함. 지원하지 않는 언어면 `'en'` |
| `replace(문자열, params)` | 44 | `{이름}` 을 `params.이름` 으로 바꿈. 없는 이름은 `''`. `nls.replace` 와 같은 규칙. Swym 계열이 씀 |
| `getSupportedLanguages()` | 1 | `{ ko: { shortName: 'korean', staticName: '한국어', localizedName: '...' }, ... }` 14 개 언어. `localizedName` 은 지금 언어로 쓴 이름 |
| `getDebugLanguages()` | 0 | `devkey`, `devtrace`, `devsymbol`. `UWA.debug` 일 때만 지원 언어에 합쳐짐 |
| `setCurrentLanguage(lang, 다시읽지않기?)` | 2 | 쿠키 `swymlang` 을 씀 (30 일, 상위 도메인). Me 설정 창 전용 |

::: warning setCurrentLanguage 는 위젯에서 부르지 않습니다
Me 설정 창이 언어를 저장할 때 쓰는 함수입니다. 2025 는 언어가 바뀌면 `window.location.reload()` 로 부른 창을 다시 읽습니다. 2026 은 쿠키만 쓰고, Me 설정 창이 "새로 고침" 확인 창을 띄웁니다. 2025 에서 위젯 안에서 부르면 위젯 프레임만 다시 읽힙니다. 어느 버전이든 사용자 계정의 언어(Passport 프로필)는 바뀌지 않고, 같은 도메인의 다른 앱이 읽는 `swymlang` 쿠키만 바뀝니다.
:::

## 날짜와 숫자 쓰기

### 2025 <Badge type="tip" text="2025" />

OOTB 는 대부분 인자 없이 `toLocaleString()`, `toLocaleDateString()`, `toLocaleTimeString()` 을 부릅니다 (215 건 / 58 앱, 숫자 포함). 브라우저 언어와 PC 시간대로 나옵니다. 언어를 넘기는 곳은 적습니다 (`toLocale...String(widget.lang` 8 건, `Intl.DateTimeFormat(widget.lang` 5 건, ENORouteMgmt 의 `swymlang` 쿠키 값).

위젯 언어에 맞추려면 `Intl` 에 `widget.lang` 을 넘깁니다.

```js
const dateFormat = new Intl.DateTimeFormat(widget.lang, {
  year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
});
cell.textContent = dateFormat.format(new Date(modified));
const count = new Intl.NumberFormat(widget.lang).format(12345.6);
```

WUX 컨트롤(날짜 입력, 그리드의 날짜 필터 등)은 `WUX.currentFormatsLanguage` 로 형식을 정합니다. 처음 값은 `widget.lang` (없으면 브라우저 언어)이고, 값을 넣으면 `WUX.onFormatsLanguageChange(fn)` 에 등록한 함수가 불립니다 (Controls, Tweakers).

`DS/Utilities/DateUtils.convertToString(date, options)` 도 같은 언어를 씁니다. OOTB 외부 호출은 없습니다.

| 옵션 | 값 | 기본 |
| --- | --- | --- |
| `locale` | 언어 | `WUX.currentFormatsLanguage` → `navigator.language` |
| `dateStyle` | `'short'` (숫자), `'medium'` (월 약어), `'long'`, `'full'` (요일 포함), `'fullShortDay'` | `'medium'` |
| `timePrecision` | 값이 있으면 시:분, `'sec'` 면 초까지 | 없음 (날짜만) |
| `timeStyle` | `'23h'` 면 24 시간제 | 지역 기본 |
| `display` | `'simplified'` 면 올해 날짜는 연도를 뺌 | |

숫자는 `DS/Utilities/Utils.getDecimalSeparator(locale?)` (소수점 문자, `(1.1).toLocaleString(locale)` 의 둘째 글자)와 `getStringFromNumber(n, locale?)` (소수점만 바꿈, 천 단위 구분 없음)가 있습니다.

### 2026 <Badge type="warning" text="2026" />

Me 설정에 "지역 형식 언어"(`RegionalFormatLanguage`)와 "ISO 8601 날짜 / 시간"(`UseISO8601ForDateAndTime`)이 생기고, 이 값을 `DS/Utilities/LanguageUtils` 가 들고 있습니다. 날짜는 `DateUtils.formatDateToString` 으로 씁니다 (105 건 / 46 앱).

```js
require(['DS/Utilities/DateUtils', 'DS/Utilities/NumberUtils'], function (DateUtils, NumberUtils) {
  const modifiedText = DateUtils.formatDateToString(new Date(modified), { timePrecision: 'min' });
  const weightText = NumberUtils.formatNumberToString(12345.678, { decimals: 2, thousandsSeparator: true });
});
```

| `LanguageUtils` | 설명 |
| --- | --- |
| `language` | 화면 언어 앞부분 (위 표) |
| `regionalFormatLanguage` | 날짜, 숫자 형식 언어 (`'ko-KR'` 처럼 전체). 3DDashboard 최상위 창의 `WUX.regionalFormatLanguage` → 브라우저 언어 → `'en'`. 모듈을 읽으면 뒤에서 Me 설정 `Languages / RegionalFormatLanguage` 를 읽어 바꿈 |
| `useISO8601ForDateAndTime` | Me 설정 `Languages / UseISO8601ForDateAndTime` 이 `'true'` 면 `true` |
| `subscribe(이벤트, fn)` / `unsubscribe(이벤트, fn)` | `'onLanguageChange'`, `'onRegionalFormatLanguageChange'`, `'onUseISO8601ForDateAndTimeChange'`. `fn({ oldValue, newValue })` |

- 지원 언어(앞부분)는 en, fr, de, es, cs, it, ja, ko, pl, pt, ru, zh, tr 입니다. 그 밖의 값은 무시합니다.
- localStorage `deactiveRegionalFormatLanguage` 가 `'true'` 면 지역 형식을 쓰지 않고 `widget.lang` → `dsLang` → 브라우저 언어를 씁니다.
- 전역 `WUX.regionalFormatLanguage`, `WUX.useISO8601ForDateAndTime` 는 이 값을 그대로 읽고 씁니다.

`DateUtils.formatDateToString(date, options)`:

| 옵션 | 값 | 기본 |
| --- | --- | --- |
| `locale` | 언어 또는 `'ISO8601'` | `useISO8601ForDateAndTime` 이면 `'ISO8601'`, 아니면 `regionalFormatLanguage` |
| `dateStyle` | 2025 `convertToString` 과 같음 | `'medium'` |
| `datePrecision` | `'day'`, `'month'`, `'year'`, `'none'` | `'day'` |
| `timePrecision` | `'hour'`, `'min'`, `'sec'`, `'none'` | 없음 (날짜만) |
| `timeStyle`, `display` | 2025 와 같음 | |

- `'ISO8601'` 이면 `2024-05-01` 형식입니다. 시간까지 쓰면 `2024-05-01T13:05` 입니다.
- 첫 인자가 Date 가 아니면 콘솔 오류를 내고 `''` 를 돌려줍니다.
- 기본값이 Me 설정을 따르므로 `locale` 은 보통 주지 않습니다. OOTB(ENOXPackageCommonUXInfra)는 `useISO8601ForDateAndTime ? 'ISO8601' : regionalFormatLanguage` 를 직접 넘기는데 기본값과 같습니다.

| 그 밖의 함수 | 설명 |
| --- | --- |
| `DateUtils.formatDateToRelativeTime(date, { relativeTo, thresholds, rounding, customFormat, capitalizeFirstLetter })` | "3일 전" 같은 상대 시간. `Intl.RelativeTimeFormat(LanguageUtils.language)` |
| `DateUtils.getDatePlaceholder({ datePrecision, timePrecision, locale, regionLanguageOnly })` | 입력 칸 안내 글자 (`'yyyy/mm/dd'`). `regionLanguageOnly: false` 일 때만 ISO 설정을 따름 |
| `NumberUtils.formatNumberToString(n, { locale, decimals, thousandsSeparator, useScientificNotation })` | 소수점을 지역 문자로. `thousandsSeparator` 면 `Intl.NumberFormat` |
| `NumberUtils.getDecimalSeparator(locale?)` | 소수점 문자 (`regionalFormatLanguage` 기준) |
| `NumberUtils.styleFormattingInfo('percent' 또는 'unit' 또는 'currency', { locale })` | 기호 앞 공백, 기호 위치 `{ space, before }` |

::: warning 2026 에서 WUX.currentFormatsLanguage 가 없어집니다
2026 Core.js 에는 `WUX.currentFormatsLanguage`, `WUX.onFormatsLanguageChange` 가 없습니다. 2025 코드가 이것을 읽으면 `undefined` 이고, 넣어도 컨트롤에 반영되지 않습니다. 2026 은 `LanguageUtils.regionalFormatLanguage` (또는 `WUX.regionalFormatLanguage`) 와 `WUX.onRegionalFormatLanguageChange` 를 씁니다. `DateUtils.convertToString` 도 `formatDateToString` 으로 바뀌었습니다.
:::

### 시간대

- `Date`, `Intl.DateTimeFormat` 은 브라우저(PC) 시간대로 씁니다. Me 설정에서 시간대를 읽는 코드는 2025, 2026 모두 찾지 못했습니다.
- 서버에 시간대를 알릴 때 DocumentManagement 는 `new Date().getTimezoneOffset()` (분. 한국은 `-540`) 을 `e6w-timezone` 으로 보냅니다 ([REST API](/rest-api/)).

## Me 환경설정 읽기

```js
require(['DS/MePreferencesClientAPI/MePreferencesClientAPI'], function (MePreferences) {
  MePreferences.readPreferences([
    { Repository: 'cke', PreferenceNames: ['Length', 'LengthCalcDisplay', 'TrailingZeros'] }
  ]).then(function (result) {
    result.repositories.forEach(function (repo) {
      repo.preferences.forEach(function (pref) {
        console.log(repo.name, pref.name, pref.value);
      });
    });
  }).catch(function (error) {
    console.warn('MePreferences', error);
  });
});
```

- 입력은 `[{ Repository: '저장소', PreferenceNames: ['키', ...] }, ...]` 입니다. 같은 저장소를 두 번 쓰면 키를 합칩니다. 빈 배열이면 reject 합니다.
- 플랫폼 id (`PlatformAPI.getWidgetTenant()`, 없으면 `getTenant()`) 로 Compass 에서 서비스 `mepreferences` 의 주소를 찾고 `POST <mepreferences>/api/v1/preferences` 를 WAFData 로 부릅니다. 본문은 `{ repositories: [{ name, preferenceNames }] }` 입니다. 주소는 한 번 찾으면 기억합니다.
- 결과는 `{ repositories: [{ name, preferences: [{ name, value }] }] }` 입니다. BetaOptions 는 `preferences` 항목의 `datatype` 도 봅니다.
- 저장소 순서에 기대지 말고 `name` 으로 찾습니다 (PIMwebPropertiesTabs 가 이렇게 함).
- 2026 은 익명 사용자(`window.ds.anonymous`)면 바로 reject 합니다.

::: warning 값은 문자열입니다
`value` 는 `'true'`, `'3'`, `'Millimeter'` 같은 문자열입니다. `'false'` 도 참으로 판정되므로 `if (pref.value)` 로 쓰지 않습니다. OOTB (DELWebOERCommonUtils) 처럼 바꿔서 씁니다.
:::

```js
function readMePreference(MePreferences, repository, name) {
  return MePreferences.readPreferences([{ Repository: repository, PreferenceNames: [name] }])
    .then(function (result) {
      let value;
      (result.repositories || []).forEach(function (repo) {
        (repo.preferences || []).forEach(function (pref) {
          if (pref.name === name) {
            value = pref.value === 'true' ? true : pref.value === 'false' ? false : pref.value;
          }
        });
      });
      return value;
    });
}
```

### OOTB 가 읽는 저장소와 키

| 저장소 | 키 | 값 | 쓰는 곳 |
| --- | --- | --- | --- |
| `Languages` <Badge type="warning" text="2026" /> | `RegionalFormatLanguage`, `UseISO8601ForDateAndTime` | 지역 형식 언어, `'true'` / `'false'` | LanguageUtils, ENOProjectMgmtCommonComponents |
| `3DDashboard` | `OpenInNewTab` | `'FullScreenExperience'` 면 다른 앱을 새 탭(전체 화면)에서 엶 | TransientWidget (2025) |
| `cke` | `Length`, `LengthDisplay`, `LengthCalcDisplay`, `Angle`, `AngleDisplay`, `AngleCalcDisplay`, `Area`, `Volume`, `Mass` (각각 `Display`, `CalcDisplay`), `TrailingZeros` | 단위 이름 (PIMwebPropertiesTabs 의 `Length` 기본값은 `'Millimeter'`), 소수 자릿수 (`...CalcDisplay`), 끝자리 0 표시 | 3D 앱 9 개 (PIMwebPropertiesTabs, ENOWebNavVolumeQuery, CATRelatedData3DGrid ...) |
| `VisualizationRepository`, `FrameGeneral`, `SelectionRepository`, `MeasureRepository`, `DMURepository` ... | `Gravity`, `ProjectionType`, `Select3DGeometry` ... | 3D 보기 설정 | 3DPlay, DMU, CAT3D 계열 |

- 위 목록은 OOTB 코드가 읽는 이름입니다. 저장소와 키의 정의는 서버(mepreferences 서비스)에 있어서 전체 목록은 webapps 에 없습니다.
- PIMwebPropertiesTabs 는 `LengthCalcDisplay` 를 소수 자릿수(`parseInt`)로, `TrailingZeros` 를 `'true'` 비교로 씁니다. `LengthDisplay` 는 화면 표시용 자릿수로 보이나 `...CalcDisplay` 와의 차이는 원본에서 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.
- 2025 에는 날짜 형식을 정하는 Me 설정을 읽는 코드가 없습니다. 2026 의 `Languages` 저장소가 그 역할을 합니다.

### BetaOptions.getMePreference

`DS/WebappsUtils/BetaOptions` 는 3DDashboard 최상위 창의 MePreferencesClientAPI 를 불러 한 값을 읽습니다. TransientWidget 이 "새 탭에서 열기" 를 이것으로 봅니다.

```js
require(['DS/WebappsUtils/BetaOptions'], function (BetaOptions) {
  BetaOptions.getMePreference({
    preferenceName: 'OpenInNewTab',
    preferenceValueExpected: 'FullScreenExperience'
  }).then(function (pref) {
    const openInNewTab = Boolean(pref && pref.value);
  });
});
```

- `repository` 를 빼면 `'3DDashboard'` 입니다.
- `preferenceValueExpected` 를 주면 `value` 가 "그 값과 같은지" (`true` / `false`) 로 바뀝니다. `'true'` / `'false'` 문자열도 boolean 으로 바꿉니다.
- 읽기에 실패해도 reject 하지 않습니다. 2025 는 `undefined`, 2026 은 `{}` 로 끝납니다.
- 2026 은 결과를 localStorage `3DD_mePrefOpts_...` 에 저장해 두고 다음에는 그 값을 먼저 돌려줍니다. 뒤에서 다시 읽어 값이 바뀌었으면 **페이지를 다시 읽습니다**.

::: warning BetaOptions.getMePreference 는 3DDashboard 밖에서 끝나지 않습니다 (2025)
2025 는 `window.dsDashboardWindow` 가 없으면 아무것도 하지 않아서 `then` 도 `catch` 도 불리지 않습니다. 단독 실행도 지원하는 위젯은 `MePreferencesClientAPI.readPreferences` 를 직접 부르고 기본값을 정해 둡니다. 2026 은 `window.top` 으로 대신 찾습니다.
:::

### Me 설정 창 열기

```js
MePreferences.getMePreferencesDialog('3DSApp.' + widget.getValue('appId')).then(function (dialog) {
  if (dialog) {
    dialog.inject(widget.body);
  }
});
```

- 앱 id 앞에 `3DSApp.` 을 붙입니다 (CATWebUXPreferences, ENXNav).
- 둘째 인자는 `{ type: 'appview' 또는 'globalview' 또는 'customview', showCategoryView, preference }` 입니다. 빼면 `{ type: 'appview', showCategoryView: true }` 입니다.
- 2025 는 창 객체를 돌려주고 OOTB 가 `inject` 합니다. MePreferencesClientAPI 를 읽기만 해도 창 모듈(MePreferencesUIElements, MePreferencesUIBuilder)이 같이 읽힙니다.
- 2026 은 앱 id 를 주면 창을 직접 만들지 않고 `PlatformAPI.publish('com.ds.mep:OnDialogCmd', ...)` 로 3DDashboard 에 열어 달라고 한 뒤 `null` 을 돌려줍니다. 그래서 `if (dialog)` 확인이 필요합니다. 창 모듈은 이때 읽습니다.

## 값이 바뀔 때

Me 설정이 저장되면 PlatformAPI 메시지 `com.ds.mep:onPrefUpdate` 가 옵니다 (29 앱이 받음). 받기와 해제는 [PlatformAPI](/widget/platform-api#publish-subscribe) 와 같습니다.

```js
require(['DS/PlatformAPI/PlatformAPI'], function (PlatformAPI) {
  const subscription = PlatformAPI.subscribe('com.ds.mep:onPrefUpdate', function (data) {
    if (!Array.isArray(data) || !data[1] || !data[1].preferences) {
      return;
    }
    const tenantId = data[2] && data[2].tenantId;
    const myTenant = PlatformAPI.getWidgetTenant() || PlatformAPI.getTenant();
    if (tenantId && tenantId !== myTenant) {
      return;
    }
    JSON.parse(data[1].preferences).repositories.forEach(function (repo) {
      repo.preferences.forEach(function (pref) {
        if (repo.name === 'cke' && pref.name === 'Length') {
          applyLengthUnit(pref.value);
        }
      });
    });
  });
});
```

- 데이터는 3 개짜리 배열입니다. `data[1].preferences` 는 바뀐 값의 JSON **문자열** (`{ repositories: [...] }`), `data[2].tenantId` 는 플랫폼 id 입니다 (VisuMePreferences, ENO3DUtils, 3DPlayReviewExperience 가 이렇게 읽음). `data[0]` 의 내용은 확인하지 못했습니다.
- 다른 플랫폼의 변경일 수 있으므로 `tenantId` 를 `getWidgetTenant()` (없으면 `getTenant()`) 와 비교합니다 (MePreferencesModalDialog 가 이렇게 함).
- 이 메시지를 보내는 쪽은 webapps 에 없습니다. 3DDashboard 또는 서버 알림으로 보이며, 다른 탭에서 바꾼 경우에도 오는지는 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.
- 언어 변경은 이 메시지로 처리하지 않습니다. 2025 는 Me 설정 창이 페이지를 다시 읽고, 2026 은 "새로 고침" 확인 창을 띄웁니다.
- 2026 의 지역 형식 언어는 `LanguageUtils.subscribe('onRegionalFormatLanguageChange', fn)` 로 받습니다. 날짜 입력 컨트롤이 이것으로 다시 그리고 `destroy` 에서 `unsubscribe` 합니다.

## 그 밖의 모듈

| 모듈 | 앱 | 내용 |
| --- | --- | --- |
| `DS/WebSystem/Nls` | 21 | 3DPlay 계열의 CATNls 리소스 로더 (`nls(파일, 키, 콜백)` 24 건, `loadNls` 2 건). `DS/RuntimeView/NLSManager` 를 씀. 위젯 번역은 `i18n!` 을 씀 |
| `DS/MePreferencesClientCacheMgr/MePreferencesClientCacheMgr` | 2 | 브라우저 Cache Storage `MePreferences` 에 응답 저장 (`retrieveCache`, `addCache`, `deleteCache`, `retrieveEtag`). Me 설정 창 내부용 |
| `DS/MePreferencesClientCacheMgr/MePreferencesClientCacheHelper` | 2 | `GET <mepreferences>/api/v1/version` 을 sessionStorage 에 기억. 내부용 |
| `DS/MePreferencesClientUtilities/MePreferencesPlatformServicesUtils` | 4 | `getTenant()`, `getServiceUrl(tenant, 'mepreferences')`, `callRestAPI(body, url, method, options)`, `setMyProfileLang(lang)` (Passport `my-profile/update/language/언어`). readPreferences 가 씀 |
| `DS/MePreferencesClientUtilities/MePreferencesWriter` | 1 | 저장 `PUT <mepreferences>/api/v1/preferences` (본문 `{ repositories: [{ name, preferences: [{ name, value }] }] }`, 값은 문자열). Me 설정 창 내부용 |
| `DS/UWPClientCode/BetaOptions` | 3 | Me 설정 창의 Beta 옵션 (`getSupportedBetaOptions`, `setBetaOptionsValues`) |

## 2026 변경

| 항목 | 2025 | 2026 |
| --- | --- | --- |
| 지역 형식 언어 | 없음. `WUX.currentFormatsLanguage` 는 위젯 언어 | Me 설정 `Languages / RegionalFormatLanguage`, `LanguageUtils.regionalFormatLanguage`, `WUX.regionalFormatLanguage` |
| ISO 8601 날짜 | 없음 | `Languages / UseISO8601ForDateAndTime` |
| 날짜를 글자로 | `DateUtils.convertToString` | `DateUtils.formatDateToString` (46 앱), `formatDateToRelativeTime`, `getDatePlaceholder` |
| 숫자를 글자로 | `Utils.getDecimalSeparator`, `getStringFromNumber` | `NumberUtils.formatNumberToString`, `getDecimalSeparator`, `styleFormattingInfo`. `Utils.getStringFromNumber` 없어짐 |
| WUX 전역 | `currentFormatsLanguage`, `onFormatsLanguageChange` | 없어짐 → `regionalFormatLanguage`, `useISO8601ForDateAndTime`, `onRegionalFormatLanguageChange` |
| `WUX.currentLanguage` | `widget.lang` → `navigator.language` | `LanguageUtils.language` (`dsLang` 도 봄) |
| `I18n.setCurrentLanguage` | 쿠키 + 다시 읽기 | 쿠키만 (둘째 인자 없음). 모듈을 읽을 때도 지금 언어로 쿠키를 한 번 씀 |
| `readPreferences` | 누구나 부름 | 익명 사용자는 reject |
| `getMePreferencesDialog` | 창을 만들어 돌려줌 | 3DDashboard 에 요청하고 `null` |
| `BetaOptions` | 최상위 창이 없으면 끝나지 않음 | `window.top` 사용, localStorage 캐시, 값이 바뀌면 다시 읽기 |
| 새 탭에서 열기 | `3DDashboard / OpenInNewTab` | `DS/Dashboard/Utils.getAppUserPreferences({ appId, isThirdPartyApp })` 의 `preferences.openInNewTab` ([TransientWidget](/widget/transient-widget#동작-방식)) |
| MePreferencesClientAPI 를 쓰는 앱 (의존성 기준) | 11 | 18 |

- `DS/Dashboard/Utils` 는 3DDashboard 최상위 창의 모듈이라 webapps 에 없습니다. `getAppUserPreferences` 가 `openInNewTab` 말고 무엇을 돌려주는지는 확인하지 못했습니다 <Badge type="danger" text="검증 필요" />.
