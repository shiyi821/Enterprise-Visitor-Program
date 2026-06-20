package com.youlai.boot.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "通用图表数据VO")
public class StatChartDataVO {
    @Schema(description = "维度名称(如日期、公司名、部门名)")
    private String name;

    @Schema(description = "数值(如访客数)")
    private Integer value;
}
