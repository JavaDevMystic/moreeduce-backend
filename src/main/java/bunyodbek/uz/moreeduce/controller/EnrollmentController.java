package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.CourseDto;
import bunyodbek.uz.moreeduce.dto.EnrollmentDto;
import bunyodbek.uz.moreeduce.dto.EnrollmentStatusDto;
import bunyodbek.uz.moreeduce.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollment Management", description = "Kursga a'zo bo'lish va a'zolikni boshqarish")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Operation(summary = "Kursga yozilish (bepul yoki pullik)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<EnrollmentDto> enroll(
            @RequestParam Long courseId,
            @RequestPart(value = "receipt", required = false) MultipartFile receipt,
            Principal principal) throws IOException {
        EnrollmentDto enrollment = enrollmentService.enroll(courseId, receipt, principal);
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
    }

    @Operation(summary = "Talabaning o'z a'zo bo'lgan kurslarini olish")
    @GetMapping("/my-courses")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<List<CourseDto>> getMyEnrolledCourses(Principal principal) {
        return ResponseEntity.ok(enrollmentService.getMyEnrolledCourses(principal.getName()));
    }

    @Operation(summary = "Kursga a'zolik holatini tekshirish")
    @GetMapping("/status/{courseId}")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<EnrollmentStatusDto> getStatus(@PathVariable Long courseId, Principal principal) {
        boolean isEnrolled = enrollmentService.isEnrolled(courseId, principal.getName());
        String status = isEnrolled ? "ENROLLED" : "NOT_ENROLLED";
        return ResponseEntity.ok(new EnrollmentStatusDto(status));
    }

    @Operation(summary = "Kursdan chiqish (siyosat bilan)")
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<Void> unenrollCourse(@PathVariable Long courseId, Principal principal) {
        enrollmentService.unenrollCourse(courseId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
