package org.jeecg.modules.fwbz.activeMeet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetInfo;
import org.jeecg.modules.fwbz.activeMeet.vo.WeekActivityVO;

import java.util.Date;
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
     * 按日期范围查询活动（含场馆名称）
     * <p>startDate、endDate 可空：都为空查全部；只有开始日期查其之后；只有结束日期查其之前；都有查之间。</p>
     */
    List<ActiveMeetInfo> listByDateRange(Date startDate, Date endDate);

    /**
     * 查询本周活动（按日期分组）
     */
    List<WeekActivityVO> listThisWeek();
}
