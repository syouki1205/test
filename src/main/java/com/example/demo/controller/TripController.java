package com.example.demo.controller;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
//aa
	private static final Map<String, String[]> REGION_MAP = new LinkedHashMap<>();
	static {
		REGION_MAP.put("北海道・東北", new String[] { "北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県" });
		REGION_MAP.put("関東", new String[] { "茨城県", "栃木県", "群馬県", "埼玉県", "千葉県", "東京都", "神奈川県" });
		REGION_MAP.put("中部", new String[] { "新潟県", "富山県", "石川県", "福井県", "山梨県", "長野県", "岐阜県", "静岡県", "愛知県", "三重県" });
		REGION_MAP.put("近畿", new String[] { "滋賀県", "京都府", "大阪府", "兵庫県", "奈良県", "和歌山県" });
		REGION_MAP.put("中国", new String[] { "鳥取県", "島根県", "岡山県", "広島県", "山口県" });
		REGION_MAP.put("四国", new String[] { "徳島県", "香川県", "愛媛県", "高知県" });
		REGION_MAP.put("九州・沖縄", new String[] { "福岡県", "佐賀県", "長崎県", "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県" });
	}

	@GetMapping("/user/{id}")
	public String userTrips(
			@PathVariable Long id,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String region,
			@RequestParam(required = false) String prefecture,
			@RequestParam(required = false) Integer minRating,
			@RequestParam(required = false, defaultValue = "startDate") String sortBy,
			@RequestParam(required = false, defaultValue = "desc") String order,
			Model model,
			Principal principal) {

		User selectedUser = userRepository.findById(id).orElseThrow();
		User loginUser = userRepository.findByUsername(principal.getName()).orElseThrow();

		List<Trip> trips = selectedUser.getTrips();

		// フィルタ処理
		if (keyword != null && !keyword.isEmpty()) {
			trips = trips.stream()
					.filter(t -> t.getTitle().contains(keyword))
					.collect(Collectors.toList());
		}
		if (region != null && !region.isEmpty()) {
			trips = trips.stream()
					.filter(t -> region.equals(t.getRegion()))
					.collect(Collectors.toList());
		}
		if (prefecture != null && !prefecture.isEmpty()) {
			trips = trips.stream()
					.filter(t -> prefecture.equals(t.getPrefecture()))
					.collect(Collectors.toList());
		}
		if (minRating != null) {
			trips = trips.stream()
					.filter(t -> t.getRating() != null && t.getRating() >= minRating)
					.collect(Collectors.toList());
		}

		// 並び替え
		trips = trips.stream()
				.sorted((t1, t2) -> {
					int cmp = 0;
					if ("startDate".equals(sortBy)) {
						cmp = t1.getStartDate().compareTo(t2.getStartDate());
					} else if ("rating".equals(sortBy)) {
						cmp = t1.getRating().compareTo(t2.getRating());
					}
					return "desc".equals(order) ? -cmp : cmp;
				})
				.collect(Collectors.toList());

		boolean isOwner = selectedUser.equals(loginUser);

		model.addAttribute("trips", trips);
		model.addAttribute("user", selectedUser);
		model.addAttribute("isOwner", isOwner);
		model.addAttribute("keyword", keyword);
		model.addAttribute("region", region);
		model.addAttribute("prefecture", prefecture);
		model.addAttribute("minRating", minRating);
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("order", order);
		model.addAttribute("prefecturesMap", REGION_MAP);

		return "trip_list";
	}

	// 以下、新規作成・編集・削除は元コード通り
	@GetMapping("/new")
	public String newTrip(Model model, Principal principal) {
		Trip trip = new Trip();
		User loginUser = userRepository.findByUsername(principal.getName()).orElseThrow();
		model.addAttribute("trip", trip);
		model.addAttribute("regions", REGION_MAP);
		model.addAttribute("userId", loginUser.getId());
		return "trip_form";
	}

	@PostMapping
	public String create(@ModelAttribute Trip trip, Principal principal) {
		User loginUser = userRepository.findByUsername(principal.getName()).orElseThrow();
		trip.setUser(loginUser);
		tripRepository.save(trip);
		return "redirect:/trips/user/" + loginUser.getId();
	}

	@GetMapping("/{id}")
	public String show(@PathVariable Long id, Model model, Principal principal) {
		Trip trip = tripRepository.findById(id).orElseThrow();
		User loginUser = userRepository.findByUsername(principal.getName()).orElseThrow();
		boolean isOwner = trip.getUser().equals(loginUser);
		model.addAttribute("trip", trip);
		model.addAttribute("isOwner", isOwner);
		return "trip_detail";
	}

	@GetMapping("/{id}/edit")
	public String editTrip(@PathVariable Long id, Model model, Principal principal) {
		Trip trip = tripRepository.findById(id).orElseThrow();
		User loginUser = userRepository.findByUsername(principal.getName()).orElseThrow();
		if (!trip.getUser().equals(loginUser)) {
			return "redirect:/trips/user/" + trip.getUser().getId();
		}
		model.addAttribute("trip", trip);
		model.addAttribute("regions", REGION_MAP);
		model.addAttribute("userId", loginUser.getId());
		return "trip_form";
	}

	@PostMapping("/{id}/update")
	public String updateTrip(@PathVariable Long id,
			@ModelAttribute Trip tripForm,
			Principal principal) {
		Trip trip = tripRepository.findById(id).orElseThrow();
		User loginUser = userRepository.findByUsername(principal.getName()).orElseThrow();
		if (!trip.getUser().equals(loginUser)) {
			return "redirect:/trips/user/" + trip.getUser().getId();
		}
		trip.setTitle(tripForm.getTitle());
		trip.setPrefecture(tripForm.getPrefecture());
		trip.setRegion(tripForm.getRegion());
		trip.setStartDate(tripForm.getStartDate());
		trip.setEndDate(tripForm.getEndDate());
		trip.setRating(tripForm.getRating());
		trip.setComment(tripForm.getComment());
		tripRepository.save(trip);
		return "redirect:/trips/user/" + loginUser.getId();
	}

	@PostMapping("/{id}/delete")
	public String deleteTrip(@PathVariable Long id, Principal principal) {
		Trip trip = tripRepository.findById(id).orElseThrow();
		User loginUser = userRepository.findByUsername(principal.getName()).orElseThrow();
		if (!trip.getUser().equals(loginUser)) {
			return "redirect:/trips/user/" + trip.getUser().getId();
		}
		tripRepository.delete(trip);
		return "redirect:/trips/user/" + loginUser.getId();
	}
}
