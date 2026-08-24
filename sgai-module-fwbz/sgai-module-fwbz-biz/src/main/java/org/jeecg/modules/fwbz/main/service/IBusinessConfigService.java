package org.jeecg.modules.fwbz.main.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.main.entity.BusinessConfig;

import java.util.List;

public interface IBusinessConfigService extends IService<BusinessConfig> {

    void updateByKey(String key,String value);

    String getValueByKey(String key);

    Long getLongByKey(String key);

    <T> List<T> getListByKey(String key,Class<T> clazz);

    <T> T getObjectByKey(String key,Class<T> clazz);
}
