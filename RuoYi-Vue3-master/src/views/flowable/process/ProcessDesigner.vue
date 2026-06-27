<template>
  <div class="process-designer">
    <div class="designer-header">
      <div class="header-left">
        <el-button icon="ArrowLeft" @click="goBack">返回</el-button>
        <span class="process-name">{{ processName }}</span>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Check" @click="saveProcess">保存</el-button>
      </div>
    </div>
    
    <div class="designer-body">
      <div class="canvas-container" ref="canvasRef"></div>
    </div>
    
    <div class="properties-panel">
      <div v-if="selectedElement" class="property-content">
        <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="基本属性" name="basic">
            <el-form label-width="80px" size="small" inline>
              <el-form-item label="节点ID">
                <el-input v-model="selectedElement.id" disabled style="width: 180px" />
              </el-form-item>
              <el-form-item label="节点名称">
                <el-input v-model="selectedElement.name" placeholder="请输入节点名称" style="width: 200px" @change="updateElementName" />
              </el-form-item>
              <el-form-item v-if="selectedElement.type === 'bpmn:UserTask'" label="审批人">
                <el-input v-model="selectedElement.assignee" placeholder="如: ${initiator}" style="width: 200px" @change="updateElementProperty('assignee', selectedElement.assignee)" />
              </el-form-item>
              <el-form-item v-if="selectedElement.type === 'bpmn:UserTask'" label="候选用户">
                <el-input v-model="selectedElement.candidateUsers" placeholder="多个用逗号分隔" style="width: 200px" @change="updateElementProperty('candidateUsers', selectedElement.candidateUsers)" />
              </el-form-item>
              <el-form-item v-if="selectedElement.type === 'bpmn:UserTask'" label="候选角色">
                <el-input v-model="selectedElement.candidateGroups" placeholder="多个用逗号分隔" style="width: 200px" @change="updateElementProperty('candidateGroups', selectedElement.candidateGroups)" />
              </el-form-item>
              <el-form-item label="描述">
                <el-input v-model="selectedElement.documentation" type="textarea" :rows="2" placeholder="请输入描述" style="width: 300px" @change="updateDocumentation" />
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="权限" name="permission" v-if="selectedElement.type === 'bpmn:StartEvent'">
            <el-form label-width="80px" size="small" inline>
              <el-form-item label="可写字段">
                <el-select v-model="nodeConfig.writeFields" multiple placeholder="请选择表单字段" style="width: 200px">
                  <el-option v-for="field in formFields" :key="field.key" :label="field.label" :value="field.key" />
                </el-select>
              </el-form-item>
              <el-form-item label="只读字段">
                <el-select v-model="nodeConfig.readonlyFields" multiple placeholder="请选择表单字段" style="width: 200px">
                  <el-option v-for="field in formFields" :key="field.key" :label="field.label" :value="field.key" />
                </el-select>
              </el-form-item>
              <el-form-item label="隐藏字段">
                <el-select v-model="nodeConfig.hideFields" multiple placeholder="请选择表单字段" style="width: 200px">
                  <el-option v-for="field in formFields" :key="field.key" :label="field.label" :value="field.key" />
                </el-select>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="按钮" name="button" v-if="selectedElement.type === 'bpmn:StartEvent'">
            <el-form label-width="80px" size="small" inline>
              <el-form-item label="按钮配置">
                <el-checkbox-group v-model="nodeConfig.buttons">
                  <el-checkbox label="saveDraft">保存草稿</el-checkbox>
                  <el-checkbox label="clear">清空</el-checkbox>
                  <el-checkbox label="cc">抄送</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="抄送人" name="cc" v-if="selectedElement.type === 'bpmn:StartEvent'">
            <el-form label-width="80px" size="small" inline>
              <el-form-item label="抄送人类型">
                <el-select v-model="nodeConfig.ccType" placeholder="请选择类型" style="width: 150px" @change="onCcTypeChange">
                  <el-option label="用户" value="user" />
                  <el-option label="部门" value="dept" />
                  <el-option label="角色" value="role" />
                  <el-option label="表单组件" value="formComponent" />
                </el-select>
              </el-form-item>
              <el-form-item label="抄送人">
                <el-input
                  :model-value="ccDisplayText"
                  placeholder="请选择抄送人"
                  readonly
                  @click="openCcDialog"
                  style="width: 200px"
                />
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
      <div v-else class="no-selection">
        <p>请选择一个元素查看属性</p>
      </div>
    </div>

    <el-dialog title="选择用户" v-model="ccUserVisible" width="800px" top="5vh" append-to-body>
      <el-form :model="ccUserQuery" ref="ccUserQueryRef" :inline="true">
        <el-form-item label="用户名称" prop="userName">
          <el-input
            v-model="ccUserQuery.userName"
            placeholder="请输入用户名称"
            clearable
            style="width: 200px"
            @keyup.enter="searchCcUsers"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="searchCcUsers">搜索</el-button>
          <el-button icon="Refresh" @click="resetCcUserQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table ref="ccUserTableRef" :data="ccUserList" @selection-change="handleCcUserSelection" height="300px">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="用户名称" prop="userName" :show-overflow-tooltip="true" />
        <el-table-column label="用户昵称" prop="nickName" :show-overflow-tooltip="true" />
        <el-table-column label="状态" align="center" prop="status" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
            <el-tag v-else type="danger">停用</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="confirmCcUsers">确 定</el-button>
          <el-button @click="ccUserVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="选择部门" v-model="ccDeptVisible" width="600px" top="5vh" append-to-body>
      <el-form :model="ccDeptQuery" ref="ccDeptQueryRef" :inline="true">
        <el-form-item label="部门名称" prop="deptName">
          <el-input
            v-model="ccDeptQuery.deptName"
            placeholder="请输入部门名称"
            clearable
            style="width: 200px"
            @keyup.enter="searchCcDepts"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="searchCcDepts">搜索</el-button>
          <el-button icon="Refresh" @click="resetCcDeptQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-tree
        ref="ccDeptTreeRef"
        :data="ccDeptTree"
        :props="{ label: 'deptName', children: 'children' }"
        :show-checkbox="true"
        node-key="deptId"
        :default-expand-all="true"
        @check-change="handleCcDeptCheck"
        style="height: 300px; overflow-y: auto;"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="confirmCcDepts">确 定</el-button>
          <el-button @click="ccDeptVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="选择角色" v-model="ccRoleVisible" width="600px" top="5vh" append-to-body>
      <el-form :model="ccRoleQuery" ref="ccRoleQueryRef" :inline="true">
        <el-form-item label="角色名称" prop="roleName">
          <el-input
            v-model="ccRoleQuery.roleName"
            placeholder="请输入角色名称"
            clearable
            style="width: 200px"
            @keyup.enter="searchCcRoles"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="searchCcRoles">搜索</el-button>
          <el-button icon="Refresh" @click="resetCcRoleQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table ref="ccRoleTableRef" :data="ccRoleList" @selection-change="handleCcRoleSelection" height="300px">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="角色名称" prop="roleName" :show-overflow-tooltip="true" />
        <el-table-column label="角色标识" prop="roleKey" :show-overflow-tooltip="true" />
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="confirmCcRoles">确 定</el-button>
          <el-button @click="ccRoleVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="选择表单组件" v-model="ccFormComponentVisible" width="600px" top="5vh" append-to-body>
      <el-table ref="ccFormComponentTableRef" :data="formFields" @row-click="handleCcFormComponentClick" height="300px">
        <el-table-column width="55">
          <template #default="scope">
            <el-radio :model-value="selectedCcFormComponent?.key" :label="scope.row.key">&nbsp;</el-radio>
          </template>
        </el-table-column>
        <el-table-column label="组件名称" prop="label" :show-overflow-tooltip="true" />
        <el-table-column label="组件类型" prop="key" :show-overflow-tooltip="true" />
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="confirmCcFormComponent">确 定</el-button>
          <el-button @click="ccFormComponentVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, getCurrentInstance, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'
import { updateProcess } from '@/api/flowable'
import { listUser } from '@/api/system/user'
import { listDept } from '@/api/system/dept'
import { listRole } from '@/api/system/role'
import { getProcessFormByKey } from '@/api/flowable/processForm'
import { getForm } from '@/api/flowable/form'

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const canvasRef = ref(null)
const processName = ref('')
const selectedElement = ref(null)
const activeTab = ref('basic')

const formFields = ref([])

const nodeConfig = ref({
  writeFields: [],
  readonlyFields: [],
  hideFields: [],
  buttons: [],
  ccType: '',
  ccUsers: [],
  ccDepts: [],
  ccRoles: [],
  ccFormComponents: []
})

const ccUserVisible = ref(false)
const ccDeptVisible = ref(false)
const ccRoleVisible = ref(false)
const ccFormComponentVisible = ref(false)

const ccUserList = ref([])
const ccDeptTree = ref([])
const ccRoleList = ref([])

const selectedCcUsers = ref([])
const selectedCcDepts = ref([])
const selectedCcRoles = ref([])
const selectedCcFormComponent = ref(null)

const ccUserQuery = ref({
  pageNum: 1,
  pageSize: 10,
  userName: undefined
})

const ccDeptQuery = ref({
  deptName: undefined
})

const ccRoleQuery = ref({
  pageNum: 1,
  pageSize: 10,
  roleName: undefined
})

const ccDisplayText = computed(() => {
  const type = nodeConfig.value.ccType
  if (type === 'user') {
    return selectedCcUsers.value.map(u => u.userName).join('，') || '请选择用户'
  } else if (type === 'dept') {
    return selectedCcDepts.value.map(d => d.deptName).join('，') || '请选择部门'
  } else if (type === 'role') {
    return selectedCcRoles.value.map(r => r.roleName).join('，') || '请选择角色'
  } else if (type === 'formComponent') {
    return selectedCcFormComponent.value?.label || '请选择表单组件'
  }
  return '请选择抄送人'
})

let bpmnModeler = null

onMounted(() => {
  initBpmn()
  loadFormFields()
})

onUnmounted(() => {
  if (bpmnModeler) {
    bpmnModeler.destroy()
  }
})

async function initBpmn() {
  const processKey = route.params.key
  processName.value = route.query.name || processKey
  
  bpmnModeler = new BpmnModeler({
    container: canvasRef.value
  })
  
  const xml = await loadProcessXml(processKey)
  await bpmnModeler.importXML(xml)
  
  const eventBus = bpmnModeler.get('eventBus')
  
  eventBus.on('selection.changed', function(event) {
    const elementRegistry = bpmnModeler.get('elementRegistry')
    const selected = event.newSelection[0]
    if (selected) {
      let element = null
      if (typeof selected === 'string') {
        element = elementRegistry.get(selected)
      } else if (selected.id) {
        element = elementRegistry.get(selected.id)
      }
      if (element && element.businessObject) {
        const data = JSON.parse(JSON.stringify({
          ...element.businessObject,
          type: element.type
        }))
        selectedElement.value = null
        setTimeout(() => {
          selectedElement.value = data
        }, 0)
      } else {
        selectedElement.value = null
      }
    } else {
      selectedElement.value = null
    }
  })
  
  eventBus.on('element.changed', (event) => {
    if (selectedElement.value && event.element && event.element.id === selectedElement.value.id) {
      const elementRegistry = bpmnModeler.get('elementRegistry')
      const element = elementRegistry.get(event.element.id)
      if (element && element.businessObject) {
        selectedElement.value = JSON.parse(JSON.stringify({
          ...element.businessObject,
          type: element.type
        }))
      }
    }
  })
}

async function loadProcessXml(processKey) {
  try {
    return getEmptyBpmn(processKey)
  } catch (e) {
    return getEmptyBpmn(processKey)
  }
}

function getEmptyBpmn(key) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  id="Definitions_${key}"
  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="${key}" name="${processName.value}" isExecutable="true">
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${key}">
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`
}

function handleSelectionChanged(element) {
  if (element && element.businessObject) {
    selectedElement.value = JSON.parse(JSON.stringify({
      ...element.businessObject,
      type: element.type
    }))
  } else {
    selectedElement.value = null
  }
}

function updateElementName() {
  if (!selectedElement.value || !selectedElement.value.id) return
  
  const modeling = bpmnModeler.get('modeling')
  const elementRegistry = bpmnModeler.get('elementRegistry')
  const element = elementRegistry.get(selectedElement.value.id)
  
  if (element) {
    modeling.updateProperties(element, {
      name: selectedElement.value.name
    })
  }
}

function updateElementProperty(prop, value) {
  if (!selectedElement.value || !selectedElement.value.id) return
  
  const modeling = bpmnModeler.get('modeling')
  const elementRegistry = bpmnModeler.get('elementRegistry')
  const element = elementRegistry.get(selectedElement.value.id)
  
  if (element) {
    modeling.updateProperties(element, {
      [prop]: value
    })
  }
}

function updateDocumentation() {
  if (!selectedElement.value || !selectedElement.value.id) return
  
  const modeling = bpmnModeler.get('modeling')
  const elementRegistry = bpmnModeler.get('elementRegistry')
  const element = elementRegistry.get(selectedElement.value.id)
  
  if (element) {
    modeling.updateProperties(element, {
      documentation: [{ text: selectedElement.value.documentation }]
    })
  }
}

async function saveProcess() {
  try {
    const { xml } = await bpmnModeler.saveXML({ format: true })
    const processKey = route.params.key
    await updateProcess({
      key: processKey,
      name: processName.value,
      bpmnXml: xml
    })
    proxy.$modal.msgSuccess('保存成功')
  } catch (e) {
    console.error('保存失败:', e)
    proxy.$modal.msgError('保存失败')
  }
}

function goBack() {
  router.back()
}

async function loadFormFields() {
  const processKey = route.params.key
  try {
    const formRes = await getProcessFormByKey(processKey)
    if (formRes.data && formRes.data.formId) {
      const formDataRes = await getForm(formRes.data.formId)
      if (formDataRes.data && formDataRes.data.formJson) {
        const components = JSON.parse(formDataRes.data.formJson)
        const fields = []

        components.forEach(comp => {
          if (comp.type !== 'title' && comp.type !== 'table') {
            fields.push({
              key: comp.id.toString(),
              label: comp.label || comp.type
            })
          }

          if (comp.type === 'dataTable' && comp.columns) {
            comp.columns.forEach(col => {
              if (col.type !== 'title') {
                fields.push({
                  key: `${comp.id}_${col.prop}`,
                  label: `${comp.label || '表格'}-${col.label || col.prop}`
                })
              }
            })
          }

          if (comp.type === 'table' && comp.children && comp.children.length > 0) {
            comp.children.forEach(child => {
              if (child.type !== 'title') {
                fields.push({
                  key: child.id.toString(),
                  label: `${comp.label || '表格布局'}-${child.label || child.type}`
                })
              }
            })
          }
        })

        formFields.value = fields
      }
    }
  } catch (e) {
    console.error('加载表单字段失败:', e)
  }
}

function onCcTypeChange() {
  selectedCcUsers.value = []
  selectedCcDepts.value = []
  selectedCcRoles.value = []
  selectedCcFormComponent.value = null
}

function openCcDialog() {
  const type = nodeConfig.value.ccType
  if (type === 'user') {
    ccUserVisible.value = true
    searchCcUsers()
  } else if (type === 'dept') {
    ccDeptVisible.value = true
    searchCcDepts()
  } else if (type === 'role') {
    ccRoleVisible.value = true
    searchCcRoles()
  } else if (type === 'formComponent') {
    ccFormComponentVisible.value = true
  } else {
    proxy.$modal.msgWarning('请先选择抄送人类型')
  }
}

function searchCcUsers() {
  listUser(ccUserQuery.value).then(res => {
    ccUserList.value = res.rows
  })
}

function resetCcUserQuery() {
  ccUserQuery.value = {
    pageNum: 1,
    pageSize: 10,
    userName: undefined
  }
  searchCcUsers()
}

function handleCcUserSelection(selection) {
  selectedCcUsers.value = selection
}

function confirmCcUsers() {
  ccUserVisible.value = false
}

function searchCcDepts() {
  listDept(ccDeptQuery.value).then(res => {
    ccDeptTree.value = res.data
  })
}

function resetCcDeptQuery() {
  ccDeptQuery.value = {
    deptName: undefined
  }
  searchCcDepts()
}

function handleCcDeptCheck() {
  const treeRef = proxy?.$refs?.ccDeptTreeRef
  if (!treeRef) return
  const checkedNodes = treeRef.getCheckedNodes()
  selectedCcDepts.value = checkedNodes
}

function confirmCcDepts() {
  ccDeptVisible.value = false
}

function searchCcRoles() {
  listRole(ccRoleQuery.value).then(res => {
    ccRoleList.value = res.rows
  })
}

function resetCcRoleQuery() {
  ccRoleQuery.value = {
    pageNum: 1,
    pageSize: 10,
    roleName: undefined
  }
  searchCcRoles()
}

function handleCcRoleSelection(selection) {
  selectedCcRoles.value = selection
}

function confirmCcRoles() {
  ccRoleVisible.value = false
}

function handleCcFormComponentClick(row) {
  selectedCcFormComponent.value = row
}

function confirmCcFormComponent() {
  ccFormComponentVisible.value = false
}
</script>

<style scoped>
.process-designer {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.designer-header {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.process-name {
  font-size: 16px;
  font-weight: 500;
}

.designer-body {
  flex: 1;
  overflow: hidden;
}

.canvas-container {
  width: 100%;
  height: 100%;
}

.canvas-container :deep(.bjs-container) {
  width: 100%;
  height: 100%;
}

.properties-panel {
  height: 280px;
  border-top: 1px solid #e4e7ed;
  background: #fafafa;
  overflow-y: auto;
}

.property-content {
  height: 100%;
  padding: 0;
}

.property-content :deep(.el-tabs) {
  height: 100%;
}

.property-content :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 16px;
  background: #fff;
}

.property-content :deep(.el-tabs__content) {
  padding: 10px 16px;
  overflow-y: auto;
}

.no-selection {
  padding: 20px 16px;
  text-align: center;
  color: #909399;
}

.no-selection p {
  margin: 0;
}
</style>