package com.fitol.controller;

import com.fitol.model.DietPlan;
import com.fitol.model.User;
import com.fitol.repository.DietPlanRepository;
import com.fitol.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/diet-plan")
public class DietPlanController {

    private final DietPlanRepository dietPlanRepository;
    private final UserRepository userRepository;

    public DietPlanController(DietPlanRepository dietPlanRepository, UserRepository userRepository) {
        this.dietPlanRepository = dietPlanRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("plans", dietPlanRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
        return "diet-plan/list";
    }

    @GetMapping("/create")
    public String createForm(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        return "diet-plan/create";
    }

    @PostMapping("/create")
    public String create(@AuthenticationPrincipal UserDetails ud,
                         @RequestParam String name, @RequestParam(defaultValue = "koruma") String goal,
                         @RequestParam(defaultValue = "2000") int dailyCalories,
                         @RequestParam(defaultValue = "30") double proteinRatio,
                         @RequestParam(defaultValue = "40") double carbRatio,
                         @RequestParam(defaultValue = "30") double fatRatio,
                         @RequestParam(defaultValue = "") String breakfast, @RequestParam(defaultValue = "") String lunch,
                         @RequestParam(defaultValue = "") String dinner, @RequestParam(defaultValue = "") String snacks,
                         @RequestParam(defaultValue = "") String notes, RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        DietPlan p = new DietPlan();
        p.setUserId(user.getId()); p.setName(name); p.setGoal(goal);
        p.setDailyCalories(dailyCalories); p.setProteinRatio(proteinRatio);
        p.setCarbRatio(carbRatio); p.setFatRatio(fatRatio);
        p.setBreakfast(breakfast); p.setLunch(lunch); p.setDinner(dinner);
        p.setSnacks(snacks); p.setNotes(notes);
        dietPlanRepository.save(p);
        ra.addFlashAttribute("success", "Diyet planı oluşturuldu!");
        return "redirect:/diet-plan";
    }

    @GetMapping("/toggle/{id}")
    public String toggle(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        DietPlan p = dietPlanRepository.findById(id).orElse(null);
        if (p == null || !p.getUserId().equals(user.getId())) { ra.addFlashAttribute("error","Yetkisiz."); return "redirect:/diet-plan"; }
        p.setIsActive(!p.getIsActive());
        dietPlanRepository.save(p);
        return "redirect:/diet-plan";
    }

    @GetMapping("/delete/{id}")
    public String delete(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        DietPlan p = dietPlanRepository.findById(id).orElse(null);
        if (p == null || !p.getUserId().equals(user.getId())) { ra.addFlashAttribute("error","Yetkisiz."); return "redirect:/diet-plan"; }
        dietPlanRepository.delete(p);
        ra.addFlashAttribute("info", "Plan silindi.");
        return "redirect:/diet-plan";
    }
}
