package org.jeecg.modules.fwbz.hikvision.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.hikvision.dto.AcsDeviceListVO;
import org.jeecg.modules.fwbz.hikvision.dto.AcsDevicePageDto;
import org.jeecg.modules.fwbz.hikvision.entity.AcsDevice;

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

    /**
     * 分页查询门禁设备列表，支持按名称、设备类型编码、区域名称、在线状态、IP检索，为空查全部
     *
     * @param dto 分页及查询条件
     * @return 门禁设备分页列表
     */
    IPage<AcsDeviceListVO> getDeviceList(AcsDevicePageDto dto);
}
