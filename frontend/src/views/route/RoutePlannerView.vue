<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { apiMapData, apiPlanRoute, apiPlanRouteMulti, apiScenicSearchByKeyword, type ScenicArea } from '../../lib/api'

type Edge = {
  startId: number
  endId: number
  distance: number
  speed: number
  congestion: number
  vehicleType?: string
}

const loading = ref(false)
const map = ref<{ nodes: number[]; edges: Edge[] } | null>(null)
const chartEl = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null

const form = reactive({
  areaId: undefined as number | undefined,
  startId: null as number | null,
  endId: null as number | null,
  vehicle: '' as string,
  strategy: '' as '' | 'distance' | 'time',
  multiPoints: '',
})

const result = ref<{ path: number[]; distance: number; time: number } | null>(null)

const areaOpts = ref<ScenicArea[]>([])
const areaLoading = ref(false)
let areaSeq = 0

async function remoteArea(keyword: string) {
  const q = keyword.trim()
  if (!q) {
    areaSeq++
    areaOpts.value = []
    return
  }
  const seq = ++areaSeq
  areaLoading.value = true
  try {
    areaOpts.value = await apiScenicSearchByKeyword({ keyword: q, limit: 50 })
  } finally {
    if (seq === areaSeq) areaLoading.value = false
  }
}

function renderGraph(highlightPath?: number[]) {
  if (!chartEl.value || !map.value) return
  if (!chart) chart = echarts.init(chartEl.value)

  const nodes = map.value.nodes.map((id) => ({
    id: String(id),
    name: String(id),
    symbolSize: highlightPath?.includes(id) ? 18 : 10,
    itemStyle: highlightPath?.includes(id)
      ? { color: 'rgba(204,120,92,0.95)' }
      : { color: 'rgba(255,255,255,0.65)' },
  }))

  const pathSet = new Set<string>()
  if (highlightPath && highlightPath.length > 1) {
    for (let i = 0; i < highlightPath.length - 1; i++) {
      pathSet.add(`${highlightPath[i]}-${highlightPath[i + 1]}`)
      pathSet.add(`${highlightPath[i + 1]}-${highlightPath[i]}`)
    }
  }

  const links = map.value.edges.map((e) => {
    const key = `${e.startId}-${e.endId}`
    const isOnPath = pathSet.has(key)
    return {
      source: String(e.startId),
      target: String(e.endId),
      value: e.distance,
      lineStyle: isOnPath
        ? { width: 3, color: 'rgba(204,120,92,0.95)' }
        : { width: 1, color: 'rgba(255,255,255,0.12)' },
    }
  })

  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'graph',
        layout: 'force',
        roam: true,
        draggable: true,
        label: { show: true, color: 'rgba(255,255,255,0.8)' },
        force: { repulsion: 90, edgeLength: [40, 120] },
        data: nodes,
        links,
      },
    ],
  })
}

async function loadMap() {
  loading.value = true
  try {
    map.value = await apiMapData({ areaId: form.areaId })
    result.value = null
    renderGraph()
  } finally {
    loading.value = false
  }
}

async function plan() {
  if (!form.startId || !form.endId) {
    ElMessage.warning('请填写 起点节点 ID 和 终点节点 ID（必填）')
    return
  }
  loading.value = true
  try {
    result.value = await apiPlanRoute({
      areaId: form.areaId,
      startId: Number(form.startId),
      endId: Number(form.endId),
      vehicle: form.vehicle || undefined,
      strategy: form.strategy || undefined,
    })
    renderGraph(result.value.path)
  } finally {
    loading.value = false
  }
}

async function planMulti() {
  const points = form.multiPoints
    .split(',')
    .map((s) => Number(s.trim()))
    .filter((n) => Number.isFinite(n))
  if (points.length < 2) {
    ElMessage.warning('多点规划至少需要 2 个节点 ID（用逗号分隔）')
    return
  }

  loading.value = true
  try {
    result.value = await apiPlanRouteMulti({
      areaId: form.areaId,
      points,
      vehicle: form.vehicle || undefined,
      strategy: form.strategy || undefined,
    })
    renderGraph(result.value.path)
  } finally {
    loading.value = false
  }
}

onMounted(loadMap)
</script>

<template>
  <div class="page">
    <div class="grid">
      <el-card class="glass" shadow="never">
        <template #header>
          <div style="font-weight: 900">路线规划</div>
        </template>

        <el-form label-position="top">
          <el-form-item label="景区">
            <el-select
              v-model="form.areaId"
              filterable
              remote
              clearable
              :reserve-keyword="false"
              placeholder="输入关键字"
              :remote-method="remoteArea"
              :loading="areaLoading"
              style="width: 100%"
            >
              <el-option
                v-for="o in areaOpts"
                :key="o.id"
                :label="`${o.name}（ID ${o.id}）`"
                :value="o.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="交通工具">
            <el-segmented
              v-model="form.vehicle"
              :options="[
                { label: '步行', value: 'walk' },
                { label: '自行车', value: 'bike' },
                { label: '电瓶车', value: 'shuttle' },
              ]"
            />
          </el-form-item>
          <el-form-item label="策略">
            <el-radio-group v-model="form.strategy">
              <el-radio-button label="distance">最短距离</el-radio-button>
              <el-radio-button label="time">最短时间</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <div class="row">
            <el-form-item label="起点节点 ID（必填）">
              <el-input-number
                v-model="form.startId"
                :min="1"
                :controls="false"
                placeholder="起点节点ID"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="终点节点 ID（必填）">
              <el-input-number
                v-model="form.endId"
                :min="1"
                :controls="false"
                placeholder="终点节点ID"
                style="width: 100%"
              />
            </el-form-item>
          </div>

          <div class="actions">
            <el-button @click="loadMap" :loading="loading">刷新地图数据</el-button>
            <el-button type="primary" @click="plan" :loading="loading">两点规划</el-button>
          </div>

          <el-divider />

          <el-form-item label="多点规划（用逗号分隔）">
            <el-input v-model="form.multiPoints" placeholder="输入多个节点 ID，用逗号分隔（必填：至少 2 个点）" />
          </el-form-item>
          <el-button type="primary" plain @click="planMulti" :loading="loading">多点规划</el-button>

          <div v-if="result" class="glass result">
            <div style="font-weight: 900">结果</div>
            <div class="muted">path：{{ result.path.join(' → ') }}</div>
            <div class="muted">distance：{{ result.distance.toFixed(2) }} m</div>
            <div class="muted">time：{{ result.time.toFixed(2) }} s</div>
          </div>
        </el-form>
      </el-card>

      <el-card class="glass" shadow="never">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <div style="font-weight: 900">节点 / 路径</div>
            <div class="muted" style="font-size: 12px">当前规划会高亮路径</div>
          </div>
        </template>
        <div ref="chartEl" class="chart" />
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 16px;
}
.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.result {
  margin-top: 12px;
  padding: 14px;
}
.hint {
  margin-top: 6px;
  font-size: 12px;
}
.chart {
  height: 560px;
  width: 100%;
}
@media (max-width: 1080px) {
  .grid {
    grid-template-columns: 1fr;
  }
  .chart {
    height: 420px;
  }
}
</style>

