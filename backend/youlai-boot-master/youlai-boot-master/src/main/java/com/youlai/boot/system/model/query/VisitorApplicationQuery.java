package com.youlai.boot.system.model.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "访客申请查询对象")
public class VisitorApplicationQuery extends BaseQuery {

    @Schema(description = "关键字(访客姓名/手机号/来访事由)")
    private String keywords;

    @Schema(description = "被访人审批状态(0:待审批;1:已通过;2:已拒绝)")
    private Integer visitedPersonApprovalStatus;

    @Schema(description = "管理员审批状态(0:待审批;1:已通过;2:已拒绝)")
    private Integer adminApprovalStatus;

    @Schema(description = "申请整体状态(0:待来访;1:已来访;2:已拒绝)")
    private Integer applicationStatus;

    @Schema(description = "所属部门ID")
    private Long deptId;

    @Schema(description = "审批人ID")
    private Long adminId;

    @Schema(description = "来访时间范围")
    private List<String> visitTime;

    @Schema(description = "创建时间范围")
    private List<String> createTime;

    @JsonIgnore
    @Schema(hidden = true)
    private Boolean isRoot;

    @Schema(description = "申请人账号ID（后端自动注入，前端无需传递）")
    private Long userId;

    @Schema(description = "被访人ID（后端自动注入，前端无需传递）")
    private String visitedPersonId;

    @Schema(description = "前端传递的Tab类型(todo/processing/done)")
    private String tabType;
}
