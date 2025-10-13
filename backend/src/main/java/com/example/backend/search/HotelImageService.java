package com.example.backend.search;

import org.springframework.beans.factory.annotation.Value; // ✅ 추가
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class HotelImageService {

    // ✅ 수정: 설정 파일에서 경로를 주입받음
    @Value("${file.upload-dir}")
    private String uploadDir;

    public void saveHotelImage(Long hotelId, MultipartFile image) throws IOException {
        // ✅ 수정: 주입받은 uploadDir 변수 사용
        File hotelDir = new File(uploadDir + "search/" + hotelId);
        if (!hotelDir.exists()) {
            hotelDir.mkdirs();
        }

        File dest = new File(hotelDir, hotelId + ".jpg");
        image.transferTo(dest);
    }
}