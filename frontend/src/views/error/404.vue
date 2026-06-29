<template>
  <div class="not-found-page flex-center">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1>页面不存在</h1>
      <p>你访问的页面可能已被移除或不存在</p>
      <el-button type="primary" size="large" round @click="$router.push('/')">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.not-found-page {
  min-height: 100vh;
  background: $color-bg;
}

.not-found-content {
  text-align: center;

  .error-code {
    font-size: 120px;
    font-weight: 800;
    color: $color-primary;
    opacity: 0.2;
    line-height: 1;
  }

  h1 {
    margin: 8px 0;
    font-size: $font-size-2xl;
    color: $color-text-primary;
  }

  p {
    color: $color-text-secondary;
    margin-bottom: $spacing-xl;
  }
}
</style>
