package com.fitol.repository;

import com.fitol.model.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {

    List<DietPlan> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<DietPlan> findByUserIdAndIsActiveTrue(Long userId);
}
