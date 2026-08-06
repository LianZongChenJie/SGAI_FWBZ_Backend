package org.jeecg.modules.fwbz.activeMeetPreparation.service;

import org.jeecg.modules.fwbz.activeMeetPreparation.vo.PreparationChecklistVO;

public interface IActiveMeetPreparationService {

    /**
     * 根据会议ID获取会前筹备清单
     */
    PreparationChecklistVO getChecklist(Long activeMeetId);

    /**
     * 完成筹备项：更新筹备值、状态、完成时间，并重算活动总体进度
     */
    void completePreparation(Long preparationInfoId, Long preparationValue, Long realValue);
}
