<template>
  <div class="student-detail">
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <span class="text-large font-600 mr-3">学生详情</span>
      </template>
    </el-page-header>
    
    <el-card v-loading="loading" class="detail-card">
      <div class="detail-content">
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :disabled="authStore.user?.role !== 'ADMIN'"
          >
            <img v-if="student.avatar" :src="student.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <p class="avatar-hint" v-if="authStore.user?.role === 'ADMIN'">点击上传头像</p>
        </div>
        
        <div class="info-section">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="学号">{{ student.studentNo }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ student.name }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ student.gender }}</el-descriptions-item>
            <el-descriptions-item label="生日">{{ student.birthDate }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ student.phone }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ student.email }}</el-descriptions-item>
            <el-descriptions-item label="地址" :span="2">{{ student.address }}</el-descriptions-item>
            <el-descriptions-item label="班级">{{ student.studentClass?.className }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDate(student.createdAt) }}</el-descriptions-item>
          </el-descriptions>
          
          <div class="action-buttons" v-if="authStore.user?.role === 'ADMIN'">
            <el-button type="primary" @click="handleEdit">编辑</el-button>
            <el-button type="danger" @click="handleDelete">删除</el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const student = ref({})

const uploadUrl = ref('')
const uploadHeaders = ref({})

onMounted(() => {
  uploadUrl.value = `/api/student/${route.params.id}/avatar`
  uploadHeaders.value = {
    Authorization: `Bearer ${authStore.token}`
  }
  loadStudent()
})

const loadStudent = async () => {
  loading.value = true
  try {
    const response = await axios.get(`/student/${route.params.id}`)
    if (response.data.code === 200) {
      student.value = response.data.data
    }
  } catch (error) {
    ElMessage.error('加载学生信息失败')
  } finally {
    loading.value = false
  }
}

const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    student.value.avatar = response.data.avatar
    ElMessage.success('头像上传成功')
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleEdit = () => {
  router.push({ name: 'StudentEdit', params: { id: route.params.id } })
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个学生吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await axios.delete(`/student/${route.params.id}`)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      router.push({ name: 'StudentList' })
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const goBack = () => {
  router.back()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style lang="scss" scoped>
@import '../styles/mixins.scss';

.student-detail {
  .page-header {
    margin-bottom: 20px;
  }
  
  .detail-card {
    .detail-content {
      display: flex;
      gap: 40px;
      
      .avatar-section {
        text-align: center;
        
        .avatar-uploader {
          :deep(.el-upload) {
            border: 1px dashed var(--el-border-color);
            border-radius: 6px;
            cursor: pointer;
            position: relative;
            overflow: hidden;
            transition: var(--el-transition-duration-fast);
            
            &:hover {
              border-color: var(--el-color-primary);
            }
          }
          
          .avatar {
            width: 178px;
            height: 178px;
            display: block;
            object-fit: cover;
          }
          
          .avatar-uploader-icon {
            font-size: 28px;
            color: #8c939d;
            width: 178px;
            height: 178px;
            text-align: center;
            line-height: 178px;
          }
        }
        
        .avatar-hint {
          margin-top: 10px;
          color: var(--el-text-color-secondary);
          font-size: 12px;
        }
      }
      
      .info-section {
        flex: 1;
        
        .action-buttons {
          margin-top: 20px;
        }
      }
    }
  }
}

@include mobile {
  .detail-content {
    flex-direction: column;
  }
}
</style>

