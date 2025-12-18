<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>数据仪表盘</h1>
    </div>
    
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-title">{{ stat.title }}</div>
          </div>
          <el-icon :size="40" class="stat-icon" :style="{ color: stat.color }">
            <component :is="stat.icon" />
          </el-icon>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <span>操作统计</span>
          </template>
          <v-chart :option="operationChartOption" :loading="loading" style="height: 300px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <span>每日操作趋势</span>
          </template>
          <v-chart :option="dailyChartOption" :loading="loading" style="height: 300px" />
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24">
        <el-card>
          <template #header>
            <span>操作热力图</span>
          </template>
          <v-chart :option="heatmapOption" :loading="loading" style="height: 400px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, HeatmapChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CalendarComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import axios from '@/utils/axios'
import { User, Document, Edit, Delete } from '@element-plus/icons-vue'

use([
  CanvasRenderer,
  BarChart,
  LineChart,
  HeatmapChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CalendarComponent
])

const loading = ref(false)
const stats = ref([
  { title: '总学生数', value: 0, icon: 'User', color: '#409eff' },
  { title: '操作次数', value: 0, icon: 'Document', color: '#67c23a' },
  { title: '编辑次数', value: 0, icon: 'Edit', color: '#e6a23c' },
  { title: '删除次数', value: 0, icon: 'Delete', color: '#f56c6c' }
])

const operationChartOption = reactive({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' }
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value'
  },
  series: [{
    data: [],
    type: 'bar',
    itemStyle: {
      color: '#409eff'
    }
  }]
})

const dailyChartOption = reactive({
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value'
  },
  series: [{
    data: [],
    type: 'line',
    smooth: true,
    areaStyle: {
      color: {
        type: 'linear',
        x: 0,
        y: 0,
        x2: 0,
        y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
        ]
      }
    }
  }]
})

const heatmapOption = reactive({
  tooltip: {
    position: 'top'
  },
  visualMap: {
    min: 0,
    max: 10,
    calculable: true,
    orient: 'horizontal',
    left: 'center',
    top: 'top',
    inRange: {
      color: ['#50a3ba', '#eac736', '#d94e5d']
    }
  },
  calendar: {
    top: 60,
    left: 30,
    right: 30,
    cellSize: ['auto', 13],
    range: '2024',
    itemStyle: {
      borderWidth: 0.5
    },
    yearLabel: { show: false }
  },
  series: [{
    type: 'heatmap',
    coordinateSystem: 'calendar',
    data: []
  }]
})

const loadData = async () => {
  loading.value = true
  try {
    const response = await axios.get('/dashboard/operation-stats')
    if (response.data.code === 200) {
      const data = response.data.data
      const operationCounts = data.operationCounts || {}
      const dailyStats = data.dailyStats || {}
      
      stats.value[1].value = Object.values(operationCounts).reduce((a, b) => a + b, 0)
      stats.value[2].value = operationCounts.updateStudent || 0
      stats.value[3].value = operationCounts.deleteStudent || 0
      
      operationChartOption.xAxis.data = Object.keys(operationCounts)
      operationChartOption.series[0].data = Object.values(operationCounts)
      
      const dates = Object.keys(dailyStats).sort()
      dailyChartOption.xAxis.data = dates.map(d => d.split('T')[0])
      dailyChartOption.series[0].data = dates.map(d => dailyStats[d])
      
      const heatmapData = dates.map(date => {
        const value = dailyStats[date]
        return [date, value]
      })
      heatmapOption.series[0].data = heatmapData
    }
    
    const studentResponse = await axios.get('/student/all')
    if (studentResponse.data.code === 200) {
      stats.value[0].value = studentResponse.data.data.length
    }
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
  setInterval(loadData, 30000)
})
</script>

<style lang="scss" scoped>
.dashboard {
  .page-header {
    margin-bottom: 20px;
  }
  
  .stat-card {
    margin-bottom: 20px;
    position: relative;
    overflow: hidden;
    
    .stat-content {
      .stat-value {
        font-size: 32px;
        font-weight: bold;
        margin-bottom: 10px;
      }
      
      .stat-title {
        font-size: 14px;
        color: var(--el-text-color-secondary);
      }
    }
    
    .stat-icon {
      position: absolute;
      right: 20px;
      top: 50%;
      transform: translateY(-50%);
      opacity: 0.3;
    }
  }
  
  .chart-row {
    margin-top: 20px;
  }
}
</style>

