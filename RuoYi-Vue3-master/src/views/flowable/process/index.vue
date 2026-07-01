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
         <el-table-column label="分类" align="center" prop="categoryName" width="120" />
         <el-table-column label="部署时间" align="center" prop="deploymentTime" width="180" />
         <el-table-column label="状态" align="center" prop="suspended" width="80">
            <template #default="scope">
               <el-tag v-if="scope.row.suspended" type="warning">挂起</el-tag>
               <el-tag v-else type="success">激活</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="360" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleDesign(scope.row)" v-hasPermi="['flowable:process:design']">流程设计</el-button>
               <el-button link type="primary" icon="Document" @click="handleBindForm(scope.row)" v-hasPermi="['flowable:processForm:add']">绑定表单</el-button>
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

      <!-- 启动流程组件 -->
      <ProcessStart 
         :visible="startOpen"
         :process-key="startProcessKey"
         :process-name="startProcessName"
         @close="cancelStart"
      />

      <!-- 流程图对话框 -->
      <el-dialog v-model="diagramOpen" title="流程图" width="80%" top="5vh" append-to-body>
         <img v-if="diagramXml" :src="diagramXml" class="diagram-container" />
         <div v-else class="diagram-container">暂无流程图</div>
      </el-dialog>

      <!-- 绑定表单对话框 -->
      <el-dialog :title="'绑定表单 - ' + bindForm.processName" v-model="bindOpen" width="500px" append-to-body>
         <el-form ref="bindRef" :model="bindForm" :rules="bindRules" label-width="100px">
            <el-form-item label="流程名称" prop="processName">
               <el-input v-model="bindForm.processName" disabled />
            </el-form-item>
            <el-form-item label="流程Key" prop="processKey">
               <el-input v-model="bindForm.processKey" disabled />
            </el-form-item>
            <el-form-item label="绑定表单" prop="formId">
               <el-select v-model="bindForm.formId" placeholder="请选择表单" style="width: 100%">
                  <el-option v-for="item in formList" :key="item.id" :label="item.formName" :value="item.id" />
               </el-select>
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitBind">确 定</el-button>
               <el-button @click="cancelBind">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Process">
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import {
   listProcessDefinition,
   deployProcess,
   deleteProcessDefinition,
   startProcess,
   getProcessDiagram
} from '@/api/flowable'
import { listCategoryAll } from '@/api/flowable/category'
import { listForm } from '@/api/flowable/form'
import { bindProcessForm, updateProcessForm, getProcessFormByKey } from '@/api/flowable/processForm'
import ProcessStart from './ProcessStart.vue'

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
const bindOpen = ref(false)
const diagramXml = ref('')
const formList = ref([])
const startProcessKey = ref('')
const startProcessName = ref('')
const addForm = reactive({ name: '', category: '', key: '' })
const bindForm = reactive({ id: '', processKey: '', processName: '', formId: '' })

const addRules = {
   name: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
   key: [{ required: true, message: '请输入流程Key', trigger: 'blur' }]
}
const bindRules = {
   formId: [{ required: true, message: '请选择表单', trigger: 'change' }]
}

onMounted(() => {
   getList()
   getCategoryList()
   getFormList()
})

async function getFormList() {
   try {
      const res = await listForm({})
      formList.value = res.rows || res.data || []
   } catch (e) {
      console.error('加载表单列表失败', e)
   }
}

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

function handleQuery() {
   getList()
}

function resetQuery() {
   queryParams.name = ''
   queryParams.category = ''
   getList()
}

function generateDefaultBpmn(processName, processKey, category) {
   const ns = category || 'other'
   return `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xmlns:flowable="http://flowable.org/bpmn"
   xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
   xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
   xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
   id="Definitions_${processKey}"
   targetNamespace="${ns}">
   <process id="${processKey}" name="${processName}" isExecutable="true">
      <startEvent id="startEvent1" name="开始">
         <outgoing>flow1</outgoing>
      </startEvent>
      <userTask id="userTask1" name="审批节点">
         <extensionElements>
            <flowable:assignee>${'${}'}initiator</flowable:assignee>
         </extensionElements>
         <incoming>flow1</incoming>
         <outgoing>flow2</outgoing>
      </userTask>
      <endEvent id="endEvent1" name="结束">
         <incoming>flow2</incoming>
      </endEvent>
      <sequenceFlow id="flow1" sourceRef="startEvent1" targetRef="userTask1" />
      <sequenceFlow id="flow2" sourceRef="userTask1" targetRef="endEvent1" />
   </process>
   <bpmndi:BPMNDiagram id="BPMNDiagram_1">
      <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${processKey}">
         <bpmndi:BPMNShape id="_BPMNShape_startEvent1" bpmnElement="startEvent1">
            <dc:Bounds x="100" y="100" width="30" height="30" />
         </bpmndi:BPMNShape>
         <bpmndi:BPMNShape id="_BPMNShape_userTask1" bpmnElement="userTask1">
            <dc:Bounds x="180" y="80" width="100" height="60" />
         </bpmndi:BPMNShape>
         <bpmndi:BPMNShape id="_BPMNShape_endEvent1" bpmnElement="endEvent1">
            <dc:Bounds x="330" y="100" width="30" height="30" />
         </bpmndi:BPMNShape>
         <bpmndi:BPMNEdge id="_BPMNEdge_flow1" bpmnElement="flow1">
            <di:waypoint x="130" y="115" />
            <di:waypoint x="180" y="110" />
         </bpmndi:BPMNEdge>
         <bpmndi:BPMNEdge id="_BPMNEdge_flow2" bpmnElement="flow2">
            <di:waypoint x="280" y="110" />
            <di:waypoint x="330" y="115" />
         </bpmndi:BPMNEdge>
      </bpmndi:BPMNPlane>
   </bpmndi:BPMNDiagram>
</definitions>`
}

function handleAdd() {
   addForm.name = ''
   addForm.category = ''
   addForm.key = 'process_' + Date.now().toString(36) + Math.random().toString(36).substr(2, 5)
   addOpen.value = true
}

async function submitAdd() {
   proxy.$refs["addRef"].validate(async valid => {
      if (valid) {
         try {
            const bpmnXml = generateDefaultBpmn(addForm.name, addForm.key, addForm.category)
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
   let cat = row.category || ''
   if (cat.startsWith('http://') || cat.startsWith('https://')) {
      cat = ''
   }
   router.push({ path: '/flowable/process-designer/' + row.key, query: { name: row.name, category: cat } })
}

function handleStart(row) {
   startProcessKey.value = row.key
   startProcessName.value = row.name
   startOpen.value = true
}

function cancelStart() {
   startOpen.value = false
}

async function handleDiagram(row) {
   try {
      const res = await request({
         url: `/flowable/process/diagram/${row.id}`,
         method: 'get',
         responseType: 'blob'
      })
      const blob = new Blob([res], { type: 'image/png' })
      diagramXml.value = URL.createObjectURL(blob)
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

async function handleBindForm(row) {
   bindForm.id = ''
   bindForm.processKey = row.key
   bindForm.processName = row.name
   bindForm.formId = ''
   
   try {
      const res = await getProcessFormByKey(row.key)
      if (res.data) {
         bindForm.id = res.data.id
         bindForm.formId = res.data.formId
      }
   } catch (e) {
      // 未绑定表单
   }
   bindOpen.value = true
}

async function submitBind() {
   proxy.$refs["bindRef"].validate(async valid => {
      if (valid) {
         try {
            if (bindForm.id) {
               await updateProcessForm(bindForm)
            } else {
               await bindProcessForm(bindForm)
            }
            proxy.$modal.msgSuccess('绑定成功')
            bindOpen.value = false
         } catch (error) {
            proxy.$modal.msgError('绑定失败')
         }
      }
   })
}

function cancelBind() {
   bindOpen.value = false
}
</script>

<style scoped>
.diagram-container {
   text-align: center;
}
</style>