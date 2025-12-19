<template>
  <el-container class="main-layout">
    <el-header class="header">
      <div class="header-left">
        <h1>学生信息管理系统</h1>
      </div>
      <div class="header-right">
        <el-button :icon="themeStore.isDark ? 'Sunny' : 'Moon'" circle @click="themeStore.toggleTheme" />
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-info">
            <el-avatar :src="authStore.user?.avatar" :size="32" />
            <span class="username">{{ authStore.user?.username }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px" class="sidebar">
        <el-menu
          :default-active="activeMenu"
          router
          :collapse="false"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/dashboard" v-if="authStore.user?.role === 'ADMIN'">
            <el-icon><DataAnalysis /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/students">
            <el-icon><User /></el-icon>
            <span>学生列表</span>
          </el-menu-item>
          <el-menu-item index="/gallery">
            <el-icon><Picture /></el-icon>
            <span>班级相册</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { DataAnalysis, User, Picture } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'logout') {
    authStore.logout()
    router.push({ name: 'Login' })
  }
}

const handleMenuSelect = (index) => {
  // 显式跳转，确保点击菜单一定导航
  if (index) {
    router.push(index)
  }
}
</script>

<style lang="scss" scoped>
@import '../styles/mixins.scss';

.main-layout {
  height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color);
  padding: 0 20px;
  
  h1 {
    font-size: 20px;
    font-weight: 600;
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 15px;
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      
      .username {
        font-size: 14px;
      }
    }
  }
}

.sidebar {
  background-color: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color);
}

.sidebar-menu {
  border-right: none;
  height: calc(100vh - 60px);
}

.main-content {
  background-color: var(--el-bg-color-page);
  padding: 20px;
  overflow-y: auto;
}

@include mobile {
  .sidebar {
    width: 64px !important;
  }
}
</style>

