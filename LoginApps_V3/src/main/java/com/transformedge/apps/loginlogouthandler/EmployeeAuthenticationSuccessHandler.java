package com.transformedge.apps.loginlogouthandler;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
public class EmployeeAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ActiveUserStore activeUserStore;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	LoginLogoutDateTimeComponent loginLogoutDateTimeComponent;

	@Setter
	@Getter
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		logger.info("INSIDE EmployeeAuthenticationSuccessHandler  username ::"+authentication.getName());
		HttpSession session = request.getSession(false);
		if (session != null) {
			LoggedUser user = new LoggedUser(authentication.getName(), activeUserStore);
			session.setAttribute("user", user);
		}
		String targetUrl = determineTargetUrl(authentication); 
		loginLogoutDateTimeComponent.saveLoginDateTimeWithUser(request, response, authentication);
		//loginNotification(authentication, request);
		redirectStrategy.sendRedirect(request, response, targetUrl);
	}

	private void loginNotification(Authentication authentication, HttpServletRequest request) {
		try {
			if (authentication.getPrincipal() != null) {
				deviceService.verifyDevice(authentication.getName(), request);
			}
		} catch (Exception e) {
			logger.error("An error occurred while verifying device or location", e);
			throw new RuntimeException(e);
		}		
	}

	private String determineTargetUrl(Authentication authentication) {
		Set<String> authorities = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
		if (authorities.contains("ADMIN") || authorities.contains("HR") || authorities.contains("MANAGER") || authorities.contains("EMPLOYEE") ) {
			return "/welcome_page";
		} else {
			throw new IllegalStateException();
		}
	}
}
