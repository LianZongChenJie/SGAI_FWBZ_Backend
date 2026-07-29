package org.jeecg.modules.fwbz.project.dto;

import lombok.Data;

@Data
public class ProjectEnergyConservationResult {

    private ProjectEnergyConservationData water;

    private ProjectEnergyConservationData electricity;
}
