<script setup>
import HolographicCard from './HolographicCard.vue'
import RuleSysMark from './RuleSysMark.vue'

const rows = [
  { label: '분석한 소스', value: 'R2025x · R2026x' },
  { label: '최종 갱신', value: __DOCS_UPDATED__ },
]
const scopes = ['3DSpace', 'webapps']
</script>

<template>
  <div class="home-info-card">
    <HolographicCard>
      <div class="app-info-card">
        <div class="app-info-card-bg" aria-hidden="true"></div>
        <div class="card-head">
          <p class="card-title">3D Docs</p>
          <div class="card-brand">
            <button type="button" class="info-link">
              <RuleSysMark class="brand-mark" />
              <span class="brand-name mode-swap">
                <span class="dark-only">Rule<span class="brand-amp">&amp;</span>Sys</span>
                <span class="brand-ko light-only"><span class="brand-ko-corp">(주)</span>룰앤시스</span>
              </span>
              <svg class="link-icon" viewBox="0 0 24 24" width="13" height="13" aria-hidden="true">
                <path d="M15 3h6v6" />
                <path d="M10 14 21 3" />
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
              </svg>
            </button>
            <span class="tagline">Your AX Service Partner</span>
          </div>
        </div>
        <div class="card-body">
          <div class="card-main">
            <div class="icon-box">
              <svg class="app-icon" viewBox="0 0 58 58" aria-label="3D Docs">
                <defs>
                  <linearGradient id="docs-icon-bg" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0" stop-color="#0f5c94" />
                    <stop offset="1" stop-color="#073457" />
                  </linearGradient>
                </defs>
                <rect width="58" height="58" rx="11" fill="url(#docs-icon-bg)" />
                <g transform="translate(11 11) scale(1.5)" fill="none" stroke="#fff" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 5v16" />
                  <path d="M16 13h2" stroke="#4fb3e8" />
                  <path d="M16 9h2" stroke="#4fb3e8" />
                  <path d="M20.001 19A2 2 0 0022 17V5a2 2 0 00-1.999-2L16 3.002A5 5 0 0012 5a5 5 0 00-4-2H4a2 2 0 00-2 2v12a2 2 0 001.999 2H8a5 5 0 014 2 5 5 0 014-2z" />
                  <path d="M6 13h2" stroke="#4fb3e8" />
                  <path d="M6 9h2" stroke="#4fb3e8" />
                </g>
              </svg>
            </div>
            <div class="rows">
              <div v-for="row in rows" :key="row.label" class="row">
                <span>{{ row.label }}</span>
                <span class="value">{{ row.value }}</span>
              </div>
              <div class="row chips-row">
                <span>범위</span>
                <span class="chips">
                  <span v-for="scope in scopes" :key="scope" class="chip">{{ scope }}</span>
                </span>
              </div>
            </div>
          </div>
          <p class="credit">Created with ✨ by <span class="mode-swap"><span class="dark-only">Lee Ou Hyoun</span><span class="credit-ko light-only">이우현</span></span></p>
        </div>
      </div>
    </HolographicCard>
  </div>
</template>

<style scoped>
.home-info-card {
  --info-border: rgb(255 255 255 / 0.3);
  --info-title: #fff;
  --info-link: rgb(255 255 255 / 0.9);
  --info-link-hover: #fff;
  --info-tagline: rgb(255 255 255 / 0.75);
  --info-label: rgb(255 255 255 / 0.8);
  --info-value: #fff;
  --info-icon-border: rgb(255 255 255 / 0.8);
  --info-chip-border: rgb(255 255 255 / 0.4);
  --info-chip-bg: rgb(255 255 255 / 0.15);
  --info-chip-text: #fff;
  position: relative;
  z-index: 2;
  width: 360px;
  margin: 0 auto;
  font-family: 'Segoe UI', 'Malgun Gothic', sans-serif;
  line-height: 1.5;
  user-select: none;
}

.dark .home-info-card {
  --info-border: rgb(255 255 255 / 0.2);
  --info-title: oklch(96.7% 0.003 264.542);
  --info-link: #74c3ff;
  --info-link-hover: #9dcefb;
  --info-tagline: oklch(70.7% 0.022 261.325);
  --info-label: oklch(70.7% 0.022 261.325);
  --info-value: oklch(92.8% 0.006 264.531);
  --info-icon-border: oklch(27.8% 0.033 256.848);
  --info-chip-border: oklch(37.3% 0.034 259.733);
  --info-chip-bg: oklch(27.8% 0.033 256.848 / 0.4);
  --info-chip-text: oklch(92.8% 0.006 264.531);
}

.app-info-card {
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--info-border);
  border-radius: 0.75rem;
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.16),
    inset 0 1px 1px 0 rgba(255, 255, 255, 0.42),
    inset 0 -1px 1.5px 0 rgba(8, 28, 72, 0.42),
    inset 1px 0 1px 0 rgba(255, 255, 255, 0.22),
    inset -1px 0 1.5px 0 rgba(8, 28, 72, 0.28);
}

.dark .app-info-card {
  background: #000;
  box-shadow:
    inset 0 1px 1px 0 rgba(255, 255, 255, 0.4),
    inset 0 -1px 1px 0 rgba(255, 255, 255, 0.12),
    inset 1px 0 1px 0 rgba(255, 255, 255, 0.15),
    inset -1px 0 1px 0 rgba(255, 255, 255, 0.15);
}

.app-info-card-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
  background: linear-gradient(
    122deg,
    #0096d3 0%,
    #1c8bc6 14%,
    #3583bd 30%,
    #3e76b4 46%,
    #3a62a8 60%,
    #2a4e98 74%,
    #1c4084 86%,
    #1a3c74 100%
  );
}

.app-info-card-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  opacity: 0.32;
  mix-blend-mode: overlay;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='350' height='240'%3E%3Cfilter id='hairline' x='0' y='0' width='100%25' height='100%25'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.0023 0.48' numOctaves='4' seed='6' stitchTiles='stitch' result='fine'/%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.0012 0.13' numOctaves='2' seed='19' stitchTiles='stitch' result='coarse'/%3E%3CfeBlend in='fine' in2='coarse' mode='soft-light' result='mix'/%3E%3CfeColorMatrix type='saturate' values='0' in='mix'/%3E%3CfeComponentTransfer%3E%3CfeFuncR type='gamma' exponent='1.18'/%3E%3CfeFuncG type='gamma' exponent='1.18'/%3E%3CfeFuncB type='gamma' exponent='1.18'/%3E%3C/feComponentTransfer%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23hairline)'/%3E%3C/svg%3E");
  background-size: 100% 100%;
  background-repeat: no-repeat;
}

.dark .app-info-card-bg {
  background:
    linear-gradient(180deg, rgb(0 0 0 / 0.92) 0%, rgb(0 0 0 / 0.55) 18%, transparent 40%),
    linear-gradient(
      15deg,
      transparent 0%,
      rgba(255, 255, 255, 0.05) 24%,
      rgba(255, 255, 255, 0.14) 42%,
      rgba(255, 255, 255, 0.02) 50%,
      rgba(0, 0, 0, 0.3) 65%,
      rgba(255, 255, 255, 0.08) 84%,
      transparent 100%
    ),
    radial-gradient(
      ellipse 150% 160% at 112% 78%,
      rgb(230 255 248 / 0.72) 0%,
      rgb(110 255 214 / 0.42) 9%,
      rgb(0 214 176 / 0.28) 18%,
      rgb(0 156 132 / 0.14) 32%,
      rgb(0 72 68 / 0.06) 46%,
      transparent 62%
    ),
    radial-gradient(
      ellipse 80% 100% at 98% 70%,
      rgb(255 255 255 / 0.2) 0%,
      rgb(190 255 236 / 0.07) 16%,
      transparent 38%
    ),
    #000;
}

.dark .app-info-card-bg::before {
  opacity: 0.6;
}

.app-info-card > :not(.app-info-card-bg) {
  position: relative;
  z-index: 1;
}

.card-head {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  padding: 14px 1.25rem 0.75rem;
}

.card-title {
  margin: 0;
  font-size: 1.25rem;
  line-height: calc(1.75 / 1.25);
  font-weight: 700;
  color: var(--info-title);
}

.card-brand {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.25rem;
}

.info-link {
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 0.125rem;
  border: 0;
  background: none;
  font-family: ui-serif, Georgia, Cambria, 'Times New Roman', Times, serif;
  font-size: 15px;
  line-height: 1;
  font-weight: 700;
  color: var(--info-link);
  cursor: pointer;
  transition: color 0.12s ease;
}

.info-link:hover {
  color: var(--info-link-hover);
  text-decoration: underline;
}

.info-link:focus {
  outline: none;
}

.brand-mark {
  display: block;
  flex: 0 0 auto;
  height: 1rem;
}

.brand-amp {
  margin: 0 0.15em;
}

.mode-swap {
  display: inline-grid;
  align-items: center;
}

.mode-swap > * {
  grid-area: 1 / 1;
}

.dark-only {
  visibility: hidden;
}

.brand-name {
  justify-items: center;
}

.brand-ko {
  font-family: 'RuleSysBrand', 'Malgun Gothic', sans-serif;
  font-size: 14px;
}

.credit-ko {
  font-family: 'RuleSysBrand', 'Malgun Gothic', sans-serif;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.brand-ko-corp {
  margin-right: 0.03em;
}

.dark .dark-only {
  visibility: visible;
}

.dark .light-only {
  visibility: hidden;
}

.link-icon {
  flex: 0 0 auto;
  margin-left: 5px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.tagline {
  padding-right: 0.125rem;
  font-size: 11px;
  line-height: 1;
  font-weight: 500;
  letter-spacing: 0.06em;
  color: var(--info-tagline);
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0.75rem 1.25rem 14px;
}

.card-main {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.icon-box {
  display: flex;
  flex-shrink: 0;
  justify-content: center;
  width: 90px;
}

.app-icon {
  display: block;
  box-sizing: border-box;
  width: 58px;
  height: 58px;
  border: 1px solid var(--info-icon-border);
  border-radius: 0.75rem;
  box-shadow:
    0 1px 3px 0 rgb(0 0 0 / 0.1),
    0 1px 2px -1px rgb(0 0 0 / 0.1);
}

.rows {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
  font-size: 13px;
  color: var(--info-label);
}

.row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.chips-row {
  flex-wrap: wrap;
  gap: 0.375rem;
}

.value {
  font-weight: 600;
  color: var(--info-value);
}

.chips {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  line-height: calc(1 / 0.75);
}

.chip {
  padding: 0.125rem 0.375rem;
  border: 1px solid var(--info-chip-border);
  border-radius: 0.375rem;
  background: var(--info-chip-bg);
  color: var(--info-chip-text);
  font-weight: 600;
}

.credit {
  margin: 0;
  font-size: 0.875rem;
  line-height: 1.625;
  text-align: left;
  color: var(--info-label);
}
</style>
