package org.jeecg.modules.fwbz.activeMeetPreparation.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.activeMeetPreparation.service.IActiveMeetPreparationService;
import org.jeecg.modules.fwbz.activeMeetPreparation.vo.CompletePreparationDTO;
import org.jeecg.modules.fwbz.activeMeetPreparation.vo.PreparationChecklistVO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fwbz/activeMeet/preparation")
@Api(tags = "会前筹备")
public class ActiveMeetPreparationController {

    private final IActiveMeetPreparationService preparationService;

    public ActiveMeetPreparationController(IActiveMeetPreparationService preparationService) {
        this.preparationService = preparationService;
    }

    @GetMapping("/checklist")
    @ApiOperation("获取会前筹备清单")
    public Result<PreparationChecklistVO> getChecklist(@RequestParam(name = "id") Long id) {
        PreparationChecklistVO checklist = preparationService.getChecklist(id);
        if (checklist == null) {
            return Result.error("会议不存在");
        }
        return Result.ok(checklist);
    }

    @PostMapping("/complete")
    @ApiOperation("完成筹备项")
    public Result<String> complete(@RequestBody CompletePreparationDTO dto) {
        preparationService.completePreparation(dto.getPreparationInfoId(), dto.getPreparationValue(), dto.getRealValue());
        return Result.ok("修改成功");
    }
}
