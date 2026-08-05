package org.jeecg.modules.fwbz.hikvision.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.hikvision.entity.CameraResource;
import org.jeecg.modules.fwbz.hikvision.dto.CameraListVO;
import org.jeecg.modules.fwbz.hikvision.dto.CameraPlayUrlVO;

import java.util.List;

/**
 * 摄像头资源同步服务接口
 *
 * @author fwbz
 */
public interface ICameraResourceService extends IService<CameraResource> {

    /**
     * 从海康平台全量拉取摄像头数据并同步到数据库
     * <p>同步逻辑：先清空表内全部数据，再逐页拉取海康数据批量插入。</p>
     *
     * @return 同步成功的记录数
     */
    int syncFromHikvision();

    /**
     * 根据摄像头唯一编码列表，从海康平台获取播放地址
     *
     * @param cameraIndexCodes 摄像头唯一编码列表（1个或多个）
     * @return 每个摄像头的播放地址列表
     */
    List<CameraPlayUrlVO> getPlayUrls(List<String> cameraIndexCodes);

    /**
     * 从海康平台查询监控点在线状态并更新到数据库
     * <p>逐页拉取海康在线状态数据，根据 indexCode 匹配更新 table_camera_resource 的 online 字段。</p>
     *
     * @return 更新的记录数
     */
    int syncOnlineStatus();

    /**
     * 查询本地数据库中全部摄像头列表
     *
     * @return 摄像头列表
     */
    List<CameraListVO> getCameraList();
}
