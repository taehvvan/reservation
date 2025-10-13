package com.example.backend.wishlist;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.backend.register.UserEntity;
import com.example.backend.register.UserRepository;
import com.example.backend.search.Hotel;
import com.example.backend.search.HotelDTO;
import com.example.backend.search.HotelRepository;
import com.example.backend.search.Review;
import com.example.backend.search.Room;
import com.example.backend.search.RoomAvailability;
import com.example.backend.search.RoomAvailabilityDTO;
import com.example.backend.search.RoomDTO;
import com.example.backend.search.ServiceDTO;

import jakarta.transaction.Transactional;

@Service
public class WishlistService {

    private final DibsRepository dibsRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;

    public WishlistService(DibsRepository dibsRepository, HotelRepository hotelRepository,
            UserRepository userRepository) {
        this.dibsRepository = dibsRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
    }

    /**
     * 사용자 이메일 기준으로 찜한 호텔 리스트 조회 및 DTO 변환
     * 최저가, 리뷰 평균 점수, 리뷰 카운트 포함
     */
    public List<HotelDTO> getWishlistByUserId(String email) {
        List<DibsEntity> dibsList = dibsRepository.findByUser_Email(email);

        return Optional.ofNullable(dibsList)
                .orElseGet(List::of)
                .stream()
                .map(dibs -> convertToDTO(dibs.getHotel()))
                .collect(Collectors.toList());
    }

    /**
     * Hotel → HotelDTO 변환 (null-safe, 리뷰 및 최저가 포함)
     */
    private HotelDTO convertToDTO(Hotel hotel) {
        HotelDTO dto = new HotelDTO();
        dto.setHId(hotel.getHId());
        dto.setHName(hotel.getHName());
        dto.setType(hotel.getType());
        dto.setRegion(hotel.getRegion());
        dto.setAddress(hotel.getAddress());
        dto.setStar(hotel.getStar());
        dto.setActive(hotel.isActive());
        dto.setInfo(hotel.getInfo());

        // 서비스 변환
        List<ServiceDTO> serviceDTOs = Optional.ofNullable(hotel.getServices())
                .orElseGet(ArrayList::new)
                .stream()
                .map(s -> {
                    ServiceDTO sDto = new ServiceDTO();
                    sDto.setServiceName(s.getServiceName());
                    return sDto;
                })
                .collect(Collectors.toList());
        dto.setServices(serviceDTOs);

        // 룸 변환
        List<RoomDTO> roomDTOs = Optional.ofNullable(hotel.getRooms())
                .orElseGet(ArrayList::new)
                .stream()
                .map(room -> {
                    RoomDTO rDto = new RoomDTO();
                    rDto.setRId(room.getRId());
                    rDto.setType(room.getType());
                    rDto.setCount(room.getCount());
                    rDto.setPeople(room.getPeople());
                    rDto.setPrice(room.getPrice());
                    rDto.setInfo(room.getInfo());
                    rDto.setCheckinTime(room.getCheckinTime());
                    rDto.setCheckoutTime(room.getCheckoutTime());

                    // availabilities 변환
                    List<RoomAvailabilityDTO> raDTOs = Optional.ofNullable(room.getRoomAvailabilities())
                            .orElseGet(ArrayList::new)
                            .stream()
                            .map(ra -> {
                                RoomAvailabilityDTO raDto = new RoomAvailabilityDTO();
                                raDto.setDate(ra.getDate());
                                raDto.setAvailableCount(ra.getAvailableCount());
                                return raDto;
                            })
                            .collect(Collectors.toList());
                    rDto.setAvailabilities(raDTOs);
                    return rDto;
                })
                .collect(Collectors.toList());
        dto.setRooms(roomDTOs);

        // 최저가 계산
        Integer minPrice = roomDTOs.stream()
                .map(RoomDTO::getPrice)
                .min(Integer::compareTo)
                .orElse(0);
        dto.setMinPrice(minPrice);

        // 리뷰 평균 & 카운트
        List<Review> reviews = Optional.ofNullable(hotel.getReviews())
                .orElseGet(ArrayList::new);
        if (!reviews.isEmpty()) {
            int sum = reviews.stream().mapToInt(Review::getScore).sum();
            int count = reviews.size();
            dto.setAvgScore(sum / (double) count);
            dto.setReviewCount(count);
        } else {
            dto.setAvgScore(0.0);
            dto.setReviewCount(0);
        }

        return dto;
    }

    @Transactional
    public void addByEmailAndHotelId(String email, Long hid) {
        Hotel hotel = hotelRepository.findById(hid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 호텔 ID: " + hid));

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 이메일: " + email));

        DibsEntity dibs = new DibsEntity();
        dibs.setId(new DibsId());
        dibs.setHotel(hotel);
        dibs.setUser(user);
        dibsRepository.save(dibs);
    }

    @Transactional
    public void removeByEmailAndHotelId(String email, Long hid) {
        dibsRepository.deleteByUserEmailAndHotelHId(email, hid);
    }

    @Transactional
    public List<HotelDTO> getWishlistByUserIdWithFilter(
            String email,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer numberOfRooms,
            Integer numberOfPeople) {

        List<DibsEntity> dibsList = dibsRepository.findByUser_Email(email);

        System.out.println("rooms: " + numberOfRooms + ", persons: " + numberOfPeople);

        return dibsList.stream()
                .map(dibs -> {
                    Hotel hotel = dibs.getHotel();

                    // 필터링 조건을 만족하는 객실만 찾기
                    List<RoomDTO> roomDTOs = Optional.ofNullable(hotel.getRooms())
                            .orElseGet(ArrayList::new)
                            .stream()
                            .filter(room -> {
                                boolean meetsPeople = (numberOfPeople == null
                                        || (room.getPeople() * numberOfRooms) >= numberOfPeople);

                                boolean meetsRooms = (numberOfRooms == null || (room.getCount() >= numberOfRooms));
                                boolean meetsAvailability = (checkIn == null || checkOut == null)
                                        || isRoomAvailable(room, checkIn, checkOut, numberOfRooms);

                                return meetsPeople && meetsRooms && meetsAvailability;
                            })
                            .map(room -> {
                                RoomDTO rDto = new RoomDTO();
                                rDto.setRId(room.getRId());
                                rDto.setType(room.getType());
                                rDto.setCount(room.getCount());
                                rDto.setPeople(room.getPeople());
                                rDto.setPrice(room.getPrice());
                                rDto.setInfo(room.getInfo());
                                rDto.setCheckinTime(room.getCheckinTime());
                                rDto.setCheckoutTime(room.getCheckoutTime());
                                rDto.setAvailabilities(
                                        room.getRoomAvailabilities().stream()
                                                .filter(ra -> {
                                                    if (checkIn == null || checkOut == null)
                                                        return true; // null이면 모두 포함
                                                    LocalDate date = ra.getDate();
                                                    return !(date.isBefore(checkIn) || date.isAfter(checkOut));
                                                })
                                                .map(ra -> new RoomAvailabilityDTO(ra.getDate(),
                                                        ra.getAvailableCount()))
                                                .collect(Collectors.toList()));
                                return rDto;
                            })
                            .collect(Collectors.toList());

                    // 필터링된 객실 중에서 최저가 계산
                    Integer minPrice = roomDTOs.stream()
                            .map(RoomDTO::getPrice)
                            .min(Integer::compareTo)
                            .orElse(0);

                    // HotelDTO에 데이터 설정
                    HotelDTO dto = new HotelDTO();
                    dto.setHId(hotel.getHId());
                    dto.setHName(hotel.getHName());
                    dto.setRegion(hotel.getRegion());
                    dto.setType(hotel.getType());
                    dto.setMinPrice(minPrice); // 최저가 설정
                    dto.setRooms(roomDTOs); // 필터링된 객실 정보 설정
                    dto.setAvgScore(getAverageScore(hotel));
                    dto.setReviewCount(getReviewCount(hotel));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 날짜 범위 동안 객실 이용 가능 여부 확인
     * 각 날짜별로 필요한 객실 수(numberOfRooms)가 충분한지 확인
     */
    private boolean isRoomAvailable(Room room, LocalDate checkIn, LocalDate checkOut, Integer numberOfRooms) {
        if (checkIn == null || checkOut == null || numberOfRooms == null) {
            return true; // 날짜나 방 수가 없으면 항상 true
        }

        LocalDate currentDate = checkIn;
        while (!currentDate.isAfter(checkOut.minusDays(1))) {
            LocalDate finalCurrentDate = currentDate;
            // 1. 해당 날짜의 RoomAvailability 데이터가 있는지 찾습니다.
            Optional<RoomAvailability> availability = room.getRoomAvailabilities().stream()
                    .filter(ra -> ra.getDate().equals(finalCurrentDate))
                    .findFirst();

            // 2. 데이터가 존재하는 경우에만 예약 가능한 방의 갯수를 확인합니다.
            if (availability.isPresent()) {
                if (availability.get().getAvailableCount() < numberOfRooms) {
                    // 특정 날짜에 요청한 방 수보다 가용 방 수가 적으면 false
                    return false;
                }
            }
            // 3. 데이터가 존재하지 않으면, 모든 방이 예약 가능하다고 간주하므로 이 조건은 통과

            currentDate = currentDate.plusDays(1);
        }

        return true; // 모든 날짜의 조건을 통과하면 true
    }

    // 리뷰 평균/개수를 위한 헬퍼 메서드 추가
    private double getAverageScore(Hotel hotel) {
        List<Review> reviews = Optional.ofNullable(hotel.getReviews()).orElseGet(ArrayList::new);
        if (reviews.isEmpty())
            return 0.0;
        return reviews.stream().mapToInt(Review::getScore).average().orElse(0.0);
    }

    private int getReviewCount(Hotel hotel) {
        return Optional.ofNullable(hotel.getReviews()).orElseGet(ArrayList::new).size();
    }
}