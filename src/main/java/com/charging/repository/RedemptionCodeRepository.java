package com.charging.repository;

import com.charging.model.RedemptionCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RedemptionCodeRepository extends JpaRepository<RedemptionCode, String> {
    Optional<RedemptionCode> findByCode(String code);
    List<RedemptionCode> findByStatusOrderByCreatedAtDesc(String status);
    List<RedemptionCode> findAllByOrderByCreatedAtDesc();
}
