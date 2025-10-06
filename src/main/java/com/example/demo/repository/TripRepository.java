package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Trip;
import com.example.demo.entity.User;

public interface TripRepository extends JpaRepository<Trip, Long> {
    // ユーザーごとの旅行を取得するメソッド
    List<Trip> findByUser(User user);
}
