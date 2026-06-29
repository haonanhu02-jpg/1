<template>
  <div class="phase-one-placeholder">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <span>{{ title }}</span>
          <el-tag type="warning">{{ status }}</el-tag>
        </div>
      </template>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="当前复用">
          {{ reuseModule }}
        </el-descriptions-item>
        <el-descriptions-item label="后续动作">
          {{ todo }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'PhaseOnePlaceholder',
  computed: {
    title() {
      return this.$route.meta.title || '一期模块'
    },
    status() {
      return this.$route.meta.phaseOneStatus || '已接入入口'
    },
    reuseModule() {
      return this.$route.meta.reuseModule || '暂无可直接复用模块'
    },
    todo() {
      // 占位页用于保持菜单完整，不提前伪造还没有实现的业务能力。
      return this.$route.meta.todo || '后续 MVP 阶段接入真实业务接口。'
    },
  },
}
</script>

<style lang="scss" scoped>
.phase-one-placeholder {
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}
</style>
