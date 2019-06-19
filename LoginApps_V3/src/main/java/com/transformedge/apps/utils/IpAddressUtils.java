package com.transformedge.apps.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class IpAddressUtils {

	private String clientIpAddress;
	
	public String getSystemIdAddress(){
		InetAddress localhost = null;
		try {
			localhost = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 
		return localhost.getHostAddress().trim();
	}
	
	public String extractCleintIp(HttpServletRequest request) {
		if (request != null) {
			this.clientIpAddress = request.getHeader("X-FORWARDED-FOR");
			System.out.println("clientIpAddress :::::"+clientIpAddress);
            if (clientIpAddress == null || "".equals(clientIpAddress)) {
            	this.clientIpAddress = request.getRemoteAddr();
            }
        }
	   // this.clientIpAddress = request.getRemoteAddr();
	    return clientIpAddress;
	}
	
}
