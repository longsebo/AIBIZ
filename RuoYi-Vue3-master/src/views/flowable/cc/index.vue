<template>
   <div class="app-container">
      <el-table v-loading="loading" :data="dataList" border>
         <el-table-column label="流程名称" align="center" prop="processName" :show-overflow-tooltip="true" />
         <el-table-column label="流程Key" align="center" prop="processKey" :show-overflow-tooltip="true" width="150" />
         <el-table-column label="状态" align="center" prop="status" width="80">
            <template #default="scope">
               <el-tag v-if="scope.row.status === '0'" type="warning">未读</el-tag>
               <el-tag v-else type="success">已读</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="抄送时间" align="center" prop="createTime" width="180" />
         <el-table-column label="操作" align="center" width="200">
            <template #default="scope">
               <el-button link type="primary" icon="Document" @click="handleView(scope.row)">查看详情</el-button>
               <el-button link type="primary" icon="Eye" @click="handleRead(scope.row)" v-if="scope.row.status === '0'">标记已读</el-button>
            </template>
         </el-table-column>
      </el-table>
   </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listCc } from '@/api/flowable/index'

const loading = ref(false)
const dataList = ref([])

const getList = async () => {
   loading.value = true
   try {
      const res = await listCc()
      dataList.value = res.data
   } catch (e) {
      console.error('获取抄送列表失败', e)
   } finally {
      loading.value = false
   }
}

const handleView = (row) => {
   console.log('查看详情:', row)
}

const handleRead = async (row) => {
   try {
      await readCc(row.id)
      row.status = '1'
   } catch (e) {
      console.error('标记已读失败', e)
   }
}

onMounted(() => {
   getList()
})
</script>