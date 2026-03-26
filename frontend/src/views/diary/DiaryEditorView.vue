<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  apiDiaryCreate,
  apiDiaryDetail,
  apiDiaryUpdate,
  apiScenicDetail,
  apiScenicSearchByKeyword,
  type ScenicArea,
} from '../../lib/api'

const route = useRoute()
const router = useRouter()
const loading = ref(false)

const isEdit = ref(false)
const diaryId = ref<number | null>(null)

const form = reactive({
  title: '',
  content: '',
  imagesText: '',
  videosText: '',
})
const destinationIds = ref<number[]>([])
const destinationOptions = ref<Array<{ id: number; name: string; location?: string }>>([])
const destinationLoading = ref(false)
let searchSeq = 0

function splitLines(s: string) {
  return s
    .split('\n')
    .map((x) => x.trim())
    .filter(Boolean)
}

function upsertDestinationOption(item: { id: number; name: string; location?: string }) {
  if (!item.id || !item.name) return
  const idx = destinationOptions.value.findIndex((x) => x.id === item.id)
  if (idx >= 0) {
    destinationOptions.value[idx] = item
  } else {
    destinationOptions.value.push(item)
  }
}

function keepSelectedDestinationOptions() {
  if (destinationIds.value.length === 0) {
    destinationOptions.value = []
    return
  }
  const selectedSet = new Set(destinationIds.value)
  destinationOptions.value = destinationOptions.value.filter((x) => selectedSet.has(x.id))
}

function toDestinationOption(item: ScenicArea) {
  return {
    id: item.id,
    name: item.name,
    location: item.location,
  }
}

async function ensureDestinationOptionsByIds(ids: number[]) {
  const missed = ids.filter((id) => !destinationOptions.value.some((x) => x.id === id))
  if (missed.length === 0) return
  for (const id of missed) {
    try {
      const detail = await apiScenicDetail(id)
      upsertDestinationOption(toDestinationOption(detail))
    } catch {
      // 忽略单个目的地加载失败，避免影响整体编辑。
    }
  }
}

async function remoteSearchDestination(keyword: string) {
  const q = keyword.trim()
  if (!q) {
    searchSeq++
    destinationLoading.value = false
    keepSelectedDestinationOptions()
    return
  }
  const currentSeq = ++searchSeq
  destinationLoading.value = true
  try {
    keepSelectedDestinationOptions()
    const list = await apiScenicSearchByKeyword({ keyword: q, limit: 50 })
    if (currentSeq !== searchSeq) return
    list.forEach((item) => upsertDestinationOption(toDestinationOption(item)))
  } finally {
    if (currentSeq === searchSeq) destinationLoading.value = false
  }
}

async function loadForEdit(id: number) {
  loading.value = true
  try {
    const d = await apiDiaryDetail(id)
    form.title = d.title
    form.content = d.content
    try {
      form.imagesText = (JSON.parse(d.images || '[]') as string[]).join('\n')
    } catch {
      form.imagesText = ''
    }
    try {
      form.videosText = (JSON.parse(d.videos || '[]') as string[]).join('\n')
    } catch {
      form.videosText = ''
    }
    destinationIds.value = [...((d.destinations || []) as number[])]
    await ensureDestinationOptionsByIds(destinationIds.value)
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写标题与正文')
    return
  }
  loading.value = true
  try {
    const images = splitLines(form.imagesText)
    const videos = splitLines(form.videosText)
    const destinations = [...destinationIds.value]
    if (isEdit.value && diaryId.value) {
      await apiDiaryUpdate(diaryId.value, { title: form.title, content: form.content, images, videos, destinations })
      ElMessage.success('更新成功')
      router.push(`/diary/${diaryId.value}`)
      return
    }
    if (destinations.length === 0) {
      ElMessage.warning('请至少选择一个目的地')
      return
    }
    const data = await apiDiaryCreate({ title: form.title, content: form.content, images, videos, destinations })
    ElMessage.success('创建成功')
    router.push(`/diary/${data.diary_id}`)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const id = route.params.id ? Number(route.params.id) : null
  if (id) {
    isEdit.value = true
    diaryId.value = id
    loadForEdit(id)
  }
})
</script>

<template>
  <div class="page" v-loading="loading">
    <el-card class="editor-card" shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div style="font-weight: 900">{{ isEdit ? '编辑日记' : '新建日记' }}</div>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="写下你的旅途故事..." />
        </el-form-item>

        <div class="grid">
          <el-form-item label="目的地名称（必填，可多选）">
            <el-select
              v-model="destinationIds"
              multiple
              filterable
              remote
              clearable
              collapse-tags
              collapse-tags-tooltip
              :reserve-keyword="false"
              :remote-method="remoteSearchDestination"
              :loading="destinationLoading"
              @change="keepSelectedDestinationOptions"
              placeholder="输入景区名称关键字"
              style="width: 100%"
            >
              <el-option
                v-for="item in destinationOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              >
                <div style="display: flex; justify-content: space-between; gap: 10px">
                  <span>{{ item.name }}</span>
                  <span class="muted">{{ item.location || '—' }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </div>

        <div class="grid">
          <el-form-item label="图片URL（每行一个，选填）">
            <el-input v-model="form.imagesText" type="textarea" :rows="5" placeholder="https://..." />
          </el-form-item>
          <el-form-item label="视频URL（每行一个，选填）">
            <el-input v-model="form.videosText" type="textarea" :rows="5" placeholder="https://..." />
          </el-form-item>
        </div>

        <el-button type="primary" size="large" :loading="loading" @click="submit">
          {{ isEdit ? '保存修改' : '发布日记' }}
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.editor-card {
  background: transparent;
  border: none;
}

.editor-card :deep(.el-card__header),
.editor-card :deep(.el-card__body) {
  background: transparent;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 860px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>

