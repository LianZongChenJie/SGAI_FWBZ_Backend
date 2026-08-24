package org.jeecg.modules.fwbz.main.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.main.dto.DataAmendLogDto;
import org.jeecg.modules.fwbz.main.dto.DataAmendParamDto;
import org.jeecg.modules.fwbz.main.entity.DataAmendLog;

public interface IDataAmendLogService extends IService<DataAmendLog> {

    IPage<DataAmendLogDto> listPage(DataAmendParamDto param);
}
