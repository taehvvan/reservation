package com.example.backend.admin;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/regional")
public class RegionalSalesController {
    
    private final SalesAnalysisService salesAnalysisService;

    public RegionalSalesController(SalesAnalysisService salesAnalysisService){
        this.salesAnalysisService = salesAnalysisService;
    }

    @GetMapping("/daily")
    public ResponseEntity<List<RegionalSalesDto>> getDailyRegionalSales(){
        List<RegionalSalesDto> data = salesAnalysisService.getDailyRegionalSales();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<RegionalSalesDto>> getMonthlyRegionalSales(){
        List<RegionalSalesDto> data = salesAnalysisService.getMonthlyRegionalSales();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/yearly")
    public ResponseEntity<List<RegionalSalesDto>> getYearlyRegionSales(){
        List<RegionalSalesDto> data = salesAnalysisService.getYearlyRegionalSales();
        return ResponseEntity.ok(data);
    }

    //사용자가 날짜를 선택해서 그 기간의 결과를 보게 할 수 있는 
    @GetMapping
    public ResponseEntity<List<RegionalSalesDto>> getRegionalSalesByPeriod(
        @RequestParam("startDate") LocalDate startDate, @RequestParam("endDate") LocalDate endDate){
            List<RegionalSalesDto> data = salesAnalysisService.getRegionalSalesForPeriod(startDate, endDate);
            return ResponseEntity.ok(data);
    }


}
