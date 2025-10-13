package com.example.backend.search;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 호텔 검색을 위한 비즈니스 로직을 처리하는 서비스 클래스.
 * HotelRepository를 사용하여 조건에 맞는 호텔을 검색합니다.
 */
@Service
public class HotelSearchService {

    private final HotelRepository hotelRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    public HotelSearchService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    /**
     * 클라이언트로부터 받은 검색 조건에 따라 호텔을 검색하고 DTO로 변환합니다.
     *
     * @param request 검색 요청 객체
     * @return 검색 조건에 맞는 호텔 목록 (DTO)
     */
    public List<HotelDTO> search(SearchRequest request) {
        List<Hotel> hotels = hotelRepository.searchHotels(
                request.getRegion(),
                request.getNumberOfPeople(),
                request.getNumberOfRooms(),
                request.getStartDate(),
                request.getEndDate());

        // 엔티티 → DTO 변환
        return hotels.stream()
                .map(hotel -> convertToDTO(hotel, request))
                .collect(Collectors.toList());
    }

    /**
     * Hotel 엔티티를 HotelDTO로 변환 (null-safe)
     */
    private HotelDTO convertToDTO(Hotel hotel, SearchRequest request) {
        HotelDTO dto = new HotelDTO();
        dto.setHId(hotel.getHId());
        dto.setHName(hotel.getHName());
        dto.setType(hotel.getType());
        dto.setRegion(hotel.getRegion());
        dto.setAddress(hotel.getAddress());
        dto.setStar(hotel.getStar());
        dto.setInfo(hotel.getInfo());
        dto.setActive(hotel.isActive());
        dto.setStatus(hotel.getStatus());

        // services 변환 (null-safe)
        List<ServiceDTO> serviceDTOs = Optional.ofNullable(hotel.getServices())
                .orElseGet(ArrayList::new)
                .stream()
                .map(service -> {
                    ServiceDTO sDto = new ServiceDTO();
                    sDto.setServiceName(service.getServiceName());
                    return sDto;
                })
                .collect(Collectors.toList());
        dto.setServices(serviceDTOs);

         // 이미지 변환 로직 추가 (로컬 경로 -> 웹 URL)
        List<HotelImageDTO> imageDTOs = hotel.getImages().stream()
                .map(image -> {
                    HotelImageDTO iDto = new HotelImageDTO();
                    String webUrl = image.getImageUrl()
                                         .replace("E:/hotel_images", baseUrl+"/images");
                    iDto.setImageUrl(webUrl);
                    iDto.setFilename(image.getFilename());
                    return iDto;
                })
                .collect(Collectors.toList());
        dto.setImages(imageDTOs);

        // rooms 처리 (null-safe)
        List<RoomDTO> roomDTOs = Optional.ofNullable(hotel.getRooms())
                .orElseGet(ArrayList::new)
                .stream()
                .map(room -> {
                    RoomDTO rDto = new RoomDTO();
                    rDto.setRId(room.getRId());
                    if (room.getHotel() != null) {
                        rDto.setHId(room.getHotel().getHId());
                    }
                    rDto.setType(room.getType());
                    rDto.setCount(room.getCount());
                    rDto.setPeople(room.getPeople());
                    rDto.setPrice(room.getPrice());
                    rDto.setInfo(room.getInfo());
                    rDto.setCheckinTime(room.getCheckinTime());
                    rDto.setCheckoutTime(room.getCheckoutTime());

                    // availabilities 변환 (null-safe)
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

        // 최저가 계산 (조건 null-safe)
        Integer minPrice = roomDTOs.stream()
                .filter(room -> room.getCount() >= request.getNumberOfRooms()
                        && (room.getPeople() * request.getNumberOfRooms()) >= request.getNumberOfPeople()
                        && (request.getStartDate() != null && request.getEndDate() != null
                                ? room.getAvailabilities().stream()
                                        .noneMatch(ra -> !ra.getDate().isBefore(request.getStartDate()) &&
                                                !ra.getDate().isAfter(request.getEndDate()) &&
                                                ra.getAvailableCount() < request.getNumberOfRooms())
                                : true))
                .map(RoomDTO::getPrice)
                .min(Integer::compareTo)
                .orElse(0);
        dto.setMinPrice(minPrice);

        // 리뷰 처리 (null-safe)
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

        return dto;
    }
}