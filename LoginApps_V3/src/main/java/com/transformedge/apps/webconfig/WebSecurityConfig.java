package com.transformedge.apps.webconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.transformedge.apps.loginlogouthandler.EmployeeAuthenticationSuccessHandler;
import com.transformedge.apps.loginlogouthandler.EmployeeLoginFailureHandler;
import com.transformedge.apps.loginlogouthandler.EmployeeLogoutSuccessHandler;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private EmployeeAuthenticationSuccessHandler successHandler;
	
	@Autowired
	private EmployeeLogoutSuccessHandler logoutSuccessHandler;
	
	@Autowired
	private EmployeeLoginFailureHandler failureHandler;
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.csrf().disable()
		.authorizeRequests()
		.antMatchers("/login").permitAll()
		.antMatchers("/403").permitAll()
		.antMatchers("/home/employee_controller/**").permitAll()
		.antMatchers("/home/task_controller/**").permitAll()
		.antMatchers("/home/file_controller/**").hasAuthority("ADMIN")
		.antMatchers("/home/comments_controller/**").hasAnyAuthority("ADMIN", "EMPLOYEE")
		.antMatchers("/home/comments_reply_controller/**").permitAll()
		.antMatchers("/home/password_reset_controller/**").permitAll()
		.antMatchers("/home/test_controller/**").permitAll()
		.antMatchers("/resetPassword").permitAll()
/*		.antMatchers("/home/redirect_controller/**").permitAll()
*/		.anyRequest()
		.authenticated()
		.and()
		.formLogin().loginPage("/login").successHandler(successHandler).failureHandler(failureHandler)
		.and()
		.logout().logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler).permitAll()
		.and()
        .exceptionHandling()
		.accessDeniedPage("/403");
	}


	@Bean
	public AuthenticationManager customAuthenticationManager() throws Exception {
		return authenticationManager();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web
		.ignoring()
		.antMatchers("/resources/**", "/static/**","/customCss/**", "/images/**");
	}
}