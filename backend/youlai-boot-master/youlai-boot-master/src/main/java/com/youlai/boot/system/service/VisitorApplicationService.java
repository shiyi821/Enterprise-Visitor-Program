package com.youlai.boot.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.system.model.entity.VisitorApplication;
import com.youlai.boot.system.model.form.VisitorApplicationForm;
import com.youlai.boot.system.model.query.VisitorApplicationQuery;
import com.youlai.boot.system.model.vo.AdminDashboardVO;
import com.youlai.boot.system.model.vo.ComprehensiveStatsVO;
import com.youlai.boot.system.model.vo.VisitorApplicationPageVO;

public interface VisitorApplicationService extends IService<VisitorApplication> {

    /**
     * 分页查询访客申请列表 (普通申请人视角)
     */
    IPage<VisitorApplicationPageVO> getApplicationPage(VisitorApplicationQuery queryParams);

    /**
     * 新增访客申请
     */
    boolean saveApplication(VisitorApplicationForm formData);

    /**
     * 获取访客申请表单回显数据
     */
    VisitorApplicationForm getApplicationFormData(Long id);

    /**
     * 修改访客申请
     */
    boolean updateApplication(Long id, VisitorApplicationForm formData);

    /**
     * 批量删除访客申请
     */
    boolean deleteApplications(String ids);

    /**
     * 被访人视角-分页查询待审批记录
     */
    IPage<VisitorApplicationPageVO> getAuditApplicationPage(VisitorApplicationQuery queryParams);

    /**
     * 被访人执行审批
     */
    boolean auditApplication(Long id, Integer action);

    /**
     * 管理员视角-分页查询审批记录
     */
    IPage<VisitorApplicationPageVO> getAdminApprovalPage(VisitorApplicationQuery queryParams);

    /**
     * 管理员执行审批
     */
    boolean adminAuditApplication(Long id, Integer action);

    /**
     * 获取管理台看板动态统计数据
     */
    AdminDashboardVO getDashboardStats();

    /**
     * 获取访客申请详细信息
     */
    VisitorApplicationPageVO getApplicationDetail(Long id);

    /**
     * 门岗核验放行
     */
    boolean passApplication(Long id);
    boolean cancelApplication(Long id);
    boolean rebookApplication(Long id);

    /**
     * 门卫视角-分页查询今日待访以及历史记录 (💡 核心新增)
     */
    IPage<VisitorApplicationPageVO> getGuardApplicationPage(VisitorApplicationQuery queryParams);

    /**
     * 获取数据看板图表统计数据
     */
    ComprehensiveStatsVO getComprehensiveStats(String startDate, String endDate);
}
