package com.youlai.boot.system.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 访客申请分页视图对象
 */
@Schema(description = "访客申请分页对象")
@Data
public class VisitorApplicationPageVO {

    @Schema(description = "申请ID")
    private String id;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "申请人手机号")
    private String applicantPhone;

    @Schema(description = "来访单位")
    private String visitorCompany;

    @Schema(description = "来访人数")
    private Integer visitorCount;

    @Schema(description = "来访事由")
    private String visitPurpose;

    @Schema(description = "来访日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    @Schema(description = "来访时间点")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime visitTime;

    @Schema(description = "拜访部门名称")
    private String deptName;

    @Schema(description = "被访人姓名")
    private String visitedPersonName;

    @Schema(description = "被访人审批状态")
    private Integer visitedPersonApprovalStatus;

    @Schema(description = "管理员审批状态")
    private Integer adminApprovalStatus;

    @Schema(description = "申请整体状态")
    private Integer applicationStatus;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime createTime;
}
