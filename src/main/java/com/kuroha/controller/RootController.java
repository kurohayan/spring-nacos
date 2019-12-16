package com.kuroha.controller;

import com.kuroha.service.CloudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author kuroha
 */
@RestController
public class RootController {

	@Autowired
	private CloudService cloudService;

	@GetMapping(value = "naming",produces = "application/json;charset=utf-8")
	public String getAllUri(){
		return cloudService.getAllUri();
	}
	@GetMapping(value = "initRouting",produces = "application/json;charset=utf-8")
	public String initRouting(){
		cloudService.initRoutingMap();
		return "SUCCESS";
	}
	@GetMapping(value = "say",produces = "application/json;charset=utf-8")
	public String say() {
		return cloudService.get("service-algorithm","say");
	}
	@GetMapping(value = "say2",produces = "application/json;charset=utf-8")
	public String say2() {
		return cloudService.get("cloud-service","say");
	}


}
