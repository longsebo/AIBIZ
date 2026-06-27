<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="表单名称" prop="formName">
            <el-input v-model="queryParams.formName" placeholder="请输入表单名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['flowable:form:add']">新增表单</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="formList" border>
         <el-table-column label="表单ID" align="center" prop="id" width="80" />
         <el-table-column label="表单名称" align="center" prop="formName" :show-overflow-tooltip="true" />
         <el-table-column label="表单编码" align="center" prop="formCode" width="180" />
         <el-table-column label="表单描述" align="center" prop="formDesc" :show-overflow-tooltip="true" />
         <el-table-column label="状态" align="center" prop="status" width="80">
            <template #default="scope">
               <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
               <el-tag v-else type="danger">停用</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
         <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['flowable:form:edit']">编辑</el-button>
               <el-button link type="primary" icon="View" @click="handlePreview(scope.row)" v-hasPermi="['flowable:form:query']">预览</el-button>
               <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['flowable:form:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination
         v-show="total > 0"
         :total="total"
         v-model:page="queryParams.pageNum"
         v-model:limit="queryParams.pageSize"
         @pagination="getList"
      />

      <!-- 预览表单对话框 -->
      <el-dialog v-model="previewOpen" title="表单预览" width="70%" top="5vh" append-to-body>
         <FormPreview v-if="previewFormData" :form-data="previewFormData" />
      </el-dialog>
   </div>
</template>

<script setup name="Form">
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listForm, getForm, deleteForm } from '@/api/flowable/form'
import FormPreview from './FormPreview.vue'

const { proxy } = getCurrentInstance()
const router = useRouter()

const loading = ref(true)
const showSearch = ref(true)
const formList = ref([])
const total = ref(0)
const previewOpen = ref(false)
const previewFormData = ref(null)

const queryParams = reactive({
   pageNum: 1,
   pageSize: 10,
   formName: ''
})

onMounted(() => {
   getList()
})

async function getList() {
   loading.value = true
   try {
      const res = await listForm(queryParams)
      formList.value = res.rows || res.data || []
      total.value = res.total || formList.value.length
   } finally {
      loading.value = false
   }
}

function handleQuery() {
   queryParams.pageNum = 1
   getList()
}

function resetQuery() {
   proxy.$refs.queryRef.resetFields()
   handleQuery()
}

function handleAdd() {
   router.push({ path: '/flowable/form-design' })
}

function handleEdit(row) {
   router.push({ path: '/flowable/form-design/' + row.id })
}

async function handlePreview(row) {
   try {
      const res = await getForm(row.id)
      if (res.data && res.data.formJson) {
         previewFormData.value = JSON.parse(res.data.formJson)
         previewOpen.value = true
      }
   } catch (e) {
      proxy.$modal.msgError('获取表单数据失败')
   }
}

async function handleDelete(row) {
   try {
      await proxy.$modal.confirm(`确认删除表单 "${row.formName}" 吗？`)
      await deleteForm(row.id)
      proxy.$modal.msgSuccess('删除成功')
      getList()
   } catch (error) {
      // 用户取消
   }
}
</script>

<style scoped>
</style>