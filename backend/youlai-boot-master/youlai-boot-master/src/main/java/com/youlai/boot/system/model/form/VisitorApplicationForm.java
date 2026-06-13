package com.youlai.boot.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 访客申请表单对象
 */
@Schema(description = "访客申请表单对象")
@Data
public class VisitorApplicationForm {

    @Schema(description = "申请ID(编辑时传)")
    private Long id;

    @Schema(description = "申请来访日期")
    @NotNull(message = "来访日期不能为空")
    private LocalDate visitDate;

    @Schema(description = "申请来访时间")
    @NotNull(message = "来访时间不能为空")
    private LocalTime visitTime;

    @Schema(description = "申请人姓名")
    @NotBlank(message = "申请人姓名不能为空")
    private String applicantName;

    @Schema(description = "申请人电话")
    @NotBlank(message = "申请人电话不能为空")
    private String applicantPhone;

    @Schema(description = "来访单位")
    private String visitorCompany;

    @Schema(description = "来访人数")
    private Integer visitorCount;

    @Schema(description = "来访事由")
    @NotBlank(message = "来访事由不能为空")
    private String visitPurpose;

    @Schema(description = "拜访部门ID")
    @NotBlank(message = "所属部门不能为空")
    private String deptId;

    @Schema(description = "被访问人ID")
    @NotBlank(message = "被访人不能为空")
    private String visitedPersonId;

    @Schema(description = "同行来访人")
    private String companionVisitors;
}
