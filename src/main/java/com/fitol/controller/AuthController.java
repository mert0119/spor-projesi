package com.fitol.controller;

import com.fitol.model.User;
import com.fitol.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "E-posta veya şifre hatalı.");
        }
        if (logout != null) {
            model.addAttribute("message", "Başarıyla çıkış yapıldı.");
        }
        return "auth/login";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String password2,
                           RedirectAttributes redirectAttributes) {

        // Dogrulama
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Tüm alanları doldurun.");
            return "redirect:/register";
        }
        if (!password.equals(password2)) {
            redirectAttributes.addFlashAttribute("error", "Şifreler eşleşmiyor.");
            return "redirect:/register";
        }
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Şifre en az 6 karakter olmalı.");
            return "redirect:/register";
        }
        if (userRepository.existsByEmail(email.trim())) {
            redirectAttributes.addFlashAttribute("error", "Bu e-posta zaten kayıtlı.");
            return "redirect:/register";
        }
        if (userRepository.existsByUsername(username.trim())) {
            redirectAttributes.addFlashAttribute("error", "Bu kullanıcı adı zaten alınmış.");
            return "redirect:/register";
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Hesabınız oluşturuldu! Giriş yapabilirsiniz. 🎉");
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }
}
