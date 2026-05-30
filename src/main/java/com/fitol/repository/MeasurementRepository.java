package com.fitol.repository;

import com.fitol.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    List<Measurement> findByUserIdOrderByDateDesc(Long userId);

    Optional<Measurement> findFirstByUserIdOrderByDateDesc(Long userId);
}
