package org.jeecg.modules.fwbz.hikvision.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.hikvision.entity.PersonRecognition;

import java.time.LocalDateTime;

/**
 * 人员识别记录服务接口
 *
 * @author fwbz
 */
public interface IPersonRecognitionService extends IService<PersonRecognition> {

    /**
     * 分页查询人员识别记录，支持按人员类型、姓名、识别位置、置信度、进出方向、场馆、员工号、时间范围筛选
     *
     * @param pageNo              页码，从1开始
     * @param pageSize            每页条数
     * @param personType          人员类型（员工/访客/VIP/临时人员/黑名单等），为空查全部
     * @param personName          姓名，模糊匹配
     * @param recognizeLocation   识别位置，模糊匹配
     * @param direction           进出方向（进/出/未知），为空查全部
     * @param venue               所属场馆，模糊匹配
     * @param employeeNo          员工号，精确匹配
     * @param startTime           识别开始时间，为空不限制
     * @param endTime             识别结束时间，为空不限制
     * @return 人员识别记录分页列表
     */
    IPage<PersonRecognition> getRecognitionList(int pageNo, int pageSize,
                                                 String personType, String personName,
                                                 String recognizeLocation, String direction,
                                                 String venue, String employeeNo,
                                                 LocalDateTime startTime, LocalDateTime endTime);
}
