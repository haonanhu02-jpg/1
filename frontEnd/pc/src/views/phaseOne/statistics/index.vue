<template>
  <div class="phase-page">
    <div class="g-card">
      <el-form :model="query" inline>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期" />
        </el-form-item>
        <el-form-item label="渠道">
          <el-select v-model="query.channelCodeId" clearable filterable placeholder="全部渠道" style="width: 180px">
            <el-option v-for="item in userCodeOptions" :key="item.val" :label="item.key" :value="item.val" />
          </el-select>
        </el-form-item>
        <el-form-item label="员工">
          <el-input v-model="query.userId" clearable placeholder="员工 UserID" />
        </el-form-item>
        <el-form-item label="群池ID">
          <el-input v-model="query.groupPoolId" clearable placeholder="群池ID" />
        </el-form-item>
        <el-form-item label="客户群ID">
          <el-input v-model="query.chatId" clearable placeholder="chat_id" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadReport">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button @click="downloadStatistics">导出CSV</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="g-card summary-card">
      <el-row :gutter="16">
        <el-col v-for="item in summaryItems" :key="item.key" :span="4">
          <el-statistic :title="item.title" :value="report.summary?.[item.key] || 0" />
        </el-col>
      </el-row>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="渠道统计" name="channel">
        <div class="g-card">
          <el-table v-loading="loading" :data="report.channelStats || []" stripe>
            <el-table-column prop="channelCodeName" label="渠道" min-width="150" />
            <el-table-column prop="channelCodeId" label="渠道ID" min-width="120" />
            <el-table-column prop="addFriendCount" label="新增好友数" width="120" />
            <el-table-column prop="joinCount" label="成功进群人数" width="130" />
            <el-table-column prop="leaveCount" label="退群人数" width="100" />
            <el-table-column prop="joinConversionRate" label="进群转化率" width="120" />
            <el-table-column prop="welcomeSuccessCount" label="欢迎语成功" width="120" />
            <el-table-column prop="welcomeFailureCount" label="欢迎语失败" width="120" />
          </el-table>
        </div>
      </el-tab-pane>
      <el-tab-pane label="员工统计" name="employee">
        <div class="g-card">
          <el-table v-loading="loading" :data="report.employeeStats || []" stripe>
            <el-table-column prop="userId" label="员工ID" min-width="140" />
            <el-table-column prop="userName" label="员工名称" min-width="120" />
            <el-table-column prop="customerCount" label="承接客户数" width="120" />
            <el-table-column prop="joinCount" label="客户进群人数" width="130" />
            <el-table-column prop="joinConversionRate" label="进群转化率" width="120" />
            <el-table-column label="渠道分布" min-width="220">
              <template #default="{ row }">{{ formatDistribution(row.channelDistribution) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      <el-tab-pane label="群池统计" name="groupPool">
        <div class="g-card">
          <el-table v-loading="loading" :data="report.groupPoolStats || []" stripe>
            <el-table-column prop="groupPoolName" label="群池" min-width="160" />
            <el-table-column prop="availableChatCount" label="可用群数量" width="120" />
            <el-table-column prop="currentMemberCount" label="群当前人数" width="120" />
            <el-table-column prop="remainingCapacity" label="剩余容量" width="120" />
            <el-table-column prop="thresholdReachedCount" label="达阈值群数" width="120" />
            <el-table-column prop="noAvailableAlertCount" label="无可用群次数" width="130" />
          </el-table>
        </div>
      </el-tab-pane>
      <el-tab-pane label="异常统计" name="exception">
        <div class="g-card">
          <el-row :gutter="16">
            <el-col v-for="item in exceptionItems" :key="item.key" :span="6">
              <el-statistic :title="item.title" :value="report.exceptionStats?.[item.key] || 0" />
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { exportStatisticsCsv, getStatisticsReport, getUserCodeOptions } from '../api'

const loading = ref(false)
const activeTab = ref('channel')
const dateRange = ref([])
const report = ref({})
const userCodeOptions = ref([])
const query = reactive({ startTime: '', endTime: '', channelCodeId: '', userId: '', groupPoolId: '', chatId: '' })
const summaryItems = [
  { key: 'addFriendCount', title: '新增好友数' },
  { key: 'joinCount', title: '成功进群' },
  { key: 'leaveCount', title: '退群人数' },
  { key: 'joinConversionRate', title: '进群转化率' },
  { key: 'welcomeSuccessCount', title: '欢迎语成功' },
  { key: 'apiFailureCount', title: '接口异常' },
]
const exceptionItems = [
  { key: 'apiFailureCount', title: '接口异常数' },
  { key: 'welcomeFailureCount', title: '欢迎语发送失败数' },
  { key: 'tagFailureCount', title: '标签打标失败数' },
  { key: 'groupQrInvalidCount', title: '群二维码失效数' },
  { key: 'callbackFailureCount', title: '回调异常数' },
  { key: 'syncFailureCount', title: '数据同步失败数' },
]

function buildQuery() {
  query.startTime = dateRange.value?.[0] || ''
  query.endTime = dateRange.value?.[1] || ''
  return { ...query, groupPoolId: query.groupPoolId || null }
}

function loadReport() {
  loading.value = true
  getStatisticsReport(buildQuery())
    .then((res) => {
      report.value = res.data || {}
    })
    .finally(() => {
      loading.value = false
    })
}

function resetQuery() {
  dateRange.value = []
  Object.assign(query, { startTime: '', endTime: '', channelCodeId: '', userId: '', groupPoolId: '', chatId: '' })
  loadReport()
}

function downloadStatistics() {
  exportStatisticsCsv(buildQuery()).then((blob) => downloadBlob(blob, 'phase_one_statistics.csv'))
}

function downloadBlob(blob, fileName) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  URL.revokeObjectURL(url)
}

function formatDistribution(value) {
  if (!value) {
    return '-'
  }
  return Object.entries(value)
    .map(([key, count]) => `${key}: ${count}`)
    .join('；')
}

onMounted(() => {
  getUserCodeOptions().then((res) => {
    userCodeOptions.value = res.data || []
  })
  loadReport()
})
</script>

<style scoped lang="scss">
.summary-card {
  margin: 12px 0;
}
</style>
