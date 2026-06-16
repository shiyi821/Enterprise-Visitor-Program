package com.youlai.boot.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.system.mapper.SysHolidayMapper;
import com.youlai.boot.system.model.entity.SysHoliday;
import com.youlai.boot.system.service.SysHolidayService;
import org.springframework.stereotype.Service;

@Service
public class SysHolidayServiceImpl extends ServiceImpl<SysHolidayMapper, SysHoliday> implements SysHolidayService {}
