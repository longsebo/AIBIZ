<template>
  <div class="form-designer">
    <div class="designer-header">
      <h2>表单设计器</h2>
      <div class="header-actions">
        <el-button type="primary" @click="exportJson">导出JSON</el-button>
        <el-button @click="clearForm">清空</el-button>
      </div>
    </div>
    
    <div class="designer-body">
      <div class="component-panel">
        <h3>组件库</h3>
        <div class="component-list">
          <div 
            v-for="comp in componentLibrary"
            :key="comp.type"
            class="component-item"
            draggable="true"
            @dragstart="(e) => handleDragStart(e, comp)"
          >
            <el-icon><component :is="getIcon(comp.icon)" /></el-icon>
            <span>{{ comp.label }}</span>
          </div>
        </div>
      </div>
      
      <div class="canvas-container">
        <h3>设计区</h3>
        <div class="canvas-wrapper">
          <FormCanvas
            :form-data="formData"
            :selected-component="selectedComponent"
            @select="selectComponent"
            @update="updateComponent"
            @delete="deleteComponent"
            @add-component="addComponent"
            @add-child="addTableChild"
            @update-child="updateTableChild"
            @delete-child="deleteTableChild"
          />
        </div>
      </div>
    </div>
    
    <div class="property-panel">
      <h3>属性</h3>
      <PropertyPanel
        v-if="selectedComponent"
        :component="selectedComponent"
        @update="(updates) => updateComponent(selectedComponent.id, updates)"
        @delete="() => deleteComponent(selectedComponent.id)"
      />
      <div v-else class="empty-property">
        <p>请选择一个组件</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import FormCanvas from './FormCanvas.vue'
import PropertyPanel from './PropertyPanel.vue'
import {
  EditPen,
  Edit,
  CircleCheck,
  Check,
  ArrowDown,
  Document,
  Calendar,
  User,
  UserFilled,
  OfficeBuilding,
  Grid
} from '@element-plus/icons-vue'

const componentLibrary = ref([
  { type: 'input', label: '单行输入框', icon: 'input' },
  { type: 'textarea', label: '多行输入框', icon: 'edit' },
  { type: 'radio', label: '单选框', icon: 'circle-check' },
  { type: 'checkbox', label: '复选框', icon: 'check' },
  { type: 'select', label: '组合框', icon: 'arrow-down' },
  { type: 'title', label: '标题', icon: 'font' },
  { type: 'date', label: '日期', icon: 'calendar' },
  { type: 'user', label: '单选用户', icon: 'user' },
  { type: 'users', label: '多选用户', icon: 'users' },
  { type: 'roleUser', label: '按角色选用户', icon: 'role-user' },
  { type: 'roleUsers', label: '按角色多选用户', icon: 'role-users' },
  { type: 'dept', label: '单选部门', icon: 'building' },
  { type: 'depts', label: '多选部门', icon: 'buildings' },
  { type: 'table', label: '表格布局', icon: 'grid' }
])

const iconMap = {
  input: EditPen,
  edit: Edit,
  'circle-check': CircleCheck,
  check: Check,
  'arrow-down': ArrowDown,
  font: Document,
  calendar: Calendar,
  user: User,
  users: UserFilled,
  'role-user': UserFilled,
  'role-users': UserFilled,
  building: OfficeBuilding,
  buildings: OfficeBuilding,
  grid: Grid
}

function getIcon(name) {
  return iconMap[name] || EditPen
}

const formData = ref([
  { id: 1, type: 'title', label: '表单标题', fontStyle: 'bold', fontFamily: '', fontSize: 18, align: 'left', required: false, placeholder: '', defaultValue: '' },
  { id: 2, type: 'input', label: '姓名', field: 'name', required: true, placeholder: '请输入姓名', defaultValue: '' },
  { id: 3, type: 'input', label: '手机号', field: 'phone', required: true, placeholder: '请输入手机号', defaultValue: '' }
])

const selectedComponent = ref(null)
const nextId = ref(4)

function handleDragStart(e, comp) {
  e.dataTransfer.setData('componentType', comp.type)
  e.dataTransfer.effectAllowed = 'copy'
}

function addComponent(componentType) {
  const configs = {
    input: { type: 'input', label: '单行输入框', field: `field_${nextId.value}`, required: false, placeholder: '请输入', defaultValue: '' },
    textarea: { type: 'textarea', label: '多行输入框', field: `field_${nextId.value}`, required: false, placeholder: '请输入内容', defaultValue: '' },
    radio: { type: 'radio', label: '单选框', field: `field_${nextId.value}`, required: false, options: ['选项1', '选项2', '选项3'], defaultValue: '' },
    checkbox: { type: 'checkbox', label: '复选框', field: `field_${nextId.value}`, required: false, options: ['选项1', '选项2', '选项3'], defaultValue: [] },
    select: { type: 'select', label: '组合框', field: `field_${nextId.value}`, required: false, options: ['选项1', '选项2', '选项3'], defaultValue: '' },
    title: { type: 'title', label: '标题', fontStyle: 'bold', fontFamily: '', fontSize: 18, align: 'left', required: false, placeholder: '', defaultValue: '' },
    date: { type: 'date', label: '日期', field: `field_${nextId.value}`, required: false, placeholder: '请选择日期', defaultValue: '' },
    user: { type: 'user', label: '单选用户', field: `field_${nextId.value}`, required: false, placeholder: '请选择用户', defaultValue: '' },
    users: { type: 'users', label: '多选用户', field: `field_${nextId.value}`, required: false, placeholder: '请选择用户', defaultValue: [] },
    roleUser: { type: 'roleUser', label: '按角色选用户', field: `field_${nextId.value}`, required: false, placeholder: '请按角色选择用户', defaultValue: '' },
    roleUsers: { type: 'roleUsers', label: '按角色多选用户', field: `field_${nextId.value}`, required: false, placeholder: '请按角色选择用户', defaultValue: [] },
    dept: { type: 'dept', label: '单选部门', field: `field_${nextId.value}`, required: false, placeholder: '请选择部门', defaultValue: '' },
    depts: { type: 'depts', label: '多选部门', field: `field_${nextId.value}`, required: false, placeholder: '请选择部门', defaultValue: [] },
    table: { type: 'table', label: '表格布局', field: `table_${nextId.value}`, rows: 2, cols: 2, children: [], required: false }
  }
  const newComp = { id: nextId.value++, ...configs[componentType] }
  formData.value.push(newComp)
}

function selectComponent(comp) {
  selectedComponent.value = comp
}

function updateComponent(id, updates) {
  let found = false
  const idx = formData.value.findIndex(c => c.id === id)
  if (idx !== -1) {
    formData.value[idx] = { ...formData.value[idx], ...updates }
    found = true
  }
  
  if (!found) {
    for (const table of formData.value) {
      if (table.type === 'table' && table.children) {
        const childIdx = table.children.findIndex(ch => ch.id === id)
        if (childIdx !== -1) {
          table.children[childIdx] = { ...table.children[childIdx], ...updates }
          found = true
          break
        }
      }
    }
  }
  
  if (found && selectedComponent.value?.id === id) {
    selectedComponent.value = { ...selectedComponent.value, ...updates }
  }
}

function deleteComponent(id) {
  let found = false
  const idx = formData.value.findIndex(c => c.id === id)
  if (idx !== -1) {
    formData.value = formData.value.filter(c => c.id !== id)
    found = true
  }
  
  if (!found) {
    for (const table of formData.value) {
      if (table.type === 'table' && table.children) {
        const childIdx = table.children.findIndex(ch => ch.id === id)
        if (childIdx !== -1) {
          table.children.splice(childIdx, 1)
          found = true
          break
        }
      }
    }
  }
  
  if (found && selectedComponent.value?.id === id) {
    selectedComponent.value = null
  }
}

function addTableChild(tableId, componentType, rowIdx, colIdx) {
  const table = formData.value.find(c => c.id === tableId)
  if (table && table.type === 'table') {
    if (!table.children) {
      table.children = []
    }
    
    const configs = {
      input: { type: 'input', label: '单行输入框', field: `cell_${nextId.value}`, required: false, placeholder: '请输入', defaultValue: '' },
      textarea: { type: 'textarea', label: '多行输入框', field: `cell_${nextId.value}`, required: false, placeholder: '请输入内容', defaultValue: '' },
      radio: { type: 'radio', label: '单选框', field: `cell_${nextId.value}`, required: false, options: ['选项1', '选项2'], defaultValue: '' },
      checkbox: { type: 'checkbox', label: '复选框', field: `cell_${nextId.value}`, required: false, options: ['选项1', '选项2'], defaultValue: [] },
      select: { type: 'select', label: '组合框', field: `cell_${nextId.value}`, required: false, options: ['选项1', '选项2'], defaultValue: '' },
      title: { type: 'title', label: '标题', fontStyle: 'bold', fontFamily: '', fontSize: 18, align: 'left', required: false, placeholder: '', defaultValue: '' },
      date: { type: 'date', label: '日期', field: `cell_${nextId.value}`, required: false, placeholder: '请选择日期', defaultValue: '' },
      user: { type: 'user', label: '单选用户', field: `cell_${nextId.value}`, required: false, placeholder: '请选择用户', defaultValue: '' },
      users: { type: 'users', label: '多选用户', field: `cell_${nextId.value}`, required: false, placeholder: '请选择用户', defaultValue: [] },
      dept: { type: 'dept', label: '单选部门', field: `cell_${nextId.value}`, required: false, placeholder: '请选择部门', defaultValue: '' },
      depts: { type: 'depts', label: '多选部门', field: `cell_${nextId.value}`, required: false, placeholder: '请选择部门', defaultValue: [] }
    }
    
    if (rowIdx && colIdx) {
      const existingIdx = table.children.findIndex(ch => ch.rowIdx === rowIdx && ch.colIdx === colIdx)
      const newChild = { id: nextId.value++, ...configs[componentType], rowIdx, colIdx }
      if (existingIdx !== -1) {
        table.children.splice(existingIdx, 1, newChild)
      } else {
        table.children.push(newChild)
      }
    } else {
      const newChild = { id: nextId.value++, ...configs[componentType] }
      table.children.push(newChild)
    }
  }
}

function updateTableChild(tableId, childId, updates) {
  const table = formData.value.find(c => c.id === tableId)
  if (table && table.children) {
    const idx = table.children.findIndex(ch => ch.id === childId)
    if (idx !== -1) {
      table.children[idx] = { ...table.children[idx], ...updates }
    }
  }
}

function deleteTableChild(tableId, childId) {
  const table = formData.value.find(c => c.id === tableId)
  if (table && table.children) {
    table.children = table.children.filter(ch => ch.id !== childId)
    if (selectedComponent.value?.id === childId) {
      selectedComponent.value = null
    }
  }
}

function exportJson() {
  const json = JSON.stringify(formData.value, null, 2)
  const blob = new Blob([json], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'form-design.json'
  a.click()
  URL.revokeObjectURL(url)
}

function clearForm() {
  formData.value = []
  selectedComponent.value = null
  nextId.value = 1
}
</script>

<style scoped>
.form-designer {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.designer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  flex-shrink: 0;
}

.designer-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.designer-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.component-panel {
  width: 220px;
  background: #f5f5f5;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.component-panel h3 {
  padding: 16px;
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  border-bottom: 1px solid #e6e6e6;
  background: #fff;
}

.component-panel > div {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.component-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.component-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  margin-bottom: 6px;
  background: #fff;
  border-radius: 6px;
  cursor: move;
  transition: all 0.2s;
}

.component-item:hover {
  background: #e8f4ff;
  transform: translateX(4px);
}

.component-item span {
  font-size: 13px;
}

.canvas-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fafafa;
  min-width: 0;
}

.canvas-container h3 {
  padding: 16px;
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  border-bottom: 1px solid #e6e6e6;
  background: #fff;
  flex-shrink: 0;
}

.canvas-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.property-panel {
  height: 200px;
  background: #fff;
  border-top: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.property-panel h3 {
  padding: 12px 16px;
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  border-bottom: 1px solid #e6e6e6;
  background: #fafafa;
  flex-shrink: 0;
}

.property-panel > div {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}

.empty-property {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

.ghost {
  opacity: 0.5;
}

.drag {
  opacity: 0.8;
}
</style>
