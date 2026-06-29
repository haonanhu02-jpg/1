<template>
  <div class="phase-page">
    <div class="g-card">
      <el-form :model="query" inline>
        <el-form-item label="规则名称">
          <el-input v-model="query.ruleName" clearable placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.enabled" clearable placeholder="全部" style="width: 120px">
            <el-option label="启用" :value="true" />
            <el-option label="停用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则类型">
          <el-select v-model="query.ruleType" clearable placeholder="全部" style="width: 150px">
            <el-option v-for="item in ruleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="toolbar">
        <el-button type="primary" @click="openDialog()">新增规则</el-button>
        <el-button @click="openPreview">匹配预览</el-button>
      </div>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="ruleName" label="规则名称" min-width="160" />
        <el-table-column label="规则类型" width="130">
          <template #default="{ row }">
            <el-tag>{{ ruleTypeMap[row.ruleType] || row.ruleType || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="channelCodeName" label="渠道活码" min-width="140" />
        <el-table-column prop="requiredTagIds" label="标签条件" min-width="180" />
        <el-table-column prop="targetPoolName" label="目标群池" min-width="140" />
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" title="分群规则" width="640px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="规则名称" required>
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" required>
          <el-select v-model="form.ruleType" placeholder="请选择规则类型" style="width: 100%" @change="onRuleTypeChange">
            <el-option v-for="item in ruleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="requiresChannel(form.ruleType)" label="渠道活码" required>
          <el-select v-model="form.channelCodeId" clearable filterable placeholder="不限渠道" style="width: 100%" @change="syncChannelName">
            <el-option v-for="item in userCodeOptions" :key="item.val" :label="item.key" :value="item.val" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="requiresTags(form.ruleType)" label="客户标签ID" required>
          <el-input v-model="form.requiredTagIds" placeholder="多个标签ID用英文逗号分隔" />
        </el-form-item>
        <el-form-item label="目标群池" required>
          <el-select v-model="form.targetPoolId" filterable placeholder="请选择群池" style="width: 100%">
            <el-option v-for="item in groupPoolOptions" :key="item.id" :label="item.poolName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="0" :step="10" />
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

    <el-dialog v-model="previewVisible" title="分群匹配预览" width="640px">
      <el-form :model="previewForm" label-width="120px">
        <el-form-item label="渠道活码">
          <el-select v-model="previewForm.channelCodeId" clearable filterable placeholder="请选择渠道活码" style="width: 100%">
            <el-option v-for="item in userCodeOptions" :key="item.val" :label="item.key" :value="item.val" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户标签ID">
          <el-input v-model="previewForm.requiredTagIds" placeholder="多个标签ID用英文逗号分隔" />
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="runPreview">执行预览</el-button>
      <el-descriptions v-if="previewResult" class="preview-result" border :column="1">
        <el-descriptions-item label="结果">{{ previewResult.reason }}</el-descriptions-item>
        <el-descriptions-item label="匹配路径">{{ previewResult.matchPath || '-' }}</el-descriptions-item>
        <el-descriptions-item label="规则类型">{{ ruleTypeMap[previewResult.ruleType] || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ routeStatusMap[previewResult.routeStatus] || previewResult.routeStatus || '-' }}</el-descriptions-item>
        <el-descriptions-item label="命中规则">{{ previewResult.rule?.ruleName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标群池">{{ previewResult.pool?.poolName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户群">{{ previewResult.chat?.chatName || previewResult.chat?.chatId || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteGroupRouteRule,
  getGroupPoolList,
  getGroupRouteRuleList,
  getUserCodeOptions,
  matchGroupRoutePreview,
  saveGroupRouteRule,
  updateGroupRouteRule,
} from '../api'

const loading = ref(false)
const dialogVisible = ref(false)
const previewVisible = ref(false)
const list = ref([])
const total = ref(0)
const userCodeOptions = ref([])
const groupPoolOptions = ref([])
const previewResult = ref(null)
const query = reactive({ ruleName: '', enabled: null, ruleType: '', page: 1, size: 10 })
const form = reactive(defaultForm())
const previewForm = reactive({ channelCodeId: '', requiredTagIds: '' })
const ruleTypeOptions = [
  { label: '客户标签规则', value: 'CUSTOMER_TAG' },
  { label: '渠道标签规则', value: 'CHANNEL_TAG' },
  { label: '渠道默认群池', value: 'CHANNEL_DEFAULT' },
  { label: '全局默认群池', value: 'GLOBAL_DEFAULT' },
]
const ruleTypeMap = ruleTypeOptions.reduce((map, item) => ({ ...map, [item.value]: item.label }), {})
const routeStatusMap = {
  MATCHED: '匹配成功',
  NO_ROUTE_RULE: '未匹配规则',
  TARGET_POOL_DISABLED: '目标群池不可用',
  NO_AVAILABLE_CHAT: '无可用客户群',
}

function defaultForm() {
  return {
    id: null,
    ruleName: '',
    ruleType: 'CUSTOMER_TAG',
    channelCodeId: '',
    channelCodeName: '',
    requiredTagIds: '',
    targetPoolId: null,
    priority: 0,
    fallbackFlag: false,
    enabled: true,
    remark: '',
  }
}

function loadList() {
  loading.value = true
  getGroupRouteRuleList(query)
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
  getGroupPoolList({ page: 1, size: 200, enabled: true }).then((res) => {
    groupPoolOptions.value = res.data || []
  })
}

function resetQuery() {
  Object.assign(query, { ruleName: '', enabled: null, ruleType: '', page: 1, size: 10 })
  loadList()
}

function openDialog(row) {
  Object.assign(form, defaultForm(), row || {})
  if (!form.ruleType) {
    form.ruleType = form.fallbackFlag ? 'GLOBAL_DEFAULT' : form.requiredTagIds ? 'CUSTOMER_TAG' : form.channelCodeId ? 'CHANNEL_DEFAULT' : 'GLOBAL_DEFAULT'
  }
  dialogVisible.value = true
}

function syncChannelName(value) {
  const item = userCodeOptions.value.find((option) => option.val === value)
  form.channelCodeName = item?.key || ''
}

function requiresChannel(ruleType) {
  return ['CHANNEL_TAG', 'CHANNEL_DEFAULT'].includes(ruleType)
}

function requiresTags(ruleType) {
  return ['CUSTOMER_TAG', 'CHANNEL_TAG'].includes(ruleType)
}

function onRuleTypeChange() {
  if (!requiresChannel(form.ruleType)) {
    form.channelCodeId = ''
    form.channelCodeName = ''
  }
  if (!requiresTags(form.ruleType)) {
    form.requiredTagIds = ''
  }
  form.fallbackFlag = form.ruleType === 'GLOBAL_DEFAULT'
}

function submit() {
  if (!form.ruleName || !form.targetPoolId) {
    ElMessage.warning('请填写规则名称并选择目标群池')
    return
  }
  if (requiresChannel(form.ruleType) && !form.channelCodeId) {
    ElMessage.warning('请选择渠道活码')
    return
  }
  if (requiresTags(form.ruleType) && !form.requiredTagIds) {
    ElMessage.warning('请填写客户标签ID')
    return
  }
  const request = form.id ? updateGroupRouteRule : saveGroupRouteRule
  request({ ...form, fallbackFlag: form.ruleType === 'GLOBAL_DEFAULT' }).then(() => {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
  })
}

function remove(row) {
  ElMessageBox.confirm('确认删除该分群规则？', '提示', { type: 'warning' }).then(() => {
    deleteGroupRouteRule(row.id).then(() => {
      ElMessage.success('删除成功')
      loadList()
    })
  })
}

function openPreview() {
  previewResult.value = null
  previewVisible.value = true
}

function runPreview() {
  matchGroupRoutePreview({ ...previewForm }).then((res) => {
    previewResult.value = res.data
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

.preview-result {
  margin-top: 16px;
}
</style>
