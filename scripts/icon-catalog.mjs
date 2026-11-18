import { copyFileSync, existsSync, mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const ROOT = join(dirname(fileURLToPath(import.meta.url)), '..')
const VERSIONS = {
  2025: resolve(process.argv[2] ?? join(ROOT, '..', 'enovia2025', '3dspace', 'webapps')),
  2026: resolve(process.argv[3] ?? join(ROOT, '..', 'enovia2026_webapps'))
}
const OUT = join(ROOT, 'docs', 'public', 'icons')
const FONTS = join(OUT, 'fonts')

const FAMILIES = [
  {
    id: 'wux3ds',
    label: 'WebUX 3DS',
    css: 'Core/wux-3ds-fonticons.css',
    font: 'Core/assets/fonts/3ds_fonticon.woff2',
    selector: /^\.wux-ui-3ds-([a-z0-9-]+)::?before$/,
    skip: /^(lg|[2-5]x|fw)$/
  },
  {
    id: 'uikit',
    label: 'UIKIT',
    css: 'UIKIT/UIKIT.css',
    font: 'UIKIT/assets/fonts/3ds-icon.woff2',
    selector: /^\.fonticon-([a-z0-9-]+)::?before$/,
    skip: /^([2-5]x|spin)$/
  },
  {
    id: 'fa',
    label: 'FontAwesome 4.3',
    css: 'Core/wux-3ds-fonticons.css',
    font: 'Core/assets/fonts/fontawesome-webfont.woff2',
    selector: /^\.wux-ui-fa-([a-z0-9-]+)::?before$/,
    skip: /^$/
  },
  {
    id: 'uwa',
    label: 'UWA',
    css: 'UWA2/assets/css/base/base/icons.css',
    font: 'UWA2/assets/css/base/font/icons.woff',
    selector: /^\.uwa-icon\.([a-z0-9-]+(?:\.small)?)\s*::?before$/,
    skip: /^$/
  }
]

const codeOf = (raw) => {
  const text = raw.trim().replace(/^['"]|['"]$/g, '')
  const escaped = text.match(/^\\([0-9a-fA-F]{1,6})$/)
  if (escaped) return parseInt(escaped[1], 16)
  return [...text].length === 1 ? text.codePointAt(0) : null
}

const parse = (family, webapps) => {
  const css = readFileSync(join(webapps, family.css), 'utf8').replace(/\/\*[\s\S]*?\*\//g, '')
  const icons = new Map()
  for (const [, selectors, body] of css.matchAll(/([^{}]+)\{([^{}]*)\}/g)) {
    const content = body.match(/content\s*:\s*(['"][^'"]*['"])/)
    const code = content && codeOf(content[1])
    if (!code) continue
    for (const selector of selectors.split(',')) {
      const name = selector.trim().replace(/\s+/g, ' ').replace(/\.small ::?before$/, '.small:before').match(family.selector)?.[1]
      if (name && !family.skip.test(name) && !icons.has(name)) icons.set(name, code)
    }
  }
  return icons
}

rmSync(FONTS, { recursive: true, force: true })
mkdirSync(FONTS, { recursive: true })

const catalog = FAMILIES.map((family) => {
  const [old, now] = [parse(family, VERSIONS[2025]), parse(family, VERSIONS[2026])]
  const names = [...new Set([...now.keys(), ...old.keys()])].sort()
  const icons = names.map((name) => [name, (now.get(name) ?? old.get(name)).toString(16), old.has(name) ? 1 : 0])
  const webapps = [VERSIONS[2026], VERSIONS[2025]].find((dir) => existsSync(join(dir, family.font)))
  const font = family.id + family.font.match(/\.[^.]+$/)[0]
  copyFileSync(join(webapps, family.font), join(FONTS, font))
  console.log(`${family.label}: 2025 ${old.size}, 2026 ${now.size}, 합계 ${names.length}, 2025만 ${names.length - now.size}, 2026 추가 ${names.length - old.size}`)
  const { id, label } = family
  return { id, label, file: family.font.split('/').pop(), font, icons }
})

writeFileSync(join(OUT, 'catalog.json'), JSON.stringify(catalog))
