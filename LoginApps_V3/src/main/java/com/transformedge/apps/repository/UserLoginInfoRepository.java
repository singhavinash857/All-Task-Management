package com.transformedge.apps.repository;

import java.sql.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.transformedge.apps.entity.UserLoginInfo;

public interface UserLoginInfoRepository extends CrudRepository<UserLoginInfo, Long> {
	UserLoginInfo findByUserNameAndLogoutTimeAndLoginDate(String userName,String logoutTime, Date todayDate);
	UserLoginInfo findByUserNameAndSystemIpAddressAndLogoutTime(String userName , String systemIpAddress, String logoutTime);
	UserLoginInfo findByUserNameAndLogoutTimeAndLoginDateAndSystemIpAddress(String userName, String logoutTime,
			Date todayDate, String remoteAddr);
    @Query("select u from UserLoginInfo u where year(u.loginDate) = year(current_date) and  month(u.loginDate) = month(current_date) and u.userName = :username")
	Page<UserLoginInfo> findByUserName(@Param("username") String username, Pageable pageable);
}
