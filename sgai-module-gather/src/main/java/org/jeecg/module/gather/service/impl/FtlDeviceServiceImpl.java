package org.jeecg.module.gather.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.module.gather.entity.FtlDevice;
import org.jeecg.module.gather.mapper.FtlDeviceMapper;
import org.jeecg.module.gather.service.IFtlDeviceService;
import org.springframework.stereotype.Service;

@Service
public class FtlDeviceServiceImpl extends ServiceImpl<FtlDeviceMapper, FtlDevice> implements IFtlDeviceService {
}
