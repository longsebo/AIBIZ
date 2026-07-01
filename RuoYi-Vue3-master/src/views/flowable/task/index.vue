<template>
   <div class="app-container">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
         <el-tab-pane label="我的待办" name="todo">
            <el-table v-loading="loading" :data="todoList" border @row-click="handleRowClick">
               <el-table-column label="任务ID" align="center" prop="id" width="180" :show-overflow-tooltip="true" />
               <el-table-column label="任务名称" align="center" prop="name" :show-overflow-tooltip="true" />
               <el-table-column label="流程名称" align="center" prop="processDefinitionName" :show-overflow-tooltip="true" />
               <el-table-column label="业务键" align="center" prop="businessKey" width="100" />
               <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
               <el-table-column label="操作" align="center" width="120" class-name="small-padding fixed-width">
                  <template #default="scope">
                     <el-button link type="primary" icon="Edit" @click.stop="handleProcess(scope.row)">处理</el-button>
                     <el-button link type="danger" icon="Close" @click.stop="handleReject(scope.row)">驳回</el-button>
                  </template>
               </el-table-column>
            </el-table>
         </el-tab-pane>

         <el-tab-pane label="我的已办" name="done">
            <el-table v-loading="loading" :data="doneList" border>
               <el-table-column label="任务ID" align="center" prop="id" width="180" :show-overflow-tooltip="true" />
               <el-table-column label="任务名称" align="center" prop="name" :show-overflow-tooltip="true" />
               <el-table-column label="流程名称" align="center" prop="processDefinitionName" :show-overflow-tooltip="true" />
               <el-table-column label="办理人" align="center" width="100">
                  <template #default="scope">
                     {{ scope.row.assigneeName || scope.row.assignee }}
                  </template>
               </el-table-column>
               <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
            </el-table>
         </el-tab-pane>
      </el-tabs>

      <ProcessTodo 
        :visible="todoDialogVisible" 
        :task-id="currentTaskId"
        @close="todoDialogVisible = false"
        @success="handleTodoSuccess"
      />
   </div>
</template>

<script setup name="Task">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
   myTodo,
   myDone,
   completeTask,
   rejectTask
} from '@/api/flowable'
import ProcessTodo from './ProcessTodo.vue'

const activeTab = ref('todo')
const loading = ref(false)
const todoList = ref([])
const doneList = ref([])

const todoDialogVisible = ref(false)
const currentTaskId = ref('')

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

function handleRowClick(row) {
   if (activeTab.value === 'todo') {
      handleProcess(row)
   }
}

function handleProcess(row) {
   currentTaskId.value = row.id
   todoDialogVisible.value = true
}

function handleTodoSuccess() {
   loadTasks()
}

async function handleReject(row) {
   const { value: reason } = await ElMessageBox.prompt('请输入驳回原因', '驳回任务', { confirmButtonText: '确定', cancelButtonText: '取消' })
   await rejectTask(row.id, reason)
   ElMessage.success('已驳回')
   loadTasks()
}
</script>