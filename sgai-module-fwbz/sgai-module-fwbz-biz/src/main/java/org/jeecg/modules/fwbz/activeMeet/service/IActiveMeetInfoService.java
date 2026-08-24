package org.jeecg.modules.fwbz.activeMeet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetInfo;
import org.jeecg.modules.fwbz.activeMeet.vo.WeekActivityVO;

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

    /**
     * 查询本周活动（按日期分组）
     */
    List<WeekActivityVO> listThisWeek();
}
