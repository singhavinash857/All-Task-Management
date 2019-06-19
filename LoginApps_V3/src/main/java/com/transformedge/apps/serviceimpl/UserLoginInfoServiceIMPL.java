package com.transformedge.apps.serviceimpl;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.transformedge.apps.entity.UserLoginInfo;
import com.transformedge.apps.model.UserLoginInfoResponse;
import com.transformedge.apps.repository.UserLoginInfoRepository;
import com.transformedge.apps.service.UserLoginInfoService;

@Service
public class UserLoginInfoServiceIMPL implements UserLoginInfoService{

	@Autowired
	UserLoginInfoRepository userLoginInfoRepository;
	
	@Override
	public void saveLoginDateTimeWithUser(UserLoginInfo userLoginInfo) {
		userLoginInfoRepository.save(userLoginInfo);
	}

	@Override
	public UserLoginInfo getUserWhoNotLoggedOut(String userName, String logoutTime, Date todayDate) {
		return userLoginInfoRepository.findByUserNameAndLogoutTimeAndLoginDate(userName, logoutTime,todayDate);
	}

	@Override
	public void updateLogoutTimeWithUser(UserLoginInfo userLoginInfo) {
		System.out.println("userLoginInfo :::::::::"+userLoginInfo);
		userLoginInfoRepository.save(userLoginInfo);	
	}

	@Override
	public UserLoginInfo findIfUserLoggedinSameIp(UserLoginInfo userLoginInfo) {
		return userLoginInfoRepository.findByUserNameAndLogoutTimeAndLoginDateAndSystemIpAddress(userLoginInfo.getUserName(), userLoginInfo.getLogoutTime(), userLoginInfo.getLoginDate(), userLoginInfo.getSystemIpAddress());		
	}

	@Override
	public UserLoginInfo getUserWhoNotLoggedOutWithIp(String userName, String logoutTime, Date todayDate, String remoteAddr) {
		return userLoginInfoRepository.findByUserNameAndLogoutTimeAndLoginDateAndSystemIpAddress(userName, logoutTime, todayDate, remoteAddr);
	}

	@Override
	public List<UserLoginInfo> getLoggedinUserDetails(String userName) {
		return  (List<UserLoginInfo>) userLoginInfoRepository.findAll();
	}

	@SuppressWarnings("deprecation")
	@Override
	public UserLoginInfoResponse getLoggedInEmployeeByMailId(String userName,int pageNumber, int pageSize) {
		Pageable pageable = new PageRequest((pageNumber-1), pageSize, Sort.by("id").descending());
		Page<UserLoginInfo> page = userLoginInfoRepository.findByUserName(userName,pageable);
		List<UserLoginInfo> userLoginInfoList = page.getContent();
		
		UserLoginInfoResponse userLoginInfoResponse = new UserLoginInfoResponse();
		userLoginInfoResponse.setPageNum(page.getNumber());
		userLoginInfoResponse.setPageSize(page.getSize());
		userLoginInfoResponse.setToalPages(page.getTotalPages());
		userLoginInfoResponse.setTotalCounts(page.getTotalElements());
		userLoginInfoResponse.setHashMore(page.hasNext());
		userLoginInfoResponse.setValues(userLoginInfoList);
		
		return userLoginInfoResponse;
	//	return userLoginInfoRepository.findByUserName(userName,pageable);
	}

	public UserLoginInfo getUserWhoNotLoggedOutWithIp(String userName, String logoutTime, Date todayDate) {
		return userLoginInfoRepository.findByUserNameAndLogoutTimeAndLoginDate(userName, logoutTime, todayDate);
	}
}
