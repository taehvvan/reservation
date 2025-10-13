package com.example.backend.mailcheck;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.register.UserEntity;
import com.example.backend.register.UserRepository;

@Service
public class VerificationService {

    private final EmailSendService emailSendService;
    
    private final UserRepository userRepository;

    private final ConcurrentHashMap<String, String> verificationCodes = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, String> resetTokens = new ConcurrentHashMap<>();

    private final PasswordEncoder passwordEncoder;

    // 인증번호 만료를 위한 스케줄러
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @Autowired
    public VerificationService(EmailSendService emailSendService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.emailSendService = emailSendService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public void sendVerificationCode(String email) {
        String code = generateRandomCode();

        // 2. 인증번호를 맵에 저장하고 3분 후 만료되도록 설정
        verificationCodes.put(email, code);
            
        // 3분 후에 맵에서 해당 인증번호를 삭제하는 작업 예약
        scheduler.schedule(() -> verificationCodes.remove(email), 3, TimeUnit.MINUTES);

         // 3. EmailSendService를 호출하여 인증 메일 전송
        emailSendService.sendVerificationEmail(email, code);
    }

        
        public boolean verifyCode(String email, String code) {
            // 맵에 저장된 인증번호를 가져옴
            String storedCode = verificationCodes.get(email);

            // 맵에 저장된 코드가 없거나, 입력된 코드와 일치하지 않으면 false 반환
            if (storedCode == null || !storedCode.equals(code)) {
                return false;
            }

            // 인증 성공 후, 재사용 방지를 위해 맵에서 인증번호 삭제
            verificationCodes.remove(email);
            return true;
        }

        
        private String generateRandomCode() {
            Random random = new Random();
            int code = random.nextInt(900000) + 100000; // 100000 ~ 999999 범위
            return String.valueOf(code);
        }

        public String createResetToken(String email){
            userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            String resetToken = UUID.randomUUID().toString();
            resetTokens.put(email, resetToken);

            scheduler.schedule(() -> resetTokens.remove(email), 5, TimeUnit.MINUTES);
            
            return resetToken;
        }

        public void resetPassword(String resetToken, String newPassword){
            String email = findEmailByToken(resetToken);

            if(email == null){
                throw new IllegalArgumentException("유효하지 않거나 만료된 토큰입니다.");
            }
            UserEntity user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            resetTokens.remove(email);
        }

        private String findEmailByToken(String resetToken){
            for(Map.Entry<String, String> entry : resetTokens.entrySet()){
                if(entry.getValue().equals(resetToken)){
                    return entry.getKey();
                }
            }
            return null;
        }

}