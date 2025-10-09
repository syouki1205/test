package com.example.demo.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
		if (principal == null) {
			return "redirect:/login";
		}

		User loginUser = userRepository.findByUsername(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		return "redirect:/mypage/" + loginUser.getId();
	}

	@GetMapping("/mypage/{userId}")
	public String showUserPage(@PathVariable Long userId, Model model, Principal principal) {

		// 表示対象ユーザー
		User targetUser = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));
		model.addAttribute("user", targetUser);

		// ログインユーザー
		User loginUser = null;
		if (principal != null) {
			loginUser = userRepository.findByUsername(principal.getName()).orElse(null);
		}
		model.addAttribute("loginUser", loginUser);

		// 最近の旅行
		List<Trip> recentTrips = tripRepository.findTop1ByUserOrderByStartDateDesc(targetUser);
		model.addAttribute("recentTrips", recentTrips);

		// 統計情報
		long tripCount = tripRepository.countByUser(targetUser);
		long distinctPrefectureCount = tripRepository.countDistinctPrefectureByUser(targetUser);
		model.addAttribute("tripCount", tripCount);
		model.addAttribute("distinctPrefectureCount", distinctPrefectureCount);

		// プロフィール画像パス
		String profileImagePath;

		if ("syouki".equals(targetUser.getUsername())) {
			// syoukiは常に既存の画像
			profileImagePath = "/images/default-profile.jpeg";
		} else {
			// その他のユーザーは個別画像があれば使用、なければ新しいデフォルト
			String userImagePath = "src/main/resources/static/images/profiles/user_" + targetUser.getId() + ".jpeg";
			String defaultImagePath = "/images/default-profile-new.jpeg";
			if (Files.exists(Paths.get(userImagePath))) {
				profileImagePath = "/images/profiles/user_" + targetUser.getId() + ".jpeg";
			} else {
				profileImagePath = defaultImagePath;
			}
		}

		model.addAttribute("profileImagePath", profileImagePath);

		return "mypage";
	}
}
