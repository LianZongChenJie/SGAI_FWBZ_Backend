package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.fwbz.hikvision.entity.CameraInfo;
import org.jeecg.modules.fwbz.hikvision.mapper.CameraInfoMapper;
import org.jeecg.modules.fwbz.hikvision.service.ICameraInfoService;
import org.springframework.stereotype.Service;

/**
 * 摄像头信息表 Service 实现
 *
 * @author fwbz
 */
@Service
public class CameraInfoServiceImpl extends ServiceImpl<CameraInfoMapper, CameraInfo> implements ICameraInfoService {

}
