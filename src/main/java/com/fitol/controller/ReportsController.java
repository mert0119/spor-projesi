package com.fitol.controller;

import com.fitol.model.User;
import com.fitol.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    private final UserRepository userRepository;
    private final FoodLogRepository foodLogRepository;
    private final ExerciseLogRepository exerciseLogRepository;
    private final WaterLogRepository waterLogRepository;

    public ReportsController(UserRepository userRepository, FoodLogRepository foodLogRepository,
                              ExerciseLogRepository exerciseLogRepository, WaterLogRepository waterLogRepository) {
        this.userRepository = userRepository;
        this.foodLogRepository = foodLogRepository;
        this.exerciseLogRepository = exerciseLogRepository;
        this.waterLogRepository = waterLogRepository;
    }

    private Object[] unwrap(Object[] raw) {
        if (raw == null || raw.length == 0) return new Object[]{0, 0, 0, 0};
        if (raw[0] instanceof Object[]) return (Object[]) raw[0];
        return raw;
    }
    private int n(Object obj) { return obj instanceof Number ? ((Number) obj).intValue() : 0; }

    @GetMapping
    public String index(@AuthenticationPrincipal UserDetails ud,
                        @RequestParam(defaultValue = "weekly") String period, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        String title;

        switch (period) {
            case "daily" -> { startDate = today; title = "Günlük Rapor"; }
            case "monthly" -> { startDate = today.minusDays(30); title = "Aylık Rapor — Son 30 Gün"; }
            default -> { startDate = today.minusDays(7); title = "Haftalık Rapor — Son 7 Gün"; period = "weekly"; }
        }

        int daysCount = (int) (today.toEpochDay() - startDate.toEpochDay()) + 1;

        // Kalori istatistikleri
        Object[] calStats = unwrap(foodLogRepository.sumStatsByUserIdAndDateRange(user.getId(), startDate, today));
        // Egzersiz istatistikleri
        Object[] exStats = unwrap(exerciseLogRepository.sumStatsByUserIdAndDateRange(user.getId(), startDate, today));
        // Su istatistikleri
        double waterTotal = waterLogRepository.sumByUserIdAndDateRange(user.getId(), startDate, today);

        // Gunluk grafik verisi
        List<String> labels = new ArrayList<>();
        List<Integer> caloriesIn = new ArrayList<>();
        List<Integer> caloriesOut = new ArrayList<>();
        List<Integer> waterData = new ArrayList<>();
        for (int i = 0; i < daysCount; i++) {
            LocalDate d = startDate.plusDays(i);
            labels.add(String.format("%02d/%02d", d.getDayOfMonth(), d.getMonthValue()));
            caloriesIn.add((int) Math.round(foodLogRepository.sumCaloriesByUserIdAndDate(user.getId(), d)));
            Object[] exDay = unwrap(exerciseLogRepository.sumByUserIdAndDate(user.getId(), d));
            caloriesOut.add(n(exDay.length > 0 ? exDay[0] : null));
            waterData.add((int) Math.round(waterLogRepository.sumByUserIdAndDate(user.getId(), d)));
        }

        // Top yemekler ve egzersizler
        var topFoods = foodLogRepository.findTopFoods(user.getId(), startDate, PageRequest.of(0, 5));
        var topExercises = exerciseLogRepository.findTopExercises(user.getId(), startDate, PageRequest.of(0, 5));

        model.addAttribute("user", user); model.addAttribute("period", period); model.addAttribute("title", title);
        model.addAttribute("daysCount", daysCount);
        model.addAttribute("totalCalories", n(calStats.length > 0 ? calStats[0] : null));
        model.addAttribute("avgDailyCalories", n(calStats.length > 0 ? calStats[0] : null) / Math.max(daysCount, 1));
        model.addAttribute("totalProtein", n(calStats.length > 1 ? calStats[1] : null));
        model.addAttribute("totalCarbs", n(calStats.length > 2 ? calStats[2] : null));
        model.addAttribute("totalFat", n(calStats.length > 3 ? calStats[3] : null));
        model.addAttribute("exerciseTotalCal", n(exStats.length > 0 ? exStats[0] : null));
        model.addAttribute("exerciseTotalMin", n(exStats.length > 1 ? exStats[1] : null));
        model.addAttribute("exerciseSessions", n(exStats.length > 2 ? exStats[2] : null));
        model.addAttribute("waterTotalMl", (int) Math.round(waterTotal));
        model.addAttribute("waterAvgDaily", (int) Math.round(waterTotal / Math.max(daysCount, 1)));
        model.addAttribute("labels", labels); model.addAttribute("caloriesIn", caloriesIn);
        model.addAttribute("caloriesOut", caloriesOut); model.addAttribute("waterChartData", waterData);
        model.addAttribute("topFoods", topFoods); model.addAttribute("topExercises", topExercises);
        return "reports";
    }
}
