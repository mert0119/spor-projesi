package com.fitol.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "measurements")
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate date = LocalDate.now();

    @Column
    private Double weight; // kg

    @Column
    private Double waist; // cm - bel

    @Column
    private Double chest; // cm - gogus

    @Column
    private Double arm; // cm - kol

    @Column
    private Double leg; // cm - bacak

    @Column
    private Double hip; // cm - kalca

    @Column
    private Double bodyFat; // yuzde

    @Column(columnDefinition = "TEXT")
    private String notes = "";

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getter ve Setter'lar
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getWaist() { return waist; }
    public void setWaist(Double waist) { this.waist = waist; }

    public Double getChest() { return chest; }
    public void setChest(Double chest) { this.chest = chest; }

    public Double getArm() { return arm; }
    public void setArm(Double arm) { this.arm = arm; }

    public Double getLeg() { return leg; }
    public void setLeg(Double leg) { this.leg = leg; }

    public Double getHip() { return hip; }
    public void setHip(Double hip) { this.hip = hip; }

    public Double getBodyFat() { return bodyFat; }
    public void setBodyFat(Double bodyFat) { this.bodyFat = bodyFat; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
