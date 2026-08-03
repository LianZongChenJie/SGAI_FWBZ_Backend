package org.jeecg.modules.fwbz.energyAnalysis.controller;

import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointChatDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointDataDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.RecalculateDto;
import org.jeecg.modules.fwbz.energyAnalysis.service.IMeteringPointDataService;
import org.jeecg.modules.fwbz.energyAnalysis.vo.Chat;
import org.jeecg.modules.fwbz.energyAnalysis.vo.MeteringPointDataChartVo;
import org.jeecg.modules.fwbz.energyAnalysis.vo.Table;
import org.jeecg.modules.fwbz.energyAnalysis.vo.chat.PieChat;
import org.jeecg.modules.fwbz.energyAnalysis.vo.chat.PieChatSeriesData;
import org.jeecg.modules.fwbz.mq.send.MqSendService;
import org.jeecg.modules.fwbz.service.IBusinessConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(tags = "计量点位数据信息")
@RestController
@RequestMapping("/fwbz/meterPointData")
@AllArgsConstructor
public class MeteringPointDataController {

    private final IMeteringPointDataService service;

    private final MqSendService mqSendService;

    @GetMapping("/findMinute")
    public Result<MeteringPointDataChartVo> findMinute(MeteringPointDataDto param){
        return Result.ok(new MeteringPointDataChartVo(service.findMinute(param.getEnergyFlowDiagramIds(), param.getHour())));
    }

    @ApiOperation(value = "查询日数据", notes = "查询日数据")
    @GetMapping("/findDay")
    public Result<MeteringPointDataChartVo> findDay(MeteringPointDataDto param) {
        return Result.ok(new MeteringPointDataChartVo(service.findDay(param.getEnergyFlowDiagramIds(), param.getDay())));
    }

    @ApiOperation(value = "查询月数据", notes = "查询月数据")
    @GetMapping("/findMonth")
    public Result<MeteringPointDataChartVo> findMonth(MeteringPointDataDto param){
        return Result.ok(new MeteringPointDataChartVo(service.findMonth(param.getEnergyFlowDiagramIds(), param.getDay())));
    }

    @ApiOperation(value = "查询年数据", notes = "查询年数据")
    @GetMapping("/findYear")
    public Result<MeteringPointDataChartVo> findYear(MeteringPointDataDto param){
        return Result.ok(new MeteringPointDataChartVo(service.findYear(param.getEnergyFlowDiagramIds(), param.getDay())));
    }

    @ApiOperation(value = "重新计算", notes = "重新计算")
    @AutoLog(value = "计量点位-重新计算")
//    @RequiresPermissions("Fwbz:meterPointData:calculateValue")
    @PostMapping("/calculateValue")
    public Result<String> calculateValue(@RequestBody MeteringPointDataDto param){
        service.calculateValue(param.getHour());
        return Result.ok("计算成功");
    }

    /**
     * 重新计算，批量
     * @param param
     * @return
     */
    @PostMapping("/recalculate")
    public Result<String> recalculate(@RequestBody RecalculateDto param){
        if(param.getTime() == null || StrUtil.isEmpty(param.getMeteringPointIds())){
            return Result.error("参数错误");
        }
        String[] split = param.getMeteringPointIds().split(",");
        for(String id : split) {
            mqSendService.sendMeteringPointValueUpdate(Long.valueOf(id),param.getTime());
        }
        return Result.ok();
    }

    /**
     * 饼图
     */
    @GetMapping("/findPieChat")
    public Result<PieChat> findPieChat(MeteringPointChatDto param){
        return Result.ok(service.findPieChat(param));
    }

    /**
     * 折线图
     */
    @GetMapping("/findLineChat")
    public Result<Chat> findLineChat(MeteringPointChatDto param){
        return Result.ok(service.findLineChat(param));
    }

    /**
     * 柱状图
     */
    @GetMapping("/findBarChat")
    public Result<Chat> findBarChat(MeteringPointChatDto param){
        return Result.ok(service.findBarChat(param));
    }

    /**
     * 堆叠柱状图
     */
    @GetMapping("/findStackedColumnChart")
    public Result<Chat> findStackedColumnChart(MeteringPointChatDto param){
        return Result.ok(service.findStackedColumnChart(param));
    }


    /**
     * 计量分析数据统计
     * @return 统计结果
     */
    @GetMapping("/statistics")
    public Result<?> statistics(){
        return Result.ok(service.statistics());
    }


    /**
     * 各场馆用电量柱状图
     * @param param
     * @return
     */
    @GetMapping("/findDayByConfig")
    public Result<MeteringPointDataChartVo> findDayByConfig(MeteringPointDataDto param){
        return Result.ok(new MeteringPointDataChartVo(service.findDayByConfig(param.getBusinessConfigKey(),param.getEnergyFlowDiagramIds(), param.getDay())));
    }
    /**
     * 各场馆用电量柱状图
     * @param param
     * @return
     */
    @GetMapping("/findMonthByConfig")
    public Result<MeteringPointDataChartVo> findMonthByConfig(MeteringPointDataDto param){
        return Result.ok(new MeteringPointDataChartVo(service.findMonthByConfig(param.getBusinessConfigKey(),param.getEnergyFlowDiagramIds(), param.getDay())));
    }
    /**
     * 各场馆用电量柱状图
     * @param param
     * @return
     */
    @GetMapping("/findYearByConfig")
    public Result<MeteringPointDataChartVo> findYearByConfig(MeteringPointDataDto param){
        return Result.ok(new MeteringPointDataChartVo(service.findYearByConfig(param.getBusinessConfigKey(),param.getEnergyFlowDiagramIds(), param.getDay())));
    }




    /**
     * 用能结构分析
     * @param param
     * @return
     */
    @GetMapping("/findPieDayByConfig")
    public Result<List<PieChatSeriesData>> findPieDayByConfig(MeteringPointDataDto param){
        return Result.ok(service.findPieDayByConfig(param.getBusinessConfigKey(), param.getEnergyFlowDiagramIds(), param.getDay()));
    }
    /**
     * 用能结构分析
     * @param param
     * @return
     */
    @GetMapping("/findPieMonthByConfig")
    public Result<List<PieChatSeriesData>> findPieMonthByConfig(MeteringPointDataDto param){
        return Result.ok(service.findPieMonthByConfig(param.getBusinessConfigKey(), param.getEnergyFlowDiagramIds(), param.getDay()));
    }
    /**
     * 用能结构分析
     * @param param
     * @return
     */
    @GetMapping("/findPieYearByConfig")
    public Result<List<PieChatSeriesData>> findPieYearByConfig(MeteringPointDataDto param){
        return Result.ok(service.findPieYearByConfig(param.getBusinessConfigKey(), param.getEnergyFlowDiagramIds(), param.getDay()));

    }





}
