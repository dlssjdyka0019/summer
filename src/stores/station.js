import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY = 'ev-charging-stations'

const DEMO_STATIONS = [
  {
    id: 'demo_1',
    name: '大庆万达广场充电站',
    address: '萨尔图区东风路15号万达广场B1层停车场',
    longitude: 125.1036,
    latitude: 46.5982,
    operator: '特来电',
    totalSpots: 20,
    availableSpots: 14,
    power: 120,
    chargerTypes: ['快充', '慢充'],
    price: 1.20,
    openHours: '06:00-23:00',
    phone: '0459-6116789',
    status: 'open',
    description: '位于万达广场地下停车场，入口在东风路一侧',
    createdAt: '2025-01-01T00:00:00.000Z',
    updatedAt: '2025-01-01T00:00:00.000Z'
  },
  {
    id: 'demo_2',
    name: '大庆萨尔图机场充电站',
    address: '萨尔图区机场路1号机场停车场P1区',
    longitude: 125.1347,
    latitude: 46.7433,
    operator: '国网电动',
    totalSpots: 12,
    availableSpots: 10,
    power: 60,
    chargerTypes: ['快充'],
    price: 1.50,
    openHours: '00:00-24:00',
    phone: '0459-6390222',
    status: 'open',
    description: '机场P1停车场西侧，24小时营业',
    createdAt: '2025-01-02T00:00:00.000Z',
    updatedAt: '2025-01-02T00:00:00.000Z'
  },
  {
    id: 'demo_3',
    name: '大庆东站充电站',
    address: '龙凤区龙凤大街大庆东站南广场',
    longitude: 125.1281,
    latitude: 46.5436,
    operator: '星星充电',
    totalSpots: 16,
    availableSpots: 8,
    power: 180,
    chargerTypes: ['快充', '超快充'],
    price: 1.35,
    openHours: '00:00-24:00',
    phone: '',
    status: 'open',
    description: '东站南广场地面停车场，支持超快充',
    createdAt: '2025-01-03T00:00:00.000Z',
    updatedAt: '2025-01-03T00:00:00.000Z'
  },
  {
    id: 'demo_4',
    name: '让胡路区昆仑大街充电站',
    address: '让胡路区昆仑大街68号',
    longitude: 124.8847,
    latitude: 46.6269,
    operator: '国网电动',
    totalSpots: 10,
    availableSpots: 10,
    power: 60,
    chargerTypes: ['慢充'],
    price: 0.90,
    openHours: '08:00-20:00',
    phone: '',
    status: 'open',
    description: '让胡路区核心位置，适合居民区充电',
    createdAt: '2025-01-04T00:00:00.000Z',
    updatedAt: '2025-01-04T00:00:00.000Z'
  },
  {
    id: 'demo_5',
    name: '大庆市政府充电站',
    address: '萨尔图区世纪大道市政府院内停车场',
    longitude: 125.0382,
    latitude: 46.5897,
    operator: '特来电',
    totalSpots: 8,
    availableSpots: 3,
    power: 120,
    chargerTypes: ['快充', '慢充'],
    price: 1.10,
    openHours: '07:00-21:00',
    phone: '',
    status: 'open',
    description: '市政府院内东侧停车场',
    createdAt: '2025-01-05T00:00:00.000Z',
    updatedAt: '2025-01-05T00:00:00.000Z'
  },
  {
    id: 'demo_6',
    name: '龙凤区世纪大道充电站',
    address: '龙凤区世纪大道与龙永路交叉口',
    longitude: 125.1397,
    latitude: 46.5581,
    operator: '星星充电',
    totalSpots: 14,
    availableSpots: 0,
    power: 120,
    chargerTypes: ['快充', '慢充'],
    price: 1.25,
    openHours: '00:00-24:00',
    phone: '',
    status: 'closed',
    description: '临时关闭，设备升级中',
    createdAt: '2025-02-01T00:00:00.000Z',
    updatedAt: '2025-06-15T00:00:00.000Z'
  },
  {
    id: 'demo_7',
    name: '红岗区充电站',
    address: '红岗区萨大路红岗商城停车场',
    longitude: 124.9756,
    latitude: 46.3698,
    operator: '国网电动',
    totalSpots: 6,
    availableSpots: 5,
    power: 60,
    chargerTypes: ['慢充'],
    price: 0.85,
    openHours: '06:00-22:00',
    phone: '',
    status: 'open',
    description: '红岗商城西侧停车场',
    createdAt: '2025-02-10T00:00:00.000Z',
    updatedAt: '2025-02-10T00:00:00.000Z'
  },
  {
    id: 'demo_8',
    name: '大同区同阳路充电站',
    address: '大同区同阳路178号',
    longitude: 124.7983,
    latitude: 46.0487,
    operator: '特来电',
    totalSpots: 8,
    availableSpots: 8,
    power: 120,
    chargerTypes: ['快充', '慢充'],
    price: 1.15,
    openHours: '07:00-21:00',
    phone: '',
    status: 'maintenance',
    description: '设备维护中，预计7月10日恢复',
    createdAt: '2025-03-01T00:00:00.000Z',
    updatedAt: '2025-07-01T00:00:00.000Z'
  }
]

function loadFromStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const data = JSON.parse(raw)
      if (data.length > 0) return data
    }
    // First time: use demo data
    saveToStorage(DEMO_STATIONS)
    return JSON.parse(JSON.stringify(DEMO_STATIONS))
  } catch {
    return JSON.parse(JSON.stringify(DEMO_STATIONS))
  }
}

function saveToStorage(stations) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(stations))
}

export const useStationStore = defineStore('station', () => {
  const stations = ref(loadFromStorage())
  const searchQuery = ref('')
  const filterStatus = ref('all')
  const selectedStationId = ref(null)

  const filteredStations = computed(() => {
    let list = stations.value
    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase()
      list = list.filter(s =>
        s.name.toLowerCase().includes(q) ||
        s.address.toLowerCase().includes(q) ||
        s.operator.toLowerCase().includes(q)
      )
    }
    if (filterStatus.value !== 'all') {
      list = list.filter(s => s.status === filterStatus.value)
    }
    return list
  })

  const selectedStation = computed(() =>
    stations.value.find(s => s.id === selectedStationId.value) || null
  )

  function generateId() {
    return 'st_' + Date.now().toString(36) + '_' + Math.random().toString(36).slice(2, 8)
  }

  function addStation(data) {
    const now = new Date().toISOString()
    const station = {
      id: generateId(),
      name: data.name,
      address: data.address || '',
      longitude: data.longitude,
      latitude: data.latitude,
      operator: data.operator || '',
      totalSpots: data.totalSpots || 0,
      availableSpots: data.availableSpots || 0,
      power: data.power || 0,
      chargerTypes: data.chargerTypes || [],
      price: data.price || 0,
      openHours: data.openHours || '00:00-24:00',
      phone: data.phone || '',
      status: data.status || 'open',
      description: data.description || '',
      createdAt: now,
      updatedAt: now
    }
    stations.value.push(station)
    saveToStorage(stations.value)
    return station
  }

  function updateStation(id, data) {
    const idx = stations.value.findIndex(s => s.id === id)
    if (idx === -1) return null
    stations.value[idx] = {
      ...stations.value[idx],
      ...data,
      id: stations.value[idx].id,
      createdAt: stations.value[idx].createdAt,
      updatedAt: new Date().toISOString()
    }
    saveToStorage(stations.value)
    return stations.value[idx]
  }

  function deleteStation(id) {
    stations.value = stations.value.filter(s => s.id !== id)
    if (selectedStationId.value === id) selectedStationId.value = null
    saveToStorage(stations.value)
  }

  function exportData() {
    const blob = new Blob([JSON.stringify(stations.value, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `充电站数据备份_${new Date().toLocaleDateString()}.json`
    a.click()
    URL.revokeObjectURL(url)
  }

  function importData(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = (e) => {
        try {
          const data = JSON.parse(e.target.result)
          if (!Array.isArray(data)) throw new Error('数据格式错误')
          stations.value = data
          saveToStorage(stations.value)
          resolve(data.length)
        } catch (err) {
          reject(err)
        }
      }
      reader.onerror = () => reject(new Error('文件读取失败'))
      reader.readAsText(file)
    })
  }

  return {
    stations,
    searchQuery,
    filterStatus,
    selectedStationId,
    filteredStations,
    selectedStation,
    addStation,
    updateStation,
    deleteStation,
    exportData,
    importData
  }
})
