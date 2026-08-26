package org.jeecg.modules.fwbz.coldSourceSystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.TablePageInfo;

/**
 * 页面与冷源对应关系表 Mapper
 */
@Mapper
public interface TablePageInfoMapper extends BaseMapper<TablePageInfo> {
}
