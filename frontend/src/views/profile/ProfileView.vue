<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRefresh, apiUpdateInterest } from '../../lib/api'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const interestsText = ref((auth.user?.interests ?? []).join(','))

const interests = computed(() =>
  interestsText.value
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean),
)

// 说明：后端 UpdateInterestRequest 里是 InterestItemRequest 列表（type/value）。
// 这里为了方便阅读与操作，前端把兴趣当作一个简单列表，统一按 type=tag 提交。
async function saveInterest() {
  loading.value = true
  try {
    await apiUpdateInterest({
      interests: interests.value.map((v) => ({ type: 'tag', value: v })),
    })
    ElMessage.success('兴趣已更新')
    if (auth.user) auth.user.interests = interests.value
  } finally {
    loading.value = false
  }
}

async function refreshToken() {
  if (!auth.token) return
  const data = await apiRefresh(auth.token)
  auth.setAuth(data.token, auth.user!)
  ElMessage.success('令牌已刷新')
}
</script>

<template>
  <div class="page">
    <el-card class="glass" shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div style="font-weight: 800">个人中心</div>
          <el-tag effect="plain">{{ auth.user?.role || 'USER' }}</el-tag>
        </div>
      </template>

      <div class="grid">
        <div class="glass block">
          <div class="k">用户名</div>
          <div class="v">{{ auth.user?.username }}</div>
        </div>
        <div class="glass block">
          <div class="k">用户ID</div>
          <div class="v">{{ auth.user?.id }}</div>
        </div>
      </div>

      <div class="glass block" style="margin-top: 12px">
        <div class="k">兴趣标签（用英文逗号分隔）</div>
        <el-input v-model="interestsText" placeholder="例如：海边, 美食, 校园" />
        <div class="actions">
          <el-button type="primary" :loading="loading" @click="saveInterest">保存兴趣</el-button>
          <el-button @click="refreshToken">刷新 Token</el-button>
        </div>
        <div class="hint muted">
          输入格式：用英文逗号分隔多个兴趣，例如 <code>海边, 美食, 校园</code>。
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.block {
  padding: 14px;
}
.k {
  font-size: 12px;
  color: var(--text-2);
}
.v {
  font-size: 16px;
  font-weight: 800;
  margin-top: 6px;
}
.actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.hint {
  margin-top: 8px;
  font-size: 12px;
}
@media (max-width: 720px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>

