<template>
  <div class="preview-renderer">
    <!-- 单行输入框 -->
    <el-form-item v-if="component.type === 'input'" :required="component.required" label-width="0" size="small">
      <el-input v-model="previewData[component.field]" :placeholder="component.placeholder" />
    </el-form-item>
    
    <!-- 多行输入框 -->
    <el-form-item v-if="component.type === 'textarea'" :required="component.required" label-width="0" size="small">
      <el-input v-model="previewData[component.field]" type="textarea" :placeholder="component.placeholder" :rows="2" />
    </el-form-item>
    
    <!-- 单选框 -->
    <el-form-item v-if="component.type === 'radio'" :required="component.required" label-width="0" size="small">
      <el-radio-group v-model="previewData[component.field]">
        <el-radio v-for="opt in component.options" :key="opt" :label="opt">{{ opt }}</el-radio>
      </el-radio-group>
    </el-form-item>
    
    <!-- 复选框 -->
    <el-form-item v-if="component.type === 'checkbox'" :required="component.required" label-width="0" size="small">
      <el-checkbox-group v-model="previewData[component.field]">
        <el-checkbox v-for="opt in component.options" :key="opt" :label="opt">{{ opt }}</el-checkbox>
      </el-checkbox-group>
    </el-form-item>
    
    <!-- 组合框 -->
    <el-form-item v-if="component.type === 'select'" :required="component.required" label-width="0" size="small">
      <el-select v-model="previewData[component.field]" :placeholder="component.placeholder">
        <el-option v-for="opt in component.options" :key="opt" :label="opt" :value="opt" />
      </el-select>
    </el-form-item>
    
    <!-- 日期 -->
    <el-form-item v-if="component.type === 'date'" :required="component.required" label-width="0" size="small">
      <el-date-picker v-model="previewData[component.field]" :placeholder="component.placeholder" />
    </el-form-item>
    
    <!-- 标题 -->
    <div v-if="component.type === 'title'" class="preview-cell-title" :class="`align-${component.align || 'left'}`">
      <span 
        class="title-text" 
        :class="`style-${component.fontStyle || 'normal'}`"
        :style="titleStyle(component)"
      >{{ component.label || '标题' }}</span>
    </div>
    
    <!-- 单选用户 -->
    <el-form-item v-if="component.type === 'user'" :required="component.required" label-width="0" size="small">
      <FormUserSelect 
        v-model="previewData[component.field]" 
        :placeholder="component.placeholder"
        :multiple="false"
      />
    </el-form-item>
    
    <!-- 多选用户 -->
    <el-form-item v-if="component.type === 'users'" :required="component.required" label-width="0" size="small">
      <FormUserSelect 
        v-model="previewData[component.field]" 
        :placeholder="component.placeholder"
        :multiple="true"
      />
    </el-form-item>
    
    <!-- 按角色选用户 -->
    <el-form-item v-if="component.type === 'roleUser'" :required="component.required" label-width="0" size="small">
      <FormRoleUserSelect 
        v-model="previewData[component.field]" 
        :placeholder="component.placeholder"
        :multiple="false"
      />
    </el-form-item>
    
    <!-- 按角色多选用户 -->
    <el-form-item v-if="component.type === 'roleUsers'" :required="component.required" label-width="0" size="small">
      <FormRoleUserSelect 
        v-model="previewData[component.field]" 
        :placeholder="component.placeholder"
        :multiple="true"
      />
    </el-form-item>
    
    <!-- 单选部门 -->
    <el-form-item v-if="component.type === 'dept'" :required="component.required" label-width="0" size="small">
      <FormDeptSelect 
        v-model="previewData[component.field]" 
        :placeholder="component.placeholder"
        :multiple="false"
      />
    </el-form-item>
    
    <!-- 多选部门 -->
    <el-form-item v-if="component.type === 'depts'" :required="component.required" label-width="0" size="small">
      <FormDeptSelect 
        v-model="previewData[component.field]" 
        :placeholder="component.placeholder"
        :multiple="true"
      />
    </el-form-item>
    
    <!-- 表格组件 -->
    <div v-if="component.type === 'dataTable'" class="preview-datatable">
      <FormDataTable 
        :columns="component.columns"
        v-model:data="previewData[component.field]"
        :border="component.border"
        :stripe="component.stripe"
        :size="component.size || 'small'"
        :height="component.height"
        :editable="component.editable"
        :show-actions="component.showActions"
      />
    </div>
    
    <!-- 默认 -->
    <div v-if="!hasRenderer" class="preview-default">
      <span>{{ component.label }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import FormUserSelect from './FormUserSelect.vue'
import FormRoleUserSelect from './FormRoleUserSelect.vue'
import FormDeptSelect from './FormDeptSelect.vue'
import FormDataTable from './FormDataTable.vue'

const props = defineProps({
  component: {
    type: Object,
    required: true
  },
  previewData: {
    type: Object,
    default: () => {}
  }
})

// 初始化默认值
if (props.component.field && props.component.defaultValue !== undefined) {
  props.previewData[props.component.field] = props.component.defaultValue
}

const hasRenderer = computed(() => {
  const types = ['input', 'textarea', 'radio', 'checkbox', 'select', 'date', 'title', 'user', 'users', 'roleUser', 'roleUsers', 'dept', 'depts', 'dataTable']
  return types.includes(props.component.type)
})

function titleStyle(comp) {
  const styles = {}
  if (comp.fontFamily) {
    styles.fontFamily = comp.fontFamily
  }
  if (comp.fontSize) {
    styles.fontSize = comp.fontSize + 'px'
  }
  return styles
}
</script>

<style scoped>
.preview-renderer {
  width: 100%;
}

.preview-cell-title {
  padding: 4px 0;
}

.preview-cell-title.align-left {
  text-align: left;
}

.preview-cell-title.align-center {
  text-align: center;
}

.preview-cell-title.align-right {
  text-align: right;
}

.preview-cell-title .title-text {
  display: inline-block;
}

.preview-cell-title .style-normal {
  font-weight: normal;
  font-style: normal;
}

.preview-cell-title .style-bold {
  font-weight: bold;
  font-style: normal;
}

.preview-cell-title .style-italic {
  font-weight: normal;
  font-style: italic;
}

.preview-cell-title .style-bold-italic {
  font-weight: bold;
  font-style: italic;
}

.preview-default {
  padding: 4px 0;
  color: #909399;
  font-size: 12px;
}
</style>