package com.dabashou.shelf.vo;

import java.time.LocalDate;
import java.time.LocalTime;

public class TimeSlotVo {

    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
