package com.fitol.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diet_plans")
public class DietPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 20)
    private String goal; // kilo_verme, kilo_alma, koruma

    @Column
    private Integer dailyCalories;

    @Column
    private Double proteinRatio = 30.0; // yuzde

    @Column
    private Double carbRatio = 40.0; // yuzde

    @Column
    private Double fatRatio = 30.0; // yuzde

    @Column(columnDefinition = "TEXT")
    private String breakfast = "";

    @Column(columnDefinition = "TEXT")
    private String lunch = "";

    @Column(columnDefinition = "TEXT")
    private String dinner = "";

    @Column(columnDefinition = "TEXT")
    private String snacks = "";

    @Column(columnDefinition = "TEXT")
    private String notes = "";

    @Column
    private Boolean isActive = true;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    // Makro gram hesaplamalari
    public int getProteinGrams() {
        if (dailyCalories != null) {
            return (int) Math.round((dailyCalories * proteinRatio / 100.0) / 4.0);
        }
        return 0;
    }

    public int getCarbGrams() {
        if (dailyCalories != null) {
            return (int) Math.round((dailyCalories * carbRatio / 100.0) / 4.0);
        }
        return 0;
    }

    public int getFatGrams() {
        if (dailyCalories != null) {
            return (int) Math.round((dailyCalories * fatRatio / 100.0) / 9.0);
        }
        return 0;
    }

    // Getter ve Setter'lar
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public Integer getDailyCalories() { return dailyCalories; }
    public void setDailyCalories(Integer dailyCalories) { this.dailyCalories = dailyCalories; }

    public Double getProteinRatio() { return proteinRatio; }
    public void setProteinRatio(Double proteinRatio) { this.proteinRatio = proteinRatio; }

    public Double getCarbRatio() { return carbRatio; }
    public void setCarbRatio(Double carbRatio) { this.carbRatio = carbRatio; }

    public Double getFatRatio() { return fatRatio; }
    public void setFatRatio(Double fatRatio) { this.fatRatio = fatRatio; }

    public String getBreakfast() { return breakfast; }
    public void setBreakfast(String breakfast) { this.breakfast = breakfast; }

    public String getLunch() { return lunch; }
    public void setLunch(String lunch) { this.lunch = lunch; }

    public String getDinner() { return dinner; }
    public void setDinner(String dinner) { this.dinner = dinner; }

    public String getSnacks() { return snacks; }
    public void setSnacks(String snacks) { this.snacks = snacks; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
