package com.example.backend.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자(Admin)가 사용자를 관리하기 위한 API 컨트롤러입니다.
 * 사용자 목록 조회, 필터링, 검색, 삭제 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/admin/users") // 관리자 전용 API 경로
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}") 
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 사용자 목록을 조건에 따라 조회하는 API 엔드포인트입니다.
     * 역할(role)과 검색어(searchQuery)를 파라미터로 받아 필터링 및 검색을 수행합니다.
     * @param role 'ALL', 'USER', 'MANAGER' 중 하나를 받습니다. 기본값은 'ALL'입니다.
     * @param searchQuery 이름 또는 이메일에 포함될 검색어입니다.
     * @return 조건에 맞는 사용자 DTO 리스트를 반환합니다.
     */
    @GetMapping
    public ResponseEntity<List<AdminUserDto>> getUsers(
            @RequestParam(defaultValue = "ALL") String role,
            @RequestParam(required = false) String searchQuery
    ) {
        List<AdminUserDto> users = adminUserService.getUsers(role, searchQuery);
        return ResponseEntity.ok(users);
    }

    /**
     * 특정 사용자를 삭제하는 API 엔드포인트입니다.
     * @param userId 삭제할 사용자의 ID를 URL 경로에서 받습니다.
     * @return 성공적으로 삭제되면 빈 응답(200 OK)을 반환합니다.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

     @PostMapping("/{userId}/approve")
    public ResponseEntity<Void> approveManager(@PathVariable Integer userId) {
        try {
            adminUserService.approveManager(userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            // 존재하지 않는 사용자이거나, 상태가 PENDING이 아닐 경우
            return ResponseEntity.badRequest().build();
        }
    }
}