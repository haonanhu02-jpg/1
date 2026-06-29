<template>
  <div class="phase-page">
    <div class="g-card">
      <div class="header">
        <h2>一期验收看板</h2>
        <el-button type="primary" @click="loadList">刷新</el-button>
      </div>
      <el-alert
        class="tip"
        type="info"
        show-icon
        :closable="false"
        title="验收状态基于当前系统数据判断；需要真实通过企业微信回调产生数据后，端到端链路才会显示通过。" />
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="item" label="验收项" min-width="160" />
        <el-table-column prop="standard" label="通过标准" min-width="420" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PASS' ? 'success' : 'warning'">{{ row.status === 'PASS' ? '通过' : '待验证' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getAcceptanceSummary } from '../api'

const loading = ref(false)
const list = ref([])

function loadList() {
  loading.value = true
  getAcceptanceSummary()
    .then((res) => {
      list.value = res.data || []
    })
    .finally(() => {
      loading.value = false
    })
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;

  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
  }
}

.tip {
  margin-bottom: 12px;
}
</style>
