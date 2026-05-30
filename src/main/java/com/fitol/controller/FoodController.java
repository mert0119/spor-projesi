package com.fitol.controller;

import com.fitol.model.FoodLog;
import com.fitol.model.User;
import com.fitol.repository.FoodLogRepository;
import com.fitol.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/food")
public class FoodController {

    private final FoodLogRepository foodLogRepository;
    private final UserRepository userRepository;

    public FoodController(FoodLogRepository foodLogRepository, UserRepository userRepository) {
        this.foodLogRepository = foodLogRepository;
        this.userRepository = userRepository;
    }

    private Object[] unwrap(Object[] raw) {
        if (raw == null || raw.length == 0) return new Object[]{0, 0, 0, 0};
        if (raw[0] instanceof Object[]) return (Object[]) raw[0];
        return raw;
    }
    private int num(Object obj) { return obj instanceof Number ? ((Number) obj).intValue() : 0; }

    @GetMapping
    public String log(@AuthenticationPrincipal UserDetails userDetails,
                      @RequestParam(required = false) String date, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        LocalDate selectedDate = parseDate(date);

        Map<String, List<FoodLog>> meals = new LinkedHashMap<>();
        for (String mt : List.of("kahvalti", "ogle", "aksam", "atistirmalik")) {
            meals.put(mt, foodLogRepository.findByUserIdAndDateAndMealTypeOrderByCreatedAtDesc(user.getId(), selectedDate, mt));
        }

        double totalCal = foodLogRepository.sumCaloriesByUserIdAndDate(user.getId(), selectedDate);
        Object[] macrosRaw = foodLogRepository.sumMacrosByUserIdAndDate(user.getId(), selectedDate);
        Object[] macros = unwrap(macrosRaw);

        Map<String, String> mealNames = new LinkedHashMap<>();
        mealNames.put("kahvalti", "Kahvaltı");
        mealNames.put("ogle", "Öğle Yemeği");
        mealNames.put("aksam", "Akşam Yemeği");
        mealNames.put("atistirmalik", "Atıştırmalık");

        model.addAttribute("user", user);
        model.addAttribute("meals", meals);
        model.addAttribute("mealNames", mealNames);
        model.addAttribute("totalCalories", (int) Math.round(totalCal));
        model.addAttribute("totalProtein", num(macros.length > 0 ? macros[0] : null));
        model.addAttribute("totalCarbs", num(macros.length > 1 ? macros[1] : null));
        model.addAttribute("totalFat", num(macros.length > 2 ? macros[2] : null));
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("today", LocalDate.now());
        return "food/log";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal UserDetails ud,
                      @RequestParam String foodName, @RequestParam(defaultValue = "kahvalti") String mealType,
                      @RequestParam(defaultValue = "1") double portion, @RequestParam(defaultValue = "0") double calories,
                      @RequestParam(defaultValue = "0") double protein, @RequestParam(defaultValue = "0") double carbs,
                      @RequestParam(defaultValue = "0") double fat, @RequestParam(required = false) String date,
                      RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        LocalDate logDate = parseDate(date);
        if (foodName == null || foodName.isBlank()) { ra.addFlashAttribute("error", "Yemek adı gerekli."); return "redirect:/food"; }
        FoodLog e = new FoodLog();
        e.setUserId(user.getId()); e.setDate(logDate); e.setMealType(mealType);
        e.setFoodName(foodName.trim()); e.setPortion(portion); e.setCalories(calories);
        e.setProtein(protein); e.setCarbs(carbs); e.setFat(fat);
        foodLogRepository.save(e);
        ra.addFlashAttribute("success", foodName.trim() + " eklendi!");
        return "redirect:/food?date=" + logDate;
    }

    @GetMapping("/delete/{id}")
    public String delete(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        FoodLog entry = foodLogRepository.findById(id).orElse(null);
        if (entry == null || !entry.getUserId().equals(user.getId())) { ra.addFlashAttribute("error", "Yetkisiz."); return "redirect:/food"; }
        String d = entry.getDate().toString();
        foodLogRepository.delete(entry);
        ra.addFlashAttribute("info", "Kayıt silindi.");
        return "redirect:/food?date=" + d;
    }

    @GetMapping("/history")
    public String history(@AuthenticationPrincipal UserDetails ud, @RequestParam(defaultValue = "0") int page, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        model.addAttribute("logs", foodLogRepository.findByUserIdOrderByDateDescCreatedAtDesc(user.getId(), PageRequest.of(page, 20)));
        model.addAttribute("user", user);
        return "food/history";
    }

    private LocalDate parseDate(String date) {
        if (date != null && !date.isBlank()) { try { return LocalDate.parse(date); } catch (Exception e) {} }
        return LocalDate.now();
    }
}
