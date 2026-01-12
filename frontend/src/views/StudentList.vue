<template>
  <div class="student-list">
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <el-icon class="title-icon"><User /></el-icon>
          <h1>学生列表</h1>
        </div>
        <el-button type="primary" @click="handleAdd" v-if="authStore.user?.role === 'ADMIN'" class="add-btn">
          <el-icon><Plus /></el-icon>
          <span>添加学生</span>
        </el-button>
      </div>
    </div>
    
    <el-card class="main-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">学生信息</span>
          <el-tag type="info" size="small">共 {{ pagination.total }} 名学生</el-tag>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索学号或姓名"
            clearable
            @clear="loadStudents"
            @keyup.enter="loadStudents"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStudents" :icon="Search">搜索</el-button>
        </el-form-item>
      </el-form>
      
      <el-table 
        :data="students" 
        v-loading="loading" 
        stripe
        class="student-table"
        :row-class-name="tableRowClassName"
      >
        <el-table-column prop="studentNo" label="学号" width="120" sortable>
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.studentNo }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="120">
          <template #default="{ row }">
            <div class="name-cell">
              <el-avatar :src="row.avatar || '/default-avatar.png'" :size="32" class="table-avatar" />
              <span class="name-text">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            <el-tag :type="row.gender === '男' ? 'primary' : 'danger'" size="small">
              {{ row.gender }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="150">
          <template #default="{ row }">
            <el-icon class="icon-margin"><Phone /></el-icon>
            {{ row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱">
          <template #default="{ row }">
            <el-icon class="icon-margin"><Message /></el-icon>
            {{ row.email || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row.id)" :icon="View">
              详情
            </el-button>
            <el-button
              link
              type="warning"
              @click="handleEdit(row.id)"
              v-if="authStore.user?.role === 'ADMIN'"
              :icon="Edit"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(row.id)"
              v-if="authStore.user?.role === 'ADMIN'"
              :icon="Delete"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadStudents"
        @current-change="loadStudents"
        class="pagination"
      />
    </el-card>
    
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
      class="student-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="studentForm" :rules="rules" ref="formRef" label-width="100px" label-position="left">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="studentForm.studentNo" placeholder="请输入学号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="studentForm.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="studentForm.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="studentForm.phone" placeholder="请输入电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="studentForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="studentForm.address" type="textarea" :rows="3" placeholder="请输入地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Plus, Search, View, Edit, Delete, Phone, Message } from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const students = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加学生')
const formRef = ref()
const isEdit = ref(false)
const currentId = ref(null)

const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const studentForm = reactive({
  studentNo: '',
  name: '',
  gender: '',
  phone: '',
  email: '',
  address: ''
})

const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const loadStudents = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    const response = await axios.get('/student/list', { params })
    if (response.data.code === 200) {
      students.value = response.data.data.content
      pagination.total = response.data.data.totalElements
    }
  } catch (error) {
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加学生'
  Object.assign(studentForm, {
    studentNo: '',
    name: '',
    gender: '',
    phone: '',
    email: '',
    address: ''
  })
  dialogVisible.value = true
}

const handleView = (id) => {
  router.push({ name: 'StudentDetail', params: { id } })
}

const handleEdit = async (id) => {
  isEdit.value = true
  currentId.value = id
  dialogTitle.value = '编辑学生'
  try {
    const response = await axios.get(`/student/${id}`)
    if (response.data.code === 200) {
      Object.assign(studentForm, response.data.data)
      dialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载学生信息失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个学生吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await axios.delete(`/student/${id}`)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      loadStudents()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (isEdit.value) {
          response = await axios.put(`/student/${currentId.value}`, studentForm)
        } else {
          response = await axios.post('/student', studentForm)
        }
        if (response.data.code === 200) {
          // 显示成功消息
          ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
          
          // 如果是新增学生，检查并显示 Kafka 消息状态
          if (!isEdit.value && response.data.data) {
            const data = response.data.data
            console.log('创建学生返回的数据:', data)
            // 处理新的响应结构：{ student: {...}, kafkaMessageSent: true/false, kafkaMessage: "..." }
            if (data.kafkaMessageSent !== undefined) {
              if (data.kafkaMessageSent) {
                ElMessage({
                  message: data.kafkaMessage || '✅ Kafka 消息已成功发送：学生注册消息和欢迎消息已发送到消息队列',
                  type: 'success',
                  duration: 5000,
                  showClose: true
                })
              } else {
                ElMessage({
                  message: data.kafkaMessage || '⚠️ Kafka 消息发送失败，请检查 Kafka 服务是否正常运行',
                  type: 'warning',
                  duration: 6000,
                  showClose: true
                })
              }
            }
          }
          
          dialogVisible.value = false
          loadStudents()
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '操作失败')
      }
    }
  })
}

const tableRowClassName = ({ rowIndex }) => {
  return rowIndex % 2 === 1 ? 'table-row-even' : 'table-row-odd'
}

onMounted(() => {
  loadStudents()
})
</script>

<style lang="scss" scoped>
@import '../styles/mixins.scss';

.student-list {
  .page-header {
    margin-bottom: 24px;
    
    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-title {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .title-icon {
          font-size: 28px;
          color: var(--el-color-primary);
        }
        
        h1 {
          font-size: 24px;
          font-weight: 600;
          margin: 0;
          background: linear-gradient(135deg, var(--el-color-primary), #66b1ff);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }
      }
      
      .add-btn {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 10px 20px;
        border-radius: 8px;
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
        }
      }
    }
  }
  
  .main-card {
    border-radius: 12px;
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }
    
    :deep(.el-card__header) {
      padding: 18px 20px;
      border-bottom: 1px solid var(--el-border-color-lighter);
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .card-title {
          font-size: 16px;
          font-weight: 600;
          color: var(--el-text-color-primary);
        }
      }
    }
    
    :deep(.el-card__body) {
      padding: 20px;
    }
  }
  
  .search-form {
    margin-bottom: 20px;
    padding: 16px;
    background: var(--el-bg-color-page);
    border-radius: 8px;
    
    .search-input {
      width: 300px;
      
      :deep(.el-input__wrapper) {
        border-radius: 8px;
        transition: all 0.3s ease;
        
        &:hover {
          box-shadow: 0 0 0 1px var(--el-color-primary-light-7) inset;
        }
      }
    }
  }
  
  .student-table {
    :deep(.el-table__header) {
      th {
        background-color: var(--el-bg-color-page);
        color: var(--el-text-color-primary);
        font-weight: 600;
        border-bottom: 2px solid var(--el-border-color);
      }
    }
    
    :deep(.el-table__body) {
      tr {
        transition: all 0.2s ease;
        
        &:hover {
          background-color: var(--el-bg-color-page);
          transform: scale(1.01);
        }
      }
      
      .table-row-even {
        background-color: var(--el-bg-color);
      }
      
      .table-row-odd {
        background-color: var(--el-bg-color-page);
      }
    }
    
    .name-cell {
      display: flex;
      align-items: center;
      gap: 10px;
      
      .table-avatar {
        flex-shrink: 0;
      }
      
      .name-text {
        font-weight: 500;
        color: var(--el-text-color-primary);
      }
    }
    
    .icon-margin {
      margin-right: 6px;
      color: var(--el-text-color-secondary);
      font-size: 14px;
    }
  }
  
  .pagination {
    margin-top: 24px;
    display: flex;
    justify-content: flex-end;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-lighter);
  }
  
  .student-dialog {
    :deep(.el-dialog__header) {
      padding: 20px 20px 10px;
      border-bottom: 1px solid var(--el-border-color-lighter);
    }
    
    :deep(.el-dialog__body) {
      padding: 20px;
    }
    
    :deep(.el-form-item__label) {
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
  }
}

@include mobile {
  .student-list {
    .page-header .header-content {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
      
      .add-btn {
        width: 100%;
        justify-content: center;
      }
    }
    
    .search-form {
      .search-input {
        width: 100%;
      }
    }
  }
}
</style>

