<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="指令" prop="command">
        <el-input
          v-model="queryParams.command"
          placeholder="请输入指令"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="指令类型" prop="commandType">
        <el-select v-model="queryParams.commandType" placeholder="请选择" clearable style="width: 150px">
          <el-option label="查看" value="view" />
          <el-option label="新增" value="add" />
          <el-option label="编辑" value="edit" />
          <el-option label="删除" value="delete" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 150px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:aiCommand:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:aiCommand:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:aiCommand:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="aiCommandList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="id" width="80" />
      <el-table-column label="指令" align="center" prop="command" width="150" />
      <el-table-column label="指令类型" align="center" prop="commandType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.commandType === 'view'" type="info">查看</el-tag>
          <el-tag v-else-if="scope.row.commandType === 'add'" type="success">新增</el-tag>
          <el-tag v-else-if="scope.row.commandType === 'edit'" type="warning">编辑</el-tag>
          <el-tag v-else-if="scope.row.commandType === 'delete'" type="danger">删除</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="菜单路径" align="center" prop="menuPath" width="200" :show-overflow-tooltip="true" />
      <el-table-column label="菜单名称" align="center" prop="menuName" width="120" />
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:aiCommand:edit']"
          >修改</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:aiCommand:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改AI指令映射对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="指令" prop="command">
          <el-input v-model="form.command" placeholder="请输入指令，如：用户列表" />
        </el-form-item>
        <el-form-item label="指令类型" prop="commandType">
          <el-select v-model="form.commandType" placeholder="请选择指令类型">
            <el-option label="查看" value="view" />
            <el-option label="新增" value="add" />
            <el-option label="编辑" value="edit" />
            <el-option label="删除" value="delete" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单路径" prop="menuPath">
          <el-input v-model="form.menuPath" placeholder="请输入菜单路径，如：system/user/index" />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AiCommand">
import { list, add, edit, del } from '@/api/system/aiCommand'

const { proxy } = getCurrentInstance()

const aiCommandList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const title = ref('')
const total = ref(0)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    command: null,
    commandType: null,
    menuPath: null,
    status: null
  },
  rules: {
    command: [{ required: true, message: '指令不能为空', trigger: 'blur' }],
    commandType: [{ required: true, message: '指令类型不能为空', trigger: 'change' }],
    menuPath: [{ required: true, message: '菜单路径不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  list(queryParams.value).then(response => {
    aiCommandList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    id: null,
    command: null,
    commandType: null,
    menuPath: null,
    menuName: null,
    sort: 0,
    status: '0',
    remark: null
  }
  proxy.resetForm('formRef')
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加AI指令映射'
}

function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value[0]
  aiCommandList.value.filter(v => v.id === _id).forEach(v => {
    form.value = {
      id: v.id,
      command: v.command,
      commandType: v.commandType,
      menuPath: v.menuPath,
      menuName: v.menuName,
      sort: v.sort,
      status: v.status,
      remark: v.remark
    }
  })
  open.value = true
  title.value = '修改AI指令映射'
}

function handleStatusChange(row) {
  let text = row.status === '0' ? '启用' : '停用'
  proxy.$modal.confirm('确认要"' + text + '""' + row.command + '"吗？').then(function() {
    return edit(row)
  }).then(() => {
    proxy.$modal.msgSuccess(text + '成功')
  }).catch(function() {
    row.status = row.status === '0' ? '1' : '0'
  })
}

function submitForm() {
  proxy.$refs['formRef'].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        edit(form.value).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        add(form.value).then(response => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除AI指令映射编号为"' + _ids + '"的数据项？').then(function() {
    return del(row.id || _ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('system/aiCommand/export', {
    ...queryParams.value
  }, `aiCommand_${new Date().getTime()}.xlsx`)
}

getList()
</script>
