<template>
  <div class="form-dept-select">
    <el-input 
      :model-value="displayText" 
      :placeholder="placeholder"
      readonly
      :disabled="disabled"
      @click="disabled ? undefined : openDialog"
      size="default"
    >
      <template #suffix>
        <el-icon :class="disabled ? '' : 'cursor-pointer'" @click="disabled ? undefined : openDialog"><OfficeBuilding /></el-icon>
      </template>
    </el-input>
    
    <el-dialog title="选择部门" v-model="visible" width="600px" top="5vh" append-to-body>
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="部门名称" prop="deptName">
          <el-input
            v-model="queryParams.deptName"
            placeholder="请输入部门名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-row>
        <el-tree
          ref="deptTreeRef"
          :data="deptTree"
          :props="treeProps"
          :show-checkbox="multiple"
          node-key="deptId"
          :default-expand-all="true"
          :filter-node-method="filterNode"
          @node-click="handleNodeClick"
          @check-change="handleCheckChange"
          style="height: 300px; overflow-y: auto;"
        >
          <template #default="{ data }">
            <span>{{ data.deptName }}</span>
          </template>
        </el-tree>
      </el-row>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleConfirm">确 定</el-button>
          <el-button @click="visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { OfficeBuilding } from '@element-plus/icons-vue'
import { listDept } from '@/api/system/dept'

const props = defineProps({
  modelValue: {
    type: [String, Number, Array],
    default: ''
  },
  placeholder: {
    type: String,
    default: '请选择部门'
  },
  multiple: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const deptTree = ref([])
const deptTreeRef = ref(null)
const selectedDepts = ref([])
const selectedDeptIds = ref([])
const selectedDeptId = ref(null) // 单选模式下选中的部门ID

const queryParams = ref({
  deptName: undefined
})

const treeProps = {
  label: 'deptName',
  children: 'children'
}

const displayText = computed(() => {
  if (!props.modelValue) return ''
  if (props.multiple && Array.isArray(props.modelValue)) {
    return props.modelValue.map(item => {
      if (typeof item === 'string') return item
      return item.deptName || item.name || ''
    }).join('，')
  }
  if (typeof props.modelValue === 'string') return props.modelValue
  return props.modelValue.deptName || props.modelValue.name || props.modelValue || ''
})

function openDialog() {
  visible.value = true
  selectedDeptId.value = null
  selectedDepts.value = []
  selectedDeptIds.value = []
  getDeptTree()
}

function getDeptTree() {
  listDept(queryParams.value).then(res => {
    deptTree.value = res.data
  })
}

function handleQuery() {
  getDeptTree()
}

function resetQuery() {
  queryParams.value = {
    deptName: undefined
  }
  handleQuery()
}

function filterNode(value, data) {
  if (!value) return true
  return data.deptName.indexOf(value) !== -1
}

function handleNodeClick(data) {
  if (!props.multiple) {
    selectedDeptId.value = data.deptId
    selectedDepts.value = [data]
  }
}

function handleCheckChange() {
  const treeRef = deptTreeRef.value
  if (!treeRef) return
  if (props.multiple) {
    const checkedNodes = treeRef.getCheckedNodes()
    selectedDepts.value = checkedNodes
    selectedDeptIds.value = checkedNodes.map(n => n.deptId)
  } else {
    const checkedNodes = treeRef.getCheckedNodes()
    if (checkedNodes.length > 1) {
      const lastNode = checkedNodes[checkedNodes.length - 1]
      treeRef.setCheckedNodes([lastNode])
      selectedDepts.value = [lastNode]
      selectedDeptIds.value = [lastNode.deptId]
    } else {
      selectedDepts.value = checkedNodes
      selectedDeptIds.value = checkedNodes.map(n => n.deptId)
    }
  }
}

function handleConfirm() {
  if (props.multiple) {
    if (selectedDepts.value.length === 0) {
      ElMessage.warning('请选择部门')
      return
    }
    emit('update:modelValue', selectedDepts.value)
  } else {
    if (selectedDepts.value.length === 0) {
      ElMessage.warning('请选择一个部门')
      return
    }
    emit('update:modelValue', selectedDepts.value[0])
  }
  visible.value = false
}
</script>

<style scoped>
.form-dept-select {
  width: 100%;
}

.cursor-pointer {
  cursor: pointer;
}
</style>
