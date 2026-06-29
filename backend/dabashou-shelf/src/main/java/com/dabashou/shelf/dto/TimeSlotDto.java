package com.dabashou.shelf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TimeSlotDto {

    @NotNull(message = "日期不能为空")
    private java.time.LocalDate date;

    @NotNull(message = "开始时间不能为空")
    private java.time.LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private java.time.LocalTime endTime;

    public java.time.LocalDate getDate() { return date; }
    public void setDate(java.time.LocalDate date) { this.date = date; }
    public java.time.LocalTime getStartTime() { return startTime; }
    public void setStartTime(java.time.LocalTime startTime) { this.startTime = startTime; }
    public java.time.LocalTime getEndTime() { return endTime; }
    public void setEndTime(java.time.LocalTime endTime) { this.endTime = endTime; }
}
