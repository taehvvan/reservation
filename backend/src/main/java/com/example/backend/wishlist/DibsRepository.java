package com.example.backend.wishlist;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DibsRepository extends JpaRepository<DibsEntity, DibsId> {
    List<DibsEntity> findByUser_Email(String email);

    @Modifying
    @Query("DELETE FROM DibsEntity d WHERE d.user.email = :email AND d.hotel.hId = :hotelId")
    void deleteByUserEmailAndHotelHId(@Param("email") String email, @Param("hotelId") Long hotelId);
}