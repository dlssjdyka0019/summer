package com.charging.repository;

import com.charging.model.Pile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PileRepository extends JpaRepository<Pile, String> {
}
