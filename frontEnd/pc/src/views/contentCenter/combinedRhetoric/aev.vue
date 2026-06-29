<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as apiCategory from '@/api/category'
import { save } from './api'
import AddOrEditMaterialDialog from '../components/AddOrEditMaterialDialog.vue'
import SelectMaterial from '../commonMaterial/SelectMaterial.vue'
import stores from '@/stores'
import { $sdk } from '@/utils/sdk'

const $store = stores()
const router = useRouter()
const route = useRoute()

const isDetail = route.query.isDetail === 'true' // 是否为详情页面
const maxlength = 9 // 最大素材数量
const form = ref({})
const formRef = ref()
const dialogRef = ref()
const dialogSelectMaterialRef = ref()
const groupProps = {
  expandTrigger: 'hover',
  checkStrictly: true,
  children: 'children',
  label: 'name',
  value: 'id',
  emitPath: false,
}
const talkList = ref([]) // 话术素材列表
const fontType = ref('素材') // 素材类型名称
const type = ref('0') // 素材类型：0 图片，4 文本，9 图文
const tree = ref([])

// 获取类目树，并清理子级，避免级联选择器递归展示无效层级。
function getTree() {
  apiCategory.getList('13').then(({ data }) => {
    ;(data || []).forEach((item) => {
      item.children = null
    })
    tree.value = data || []
  })
}
getTree()

function openMaterialDialog(command) {
  type.value = command
  dialogRef.value?.action()
}

function updateMaterial({ form: materialForm, visible, loading }) {
  return dialogRef.value?.$refs?.form
    .validate()
    .then(() => {
      const material = materialForm.value
      if (material._index !== undefined && material._index !== null && material._index !== '') {
        talkList.value[material._index] = material
      } else {
        talkList.value.push(material)
      }
      visible.value = false
    })
    .finally(() => {
      loading.value = false
    })
}

function openSelectMaterialDialog() {
  dialogSelectMaterialRef.value.dialogRef.visible = true
}

function appendSelectedMaterial({ visible, loading, selected }) {
  const selectedList = Array.isArray(selected) ? selected : selected?.value || []
  talkList.value.push(...selectedList)
  visible.value = false
  loading.value = false
}

function editMaterial(row) {
  type.value = $sdk.dictMaterialType[row.msgtype].type
  dialogRef.value?.action(row)
}

async function submit() {
  await formRef.value.validate()
  $store.loading = true
  return save({
    ...form.value,
    scriptSubs: talkList.value,
  })
    .then(() => {
      $sdk.msgSuccess('保存成功')
      // router.back()
      $store.loading = false
    })
    .finally(() => {
      $store.loading = false
    })
}

defineExpose({
  form,
  formRef,
  groupProps,
  talkList,
  submit,
})
</script>

<template>
  <div class="flex --Gap">
    <div class="flex-auto">
      <el-form
        ref="formRef"
        class="g-card"
        :model="form"
        :rules="{ title: [$sdk.ruleRequiredBlur], categoryId: [$sdk.ruleRequiredChange] }"
        label-width="100px">
        <el-form-item label="选择分组" prop="categoryId">
          <el-cascader v-model="form.categoryId" :options="tree" :props="groupProps"></el-cascader>
        </el-form-item>
        <el-form-item label="话术标题" prop="title">
          <el-input v-model="form.title" type="text" :maxlength="32" show-word-limit placeholder="请输入话术标题"></el-input>
        </el-form-item>
      </el-form>

      <div class="g-card">
        <div class="g-card-title">
          话术素材
          <el-popover trigger="hover" :content="'最多添加' + maxlength + '个素材'" placement="top-start">
            <template #reference>
              <el-icon-QuestionFilled class="el-icon-QuestionFilled"></el-icon-QuestionFilled>
            </template>
          </el-popover>
        </div>
        <div>
          <div class="--MarginT flex">
            <el-popover
              trigger="hover"
              :content="'最多添加' + maxlength + '个' + fontType + '，如需修改请删除已有' + fontType + '后重新尝试'"
              placement="top-start"
              :disabled="talkList?.length < maxlength">
              <template #reference>
                <el-dropdown
                  :disabled="talkList?.length >= maxlength"
                  @command="openMaterialDialog">
                  <el-button type="primary">
                    {{ '新建' + fontType }}
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu trigger="click">
                      <el-dropdown-item :command="'4'">
                        <el-button text>文本</el-button>
                      </el-dropdown-item>
                      <el-dropdown-item :command="'0'">
                        <el-button text>图片</el-button>
                      </el-dropdown-item>
                      <el-dropdown-item :command="'9'">
                        <el-button text>图文</el-button>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-popover>

            <AddOrEditMaterialDialog
              ref="dialogRef"
              :type="type"
              :dynamic-title="$sdk.dictMaterialType[type]?.name"
              @confirm="updateMaterial"></AddOrEditMaterialDialog>

            <el-popover
              trigger="hover"
              :content="'最多添加' + maxlength + '个' + fontType + '，如需修改请删除已有' + fontType + '后重新尝试'"
              placement="top-start"
              :disabled="talkList?.length < maxlength">
              <template #reference>
                <div class="ml20">
                  <el-button :disabled="talkList?.length >= maxlength" @click="openSelectMaterialDialog">
                    从素材中心选择
                  </el-button>
                </div>
              </template>
            </el-popover>

            <SelectMaterial ref="dialogSelectMaterialRef" @confirm="appendSelectedMaterial" />
          </div>
        </div>
      </div>
      <div v-if="talkList?.length" class="g-card">
        <DragTable
          :data="talkList"
          :is-detail="isDetail"
          :darg-able="true"
          @change="talkList = $event"
          @edit="editMaterial" />
      </div>

      <!-- <CommonTopRight>
        <el-button @click="$router.back()">取消</el-button>
        <el-button type="primary" v-loading="$store.loading" :disabled="$store.loading" @click="submit">确定</el-button>
      </CommonTopRight> -->
    </div>
    <div class="g-card flex-none mt0 w-[375px]">
      <PhoneChatList :list="talkList" />
    </div>
  </div>
</template>

<style lang="scss" scoped></style>
