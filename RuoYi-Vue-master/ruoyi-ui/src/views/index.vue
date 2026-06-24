<template>
  <div class="ai-home-container">
    <div class="content-area">
      <div v-if="!currentView" class="welcome-screen">
        <div class="logo-area">
          <i class="el-icon-robot" style="font-size: 80px; margin-bottom: 20px;"></i>
          <h1>智能助手</h1>
          <p class="subtitle">通过自然语言指令管理您的系统</p>
        </div>
        <div class="quick-tips">
          <h3>尝试输入以下指令：</h3>
          <div class="tip-tags">
            <el-tag v-for="tip in quickTips" :key="tip" @click="handleInput(tip)" class="tip-tag">{{ tip }}</el-tag>
          </div>
        </div>
      </div>

      <div v-else-if="currentView === 'user-list'" class="embedded-view">
        <UserList />
      </div>

      <div v-else-if="currentView === 'role-list'" class="embedded-view">
        <RoleList />
      </div>

      <div v-else-if="currentView === 'dept-list'" class="embedded-view">
        <DeptList />
      </div>

      <div v-else-if="currentView === 'menu-list'" class="embedded-view">
        <MenuList />
      </div>

      <div v-else-if="currentView === 'dict-list'" class="embedded-view">
        <DictList />
      </div>

      <div v-else-if="currentView === 'log-list'" class="embedded-view">
        <LogList />
      </div>

      <div v-else-if="currentView === 'online-list'" class="embedded-view">
        <OnlineList />
      </div>

      <div v-else-if="currentView === 'cache-list'" class="embedded-view">
        <CacheList />
      </div>

      <div v-else-if="currentView === 'server-list'" class="embedded-view">
        <ServerList />
      </div>

      <div v-else-if="currentView === 'job-list'" class="embedded-view">
        <JobList />
      </div>
    </div>

    <div class="chat-input-area">
      <div class="input-wrapper">
        <el-input
          v-model="inputText"
          placeholder="输入指令，如：用户列表、新增用户、角色管理..."
          @keyup.enter.native="handleEnter"
          class="ai-input"
          size="large"
          clearable
        >
          <template slot="prepend">
            <i class="el-icon-message"></i>
          </template>
          <template slot="append">
            <el-button type="primary" @click="handleEnter" :loading="loading">
              <i class="el-icon-send"></i>
            </el-button>
          </template>
        </el-input>
      </div>
      <div class="history-tips">
        <span v-for="(history, index) in recentHistory" :key="index" @click="handleInput(history)" class="history-tag">{{ history }}</span>
      </div>
    </div>
  </div>
</template>

<script>
const UserList = () => import('@/views/system/user/index')
const RoleList = () => import('@/views/system/role/index')
const DeptList = () => import('@/views/system/dept/index')
const MenuList = () => import('@/views/system/menu/index')
const DictList = () => import('@/views/system/dict/index')
const LogList = () => import('@/views/monitor/operlog/index')
const OnlineList = () => import('@/views/monitor/online/index')
const CacheList = () => import('@/views/monitor/cache/index')
const ServerList = () => import('@/views/monitor/server/index')
const JobList = () => import('@/views/monitor/job/index')

export default {
  name: 'AIHome',
  components: {
    UserList,
    RoleList,
    DeptList,
    MenuList,
    DictList,
    LogList,
    OnlineList,
    CacheList,
    ServerList,
    JobList
  },
  data() {
    return {
      inputText: '',
      currentView: '',
      loading: false,
      quickTips: [
        '用户列表',
        '新增用户',
        '角色管理',
        '部门列表',
        '菜单管理',
        '操作日志',
        '在线用户',
        '系统监控',
        '定时任务',
        '缓存管理'
      ],
      recentHistory: []
    }
  },
  methods: {
    handleEnter() {
      const text = this.inputText.trim()
      if (!text) return
      this.handleInput(text)
    },
    handleInput(text) {
      this.loading = true
      this.inputText = ''
      
      if (this.recentHistory.indexOf(text) === -1) {
        this.recentHistory.unshift(text)
        if (this.recentHistory.length > 5) {
          this.recentHistory.pop()
        }
      }

      setTimeout(() => {
        this.parseCommand(text)
        this.loading = false
      }, 300)
    },
    parseCommand(command) {
      const cmd = command.toLowerCase()

      if ((cmd.includes('用户') && cmd.includes('列表')) || cmd === '用户列表' || cmd === '用户管理') {
        this.currentView = 'user-list'
      } else if (cmd.includes('用户') && (cmd.includes('新增') || cmd.includes('添加') || cmd.includes('创建'))) {
        this.currentView = 'user-list'
        setTimeout(() => {
          const btn = document.querySelector('.el-button.el-button--primary.el-button--plain[icon="el-icon-plus"]')
          if (btn) btn.click()
        }, 500)
      } else if (cmd.includes('角色') && (cmd.includes('列表') || cmd.includes('管理'))) {
        this.currentView = 'role-list'
      } else if (cmd.includes('角色') && (cmd.includes('新增') || cmd.includes('添加') || cmd.includes('创建'))) {
        this.currentView = 'role-list'
        setTimeout(() => {
          const btn = document.querySelector('.el-button.el-button--primary.el-button--plain[icon="el-icon-plus"]')
          if (btn) btn.click()
        }, 500)
      } else if (cmd.includes('部门') && (cmd.includes('列表') || cmd.includes('管理'))) {
        this.currentView = 'dept-list'
      } else if (cmd.includes('菜单') && (cmd.includes('列表') || cmd.includes('管理'))) {
        this.currentView = 'menu-list'
      } else if (cmd.includes('字典') && (cmd.includes('列表') || cmd.includes('管理'))) {
        this.currentView = 'dict-list'
      } else if (cmd.includes('日志') || cmd.includes('操作日志')) {
        this.currentView = 'log-list'
      } else if (cmd.includes('在线') && cmd.includes('用户')) {
        this.currentView = 'online-list'
      } else if (cmd.includes('缓存') && (cmd.includes('列表') || cmd.includes('管理'))) {
        this.currentView = 'cache-list'
      } else if (cmd.includes('监控') || cmd.includes('服务器')) {
        this.currentView = 'server-list'
      } else if (cmd.includes('任务') || cmd.includes('定时')) {
        this.currentView = 'job-list'
      } else if (cmd.includes('返回') || cmd.includes('首页') || cmd.includes('清空')) {
        this.currentView = ''
      } else {
        this.$message({
          message: '未识别的指令，请尝试：用户列表、新增用户、角色管理等',
          type: 'warning'
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-home-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.content-area {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.welcome-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #fff;
  text-align: center;
}

.logo-area {
  margin-bottom: 60px;
}

h1 {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 18px;
  opacity: 0.8;
}

.quick-tips {
  background: rgba(255, 255, 255, 0.15);
  padding: 30px 40px;
  border-radius: 16px;
  backdrop-filter: blur(10px);
}

.quick-tips h3 {
  font-size: 16px;
  margin-bottom: 20px;
  opacity: 0.9;
}

.tip-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}

.tip-tag {
  cursor: pointer;
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
  transition: all 0.3s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.35);
    transform: translateY(-2px);
  }
}

.embedded-view {
  height: 100%;
  background: #f0f2f5;
  overflow: auto;
  padding: 10px;
}

.chat-input-area {
  background: #fff;
  padding: 15px 20px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
}

.input-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

.ai-input {
  border-radius: 25px;
}

.history-tips {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
}

.history-tag {
  font-size: 12px;
  color: #909399;
  cursor: pointer;
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 12px;
  transition: all 0.2s ease;

  &:hover {
    background: #e4e7ed;
    color: #606266;
  }
}
</style>