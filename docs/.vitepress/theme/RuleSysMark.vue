<script setup>
import { useId } from 'vue'
import { markShapes, markViewBox } from './brandMark.js'

const shapes = markShapes(`rule-sys-${useId().replace(/\W/g, '')}`)
</script>

<template>
  <svg xmlns="http://www.w3.org/2000/svg" :viewBox="markViewBox" aria-hidden="true" v-html="shapes" />
</template>
