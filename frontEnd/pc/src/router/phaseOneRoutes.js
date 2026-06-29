/* Layout */
import Layout from '@/layout/index.vue'

// 一期运营后台路由，承载 01-07 已落地入口。
export const phaseOneNavRoutes = [
  {
    path: '/phase-one',
    component: Layout,
    redirect: '/phase-one/workbench',
    meta: { title: '一期运营后台', icon: 'system' },
    children: [
      {
        path: 'workbench',
        component: () => import('@/views/phaseOne/workbench/index.vue'),
        meta: { title: '运营工作台' },
      },
      {
        path: 'mock',
        component: () => import('@/views/phaseOne/mock/index.vue'),
        meta: { title: '本地模拟' },
      },
      {
        path: 'channel-code',
        component: () => import('@/views/liveCode/index'),
        meta: { title: '渠道活码', reuseModule: '复用 Iyque 员工活码' },
      },
      {
        path: 'employee-pool',
        component: () => import('@/views/phaseOne/employeePool/index.vue'),
        meta: { title: '员工池管理' },
      },
      {
        path: 'welcome-template',
        component: () => import('@/views/phaseOne/placeholder/index.vue'),
        meta: {
          title: '欢迎语模板',
          phaseOneStatus: '复用中',
          reuseModule: '当前欢迎语能力复用员工活码表单',
          todo: '后续需要独立模板库时再拆分。',
        },
      },
      {
        path: 'material',
        component: () => import('@/views/contentCenter/commonMaterial/index'),
        meta: { title: '素材库', reuseModule: '复用 Iyque 普通素材' },
      },
      {
        path: 'customer-tag',
        component: () => import('@/views/customerTag/index'),
        meta: { title: '客户标签', reuseModule: '复用 Iyque 标签管理' },
      },
      {
        path: 'group-pool',
        component: () => import('@/views/phaseOne/groupPool/index.vue'),
        meta: { title: '群池管理' },
      },
      {
        path: 'group-route-rule',
        component: () => import('@/views/phaseOne/groupRouteRule/index.vue'),
        meta: { title: '分群规则' },
      },
      {
        path: 'customer',
        component: () => import('@/views/customer/index'),
        meta: { title: '客户管理', reuseModule: '复用 Iyque 客户列表' },
      },
      {
        path: 'customer-chat-relation',
        component: () => import('@/views/phaseOne/customerChatRelation/index.vue'),
        meta: { title: '进群 / 退群记录' },
      },
      {
        path: 'statistics',
        component: () => import('@/views/phaseOne/statistics/index.vue'),
        meta: { title: '统计报表' },
      },
      {
        path: 'logs',
        component: () => import('@/views/phaseOne/apiLog/index.vue'),
        meta: { title: 'API 调用日志' },
      },
      {
        path: 'alerts',
        component: () => import('@/views/phaseOne/alerts/index.vue'),
        meta: { title: '告警中心' },
      },
      {
        path: 'failed-task',
        component: () => import('@/views/phaseOne/failedTask/index.vue'),
        meta: { title: '失败任务' },
      },
      {
        path: 'acceptance',
        component: () => import('@/views/phaseOne/acceptance/index.vue'),
        meta: { title: '验收看板' },
      },
      {
        path: 'system-config',
        component: () => import('@/views/config/index'),
        meta: { title: '系统基础配置', reuseModule: '复用 Iyque 企业微信配置' },
      },
    ],
  },
]
