package com.youlai.boot.system.controller;

import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.enums.ActionTypeEnum;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.common.model.Option;
import com.youlai.boot.common.result.PageResult;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.system.model.form.VisitorApplicationForm;
import com.youlai.boot.system.model.query.VisitorApplicationQuery;
import com.youlai.boot.system.model.vo.AdminDashboardVO;
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

    @Operation(summary = "被访人视角-获取审批分页列表")
    @GetMapping("/audit")
    public PageResult<VisitorApplicationPageVO> getAuditApplicationList(@Valid VisitorApplicationQuery queryParams) {
        return PageResult.success(visitorApplicationService.getAuditApplicationPage(queryParams));
    }

    @Operation(summary = "被访人视角-执行审批(同意/拒绝)")
    @PutMapping("/{id}/audit")
    @Log(module = LogModuleEnum.VISITOR, value = ActionTypeEnum.UPDATE)
    public Result<Void> auditApplication(
        @Parameter(description = "申请ID") @PathVariable Long id,
        @Parameter(description = "审批动作：1同意，2拒绝") @RequestParam Integer action
    ) {
        boolean result = visitorApplicationService.auditApplication(id, action);
        return Result.judge(result);
    }

    @Operation(summary = "管理员审批列表")
    @GetMapping("/admin-approval")
    @Log(module = LogModuleEnum.VISITOR, value = ActionTypeEnum.LIST)
    public PageResult<VisitorApplicationPageVO> getAdminApprovalList(
        @Valid VisitorApplicationQuery queryParams
    ) {
        return PageResult.success(visitorApplicationService.getAdminApprovalPage(queryParams));
    }

    @Operation(summary = "管理员审批（同意/拒绝）")
    @PutMapping("/{id}/admin-audit")
    @Log(module = LogModuleEnum.VISITOR, value = ActionTypeEnum.UPDATE)
    public Result<Void> adminAuditApplication(
        @Parameter(description = "申请ID") @PathVariable Long id,
        @Parameter(description = "操作类型：1-同意，2-拒绝") @RequestParam Integer action
    ) {
        boolean result = visitorApplicationService.adminAuditApplication(id, action);
        return Result.judge(result);
    }

    /**
     * 获取管理台看板动态统计数据
     */
    @Operation(summary = "获取管理员看板统计数据")
    @GetMapping("/dashboard/stats")
    public Result<AdminDashboardVO> getDashboardStats() {
        AdminDashboardVO stats = visitorApplicationService.getDashboardStats();
        return Result.success(stats);
    }
}
