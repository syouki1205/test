package com.example.demo.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Controller
public class UserController {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	// 登録ページ表示
	@GetMapping("/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	// 登録処理
	@PostMapping("/register")
	public String registerUser(@ModelAttribute User user, Model model) {
		// 同じユーザー名の存在チェック
		if (userRepository.findByUsername(user.getUsername()).isPresent()) {
			model.addAttribute("error", "このユーザー名はすでに使用されています。");
			return "register";
		}

		// パスワードを暗号化して保存
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userRepository.save(user);

		// 登録完了後はログインページへ
		return "redirect:/login?registered";
	}
}
