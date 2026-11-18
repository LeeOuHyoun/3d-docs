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
const familyId = ref('all')
const familyOf = (id) => families.value.find((item) => item.id === id)
const total = computed(() => families.value.reduce((sum, item) => sum + item.icons.length, 0))

const fontsDb = (mode, run) =>
  new Promise((resolve, reject) => {
    const open = indexedDB.open('ds-icon-fonts', 1)
    open.onupgradeneeded = () => open.result.createObjectStore('fonts')
    open.onerror = () => reject(open.error)
    open.onsuccess = () => {
      const request = run(open.result.transaction('fonts', mode).objectStore('fonts'))
      request.onsuccess = () => resolve(request.result)
      request.onerror = () => reject(request.error)
    }
  })

const fontState = reactive({})
const stored = reactive({})
const missing = computed(() => families.value.filter((item) => fontState[item.id] === 'missing'))
const storedLabels = computed(() => families.value.filter((item) => stored[item.id]).map((item) => item.label))

const loadFont = async (item, source) => {
  try {
    document.fonts.add(await new FontFace(`ds-icon-${item.id}`, source).load())
    fontState[item.id] = 'ready'
    return true
  } catch {
    fontState[item.id] = 'missing'
    return false
  }
}

const restoreFont = async (item) => {
  if (await loadFont(item, `url(${withBase(`/icons/fonts/${item.font}`)})`)) return
  const saved = await fontsDb('readonly', (store) => store.get(item.id)).catch(() => null)
  if (saved && (await loadFont(item, saved))) stored[item.id] = true
}

const applyFont = async (item, buffer) => {
  if (!(await loadFont(item, buffer))) return false
  await fontsDb('readwrite', (store) => store.put(buffer, item.id)).catch(() => null)
  stored[item.id] = true
  return true
}

const hasFolderPicker = ref(false)
const fontGuide = computed(() => {
  const lead = `글꼴 ${missing.value.length}개가 없어 아이콘 모양 대신 이름만 보입니다.`
  const how = hasFolderPicker.value ? '원본 webapps 폴더를 고르면 아래 글꼴을 한 번에 찾아' : '원본 webapps 의 아래 파일을 고르면'
  return `${lead} ${how} 이 브라우저에서만 읽어 그리고 기억합니다 (서버로 보내지 않음).`
})

onMounted(async () => {
  hasFolderPicker.value = 'showDirectoryPicker' in window
  families.value = await (await fetch(withBase('/icons/catalog.json'))).json()
  families.value.forEach(restoreFont)
})

const notice = ref('')

const report = (results) => {
  const loaded = results.filter(([, ok]) => ok).map(([item]) => item.label)
  const failed = results.filter(([, ok]) => !ok).map(([item]) => item.label)
  notice.value = [loaded.length && `읽음: ${loaded.join(', ')}`, failed.length && `못 찾음: ${failed.join(', ')}`].filter(Boolean).join(' / ')
}

const readFolderFile = async (dir, path) => {
  const parts = path.split('/')
  for (const part of parts.slice(0, -1)) dir = await dir.getDirectoryHandle(part)
  return (await (await dir.getFileHandle(parts.at(-1))).getFile()).arrayBuffer()
}

const pickFolder = async () => {
  const dir = await window.showDirectoryPicker().catch(() => null)
  if (!dir) return
  report(await Promise.all(missing.value.map(async (item) => [item, await readFolderFile(dir, item.file).then((buffer) => applyFont(item, buffer)).catch(() => false)])))
}

const fileInput = ref()
const baseName = (path) => path.split('/').pop().replace(/\.[^.]+$/, '')

const handleFontFiles = async (event) => {
  const files = [...event.target.files]
  event.target.value = ''
  const targets = missing.value.flatMap((item) => {
    const file = files.find((candidate) => baseName(candidate.name) === baseName(item.file))
    return file ? [[item, file]] : []
  })
  report(await Promise.all(targets.map(async ([item, file]) => [item, await applyFont(item, await file.arrayBuffer())])))
  if (!targets.length) notice.value = `맞는 글꼴 파일이 없습니다: ${files.map((file) => file.name).join(', ')}`
}

const clearFonts = async () => {
  await fontsDb('readwrite', (store) => store.clear()).catch(() => null)
  location.reload()
}

const query = ref('')
const since = ref('all')
const size = ref(28)

const sections = computed(() => {
  const words = query.value.trim().toLowerCase().split(/\s+/).filter(Boolean)
  return families.value
    .filter((item) => familyId.value === 'all' || item.id === familyId.value)
    .map((item) => ({
      ...item,
      ready: fontState[item.id] === 'ready',
      icons: item.icons.filter(([name, , old]) => words.every((word) => name.includes(word)) && (since.value === 'all' || (since.value === 'old') === Boolean(old)))
    }))
    .filter((section) => section.icons.length || familyId.value !== 'all')
})
const count = computed(() => sections.value.reduce((sum, section) => sum + section.icons.length, 0))

const glyph = (code) => String.fromCodePoint(parseInt(code, 16))

const selected = ref(null)
const selectedFamily = computed(() => selected.value && familyOf(selected.value.id))
const usages = computed(() => (selected.value ? USAGE[selected.value.id](selected.value.icon[0]) : []))
const isSelected = (id, icon) => selected.value?.id === id && selected.value.icon[0] === icon[0]

const selectIcon = (id, icon) => {
  selected.value = { id, icon }
}

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
    <div v-if="missing.length || storedLabels.length" class="ig-font">
      <template v-if="missing.length">
        <p>{{ fontGuide }}</p>
        <ul>
          <li v-for="item in missing" :key="item.id"><strong>{{ item.label }}</strong> <code>webapps/{{ item.file }}</code></li>
        </ul>
        <div class="ig-font-actions">
          <button v-if="hasFolderPicker" class="ig-primary" @click="pickFolder">webapps 폴더 선택</button>
          <button :class="{ 'ig-secondary': hasFolderPicker }" @click="fileInput.click()">{{ hasFolderPicker ? '글꼴 파일 직접 고르기' : '파일 선택 (여러 개)' }}</button>
          <input ref="fileInput" type="file" multiple accept=".woff,.woff2,.ttf,.otf" hidden @change="handleFontFiles" />
        </div>
      </template>
      <p v-if="notice" class="ig-notice">{{ notice }}</p>
      <p v-if="storedLabels.length" class="ig-stored">
        브라우저에 기억한 글꼴: {{ storedLabels.join(', ') }}
        <button @click="clearFonts">글꼴 지우기</button>
      </p>
    </div>
    <div class="ig-tabs">
      <button :class="{ active: familyId === 'all' }" @click="selectFamily('all')">전체 <span>{{ total }}</span></button>
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
      <span class="ig-count">{{ count }}개</span>
    </div>
    <div class="ig-body">
      <div class="ig-main" :style="{ '--ig-size': `${size}px` }">
        <section v-for="section in sections" :key="section.id">
          <div v-if="familyId === 'all'" class="ig-section">{{ section.label }} <span>{{ section.icons.length }}</span></div>
          <div class="ig-grid">
            <button v-for="icon in section.icons" :key="icon[0]" :class="{ active: isSelected(section.id, icon) }" :title="icon[0]" @click="selectIcon(section.id, icon)">
              <span v-if="section.ready" class="ig-glyph" :style="{ fontFamily: `ds-icon-${section.id}` }">{{ glyph(icon[1]) }}</span>
              <span class="ig-name">{{ icon[0] }}</span>
              <span v-if="icon[2]" class="ig-old">2025</span>
            </button>
          </div>
        </section>
        <p v-if="families.length && !count" class="ig-empty">찾는 아이콘이 없습니다.</p>
      </div>
      <aside v-if="selected" class="ig-detail">
        <button class="ig-close" @click="selected = null">×</button>
        <span v-if="fontState[selected.id] === 'ready'" class="ig-big" :style="{ fontFamily: `ds-icon-${selected.id}` }">{{ glyph(selected.icon[1]) }}</span>
        <strong class="ig-copy" @click="copy(selected.icon[0])">{{ selected.icon[0] }}</strong>
        <p>{{ selectedFamily.label }} · <code>\{{ selected.icon[1] }}</code> · {{ selected.icon[2] ? '2025에 있음' : '2026 추가' }}</p>
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

.ig-font p,
.ig-font ul {
  margin: 0 0 8px;
}

.ig-font ul {
  padding-left: 20px;
}

.ig-font button {
  padding: 2px 10px;
  border: 1px solid var(--vp-c-warning-1);
  border-radius: 6px;
}

.ig-font-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.ig-font .ig-primary {
  padding: 4px 14px;
  border-color: var(--vp-c-warning-1);
  background: var(--vp-c-warning-1);
  color: var(--vp-c-bg);
  font-weight: 600;
}

.ig-font .ig-secondary {
  border-color: transparent;
  color: var(--vp-c-text-2);
  text-decoration: underline;
}

.ig-font .ig-notice {
  margin: 8px 0 0;
  font-weight: 600;
}

.ig-font .ig-stored {
  margin: 8px 0 0;
  color: var(--vp-c-text-2);
}

.ig-stored button {
  margin-left: 8px;
}

.ig-body {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.ig-main {
  flex: 1;
  min-width: 0;
}

.ig-section {
  margin: 20px 0 10px;
  font-size: 16px;
  font-weight: 600;
}

.ig-section span,
.ig-empty {
  color: var(--vp-c-text-3);
}

.ig-main section:first-child .ig-section {
  margin-top: 4px;
}

.ig-grid {
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
  padding: 18px 4px 8px;
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
    align-items: stretch;
  }

  .ig-detail {
    position: static;
    width: 100%;
  }
}
</style>
