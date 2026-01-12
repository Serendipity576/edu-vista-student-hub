<template>
  <div class="student-detail">
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <span class="text-large font-600 mr-3">学生详情</span>
      </template>
    </el-page-header>
    
    <el-card v-loading="loading" class="detail-card" shadow="hover">
      <div class="detail-content">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :disabled="authStore.user?.role !== 'ADMIN'"
            >
              <div class="avatar-container">
                <img v-if="student.avatar" :src="student.avatar" class="avatar" />
                <div v-else class="avatar-placeholder">
                  <el-icon class="avatar-uploader-icon"><User /></el-icon>
                </div>
                <div v-if="authStore.user?.role === 'ADMIN'" class="avatar-overlay">
                  <el-icon><Camera /></el-icon>
                </div>
              </div>
            </el-upload>
            <p class="avatar-hint" v-if="authStore.user?.role === 'ADMIN'">点击上传头像</p>
            <h2 class="student-name">{{ student.name }}</h2>
            <el-tag :type="student.gender === '男' ? 'primary' : 'danger'" size="large" class="gender-tag">
              {{ student.gender }}
            </el-tag>
          </div>
        </div>
        
        <div class="info-section">
          <el-descriptions :column="2" border class="info-descriptions">
            <el-descriptions-item label="学号">
              <el-tag type="primary">{{ student.studentNo }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="姓名">{{ student.name }}</el-descriptions-item>
            <el-descriptions-item label="性别">
              <el-tag :type="student.gender === '男' ? 'primary' : 'danger'">{{ student.gender }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="生日">{{ student.birthDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="电话">
              <el-icon class="info-icon"><Phone /></el-icon>
              {{ student.phone || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              <el-icon class="info-icon"><Message /></el-icon>
              {{ student.email || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="地址" :span="2">
              <el-icon class="info-icon"><Location /></el-icon>
              {{ student.address || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="班级">
              <el-tag type="info">{{ student.studentClass?.className || '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              <el-icon class="info-icon"><Clock /></el-icon>
              {{ formatDate(student.createdAt) }}
            </el-descriptions-item>
          </el-descriptions>
          
          <div class="action-buttons" v-if="authStore.user?.role === 'ADMIN'">
            <el-button type="primary" @click="handleEdit" :icon="Edit" size="large">
              编辑信息
            </el-button>
            <el-button type="danger" @click="handleDelete" :icon="Delete" size="large">
              删除学生
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User, Camera, Phone, Message, Location, Clock, Edit, Delete } from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const student = ref({})

const uploadUrl = ref('')
const uploadHeaders = ref({})

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

// 当路由参数 id 变化时，重新加载学生信息与上传配置（支持在详情页内跳转不同学生）
watch(
  () => route.params.id,
  () => {
    uploadUrl.value = `/api/student/${route.params.id}/avatar`
    uploadHeaders.value = {
      Authorization: `Bearer ${authStore.token}`
    }
    loadStudent()
  },
  { immediate: true }
)

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
    margin-bottom: 24px;
    
    :deep(.el-page-header__content) {
      .text-large {
        font-size: 24px;
        font-weight: 600;
        background: linear-gradient(135deg, var(--el-color-primary), #66b1ff);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
    }
  }
  
  .detail-card {
    border-radius: 12px;
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }
    
    :deep(.el-card__body) {
      padding: 30px;
    }
    
    .detail-content {
      display: flex;
      gap: 40px;
      
      .avatar-section {
        text-align: center;
        min-width: 220px;
        
        .avatar-wrapper {
          .avatar-uploader {
            :deep(.el-upload) {
              border: none;
              border-radius: 50%;
              cursor: pointer;
              position: relative;
              overflow: visible;
              transition: all 0.3s ease;
              
              &:hover {
                transform: scale(1.05);
                
                .avatar-overlay {
                  opacity: 1;
                }
              }
            }
            
            .avatar-container {
              position: relative;
              width: 180px;
              height: 180px;
              margin: 0 auto;
              border-radius: 50%;
              overflow: hidden;
              box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
              border: 4px solid var(--el-color-primary-light-9);
              
              .avatar {
                width: 100%;
                height: 100%;
                object-fit: cover;
              }
              
              .avatar-placeholder {
                width: 100%;
                height: 100%;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                display: flex;
                align-items: center;
                justify-content: center;
                
                .avatar-uploader-icon {
                  font-size: 64px;
                  color: white;
                }
              }
              
              .avatar-overlay {
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: rgba(0, 0, 0, 0.5);
                display: flex;
                align-items: center;
                justify-content: center;
                opacity: 0;
                transition: opacity 0.3s ease;
                
                .el-icon {
                  font-size: 32px;
                  color: white;
                }
              }
            }
          }
          
          .avatar-hint {
            margin-top: 16px;
            color: var(--el-text-color-secondary);
            font-size: 13px;
          }
          
          .student-name {
            margin: 20px 0 12px;
            font-size: 24px;
            font-weight: 600;
            color: var(--el-text-color-primary);
          }
          
          .gender-tag {
            margin-top: 8px;
          }
        }
      }
      
      .info-section {
        flex: 1;
        
        .info-descriptions {
          :deep(.el-descriptions__label) {
            font-weight: 600;
            color: var(--el-text-color-primary);
          }
          
          :deep(.el-descriptions__content) {
            color: var(--el-text-color-regular);
          }
          
          .info-icon {
            margin-right: 6px;
            color: var(--el-text-color-secondary);
            font-size: 14px;
          }
        }
        
        .action-buttons {
          margin-top: 30px;
          display: flex;
          gap: 12px;
          padding-top: 20px;
          border-top: 1px solid var(--el-border-color-lighter);
          
          .el-button {
            flex: 1;
            border-radius: 8px;
            transition: all 0.3s ease;
            
            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            }
          }
        }
      }
    }
  }
}

@include mobile {
  .detail-content {
    flex-direction: column;
    gap: 24px;
    
    .avatar-section {
      min-width: auto;
    }
  }
}
</style>

