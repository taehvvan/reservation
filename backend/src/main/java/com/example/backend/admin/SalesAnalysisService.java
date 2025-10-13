package com.example.backend.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.example.backend.reservation.PaymentRepository;
import com.example.backend.reservation.ReservationRepository;
import com.example.backend.search.HotelRepository;

@Service
public class SalesAnalysisService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final HotelRepository hotelRepository;

    public SalesAnalysisService(ReservationRepository reservationRepository, PaymentRepository paymentRepository, HotelRepository hotelRepository){
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.hotelRepository = hotelRepository;
    }

    public List<TotalSalesDto> getTotalSalesTrend(String periodType){
        LocalDate today = LocalDate.now();
        LocalDate startDate;

        switch (periodType.toLowerCase()) {
            case "week": // 최근 3주 (일별로 데이터를 집계)
                startDate = today.minusWeeks(3);
                return getSalesDailyForPeriod(startDate, today);
                
            case "month": // 최근 3개월 (월별로 데이터를 집계)
                // 현재 월 1일 기준 3개월 전의 1일
                startDate = today.minusMonths(3).with(TemporalAdjusters.firstDayOfMonth());
                return getSalesMonthlyForPeriod(startDate, today);

            case "year": // 최근 3년 (연도별로 데이터를 집계)
                // 현재 연도 1월 1일 기준 3년 전의 1월 1일
                startDate = today.minusYears(3).with(TemporalAdjusters.firstDayOfYear());
                return getSalesYearlyForPeriod(startDate, today);

            default:
                // periodType이 유효하지 않은 경우 기본 월별 전체 데이터를 반환
                return getTotalSalesMonthly(); 
        }
    }

    private List<TotalSalesDto> getSalesDailyForPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        // 🚨 Repository에 '기간 지정 일별 조회' 메서드가 필요합니다.
        List<Object[]> results = reservationRepository.findTotalSalesAndCountDailyForPeriod(start, end);

        return results.stream()
                .map(arr -> {
                    // period: YYYY-MM-DD 형식으로 포맷 (arr[0]: year, arr[1]: month, arr[2]: day)
                    String year = String.valueOf(arr[0]);
                    String month = String.format("%02d", (Integer) arr[1]);
                    String day = String.format("%02d", (Integer) arr[2]);
                    return new TotalSalesDto(
                        year + "-" + month + "-" + day, 
                        (Long) arr[3],     // totalSales
                        (Long) arr[4]      // totalCount
                    );
                })
                .collect(Collectors.toList());
    }


    private List<TotalSalesDto> getSalesMonthlyForPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        // 🚨 Repository에 '기간 지정 월별 조회' 메서드가 필요합니다.
        List<Object[]> results = reservationRepository.findTotalSalesAndCountMonthlyForPeriod(start, end);

        return results.stream()
                .map(arr -> {
                    // period: YYYY-MM 형식으로 포맷 (arr[0]: year, arr[1]: month)
                    String year = String.valueOf(arr[0]);
                    String month = String.format("%02d", (Integer) arr[1]);
                    return new TotalSalesDto(
                        year + "-" + month, 
                        (Long) arr[2],     // totalSales
                        (Long) arr[3]      // totalCount
                    );
                })
                .collect(Collectors.toList());
    }

    private List<TotalSalesDto> getSalesYearlyForPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        // 🚨 Repository에 '기간 지정 연도별 조회' 메서드가 필요합니다.
        List<Object[]> results = reservationRepository.findTotalSalesAndCountYearlyForPeriod(start, end);
        
        return results.stream()
                .map(arr -> new TotalSalesDto(
                        String.valueOf(arr[0]), // period: YYYY
                        (Long) arr[1],          // totalSales
                        (Long) arr[2]           // totalCount
                ))
                .collect(Collectors.toList());
    }


    // 연도별 총 매출 및 건수 
    public List<TotalSalesDto> getTotalSalesYearly() {
        List<Object[]> results = reservationRepository.findTotalSalesAndCountYearly();
        
        return results.stream()
                .map(arr -> new TotalSalesDto(
                        String.valueOf(arr[0]), // period: YYYY
                        (Long) arr[1],          // totalSales
                        (Long) arr[2]           // totalCount
                ))
                .collect(Collectors.toList());
    }

    //월별 총 매출 및 추이
    public List<TotalSalesDto> getTotalSalesMonthly() {
        List<Object[]> results = reservationRepository.findTotalSalesAndCountMonthly();
        
        return results.stream()
                .map(arr -> {
                    // period: YYYY-MM 형식으로 포맷
                    String year = String.valueOf(arr[0]);
                    String month = String.format("%02d", (Integer) arr[1]);
                    return new TotalSalesDto(
                        year + "-" + month, 
                        (Long) arr[2],          // totalSales
                        (Long) arr[3]           // totalCount
                    );
                })
                .collect(Collectors.toList());
    }

    //일별 매출 및 예약 추이
    public List<TotalSalesDto> getTotalSalesDaily() {
        List<Object[]> results = reservationRepository.findTotalSalesAndCountDaily();

        return results.stream()
                .map(arr -> {
                    // period: YYYY-MM-DD 형식으로 포맷
                    String year = String.valueOf(arr[0]);
                    String month = String.format("%02d", (Integer) arr[1]);
                    String day = String.format("%02d", (Integer) arr[2]);
                    return new TotalSalesDto(
                        year + "-" + month + "-" + day, 
                        (Long) arr[3],          // totalSales
                        (Long) arr[4]           // totalCount
                    );
                })
                .collect(Collectors.toList());
    }

    private List<RegionalSalesDto> mapToDto(List<Object[]> results) {
        return results.stream()
                .map(result -> new RegionalSalesDto(
                        (String) result[0], // region
                        (Long) result[1]    // totalSales
                ))
                .collect(Collectors.toList());
    }

    //일 지역별
    public List<RegionalSalesDto> getDailyRegionalSales() {
        LocalDate today = LocalDate.now();

        // 오늘의 시작 시각 (00:00:00)
        LocalDateTime startOfDay = today.atStartOfDay();
        
        // 오늘의 마지막 시각 (23:59:59.999999999)
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Object[]> results = reservationRepository.findSalesByRegionForPeriod(startOfDay, endOfDay);
        return mapToDto(results);
    }

    //월별 판매
    public List<RegionalSalesDto> getMonthlyRegionalSales() {
        LocalDate today = LocalDate.now();

        // 이번 달의 첫 날 (예: 2025-09-01)
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        // 이번 달의 마지막 날 (예: 2025-09-30)
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = lastDayOfMonth.atTime(LocalTime.MAX);

        List<Object[]> results = reservationRepository.findSalesByRegionForPeriod(startOfMonth, endOfMonth);
        return mapToDto(results);
    }

    public List<RegionalSalesDto> getYearlyRegionalSales() {
        LocalDate today = LocalDate.now();

        // 올해의 첫 날 (1월 1일)
        LocalDate firstDayOfYear = today.with(TemporalAdjusters.firstDayOfYear());
        // 올해의 마지막 날 (12월 31일)
        LocalDate lastDayOfYear = today.with(TemporalAdjusters.lastDayOfYear());

        LocalDateTime startOfYear = firstDayOfYear.atStartOfDay();
        LocalDateTime endOfYear = lastDayOfYear.atTime(LocalTime.MAX);

        List<Object[]> results = reservationRepository.findSalesByRegionForPeriod(startOfYear, endOfYear);
        return mapToDto(results);
    }

    public List<RegionalSalesDto> getRegionalSalesForPeriod(LocalDate startDate, LocalDate endDate) {
        // LocalDate를 LocalDateTime으로 변환하면서 시간 범위 추가
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX); 

        List<Object[]> results = reservationRepository.findSalesByRegionForPeriod(start, end);
        return mapToDto(results);
    }

    //payment
    public Long getCurrentMonthTotalPaymentAmount() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        // Payment 테이블에서 당월 금액 합계 조회
        Long totalAmount = paymentRepository.findTotalAmountByYearAndMonth(currentYear, currentMonth);

        // 결과가 null이면 0L로 처리
        return totalAmount != null ? totalAmount : 0L;
    }

    public TotalSalesDto getPaymentSummaryForMonth(int year, int month) {
        Long totalAmount = paymentRepository.findTotalAmountByYearAndMonth(year, month);
        
        // YYYY-MM 형식으로 포맷
        String period = String.format("%d-%02d", year, month);
        
        // 건수는 Payment 테이블에서는 별도로 세지 않으므로 임의로 0L 설정 (필요 시 쿼리 변경)
        return new TotalSalesDto(period, totalAmount != null ? totalAmount : 0L, 0L);
    }

    //오늘 예약 완료 건수
    public Long getTodayCompletedReservationCount() {
        LocalDate today = LocalDate.now();

        // 오늘의 시작 시각 (00:00:00)
        LocalDateTime startOfDay = today.atStartOfDay();
        
        // 오늘의 마지막 시각 (23:59:59.999999999)
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Long count = reservationRepository.countCompletedReservationsBetween(startOfDay, endOfDay);
        
        return count;
    }
    public Long getCurrentMonthCommissionRevenue() {
        // 1. 당월 총 결제 금액을 가져옵니다. (PaymentRepository 사용)
        Long totalPayment = getCurrentMonthTotalPaymentAmount(); // 기존 구현된 메서드 사용

        // 2. 수수료(1%)를 계산합니다. 
        // 정수 Long 타입 계산 시 오차가 발생할 수 있으므로, Double로 계산 후 다시 Long으로 변환합니다.
        if (totalPayment == null || totalPayment == 0) {
            return 0L;
        }

        // 소수점 처리를 위해 BigInteger 또는 BigDecimal을 사용하는 것이 가장 안전하지만, 
        // 간단히 Long으로 처리하는 경우:
        return Math.round(totalPayment * 0.01);
    }

    public Long getNewHotelPendingCount() {
        // Repository '대기' count로 가져옴
        return hotelRepository.countByStatus("대기");
    }

    public Long getTotalPlatformRevenue() {
        Long totalRevenue = paymentRepository.findTotalPlatformRevenue();
        
        // 결과가 NULL일 경우 0L을 반환하여 계산 오류를 방지합니다.
        return totalRevenue != null ? totalRevenue : 0L;
    }

}