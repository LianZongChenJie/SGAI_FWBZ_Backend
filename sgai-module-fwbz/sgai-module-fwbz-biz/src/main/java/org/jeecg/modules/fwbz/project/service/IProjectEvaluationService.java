package org.jeecg.modules.fwbz.project.service;

import org.jeecg.modules.fwbz.project.dto.EvaluationReportData;
import org.jeecg.modules.fwbz.project.dto.EvaluationReportQueryDto;
import org.jeecg.modules.fwbz.project.vo.ProjectCategoryVo;
import org.jeecg.modules.fwbz.project.vo.ProjectOverviewVo;
import org.jeecg.modules.fwbz.project.entity.Project;

import java.util.List;

public interface IProjectEvaluationService{
    /**
     * 项目概览
     */
    ProjectOverviewVo getOverview();

    /**
     * 项目投资前5排名
     * @param top 默认5
     */
    List<Project> getInvestmentRanking(Integer top);

    /**
     * 项目数量占比-各类别项目占比
     */
//    List<ProjectCategoryVo> getProportionOfProjectCategories();

    /**
     * 获取项目评价报告
     */
    EvaluationReportData getReport(EvaluationReportQueryDto params);
}
