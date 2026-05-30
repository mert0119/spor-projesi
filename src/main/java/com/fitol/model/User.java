package com.fitol.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 80)
    private String username;

    @Column(unique = true, nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 256)
    private String passwordHash;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    // Profil bilgileri
    @Column(length = 50)
    private String firstName = "";

    @Column(length = 50)
    private String lastName = "";

    @Column
    private Integer age;

    @Column(length = 10)
    private String gender; // "erkek" veya "kadin"

    @Column
    private Double height; // cm

    @Column
    private Double weight; // kg

    @Column
    private Double targetWeight; // kg

    @Column(length = 20)
    private String activityLevel = "orta"; // dusuk, orta, yuksek, cok_yuksek

    @Column(length = 20)
    private String goal = "koruma"; // kilo_verme, kilo_alma, koruma

    @Column
    private Integer dailyCalorieGoal = 2000;

    @Column
    private Double dailyWaterGoal = 2.5; // litre

    @Column
    private Integer proteinGoal = 150; // gram

    @Column
    private Integer carbsGoal = 200; // gram

    @Column
    private Integer fatGoal = 65; // gram

    @Column(length = 200)
    private String profilePhoto = "";

    // BMR hesaplama (Mifflin-St Jeor)
    public Double calculateBmr() {
        if (weight == null || height == null || age == null || gender == null) return null;
        if ("erkek".equals(gender)) {
            return 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            return 10 * weight + 6.25 * height - 5 * age - 161;
        }
    }

    // TDEE hesaplama
    public Double calculateTdee() {
        Double bmr = calculateBmr();
        if (bmr == null) return null;
        double multiplier = switch (activityLevel != null ? activityLevel : "orta") {
            case "dusuk" -> 1.2;
            case "yuksek" -> 1.725;
            case "cok_yuksek" -> 1.9;
            default -> 1.55;
        };
        return bmr * multiplier;
    }

    // BMI hesaplama
    public Double calculateBmi() {
        if (weight == null || height == null) return null;
        double heightM = height / 100.0;
        return Math.round(weight / (heightM * heightM) * 10.0) / 10.0;
    }

    // BMI kategori
    public String getBmiCategory() {
        Double bmi = calculateBmi();
        if (bmi == null) return null;
        if (bmi < 18.5) return "Zayıf";
        else if (bmi < 25) return "Normal";
        else if (bmi < 30) return "Fazla Kilolu";
        else return "Obez";
    }

    // Getter ve Setter'lar
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(Double targetWeight) { this.targetWeight = targetWeight; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public Integer getDailyCalorieGoal() { return dailyCalorieGoal; }
    public void setDailyCalorieGoal(Integer dailyCalorieGoal) { this.dailyCalorieGoal = dailyCalorieGoal; }

    public Double getDailyWaterGoal() { return dailyWaterGoal; }
    public void setDailyWaterGoal(Double dailyWaterGoal) { this.dailyWaterGoal = dailyWaterGoal; }

    public Integer getProteinGoal() { return proteinGoal; }
    public void setProteinGoal(Integer proteinGoal) { this.proteinGoal = proteinGoal; }

    public Integer getCarbsGoal() { return carbsGoal; }
    public void setCarbsGoal(Integer carbsGoal) { this.carbsGoal = carbsGoal; }

    public Integer getFatGoal() { return fatGoal; }
    public void setFatGoal(Integer fatGoal) { this.fatGoal = fatGoal; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
}
