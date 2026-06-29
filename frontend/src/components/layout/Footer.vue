<template>
  <footer class="app-footer">
    <div class="footer-inner">
      <span class="footer-brand">🤝 搭把手</span>
      <span class="footer-desc">校园共享技能互助平台</span>
      <span class="footer-copy">&copy; 2026 DaBaShou</span>
    </div>
  </footer>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
.app-footer {
  background: #FFFFFF;
  border-top: 1px solid $color-border;
  padding: 16px 0;
  // 为底部导航留空间
  @media (max-width: 768px) {
    padding-bottom: calc(#{$bottom-nav-height} + 24px);
  }
}

.footer-inner {
  max-width: $max-width;
  margin: 0 auto;
  padding: 0 $spacing-md;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;

  .footer-brand {
    font-size: 13px;
    font-weight: 700;
    color: $color-primary-dark;
  }

  .footer-desc {
    font-size: 11px;
    color: $color-text-secondary;
  }

  .footer-copy {
    font-size: 11px;
    color: $color-text-placeholder;

    &::before {
      content: '·';
      margin-right: 12px;
    }
  }
}
</style>
