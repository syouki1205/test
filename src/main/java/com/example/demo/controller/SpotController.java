package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.Spot;
import com.example.demo.entity.Trip;
import com.example.demo.repository.SpotRepository;
import com.example.demo.repository.TripRepository;

@Controller
@RequestMapping("/trips/{tripId}/spots")
public class SpotController {

	private final SpotRepository spotRepository;
	private final TripRepository tripRepository;

	public SpotController(SpotRepository spotRepository, TripRepository tripRepository) {
		this.spotRepository = spotRepository;
		this.tripRepository = tripRepository;
	}

	// スポット追加フォーム
	@GetMapping("/new")
	public String newForm(@PathVariable Long tripId, Model model) {
		Trip trip = tripRepository.findById(tripId).orElseThrow();
		model.addAttribute("trip", trip); // ← trip を渡す
		model.addAttribute("spot", new Spot());
		return "spot_form";
	}

	// スポット登録
	@PostMapping
	public String create(@PathVariable Long tripId, @ModelAttribute Spot spot) {
		Trip trip = tripRepository.findById(tripId).orElseThrow();
		spot.setTrip(trip);
		spotRepository.save(spot);
		return "redirect:/trips/" + tripId;
	}

	// 編集フォーム
	// 編集フォーム
	@GetMapping("/{spotId}/edit")
	public String editForm(@PathVariable Long tripId, @PathVariable Long spotId, Model model) {
		Spot spot = spotRepository.findById(spotId).orElseThrow();
		Trip trip = tripRepository.findById(tripId).orElseThrow(); // ← trip を取得
		model.addAttribute("spot", spot);
		model.addAttribute("trip", trip); // ← trip を Model に追加
		return "spot_form"; // 新規と同じフォームを利用
	}

	// 更新処理
	@PostMapping("/{spotId}/update")
	public String update(@PathVariable Long tripId,
			@PathVariable Long spotId,
			@ModelAttribute Spot spotForm) {
		Spot spot = spotRepository.findById(spotId).orElseThrow();

		// 入力値で上書き
		spot.setName(spotForm.getName());
		spot.setCategory(spotForm.getCategory());
		spot.setRating(spotForm.getRating());
		spot.setComment(spotForm.getComment());

		spotRepository.save(spot);
		return "redirect:/trips/" + tripId;
	}

}
