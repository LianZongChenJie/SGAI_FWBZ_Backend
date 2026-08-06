package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.fwbz.hikvision.entity.PersonRecognition;
import org.jeecg.modules.fwbz.hikvision.mapper.PersonRecognitionMapper;
import org.jeecg.modules.fwbz.hikvision.service.IPersonRecognitionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 人员识别记录服务实现
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class PersonRecognitionServiceImpl extends ServiceImpl<PersonRecognitionMapper, PersonRecognition>
        implements IPersonRecognitionService {

    @Override
    public IPage<PersonRecognition> getRecognitionList(int pageNo, int pageSize,
                                                        String personType, String personName,
                                                        String recognizeLocation, String direction,
                                                        String venue, String employeeNo,
                                                        LocalDateTime startTime, LocalDateTime endTime) {
        log.info("分页查询人员识别记录, pageNo={}, pageSize={}, personType={}, personName={}, recognizeLocation={}, direction={}, venue={}, employeeNo={}, startTime={}, endTime={}",
                pageNo, pageSize, personType, personName, recognizeLocation, direction, venue, employeeNo, startTime, endTime);

        LambdaQueryWrapper<PersonRecognition> wrapper = new LambdaQueryWrapper<PersonRecognition>()
                .eq(StringUtils.isNotBlank(personType), PersonRecognition::getPersonType, personType)
                .like(StringUtils.isNotBlank(personName), PersonRecognition::getPersonName, personName)
                .like(StringUtils.isNotBlank(recognizeLocation), PersonRecognition::getRecognizeLocation, recognizeLocation)
                .eq(StringUtils.isNotBlank(direction), PersonRecognition::getDirection, direction)
                .like(StringUtils.isNotBlank(venue), PersonRecognition::getVenue, venue)
                .eq(StringUtils.isNotBlank(employeeNo), PersonRecognition::getEmployeeNo, employeeNo)
                .ge(startTime != null, PersonRecognition::getRecognizeTime, startTime)
                .le(endTime != null, PersonRecognition::getRecognizeTime, endTime)
                .orderByDesc(PersonRecognition::getRecognizeTime);

        IPage<PersonRecognition> resultPage = page(new Page<>(pageNo, pageSize), wrapper);

        log.info("分页查询人员识别记录完成, 共{}条, 当前页{}条", resultPage.getTotal(), resultPage.getRecords().size());
        return resultPage;
    }
}
