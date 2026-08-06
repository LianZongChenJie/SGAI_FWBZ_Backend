package org.jeecg.modules.fwbz.complaint.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.complaint.dto.ComplaintHandleDTO;
import org.jeecg.modules.fwbz.complaint.entity.ComplaintInfo;

/**
 * @Description: 投诉建议信息
 * @Author: jeecg-boot
 * @Date:   2026-08-06
 * @Version: V1.0
 */
public interface IComplaintInfoService extends IService<ComplaintInfo> {

    /**
     * 处理投诉建议（修改状态并添加处理记录）
     *
     * @param dto 处理请求
     */
    void handleComplaint(ComplaintHandleDTO dto);

    /**
     * 分页查询并联动投诉类型表填充typeName
     *
     * @param page         分页参数
     * @param queryWrapper 查询条件
     * @return 包含typeName的分页结果
     */
    IPage<ComplaintInfo> pageWithTypeName(Page<ComplaintInfo> page, QueryWrapper<ComplaintInfo> queryWrapper);
}
