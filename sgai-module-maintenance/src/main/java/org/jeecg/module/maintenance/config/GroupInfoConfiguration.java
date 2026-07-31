package org.jeecg.module.maintenance.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "group-info")
@Data
public class GroupInfoConfiguration {

    private Map<Long,String> groupNames;

    private List<String> weibaoTypeList;



}
