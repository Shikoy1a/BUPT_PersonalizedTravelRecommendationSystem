<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiGetInterest, apiRefresh, apiUpdateInterest } from '../../lib/api'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const interestsText = ref((auth.user?.interests ?? []).join(','))

type InterestInput = { type: string; weight?: number }

function parseInterests(): InterestInput[] {
  const out: InterestInput[] = []
  const rawItems = interestsText.value
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)

  for (const raw of rawItems) {
    const pair = raw.split(/[:：]/)
    const type = (pair[0] || '').trim()
    if (!type) continue

    if (pair.length < 2 || pair[1] === undefined || pair[1].trim() === '') {
      out.push({ type, weight: 1 })
      continue
    }

    const weight = Number(pair[1].trim())
    if (!Number.isFinite(weight) || weight <= 0 || weight > 5) {
      throw new Error(`兴趣权重非法（${raw}），请使用 标签:权重 且权重在 (0,5]`) 
    }
    out.push({ type, weight })
  }

  return out
}

async function saveInterest() {
  loading.value = true
  try {
    const payload = parseInterests()
    await apiUpdateInterest({
      interests: payload,
    })
    ElMessage.success('兴趣已更新')
    if (auth.user) auth.user.interests = payload.map((i) => i.type)
  } catch (e: any) {
    ElMessage.error(e?.message || '兴趣保存失败')
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

async function loadInterests() {
  if (!auth.isAuthed) return
  try {
    const items = await apiGetInterest()
    interestsText.value = (items ?? [])
      .map((item) => `${item.type}:${item.weight}`)
      .join(', ')
    if (auth.user) {
      auth.user.interests = (items ?? []).map((item) => item.type)
    }
  } catch {
    // 回显失败不阻塞页面渲染，保留本地已有展示
  }
}

onMounted(loadInterests)
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
        <div class="k">兴趣权重（格式：标签:权重，逗号分隔）</div>
        <el-input v-model="interestsText" placeholder="例如：美食:2, 历史:1.2, 徒步" />
        <div class="actions">
          <el-button type="primary" :loading="loading" @click="saveInterest">保存兴趣</el-button>
          <el-button @click="refreshToken">刷新 Token</el-button>
        </div>
        <div class="hint muted">
          输入格式：<code>标签:权重</code>，多个兴趣用英文逗号分隔；不填权重默认 <code>1</code>，合法范围是 <code>(0,5]</code>。
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

