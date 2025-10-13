package com.example.backend;

import com.example.backend.search.HotelExcelService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Component
public class ExcelImportRunner implements CommandLineRunner {

    private final HotelExcelService hotelExcelService;
    
    @Autowired
    public ExcelImportRunner(@Lazy HotelExcelService hotelExcelService) {
        this.hotelExcelService = hotelExcelService;
    }

    @Override
    public void run(String... args) {
        // 파일 경로와 타입을 배열로 관리
        String[][] files = {
            {"D:/data/Busan_Guesthouse.xlsx", "Guesthouse"},
            {"D:/data/Busan_Hotel.xlsx", "Hotel"},
            {"D:/data/Busan_Motel.xlsx", "Motel"},

            {"D:/data/Gangneung_Guesthouse.xlsx", "Guesthouse"},
            {"D:/data/Gangneung_Hanok.xlsx", "Hanok"},
            {"D:/data/Gangneung_Hotel.xlsx", "Hotel"},

            {"D:/data/Incheon_Hotel.xlsx", "Hotel"},
            {"D:/data/Incheon_Motel.xlsx", "Motel"},

            {"D:/data/Jeju_Hotel.xlsx", "Hotel"},
            {"D:/data/Jeju_Motel.xlsx", "Motel"},
            {"D:/data/Jeju_Pension.xlsx", "Pension"},

            {"D:/data/Seoul_Guesthouse.xlsx", "Guesthouse"},
            {"D:/data/Seoul_Hanok.xlsx", "Hanok"},
            {"D:/data/Seoul_Hotel.xlsx", "Hotel"},
            {"D:/data/Seoul_Motel.xlsx", "Motel"},

            {"D:/data/Sokcho_Hotel.xlsx", "Hotel"},
            {"D:/data/Sokcho_Motel.xlsx", "Motel"}
        };

        try {
            for (String[] file : files) {
                hotelExcelService.importHotelsFromExcel(file[0], file[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
