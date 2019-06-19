package com.transformedge.apps.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transformedge.apps.appconfiguration.Translator;
import com.transformedge.apps.enums.RequestOperationName;
import com.transformedge.apps.enums.RequestOperationStatus;
import com.transformedge.apps.exceptions.ErrorFormInfo;
import com.transformedge.apps.model.OperationStatusModel;
import com.transformedge.apps.model.PasswordResetModel;
import com.transformedge.apps.model.PasswordResetRequestModel;
import com.transformedge.apps.service.UserService;

@CrossOrigin
@RestController
@RequestMapping(value="${spring.data.rest.base-path}/password_reset_controller")
public class PasswordResetController {

	@Autowired
	UserService userService;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@PostMapping("/password_reset_request")
	public ResponseEntity<?> passwordResetRquest(@RequestBody PasswordResetRequestModel passwordResetRequestModel,HttpServletRequest request){
		logger.info("INSIDE PasswordResetController START METHOD passwordResetRquest:");
	
		System.out.println("passwordResetRequestModel.getEmail() ::"+passwordResetRequestModel.getEmail());
		//	OperationStatusModel returnValue = new OperationStatusModel();
		boolean operationResult = userService.requestPasswordReset(passwordResetRequestModel.getEmail());
//		returnValue.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());
//		returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
		
		ErrorFormInfo errorInfo = null;
		if(operationResult){
				String successMsg = Translator.toLocale("reset.linksent.successfully");
				errorInfo = new ErrorFormInfo(HttpStatus.OK, true, request.getRequestURI(), successMsg, null);
				return new ResponseEntity<>(errorInfo, HttpStatus.OK);
		     //	returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		}
		return new ResponseEntity<>(errorInfo, HttpStatus.RESET_CONTENT);
	}
	
	@PostMapping("/password_reset")
	public ResponseEntity<?> resetPassword(@RequestBody PasswordResetModel passwordResetModel, HttpServletRequest request){
		logger.info("INSIDE PasswordResetController START METHOD resetPassword:");
//		OperationStatusModel returnValue = new OperationStatusModel();
		boolean operationResult = userService.resetPassword(passwordResetModel.getToken(),passwordResetModel.getPassword());
//		returnValue.setOperationName(RequestOperationName.PASSWORD_RESET.name());
//		returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
		ErrorFormInfo errorInfo = null;

		if(operationResult){
			String successMsg = Translator.toLocale("reset.password.successfully");
			errorInfo = new ErrorFormInfo(HttpStatus.OK, true, request.getRequestURI(), successMsg, null);
			return new ResponseEntity<>(errorInfo, HttpStatus.OK);
//			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		}
		return new ResponseEntity<>(errorInfo, HttpStatus.RESET_CONTENT);
	}
	
	
	@GetMapping("/test")
	public String getTestMessage(){
		return "hello reset password !!";
	}
}
