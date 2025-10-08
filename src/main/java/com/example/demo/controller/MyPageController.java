package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.Trip;
import com.example.demo.entity.User;
import com.example.demo.repository.TripRepository;
import com.example.demo.repository.UserRepository;

@Controller
public class MyPageController {

	private final UserRepository userRepository;
	private final TripRepository tripRepository;

	public MyPageController(UserRepository userRepository, TripRepository tripRepository) {
		this.userRepository = userRepository;
		this.tripRepository = tripRepository;
	}

	@GetMapping("/mypage")
	public String showMyPage(Model model, Principal principal) {
		// ログインユーザー取得
		User user = userRepository.findByUsername(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		model.addAttribute("user", user);

		// 直近1件の旅行
		List<Trip> recentTrips = tripRepository.findTop1ByUserOrderByStartDateDesc(user);
		model.addAttribute("recentTrips", recentTrips);

		// 総旅行数
		long tripCount = tripRepository.countByUser(user);
		model.addAttribute("tripCount", tripCount);

		// 訪問都道府県数（重複なし）
		long distinctPrefectureCount = tripRepository.countDistinctPrefectureByUser(user);
		model.addAttribute("distinctPrefectureCount", distinctPrefectureCount);

		return "mypage"; // mypage.html に遷移
	}
}
