package org.jeecg.modules.fwbz.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.modules.fwbz.entity.FwbzHelloEntity;
import org.jeecg.modules.fwbz.service.IFwbzHelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Api(tags = "fwbz示例")
@RestController
@RequestMapping("/fwbz")
@Slf4j
public class FwbzHelloController {

	@Autowired
	private IFwbzHelloService jeecgHelloService;

	@ApiOperation(value = "hello", notes = "对外服务接口")
	@GetMapping(value = "/hello")
	public String sayHello() {
		log.info(" ---我被调用了--- ");
		return jeecgHelloService.hello();
	}

}
