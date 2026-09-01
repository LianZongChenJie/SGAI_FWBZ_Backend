package org.jeecg.modules.fwbz.coldSourceSystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.ColdSourceEquipmentCategory;

import java.util.List;

/**
 * 冷源设备类别表 Mapper
 */
@Mapper
public interface ColdSourceEquipmentCategoryMapper extends BaseMapper<ColdSourceEquipmentCategory> {

    /**
     * 查询设备类别列表
     *
     * @param categoryName 类别名称（模糊匹配，可为空）
     * @param type         类别类型（精确匹配，1计量 2楼控，可为空）
     * @param pid          父级id（精确匹配，可为空）
     * @return 类别列表
     */
    @Select("<script>"
            + "SELECT id, pid, has_child AS hasChild, category_name AS categoryName, "
            + "       sort, remark, full_name AS fullName, full_id AS fullId, type, master_id AS masterId "
            + "FROM \"FWBZ\".\"cold_source_equipment_category\" "
            + "<where>"
            + "  <if test='categoryName != null and categoryName != \"\"'> AND category_name LIKE CONCAT('%', #{categoryName}, '%') </if>"
            + "  <if test='type != null'> AND type = #{type} </if>"
            + "  <if test='pid != null'> AND pid = #{pid} </if>"
            + "</where>"
            + "ORDER BY sort ASC, id DESC"
            + "</script>")
    List<ColdSourceEquipmentCategory> selectCategoryList(@Param("categoryName") String categoryName,
                                                         @Param("type") Integer type,
                                                         @Param("pid") Long pid);
}
