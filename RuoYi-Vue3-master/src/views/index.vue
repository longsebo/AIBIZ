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
              // 解析参数并自动填表
              setTimeout(() => {
                fillFormFromCommand(command, targetCommand.menuPath)
              }, 500)
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

/**
 * 从指令中解析参数并自动填表（通用方法）
 * 支持多种模块：用户、部门、角色、岗位等
 * 
 * 用户示例: 新增用户：张三  男  zhangsan  18  13800138000
 * 部门示例: 新增部门：研发部  张三  13800138000  排序1
 * 角色示例: 新增角色：管理员  admin  备注
 * 岗位示例: 新增岗位：工程师  10
 */
function fillFormFromCommand(command, menuPath) {
  // 提取冒号后的内容
  const colonMatch = command.match(/[：:]\s*(.+)/)
  if (!colonMatch) return

  const paramStr = colonMatch[1].trim()
  // 按空格分割参数
  const params = paramStr.split(/\s+/).filter(p => p)
  if (params.length === 0) return

  // 根据模块定义不同的解析规则
  const moduleRules = {
    'system/user/index': {
      // 字段识别顺序：手机号(11位数字) > 邮箱(含@) > 性别 > 排序 > 数字 > 字母(用户名) > 中文(姓名)
      name: '用户表单',
      fields: [
        // 邮箱
        { match: /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, field: 'email' },
        // 手机号
        { match: /^1[3-9]\d{9}$/, field: 'phonenumber' },
        // 性别
        { match: /^(男|女)$/, field: 'sex', transform: v => v === '男' ? '0' : '1' },
        // 纯数字
        { match: /^\d+$/, field: 'age' },
        // 字母开头（用户名）
        { match: /^[a-zA-Z][a-zA-Z0-9_]*$/, field: 'userName' },
        // 中文（昵称）
        { match: /^[\u4e00-\u9fa5]{2,}$/, field: 'nickName' }
      ]
    },
    'system/dept/index': {
      name: '部门表单',
      fields: [
        // 邮箱
        { match: /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, field: 'email' },
        // 手机号
        { match: /^1[3-9]\d{9}$/, field: 'phone' },
        // 排序
        { match: /^排序?\d+$/, field: 'orderNum', transform: v => v.replace(/[^\d]/g, '') },
        // 纯数字
        { match: /^\d+$/, field: 'orderNum' },
        // 中文（部门名称 / 负责人）
        { match: /^[\u4e00-\u9fa5]{2,}$/, field: 'deptName' }
      ]
    },
    'system/role/index': {
      name: '角色表单',
      fields: [
        // 字母（角色权限字符）
        { match: /^[a-zA-Z][a-zA-Z0-9_]*$/, field: 'roleKey' },
        // 中文（角色名称）
        { match: /^[\u4e00-\u9fa5]{2,}$/, field: 'roleName' }
      ]
    },
    'system/post/index': {
      name: '岗位表单',
      fields: [
        // 排序
        { match: /^排序?\d+$/, field: 'postSort', transform: v => v.replace(/[^\d]/g, '') },
        // 纯数字
        { match: /^\d+$/, field: 'postSort' },
        // 中文（岗位名称）
        { match: /^[\u4e00-\u9fa5]{2,}$/, field: 'postName' }
      ]
    }
  }

  // 找到对应模块的规则
  let rule = null
  for (const path in moduleRules) {
    if (menuPath && menuPath.includes(path)) {
      rule = moduleRules[path]
      break
    }
  }

  // 解析参数
  const fieldMappings = []
  const usedFields = new Set()

  for (const param of params) {
    if (!rule) break
    
    for (const fieldRule of rule.fields) {
      if (usedFields.has(fieldRule.field)) continue
      
      if (fieldRule.match.test(param)) {
        const value = fieldRule.transform ? fieldRule.transform(param) : param
        fieldMappings.push({ field: fieldRule.field, value: value })
        usedFields.add(fieldRule.field)
        break
      }
    }
  }

  // 自动填充表单
  fieldMappings.forEach(mapping => {
    setInputValue(mapping.field, mapping.value)
  })

  if (fieldMappings.length > 0) {
    ElMessage({
      message: `已自动填充 ${fieldMappings.length} 个字段`,
      type: 'success'
    })
  } else if (rule) {
    ElMessage({
      message: `${rule.name}未能解析出任何参数`,
      type: 'info'
    })
  }
}

/**
 * 设置Element Plus输入框/下拉框的值并触发v-model更新
 */
function setInputValue(fieldName, value) {
  // 通过label查找对应的输入框
  const formItems = document.querySelectorAll('.el-dialog .el-form-item')
  for (const item of formItems) {
    const label = item.querySelector('.el-form-item__label')
    if (!label) continue

    const labelText = label.textContent.trim()
    let targetField = null

    // 匹配label和字段
    if ((labelText.includes('用户名称') || labelText.includes('登录账号')) && fieldName === 'userName') {
      targetField = value
    } else if ((labelText.includes('用户昵称') || labelText.includes('昵称')) && fieldName === 'nickName') {
      targetField = value
    } else if (labelText.includes('性别') && fieldName === 'sex') {
      targetField = value
    } else if (labelText.includes('年龄') && fieldName === 'age') {
      targetField = value
    } else if (labelText.includes('手机') && fieldName === 'phonenumber') {
      targetField = value
    } else if (labelText.includes('邮箱') && fieldName === 'email') {
      targetField = value
    } else if (labelText.includes('部门名称') && fieldName === 'deptName') {
      targetField = value
    } else if (labelText.includes('负责人') && fieldName === 'leader') {
      targetField = value
    } else if ((labelText.includes('联系电话') || labelText.includes('电话')) && fieldName === 'phone') {
      targetField = value
    } else if (labelText.includes('显示排序') && fieldName === 'orderNum') {
      targetField = value
    } else if (labelText.includes('角色名称') && fieldName === 'roleName') {
      targetField = value
    } else if ((labelText.includes('权限字符') || labelText.includes('角色Key')) && fieldName === 'roleKey') {
      targetField = value
    } else if (labelText.includes('岗位名称') && fieldName === 'postName') {
      targetField = value
    } else if (labelText.includes('岗位排序') && fieldName === 'postSort') {
      targetField = value
    }

    if (targetField !== null) {
      // 处理普通输入框
      const input = item.querySelector('input')
      if (input && input.type !== 'hidden') {
        const nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set
        nativeInputValueSetter.call(input, targetField)
        input.dispatchEvent(new Event('input', { bubbles: true }))
        input.dispatchEvent(new Event('change', { bubbles: true }))
      }
      // 处理数字输入框(el-input-number)
      const numberInput = item.querySelector('.el-input-number input')
      if (numberInput && (fieldName === 'orderNum' || fieldName === 'postSort' || fieldName === 'age')) {
        const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set
        setter.call(numberInput, targetField)
        numberInput.dispatchEvent(new Event('input', { bubbles: true }))
        numberInput.dispatchEvent(new Event('change', { bubbles: true }))
      }
      // 处理下拉框(性别/部门状态等)
      const select = item.querySelector('.el-select')
      if (select && (fieldName === 'sex' || fieldName === 'status' || fieldName === 'parentId')) {
        select.click()
        setTimeout(() => {
          const options = document.querySelectorAll('.el-select-dropdown__item')
          for (const opt of options) {
            if (opt.textContent.trim() === value || opt.textContent.trim().includes(value)) {
              opt.click()
              break
            }
          }
        }, 200)
      }
    }
  }
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