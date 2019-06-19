package com.transformedge.apps.loginlogouthandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.transformedge.apps.appconfiguration.Translator;

@Component
public class EmployeeLogoutSuccessHandler implements LogoutSuccessHandler{

	@Autowired
	LoginLogoutDateTimeComponent  loginLogoutDateTimeComponent;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void onLogoutSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
					throws IOException, ServletException {
		logger.info("Logout Sucessfull with Principal ====> " + authentication.getName());
		
		String logoutSuccessMsg = Translator.toLocale("user.logout.successfully");
		request.getSession().setAttribute("message",logoutSuccessMsg);
		request.getSession().getId();
		response.setStatus(HttpServletResponse.SC_OK);
		loginLogoutDateTimeComponent.setLogoutDateTimeWithUser(request, response, authentication);
		response.sendRedirect("/login?logout=true");
	}

}
