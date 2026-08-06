package org.jeecg.modules.fwbz.hikvision.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.entity.PersonRecognition;
import org.jeecg.modules.fwbz.hikvision.service.IPersonRecognitionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 人员识别记录接口
 *
 * @author fwbz
 */
@Api(tags = "人员识别记录")
@RestController
@RequestMapping("/fwbz/hikvision/personRecognition")
@AllArgsConstructor
public class PersonRecognitionController {

    private final IPersonRecognitionService personRecognitionService;

    /**
     * 分页查询人员识别记录
     * <p>支持按人员类型、姓名、识别位置、进出方向、场馆、员工号、识别时间范围筛选，为空查全部</p>
     */
    @GetMapping("/list")
    @ApiOperation(value = "分页查询人员识别记录", notes = "支持多条件筛选，从 table_person_recognition 分页查询")
    public Result<IPage<PersonRecognition>> getRecognitionList(
            @ApiParam(value = "页码，从1开始", defaultValue = "1") @RequestParam(defaultValue = "1") int pageNo,
            @ApiParam(value = "每页条数", defaultValue = "10") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam(value = "人员类型（员工/访客/VIP/临时人员/黑名单等）") @RequestParam(required = false) String personType,
            @ApiParam(value = "姓名，模糊匹配") @RequestParam(required = false) String personName,
            @ApiParam(value = "识别位置，模糊匹配") @RequestParam(required = false) String recognizeLocation,
            @ApiParam(value = "进出方向（进/出/未知）") @RequestParam(required = false) String direction,
            @ApiParam(value = "所属场馆，模糊匹配") @RequestParam(required = false) String venue,
            @ApiParam(value = "员工号，精确匹配") @RequestParam(required = false) String employeeNo,
            @ApiParam(value = "识别开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @ApiParam(value = "识别结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.OK(personRecognitionService.getRecognitionList(
                pageNo, pageSize, personType, personName, recognizeLocation,
                direction, venue, employeeNo, startTime, endTime));
    }
}
