package com.fitol.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CalculatorService {

    public Map<String, Object> calculate(double weight, double height, int age, String gender, String activityLevel) {
        Map<String, Object> results = new HashMap<>();

        // BMI
        double heightM = height / 100.0;
        double bmi = Math.round(weight / (heightM * heightM) * 10.0) / 10.0;
        String bmiCategory;
        String bmiColor;

        if (bmi < 18.5) {
            bmiCategory = "Zayıf";
            bmiColor = "#3498db";
        } else if (bmi < 25) {
            bmiCategory = "Normal";
            bmiColor = "#2ecc71";
        } else if (bmi < 30) {
            bmiCategory = "Fazla Kilolu";
            bmiColor = "#f39c12";
        } else {
            bmiCategory = "Obez";
            bmiColor = "#e74c3c";
        }

        // BMR (Mifflin-St Jeor)
        double bmr;
        if ("erkek".equals(gender)) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        }

        // TDEE
        double multiplier = switch (activityLevel) {
            case "dusuk" -> 1.2;
            case "yuksek" -> 1.725;
            case "cok_yuksek" -> 1.9;
            default -> 1.55;
        };
        double tdee = bmr * multiplier;

        // Ideal kilo (Devine formulu)
        double idealWeight;
        if ("erkek".equals(gender)) {
            idealWeight = 50 + 2.3 * ((height / 2.54) - 60);
        } else {
            idealWeight = 45.5 + 2.3 * ((height / 2.54) - 60);
        }

        // Vucut yag orani tahmini
        double bodyFat;
        if ("erkek".equals(gender)) {
            bodyFat = Math.round((1.2 * bmi + 0.23 * age - 16.2) * 10.0) / 10.0;
        } else {
            bodyFat = Math.round((1.2 * bmi + 0.23 * age - 5.4) * 10.0) / 10.0;
        }

        results.put("bmi", bmi);
        results.put("bmiCategory", bmiCategory);
        results.put("bmiColor", bmiColor);
        results.put("bmr", (int) Math.round(bmr));
        results.put("tdee", (int) Math.round(tdee));
        results.put("idealWeight", Math.round(idealWeight * 10.0) / 10.0);
        results.put("bodyFat", Math.max(bodyFat, 5));
        results.put("caloriesLose", (int) Math.round(tdee - 500));
        results.put("caloriesGain", (int) Math.round(tdee + 500));
        results.put("caloriesMaintain", (int) Math.round(tdee));

        return results;
    }
}
