package org.jeecg.modules.fwbz.energyAnalysis.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import dm.jdbc.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointChatDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointDataStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.entity.MeteringPoint;
import org.jeecg.modules.fwbz.energyAnalysis.entity.MeteringPointData;
import org.jeecg.modules.fwbz.energyAnalysis.service.*;
import org.jeecg.modules.fwbz.energyAnalysis.util.Jexl3Util;
import org.jeecg.modules.fwbz.energyAnalysis.util.TableUtil;
import org.jeecg.modules.fwbz.energyAnalysis.vo.*;
import org.jeecg.modules.fwbz.energyAnalysis.vo.chat.PieChat;
import org.jeecg.modules.fwbz.energyAnalysis.vo.chat.PieChatSeriesData;
import org.jeecg.modules.fwbz.entity.*;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.mdm.service.IDeviceService;
import org.jeecg.modules.fwbz.mq.send.MqSendService;
import org.jeecg.modules.fwbz.service.*;
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

@Service
@AllArgsConstructor
@Slf4j
public class EenergyMeteringServiceImpl implements IEenergyMeteringService {

//    private final IDeviceMeterDataService deviceMeterDataService;

    @Override
    public IPage<Device> deviceMeterDataList(Device device){


//        deviceMeterDataService.list();
        return null;
    }




}
