package com.youlai.boot.system.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客申请分页视图对象
 *
 * @author haoxr
 * @since 2.x.x
 */
@Schema(description = "访客申请分页对象")
@Data
public class VisitorApplicationPageVO {

    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "访客手机号")
    private String visitorMobile;

    @Schema(description = "来访事由")
    private String visitReason;

    @Schema(description = "来访时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime visitTime;

    @Schema(description = "离开时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime leaveTime;

    @Schema(description = "申请人工号/姓名")
    private String applicantName;

    @Schema(description = "审批人姓名")
    private String approverName;

    @Schema(description = "申请状态(0:待审批;1:已通过;2:已驳回)")
    private Integer status;

    @Schema(description = "所属部门名称")
    private String deptName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime createTime;
}
