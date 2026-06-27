<template>
  <div class="component-renderer">
    <FormComponentRenderer 
      :component="component" 
      v-model="component.defaultValue"
      :show-label="false"
      label-width="0"
    />
  </div>
</template>

<script setup>
import FormComponentRenderer from './FormComponentRenderer.vue'

defineProps({
  component: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.component-renderer {
  width: 100%;
}
</style>