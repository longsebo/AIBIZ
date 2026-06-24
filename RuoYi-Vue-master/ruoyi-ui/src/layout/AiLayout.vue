<template>
  <div class="ai-layout-container">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'AiLayout'
}
</script>

<style lang="scss" scoped>
.ai-layout-container {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}
</style>