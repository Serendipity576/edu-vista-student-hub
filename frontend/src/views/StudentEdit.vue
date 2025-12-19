<template>
  <div class="student-edit">
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <span class="text-large font-600 mr-3">编辑学生</span>
      </template>
    </el-page-header>
    
    <el-card v-loading="loading">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="生日" prop="birthDate">
          <el-date-picker
            v-model="form.birthDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from '@/utils/axios'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const formRef = ref()

const form = reactive({
  studentNo: '',
  name: '',
  gender: '',
  birthDate: '',
  phone: '',
  email: '',
  address: ''
})

const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const loadStudent = async () => {
  loading.value = true
  try {
    const response = await axios.get(`/student/${route.params.id}`)
    if (response.data.code === 200) {
      Object.assign(form, response.data.data)
      if (form.birthDate) {
        form.birthDate = form.birthDate.split('T')[0]
      }
    }
  } catch (error) {
    ElMessage.error('加载学生信息失败')
  } finally {
    loading.value = false
  }
}

// 当路由参数 id 变化时，重新加载学生信息（支持在编辑页内跳转不同学生）
watch(
  () => route.params.id,
  () => {
    loadStudent()
  },
  { immediate: true }
)

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await axios.put(`/student/${route.params.id}`, form)
        if (response.data.code === 200) {
          ElMessage.success('更新成功')
          router.push({ name: 'StudentDetail', params: { id: route.params.id } })
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '更新失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const goBack = () => {
  router.back()
}
</script>

<style lang="scss" scoped>
.student-edit {
  .page-header {
    margin-bottom: 20px;
  }
}
</style>

