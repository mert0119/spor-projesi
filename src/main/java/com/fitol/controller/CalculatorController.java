package com.fitol.controller;

import com.fitol.model.User;
import com.fitol.repository.UserRepository;
import com.fitol.service.CalculatorService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/calculator")
public class CalculatorController {

    private final CalculatorService calculatorService;
    private final UserRepository userRepository;

    public CalculatorController(CalculatorService calculatorService, UserRepository userRepository) {
        this.calculatorService = calculatorService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("results", null);
        return "calculator";
    }

    @PostMapping
    public String calculate(@AuthenticationPrincipal UserDetails ud,
                            @RequestParam double weight, @RequestParam double height,
                            @RequestParam int age, @RequestParam(defaultValue = "erkek") String gender,
                            @RequestParam(defaultValue = "orta") String activityLevel, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        model.addAttribute("user", user);

        if (weight <= 0 || height <= 0 || age <= 0) {
            model.addAttribute("error", "Lütfen geçerli değerler girin.");
            return "calculator";
        }

        Map<String, Object> results = calculatorService.calculate(weight, height, age, gender, activityLevel);
        results.put("weight", weight); results.put("height", height);
        results.put("age", age); results.put("gender", gender);
        results.put("activityLevel", activityLevel);
        model.addAttribute("results", results);
        return "calculator";
    }
}
