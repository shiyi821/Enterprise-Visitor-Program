package com.youlai.boot.system.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.framework.security.util.SecurityUtils;
import com.youlai.boot.system.mapper.VisitorApplicationMapper;
import com.youlai.boot.system.model.entity.SysUser;
import com.youlai.boot.system.model.entity.VisitorApplication;
import com.youlai.boot.system.model.entity.Notice; // 👈 引入框架通知实体
import com.youlai.boot.system.model.form.VisitorApplicationForm;
import com.youlai.boot.system.model.query.VisitorApplicationQuery;
import com.youlai.boot.system.model.vo.AdminDashboardVO;
import com.youlai.boot.system.model.vo.VisitorApplicationPageVO;
import com.youlai.boot.system.service.UserService;
import com.youlai.boot.system.service.VisitorApplicationService;
import com.youlai.boot.system.service.AiService;       // 👈 引入你写的 AI 服务
import com.youlai.boot.system.service.NoticeService;   // 👈 引入框架自带通知服务
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // 👈 Lombok 会自动为 private final 的属性生成构造方法注入
@Slf4j
public class VisitorApplicationServiceImpl extends ServiceImpl<VisitorApplicationMapper, VisitorApplication> implements VisitorApplicationService {

    private final UserService userService;
    private final AiService aiService;         // 👈 顺应框架，通过 final 自动注入 AI 服务
    private final NoticeService noticeService; // 👈 顺应框架，通过 final 自动注入系统通知服务

    @Override
    public IPage<VisitorApplicationPageVO> getApplicationPage(VisitorApplicationQuery queryParams) {
        // 绑定申请人自己
        queryParams.setUserId(SecurityUtils.getUserId());
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        Page<VisitorApplicationPageVO> page = new Page<>(pageNum, pageSize);
        return this.baseMapper.getApplicationPage(page, queryParams);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveApplication(VisitorApplicationForm formData) {
        VisitorApplication entity = new VisitorApplication();
        BeanUtils.copyProperties(formData, entity);
        entity.setUserId(SecurityUtils.getUserId());
        entity.setVisitedPersonApprovalStatus(0);
        entity.setAdminApprovalStatus(0);
        entity.setApplicationStatus(0);
        return this.save(entity);
    }

    @Override
    public VisitorApplicationForm getApplicationFormData(Long id) {
        VisitorApplication entity = this.getById(id);
        Assert.notNull(entity, "访客申请不存在");
        VisitorApplicationForm formData = new VisitorApplicationForm();
        BeanUtils.copyProperties(entity, formData);
        return formData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateApplication(Long id, VisitorApplicationForm formData) {
        VisitorApplication oldEntity = this.getById(id);
        Assert.notNull(oldEntity, "访客申请不存在");
        VisitorApplication entity = new VisitorApplication();
        BeanUtils.copyProperties(formData, entity);
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteApplications(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的数据不能为空");
<<<<<<< HEAD
        List<String> idList = Arrays.stream(ids.split(","))
            .map(String::trim)
            .collect(Collectors.toList());
=======
        List<String> idList = Arrays.stream(ids.split(",")).map(String::trim).collect(Collectors.toList());
>>>>>>> 61b7618cb3093c50965dc11374f9bfe1ac6f7dc4
        return this.removeByIds(idList);
    }

    @Override
    public IPage<VisitorApplicationPageVO> getAuditApplicationPage(VisitorApplicationQuery queryParams) {
<<<<<<< HEAD
        queryParams.setVisitedPersonId(String.valueOf(SecurityUtils.getUserId()));
        queryParams.setUserId(null);
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        Page<VisitorApplicationPageVO> page = new Page<>(pageNum, pageSize);
=======
        // 员工查自己的被访记录，VisitedPersonId在Query里被定义成了String，所以需要String.valueOf转换
        queryParams.setVisitedPersonId(String.valueOf(SecurityUtils.getUserId()));
        queryParams.setUserId(null); // 清除用户限制，否则查不到别人的提交
        Page<VisitorApplicationPageVO> page = new Page<>(queryParams.getPageNum(), queryParams.getPageSize());
>>>>>>> 61b7618cb3093c50965dc11374f9bfe1ac6f7dc4
        return this.baseMapper.getApplicationPage(page, queryParams);
    }

    /**
     * 被访人（员工）审批操作
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditApplication(Long id, Integer action) {
        VisitorApplication entity = this.getById(id);
        Assert.notNull(entity, "访客申请不存在");
        if (action == 1) {
            entity.setVisitedPersonApprovalStatus(1);
        } else if (action == 2) {
            entity.setVisitedPersonApprovalStatus(2);
<<<<<<< HEAD
            entity.setApplicationStatus(2);
=======
            entity.setApplicationStatus(2); // 被访人拒绝整个流程终止
>>>>>>> 61b7618cb3093c50965dc11374f9bfe1ac6f7dc4
        }
        return this.updateById(entity);
    }

<<<<<<< HEAD
    /**
     * 管理员审批列表
     */
=======
>>>>>>> 61b7618cb3093c50965dc11374f9bfe1ac6f7dc4
    @Override
    public IPage<VisitorApplicationPageVO> getAdminApprovalPage(VisitorApplicationQuery queryParams) {
        Page<VisitorApplicationPageVO> page = new Page<>(queryParams.getPageNum(), queryParams.getPageSize());
        return this.baseMapper.getApplicationPage(page, queryParams);
    }

<<<<<<< HEAD
    /**
     * 管理员审批操作（💡 核心修改：审批通过后，给访客和被访人发送通知，并解决事务回滚问题）
     */
=======
>>>>>>> 61b7618cb3093c50965dc11374f9bfe1ac6f7dc4
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminAuditApplication(Long id, Integer action) {
        VisitorApplication entity = this.getById(id);
        Assert.notNull(entity, "访客申请不存在");
<<<<<<< HEAD
        Assert.isTrue(entity.getVisitedPersonApprovalStatus() == 1, "被访人未审批，无法操作");
        Assert.isTrue(entity.getAdminApprovalStatus() == 0, "该申请已审批，请勿重复操作");

        Long currentUserId = SecurityUtils.getUserId();
        if (action == 1) {
            entity.setAdminApprovalStatus(1);
            entity.setApplicationStatus(0);
        } else if (action == 2) {
            entity.setAdminApprovalStatus(2);
            entity.setApplicationStatus(2);
        } else {
            throw new IllegalArgumentException("非法的审批操作类型");
=======
        if (action == 1) {
            entity.setAdminApprovalStatus(1);
            entity.setApplicationStatus(0); // 整体流程流转到：待门岗核验(待来访)
        } else if (action == 2) {
            entity.setAdminApprovalStatus(2);
            entity.setApplicationStatus(2);
>>>>>>> 61b7618cb3093c50965dc11374f9bfe1ac6f7dc4
        }
        entity.setAdminId(SecurityUtils.getUserId());
        entity.setAdminApprovalTime(LocalDateTime.now());

        // 1. 先执行核心的数据库状态更新
        boolean isSuccess = this.updateById(entity);

        // 2. 只有审批【同意】并且数据库更新成功了，才去发通知
        if (isSuccess && action == 1) {
            // 核心修复：使用事务同步器，等审批完全落库提交后，再执行发消息。这样发消息出任何错都不会回滚审批！
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        // ① 调用大模型生成话术
                        String aiScript = aiService.generateReceptionScript(
                            entity.getApplicantName(),
                            entity.getVisitorCompany(),
                            entity.getVisitPurpose()
                        );

                        // ② 给【被访员工】发通知（带AI）
                        Notice hostNotice = new Notice();
                        hostNotice.setTitle("访客预约成功通知");
                        hostNotice.setContent(String.format("您好，由您接待的访客【%s】（单位：%s）已通过最终审批。\n\n💡 AI 专属接待话术建议：\n%s",
                            entity.getApplicantName(), entity.getVisitorCompany(), aiScript));
                        hostNotice.setType(1);
                        hostNotice.setTargetType(2);
                        hostNotice.setTargetUserIds(entity.getVisitedPersonId()); // 发给被访人
                        hostNotice.setLevel("M");
                        hostNotice.setPublisherId(currentUserId); // 补全发布人，防空指针
                        hostNotice.setPublishStatus(0); // ⚠️ 必须设为0(草稿)，由后面的publishNotice去转正！

                        noticeService.save(hostNotice);
                        noticeService.publishNotice(hostNotice.getId());

                        // ③ 给【访客】发普通成功通知
                        Notice visitorNotice = new Notice();
                        visitorNotice.setTitle("预约申请通过通知");
                        visitorNotice.setContent(String.format("您好，您提交的访客预约申请（预计到访日期：%s）已通过最终审批，请按时前往企业园区。",
                            entity.getVisitDate()));
                        visitorNotice.setType(1);
                        visitorNotice.setTargetType(2);
                        visitorNotice.setTargetUserIds(String.valueOf(entity.getUserId())); // 发给提交申请的访客
                        visitorNotice.setLevel("M");
                        visitorNotice.setPublisherId(currentUserId);
                        visitorNotice.setPublishStatus(0); // ⚠️ 必须设为0

                        noticeService.save(visitorNotice);
                        noticeService.publishNotice(visitorNotice.getId());

                    } catch (Exception e) {
                        log.error("审批成功后，分发通知或AI调用失败（不影响主流程）", e);
                    }
                }
            });
        }

        return isSuccess;
    }

    @Override
    public AdminDashboardVO getDashboardStats() {
        AdminDashboardVO vo = new AdminDashboardVO();
        long visitorCount = this.count(new LambdaQueryWrapper<VisitorApplication>()
            .eq(VisitorApplication::getVisitedPersonApprovalStatus, 1)
            .eq(VisitorApplication::getAdminApprovalStatus, 1));
        vo.setTotalVisitorCount((int) visitorCount);

        long adminPending = this.count(new LambdaQueryWrapper<VisitorApplication>()
            .eq(VisitorApplication::getVisitedPersonApprovalStatus, 1)
            .eq(VisitorApplication::getAdminApprovalStatus, 0));
        vo.setAdminPendingCount((int) adminPending);

        long hostPending = this.count(new LambdaQueryWrapper<VisitorApplication>()
            .eq(VisitorApplication::getVisitedPersonApprovalStatus, 0));
        vo.setHostPendingCount((int) hostPending);

        long employeeCount = userService.count(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getStatus, 1)
<<<<<<< HEAD
            .exists("SELECT 1 FROM sys_user_role ur WHERE ur.user_id = sys_user.id AND ur.role_id IN (1, 2,4,5)"));
=======
            .exists("SELECT 1 FROM sys_user_role ur WHERE ur.user_id = sys_user.id AND ur.role_id IN (1, 2, 4, 5)"));

>>>>>>> 61b7618cb3093c50965dc11374f9bfe1ac6f7dc4
        vo.setEmployeeCount((int) employeeCount);
        return vo;
    }

    @Override
    public VisitorApplicationPageVO getApplicationDetail(Long id) {
        VisitorApplicationPageVO detail = this.baseMapper.getApplicationDetail(id);
        Assert.notNull(detail, "访客申请记录不存在");
        return detail;
    }

<<<<<<< HEAD
    /**
     * 门岗核验放行 —— 纯粹状态核验，不发额外通知
     */
=======
>>>>>>> 61b7618cb3093c50965dc11374f9bfe1ac6f7dc4
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean passApplication(Long id) {
        VisitorApplication entity = this.getById(id);
        Assert.notNull(entity, "访客申请不存在");
        Assert.isTrue(entity.getVisitedPersonApprovalStatus() == 1, "被访人未审批，无法放行");
        Assert.isTrue(entity.getAdminApprovalStatus() == 1, "管理员未审批，无法放行");
        Assert.isTrue(entity.getApplicationStatus() == 0, "该申请当前状态无法放行");

<<<<<<< HEAD
        entity.setApplicationStatus(1);
        entity.setGuardId(SecurityUtils.getUserId());

        SysUser guardUser = userService.getById(SecurityUtils.getUserId());
        if(guardUser != null) {
            entity.setGuardName(guardUser.getNickname());
=======
        entity.setApplicationStatus(1); // 1表示已来访完成
        entity.setGuardId(SecurityUtils.getUserId()); // 此时类型完美匹配 Long
        entity.setGuardTime(LocalDateTime.now());
        return this.updateById(entity);
    }

    // ====================================================================
    // 💡 门岗卫兵的专属分页查询拦截
    // ====================================================================
    @Override
    public IPage<VisitorApplicationPageVO> getGuardApplicationPage(VisitorApplicationQuery queryParams) {
        // 1. 解除用户ID过滤，门卫可查看所有人的数据
        queryParams.setUserId(null);

        // 2. 核心拦截规则：只能看“被访人通过(1)”且“管理员通过(1)”的完备记录
        queryParams.setVisitedPersonApprovalStatus(1);
        queryParams.setAdminApprovalStatus(1);

        // 3. 兜底判断：如果前端没有传任何日期查询条件（既没有按天过滤，也没有区间范围）
        boolean noDateFilter = StrUtil.isBlank(queryParams.getVisitDate())
            && StrUtil.isBlank(queryParams.getStartDate())
            && StrUtil.isBlank(queryParams.getEndDate());

        if (noDateFilter) {
            // 没有查询条件时，默认强制绑定今日
            queryParams.setVisitDate(LocalDate.now().toString());
>>>>>>> 61b7618cb3093c50965dc11374f9bfe1ac6f7dc4
        }
        entity.setGuardTime(LocalDateTime.now());

<<<<<<< HEAD
        // 执行数据库状态更新并直接返回结果
        return this.updateById(entity);
=======
        Page<VisitorApplicationPageVO> page = new Page<>(queryParams.getPageNum(), queryParams.getPageSize());
        return this.baseMapper.getApplicationPage(page, queryParams);
>>>>>>> 61b7618cb3093c50965dc11374f9bfe1ac6f7dc4
    }

    @Override
    public boolean cancelApplication(Long id) {
        VisitorApplication application = new VisitorApplication();
        application.setId(id);
        application.setApplicationStatus(3);
        application.setVisitedPersonApprovalStatus(0);
        application.setAdminApprovalStatus(0);
        application.setVisitedApprovalTime(null);
        application.setAdminApprovalTime(null);
        return this.updateById(application);
    }

    @Override
    public boolean rebookApplication(Long id) {
        VisitorApplication application = new VisitorApplication();
        application.setId(id);
        application.setApplicationStatus(0);
        application.setVisitedPersonApprovalStatus(0);
        application.setAdminApprovalStatus(0);
        application.setVisitedApprovalTime(null);
        application.setAdminApprovalTime(null);
        return this.updateById(application);
    }
}
