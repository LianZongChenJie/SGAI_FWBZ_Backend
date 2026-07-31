package org.jeecg.modules.fwbz.hikvision.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.entity.CameraResource;
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
}
