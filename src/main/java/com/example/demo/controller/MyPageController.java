package com.example.demo.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Controller
public class MyPageController {

	private final UserRepository userRepository;

	public MyPageController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping("/mypage")
	public String showMyPage(Model model, Principal principal) {
		User user = userRepository.findByUsername(principal.getName()).orElseThrow();
		model.addAttribute("user", user);
		return "mypage"; // mypage.html に遷移
	}
}
