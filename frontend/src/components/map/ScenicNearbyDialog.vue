<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { loadAmapSdk } from './AMapLoader'

const props = defineProps<{
  modelValue: boolean
  scenicName?: string
  location?: string
  latitude?: number | null
  longitude?: number | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const mapRef = ref<HTMLElement | null>(null)
const loading = ref(false)
const errorText = ref('')

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
})

let map: any = null
let AMap: any = null
let scenicMarker: any = null
let centerLngLat: [number, number] | null = null

function disposeMap() {
  if (map) {
    map.destroy()
    map = null
  }
  scenicMarker = null
  centerLngLat = null
}

function geocodeByKeyword(keyword: string) {
  return new Promise<[number, number]>((resolve, reject) => {
    const geocoder = new AMap.Geocoder()
    geocoder.getLocation(keyword, (status: string, result: any) => {
      if (status !== 'complete' || !result?.geocodes?.[0]?.location) {
        reject(new Error('地理编码失败'))
        return
      }
      const loc = result.geocodes[0].location
      resolve([Number(loc.lng), Number(loc.lat)])
    })
  })
}

async function resolveCenterByProps() {
  if (!AMap) {
    throw new Error('地图 SDK 未加载完成')
  }

  if (props.longitude != null && props.latitude != null) {
    return [Number(props.longitude), Number(props.latitude)] as [number, number]
  }

  if (props.location?.trim()) {
    try {
      return await geocodeByKeyword(props.location)
    } catch {}
  }

  throw new Error('缺少景区经纬度与地址，无法定位')
}

async function initMapAndData() {
  if (!mapRef.value) return
  loading.value = true
  errorText.value = ''
  try {
    AMap = await loadAmapSdk()
    centerLngLat = await resolveCenterByProps()

    map = new AMap.Map(mapRef.value, {
      zoom: 13,
      center: centerLngLat,
      viewMode: '3D',
    })
    scenicMarker = new AMap.Marker({
      position: centerLngLat,
      title: props.scenicName || '景区位置',
      label: { content: '景区', direction: 'top' },
    })
    map.add(scenicMarker)
    map.setFitView([scenicMarker])
  } catch (err: any) {
    errorText.value = err?.message || '地图加载失败'
    ElMessage.error(errorText.value)
  } finally {
    loading.value = false
  }
}

async function onDialogOpen() {
  await nextTick()
  await initMapAndData()
}

function onDialogClosed() {
  disposeMap()
}
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    title="景区周边信息"
    width="1000px"
    top="6vh"
    append-to-body
    @open="onDialogOpen"
    @closed="onDialogClosed"
  >
    <div class="head">
      <div class="title">{{ scenicName || '当前景区' }}</div>
      <div class="subtitle">{{ location || '暂无地址信息' }}</div>
    </div>
    <div v-loading="loading" ref="mapRef" class="map"></div>
    <div v-if="errorText" class="muted empty">{{ errorText }}</div>
    <div v-else class="muted hint">拖拽或缩放可查看更多地图信息</div>
  </el-dialog>
</template>

<style scoped>
.head {
  margin-bottom: 10px;
}
.title {
  font-size: 16px;
  font-weight: 800;
}
.subtitle {
  font-size: 12px;
  color: var(--text-2);
  margin-top: 2px;
}
.map {
  width: 100%;
  height: 560px;
  border-radius: 14px;
  border: 1px solid var(--border);
}
.hint {
  margin-top: 10px;
  font-size: 12px;
}
.empty {
  margin-top: 10px;
}
@media (max-width: 1100px) {
  .map {
    height: 420px;
  }
}
</style>
