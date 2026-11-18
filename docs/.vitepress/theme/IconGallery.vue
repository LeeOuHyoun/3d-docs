<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { withBase } from 'vitepress'

const USAGE = {
  wux3ds: (name) => [
    ['HTML', `<span class="wux-ui-3ds wux-ui-3ds-${name}"></span>`],
    ['WebUX Button', `icon: { iconName: '${name}', fontIconFamily: WUXManagedFontIcons.Font3DS }`],
    ['Dom.generateIcon', `Dom.generateIcon('${name}')`]
  ],
  uikit: (name) => [
    ['HTML', `<span class="fonticon fonticon-${name}"></span>`],
    ['UIKIT Button', `icon: '${name}'`]
  ],
  fa: (name) => [
    ['HTML', `<span class="wux-ui-fa wux-ui-fa-${name}"></span>`],
    ['WebUX Button (문자열)', `icon: '${name}'`],
    ['WebUX Button (객체)', `icon: { iconName: '${name}', fontIconFamily: WUXManagedFontIcons.FontAwesome }`]
  ],
  uwa: (name) => [['HTML', `<span class="uwa-icon ${name.replace('.', ' ')}"></span>`]]
}

const families = ref([])
const familyId = ref('wux3ds')
const family = computed(() => families.value.find((item) => item.id === familyId.value))
const fontState = reactive({})

const loadFont = async (item, source) => {
  try {
    const face = await new FontFace(`ds-icon-${item.id}`, source).load()
    document.fonts.add(face)
    fontState[item.id] = 'ready'
  } catch {
    fontState[item.id] = 'missing'
  }
}

onMounted(async () => {
  families.value = await (await fetch(withBase('/icons/catalog.json'))).json()
  families.value.forEach((item) => loadFont(item, `url(${withBase(`/icons/fonts/${item.font}`)})`))
})

const fileInput = ref()

const handleFontFile = async (event) => {
  const [file] = event.target.files
  if (file) await loadFont(family.value, await file.arrayBuffer())
  event.target.value = ''
}

const query = ref('')
const since = ref('all')
const size = ref(28)

const icons = computed(() => {
  const words = query.value.trim().toLowerCase().split(/\s+/).filter(Boolean)
  return (family.value?.icons ?? []).filter(([name, , old]) =>
    words.every((word) => name.includes(word)) && (since.value === 'all' || (since.value === 'old') === Boolean(old))
  )
})

const glyph = (code) => String.fromCodePoint(parseInt(code, 16))

const selected = ref(null)
const usages = computed(() => (selected.value ? USAGE[familyId.value](selected.value[0]) : []))

const selectFamily = (id) => {
  familyId.value = id
  selected.value = null
}

const copied = ref('')

const copy = async (text) => {
  await navigator.clipboard.writeText(text)
  copied.value = text
  setTimeout(() => (copied.value = ''), 1200)
}
</script>

<template>
  <div class="icon-gallery">
    <div class="ig-tabs">
      <button v-for="item in families" :key="item.id" :class="{ active: item.id === familyId }" @click="selectFamily(item.id)">
        {{ item.label }} <span>{{ item.icons.length }}</span>
      </button>
    </div>
    <div class="ig-toolbar">
      <input v-model="query" type="search" placeholder="이름으로 찾기 (공백으로 여러 단어)" />
      <select v-model="since">
        <option value="all">전체</option>
        <option value="old">2025에 있음</option>
        <option value="new">2026 추가</option>
      </select>
      <label>크기 <input v-model.number="size" type="range" min="16" max="56" /> {{ size }}px</label>
      <span class="ig-count">{{ icons.length }}개</span>
    </div>
    <div v-if="family && fontState[family.id] === 'missing'" class="ig-font">
      글꼴이 없어 아이콘 모양을 그릴 수 없습니다. webapps 의 <code>{{ family.file }}</code> 를 고르면 이 화면에서만 읽어 그립니다 (서버로 보내지 않음).
      <button @click="fileInput.click()">글꼴 파일 선택</button>
      <input ref="fileInput" type="file" accept=".woff,.woff2,.ttf,.otf" hidden @change="handleFontFile" />
    </div>
    <div class="ig-body">
      <div class="ig-grid" :style="{ '--ig-size': `${size}px` }">
        <button v-for="icon in icons" :key="icon[0]" :class="{ active: selected?.[0] === icon[0] }" :title="icon[0]" @click="selected = icon">
          <span class="ig-glyph" :style="{ fontFamily: `ds-icon-${familyId}` }">{{ glyph(icon[1]) }}</span>
          <span class="ig-name">{{ icon[0] }}</span>
          <span v-if="icon[2]" class="ig-old">2025</span>
        </button>
      </div>
      <aside v-if="selected" class="ig-detail">
        <button class="ig-close" @click="selected = null">×</button>
        <span class="ig-big" :style="{ fontFamily: `ds-icon-${familyId}` }">{{ glyph(selected[1]) }}</span>
        <strong class="ig-copy" @click="copy(selected[0])">{{ selected[0] }}</strong>
        <p>{{ family.label }} · <code>\{{ selected[1] }}</code> · {{ selected[2] ? '2025에 있음' : '2026 추가' }}</p>
        <div v-for="[label, code] in usages" :key="label" class="ig-usage">
          <span>{{ label }}</span>
          <code class="ig-copy" @click="copy(code)">{{ code }}</code>
        </div>
        <p class="ig-hint">{{ copied ? '복사했습니다' : '이름이나 코드를 누르면 복사합니다' }}</p>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.icon-gallery button {
  cursor: pointer;
}

.ig-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 16px 0;
}

.ig-tabs button {
  padding: 6px 14px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 20px;
  font-size: 14px;
}

.ig-tabs button span {
  color: var(--vp-c-text-3);
}

.ig-tabs button.active {
  border-color: var(--vp-c-brand-1);
  color: var(--vp-c-brand-1);
}

.ig-toolbar {
  position: sticky;
  top: var(--vp-nav-height);
  z-index: 10;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  background: var(--vp-c-bg);
  font-size: 14px;
}

.ig-toolbar input[type='search'] {
  flex: 1;
  min-width: 220px;
  padding: 8px 12px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
}

.ig-toolbar select {
  padding: 7px 10px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
}

.ig-toolbar label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.ig-count {
  color: var(--vp-c-text-2);
}

.ig-font {
  margin: 8px 0;
  padding: 12px 16px;
  border-radius: 8px;
  background: var(--vp-c-warning-soft);
  font-size: 14px;
}

.ig-font button {
  margin-left: 8px;
  padding: 2px 10px;
  border: 1px solid var(--vp-c-warning-1);
  border-radius: 6px;
}

.ig-body {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.ig-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(calc(var(--ig-size) + 72px), 1fr));
  gap: 8px;
}

.ig-grid button {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 4px 8px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  content-visibility: auto;
  contain-intrinsic-size: auto calc(var(--ig-size) + 64px);
}

.ig-grid button:hover,
.ig-grid button.active {
  border-color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
}

.ig-glyph {
  font-size: var(--ig-size);
  line-height: 1;
}

.ig-name {
  width: 100%;
  overflow: hidden;
  font-size: 11px;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--vp-c-text-2);
}

.ig-old {
  position: absolute;
  top: 3px;
  right: 5px;
  font-size: 9px;
  color: var(--vp-c-text-3);
}

.ig-detail {
  position: sticky;
  top: calc(var(--vp-nav-height) + 70px);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: 300px;
  padding: 20px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
  background: var(--vp-c-bg-soft);
}

.ig-close {
  align-self: flex-end;
  font-size: 20px;
  line-height: 1;
}

.ig-big {
  font-size: 96px;
  line-height: 1;
}

.ig-detail p {
  margin: 0;
  font-size: 13px;
  color: var(--vp-c-text-2);
}

.ig-usage {
  width: 100%;
  font-size: 12px;
}

.ig-usage code {
  display: block;
  margin-top: 2px;
  word-break: break-all;
}

.ig-copy {
  cursor: copy;
}

@media (max-width: 960px) {
  .ig-body {
    flex-direction: column-reverse;
  }

  .ig-detail {
    position: static;
    width: 100%;
  }
}
</style>
