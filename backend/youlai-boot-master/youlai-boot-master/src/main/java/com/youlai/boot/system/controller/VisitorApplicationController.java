package com.youlai.boot.system.controller;

import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.enums.ActionTypeEnum;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.common.model.Option;
import com.youlai.boot.common.result.PageResult;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.system.model.form.VisitorApplicationForm;
import com.youlai.boot.system.model.query.VisitorApplicationQuery;
import com.youlai.boot.system.model.vo.VisitorApplicationPageVO;
import com.youlai.boot.system.service.VisitorApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 访客申请控制层
 *
 * @author youlai-boot
 */
@Tag(name = "03.访客申请接口")
@RestController
@RequestMapping("/api/v1/visitor-applications")
@RequiredArgsConstructor
public class VisitorApplicationController {

    private final VisitorApplicationService visitorApplicationService;

    @Operation(summary = "访客申请分页列表")
    @GetMapping
    @Log(module = LogModuleEnum.VISITOR, value = ActionTypeEnum.LIST)
    public PageResult<VisitorApplicationPageVO> getApplicationList(
        @Valid VisitorApplicationQuery queryParams
    ) {
        return PageResult.success(visitorApplicationService.getApplicationPage(queryParams));
    }

    @Operation(summary = "新增访客申请")
    @PostMapping
    @Log(module = LogModuleEnum.VISITOR, value = ActionTypeEnum.INSERT)
    public Result<?> saveApplication(
        @RequestBody @Valid VisitorApplicationForm formData
    ) {
        boolean result = visitorApplicationService.saveApplication(formData);
        return Result.judge(result);
    }

    @Operation(summary = "获取访客申请表单数据")
    @GetMapping("/{id}/form")
    public Result<VisitorApplicationForm> getApplicationForm(
        @Parameter(description = "申请ID") @PathVariable Long id
    ) {
        VisitorApplicationForm formData = visitorApplicationService.getApplicationFormData(id);
        return Result.success(formData);
    }

    @Operation(summary = "修改访客申请")
    @PutMapping("/{id}")
    @Log(module = LogModuleEnum.VISITOR, value = ActionTypeEnum.UPDATE)
    public Result<Void> updateApplication(
        @Parameter(description = "申请ID") @PathVariable Long id,
        @RequestBody @Valid VisitorApplicationForm formData
    ) {
        boolean result = visitorApplicationService.updateApplication(id, formData);
        return Result.judge(result);
    }

    @Operation(summary = "删除访客申请")
    @DeleteMapping("/{ids}")
    @Log(module = LogModuleEnum.VISITOR, value = ActionTypeEnum.DELETE)
    public Result<Void> deleteApplications(
        @Parameter(description = "申请ID，多个以英文逗号(,)分割") @PathVariable String ids
    ) {
        boolean result = visitorApplicationService.deleteApplications(ids);
        return Result.judge(result);
    }

}
