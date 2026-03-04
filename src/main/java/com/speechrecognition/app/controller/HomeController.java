package com.speechrecognition.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {
	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	@GetMapping("/dashboard")
	public String dashboard() {
		return "dashboard";
	}
	
	@GetMapping("/all-records")
	public String allRecords() {
    return "all-records";
	}

	@GetMapping("/project/{id}")          
    public String viewProject(@PathVariable Long id) {
        return "view-project";
    }
}
