package com.example.backend.inquiry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Integer> {
    
    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.user")
    List<Inquiry> findAllWithUser();

    // [추가] 모든 문의를 User 정보와 함께 최신순으로 조회
    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.user ORDER BY i.createdAt DESC")
    List<Inquiry> findAllWithUserOrderByCreatedAtDesc();
    
    // 특정 사용자의 문의 목록을 조회
    List<Inquiry> findByUser_Id(Integer userId);
}
