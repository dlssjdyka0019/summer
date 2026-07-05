# 大庆市新能源汽车充电站管理系统

基于 **Vue 3 + Leaflet + OpenStreetMap** 的新能源汽车充电站管理程序，支持在大庆市实际地图上进行充电站的添加、编辑、删除和查询。

## 🚀 快速开始

```bash
# 1. 进入项目目录
cd ev-charging-manager

# 2. 安装依赖（如果卡顿可用淘宝镜像：npm install --registry=https://registry.npmmirror.com）
npm install

# 3. 启动开发服务器
npm run dev

# 4. 浏览器打开
# http://localhost:3000
```

## ✨ 功能特性

| 功能 | 说明 |
|------|------|
| 🗺️ **真实地图** | OpenStreetMap 提供的大庆市实际地图，完全免费无需 API Key |
| 📍 **添加充电站** | 点击地图任意位置 → 填写信息 → 自动生成标记 |
| ✏️ **编辑充电站** | 点击标记或列表项，修改充电站全部信息 |
| 🗑️ **删除充电站** | 一键删除，支持撤销提醒 |
| 🔍 **搜索筛选** | 按名称、地址、运营商搜索；按营业状态筛选 |
| 💾 **数据持久化** | localStorage 自动保存，刷新不丢失 |
| 📥 **数据导出** | 一键导出全量数据为 JSON 文件 |
| 📤 **数据导入** | 从 JSON 文件批量导入充电站数据 |
| 📱 **响应式设计** | 支持 PC 端和移动端浏览器 |
| ⚡ **演示数据** | 首次使用预置 8 个大庆市各区真实位置的充电站 |

## 📋 充电站信息字段

- 充电站名称、地址、坐标（经纬度）
- 运营商、营业状态（营业中/已关闭/维护中）
- 总车位数、可用车位数
- 充电功率（kW）、电费价格（元/度）
- 接口类型（快充/慢充/超快充/特斯拉专用）
- 营业时间、联系电话
- 备注信息

## 🛠️ 技术栈

- **前端框架**：Vue 3 (Composition API)
- **状态管理**：Pinia
- **构建工具**：Vite 5
- **地图库**：Leaflet + OpenStreetMap（免费，无需 API Key）
- **数据存储**：localStorage

## 📂 项目结构

```
ev-charging-manager/
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── main.js                    # 入口文件
    ├── App.vue                    # 主布局
    ├── stores/
    │   └── station.js             # Pinia 状态管理 + 演示数据
    ├── components/
    │   ├── MapView.vue            # 地图组件
    │   ├── StationSidebar.vue     # 侧边栏列表组件
    │   └── StationForm.vue        # 添加/编辑表单组件
    └── styles/
        └── main.css               # 全局样式
```

## ⚠️ 注意事项

- 地图使用 **OpenStreetMap** 瓦片服务，完全免费无需注册
- 数据存储在浏览器 localStorage 中，清除浏览器数据会丢失，请定期导出备份
- 首次打开自动加载大庆市 8 个演示充电站（覆盖萨尔图、龙凤、让胡路、红岗、大同区）
- 如果地图加载较慢，是因为 OSM 服务器在海外，耐心等待即可
