package com.example.backend.register;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.admin.AdminUserDto;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByEmailAndSocial(String email, String social);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<UserEntity> findByRefreshToken(String refreshToken);
    Optional<UserEntity> findByBusinessNumber(String businessNumber);

    // [추가된 코드] 관리자 페이지를 위한 사용자 검색 및 필터링 쿼리
    @Query("SELECT new com.example.backend.admin.AdminUserDto(u.id, u.name, u.email, u.role, u.social, u.companyName, u.businessNumber) " +
        "FROM UserEntity u " +
        "WHERE (:role = 'ALL' OR u.role = :role) " +
        "AND (:searchQuery IS NULL OR u.name LIKE %:searchQuery% OR u.email LIKE %:searchQuery%) " +
        "ORDER BY u.id DESC")
    List<AdminUserDto> findUsersForAdmin(
        @Param("role") String role,
        @Param("searchQuery") String searchQuery
    );

    List<UserEntity> findAllByRole(String role);
}