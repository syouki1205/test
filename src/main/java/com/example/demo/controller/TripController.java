package com.example.demo.controller;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Trip;
import com.example.demo.entity.User;
import com.example.demo.repository.TripRepository;
import com.example.demo.repository.UserRepository;

@Controller
@RequestMapping("/trips")
public class TripController {

	private final TripRepository tripRepository;
	private final UserRepository userRepository;

	public TripController(TripRepository tripRepository, UserRepository userRepository) {
		this.tripRepository = tripRepository;
		this.userRepository = userRepository;
	}

	// 旅行一覧（ソート・検索・フィルタ対応）
	@GetMapping
	public String listTrips(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String prefecture,
			@RequestParam(required = false) Integer minRating,
			@RequestParam(defaultValue = "startDate") String sortBy,
			@RequestParam(defaultValue = "desc") String order,
			Model model,
			Principal principal) {

		User user = userRepository.findByUsername(principal.getName()).orElseThrow();

		// 基本はユーザーの旅行一覧
		List<Trip> trips = tripRepository.findByUser(user);

		// キーワード検索（タイトル・コメント・都道府県・スポット情報）
		if (keyword != null && !keyword.isBlank()) {
			String lowerKeyword = keyword.toLowerCase();
			trips = trips.stream()
					.filter(t -> t.getTitle().toLowerCase().contains(lowerKeyword) ||
							(t.getComment() != null && t.getComment().toLowerCase().contains(lowerKeyword)) ||
							(t.getPrefecture() != null && t.getPrefecture().toLowerCase().contains(lowerKeyword)) ||
							(t.getSpots() != null && t.getSpots().stream().anyMatch(
									s -> s.getName().toLowerCase().contains(lowerKeyword) ||
											(s.getCategory() != null
													&& s.getCategory().toLowerCase().contains(lowerKeyword))
											||
											(s.getComment() != null
													&& s.getComment().toLowerCase().contains(lowerKeyword)))))
					.toList();
		}

		// 都道府県フィルタ
		if (prefecture != null && !prefecture.isBlank()) {
			trips = trips.stream()
					.filter(t -> t.getPrefecture() != null && t.getPrefecture().equals(prefecture))
					.toList();
		}

		// 評価フィルタ
		if (minRating != null) {
			trips = trips.stream()
					.filter(t -> t.getRating() != null && t.getRating() >= minRating)
					.toList();
		}

		// 並び替え（期間対応）
		Comparator<Trip> comparator = switch (sortBy) {
		case "rating" -> Comparator.comparing(Trip::getRating, Comparator.nullsLast(Comparator.naturalOrder()));
		case "endDate" -> Comparator.comparing(Trip::getEndDate, Comparator.nullsLast(Comparator.naturalOrder()));
		default -> Comparator.comparing(Trip::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
		};
		if ("desc".equals(order)) {
			comparator = comparator.reversed();
		}
		trips = trips.stream().sorted(comparator).toList();

		model.addAttribute("trips", trips);
		model.addAttribute("keyword", keyword);
		model.addAttribute("prefecture", prefecture);
		model.addAttribute("minRating", minRating);
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("order", order);

		return "trip_list";
	}

	@GetMapping("/new")
	public String newTrip(Model model) {
		model.addAttribute("trip", new Trip());
		return "trip_form";
	}

	@PostMapping
	public String create(@ModelAttribute Trip trip, Principal principal) {
		User user = userRepository.findByUsername(principal.getName()).orElseThrow();
		trip.setUser(user);
		tripRepository.save(trip);
		return "redirect:/trips";
	}

	@GetMapping("/{id}")
	public String show(@PathVariable Long id, Model model) {
		Trip trip = tripRepository.findById(id).orElseThrow();
		model.addAttribute("trip", trip);
		return "trip_detail";
	}

	@GetMapping("/{id}/edit")
	public String editTrip(@PathVariable Long id, Model model, Principal principal) {
		Trip trip = tripRepository.findById(id).orElseThrow();

		User user = userRepository.findByUsername(principal.getName()).orElseThrow();
		if (!trip.getUser().equals(user)) {
			return "redirect:/trips";
		}

		model.addAttribute("trip", trip);
		return "trip_form";
	}

	@PostMapping("/{id}/update")
	public String updateTrip(@PathVariable Long id,
			@ModelAttribute Trip tripForm,
			Principal principal) {
		Trip trip = tripRepository.findById(id).orElseThrow();
		User user = userRepository.findByUsername(principal.getName()).orElseThrow();

		if (!trip.getUser().equals(user)) {
			return "redirect:/trips";
		}

		// ✅ フィールド更新（date → startDate, endDate）
		trip.setTitle(tripForm.getTitle());
		trip.setPrefecture(tripForm.getPrefecture());
		trip.setStartDate(tripForm.getStartDate());
		trip.setEndDate(tripForm.getEndDate());
		trip.setRating(tripForm.getRating());
		trip.setComment(tripForm.getComment());

		tripRepository.save(trip);

		return "redirect:/trips";
	}
}
