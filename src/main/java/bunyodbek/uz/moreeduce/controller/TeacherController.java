package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.*;
import bunyodbek.uz.moreeduce.entity.EnrollmentStatus;
import bunyodbek.uz.moreeduce.service.EnrollmentService;
import bunyodbek.uz.moreeduce.service.TeacherDashboardService;
import bunyodbek.uz.moreeduce.service.UserProfileService;
import bunyodbek.uz.moreeduce.service.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
@Tag(name = "Teacher Panel", description = "O'qituvchi uchun shaxsiy kabinet API'lari")
@PreAuthorize("hasAuthority('TEACHER')")
public class TeacherController {

    private final UserProfileService userProfileService;
    private final WithdrawalService withdrawalService;
    private final TeacherDashboardService dashboardService;
    private final EnrollmentService enrollmentService; // To'g'ri servisga o'zgartirildi

    // --- Profil ---
    @Operation(summary = "O'z profil ma'lumotlarini olish")
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getMyProfile(Principal principal) {
        return ResponseEntity.ok(userProfileService.getMyProfile(principal.getName()));
    }

    @Operation(summary = "O'z profil ma'lumotlarini yangilash")
    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateTeacherProfile(@RequestBody TeacherProfileUpdateDto profileDto, Principal principal) {
        return ResponseEntity.ok(userProfileService.updateTeacherProfile(principal.getName(), profileDto));
    }

    // --- Dashboard (Statistika) ---
    @Operation(summary = "Dashboard: Umumiy talabalar sonini olish")
    @GetMapping("/dashboard/total-students")
    public ResponseEntity<Long> getTotalStudents(Principal principal) {
        return ResponseEntity.ok(dashboardService.getTotalStudentsCount(principal));
    }

    @Operation(summary = "Dashboard: Baholanmagan vazifalar sonini olish")
    @GetMapping("/dashboard/pending-submissions")
    public ResponseEntity<Long> getPendingSubmissions(Principal principal) {
        return ResponseEntity.ok(dashboardService.getPendingSubmissionsCount(principal));
    }

    @Operation(summary = "Dashboard: Tasdiqlanishi kerak bo'lgan a'zoliklar soni")
    @GetMapping("/dashboard/pending-enrollments")
    public ResponseEntity<Long> getPendingEnrollments(Principal principal) {
        return ResponseEntity.ok(dashboardService.getPendingEnrollmentsCount(principal));
    }

    @Operation(summary = "Dashboard: Joriy oydagi daromadni olish")
    @GetMapping("/dashboard/monthly-revenue")
    public ResponseEntity<BigDecimal> getMonthlyRevenue(Principal principal) {
        return ResponseEntity.ok(dashboardService.getMonthlyRevenue(principal));
    }

    @Operation(summary = "Dashboard: Eng ommabop kursni olish")
    @GetMapping("/dashboard/most-popular-course")
    public ResponseEntity<CourseDto> getMostPopularCourse(Principal principal) {
        return ResponseEntity.ok(dashboardService.getMostPopularCourse(principal));
    }

    // --- Talabalarni boshqarish ---
    @Operation(summary = "Talabaning pullik kursga yozilish so'rovini tasdiqlash/rad etish")
    @PutMapping("/enrollments/{enrollmentId}/status")
    public ResponseEntity<String> updateEnrollmentStatus(
            @PathVariable Long enrollmentId,
            @RequestParam EnrollmentStatus status,
            Principal principal) {
        // XATO TUZATILDI: To'g'ri servis metodi chaqirildi
        enrollmentService.updateEnrollmentStatus(enrollmentId, status, principal);
        return ResponseEntity.ok("Enrollment status updated successfully.");
    }

    // --- Moliya ---
    @Operation(summary = "Pul yechish so'rovini yuborish")
    @PostMapping("/withdrawals")
    public ResponseEntity<WithdrawalRequestDto> requestWithdrawal(@RequestBody WithdrawalRequestDto requestDto, Principal principal) {
        return new ResponseEntity<>(withdrawalService.createWithdrawalRequest(requestDto, principal.getName()), HttpStatus.CREATED);
    }

    @Operation(summary = "O'zining pul yechish so'rovlari tarixini olish")
    @GetMapping("/withdrawals")
    public ResponseEntity<List<WithdrawalRequestDto>> getMyWithdrawals(Principal principal) {
        return ResponseEntity.ok(withdrawalService.getMyWithdrawalRequests(principal.getName()));
    }
}
