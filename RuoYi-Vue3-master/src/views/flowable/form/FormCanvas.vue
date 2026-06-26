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
            >
              <table>
                <tbody>
                  <tr v-for="rowIdx in comp.rows" :key="rowIdx">
                    <td 
                      v-for="colIdx in comp.cols" 
                      :key="`${rowIdx}-${colIdx}`"
                      class="table-cell"
                      @dragover.stop.prevent
                      @drop.stop="(e) => handleTableCellDrop(e, comp.id, rowIdx, colIdx)"
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
                  </tr>
                </tbody>
              </table>
            </div>
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
import ComponentRenderer from './ComponentRenderer.vue'
import FormUserSelect from './FormUserSelect.vue'
import FormRoleUserSelect from './FormRoleUserSelect.vue'
import FormDeptSelect from './FormDeptSelect.vue'

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

const emit = defineEmits(['select', 'update', 'delete', 'add-component', 'add-child', 'update-child', 'delete-child'])

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
</style>
