package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.CourseDto;
import bunyodbek.uz.moreeduce.dto.EnrollmentDto;
import bunyodbek.uz.moreeduce.dto.ManualEnrollmentRequest;
import bunyodbek.uz.moreeduce.entity.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

public interface EnrollmentService {
    
    // --- Talaba uchun ---
    EnrollmentDto enroll(Long courseId, MultipartFile receipt, Principal principal) throws IOException;
    List<CourseDto> getMyEnrolledCourses(String studentEmail);
    void unenrollCourse(Long courseId, String studentEmail);
    boolean isEnrolled(Long courseId, String studentEmail);

    // --- Admin/O'qituvchi uchun ---
    Page<EnrollmentDto> getEnrollmentsByCourse(Long courseId, Pageable pageable, Principal principal);
    EnrollmentDto manuallyEnrollStudent(ManualEnrollmentRequest request, Principal principal);
    void unenrollStudentByAdmin(Long enrollmentId, Principal principal);
    void updateEnrollmentStatus(Long enrollmentId, EnrollmentStatus status, Principal principal); // Yangi metod
    Page<EnrollmentDto> getAllEnrollments(Pageable pageable, Principal principal);
    Page<EnrollmentDto> getPendingEnrollments(Pageable pageable, Principal principal);
}
