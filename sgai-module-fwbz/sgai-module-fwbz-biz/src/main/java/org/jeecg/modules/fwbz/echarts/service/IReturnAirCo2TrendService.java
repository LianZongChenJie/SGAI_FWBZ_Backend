package org.jeecg.modules.fwbz.echarts.service;

import org.jeecg.modules.fwbz.echarts.dto.ActivePowerTrendQueryDto;
import org.jeecg.modules.fwbz.echarts.dto.ReturnAirCo2TrendQueryDto;
import org.jeecg.modules.fwbz.echarts.vo.ReturnAirCo2TrendVo;

/**
 * 设备属性趋势图服务
 *
 * @author sgai-fwbz
 */
public interface IReturnAirCo2TrendService {

    /**
     * 查询设备某属性历史值并组装为 ECharts 折线图数据
     * <p>
     * 逻辑：
     * <ol>
     *   <li>按 deviceId + attributeName 从 {@code device_attribute} 定位属性</li>
     *   <li>从 {@code device_attribute_history} 按时间范围取历史</li>
     *   <li>按 deviceId 分组、按时间桶（默认 1 小时）聚合</li>
     *   <li>填齐 xAxis 时间槽，缺失值填 null</li>
     * </ol>
     *
     * @param query 查询参数
     * @return ECharts 折线图数据
     */
    ReturnAirCo2TrendVo getReturnAirCo2Trend(ReturnAirCo2TrendQueryDto query);

    /**
     * 查询电表"总有功功率"历史趋势并组装为 ECharts 折线图数据
     * <p>
     * 逻辑：
     * <ol>
     *   <li>从 {@code table_mqtt_history} 按设备 ID + desc 模糊匹配"总有功功率" + 时间范围取遥测历史</li>
     *   <li>按 deviceId 分组、按时间桶（默认 1 小时）聚合（桶内多值取平均）</li>
     *   <li>填齐 xAxis 时间槽，缺失值填 0</li>
     * </ol>
     *
     * @param query 查询参数
     * @return ECharts 折线图数据
     */
    ReturnAirCo2TrendVo getActivePowerTrend(ActivePowerTrendQueryDto query);
}
