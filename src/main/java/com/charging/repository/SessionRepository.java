package com.charging.repository;

import com.charging.model.ChargingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionRepository extends JpaRepository<ChargingSession, String> {
    List<ChargingSession> findByUserIdOrderByStartTimeDesc(Long userId);
    List<ChargingSession> findByUserIdAndStatus(Long userId, String status);
}
