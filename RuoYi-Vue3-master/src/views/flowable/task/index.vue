<template>
   <div class="app-container">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
         <el-tab-pane label="我的待办" name="todo">
            <el-table v-loading="loading" :data="todoList" border>
               <el-table-column label="任务ID" align="center" prop="id" width="180" :show-overflow-tooltip="true" />
               <el-table-column label="任务名称" align="center" prop="name" :show-overflow-tooltip="true" />
               <el-table-column label="流程名称" align="center" prop="processDefinitionName" :show-overflow-tooltip="true" />
               <el-table-column label="业务键" align="center" prop="businessKey" width="100" />
               <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
               <el-table-column label="操作" align="center" width="240" class-name="small-padding fixed-width">
                  <template #default="scope">
                     <el-button link type="primary" icon="Check" @click="handleComplete(scope.row)">通过</el-button>
                     <el-button link type="danger" icon="Close" @click="handleReject(scope.row)">驳回</el-button>
                     <el-button link type="warning" icon="User" @click="handleDelegate(scope.row)">委派</el-button>
                  </template>
               </el-table-column>
            </el-table>
         </el-tab-pane>

         <el-tab-pane label="我的已办" name="done">
            <el-table v-loading="loading" :data="doneList" border>
               <el-table-column label="任务ID" align="center" prop="id" width="180" :show-overflow-tooltip="true" />
               <el-table-column label="任务名称" align="center" prop="name" :show-overflow-tooltip="true" />
               <el-table-column label="流程名称" align="center" prop="processDefinitionName" :show-overflow-tooltip="true" />
               <el-table-column label="办理人" align="center" prop="assignee" width="100" />
               <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
            </el-table>
         </el-tab-pane>
      </el-tabs>

      <!-- 委派任务对话框 -->
      <el-dialog v-model="delegateOpen" title="委派任务" width="500px" append-to-body>
         <el-form ref="delegateRef" :model="delegateForm" label-width="100px">
            <el-form-item label="任务名称">
               <el-input v-model="delegateForm.taskName" disabled />
            </el-form-item>
            <el-form-item label="被委派人">
               <el-input v-model="delegateForm.userId" placeholder="请输入被委派人用户ID" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitDelegate">确 定</el-button>
               <el-button @click="delegateOpen = false">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Task">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
   myTodo,
   myDone,
   completeTask,
   rejectTask,
   delegateTask
} from '@/api/flowable'

const activeTab = ref('todo')
const loading = ref(false)
const todoList = ref([])
const doneList = ref([])

const delegateOpen = ref(false)
const delegateForm = reactive({ taskId: '', taskName: '', userId: '' })

onMounted(() => {
   loadTasks()
})

async function loadTasks() {
   if (activeTab.value === 'todo') {
      loading.value = true
      try {
         const res = await myTodo()
         todoList.value = res.data || []
      } finally {
         loading.value = false
      }
   } else {
      loading.value = true
      try {
         const res = await myDone()
         doneList.value = res.data || []
      } finally {
         loading.value = false
      }
   }
}

function handleTabChange() {
   loadTasks()
}

async function handleComplete(row) {
   await ElMessageBox.confirm(`确认完成任务 "${row.name}" 吗？`)
   await completeTask(row.id, {})
   ElMessage.success('任务已完成')
   loadTasks()
}

async function handleReject(row) {
   const { value: reason } = await ElMessageBox.prompt('请输入驳回原因', '驳回任务', { confirmButtonText: '确定', cancelButtonText: '取消' })
   await rejectTask(row.id, reason)
   ElMessage.success('已驳回')
   loadTasks()
}

function handleDelegate(row) {
   delegateForm.taskId = row.id
   delegateForm.taskName = row.name
   delegateForm.userId = ''
   delegateOpen.value = true
}

async function submitDelegate() {
   await delegateTask(delegateForm.taskId, delegateForm.userId)
   ElMessage.success('委派成功')
   delegateOpen.value = false
   loadTasks()
}
</script>
