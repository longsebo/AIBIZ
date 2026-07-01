<template>
  <div class="form-data-table">
    <el-table 
      :data="tableData" 
      :border="border"
      :stripe="stripe"
      :size="size"
      :height="height"
      style="width: 100%"
    >
      <el-table-column 
        v-for="col in columns" 
        :key="col.prop"
        :prop="col.prop"
        :label="col.label"
        :width="col.width"
        :min-width="col.minWidth"
        :fixed="col.fixed"
        :sortable="col.sortable"
      >
        <template #default="scope">
          <span>{{ scope.row[col.prop] }}</span>
        </template>
      </el-table-column>
      
      <!-- 操作列 -->
      <el-table-column v-if="showActions" label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button type="primary" link size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(scope.$index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 添加行按钮 -->
    <div v-if="editable" class="table-actions">
      <el-button type="primary" size="small" @click="handleAdd">添加行</el-button>
    </div>
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="editVisible" title="编辑数据" width="500px" append-to-body>
      <el-form :model="editForm" label-width="100px">
        <el-form-item v-for="col in columns" :key="col.prop" :label="col.label">
          <el-input v-model="editForm[col.prop]" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  columns: {
    type: Array,
    default: () => []
  },
  data: {
    type: Array,
    default: () => []
  },
  border: {
    type: Boolean,
    default: true
  },
  stripe: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default'
  },
  height: {
    type: [Number, String],
    default: undefined
  },
  editable: {
    type: Boolean,
    default: true
  },
  showActions: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:data'])

const tableData = computed({
  get: () => props.data || [],
  set: (val) => emit('update:data', val)
})

const editVisible = ref(false)
const editForm = ref({})
const editIndex = ref(-1)

function handleAdd() {
  editIndex.value = -1
  const newRow = {}
  props.columns.forEach(col => {
    newRow[col.prop] = ''
  })
  editForm.value = newRow
  editVisible.value = true
}

function handleEdit(row) {
  const index = tableData.value.indexOf(row)
  editIndex.value = index
  editForm.value = { ...row }
  editVisible.value = true
}

function handleDelete(index) {
  tableData.value = tableData.value.filter((_, i) => i !== index)
}

function handleSave() {
  if (editIndex.value === -1) {
    tableData.value = [...tableData.value, { ...editForm.value }]
  } else {
    const newData = [...tableData.value]
    newData[editIndex.value] = { ...editForm.value }
    tableData.value = newData
  }
  editVisible.value = false
}
</script>

<style scoped>
.form-data-table {
  width: 100%;
}

.table-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-start;
}
</style>