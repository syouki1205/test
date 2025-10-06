package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Spot;

public interface SpotRepository extends JpaRepository<Spot, Long> {}
