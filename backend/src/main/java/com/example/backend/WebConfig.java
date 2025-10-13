package com.example.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ application.properties에서 값을 주입받음
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/*") // 모든 /api 경로에 대해 CORS 허용
                .allowedOrigins("http://localhost:5173", "http://localhost:8888", "http://172.16.15.93:5173",
                        "http://172.16.15.95:5173") // Vue.js 앱의 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 자격증명(쿠키 등) 포함한 요청 허용ㄴ
    }

    // 이미지 파일 서빙 설정
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ 주입받은 uploadDir 변수를 사용하여 동적으로 경로 설정
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///" + uploadDir);
    }
}
