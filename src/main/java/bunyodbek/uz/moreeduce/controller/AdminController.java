package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.*;
import bunyodbek.uz.moreeduce.entity.EnrollmentStatus;
import bunyodbek.uz.moreeduce.entity.WithdrawalStatus;
import bunyodbek.uz.moreeduce.service.AdminService;
import bunyodbek.uz.moreeduce.service.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Admin uchun boshqaruv paneli")
@PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')") // Admin va Super Admin uchun
public class AdminController {

    private final AdminService adminService;
    private final WithdrawalService withdrawalService;

    @Operation(summary = "Dashboard statistikasini olish")
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @Operation(summary = "Barcha foydalanuvchilarni olish")
    @GetMapping("/users")
    public ResponseEntity<List<UserProfileDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @Operation(summary = "Foydalanuvchi ma'lumotlarini tahrirlash (Admin tomonidan)")
    @PutMapping("/users/{userId}")
    public ResponseEntity<UserProfileDto> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateByAdminDto userUpdateDto) {
        return ResponseEntity.ok(adminService.updateUser(userId, userUpdateDto));
    }

    @Operation(summary = "Barcha kurslarni olish")
    @GetMapping("/courses")
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        return ResponseEntity.ok(adminService.getAllCourses());
    }

    @Operation(summary = "Kurs ma'lumotlarini tahrirlash (Admin tomonidan)")
    @PutMapping("/courses/{courseId}")
    public ResponseEntity<CourseDto> updateCourse(
            @PathVariable Long courseId,
            @RequestBody CourseUpdateByAdminDto courseUpdateDto) {
        return ResponseEntity.ok(adminService.updateCourse(courseId, courseUpdateDto));
    }

    @Operation(summary = "Foydalanuvchini o'chirish")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @Operation(summary = "Kursni o'chirish")
    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<String> deleteCourse(@PathVariable Long courseId) {
        adminService.deleteCourse(courseId);
        return ResponseEntity.ok("Course deleted successfully");
    }

    @Operation(summary = "O'qituvchini tasdiqlash")
    @PutMapping("/users/{userId}/verify")
    public ResponseEntity<UserProfileDto> verifyTeacher(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.verifyTeacher(userId));
    }

    @Operation(summary = "Foydalanuvchini bloklash")
    @PutMapping("/users/{userId}/block")
    public ResponseEntity<String> blockUser(@PathVariable Long userId) {
        adminService.blockUser(userId);
        return ResponseEntity.ok("User blocked successfully");
    }

    @Operation(summary = "Foydalanuvchini blokdan chiqarish")
    @PutMapping("/users/{userId}/unblock")
    public ResponseEntity<String> unblockUser(@PathVariable Long userId) {
        adminService.unblockUser(userId);
        return ResponseEntity.ok("User unblocked successfully");
    }

    @Operation(summary = "Kursni tasdiqlash")
    @PutMapping("/courses/{courseId}/approve")
    public ResponseEntity<String> approveCourse(@PathVariable Long courseId) {
        adminService.approveCourse(courseId);
        return ResponseEntity.ok("Course approved successfully");
    }

    @Operation(summary = "Kursni rad etish")
    @PutMapping("/courses/{courseId}/reject")
    public ResponseEntity<String> rejectCourse(@PathVariable Long courseId) {
        adminService.rejectCourse(courseId);
        return ResponseEntity.ok("Course rejected successfully");
    }

    @Operation(summary = "Barcha pul yechish so'rovlarini olish")
    @GetMapping("/withdrawals")
    public ResponseEntity<List<WithdrawalRequestDto>> getAllWithdrawals() {
        return ResponseEntity.ok(withdrawalService.getAllWithdrawalRequests());
    }

    @Operation(summary = "Pul yechish so'rovini tasdiqlash")
    @PutMapping("/withdrawals/{requestId}/approve")
    public ResponseEntity<Void> approveWithdrawal(@PathVariable Long requestId) {
        withdrawalService.approveWithdrawalRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Pul yechish so'rovini rad etish")
    @PutMapping("/withdrawals/{requestId}/reject")
    public ResponseEntity<Void> rejectWithdrawal(@PathVariable Long requestId) {
        withdrawalService.rejectWithdrawalRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Barcha platforma to'lovlarini olish")
    @GetMapping("/platform-payments")
    public ResponseEntity<List<PlatformPaymentDto>> getAllPlatformPayments() {
        return ResponseEntity.ok(adminService.getAllPlatformPayments());
    }

    @Operation(summary = "Platforma to'lovlarini tasdiqlash")
    @PutMapping("/platform-payments/{paymentId}/approve")
    public ResponseEntity<PlatformPaymentDto> approvePlatformPayment(
            @PathVariable Long paymentId,
            @RequestParam EnrollmentStatus status) {
        return ResponseEntity.ok(adminService.approvePlatformPayment(paymentId, status));
    }

    @Operation(summary = "Barcha izohlarni olish")
    @GetMapping("/comments")
    public ResponseEntity<List<CommentDto>> getAllComments() {
        return ResponseEntity.ok(adminService.getAllComments());
    }

    @Operation(summary = "Izohni o'chirish")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        adminService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/enrollments/pending")
    public ResponseEntity<List<AdminEnrollmentDto>> getPendingEnrollments() {
        return ResponseEntity.ok(
                withdrawalService.getPendingEnrollments()
        );
    }

    @PutMapping("/{enrollmentId}/status")
    public ResponseEntity<String> updateEnrollmentStatus(
            @PathVariable Long enrollmentId,
            @RequestParam EnrollmentStatus status
    ) {

        withdrawalService.updateEnrollmentStatus(
                enrollmentId,
                status
        );

        return ResponseEntity.ok(
                "Enrollment status muvaffaqiyatli yangilandi"
        );
    }

}
