package com.youlai.boot.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.system.mapper.VisitorApplicationMapper;
import com.youlai.boot.system.model.entity.VisitorApplication;
import com.youlai.boot.system.service.VisitorApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitorApplicationServiceImpl extends ServiceImpl<VisitorApplicationMapper, VisitorApplication> implements VisitorApplicationService {

}
