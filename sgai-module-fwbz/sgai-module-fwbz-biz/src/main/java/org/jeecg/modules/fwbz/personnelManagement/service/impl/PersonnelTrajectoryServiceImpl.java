package org.jeecg.modules.fwbz.personnelManagement.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.hikvision.entity.CameraResource;
import org.jeecg.modules.fwbz.hikvision.dto.CaptureSearchResponse;
import org.jeecg.modules.fwbz.hikvision.dto.FaceGroupSearchResponse;
import org.jeecg.modules.fwbz.hikvision.service.ICameraResourceService;
import org.jeecg.modules.fwbz.hikvision.service.ICaptureSearchService;
import org.jeecg.modules.fwbz.hikvision.service.IFaceGroupSearchService;
import org.jeecg.modules.fwbz.personnelManagement.dto.PersonnelTrajectoryRequest;
import org.jeecg.modules.fwbz.personnelManagement.dto.PersonnelTrajectoryResultVO;
import org.jeecg.modules.fwbz.personnelManagement.dto.PersonnelTrajectoryVO;
import org.jeecg.modules.fwbz.personnelManagement.service.IPersonnelTrajectoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 人员轨迹服务实现
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class PersonnelTrajectoryServiceImpl implements IPersonnelTrajectoryService {

    private final IFaceGroupSearchService faceGroupSearchService;
    private final ICaptureSearchService captureSearchService;
    private final ICameraResourceService cameraResourceService;

    /** 海康人脸分组唯一标志列表（固定值） */
    private static final String[] FACE_GROUP_INDEX_CODES = {"your-face-group-code"};

    @Override
    public PersonnelTrajectoryResultVO queryTrajectory(PersonnelTrajectoryRequest request) {
        String startTime = request.getStartTime();
        String endTime = request.getEndTime();
        String facePhoto = request.getFacePhoto();

        log.info("开始查询人员轨迹, startTime={}, endTime={}", startTime, endTime);

        PersonnelTrajectoryResultVO resultVO = new PersonnelTrajectoryResultVO();

        // 第一步：调取海康人脸识别分组信息（1:N检索）
        FaceGroupSearchResponse.FaceGroupSearchItem bestGroupMatch = null;
        log.info("开始海康人脸分组检索, faceGroupIndexCodes={}", (Object) FACE_GROUP_INDEX_CODES);
        try {
            FaceGroupSearchResponse groupResponse = faceGroupSearchService.oneToMany(facePhoto, FACE_GROUP_INDEX_CODES);
            List<FaceGroupSearchResponse.FaceGroupSearchItem> groupItems = groupResponse.getList();
            if (groupItems != null && !groupItems.isEmpty()) {
                bestGroupMatch = groupItems.get(0);
                log.info("人脸分组检索完成, 匹配到{}条记录, 最高相似度={}, 姓名={}",
                        groupItems.size(), bestGroupMatch.getSimilarity(),
                        bestGroupMatch.getFaceInfo() != null ? bestGroupMatch.getFaceInfo().getName() : "未知");
            } else {
                log.warn("人脸分组检索未匹配到任何人员, 继续执行以图搜图");
            }
        } catch (Exception e) {
            log.error("人脸分组检索异常, 继续执行以图搜图", e);
        }

        // 设置1:N识别信息到顶层
        if (bestGroupMatch != null) {
            resultVO.setSimilarity(bestGroupMatch.getSimilarity());
            FaceGroupSearchResponse.FaceInfo faceInfo = bestGroupMatch.getFaceInfo();
            if (faceInfo != null) {
                resultVO.setName(faceInfo.getName());
                resultVO.setCertificateType(faceInfo.getCertificateType());
                resultVO.setCertificateNum(faceInfo.getCertificateNum());
            }
            FaceGroupSearchResponse.FacePic facePic = bestGroupMatch.getFacePic();
            if (facePic != null) {
                resultVO.setFaceUrl(facePic.getFaceUrl());
            }
        }

        // 第二步：调取海康以图搜图功能
        CaptureSearchResponse searchResponse;
        try {
            searchResponse = captureSearchService.searchByImage(facePhoto, startTime, endTime);
        } catch (Exception e) {
            log.error("海康以图搜图失败", e);
            throw new RuntimeException("海康以图搜图失败: " + e.getMessage(), e);
        }

        List<CaptureSearchResponse.CaptureSearchItem> searchItems = searchResponse.getList();
        if (searchItems == null || searchItems.isEmpty()) {
            log.info("以图搜图未查到匹配记录, 返回空列表");
            resultVO.setCameraList(Collections.emptyList());
            return resultVO;
        }

        log.info("以图搜图完成, 共{}条记录", searchItems.size());

        // 第三步：提取所有摄像头编码，批量查询数据库获取摄像头信息
        List<String> cameraIndexCodes = searchItems.stream()
                .map(CaptureSearchResponse.CaptureSearchItem::getCameraIndexCode)
                .distinct()
                .collect(Collectors.toList());

        Map<String, CameraResource> cameraMap = queryCameraMap(cameraIndexCodes);
        log.info("摄像头信息查询完成, 匹配到{}/{}个摄像头", cameraMap.size(), cameraIndexCodes.size());

        // 第四步：组装cameraList轨迹列表
        List<PersonnelTrajectoryVO> cameraList = new ArrayList<>(searchItems.size());
        for (CaptureSearchResponse.CaptureSearchItem item : searchItems) {
            PersonnelTrajectoryVO vo = new PersonnelTrajectoryVO();
            // 抓拍信息
            vo.setCaptureTime(item.getCaptureTime());
            vo.setSimilarity(item.getSimilarity());
            vo.setBkgPicUrl(item.getBkgPicUrl());
            vo.setFacePicUrl(item.getFacePicUrl());

            // 摄像头信息（从数据库关联）
            String cameraIndexCode = item.getCameraIndexCode();
            vo.setCameraIndexCode(cameraIndexCode);
            CameraResource camera = cameraMap.get(cameraIndexCode);
            if (camera != null) {
                vo.setCameraName(camera.getName());
                vo.setInstallLocation(camera.getInstallLocation());
                vo.setLongitude(camera.getLongitude());
                vo.setLatitude(camera.getLatitude());
            }

            cameraList.add(vo);
        }

        resultVO.setCameraList(cameraList);
        log.info("人员轨迹查询完成, 返回{}条轨迹记录", cameraList.size());
        return resultVO;
    }

    /**
     * 根据摄像头编码列表批量查询摄像头信息
     *
     * @param cameraIndexCodes 摄像头编码列表
     * @return cameraIndexCode -> CameraResource 映射
     */
    private Map<String, CameraResource> queryCameraMap(List<String> cameraIndexCodes) {
        if (cameraIndexCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        List<CameraResource> cameraList = cameraResourceService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CameraResource>()
                        .in(CameraResource::getIndexCode, cameraIndexCodes)
        );
        return cameraList.stream().collect(Collectors.toMap(
                CameraResource::getIndexCode,
                camera -> camera,
                (v1, v2) -> v1
        ));
    }
}
