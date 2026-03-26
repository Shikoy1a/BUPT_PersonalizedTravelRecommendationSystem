<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import {
  apiDiaryDelete,
  apiDiaryDetail,
  apiDiaryList,
  apiDiarySearch,
  apiRecommendationList,
  apiScenicSearchByKeyword,
  type Diary,
  type ScenicArea,
} from '../../lib/api'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const loading = ref(false)

const listQuery = reactive({ page: 1, size: 50, sortBy: 'heat' })
const list = ref<Diary[]>([])
const diaryDestMap = ref<Record<number, number[]>>({})
const diaryCreatorNicknameMap = ref<Record<number, string>>({})
const scenicTagMap = ref<Record<number, string[]>>({})

const scenicTags = ['推荐', '历史', '古镇', '山水', '海滨', '人文']
const activeTag = ref('推荐')

const searchQuery = reactive({ keyword: '', destination: undefined as number | undefined, page: 1, size: 50 })
const searchList = ref<Diary[]>([])

const searchPanelOpen = ref(false)
const fromSearch = ref(false)

const destOptions = ref<ScenicArea[]>([])
const destLoading = ref(false)
let destSearchSeq = 0

async function remoteDestSearch(keyword: string) {
  const q = keyword.trim()
  if (!q) {
    destSearchSeq++
    destOptions.value = []
    return
  }
  const seq = ++destSearchSeq
  destLoading.value = true
  try {
    destOptions.value = await apiScenicSearchByKeyword({ keyword: q, limit: 50 })
  } finally {
    if (seq === destSearchSeq) destLoading.value = false
  }
}

async function load() {
  loading.value = true
  try {
    await ensureScenicTagMap()
    list.value = await apiDiaryList(listQuery)
    await hydrateDiaryDestinations(list.value)
    fromSearch.value = false
  } finally {
    loading.value = false
  }
}

async function runSearch() {
  loading.value = true
  try {
    await ensureScenicTagMap()
    searchList.value = await apiDiarySearch({
      destination: searchQuery.destination,
      page: searchQuery.page,
      size: searchQuery.size,
    })
    await hydrateDiaryDestinations(searchList.value)
    fromSearch.value = true
  } finally {
    loading.value = false
  }
}

const baseItems = computed(() => (fromSearch.value ? searchList.value : list.value))

const displayItems = computed(() => {
  if (activeTag.value === '推荐') return baseItems.value
  return baseItems.value.filter((row) => {
    const destIds = diaryDestMap.value[row.id] || []
    if (!destIds.length) return false
    return destIds.some((destId) => {
      const tags = scenicTagMap.value[destId] || []
      return tags.includes(activeTag.value)
    })
  })
})

function firstImage(d: Diary): string | null {
  const raw = d.images
  if (!raw) return null
  if (Array.isArray(raw)) {
    const r = raw as unknown as string[]
    return r[0] && typeof r[0] === 'string' ? r[0] : null
  }
  try {
    const v = JSON.parse(raw) as string[]
    return Array.isArray(v) && v[0] ? String(v[0]) : null
  } catch {
    return null
  }
}

function toggleSearchPanel() {
  searchPanelOpen.value = !searchPanelOpen.value
  if (!searchPanelOpen.value) {
    fromSearch.value = false
    searchQuery.destination = undefined
    void load()
  }
}

async function onSearchClick() {
  if (!searchPanelOpen.value) {
    toggleSearchPanel()
    return
  }
  await runSearch()
}

async function showAll() {
  fromSearch.value = false
  searchPanelOpen.value = false
  searchQuery.destination = undefined
  await load()
}

async function ensureScenicTagMap() {
  if (Object.keys(scenicTagMap.value).length > 0) return
  const res = await apiRecommendationList({ page: 1, size: 300, sortBy: 'heat' })
  const map: Record<number, string[]> = {}
  for (const item of res.list || []) {
    map[item.id] = Array.isArray(item.tags) ? item.tags : []
  }
  scenicTagMap.value = map
}

async function hydrateDiaryDestinations(diaries: Diary[]) {
  const missIds = diaries.map((d) => d.id).filter((id) => !(id in diaryDestMap.value))
  if (!missIds.length) return
  const entries = await Promise.all(
    missIds.map(async (id) => {
      try {
        const detail = await apiDiaryDetail(id)
        return {
          id,
          destinations: detail.destinations ?? [],
          nickname: detail.creatorNickname ?? '',
        } as const
      } catch {
        return {
          id,
          destinations: [] as number[],
          nickname: '',
        } as const
      }
    }),
  )
  diaryDestMap.value = {
    ...diaryDestMap.value,
    ...Object.fromEntries(entries.map((x) => [x.id, x.destinations])),
  }
  diaryCreatorNicknameMap.value = {
    ...diaryCreatorNicknameMap.value,
    ...Object.fromEntries(entries.map((x) => [x.id, x.nickname])),
  }
}

async function selectTag(tag: string) {
  activeTag.value = tag
  if (tag === '推荐') {
    if (!fromSearch.value && !list.value.length) await load()
    return
  }
  if (!baseItems.value.length) await load()
}

function canManage(row: Diary) {
  return auth.isAuthed && auth.user?.id === row.userId
}

async function del(row: Diary, e: Event) {
  e.stopPropagation()
  await ElMessageBox.confirm('确认删除该日记？此操作不可恢复。', '警告', { type: 'warning' })
  await apiDiaryDelete(row.id)
  if (fromSearch.value) await runSearch()
  else await load()
}

onMounted(load)
</script>

<template>
  <div class="page diary-list-page">
    <div class="diary-feed-shell">
      <div class="hdr">
        <div class="tag-row">
          <button
            v-for="tag in scenicTags"
            :key="tag"
            class="tag-btn"
            :class="{ active: activeTag === tag }"
            type="button"
            @click="selectTag(tag)"
          >
            {{ tag }}
          </button>
        </div>
        <div class="hdr-actions">
          <el-select
            v-if="searchPanelOpen"
            v-model="searchQuery.destination"
            filterable
            remote
            clearable
            :reserve-keyword="false"
            placeholder="目的地"
            :remote-method="remoteDestSearch"
            :loading="destLoading"
            class="dest-pill"
          >
            <el-option
              v-for="o in destOptions"
              :key="o.id"
              :label="o.name"
              :value="o.id"
            />
          </el-select>
          <el-button type="primary" plain class="hdr-btn search-btn" @click="onSearchClick">搜索</el-button>
          <el-button type="primary" class="hdr-btn plus-btn" :disabled="!auth.isAuthed" @click="$router.push('/diary/new')">
            +
          </el-button>
        </div>
      </div>

      <div v-if="fromSearch" class="search-hint">
        <span class="muted">以下为搜索结果</span>
        <el-button text type="primary" @click="showAll">查看全部</el-button>
      </div>

      <div class="feed" v-loading="loading">
        <div
          v-for="row in displayItems"
          :key="row.id"
          class="diary-card"
          @click="$router.push(`/diary/${row.id}`)"
        >
          <div class="cover">
            <img v-if="firstImage(row)" :src="firstImage(row)!" alt="" />
            <div v-else class="cover-placeholder">无图</div>
          </div>
          <div class="card-title">
            <div class="card-title-row">
              <div class="card-title-text">{{ row.title }}</div>
              <div v-if="diaryCreatorNicknameMap[row.id]" class="card-nickname">{{ diaryCreatorNicknameMap[row.id] }}</div>
            </div>
          </div>
          <div v-if="canManage(row)" class="card-actions" @click.stop>
            <el-button size="small" @click="$router.push(`/diary/${row.id}/edit`)">编辑</el-button>
            <el-button size="small" type="danger" @click="del(row, $event)">删除</el-button>
          </div>
        </div>

        <div v-if="!displayItems.length && !loading" class="empty muted">暂无日记</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.diary-list-page {
  max-width: none !important;
  margin: 0 !important;
  padding: 0 18px 18px;
}

.hdr {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  position: sticky;
  top: 0;
  z-index: 20;
  background: var(--bg-base);
  padding: 12px 0;
  margin-bottom: 14px;
}

.diary-feed-shell {
  background: transparent;
}

.tag-row {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: none;
}

.tag-row::-webkit-scrollbar {
  display: none;
}

.tag-btn {
  border: none;
  background: transparent;
  padding: 6px 10px;
  border-radius: 999px;
  color: var(--text-secondary, #756b59);
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
}

.tag-btn.active {
  background: rgba(0, 0, 0, 0.08);
  color: var(--text-primary, #2d2618);
  font-weight: 700;
}

.hdr-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.hdr-btn {
  min-width: 62px;
  height: 34px;
}

.search-btn {
  min-width: 70px;
}

.plus-btn {
  min-width: 34px;
  padding: 0 10px;
  font-size: 18px;
  font-weight: 700;
}

.dest-pill {
  width: 240px;
}

.dest-pill :deep(.el-input__wrapper),
.dest-pill :deep(.el-select__wrapper) {
  border-radius: 999px !important;
  border: none !important;
  box-shadow: none !important;
  background: rgba(255, 255, 255, 0.2) !important;
}

.dest-pill :deep(.el-input__inner),
.dest-pill :deep(.el-select__selected-value) {
  border-radius: 999px !important;
}

.dest-pill :deep(.el-input__wrapper) {
  height: 34px;
  padding: 0 14px;
}

.search-panel {
  display: flex;
  flex-wrap: nowrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.55);
}

.search-dest {
  flex: 1;
  min-width: 240px;
}

.search-panel :deep(.el-input) {
  flex: 1;
  min-width: 200px;
}

.search-panel :deep(.el-button) {
  flex-shrink: 0;
}

@media (max-width: 860px) {
  .search-panel {
    flex-wrap: wrap;
  }
}

.search-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 13px;
}

.feed {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
  max-width: none;
  margin: 0;
  min-height: 120px;
}

@media (max-width: 1200px) {
  .feed {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .feed {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .feed {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 480px) {
  .feed {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
}

.diary-card {
  cursor: pointer;
  border-radius: 16px;
  overflow: hidden;
  /* 使用全局主题变量，避免浅色/深色切换导致标题看不见 */
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.diary-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
}

.cover {
  width: 100%;
  aspect-ratio: 3 / 4;
  background: rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.35);
}

.card-title {
  padding: 14px 16px 12px;
  font-weight: 700;
  font-size: 16px;
  line-height: 1.45;
  color: var(--text-primary, #1a1a18);
}

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.card-title-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-nickname {
  flex-shrink: 0;
  margin-top: 2px;
  font-size: 12px;
  font-weight: 400;
  color: rgba(58, 51, 40, 0.65);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 45%;
}

.card-actions {
  padding: 0 16px 14px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.empty {
  text-align: center;
  padding: 32px 12px;
  font-size: 14px;
}

.muted {
  color: rgba(58, 51, 40, 0.65);
}
</style>
