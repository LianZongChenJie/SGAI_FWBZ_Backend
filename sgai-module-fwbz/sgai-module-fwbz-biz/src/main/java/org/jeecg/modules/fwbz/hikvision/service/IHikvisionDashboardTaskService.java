package org.jeecg.modules.fwbz.hikvision.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.fwbz.hikvision.dto.CurrentOnsiteCountVO;
import org.jeecg.modules.fwbz.hikvision.dto.StatCardVO;
import org.jeecg.modules.fwbz.hikvision.dto.TodayEntryCountVO;
import org.jeecg.modules.fwbz.hikvision.entity.EventNotify;
import org.jeecg.modules.fwbz.hikvision.entity.PersonRecognition;

import java.util.List;

/**
 * 海康数据看板定时任务服务
 * <p>定时从海康API获取看板数据并存入数据库，前端接口从数据库读取。</p>
 *
 * @author fwbz
 */
public interface IHikvisionDashboardTaskService {

    /** 同步客流数据到 table_visitor_flow */
    void syncVisitorFlow();

    /** 同步人员统计数据到 table_personnel_statistics */
    void syncPersonnelStatistics();

    /** 从海康API同步人员识别记录到 table_person_recognition */
    void syncPersonRecognition();

    /** 从 table_personnel_statistics 查询今日进场人数 */
    TodayEntryCountVO queryTodayEntryCount();

    /** 从 table_personnel_statistics 查询当前在场人数 */
    CurrentOnsiteCountVO queryCurrentOnsiteCount();

    /** 从 table_person_recognition 分页查询今日人员识别记录 */
    Page<PersonRecognition> queryRecognitionRecords(int pageNo, int pageSize);

    /** 从 table_event_notify 分页查询今日异常行为预警 */
    Page<EventNotify> queryAbnormalAlerts(int pageNo, int pageSize);

    // ========== 看板统计卡片 ==========

    /** 今日进场人数统计卡片（从 table_visitor_flow，含较昨日趋势） */
    StatCardVO getTodayEntryCard();

    /** 当前在场人数统计卡片（从 table_visitor_flow，含较昨日趋势） */
    StatCardVO getCurrentOnsiteCard();

    /** 人员识别记录统计卡片（从 table_person_recognition，含较昨日趋势） */
    StatCardVO getRecognitionRecordCard();

    /** 异常行为预警统计卡片（从 table_event_notify，含较昨日趋势） */
    StatCardVO getAbnormalAlertCard();

    /** 汇总看板四个统计卡片 */
    List<StatCardVO> getSummaryCards();
}
