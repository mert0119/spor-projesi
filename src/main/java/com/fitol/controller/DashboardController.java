package com.fitol.controller;

import com.fitol.model.User;
import com.fitol.repository.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.*;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final FoodLogRepository foodLogRepository;
    private final ExerciseLogRepository exerciseLogRepository;
    private final WaterLogRepository waterLogRepository;
    private final MeasurementRepository measurementRepository;

    public DashboardController(UserRepository userRepository,
                                FoodLogRepository foodLogRepository,
                                ExerciseLogRepository exerciseLogRepository,
                                WaterLogRepository waterLogRepository,
                                MeasurementRepository measurementRepository) {
        this.userRepository = userRepository;
        this.foodLogRepository = foodLogRepository;
        this.exerciseLogRepository = exerciseLogRepository;
        this.waterLogRepository = waterLogRepository;
        this.measurementRepository = measurementRepository;
    }

    /** JPA multi-column sonucunu duz Object[] olarak al */
    private Object[] unwrap(Object[] raw) {
        if (raw == null || raw.length == 0) return new Object[]{0, 0, 0, 0};
        if (raw[0] instanceof Object[]) return (Object[]) raw[0];
        return raw;
    }

    private double num(Object obj) {
        return obj instanceof Number ? ((Number) obj).doubleValue() : 0;
    }

    private int pct(double value, Number goal) {
        if (goal == null || goal.doubleValue() <= 0) return 0;
        return (int) Math.min(Math.round(value * 100.0 / goal.doubleValue()), 100);
    }

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        LocalDate today = LocalDate.now();

        // Gunluk kalori
        double dailyCalories = foodLogRepository.sumCaloriesByUserIdAndDate(user.getId(), today);

        // Gunluk makrolar
        Object[] macros = unwrap(foodLogRepository.sumMacrosByUserIdAndDate(user.getId(), today));
        double dailyProtein = num(macros.length > 0 ? macros[0] : null);
        double dailyCarbs = num(macros.length > 1 ? macros[1] : null);
        double dailyFat = num(macros.length > 2 ? macros[2] : null);

        // Gunluk su
        double dailyWaterMl = waterLogRepository.sumByUserIdAndDate(user.getId(), today);

        // Gunluk egzersiz
        Object[] exercise = unwrap(exerciseLogRepository.sumByUserIdAndDate(user.getId(), today));
        double dailyExerciseCal = num(exercise.length > 0 ? exercise[0] : null);
        double dailyExerciseMin = num(exercise.length > 1 ? exercise[1] : null);

        // Son 7 gun kalori (grafik)
        List<Integer> weeklyCalories = new ArrayList<>();
        List<String> weeklyLabels = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            double cal = foodLogRepository.sumCaloriesByUserIdAndDate(user.getId(), d);
            weeklyCalories.add((int) Math.round(cal));
            weeklyLabels.add(String.format("%02d/%02d", d.getDayOfMonth(), d.getMonthValue()));
        }

        // Son olcum
        var lastMeasurement = measurementRepository.findFirstByUserIdOrderByDateDesc(user.getId());

        // Motivasyon
        String[] motivations = {
            "Bugün de harika gidiyorsun! 💪",
            "Her adım seni hedefe yaklaştırıyor! 🎯",
            "Disiplin, başarının anahtarıdır! 🔑",
            "Vücudun sana teşekkür edecek! 🌟",
            "Vazgeçme, en iyi versiyonun olmak üzeresin! 🚀",
            "Sağlıklı yaşam bir tercih değil, bir yaşam biçimi! 🍀",
            "Bugünün emeği, yarının gücü! ⚡",
            "Kendine yatırım yap, karşılığını alacaksın! 💎",
        };
        String motivation = motivations[new Random().nextInt(motivations.length)];

        model.addAttribute("user", user);
        model.addAttribute("dailyCalories", (int) Math.round(dailyCalories));
        model.addAttribute("dailyProtein", (int) Math.round(dailyProtein));
        model.addAttribute("dailyCarbs", (int) Math.round(dailyCarbs));
        model.addAttribute("dailyFat", (int) Math.round(dailyFat));
        model.addAttribute("dailyWaterMl", (int) Math.round(dailyWaterMl));
        model.addAttribute("dailyWaterLitre", Math.round(dailyWaterMl / 100.0) / 10.0);
        model.addAttribute("dailyExerciseCal", (int) Math.round(dailyExerciseCal));
        model.addAttribute("dailyExerciseMin", (int) Math.round(dailyExerciseMin));
        model.addAttribute("weeklyCalories", weeklyCalories);
        model.addAttribute("weeklyLabels", weeklyLabels);
        model.addAttribute("lastMeasurement", lastMeasurement.orElse(null));
        model.addAttribute("motivation", motivation);
        model.addAttribute("today", today);

        // Progress bar yuzdeleri (Thymeleaf'te karmasik hesaplamadan kacinmak icin)
        model.addAttribute("calPct", pct(dailyCalories, user.getDailyCalorieGoal()));
        model.addAttribute("protPct", pct(dailyProtein, user.getProteinGoal()));
        model.addAttribute("carbPct", pct(dailyCarbs, user.getCarbsGoal()));
        model.addAttribute("fatPct", pct(dailyFat, user.getFatGoal()));
        double waterLitre = dailyWaterMl / 1000.0;
        model.addAttribute("waterPct", pct(waterLitre, user.getDailyWaterGoal()));

        return "dashboard";
    }
}
