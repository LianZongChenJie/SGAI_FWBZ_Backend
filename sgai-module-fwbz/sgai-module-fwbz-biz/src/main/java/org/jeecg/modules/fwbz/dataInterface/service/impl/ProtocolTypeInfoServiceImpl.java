package org.jeecg.modules.fwbz.dataInterface.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.fwbz.dataInterface.entity.ProtocolTypeInfo;
import org.jeecg.modules.fwbz.dataInterface.mapper.ProtocolTypeInfoMapper;
import org.jeecg.modules.fwbz.dataInterface.service.IProtocolTypeInfoService;
import org.springframework.stereotype.Service;

/**
 * 接口协议类型 Service 实现
 */
@Service
public class ProtocolTypeInfoServiceImpl extends ServiceImpl<ProtocolTypeInfoMapper, ProtocolTypeInfo>
        implements IProtocolTypeInfoService {
}
