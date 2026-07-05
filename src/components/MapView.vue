<template>
  <div class="map-container" ref="mapContainer">
    <div v-if="!mapReady" class="map-loading">
      <div class="loading-spinner"></div>
      <p>地图加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useStationStore } from '../stores/station.js'
import L from 'leaflet'

const DAQING_CENTER = [46.5892, 125.0036]
const DEFAULT_ZOOM = 12

const store = useStationStore()
const mapContainer = ref(null)
const mapReady = ref(false)

let map = null
let markersLayer = null
let tempMarker = null

// Custom station icons
const stationIcons = {
  open: L.icon({
    iconUrl: createSvgIcon('#16a34a'),
    iconSize: [32, 44],
    iconAnchor: [16, 44],
    popupAnchor: [0, -44],
  }),
  closed: L.icon({
    iconUrl: createSvgIcon('#dc2626'),
    iconSize: [32, 44],
    iconAnchor: [16, 44],
    popupAnchor: [0, -44],
  }),
  maintenance: L.icon({
    iconUrl: createSvgIcon('#f59e0b'),
    iconSize: [32, 44],
    iconAnchor: [16, 44],
    popupAnchor: [0, -44],
  }),
}

function createSvgIcon(color) {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 44" width="32" height="44">
    <path d="M16 0C7.2 0 0 7.2 0 16c0 12 16 28 16 28s16-16 16-28C32 7.2 24.8 0 16 0z" fill="${color}"/>
    <circle cx="16" cy="15" r="8" fill="white"/>
    <circle cx="16" cy="15" r="4" fill="${color}"/>
  </svg>`
  return 'data:image/svg+xml,' + encodeURIComponent(svg)
}

// Charging station icon (⚡ themed)
const chargingIcon = L.divIcon({
  html: `<div style="
    background: #16a34a;
    border: 3px solid white;
    border-radius: 50%;
    width: 28px; height: 28px;
    display: flex; align-items: center; justify-content: center;
    box-shadow: 0 2px 6px rgba(0,0,0,0.3);
    font-size: 14px;
  ">⚡</div>`,
  iconSize: [28, 28],
  iconAnchor: [14, 14],
  className: ''
})

// ---- Tile layer definitions ----
// Multiple sources so at least one works in China
const TILE_LAYERS = {
  '高德地图': {
    url: 'https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}',
    options: {
      subdomains: '1234',
      maxZoom: 18,
      attribution: '&copy; 高德地图',
    }
  },
  '高德卫星': {
    url: 'https://webst0{s}.is.autonavi.com/appmaptile?style=6&x={x}&y={y}&z={z}',
    options: {
      subdomains: '1234',
      maxZoom: 18,
      attribution: '&copy; 高德地图',
    }
  },
  'OpenStreetMap': {
    url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    options: {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OSM</a>',
    }
  }
}

function initMap() {
  if (!mapContainer.value) return

  map = L.map(mapContainer.value, {
    center: DAQING_CENTER,
    zoom: DEFAULT_ZOOM,
    zoomControl: true,
  })

  // Try 高德 tiles first (works in China), fall back to OSM
  const primaryLayer = L.tileLayer(TILE_LAYERS['高德地图'].url, TILE_LAYERS['高德地图'].options)
  const satelliteLayer = L.tileLayer(TILE_LAYERS['高德卫星'].url, TILE_LAYERS['高德卫星'].options)
  const osmLayer = L.tileLayer(TILE_LAYERS['OpenStreetMap'].url, TILE_LAYERS['OpenStreetMap'].options)

  // Add primary layer
  primaryLayer.addTo(map)

  // Layer switcher control
  const baseLayers = {
    '高德地图': primaryLayer,
    '高德卫星': satelliteLayer,
    'OpenStreetMap': osmLayer
  }
  L.control.layers(baseLayers, null, { position: 'topright' }).addTo(map)

  // Scale control
  L.control.scale({ metric: true, imperial: false, position: 'bottomleft' }).addTo(map)

  // Layer for all station markers
  markersLayer = L.layerGroup().addTo(map)

  // Click on map to add station
  map.on('click', (e) => {
    if (store.selectedStationId === '__adding__') {
      store.selectedStationId = null
      window.dispatchEvent(new CustomEvent('map-click-add', {
        detail: { lat: e.latlng.lat, lng: e.latlng.lng }
      }))
    }
  })

  // Error detection: if primary tiles fail, try OSM
  primaryLayer.on('tileerror', function() {
    if (!map.hasLayer(osmLayer) && map.hasLayer(primaryLayer)) {
      map.removeLayer(primaryLayer)
      osmLayer.addTo(map)
      console.warn('高德地图瓦片加载失败，已切换到 OpenStreetMap')
    }
  })

  mapReady.value = true
  refreshMarkers()
}

function getStationIcon(status) {
  return stationIcons[status] || stationIcons.open
}

function refreshMarkers() {
  if (!markersLayer) return
  markersLayer.clearLayers()

  store.filteredStations.forEach(station => {
    const marker = L.marker([station.latitude, station.longitude], {
      icon: getStationIcon(station.status)
    })

    const statusText = {
      open: '营业中',
      closed: '已关闭',
      maintenance: '维护中'
    }[station.status] || '未知'

    const typesText = (station.chargerTypes || []).join('、') || '未设置'

    marker.bindPopup(`
      <div class="station-popup">
        <h3>${escapeHtml(station.name)}</h3>
        <p>📍 ${escapeHtml(station.address || '地址未填写')}</p>
        <p>🔌 接口类型：${escapeHtml(typesText)}</p>
        <p>⚡ 功率：${station.power ? station.power + ' kW' : '未设置'}</p>
        <p>💰 价格：${station.price ? station.price + ' 元/度' : '未设置'}</p>
        <p>🅿️ 车位：${station.availableSpots || 0}/${station.totalSpots || 0} 可用</p>
        <p>📌 状态：<span style="color:${
          station.status === 'open' ? '#16a34a' : station.status === 'maintenance' ? '#f59e0b' : '#dc2626'
        }">${statusText}</span></p>
        <div class="popup-actions" style="margin-top:8px;display:flex;gap:6px;">
          <button class="btn btn-primary btn-sm"
            onclick="window.__editStation('${station.id}')">编辑</button>
          <button class="btn btn-danger btn-sm"
            onclick="window.__deleteStation('${station.id}')">删除</button>
        </div>
      </div>
    `)

    marker.on('click', () => {
      store.selectedStationId = station.id
    })

    markersLayer.addLayer(marker)
  })
}

function escapeHtml(str) {
  const div = document.createElement('div')
  div.textContent = str
  return div.innerHTML
}

function flyToStation(station) {
  if (!map) return
  map.flyTo([station.latitude, station.longitude], 16, { duration: 0.8 })
  // Open popup
  markersLayer.eachLayer(marker => {
    const latlng = marker.getLatLng()
    if (latlng.lat === station.latitude && latlng.lng === station.longitude) {
      marker.openPopup()
    }
  })
}

// Watch for store changes to refresh markers
watch(
  () => [store.filteredStations, store.stations.length],
  () => refreshMarkers(),
  { deep: true }
)

// Expose methods globally
defineExpose({ flyToStation, refreshMarkers, initMap })

// Global handlers for popup buttons
window.__editStation = (id) => {
  store.selectedStationId = id
  window.dispatchEvent(new CustomEvent('edit-station', { detail: { id } }))
}
window.__deleteStation = (id) => {
  if (confirm('确定要删除这个充电站吗？此操作不可恢复。')) {
    store.deleteStation(id)
    window.dispatchEvent(new CustomEvent('toast', { detail: { msg: '充电站已删除', type: 'success' } }))
  }
}

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map) {
    map.remove()
    map = null
  }
  delete window.__editStation
  delete window.__deleteStation
})
</script>

<style scoped>
.map-loading {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  z-index: 10;
}
.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #e2e8f0;
  border-top-color: #16a34a;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.map-loading p { margin-top: 12px; color: #64748b; font-size: 14px; }
</style>
