<template>
  <el-dialog v-model="dialogVisible" :title="'待办任务 - ' + taskDetail.taskName" width="800px" append-to-body>
    <div v-loading="loading" element-loading-text="加载中..." class="dialog-content">
      <div v-if="!loading">
      <div class="form-section">
        <div class="section-title">
          <i class="el-icon-document" />
          <span>流程表单</span>
        </div>
        <el-form ref="formRef" :model="formData" label-width="120px" style="max-height: 400px; overflow-y: auto;">
          <template v-for="comp in visibleComponents" :key="comp.id">
            <FormComponentRenderer
              v-if="comp.type !== 'table'"
              :component="comp"
              :model-value="formData[comp.field]"
              @update:model-value="handleFieldChange(comp.field, $event)"
              :disabled="comp._disabled"
            />
            <div v-else-if="comp.type === 'table' && comp.children" class="form-table-layout">
              <div class="table-title">{{ comp.label }}</div>
              <div class="table-row">
                <div v-for="child in comp.children" :key="child.id" class="table-cell">
                  <FormComponentRenderer
                    :component="child"
                    :model-value="formData[child.field]"
                    @update:model-value="handleFieldChange(child.field, $event)"
                    :showLabel="false"
                    label-width="0"
                    :disabled="child._disabled"
                  />
                </div>
              </div>
            </div>
          </template>
        </el-form>
      </div>

      <div class="comments-section">
        <div class="section-title">
          <i class="el-icon-message" />
          <span>审批意见</span>
        </div>
        <div v-if="groupedComments && groupedComments.length > 0" class="comments-list">
          <div v-for="(group, groupIndex) in groupedComments" :key="group.nodeName" class="comment-group">
            <div class="group-title">
              <span class="group-index">{{ groupIndex + 1 }}</span>
              <span class="group-name">{{ group.nodeName }}</span>
            </div>
            <div v-for="comment in group.comments" :key="comment.taskId" class="comment-item">
              <div class="comment-header">
                <span class="comment-assignee">{{ comment.assigneeName || comment.assignee }}</span>
                <span class="comment-time">{{ formatDate(comment.endTime) }}</span>
                <span :class="['comment-action', comment.action]">
                  {{ comment.action === 'approve' ? '通过' : comment.action === 'reject' ? '驳回' : '已办' }}
                </span>
              </div>
              <div v-if="comment.comment" class="comment-content">
                {{ comment.comment }}
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-comments">
          暂无审批意见
        </div>
      </div>

      <div class="comment-input-section">
        <el-form-item label="审批意见" label-width="120px">
          <el-input
            v-model="comment"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见"
          />
        </el-form-item>
      </div>
    </div>
  </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button
          v-for="btn in actionButtons"
          :key="btn.key"
          :type="btn.type"
          :icon="btn.icon"
          @click="handleAction(btn.key)"
        >{{ btn.label }}</el-button>
        <el-button @click="handleClose">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTaskDetail, completeTask, rejectTask } from '@/api/flowable'
import { getForm } from '@/api/flowable/form'
import { getProcessFormByKey } from '@/api/flowable/processForm'
import FormComponentRenderer from '@/views/flowable/form/FormComponentRenderer.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  taskId: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['close', 'success'])

const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref(null)
const formData = reactive({})
const formComponents = ref([])
const comment = ref('')

const taskDetail = reactive({
  taskId: '',
  taskName: '',
  processDefinitionId: '',
  processDefinitionKey: '',
  processDefinitionName: '',
  processInstanceId: '',
  businessKey: '',
  assignee: '',
  createTime: null,
  startUserId: '',
  processVariables: {},
  historyComments: [],
  nextNodes: [],
  currentNodeConfig: null
})

const currentNodeConfig = computed(() => {
  if (!taskDetail.currentNodeConfig) {
    return {}
  }
  try {
    return JSON.parse(taskDetail.currentNodeConfig)
  } catch (e) {
    return {}
  }
})

const groupedComments = computed(() => {
  const comments = taskDetail.historyComments || []
  const groups = {}
  
  comments.forEach(comment => {
    const nodeName = comment.taskName || '未知节点'
    if (!groups[nodeName]) {
      groups[nodeName] = {
        nodeName: nodeName,
        comments: []
      }
    }
    groups[nodeName].comments.push(comment)
  })
  
  return Object.values(groups)
})

const actionButtons = computed(() => {
  const config = currentNodeConfig.value
  const buttons = config.buttons || ['approve', 'reject']
  const buttonMap = {
    approve: { key: 'approve', label: '通过', type: 'primary', icon: 'Check' },
    reject: { key: 'reject', label: '驳回', type: 'danger', icon: 'Close' },
    delegate: { key: 'delegate', label: '委派', type: 'warning', icon: 'User' },
    addSign: { key: 'addSign', label: '加签', type: 'info', icon: 'Plus' },
    transfer: { key: 'transfer', label: '转办', type: '', icon: 'Switch' }
  }
  return buttons.map(key => buttonMap[key] || { key, label: key, type: '', icon: '' })
})

const visibleComponents = computed(() => {
  const { writeFields = [], readonlyFields = [], hideFields = [] } = currentNodeConfig.value
  
  return formComponents.value.map(comp => {
    const field = comp.field || comp.id?.toString()
    const fullField = comp.label ? `${comp.label}-${field}` : field
    let disabled = true
    
    if (writeFields.length > 0) {
      disabled = !writeFields.includes(field) && !writeFields.includes(fullField)
    } else {
      disabled = readonlyFields.includes(field) || readonlyFields.includes(fullField)
    }
    
    let processedComp = { ...comp, _disabled: disabled }
    
    if (comp.type === 'table' && comp.children) {
      processedComp.children = comp.children.filter(child => {
        const childField = child.field || child.id?.toString()
        const childFullField = comp.label ? `${comp.label}-${child.label || childField}` : childField
        return !hideFields.includes(childField) && !hideFields.includes(childFullField)
      }).map(child => {
        const childField = child.field || child.id?.toString()
        const childFullField = comp.label ? `${comp.label}-${child.label || childField}` : childField
        let childDisabled = disabled
        if (!disabled && writeFields.length > 0) {
          childDisabled = !writeFields.includes(childField) && !writeFields.includes(childFullField)
        } else if (!disabled && readonlyFields.length > 0) {
          childDisabled = readonlyFields.includes(childField) || readonlyFields.includes(childFullField)
        }
        return { ...child, _disabled: childDisabled }
      })
    }
    
    return processedComp
  }).filter(comp => {
    if (comp.type === 'title') return true
    const field = comp.field || comp.id?.toString()
    const fullField = comp.label ? `${comp.label}-${field}` : field
    if (hideFields.includes(field) || hideFields.includes(fullField)) return false
    if (comp.type === 'table' && comp.children && comp.children.length === 0) return false
    return true
  })
})

watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val && props.taskId) {
    initTask()
  }
})

async function initTask() {
  loading.value = true
  Object.keys(formData).forEach(key => delete formData[key])
  formComponents.value = []
  comment.value = ''
  
  try {
    const detailRes = await getTaskDetail(props.taskId)
    const detail = detailRes.data
    if (detail) {
      Object.assign(taskDetail, detail)
      
      if (detail.processVariables) {
        Object.assign(formData, detail.processVariables)
      }
      
      if (detail.processDefinitionKey) {
        await loadForm(detail.processDefinitionKey)
      }
    }
  } catch (e) {
    console.error('加载任务详情失败', e)
    ElMessage.error('加载任务详情失败')
  } finally {
    loading.value = false
  }
}

async function loadForm(processKey) {
  try {
    const formRes = await getProcessFormByKey(processKey)
    if (formRes.data && formRes.data.formId) {
      const formDataRes = await getForm(formRes.data.formId)
      if (formDataRes.data && formDataRes.data.formJson) {
        const components = JSON.parse(formDataRes.data.formJson)
        formComponents.value = components
      }
    }
  } catch (e) {
    console.error('加载表单失败', e)
  }
}

function handleFieldChange(field, value) {
  formData[field] = value
}

function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function handleAction(action) {
  try {
    if (action === 'approve') {
      const variables = { ...formData }
      variables.comment = comment.value
      variables.action = 'approve'
      await completeTask(props.taskId, { variables })
      ElMessage.success('审批通过')
    } else if (action === 'reject') {
      await rejectTask(props.taskId, comment.value || '驳回')
      ElMessage.success('已驳回')
    } else if (action === 'delegate') {
      ElMessage.warning('委派功能开发中')
      return
    } else if (action === 'addSign') {
      ElMessage.warning('加签功能开发中')
      return
    } else if (action === 'transfer') {
      ElMessage.warning('转办功能开发中')
      return
    }
    
    dialogVisible.value = false
    emit('success')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function handleClose() {
  dialogVisible.value = false
  emit('close')
}
</script>

<style scoped>
.loading-container {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.section-title {
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 14px;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebf0f5;
}

.section-title i {
  margin-right: 8px;
  color: #409eff;
}

.form-section {
  margin-bottom: 20px;
}

.comments-section {
  margin-bottom: 20px;
}

.comments-list {
  max-height: 200px;
  overflow-y: auto;
}

.comment-group {
  margin-bottom: 20px;
  border-left: 3px solid #409eff;
  padding-left: 15px;
  background: #fafafa;
  padding-top: 10px;
  padding-bottom: 10px;
  padding-right: 10px;
  border-radius: 0 4px 4px 0;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebf0f5;
}

.group-index {
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  background: #409eff;
  color: #fff;
  border-radius: 50%;
  font-size: 12px;
}

.group-name {
  font-weight: bold;
  color: #303133;
}

.comment-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
  margin-bottom: 10px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-node {
  font-weight: bold;
  color: #303133;
}

.comment-assignee {
  color: #606266;
  font-size: 13px;
}

.comment-time {
  color: #909399;
  font-size: 12px;
}

.comment-action {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.comment-action.approve {
  background: #f0f9eb;
  color: #67c23a;
}

.comment-action.reject {
  background: #fef0f0;
  color: #f56c6c;
}

.comment-action.complete {
  background: #ecf5ff;
  color: #409eff;
}

.comment-content {
  color: #606266;
  font-size: 13px;
  padding-left: 0;
}

.empty-comments {
  text-align: center;
  color: #909399;
  padding: 20px;
  background: #fafafa;
  border-radius: 4px;
}

.next-node-section {
  margin-bottom: 20px;
}

.next-nodes-list {
  max-height: 150px;
  overflow-y: auto;
}

.next-node-item {
  padding: 12px;
  background: #f0f9eb;
  border-radius: 4px;
  margin-bottom: 8px;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.node-name {
  font-weight: bold;
  color: #67c23a;
}

.node-type {
  padding: 2px 6px;
  background: #c2e7b0;
  color: #389e0d;
  border-radius: 4px;
  font-size: 12px;
}

.node-type.end {
  background: #ffe5e5;
  color: #d93636;
}

.node-assignees {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.assignee-label {
  color: #909399;
}

.assignee-names {
  color: #606266;
}

.empty-next-nodes {
  text-align: center;
  color: #909399;
  padding: 20px;
  background: #fafafa;
  border-radius: 4px;
}

.comment-input-section {
  margin-top: 15px;
}

.form-table-layout {
  margin-bottom: 15px;
}

.table-title {
  font-weight: bold;
  margin-bottom: 8px;
  font-size: 13px;
}

.table-row {
  display: flex;
  gap: 15px;
}

.table-cell {
  flex: 1;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>