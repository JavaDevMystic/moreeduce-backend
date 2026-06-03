package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.ReflectionDto;
import bunyodbek.uz.moreeduce.dto.ReflectionSubmissionDto;

public interface ReflectionService {
    // O'qituvchi uchun
    ReflectionDto createReflectionForLesson(Long lessonId, ReflectionDto reflectionDto, String teacherEmail);
    ReflectionDto updateReflection(Long reflectionId, ReflectionDto reflectionDto, String teacherEmail);
    void deleteReflection(Long reflectionId, String teacherEmail);
    ReflectionSubmissionDto gradeSubmission(Long submissionId, Double teacherScore, String teacherEmail);

    // Talaba uchun
    ReflectionSubmissionDto submitReflection(Long reflectionId, ReflectionSubmissionDto submissionDto, String studentEmail);
    ReflectionSubmissionDto getMyResultForLesson(Long lessonId, String studentEmail);

    // Umumiy
    ReflectionDto getReflectionByLessonId(Long lessonId);
}
