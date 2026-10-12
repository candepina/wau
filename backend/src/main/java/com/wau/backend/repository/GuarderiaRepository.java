package com.wau.backend.repository;

import com.wau.backend.entity.Guarderia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuarderiaRepository extends JpaRepository<Guarderia, Long> {
}