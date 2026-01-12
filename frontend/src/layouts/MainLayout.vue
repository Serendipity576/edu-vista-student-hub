<template>
  <el-container class="main-layout">
    <el-header class="header">
      <div class="header-left">
        <div class="logo">
          <el-icon class="logo-icon"><Document /></el-icon>
          <h1>学生信息管理系统</h1>
        </div>
      </div>
      <div class="header-right">
        <el-tooltip content="切换主题" placement="bottom">
          <el-button 
            :icon="themeStore.isDark ? 'Sunny' : 'Moon'" 
            circle 
            @click="themeStore.toggleTheme"
            class="theme-btn"
          />
        </el-tooltip>
        <el-dropdown trigger="click" @command="handleCommand" class="user-dropdown">
          <span class="user-info">
            <el-avatar :src="authStore.user?.avatar" :size="36" class="user-avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <span class="username">{{ authStore.user?.username }}</span>
            <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout" :icon="SwitchButton">
                退出登录
              </el-dropdown-item>
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
import { DataAnalysis, User, Picture, Document, ArrowDown, SwitchButton } from '@element-plus/icons-vue'

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
  background: linear-gradient(135deg, var(--el-color-primary) 0%, #66b1ff 100%);
  border-bottom: none;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  .header-left {
    .logo {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .logo-icon {
        font-size: 28px;
        color: white;
      }
      
      h1 {
        font-size: 22px;
        font-weight: 600;
        color: white;
        margin: 0;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .theme-btn {
      background: rgba(255, 255, 255, 0.2);
      border: 1px solid rgba(255, 255, 255, 0.3);
      color: white;
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba(255, 255, 255, 0.3);
        transform: rotate(15deg);
      }
    }
    
    .user-dropdown {
      .user-info {
        display: flex;
        align-items: center;
        gap: 10px;
        cursor: pointer;
        padding: 6px 12px;
        border-radius: 20px;
        background: rgba(255, 255, 255, 0.15);
        backdrop-filter: blur(10px);
        transition: all 0.3s ease;
        
        &:hover {
          background: rgba(255, 255, 255, 0.25);
        }
        
        .user-avatar {
          border: 2px solid rgba(255, 255, 255, 0.5);
        }
        
        .username {
          font-size: 14px;
          font-weight: 500;
          color: white;
        }
        
        .dropdown-icon {
          font-size: 12px;
          color: white;
          transition: transform 0.3s ease;
        }
      }
      
      &:hover .user-info .dropdown-icon {
        transform: rotate(180deg);
      }
    }
  }
}

.sidebar {
  background: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color-lighter);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.sidebar-menu {
  border-right: none;
  height: calc(100vh - 60px);
  padding-top: 12px;
  
  :deep(.el-menu-item) {
    margin: 4px 8px;
    border-radius: 8px;
    transition: all 0.3s ease;
    
    &:hover {
      background-color: var(--el-color-primary-light-9);
      transform: translateX(4px);
    }
    
    &.is-active {
      background: linear-gradient(90deg, var(--el-color-primary-light-9), transparent);
      color: var(--el-color-primary);
      font-weight: 600;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 4px;
        height: 60%;
        background: var(--el-color-primary);
        border-radius: 0 4px 4px 0;
      }
    }
    
    .el-icon {
      margin-right: 8px;
      font-size: 18px;
    }
  }
}

.main-content {
  background-color: var(--el-bg-color-page);
  padding: 24px;
  overflow-y: auto;
}

@include mobile {
  .header {
    padding: 0 16px;
    
    .header-left .logo h1 {
      font-size: 18px;
    }
    
    .header-right .user-dropdown .user-info .username {
      display: none;
    }
  }
  
  .sidebar {
    width: 64px !important;
  }
  
  .main-content {
    padding: 16px;
  }
}
</style>

