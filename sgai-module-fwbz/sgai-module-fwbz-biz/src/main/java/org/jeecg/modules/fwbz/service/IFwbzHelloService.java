package org.jeecg.modules.fwbz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.entity.FwbzHelloEntity;

/**
 * 测试接口
 */
public interface IFwbzHelloService extends IService<FwbzHelloEntity> {

    String hello();

}
