<template>
  <div class="es-auth-page">
    <div class="wrap">
      <div class="glass box">
        <div class="title">404</div>
        <div class="muted">页面不存在，可能是路由地址写错了。</div>
        <div style="margin-top: 14px">
          <el-button type="primary" @click="$router.push('/home')">回到首页</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.wrap {
  width: 100%;
  max-width: 720px;
}
.box {
  padding: 26px;
}
.title {
  font-size: 44px;
  font-weight: 800;
  letter-spacing: 1px;
  color: #16423c;
}
</style>
