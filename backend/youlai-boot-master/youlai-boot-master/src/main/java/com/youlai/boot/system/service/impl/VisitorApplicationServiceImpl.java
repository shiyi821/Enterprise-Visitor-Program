package com.youlai.boot.system.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.framework.security.util.SecurityUtils;
import com.youlai.boot.system.mapper.VisitorApplicationMapper;
import com.youlai.boot.system.model.entity.VisitorApplication;
import com.youlai.boot.system.model.form.VisitorApplicationForm;
import com.youlai.boot.system.model.query.VisitorApplicationQuery;
import com.youlai.boot.system.model.vo.VisitorApplicationPageVO;
import com.youlai.boot.system.service.VisitorApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
public class VisitorApplicationServiceImpl extends ServiceImpl<VisitorApplicationMapper, VisitorApplication> implements VisitorApplicationService {
    /**
     * 分页查询访客申请列表
     */
    @Override
    public IPage<VisitorApplicationPageVO> getApplicationPage(VisitorApplicationQuery queryParams) {
        queryParams.setUserId(SecurityUtils.getUserId());
        // 构建分页参数
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        Page<VisitorApplicationPageVO> page = new Page<>(pageNum, pageSize);

        // 调用Mapper连表分页查询
        return this.baseMapper.getApplicationPage(page, queryParams);
    }

    /**
     * 新增访客申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveApplication(VisitorApplicationForm formData) {
        // Form 转 Entity
        VisitorApplication entity = new VisitorApplication();
        BeanUtils.copyProperties(formData, entity);
        entity.setUserId(SecurityUtils.getUserId());
        entity.setVisitedPersonApprovalStatus(0);
        entity.setAdminApprovalStatus(0);
        entity.setApplicationStatus(0);
        // 保存到数据库
        return this.save(entity);
    }

    /**
     * 获取申请表单回显数据
     */
    @Override
    public VisitorApplicationForm getApplicationFormData(Long id) {
        // 查询实体
        VisitorApplication entity = this.getById(id);
        Assert.notNull(entity, "访客申请不存在");

        // Entity 转 Form
        VisitorApplicationForm formData = new VisitorApplicationForm();
        BeanUtils.copyProperties(entity, formData);
        return formData;
    }

    /**
     * 修改访客申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateApplication(Long id, VisitorApplicationForm formData) {
        // 校验申请是否存在
        VisitorApplication oldEntity = this.getById(id);
        Assert.notNull(oldEntity, "访客申请不存在");

        // Form 转 Entity，设置ID
        VisitorApplication entity = new VisitorApplication();
        BeanUtils.copyProperties(formData, entity);
        entity.setId(id);

        // 更新
        return this.updateById(entity);
    }

    /**
     * 批量删除访客申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteApplications(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的数据不能为空");

        // 逗号分割ID，转为集合
        List<String> idList = Arrays.stream(ids.split(","))
            .map(String::trim)
            .collect(Collectors.toList());

        // 批量删除（如果是逻辑删除，框架会自动处理，无需额外代码）
        return this.removeByIds(idList);
    }

    // --- 记得在接口类 VisitorApplicationService.java 里也加上这两个方法的定义 ---

    @Override
    public IPage<VisitorApplicationPageVO> getAuditApplicationPage(VisitorApplicationQuery queryParams) {
        // 💡 核心：审批视角下，不查 userId，而是查 visitedPersonId = 当前登录人
        // 这里把当前登录用户的 ID 转换为 String 传给被访人字段
        queryParams.setVisitedPersonId(String.valueOf(SecurityUtils.getUserId()));

        // 确保清除 userId 限制，否则查不到别人的申请
        queryParams.setUserId(null);

        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        Page<VisitorApplicationPageVO> page = new Page<>(pageNum, pageSize);

        return this.baseMapper.getApplicationPage(page, queryParams);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditApplication(Long id, Integer action) {
        VisitorApplication entity = this.getById(id);
        Assert.notNull(entity, "访客申请不存在");

        if (action == 1) { // 1代表同意
            entity.setVisitedPersonApprovalStatus(1);
        } else if (action == 2) { // 2代表拒绝
            entity.setVisitedPersonApprovalStatus(2);
            entity.setApplicationStatus(2); // 如果被访人拒绝了，整个单子直接结束变“已拒绝”
        }

        return this.updateById(entity);
    }
    /**
     * 管理员审批列表：不限制被访人，仅按管理员审批状态筛选
     */
    @Override
    public IPage<VisitorApplicationPageVO> getAdminApprovalPage(VisitorApplicationQuery queryParams) {
        Page<VisitorApplicationPageVO> page = new Page<>(queryParams.getPageNum(), queryParams.getPageSize());
        // 直接复用原有分页SQL，通过Query参数控制筛选条件
        return this.baseMapper.getApplicationPage(page, queryParams);
    }

    /**
     * 管理员审批操作
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminAuditApplication(Long id, Integer action) {
        // 1. 校验申请是否存在
        VisitorApplication entity = this.getById(id);
        Assert.notNull(entity, "访客申请不存在");

        // 2. 状态校验：必须是待管理员审批状态（被访人已通过、管理员待审批）
        Assert.isTrue(entity.getVisitedPersonApprovalStatus() == 1, "被访人未审批，无法操作");
        Assert.isTrue(entity.getAdminApprovalStatus() == 0, "该申请已审批，请勿重复操作");

        // 3. 更新审批状态
        Long currentUserId = SecurityUtils.getUserId();
        if (action == 1) {
            // 管理员同意
            entity.setAdminApprovalStatus(1);
            entity.setApplicationStatus(0); // 整体状态保持「待来访」
        } else if (action == 2) {
            // 管理员拒绝
            entity.setAdminApprovalStatus(2);
            entity.setApplicationStatus(2); // 整体状态置为「已拒绝」
        } else {
            throw new IllegalArgumentException("非法的审批操作类型");
        }
        entity.setAdminId(currentUserId);
        entity.setAdminApprovalTime(LocalDateTime.now());
        return this.updateById(entity);
    }
}
