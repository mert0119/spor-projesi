package com.fitol.repository;

import com.fitol.model.ExerciseLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, Long> {

    List<ExerciseLog> findByUserIdAndDateOrderByCreatedAtDesc(Long userId, LocalDate date);

    @Query("SELECT COALESCE(SUM(e.caloriesBurned), 0), COALESCE(SUM(e.duration), 0) " +
           "FROM ExerciseLog e WHERE e.userId = :userId AND e.date = :date")
    Object[] sumByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(e.caloriesBurned), 0), COALESCE(SUM(e.duration), 0), COUNT(e) " +
           "FROM ExerciseLog e WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate")
    Object[] sumStatsByUserIdAndDateRange(@Param("userId") Long userId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT e.exerciseName, COUNT(e), COALESCE(SUM(e.caloriesBurned), 0) FROM ExerciseLog e " +
           "WHERE e.userId = :userId AND e.date >= :startDate " +
           "GROUP BY e.exerciseName ORDER BY COUNT(e) DESC")
    List<Object[]> findTopExercises(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, Pageable pageable);

    Page<ExerciseLog> findByUserIdOrderByDateDescCreatedAtDesc(Long userId, Pageable pageable);
}
