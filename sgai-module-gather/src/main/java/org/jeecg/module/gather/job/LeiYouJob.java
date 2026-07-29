package org.jeecg.module.gather.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.module.gather.service.impl.FtlLeiYouServiceImpl;
import org.jeecg.module.gather.service.impl.LeiYouServiceImpl;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class LeiYouJob {

    private final LeiYouServiceImpl leiYouService;

    private final FtlLeiYouServiceImpl ftlLeiYouService;

    public void refreshToken(){

    }

}
