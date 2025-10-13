package com.example.backend.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/trend")
public class TotalSalesController {

    private final SalesAnalysisService salesAnalysisService;

    public TotalSalesController(SalesAnalysisService salesAnalysisService){
        this.salesAnalysisService = salesAnalysisService;
    }

    //연도별 data 가져옴
    @GetMapping("/yearly")
    public ResponseEntity<List<TotalSalesDto>> getYearlySalesTrend(){
        List<TotalSalesDto> data = salesAnalysisService.getTotalSalesYearly();
        return ResponseEntity.ok(data);
    }

    //월별 data 가져옴
    @GetMapping("/monthly")
    public ResponseEntity<List<TotalSalesDto>> getMonthlySalesTrend(){
        List<TotalSalesDto> data = salesAnalysisService.getTotalSalesMonthly();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/daily")
    public ResponseEntity<List<TotalSalesDto>> getDailySalesTrend(){
        List<TotalSalesDto> data = salesAnalysisService.getTotalSalesDaily();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/count/today")
    public ResponseEntity<Long> getTodayCompletedReservationCount(){
        Long count = salesAnalysisService.getTodayCompletedReservationCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/commission/current-month")
    public ResponseEntity<Long> getCurrentMonthCommissionRevenue() {
        Long revenue = salesAnalysisService.getCurrentMonthCommissionRevenue();
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/hotel/pending-count")
    public ResponseEntity<Long> getNewHotelPendingCount() {
        Long count = salesAnalysisService.getNewHotelPendingCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/total-revenue")
    public ResponseEntity<Long> getTotalPlatformRevenue() {
        Long totalRevenue = salesAnalysisService.getTotalPlatformRevenue();
        return ResponseEntity.ok(totalRevenue);
    }

    @GetMapping("/trend") 
    public ResponseEntity<List<TotalSalesDto>> getSalesTrend(
            @RequestParam("period") String period) {
        
        List<TotalSalesDto> data = salesAnalysisService.getTotalSalesTrend(period);
        return ResponseEntity.ok(data);
    }


}