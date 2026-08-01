package org.jeecg.modules.fwbz.energyAnalysis.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import dm.jdbc.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.fwbz.dto.DeviceDataFindDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.EnergyMeteringStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointChatDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointDataStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.entity.MeteringPoint;
import org.jeecg.modules.fwbz.energyAnalysis.entity.MeteringPointData;
import org.jeecg.modules.fwbz.energyAnalysis.service.*;
import org.jeecg.modules.fwbz.energyAnalysis.util.Jexl3Util;
import org.jeecg.modules.fwbz.energyAnalysis.util.TableUtil;
import org.jeecg.modules.fwbz.energyAnalysis.vo.*;
import org.jeecg.modules.fwbz.energyAnalysis.vo.chat.PieChat;
import org.jeecg.modules.fwbz.energyAnalysis.vo.chat.PieChatSeriesData;
import org.jeecg.modules.fwbz.entity.*;
import org.jeecg.modules.fwbz.mdm.constant.DeviceConstant;
import org.jeecg.modules.fwbz.mdm.dto.DeviceRunStateStatisticsDto;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.mdm.service.IDeviceService;
import org.jeecg.modules.fwbz.mq.send.MqSendService;
import org.jeecg.modules.fwbz.service.*;
import org.jeecg.modules.fwbz.vo.DeviceDataVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.jeecg.modules.fwbz.mdm.entity.Device.DEVICE_TYPE_MEASURING;

@Service
@AllArgsConstructor
@Slf4j
public class EenergyMeteringServiceImpl implements IEenergyMeteringService {

    private final IDeviceDataService deviceDataService;


    @Override
    public EnergyMeteringStatisticsDto statistics() {

        DeviceDataFindDto params = new DeviceDataFindDto();
        params.setDeviceType(Device.DEVICE_TYPE_MEASURING);
        LocalDateTime now = LocalDateTime.now();
        params.setStartTime(now.truncatedTo(ChronoUnit.DAYS));
        params.setEndTime(now);
        List<DeviceDataVo> list = deviceDataService.findAll(params);


        Map<String, Long> collect = list.stream().filter(item -> item.getRunState() != null).collect(Collectors.groupingBy(DeviceDataVo::getRunState, Collectors.counting()));
        Map<Long, BigDecimal> collect1 = list.stream()
                .filter(item -> item.getCategoryId() != null)
                .collect(Collectors.groupingBy(DeviceDataVo::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO, DeviceDataVo::getValue, BigDecimal::add)));


        EnergyMeteringStatisticsDto dto = new EnergyMeteringStatisticsDto();
        dto.setCount((long) list.size());
        dto.setOnlineRate(new BigDecimal(
                (double) collect.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L) / list.size() * 100
        ).setScale(2, RoundingMode.HALF_UP)
                .toString());
        dto.setElectricCount(collect1.getOrDefault(DeviceConstant.CATEGORY_ELECTRICITY,  BigDecimal.ZERO));
        dto.setWaterCount(collect1.getOrDefault(DeviceConstant.CATEGORY_WATER, BigDecimal.ZERO));
        return dto;




    }



}
