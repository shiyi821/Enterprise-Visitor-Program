package com.youlai.boot.system.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
}
