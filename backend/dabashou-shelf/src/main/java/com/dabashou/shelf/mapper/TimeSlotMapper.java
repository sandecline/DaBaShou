package com.dabashou.shelf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabashou.shelf.domain.TimeSlot;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 时间格子 Mapper
 */
@Mapper
public interface TimeSlotMapper extends BaseMapper<TimeSlot> {

    @Select("SELECT * FROM dbs_time_slot WHERE user_id = #{userId} ORDER BY date, start_time")
    List<TimeSlot> selectByUserId(Long userId);

    @Delete("DELETE FROM dbs_time_slot WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(Long id, Long userId);
}
