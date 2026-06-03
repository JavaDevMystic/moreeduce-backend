package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.CourseDto;
import bunyodbek.uz.moreeduce.dto.EnrollmentDto;
import bunyodbek.uz.moreeduce.dto.ManualEnrollmentRequest;
import bunyodbek.uz.moreeduce.entity.*;
import bunyodbek.uz.moreeduce.repository.*;
import bunyodbek.uz.moreeduce.service.EnrollmentService;
import bunyodbek.uz.moreeduce.service.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final StudentProgressRepository progressRepository;
    private final LessonRepository lessonRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public EnrollmentDto enroll(Long courseId, MultipartFile receipt, Principal principal) throws IOException {
        User student = getUserByEmail(principal.getName());
        Course course = getCourseById(courseId);

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new IllegalStateException("You are already enrolled in this course or your request is pending.");
        }

        if (course.getPrice() == null || course.getPrice().doubleValue() == 0) {
            Enrollment enrollment = Enrollment.builder()
                    .student(student)
                    .course(course)
                    .enrolledAt(LocalDateTime.now())
                    .status(EnrollmentStatus.APPROVED)
                    .build();
            return mapToDto(enrollmentRepository.save(enrollment));
        }

        if (receipt == null || receipt.isEmpty()) {
            throw new IllegalArgumentException("Payment receipt is required for paid courses.");
        }

        String receiptUrl = fileStorageService.uploadFile(receipt);

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrolledAt(LocalDateTime.now())
                .paymentReceiptUrl(receiptUrl)
                .status(EnrollmentStatus.PENDING)
                .build();
        
        return mapToDto(enrollmentRepository.save(enrollment));
    }

    @Override
    public List<CourseDto> getMyEnrolledCourses(String studentEmail) {
        User student = getUserByEmail(studentEmail);
        return enrollmentRepository.findByStudentId(student.getId()).stream()
                .map(Enrollment::getCourse)
                .map(this::mapToCourseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void unenrollCourse(Long courseId, String studentEmail) {
        User student = getUserByEmail(studentEmail);
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseThrow(() -> new EntityNotFoundException("You are not enrolled in this course."));

        long daysEnrolled = ChronoUnit.DAYS.between(enrollment.getEnrolledAt(), LocalDateTime.now());
        if (daysEnrolled > 14) {
            throw new IllegalStateException("You cannot unenroll from a course after 14 days.");
        }

        long totalLessons = lessonRepository.countByCourseId(courseId);
        if (totalLessons > 0) {
            long completedLessons = progressRepository.countByStudentIdAndCourseIdAndIsCompletedTrue(student.getId(), courseId);
            double completionPercentage = ((double) completedLessons / totalLessons) * 100;
            if (completionPercentage > 10) {
                throw new IllegalStateException("You cannot unenroll after completing more than 10% of the course.");
            }
        }

        enrollmentRepository.delete(enrollment);
    }

    @Override
    public boolean isEnrolled(Long courseId, String studentEmail) {
        User student = getUserByEmail(studentEmail);
        return enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId);
    }

    @Override
    public Page<EnrollmentDto> getEnrollmentsByCourse(Long courseId, Pageable pageable, Principal principal) {
        Course course = getCourseById(courseId);
        User currentUser = getUserByEmail(principal.getName());

        if (!currentUser.getRole().equals(Role.ADMIN) && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to view enrollments for this course.");
        }

        return enrollmentRepository.findByCourseId(courseId, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional
    public EnrollmentDto manuallyEnrollStudent(ManualEnrollmentRequest request, Principal principal) {
        User currentUser = getUserByEmail(principal.getName());
        Course course = getCourseById(request.getCourseId());
        User student = getUserById(request.getStudentId());

        if (!currentUser.getRole().equals(Role.ADMIN) && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to enroll students in this course.");
        }

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new IllegalStateException("Student is already enrolled in this course.");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrolledAt(LocalDateTime.now())
                .status(EnrollmentStatus.APPROVED)
                .build();

        return mapToDto(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public void unenrollStudentByAdmin(Long enrollmentId, Principal principal) {
        User currentUser = getUserByEmail(principal.getName());
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found."));

        if (!currentUser.getRole().equals(Role.ADMIN) && !enrollment.getCourse().getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to unenroll students from this course.");
        }

        enrollmentRepository.delete(enrollment);
    }

    @Override
    @Transactional
    public void updateEnrollmentStatus(Long enrollmentId, EnrollmentStatus status, Principal principal) {
        User currentUser = getUserByEmail(principal.getName());
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found."));

        // Xavfsizlik: Faqat kurs o'qituvchisi yoki admin statusni o'zgartira oladi
        if (!currentUser.getRole().equals(Role.ADMIN) && !enrollment.getCourse().getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to update this enrollment status.");
        }

        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING enrollments can be updated.");
        }

        enrollment.setStatus(status);
        enrollmentRepository.save(enrollment);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    private Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
    }

    private EnrollmentDto mapToDto(Enrollment enrollment) {
        return EnrollmentDto.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentName(enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .enrolledAt(enrollment.getEnrolledAt())
                .status(enrollment.getStatus().name())
                .build();
    }

    private CourseDto mapToCourseDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .thumbnailUrl(course.getThumbnailUrl())
                .category(course.getCategory())
                .language(course.getLanguage())
                .rating(course.getRating())
                .status(course.getStatus())
                .isPublic(course.isPublic())
                .teacherId(course.getTeacher().getId())
                .teacherName(course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName())
                .studentsCount(enrollmentRepository.countByCourseId(course.getId()))
                .build();
    }
}
