package com.transformedge.apps.loginlogouthandler;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import com.transformedge.apps.entity.User;
import com.transformedge.apps.entity.UserLoginInfo;
import com.transformedge.apps.service.UserLoginInfoService;
import com.transformedge.apps.service.UserService;
import com.transformedge.apps.utils.DateTimeUtility;
import com.transformedge.apps.utils.IpAddressUtils;

public class LoginLogoutDateTimeComponent {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	UserLoginInfoService userLoginInfoService;
	
	@Autowired
	DateTimeUtility dateTimeUtility;
	
	@Autowired
	IpAddressUtils ipAddressUtils;
	
	@Autowired
	UserService userService;

	public void saveLoginDateTimeWithUser(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) {
		logger.info("INSIDE  LoginLogoutDateTimeComponent CALLING METHOD saveLoginLogoutDateTimeWithUser");
		UserLoginInfo  userLoginInfo = new UserLoginInfo();
		String userName = authentication.getName();
		User user = userService.findByUsername(userName);
		if(user != null){
			userLoginInfo.setUserId(user.getId());
		}
		userLoginInfo.setLoginDate(dateTimeUtility.getTodayDate());
		userLoginInfo.setLoginTime(dateTimeUtility.getCurrentTime());
		userLoginInfo.setUserName(userName);
		userLoginInfo.setUserSessionId(request.getSession().getId());
		userLoginInfo.setSystemIpAddress(ipAddressUtils.extractCleintIp(request));
		userLoginInfo.setLogoutTime(null);
		
		UserLoginInfo userLoginInfoIfSameIp =  userLoginInfoService.findIfUserLoggedinSameIp(userLoginInfo);
		logger.info("The ip Address of the system is ::"+request.getRemoteAddr());
		logger.info("the user loggedin into this ip is ::"+userLoginInfoIfSameIp);
		if(userLoginInfoIfSameIp == null){
			userLoginInfoService.saveLoginDateTimeWithUser(userLoginInfo);
		}
	}

	public void setLogoutDateTimeWithUser(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) {
		logger.info("INSIDE  LoginLogoutDateTimeComponent CALLING METHOD saveLoginLogoutDateTimeWithUser");
		String clientIpAddress = ipAddressUtils.extractCleintIp(request);
		UserLoginInfo userLoginInfo = userLoginInfoService.getUserWhoNotLoggedOutWithIp(authentication.getName(),null,dateTimeUtility.getTodayDate(),clientIpAddress);
		if(userLoginInfo != null){
    		userLoginInfo.setLogoutTime(dateTimeUtility.getCurrentTime());
    		userLoginInfo.setUserSessionId(request.getSession().getId());
        }else{
        	userLoginInfoService.getUserWhoNotLoggedOutWithIp(authentication.getName(),null,dateTimeUtility.getTodayDate());
        }
		userLoginInfoService.updateLogoutTimeWithUser(userLoginInfo);
	}
}
