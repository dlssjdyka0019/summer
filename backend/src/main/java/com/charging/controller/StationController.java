package com.charging.controller;

import com.charging.dto.*;
import com.charging.service.AuthService;
import com.charging.service.StationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StationController {

    private final StationService stationService;
    private final AuthService authService;

    public StationController(StationService stationService, AuthService authService) {
        this.stationService = stationService;
        this.authService = authService;
    }

    @GetMapping("/stations")
    public List<StationDTO> getAll() {
        return stationService.getAllStations();
    }

    @PostMapping("/stations")
    public Map<String, Object> create(@RequestBody StationDTO dto, @RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        StationDTO created = stationService.createStation(dto, dto.id);
        return Map.of("ok", true, "id", created.id);
    }

    @PutMapping("/stations/{id}")
    public Map<String, Object> update(@PathVariable String id, @RequestBody StationDTO dto,
                                       @RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        stationService.updateStation(id, dto);
        return Map.of("ok", true);
    }

    @DeleteMapping("/stations/{id}")
    public Map<String, Object> delete(@PathVariable String id, @RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        stationService.deleteStation(id);
        return Map.of("ok", true);
    }

    @PostMapping("/stations/{sid}/piles")
    public Map<String, Object> addPile(@PathVariable String sid, @RequestBody PileDTO dto,
                                        @RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        stationService.addPile(sid, dto);
        return Map.of("ok", true, "id", dto.id);
    }

    @PutMapping("/piles/{pid}")
    public Map<String, Object> updatePile(@PathVariable String pid, @RequestBody PileDTO dto,
                                           @RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        stationService.updatePile(pid, dto);
        return Map.of("ok", true);
    }

    @DeleteMapping("/piles/{pid}")
    public Map<String, Object> deletePile(@PathVariable String pid, @RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        stationService.deletePile(pid);
        return Map.of("ok", true);
    }
}
