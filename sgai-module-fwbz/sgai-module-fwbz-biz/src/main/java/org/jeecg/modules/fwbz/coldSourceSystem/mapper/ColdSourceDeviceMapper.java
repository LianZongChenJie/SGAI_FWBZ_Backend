package org.jeecg.modules.fwbz.coldSourceSystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.coldSourceSystem.dto.ColdSourceDevicePageDto;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.ColdSourceDevice;

import java.util.List;

/**
 * 冷源设备信息表 Mapper
 */
@Mapper
public interface ColdSourceDeviceMapper extends BaseMapper<ColdSourceDevice> {

    /**
     * 分页查询冷源设备列表（关联 cold_source_equipment_category 取类别名称）
     *
     * @param page       分页参数
     * @param deviceName 设备名称（模糊匹配，可为空）
     * @param deviceCode 设备编号（模糊匹配，可为空）
     * @param status     设备状态（精确匹配，可为空）
     * @param categoryId 设备类别id（精确匹配，可为空）
     * @return 设备列表（含类别名称）
     */
    @Select("<script>"
            + "SELECT d.id AS id, d.device_code AS deviceCode, d.device_name AS deviceName, "
            + "       d.category_id AS categoryId, c.category_name AS categoryName, "
            + "       d.system_code AS systemCode, d.niagara_path AS niagaraPath, "
            + "       d.status AS status, d.sort AS sort, d.remark AS remark "
            + "FROM \"FWBZ\".\"cold_source_device\" d "
            + "LEFT JOIN \"FWBZ\".\"cold_source_equipment_category\" c ON d.category_id = c.id "
            + "<where>"
            + "  <if test='deviceName != null and deviceName != \"\"'> AND d.device_name LIKE CONCAT('%', #{deviceName}, '%') </if>"
            + "  <if test='deviceCode != null and deviceCode != \"\"'> AND d.device_code LIKE CONCAT('%', #{deviceCode}, '%') </if>"
            + "  <if test='status != null'> AND d.status = #{status} </if>"
            + "  <if test='categoryId != null'> AND d.category_id = #{categoryId} </if>"
            + "</where>"
            + "ORDER BY d.sort ASC, d.id DESC"
            + "</script>")
    IPage<ColdSourceDevicePageDto> selectDevicePage(Page<ColdSourceDevicePageDto> page,
                                                    @Param("deviceName") String deviceName,
                                                    @Param("deviceCode") String deviceCode,
                                                    @Param("status") Integer status,
                                                    @Param("categoryId") Long categoryId);

    /**
     * 查询冷源设备列表（不分页，按条件查全部）
     *
     * @param deviceName 设备名称（模糊匹配，可为空）
     * @param deviceCode 设备编号（模糊匹配，可为空）
     * @param status     设备状态（精确匹配，可为空）
     * @param categoryId 设备类别id（精确匹配，可为空）
     * @return 设备列表（含类别名称）
     */
    @Select("<script>"
            + "SELECT d.id AS id, d.device_code AS deviceCode, d.device_name AS deviceName, "
            + "       d.category_id AS categoryId, c.category_name AS categoryName, "
            + "       d.system_code AS systemCode, d.niagara_path AS niagaraPath, "
            + "       d.status AS status, d.sort AS sort, d.remark AS remark "
            + "FROM \"FWBZ\".\"cold_source_device\" d "
            + "LEFT JOIN \"FWBZ\".\"cold_source_equipment_category\" c ON d.category_id = c.id "
            + "<where>"
            + "  <if test='deviceName != null and deviceName != \"\"'> AND d.device_name LIKE CONCAT('%', #{deviceName}, '%') </if>"
            + "  <if test='deviceCode != null and deviceCode != \"\"'> AND d.device_code LIKE CONCAT('%', #{deviceCode}, '%') </if>"
            + "  <if test='status != null'> AND d.status = #{status} </if>"
            + "  <if test='categoryId != null'> AND d.category_id = #{categoryId} </if>"
            + "</where>"
            + "ORDER BY d.sort ASC, d.id DESC"
            + "</script>")
    List<ColdSourceDevicePageDto> selectDeviceList(@Param("deviceName") String deviceName,
                                                   @Param("deviceCode") String deviceCode,
                                                   @Param("status") Integer status,
                                                   @Param("categoryId") Long categoryId);

    /**
     * 根据设备id查询设备详情（关联 cold_source_equipment_category 取类别名称）
     *
     * @param deviceId 设备id
     * @return 设备详情（含类别名称）
     */
    @Select("SELECT d.id AS id, d.device_code AS deviceCode, d.device_name AS deviceName, "
            + "       d.category_id AS categoryId, c.category_name AS categoryName, "
            + "       d.system_code AS systemCode, d.niagara_path AS niagaraPath, "
            + "       d.status AS status, d.sort AS sort, d.remark AS remark "
            + "FROM \"FWBZ\".\"cold_source_device\" d "
            + "LEFT JOIN \"FWBZ\".\"cold_source_equipment_category\" c ON d.category_id = c.id "
            + "WHERE d.id = #{deviceId}")
    ColdSourceDevicePageDto selectDeviceDetail(@Param("deviceId") Long deviceId);
}
