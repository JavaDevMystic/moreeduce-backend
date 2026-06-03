package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.AssignmentDto;
import bunyodbek.uz.moreeduce.dto.AssignmentSubmissionDto;
import bunyodbek.uz.moreeduce.dto.GradeAssignmentRequest;
import bunyodbek.uz.moreeduce.dto.SubmitAssignmentRequest;
import bunyodbek.uz.moreeduce.entity.*;
import bunyodbek.uz.moreeduce.repository.*;
import bunyodbek.uz.moreeduce.service.AssignmentService;
import bunyodbek.uz.moreeduce.service.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileStorageService fileStorageService; // Fayl saqlash uchun

    @Override
    @Transactional
    public AssignmentDto createAssignment(Long lessonId, AssignmentDto assignmentDto, String teacherEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

        if (!lesson.getCourse().getTeacher().getEmail().equals(teacherEmail)) {
            throw new SecurityException("You are not the owner of this course");
        }

        Assignment assignment = Assignment.builder()
                .title(assignmentDto.getTitle())
                .description(assignmentDto.getDescription())
                .lesson(lesson)
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);
        return mapToDto(savedAssignment);
    }

    @Override
    @Transactional
    public AssignmentDto updateAssignment(Long assignmentId, AssignmentDto assignmentDto, String teacherEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        if (!assignment.getLesson().getCourse().getTeacher().getEmail().equals(teacherEmail)) {
            throw new SecurityException("You are not the owner of this course");
        }

        assignment.setTitle(assignmentDto.getTitle());
        assignment.setDescription(assignmentDto.getDescription());

        Assignment updatedAssignment = assignmentRepository.save(assignment);
        return mapToDto(updatedAssignment);
    }

    @Override
    @Transactional
    public void deleteAssignment(Long assignmentId, String teacherEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        if (!assignment.getLesson().getCourse().getTeacher().getEmail().equals(teacherEmail)) {
            throw new SecurityException("You are not the owner of this course");
        }

        // Faylni o'chirish
        List<AssignmentSubmission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        for (AssignmentSubmission submission : submissions) {
            if (submission.getFileUrl() != null && !submission.getFileUrl().isEmpty()) {
                fileStorageService.deleteFile(submission.getFileUrl());
            }
        }

        assignmentRepository.delete(assignment);
    }

    @Override
    public AssignmentDto getAssignmentByLessonId(Long lessonId) {
        Assignment assignment = assignmentRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found for this lesson"));
        return mapToDto(assignment);
    }

    @Override
    public AssignmentSubmissionDto getMySubmission(Long assignmentId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        AssignmentSubmission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, student.getId())
                .orElseThrow(() -> new EntityNotFoundException("You have not submitted a solution for this assignment yet."));
        
        return mapToSubmissionDto(submission);
    }

    @Override
    @Transactional
    public void submitAssignment(Long assignmentId, SubmitAssignmentRequest request, MultipartFile file, String studentEmail) throws IOException {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        Long courseId = assignment.getLesson().getCourse().getId();
        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new SecurityException("You are not enrolled in this course");
        }

        Optional<AssignmentSubmission> existingSubmissionOpt = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, student.getId());

        AssignmentSubmission submission;
        if (existingSubmissionOpt.isPresent()) {
            submission = existingSubmissionOpt.get();
            if (submission.getStatus() != SubmissionStatus.RESUBMIT) {
                throw new IllegalStateException("You can only resubmit if the teacher requested changes.");
            }
            // Eski faylni o'chirish
            if (submission.getFileUrl() != null && !submission.getFileUrl().isEmpty()) {
                fileStorageService.deleteFile(submission.getFileUrl());
            }
        } else {
            submission = new AssignmentSubmission();
            submission.setAssignment(assignment);
            submission.setStudent(student);
        }

        submission.setSubmissionContent(request.getSubmissionContent());
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(SubmissionStatus.PENDING);

        if (file != null && !file.isEmpty()) {
            // XATOLIK TUZATILDI: storeFile -> uploadFile
            String fileUrl = fileStorageService.uploadFile(file);
            submission.setFileUrl(fileUrl);
        } else {
            submission.setFileUrl(null); // Agar yangi fayl yuklanmasa, eski URLni tozalash
        }

        submissionRepository.save(submission);
    }

    @Override
    @Transactional
    public void gradeAssignment(Long submissionId, GradeAssignmentRequest request, String teacherEmail) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));

        if (!submission.getAssignment().getLesson().getCourse().getTeacher().getEmail().equals(teacherEmail)) {
            throw new SecurityException("You are not the owner of this course");
        }

        submission.setGrade(request.getGrade());
        submission.setTeacherFeedback(request.getFeedback());
        submission.setStatus(request.getStatus());

        submissionRepository.save(submission);
    }

    @Override
    public List<AssignmentSubmissionDto> getAssignmentSubmissions(Long assignmentId, String teacherEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        if (!assignment.getLesson().getCourse().getTeacher().getEmail().equals(teacherEmail)) {
            throw new SecurityException("You are not the owner of this course");
        }

        List<AssignmentSubmission> submissions = submissionRepository.findByAssignmentId(assignmentId);

        return submissions.stream()
                .map(this::mapToSubmissionDto)
                .collect(Collectors.toList());
    }

    private AssignmentDto mapToDto(Assignment assignment) {
        return AssignmentDto.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .lessonId(assignment.getLesson().getId())
                .build();
    }

    private AssignmentSubmissionDto mapToSubmissionDto(AssignmentSubmission submission) {
        return AssignmentSubmissionDto.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignment().getId())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getFirstName() + " " + submission.getStudent().getLastName())
                .submissionContent(submission.getSubmissionContent())
                .fileUrl(submission.getFileUrl())
                .grade(submission.getGrade())
                .teacherFeedback(submission.getTeacherFeedback())
                .status(submission.getStatus())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
}
