package com.example.backend.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TotalSalesDto {
    private String period;  // YYYY-MM 형식
    private Long totalSales;
    private Long totalCount;
}
