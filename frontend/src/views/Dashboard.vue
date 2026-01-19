<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>数据仪表盘</h1>
    </div>
    
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper" :style="{ background: stat.gradient }">
              <el-icon :size="32" class="stat-icon">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
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
  CalendarComponent,
  VisualMapComponent
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
  CalendarComponent,
  VisualMapComponent
])

const loading = ref(false)
const stats = ref([
  { 
    title: '总学生数', 
    value: 0, 
    icon: 'User', 
    color: '#409eff',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  { 
    title: '操作次数', 
    value: 0, 
    icon: 'Document', 
    color: '#67c23a',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  { 
    title: '编辑次数', 
    value: 0, 
    icon: 'Edit', 
    color: '#e6a23c',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  { 
    title: '删除次数', 
    value: 0, 
    icon: 'Delete', 
    color: '#f56c6c',
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  }
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
@import '../styles/mixins.scss';

.dashboard {
  .page-header {
    margin-bottom: 24px;
    
    h1 {
      font-size: 24px;
      font-weight: 600;
      background: linear-gradient(135deg, var(--el-color-primary), #66b1ff);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
  
  .stats-row {
    margin-bottom: 24px;
  }
  
  .stat-card {
    border-radius: 12px;
    transition: all 0.3s ease;
    overflow: hidden;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    }
    
    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .stat-icon-wrapper {
        width: 64px;
        height: 64px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        
        .stat-icon {
          color: white;
        }
      }
      
      .stat-info {
        flex: 1;
        
        .stat-value {
          font-size: 32px;
          font-weight: 700;
          margin-bottom: 8px;
          color: var(--el-text-color-primary);
          line-height: 1;
        }
        
        .stat-title {
          font-size: 14px;
          color: var(--el-text-color-secondary);
          font-weight: 500;
        }
      }
    }
  }
  
  .chart-row {
    margin-top: 24px;
    
    :deep(.el-card) {
      border-radius: 12px;
      transition: all 0.3s ease;
      
      &:hover {
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }
      
      .el-card__header {
        padding: 18px 20px;
        border-bottom: 1px solid var(--el-border-color-lighter);
        font-weight: 600;
        font-size: 16px;
      }
    }
  }
}

@include mobile {
  .dashboard {
    .stat-card .stat-content {
      .stat-icon-wrapper {
        width: 56px;
        height: 56px;
      }
      
      .stat-info .stat-value {
        font-size: 28px;
      }
    }
  }
}
</style>

