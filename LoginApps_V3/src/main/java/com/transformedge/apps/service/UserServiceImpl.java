package com.transformedge.apps.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.internet.AddressException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.transformedge.apps.appconfiguration.Translator;
import com.transformedge.apps.entity.Employee;
import com.transformedge.apps.entity.PasswordResetTokenEnity;
import com.transformedge.apps.entity.User;
import com.transformedge.apps.exceptions.PasswordLinkExpiredException;
import com.transformedge.apps.exceptions.UserAlreadyExistException;
import com.transformedge.apps.exceptions.UserNotFoundException;
import com.transformedge.apps.repository.PasswordResetTokenRepository;
import com.transformedge.apps.repository.RoleRepository;
import com.transformedge.apps.repository.UserRepository;
import com.transformedge.apps.utils.MailsUtils;
import com.transformedge.apps.utils.StringUtils;

@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	StringUtils stringUtils;
	
	@Autowired
	MailsUtils mailsUtils;
	
	@Autowired
	PasswordResetTokenRepository passwordResetTokenRepository;
	
	@Autowired
	EmployeeService employeeService;

	@Override
	public void save(User user) {
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		user.setRoles(roleRepository.findAll());
		userRepository.save(user);
	}

	@Override
	public User findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	public boolean requestPasswordReset(String email) {
	    boolean returnValue = true;
		User user = userRepository.findByUsername(email);
		
		if (user == null) {
			String userNotExistMessage = Translator.toLocale("user.maill.notExist");
			throw new UserNotFoundException(userNotExistMessage + email);
		}
		String token = stringUtils.generatePasswordResetToken(user.getId());
		
		PasswordResetTokenEnity passwordResetTokenEnity = new PasswordResetTokenEnity();
		passwordResetTokenEnity.setToken(token);
		passwordResetTokenEnity.setUserDetails(user);
		
		PasswordResetTokenEnity savedEntity = passwordResetTokenRepository.save(passwordResetTokenEnity);
		if(savedEntity != null){
			Employee employee = employeeService.getEmployeeByMailId(user.getUsername());
			if(employee != null){
				ExecutorService executorService = Executors.newFixedThreadPool(10);
				executorService.execute(new Runnable() {
					
				    public void run() {
						try {
							mailsUtils.sendPasswordResetToken(employee.getEmployeeFirstName(),user.getUsername(),token);
						} catch (AddressException e) {
							e.printStackTrace();
						} 
				    }
				});
			}
		}
		return returnValue;
	}

	@Override
	public boolean resetPassword(String token, String password) {
		boolean returnValue = false;
		if(stringUtils.isTokenExpired(token)){
			String passwordTokenExpiredMessage = Translator.toLocale("user.resetLinkExpire.notExist");
			throw new PasswordLinkExpiredException(passwordTokenExpiredMessage);
		}
		PasswordResetTokenEnity  passwordResetTokenEnity = passwordResetTokenRepository.findByToken(token);
		if(passwordResetTokenEnity == null){
			return returnValue;
		}
		
		String encryptPassword = bCryptPasswordEncoder.encode(password);
		User user = passwordResetTokenEnity.getUserDetails();
		user.setPassword(encryptPassword);
		user.setPasswordConfirm(encryptPassword);
		
		User updatedUser = userRepository.save(user);
		if(updatedUser != null && updatedUser.getPassword().equalsIgnoreCase(encryptPassword)){
			returnValue = true;
		}
		passwordResetTokenRepository.delete(passwordResetTokenEnity);
		return returnValue;
	}
}
