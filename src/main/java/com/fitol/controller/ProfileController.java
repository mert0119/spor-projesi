package com.fitol.controller;

import com.fitol.model.User;
import com.fitol.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String view(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editForm(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String edit(@AuthenticationPrincipal UserDetails ud,
                       @RequestParam(defaultValue = "") String firstName, @RequestParam(defaultValue = "") String lastName,
                       @RequestParam(defaultValue = "0") int age, @RequestParam(defaultValue = "") String gender,
                       @RequestParam(defaultValue = "0") double height, @RequestParam(defaultValue = "0") double weight,
                       @RequestParam(defaultValue = "0") double targetWeight,
                       @RequestParam(defaultValue = "orta") String activityLevel,
                       @RequestParam(defaultValue = "koruma") String goal,
                       @RequestParam(defaultValue = "150") int proteinGoal,
                       @RequestParam(defaultValue = "200") int carbsGoal,
                       @RequestParam(defaultValue = "65") int fatGoal,
                       RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        user.setFirstName(firstName.trim()); user.setLastName(lastName.trim());
        if (age > 0) user.setAge(age); user.setGender(gender);
        if (height > 0) user.setHeight(height); if (weight > 0) user.setWeight(weight);
        if (targetWeight > 0) user.setTargetWeight(targetWeight);
        user.setActivityLevel(activityLevel); user.setGoal(goal);
        user.setProteinGoal(proteinGoal); user.setCarbsGoal(carbsGoal); user.setFatGoal(fatGoal);

        // TDEE'den kalori hedefi
        Double tdee = user.calculateTdee();
        if (tdee != null) {
            switch (user.getGoal()) {
                case "kilo_verme" -> user.setDailyCalorieGoal((int) (tdee - 500));
                case "kilo_alma" -> user.setDailyCalorieGoal((int) (tdee + 500));
                default -> user.setDailyCalorieGoal((int) Math.round(tdee));
            }
        }
        // Su hedefi
        if (user.getWeight() != null) {
            user.setDailyWaterGoal(Math.round(user.getWeight() * 0.035 * 10.0) / 10.0);
        }

        userRepository.save(user);
        ra.addFlashAttribute("success", "Profil güncellendi! ✅");
        return "redirect:/profile";
    }
}
