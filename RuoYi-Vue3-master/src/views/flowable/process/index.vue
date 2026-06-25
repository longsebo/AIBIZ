<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="流程名称" prop="name">
            <el-input v-model="queryParams.name" placeholder="请输入流程名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button type="warning" plain icon="Upload" @click="handleImport" v-hasPermi="['flowable:process:deploy']">部署流程</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="dataList" border>
         <el-table-column label="流程ID" align="center" prop="id" :show-overflow-tooltip="true" width="200" />
         <el-table-column label="流程名称" align="center" prop="name" :show-overflow-tooltip="true" />
         <el-table-column label="流程Key" align="center" prop="key" :show-overflow-tooltip="true" width="150" />
         <el-table-column label="版本" align="center" prop="version" width="60" />
         <el-table-column label="分类" align="center" prop="category" width="100" />
         <el-table-column label="部署时间" align="center" prop="deploymentTime" width="160" />
         <el-table-column label="状态" align="center" prop="suspended" width="80">
            <template #default="scope">
               <el-tag v-if="scope.row.suspended" type="warning">挂起</el-tag>
               <el-tag v-else type="success">激活</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="VideoPlay" @click="handleStart(scope.row)" v-hasPermi="['flowable:process:start']">启动</el-button>
               <el-button link type="primary" icon="Picture" @click="handleDiagram(scope.row)" v-hasPermi="['flowable:process:query']">流程图</el-button>
               <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['flowable:process:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <!-- 启动流程对话框 -->
      <el-dialog v-model="startOpen" :title="'启动流程 - ' + startForm.name" width="500px" append-to-body>
         <el-form ref="startRef" :model="startForm" :rules="startRules" label-width="100px">
            <el-form-item label="业务键" prop="businessKey">
               <el-input v-model="startForm.businessKey" placeholder="请输入业务键(如请假单ID)" />
            </el-form-item>
            <el-form-item label="申请人" prop="applicant">
               <el-input v-model="startForm.applicant" placeholder="请输入申请人" />
            </el-form-item>
            <el-form-item label="申请事由" prop="reason">
               <el-input v-model="startForm.reason" type="textarea" :rows="3" placeholder="请输入申请事由" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitStart">确 定</el-button>
               <el-button @click="cancelStart">取 消</el-button>
            </div>
         </template>
      </el-dialog>

      <!-- 流程图对话框 -->
      <el-dialog v-model="diagramOpen" title="流程图" width="80%" top="5vh" append-to-body>
         <div v-html="diagramXml" class="diagram-container"></div>
      </el-dialog>

      <!-- 部署流程对话框 -->
      <el-dialog v-model="importOpen" title="部署流程" width="500px" append-to-body>
         <el-form ref="importRef" :model="importForm" :rules="importRules" label-width="100px">
            <el-form-item label="流程名称" prop="name">
               <el-input v-model="importForm.name" placeholder="请输入流程名称" />
            </el-form-item>
            <el-form-item label="流程分类" prop="category">
               <el-input v-model="importForm.category" placeholder="请输入流程分类" />
            </el-form-item>
            <el-form-item label="BPMN XML" prop="bpmnXml">
               <el-input v-model="importForm.bpmnXml" type="textarea" :rows="6" placeholder="请粘贴BPMN 2.0 XML内容" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitImport">确 定</el-button>
               <el-button @click="cancelImport">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Process">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
   listProcessDefinition,
   deployProcess,
   deleteProcessDefinition,
   startProcess,
   getProcessDiagram
} from '@/api/flowable'

const loading = ref(true)
const showSearch = ref(true)
const dataList = ref([])
const queryParams = reactive({ name: '' })

const startOpen = ref(false)
const diagramOpen = ref(false)
const importOpen = ref(false)
const diagramXml = ref('')
const startForm = reactive({ key: '', name: '', businessKey: '', applicant: '', reason: '' })
const importForm = reactive({ name: '', category: '', bpmnXml: '' })

const startRules = {
   businessKey: [{ required: true, message: '请输入业务键', trigger: 'blur' }]
}
const importRules = {
   name: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
   bpmnXml: [{ required: true, message: '请输入BPMN XML', trigger: 'blur' }]
}

onMounted(() => {
   getList()
})

async function getList() {
   loading.value = true
   try {
      const res = await listProcessDefinition()
      dataList.value = res.data || []
   } finally {
      loading.value = false
   }
}

function handleQuery() {
   getList()
}

function resetQuery() {
   queryParams.name = ''
   getList()
}

function handleStart(row) {
   startForm.key = row.key
   startForm.name = row.name
   startForm.businessKey = ''
   startForm.applicant = ''
   startForm.reason = ''
   startOpen.value = true
}

async function submitStart() {
   const variables = {
      applicant: startForm.applicant,
      reason: startForm.reason
   }
   await startProcess({
      processDefinitionKey: startForm.key,
      businessKey: startForm.businessKey,
      variables: variables
   })
   ElMessage.success('启动成功')
   startOpen.value = false
}

function cancelStart() {
   startOpen.value = false
}

async function handleDiagram(row) {
   const res = await getProcessDiagram(row.id)
   diagramXml.value = res.data?.xml || '<div>暂无流程图</div>'
   diagramOpen.value = true
}

function handleImport() {
   importForm.name = ''
   importForm.category = ''
   importForm.bpmnXml = ''
   importOpen.value = true
}

async function submitImport() {
   await deployProcess(importForm)
   ElMessage.success('部署成功')
   importOpen.value = false
   getList()
}

function cancelImport() {
   importOpen.value = false
}

async function handleDelete(row) {
   await ElMessageBox.confirm(`确认删除流程 "${row.name}" 吗？`)
   await deleteProcessDefinition(row.deploymentId)
   ElMessage.success('删除成功')
   getList()
}
</script>

<style scoped>
.diagram-container {
   text-align: center;
}
</style>
