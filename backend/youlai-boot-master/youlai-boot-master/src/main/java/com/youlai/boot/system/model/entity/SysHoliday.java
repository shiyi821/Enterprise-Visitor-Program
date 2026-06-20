package com.youlai.boot.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_holiday")
public class SysHoliday {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String holidayDate;
    private LocalDateTime createTime;
}
