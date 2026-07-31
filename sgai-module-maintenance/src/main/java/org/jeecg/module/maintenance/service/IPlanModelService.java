package org.jeecg.module.maintenance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.module.maintenance.dto.PlanModelDto;
import org.jeecg.module.maintenance.dto.ResultInfo;
import org.jeecg.module.maintenance.entity.PlanModel;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface IPlanModelService extends IService<PlanModel> {

    void exportTemplate(int year, HttpServletResponse response, String labelType);

    List<ResultInfo> importData(MultipartFile file, String labelType);

    long countByYear(int year, String labelType);

    void remove(Integer year,String labelType);

    PlanModelDto findByYear(Integer year, String labelType);

    Page<PlanModel> queryPage(PlanModel param);

}
