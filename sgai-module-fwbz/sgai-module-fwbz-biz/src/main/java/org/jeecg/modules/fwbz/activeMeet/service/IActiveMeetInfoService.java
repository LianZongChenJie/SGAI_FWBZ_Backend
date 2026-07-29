package org.jeecg.modules.fwbz.activeMeet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetInfo;

import java.util.List;

public interface IActiveMeetInfoService extends IService<ActiveMeetInfo> {

    /**
     * 分页查询
     */
    IPage<ActiveMeetInfo> listPage(ActiveMeetInfo params);

    /**
     * 查询全部
     */
    List<ActiveMeetInfo> listAll();
}
