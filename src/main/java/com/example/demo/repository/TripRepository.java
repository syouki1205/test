package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.Trip;
import com.example.demo.entity.User;

public interface TripRepository extends JpaRepository<Trip, Long> {

	// ユーザーごとの全旅行を取得
	List<Trip> findByUser(User user);

	// ユーザーごとの直近1件の旅行を取得（開始日降順）
	List<Trip> findTop1ByUserOrderByStartDateDesc(User user);

	// ユーザーごとの旅行件数
	long countByUser(User user);

	// ユーザーごとの訪問都道府県数（重複なし）
	@Query("SELECT COUNT(DISTINCT t.prefecture) FROM Trip t WHERE t.user = :user AND t.prefecture IS NOT NULL")
	long countDistinctPrefectureByUser(User user);
}
