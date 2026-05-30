package com.fitol.controller;

import com.fitol.model.Reminder;
import com.fitol.model.User;
import com.fitol.repository.ReminderRepository;
import com.fitol.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reminder")
public class ReminderController {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    public ReminderController(ReminderRepository reminderRepository, UserRepository userRepository) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("reminders", reminderRepository.findByUserIdOrderByReminderTimeAsc(user.getId()));
        return "reminder/index";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal UserDetails ud,
                      @RequestParam String title, @RequestParam(defaultValue = "") String message,
                      @RequestParam String reminderTime, @RequestParam(defaultValue = "daily") String repeatType,
                      RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        Reminder r = new Reminder();
        r.setUserId(user.getId()); r.setTitle(title); r.setMessage(message);
        r.setReminderTime(reminderTime); r.setRepeatType(repeatType);
        reminderRepository.save(r);
        ra.addFlashAttribute("success", "Hatırlatıcı eklendi! ⏰");
        return "redirect:/reminder";
    }

    @GetMapping("/toggle/{id}")
    public String toggle(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        Reminder r = reminderRepository.findById(id).orElse(null);
        if (r == null || !r.getUserId().equals(user.getId())) { ra.addFlashAttribute("error","Yetkisiz."); return "redirect:/reminder"; }
        r.setIsActive(!r.getIsActive());
        reminderRepository.save(r);
        return "redirect:/reminder";
    }

    @GetMapping("/delete/{id}")
    public String delete(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        Reminder r = reminderRepository.findById(id).orElse(null);
        if (r == null || !r.getUserId().equals(user.getId())) { ra.addFlashAttribute("error","Yetkisiz."); return "redirect:/reminder"; }
        reminderRepository.delete(r);
        ra.addFlashAttribute("info", "Hatırlatıcı silindi.");
        return "redirect:/reminder";
    }
}
