package org.jeecg.modules.fwbz.coldSourceSystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.coldSourceSystem.dto.ColdSourceHistoryPageDto;
import org.jeecg.modules.fwbz.coldSourceSystem.dto.ColdSourceHistoryPageQueryDto;
import org.jeecg.modules.fwbz.coldSourceSystem.mapper.TableColdSourceHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 冷源历史记录查询服务
 */
@Slf4j
@Service
public class ColdSourceHistoryService {

    @Autowired
    private TableColdSourceHistoryMapper tableColdSourceHistoryMapper;

    /**
     * 分页查询冷源历史记录
     * 条件：tagId（精确）、desc（描述模糊）；返回：tagId、desc、dataTime(采集时间)、value(值)
     */
    public IPage<ColdSourceHistoryPageDto> pageHistory(ColdSourceHistoryPageQueryDto params,
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        Page<ColdSourceHistoryPageDto> page = new Page<>(params.getPageNo(), params.getPageSize());
        return tableColdSourceHistoryMapper.selectHistoryPage(page, params.getTagId(), params.getDesc(),
                startTime, endTime);
    }

    /**
     * 导出冷源历史记录（不分页，按条件返回全部匹配数据）
     */
    public List<ColdSourceHistoryPageDto> exportHistory(ColdSourceHistoryPageQueryDto params,
                                                        LocalDateTime startTime, LocalDateTime endTime) {
        return tableColdSourceHistoryMapper.selectHistoryList(params.getTagId(), params.getDesc(),
                startTime, endTime);
    }
}
