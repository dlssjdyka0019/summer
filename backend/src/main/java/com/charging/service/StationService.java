package com.charging.service;

import com.charging.dto.*;
import com.charging.model.*;
import com.charging.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StationService {

    private final StationRepository stationRepo;
    private final PileRepository pileRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public StationService(StationRepository stationRepo, PileRepository pileRepo) {
        this.stationRepo = stationRepo;
        this.pileRepo = pileRepo;
    }

    public List<StationDTO> getAllStations() {
        return stationRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public StationDTO createStation(StationDTO dto, String id) {
        Station s = new Station();
        s.setId(id != null ? id : "s" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
        applyDTO(s, dto);
        s = stationRepo.save(s);
        // Save piles
        if (dto.piles != null) {
            for (PileDTO pd : dto.piles) {
                Pile p = new Pile();
                p.setId(pd.id != null ? pd.id : "p" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
                p.setStation(s);
                p.setName(pd.name);
                p.setType(pd.type);
                p.setPower(pd.power);
                p.setPrice(pd.price);
                p.setStatus(pd.status);
                pileRepo.save(p);
            }
        }
        return toDTO(stationRepo.findById(s.getId()).orElse(s));
    }

    @Transactional
    public StationDTO updateStation(String id, StationDTO dto) {
        Station s = stationRepo.findById(id).orElseThrow(() -> new RuntimeException("充电站不存在"));
        applyDTO(s, dto);
        // Replace piles
        pileRepo.deleteAll(s.getPiles());
        pileRepo.flush(); // 立即同步删除操作，清除 Hibernate 会话中的已删除实体
        s.getPiles().clear();
        if (dto.piles != null) {
            for (PileDTO pd : dto.piles) {
                Pile p = new Pile();
                p.setId(pd.id != null ? pd.id : "p" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
                p.setStation(s);
                p.setName(pd.name);
                p.setType(pd.type);
                p.setPower(pd.power);
                p.setPrice(pd.price);
                p.setStatus(pd.status);
                s.getPiles().add(p);
            }
        }
        s = stationRepo.save(s);
        return toDTO(s);
    }

    public void deleteStation(String id) {
        stationRepo.deleteById(id);
    }

    public PileDTO addPile(String stationId, PileDTO dto) {
        Station s = stationRepo.findById(stationId).orElseThrow(() -> new RuntimeException("充电站不存在"));
        Pile p = new Pile();
        p.setId(dto.id != null ? dto.id : "p" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        p.setStation(s);
        p.setName(dto.name);
        p.setType(dto.type);
        p.setPower(dto.power);
        p.setPrice(dto.price);
        p.setStatus(dto.status);
        p = pileRepo.save(p);
        dto.id = p.getId();
        return dto;
    }

    public void updatePile(String pileId, PileDTO dto) {
        Pile p = pileRepo.findById(pileId).orElseThrow(() -> new RuntimeException("充电桩不存在"));
        p.setName(dto.name);
        p.setType(dto.type);
        p.setPower(dto.power);
        p.setPrice(dto.price);
        p.setStatus(dto.status);
        pileRepo.save(p);
    }

    public void deletePile(String pileId) {
        pileRepo.deleteById(pileId);
    }

    // === Helpers ===
    private void applyDTO(Station s, StationDTO dto) {
        s.setName(dto.name);
        s.setAddress(dto.address);
        s.setLongitude(dto.longitude);
        s.setLatitude(dto.latitude);
        s.setOperator(dto.operator);
        s.setPower(dto.power);
        try { s.setChargerTypes(mapper.writeValueAsString(dto.chargerTypes)); } catch (Exception e) { s.setChargerTypes("[]"); }
        s.setPrice(dto.price);
        s.setOpenHours(dto.openHours);
        s.setPhone(dto.phone);
        s.setStatus(dto.status);
        s.setDescription(dto.desc);
        s.setUpdatedAt(java.time.LocalDateTime.now());
    }

    private StationDTO toDTO(Station s) {
        StationDTO dto = new StationDTO();
        dto.id = s.getId();
        dto.name = s.getName();
        dto.address = s.getAddress();
        dto.longitude = s.getLongitude();
        dto.latitude = s.getLatitude();
        dto.operator = s.getOperator();
        dto.power = s.getPower();
        try { dto.chargerTypes = mapper.readValue(s.getChargerTypes(), new TypeReference<List<String>>() {}); } catch (Exception e) { dto.chargerTypes = List.of(); }
        dto.price = s.getPrice();
        dto.openHours = s.getOpenHours();
        dto.phone = s.getPhone();
        dto.status = s.getStatus();
        dto.desc = s.getDescription();

        List<Pile> piles = s.getPiles();
        if (piles != null) {
            dto.piles = piles.stream().map(p -> {
                PileDTO pd = new PileDTO();
                pd.id = p.getId(); pd.name = p.getName(); pd.type = p.getType();
                pd.power = p.getPower(); pd.price = p.getPrice(); pd.status = p.getStatus();
                return pd;
            }).collect(Collectors.toList());
        }
        dto.totalSpots = dto.piles.size();
        dto.availableSpots = (int) dto.piles.stream().filter(p -> "open".equals(p.status)).count();
        return dto;
    }
}
