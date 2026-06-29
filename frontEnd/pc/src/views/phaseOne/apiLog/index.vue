<template>
  <div class="phase-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="业务日志" name="business">
        <div class="g-card">
          <el-form :model="businessQuery" inline>
            <el-form-item label="日志类型">
              <el-input v-model="businessQuery.logType" clearable placeholder="请输入日志类型" />
            </el-form-item>
            <el-form-item label="客户ID">
              <el-input v-model="businessQuery.externalUserid" clearable placeholder="external_userid" />
            </el-form-item>
            <el-form-item label="员工ID">
              <el-input v-model="businessQuery.userId" clearable placeholder="userid" />
            </el-form-item>
            <el-form-item label="渠道ID">
              <el-input v-model="businessQuery.channelCodeId" clearable placeholder="渠道ID" />
            </el-form-item>
            <el-form-item label="结果">
              <el-select v-model="businessQuery.success" clearable placeholder="全部" style="width: 120px">
                <el-option label="成功" :value="true" />
                <el-option label="失败" :value="false" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadBusinessList">查询</el-button>
              <el-button @click="resetBusinessQuery">重置</el-button>
              <el-button @click="downloadBusinessLog">导出CSV</el-button>
            </el-form-item>
          </el-form>
          <el-table v-loading="businessLoading" :data="businessList" stripe>
            <el-table-column prop="logType" label="日志类型" width="170" />
            <el-table-column prop="businessId" label="业务对象" min-width="180" show-overflow-tooltip />
            <el-table-column prop="externalUserid" label="客户ID" min-width="170" />
            <el-table-column prop="userId" label="员工ID" min-width="120" />
            <el-table-column prop="chatId" label="客户群ID" min-width="150" />
            <el-table-column prop="channelCodeId" label="渠道ID" min-width="120" />
            <el-table-column label="结果" width="90">
              <template #default="{ row }">
                <el-tag :type="row.success ? 'success' : 'danger'">{{ row.success ? '成功' : '失败' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="errorMsg" label="错误信息" min-width="220" show-overflow-tooltip />
            <el-table-column prop="createTime" label="创建时间" width="170" />
          </el-table>
          <pagination
            v-show="businessTotal > 0"
            :total="businessTotal"
            v-model:page="businessQuery.page"
            v-model:limit="businessQuery.size"
            @pagination="loadBusinessList" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="API 调用日志" name="api">
        <div class="g-card">
          <el-form :model="apiQuery" inline>
            <el-form-item label="API 名称">
              <el-input v-model="apiQuery.apiName" clearable placeholder="请输入 API 名称" />
            </el-form-item>
            <el-form-item label="结果">
              <el-select v-model="apiQuery.success" clearable placeholder="全部" style="width: 120px">
                <el-option label="成功" :value="true" />
                <el-option label="失败" :value="false" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadApiList">查询</el-button>
              <el-button @click="resetApiQuery">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table v-loading="apiLoading" :data="apiList" stripe>
            <el-table-column prop="apiName" label="API 名称" min-width="180" />
            <el-table-column prop="requestSummary" label="请求摘要" min-width="240" show-overflow-tooltip />
            <el-table-column prop="responseSummary" label="响应摘要" min-width="220" show-overflow-tooltip />
            <el-table-column label="结果" width="90">
              <template #default="{ row }">
                <el-tag :type="row.success ? 'success' : 'danger'">{{ row.success ? '成功' : '失败' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="costMs" label="耗时(ms)" width="110" />
            <el-table-column prop="errorMsg" label="错误信息" min-width="220" show-overflow-tooltip />
            <el-table-column prop="createTime" label="调用时间" width="170" />
          </el-table>
          <pagination
            v-show="apiTotal > 0"
            :total="apiTotal"
            v-model:page="apiQuery.page"
            v-model:limit="apiQuery.size"
            @pagination="loadApiList" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { exportBusinessLogCsv, getApiCallLogList, getBusinessLogList } from '../api'

const activeTab = ref('business')
const businessLoading = ref(false)
const apiLoading = ref(false)
const businessList = ref([])
const apiList = ref([])
const businessTotal = ref(0)
const apiTotal = ref(0)
const businessQuery = reactive({ logType: '', externalUserid: '', userId: '', channelCodeId: '', success: null, page: 1, size: 10 })
const apiQuery = reactive({ apiName: '', success: null, page: 1, size: 10 })

function loadBusinessList() {
  businessLoading.value = true
  getBusinessLogList(businessQuery)
    .then((res) => {
      businessList.value = res.data || []
      businessTotal.value = res.count || 0
    })
    .finally(() => {
      businessLoading.value = false
    })
}

function loadApiList() {
  apiLoading.value = true
  getApiCallLogList(apiQuery)
    .then((res) => {
      apiList.value = res.data || []
      apiTotal.value = res.count || 0
    })
    .finally(() => {
      apiLoading.value = false
    })
}

function resetBusinessQuery() {
  Object.assign(businessQuery, { logType: '', externalUserid: '', userId: '', channelCodeId: '', success: null, page: 1, size: 10 })
  loadBusinessList()
}

function resetApiQuery() {
  Object.assign(apiQuery, { apiName: '', success: null, page: 1, size: 10 })
  loadApiList()
}

function downloadBusinessLog() {
  exportBusinessLogCsv(businessQuery).then((blob) => downloadBlob(blob, 'phase_one_business_log.csv'))
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
  if (value === 'api' && apiList.value.length === 0) {
    loadApiList()
  }
})

onMounted(loadBusinessList)
</script>
