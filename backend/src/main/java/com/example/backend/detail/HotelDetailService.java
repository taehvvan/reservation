package com.example.backend.detail;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.function.Function;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.example.backend.search.Hotel;
import com.example.backend.search.HotelDTO;
import com.example.backend.search.HotelRepository;
import com.example.backend.search.Room;
import com.example.backend.search.RoomAvailability;
import com.example.backend.search.RoomDTO;
import com.example.backend.search.ServiceDTO;
import com.example.backend.search.Review;
import com.example.backend.search.ReviewDTO;
import com.example.backend.search.RoomAvailabilityDTO;
import com.example.backend.search.RoomAvailabilityRepository;

@Service
public class HotelDetailService {

        private final String imageRootPath = "D:/hotel_images/";

    private final HotelRepository hotelRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;

    public HotelDetailService(HotelRepository hotelRepository, RoomAvailabilityRepository roomAvailabilityRepository) {
        this.hotelRepository = hotelRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
    }

    public HotelDTO detail(DetailRequest request) {
        // Safe null check for hId
        if (request.getHId() == null) {
            throw new IllegalArgumentException("호텔 ID가 null입니다.");
        }

        // 1. Retrieve hotel basic information
        Hotel hotel = hotelRepository.findById(request.getHId())
                .orElseThrow(() -> new RuntimeException("호텔을 찾을 수 없습니다."));

        // 2. Filter rooms based on the provided date, number of people, and rooms
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("시작일 또는 종료일이 null입니다.");
        }

        List<Room> availableRooms = hotel.getRooms().stream()
                .filter(r -> isRoomAvailable(r, startDate, endDate, request.getNumberOfRooms(),
                        request.getNumberOfPeople()))
                .collect(Collectors.toList());

        // 3. Convert to DTO
        return convertToDTO(hotel, availableRooms, request);
    }

    private boolean isRoomAvailable(Room room, LocalDate start, LocalDate end, int numberOfRooms, int numberOfPeople) {
        // 1. Check if the room count and total capacity meet the request
        if (room.getCount() < numberOfRooms || room.getPeople() * room.getCount() < numberOfPeople) {
            return false;
        }

        // 2. Fetch all availability data within the requested date range
        List<RoomAvailability> availabilities = room.getRoomAvailabilities().stream()
                .filter(a -> !a.getDate().isBefore(start) && !a.getDate().isAfter(end.minusDays(1)))
                .collect(Collectors.toList());

        // 3. Check for insufficient availability, matching the SQL's NOT EXISTS logic.
        boolean hasInsufficientAvailability = availabilities.stream()
                .anyMatch(a -> a.getAvailableCount() < numberOfRooms);

        return !hasInsufficientAvailability;
    }

    private HotelDTO convertToDTO(Hotel hotel, List<Room> availableRooms, DetailRequest request) {
        HotelDTO dto = new HotelDTO();
        dto.setHId(hotel.getHId());
        dto.setHName(hotel.getHName());
        dto.setRegion(hotel.getRegion());
        dto.setType(hotel.getType());
        dto.setAddress(hotel.getAddress());
        dto.setStar(hotel.getStar());
        dto.setActive(hotel.isActive());
        dto.setInfo(hotel.getInfo());
        dto.setLatitude(hotel.getLatitude());
        dto.setLongitude(hotel.getLongitude());

        // Calculate minPrice from available rooms
        Integer minPrice = availableRooms.stream()
                .map(Room::getPrice)
                .min(Integer::compareTo)
                .orElse(0);
        dto.setMinPrice(minPrice);

        // Handle reviews (null-safe)
        List<Review> reviews = Optional.ofNullable(hotel.getReviews()).orElseGet(ArrayList::new);
        if (!reviews.isEmpty()) {
            int sum = reviews.stream().mapToInt(Review::getScore).sum();
            int count = reviews.size();
            dto.setAvgScore(sum / (double) count);
            dto.setReviewCount(count);
        } else {
            dto.setAvgScore(0.0);
            dto.setReviewCount(0);
        }

        Integer imageCount = 0;
        String hotelImageDirectoryPath = imageRootPath + hotel.getType() + "/";
        File dir = new File(hotelImageDirectoryPath);

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                String fileNamePrefix = String.valueOf(hotel.getHId());
                imageCount = (int) Arrays.stream(files)
                    .filter(file -> file.getName().startsWith(fileNamePrefix + ".") || file.getName().startsWith(fileNamePrefix + "_"))
                    .count();
            }
        }
        dto.setImageCount(imageCount);

        // [수정] 오류가 발생한 수동 매핑 로직을 ReviewDTO 생성자를 사용하도록 변경
        dto.setReviews(hotel.getReviews().stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList()));

        // Convert Rooms to Room DTOs
        dto.setRooms(availableRooms.stream().map(room -> {
            RoomDTO roomDTO = new RoomDTO();
            roomDTO.setRId(room.getRId());
            if (room.getHotel() != null) {
                roomDTO.setHId(room.getHotel().getHId());
            }
            roomDTO.setType(room.getType());
            roomDTO.setPrice(room.getPrice());
            roomDTO.setPeople(room.getPeople());
            roomDTO.setCount(room.getCount());
            roomDTO.setInfo(room.getInfo());
            roomDTO.setCheckinTime(room.getCheckinTime());
            roomDTO.setCheckoutTime(room.getCheckoutTime());

            // 선택된 기간의 날짜별 재고 리스트를 가져와 DTO에 설정
            LocalDate startDate = request.getStartDate();
            LocalDate endDate = request.getEndDate();

           // 1. DB에서 해당 기간에 존재하는 재고 기록만 일단 가져옵니다.
            //    그리고 날짜(LocalDate)를 키로 하여 빠르게 찾을 수 있도록 Map으로 변환합니다.
            Map<LocalDate, RoomAvailability> existingAvailabilitiesMap = roomAvailabilityRepository
                    .findByRoom_rIdAndDateBetween(room.getRId(), startDate, endDate.minusDays(1))
                    .stream()
                    .collect(Collectors.toMap(RoomAvailability::getDate, Function.identity()));

            // 2. 요청된 기간의 모든 날짜를 순회하면서 재고 DTO 리스트를 생성합니다.
            List<RoomAvailabilityDTO> finalAvailabilityDTOs = startDate.datesUntil(endDate)
                    .map(date -> {
                        RoomAvailabilityDTO availabilityDTO = new RoomAvailabilityDTO();
                        availabilityDTO.setDate(date);

                        // 3. 해당 날짜의 재고 기록이 DB에 존재하는지 확인합니다.
                        if (existingAvailabilitiesMap.containsKey(date)) {
                            // 존재하면 -> DB에 기록된 availableCount를 사용합니다.
                            availabilityDTO.setAvailableCount(existingAvailabilitiesMap.get(date).getAvailableCount());
                        } else {
                            // 존재하지 않으면 (한 번도 예약된 적 없으면) -> 객실의 총 개수(room.count)를 재고로 설정합니다.
                            availabilityDTO.setAvailableCount(room.getCount());
                        }
                        return availabilityDTO;
                    })
                    .collect(Collectors.toList());


            // 4. 최종적으로 만들어진 재고 DTO 리스트를 RoomDTO에 설정합니다.
            roomDTO.setAvailabilities(finalAvailabilityDTOs);

            return roomDTO;
        }).collect(Collectors.toList()));

        // Convert Hotel Services to Service DTOs
        dto.setServices(hotel.getServices().stream()
                .map(s -> {
                    ServiceDTO serviceDTO = new ServiceDTO();
                    serviceDTO.setServiceName(s.getServiceName());
                    return serviceDTO;
                })
                .collect(Collectors.toList()));

        return dto;
    }
}