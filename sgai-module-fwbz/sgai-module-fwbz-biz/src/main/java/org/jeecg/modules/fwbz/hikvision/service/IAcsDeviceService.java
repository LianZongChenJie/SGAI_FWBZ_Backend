package org.jeecg.modules.fwbz.hikvision.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.entity.AcsDevice;

/**
 * 门禁设备同步服务接口
 *
 * @author fwbz
 */
public interface IAcsDeviceService extends IService<AcsDevice> {

    /**
     * 从海康平台全量拉取门禁设备数据并同步到数据库
     * <p>同步逻辑：先清空表内全部数据，再逐页拉取海康数据批量插入。</p>
     *
     * @return 同步成功的记录数
     */
    int syncFromHikvision();

    /**
     * 从海康平台查询门禁设备在线状态并更新到数据库
     * <p>从数据库获取所有设备 indexCode，分批（每批500个）请求海康在线状态接口，
     * 根据 indexCode 匹配更新 table_acs_device 的 online 字段。</p>
     *
     * @return 更新的记录数
     */
    int syncOnlineStatus();
}
