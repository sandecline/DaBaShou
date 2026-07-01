Component({
  properties: {
    demand: {
      type: Object,
      value: null,
    },
  },

  methods: {
    onTap() {
      this.triggerEvent('tap', { demand: this.properties.demand });
    },
  },
});
