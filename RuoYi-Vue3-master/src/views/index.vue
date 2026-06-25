<template>
  <div class="ai-chat-container">
    <div class="chat-display-area">
      <div v-if="!currentView && chatHistory.length === 0" class="empty-state">
        <div class="empty-icon">
          <el-icon size="64">MessageSquare</el-icon>
        </div>
        <h2>AI 智能助手</h2>
        <p class="empty-desc">输入指令，让 AI 帮您完成系统操作</p>
        <div class="quick-commands">
          <span class="command-item" v-for="item in menuShortcuts" :key="item.id" @click="handleCommand(item.command)">
            <el-icon>{{ getIcon(item.commandType) }}</el-icon>
            {{ item.command }}
          </span>
        </div>
      </div>

      <div v-else-if="chatHistory.length > 0 && !currentView" class="history-list">
        <div class="history-item" v-for="(item, index) in chatHistory" :key="index">
          <div class="history-avatar">
            <el-icon>User</el-icon>
          </div>
          <div class="history-content">
            <div class="history-text">{{ item.command }}</div>
            <div class="history-time">{{ item.time }}</div>
          </div>
          <div class="history-action" @click="handleCommand(item.command)">
            <el-icon size="16">RefreshRight</el-icon>
          </div>
        </div>
      </div>

      <div v-else class="content-wrapper">
        <component :is="currentComponent" />
      </div>
    </div>

    <div class="chat-input-area">
      <div class="quick-tips">
        <span v-for="item in menuShortcuts" :key="item.id" @click="handleCommand(item.command)" class="tip-tag">{{ item.command }}</span>
      </div>
      <div class="input-panel">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="输入指令，如：用户列表、新增用户..."
          @keyup.enter.ctrl="handleSend"
          resize="none"
          class="ai-textarea"
        />
        <div class="input-actions">
          <el-button type="primary" @click="handleSend" :loading="loading" class="send-btn">发送</el-button>
          <el-button @click="clearContent" class="clear-btn">清空</el-button>
        </div>
      </div>
      <div class="shortcut-hint">
        <el-icon size="12">Keyboard</el-icon>
        <span>Ctrl + Enter 快速发送</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, shallowRef, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listAll, aiMatch } from '@/api/system/aiCommand'
import { loadView } from '@/store/modules/permission'

const inputText = ref('')
const currentView = ref('')
const currentComponent = shallowRef(null)
const loading = ref(false)
const chatHistory = ref([])
const commandMapList = ref([])
const menuShortcuts = ref([])

function getIcon(commandType) {
  const iconMap = {
    'view': 'View',
    'add': 'Plus',
    'edit': 'Edit',
    'delete': 'Delete'
  }
  return iconMap[commandType] || 'Menu'
}

onMounted(() => {
  const savedHistory = localStorage.getItem('aiChatHistory')
  if (savedHistory) {
    chatHistory.value = JSON.parse(savedHistory)
  }

  listAll().then(res => {
    if (res.code === 200) {
      commandMapList.value = res.data || []
      menuShortcuts.value = commandMapList.value.slice(0, 10)
    }
  }).catch(() => {
    console.error('加载指令映射失败')
  })
})

onUnmounted(() => {
  localStorage.setItem('aiChatHistory', JSON.stringify(chatHistory.value))
})

function handleSend() {
  const text = inputText.value.trim()
  if (!text) return
  handleCommand(text)
}

function handleCommand(command) {
  loading.value = true
  inputText.value = ''

  addToHistory(command)

  setTimeout(async () => {
    await parseCommand(command)
    loading.value = false
  }, 200)
}

function addToHistory(command) {
  const now = new Date()
  const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`

  const exists = chatHistory.value.find(h => h.command === command)
  if (exists) {
    exists.time = timeStr
  } else {
    chatHistory.value.unshift({ command, time: timeStr })
    if (chatHistory.value.length > 10) {
      chatHistory.value.pop()
    }
  }

  localStorage.setItem('aiChatHistory', JSON.stringify(chatHistory.value))
}

async function parseCommand(command) {
  const cmd = command.toLowerCase()

  if (cmd.includes('返回') || cmd.includes('首页') || cmd.includes('清空')) {
    clearContent()
    return
  }

  let targetCommand = null

  for (const item of commandMapList.value) {
    if (item.command.toLowerCase() === cmd || item.command === command) {
      targetCommand = item
      break
    }
  }

  if (!targetCommand) {
    for (const item of commandMapList.value) {
      const cmdName = (item.command || '').toLowerCase()
      if (cmdName.includes(cmd) || cmd.includes(cmdName)) {
        targetCommand = item
        break
      }
    }
  }

  // 调用后端AI语义匹配
  if (!targetCommand) {
    try {
      const res = await aiMatch(command)
      if (res.code === 200 && res.data) {
        targetCommand = res.data
      }
    } catch (e) {
      console.error('AI匹配失败:', e)
    }
  }

  if (targetCommand) {
    currentView.value = targetCommand.menuPath
    const loader = loadView(targetCommand.menuPath)
    if (loader) {
      const comp = await loader()
      currentComponent.value = comp.default || comp

      if (targetCommand.commandType === 'add') {
        setTimeout(() => {
          const btns = document.querySelectorAll('.el-button')
          for (const btn of btns) {
            const text = btn.textContent.trim()
            if (text.includes('新增') || text.includes('添加') || text.includes('创建')) {
              btn.click()
              break
            }
          }
        }, 1500)
      }
    } else {
      ElMessage({
        message: `无法找到页面: ${targetCommand.menuPath}`,
        type: 'error'
      })
    }
  } else {
    ElMessage({
      message: '未识别的指令，请尝试：用户列表、新增用户、角色管理等',
      type: 'warning'
    })
  }
}

function clearContent() {
  currentView.value = ''
  currentComponent.value = null
}
</script>

<style lang="scss" scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f7fa;
}

.chat-display-area {
  flex: 1;
  overflow: hidden;
  position: relative;
  padding-top: 50px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #606266;
  text-align: center;
  padding: 40px;
}

.empty-icon {
  color: #c0c4cc;
  margin-bottom: 20px;
}

.empty-state h2 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #303133;
}

.empty-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 40px;
}

.quick-commands {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.command-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #fff;
  border-radius: 25px;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.3s ease;

  &:hover {
    background: #ecf5ff;
    border-color: #b3d8ff;
    color: #409eff;
    transform: translateY(-2px);
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  }
}

.history-list {
  padding: 20px;
  overflow-y: auto;
  height: 100%;
}

.history-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.history-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.history-content {
  flex: 1;
  min-width: 0;
}

.history-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 6px;
  word-break: break-all;
}

.history-time {
  font-size: 12px;
  color: #c0c4cc;
}

.history-action {
  padding: 8px;
  color: #c0c4cc;
  cursor: pointer;
  transition: color 0.2s ease;

  &:hover {
    color: #409eff;
  }
}

.content-wrapper {
  height: 100%;
  background: #f5f7fa;
  overflow: auto;
  padding: 10px;
}

.chat-input-area {
  background: #fff;
  padding: 15px 20px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  border-top: 1px solid #e4e7ed;
}

.quick-tips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tip-tag {
  padding: 4px 12px;
  background: #f0f2f5;
  border-radius: 12px;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #ecf5ff;
    color: #409eff;
  }
}

.input-panel {
  display: flex;
  gap: 12px;
  align-items: center;
}

.ai-textarea {
  flex: 1;
  border-radius: 8px;
  min-height: 60px;
}

.input-actions {
  display: flex;
  flex-direction: row;
  gap: 8px;
}

.send-btn,
.clear-btn {
  border-radius: 8px;
  width: 70px;
  padding: 8px 10px;
  font-size: 13px;
  white-space: nowrap;
}

.shortcut-hint {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  color: #c0c4cc;
}
</style>