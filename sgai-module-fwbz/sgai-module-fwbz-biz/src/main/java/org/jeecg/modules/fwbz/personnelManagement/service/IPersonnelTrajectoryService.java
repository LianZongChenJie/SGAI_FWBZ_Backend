package org.jeecg.modules.fwbz.personnelManagement.service;

import org.jeecg.modules.fwbz.personnelManagement.dto.PersonnelTrajectoryRequest;
import org.jeecg.modules.fwbz.personnelManagement.dto.PersonnelTrajectoryResultVO;

/**
 * 人员轨迹服务接口
 *
 * @author fwbz
 */
public interface IPersonnelTrajectoryService {

    /**
     * 查询人员轨迹
     * <p>根据时间范围和人脸照片，依次调取海康人脸分组检索和以图搜图，
     * 并将以图搜图结果中的摄像头编码关联数据库获取摄像头详细信息。</p>
     *
     * @param request 查询请求（包含开始时间、结束时间、人脸照片Base64）
     * @return 人员轨迹结果（含1:N识别信息和轨迹摄像头列表）
     */
    PersonnelTrajectoryResultVO queryTrajectory(PersonnelTrajectoryRequest request);
}
