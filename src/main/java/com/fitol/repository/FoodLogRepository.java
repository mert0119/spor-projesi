package com.fitol.repository;

import com.fitol.model.FoodLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {

    List<FoodLog> findByUserIdAndDateAndMealTypeOrderByCreatedAtDesc(Long userId, LocalDate date, String mealType);

    List<FoodLog> findByUserIdAndDateOrderByCreatedAtDesc(Long userId, LocalDate date);

    @Query("SELECT COALESCE(SUM(f.calories), 0) FROM FoodLog f WHERE f.userId = :userId AND f.date = :date")
    Double sumCaloriesByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(f.protein), 0), COALESCE(SUM(f.carbs), 0), COALESCE(SUM(f.fat), 0) " +
           "FROM FoodLog f WHERE f.userId = :userId AND f.date = :date")
    Object[] sumMacrosByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(f.calories), 0), COALESCE(SUM(f.protein), 0), " +
           "COALESCE(SUM(f.carbs), 0), COALESCE(SUM(f.fat), 0) " +
           "FROM FoodLog f WHERE f.userId = :userId AND f.date BETWEEN :startDate AND :endDate")
    Object[] sumStatsByUserIdAndDateRange(@Param("userId") Long userId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT f.foodName, COUNT(f), AVG(f.calories) FROM FoodLog f " +
           "WHERE f.userId = :userId AND f.date >= :startDate " +
           "GROUP BY f.foodName ORDER BY COUNT(f) DESC")
    List<Object[]> findTopFoods(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, Pageable pageable);

    Page<FoodLog> findByUserIdOrderByDateDescCreatedAtDesc(Long userId, Pageable pageable);
}
