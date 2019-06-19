package com.transformedge.apps.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transformedge.apps.appconfiguration.Translator;
import com.transformedge.apps.entity.Employee;
import com.transformedge.apps.entity.LovsInfo;
import com.transformedge.apps.exceptions.ErrorFormInfo;
import com.transformedge.apps.service.LovService;

@RestController
@RequestMapping(value="${spring.data.rest.base-path}/lov_controller")
public class LovController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LovService  lovService;
	
	@RequestMapping(value="/save-lov-detail")
	public ResponseEntity<?> saveLovDetails(@RequestBody @Valid LovsInfo lovData,HttpServletRequest request){
		logger.info("INSIDE LovController START saveLovDetails METHOD");
		final LovsInfo registeredLov = lovService.saveLovData(lovData);
		ErrorFormInfo errorInfo = null;
		if (registeredLov != null) {
			String successMsg = Translator.toLocale("lov.added.successfully");
			errorInfo = new ErrorFormInfo(HttpStatus.OK, true, request.getRequestURI(), successMsg, null);
			return new ResponseEntity<>(errorInfo, HttpStatus.OK);
		}
		return new ResponseEntity<>(errorInfo, HttpStatus.RESET_CONTENT);
	}
	
}
