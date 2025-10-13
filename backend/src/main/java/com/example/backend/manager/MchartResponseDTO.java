package com.example.backend.manager;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MchartResponseDTO {
    private Integer userId;
    private String userName;

    private SummaryDto summary; // 전체 요약
    private List<HotelDto> hotels; // 호텔별 데이터
    private DonutDto donut; // 도넛 차트 (호텔 기준 매출 비율)

    // =====================
    // 전체 요약
    // =====================
    @Getter
    @Setter
    @Builder
    public static class SummaryDto {
        private long totalSales; // 총 매출
        private long dailySales; // 오늘 매출
        private long yesterdaySales; // 어제 매출
        private double dailyOccupancy; // 오늘 점유율
        private double yesterdayOccupancy; // 어제 점유율
    }

    // =====================
    // 호텔 정보
    // =====================
    @Getter
    @Setter
    @Builder
    public static class HotelDto {
        private Long hotelId;
        private String hotelName;
        private String address;

        private SalesDto sales; // 매출 요약
        private ChartDataDto chart; // 차트 데이터
        private List<RoomSalesDto> rooms; // 방별 매출 비율
    }

    // =====================
    // 매출 요약
    // =====================
    @Getter
    @Setter
    @Builder
    public static class SalesDto {
        private long total; // 기간 내 총 매출
        private long daily; // 오늘 매출
        private double occupancy; // 오늘 점유율
    }

    // =====================
    // 차트 데이터 (기간별)
    // =====================
    @Getter
    @Setter
    @Builder
    public static class ChartDataDto {
        private ChartPeriodDto daily;
        private ChartPeriodDto weekly;
        private ChartPeriodDto monthly;
        private ChartPeriodDto yearly;
    }

    // =====================
    // 기간별 데이터
    // =====================
    @Getter
    @Setter
    @Builder
    public static class ChartPeriodDto {
        private List<String> labels; // ["09-20", "09-21"]
        private List<Long> sales; // 매출 리스트
        private List<Double> occupancy; // 점유율 리스트
    }

    // =====================
    // 도넛 차트 (호텔별 매출)
    // =====================
    @Getter
    @Setter
    @Builder
    public static class DonutDto {
        private List<String> labels; // 호텔명 리스트
        private List<Long> sales; // 매출 합계 리스트
    }

    // =====================
    // 방별 매출 (호텔 선택 시)
    // =====================
    @Getter
    @Setter
    @Builder
    public static class RoomSalesDto {
        private String name; // 방 이름
        private long sales; // 해당 방 매출
    }
}