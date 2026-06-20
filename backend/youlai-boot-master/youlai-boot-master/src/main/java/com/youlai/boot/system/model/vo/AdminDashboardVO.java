package com.youlai.boot.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员看板数据视图")
public class AdminDashboardVO {

    @Schema(description = "到访总人数")
    private Integer totalVisitorCount;

    @Schema(description = "待管理员审批数")
    private Integer adminPendingCount;

    @Schema(description = "待被访人审批数")
    private Integer hostPendingCount;

    @Schema(description = "员工总数")
    private Integer employeeCount;

    @Schema(description = "今日到访人数(被访人视角)")
    private Integer todayVisitorCount;
}
