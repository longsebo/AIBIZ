<template>
  <div class="property-panel-content">
    <el-form label-width="80px" size="small">
      <el-form-item label="组件类型">
        <el-input :value="componentTypeLabel" disabled />
      </el-form-item>
      
      <el-form-item label="标签名称">
        <el-input v-model="localLabel" @input="updateProp('label', localLabel)" />
      </el-form-item>
      
      <el-form-item label="字段名称" v-if="hasField">
        <el-input v-model="localField" @input="updateProp('field', localField)" />
      </el-form-item>
      
      <el-form-item label="必填" v-if="hasRequired">
        <el-switch v-model="localRequired" @change="updateProp('required', localRequired)" />
      </el-form-item>
      
      <el-form-item label="占位提示" v-if="hasPlaceholder">
        <el-input v-model="localPlaceholder" @input="updateProp('placeholder', localPlaceholder)" />
      </el-form-item>
      
      <el-form-item label="默认值" v-if="hasDefaultValue">
        <el-input v-model="localDefaultValue" @input="updateProp('defaultValue', localDefaultValue)" />
      </el-form-item>
      
      <el-form-item label="选项列表" v-if="hasOptions">
        <div class="options-editor">
          <div v-for="(opt, idx) in localOptions" :key="idx" class="option-item">
            <el-input v-model="localOptions[idx]" @input="updateProp('options', localOptions)" />
            <el-button size="small" type="danger" @click="removeOption(idx)">删除</el-button>
          </div>
          <el-button size="small" type="primary" @click="addOption">添加选项</el-button>
        </div>
      </el-form-item>
      
      <el-form-item label="字体风格" v-if="component.type === 'title'">
        <el-select v-model="localFontStyle" @change="updateProp('fontStyle', localFontStyle)">
          <el-option label="正常" value="normal" />
          <el-option label="加粗" value="bold" />
          <el-option label="斜体" value="italic" />
          <el-option label="加粗斜体" value="bold-italic" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="字体名称" v-if="component.type === 'title'">
        <el-select v-model="localFontFamily" @change="updateProp('fontFamily', localFontFamily)">
          <el-option label="默认" value="" />
          <el-option label="宋体" value="SimSun" />
          <el-option label="黑体" value="SimHei" />
          <el-option label="微软雅黑" value="Microsoft YaHei" />
          <el-option label="楷体" value="KaiTi" />
          <el-option label="仿宋" value="FangSong" />
          <el-option label="Arial" value="Arial" />
          <el-option label="Times New Roman" value="Times New Roman" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="字体大小" v-if="component.type === 'title'">
        <el-input-number v-model="localFontSize" :min="12" :max="72" :step="1" @change="updateProp('fontSize', localFontSize)" />
        <span style="margin-left: 8px; color: #999;">px</span>
      </el-form-item>
      
      <el-form-item label="对齐方式" v-if="component.type === 'title'">
        <el-radio-group v-model="localAlign" @change="updateProp('align', localAlign)">
          <el-radio-button label="left">居左</el-radio-button>
          <el-radio-button label="center">居中</el-radio-button>
          <el-radio-button label="right">居右</el-radio-button>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="表格行数" v-if="component.type === 'table'">
        <el-input-number v-model="localRows" :min="1" :max="10" @change="updateProp('rows', localRows)" />
      </el-form-item>
      
      <el-form-item label="表格列数" v-if="component.type === 'table'">
        <el-input-number v-model="localCols" :min="1" :max="10" @change="updateProp('cols', localCols)" />
      </el-form-item>
      
      <el-form-item>
        <el-button type="danger" @click="emit('delete')">删除组件</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  component: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update', 'delete'])

const localLabel = ref(props.component.label)
const localField = ref(props.component.field || '')
const localRequired = ref(props.component.required || false)
const localPlaceholder = ref(props.component.placeholder || '')
const localDefaultValue = ref(props.component.defaultValue || '')
const localOptions = ref(props.component.options ? [...props.component.options] : [])
const localFontStyle = ref(props.component.fontStyle || 'normal')
const localFontFamily = ref(props.component.fontFamily || '')
const localFontSize = ref(props.component.fontSize || 18)
const localAlign = ref(props.component.align || 'left')
const localRows = ref(props.component.rows || 2)
const localCols = ref(props.component.cols || 2)

const componentTypeLabel = computed(() => {
  const labels = {
    input: '单行输入框',
    textarea: '多行输入框',
    radio: '单选框',
    checkbox: '复选框',
    select: '组合框',
    title: '标题',
    date: '日期',
    user: '单选用户',
    users: '多选用户',
    dept: '单选部门',
    depts: '多选部门',
    table: '表格布局'
  }
  return labels[props.component.type] || props.component.type
})

const hasField = computed(() => {
  return ['input', 'textarea', 'radio', 'checkbox', 'select', 'date', 'user', 'users', 'dept', 'depts'].includes(props.component.type)
})

const hasRequired = computed(() => {
  return ['input', 'textarea', 'radio', 'checkbox', 'select', 'date', 'user', 'users', 'dept', 'depts'].includes(props.component.type)
})

const hasPlaceholder = computed(() => {
  return ['input', 'textarea', 'date', 'user', 'users', 'dept', 'depts'].includes(props.component.type)
})

const hasDefaultValue = computed(() => {
  return ['input', 'textarea', 'date', 'user', 'users', 'dept', 'depts'].includes(props.component.type)
})

const hasOptions = computed(() => {
  return ['radio', 'checkbox', 'select'].includes(props.component.type)
})

watch(() => props.component, (newVal) => {
  localLabel.value = newVal.label
  localField.value = newVal.field || ''
  localRequired.value = newVal.required || false
  localPlaceholder.value = newVal.placeholder || ''
  localDefaultValue.value = newVal.defaultValue || ''
  localOptions.value = newVal.options ? [...newVal.options] : []
  localFontStyle.value = newVal.fontStyle || 'normal'
  localFontFamily.value = newVal.fontFamily || ''
  localFontSize.value = newVal.fontSize || 18
  localAlign.value = newVal.align || 'left'
  localRows.value = newVal.rows || 2
  localCols.value = newVal.cols || 2
}, { immediate: true, deep: true })

function updateProp(key, value) {
  emit('update', { [key]: value })
}

function addOption() {
  localOptions.value.push(`选项${localOptions.value.length + 1}`)
  updateProp('options', localOptions.value)
}

function removeOption(idx) {
  localOptions.value.splice(idx, 1)
  updateProp('options', localOptions.value)
}
</script>

<style scoped>
.property-panel-content {
  padding: 8px;
}

.options-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  gap: 8px;
  align-items: center;
}

.option-item .el-input {
  flex: 1;
}
</style>
