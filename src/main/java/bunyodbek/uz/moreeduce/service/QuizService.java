package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.QuizDto;
import bunyodbek.uz.moreeduce.dto.QuizResultDto;
import bunyodbek.uz.moreeduce.dto.QuizSubmissionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;

public interface QuizService {
    // O'qituvchi uchun
    QuizDto createQuizForLesson(Long lessonId, QuizDto quizDto, String teacherEmail);
    QuizDto createQuizForModule(Long moduleId, QuizDto quizDto, String teacherEmail);
    QuizDto updateQuiz(Long quizId, QuizDto quizDto, String teacherEmail);
    void deleteQuiz(Long quizId, String teacherEmail);
    Page<QuizResultDto> getResultsByQuiz(Long quizId, Pageable pageable, Principal principal);

    // Talaba uchun
    QuizDto getQuizForStudent(Long quizId);
    QuizResultDto submitAndSaveQuiz(Long quizId, QuizSubmissionDto submissionDto, String studentEmail); // Qaytarish turi o'zgartirildi
    List<QuizResultDto> getMyResults(String studentEmail);

    // Umumiy
    QuizDto getQuizByLessonId(Long lessonId);
    QuizDto getQuizByModuleId(Long moduleId);
}
