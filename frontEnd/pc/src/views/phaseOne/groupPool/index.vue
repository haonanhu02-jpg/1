<template>
  <div class="phase-page">
    <div class="g-card">
      <el-form :model="query" inline>
        <el-form-item label="群池名称">
          <el-input v-model="query.poolName" clearable placeholder="请输入群池名称" />
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
        <el-button type="primary" @click="openDialog()">新增群池</el-button>
      </div>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="poolName" label="群池名称" min-width="160" />
        <el-table-column prop="channelCodeName" label="关联活码" min-width="150" />
        <el-table-column prop="chatCount" label="客户群数" width="100" />
        <el-table-column prop="availableChatCount" label="可用群数" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" />
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

    <el-dialog v-model="dialogVisible" title="群池配置" width="900px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="群池名称" required>
          <el-input v-model="form.poolName" placeholder="请输入群池名称" />
        </el-form-item>
        <el-form-item label="关联活码">
          <el-select v-model="form.channelCodeId" clearable filterable placeholder="请选择渠道活码" style="width: 100%" @change="syncChannelName">
            <el-option v-for="item in userCodeOptions" :key="item.val" :label="item.key" :value="item.val" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="客户群">
          <div class="chat-editor">
            <el-table :data="form.chats" border>
              <el-table-column label="客户群ID" min-width="180">
                <template #default="{ row }">
                  <el-input v-model="row.chatId" placeholder="chat_id" />
                </template>
              </el-table-column>
              <el-table-column label="客户群名称" min-width="180">
                <template #default="{ row }">
                  <el-input v-model="row.chatName" placeholder="客户群名称" />
                </template>
              </el-table-column>
              <el-table-column label="容量阈值" width="120">
                <template #default="{ row }">
                  <el-input-number v-model="row.capacityThreshold" :min="1" :max="500" controls-position="right" />
                </template>
              </el-table-column>
              <el-table-column label="当前人数" width="120">
                <template #default="{ row }">
                  <el-input-number v-model="row.currentMemberCount" :min="0" :max="500" controls-position="right" />
                </template>
              </el-table-column>
              <el-table-column label="备用" width="90">
                <template #default="{ row }">
                  <el-switch v-model="row.backupFlag" />
                </template>
              </el-table-column>
              <el-table-column label="排序" width="110">
                <template #default="{ row }">
                  <el-input-number v-model="row.sort" :min="0" controls-position="right" />
                </template>
              </el-table-column>
              <el-table-column label="启用" width="90">
                <template #default="{ row }">
                  <el-switch v-model="row.enabled" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template #default="{ $index }">
                  <el-button text type="danger" @click="form.chats.splice($index, 1)">移除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button class="add-chat" @click="addChat">添加客户群</el-button>
          </div>
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
import { deleteGroupPool, getGroupPoolList, getUserCodeOptions, saveGroupPool, updateGroupPool } from '../api'

const loading = ref(false)
const dialogVisible = ref(false)
const list = ref([])
const total = ref(0)
const userCodeOptions = ref([])
const query = reactive({ poolName: '', enabled: null, page: 1, size: 10 })
const form = reactive(defaultForm())

function defaultChat() {
  return { chatId: '', chatName: '', capacityThreshold: 180, currentMemberCount: 0, backupFlag: false, sort: 0, enabled: true }
}

function defaultForm() {
  return { id: null, poolName: '', channelCodeId: '', channelCodeName: '', enabled: true, remark: '', chats: [defaultChat()] }
}

function loadList() {
  loading.value = true
  getGroupPoolList(query)
    .then((res) => {
      list.value = res.data || []
      total.value = res.count || 0
    })
    .finally(() => {
      loading.value = false
    })
}

function loadOptions() {
  getUserCodeOptions().then((res) => {
    userCodeOptions.value = res.data || []
  })
}

function resetQuery() {
  Object.assign(query, { poolName: '', enabled: null, page: 1, size: 10 })
  loadList()
}

function openDialog(row) {
  Object.assign(form, defaultForm(), row || {})
  form.chats = row?.chats?.length ? row.chats.map((item) => ({ ...defaultChat(), ...item })) : [defaultChat()]
  dialogVisible.value = true
}

function addChat() {
  form.chats.push(defaultChat())
}

function syncChannelName(value) {
  const item = userCodeOptions.value.find((option) => option.val === value)
  form.channelCodeName = item?.key || ''
}

function submit() {
  if (!form.poolName) {
    ElMessage.warning('请填写群池名称')
    return
  }
  const request = form.id ? updateGroupPool : saveGroupPool
  request({ ...form, chats: form.chats }).then(() => {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
  })
}

function remove(row) {
  ElMessageBox.confirm('确认删除该群池？', '提示', { type: 'warning' }).then(() => {
    deleteGroupPool(row.id).then(() => {
      ElMessage.success('删除成功')
      loadList()
    })
  })
}

onMounted(() => {
  loadOptions()
  loadList()
})
</script>

<style scoped lang="scss">
.toolbar {
  margin-bottom: 12px;
}

.chat-editor {
  width: 100%;
}

.add-chat {
  margin-top: 10px;
}
</style>
