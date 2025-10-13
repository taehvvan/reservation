package com.example.backend.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityService {

    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final RoomRepository roomRepository;

    // 예약 가능 여부만 체크 (true / false)
    public boolean isRoomAvailable(Integer rId, LocalDate checkin, LocalDate checkout, int quantity) {
        List<LocalDate> dates = checkin.datesUntil(checkout).toList();
        for (LocalDate date : dates) {
            RoomAvailability ra = roomAvailabilityRepository
                    .findByRoom_rIdAndDate(rId, date)
                    .orElseGet(() -> {
                        Room room = roomRepository.findById(rId)
                                .orElseThrow(() -> new RuntimeException("객실 정보 없음"));
                        return roomAvailabilityRepository.save(new RoomAvailability(null, room, date, room.getCount()));
                    });
            if (ra.getAvailableCount() < quantity) return false;
        }
        return true;
    }

    // 날짜별 객실 수 리스트 조회
    @Transactional(readOnly = true)
    public List<RoomAvailabilityDTO> getAvailabilities(Integer rId, LocalDate checkin, LocalDate checkout) {
        List<LocalDate> dates = checkin.datesUntil(checkout).toList();
        List<RoomAvailabilityDTO> result = new ArrayList<>();

        for (LocalDate date : dates) {
            RoomAvailability ra = roomAvailabilityRepository
                    .findByRoom_rIdAndDate(rId, date)
                    .orElseGet(() -> {
                        Room room = roomRepository.findById(rId)
                                .orElseThrow(() -> new RuntimeException("객실 정보 없음"));
                        return roomAvailabilityRepository.save(
                                new RoomAvailability(null, room, date, room.getCount())
                        );
                    });
            result.add(new RoomAvailabilityDTO(ra));
        }
        return result;
    }

    // 예약 후 객실 수 차감
    @Transactional
    public void reserveRoom(Integer rId, LocalDate checkin, LocalDate checkout, int quantity) {

        if (rId == null || checkin == null || checkout == null) {
            throw new IllegalArgumentException("객실 재고를 차감하기 위한 필수 정보(roomId, checkin, checkout)가 누락되었습니다.");
        }

        // 1. 재고 차감의 기준이 되는 원본 객실 정보를 가져옵니다.
        Room room = roomRepository.findById(rId)
                .orElseThrow(() -> new RuntimeException("객실 정보를 찾을 수 없습니다. ID: " + rId));
        int totalCount = room.getCount(); // 이 객실의 전체 개수

        List<LocalDate> dates = checkin.datesUntil(checkout).toList();

        for (LocalDate date : dates) {
            
           RoomAvailability ra = roomAvailabilityRepository.findByRoom_rIdAndDate(rId, date)
                    .orElseGet(() -> {
                        RoomAvailability newRa = new RoomAvailability();
                        newRa.setRoom(room);
                        newRa.setDate(date);
                        newRa.setAvailableCount(totalCount); // 최초 재고는 객실의 총 개수
                        return newRa;
                    });

            // 5. 해당 날짜에 예약 가능한 재고가 있는지 확인합니다.
            if (ra.getAvailableCount() < quantity) {
                // 재고가 부족하면 예외를 발생시켜 전체 트랜잭션을 롤백합니다.
                throw new RuntimeException("재고가 부족합니다. [날짜: " + date + ", 남은 객실: " + ra.getAvailableCount() + "]");
            }

            // 6. 재고를 차감합니다.
            ra.setAvailableCount(ra.getAvailableCount() - quantity);

            // 7. 변경된 재고 정보를 해당 날짜로 저장(또는 업데이트)합니다.
            roomAvailabilityRepository.save(ra);
        }
    }

    @Transactional
    public void cancelRoomReservation(Integer rId, LocalDate checkin, LocalDate checkout, int quantity) {
        // 체크아웃 날짜는 포함하지 않으므로, datesUntil의 두 번째 인자는 checkout 날짜 그대로 사용합니다.
        List<LocalDate> dates = checkin.datesUntil(checkout).toList();

        for (LocalDate date : dates) {
            // 해당 날짜의 재고 정보를 찾습니다.
            RoomAvailability ra = roomAvailabilityRepository
                    .findByRoom_rIdAndDate(rId, date)
                    // 만약 재고 정보가 없다면 로직상 문제가 있는 것이므로 예외를 발생시킵니다.
                    // (예약했던 기록이 있다면 반드시 재고 정보가 있어야 합니다.)
                    .orElseThrow(() -> new RuntimeException("객실 재고 정보를 찾을 수 없습니다. [rId: " + rId + ", date: " + date + "]"));
            
            System.out.println("[예약 취소 전] 날짜: " + date + ", 남은 객실: " + ra.getAvailableCount());

            // ✅ 재고를 다시 1 증가시킵니다.
            ra.setAvailableCount(ra.getAvailableCount() + quantity);
            roomAvailabilityRepository.save(ra);

            System.out.println("[예약 취소 후] 날짜: " + date + ", 남은 객실: " + ra.getAvailableCount());
        }
    }
}