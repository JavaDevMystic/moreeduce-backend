package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.*;
import bunyodbek.uz.moreeduce.entity.EnrollmentStatus;

import java.util.List;

public interface AdminService {
    AdminDashboardDto getDashboardStats();
    List<UserProfileDto> getAllUsers();
    List<CourseDto> getAllCourses();

    void deleteUser(Long userId);
    void deleteCourse(Long courseId);

    UserProfileDto verifyTeacher(Long teacherId);

    // Platforma to'lovlarini boshqarish
    List<PlatformPaymentDto> getAllPlatformPayments();
    PlatformPaymentDto approvePlatformPayment(Long paymentId, EnrollmentStatus status);

    // Foydalanuvchilarni boshqarish
    void blockUser(Long userId);
    void unblockUser(Long userId);
    UserProfileDto updateUser(Long userId, UserUpdateByAdminDto userUpdateDto);

    // Kurslarni boshqarish
    void approveCourse(Long courseId);
    void rejectCourse(Long courseId);
    CourseDto updateCourse(Long courseId, CourseUpdateByAdminDto courseUpdateDto);

    // Izohlarni boshqarish
    List<CommentDto> getAllComments();
    void deleteComment(Long commentId);
}
