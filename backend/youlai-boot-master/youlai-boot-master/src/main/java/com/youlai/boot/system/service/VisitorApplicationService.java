package com.youlai.boot.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.system.model.entity.VisitorApplication;
import com.youlai.boot.system.model.form.VisitorApplicationForm;
import com.youlai.boot.system.model.query.VisitorApplicationQuery;
import com.youlai.boot.system.model.vo.VisitorApplicationPageVO;

public interface VisitorApplicationService extends IService<VisitorApplication> {
    /**
     * 分页查询访客申请列表
     *
     * @param queryParams 查询参数
     * @return 分页结果
     */
    IPage<VisitorApplicationPageVO> getApplicationPage(VisitorApplicationQuery queryParams);

    /**
     * 新增访客申请
     *
     * @param formData 表单数据
     * @return 操作结果
     */
    boolean saveApplication(VisitorApplicationForm formData);

    /**
     * 获取访客申请表单回显数据
     *
     * @param id 申请ID
     * @return 表单数据
     */
    VisitorApplicationForm getApplicationFormData(Long id);

    /**
     * 修改访客申请
     *
     * @param id       申请ID
     * @param formData 表单数据
     * @return 操作结果
     */
    boolean updateApplication(Long id, VisitorApplicationForm formData);

    /**
     * 批量删除访客申请
     *
     * @param ids 申请ID，多个英文逗号分隔
     * @return 操作结果
     */
    boolean deleteApplications(String ids);
}
