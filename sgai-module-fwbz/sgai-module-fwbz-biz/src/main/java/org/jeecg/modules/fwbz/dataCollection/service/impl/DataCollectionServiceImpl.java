package org.jeecg.modules.fwbz.dataCollection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
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

    @Override
    public StatCardVO collectionPointCount() {
        List<InterfaceInfo> all = interfaceInfoMapper.selectList(null);
        long totalPoints = all.stream()
                .mapToLong(i -> i.getCollectionPointLocation() != null ? i.getCollectionPointLocation() : 0)
                .sum();

        Date todayStart = getTodayStart();
        List<InterfaceInfo> todayNew = interfaceInfoMapper.selectList(
                new LambdaQueryWrapper<InterfaceInfo>()
                        .ge(InterfaceInfo::getCreateTime, todayStart));
        long newPoints = todayNew.stream()
                .mapToLong(i -> i.getCollectionPointLocation() != null ? i.getCollectionPointLocation() : 0)
                .sum();

        StatCardVO vo = new StatCardVO();
        vo.setTitle("采集点位数");
        vo.setValue(totalPoints);
        vo.setContext(newPoints > 0 ? "↑" + newPoints + " 新增" : "全部覆盖");
        return vo;
    }

    @Override
    public StatCardVO todayCollectionAmount() {
        Double todaySum = interfaceHistoryMapper.selectDataSizeSum(new Date());
        Double yesterdaySum = interfaceHistoryMapper.selectDataSizeSum(getYesterdayDate());

        todaySum = todaySum != null ? todaySum : 0;
        yesterdaySum = yesterdaySum != null ? yesterdaySum : 0;

        double diff = yesterdaySum > 0 ? (todaySum - yesterdaySum) / yesterdaySum * 100 : 0;

        StatCardVO vo = new StatCardVO();
        vo.setTitle("今日采集量");
        vo.setValue(Math.round(todaySum * 100) / 100.0);
        if (diff > 0) {
            vo.setContext("↑" + String.format("%.2f", diff) + "% 较昨日");
        } else if (diff < 0) {
            vo.setContext("↓" + String.format("%.2f", -diff) + "% 较昨日");
        } else {
            vo.setContext("较昨日持平");
        }
        return vo;
    }

    @Override
    public StatCardVO dataCompletenessRate() {
        List<InterfaceInfo> all = interfaceInfoMapper.selectList(null);
        if (all.isEmpty()) {
            StatCardVO vo = new StatCardVO();
            vo.setTitle("数据完整率");
            vo.setValue(0);
            vo.setContext("暂无接口");
            return vo;
        }

        int total = all.size();
        int online = (int) all.stream().filter(i -> InterfaceInfo.STATE_ONLINE.equals(i.getState())).count();
        double todayRate = (double) online * 100 / total;

        Date todayStart = getTodayStart();
        List<InterfaceInfo> beforeToday = interfaceInfoMapper.selectList(
                new LambdaQueryWrapper<InterfaceInfo>()
                        .lt(InterfaceInfo::getCreateTime, todayStart));

        double yesterdayRate;
        if (beforeToday.isEmpty()) {
            yesterdayRate = todayRate;
        } else {
            int totalBefore = beforeToday.size();
            int onlineBefore = (int) beforeToday.stream().filter(i -> InterfaceInfo.STATE_ONLINE.equals(i.getState())).count();
            yesterdayRate = (double) onlineBefore * 100 / totalBefore;
        }

        double diff = todayRate - yesterdayRate;

        StatCardVO vo = new StatCardVO();
        vo.setTitle("数据完整率");
        vo.setValue(Math.round(todayRate * 100) / 100.0);
        if (diff > 0) {
            vo.setContext("↑" + String.format("%.1f", diff) + "% 较昨日");
        } else if (diff < 0) {
            vo.setContext("↓" + String.format("%.1f", -diff) + "% 较昨日");
        } else {
            vo.setContext("较昨日持平");
        }
        return vo;
    }

    @Override
    public StatCardVO storageCapacity() {
        Double total = interfaceHistoryMapper.selectTotalDataSizeSum();
        total = total != null ? total : 0;

        Date monthStart = getMonthStart();
        Date monthEnd = getMonthEnd();
        Double monthSum = interfaceHistoryMapper.selectDataSizeSumByRange(monthStart, monthEnd);
        monthSum = monthSum != null ? monthSum : 0;

        StatCardVO vo = new StatCardVO();
        vo.setTitle("存储容量");
        vo.setValue(Math.round(total * 100) / 100.0);
        vo.setContext("↑" + formatSize(monthSum) + " 本月");
        return vo;
    }

    @Override
    public List<StatCardVO> getSummary() {
        return Arrays.asList(collectionPointCount(), todayCollectionAmount(), dataCompletenessRate(), storageCapacity());
    }

    // ============ helper methods ============

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

    private Date getYesterdayDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }

    private Date getMonthStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getMonthEnd() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    private String formatSize(double kb) {
        if (kb >= 1024 * 1024 * 1024) {
            return String.format("%.1fTB", kb / (1024 * 1024 * 1024));
        } else if (kb >= 1024 * 1024) {
            return String.format("%.1fGB", kb / (1024 * 1024));
        } else if (kb >= 1024) {
            return String.format("%.1fMB", kb / 1024);
        }
        return String.format("%.1fKB", kb);
    }
}
