<template>
  <div class="phase-page">
    <div class="g-card">
      <el-form :model="query" inline>
        <el-form-item label="任务类型">
          <el-input v-model="query.taskType" clearable placeholder="请输入任务类型" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px">
            <el-option label="待重试" value="PENDING" />
            <el-option label="失败" value="FAILED" />
            <el-option label="已耗尽" value="EXHAUSTED" />
            <el-option label="成功" value="SUCCESS" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="taskType" label="任务类型" width="170" />
        <el-table-column prop="businessKey" label="业务键" min-width="200" show-overflow-tooltip />
        <el-table-column prop="errorMsg" label="失败原因" min-width="260" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="110" />
        <el-table-column prop="retryCount" label="重试次数" width="100" />
        <el-table-column prop="nextRetryTime" label="下次重试" width="170" />
        <el-table-column prop="lastRetryTime" label="最近重试" width="170" />
        <el-table-column label="操作" fixed="right" width="100">
          <template #default="{ row }">
            <el-button text type="primary" :disabled="row.status === 'SUCCESS'" @click="retry(row)">重试</el-button>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getFailedTaskList, retryFailedTask } from '../api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ taskType: '', status: '', page: 1, size: 10 })

function loadList() {
  loading.value = true
  getFailedTaskList(query)
    .then((res) => {
      list.value = res.data || []
      total.value = res.count || 0
    })
    .finally(() => {
      loading.value = false
    })
}

function resetQuery() {
  Object.assign(query, { taskType: '', status: '', page: 1, size: 10 })
  loadList()
}

function retry(row) {
  retryFailedTask(row.id).then(() => {
    ElMessage.success('已加入重试队列')
    loadList()
  })
}

onMounted(loadList)
</script>
