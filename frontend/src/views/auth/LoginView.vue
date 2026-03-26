<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { apiLogin } from '../../lib/api'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
})

async function submit() {
  loading.value = true
  try {
    const data = await apiLogin({ username: form.username, password: form.password })
    auth.setAuth(data.token, data.user)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/home'
    router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth">
    <div class="panel glass">
      <div class="hero">
        <div class="h1">欢迎回来</div>
        <div class="muted">登录后可使用个性化推荐、评分、写日记等功能。</div>
      </div>

      <el-form label-position="top" class="form" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="submit">
          登录
        </el-button>
        <div class="muted links">
          没有账号？
          <a @click.prevent="$router.push('/register')">去注册</a>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.auth {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 18px;
  box-sizing: border-box;
}
.panel {
  width: min(980px, 100%);
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 16px;
  padding: 16px;
}
.hero {
  padding: 16px;
  border-radius: 16px;
  background:
    radial-gradient(420px 220px at 18% 15%, var(--accent-pink-2), transparent 60%),
    radial-gradient(360px 240px at 88% 26%, var(--accent-main-2), transparent 62%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.02));
  border: 1px solid rgba(255, 255, 255, 0.10);
}
.h1 {
  font-size: 28px;
  font-weight: 820;
  margin-bottom: 8px;
}
.form {
  padding: 14px;
}
.links {
  margin-top: 12px;
  font-size: 13px;
}
.links a {
  color: var(--accent-main);
  cursor: pointer;
  text-decoration: none;
}
@media (max-width: 860px) {
  .panel {
    grid-template-columns: 1fr;
  }
}
</style>

