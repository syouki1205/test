package com.example.demo.controller;

import java.security.Principal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // 地方と都道府県のマップ
    private static final Map<String, String[]> REGION_MAP = new LinkedHashMap<>();
    static {
        REGION_MAP.put("北海道・東北", new String[]{"北海道","青森県","岩手県","宮城県","秋田県","山形県","福島県"});
        REGION_MAP.put("関東", new String[]{"茨城県","栃木県","群馬県","埼玉県","千葉県","東京都","神奈川県"});
        REGION_MAP.put("中部", new String[]{"新潟県","富山県","石川県","福井県","山梨県","長野県","岐阜県","静岡県","愛知県","三重県"});
        REGION_MAP.put("近畿", new String[]{"滋賀県","京都府","大阪府","兵庫県","奈良県","和歌山県"});
        REGION_MAP.put("中国", new String[]{"鳥取県","島根県","岡山県","広島県","山口県"});
        REGION_MAP.put("四国", new String[]{"徳島県","香川県","愛媛県","高知県"});
        REGION_MAP.put("九州・沖縄", new String[]{"福岡県","佐賀県","長崎県","熊本県","大分県","宮崎県","鹿児島県","沖縄県"});
    }

    // 旅行一覧
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
        List<Trip> trips = tripRepository.findByUser(user);

        if (keyword != null && !keyword.isBlank()) {
            String lowerKeyword = keyword.toLowerCase();
            trips = trips.stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(lowerKeyword) ||
                            (t.getComment() != null && t.getComment().toLowerCase().contains(lowerKeyword)) ||
                            (t.getPrefecture() != null && t.getPrefecture().toLowerCase().contains(lowerKeyword)) ||
                            (t.getSpots() != null && t.getSpots().stream().anyMatch(
                                    s -> s.getName().toLowerCase().contains(lowerKeyword) ||
                                         (s.getCategory() != null && s.getCategory().toLowerCase().contains(lowerKeyword)) ||
                                         (s.getComment() != null && s.getComment().toLowerCase().contains(lowerKeyword)))))
                    .toList();
        }

        if (prefecture != null && !prefecture.isBlank()) {
            trips = trips.stream()
                    .filter(t -> t.getPrefecture() != null && t.getPrefecture().equals(prefecture))
                    .toList();
        }

        if (minRating != null) {
            trips = trips.stream()
                    .filter(t -> t.getRating() != null && t.getRating() >= minRating)
                    .toList();
        }

        Comparator<Trip> comparator = switch (sortBy) {
            case "rating" -> Comparator.comparing(Trip::getRating, Comparator.nullsLast(Comparator.naturalOrder()));
            case "endDate" -> Comparator.comparing(Trip::getEndDate, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Trip::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if ("desc".equals(order)) comparator = comparator.reversed();

        trips = trips.stream().sorted(comparator).toList();

        model.addAttribute("trips", trips);
        model.addAttribute("keyword", keyword);
        model.addAttribute("prefecture", prefecture);
        model.addAttribute("minRating", minRating);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);

        return "trip_list";
    }

    // 新規作成フォーム
    @GetMapping("/new")
    public String newTrip(Model model) {
        Trip trip = new Trip();
        trip.setPrefecture("東京都"); // デフォルト選択
        model.addAttribute("trip", trip);
        model.addAttribute("regions", REGION_MAP);
        return "trip_form";
    }

    // 作成処理
    @PostMapping
    public String create(@ModelAttribute Trip trip, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        trip.setUser(user);
        tripRepository.save(trip);
        return "redirect:/trips";
    }

    // 詳細表示
    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        Trip trip = tripRepository.findById(id).orElseThrow();
        model.addAttribute("trip", trip);
        return "trip_detail";
    }

    // 編集フォーム
    @GetMapping("/{id}/edit")
    public String editTrip(@PathVariable Long id, Model model, Principal principal) {
        Trip trip = tripRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!trip.getUser().equals(user)) return "redirect:/trips";

        // 既存データをフォームにセット（startDate, endDate, region, prefectureも含む）
        model.addAttribute("trip", trip);
        model.addAttribute("regions", REGION_MAP);
        return "trip_form";
    }

    // 更新処理
    @PostMapping("/{id}/update")
    public String updateTrip(@PathVariable Long id,
                             @ModelAttribute Trip tripForm,
                             Principal principal) {
        Trip trip = tripRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!trip.getUser().equals(user)) return "redirect:/trips";

        trip.setTitle(tripForm.getTitle());
        trip.setPrefecture(tripForm.getPrefecture());
        trip.setRegion(tripForm.getRegion());
        trip.setStartDate(tripForm.getStartDate());
        trip.setEndDate(tripForm.getEndDate());
        trip.setRating(tripForm.getRating());
        trip.setComment(tripForm.getComment());

        tripRepository.save(trip);
        return "redirect:/trips";
    }
}
