package com.example.backend.register;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Entity
@Table(name="users")
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "u_id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "phone", length = 13)
    private String phone;

    @Column(name = "birth", length = 255)
    private String birth;

    @Column(name = "social", length = 255)
    private String social;

   @Builder.Default
@Column(name = "role", length = 50, nullable = false, columnDefinition = "varchar(50) default 'ROLE_USER'")
private String role = "ROLE_USER";

    @Column(name = "refresh_token")
    private String refreshToken;

    // 매니저 관련 필드 추가
    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "business_number", length = 255)
    private String businessNumber;

    public static UserEntity fromDto(UserDTO userDto, boolean isManager) {
        UserEntity user = UserEntity.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .phone(userDto.getPhone())
                .birth(userDto.getBirth())
                .build();

        if (isManager) {
            if (userDto.getBusinessNumber() == null || userDto.getBusinessNumber().isEmpty()) {
                throw new IllegalArgumentException("사업자 번호는 필수입니다.");
            }
            user.setBusinessNumber(userDto.getBusinessNumber());
        }

    return user;
    }
}
