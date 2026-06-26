<template>
  <div class="form-role-user-select">
    <el-input 
      :model-value="displayText" 
      :placeholder="placeholder"
      readonly
      @click="openDialog"
      size="default"
    >
      <template #suffix>
        <el-icon class="cursor-pointer" @click="openDialog"><UserFilled /></el-icon>
      </template>
    </el-input>
    
    <el-dialog title="按角色选择用户" v-model="visible" width="900px" top="5vh" append-to-body>
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="角色名称" prop="roleName">
          <el-select
            v-model="selectedRoleId"
            placeholder="请选择角色"
            clearable
            style="width: 200px"
            @change="handleRoleChange"
          >
            <el-option
              v-for="role in roleList"
              :key="role.roleId"
              :label="role.roleName"
              :value="role.roleId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名称" prop="userName">
          <el-input
            v-model="queryParams.userName"
            placeholder="请输入用户名称"
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
        <el-table ref="refTable" :data="userList" @selection-change="handleSelectionChange" @row-click="handleRowClick" height="300px">
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
      </el-row>
      <el-pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="visible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { listRole, allocatedUserList } from '@/api/system/role'

const props = defineProps({
  modelValue: {
    type: [String, Object, Array],
    default: null
  },
  placeholder: {
    type: String,
    default: '请按角色选择用户'
  },
  multiple: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const roleList = ref([])
const userList = ref([])
const total = ref(0)
const selectedRoleId = ref(null)
const selectedUsers = ref([])
const selectedUserIds = ref([])
const selectedUserId = ref(null)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  roleId: undefined,
  userName: undefined
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
  selectedRoleId.value = null
  selectedUserId.value = null
  selectedUsers.value = []
  selectedUserIds.value = []
  userList.value = []
  queryParams.value.roleId = undefined
  getRoleList()
}

function getRoleList() {
  listRole({}).then(res => {
    roleList.value = res.rows || []
  })
}

function handleRoleChange(roleId) {
  selectedRoleId.value = roleId
  queryParams.value.roleId = roleId
  queryParams.value.pageNum = 1
  selectedUsers.value = []
  selectedUserIds.value = []
  if (roleId) {
    getList()
  } else {
    userList.value = []
    total.value = 0
  }
}

function getList() {
  allocatedUserList(queryParams.value).then(res => {
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
    roleId: selectedRoleId.value,
    userName: undefined
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
.form-role-user-select {
  width: 100%;
}
</style>