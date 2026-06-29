<template>
  <div class="time-slot-picker">
    <div class="picker-header">
      <h4>选择空闲时段</h4>
      <div class="picker-actions">
        <el-button size="small" text @click="quickSetAll">全部设为空闲</el-button>
        <el-button size="small" text @click="clearAll">清空</el-button>
      </div>
    </div>

    <!-- 日期标签页 -->
    <el-tabs v-model="activeDay" type="card">
      <el-tab-pane
        v-for="day in days"
        :key="day.date"
        :label="day.label"
        :name="day.date"
      />
    </el-tabs>

    <!-- 时间段网格 -->
    <div class="slot-grid">
      <div
        v-for="slot in currentDaySlots"
        :key="slot.key"
        class="slot-cell"
        :class="{
          'slot-selected': slot.selected,
          'slot-past': slot.isPast,
        }"
        @click="!slot.isPast && toggleSlot(slot.key)"
      >
        <span class="slot-time">{{ slot.label }}</span>
        <el-icon v-if="slot.selected" class="slot-check"><Check /></el-icon>
      </div>
    </div>

    <div class="picker-tip">
      <span>已选择 {{ selectedCount }} 个时段</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import dayjs from 'dayjs'

const model = defineModel<Array<{ date: string; startTime: string; endTime: string }>>({
  default: () => [],
})

const activeDay = ref(dayjs().format('YYYY-MM-DD'))

// 生成未来7天
const days = computed(() => {
  return Array.from({ length: 7 }, (_, i) => {
    const d = dayjs().add(i, 'day')
    const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.day()]
    return {
      date: d.format('YYYY-MM-DD'),
      label: `${d.format('MM/DD')} ${i === 0 ? '今天' : i === 1 ? '明天' : weekDay}`,
    }
  })
})

// 时间段定义（每1小时一段，8:00-22:00）
const timeSegments = [
  '08:00-09:00', '09:00-10:00', '10:00-11:00', '11:00-12:00',
  '12:00-13:00', '13:00-14:00', '14:00-15:00', '15:00-16:00',
  '16:00-17:00', '17:00-18:00', '18:00-19:00', '19:00-20:00',
  '20:00-21:00', '21:00-22:00',
]

const currentDaySlots = computed(() => {
  const now = dayjs()
  const isToday = activeDay.value === now.format('YYYY-MM-DD')

  return timeSegments.map((seg) => {
    const [start, end] = seg.split('-')
    const isSelected = model.value.some(
      (s) => s.date === activeDay.value && s.startTime === start && s.endTime === end,
    )
    const isPast = isToday && dayjs(`${activeDay.value} ${start}`).isBefore(now)

    return {
      key: `${activeDay.value}_${seg}`,
      label: seg.replace('-', ' - '),
      date: activeDay.value,
      startTime: start,
      endTime: end,
      selected: isSelected,
      isPast,
    }
  })
})

const selectedCount = computed(() => model.value.length)

function toggleSlot(key: string) {
  const slot = currentDaySlots.value.find((s) => s.key === key)
  if (!slot) return

  const idx = model.value.findIndex(
    (s) => s.date === slot.date && s.startTime === slot.startTime && s.endTime === slot.endTime,
  )

  if (idx >= 0) {
    model.value.splice(idx, 1)
  } else {
    model.value.push({
      date: slot.date,
      startTime: slot.startTime,
      endTime: slot.endTime,
    })
  }
}

function quickSetAll() {
  const newSlots = currentDaySlots.value
    .filter((s) => !s.isPast && !s.selected)
    .map((s) => ({
      date: s.date,
      startTime: s.startTime,
      endTime: s.endTime,
    }))
  model.value.push(...newSlots)
}

function clearAll() {
  model.value = model.value.filter((s) => s.date !== activeDay.value)
}
</script>

<style scoped lang="scss">
.time-slot-picker {
  background: #ffffff;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $spacing-md;
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;

  h4 {
    margin: 0;
    font-size: $font-size-base;
    font-weight: 600;
  }
}

.slot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  gap: 8px;
}

.slot-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px;
  border: 1px solid $color-border;
  border-radius: 6px;
  cursor: pointer;
  font-size: $font-size-xs;
  color: $color-text-regular;
  transition: all 0.15s;

  .slot-time {
    font-variant-numeric: tabular-nums;
  }

  &:hover:not(.slot-past) {
    border-color: $color-primary;
    color: $color-primary;
  }

  &.slot-selected {
    background: #FFF8E1;
    border-color: $color-primary;
    color: $color-primary;
    font-weight: 500;
  }

  &.slot-past {
    background: $color-bg;
    color: $color-text-placeholder;
    cursor: not-allowed;
  }
}

.slot-check {
  color: $color-primary;
}

.picker-tip {
  margin-top: 8px;
  font-size: $font-size-xs;
  color: $color-text-secondary;
}
</style>
