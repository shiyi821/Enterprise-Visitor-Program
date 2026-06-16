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
import com.youlai.boot.system.model.form.VisitorApplicationForm;
import com.youlai.boot.system.model.query.VisitorApplicationQuery;
import com.youlai.boot.system.model.vo.AdminDashboardVO;
import com.youlai.boot.system.model.vo.VisitorApplicationPageVO;
import com.youlai.boot.system.service.UserService;
import com.youlai.boot.system.service.VisitorApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitorApplicationServiceImpl extends ServiceImpl<VisitorApplicationMapper, VisitorApplication> implements VisitorApplicationService {

    private final UserService userService;

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
        List<String> idList = Arrays.stream(ids.split(",")).map(String::trim).collect(Collectors.toList());
        return this.removeByIds(idList);
    }

    @Override
    public IPage<VisitorApplicationPageVO> getAuditApplicationPage(VisitorApplicationQuery queryParams) {
        // 员工查自己的被访记录，VisitedPersonId在Query里被定义成了String，所以需要String.valueOf转换
        queryParams.setVisitedPersonId(String.valueOf(SecurityUtils.getUserId()));
        queryParams.setUserId(null); // 清除用户限制，否则查不到别人的提交
        Page<VisitorApplicationPageVO> page = new Page<>(queryParams.getPageNum(), queryParams.getPageSize());
        return this.baseMapper.getApplicationPage(page, queryParams);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditApplication(Long id, Integer action) {
        VisitorApplication entity = this.getById(id);
        Assert.notNull(entity, "访客申请不存在");
        if (action == 1) {
            entity.setVisitedPersonApprovalStatus(1);
        } else if (action == 2) {
            entity.setVisitedPersonApprovalStatus(2);
            entity.setApplicationStatus(2); // 被访人拒绝整个流程终止
        }
        return this.updateById(entity);
    }

    @Override
    public IPage<VisitorApplicationPageVO> getAdminApprovalPage(VisitorApplicationQuery queryParams) {
        Page<VisitorApplicationPageVO> page = new Page<>(queryParams.getPageNum(), queryParams.getPageSize());
        return this.baseMapper.getApplicationPage(page, queryParams);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminAuditApplication(Long id, Integer action) {
        VisitorApplication entity = this.getById(id);
        Assert.notNull(entity, "访客申请不存在");
        if (action == 1) {
            entity.setAdminApprovalStatus(1);
            entity.setApplicationStatus(0); // 整体流程流转到：待门岗核验(待来访)
        } else if (action == 2) {
            entity.setAdminApprovalStatus(2);
            entity.setApplicationStatus(2);
        }
        entity.setAdminId(SecurityUtils.getUserId());
        entity.setAdminApprovalTime(LocalDateTime.now());
        return this.updateById(entity);
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
            .exists("SELECT 1 FROM sys_user_role ur WHERE ur.user_id = sys_user.id AND ur.role_id IN (1, 2, 4, 5)"));

        vo.setEmployeeCount((int) employeeCount);
        return vo;
    }

    @Override
    public VisitorApplicationPageVO getApplicationDetail(Long id) {
        VisitorApplicationPageVO detail = this.baseMapper.getApplicationDetail(id);
        Assert.notNull(detail, "访客申请记录不存在");
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean passApplication(Long id) {
        VisitorApplication entity = this.getById(id);
        Assert.notNull(entity, "访客申请不存在");
        Assert.isTrue(entity.getVisitedPersonApprovalStatus() == 1, "被访人未审批，无法放行");
        Assert.isTrue(entity.getAdminApprovalStatus() == 1, "管理员未审批，无法放行");
        Assert.isTrue(entity.getApplicationStatus() == 0, "该申请当前状态无法放行");

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
        }

        Page<VisitorApplicationPageVO> page = new Page<>(queryParams.getPageNum(), queryParams.getPageSize());
        return this.baseMapper.getApplicationPage(page, queryParams);
    }
}
