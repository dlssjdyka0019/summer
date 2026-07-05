<template>
  <div class="sidebar">
    <!-- Toolbar -->
    <div class="sidebar-toolbar">
      <div class="search-box">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
        </svg>
        <input
          v-model="store.searchQuery"
          placeholder="搜索充电站名称、地址..."
          type="text"
        />
      </div>
      <div class="filter-row">
        <select v-model="store.filterStatus">
          <option value="all">全部状态</option>
          <option value="open">✅ 营业中</option>
          <option value="closed">❌ 已关闭</option>
          <option value="maintenance">🔧 维护中</option>
        </select>
        <span class="station-count">共 {{ store.filteredStations.length }} 个</span>
      </div>
    </div>

    <!-- Station List -->
    <div class="sidebar-list" v-if="store.filteredStations.length > 0">
      <div
        v-for="station in store.filteredStations"
        :key="station.id"
        class="station-card"
        :class="{ active: store.selectedStationId === station.id }"
        @click="selectStation(station)"
      >
        <div class="station-card-header">
          <h3>{{ station.name }}</h3>
          <span class="status-badge" :class="'status-' + station.status">
            {{ statusMap[station.status] || '未知' }}
          </span>
        </div>
        <div class="address">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
          {{ station.address || '地址未填写' }}
        </div>
        <div class="tags">
          <span class="tag tag-green" v-for="t in station.chargerTypes" :key="t">{{ t }}</span>
          <span class="tag tag-blue" v-if="station.power">{{ station.power }}kW</span>
        </div>
        <div class="meta">
          <span class="meta-item">
            🅿️ {{ station.availableSpots }}/{{ station.totalSpots }}
          </span>
          <span class="meta-item" v-if="station.price">
            💰 ¥{{ station.price }}/度
          </span>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div class="empty-state" v-else>
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M5 12h14M12 5l7 7-7 7"/>
      </svg>
      <p v-if="store.stations.length === 0">
        还没有充电站<br>在地图上点击或点击下方按钮添加
      </p>
      <p v-else>没有匹配的充电站</p>
    </div>
  </div>
</template>

<script setup>
import { useStationStore } from '../stores/station.js'

const store = useStationStore()

const statusMap = {
  open: '营业中',
  closed: '已关闭',
  maintenance: '维护中'
}

const emit = defineEmits(['select-station'])

function selectStation(station) {
  store.selectedStationId = station.id
  emit('select-station', station)
}
</script>
