package com.example.backend.search;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.backend.register.UserEntity;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByUserId(Integer userId);

    boolean existsByReservationReId(Integer reId);

   
    List<Review> findByHotel_hIdOrderByCreatedAtDesc(Long hotelId);

    List<Review> findByHotel_hId(Long hotelId);

    List<Review> findByHotel_hIdIn(List<Long> hotelIds);

    
}