<template>
  <div id="app-root">
    <!-- Header -->
    <header class="app-header">
      <div class="logo">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
        </svg>
        大庆充电站管理
      </div>
      <span class="subtitle">新能源汽车充电站管理系统</span>
      <div class="header-spacer"></div>
      <div class="header-actions">
        <button class="btn btn-sm" @click="handleExport" title="导出数据备份">
          📥 导出
        </button>
        <label class="btn btn-sm" title="导入数据">
          📤 导入
          <input
            type="file"
            accept=".json"
            style="display:none"
            @change="handleImport"
            ref="fileInput"
          />
        </label>
        <button class="btn btn-primary btn-sm" @click="startAddStation">
          ➕ 添加充电站
        </button>
      </div>
    </header>

    <!-- Main Content -->
    <div class="main-container">
      <!-- Sidebar -->
      <StationSidebar @select-station="onSelectStation" />

      <!-- Map -->
      <MapView ref="mapRef" />
    </div>

    <!-- Add/Edit Modal -->
    <StationForm
      v-if="showForm"
      :station-id="editingStationId"
      :lat="newStationLat"
      :lng="newStationLng"
      @close="closeForm"
      @saved="onFormSaved"
    />

    <!-- Toast -->
    <div v-if="toast" class="toast" :class="'toast-' + toast.type">
      {{ toast.msg }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useStationStore } from './stores/station.js'
import MapView from './components/MapView.vue'
import StationSidebar from './components/StationSidebar.vue'
import StationForm from './components/StationForm.vue'

const store = useStationStore()

const mapRef = ref(null)
const fileInput = ref(null)
const showForm = ref(false)
const editingStationId = ref(null)
const newStationLat = ref(null)
const newStationLng = ref(null)
const toast = ref(null)

let toastTimer = null

// ---- Add Station Flow ----
function startAddStation() {
  editingStationId.value = null
  newStationLat.value = null
  newStationLng.value = null
  showForm.value = true
  // Enable map click-to-pick mode
  store.selectedStationId = '__adding__'
}

function onMapClickAdd(e) {
  newStationLat.value = e.detail.lat
  newStationLng.value = e.detail.lng
  store.selectedStationId = null
  showToast('已获取坐标，请完善信息后保存', 'success')
}

function onSelectStation(station) {
  if (mapRef.value) {
    mapRef.value.flyToStation(station)
  }
}

function onEditStation(e) {
  editingStationId.value = e.detail.id
  newStationLat.value = null
  newStationLng.value = null
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editingStationId.value = null
  newStationLat.value = null
  newStationLng.value = null
  if (store.selectedStationId === '__adding__') {
    store.selectedStationId = null
  }
}

function onFormSaved() {
  closeForm()
  if (mapRef.value) {
    mapRef.value.refreshMarkers()
  }
}

// ---- Import / Export ----
function handleExport() {
  if (store.stations.length === 0) {
    showToast('暂无数据可导出', 'error')
    return
  }
  store.exportData()
  showToast('数据导出成功！', 'success')
}

async function handleImport(e) {
  const file = e.target.files[0]
  if (!file) return
  try {
    const count = await store.importData(file)
    showToast(`成功导入 ${count} 个充电站数据！`, 'success')
    if (mapRef.value) {
      mapRef.value.refreshMarkers()
    }
  } catch (err) {
    showToast('导入失败：' + err.message, 'error')
  }
  // Reset file input
  if (fileInput.value) fileInput.value.value = ''
}

// ---- Toast ----
function showToast(msg, type = 'success') {
  toast.value = { msg, type }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toast.value = null }, 2500)
}

// ---- Event Listeners ----
onMounted(() => {
  window.addEventListener('map-click-add', onMapClickAdd)
  window.addEventListener('edit-station', onEditStation)
  window.addEventListener('toast', (e) => showToast(e.detail.msg, e.detail.type))
})

onUnmounted(() => {
  window.removeEventListener('map-click-add', onMapClickAdd)
  window.removeEventListener('edit-station', onEditStation)
  window.removeEventListener('toast', () => {})
  clearTimeout(toastTimer)
})
</script>
