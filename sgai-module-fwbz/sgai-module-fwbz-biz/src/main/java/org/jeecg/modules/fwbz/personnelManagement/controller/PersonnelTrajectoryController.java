package org.jeecg.modules.fwbz.personnelManagement.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.personnelManagement.dto.PersonnelTrajectoryRequest;
import org.jeecg.modules.fwbz.personnelManagement.dto.PersonnelTrajectoryResultVO;
import org.jeecg.modules.fwbz.personnelManagement.service.IPersonnelTrajectoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 人员轨迹控制器
 *
 * @author fwbz
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/fwbz/personnelTrajectory")
@Api(tags = "人员轨迹查询")
public class PersonnelTrajectoryController {

    private final IPersonnelTrajectoryService personnelTrajectoryService;

    /**
     * 人员轨迹查询
     * <p>根据时间范围和人脸照片，依次调取海康人脸分组检索和以图搜图，
     * 并关联数据库中的摄像头信息（名称、位置、经纬度）。</p>
     *
     * @param request 查询请求（startTime, endTime, facePhoto）
     * @return 人员轨迹列表（含摄像头名称、安装位置、经纬度信息）
     */
    @PostMapping("/query")
    @ApiOperation(value = "人员轨迹查询", notes = "传入开始时间、结束时间、人脸照片Base64，返回人员轨迹列表")
    public Result<PersonnelTrajectoryResultVO> queryTrajectory(@RequestBody PersonnelTrajectoryRequest request) {
        try {
            PersonnelTrajectoryResultVO result = personnelTrajectoryService.queryTrajectory(request);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("人员轨迹查询失败", e);
            return Result.error("人员轨迹查询失败: " + e.getMessage());
        }
    }
}
