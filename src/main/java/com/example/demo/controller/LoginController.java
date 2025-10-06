package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

	// ログインページ表示
	@GetMapping("/login")
	public String login() {
		return "login"; // resources/templates/login.html
	}
}
