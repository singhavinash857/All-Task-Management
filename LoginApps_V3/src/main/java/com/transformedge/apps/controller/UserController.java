package com.transformedge.apps.controller;

import java.security.Principal;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.transformedge.apps.appconfiguration.Translator;
import com.transformedge.apps.service.EmployeeService;
import com.transformedge.apps.utils.StringUtils;

@Controller
@CrossOrigin
public class UserController {
	
	@Autowired
	EmployeeService employeeService;
	
	@Autowired
	StringUtils stringUtils;	
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@GetMapping("/login")
	public String login(Model model,String error, String logout) {
		if (error != null){
			String loginErrorMsg = Translator.toLocale("user.invalid.credentials");
			model.addAttribute("errorMsg", loginErrorMsg);
		}
		if (logout != null){
			String logoutSuccessMsg = Translator.toLocale("user.logout.successfully");
			model.addAttribute("message", logoutSuccessMsg);
		}
		
		/*Object errorObj = httpSession.getAttribute("error");
		System.out.println("errorObj ::"+errorObj);
		if (errorObj != null) {
			model.addAttribute("error", errorObj);
		}
		Object logoutObj = httpSession.getAttribute("message");
		System.out.println("logoutObj ::"+logoutObj);

		if (logoutObj != null){
			model.addAttribute("message", logoutObj);
			httpSession.invalidate(); 
		}*/
		return "/loginPage/MainLoginPage";
	}

	@GetMapping({"/welcome_page"})
	public String welcomePageAfterLogin(Principal principle) {
		return "homePage/MainWelcomePage";
	}
	
	@GetMapping(value="/403")
	public ModelAndView errorPage(){
		return new ModelAndView("loginPage/403");
	}
	
	@GetMapping(value="/resetPassword")
	public ModelAndView resetPassword(ModelAndView modelAndView, @RequestParam("token") String token) {
		logger.info("INSIDE UserController START METHOD resetPassword");
		ModelAndView model = new ModelAndView("loginPage/resetPassword");
		if(stringUtils.isTokenExpired(token)){
			model.addObject("errorMessage", "Oops!  This link has been expired!!");
		}else{
			model.addObject("resetToken", token);
		}
		return model;
	}
	
	
	/*@PostMapping({"/logoutSuccessful"})
	public String welcomePageAfterLogout(Principal principal,HttpServletRequest request, HttpServletResponse response) {
		System.out.println("principal logout ::"+principal.getName());
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();  
		if (auth != null){ 
			new SecurityContextLogoutHandler().logout(request, response, auth);  
		}  
		return "redirect:/login";
	}*/
	
	/*@GetMapping("/registration")
	public String registration(Model model) {
		model.addAttribute("userForm", new User());
		return "registration";
	}

	@PostMapping("/registration")
	public String registration(@ModelAttribute("userForm") User userForm, BindingResult bindingResult) {
		userValidator.validate(userForm, bindingResult);
		if (bindingResult.hasErrors()) {
			return "registration";
		}
		userService.save(userForm);
		securityService.autoLogin(userForm.getUsername(), userForm.getPasswordConfirm());
		return "redirect:/welcome";
	}*/

}
