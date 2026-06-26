<template>
  <div class="component-renderer">
    <div v-if="component.type === 'input'" class="cell-form-item">
      <el-form-item :label="component.label" :required="component.required" label-width="auto">
        <el-input :placeholder="component.placeholder" :model-value="component.defaultValue" size="small" />
      </el-form-item>
    </div>
    
    <div v-else-if="component.type === 'textarea'" class="cell-form-item">
      <el-form-item :label="component.label" :required="component.required" label-width="auto">
        <el-input type="textarea" :placeholder="component.placeholder" :model-value="component.defaultValue" :rows="2" size="small" />
      </el-form-item>
    </div>
    
    <div v-else-if="component.type === 'radio'" class="cell-form-item">
      <el-form-item :label="component.label" :required="component.required" label-width="auto">
        <el-radio-group :model-value="component.defaultValue" size="small">
          <el-radio v-for="(opt, idx) in component.options" :key="idx" :label="opt">{{ opt }}</el-radio>
        </el-radio-group>
      </el-form-item>
    </div>
    
    <div v-else-if="component.type === 'checkbox'" class="cell-form-item">
      <el-form-item :label="component.label" :required="component.required" label-width="auto">
        <el-checkbox-group :model-value="component.defaultValue" size="small">
          <el-checkbox v-for="(opt, idx) in component.options" :key="idx" :label="opt">{{ opt }}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
    </div>
    
    <div v-else-if="component.type === 'select'" class="cell-form-item">
      <el-form-item :label="component.label" :required="component.required" label-width="auto">
        <el-select :placeholder="component.placeholder" :model-value="component.defaultValue" size="small" style="width: 100%;">
          <el-option v-for="(opt, idx) in component.options" :key="idx" :label="opt" :value="opt" />
        </el-select>
      </el-form-item>
    </div>
    
    <div v-else-if="component.type === 'date'" class="cell-form-item">
      <el-form-item :label="component.label" :required="component.required" label-width="auto">
        <el-date-picker type="date" :placeholder="component.placeholder" :model-value="component.defaultValue" size="small" style="width: 100%;" />
      </el-form-item>
    </div>
    
    <div v-else-if="component.type === 'user'" class="cell-form-item">
      <el-form-item :label="component.label" :required="component.required" label-width="auto">
        <FormUserSelect 
          v-model="component.defaultValue" 
          :placeholder="component.placeholder"
          :multiple="false"
        />
      </el-form-item>
    </div>
    
    <div v-else-if="component.type === 'users'" class="cell-form-item">
      <el-form-item :label="component.label" :required="component.required" label-width="auto">
        <FormUserSelect 
          v-model="component.defaultValue" 
          :placeholder="component.placeholder"
          :multiple="true"
        />
      </el-form-item>
    </div>
    
    <div v-else-if="component.type === 'roleUser'" class="cell-form-item">
      <el-form-item :label="component.label" :required="component.required" label-width="auto">
        <FormRoleUserSelect 
          v-model="component.defaultValue" 
          :placeholder="component.placeholder"
          :multiple="false"
        />
      </el-form-item>
    </div>
    
    <div v-else-if="component.type === 'roleUsers'" class="cell-form-item">
      <el-form-item :label="component.label" :required="component.required" label-width="auto">
        <FormRoleUserSelect 
          v-model="component.defaultValue" 
          :placeholder="component.placeholder"
          :multiple="true"
        />
      </el-form-item>
    </div>
    
    <div v-else-if="component.type === 'dept'" class="cell-form-item">
      <el-form-item :label="component.label" :required="component.required" label-width="auto">
        <FormDeptSelect 
          v-model="component.defaultValue" 
          :placeholder="component.placeholder"
          :multiple="false"
        />
      </el-form-item>
    </div>
    
    <div v-else-if="component.type === 'depts'" class="cell-form-item">
      <el-form-item :label="component.label" :required="component.required" label-width="auto">
        <FormDeptSelect 
          v-model="component.defaultValue" 
          :placeholder="component.placeholder"
          :multiple="true"
        />
      </el-form-item>
    </div>
    
    <div v-else-if="component.type === 'title'" class="render-title" :class="`align-${component.align || 'left'}`">
      <span 
        class="title-text" 
        :class="`style-${component.fontStyle || 'normal'}`"
        :style="titleStyle(component)"
      >{{ component.label || '标题' }}</span>
    </div>
    
    <div v-else class="render-default">
      <span>{{ component.label }}</span>
    </div>
  </div>
</template>

<script setup>
import FormUserSelect from './FormUserSelect.vue'
import FormDeptSelect from './FormDeptSelect.vue'

const props = defineProps({
  component: {
    type: Object,
    required: true
  }
})

function titleStyle(comp) {
  const style = {}
  if (comp.fontSize) {
    style.fontSize = comp.fontSize + 'px'
  }
  if (comp.fontFamily) {
    style.fontFamily = comp.fontFamily
  }
  return style
}
</script>

<style scoped>
.component-renderer {
  padding: 4px;
  width: 100%;
}

.cell-form-item {
  margin-bottom: 0;
}

.cell-form-item :deep(.el-form-item__label) {
  font-size: 12px;
  line-height: 28px;
}

.cell-form-item :deep(.el-form-item__content) {
  line-height: 28px;
}

.render-title {
  padding: 4px 0;
  width: 100%;
}

.render-title.align-left {
  text-align: left;
}

.render-title.align-center {
  text-align: center;
}

.render-title.align-right {
  text-align: right;
}

.render-title .title-text {
  color: #303133;
}

.render-title .style-normal {
  font-weight: normal;
  font-style: normal;
}

.render-title .style-bold {
  font-weight: bold;
  font-style: normal;
}

.render-title .style-italic {
  font-weight: normal;
  font-style: italic;
}

.render-title .style-bold-italic {
  font-weight: bold;
  font-style: italic;
}

.render-default {
  padding: 4px 0;
  color: #909399;
  font-size: 12px;
}
</style>
