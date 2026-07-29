package org.jeecg.module.third.controller;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.module.third.service.InvestmentPromotionService;
import org.jeecg.module.third.service.MeetingSystemService;
import org.jeecg.module.third.service.SpsSystemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/third/login")
@RestController
@AllArgsConstructor
@Slf4j
public class ThirdSystemLoginController{

    private final MeetingSystemService meetingSystemService;

    private final SpsSystemService spsSystemService;

    private final InvestmentPromotionService investmentPromotionService;

    /**
     * 获取会议系统token
     */
    @GetMapping("/meetingSystem")
    public Result<?> meetingSystemToken(){
        return Result.OK("",meetingSystemService.getToken(null));
    }

    /**
     * 通知安防平台登录信息
     */
    @PostMapping("/spsSystem")
    public Result<?> spsSystem(HttpServletRequest request){
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // 获取请求头里的token
        String token = request.getHeader("X-Access-Token");
        log.info("token",token);
        spsSystemService.asyncPostUserLoginInfoToSpsSystem(sysUser, sysUser.getUsername(), token);
        return Result.ok();
    }

    /**
     * 招商url
     */
    @PostMapping("/investmentPromotionSystem")
    public Result<?> investmentPromotionSystemToken(){

        String token = investmentPromotionService.getInvestToken();
        if(StrUtil.isEmpty(token)){
            return Result.error("验证失败");
        }
        return Result.OK("",investmentPromotionService.getInvestToUrl(token));
    }

}
