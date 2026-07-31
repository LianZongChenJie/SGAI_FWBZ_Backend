package org.jeecg.module.maintenance.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.module.maintenance.dto.ResultInfo;
import org.jeecg.module.maintenance.entity.PlanModel;
import org.jeecg.module.maintenance.service.IPlanModelService;
import org.jeecg.module.maintenance.util.FileWithExcelUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/maintenance/planModel")
@AllArgsConstructor
public class PlanModelController {

    private final IPlanModelService service;

    /**
     * 维保计划模板下载
     *
     * @param year      年份
     * @param labelType 分类。维保
     */
    @GetMapping("/exportTemplate")
    public void exportTemplate(HttpServletResponse response, int year, String labelType) {
        service.exportTemplate(year, response, labelType);
    }

    /**
     * 导入维保计划
     *
     * @param labelType 分类。维保
     */
    @PostMapping("/importTemplate")
    public void importTemplate(MultipartFile file, HttpServletResponse response, String labelType) {
        List<ResultInfo> result = service.importData(file, labelType);
        FileWithExcelUtil.exportExcel(result, "导入结果", "导入结果", ResultInfo.class, "导入结果.xlsx", true, response);
    }

    /**
     * 删除计划
     *
     * @param year 年份
     * @return
     */
    @PostMapping("/remove")
    public Result<String> remove(Integer year, String labelType) {
        service.remove(year, labelType);
        return Result.ok();
    }

    /**
     * 查询计划
     *
     * @param year      年份
     * @param labelType 分类
     * @return 查询结果
     */
    @GetMapping("/findByYear")
    public Result<?> findByYear(Integer year, String labelType) {
        return Result.ok(service.findByYear(year, labelType));
    }

    /**
     * 维保计划分页查询
     *
     * @param param 查询参数
     * @return 查询结果
     */
    @GetMapping("/queryPage")
    public Result<?> queryPage(PlanModel param) {
        return Result.ok(service.queryPage(param));
    }
}
