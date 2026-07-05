package com.charging.dto;

import java.time.LocalDateTime;

public class SessionDTO {
    public String id;
    public Long userId;
    public String stationId;
    public String stationName;
    public String pileId;
    public String pileName;
    public Double duration;
    public Double amount;
    public String startTime;
    public String endTime;
    public String status;

    public SessionDTO(String id, Long userId, String stationId, String stationName,
                      String pileId, String pileName, Double duration, Double amount,
                      LocalDateTime startTime, LocalDateTime endTime, String status) {
        this.id = id; this.userId = userId; this.stationId = stationId;
        this.stationName = stationName; this.pileId = pileId; this.pileName = pileName;
        this.duration = duration; this.amount = amount;
        this.startTime = startTime != null ? startTime.toString() : null;
        this.endTime = endTime != null ? endTime.toString() : null;
        this.status = status;
    }
}
