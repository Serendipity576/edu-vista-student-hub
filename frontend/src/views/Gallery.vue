<template>
  <div class="gallery">
    <div class="page-header">
      <h1>班级相册</h1>
    </div>
    
    <el-card>
      <div class="gallery-container" v-loading="loading">
        <div
          v-for="(student, index) in students"
          :key="student.id"
          class="gallery-item"
          :style="{ height: student.randomHeight + 'px' }"
          @click="handleImageClick(student)"
        >
          <el-image
            :src="student.avatar ? `${student.avatar}` : 'https://via.placeholder.com/200x200/cccccc/000000?text=No+Avatar'"
            :alt="student.name"
            fit="cover"
            lazy
            class="gallery-image"
          />
          <div class="image-overlay">
            <div class="student-info">
              <p class="student-name">{{ student.name }}</p>
              <p class="student-no">{{ student.studentNo }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[12, 24, 48, 96]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadStudents"
        @current-change="loadStudents"
        class="pagination"
      />
    </el-card>
    
    <el-dialog v-model="previewVisible" title="预览" width="600px">
      <div class="preview-content" v-if="previewStudent">
        <el-image
          :src="previewStudent.avatar || '/default-avatar.png'"
          fit="contain"
          style="width: 100%; max-height: 500px"
        />
        <div class="preview-info">
          <p><strong>姓名：</strong>{{ previewStudent.name }}</p>
          <p><strong>学号：</strong>{{ previewStudent.studentNo }}</p>
          <p><strong>班级：</strong>{{ previewStudent.className }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/utils/axios'

const loading = ref(false)
const students = ref([])
const previewVisible = ref(false)
const previewStudent = ref(null)

const pagination = reactive({
  page: 1,
  size: 24,
  total: 0
})

const heights = [200, 250, 300, 350]

const loadStudents = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    const response = await axios.get('/student/list', { params })
    if (response.data.code === 200) {
      students.value = response.data.data.content.map(student => ({
        ...student,
        randomHeight: heights[Math.floor(Math.random() * heights.length)]
      }))
      pagination.total = response.data.data.totalElements
    }
  } catch (error) {
    console.error('加载学生列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleImageClick = (student) => {
  previewStudent.value = student
  previewVisible.value = true
}

onMounted(() => {
  loadStudents()
})
</script>

<style lang="scss" scoped>
@import '../styles/mixins.scss';

.gallery {
  .gallery-container {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 20px;
    margin-bottom: 20px;
    
    .gallery-item {
      position: relative;
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      transition: transform 0.3s, box-shadow 0.3s;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
        
        .image-overlay {
          opacity: 1;
        }
      }
      
      .gallery-image {
        width: 100%;
        height: 100%;
      }
      
      .image-overlay {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
        color: white;
        padding: 15px;
        opacity: 0;
        transition: opacity 0.3s;
        
        .student-info {
          .student-name {
            font-size: 16px;
            font-weight: bold;
            margin-bottom: 5px;
          }
          
          .student-no {
            font-size: 12px;
            opacity: 0.9;
          }
        }
      }
    }
  }
  
  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
  
  .preview-content {
    .preview-info {
      margin-top: 20px;
      
      p {
        margin-bottom: 10px;
        font-size: 14px;
      }
    }
  }
}

@include mobile {
  .gallery-container {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 10px;
  }
}
</style>

