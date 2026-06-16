package com.youlai.boot.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.system.model.entity.SysHoliday;
import com.youlai.boot.system.service.SysHolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "07.节假日管理接口")
@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
public class SysHolidayController {

    private final SysHolidayService sysHolidayService;

    @Operation(summary = "获取所有节假日")
    @GetMapping
    public Result<List<String>> getHolidays() {
        List<String> dates = sysHolidayService.list().stream()
            .map(SysHoliday::getHolidayDate)
            .collect(Collectors.toList());
        return Result.success(dates);
    }

    @Operation(summary = "添加节假日")
    @PostMapping
    public Result<?> addHoliday(@RequestBody Map<String, String> body) {
        String date = body.get("date");
        long count = sysHolidayService.count(new LambdaQueryWrapper<SysHoliday>().eq(SysHoliday::getHolidayDate, date));
        if (count == 0) {
            SysHoliday holiday = new SysHoliday();
            holiday.setHolidayDate(date);
            sysHolidayService.save(holiday);
        }
        return Result.success();
    }

    @Operation(summary = "删除节假日")
    @DeleteMapping("/{date}")
    public Result<?> deleteHoliday(@PathVariable String date) {
        sysHolidayService.remove(new LambdaQueryWrapper<SysHoliday>().eq(SysHoliday::getHolidayDate, date));
        return Result.success();
    }
}
