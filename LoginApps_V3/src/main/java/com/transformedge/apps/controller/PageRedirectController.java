package com.transformedge.apps.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@CrossOrigin
@RequestMapping(value="${spring.data.rest.base-path}/redirect_controller")
public class PageRedirectController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	
	@GetMapping("/registerPage/MainRegisterPage")
	public ModelAndView registerPage() {
		logger.info("INSIDE PageRedirectController START METHOD registerPage");
		ModelAndView model = new ModelAndView("registerPage/MainRegisterPage");
		return model;
	}
	
	@GetMapping("/projectPage/ProjectReportPage")
	public ModelAndView projectDetailsPage() {
		logger.info("INSIDE PageRedirectController START METHOD projectDetailsPage");
		ModelAndView model = new ModelAndView("registerPage/MainAdminPage");
		return model;
	}
	
	@GetMapping("/taskPage/Task")
	public ModelAndView taskAssignerPage() {
		logger.info("INSIDE PageRedirectController START METHOD registerPage");
		ModelAndView model = new ModelAndView("TaskPage/Task");
		return model;
	}
	
	@GetMapping("/taskPage/MainTaskTablePage")
	public ModelAndView individualTaskPage() {
		logger.info("INSIDE PageRedirectController START METHOD individualTaskPage");
		ModelAndView model = new ModelAndView("TaskPage/MainTaskTablePage");
		return model;
	}
	
	@GetMapping("/taskPage/comment_page_with_task/{taskId}")
	public ModelAndView individualTaskPageWithComments(@PathVariable String taskId) {
		logger.info("INSIDE PageRedirectController START METHOD individualTaskPageWithComments:"+taskId);
		
		ModelAndView model = new ModelAndView("TaskPage/MainTaskStatusPage");
		model.addObject("taskId", taskId);
		return model;
	}
}
