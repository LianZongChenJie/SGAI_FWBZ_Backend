package org.jeecg.modules.fwbz.mdm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.mdm.dto.AttributeBindingDto;
import org.jeecg.modules.fwbz.mdm.dto.DeviceAttributeData;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.vo.DeviceAttributeDataVo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface IDeviceAttributeService extends IService<DeviceAttribute> {

    IPage<DeviceAttribute> queryPage(DeviceAttribute params);

    List<DeviceAttributeDataVo> listByDeviceId(Long deviceId);

    List<DeviceAttribute> getByDeviceId(Long deviceId);

    String getValueById(Long id);

    void updateAttributeValue(Long deviceId,DeviceAttributeData data);

    List<DeviceAttribute> findByDeviceIds(Collection<Long> deviceIds);

    List<DeviceAttribute> findByIds(Collection<Long> ids);

    List<DeviceAttribute> findByAttributeCodes(Collection<String> deviceCodes);

    /**
     * 设备属性绑定楼控点位
     */
    void bindingBuildingControlPoint(AttributeBindingDto data);

    /**
     * 设备属性关联楼控点位数据更新
     * @param gatewayAdr 网关地址
     * @param bacnetAdr BACnet地址
     * @param value 采集值
     * @param collectionTime 采集时间
     */
    void updateAttributeValue(String gatewayAdr,String bacnetAdr,String value,LocalDateTime collectionTime);

    /**
     * 设备运行状态更新
     * @param deviceCode 设备编号
     * @param runState 在线，离线
     */
    void updateAttributeForRunState(String deviceCode,String runState);

    /**
     * 增加运行状态属性
     * @param deviceId 设备id
     */
    void addRunStateAttribute(Long deviceId);

    List<DeviceAttribute> findByDeviceIdsAndCode(Collection<Long> deviceIds,String code);

    DeviceAttribute findByDeviceIdAndCode(Long deviceIds,String code);

    /**
     * 查询所有配置了采集编码(acquisitionCoding)且为纯数字点ID 的设备属性
     *
     * @return 设备属性列表（仅含 acquisitionCoding 非空且可解析为 Long 的记录）
     */
    List<DeviceAttribute> findByAcquisitionCodingExists();

    /**
     * 冷源系统按点ID(tagId) 推送时更新设备属性 value
     *
     * @param tagId         冷源系统点ID（对应 device_attribute.acquisition_coding）
     * @param value         采集值
     * @param collectionTime 采集时间
     */
    void updateAttributeValueByPointId(Long tagId, String value, LocalDateTime collectionTime);
}
