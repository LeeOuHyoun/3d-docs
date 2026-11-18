---
aside: false
outline: false
---

<script setup>
import IconGallery from '../.vitepress/theme/IconGallery.vue'
</script>

# 아이콘 목록 <Badge type="info" text="원본 분석" />

3DDashboard 위젯에서 쓸 수 있는 글꼴 아이콘 전체입니다. 글꼴을 탭으로 고르고 이름으로 거릅니다. 아이콘을 누르면 클래스 이름과 컨트롤에서 지정하는 코드가 나옵니다. 목록은 2026 기준이고, 2025 에도 있는 아이콘은 오른쪽 위에 `2025` 를 표시합니다.

| 글꼴 | 클래스 | 정의 | 지정 방법 |
| --- | --- | --- | --- |
| WebUX 3DS | `wux-ui-3ds wux-ui-3ds-이름` | `Core/wux-3ds-fonticons.css` (`DSFontIcon`) | [WebUX 아이콘](/controls/webux-controls#아이콘), [Dom.generateIcon](/amd/utilities#아이콘) |
| UIKIT | `fonticon fonticon-이름` | `UIKIT/UIKIT.css` (`entypo`) | [UIKIT](/controls/uikit) `icon: '이름'` |
| FontAwesome 4.3 | `wux-ui-fa wux-ui-fa-이름` | `Core/wux-3ds-fonticons.css` (`FontAwesome`) | WebUX Button 의 문자열 `icon` |
| UWA | `uwa-icon 이름` (`small` 추가 가능) | `UWA2/assets/css/base/base/icons.css` (`uwa-icons`) | 옛 UWA 위젯 |

- WebUX 3DS 와 UIKIT 은 이름이 거의 같지만 클래스와 글꼴이 다릅니다. 같은 이름이라도 쓰는 컨트롤에 맞는 탭에서 확인합니다.
- 크기 클래스: WebUX `wux-ui-3ds-lg`, `wux-ui-3ds-2x` ~ `5x` / FontAwesome `-lg`, `-2x` ~ `5x`, `-fw`, `-spin` / UIKIT `fonticon-2x` ~ `5x`, `fonticon-spin`
- 모듈 전용 글꼴(E6WCommonApps, PlatformManagement, NotificationsCenter, CfgBaseUX, W3DXComponents)과 이미지 아이콘(`assets/icons`, `common/images`)은 이 목록에 없습니다.

::: info 글꼴 파일
이 사이트에는 원본 글꼴 파일을 올리지 않습니다. 공개 사이트에서는 "글꼴 파일 선택" 으로 webapps 의 글꼴(`Core/assets/fonts/3ds_fonticon.woff2` 등)을 고르면 그 브라우저에서만 읽어 그립니다. 로컬에서는 `npm run docs:icons` 가 목록(`docs/public/icons/catalog.json`)을 만들고 글꼴을 `docs/public/icons/fonts/` (git 제외)에 복사합니다.
:::

<IconGallery />
