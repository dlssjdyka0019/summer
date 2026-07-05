package com.charging.repository;

import com.charging.model.ElectricityCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ElectricityCardRepository extends JpaRepository<ElectricityCard, String> {
    List<ElectricityCard> findByUserIdOrderByCreatedAtDesc(Long userId);
    int countByUserIdAndStatus(Long userId, String status);
    @Query("SELECT COUNT(c) FROM ElectricityCard c WHERE c.userId = :userId AND (c.status = 'active' OR c.status IS NULL)")
    int countActiveByUserId(@Param("userId") Long userId);
    default int countByUserId(Long userId) { return countActiveByUserId(userId); }
    List<ElectricityCard> findByPhotoId(String photoId);
    List<ElectricityCard> findByStatusOrderByCreatedAtDesc(String status);
}
