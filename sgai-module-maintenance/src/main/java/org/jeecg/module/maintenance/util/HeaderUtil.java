package org.jeecg.module.maintenance.util;


import org.jeecg.module.maintenance.dto.TableHeader;
import org.jeecg.module.maintenance.dto.WeekOfMonth;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 描述:
 *
 * @author ppliu
 * created in 2021/8/13 17:47
 */
public class HeaderUtil {
    private static Map<Integer, List<WeekOfMonth>> yearWeekMonthMap = new HashMap<>();

    public static TableHeader getHeader(int year, String labelType) {

        String labelName = "";
        if(labelType.equals("maintenance")){
            labelName = "维保";
        } else if (labelType.equals("verification")) {
            labelName = "检定";
        }else{
            labelName = "巡检";
        }


        TableHeader root = new TableHeader(year + "年度设备设施"+labelName+"计划一览表");
        //正常表头部分.
        List<TableHeader> fieldHeader = new ArrayList<>(Arrays.asList(
                new TableHeader("name", "计划工作项目"),
                new TableHeader("count", "数量"),
                new TableHeader("factory", "厂家"),
                new TableHeader("associatedDevice", "是否关联设备"),
                /*new TableHeader("associatedSpace", "是否关联空间"),*/
                new TableHeader("cycle", labelName+"周期"),
                new TableHeader("unit", "单位"),
                new TableHeader("frequency", "建议频次"),
                new TableHeader("duration", "持续执行时间"),
                new TableHeader("principal", "负责人"),
                new TableHeader("department", "执行科组"),
                new TableHeader("weibaoType", "类型")
        ));
        //时间表头部分.
        TableHeader yearHeader = new TableHeader("时间安排");

        fieldHeader.add(yearHeader);

        List<TableHeader> monthHeaderList = new ArrayList<>();
        Map<String, List<String>> timeMap = getTimeHeaderMap(year);

        timeMap.forEach((k, v) -> {
            TableHeader monthHeader = new TableHeader(k);
            List<TableHeader> weekHeaderList = v.stream().map(s -> new TableHeader("w" + s.substring(s.indexOf("第") + 1, s.indexOf("周")), s)).collect(Collectors.toList());
            monthHeader.setChildren(weekHeaderList);
            monthHeaderList.add(monthHeader);
        });
        yearHeader.setChildren(monthHeaderList);

        root.setChildren(fieldHeader);
        return root;

    }

    public static Map<String, List<String>> getTimeHeaderMap(int year) {
        List<WeekOfMonth> weekOfMonthList = getWeekOfMonths(year);
        Map<String, List<String>> map = new LinkedHashMap<>();
        weekOfMonthList.stream().collect(Collectors.groupingBy(weekOfMonth -> weekOfMonth.getLocalDate().getMonth().getValue())).forEach((k, v) -> {
            List<String> weekList = v.stream().map(weekOfMonth -> {
                LocalDate date = weekOfMonth.getLocalDate();
                int index = weekOfMonth.getIndex();
                return "第" + index + "周(" + date.getDayOfMonth() + "-" + date.with(DayOfWeek.FRIDAY).getDayOfMonth() + ")";
            }).collect(Collectors.toList());
            map.put(k + "月", weekList);
        });
        return map;
    }

    public static List<WeekOfMonth> getWeekOfMonths(int year) {
        List<WeekOfMonth> list = yearWeekMonthMap.get(year);
        if (list != null) {
            return list;
        }
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        List<LocalDate> localDateList = LocalDateUtil.getAllDaysWithRange(from, to).stream()
                .filter(localDate -> localDate.getDayOfWeek().getValue() == 1 || (localDate.getDayOfYear() == 1 && !Arrays.asList(6, 7).contains(localDate.getDayOfWeek().getValue())))
                .collect(Collectors.toList());

        final int[] indexArray = {1};
        yearWeekMonthMap.put(year, localDateList.stream().map(date -> {
            WeekOfMonth weekOfMonth = new WeekOfMonth();
            weekOfMonth.setIndex(indexArray[0]);
            weekOfMonth.setLocalDate(date);
            indexArray[0]++;
            return weekOfMonth;
        }).collect(Collectors.toList()));

        return yearWeekMonthMap.get(year);
    }

    public static void main(String[] args) {
        TableHeader header = getHeader(2020, null);
    }
}
