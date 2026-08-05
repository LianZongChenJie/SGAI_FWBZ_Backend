package org.jeecg.modules.fwbz.activeMeetPreparation.service;

import org.jeecg.modules.fwbz.activeMeetPreparation.vo.PreparationChecklistVO;

public interface IActiveMeetPreparationService {

    /**
     * 根据会议ID获取会前筹备清单
     */
    PreparationChecklistVO getChecklist(Long activeMeetId);
}
