package com.example.backend.search;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceDTO {

    private Long serviceId;
    private String serviceName;

    // 기본 생성자
    public ServiceDTO() {}

    // Service 엔티티를 ServiceDTO로 변환하는 생성자
    public ServiceDTO(ServiceEntity service) {
        this.serviceId = service.getServiceId();
        this.serviceName = service.getServiceName();
    }
}