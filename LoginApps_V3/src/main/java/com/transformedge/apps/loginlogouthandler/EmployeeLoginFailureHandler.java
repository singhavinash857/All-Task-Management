package com.transformedge.apps.loginlogouthandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.transformedge.apps.appconfiguration.Translator;

@Component
public class EmployeeLoginFailureHandler implements AuthenticationFailureHandler{

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		logger.info("Login Failed With The Credentials ");
		String loginFailureSuccessMsg = Translator.toLocale("user.invalid.credentials");
		request.getSession().setAttribute("error",loginFailureSuccessMsg);
		response.setStatus(HttpServletResponse.SC_OK);
		response.sendRedirect("/login?error=true");
	}

}
