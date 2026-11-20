<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { markImage } from './brandMark.js'

const MAX_TILT = 10

const stageRef = ref(null)
const pointer = ref(null)
let frame = 0

const isReducedMotion = () => matchMedia('(prefers-reduced-motion: reduce)').matches

const style = computed(() => {
  const { x, y } = pointer.value ?? { x: 0.5, y: 0.5 }
  return {
    '--holo-mark': markImage,
    '--px': `${x * 100}%`,
    '--py': `${y * 100}%`,
    '--rx': `${(y - 0.5) * MAX_TILT * 1.2}deg`,
    '--ry': `${(0.5 - x) * MAX_TILT * 2}deg`,
    '--edge': String(Math.min(1, Math.hypot(x - 0.5, y - 0.5) * 2)),
    '--shadow-x': `${(x - 0.5) * 40}px`,
    '--shadow-y': `${20 + (y - 0.5) * 32}px`,
  }
})

const clamp = (value) => Math.min(1, Math.max(0, value))

const handlePointerMove = (e) => {
  cancelAnimationFrame(frame)
  frame = requestAnimationFrame(() => {
    const rect = stageRef.value?.getBoundingClientRect()
    if (isReducedMotion() || !rect?.width || !rect.height) return
    pointer.value = { x: clamp((e.clientX - rect.left) / rect.width), y: clamp((e.clientY - rect.top) / rect.height) }
  })
}

const handlePointerLeave = () => {
  cancelAnimationFrame(frame)
  pointer.value = null
}

onBeforeUnmount(() => cancelAnimationFrame(frame))
</script>

<template>
  <div
    ref="stageRef"
    class="holo-stage"
    :style="style"
    @pointerenter="handlePointerMove"
    @pointermove="handlePointerMove"
    @pointerleave="handlePointerLeave"
    @pointercancel="handlePointerLeave"
  >
    <div class="holo-card" :class="{ 'is-active': pointer }">
      <div class="holo-content">
        <slot />
      </div>
      <div class="holo-foil" aria-hidden="true" />
      <div class="holo-shine" aria-hidden="true" />
    </div>
  </div>
</template>

<style scoped>
.holo-stage {
  --holo-foil-pale:
    radial-gradient(
      circle 7rem at var(--px) var(--py),
      rgba(255, 255, 255, 0.36) 0%,
      rgba(186, 230, 253, 0.18) 40%,
      transparent 68%
    ),
    repeating-linear-gradient(
      calc(60deg + var(--rx) * 2),
      rgba(255, 255, 255, 0.55) 0px,
      rgba(255, 255, 255, 0.55) 1.2px,
      transparent 1.2px,
      transparent 3.8px
    ),
    linear-gradient(
      calc(118deg + var(--ry) * 3.8 + var(--rx) * 2),
      rgba(186, 230, 253, 0.95) 0%,
      rgba(125, 211, 252, 0.92) 16%,
      rgba(165, 243, 252, 0.95) 32%,
      rgba(236, 254, 255, 0.94) 48%,
      rgba(167, 243, 208, 0.92) 64%,
      rgba(196, 181, 253, 0.92) 80%,
      rgba(186, 230, 253, 0.95) 100%
    );
  --holo-shine-pale:
    radial-gradient(
      circle 5.5rem at var(--px) var(--py),
      rgba(255, 255, 255, 0.4) 0%,
      rgba(224, 242, 254, 0.24) 30%,
      rgba(165, 243, 252, 0.1) 58%,
      transparent 75%
    ),
    linear-gradient(
      calc(125deg - var(--ry) * 3),
      transparent 18%,
      rgba(255, 255, 255, 0.22) 44%,
      rgba(255, 255, 255, 0.42) 50%,
      rgba(255, 255, 255, 0.22) 56%,
      transparent 82%
    );
  --holo-foil-vivid:
    radial-gradient(
      circle 7rem at var(--px) var(--py),
      rgba(45, 212, 191, 0.5) 0%,
      rgba(6, 182, 212, 0.25) 40%,
      transparent 68%
    ),
    repeating-linear-gradient(
      calc(60deg + var(--rx) * 2),
      rgba(255, 255, 255, 0.2) 0px,
      rgba(255, 255, 255, 0.2) 1.2px,
      transparent 1.2px,
      transparent 3.8px
    ),
    linear-gradient(
      calc(118deg + var(--ry) * 3.8 + var(--rx) * 2),
      rgba(79, 70, 229, 0.85) 0%,
      rgba(14, 165, 233, 0.82) 16%,
      rgba(6, 182, 212, 0.88) 32%,
      rgba(45, 212, 191, 0.85) 48%,
      rgba(16, 185, 129, 0.82) 64%,
      rgba(139, 92, 246, 0.84) 80%,
      rgba(79, 70, 229, 0.85) 100%
    );
  --holo-shine-vivid:
    radial-gradient(
      circle 5.5rem at var(--px) var(--py),
      rgba(255, 255, 255, 0.95) 0%,
      rgba(167, 243, 208, 0.55) 30%,
      rgba(56, 189, 248, 0.25) 58%,
      transparent 75%
    ),
    linear-gradient(
      calc(125deg - var(--ry) * 3),
      transparent 18%,
      rgba(255, 255, 255, 0.45) 44%,
      rgba(255, 255, 255, 0.9) 50%,
      rgba(255, 255, 255, 0.45) 56%,
      transparent 82%
    );
  position: relative;
  display: block;
  width: 100%;
  overflow: visible;
  touch-action: none;
}

.holo-card {
  position: relative;
  border-radius: 0.75rem;
  isolation: isolate;
  overflow: visible;
  transform: perspective(640px) rotateX(var(--rx)) rotateY(var(--ry)) translateZ(0);
  transform-origin: center center;
  backface-visibility: hidden;
  -webkit-font-smoothing: antialiased;
  will-change: transform;
  box-shadow: var(--shadow-x) var(--shadow-y) 36px rgba(0, 0, 0, 0.28);
  transition:
    transform 0.5s ease-out,
    box-shadow 0.5s ease-out;
}

.holo-card.is-active {
  transition:
    transform 0s,
    box-shadow 0s;
  box-shadow: var(--shadow-x) var(--shadow-y) 36px rgba(0, 0, 0, 0.3);
}

.holo-content {
  position: relative;
  z-index: 1;
}

.holo-foil,
.holo-shine {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  overflow: hidden;
  opacity: 0;
  mix-blend-mode: screen;
  transition: opacity 0.4s ease;
  mask-image: var(--holo-mark);
  mask-size: 6.9rem auto;
  mask-position: right 0.25rem top 55%;
  mask-repeat: no-repeat;
}

.holo-foil {
  z-index: 3;
  background: var(--holo-foil-pale);
}

.holo-shine {
  z-index: 4;
  background: var(--holo-shine-pale);
}

.holo-card.is-active .holo-foil {
  opacity: calc(0.78 + var(--edge) * 0.16);
  transition: opacity 70ms linear;
}

.holo-card.is-active .holo-shine {
  opacity: calc(0.34 + var(--edge) * 0.14);
  transition: opacity 70ms linear;
}

.dark .holo-foil {
  background: var(--holo-foil-vivid);
}

.dark .holo-card.is-active .holo-foil {
  opacity: calc(0.72 + var(--edge) * 0.18);
}

.dark .holo-shine {
  background: var(--holo-shine-vivid);
}

.dark .holo-card.is-active .holo-shine {
  opacity: calc(0.45 + var(--edge) * 0.25);
}

.dark .holo-card.is-active {
  box-shadow: var(--shadow-x) var(--shadow-y) 36px rgba(0, 0, 0, 0.28);
}

@media (prefers-reduced-motion: reduce) {
  .holo-card {
    transform: none;
    transition: none;
  }
}
</style>
