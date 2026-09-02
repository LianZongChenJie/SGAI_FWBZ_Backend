package org.jeecg.modules.fwbz.complaint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.fwbz.complaint.dto.ComplaintHandleDTO;
import org.jeecg.modules.fwbz.complaint.entity.ComplaintInfo;
import org.jeecg.modules.fwbz.complaint.entity.ComplaintRecord;
import org.jeecg.modules.fwbz.complaint.entity.ComplaintType;
import org.jeecg.modules.fwbz.complaint.mapper.ComplaintInfoMapper;
import org.jeecg.modules.fwbz.complaint.mapper.ComplaintRecordMapper;
import org.jeecg.modules.fwbz.complaint.mapper.ComplaintTypeMapper;
import org.jeecg.modules.fwbz.complaint.service.IComplaintInfoService;
import org.jeecg.modules.fwbz.complaint.vo.ComplaintDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: 投诉建议信息
 * @Author: jeecg-boot
 * @Date:   2026-08-06
 * @Version: V1.0
 */
@Service
public class ComplaintInfoServiceImpl extends ServiceImpl<ComplaintInfoMapper, ComplaintInfo> implements IComplaintInfoService {

    @Autowired
    private ComplaintRecordMapper complaintRecordMapper;

    @Autowired
    private ComplaintTypeMapper complaintTypeMapper;

    @Override
    public IPage<ComplaintInfo> pageWithTypeName(Page<ComplaintInfo> page, QueryWrapper<ComplaintInfo> queryWrapper) {
        IPage<ComplaintInfo> result = page(page, queryWrapper);
        List<ComplaintInfo> records = result.getRecords();
        if (records.isEmpty()) {
            return result;
        }
        // 收集所有非空typeId
        List<Long> typeIds = records.stream()
                .map(ComplaintInfo::getTypeId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (typeIds.isEmpty()) {
            return result;
        }
        // 批量查询类型名称，构建id->name映射
        List<ComplaintType> types = complaintTypeMapper.selectBatchIds(typeIds);
        Map<Long, String> typeNameMap = types.stream()
                .collect(Collectors.toMap(ComplaintType::getId, ComplaintType::getTypeName));
        // 填充typeName
        records.forEach(r -> {
            if (r.getTypeId() != null) {
                r.setTypeName(typeNameMap.get(r.getTypeId()));
            }
        });
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleComplaint(ComplaintHandleDTO dto) {
        ComplaintInfo complaintInfo = getById(dto.getComplaintId());
        if (complaintInfo == null) {
            throw new JeecgBootException("投诉建议不存在！");
        }
        String oldStatus = complaintInfo.getStatus();
        // 更新状态
        complaintInfo.setStatus(dto.getStatus());
        if (dto.getHandler() != null && !dto.getHandler().isEmpty()) {
            complaintInfo.setHandler(dto.getHandler());
        }
        updateById(complaintInfo);

        // 添加处理记录
        ComplaintRecord record = new ComplaintRecord();
        record.setComplaintId(dto.getComplaintId());
        record.setHandleDate(new Date());
        record.setHandleTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        record.setHandleContent(dto.getHandleContent());
        record.setStatusFrom(oldStatus);
        record.setStatusTo(dto.getStatus());
        record.setHandler(dto.getHandler());
        complaintRecordMapper.insert(record);
    }

    @Override
    public ComplaintDetailVO getDetailById(String id) {
        ComplaintDetailVO vo = new ComplaintDetailVO();
        // 查询投诉信息
        ComplaintInfo complaintInfo = getById(id);
        if (complaintInfo == null) {
            return null;
        }
        // 填充类型名称
        if (complaintInfo.getTypeId() != null) {
            ComplaintType type = complaintTypeMapper.selectById(complaintInfo.getTypeId());
            if (type != null) {
                complaintInfo.setTypeName(type.getTypeName());
            }
        }
        vo.setComplaintInfo(complaintInfo);

        // 查询处理记录，按处理日期和创建时间倒序
        LambdaQueryWrapper<ComplaintRecord> qw = new LambdaQueryWrapper<ComplaintRecord>()
                .eq(ComplaintRecord::getComplaintId, complaintInfo.getId())
                .orderByDesc(ComplaintRecord::getHandleDate)
                .orderByDesc(ComplaintRecord::getGmtCreate);
        List<ComplaintRecord> records = complaintRecordMapper.selectList(qw);
        vo.setRecords(records);

        return vo;
    }
}
