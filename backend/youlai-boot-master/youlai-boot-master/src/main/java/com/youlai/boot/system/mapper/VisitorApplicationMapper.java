package com.youlai.boot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.system.model.entity.VisitorApplication;
import com.youlai.boot.system.model.query.VisitorApplicationQuery;
import com.youlai.boot.system.model.vo.VisitorApplicationPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VisitorApplicationMapper extends BaseMapper<VisitorApplication> {

    /**
     * 分页查询访客申请
     *
     * @param page        分页对象
     * @param queryParams 查询参数
     * @return 分页结果
     */
    IPage<VisitorApplicationPageVO> getApplicationPage(
        IPage<VisitorApplicationPageVO> page,
        @Param("queryParams") VisitorApplicationQuery queryParams
    );
}
