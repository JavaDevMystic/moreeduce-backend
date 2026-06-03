package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.*;
import bunyodbek.uz.moreeduce.entity.*;
import bunyodbek.uz.moreeduce.repository.*;
import bunyodbek.uz.moreeduce.service.AiProcessingService;
import bunyodbek.uz.moreeduce.service.ReflectionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReflectionServiceImpl implements ReflectionService {

    private final ReflectionRepository reflectionRepository;
    private final ReflectionSubmissionRepository submissionRepository;
    private final ReflectionCriterionRepository criterionRepository;
    private final ReflectionCriterionResultRepository criterionResultRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AiProcessingService aiProcessingService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ReflectionDto createReflectionForLesson(Long lessonId, ReflectionDto reflectionDto, String teacherEmail) {
        Lesson lesson = getLessonAndVerifyOwner(lessonId, teacherEmail);

        Reflection reflection = Reflection.builder()
                .title(reflectionDto.getTitle())
                .lesson(lesson)
                .question1(reflectionDto.getQuestion1())
                .question2(reflectionDto.getQuestion2())
                .question3(reflectionDto.getQuestion3())
                .question4(reflectionDto.getQuestion4())
                .build();
        
        List<ReflectionCriterion> criteria = buildCriteria(reflectionDto.getCriteria(), reflection);
        reflection.setCriteria(criteria);

        Reflection savedReflection = reflectionRepository.save(reflection);
        return mapToReflectionDto(savedReflection);
    }

    @Override
    @Transactional
    public ReflectionDto updateReflection(Long reflectionId, ReflectionDto reflectionDto, String teacherEmail) {
        Reflection reflection = getReflectionAndVerifyOwner(reflectionId, teacherEmail);

        reflection.setTitle(reflectionDto.getTitle());
        reflection.setQuestion1(reflectionDto.getQuestion1());
        reflection.setQuestion2(reflectionDto.getQuestion2());
        reflection.setQuestion3(reflectionDto.getQuestion3());
        reflection.setQuestion4(reflectionDto.getQuestion4());

        // Mezonlarni yangilash
        reflection.getCriteria().clear();
        reflection.getCriteria().addAll(buildCriteria(reflectionDto.getCriteria(), reflection));

        Reflection updatedReflection = reflectionRepository.save(reflection);
        return mapToReflectionDto(updatedReflection);
    }

    @Override
    @Transactional
    public void deleteReflection(Long reflectionId, String teacherEmail) {
        Reflection reflection = getReflectionAndVerifyOwner(reflectionId, teacherEmail);
        reflectionRepository.delete(reflection);
    }

    @Override
    public ReflectionDto getReflectionByLessonId(Long lessonId) {
        Reflection reflection = reflectionRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Reflection not found for this lesson"));
        return mapToReflectionDto(reflection);
    }

    @Override
    @Transactional
    public ReflectionSubmissionDto submitReflection(Long reflectionId, ReflectionSubmissionDto submissionDto, String studentEmail) {
        User student = getUserByEmail(studentEmail);
        Reflection reflection = reflectionRepository.findById(reflectionId)
                .orElseThrow(() -> new EntityNotFoundException("Reflection not found"));

        validateEnrollment(student.getId(), reflection.getLesson().getCourse().getId());

        ReflectionSubmission submission = ReflectionSubmission.builder()
                .student(student)
                .reflection(reflection)
                .answer1(submissionDto.getAnswer1())
                .answer2(submissionDto.getAnswer2())
                .answer3(submissionDto.getAnswer3())
                .answer4(submissionDto.getAnswer4())
                .selfScore(submissionDto.getSelfScore())
                .status(SubmissionStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();

        ReflectionSubmission savedSubmission = submissionRepository.save(submission);

        // AI tahlilini tranzaksiya tugaganidan keyin boshlash
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                aiProcessingService.processReflectionWithCriteria(savedSubmission.getId());
            }
        });

        return mapToSubmissionDto(savedSubmission);
    }
    
    @Transactional
    public void processAndSaveAiAssessment(Long submissionId, String aiJsonResponse) {
        ReflectionSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + submissionId));

        try {
            AiReflectionResponse response = objectMapper.readValue(aiJsonResponse, AiReflectionResponse.class);
            submission.setGeneralAiFeedback(response.getGeneralFeedback());

            Map<String, ReflectionCriterion> criteriaMap = submission.getReflection().getCriteria().stream()
                .collect(Collectors.toMap(ReflectionCriterion::getCriterion, c -> c));

            List<ReflectionCriterionResult> results = response.getScores().stream()
                .map(score -> {
                    ReflectionCriterion criterion = criteriaMap.get(score.getCriterion());
                    if (criterion == null) {
                        log.warn("AI returned score for an unknown criterion: {}", score.getCriterion());
                        return null;
                    }
                    return ReflectionCriterionResult.builder()
                        .submission(submission)
                        .criterion(criterion)
                        .score(score.getScore())
                        .build();
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
            
            criterionResultRepository.saveAll(results);
            submission.setCriterionResults(results);
            submission.calculateAndSetReflectionIndex();
            
            submissionRepository.save(submission);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response for submission {}", submissionId, e);
            submission.setGeneralAiFeedback("AI javobini tahlil qilishda xatolik yuz berdi.");
            submission.setStatus(SubmissionStatus.ERROR);
            submissionRepository.save(submission);
        }
    }

    @Override
    @Transactional
    public ReflectionSubmissionDto gradeSubmission(Long submissionId, Double teacherScore, String teacherEmail) {
        ReflectionSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));

        getReflectionAndVerifyOwner(submission.getReflection().getId(), teacherEmail);

        submission.setTeacherScore(teacherScore);
        submission.calculateAndSetReflectionIndex();

        return mapToSubmissionDto(submissionRepository.save(submission));
    }

    @Override
    public ReflectionSubmissionDto getMyResultForLesson(Long lessonId, String studentEmail) {
        User student = getUserByEmail(studentEmail);
        ReflectionSubmission submission = submissionRepository
                .findFirstByReflectionLessonIdAndStudentIdOrderBySubmittedAtDesc(lessonId, student.getId())
                .orElseThrow(() -> new EntityNotFoundException("Submission not found for this lesson"));
        return mapToSubmissionDto(submission);
    }

    // Helper va Mapper metodlar
    
    private ReflectionDto mapToReflectionDto(Reflection reflection) {
        List<ReflectionCriterionDto> criteriaDtos = (reflection.getCriteria() != null)
            ? reflection.getCriteria().stream().map(this::mapToCriterionDto).collect(Collectors.toList())
            : Collections.emptyList();

        return ReflectionDto.builder()
                .id(reflection.getId())
                .title(reflection.getTitle())
                .lessonId(reflection.getLesson().getId())
                .question1(reflection.getQuestion1())
                .question2(reflection.getQuestion2())
                .question3(reflection.getQuestion3())
                .question4(reflection.getQuestion4())
                .criteria(criteriaDtos)
                .build();
    }

    private ReflectionSubmissionDto mapToSubmissionDto(ReflectionSubmission submission) {
        List<ReflectionCriterionResultDto> criterionResultDtos = (submission.getCriterionResults() != null)
            ? submission.getCriterionResults().stream().map(this::mapToCriterionResultDto).collect(Collectors.toList())
            : Collections.emptyList();
        
        Integer aiTotalScore = (submission.getCriterionResults() != null)
            ? submission.getCriterionResults().stream().mapToInt(ReflectionCriterionResult::getScore).sum()
            : null;

        return ReflectionSubmissionDto.builder()
                .id(submission.getId())
                .studentId(submission.getStudent().getId())
                .reflectionId(submission.getReflection().getId())
                .answer1(submission.getAnswer1())
                .answer2(submission.getAnswer2())
                .answer3(submission.getAnswer3())
                .answer4(submission.getAnswer4())
                .selfScore(submission.getSelfScore())
                .teacherScore(submission.getTeacherScore())
                .reflectionIndex(submission.getReflectionIndex())
                .aiTotalScore(aiTotalScore)
                .criterionResults(criterionResultDtos)
                .generalAiFeedback(submission.getGeneralAiFeedback())
                .status(submission.getStatus())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
    
    private ReflectionCriterionDto mapToCriterionDto(ReflectionCriterion criterion) {
        return ReflectionCriterionDto.builder()
            .id(criterion.getId())
            .criterion(criterion.getCriterion())
            .points(criterion.getPoints())
            .build();
    }

    private ReflectionCriterionResultDto mapToCriterionResultDto(ReflectionCriterionResult result) {
        return ReflectionCriterionResultDto.builder()
            .criterion(result.getCriterion().getCriterion())
            .score(result.getScore())
            .maxPoints(result.getCriterion().getPoints())
            .build();
    }

    private List<ReflectionCriterion> buildCriteria(List<ReflectionCriterionDto> dtos, Reflection reflection) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream()
            .map(dto -> ReflectionCriterion.builder()
                .criterion(dto.getCriterion())
                .points(dto.getPoints())
                .reflection(reflection)
                .build())
            .collect(Collectors.toList());
    }
    
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
    }

    private Lesson getLessonAndVerifyOwner(Long lessonId, String teacherEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));
        if (!lesson.getCourse().getTeacher().getEmail().equals(teacherEmail)) {
            throw new SecurityException("You are not the owner of this course");
        }
        return lesson;
    }

    private Reflection getReflectionAndVerifyOwner(Long reflectionId, String teacherEmail) {
        Reflection reflection = reflectionRepository.findById(reflectionId)
                .orElseThrow(() -> new EntityNotFoundException("Reflection not found"));
        if (!reflection.getLesson().getCourse().getTeacher().getEmail().equals(teacherEmail)) {
            throw new SecurityException("You are not the owner of this course");
        }
        return reflection;
    }

    private void validateEnrollment(Long studentId, Long courseId) {
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new SecurityException("You are not enrolled in this course");
        }
    }
}
