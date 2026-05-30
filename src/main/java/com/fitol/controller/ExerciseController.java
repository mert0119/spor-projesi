package com.fitol.controller;

import com.fitol.model.ExerciseLog;
import com.fitol.model.User;
import com.fitol.repository.ExerciseLogRepository;
import com.fitol.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/exercise")
public class ExerciseController {

    private final ExerciseLogRepository exerciseLogRepository;
    private final UserRepository userRepository;

    // Egzersiz kutuphanesi
    public static final Map<String, List<Map<String, Object>>> EXERCISES = new LinkedHashMap<>();
    public static final Map<String, String> CATEGORY_NAMES = new LinkedHashMap<>();

    static {
        CATEGORY_NAMES.put("gogus", "Göğüs"); CATEGORY_NAMES.put("sirt", "Sırt");
        CATEGORY_NAMES.put("bacak", "Bacak"); CATEGORY_NAMES.put("omuz", "Omuz");
        CATEGORY_NAMES.put("kol", "Kol"); CATEGORY_NAMES.put("karin", "Karın");
        CATEGORY_NAMES.put("kardiyo", "Kardiyo"); CATEGORY_NAMES.put("esneklik", "Esneklik");

        EXERCISES.put("gogus", List.of(ex("Bench Press",8),ex("Dumbbell Press",7),ex("Şınav",7),ex("Cable Crossover",6),ex("İncline Press",8),ex("Decline Press",8),ex("Dips",8),ex("Dumbbell Fly",6),ex("Pec Deck",5)));
        EXERCISES.put("sirt", List.of(ex("Barfiks",9),ex("Lat Pulldown",7),ex("Barbell Row",8),ex("Dumbbell Row",7),ex("Cable Row",6),ex("Deadlift",10),ex("T-Bar Row",8),ex("Face Pull",5),ex("Hyperextension",5)));
        EXERCISES.put("bacak", List.of(ex("Squat",10),ex("Leg Press",8),ex("Leg Curl",6),ex("Leg Extension",6),ex("Lunge",9),ex("Calf Raise",5),ex("Bulgarian Split Squat",9),ex("Romanian Deadlift",8),ex("Hip Thrust",7)));
        EXERCISES.put("omuz", List.of(ex("Military Press",7),ex("Lateral Raise",5),ex("Front Raise",5),ex("Reverse Fly",5),ex("Arnold Press",7),ex("Shrug",5),ex("Dumbbell Shoulder Press",7),ex("Upright Row",6)));
        EXERCISES.put("kol", List.of(ex("Biceps Curl",5),ex("Hammer Curl",5),ex("Triceps Pushdown",5),ex("Skull Crusher",6),ex("Preacher Curl",5),ex("Triceps Dips",7),ex("EZ Bar Curl",5),ex("Cable Curl",5)));
        EXERCISES.put("karin", List.of(ex("Crunch",6),ex("Plank",5),ex("Bicycle Crunch",7),ex("Leg Raise",6),ex("Russian Twist",7),ex("Mountain Climber",10),ex("Ab Wheel Rollout",8)));
        EXERCISES.put("kardiyo", List.of(ex("Koşu",12),ex("Bisiklet",10),ex("Yüzme",11),ex("İp Atlama",13),ex("Yürüyüş",5),ex("HIIT",14),ex("Koşu Bandı",11),ex("Burpee",14),ex("Kettlebell Swing",12)));
        EXERCISES.put("esneklik", List.of(ex("Yoga",4),ex("Pilates",5),ex("Stretching",3),ex("Foam Rolling",3),ex("Dinamik Isınma",5)));
    }

    private static Map<String, Object> ex(String name, int cal) {
        Map<String, Object> m = new HashMap<>(); m.put("name", name); m.put("calPerMin", cal); return m;
    }

    public ExerciseController(ExerciseLogRepository exerciseLogRepository, UserRepository userRepository) {
        this.exerciseLogRepository = exerciseLogRepository;
        this.userRepository = userRepository;
    }

    private Object[] unwrap(Object[] raw) {
        if (raw == null || raw.length == 0) return new Object[]{0, 0, 0};
        if (raw[0] instanceof Object[]) return (Object[]) raw[0];
        return raw;
    }
    private int numI(Object obj) { return obj instanceof Number ? ((Number) obj).intValue() : 0; }

    @GetMapping
    public String log(@AuthenticationPrincipal UserDetails ud, @RequestParam(required = false) String date, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        LocalDate selectedDate = date != null ? tryParse(date) : LocalDate.now();
        var logs = exerciseLogRepository.findByUserIdAndDateOrderByCreatedAtDesc(user.getId(), selectedDate);
        Object[] totals = unwrap(exerciseLogRepository.sumByUserIdAndDate(user.getId(), selectedDate));
        model.addAttribute("user", user); model.addAttribute("logs", logs);
        model.addAttribute("exercises", EXERCISES); model.addAttribute("categoryNames", CATEGORY_NAMES);
        model.addAttribute("totalCalories", numI(totals.length > 0 ? totals[0] : null));
        model.addAttribute("totalDuration", numI(totals.length > 1 ? totals[1] : null));
        model.addAttribute("selectedDate", selectedDate); model.addAttribute("today", LocalDate.now());
        return "exercise/log";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal UserDetails ud,
                      @RequestParam String exerciseName, @RequestParam(defaultValue = "") String category,
                      @RequestParam(defaultValue = "0") int duration, @RequestParam(defaultValue = "0") int sets,
                      @RequestParam(defaultValue = "0") int reps, @RequestParam(defaultValue = "0") double weightKg,
                      @RequestParam(defaultValue = "0") double incline, @RequestParam(defaultValue = "0") double speed,
                      @RequestParam(defaultValue = "") String notes, @RequestParam(required = false) String date,
                      RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        LocalDate logDate = date != null ? tryParse(date) : LocalDate.now();
        if (exerciseName == null || exerciseName.isBlank()) { ra.addFlashAttribute("error","Egzersiz adı gerekli."); return "redirect:/exercise"; }

        int calPerMin = 7;
        for (var catExs : EXERCISES.values()) {
            for (var ex : catExs) {
                if (ex.get("name").equals(exerciseName)) { calPerMin = (int) ex.get("calPerMin"); break; }
            }
        }

        double caloriesBurned;
        boolean isCardio = "kardiyo".equals(category) || "esneklik".equals(category);
        if (isCardio && duration > 0) {
            double bonus = 1 + (incline * 0.03) + (Math.max(0, speed - 5) * 0.02);
            caloriesBurned = duration * calPerMin * bonus;
        } else if (sets > 0 && reps > 0) {
            caloriesBurned = sets * reps * 0.5 + (weightKg * 0.1 * sets * reps);
        } else {
            caloriesBurned = duration > 0 ? duration * calPerMin : 0;
        }

        ExerciseLog e = new ExerciseLog();
        e.setUserId(user.getId()); e.setDate(logDate); e.setExerciseName(exerciseName.trim());
        e.setCategory(category); e.setDuration(duration); e.setSets(sets); e.setReps(reps);
        e.setWeightKg(weightKg); e.setIncline(incline); e.setSpeed(speed);
        e.setCaloriesBurned((double) Math.round(caloriesBurned)); e.setNotes(notes);
        exerciseLogRepository.save(e);
        ra.addFlashAttribute("success", exerciseName.trim() + " eklendi!");
        return "redirect:/exercise?date=" + logDate;
    }

    @GetMapping("/delete/{id}")
    public String delete(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        ExerciseLog entry = exerciseLogRepository.findById(id).orElse(null);
        if (entry == null || !entry.getUserId().equals(user.getId())) { ra.addFlashAttribute("error","Yetkisiz."); return "redirect:/exercise"; }
        String d = entry.getDate().toString();
        exerciseLogRepository.delete(entry);
        ra.addFlashAttribute("info","Kayıt silindi.");
        return "redirect:/exercise?date=" + d;
    }

    @GetMapping("/programs")
    public String programs(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        return "exercise/programs";
    }

    @GetMapping("/history")
    public String history(@AuthenticationPrincipal UserDetails ud, @RequestParam(defaultValue = "0") int page, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        model.addAttribute("logs", exerciseLogRepository.findByUserIdOrderByDateDescCreatedAtDesc(user.getId(), PageRequest.of(page, 20)));
        model.addAttribute("user", user);
        return "exercise/history";
    }

    private LocalDate tryParse(String s) { try { return LocalDate.parse(s); } catch (Exception e) { return LocalDate.now(); } }
}
