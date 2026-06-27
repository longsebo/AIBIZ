<template>
  <div class="form-preview">
    <el-form label-width="120px" size="default">
      <template v-for="comp in formData" :key="comp.id">
        <!-- 表格布局 -->
        <div v-if="comp.type === 'table'" class="preview-table-layout">
          <table class="preview-layout-table">
            <tbody>
              <tr v-for="rowIdx in comp.rows" :key="rowIdx">
                <template v-for="colIdx in comp.cols" :key="`${rowIdx}-${colIdx}`">
                  <td 
                    v-if="!isHiddenByMerge(comp, rowIdx, colIdx)"
                    :rowspan="getRowspan(comp, rowIdx, colIdx)"
                    :colspan="getColspan(comp, rowIdx, colIdx)"
                  >
                    <div v-if="getTableCellChild(comp, rowIdx, colIdx)" class="preview-cell-content">
                      <FormComponentRenderer 
                        :component="getTableCellChild(comp, rowIdx, colIdx)" 
                        v-model="previewData[getTableCellChild(comp, rowIdx, colIdx).field]"
                        :show-label="false"
                        label-width="0"
                      />
                    </div>
                    <div v-else class="preview-cell-empty">
                      <span>-</span>
                    </div>
                  </td>
                </template>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- 其他组件 -->
        <FormComponentRenderer 
          v-else
          :component="comp" 
          v-model="previewData[comp.field]"
        />
      </template>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import FormComponentRenderer from './FormComponentRenderer.vue'

const props = defineProps({
  formData: {
    type: Array,
    default: () => []
  }
})

const previewData = ref({})

props.formData.forEach(comp => {
  if (comp.field && comp.defaultValue !== undefined) {
    previewData.value[comp.field] = comp.defaultValue
  }
})

function isHiddenByMerge(table, rowIdx, colIdx) {
  if (!table.mergeInfo) return false
  for (const merge of table.mergeInfo) {
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

function getTableCellChild(table, rowIdx, colIdx) {
  if (!table.children) return null
  return table.children.find(ch => ch.rowIdx === rowIdx && ch.colIdx === colIdx) || null
}
</script>

<style scoped>
.form-preview {
  padding: 20px;
}

.preview-table-layout {
  margin-bottom: 20px;
}

.preview-layout-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #e6e6e6;
}

.preview-layout-table td {
  border: 1px solid #e6e6e6;
  padding: 8px;
  min-height: 40px;
}

.preview-cell-content {
  min-height: 30px;
}

.preview-cell-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
}
</style>