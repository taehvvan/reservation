package com.example.backend.inquiry;

import com.example.backend.mailcheck.EmailSendService;
import com.example.backend.register.UserEntity;
import com.example.backend.register.UserRepository;
import com.example.backend.sms.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final EmailSendService emailSendService;
    private final SmsService smsService;

    // application.properties에서 발신자 번호 가져오기
    @Value("${coolsms.sender.phone}")
    private String fromNumber;

    // 문의 등록
    @Transactional
    public InquiryResponseDTO createInquiry(InquiryRequestDTO requestDTO, Integer userId) {
        Inquiry.InquiryBuilder inquiryBuilder = Inquiry.builder()
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .authorName(requestDTO.getAuthorName())
                .secret(requestDTO.isSecret())
                .status(InquiryStatus.PENDING);

        if (userId != null) {
            // 회원인 경우
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            inquiryBuilder.user(user);
        } else {
            // 비회원인 경우
            inquiryBuilder.contactEmail(requestDTO.getContactEmail());
        }

        Inquiry savedInquiry = inquiryRepository.save(inquiryBuilder.build());
        return convertToResponseDTO(savedInquiry);
    }

    // 관리자: 모든 문의 목록 조회
    @Transactional(readOnly = true)
    public List<InquiryResponseDTO> getAllInquiries() {
        // [수정] findAll() 대신 새로 만든 findAllWithUser() 메소드를 사용합니다.
        return inquiryRepository.findAllWithUser().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // 사용자: 자신의 문의 목록 조회
    @Transactional(readOnly = true)
    public List<InquiryResponseDTO> getInquiriesByUserId(Integer userId) {
        return inquiryRepository.findByUser_Id(userId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    // 문의 상세 조회
    @Transactional(readOnly = true)
    public InquiryResponseDTO getInquiryById(Integer id) {
        Inquiry inquiry = inquiryRepository.findById(id).orElseThrow(() -> new RuntimeException("Inquiry not found"));
        return convertToResponseDTO(inquiry);
    }

    // 관리자: 답변 등록
    @Transactional
    public InquiryResponseDTO addAnswer(Integer inquiryId, InquiryAnswerDTO answerDTO) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));

        inquiry.setAnswer(answerDTO.getAnswer());
        inquiry.setStatus(InquiryStatus.ANSWERED);
        inquiry.setAnsweredAt(LocalDateTime.now());

        Inquiry updatedInquiry = inquiryRepository.save(inquiry);

        String recipientEmail = null;
        if (updatedInquiry.isMember()) {
            // 회원인 경우: UserEntity에서 이메일 가져오기
            recipientEmail = updatedInquiry.getUser().getEmail();
        } else {
            // 비회원인 경우: Inquiry에 저장된 contactEmail 가져오기
            recipientEmail = updatedInquiry.getContactEmail();
        }

        // 이메일 주소가 있으면 알림 발송
        if (recipientEmail != null && !recipientEmail.isEmpty()) {
            String subject = "[쉼, 한국] 문의하신 내용에 답변이 등록되었습니다.";
            String text = "문의하신 내용에 답변이 등록되었습니다. 확인해주세요.";
            emailSendService.sendNotificationEmail(recipientEmail, subject, text);
        }

        return convertToResponseDTO(updatedInquiry);
    }

    @Transactional(readOnly = true)
    public List<InquiryResponseDTO> getInquiriesForBoard() {
        // 새로 만든 Repository 메소드를 사용하여 모든 문의를 최신순으로 가져옵니다.
        return inquiryRepository.findAllWithUserOrderByCreatedAtDesc().stream()
                .map(inquiry -> {
                    InquiryResponseDTO dto = convertToResponseDTO(inquiry);
                    // 만약 글이 비공개라면, 제목을 마스킹하고 내용은 비웁니다.
                    if (inquiry.isSecret()) {
                        dto.setTitle("비밀글입니다.");
                        dto.setContent(null); // 내용은 전송하지 않음
                        dto.setAnswer(null);  // 답변도 전송하지 않음
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }


    private InquiryResponseDTO convertToResponseDTO(Inquiry inquiry) {
        InquiryResponseDTO dto = new InquiryResponseDTO();
        dto.setId(inquiry.getId());
        dto.setTitle(inquiry.getTitle());
        dto.setContent(inquiry.getContent());
        dto.setAuthorName(inquiry.getAuthorName());
        dto.setStatus(inquiry.getStatus());
        dto.setAnswer(inquiry.getAnswer());
        dto.setCreatedAt(inquiry.getCreatedAt());
        dto.setAnsweredAt(inquiry.getAnsweredAt());
        dto.setSecret(inquiry.isSecret());

        // inquiry에 연결된 UserEntity 객체를 가져옵니다.
        UserEntity user = inquiry.getUser();
        if (user != null) {
            dto.setMember(true);
            dto.setEmail(user.getEmail()); // [수정] 통합된 email 필드에 회원 이메일 설정
        } else {
            dto.setMember(false);
            dto.setEmail(inquiry.getContactEmail()); // [수정] 통합된 email 필드에 비회원 이메일 설정
        }

        return dto;
    }
}