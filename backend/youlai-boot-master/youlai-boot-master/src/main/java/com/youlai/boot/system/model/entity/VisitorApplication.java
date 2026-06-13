package com.youlai.boot.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 访客申请实体
 *
 * @author youlai-boot
 */
@TableName("visitor_application")
@Getter
@Setter
public class VisitorApplication extends BaseEntity {

    /**
     * 申请来访日期
     */
    private LocalDate visitDate;

    /**
     * 申请来访时间点
     */
    private LocalTime visitTime;

    /**
     * 拜访部门ID
     */
    private String deptId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 申请人电话
     */
    private String applicantPhone;

    /**
     * 来访人数
     */
    private Integer visitorCount;

    /**
     * 来访事由
     */
    private String visitPurpose;

    /**
     * 被访问人ID
     */
    private String visitedPersonId;

    /**
     * 来访单位
     */
    private String visitorCompany;

    /**
     * 同行来访人
     */
    private String companionVisitors;

    /**
     * 被访人审批状态
     */
    private Integer visitedPersonApprovalStatus;

    /**
     * 管理员审批状态
     */
    private Integer adminApprovalStatus;

    /**
     * 审批管理员ID
     */
    private Long adminId;

    /**
     * 门卫ID
     */
    private Long guardId;

    /**
     * 申请整体状态
     */
    private Integer applicationStatus;

    /**
     * 被访人审批时间
     */
    private LocalDateTime visitedApprovalTime;

    /**
     * 管理员审批时间
     */
    private LocalDateTime adminApprovalTime;

    /**
     * 门岗核验时间
     */
    private LocalDateTime guardTime;
    /**
     * 拜访部门名称
     */
    @TableField(exist = false)
    private String deptName;

    /**
     * 被访人姓名
     */
    @TableField(exist = false)
    private String visitedPersonName;

    /**
     * 审批管理员姓名
     */
    @TableField(exist = false)
    private String adminName;

    /**
     * 门卫姓名
     */
    @TableField(exist = false)
    private String guardName;

    /**
     * 申请状态文本描述（可选，用于前端直接展示）
     */
    @TableField(exist = false)
    private String applicationStatusText;

}
