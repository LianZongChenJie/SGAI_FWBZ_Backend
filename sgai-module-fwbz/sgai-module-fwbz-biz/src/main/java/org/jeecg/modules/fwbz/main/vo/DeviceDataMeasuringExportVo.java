package org.jeecg.modules.fwbz.main.vo;

import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Data
public class DeviceDataMeasuringExportVo {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 设备编号
     */
    @Excel(name = "编号", orderNum = "0")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Excel(name = "名称", orderNum = "1")
    private String deviceName;

    /**
     * 表计类型（设备类别名称）
     */
    @Excel(name = "表计类型", orderNum = "2")
    private String categoryName;

    /**
     * 安装位置（空间名称）
     */
    @Excel(name = "安装位置", orderNum = "3")
    private String spaceName;

    /**
     * 今日读数
     */
    @Excel(name = "今日读数", orderNum = "4")
    private BigDecimal value;

    /**
     * 今日用量
     */
    @Excel(name = "今日用量", orderNum = "5")
    private BigDecimal dayTotal;

    /**
     * 本月累计
     */
    @Excel(name = "本月累计", orderNum = "6")
    private BigDecimal mouthTotal;

    /**
     * 备注
     */
    @Excel(name = "备注", orderNum = "7")
    private String remark;

    /**
     * 状态（在线/离线）
     */
    @Excel(name = "状态", orderNum = "8")
    private String runState;

    /**
     * 最后采集时间
     */
    @Excel(name = "最后采集时间", orderNum = "9")
    private String lastGatherTime;

    public static DeviceDataMeasuringExportVo convert(DeviceDataVo device) {
        if (device == null) {
            return null;
        }
        DeviceDataMeasuringExportVo res = new DeviceDataMeasuringExportVo();
        res.setDeviceCode(device.getDeviceCode());
        res.setDeviceName(device.getDeviceName());
        res.setCategoryName(device.getCategoryName());
        res.setSpaceName(device.getSpaceName());
        res.setValue(device.getValue());
        res.setDayTotal(device.getDayTotal());
        res.setMouthTotal(device.getMouthTotal());
        res.setRemark(device.getRemark());
        res.setRunState(device.getRunState());
        res.setLastGatherTime(device.getLastGatherTime() != null
                ? device.getLastGatherTime().format(DATE_TIME_FORMATTER)
                : null);
        return res;
    }
}
