package com.example.backend.review;

import com.example.backend.register.UserEntity;
import com.example.backend.register.UserRepository;
import com.example.backend.reservation.Reservation;
import com.example.backend.reservation.ReservationRepository;
import com.example.backend.search.Hotel;
import com.example.backend.search.Review;
import com.example.backend.search.ReviewDTO;
import com.example.backend.search.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ReviewDTO saveReview(ReviewDTO reviewDto, Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Reservation reservation = reservationRepository.findById(reviewDto.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        Hotel hotel = reservation.getHotel();

        Review review = new Review();
        review.setUser(user);
        review.setHotel(hotel);
        review.setReservation(reservation);
        review.setScore(reviewDto.getScore());
        review.setContent(reviewDto.getContent());
        
        Review savedReview = reviewRepository.save(review);
        
        // 예약 상태를 '리뷰 작성 완료'로 변경
        reservation.setStatus("리뷰 작성 완료");
        reservationRepository.save(reservation);

        return new ReviewDTO(savedReview);
    }

    public List<ReviewDTO> findReviewsByUserId(Integer userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList());
    }


    
}