<template>
  <el-dialog v-model="dialogVisible" :title="'启动流程 - ' + processName" width="700px" append-to-body>
    <el-form ref="formRef" :model="formData" label-width="120px" style="max-height: 500px; overflow-y: auto;">
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

    <div v-if="attachments.length > 0" class="attachment-section">
      <div class="section-title">附件列表</div>
      <el-table :data="attachments" border size="small" style="width: 100%;">
        <el-table-column label="文件名" prop="fileName" :show-overflow-tooltip="true" />
        <el-table-column label="大小" prop="fileSize" width="100">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button v-if="isPreviewable(scope.row.fileName)" link type="primary" @click="previewAttachment(scope.row)">预览</el-button>
            <el-button link type="info" @click="downloadAttachment(scope.row)">下载</el-button>
            <el-button link type="danger" @click="removeAttachment(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="previewVisible" :title="previewTitle" width="80%" top="5vh" append-to-body>
      <div v-if="previewType === 'image'" class="preview-content">
        <img :src="previewUrl" style="max-width: 100%; max-height: 600px;" />
      </div>
      <div v-else-if="previewType === 'pdf'" class="preview-content">
        <iframe :src="previewUrl" style="width: 100%; height: 600px;" />
      </div>
      <div v-else-if="previewType === 'html'" class="preview-content">
        <div v-html="previewHtml" style="max-height: 600px; overflow-y: auto;" />
      </div>
    </el-dialog>

    <div class="attachment-upload">
      <el-upload
        :action="uploadUrl"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :on-remove="handleRemove"
        :file-list="uploadFileList"
        :http-request="handleCustomUpload"
        accept=".doc,.docx,.pdf,.xls,.xlsx,.jpg,.jpeg,.png,.zip,.rar"
        :limit="10"
        :multiple="true"
        :auto-upload="true"
        class="upload-demo"
      >
        <el-button type="primary" size="small" icon="Upload">上传附件</el-button>
        <template #tip>
          <div class="el-upload__tip">支持 doc、docx、pdf、xls、xlsx、jpg、png、zip、rar 格式，最多上传10个文件</div>
        </template>
      </el-upload>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="showSaveDraft" @click="handleSaveDraft">保存草稿</el-button>
        <el-button v-if="showClear" @click="handleClear">重置</el-button>
        <el-button v-if="showSubmit" type="primary" @click="handleSubmit">提交</el-button>
        <el-button @click="handleClose">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { startProcess } from '@/api/flowable/index'
import { getForm } from '@/api/flowable/form'
import { getProcessFormByKey } from '@/api/flowable/processForm'
import FormComponentRenderer from '@/views/flowable/form/FormComponentRenderer.vue'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  processKey: {
    type: String,
    default: ''
  },
  processName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['close'])

const dialogVisible = ref(false)
const previewVisible = ref(false)
const previewTitle = ref('')
const previewUrl = ref('')
const previewType = ref('')
const previewHtml = ref('')
const formRef = ref(null)
const formData = reactive({})
const formComponents = ref([])
const startEventConfig = ref({})
const attachments = ref([])
const uploadFileList = ref([])

const uploadUrl = '/common/upload'

const showSaveDraft = computed(() => startEventConfig.value.buttons?.includes('saveDraft'))
const showClear = computed(() => startEventConfig.value.buttons?.includes('clear'))
const showSubmit = computed(() => true)

const visibleComponents = computed(() => {
  const { writeFields = [], readonlyFields = [], hideFields = [] } = startEventConfig.value
  
  return formComponents.value.map(comp => {
    const field = comp.field || comp.id?.toString()
    const fullField = comp.label ? `${comp.label}-${field}` : field
    let disabled = false
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
  if (val && props.processKey) {
    initForm()
  }
})

async function initForm() {
  Object.keys(formData).forEach(key => delete formData[key])
  formComponents.value = []
  startEventConfig.value = {}
  attachments.value = []
  uploadFileList.value = []

  try {
    const formRes = await getProcessFormByKey(props.processKey)
    if (formRes.data && formRes.data.formId) {
      const formDataRes = await getForm(formRes.data.formId)
      if (formDataRes.data && formDataRes.data.formJson) {
        const components = JSON.parse(formDataRes.data.formJson)
        formComponents.value = components

        components.forEach(comp => {
          if (comp.field && comp.defaultValue !== undefined) {
            formData[comp.field] = comp.defaultValue
          }
          if (comp.type === 'table' && comp.children) {
            comp.children.forEach(child => {
              if (child.field && child.defaultValue !== undefined) {
                formData[child.field] = child.defaultValue
              }
            })
          }
        })
      }
    }

    const processXmlRes = await request({
      url: `/flowable/process/xml/${props.processKey}`,
      method: 'get'
    })
    if (processXmlRes.data && processXmlRes.data.xml) {
      const xml = processXmlRes.data.xml
      const configMatch = xml.match(/flowable:startEventConfig="([^"]*)"/)
      if (configMatch) {
        startEventConfig.value = JSON.parse(configMatch[1].replace(/&#34;/g, '"').replace(/&quot;/g, '"'))
      }
    }

    const draftRes = await request({
      url: `/flowable/draft/getByProcessKey/${props.processKey}`,
      method: 'get'
    })
    if (draftRes.data) {
      const draft = draftRes.data
      if (draft.formData) {
        Object.assign(formData, JSON.parse(draft.formData))
      }
      if (draft.attachments) {
        attachments.value = JSON.parse(draft.attachments)
      }
    }
  } catch (e) {
    console.error('初始化表单失败', e)
  }
}

function handleFieldChange(field, value) {
  formData[field] = value
}

async function handleCustomUpload(options) {
  const { file, onSuccess, onError } = options
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await request({
      url: '/common/upload',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    if (response.code === 200) {
      attachments.value.push({
        fileName: file.name,
        filePath: response.fileName,
        fileSize: file.size,
        fileType: file.type
      })
      onSuccess(response, file)
      ElMessage.success('上传成功')
    } else {
      onError(response.msg || '上传失败')
      ElMessage.error(response.msg || '上传失败')
    }
  } catch (e) {
    onError(e)
    ElMessage.error('上传失败')
  }
}

function handleUploadSuccess(response, file, fileList) {
}

function handleUploadError(err, file, fileList) {
  ElMessage.error('上传失败')
}

function handleRemove(file, fileList) {
  const index = attachments.value.findIndex(a => a.fileName === file.name)
  if (index > -1) {
    attachments.value.splice(index, 1)
  }
}

function removeAttachment(index) {
  attachments.value.splice(index, 1)
}

function formatFileSize(size) {
  if (!size) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}

async function handleSubmit() {
  try {
    const variables = { ...formData }
    const result = await startProcess({
      processDefinitionKey: props.processKey,
      businessKey: '',
      variables: variables,
      attachments: attachments.value
    })
    
    ElMessage.success('提交成功')
    dialogVisible.value = false
    emit('close')
  } catch (e) {
    ElMessage.error('提交失败')
  }
}

async function handleSaveDraft() {
  try {
    await request({
      url: '/flowable/draft/save',
      method: 'post',
      data: {
        processKey: props.processKey,
        processName: props.processName,
        formData: JSON.stringify(formData),
        attachments: JSON.stringify(attachments.value)
      }
    })
    ElMessage.success('草稿保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

function handleClear() {
  Object.keys(formData).forEach(key => {
    const comp = formComponents.value.find(c => c.field === key)
    if (comp) {
      formData[key] = comp.defaultValue !== undefined ? comp.defaultValue : ''
    } else {
      formData[key] = ''
    }
  })
  attachments.value = []
  uploadFileList.value = []
}

function handleClose() {
  dialogVisible.value = false
  emit('close')
}

function isPreviewable(fileName) {
  if (!fileName) return false
  const lowerName = fileName.toLowerCase()
  return lowerName.endsWith('.doc') || lowerName.endsWith('.docx') ||
         lowerName.endsWith('.pdf') || lowerName.endsWith('.xls') ||
         lowerName.endsWith('.xlsx') || lowerName.endsWith('.jpg') ||
         lowerName.endsWith('.jpeg') || lowerName.endsWith('.png')
}

function getPreviewType(fileName) {
  if (!fileName) return 'download'
  const lowerName = fileName.toLowerCase()
  if (lowerName.endsWith('.doc') || lowerName.endsWith('.docx') ||
      lowerName.endsWith('.xls') || lowerName.endsWith('.xlsx')) {
    return 'html'
  } else if (lowerName.endsWith('.pdf')) {
    return 'pdf'
  } else if (lowerName.endsWith('.jpg') || lowerName.endsWith('.jpeg') ||
             lowerName.endsWith('.png')) {
    return 'image'
  }
  return 'download'
}

async function previewAttachment(attachment) {
  previewTitle.value = attachment.fileName
  previewType.value = getPreviewType(attachment.fileName)
  previewVisible.value = true

  const filePath = attachment.filePath.startsWith('/') ? attachment.filePath : '/profile/' + attachment.filePath

  if (previewType.value === 'image') {
    previewUrl.value = filePath
  } else if (previewType.value === 'pdf') {
    if (attachment.id) {
      previewUrl.value = '/dev-api/flowable/attachment/preview/' + attachment.id
    } else {
      const params = new URLSearchParams({ filePath: attachment.filePath, fileName: attachment.fileName })
      previewUrl.value = '/dev-api/flowable/attachment/previewByPath?' + params.toString()
    }
  } else if (previewType.value === 'html') {
    if (attachment.id) {
      const response = await fetch('/dev-api/flowable/attachment/preview/' + attachment.id)
      previewHtml.value = await response.text()
    } else {
      const params = new URLSearchParams({ filePath: attachment.filePath, fileName: attachment.fileName })
      const response = await fetch('/dev-api/flowable/attachment/previewByPath?' + params.toString())
      previewHtml.value = await response.text()
    }
  }
}

function downloadAttachment(attachment) {
  if (attachment.id) {
    window.location.href = '/dev-api/flowable/attachment/download/' + attachment.id
  } else {
    const params = new URLSearchParams({ filePath: attachment.filePath, fileName: attachment.fileName })
    window.location.href = '/dev-api/flowable/attachment/downloadByPath?' + params.toString()
  }
}
</script>

<style scoped>
.section-title {
  font-weight: bold;
  margin-bottom: 10px;
  font-size: 14px;
}

.attachment-section {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebf0f5;
}

.attachment-upload {
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
</style>