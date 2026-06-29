<template>
  <div class="phase-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="进群 / 退群记录" name="relation">
        <div class="g-card">
          <el-form :model="relationQuery" inline>
            <el-form-item label="客户ID">
              <el-input v-model="relationQuery.externalUserid" clearable placeholder="external_userid" />
            </el-form-item>
            <el-form-item label="客户群ID">
              <el-input v-model="relationQuery.chatId" clearable placeholder="chat_id" />
            </el-form-item>
            <el-form-item label="事件">
              <el-select v-model="relationQuery.eventType" clearable placeholder="全部" style="width: 130px">
                <el-option label="进群" value="JOIN" />
                <el-option label="退群" value="LEAVE" />
                <el-option label="更新" value="UPDATE" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadRelationList">查询</el-button>
              <el-button @click="resetRelationQuery">重置</el-button>
              <el-button @click="downloadRelation">导出CSV</el-button>
            </el-form-item>
          </el-form>
          <el-table v-loading="relationLoading" :data="relationList" stripe>
            <el-table-column prop="externalUserid" label="客户ID" min-width="180" />
            <el-table-column prop="customerName" label="客户名称" min-width="120" />
            <el-table-column prop="chatName" label="客户群" min-width="160" />
            <el-table-column prop="chatId" label="客户群ID" min-width="180" />
            <el-table-column prop="groupPoolName" label="群池" min-width="140" />
            <el-table-column prop="channelState" label="渠道 state" min-width="180" />
            <el-table-column label="事件" width="90">
              <template #default="{ row }">
                <el-tag :type="eventTypeMap[row.eventType]?.type || 'info'">{{ eventTypeMap[row.eventType]?.label || row.eventType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="relationStatus" label="状态" width="100" />
            <el-table-column prop="eventTime" label="事件时间" width="170" />
          </el-table>
          <pagination
            v-show="relationTotal > 0"
            :total="relationTotal"
            v-model:page="relationQuery.page"
            v-model:limit="relationQuery.size"
            @pagination="loadRelationList" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="分群引导记录" name="route">
        <div class="g-card">
          <el-form :model="routeQuery" inline>
            <el-form-item label="客户ID">
              <el-input v-model="routeQuery.externalUserid" clearable placeholder="external_userid" />
            </el-form-item>
            <el-form-item label="规则类型">
              <el-select v-model="routeQuery.ruleType" clearable placeholder="全部" style="width: 150px">
                <el-option v-for="item in ruleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="匹配状态">
              <el-select v-model="routeQuery.routeStatus" clearable placeholder="全部" style="width: 150px">
                <el-option v-for="item in routeStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadRouteList">查询</el-button>
              <el-button @click="resetRouteQuery">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table v-loading="routeLoading" :data="routeList" stripe>
            <el-table-column prop="externalUserid" label="客户ID" min-width="180" />
            <el-table-column prop="userId" label="承接员工" min-width="120" />
            <el-table-column prop="channelCodeName" label="渠道活码" min-width="150" />
            <el-table-column prop="tagIds" label="标签ID" min-width="170" show-overflow-tooltip />
            <el-table-column label="规则类型" width="130">
              <template #default="{ row }">
                <el-tag>{{ ruleTypeMap[row.ruleType] || row.ruleType || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="matchedRuleName" label="命中规则" min-width="150" />
            <el-table-column prop="targetPoolName" label="目标群池" min-width="140" />
            <el-table-column prop="targetChatName" label="目标客户群" min-width="150" />
            <el-table-column label="匹配状态" width="130">
              <template #default="{ row }">
                <el-tag :type="routeStatusTag(row.routeStatus)">{{ routeStatusMap[row.routeStatus] || row.routeStatus }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="failureReason" label="失败原因" min-width="220" show-overflow-tooltip />
            <el-table-column prop="eventTime" label="事件时间" width="170" />
          </el-table>
          <pagination
            v-show="routeTotal > 0"
            :total="routeTotal"
            v-model:page="routeQuery.page"
            v-model:limit="routeQuery.size"
            @pagination="loadRouteList" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { exportCustomerChatRelationCsv, getCustomerChatRelationList, getCustomerRouteLogList } from '../api'

const activeTab = ref('relation')
const relationLoading = ref(false)
const routeLoading = ref(false)
const relationList = ref([])
const routeList = ref([])
const relationTotal = ref(0)
const routeTotal = ref(0)
const relationQuery = reactive({ externalUserid: '', chatId: '', eventType: '', page: 1, size: 10 })
const routeQuery = reactive({ externalUserid: '', ruleType: '', routeStatus: '', page: 1, size: 10 })
const eventTypeMap = {
  JOIN: { label: '进群', type: 'success' },
  LEAVE: { label: '退群', type: 'warning' },
  UPDATE: { label: '更新', type: 'info' },
}
const ruleTypeOptions = [
  { label: '客户标签规则', value: 'CUSTOMER_TAG' },
  { label: '渠道标签规则', value: 'CHANNEL_TAG' },
  { label: '渠道默认群池', value: 'CHANNEL_DEFAULT' },
  { label: '全局默认群池', value: 'GLOBAL_DEFAULT' },
]
const routeStatusOptions = [
  { label: '匹配成功', value: 'MATCHED' },
  { label: '未匹配规则', value: 'NO_ROUTE_RULE' },
  { label: '目标群池不可用', value: 'TARGET_POOL_DISABLED' },
  { label: '无可用客户群', value: 'NO_AVAILABLE_CHAT' },
]
const ruleTypeMap = ruleTypeOptions.reduce((map, item) => ({ ...map, [item.value]: item.label }), {})
const routeStatusMap = routeStatusOptions.reduce((map, item) => ({ ...map, [item.value]: item.label }), {})

function loadRelationList() {
  relationLoading.value = true
  getCustomerChatRelationList(relationQuery)
    .then((res) => {
      relationList.value = res.data || []
      relationTotal.value = res.count || 0
    })
    .finally(() => {
      relationLoading.value = false
    })
}

function loadRouteList() {
  routeLoading.value = true
  getCustomerRouteLogList(routeQuery)
    .then((res) => {
      routeList.value = res.data || []
      routeTotal.value = res.count || 0
    })
    .finally(() => {
      routeLoading.value = false
    })
}

function resetRelationQuery() {
  Object.assign(relationQuery, { externalUserid: '', chatId: '', eventType: '', page: 1, size: 10 })
  loadRelationList()
}

function resetRouteQuery() {
  Object.assign(routeQuery, { externalUserid: '', ruleType: '', routeStatus: '', page: 1, size: 10 })
  loadRouteList()
}

function routeStatusTag(status) {
  if (status === 'MATCHED') {
    return 'success'
  }
  if (status === 'NO_AVAILABLE_CHAT' || status === 'TARGET_POOL_DISABLED') {
    return 'danger'
  }
  return 'warning'
}

function downloadRelation() {
  exportCustomerChatRelationCsv(relationQuery).then((blob) => downloadBlob(blob, 'phase_one_customer_chat_relation.csv'))
}

function downloadBlob(blob, fileName) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  URL.revokeObjectURL(url)
}

watch(activeTab, (value) => {
  if (value === 'route' && routeList.value.length === 0) {
    loadRouteList()
  }
})

onMounted(loadRelationList)
</script>
