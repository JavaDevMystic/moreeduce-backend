package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.*;
import bunyodbek.uz.moreeduce.entity.*;
import bunyodbek.uz.moreeduce.repository.*;
import bunyodbek.uz.moreeduce.service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PlatformPaymentRepository platformPaymentRepository;
    private final CommentRepository commentRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;

    @Override
    public AdminDashboardDto getDashboardStats() {
        long totalStudents = userRepository.countByRole(Role.STUDENT);
        long totalTeachers = userRepository.countByRole(Role.TEACHER);
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(LocalDateTime.now().withDayOfMonth(1));
        
        long totalCourses = courseRepository.count();
        long pendingCourses = courseRepository.countByStatus(CourseStatus.PENDING);
        
        List<CourseDto> mostPopularCourses = courseRepository.findTop5ByOrderByEnrollmentsDesc(PageRequest.of(0, 5))
                .stream()
                .map(this::mapToCourseDto)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = platformPaymentRepository.calculateTotalRevenue();
        long pendingWithdrawals = withdrawalRequestRepository.countByStatus(WithdrawalStatus.PENDING);

        return AdminDashboardDto.builder()
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .newUsersThisMonth(newUsersThisMonth)
                .totalCourses(totalCourses)
                .pendingCourses(pendingCourses)
                .mostPopularCourses(mostPopularCourses)
                .totalRevenue(totalRevenue)
                .pendingWithdrawals(pendingWithdrawals)
                .build();
    }

    @Override
    public List<UserProfileDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserProfileDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserProfileDto updateUser(Long userId, UserUpdateByAdminDto userUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        user.setFirstName(userUpdateDto.getFirstname());
        user.setLastName(userUpdateDto.getLastname());
        user.setEmail(userUpdateDto.getEmail());
        user.setRole(userUpdateDto.getRole());

        User updatedUser = userRepository.save(user);
        return mapToUserProfileDto(updatedUser);
    }

    @Override
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToCourseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourseDto updateCourse(Long courseId, CourseUpdateByAdminDto courseUpdateDto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        course.setTitle(courseUpdateDto.getTitle());
        course.setDescription(courseUpdateDto.getDescription());
        course.setPrice(courseUpdateDto.getPrice());
        course.setPublic(courseUpdateDto.isPublic());

        Course updatedCourse = courseRepository.save(course);
        return mapToCourseDto(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        log.info("Deleting course with ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        courseRepository.delete(course);
    }

    @Override
    @Transactional
    public UserProfileDto verifyTeacher(Long teacherId) {
        log.info("Verifying teacher with ID: {}", teacherId);
        User user = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + teacherId));

        if (user.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("User is not a teacher");
        }

        user.setVerified(true);
        User savedUser = userRepository.save(user);
        return mapToUserProfileDto(savedUser);
    }

    @Override
    public List<PlatformPaymentDto> getAllPlatformPayments() {
        return platformPaymentRepository.findAll().stream()
                .map(this::mapToPlatformPaymentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlatformPaymentDto approvePlatformPayment(Long paymentId, EnrollmentStatus status) {
        log.info("Approving platform payment ID: {} with status: {}", paymentId, status);
        PlatformPayment payment = platformPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + paymentId));

        payment.setStatus(status);
        
        // Video yuklash limiti olib tashlandi
        // if (status == EnrollmentStatus.APPROVED) {
        //     User teacher = payment.getTeacher();
        //     teacher.setVideoUploadLimit(teacher.getVideoUploadLimit() + 10);
        //     userRepository.save(teacher);
        // }

        PlatformPayment savedPayment = platformPaymentRepository.save(payment);
        return mapToPlatformPaymentDto(savedPayment);
    }

    @Override
    @Transactional
    public void blockUser(Long userId) {
        log.info("Blocking user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        user.setBlocked(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unblockUser(Long userId) {
        log.info("Unblocking user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        user.setBlocked(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void approveCourse(Long courseId) {
        log.info("Approving course with ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        course.setStatus(CourseStatus.APPROVED);
        courseRepository.save(course);
    }

    @Override
    @Transactional
    public void rejectCourse(Long courseId) {
        log.info("Rejecting course with ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        course.setStatus(CourseStatus.REJECTED);
        courseRepository.save(course);
    }

    @Override
    public List<CommentDto> getAllComments() {
        return commentRepository.findAll().stream()
                .map(this::mapToCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        log.info("Deleting comment with ID: {}", commentId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));
        commentRepository.delete(comment);
    }

    private UserProfileDto mapToUserProfileDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .isVerified(user.isVerified())
                .isBlocked(user.isBlocked())
                .resumeUrl(user.getResumeUrl())
                .certificatesUrl(user.getCertificatesUrl())
                .socialMediaLinks(user.getSocialMediaLinks())
                .bio(user.getBio())
                .build();
    }

    private CourseDto mapToCourseDto(Course course) {
        long studentsCount = enrollmentRepository.countByCourseId(course.getId());

        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .category(course.getCategory())
                .language(course.getLanguage())
                .rating(course.getRating())
                .status(course.getStatus())
                .teacherId(course.getTeacher().getId())
                .teacherName(course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName())
                .studentsCount(studentsCount)
                .build();
    }

    private PlatformPaymentDto mapToPlatformPaymentDto(PlatformPayment payment) {
        return PlatformPaymentDto.builder()
                .id(payment.getId())
                .teacherId(payment.getTeacher().getId())
                .teacherName(payment.getTeacher().getFirstName() + " " + payment.getTeacher().getLastName())
                .amount(payment.getAmount())
                .paymentReceiptUrl(payment.getPaymentReceiptUrl())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private CommentDto mapToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName())
                .lessonId(comment.getLesson().getId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
