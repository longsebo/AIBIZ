<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="流程名称" prop="name">
            <el-input v-model="queryParams.name" placeholder="请输入流程名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
         </el-form-item>
         <el-form-item label="流程分类" prop="category">
            <el-select v-model="queryParams.category" placeholder="请选择分类" clearable style="width: 200px">
               <el-option v-for="item in categoryList" :key="item.categoryCode" :label="item.categoryName" :value="item.categoryCode" />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['flowable:process:deploy']">新增流程</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="dataList" border>
         <el-table-column label="流程ID" align="center" prop="id" :show-overflow-tooltip="true" width="200" />
         <el-table-column label="流程名称" align="center" prop="name" :show-overflow-tooltip="true" />
         <el-table-column label="流程Key" align="center" prop="key" :show-overflow-tooltip="true" width="150" />
         <el-table-column label="版本" align="center" prop="version" width="60" />
         <el-table-column label="分类" align="center" prop="category" width="120">
            <template #default="scope">
               {{ getCategoryName(scope.row.category) }}
            </template>
         </el-table-column>
         <el-table-column label="部署时间" align="center" prop="deploymentTime" width="180" />
         <el-table-column label="状态" align="center" prop="suspended" width="80">
            <template #default="scope">
               <el-tag v-if="scope.row.suspended" type="warning">挂起</el-tag>
               <el-tag v-else type="success">激活</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleDesign(scope.row)" v-hasPermi="['flowable:process:design']">流程设计</el-button>
               <el-button link type="primary" icon="VideoPlay" @click="handleStart(scope.row)" v-hasPermi="['flowable:process:start']">启动</el-button>
               <el-button link type="primary" icon="Picture" @click="handleDiagram(scope.row)" v-hasPermi="['flowable:process:query']">流程图</el-button>
               <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['flowable:process:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <!-- 新增流程对话框 -->
      <el-dialog :title="'新增流程'" v-model="addOpen" width="500px" append-to-body>
         <el-form ref="addRef" :model="addForm" :rules="addRules" label-width="100px">
            <el-form-item label="流程名称" prop="name">
               <el-input v-model="addForm.name" placeholder="请输入流程名称" />
            </el-form-item>
            <el-form-item label="流程分类" prop="category">
               <el-select v-model="addForm.category" placeholder="请选择流程分类" style="width: 100%">
                  <el-option v-for="item in categoryList" :key="item.categoryCode" :label="item.categoryName" :value="item.categoryCode" />
               </el-select>
            </el-form-item>
            <el-form-item label="流程Key" prop="key">
               <el-input v-model="addForm.key" placeholder="请输入流程Key（唯一标识）" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitAdd">确 定</el-button>
               <el-button @click="cancelAdd">取 消</el-button>
            </div>
         </template>
      </el-dialog>

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
   </div>
</template>

<script setup name="Process">
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
   listProcessDefinition,
   deployProcess,
   deleteProcessDefinition,
   startProcess,
   getProcessDiagram
} from '@/api/flowable'
import { listCategoryAll } from '@/api/flowable/category'

const { proxy } = getCurrentInstance()
const router = useRouter()

const loading = ref(true)
const showSearch = ref(true)
const dataList = ref([])
const categoryList = ref([])
const queryParams = reactive({ name: '', category: '' })

const addOpen = ref(false)
const startOpen = ref(false)
const diagramOpen = ref(false)
const diagramXml = ref('')
const addForm = reactive({ name: '', category: '', key: '' })
const startForm = reactive({ key: '', name: '', businessKey: '', applicant: '', reason: '' })

const addRules = {
   name: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
   key: [{ required: true, message: '请输入流程Key', trigger: 'blur' }]
}
const startRules = {
   businessKey: [{ required: true, message: '请输入业务键', trigger: 'blur' }]
}

onMounted(() => {
   getList()
   getCategoryList()
})

async function getList() {
   loading.value = true
   try {
      const res = await listProcessDefinition()
      let list = res.data || []
      // 前端过滤
      if (queryParams.name) {
         list = list.filter(item => item.name && item.name.includes(queryParams.name))
      }
      if (queryParams.category) {
         list = list.filter(item => item.category === queryParams.category)
      }
      dataList.value = list
   } finally {
      loading.value = false
   }
}

async function getCategoryList() {
   try {
      const res = await listCategoryAll()
      categoryList.value = res.data || []
   } catch (e) {
      console.error('加载分类列表失败', e)
   }
}

function getCategoryName(categoryCode) {
   if (!categoryCode) return '-'
   const category = categoryList.value.find(item => item.categoryCode === categoryCode)
   return category ? category.categoryName : categoryCode
}

function handleQuery() {
   getList()
}

function resetQuery() {
   queryParams.name = ''
   queryParams.category = ''
   getList()
}

function generateDefaultBpmn(processName, processKey) {
   return `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
   xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
   xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
   targetNamespace="http://flowable.org/bpmn20">
   <process id="${processKey}" name="${processName}">
      <startEvent id="startEvent1" name="开始">
         <outgoing>flow1</outgoing>
      </startEvent>
      <userTask id="userTask1" name="审批节点" assignee="\${initiator}">
         <incoming>flow1</incoming>
         <outgoing>flow2</outgoing>
      </userTask>
      <endEvent id="endEvent1" name="结束">
         <incoming>flow2</incoming>
      </endEvent>
      <sequenceFlow id="flow1" sourceRef="startEvent1" targetRef="userTask1" />
      <sequenceFlow id="flow2" sourceRef="userTask1" targetRef="endEvent1" />
   </process>
   <bpmndi:BPMNDiagram id="BPMNDiagram1">
      <bpmndi:BPMNPlane bpmnElement="${processKey}">
         <bpmndi:BPMNShape bpmnElement="startEvent1">
            <omgdc:Bounds x="100" y="100" width="30" height="30" />
         </bpmndi:BPMNShape>
         <bpmndi:BPMNShape bpmnElement="userTask1">
            <omgdc:Bounds x="180" y="80" width="100" height="60" />
         </bpmndi:BPMNShape>
         <bpmndi:BPMNShape bpmnElement="endEvent1">
            <omgdc:Bounds x="330" y="100" width="30" height="30" />
         </bpmndi:BPMNShape>
         <bpmndi:BPMNEdge bpmnElement="flow1">
            <omgdi:waypoint x="130" y="115" />
            <omgdi:waypoint x="180" y="110" />
         </bpmndi:BPMNEdge>
         <bpmndi:BPMNEdge bpmnElement="flow2">
            <omgdi:waypoint x="280" y="110" />
            <omgdi:waypoint x="330" y="115" />
         </bpmndi:BPMNEdge>
      </bpmndi:BPMNPlane>
   </bpmndi:BPMNDiagram>
</definitions>`
}

function handleAdd() {
   addForm.name = ''
   addForm.category = ''
   addForm.key = ''
   addOpen.value = true
}

async function submitAdd() {
   proxy.$refs["addRef"].validate(async valid => {
      if (valid) {
         try {
            const bpmnXml = generateDefaultBpmn(addForm.name, addForm.key)
            await deployProcess({
               name: addForm.name,
               category: addForm.category,
               bpmnXml: bpmnXml
            })
            proxy.$modal.msgSuccess('部署成功')
            addOpen.value = false
            getList()
         } catch (error) {
            proxy.$modal.msgError('部署失败')
         }
      }
   })
}

function cancelAdd() {
   addOpen.value = false
}

function handleDesign(row) {
   router.push({ path: '/flowable/process-designer/' + row.key, query: { name: row.name } })
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
   try {
      await startProcess({
         processDefinitionKey: startForm.key,
         businessKey: startForm.businessKey,
         variables: variables
      })
      proxy.$modal.msgSuccess('启动成功')
      startOpen.value = false
   } catch (error) {
      proxy.$modal.msgError('启动失败')
   }
}

function cancelStart() {
   startOpen.value = false
}

async function handleDiagram(row) {
   try {
      const res = await getProcessDiagram(row.id)
      diagramXml.value = res.data?.xml || '<div>暂无流程图</div>'
      diagramOpen.value = true
   } catch (error) {
      proxy.$modal.msgError('获取流程图失败')
   }
}

async function handleDelete(row) {
   try {
      await proxy.$modal.confirm(`确认删除流程 "${row.name}" 吗？`)
      await deleteProcessDefinition(row.deploymentId)
      proxy.$modal.msgSuccess('删除成功')
      getList()
   } catch (error) {
      // 用户取消
   }
}
</script>

<style scoped>
.diagram-container {
   text-align: center;
}
</style>