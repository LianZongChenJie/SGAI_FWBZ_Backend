package org.jeecg.modules.fwbz.complaint.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.fwbz.complaint.dto.ComplaintHandleDTO;
import org.jeecg.modules.fwbz.complaint.entity.ComplaintInfo;
import org.jeecg.modules.fwbz.complaint.entity.ComplaintRecord;
import org.jeecg.modules.fwbz.complaint.mapper.ComplaintInfoMapper;
import org.jeecg.modules.fwbz.complaint.mapper.ComplaintRecordMapper;
import org.jeecg.modules.fwbz.complaint.service.IComplaintInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;

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
}
