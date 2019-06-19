package com.transformedge.apps.entity;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Entity
@Table(name="user_login_logout_info")
public class UserLoginInfo {
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@Column(name="user_name")
	private String userName;
	
	@Column(name="user_id")
	private long userId;
	
	@Column(name="login_date")
	private Date loginDate;
	
	@Column(name="login_time")
	private String loginTime;
	
	@Column(name="logout_time")
	private String logoutTime;
	
	@Column(name="session_id")
	private String userSessionId;
	
	@Column(name="ip_address")
	private String systemIpAddress;
	
	
}
