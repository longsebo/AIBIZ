<template>
  <div 
    class="form-canvas" 
    @dragover.prevent 
    @drop="handleDrop"
  >
    <div class="canvas-form">
      <div
        v-for="comp in formData"
        :key="comp.id"
        class="form-item"
        :class="{ selected: selectedComponent?.id === comp.id }"
        @click="emit('select', comp)"
      >
        <div v-if="comp.type === 'title'" class="title-item" :class="`align-${comp.align || 'left'}`">
          <span 
            class="title-text" 
            :class="`style-${comp.fontStyle || 'normal'}`"
            :style="titleStyle(comp)"
          >{{ comp.label }}</span>
        </div>
        
        <div v-else-if="comp.type === 'input'" class="input-item">
          <el-form-item :label="comp.label" :required="comp.required">
            <el-input :placeholder="comp.placeholder" :value="comp.defaultValue" />
          </el-form-item>
        </div>
        
        <div v-else-if="comp.type === 'textarea'" class="textarea-item">
          <el-form-item :label="comp.label" :required="comp.required">
            <el-input :type="'textarea'" :placeholder="comp.placeholder" :value="comp.defaultValue" />
          </el-form-item>
        </div>
        
        <div v-else-if="comp.type === 'radio'" class="radio-item">
          <el-form-item :label="comp.label" :required="comp.required">
            <el-radio-group :value="comp.defaultValue">
              <el-radio v-for="(opt, idx) in comp.options" :key="idx" :label="opt">{{ opt }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        
        <div v-else-if="comp.type === 'checkbox'" class="checkbox-item">
          <el-form-item :label="comp.label" :required="comp.required">
            <el-checkbox-group :value="comp.defaultValue">
              <el-checkbox v-for="(opt, idx) in comp.options" :key="idx" :label="opt">{{ opt }}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </div>
        
        <div v-else-if="comp.type === 'select'" class="select-item">
          <el-form-item :label="comp.label" :required="comp.required">
            <el-select :placeholder="comp.placeholder" :value="comp.defaultValue">
              <el-option v-for="(opt, idx) in comp.options" :key="idx" :label="opt" :value="opt" />
            </el-select>
          </el-form-item>
        </div>
        
        <div v-else-if="comp.type === 'date'" class="date-item">
          <el-form-item :label="comp.label" :required="comp.required">
            <el-date-picker :type="'date'" :placeholder="comp.placeholder" :value="comp.defaultValue" />
          </el-form-item>
        </div>
        
        <div v-else-if="comp.type === 'user'" class="user-item">
          <el-form-item :label="comp.label" :required="comp.required">
            <FormUserSelect 
              v-model="comp.defaultValue" 
              :placeholder="comp.placeholder"
              :multiple="false"
            />
          </el-form-item>
        </div>
        
        <div v-else-if="comp.type === 'users'" class="users-item">
          <el-form-item :label="comp.label" :required="comp.required">
            <FormUserSelect 
              v-model="comp.defaultValue" 
              :placeholder="comp.placeholder"
              :multiple="true"
            />
          </el-form-item>
        </div>
        
        <div v-else-if="comp.type === 'roleUser'" class="role-user-item">
          <el-form-item :label="comp.label" :required="comp.required">
            <FormRoleUserSelect 
              v-model="comp.defaultValue" 
              :placeholder="comp.placeholder"
              :multiple="false"
            />
          </el-form-item>
        </div>
        
        <div v-else-if="comp.type === 'roleUsers'" class="role-users-item">
          <el-form-item :label="comp.label" :required="comp.required">
            <FormRoleUserSelect 
              v-model="comp.defaultValue" 
              :placeholder="comp.placeholder"
              :multiple="true"
            />
          </el-form-item>
        </div>
        
        <div v-else-if="comp.type === 'dept'" class="dept-item">
          <el-form-item :label="comp.label" :required="comp.required">
            <FormDeptSelect 
              v-model="comp.defaultValue" 
              :placeholder="comp.placeholder"
              :multiple="false"
            />
          </el-form-item>
        </div>
        
        <div v-else-if="comp.type === 'depts'" class="depts-item">
          <el-form-item :label="comp.label" :required="comp.required">
            <FormDeptSelect 
              v-model="comp.defaultValue" 
              :placeholder="comp.placeholder"
              :multiple="true"
            />
          </el-form-item>
        </div>
        
        <div v-else-if="comp.type === 'table'" class="table-item">
          <el-form-item :label="comp.label">
            <div 
              class="design-table" 
              @dragover.stop.prevent 
              @drop.stop="(e) => handleTableDrop(e, comp.id)"
              @contextmenu.prevent
              @mouseup="endDragSelect"
            >
              <table>
                <tbody>
                  <tr v-for="rowIdx in comp.rows" :key="rowIdx">
                    <template v-for="colIdx in comp.cols" :key="`${rowIdx}-${colIdx}`">
                      <td 
                        v-if="!isHiddenByMerge(comp, rowIdx, colIdx)"
                        :rowspan="getRowspan(comp, rowIdx, colIdx)"
                        :colspan="getColspan(comp, rowIdx, colIdx)"
                        class="table-cell"
                        :class="{ 'cell-selected': isCellSelected(comp.id, rowIdx, colIdx) }"
                        @dragover.stop.prevent
                        @drop.stop="(e) => handleTableCellDrop(e, comp.id, rowIdx, colIdx)"
                        @contextmenu.prevent="(e) => handleCellContextMenu(e, comp.id, rowIdx, colIdx)"
                        @click.stop="handleCellClick(comp.id, rowIdx, colIdx, $event)"
                      >
                        <div 
                          v-if="getTableCellChild(comp, rowIdx, colIdx)" 
                          class="cell-content"
                          @click.stop="emit('select', getTableCellChild(comp, rowIdx, colIdx))"
                        >
                          <component-renderer 
                            :component="getTableCellChild(comp, rowIdx, colIdx)"
                          />
                        </div>
                        <div v-else class="cell-placeholder" @dragover.stop.prevent @drop.stop="(e) => handleTableCellDrop(e, comp.id, rowIdx, colIdx)">
                          <span>+ 拖入组件</span>
                        </div>
                      </td>
                    </template>
                  </tr>
                </tbody>
              </table>
            </div>
          </el-form-item>
          
          <!-- 右键菜单 -->
          <div 
            v-if="contextMenu.visible" 
            class="context-menu"
            :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
          >
            <div class="menu-item" :class="{ disabled: !hasMultipleSelection }" @click="handleMenuClick('merge')">合并单元格</div>
            <div class="menu-item" @click="handleMenuClick('unmerge')">撤销合并</div>
            <div class="menu-divider"></div>
            <div class="menu-item" @click="handleMenuClick('insertRowBefore')">前插入行</div>
            <div class="menu-item" @click="handleMenuClick('insertRowAfter')">后插入行</div>
            <div class="menu-item" @click="handleMenuClick('insertColBefore')">前插入列</div>
            <div class="menu-item" @click="handleMenuClick('insertColAfter')">后插入列</div>
            <div class="menu-divider"></div>
            <div class="menu-item danger" @click="handleMenuClick('deleteRow')">删除行</div>
            <div class="menu-item danger" @click="handleMenuClick('deleteCol')">删除列</div>
          </div>
        </div>
        
        <div v-else-if="comp.type === 'dataTable'" class="datatable-item">
          <el-form-item :label="comp.label">
            <FormDataTable 
              :columns="comp.columns"
              v-model:data="comp.data"
              :border="comp.border"
              :stripe="comp.stripe"
              :size="comp.size"
              :height="comp.height"
              :editable="comp.editable"
              :show-actions="comp.showActions"
            />
          </el-form-item>
        </div>
        
        <div class="item-actions">
          <el-button size="small" type="danger" @click.stop="emit('delete', comp.id)">删除</el-button>
        </div>
      </div>
      
      <div v-if="formData.length === 0" class="empty-canvas">
        <p>将左侧组件拖入此处</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import ComponentRenderer from './ComponentRenderer.vue'
import FormUserSelect from './FormUserSelect.vue'
import FormRoleUserSelect from './FormRoleUserSelect.vue'
import FormDeptSelect from './FormDeptSelect.vue'
import FormDataTable from './FormDataTable.vue'

const props = defineProps({
  formData: {
    type: Array,
    required: true
  },
  selectedComponent: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['select', 'update', 'delete', 'add-component', 'add-child', 'update-child', 'delete-child', 'table-operation'])

// 右键菜单状态
const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  tableId: null,
  rowIdx: null,
  colIdx: null
})

// 单元格选中状态
const selectedCells = ref(new Map())

// 是否有多个单元格被选中
const hasMultipleSelection = computed(() => {
  if (!contextMenu.value.tableId) return false
  const tableCells = selectedCells.value.get(contextMenu.value.tableId) || []
  return tableCells.length > 1
})

// 点击其他地方关闭菜单和清空选中
onMounted(() => {
  document.addEventListener('click', closeContextMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', closeContextMenu)
})

function closeContextMenu() {
  contextMenu.value.visible = false
}

// 单元格选中相关函数
function isCellSelected(tableId, rowIdx, colIdx) {
  const tableCells = selectedCells.value.get(tableId) || []
  return tableCells.some(cell => cell.row === rowIdx && cell.col === colIdx)
}

function toggleCellSelection(tableId, rowIdx, colIdx) {
  let tableCells = selectedCells.value.get(tableId) || []
  const index = tableCells.findIndex(cell => cell.row === rowIdx && cell.col === colIdx)
  if (index !== -1) {
    tableCells.splice(index, 1)
  } else {
    tableCells.push({ row: rowIdx, col: colIdx })
  }
  selectedCells.value.set(tableId, tableCells)
}

function clearCellSelection(tableId) {
  selectedCells.value.delete(tableId)
}

function handleCellClick(tableId, rowIdx, colIdx, event) {
  const table = props.formData.find(c => c.id === tableId)
  if (!table) return
  
  if (event.ctrlKey || event.metaKey) {
    toggleCellSelection(tableId, rowIdx, colIdx)
  } else {
    clearCellSelection(tableId)
    toggleCellSelection(tableId, rowIdx, colIdx)
  }
}

// 右键菜单处理
function handleCellContextMenu(e, tableId, rowIdx, colIdx) {
  e.preventDefault()
  e.stopPropagation()
  contextMenu.value = {
    visible: true,
    x: e.clientX,
    y: e.clientY,
    tableId,
    rowIdx,
    colIdx
  }
}

function handleMenuClick(operation) {
  const tableId = contextMenu.value.tableId
  const rowIdx = contextMenu.value.rowIdx
  const colIdx = contextMenu.value.colIdx
  
  if (operation === 'merge') {
    const cells = selectedCells.value.get(tableId) || []
    emit('table-operation', tableId, operation, rowIdx, colIdx, cells)
  } else {
    emit('table-operation', tableId, operation, rowIdx, colIdx)
  }
  closeContextMenu()
}

// 合并单元格辅助函数
function isHiddenByMerge(table, rowIdx, colIdx) {
  if (!table.mergeInfo) return false
  for (const merge of table.mergeInfo) {
    // 如果当前单元格在合并范围内且不是起始单元格，则隐藏
    if (rowIdx >= merge.row && rowIdx < merge.row + merge.rowspan &&
        colIdx >= merge.col && colIdx < merge.col + merge.colspan) {
      if (rowIdx !== merge.row || colIdx !== merge.col) {
        return true
      }
    }
  }
  return false
}

function getRowspan(table, rowIdx, colIdx) {
  if (!table.mergeInfo) return 1
  for (const merge of table.mergeInfo) {
    if (merge.row === rowIdx && merge.col === colIdx) {
      return merge.rowspan
    }
  }
  return 1
}

function getColspan(table, rowIdx, colIdx) {
  if (!table.mergeInfo) return 1
  for (const merge of table.mergeInfo) {
    if (merge.row === rowIdx && merge.col === colIdx) {
      return merge.colspan
    }
  }
  return 1
}

function handleDrop(e) {
  e.preventDefault()
  e.stopPropagation()
  const componentType = e.dataTransfer.getData('componentType')
  if (componentType) {
    emit('add-component', componentType)
  }
}

function handleTableDrop(e, tableId) {
  e.preventDefault()
  e.stopPropagation()
  const componentType = e.dataTransfer.getData('componentType')
  if (componentType) {
    const table = props.formData.find(c => c.id === tableId)
    if (table) {
      for (let r = 1; r <= table.rows; r++) {
        for (let c = 1; c <= table.cols; c++) {
          const child = getTableCellChild(table, r, c)
          if (!child) {
            emit('add-child', tableId, componentType, r, c)
            return
          }
        }
      }
    }
  }
}

function handleTableCellDrop(e, tableId, rowIdx, colIdx) {
  e.preventDefault()
  e.stopPropagation()
  const componentType = e.dataTransfer.getData('componentType')
  if (componentType) {
    emit('add-child', tableId, componentType, rowIdx, colIdx)
  }
}

function getTableCellChild(table, rowIdx, colIdx) {
  if (!table.children || table.children.length === 0) return null
  return table.children.find(ch => ch.rowIdx === rowIdx && ch.colIdx === colIdx) || null
}

function titleStyle(comp) {
  const style = {}
  if (comp.fontSize) {
    style.fontSize = comp.fontSize + 'px'
  }
  if (comp.fontFamily) {
    style.fontFamily = comp.fontFamily
  }
  return style
}
</script>

<style scoped>
.form-canvas {
  min-height: 400px;
  padding: 16px;
  background: #fff;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  transition: all 0.3s;
}

.form-canvas:hover {
  border-color: #409eff;
}

.canvas-form {
  min-height: 300px;
}

.form-item {
  position: relative;
  padding: 12px;
  margin-bottom: 12px;
  background: #fafafa;
  border: 1px solid #e6e6e6;
  border-radius: 6px;
  transition: all 0.2s;
}

.form-item:hover {
  border-color: #409eff;
}

.form-item.selected {
  border-color: #409eff;
  background: #e8f4ff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.item-actions {
  position: absolute;
  right: 8px;
  top: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.form-item:hover .item-actions {
  opacity: 1;
}

.title-item {
  padding: 8px 0;
  width: 100%;
}

.title-item.align-left {
  text-align: left;
}

.title-item.align-center {
  text-align: center;
}

.title-item.align-right {
  text-align: right;
}

.title-item .title-text {
  color: #303133;
}

.title-item .style-normal {
  font-weight: normal;
  font-style: normal;
}

.title-item .style-bold {
  font-weight: bold;
  font-style: normal;
}

.title-item .style-italic {
  font-weight: normal;
  font-style: italic;
}

.title-item .style-bold-italic {
  font-weight: bold;
  font-style: italic;
}

.empty-canvas {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #999;
}

.design-table {
  width: 100%;
  min-height: 120px;
}

.design-table table {
  width: 100%;
  border-collapse: collapse;
}

.design-table td {
  border: 2px dashed #d9d9d9;
  padding: 8px;
  min-width: 120px;
  min-height: 60px;
}

.design-table td:hover {
  border-color: #409eff;
  background: #f5f7fa;
}

.table-cell {
  position: relative;
}

.cell-content {
  padding: 4px;
  cursor: pointer;
}

.cell-content:hover {
  background: #e8f4ff;
  border-radius: 4px;
}

.cell-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  color: #ccc;
  font-size: 12px;
}

.context-menu {
  position: fixed;
  z-index: 9999;
  background: #fff;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  padding: 4px 0;
  min-width: 120px;
}

.menu-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #303133;
  transition: background 0.2s;
}

.menu-item:hover {
  background: #f5f7fa;
}

.menu-item.danger {
  color: #f56c6c;
}

.menu-item.danger:hover {
  background: #fef0f0;
}

.menu-item.disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.menu-item.disabled:hover {
  background: none;
}

.menu-divider {
  height: 1px;
  background: #e6e6e6;
  margin: 4px 0;
}

.cell-selected {
  background: #e8f4ff !important;
  border-color: #409eff !important;
}
</style>
