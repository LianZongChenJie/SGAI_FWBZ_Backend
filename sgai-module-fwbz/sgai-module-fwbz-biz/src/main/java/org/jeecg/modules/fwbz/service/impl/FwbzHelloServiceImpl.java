package org.jeecg.modules.fwbz.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.fwbz.entity.FwbzHelloEntity;
import org.jeecg.modules.fwbz.mapper.FwbzHelloMapper;
import org.jeecg.modules.fwbz.service.IFwbzHelloService;
import org.springframework.stereotype.Service;

/**
 * 测试Service
 */
@Service
public class FwbzHelloServiceImpl extends ServiceImpl<FwbzHelloMapper, FwbzHelloEntity> implements IFwbzHelloService {

    @Override
    public String hello() {
        return "hello ，我是 Fwbz 微服务节点!";
    }
}
