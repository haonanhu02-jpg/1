<template>
  <div class="phase-one-workbench">
    <section class="header">
      <div>
        <h1>一期运营工作台</h1>
        <p>当前只展示一期范围、运营后台入口和新增模块统计，原 Iyque 可复用能力仍保留原接口。</p>
      </div>
      <el-button type="primary" @click="loadData">刷新</el-button>
    </section>

    <el-row :gutter="16" class="summary">
      <el-col v-for="item in summaryItems" :key="item.key" :span="6">
        <el-statistic :title="item.title" :value="summary[item.key] || 0" />
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel">
      <template #header>一期范围清单</template>
      <el-table v-loading="loading" :data="scopeList" stripe>
        <el-table-column prop="moduleName" label="模块" min-width="160" />
        <el-table-column prop="buildType" label="落地方式" width="120" />
        <el-table-column prop="description" label="说明" min-width="360" />
      </el-table>
    </el-card>

    <el-card shadow="never" class="panel">
      <template #header>运营后台入口</template>
      <el-table v-loading="loading" :data="menuList" stripe>
        <el-table-column prop="title" label="菜单" min-width="150" />
        <el-table-column prop="path" label="路径" min-width="220" />
        <el-table-column prop="description" label="说明" min-width="360" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getPhaseOneMenus, getPhaseOneScope, getStatisticsSummary } from '../api'

const loading = ref(false)
const scopeList = ref([])
const menuList = ref([])
const summary = ref({})
const summaryItems = computed(() => [
  { key: 'employeePoolCount', title: '员工池配置' },
  { key: 'groupPoolCount', title: '群池数量' },
  { key: 'routeRuleCount', title: '分群规则' },
  { key: 'routeMatchedCount', title: '分群成功' },
  { key: 'routeNoAvailableChatCount', title: '无可用群' },
  { key: 'pendingHighAlertCount', title: '高等级告警' },
])

// 工作台聚合只读数据，避免在首页承载业务写操作。
function loadData() {
  loading.value = true
  Promise.all([getPhaseOneScope(), getPhaseOneMenus(), getStatisticsSummary()])
    .then(([scopeRes, menuRes, summaryRes]) => {
      scopeList.value = scopeRes.data || []
      menuList.value = menuRes.data || []
      summary.value = summaryRes.data || {}
    })
    .finally(() => {
      loading.value = false
    })
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.phase-one-workbench {
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;

    h1 {
      margin: 0 0 8px;
      font-size: 22px;
      font-weight: 600;
    }

    p {
      margin: 0;
      color: var(--font-black-6);
    }
  }

  .summary {
    margin-bottom: 16px;
  }

  .panel {
    margin-bottom: 16px;
    border-radius: 6px;
  }
}
</style>
