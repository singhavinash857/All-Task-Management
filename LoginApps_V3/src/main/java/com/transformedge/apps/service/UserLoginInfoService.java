package com.transformedge.apps.service;

import java.sql.Date;
import java.util.List;

import com.transformedge.apps.entity.UserLoginInfo;
import com.transformedge.apps.model.UserLoginInfoResponse;

public interface UserLoginInfoService {
	void saveLoginDateTimeWithUser(UserLoginInfo userLoginInfo);
	UserLoginInfo getUserWhoNotLoggedOut(String name, String logoutTime, Date date);
	void updateLogoutTimeWithUser(UserLoginInfo userLoginInfo);
	UserLoginInfo findIfUserLoggedinSameIp(UserLoginInfo userLoginInfo);
	UserLoginInfo getUserWhoNotLoggedOutWithIp(String name, String logoutTime, Date todayDate, String remoteAddr);
	List<UserLoginInfo> getLoggedinUserDetails(String userName);
	UserLoginInfoResponse getLoggedInEmployeeByMailId(String name, int page_number, int pageSize);
	UserLoginInfo getUserWhoNotLoggedOutWithIp(String name, String object, Date todayDate);
}
