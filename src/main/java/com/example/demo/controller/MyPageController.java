package com.example.demo.controller;

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

	/**
	 * ✅ 自分のマイページ（ログインユーザー専用）
	 * URL: /mypage
	 * → 自分のユーザーIDにリダイレクト
	 */
	@GetMapping("/mypage")
	public String showMyPage(Model model, Principal principal) {
		if (principal == null) {
			// ログインしていない場合、ログインページへリダイレクト
			return "redirect:/login";
		}

		// ログイン中ユーザー取得
		User loginUser = userRepository.findByUsername(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// 自分のページにリダイレクト
		return "redirect:/mypage/" + loginUser.getId();
	}

	/**
	 * ✅ 指定したユーザーのマイページ表示
	 * URL: /mypage/{userId}
	 */
	@GetMapping("/mypage/{userId}")
	public String showUserPage(@PathVariable Long userId, Model model, Principal principal) {
		// --- 表示対象ユーザー ---
		User targetUser = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));
		model.addAttribute("user", targetUser);

		// --- ログインユーザー ---
		User loginUser = null;
		if (principal != null) {
			loginUser = userRepository.findByUsername(principal.getName())
					.orElse(null);
		}
		model.addAttribute("loginUser", loginUser);

		// --- 最近の旅行（最新3件などに変更可） ---
		List<Trip> recentTrips = tripRepository.findTop1ByUserOrderByStartDateDesc(targetUser);
		model.addAttribute("recentTrips", recentTrips);

		// --- 統計情報 ---
		long tripCount = tripRepository.countByUser(targetUser);
		long distinctPrefectureCount = tripRepository.countDistinctPrefectureByUser(targetUser);

		model.addAttribute("tripCount", tripCount);
		model.addAttribute("distinctPrefectureCount", distinctPrefectureCount);

		// ✅ プロフィール画像の切り替え（ユーザーIDごとにファイルを分ける想定）
		String profileImagePath = "/images/profiles/user_" + targetUser.getId() + ".jpeg";
		model.addAttribute("profileImagePath", profileImagePath);

		return "mypage"; // → mypage.html へ
	}
}
