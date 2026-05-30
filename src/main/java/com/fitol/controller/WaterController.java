package com.fitol.controller;

import com.fitol.model.User;
import com.fitol.model.WaterLog;
import com.fitol.repository.UserRepository;
import com.fitol.repository.WaterLogRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/water")
public class WaterController {

    private final WaterLogRepository waterLogRepository;
    private final UserRepository userRepository;

    public WaterController(WaterLogRepository waterLogRepository, UserRepository userRepository) {
        this.waterLogRepository = waterLogRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal UserDetails ud, @RequestParam(required = false) String date, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        LocalDate selectedDate = tryParse(date);
        var logs = waterLogRepository.findByUserIdAndDateOrderByCreatedAtDesc(user.getId(), selectedDate);
        double totalMl = waterLogRepository.sumByUserIdAndDate(user.getId(), selectedDate);
        double goalMl = user.getDailyWaterGoal() * 1000;
        int percentage = goalMl > 0 ? Math.min((int) Math.round((totalMl / goalMl) * 100), 100) : 0;

        model.addAttribute("user", user); model.addAttribute("logs", logs);
        model.addAttribute("totalMl", (int) Math.round(totalMl));
        model.addAttribute("totalLitre", Math.round(totalMl / 100.0) / 10.0);
        model.addAttribute("goalMl", (int) Math.round(goalMl));
        model.addAttribute("goalLitre", user.getDailyWaterGoal());
        model.addAttribute("percentage", percentage);
        model.addAttribute("selectedDate", selectedDate); model.addAttribute("today", LocalDate.now());
        return "water";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal UserDetails ud,
                      @RequestParam(defaultValue = "glass") String amountType,
                      @RequestParam(defaultValue = "200") double amountMl,
                      @RequestParam(required = false) String date, RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        LocalDate logDate = tryParse(date);
        double ml = switch (amountType) {
            case "glass" -> 200; case "bottle_small" -> 500; case "bottle_large" -> 1000;
            case "custom" -> amountMl; default -> 200;
        };
        WaterLog e = new WaterLog();
        e.setUserId(user.getId()); e.setDate(logDate); e.setAmountMl(ml);
        waterLogRepository.save(e);
        ra.addFlashAttribute("success", (int) ml + "ml su eklendi! 💧");
        return "redirect:/water?date=" + logDate;
    }

    @GetMapping("/delete/{id}")
    public String delete(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        WaterLog entry = waterLogRepository.findById(id).orElse(null);
        if (entry == null || !entry.getUserId().equals(user.getId())) { ra.addFlashAttribute("error", "Yetkisiz."); return "redirect:/water"; }
        String d = entry.getDate().toString();
        waterLogRepository.delete(entry);
        ra.addFlashAttribute("info", "Kayıt silindi.");
        return "redirect:/water?date=" + d;
    }

    private LocalDate tryParse(String s) { if (s != null) { try { return LocalDate.parse(s); } catch (Exception e) {} } return LocalDate.now(); }
}
