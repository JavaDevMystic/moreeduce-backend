package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.EnrollmentDto;
import bunyodbek.uz.moreeduce.dto.ManualEnrollmentRequest;
import bunyodbek.uz.moreeduce.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/admin/enrollments")
@RequiredArgsConstructor
@Tag(name = "Admin Enrollment Management", description = "A'zoliklarni admin/o'qituvchi tomonidan boshqarish")
@PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
public class AdminEnrollmentController {

    private final EnrollmentService enrollmentService;

    @Operation(summary = "Kursga a'zo bo'lgan talabalar ro'yxatini olish")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<EnrollmentDto>> getEnrollmentsByCourse(
            @PathVariable Long courseId,
            @Parameter(hidden = true) Pageable pageable,
            Principal principal) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(courseId, pageable, principal));
    }

    @Operation(summary = "Talabani kursga qo'lda qo'shish")
    @PostMapping
    public ResponseEntity<EnrollmentDto> manuallyEnrollStudent(
            @RequestBody ManualEnrollmentRequest request,
            Principal principal) {
        EnrollmentDto enrollment = enrollmentService.manuallyEnrollStudent(request, principal);
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
    }

    @Operation(summary = "Talabani kursdan chiqarib yuborish")
    @DeleteMapping("/{enrollmentId}")
    public ResponseEntity<Void> unenrollStudent(
            @PathVariable Long enrollmentId,
            Principal principal) {
        enrollmentService.unenrollStudentByAdmin(enrollmentId, principal);
        return ResponseEntity.noContent().build();
    }
}
