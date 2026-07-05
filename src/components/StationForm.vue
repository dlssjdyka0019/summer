<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <div class="modal-header">
        <h2>{{ isEdit ? '编辑充电站' : '添加充电站' }}</h2>
        <button class="btn-icon" @click="$emit('close')" title="关闭">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6 6 18M6 6l12 12"/>
          </svg>
        </button>
      </div>

      <div class="modal-body">
        <!-- Coordinate Display -->
        <div
          v-if="!isEdit"
          class="coord-display"
          :class="{ 'coord-pick': !hasCoords }"
          @click="startMapPick"
          style="cursor: pointer;"
        >
          <template v-if="hasCoords">
            📍 已选位置：
            <span>{{ formData.longitude.toFixed(6) }}, {{ formData.latitude.toFixed(6) }}</span>
            <span style="font-size:11px;color:#16a34a;margin-left:8px;">(点击地图可重新选位置)</span>
          </template>
          <template v-else>
            👆 <strong>请在右侧地图上点击充电站的位置</strong>
          </template>
        </div>

        <!-- Name -->
        <div class="form-group">
          <label class="required">充电站名称</label>
          <input v-model="formData.name" placeholder="例如：大庆万达充电站" maxlength="50" />
        </div>

        <!-- Address -->
        <div class="form-group">
          <label>地址</label>
          <input v-model="formData.address" placeholder="详细地址" maxlength="100" />
        </div>

        <!-- Operator & Status -->
        <div class="form-row">
          <div class="form-group">
            <label>运营商</label>
            <input v-model="formData.operator" placeholder="例如：国网、特来电" maxlength="30" />
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="formData.status">
              <option value="open">✅ 营业中</option>
              <option value="closed">❌ 已关闭</option>
              <option value="maintenance">🔧 维护中</option>
            </select>
          </div>
        </div>

        <!-- Spots -->
        <div class="form-row">
          <div class="form-group">
            <label>总车位数</label>
            <input v-model.number="formData.totalSpots" type="number" min="0" placeholder="0" />
          </div>
          <div class="form-group">
            <label>可用车位数</label>
            <input v-model.number="formData.availableSpots" type="number" min="0" placeholder="0" />
          </div>
        </div>

        <!-- Power & Price -->
        <div class="form-row">
          <div class="form-group">
            <label>功率 (kW)</label>
            <input v-model.number="formData.power" type="number" min="0" step="1" placeholder="例如：120" />
          </div>
          <div class="form-group">
            <label>电费 (元/度)</label>
            <input v-model.number="formData.price" type="number" min="0" step="0.01" placeholder="例如：1.20" />
          </div>
        </div>

        <!-- Charger Types -->
        <div class="form-group">
          <label>接口类型</label>
          <div class="checkbox-group">
            <label><input type="checkbox" value="快充" v-model="formData.chargerTypes" /> 快充 (DC)</label>
            <label><input type="checkbox" value="慢充" v-model="formData.chargerTypes" /> 慢充 (AC)</label>
            <label><input type="checkbox" value="超快充" v-model="formData.chargerTypes" /> 超快充</label>
            <label><input type="checkbox" value="特斯拉" v-model="formData.chargerTypes" /> 特斯拉专用</label>
          </div>
        </div>

        <!-- Open Hours & Phone -->
        <div class="form-row">
          <div class="form-group">
            <label>营业时间</label>
            <input v-model="formData.openHours" placeholder="例如：06:00-23:00" />
          </div>
          <div class="form-group">
            <label>联系电话</label>
            <input v-model="formData.phone" placeholder="联系电话" maxlength="20" />
          </div>
        </div>

        <!-- Description -->
        <div class="form-group">
          <label>备注</label>
          <textarea v-model="formData.description" placeholder="其他备注信息..." maxlength="200"></textarea>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn" @click="$emit('close')">取消</button>
        <button class="btn btn-primary" @click="handleSubmit" :disabled="!canSubmit">
          {{ isEdit ? '保存修改' : '添加充电站' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useStationStore } from '../stores/station.js'

const props = defineProps({
  stationId: { type: String, default: null },
  lat: { type: Number, default: null },
  lng: { type: Number, default: null },
})

const emit = defineEmits(['close', 'saved'])
const store = useStationStore()

const isEdit = computed(() => !!props.stationId)

const formData = ref({
  name: '',
  address: '',
  longitude: 0,
  latitude: 0,
  operator: '',
  totalSpots: 0,
  availableSpots: 0,
  power: 0,
  chargerTypes: [],
  price: 0,
  openHours: '00:00-24:00',
  phone: '',
  status: 'open',
  description: '',
})

const hasCoords = computed(() =>
  formData.value.longitude !== 0 && formData.value.latitude !== 0
)

const canSubmit = computed(() => !!formData.value.name.trim())

function applyCoords(lat, lng) {
  formData.value.latitude = lat
  formData.value.longitude = lng
}

function startMapPick() {
  store.selectedStationId = '__adding__'
}

function onMapPick(e) {
  if (!isEdit.value) {
    applyCoords(e.detail.lat, e.detail.lng)
    store.selectedStationId = null
  }
}

// Watch parent props for coordinate changes
watch(() => props.lat, (val) => {
  if (val !== null) formData.value.latitude = val
})
watch(() => props.lng, (val) => {
  if (val !== null) formData.value.longitude = val
})

onMounted(() => {
  if (isEdit.value) {
    const station = store.stations.find(s => s.id === props.stationId)
    if (station) {
      formData.value = {
        name: station.name,
        address: station.address,
        longitude: station.longitude,
        latitude: station.latitude,
        operator: station.operator,
        totalSpots: station.totalSpots,
        availableSpots: station.availableSpots,
        power: station.power,
        chargerTypes: [...station.chargerTypes],
        price: station.price,
        openHours: station.openHours,
        phone: station.phone,
        status: station.status,
        description: station.description,
      }
    }
  } else if (props.lat !== null && props.lng !== null) {
    formData.value.longitude = props.lng
    formData.value.latitude = props.lat
  }

  window.addEventListener('map-click-add', onMapPick)
})

onUnmounted(() => {
  window.removeEventListener('map-click-add', onMapPick)
})

function handleSubmit() {
  if (!canSubmit.value) return

  try {
    if (isEdit.value) {
      store.updateStation(props.stationId, formData.value)
      window.dispatchEvent(new CustomEvent('toast', {
        detail: { msg: '充电站信息已更新', type: 'success' }
      }))
    } else {
      store.addStation(formData.value)
      window.dispatchEvent(new CustomEvent('toast', {
        detail: { msg: '充电站添加成功！', type: 'success' }
      }))
    }
    emit('saved')
  } catch (err) {
    window.dispatchEvent(new CustomEvent('toast', {
      detail: { msg: '操作失败：' + err.message, type: 'error' }
    }))
  }
}
</script>
