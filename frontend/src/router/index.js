import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'students',
        name: 'StudentList',
        component: () => import('@/views/StudentList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'students/:id',
        name: 'StudentDetail',
        component: () => import('@/views/StudentDetail.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'students/:id/edit',
        name: 'StudentEdit',
        component: () => import('@/views/StudentEdit.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'gallery',
        name: 'Gallery',
        component: () => import('@/views/Gallery.vue'),
        meta: { requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
    
    if (to.meta.roles) {
      const userRole = authStore.user?.role
      if (!to.meta.roles.includes(userRole)) {
        next({ name: 'StudentList' })
        return
      }
    }
  }
  
  if (to.path === '/login' && authStore.isAuthenticated) {
    next({ name: 'Dashboard' })
    return
  }
  
  next()
})

export default router

