package org.jeecg.module.maintenance.dto;

import lombok.Data;
import org.jeecg.module.maintenance.entity.PlanModel;

import java.util.List;

@Data
public class PlanModelDto {

    private TableHeader tableHeader;
    private List<PlanModel> planModelList;
}
