package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.CourseDto;
import bunyodbek.uz.moreeduce.dto.LessonDto;
import bunyodbek.uz.moreeduce.dto.SubmitAssignmentRequest;
import bunyodbek.uz.moreeduce.entity.*;
import bunyodbek.uz.moreeduce.repository.*;
import bunyodbek.uz.moreeduce.service.EmailService;
import bunyodbek.uz.moreeduce.service.FileStorageService;
import bunyodbek.uz.moreeduce.service.StudentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final StudentProgressRepository progressRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final FileStorageService fileStorageService;
    private final ModuleRepository  moduleRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void enrollCourse(Long courseId, MultipartFile receiptFile, String studentEmail) throws IOException {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new IllegalStateException("Already enrolled in this course");
        }

        String receiptUrl = fileStorageService.uploadFile(receiptFile);

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .paymentReceiptUrl(receiptUrl)
                .status(EnrollmentStatus.PENDING) // Tasdiq kutilmoqda
                .enrolledAt(LocalDateTime.now())
                .build();
        enrollmentRepository.save(enrollment);

        // Teacherga bildirishnoma yuborish
        String teacherEmail = course.getTeacher().getEmail();
        String subject = "New Enrollment Request";
        String message = "Student " + student.getFirstName() + " " + student.getLastName() + 
                         " has requested to enroll in your course: " + course.getTitle() + 
                         ". Please check the payment receipt.";
        emailService.sendNotification(teacherEmail, subject, message);
    }

    @Override
    public List<CourseDto> getMyCourses(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        // Faqat tasdiqlangan (APPROVED) kurslarni qaytarish kerak
        return enrollmentRepository.findByStudentId(student.getId()).stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.APPROVED)
                .map(enrollment -> mapToCourseDto(enrollment.getCourse()))
                .collect(Collectors.toList());
    }

    @Override
    public List<LessonDto> getCourseLessons(Long courseId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        Enrollment enrollment = enrollmentRepository.findByStudentId(student.getId()).stream()
                .filter(e -> e.getCourse().getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new SecurityException("You are not enrolled in this course"));

        if (enrollment.getStatus() != EnrollmentStatus.APPROVED) {
            throw new SecurityException("Your enrollment is not approved yet");
        }

        return lessonRepository.findByCourseIdOrderByLessonOrderAsc(courseId).stream()
                .map(this::mapToLessonDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void completeLesson(Long lessonId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), lesson.getCourse().getId())) {
            throw new SecurityException("You are not enrolled in this course");
        }

        StudentProgress progress = progressRepository.findByStudentIdAndLessonId(student.getId(), lesson.getId())
                .orElse(StudentProgress.builder()
                        .student(student)
                        .lesson(lesson)
                        .build());

        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }

    @Override
    @Transactional
    public void updateLessonProgress(Long lessonId, Long position, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), lesson.getCourse().getId())) {
            throw new SecurityException("You are not enrolled in this course");
        }

        StudentProgress progress = progressRepository.findByStudentIdAndLessonId(student.getId(), lesson.getId())
                .orElse(StudentProgress.builder()
                        .student(student)
                        .lesson(lesson)
                        .build());

        progress.setLastWatchedPosition(position);
        progressRepository.save(progress);
    }

    @Override
    @Transactional
    public void submitAssignment(Long assignmentId, SubmitAssignmentRequest request, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), assignment.getLesson().getCourse().getId())) {
            throw new SecurityException("You are not enrolled in this course");
        }

        AssignmentSubmission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, student.getId())
                .orElse(AssignmentSubmission.builder()
                        .student(student)
                        .assignment(assignment)
                        .build());

        submission.setSubmissionContent(request.getSubmissionContent());
        submission.setSubmittedAt(LocalDateTime.now());
        submissionRepository.save(submission);

        // Teacherga bildirishnoma yuborish
        String teacherEmail = assignment.getLesson().getCourse().getTeacher().getEmail();
        String subject = "New Assignment Submission";
        String message = "Student " + student.getFirstName() + " " + student.getLastName() + 
                         " has submitted an assignment for lesson: " + assignment.getLesson().getTitle();
        emailService.sendNotification(teacherEmail, subject, message);
    }

    // Helper methods
    private CourseDto mapToCourseDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .teacherId(course.getTeacher().getId())
                .teacherName(course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName())
                .modulesCount(moduleRepository.countByCourseId(course.getId()))
                .build();
    }

    private LessonDto mapToLessonDto(Lesson lesson) {
        return LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .videoUrl(lesson.getVideoUrl())
                .transcription(lesson.getTranscription())
                .lessonOrder(lesson.getLessonOrder())
                .courseId(lesson.getCourse().getId())
                .moduleId(lesson.getModule() != null ? lesson.getModule().getId() : null)
                .fileUrls(lesson.getFileUrls())
                .build();
    }
}
