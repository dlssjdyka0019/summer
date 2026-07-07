package com.charging.service;

import com.charging.dto.*;
import com.charging.model.*;
import com.charging.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final SessionRepository sessionRepo;
    private final StationRepository stationRepo;
    private final PileRepository pileRepo;

    public SessionService(SessionRepository sessionRepo, StationRepository stationRepo, PileRepository pileRepo) {
        this.sessionRepo = sessionRepo;
        this.stationRepo = stationRepo;
        this.pileRepo = pileRepo;
    }

    public List<SessionDTO> getUserSessions(Long userId) {
        return sessionRepo.findByUserIdOrderByStartTimeDesc(userId).stream()
                .map(s -> new SessionDTO(s.getId(), s.getUserId(), s.getStationId(), s.getStationName(),
                        s.getPileId(), s.getPileName(), s.getDuration(), s.getAmount(),
                        s.getStartTime(), s.getEndTime(), s.getStatus()))
                .collect(Collectors.toList());
    }

    @Transactional
    public SessionDTO createSession(Long userId, SessionRequest req) {
        Station station = stationRepo.findById(req.station_id)
                .orElseThrow(() -> new RuntimeException("充电站不存在"));
        Pile pile = pileRepo.findById(req.pile_id)
                .orElseThrow(() -> new RuntimeException("充电桩不存在"));
        if (!"open".equals(pile.getStatus()))
            throw new RuntimeException("该充电桩当前不可用");

        // Activate pile
        pile.setStatus("in_use");
        pileRepo.save(pile);

        // Create session
        ChargingSession s = new ChargingSession();
        s.setId("ch" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
        s.setUserId(userId);
        s.setStationId(req.station_id);
        s.setStationName(station.getName());
        s.setPileId(req.pile_id);
        s.setPileName(pile.getName());
        s.setDuration(req.duration);
        s.setAmount(req.amount);
        s.setStartTime(java.time.LocalDateTime.now());
        s.setStatus("active");
        s = sessionRepo.save(s);

        return new SessionDTO(s.getId(), s.getUserId(), s.getStationId(), s.getStationName(),
                s.getPileId(), s.getPileName(), s.getDuration(), s.getAmount(),
                s.getStartTime(), s.getEndTime(), s.getStatus());
    }

    @Transactional
    public void stopSession(String sessionId, Long userId) {
        ChargingSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!s.getUserId().equals(userId))
            throw new RuntimeException("无权操作");
        if (!"active".equals(s.getStatus()))
            throw new RuntimeException("该充电已结束");

        s.setStatus("completed");
        s.setEndTime(java.time.LocalDateTime.now());
        sessionRepo.save(s);

        // Free pile
        Pile pile = pileRepo.findById(s.getPileId()).orElse(null);
        if (pile != null) {
            pile.setStatus("open");
            pileRepo.save(pile);
        }
    }
}
