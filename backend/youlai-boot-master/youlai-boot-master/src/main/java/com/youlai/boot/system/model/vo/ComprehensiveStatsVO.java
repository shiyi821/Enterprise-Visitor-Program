package com.youlai.boot.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "看板综合统计VO")
public class ComprehensiveStatsVO {
    @Schema(description = "时间区间统计")
    private List<StatChartDataVO> trendStats;

    @Schema(description = "来访单位统计")
    private List<StatChartDataVO> companyStats;

    @Schema(description = "员工部门统计(仅管理员可见)")
    private List<StatChartDataVO> deptStats;
}
