package com.fitol.repository;

import com.fitol.model.WaterLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WaterLogRepository extends JpaRepository<WaterLog, Long> {

    List<WaterLog> findByUserIdAndDateOrderByCreatedAtDesc(Long userId, LocalDate date);

    @Query("SELECT COALESCE(SUM(w.amountMl), 0) FROM WaterLog w WHERE w.userId = :userId AND w.date = :date")
    Double sumByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(w.amountMl), 0) FROM WaterLog w " +
           "WHERE w.userId = :userId AND w.date BETWEEN :startDate AND :endDate")
    Double sumByUserIdAndDateRange(@Param("userId") Long userId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
}
