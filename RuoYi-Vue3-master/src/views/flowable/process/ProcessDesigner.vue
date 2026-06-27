<template>
  <div class="process-designer">
    <div class="designer-header">
      <div class="header-left">
        <el-button icon="ArrowLeft" @click="goBack">返回</el-button>
        <span class="process-name">{{ processName }}</span>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Check" @click="saveProcess">保存</el-button>
      </div>
    </div>
    
    <div class="designer-body">
      <!-- 左侧工具栏 -->
      <div class="palette-panel">
        <div class="palette-title">工具栏</div>
        <div class="palette-items">
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'startEvent')">
            <span class="icon">▶</span>
            <span>开始事件</span>
          </div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'userTask')">
            <span class="icon">☐</span>
            <span>用户任务</span>
          </div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'serviceTask')">
            <span class="icon">⚙</span>
            <span>服务任务</span>
          </div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'exclusiveGateway')">
            <span class="icon">◇</span>
            <span>排他网关</span>
          </div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'parallelGateway')">
            <span class="icon">✚</span>
            <span>并行网关</span>
          </div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'endEvent')">
            <span class="icon">■</span>
            <span>结束事件</span>
          </div>
        </div>
      </div>
      
      <!-- 画布区域 -->
      <div class="canvas-container" ref="canvasRef"></div>
      
      <!-- 右侧属性面板 -->
      <div class="properties-panel">
        <div class="panel-title">属性</div>
        <div v-if="selectedElement" class="property-form">
          <el-form label-width="80px" size="small">
            <el-form-item label="节点ID">
              <el-input v-model="selectedElement.id" disabled />
            </el-form-item>
            <el-form-item label="节点名称">
              <el-input v-model="selectedElement.name" placeholder="请输入节点名称" @change="updateElementName" />
            </el-form-item>
            <el-form-item v-if="selectedElement.type === 'bpmn:UserTask'" label="审批人">
              <el-input v-model="selectedElement.assignee" placeholder="如: ${initiator}" @change="updateElementProperty('assignee', selectedElement.assignee)" />
            </el-form-item>
            <el-form-item v-if="selectedElement.type === 'bpmn:UserTask'" label="候选用户">
              <el-input v-model="selectedElement.candidateUsers" placeholder="多个用逗号分隔" @change="updateElementProperty('candidateUsers', selectedElement.candidateUsers)" />
            </el-form-item>
            <el-form-item v-if="selectedElement.type === 'bpmn:UserTask'" label="候选角色">
              <el-input v-model="selectedElement.candidateGroups" placeholder="多个用逗号分隔" @change="updateElementProperty('candidateGroups', selectedElement.candidateGroups)" />
            </el-form-item>
            <el-form-item label="描述">
              <el-input v-model="selectedElement.documentation" type="textarea" :rows="3" placeholder="请输入描述" @change="updateDocumentation" />
            </el-form-item>
          </el-form>
        </div>
        <div v-else class="no-selection">
          <p>请选择一个元素查看属性</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'
import { updateProcess } from '@/api/flowable'

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const canvasRef = ref(null)
const processName = ref('')
const selectedElement = ref(null)

let bpmnModeler = null

onMounted(() => {
  initBpmn()
})

onUnmounted(() => {
  if (bpmnModeler) {
    bpmnModeler.destroy()
  }
})

async function initBpmn() {
  // 获取流程key
  const processKey = route.params.key
  processName.value = route.query.name || processKey
  
  // 初始化BpmnModeler
  bpmnModeler = new BpmnModeler({
    container: canvasRef.value,
    keyboard: {
      bindTo: document
    }
  })
  
  // 加载或创建流程
  const xml = await loadProcessXml(processKey)
  await bpmnModeler.importXML(xml)
  
  // 监听选择事件
  const eventBus = bpmnModeler.get('eventBus')
  eventBus.on('selection.changed', (event) => {
    handleSelectionChanged(event)
  })
  
  // 监听节点变化事件
  eventBus.on('element.changed', (event) => {
    if (selectedElement.value && event.element && event.element.id === selectedElement.value.id) {
      selectedElement.value = {
        ...event.element.businessObject,
        type: event.element.type
      }
    }
  })
}

async function loadProcessXml(processKey) {
  try {
    // TODO: 从后端加载流程XML
    // 暂时返回空白流程
    return getEmptyBpmn(processKey)
  } catch (e) {
    return getEmptyBpmn(processKey)
  }
}

function getEmptyBpmn(key) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  id="Definitions_${key}"
  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="${key}" name="${processName.value}" isExecutable="true">
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${key}">
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`
}

function handleSelectionChanged(event) {
  const selected = event.elements[0]
  if (selected) {
    const elementRegistry = bpmnModeler.get('elementRegistry')
    const element = elementRegistry.get(selected)
    if (element && element.businessObject) {
      selectedElement.value = {
        ...element.businessObject,
        type: element.type
      }
    }
  } else {
    selectedElement.value = null
  }
}

function updateElementName() {
  if (!selectedElement.value || !selectedElement.value.id) return
  
  const modeling = bpmnModeler.get('modeling')
  const elementRegistry = bpmnModeler.get('elementRegistry')
  const element = elementRegistry.get(selectedElement.value.id)
  
  if (element) {
    modeling.updateProperties(element, {
      name: selectedElement.value.name
    })
  }
}

function updateElementProperty(prop, value) {
  if (!selectedElement.value || !selectedElement.value.id) return
  
  const modeling = bpmnModeler.get('modeling')
  const elementRegistry = bpmnModeler.get('elementRegistry')
  const element = elementRegistry.get(selectedElement.value.id)
  
  if (element) {
    modeling.updateProperties(element, {
      [prop]: value
    })
  }
}

function updateDocumentation() {
  if (!selectedElement.value || !selectedElement.value.id) return
  
  const modeling = bpmnModeler.get('modeling')
  const elementRegistry = bpmnModeler.get('elementRegistry')
  const element = elementRegistry.get(selectedElement.value.id)
  
  if (element) {
    modeling.updateProperties(element, {
      documentation: [{ text: selectedElement.value.documentation }]
    })
  }
}

function onDragStart(event, type) {
  event.dataTransfer.setData('data-node-type', type)
}

async function saveProcess() {
  try {
    const { xml } = await bpmnModeler.saveXML({ format: true })
    const processKey = route.params.key
    await updateProcess({
      key: processKey,
      name: processName.value,
      bpmnXml: xml
    })
    proxy.$modal.msgSuccess('保存成功')
  } catch (e) {
    console.error('保存失败:', e)
    proxy.$modal.msgError('保存失败')
  }
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.process-designer {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.designer-header {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.process-name {
  font-size: 16px;
  font-weight: 500;
}

.designer-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.palette-panel {
  width: 180px;
  border-right: 1px solid #e4e7ed;
  background: #fafafa;
  overflow-y: auto;
}

.palette-title {
  padding: 12px 16px;
  font-weight: 500;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
}

.palette-items {
  padding: 8px;
}

.palette-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  margin-bottom: 4px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: move;
  transition: all 0.2s;
}

.palette-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.palette-item .icon {
  font-size: 14px;
  color: #409eff;
}

.canvas-container {
  flex: 1;
  overflow: hidden;
}

.canvas-container :deep(.bjs-container) {
  width: 100%;
  height: 100%;
}

.properties-panel {
  width: 280px;
  border-left: 1px solid #e4e7ed;
  background: #fafafa;
  overflow-y: auto;
}

.panel-title {
  padding: 12px 16px;
  font-weight: 500;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
}

.property-form {
  padding: 16px;
}

.no-selection {
  padding: 40px 16px;
  text-align: center;
  color: #909399;
}

.no-selection p {
  margin: 0;
}
</style>