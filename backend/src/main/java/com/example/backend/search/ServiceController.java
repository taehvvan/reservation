package com.example.backend.search;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRepository serviceRepository;

    @GetMapping("/api/services")
    public ResponseEntity<List<ServiceDTO>> getAllServices() {
        List<ServiceEntity> services = serviceRepository.findAll();
        
        // 2. [수정] 조회된 엔티티 목록을 DTO 목록으로 변환
        List<ServiceDTO> serviceDTOs = services.stream()
                .map(ServiceDTO::new) // .map(service -> new ServiceDTO(service)) 와 동일
                .collect(Collectors.toList());

        // 3. 변환된 DTO 목록을 반환
        return ResponseEntity.ok(serviceDTOs);
    }
}
