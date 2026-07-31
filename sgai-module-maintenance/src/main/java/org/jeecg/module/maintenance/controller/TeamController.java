package org.jeecg.module.maintenance.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.module.maintenance.config.GroupInfoConfiguration;
import org.jeecg.module.maintenance.dto.TeamDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/maintenance/team")
@AllArgsConstructor
public class TeamController {

    private final GroupInfoConfiguration groupInfoConfiguration;

    @GetMapping("/list")
    public Result<?> list(){
        List<TeamDto> res = new ArrayList<>();
        groupInfoConfiguration.getGroupNames().forEach((k,v)->{
            res.add(new TeamDto(k,v));
        });
        return Result.ok(res);
    }

}
