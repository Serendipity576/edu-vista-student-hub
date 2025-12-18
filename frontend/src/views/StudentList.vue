<template>
  <div class="student-list">
    <div class="page-header">
      <h1>学生列表</h1>
      <el-button type="primary" @click="handleAdd" v-if="authStore.user?.role === 'ADMIN'">
        添加学生
      </el-button>
    </div>
    
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索学号或姓名"
            clearable
            @clear="loadStudents"
            @keyup.enter="loadStudents"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStudents">搜索</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="students" v-loading="loading" stripe>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatar || '/default-avatar.png'" :size="40" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row.id)">详情</el-button>
            <el-button
              link
              type="warning"
              @click="handleEdit(row.id)"
              v-if="authStore.user?.role === 'ADMIN'"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(row.id)"
              v-if="authStore.user?.role === 'ADMIN'"
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
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="studentForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="studentForm.studentNo" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="studentForm.name" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="studentForm.gender" placeholder="请选择">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="studentForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="studentForm.email" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="studentForm.address" type="textarea" />
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
          ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadStudents()
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '操作失败')
      }
    }
  })
}

onMounted(() => {
  loadStudents()
})
</script>

<style lang="scss" scoped>
.student-list {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
  
  .search-form {
    margin-bottom: 20px;
  }
  
  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>

