<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useMediaQuery } from '@vueuse/core'
import {
  Compass,
  HomeFilled,
  Location,
  ForkSpoon,
  EditPen,
  Setting,
  User,
  SwitchButton,
  Star,
  Menu as MenuIcon,
} from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const active = computed(() => route.path)
const isMobile = useMediaQuery('(max-width: 980px)')
const drawerOpen = ref(false)
const sidebarExpanded = ref(false)
const desktopCollapsed = computed(() => !isMobile.value && !sidebarExpanded.value)

async function logout() {
  await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
  auth.clear()
  router.push('/home')
}

function onSidebarEnter() {
  if (!isMobile.value) sidebarExpanded.value = true
}

function onSidebarLeave() {
  if (!isMobile.value) sidebarExpanded.value = false
}
</script>

<template>
  <div class="shell" :class="{ collapsed: desktopCollapsed }">
    <aside v-if="!isMobile" class="sidebar glass" @mouseenter="onSidebarEnter" @mouseleave="onSidebarLeave">
      <div class="brand">
        <div class="logo" role="button" tabindex="0" @click="$router.push('/home')" />
        <div class="name" :class="{ hiddenText: desktopCollapsed }">
          <div class="title">Travel System</div>
          <div class="sub muted">旅游 · 路线 · 日记 · 美食</div>
        </div>
      </div>

      <el-menu :default-active="active" router class="menu" background-color="transparent">
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span :class="{ hiddenText: desktopCollapsed }">首页</span>
        </el-menu-item>
        <el-menu-item index="/recommend">
          <el-icon><Compass /></el-icon>
          <span :class="{ hiddenText: desktopCollapsed }">推荐</span>
        </el-menu-item>
        <el-menu-item index="/route">
          <el-icon><Location /></el-icon>
          <span :class="{ hiddenText: desktopCollapsed }">路线</span>
        </el-menu-item>
        <el-menu-item index="/facility">
          <el-icon><Setting /></el-icon>
          <span :class="{ hiddenText: desktopCollapsed }">设施</span>
        </el-menu-item>
        <el-menu-item index="/food">
          <el-icon><ForkSpoon /></el-icon>
          <span :class="{ hiddenText: desktopCollapsed }">美食</span>
        </el-menu-item>
        <el-menu-item index="/diary">
          <el-icon><EditPen /></el-icon>
          <span :class="{ hiddenText: desktopCollapsed }">日记</span>
        </el-menu-item>
        <el-menu-item v-if="auth.user?.role?.toUpperCase() === 'ADMIN'" index="/admin">
          <el-icon><Star /></el-icon>
          <span :class="{ hiddenText: desktopCollapsed }">管理</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <template v-if="auth.isAuthed">
          <el-button text class="footer-btn" @click="$router.push('/profile')">
            <el-icon><User /></el-icon>
            <span :class="{ hiddenText: desktopCollapsed }" style="margin-left: 6px">{{ auth.user?.username }}</span>
          </el-button>
          <el-button text class="footer-btn" @click="logout" title="退出" aria-label="退出">
            <el-icon><SwitchButton /></el-icon>
          </el-button>
        </template>
        <template v-else>
          <el-button type="primary" class="footer-btn" @click="$router.push('/login')">
            登录
          </el-button>
          <el-button class="footer-btn" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </aside>

    <el-drawer v-else v-model="drawerOpen" size="270px" direction="ltr">
      <aside class="sidebar glass">
        <div class="brand">
          <div class="logo" role="button" tabindex="0" @click="$router.push('/home'); drawerOpen=false" />
          <div class="name">
            <div class="title">Travel System</div>
            <div class="sub muted">导航与内容</div>
          </div>
        </div>
        <el-menu :default-active="active" router class="menu" background-color="transparent" @select="drawerOpen = false">
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/recommend">
            <el-icon><Compass /></el-icon>
            <span>推荐</span>
          </el-menu-item>
          <el-menu-item index="/route">
            <el-icon><Location /></el-icon>
            <span>路线</span>
          </el-menu-item>
          <el-menu-item index="/facility">
            <el-icon><Setting /></el-icon>
            <span>设施</span>
          </el-menu-item>
          <el-menu-item index="/food">
            <el-icon><ForkSpoon /></el-icon>
            <span>美食</span>
          </el-menu-item>
          <el-menu-item index="/diary">
            <el-icon><EditPen /></el-icon>
            <span>日记</span>
          </el-menu-item>
          <el-menu-item v-if="auth.user?.role?.toUpperCase() === 'ADMIN'" index="/admin">
            <el-icon><Star /></el-icon>
            <span>管理</span>
          </el-menu-item>
        </el-menu>

        <div class="sidebar-footer">
          <template v-if="auth.isAuthed">
            <el-button text class="footer-btn" @click="$router.push('/profile')">
              <el-icon><User /></el-icon>
              <span style="margin-left: 6px">{{ auth.user?.username }}</span>
            </el-button>
            <el-button text class="footer-btn" @click="logout" title="退出" aria-label="退出">
              <el-icon><SwitchButton /></el-icon>
            </el-button>
          </template>
          <template v-else>
            <el-button type="primary" class="footer-btn" @click="$router.push('/login')">登录</el-button>
            <el-button class="footer-btn" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </aside>
    </el-drawer>

    <main class="main">
      <div v-if="isMobile" class="mobile-topbar">
        <el-button text circle class="menuBtn" @click="drawerOpen = true" aria-label="Open menu">
          <el-icon><MenuIcon /></el-icon>
        </el-button>
      </div>

      <div class="content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<style scoped>
.shell {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 12px;
  padding: 12px;
  min-height: 100vh;
  box-sizing: border-box;
  background: var(--bg-base);
  transition: grid-template-columns 0.18s cubic-bezier(0.4, 0, 0.2, 1);
}

.shell.collapsed {
  grid-template-columns: 92px 1fr;
}

.sidebar {
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 28px);
  background: var(--bg-sidebar) !important;
  border: 1px solid var(--border-subtle) !important;
  overflow: hidden;
  transition: all 0.18s cubic-bezier(0.4, 0, 0.2, 1);
}

.brand {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px 10px 14px;
  min-height: 48px;
}

.logo {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  box-shadow: none;
  cursor: pointer;
}

.title {
  font-family: 'Tiempos Headline', Georgia, serif;
  font-weight: 500;
  letter-spacing: 0.2px;
  color: var(--text-primary);
}

.sub {
  font-size: 12px;
}

.menu {
  border-right: none;
  flex: 1;
  background: transparent;
}

.hiddenText {
  opacity: 0;
  width: 0;
  overflow: hidden;
  white-space: nowrap;
  transform: translateX(-4px);
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.shell:not(.collapsed) .hiddenText {
  opacity: 1;
  width: auto;
  transform: translateX(0);
}

.shell.collapsed .brand {
  justify-content: center;
}

.shell.collapsed .menu :deep(.el-menu-item) {
  justify-content: center;
  padding-inline: 0 !important;
}

.main {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 0;
}

.topbar {
  padding: 10px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-surface) !important;
  border: 1px solid var(--border-subtle) !important;
}

.sidebar-footer {
  padding: 10px 6px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: space-between;
}

.footer-btn {
  flex: 1;
  justify-content: center;
}

.mobile-topbar {
  display: flex;
  justify-content: flex-start;
  padding: 4px 2px 0;
}

.crumb {
  font-weight: 500;
  letter-spacing: 0.15px;
  color: var(--text-secondary);
}

.content {
  flex: 1;
  min-height: 0;
}

.left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.menuBtn {
  width: 40px;
  height: 40px;
}

@media (max-width: 980px) {
  .shell {
    grid-template-columns: 1fr;
    padding: 8px;
  }
  .sidebar {
    min-height: auto;
  }
}
</style>

