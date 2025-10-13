package com.example.backend.manager;

import com.example.backend.register.UserEntity;
import com.example.backend.search.Hotel;
import com.example.backend.search.HotelImage;
import com.example.backend.search.HotelImageRepository;
import com.example.backend.search.HotelRepository;
import com.example.backend.search.Room;
import com.example.backend.search.RoomRepository;
import com.example.backend.search.ServiceEntity;
import com.example.backend.search.ServiceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerHotelService {

    private final HotelRepository hotelRepository;
    private final FileStorageService fileStorageService;
    private final HotelImageRepository hotelImageRepository;
    private final RoomRepository roomRepository;
    private final ServiceRepository serviceRepository;

    @Value("${app.base-url}")
    private String baseUrl;     



    public List<ManagerHotelDTO> getHotelsByManager(Integer managerId) {
        List<Hotel> hotels = hotelRepository.findByUser_Id(managerId);
        return hotels.stream()
                // ✅ 람다식으로 변경하고 baseUrl을 명시적으로 전달합니다.
                .map(hotel -> ManagerHotelDTO.fromEntity(hotel, baseUrl)) 
                .collect(Collectors.toList());
    }

    @Transactional
    public ManagerHotelDTO createHotel(HotelSaveRequestDto hotelDto, List<MultipartFile> images, UserEntity manager) {
        // 1. Hotel 엔티티 생성 및 기본 정보 설정
        Hotel hotel = new Hotel();
        hotel.setHName(hotelDto.getName());
        hotel.setAddress(hotelDto.getLocation());
        hotel.setType(hotelDto.getType());
        hotel.setStar(hotelDto.getStars());
        hotel.setUser(manager);
        hotel.setLatitude(hotelDto.getLatitude());
        hotel.setLongitude(hotelDto.getLongitude());
        if (hotelDto.getLocation() != null && !hotelDto.getLocation().isEmpty()) {
            hotel.setRegion(hotelDto.getLocation().split(" ")[0]);
        }

        // 서비스(부대시설) 정보 설정
        if (hotelDto.getServiceIds() != null && !hotelDto.getServiceIds().isEmpty()) {
            List<ServiceEntity> services = serviceRepository.findAllById(hotelDto.getServiceIds());
            hotel.setServices(services);
        }

        // 2. Hotel을 먼저 저장하여 ID를 생성
        Hotel savedHotel = hotelRepository.save(hotel);

        // 3. Room 엔티티 생성, 저장, 이미지 처리
        if (hotelDto.getRooms() != null && !hotelDto.getRooms().isEmpty()) {
            for (RoomSaveRequestDTO roomDto : hotelDto.getRooms()) {
                Room room = new Room();
                room.setHotel(savedHotel); // ID가 있는 hotel 엔티티와 연결
                room.setType(roomDto.getType());
                room.setPrice(roomDto.getPrice());
                room.setCount(roomDto.getCount());
                room.setPeople(roomDto.getPeople());
                room.setCheckinTime(roomDto.getCheckinTime());
                room.setCheckoutTime(roomDto.getCheckoutTime());
                
                // Room을 저장하여 Room ID를 생성
                Room savedRoom = roomRepository.save(room);

                // Base64 이미지 데이터가 있으면 파일로 저장하고 HotelImage 엔티티 생성
                String imageStr = roomDto.getImage();
                if (imageStr != null && imageStr.startsWith("data:image")) {
                    // [핵심 수정] 파일명을 roomId로 지정하여 저장
                    String imageUrl = fileStorageService.saveBase64Image(imageStr, savedHotel.getType() + "/rooms", String.valueOf(savedRoom.getRId()));
                    
                    if (imageUrl != null) {
                        HotelImage roomImage = new HotelImage();
                        roomImage.setImageUrl(imageUrl);
                        roomImage.setImageType("room");
                        roomImage.setRoom(savedRoom); // ID가 있는 room 엔티티와 연결
                        hotelImageRepository.save(roomImage); // HotelImage 저장
                    }
                }
            }
        }

        // 4. 호텔 대표/서브 이미지 처리
        if (images != null && !images.isEmpty()) {
            List<HotelImage> hotelImages = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                MultipartFile imageFile = images.get(i);
                if (imageFile.isEmpty()) continue;

                FileStorageResult result = fileStorageService.saveHotelImage(savedHotel.getHId(), savedHotel.getType(), imageFile, i);
                String imageUrl = "/images/" + savedHotel.getType() + "/" + result.getFileName();

                HotelImage hotelImage = new HotelImage();
                hotelImage.setHotel(savedHotel);
                hotelImage.setFilename(result.getFileName());
                hotelImage.setImageType(i == 0 ? "main" : "sub");
                hotelImage.setImageUrl(imageUrl);
                hotelImages.add(hotelImage);
            }
            hotelImageRepository.saveAll(hotelImages);
        }
        
        // 최종적으로 저장된 호텔 정보를 다시 조회하여 DTO로 변환 후 반환
        return ManagerHotelDTO.fromEntity(hotelRepository.findById(savedHotel.getHId()).get(), baseUrl); 
    }

    @Transactional
    public ManagerHotelDTO updateHotel(Long hotelId, HotelUpdateRequestDto dto, UserEntity manager) {
        
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 호텔을 찾을 수 없습니다."));

        if (!hotel.getUser().getId().equals(manager.getId())) {
            throw new SecurityException("이 호텔을 수정할 권한이 없습니다.");
        }

        // 호텔 기본 정보 업데이트
        hotel.setHName(dto.getName());
        hotel.setAddress(dto.getLocation());
        hotel.setStar(dto.getStars());
        hotel.setLatitude(dto.getLatitude());
        hotel.setLongitude(dto.getLongitude());

        if (dto.getServiceIds() != null) {
            List<ServiceEntity> services = serviceRepository.findAllById(dto.getServiceIds());
            hotel.setServices(services);
        }

        // 객실 정보 업데이트
        if (dto.getRooms() != null) {
            // 기존 객실 이미지들을 먼저 삭제 (선택적)
            hotel.getRooms().forEach(room -> hotelImageRepository.deleteAll(room.getImages()));
            // 기존 객실 목록 비우기
            roomRepository.deleteAll(hotel.getRooms());
            hotel.getRooms().clear();
            
            for (RoomUpdateRequestDto roomDto : dto.getRooms()) {
                Room room = new Room(); 
                room.setType(roomDto.getType());
                room.setPrice(roomDto.getPrice());
                room.setCount(roomDto.getCount());
                room.setPeople(roomDto.getPeople());
                room.setCheckinTime(roomDto.getCheckinTime());
                room.setCheckoutTime(roomDto.getCheckoutTime());
                room.setHotel(hotel);
                
                // Room을 먼저 저장하여 ID 확보
                Room savedRoom = roomRepository.save(room);

                // 이미지 처리
                String imageStr = roomDto.getImage();
                if (imageStr != null && !imageStr.isBlank()) {
                    String imageUrl = imageStr;
                    //String baseUrl = "http://localhost:8888";
                    if (imageUrl.startsWith(baseUrl)) {
                        imageUrl = imageUrl.substring(baseUrl.length());
                    }

                    if (imageStr.startsWith("data:image")) {
                        // [핵심 수정] 파일명을 roomId로 지정
                        imageUrl = fileStorageService.saveBase64Image(imageStr, hotel.getType() + "/rooms", String.valueOf(savedRoom.getRId()));
                    }
                    
                    HotelImage hotelImage = new HotelImage();
                    hotelImage.setImageUrl(imageUrl);
                    hotelImage.setRoom(savedRoom);
                    hotelImageRepository.save(hotelImage);
                }
                hotel.getRooms().add(savedRoom);
            }
        }

        Hotel savedHotel = hotelRepository.save(hotel);
        return ManagerHotelDTO.fromEntity(savedHotel, baseUrl);
    }

    @Transactional(readOnly = true)
    public ManagerHotelDTO getHotelDetails(Long hotelId, UserEntity manager) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("해당 숙소를 찾을 수 없습니다. ID: " + hotelId));

        if (!hotel.getUser().getId().equals(manager.getId())) {
            throw new SecurityException("이 숙소 정보를 조회할 권한이 없습니다.");
        }
        
        return ManagerHotelDTO.fromEntity(hotel, baseUrl); 
    }

    @Transactional
    public void addImagesToHotel(Long hotelId, List<MultipartFile> images, UserEntity manager) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("해당 숙소를 찾을 수 없습니다. ID: " + hotelId));
        
        if (!hotel.getUser().getId().equals(manager.getId())) {
            throw new SecurityException("이 호텔 이미지를 수정할 권한이 없습니다.");
        }
        
        // 호텔과 직접 연관된 이미지만 삭제 (객실 이미지 제외)
        List<HotelImage> imagesToDelete = hotel.getImages().stream()
                                            .filter(img -> img.getRoom() == null)
                                            .collect(Collectors.toList());
        hotelImageRepository.deleteAll(imagesToDelete);

        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile imageFile = images.get(i);
                if(imageFile.isEmpty()) continue;

                FileStorageResult result = fileStorageService.saveHotelImage(hotel.getHId(), hotel.getType(), imageFile, i);
                String imageUrl = "/images/" + hotel.getType() + "/" + result.getFileName();
                
                HotelImage hotelImage = new HotelImage();
                hotelImage.setHotel(hotel);
                hotelImage.setFilename(result.getFileName());
                hotelImage.setImageType(i == 0 ? "main" : "sub");
                hotelImage.setImageUrl(imageUrl);
                
                hotelImageRepository.save(hotelImage);
            }
        }
    }

    @Transactional
    public void deleteHotel(Long hotelId, UserEntity manager) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("해당 숙소를 찾을 수 없습니다. ID: " + hotelId));

        if (!hotel.getUser().getId().equals(manager.getId())) {
            throw new SecurityException("이 숙소를 삭제할 권한이 없습니다.");
        }
        
        hotelRepository.delete(hotel);
    }
}