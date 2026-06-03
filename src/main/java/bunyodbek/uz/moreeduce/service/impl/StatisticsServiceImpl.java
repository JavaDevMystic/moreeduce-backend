package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.GroupStatisticsDto;
import bunyodbek.uz.moreeduce.dto.ModuleResultDto;
import bunyodbek.uz.moreeduce.dto.StudentProgressDto;
import bunyodbek.uz.moreeduce.entity.*;
import bunyodbek.uz.moreeduce.repository.*;
import bunyodbek.uz.moreeduce.service.StatisticsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final ReflectionSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final QuizResultRepository quizResultRepository;
    private final AuditLogRepository auditLogRepository;
    private final EnrollmentRepository enrollmentRepository; // Xavfsizlik uchun

    @Override
    public StudentProgressDto getMyProgress(Principal principal) {
        User student = getUserByEmail(principal.getName());
        return getStudentProgress(student.getId());
    }

    @Override
    public StudentProgressDto getStudentProgressForTeacher(Long studentId, Principal principal) {
        User teacher = getUserByEmail(principal.getName());
        
        // XAVFSIZLIK TEKSHIRUVI: O'qituvchi faqat o'z kursiga a'zo talabani ko'ra oladi
        if (!enrollmentRepository.existsByTeacherIdAndStudentId(teacher.getId(), studentId)) {
            throw new AccessDeniedException("You can only view the progress of students enrolled in your courses.");
        }
        
        return getStudentProgress(studentId);
    }

    @Override
    public GroupStatisticsDto getGroupStatistics(Long courseId, Principal principal) {
        User teacher = getUserByEmail(principal.getName());
        Course course = submissionRepository.findById(courseId)
                .map(s -> s.getReflection().getLesson().getCourse())
                .orElseThrow(() -> new EntityNotFoundException("Course not found or no submissions yet."));

        if (!course.getTeacher().getId().equals(teacher.getId())) {
             throw new AccessDeniedException("You can only view statistics for your own courses.");
        }
        
        List<Double> scores = submissionRepository.findScoresByCourseId(courseId);

        if (scores.isEmpty()) {
            return GroupStatisticsDto.builder().courseId(courseId).totalStudents(0).build();
        }

        double mean = calculateMean(scores);
        double stdDev = calculateStandardDeviation(scores, mean);

        return GroupStatisticsDto.builder()
                .courseId(courseId)
                .totalStudents(scores.size())
                .meanScore(mean)
                .standardDeviation(stdDev)
                .minScore(scores.stream().min(Double::compare).orElse(0.0))
                .maxScore(scores.stream().max(Double::compare).orElse(0.0))
                .build();
    }

    // Bu metod private bo'lishi kerak, chunki u tashqaridan chaqirilganda xavfsizlikni tekshirmaydi
    private StudentProgressDto getStudentProgress(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        long totalActivityCount = auditLogRepository.countByPerformedBy(student.getEmail());
        String lastActiveAt = auditLogRepository.findFirstByPerformedByOrderByTimestampDesc(student.getEmail())
                .map(AuditLog::getTimestamp).map(Object::toString).orElse("N/A");

        List<ReflectionSubmission> submissions = submissionRepository.findByStudentId(studentId);
        List<QuizResult> allQuizResults = quizResultRepository.findByStudentId(studentId);

        Map<Long, Double> latestQuizScores = allQuizResults.stream()
                .filter(qr -> qr.getQuiz() != null && qr.getQuiz().getLesson() != null)
                .collect(Collectors.groupingBy(
                        qr -> qr.getQuiz().getLesson().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(QuizResult::getSubmittedAt)),
                                optionalQr -> optionalQr.map(QuizResult::getScorePercentage).orElse(0.0)
                        )
                ));

        double averageScore = submissions.stream()
                .mapToDouble(s -> s.getReflectionIndex() != null ? s.getReflectionIndex() : 0.0).average().orElse(0.0);

        Map<String, Double> progressHistory = new LinkedHashMap<>();
        submissions.stream()
                .sorted(Comparator.comparing(ReflectionSubmission::getSubmittedAt))
                .forEach(s -> progressHistory.put(s.getSubmittedAt().toString(), s.getReflectionIndex() != null ? s.getReflectionIndex() : 0.0));

        List<ModuleResultDto> moduleResults = submissions.stream()
                .map(s -> {
                    double quizScore = 0.0;
                    if (s.getReflection() != null && s.getReflection().getLesson() != null) {
                        quizScore = latestQuizScores.getOrDefault(s.getReflection().getLesson().getId(), 0.0);
                    }
                    return ModuleResultDto.builder()
                            .moduleTitle(s.getReflection().getTitle())
                            .reflectionScore(s.getReflectionIndex() != null ? s.getReflectionIndex() : 0.0)
                            .quizScore(quizScore)
                            .build();
                })
                .collect(Collectors.toList());

        return StudentProgressDto.builder()
                .studentId(student.getId())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .averageScore(averageScore)
                .totalActivityCount(totalActivityCount)
                .lastActiveAt(lastActiveAt)
                .progressHistory(progressHistory)
                .moduleResults(moduleResults)
                .build();
    }

    @Override
    public double calculateTTest(List<Double> groupA, List<Double> groupB) {
        if (groupA == null || groupB == null || groupA.size() < 2 || groupB.size() < 2) return 0.0;
        double meanA = calculateMean(groupA);
        double meanB = calculateMean(groupB);
        double varA = calculateVariance(groupA, meanA);
        double varB = calculateVariance(groupB, meanB);
        double denominator = Math.sqrt((varA / groupA.size()) + (varB / groupB.size()));
        return denominator == 0 ? 0.0 : (meanA - meanB) / denominator;
    }

    @Override
    public Map<String, Double> compareCourses(Long courseId1, Long courseId2) {
        List<Double> groupA_scores = submissionRepository.findAverageScoresPerStudentByCourseId(courseId1);
        List<Double> groupB_scores = submissionRepository.findAverageScoresPerStudentByCourseId(courseId2);
        double tTestResult = calculateTTest(groupA_scores, groupB_scores);
        Map<String, Double> result = new HashMap<>();
        result.put("t_statistic", tTestResult);
        result.put("groupA_size", (double) groupA_scores.size());
        result.put("groupB_size", (double) groupB_scores.size());
        return result;
    }

    private double calculateMean(List<Double> scores) {
        return scores.stream().mapToDouble(val -> val).average().orElse(0.0);
    }

    private double calculateVariance(List<Double> scores, double mean) {
        if (scores.size() <= 1) return 0.0;
        double temp = scores.stream().mapToDouble(a -> (a - mean) * (a - mean)).sum();
        return temp / (scores.size() - 1);
    }

    private double calculateStandardDeviation(List<Double> scores, double mean) {
        return Math.sqrt(calculateVariance(scores, mean));
    }
    
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
    }
}
