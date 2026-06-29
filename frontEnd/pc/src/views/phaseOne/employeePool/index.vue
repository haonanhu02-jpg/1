<template>
  <div class="phase-page">
    <div class="g-card">
      <el-form :model="query" inline>
        <el-form-item label="员工名称">
          <el-input v-model="query.employeeName" clearable placeholder="请输入员工名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.enabled" clearable placeholder="全部" style="width: 120px">
            <el-option label="启用" :value="true" />
            <el-option label="停用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="toolbar">
        <el-button type="primary" @click="openDialog()">新增员工池</el-button>
      </div>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="employeeName" label="员工名称" min-width="140" />
        <el-table-column prop="employeeId" label="员工 UserID" min-width="160" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" fixed="right" width="140">
          <template #default="{ row }">
            <el-button text type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button text type="danger" @click="remove(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" title="员工池配置" width="560px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="员工名称" required>
          <el-input v-model="form.employeeName" placeholder="请输入员工名称" />
        </el-form-item>
        <el-form-item label="员工 UserID" required>
          <el-input v-model="form.employeeId" placeholder="请输入企业微信成员 UserID" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteEmployeePool, getEmployeePoolList, saveEmployeePool, updateEmployeePool } from '../api'

const loading = ref(false)
const dialogVisible = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ employeeName: '', enabled: null, page: 1, size: 10 })
const form = reactive(defaultForm())

function defaultForm() {
  return { id: null, employeeId: '', employeeName: '', sort: 0, enabled: true, remark: '' }
}

// 加载员工池列表，分页参数沿用 Iyque 全局 pagination 组件的 page/size。
function loadList() {
  loading.value = true
  getEmployeePoolList(query)
    .then((res) => {
      list.value = res.data || []
      total.value = res.count || 0
    })
    .finally(() => {
      loading.value = false
    })
}

function resetQuery() {
  Object.assign(query, { employeeName: '', enabled: null, page: 1, size: 10 })
  loadList()
}

function openDialog(row) {
  Object.assign(form, defaultForm(), row || {})
  dialogVisible.value = true
}

function submit() {
  if (!form.employeeId || !form.employeeName) {
    ElMessage.warning('请填写员工名称和员工 UserID')
    return
  }
  const request = form.id ? updateEmployeePool : saveEmployeePool
  request({ ...form }).then(() => {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
  })
}

function remove(row) {
  ElMessageBox.confirm('确认删除该员工池配置？', '提示', { type: 'warning' }).then(() => {
    deleteEmployeePool(row.id).then(() => {
      ElMessage.success('删除成功')
      loadList()
    })
  })
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.toolbar {
  margin-bottom: 12px;
}
</style>
