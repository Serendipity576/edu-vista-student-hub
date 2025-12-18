<template>
  <div :class="['app-container', themeStore.isDark ? 'dark-theme' : 'light-theme']">
    <router-view />
  </div>
</template>

<script setup>
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
</script>

<style lang="scss">
.app-container {
  min-height: 100vh;
  transition: background-color 0.3s, color 0.3s;
}

.light-theme {
  background-color: #f5f5f5;
  color: #333;
}

.dark-theme {
  background-color: #1a1a1a;
  color: #e0e0e0;
}
</style>

