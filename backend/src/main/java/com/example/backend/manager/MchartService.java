package com.example.backend.manager;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.*;

import org.springframework.stereotype.Service;

import com.example.backend.reservation.Reservation;
import com.example.backend.reservation.ReservationRepository;
import com.example.backend.search.Hotel;
import com.example.backend.search.HotelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MchartService {

        private final ReservationRepository reservationRepository;
        private final HotelRepository hotelRepository;

        public MchartResponseDTO getDashboard(Integer userId) {
                List<Hotel> hotels = hotelRepository.findByUser_Id(userId);

                // 전체 요약
                long totalSales = 0L;
                long dailySales = 0L;
                long yesterdaySales = 0L;
                double dailyOccupancy = 0.0;
                double yesterdayOccupancy = 0.0;

                List<MchartResponseDTO.HotelDto> hotelDtos = new ArrayList<>();

                LocalDate today = LocalDate.now();
                LocalDate yesterday = today.minusDays(1);

                for (Hotel hotel : hotels) {
                        List<Reservation> reservations = reservationRepository.findByRoomHotel(hotel);

                        long hotelSales = reservations.stream()
                                        .mapToLong(Reservation::getPrice)
                                        .sum();

                        long hotelDailySales = reservations.stream()
                                        .filter(r -> r.getCheckin().isEqual(today))
                                        .mapToLong(Reservation::getPrice)
                                        .sum();

                        long hotelYesterdaySales = reservations.stream()
                                        .filter(r -> r.getCheckin().isEqual(yesterday))
                                        .mapToLong(Reservation::getPrice)
                                        .sum();

                        double occupancy = calcOccupancy(reservations, today);
                        double yOccupancy = calcOccupancy(reservations, yesterday);

                        totalSales += hotelSales;
                        dailySales += hotelDailySales;
                        yesterdaySales += hotelYesterdaySales;
                        dailyOccupancy += occupancy;
                        yesterdayOccupancy += yOccupancy;

                        // ✅ 실제 차트 데이터 집계
                        MchartResponseDTO.ChartDataDto chart = buildChartData(reservations);

                        // 방별 매출 비율
                        List<MchartResponseDTO.RoomSalesDto> roomSales = hotel.getRooms().stream()
                                        .map(room -> MchartResponseDTO.RoomSalesDto.builder()
                                                        .name(room.getType())
                                                        .sales(reservations.stream()
                                                                        .filter(r -> r.getRoom().equals(room))
                                                                        .mapToLong(Reservation::getPrice)
                                                                        .sum())
                                                        .build())
                                        .toList();

                        hotelDtos.add(MchartResponseDTO.HotelDto.builder()
                                        .hotelId(hotel.getHId())
                                        .hotelName(hotel.getHName())
                                        .address(hotel.getAddress())
                                        .sales(MchartResponseDTO.SalesDto.builder()
                                                        .total(hotelSales)
                                                        .daily(hotelDailySales)
                                                        .occupancy(occupancy)
                                                        .build())
                                        .chart(chart)
                                        .rooms(roomSales)
                                        .build());
                }

                return MchartResponseDTO.builder()
                                .summary(MchartResponseDTO.SummaryDto.builder()
                                                .totalSales(totalSales)
                                                .dailySales(dailySales)
                                                .yesterdaySales(yesterdaySales)
                                                .dailyOccupancy(dailyOccupancy / (hotels.isEmpty() ? 1 : hotels.size()))
                                                .yesterdayOccupancy(yesterdayOccupancy
                                                                / (hotels.isEmpty() ? 1 : hotels.size()))
                                                .build())
                                .hotels(hotelDtos)
                                .donut(buildDonut(hotelDtos))
                                .build();
        }

        // ✅ 차트 데이터 생성
        private MchartResponseDTO.ChartDataDto buildChartData(List<Reservation> reservations) {
                return MchartResponseDTO.ChartDataDto.builder()
                                .daily(buildChartPeriodData(reservations, "daily"))
                                .weekly(buildChartPeriodData(reservations, "weekly"))
                                .monthly(buildChartPeriodData(reservations, "monthly"))
                                .yearly(buildChartPeriodData(reservations, "yearly"))
                                .build();
        }

        // ✅ 일간 / 주간 / 월간 / 연간 데이터 생성
        private MchartResponseDTO.ChartPeriodDto buildChartPeriodData(List<Reservation> reservations, String type) {
                Map<String, List<Reservation>> grouped = new TreeMap<>();

                switch (type) {
                        case "daily" -> {
                                LocalDate today = LocalDate.now();
                                for (int i = 6; i >= 0; i--) {
                                        LocalDate date = today.minusDays(i);
                                        grouped.put(date.toString(), new ArrayList<>());
                                }
                                reservations.forEach(r -> {
                                        String key = r.getCreatedAt().toLocalDate().toString(); // ✅ 결제일 기준
                                        if (grouped.containsKey(key)) {
                                                grouped.get(key).add(r);
                                        }
                                });
                        }
                        case "weekly" -> {
                                LocalDate today = LocalDate.now();
                                WeekFields wf = WeekFields.of(Locale.getDefault());
                                for (int i = 7; i >= 0; i--) {
                                        LocalDate date = today.minusWeeks(i);
                                        int year = date.getYear();
                                        int week = date.get(wf.weekOfWeekBasedYear());
                                        String key = year + "-W" + week;
                                        grouped.put(key, new ArrayList<>());
                                }
                                reservations.forEach(r -> {
                                        int year = r.getCreatedAt().getYear(); // ✅ 결제일 기준
                                        int week = r.getCreatedAt().get(wf.weekOfWeekBasedYear());
                                        String key = year + "-W" + week;
                                        if (grouped.containsKey(key)) {
                                                grouped.get(key).add(r);
                                        }
                                });
                        }
                        case "monthly" -> {
                                YearMonth now = YearMonth.now();
                                for (int i = 5; i >= 0; i--) {
                                        YearMonth ym = now.minusMonths(i);
                                        String key = ym.toString();
                                        grouped.put(key, new ArrayList<>());
                                }
                                reservations.forEach(r -> {
                                        String key = YearMonth.from(r.getCreatedAt().toLocalDate()).toString(); // ✅ 결제일
                                                                                                                // 기준
                                        if (grouped.containsKey(key)) {
                                                grouped.get(key).add(r);
                                        }
                                });
                        }
                        case "yearly" -> {
                                int year = LocalDate.now().getYear();
                                for (int i = 4; i >= 0; i--) {
                                        int y = year - i;
                                        grouped.put(String.valueOf(y), new ArrayList<>());
                                }
                                reservations.forEach(r -> {
                                        String key = String.valueOf(r.getCreatedAt().getYear()); // ✅ 결제일 기준
                                        if (grouped.containsKey(key)) {
                                                grouped.get(key).add(r);
                                        }
                                });
                        }
                }

                List<String> labels = new ArrayList<>(grouped.keySet());
                List<Long> sales = new ArrayList<>();
                List<Double> occupancy = new ArrayList<>();

                for (String key : labels) {
                        List<Reservation> rs = grouped.get(key);

                        // ✅ 매출 = 결제일 기준
                        long totalSales = rs.stream().mapToLong(Reservation::getPrice).sum();

                        // ✅ 점유율 = checkin~checkout 기간 기준
                        double occ;
                        if (type.equals("daily")) {
                                LocalDate targetDate = LocalDate.parse(key);
                                occ = calcOccupancy(reservations, targetDate); // 전체 예약 중 targetDate에 걸친 점유율
                        } else {
                                // 주/월/년은 그냥 평균치 (필요하면 세분화 가능)
                                occ = calcOccupancy(rs, null);
                        }

                        sales.add(totalSales);
                        occupancy.add(occ);
                }

                return MchartResponseDTO.ChartPeriodDto.builder()
                                .labels(labels)
                                .sales(sales)
                                .occupancy(occupancy)
                                .build();
        }

        // ✅ 도넛 차트
        private MchartResponseDTO.DonutDto buildDonut(List<MchartResponseDTO.HotelDto> hotels) {
                List<String> labels = hotels.stream().map(MchartResponseDTO.HotelDto::getHotelName).toList();
                List<Long> sales = hotels.stream().map(h -> h.getSales().getTotal()).toList();
                return MchartResponseDTO.DonutDto.builder()
                                .labels(labels)
                                .sales(sales)
                                .build();
        }

        // ✅ 정밀 계산: 날짜 단위로 풀어서 평균 점유율 산출
        private double calcOccupancy(List<Reservation> reservations, LocalDate targetDate) {
                if (reservations.isEmpty())
                        return 0.0;

                // 대상 기간 결정
                LocalDate start;
                LocalDate end;
                if (targetDate != null) {
                        // 하루만 계산
                        start = end = targetDate;
                } else {
                        // 그룹 전체 기간 (가장 빠른 checkin ~ 가장 늦은 checkout)
                        start = reservations.stream()
                                        .map(Reservation::getCheckin)
                                        .min(LocalDate::compareTo)
                                        .orElse(LocalDate.now());
                        end = reservations.stream()
                                        .map(Reservation::getCheckout)
                                        .max(LocalDate::compareTo)
                                        .orElse(LocalDate.now());
                }

                long totalDays = 0;
                double sumRates = 0.0;

                for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                        int occupied = 0;
                        int total = 0;

                        for (Reservation r : reservations) {
                                if (!d.isBefore(r.getCheckin()) && !d.isAfter(r.getCheckout().minusDays(1))) {
                                        occupied += r.getRoomCount();
                                }
                                if (r.getRoom() != null) {
                                        total += r.getRoom().getCount();
                                }
                        }

                        if (total > 0) {
                                double rate = (occupied * 100.0) / total;
                                sumRates += rate;
                                totalDays++;
                        }
                }

                return totalDays > 0 ? sumRates / totalDays : 0.0;
        }
}