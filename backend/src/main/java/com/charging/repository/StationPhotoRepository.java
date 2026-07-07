package com.charging.repository;

import com.charging.model.StationPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StationPhotoRepository extends JpaRepository<StationPhoto, String> {
    List<StationPhoto> findByStationIdAndStatusAndVisibleTrueOrderByCreatedAtDesc(String stationId, String status);
    List<StationPhoto> findByStationIdAndStatusOrderByCreatedAtDesc(String stationId, String status);
    List<StationPhoto> findByStatusOrderByCreatedAtDesc(String status);
    List<StationPhoto> findByStationIdOrderByCreatedAtDesc(String stationId);
}
