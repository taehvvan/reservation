package com.example.backend.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    // application.properties 에 설정된 파일 업로드 기본 경로
    @Value("${file.upload-dir}")
    private String baseDir;

    public FileStorageResult saveHotelImage(Long hotelId, String hotelType, MultipartFile file, int imageIndex) {
        try {
            String hotelDir = baseDir + "/" + hotelType + "/";
            File dir = new File(hotelDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // --- 파일명 생성 로직 ---
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = "";
            if (originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String newFileName;
            if (imageIndex == 0) {
                // 첫 번째 이미지(메인 이미지)는 h_id로만 저장
                newFileName = hotelId + extension;
            } else {
                // 두 번째 이미지부터는 h_id_[순번]으로 저장
                newFileName = hotelId + "_" + imageIndex + extension;
            }
            // ---------------------

            Path filePath = Paths.get(hotelDir + newFileName);
            file.transferTo(filePath);

            return new FileStorageResult(hotelDir, newFileName);
        } catch (IOException e) {
            throw new RuntimeException("호텔 이미지 저장 실패", e);
        }
    }

    public String saveBase64Image(String base64Image, String subDir, String fileName) {
        if (base64Image == null || base64Image.isEmpty()) {
            return null;
        }

        // "data:image/jpeg;base64," 부분을 분리하여 순수 Base64 데이터만 추출
        String[] parts = base64Image.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("잘못된 Base64 문자열 형식입니다.");
        }

        // "data:image/jpeg;base64" 에서 확장자(jpeg) 추출
        String metaPart = parts[0];
        String dataPart = parts[1];
        String extension;
        if (metaPart.contains("image/jpeg")) {
            extension = ".jpg";
        } else if (metaPart.contains("image/png")) {
            extension = ".png";
        } else if (metaPart.contains("image/gif")) {
            extension = ".gif";
        } else {
            // 지원하지 않는 형식이면 임의의 확장자 또는 예외 처리
            extension = ".jpg";
        }

        byte[] decodedBytes = Base64.getDecoder().decode(dataPart);

        // 저장 경로 및 디렉토리 생성
        String fullDir = baseDir + "/" + subDir + "/";
        File directory = new File(fullDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 최종 파일 경로 및 파일 저장
        String fullFileName = fileName + extension;
        File file = new File(fullDir + fullFileName);

        try (OutputStream os = new FileOutputStream(file)) {
            os.write(decodedBytes);
        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 저장에 실패했습니다.", e);
        }

        // 웹에서 접근 가능한 상대 경로 반환 (WebMvcConfigurer 설정 필요)
        return "/images/" + subDir + "/" + fullFileName;
    }
}