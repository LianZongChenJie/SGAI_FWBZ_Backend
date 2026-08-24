package org.jeecg.modules.fwbz.alarm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.alarm.entity.AlarmLevel;

public interface IAlarmLevelService extends IService<AlarmLevel> {

    IPage<AlarmLevel> listPage(AlarmLevel params);

    void startLevel(Long id);

    void stopLevel(Long id);
}
