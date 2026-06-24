<template>
  <div :class="classObj" class="app-wrapper" :style="{ '--current-color': theme }">
    <div v-if="device === 'mobile' && sidebar.opened" class="drawer-bg" @click="handleClickOutside" />
    <sidebar v-if="!sidebar.hide" class="sidebar-container" />
    <div :class="{ sidebarHide: sidebar.hide }" class="main-container">
      <div class="ai-header">
        <hamburger :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
        <div class="right-menu">
          <div class="avatar-container">
            <el-dropdown @command="handleCommand" class="right-menu-item hover-effect" trigger="click">
              <div class="avatar-wrapper">
                <img :src="userStore.avatar" class="user-avatar" />
                <el-icon><caret-bottom /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <router-link to="/user/profile">
                    <el-dropdown-item>个人中心</el-dropdown-item>
                  </router-link>
                  <el-dropdown-item command="logout" divided>
                    <span>退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useWindowSize } from '@vueuse/core'
import Sidebar from './components/Sidebar/index.vue'
import Hamburger from '@/components/Hamburger'
import defaultSettings from '@/settings'
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'
import useUserStore from '@/store/modules/user'

const settingsStore = useSettingsStore()
const userStore = useUserStore()
const theme = computed(() => settingsStore.theme)
const sidebar = computed(() => useAppStore().sidebar)
const device = computed(() => useAppStore().device)

const classObj = computed(() => ({
  hideSidebar: !sidebar.value.opened,
  openSidebar: sidebar.value.opened,
  withoutAnimation: sidebar.value.withoutAnimation,
  mobile: device.value === 'mobile'
}))

const WIDTH = 992

const { width } = useWindowSize()

watchEffect(() => {
  if (device.value === 'mobile' && sidebar.value.opened) {
    useAppStore().closeSideBar({ withoutAnimation: false })
  }
  if (width.value - 1 < WIDTH) {
    useAppStore().toggleDevice('mobile')
    useAppStore().closeSideBar({ withoutAnimation: true })
  } else {
    useAppStore().toggleDevice('desktop')
  }
})

onMounted(() => {
  const savedSidebar = localStorage.getItem('aiSidebarStatus')
  if (savedSidebar === '0' || savedSidebar === null) {
    useAppStore().closeSideBar({ withoutAnimation: true })
  }
})

function toggleSideBar() {
  useAppStore().toggleSideBar()
  localStorage.setItem('aiSidebarStatus', useAppStore().sidebar.opened ? '1' : '0')
}

function handleClickOutside() {
  useAppStore().closeSideBar({ withoutAnimation: false })
  localStorage.setItem('aiSidebarStatus', '0')
}

function handleCommand(command) {
  switch (command) {
    case "logout":
      logout()
      break
    default:
      break
  }
}

function logout() {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/index'
    })
  }).catch(() => { })
}
</script>

<style lang="scss" scoped>
@import "@/assets/styles/mixin.scss";
@import "@/assets/styles/variables.module.scss";

.app-wrapper {
  @include clearfix;
  position: relative;
  height: 100%;
  width: 100%;

  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }
}

.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}

.main-container {
  min-height: 100%;
  transition: margin-left 0.28s;
  margin-left: #{$base-sidebar-width};
  position: relative;
}

.hideSidebar .main-container {
  margin-left: 54px;
}

.sidebarHide .main-container {
  margin-left: 0;
}

.mobile .main-container {
  margin-left: 0;
}

.ai-header {
  height: 50px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  position: fixed;
  top: 0;
  right: 0;
  left: #{$base-sidebar-width};
  z-index: 10;
  transition: left 0.28s;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-left: 0;
  padding-right: 20px;

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    padding: 0 15px;
    cursor: pointer;
    transition: background 0.3s;

    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }
  }
}

.hideSidebar .ai-header {
  left: 54px;
}

.sidebarHide .ai-header {
  left: 0;
}

.right-menu {
  display: flex;
  align-items: center;
}

.right-menu-item {
  display: inline-block;
  padding: 0 8px;
  height: 100%;
  font-size: 18px;
  color: #5a5e66;
  vertical-align: text-bottom;

  &.hover-effect {
    cursor: pointer;
    transition: background 0.3s;

    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }
  }
}

.avatar-container {
  .avatar-wrapper {
    margin-top: 5px;
    position: relative;

    .user-avatar {
      cursor: pointer;
      width: 40px;
      height: 40px;
      border-radius: 10px;
    }

    i {
      cursor: pointer;
      position: absolute;
      right: -20px;
      top: 25px;
      font-size: 12px;
    }
  }
}
</style>