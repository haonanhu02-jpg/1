<template>
  <div class="phase-one-mock">
    <section class="toolbar">
      <div>
        <h1>本地模拟</h1>
      </div>
      <el-button :loading="loading" @click="resetForm">重置</el-button>
    </section>

    <el-card shadow="never" class="panel">
      <el-form :model="form" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="渠道 ID">
              <el-input v-model="form.channelId" placeholder="可填 iyque_user_code.id" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="State">
              <el-input v-model="form.state" placeholder="可填活码 state；为空自动生成" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="客户 ID">
              <el-input v-model="form.externalUserId" placeholder="mock_external_..." clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="员工 ID">
              <el-input v-model="form.userId" placeholder="mock_user_..." clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="客户名称">
              <el-input v-model="form.customerName" placeholder="mock_customer_..." clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="群池 ID">
              <el-input-number v-model="form.groupId" :min="1" controls-position="right" class="full-input" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="客户群 ID">
              <el-input v-model="form.chatId" placeholder="mock_chat_..." clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="客户群名称">
              <el-input v-model="form.chatName" placeholder="mock_chat_name_..." clearable />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="actions">
        <el-button type="primary" :loading="loading" @click="run('scan')">模拟扫码</el-button>
        <el-button type="success" :loading="loading" @click="run('add')">模拟加好友</el-button>
        <el-button type="warning" :loading="loading" @click="run('join')">模拟进群</el-button>
        <el-button type="danger" :loading="loading" @click="run('leave')">模拟退群</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="panel">
      <template #header>模拟结果</template>
      <pre class="result">{{ resultText }}</pre>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { mockAddContact, mockJoinGroup, mockLeaveGroup, mockScan } from '../api'

const defaultForm = () => ({
  channelId: '',
  state: '',
  externalUserId: 'mock_external_user_001',
  userId: 'mock_user_001',
  customerName: 'mock_customer_001',
  groupId: undefined,
  chatId: '',
  chatName: '',
})

const form = reactive(defaultForm())
const loading = ref(false)
const result = ref({})
const resultText = computed(() => JSON.stringify(result.value || {}, null, 2))

const actionMap = {
  scan: mockScan,
  add: mockAddContact,
  join: mockJoinGroup,
  leave: mockLeaveGroup,
}

function buildPayload() {
  return Object.fromEntries(
    Object.entries(form).filter(([, value]) => value !== '' && value !== undefined && value !== null)
  )
}

function resetForm() {
  Object.assign(form, defaultForm())
  result.value = {}
}

function run(action) {
  const api = actionMap[action]
  if (!api) return
  loading.value = true
  api(buildPayload())
    .then((res) => {
      result.value = res.data || res
      ElMessage.success('模拟事件已提交')
    })
    .finally(() => {
      loading.value = false
    })
}
</script>

<style lang="scss" scoped>
.phase-one-mock {
  .toolbar {
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

  .panel {
    margin-bottom: 16px;
    border-radius: 6px;
  }

  .full-input {
    width: 100%;
  }

  .actions {
    display: flex;
    gap: 12px;
    padding-left: 110px;
  }

  .result {
    min-height: 220px;
    margin: 0;
    padding: 12px;
    overflow: auto;
    color: #263238;
    background: #f7f9fb;
    border: 1px solid #e5e9ef;
    border-radius: 6px;
    font-size: 13px;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
  }
}
</style>
