<template>
  <div class="form-component-renderer">
    <!-- 单行输入框 -->
    <el-form-item v-if="component.type === 'input'" :label="showLabel ? component.label : ''" :required="component.required" :label-width="labelWidth">
      <el-input :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" :placeholder="component.placeholder" :disabled="disabled" />
    </el-form-item>
    
    <!-- 多行输入框 -->
    <el-form-item v-if="component.type === 'textarea'" :label="showLabel ? component.label : ''" :required="component.required" :label-width="labelWidth">
      <el-input :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" type="textarea" :placeholder="component.placeholder" :rows="component.rows || 3" :disabled="disabled" />
    </el-form-item>
    
    <!-- 单选框 -->
    <el-form-item v-if="component.type === 'radio'" :label="showLabel ? component.label : ''" :required="component.required" :label-width="labelWidth">
      <el-radio-group :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" :disabled="disabled">
        <el-radio v-for="opt in component.options" :key="opt" :label="opt">{{ opt }}</el-radio>
      </el-radio-group>
    </el-form-item>
    
    <!-- 复选框 -->
    <el-form-item v-if="component.type === 'checkbox'" :label="showLabel ? component.label : ''" :required="component.required" :label-width="labelWidth">
      <el-checkbox-group :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" :disabled="disabled">
        <el-checkbox v-for="opt in component.options" :key="opt" :label="opt">{{ opt }}</el-checkbox>
      </el-checkbox-group>
    </el-form-item>
    
    <!-- 组合框 -->
    <el-form-item v-if="component.type === 'select'" :label="showLabel ? component.label : ''" :required="component.required" :label-width="labelWidth">
      <el-select :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" :placeholder="component.placeholder" style="width: 100%" :disabled="disabled">
        <el-option v-for="opt in component.options" :key="opt" :label="opt" :value="opt" />
      </el-select>
    </el-form-item>
    
    <!-- 日期 -->
    <el-form-item v-if="component.type === 'date'" :label="showLabel ? component.label : ''" :required="component.required" :label-width="labelWidth">
      <el-date-picker :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" :placeholder="component.placeholder" style="width: 100%" :disabled="disabled" />
    </el-form-item>
    
    <!-- 标题 -->
    <div v-if="component.type === 'title'" class="preview-title" :class="`align-${component.align || 'left'}`">
      <span 
        class="title-text" 
        :class="`style-${component.fontStyle || 'normal'}`"
        :style="titleStyle(component)"
      >{{ component.label || '标题' }}</span>
    </div>
    
    <!-- 单选用户 -->
    <el-form-item v-if="component.type === 'user'" :label="showLabel ? component.label : ''" :required="component.required" :label-width="labelWidth">
      <FormUserSelect :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" :placeholder="component.placeholder" :multiple="false" :disabled="disabled" />
    </el-form-item>
    
    <!-- 多选用户 -->
    <el-form-item v-if="component.type === 'users'" :label="showLabel ? component.label : ''" :required="component.required" :label-width="labelWidth">
      <FormUserSelect :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" :placeholder="component.placeholder" :multiple="true" :disabled="disabled" />
    </el-form-item>
    
    <!-- 按角色选用户 -->
    <el-form-item v-if="component.type === 'roleUser'" :label="showLabel ? component.label : ''" :required="component.required" :label-width="labelWidth">
      <FormRoleUserSelect :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" :placeholder="component.placeholder" :multiple="false" :disabled="disabled" />
    </el-form-item>
    
    <!-- 按角色多选用户 -->
    <el-form-item v-if="component.type === 'roleUsers'" :label="showLabel ? component.label : ''" :required="component.required" :label-width="labelWidth">
      <FormRoleUserSelect :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" :placeholder="component.placeholder" :multiple="true" :disabled="disabled" />
    </el-form-item>
    
    <!-- 单选部门 -->
    <el-form-item v-if="component.type === 'dept'" :label="showLabel ? component.label : ''" :required="component.required" :label-width="labelWidth">
      <FormDeptSelect :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" :placeholder="component.placeholder" :multiple="false" :disabled="disabled" />
    </el-form-item>
    
    <!-- 多选部门 -->
    <el-form-item v-if="component.type === 'depts'" :label="showLabel ? component.label : ''" :required="component.required" :label-width="labelWidth">
      <FormDeptSelect :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" :placeholder="component.placeholder" :multiple="true" :disabled="disabled" />
    </el-form-item>
    
    <!-- 表格组件 -->
    <el-form-item v-if="component.type === 'dataTable'" :label="showLabel ? component.label : ''" :label-width="labelWidth">
      <FormDataTable 
        :columns="component.columns"
        :data="Array.isArray(modelValue) ? modelValue : []"
        @update:data="emit('update:modelValue', $event)"
        :border="component.border"
        :stripe="component.stripe"
        :size="component.size || 'small'"
        :height="component.height"
        :editable="disabled ? false : component.editable"
        :show-actions="disabled ? false : component.showActions"
      />
    </el-form-item>
  </div>
</template>

<script setup>
import FormUserSelect from './FormUserSelect.vue'
import FormRoleUserSelect from './FormRoleUserSelect.vue'
import FormDeptSelect from './FormDeptSelect.vue'
import FormDataTable from './FormDataTable.vue'

const props = defineProps({
  component: {
    type: Object,
    required: true
  },
  modelValue: {
    type: [String, Number, Array, Object],
    default: ''
  },
  showLabel: {
    type: Boolean,
    default: true
  },
  labelWidth: {
    type: String,
    default: '120px'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

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
.form-component-renderer {
  width: 100%;
}

.preview-title {
  padding: 10px 0;
  margin-bottom: 10px;
}

.preview-title.align-left {
  text-align: left;
}

.preview-title.align-center {
  text-align: center;
}

.preview-title.align-right {
  text-align: right;
}

.preview-title .title-text {
  display: inline-block;
}

.preview-title .style-normal {
  font-weight: normal;
  font-style: normal;
}

.preview-title .style-bold {
  font-weight: bold;
  font-style: normal;
}

.preview-title .style-italic {
  font-weight: normal;
  font-style: italic;
}

.preview-title .style-bold-italic {
  font-weight: bold;
  font-style: italic;
}
</style>