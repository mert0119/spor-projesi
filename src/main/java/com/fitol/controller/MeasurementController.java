package com.fitol.controller;

import com.fitol.model.Measurement;
import com.fitol.model.User;
import com.fitol.repository.MeasurementRepository;
import com.fitol.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/measurement")
public class MeasurementController {

    private final MeasurementRepository measurementRepository;
    private final UserRepository userRepository;

    public MeasurementController(MeasurementRepository measurementRepository, UserRepository userRepository) {
        this.measurementRepository = measurementRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("measurements", measurementRepository.findByUserIdOrderByDateDesc(user.getId()));
        model.addAttribute("today", LocalDate.now());
        return "measurement/index";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal UserDetails ud,
                      @RequestParam(required = false) String date,
                      @RequestParam(defaultValue = "0") double weight, @RequestParam(defaultValue = "0") double waist,
                      @RequestParam(defaultValue = "0") double chest, @RequestParam(defaultValue = "0") double arm,
                      @RequestParam(defaultValue = "0") double leg, @RequestParam(defaultValue = "0") double hip,
                      @RequestParam(defaultValue = "0") double bodyFat, @RequestParam(defaultValue = "") String notes,
                      RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        Measurement m = new Measurement();
        m.setUserId(user.getId());
        m.setDate(date != null && !date.isBlank() ? LocalDate.parse(date) : LocalDate.now());
        if (weight > 0) m.setWeight(weight); if (waist > 0) m.setWaist(waist);
        if (chest > 0) m.setChest(chest); if (arm > 0) m.setArm(arm);
        if (leg > 0) m.setLeg(leg); if (hip > 0) m.setHip(hip);
        if (bodyFat > 0) m.setBodyFat(bodyFat); m.setNotes(notes);
        measurementRepository.save(m);
        ra.addFlashAttribute("success", "Ölçüm kaydedildi! 📏");
        return "redirect:/measurement";
    }

    @GetMapping("/delete/{id}")
    public String delete(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        Measurement m = measurementRepository.findById(id).orElse(null);
        if (m == null || !m.getUserId().equals(user.getId())) { ra.addFlashAttribute("error","Yetkisiz."); return "redirect:/measurement"; }
        measurementRepository.delete(m);
        ra.addFlashAttribute("info", "Kayıt silindi.");
        return "redirect:/measurement";
    }
}
