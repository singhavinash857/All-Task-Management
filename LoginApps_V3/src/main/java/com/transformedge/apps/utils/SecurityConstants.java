package com.transformedge.apps.utils;

public class SecurityConstants {

	public static final long PASSWORD_RESTE_EXPIRATION_TIME = 120000;
//	public static final String PASSWORD_RESET_REQUEST_URL = "home/password_reset_controller/**";

	public static String getTokenSecret() {
		return "resetpassword";
	}

}
