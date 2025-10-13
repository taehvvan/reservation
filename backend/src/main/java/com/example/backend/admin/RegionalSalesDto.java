package com.example.backend.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegionalSalesDto {
    private String region;
    private Long totalSales;

    public RegionalSalesDto(String region, Long totalSales) {
        this.region = region;
        this.totalSales = totalSales;
    }
}
