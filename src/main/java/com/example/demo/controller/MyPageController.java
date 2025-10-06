package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Controller
public class MyPageController {
//aaaaaa
	private final UserRepository userRepository;

	public MyPageController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	// カスタムマイページ：ユーザー選択画面
	@GetMapping("/")
	public String selectUser(Model model) {
		List<User> users = userRepository.findAll();
		model.addAttribute("users", users);
		return "select_user";
	}

	// 選択したユーザーの旅行一覧
	@GetMapping("/users/{id}/trips")
	public String userTrips(@PathVariable Long id, Model model, Principal principal) {
		User selectedUser = userRepository.findById(id).orElseThrow();
		model.addAttribute("user", selectedUser);
		model.addAttribute("trips", selectedUser.getTrips());

		// ログインユーザー判定
		User loginUser = userRepository.findByUsername(principal.getName()).orElseThrow();
		boolean isOwner = loginUser.getId().equals(selectedUser.getId());
		model.addAttribute("isOwner", isOwner); // Thymeleafで編集ボタン表示判定に使用

		return "trip_list";
	}
}
