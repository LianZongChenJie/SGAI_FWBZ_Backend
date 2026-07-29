package org.jeecg.modules.fwbz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.dto.DataAmendLogDto;
import org.jeecg.modules.fwbz.dto.DataAmendParamDto;
import org.jeecg.modules.fwbz.entity.DataAmendLog;

public interface IDataAmendLogService extends IService<DataAmendLog> {

    IPage<DataAmendLogDto> listPage(DataAmendParamDto param);
}
