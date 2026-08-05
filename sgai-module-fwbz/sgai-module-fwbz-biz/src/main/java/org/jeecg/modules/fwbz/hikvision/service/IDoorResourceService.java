package org.jeecg.modules.fwbz.hikvision.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.hikvision.dto.DoorListVO;
import org.jeecg.modules.fwbz.hikvision.dto.DoorResourcePageDto;
import org.jeecg.modules.fwbz.hikvision.entity.DoorResource;

/**
 * 门禁点资源同步服务接口
 *
 * @author fwbz
 */
public interface IDoorResourceService extends IService<DoorResource> {

    /**
     * 从海康平台全量拉取门禁点数据并同步到数据库
     * <p>同步逻辑：先清空表内全部数据，再逐页拉取海康数据批量插入。</p>
     *
     * @return 同步成功的记录数
     */
    int syncFromHikvision();

    /**
     * 从海康平台查询门禁状态并更新到数据库
     * <p>逐页拉取海康门禁状态数据，根据 indexCode 匹配更新 table_door_resource 的 door_state 字段。</p>
     *
     * @return 更新的记录数
     */
    int syncDoorStatus();

    /**
     * 分页查询门禁点列表，支持按名称、门禁点编号、区域名称、门状态、接入协议、安装位置检索，为空查全部
     *
     * @param dto 分页及查询条件
     * @return 门禁点分页列表
     */
    IPage<DoorListVO> getDoorList(DoorResourcePageDto dto);
}
