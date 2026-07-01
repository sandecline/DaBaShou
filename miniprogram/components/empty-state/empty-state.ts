Component({
  properties: {
    text: {
      type: String,
      value: '暂无数据',
    },
    icon: {
      type: String,
      value: 'inbox',
    },
    showAction: {
      type: Boolean,
      value: false,
    },
    actionText: {
      type: String,
      value: '去发布',
    },
  },

  methods: {
    onAction() {
      this.triggerEvent('action');
    },
  },
});
