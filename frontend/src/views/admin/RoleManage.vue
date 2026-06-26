<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get, post } from '@/api/request'
import { getAdminUsers } from '@/api/admin'
import { ElMessage } from 'element-plus'

const accounts = ref<any[]>([])
const users = ref<any[]>([])
const loading = ref(false)
const bindDialog = ref(false)
const grantDialog = ref(false)
const bindForm = ref({ staffAccountId: 0, roleCode: 'DOCTOR' })
const grantForm = ref({ userId: undefined as number | undefined, roleCode: 'DOCTOR' })

const roleOptions = [
  { label: '医生', value: 'DOCTOR' },
  { label: '运营', value: 'OPERATOR' },
  { label: '管理员', value: 'ADMIN' },
]

const grantRoleOptions = [
  { label: '医生', value: 'DOCTOR' },
  { label: '运营', value: 'OPERATOR' },
]

async function loadAccounts() {
  loading.value = true
  try {
    const res: any = await get('/admin/roles/accounts')
    accounts.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadUsers() {
  const res = await getAdminUsers({ pageNum: 1, pageSize: 200 })
  users.value = res.data?.list || []
}

function openBind(accountId: number) {
  bindForm.value = { staffAccountId: accountId, roleCode: 'DOCTOR' }
  bindDialog.value = true
}

function openGrant() {
  grantForm.value = { userId: undefined, roleCode: 'DOCTOR' }
  grantDialog.value = true
  loadUsers()
}

async function handleBind() {
  await post('/admin/roles/bind', bindForm.value)
  ElMessage.success('绑定成功')
  bindDialog.value = false
  loadAccounts()
}

async function handleGrant() {
  if (!grantForm.value.userId) {
    ElMessage.warning('请选择用户')
    return
  }
  await post('/admin/roles/grant-user-role', grantForm.value)
  ElMessage.success('身份已赋予')
  grantDialog.value = false
  loadAccounts()
}

async function handleUnbind(accountId: number, roleCode: string) {
  await post('/admin/roles/unbind', { staffAccountId: accountId, roleCode })
  ElMessage.success('解绑成功')
  loadAccounts()
}

onMounted(loadAccounts)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">ROLE MANAGE</span>
        <h2>角色管理</h2>
      </div>
      <el-button type="primary" @click="openGrant">赋予用户身份</el-button>
    </div>

    <section class="table-shell data-card" v-loading="loading">
      <el-table :data="accounts">
        <el-table-column prop="username" label="账号" width="140" />
        <el-table-column prop="displayName" label="姓名" width="140" />
        <el-table-column label="角色" min-width="260">
          <template #default="{ row }">
            <el-tag
              v-for="role in row.roles || []"
              :key="role"
              size="small"
              style="margin-right: 4px; cursor: pointer;"
              @click="handleUnbind(row.id, role)"
            >
              {{ role }}
            </el-tag>
            <el-button type="primary" link size="small" @click="openBind(row.id)">+ 绑定角色</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="bindDialog" title="绑定角色" width="400px">
      <el-form label-width="80px">
        <el-form-item label="角色">
          <el-select v-model="bindForm.roleCode" style="width: 100%;">
            <el-option v-for="r in roleOptions" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindDialog = false">取消</el-button>
        <el-button type="primary" @click="handleBind">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="grantDialog" title="赋予用户身份" width="460px">
      <el-form label-width="90px">
        <el-form-item label="用户">
          <el-select v-model="grantForm.userId" filterable style="width: 100%;">
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="`${user.name || user.userNo}（${user.userNo}）`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="身份">
          <el-select v-model="grantForm.roleCode" style="width: 100%;">
            <el-option v-for="r in grantRoleOptions" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="grantDialog = false">取消</el-button>
        <el-button type="primary" @click="handleGrant">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.table-shell { padding: 20px; }
</style>
