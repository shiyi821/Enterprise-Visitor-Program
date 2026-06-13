package com.youlai.boot.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;

/**
 * 访客申请表单对象
 *
 * @author haoxr
 * @since 2.x.x
 */
@Schema(description = "访客申请表单对象")
@Data
public class VisitorApplicationForm {

    @Schema(description = "申请ID(编辑时传)")
    private Long id;

    @Schema(description = "访客姓名")
    @NotBlank(message = "访客姓名不能为空")
    private String visitorName;

    @Schema(description = "访客手机号")
    @Pattern(regexp = "^$|^1(3\\d|4[5-9]|5[0-35-9]|6[2567]|7[0-8]|8\\d|9[0-35-9])\\d{8}$", message = "手机号码格式不正确")
    @NotBlank(message = "访客手机号不能为空")
    private String visitorMobile;

    @Schema(description = "来访事由")
    @NotBlank(message = "来访事由不能为空")
    private String visitReason;

    @Schema(description = "来访时间")
    @NotNull(message = "来访时间不能为空")
    private LocalDateTime visitTime;

    @Schema(description = "离开时间")
    @NotNull(message = "离开时间不能为空")
    private LocalDateTime leaveTime;

    @Schema(description = "申请状态(0:待审批;1:已通过;2:已驳回)")
    @Range(min = 0, max = 2, message = "申请状态不正确")
    private Integer status;

    @Schema(description = "所属部门ID")
    @NotNull(message = "所属部门不能为空")
    private Long deptId;

    @Schema(description = "审批人ID")
    @NotNull(message = "审批人不能为空")
    private Long approverId;
}
