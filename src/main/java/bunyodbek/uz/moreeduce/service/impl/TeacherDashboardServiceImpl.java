package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.CourseDto;
import bunyodbek.uz.moreeduce.entity.Course;
import bunyodbek.uz.moreeduce.entity.User;
import bunyodbek.uz.moreeduce.repository.*;
import bunyodbek.uz.moreeduce.service.TeacherDashboardService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeacherDashboardServiceImpl implements TeacherDashboardService {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;

    @Override
    public long getTotalStudentsCount(Principal principal) {
        User teacher = getUserByEmail(principal.getName());
        return enrollmentRepository.countDistinctStudentByTeacherId(teacher.getId());
    }

    @Override
    public long getPendingSubmissionsCount(Principal principal) {
        User teacher = getUserByEmail(principal.getName());
        return submissionRepository.countByTeacherIdAndStatusPending(teacher.getId());
    }

    @Override
    public long getPendingEnrollmentsCount(Principal principal) {
        User teacher = getUserByEmail(principal.getName());
        return enrollmentRepository.countByTeacherIdAndStatusPending(teacher.getId());
    }

    @Override
    public BigDecimal getMonthlyRevenue(Principal principal) {
        User teacher = getUserByEmail(principal.getName());
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        // Bu yerda daromadni hisoblash logikasi kerak. Hozircha 0 qaytaramiz.
        // Buning uchun alohida "Transaction" yoki "Payment" entitysi kerak bo'ladi.
        return BigDecimal.ZERO;
    }

    @Override
    public CourseDto getMostPopularCourse(Principal principal) {
        User teacher = getUserByEmail(principal.getName());
        Optional<Course> courseOpt = courseRepository
                .findMostPopularCourses(
                        teacher.getId(),
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst();
        return courseOpt.map(this::mapToCourseDto).orElse(null);
    }


    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
    }

    private CourseDto mapToCourseDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .thumbnailUrl(course.getThumbnailUrl())
                .studentsCount(enrollmentRepository.countByCourseId(course.getId()))
                .modulesCount(moduleRepository.countByCourseId(course.getId()))
                .build();
    }
}
