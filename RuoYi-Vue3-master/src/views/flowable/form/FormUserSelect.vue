<template>
  <div class="form-user-select">
    <el-input 
      :model-value="displayText" 
      :placeholder="placeholder"
      readonly
      @click="openDialog"
      size="default"
    >
      <template #suffix>
        <el-icon class="cursor-pointer" @click="openDialog"><User /></el-icon>
      </template>
    </el-input>
    
    <el-dialog title="选择用户" v-model="visible" width="800px" top="5vh" append-to-body>
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="用户名称" prop="userName">
          <el-input
            v-model="queryParams.userName"
            placeholder="请输入用户名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="手机号码" prop="phonenumber">
          <el-input
            v-model="queryParams.phonenumber"
            placeholder="请输入手机号码"
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
        <el-table ref="refTable" :data="userList" @row-click="handleRowClick" @selection-change="handleSelectionChange" height="300px">
          <el-table-column v-if="multiple" type="selection" width="55"></el-table-column>
          <el-table-column v-if="!multiple" width="55">
            <template #default="scope">
              <el-radio :model-value="selectedUserId" :label="scope.row.userId" @change="handleRadioChange(scope.row)">&nbsp;</el-radio>
            </template>
          </el-table-column>
          <el-table-column label="用户名称" prop="userName" :show-overflow-tooltip="true" />
          <el-table-column label="用户昵称" prop="nickName" :show-overflow-tooltip="true" />
          <el-table-column label="邮箱" prop="email" :show-overflow-tooltip="true" />
          <el-table-column label="手机" prop="phonenumber" :show-overflow-tooltip="true" />
          <el-table-column label="状态" align="center" prop="status" width="80">
            <template #default="scope">
              <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
              <el-tag v-else type="danger">停用</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="total > 0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
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
import { User } from '@element-plus/icons-vue'
import { listUser } from '@/api/system/user'

const props = defineProps({
  modelValue: {
    type: [String, Number, Array],
    default: ''
  },
  placeholder: {
    type: String,
    default: '请选择用户'
  },
  multiple: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const userList = ref([])
const total = ref(0)
const selectedUsers = ref([])
const selectedUserIds = ref([])
const selectedUserId = ref(null) // 单选模式下选中的用户ID

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  userName: undefined,
  phonenumber: undefined
})

const displayText = computed(() => {
  if (!props.modelValue) return ''
  if (props.multiple && Array.isArray(props.modelValue)) {
    return props.modelValue.map(item => {
      if (typeof item === 'string') return item
      return item.userName || item.name || ''
    }).join('，')
  }
  if (typeof props.modelValue === 'string') return props.modelValue
  return props.modelValue.userName || props.modelValue.name || props.modelValue || ''
})

function openDialog() {
  visible.value = true
  selectedUserId.value = null
  selectedUsers.value = []
  selectedUserIds.value = []
  getList()
}

function getList() {
  listUser(queryParams.value).then(res => {
    userList.value = res.rows
    total.value = res.total
    nextTick(() => {
      if (selectedUserIds.value.length > 0) {
        userList.value.forEach(row => {
          if (selectedUserIds.value.includes(row.userId)) {
            proxy?.$refs?.refTable?.toggleRowSelection(row, true)
          }
        })
      }
    })
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    phonenumber: undefined
  }
  handleQuery()
}

function handleSelectionChange(selection) {
  selectedUsers.value = selection
  selectedUserIds.value = selection.map(item => item.userId)
}

function handleRowClick(row) {
  if (!props.multiple) {
    selectedUserId.value = row.userId
    selectedUsers.value = [row]
  }
}

function handleRadioChange(row) {
  selectedUserId.value = row.userId
  selectedUsers.value = [row]
}

function handleConfirm() {
  if (props.multiple) {
    if (selectedUsers.value.length === 0) {
      ElMessage.warning('请选择用户')
      return
    }
    emit('update:modelValue', selectedUsers.value)
  } else {
    if (selectedUsers.value.length === 0) {
      ElMessage.warning('请选择一个用户')
      return
    }
    emit('update:modelValue', selectedUsers.value[0])
  }
  visible.value = false
}
</script>

<style scoped>
.form-user-select {
  width: 100%;
}

.cursor-pointer {
  cursor: pointer;
}
</style>
