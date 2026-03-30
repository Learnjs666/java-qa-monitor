import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/token'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/login/LoginView.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/register/RegisterView.vue')
    },
    {
      path: '/',
      component: () => import('../layout/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/dashboard/DashboardView.vue')
        },
        {
          path: 'project',
          name: 'ProjectList',
          component: () => import('../views/project/ProjectListView.vue')
        },
        {
          path: 'project/:id',
          name: 'ProjectDetail',
          component: () => import('../views/project/ProjectDetailView.vue')
        },
        {
          path: 'scan/:taskId',
          name: 'ScanDetail',
          component: () => import('../views/scan/ScanDetailView.vue')
        },
        {
          path: 'issues/:taskId',
          name: 'IssueList',
          component: () => import('../views/scan/IssueListView.vue')
        },
        {
          path: 'rules',
          name: 'RuleConfig',
          component: () => import('../views/setting/RuleConfigView.vue')
        },
        {
          path: 'users',
          name: 'UserManagement',
          component: () => import('../views/setting/UserManagementView.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = getToken()
  if (to.path === '/login' || to.path === '/register') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
