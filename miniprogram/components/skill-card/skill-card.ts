Component({
  properties: {
    skill: {
      type: Object,
      value: null,
    },
  },

  data: {
    defaultImage: '',
  },

  methods: {
    onTap() {
      this.triggerEvent('tap', { skill: this.properties.skill });
    },
  },
});
