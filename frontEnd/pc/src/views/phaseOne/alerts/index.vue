<template>
  <div class="phase-page">
    <div class="g-card">
      <el-form :model="query" inline>
        <el-form-item label="告警类型">
          <el-input v-model="query.alertType" clearable placeholder="请输入告警类型" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 130px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已处理" value="HANDLED" />
            <el-option label="已忽略" value="IGNORED" />
          </el-select>
        </el-form-item>
        <el-form-item label="级别">
          <el-select v-model="query.alertLevel" clearable placeholder="全部" style="width: 130px">
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button @click="downloadAlertLog">导出CSV</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="alertType" label="类型" width="160" />
        <el-table-column label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="alertLevelMap[row.alertLevel]?.type || 'info'">{{ alertLevelMap[row.alertLevel]?.label || row.alertLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertTitle" label="标题" min-width="180" />
        <el-table-column prop="alertContent" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="businessKey" label="业务键" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'warning'">{{ statusMap[row.status]?.label || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notifyStatus" label="通知状态" min-width="150" />
        <el-table-column prop="notifyError" label="通知错误" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" fixed="right" width="100">
          <template #default="{ row }">
            <el-button text type="primary" :disabled="row.status === 'HANDLED'" @click="openHandle(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="query.page"
        v-model:limit="query.size"
        @pagination="loadList" />
    </div>

    <el-dialog v-model="dialogVisible" title="处理告警" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="处理人">
          <el-input v-model="form.handledBy" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="form.status" placeholder="请选择处理状态" style="width: 100%">
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已处理" value="HANDLED" />
            <el-option label="已忽略" value="IGNORED" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="form.handledRemark" type="textarea" :rows="4" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { exportAlertLogCsv, getAlertLogList, handleAlertLog } from '../api'

const loading = ref(false)
const dialogVisible = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ alertType: '', status: '', alertLevel: '', page: 1, size: 10 })
const form = reactive({ id: null, handledBy: '', handledRemark: '', status: 'HANDLED' })
const alertLevelMap = {
  HIGH: { label: '高', type: 'danger' },
  MEDIUM: { label: '中', type: 'warning' },
  LOW: { label: '低', type: 'info' },
  ERROR: { label: '高', type: 'danger' },
}
const statusMap = {
  PENDING: { label: '待处理', type: 'warning' },
  PROCESSING: { label: '处理中', type: 'primary' },
  HANDLED: { label: '已处理', type: 'success' },
  IGNORED: { label: '已忽略', type: 'info' },
}

function loadList() {
  loading.value = true
  getAlertLogList(query)
    .then((res) => {
      list.value = res.data || []
      total.value = res.count || 0
    })
    .finally(() => {
      loading.value = false
    })
}

function resetQuery() {
  Object.assign(query, { alertType: '', status: '', alertLevel: '', page: 1, size: 10 })
  loadList()
}

function openHandle(row) {
  Object.assign(form, { id: row.id, handledBy: '', handledRemark: '', status: 'HANDLED' })
  dialogVisible.value = true
}

function submitHandle() {
  if (!form.handledRemark) {
    ElMessage.warning('请填写处理备注')
    return
  }
  handleAlertLog({ ...form }).then(() => {
    ElMessage.success('处理成功')
    dialogVisible.value = false
    loadList()
  })
}

function downloadAlertLog() {
  exportAlertLogCsv(query).then((blob) => downloadBlob(blob, 'phase_one_alert_log.csv'))
}

function downloadBlob(blob, fileName) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  URL.revokeObjectURL(url)
}

onMounted(loadList)
</script>
