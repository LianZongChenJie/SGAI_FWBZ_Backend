package org.jeecg.modules.fwbz.dataCollection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.fwbz.dataCollection.service.IDataCollectionService;
import org.jeecg.modules.fwbz.dataCollection.vo.InterfaceListVO;
import org.jeecg.modules.fwbz.dataInterface.entity.InterfaceInfo;
import org.jeecg.modules.fwbz.dataInterface.mapper.InterfaceInfoMapper;
import org.jeecg.modules.fwbz.interfaceStatistics.entity.InterfaceHistory;
import org.jeecg.modules.fwbz.interfaceStatistics.mapper.InterfaceHistoryMapper;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataCollectionServiceImpl implements IDataCollectionService {

    private final InterfaceInfoMapper interfaceInfoMapper;
    private final InterfaceHistoryMapper interfaceHistoryMapper;

    public DataCollectionServiceImpl(InterfaceInfoMapper interfaceInfoMapper,
                                      InterfaceHistoryMapper interfaceHistoryMapper) {
        this.interfaceInfoMapper = interfaceInfoMapper;
        this.interfaceHistoryMapper = interfaceHistoryMapper;
    }

    @Override
    public List<InterfaceListVO> getInterfaceList() {
        // 1. 查询所有接口
        List<InterfaceInfo> interfaces = interfaceInfoMapper.selectList(null);

        // 2. 查询今日所有历史记录，按 system_id 聚合当日采集量
        Date todayStart = getTodayStart();
        Date todayEnd = getTodayEnd();
        List<InterfaceHistory> todayHistories = interfaceHistoryMapper.selectList(
                new LambdaQueryWrapper<InterfaceHistory>()
                        .ge(InterfaceHistory::getClinetDate, todayStart)
                        .le(InterfaceHistory::getClinetDate, todayEnd));

        Map<Long, Double> todayCollectionMap = todayHistories.stream()
                .collect(Collectors.groupingBy(
                        InterfaceHistory::getSystemId,
                        Collectors.summingDouble(h -> h.getDataSize() != null ? h.getDataSize() : 0)
                ));

        // 3. 组装结果
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm:ss");
        List<InterfaceListVO> result = new ArrayList<>();
        for (InterfaceInfo info : interfaces) {
            InterfaceListVO vo = new InterfaceListVO();
            vo.setId(info.getId());
            vo.setSysName(info.getSysName());
            vo.setInterfacePath(info.getInterfacePath());
            vo.setProtocolTypeId(info.getProtocolTypeId());
            vo.setState(info.getState());
            vo.setRequestTime(info.getRequestTime());
            vo.setResponseTime(info.getResponseTime());
            vo.setTestPath(info.getTestPath());
            vo.setCycle(info.getCycle());
            vo.setCollectionPointLocation(info.getCollectionPointLocation());
            vo.setSysOrgCode(info.getSysOrgCode());

            // 今日采集量
            vo.setTodayCollection(todayCollectionMap.getOrDefault(info.getId(), 0.0));

            // 数据完整率：在线→100，其他→0
            vo.setDataCompleteRate(
                    InterfaceInfo.STATE_ONLINE.equals(info.getState()) ? 100 : 0);

            // 最后采集时间
            InterfaceHistory latest = interfaceHistoryMapper.selectLatestBySystemId(info.getId());
            if (latest != null && latest.getClinetDate() != null) {
                String datePart = dateFmt.format(latest.getClinetDate());
                String timePart = latest.getClinetTime() != null ? timeFmt.format(latest.getClinetTime()) : "00:00:00";
                vo.setLastCollectionTime(datePart + " " + timePart);
            }

            result.add(vo);
        }
        return result;
    }

    private Date getTodayStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getTodayEnd() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
}
