---
aside: false
outline: false
---

<script setup>
import IconGallery from '../.vitepress/theme/IconGallery.vue'
</script>

# 아이콘 목록 <Badge type="info" text="원본 분석" />

3DDashboard 위젯에서 쓸 수 있는 글꼴 아이콘 전체입니다. "전체" 탭에서 모든 글꼴을 글꼴별로 나눠 보고, 글꼴 탭으로 하나만 골라 볼 수 있습니다. 이름으로 거릅니다. 아이콘을 누르면 클래스 이름과 컨트롤에서 지정하는 코드가 나옵니다. 목록은 2026 기준이고, 2025 에도 있는 아이콘은 오른쪽 위에 `2025` 를 표시합니다.

| 글꼴 | 클래스 | 정의 | 글꼴 파일 (webapps 기준) | 지정 방법 |
| --- | --- | --- | --- | --- |
| WebUX 3DS | `wux-ui-3ds wux-ui-3ds-이름` | `Core/wux-3ds-fonticons.css` (`DSFontIcon`) | `Core/assets/fonts/3ds_fonticon.woff2` | [WebUX 아이콘](/controls/webux-controls#아이콘), [Dom.generateIcon](/amd/utilities#아이콘) |
| UIKIT | `fonticon fonticon-이름` | `UIKIT/UIKIT.css` (`entypo`) | `UIKIT/assets/fonts/3ds-icon.woff2` | [UIKIT](/controls/uikit) `icon: '이름'` |
| FontAwesome 4.3 | `wux-ui-fa wux-ui-fa-이름` | `Core/wux-3ds-fonticons.css` (`FontAwesome`) | `Core/assets/fonts/fontawesome-webfont.woff2` | WebUX Button 의 문자열 `icon` |
| UWA | `uwa-icon 이름` (`small` 추가 가능) | `UWA2/assets/css/base/base/icons.css` (`uwa-icons`) | `UWA2/assets/css/base/font/icons.woff` | 옛 UWA 위젯 |

- WebUX 3DS 와 UIKIT 은 이름이 거의 같지만 클래스와 글꼴이 다릅니다. 같은 이름이라도 쓰는 컨트롤에 맞는 탭에서 확인합니다.
- 2026 은 WebUX 3DS 와 UIKIT 글꼴이 같은 파일(3DS Fonticon 2.028)입니다. 2025 는 버전이 다릅니다 (Core 2.022, UIKIT 2.024).
- 크기 클래스: WebUX `wux-ui-3ds-lg`, `wux-ui-3ds-2x` ~ `5x` / FontAwesome `-lg`, `-2x` ~ `5x`, `-fw`, `-spin` / UIKIT `fonticon-2x` ~ `5x`, `fonticon-spin`
- 모듈 전용 글꼴(E6WCommonApps, PlatformManagement, NotificationsCenter, CfgBaseUX, W3DXComponents)과 이미지 아이콘(`assets/icons`, `common/images`)은 이 목록에 없습니다.

::: info 글꼴 파일
WebUX 3DS, UIKIT 글꼴(3DS Fonticon)과 UWA 글꼴은 Dassault Systemes 의 글꼴이라 이 사이트에 올리지 않습니다. 글꼴에 적힌 라이선스는 "글꼴이 포함된 제품의 라이선스 범위에서 표시, 인쇄" 만 허용합니다. 공개 사이트에서는 아이콘 이름만 보이고, 위쪽 안내에서 글꼴을 읽으면 그 브라우저에서만 그립니다.

- "webapps 폴더 선택" (Chrome, Edge): webapps 폴더를 한 번 고르면 위 표의 경로에서 없는 글꼴을 모두 찾아 읽습니다.
- "글꼴 파일 직접 고르기" (폴더 선택이 안 되는 브라우저에서는 "파일 선택 (여러 개)"): webapps 폴더 없이 글꼴 파일만 있을 때 씁니다. 파일 이름(`3ds_fonticon`, `3ds-icon`, `icons`)으로 글꼴을 맞춥니다. `.woff2`, `.woff`, `.ttf` 를 받습니다.
- 읽은 글꼴은 브라우저(IndexedDB)에 기억해 다음에 열 때 다시 고르지 않아도 됩니다. "글꼴 지우기" 로 지웁니다.

FontAwesome 4.3 글꼴(Dave Gandy, [SIL OFL 1.1](https://openfontlicense.org))은 다시 배포할 수 있어 사이트에 함께 올립니다.

로컬에서는 `npm run docs:icons` 가 목록(`docs/public/icons/catalog.json`)을 만들고 글꼴을 `docs/public/icons/fonts/` 에 복사합니다. 이 폴더는 `fa.woff2` 만 git 에 올라갑니다.
:::

<IconGallery />
