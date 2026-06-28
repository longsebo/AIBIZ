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
              <el-form-item v-if="selectedElement.type === 'bpmn:UserTask'" label="审批人类型">
                <el-select v-model="nodeConfig.assigneeType" placeholder="请选择类型" style="width: 150px" @change="onAssigneeTypeChange">
                  <el-option label="用户" value="user" />
                  <el-option label="部门" value="dept" />
                  <el-option label="角色" value="role" />
                  <el-option label="表单组件" value="formComponent" />
                </el-select>
              </el-form-item>
              <el-form-item v-if="selectedElement.type === 'bpmn:UserTask'" label="审批人">
                <el-input
                  :model-value="assigneeDisplayText"
                  placeholder="请选择审批人"
                  readonly
                  @click="openAssigneeDialog"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item v-if="selectedElement.type === 'bpmn:UserTask'" label="是否会签">
                <el-checkbox v-model="nodeConfig.countersign" />
              </el-form-item>
              <el-form-item v-if="selectedElement.type === 'bpmn:UserTask' && nodeConfig.countersign" label="通过率">
                <el-select v-model="nodeConfig.passRate" placeholder="请选择通过率" style="width: 150px">
                  <el-option label="全部通过" value="all" />
                  <el-option label="半数以上" value="half" />
                  <el-option label="三分之二" value="twoThirds" />
                  <el-option label="自定义比例" value="custom" />
                </el-select>
              </el-form-item>
              <el-form-item v-if="selectedElement.type === 'bpmn:UserTask' && nodeConfig.countersign && nodeConfig.passRate === 'custom'" label="自定义比例">
                <el-input-number v-model="nodeConfig.customPassRate" :min="1" :max="100" style="width: 120px" />
                <span style="margin-left: 8px;">%</span>
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

          <el-tab-pane label="权限" name="userPermission" v-if="selectedElement.type === 'bpmn:UserTask'">
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

          <el-tab-pane label="按钮" name="userButton" v-if="selectedElement.type === 'bpmn:UserTask'">
            <el-form label-width="80px" size="small" inline>
              <el-form-item label="按钮配置">
                <el-checkbox-group v-model="nodeConfig.userTaskButtons">
                  <el-checkbox label="reject">驳回</el-checkbox>
                  <el-checkbox label="transfer">转办</el-checkbox>
                  <el-checkbox label="addSign">加签</el-checkbox>
                  <el-checkbox label="suspend">挂起</el-checkbox>
                  <el-checkbox label="viewTrack">查看轨迹</el-checkbox>
                  <el-checkbox label="print">打印</el-checkbox>
                  <el-checkbox label="export">导出</el-checkbox>
                  <el-checkbox label="withdrawSign">撤签</el-checkbox>
                </el-checkbox-group>
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

    <el-dialog title="选择审批人" v-model="assigneeUserVisible" width="800px" top="5vh" append-to-body>
      <el-form :model="assigneeUserQuery" ref="assigneeUserQueryRef" :inline="true">
        <el-form-item label="用户名称" prop="userName">
          <el-input
            v-model="assigneeUserQuery.userName"
            placeholder="请输入用户名称"
            clearable
            style="width: 200px"
            @keyup.enter="searchAssigneeUsers"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="searchAssigneeUsers">搜索</el-button>
          <el-button icon="Refresh" @click="resetAssigneeUserQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table ref="assigneeUserTableRef" :data="assigneeUserList" @selection-change="handleAssigneeUserSelection" height="300px">
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
          <el-button type="primary" @click="confirmAssigneeUsers">确 定</el-button>
          <el-button @click="assigneeUserVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="选择部门" v-model="assigneeDeptVisible" width="600px" top="5vh" append-to-body>
      <el-form :model="assigneeDeptQuery" ref="assigneeDeptQueryRef" :inline="true">
        <el-form-item label="部门名称" prop="deptName">
          <el-input
            v-model="assigneeDeptQuery.deptName"
            placeholder="请输入部门名称"
            clearable
            style="width: 200px"
            @keyup.enter="searchAssigneeDepts"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="searchAssigneeDepts">搜索</el-button>
          <el-button icon="Refresh" @click="resetAssigneeDeptQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-tree
        ref="assigneeDeptTreeRef"
        :data="assigneeDeptTree"
        :props="{ label: 'deptName', children: 'children' }"
        :show-checkbox="true"
        node-key="deptId"
        :default-expand-all="true"
        @check-change="handleAssigneeDeptCheck"
        style="height: 300px; overflow-y: auto;"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="confirmAssigneeDepts">确 定</el-button>
          <el-button @click="assigneeDeptVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="选择角色" v-model="assigneeRoleVisible" width="600px" top="5vh" append-to-body>
      <el-form :model="assigneeRoleQuery" ref="assigneeRoleQueryRef" :inline="true">
        <el-form-item label="角色名称" prop="roleName">
          <el-input
            v-model="assigneeRoleQuery.roleName"
            placeholder="请输入角色名称"
            clearable
            style="width: 200px"
            @keyup.enter="searchAssigneeRoles"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="searchAssigneeRoles">搜索</el-button>
          <el-button icon="Refresh" @click="resetAssigneeRoleQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table ref="assigneeRoleTableRef" :data="assigneeRoleList" @selection-change="handleAssigneeRoleSelection" height="300px">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="角色名称" prop="roleName" :show-overflow-tooltip="true" />
        <el-table-column label="角色标识" prop="roleKey" :show-overflow-tooltip="true" />
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="confirmAssigneeRoles">确 定</el-button>
          <el-button @click="assigneeRoleVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="选择表单组件" v-model="assigneeFormComponentVisible" width="600px" top="5vh" append-to-body>
      <el-table ref="assigneeFormComponentTableRef" :data="formFields" @row-click="handleAssigneeFormComponentClick" height="300px">
        <el-table-column width="55">
          <template #default="scope">
            <el-radio :model-value="selectedAssigneeFormComponent?.key" :label="scope.row.key">&nbsp;</el-radio>
          </template>
        </el-table-column>
        <el-table-column label="组件名称" prop="label" :show-overflow-tooltip="true" />
        <el-table-column label="组件类型" prop="key" :show-overflow-tooltip="true" />
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="confirmAssigneeFormComponent">确 定</el-button>
          <el-button @click="assigneeFormComponentVisible = false">取 消</el-button>
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
import flowableModdle from 'flowable-bpmn-moddle/resources/camunda.json'
import { updateProcess, getProcessXmlByKey } from '@/api/flowable'
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
  userTaskButtons: [],
  ccType: '',
  ccUsers: [],
  ccDepts: [],
  ccRoles: [],
  ccFormComponents: [],
  assigneeType: '',
  assigneeUsers: [],
  assigneeDepts: [],
  assigneeRoles: [],
  assigneeFormComponent: null,
  countersign: false,
  passRate: 'all',
  customPassRate: 100
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

const assigneeUserVisible = ref(false)
const assigneeDeptVisible = ref(false)
const assigneeRoleVisible = ref(false)
const assigneeFormComponentVisible = ref(false)

const assigneeUserList = ref([])
const assigneeDeptTree = ref([])
const assigneeRoleList = ref([])

const selectedAssigneeUsers = ref([])
const selectedAssigneeDepts = ref([])
const selectedAssigneeRoles = ref([])
const selectedAssigneeFormComponent = ref(null)

const assigneeUserQuery = ref({
  pageNum: 1,
  pageSize: 10,
  userName: undefined
})

const assigneeDeptQuery = ref({
  deptName: undefined
})

const assigneeRoleQuery = ref({
  pageNum: 1,
  pageSize: 10,
  roleName: undefined
})

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

const assigneeDisplayText = computed(() => {
  const type = nodeConfig.value.assigneeType
  if (type === 'user') {
    return selectedAssigneeUsers.value.map(u => u.userName).join('，') || '请选择用户'
  } else if (type === 'dept') {
    return selectedAssigneeDepts.value.map(d => d.deptName).join('，') || '请选择部门'
  } else if (type === 'role') {
    return selectedAssigneeRoles.value.map(r => r.roleName).join('，') || '请选择角色'
  } else if (type === 'formComponent') {
    return selectedAssigneeFormComponent.value?.label || '请选择表单组件'
  }
  return '请选择审批人'
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
    container: canvasRef.value,
    moddleExtensions: {
      flowable: flowableModdle
    }
  })
  
  const xml = await loadProcessXml(processKey)
  await bpmnModeler.importXML(xml)
  
  const eventBus = bpmnModeler.get('eventBus')
  let previousElement = null  // 记录上一个选中的节点
  
  eventBus.on('selection.changed', function(event) {
    const elementRegistry = bpmnModeler.get('elementRegistry')
    const modeling = bpmnModeler.get('modeling')
    const selected = event.newSelection[0]
    
    // 如果之前有选中的节点，先保存当前配置
    if (previousElement && previousElement !== selected) {
      saveCurrentNodeConfig(previousElement, modeling)
    }
    
    if (selected) {
      let element = null
      if (typeof selected === 'string') {
        element = elementRegistry.get(selected)
      } else if (selected.id) {
        element = elementRegistry.get(selected.id)
      }
      if (element && element.businessObject) {
        previousElement = element  // 更新上一个节点
        const data = JSON.parse(JSON.stringify({
          ...element.businessObject,
          type: element.type
        }))
        selectedElement.value = null
        setTimeout(() => {
          selectedElement.value = data
          // 加载节点保存的配置
          loadNodeConfig(element, data)
        }, 0)
      } else {
        selectedElement.value = null
      }
    } else {
      selectedElement.value = null
      previousElement = null
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
    const res = await getProcessXmlByKey(processKey)
    if (res.data && res.data.xml) {
      return res.data.xml
    }
  } catch (e) {
    console.warn('未找到已保存的流程，使用空白模板')
  }
  return getEmptyBpmn(processKey)
}

function getEmptyBpmn(key) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  xmlns:flowable="http://flowable.org/bpmn"
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
    const modeling = bpmnModeler.get('modeling')
    const elementRegistry = bpmnModeler.get('elementRegistry')

    // 保存当前选中节点的配置（如果有）
    if (selectedElement.value) {
      const currentElement = elementRegistry.get(selectedElement.value.id)
      if (currentElement) {
        saveCurrentNodeConfig(currentElement, modeling)
      }
    }

    const { xml } = await bpmnModeler.saveXML({ format: true })
    
    // 手动将扩展属性注入到 XML 中
    let modifiedXml = xml
    
    // 遍历所有元素，将 $attrs 中的属性注入到 XML
    const allElements = elementRegistry.getAll()
    for (const element of allElements) {
      if (element.businessObject && element.businessObject.$attrs) {
        const bo = element.businessObject
        const attrs = bo.$attrs
        
        // 检查是否有需要注入的属性
        if (attrs['flowable:startEventConfig'] || attrs['flowable:userTaskConfig']) {
          const elementId = element.id
          const configStr = attrs['flowable:startEventConfig'] || attrs['flowable:userTaskConfig']
          const attrName = attrs['flowable:startEventConfig'] ? 'flowable:startEventConfig' : 'flowable:userTaskConfig'
          
          // 在 XML 中找到对应的元素标签并注入属性
          // 使用正则表达式匹配元素标签
          const regex = new RegExp(`<bpmn:(startEvent|userTask)\\s+id="${elementId}"([^>]*)>`, 'g')
          modifiedXml = modifiedXml.replace(regex, (match, type, attrs) => {
            // 检查是否已经有该属性
            if (attrs.includes(attrName)) {
              return match
            }
            // 添加属性
            return `<bpmn:${type} id="${elementId}"${attrs} ${attrName}="${configStr.replace(/"/g, '&quot;')}">`
          })
        }
      }
    }
    
    const processKey = route.params.key
    await updateProcess({
      key: processKey,
      name: processName.value,
      bpmnXml: modifiedXml
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

function saveCurrentNodeConfig(element, modeling) {
  if (!element || !element.businessObject) return
  if (!selectedElement.value || selectedElement.value.id !== element.id) return
  
  const bo = element.businessObject
  
  if (element.type === 'bpmn:StartEvent') {
    const config = {
      writeFields: nodeConfig.value.writeFields || [],
      readonlyFields: nodeConfig.value.readonlyFields || [],
      hideFields: nodeConfig.value.hideFields || [],
      buttons: nodeConfig.value.buttons || [],
      ccType: nodeConfig.value.ccType || '',
      ccUsers: selectedCcUsers.value.map(u => u.userId),
      ccDepts: selectedCcDepts.value.map(d => d.deptId),
      ccRoles: selectedCcRoles.value.map(r => r.roleId),
      ccFormComponent: selectedCcFormComponent.value ? selectedCcFormComponent.value.key : null
    }
    // 直接设置到 businessObject 上
    bo.$attrs['flowable:startEventConfig'] = JSON.stringify(config)
  }
  
  if (element.type === 'bpmn:UserTask') {
    const config = {
      writeFields: nodeConfig.value.writeFields || [],
      readonlyFields: nodeConfig.value.readonlyFields || [],
      hideFields: nodeConfig.value.hideFields || [],
      userTaskButtons: nodeConfig.value.userTaskButtons || [],
      assigneeType: nodeConfig.value.assigneeType || '',
      assigneeUsers: selectedAssigneeUsers.value.map(u => u.userId),
      assigneeDepts: selectedAssigneeDepts.value.map(d => d.deptId),
      assigneeRoles: selectedAssigneeRoles.value.map(r => r.roleId),
      assigneeFormComponent: selectedAssigneeFormComponent.value ? selectedAssigneeFormComponent.value.key : null,
      countersign: nodeConfig.value.countersign || false,
      passRate: nodeConfig.value.passRate || 'all',
      customPassRate: nodeConfig.value.customPassRate || 100
    }
    // 直接设置到 businessObject 上
    bo.$attrs['flowable:userTaskConfig'] = JSON.stringify(config)
  }
}

async function loadNodeConfig(element, data) {
  // 重置配置
  nodeConfig.value = {
    writeFields: [],
    readonlyFields: [],
    hideFields: [],
    buttons: [],
    userTaskButtons: [],
    ccType: '',
    ccUsers: [],
    ccDepts: [],
    ccRoles: [],
    ccFormComponents: [],
    assigneeType: '',
    assigneeUsers: [],
    assigneeDepts: [],
    assigneeRoles: [],
    assigneeFormComponent: null,
    countersign: false,
    passRate: 'all',
    customPassRate: 100
  }
  selectedCcUsers.value = []
  selectedCcDepts.value = []
  selectedCcRoles.value = []
  selectedCcFormComponent.value = null
  selectedAssigneeUsers.value = []
  selectedAssigneeDepts.value = []
  selectedAssigneeRoles.value = []
  selectedAssigneeFormComponent.value = null

  if (!element || !element.businessObject) return

  const bo = element.businessObject

  // 从 $attrs 加载开始节点配置
  if (element.type === 'bpmn:StartEvent') {
    const configStr = bo.$attrs['flowable:startEventConfig']
    if (configStr) {
      try {
        const config = JSON.parse(configStr)
        nodeConfig.value.writeFields = config.writeFields || []
        nodeConfig.value.readonlyFields = config.readonlyFields || []
        nodeConfig.value.hideFields = config.hideFields || []
        nodeConfig.value.buttons = config.buttons || []
        nodeConfig.value.ccType = config.ccType || ''
        nodeConfig.value.ccFormComponents = config.ccFormComponent ? [config.ccFormComponent] : []

        // 加载抄送人
        if (config.ccType === 'user' && config.ccUsers && config.ccUsers.length > 0) {
          await loadUsersByIds(config.ccUsers, selectedCcUsers)
        } else if (config.ccType === 'dept' && config.ccDepts && config.ccDepts.length > 0) {
          await loadDeptsByIds(config.ccDepts, selectedCcDepts)
        } else if (config.ccType === 'role' && config.ccRoles && config.ccRoles.length > 0) {
          await loadRolesByIds(config.ccRoles, selectedCcRoles)
        } else if (config.ccType === 'formComponent' && config.ccFormComponent) {
          const field = formFields.value.find(f => f.key === config.ccFormComponent)
          if (field) {
            selectedCcFormComponent.value = field
          }
        }
      } catch (e) {
        console.error('加载开始节点配置失败', e)
      }
    }
    return
  }

  // 从 $attrs 加载用户任务节点配置
  if (element.type === 'bpmn:UserTask') {
    const configStr = bo.$attrs['flowable:userTaskConfig']
    if (configStr) {
      try {
        const config = JSON.parse(configStr)
        nodeConfig.value.writeFields = config.writeFields || []
        nodeConfig.value.readonlyFields = config.readonlyFields || []
        nodeConfig.value.hideFields = config.hideFields || []
        nodeConfig.value.userTaskButtons = config.userTaskButtons || []
        nodeConfig.value.assigneeType = config.assigneeType || ''
        nodeConfig.value.countersign = config.countersign || false
        nodeConfig.value.passRate = config.passRate || 'all'
        nodeConfig.value.customPassRate = config.customPassRate || 100

        // 加载审批人
        if (config.assigneeType === 'user' && config.assigneeUsers && config.assigneeUsers.length > 0) {
          await loadUsersByIds(config.assigneeUsers, selectedAssigneeUsers)
        } else if (config.assigneeType === 'dept' && config.assigneeDepts && config.assigneeDepts.length > 0) {
          await loadDeptsByIds(config.assigneeDepts, selectedAssigneeDepts)
        } else if (config.assigneeType === 'role' && config.assigneeRoles && config.assigneeRoles.length > 0) {
          await loadRolesByIds(config.assigneeRoles, selectedAssigneeRoles)
        } else if (config.assigneeType === 'formComponent' && config.assigneeFormComponent) {
          const field = formFields.value.find(f => f.key === config.assigneeFormComponent)
          if (field) {
            selectedAssigneeFormComponent.value = field
          }
        }
      } catch (e) {
        console.error('加载用户任务节点配置失败', e)
      }
    }
  }
}

async function loadUsersByIds(userIds, targetRef) {
  if (!userIds || userIds.length === 0) return
  try {
    const res = await listUser({ pageNum: 1, pageSize: 100 })
    const users = res.rows.filter(u => userIds.includes(u.userId))
    targetRef.value = users
  } catch (e) {
    console.error('加载用户失败', e)
  }
}

async function loadDeptsByIds(deptIds, targetRef) {
  if (!deptIds || deptIds.length === 0) return
  try {
    const res = await listDept({})
    const depts = res.data.filter(d => deptIds.includes(d.deptId))
    targetRef.value = depts
  } catch (e) {
    console.error('加载部门失败', e)
  }
}

async function loadRolesByIds(roleIds, targetRef) {
  if (!roleIds || roleIds.length === 0) return
  try {
    const res = await listRole({ pageNum: 1, pageSize: 100 })
    const roles = res.rows.filter(r => roleIds.includes(r.roleId))
    targetRef.value = roles
  } catch (e) {
    console.error('加载角色失败', e)
  }
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

function onAssigneeTypeChange() {
  selectedAssigneeUsers.value = []
  selectedAssigneeDepts.value = []
  selectedAssigneeRoles.value = []
  selectedAssigneeFormComponent.value = null
}

function openAssigneeDialog() {
  const type = nodeConfig.value.assigneeType
  if (type === 'user') {
    assigneeUserVisible.value = true
    searchAssigneeUsers()
  } else if (type === 'dept') {
    assigneeDeptVisible.value = true
    searchAssigneeDepts()
  } else if (type === 'role') {
    assigneeRoleVisible.value = true
    searchAssigneeRoles()
  } else if (type === 'formComponent') {
    assigneeFormComponentVisible.value = true
  } else {
    proxy.$modal.msgWarning('请先选择审批人类型')
  }
}

function searchAssigneeUsers() {
  listUser(assigneeUserQuery.value).then(res => {
    assigneeUserList.value = res.rows
  })
}

function resetAssigneeUserQuery() {
  assigneeUserQuery.value = {
    pageNum: 1,
    pageSize: 10,
    userName: undefined
  }
  searchAssigneeUsers()
}

function handleAssigneeUserSelection(selection) {
  selectedAssigneeUsers.value = selection
}

function confirmAssigneeUsers() {
  assigneeUserVisible.value = false
}

function searchAssigneeDepts() {
  listDept(assigneeDeptQuery.value).then(res => {
    assigneeDeptTree.value = res.data
  })
}

function resetAssigneeDeptQuery() {
  assigneeDeptQuery.value = {
    deptName: undefined
  }
  searchAssigneeDepts()
}

function handleAssigneeDeptCheck() {
  const treeRef = proxy?.$refs?.assigneeDeptTreeRef
  if (!treeRef) return
  const checkedNodes = treeRef.getCheckedNodes()
  selectedAssigneeDepts.value = checkedNodes
}

function confirmAssigneeDepts() {
  assigneeDeptVisible.value = false
}

function searchAssigneeRoles() {
  listRole(assigneeRoleQuery.value).then(res => {
    assigneeRoleList.value = res.rows
  })
}

function resetAssigneeRoleQuery() {
  assigneeRoleQuery.value = {
    pageNum: 1,
    pageSize: 10,
    roleName: undefined
  }
  searchAssigneeRoles()
}

function handleAssigneeRoleSelection(selection) {
  selectedAssigneeRoles.value = selection
}

function confirmAssigneeRoles() {
  assigneeRoleVisible.value = false
}

function handleAssigneeFormComponentClick(row) {
  selectedAssigneeFormComponent.value = row
}

function confirmAssigneeFormComponent() {
  assigneeFormComponentVisible.value = false
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

.canvas-container :deep(.djs-palette) {
  overflow-y: auto;
  overflow-x: hidden;
  max-height: calc(100% - 20px);
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