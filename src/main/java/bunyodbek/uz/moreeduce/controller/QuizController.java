package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.QuizDto;
import bunyodbek.uz.moreeduce.dto.QuizResultDto;
import bunyodbek.uz.moreeduce.dto.QuizSubmissionDto;
import bunyodbek.uz.moreeduce.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quiz Management", description = "Testlar bilan ishlash uchun API")
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "Dars uchun test yaratish (O'qituvchi)")
    @PostMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<QuizDto> createQuizForLesson(@PathVariable Long lessonId, @RequestBody QuizDto quizDto, Principal principal) {
        return ResponseEntity.ok(quizService.createQuizForLesson(lessonId, quizDto, principal.getName()));
    }

    @Operation(summary = "Modul uchun test yaratish (O'qituvchi)")
    @PostMapping("/module/{moduleId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<QuizDto> createQuizForModule(@PathVariable Long moduleId, @RequestBody QuizDto quizDto, Principal principal) {
        return ResponseEntity.ok(quizService.createQuizForModule(moduleId, quizDto, principal.getName()));
    }

    @Operation(summary = "Testni yangilash (O'qituvchi)")
    @PutMapping("/{quizId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<QuizDto> updateQuiz(@PathVariable Long quizId, @RequestBody QuizDto quizDto, Principal principal) {
        return ResponseEntity.ok(quizService.updateQuiz(quizId, quizDto, principal.getName()));
    }

    @Operation(summary = "Testni o'chirish (O'qituvchi)")
    @DeleteMapping("/{quizId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId, Principal principal) {
        quizService.deleteQuiz(quizId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Darsning testini olish")
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<QuizDto> getQuizByLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(quizService.getQuizByLessonId(lessonId));
    }

    @Operation(summary = "Modulning testini olish")
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<QuizDto> getQuizByModule(@PathVariable Long moduleId) {
        return ResponseEntity.ok(quizService.getQuizByModuleId(moduleId));
    }

    @Operation(summary = "Testni ID bo'yicha olish (savollarni aralashtirish bilan)")
    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDto> getQuizById(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizForStudent(quizId));
    }

    @Operation(summary = "Testni topshirish va natijani olish (Talaba)")
    @PostMapping("/{quizId}/submit")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<QuizResultDto> submitQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizSubmissionDto submissionDto,
            Principal principal) {
        QuizResultDto result = quizService.submitAndSaveQuiz(quizId, submissionDto, principal.getName());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Mening test natijalarimni olish (Talaba)")
    @GetMapping("/results/my-results")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<List<QuizResultDto>> getMyResults(Principal principal) {
        return ResponseEntity.ok(quizService.getMyResults(principal.getName()));
    }

    @Operation(summary = "Test natijalarini ko'rish (O'qituvchi/Admin)")
    @GetMapping("/{quizId}/results")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'ADMIN')")
    public ResponseEntity<Page<QuizResultDto>> getResultsByQuiz(
            @PathVariable Long quizId,
            @Parameter(hidden = true) Pageable pageable,
            Principal principal) {
        return ResponseEntity.ok(quizService.getResultsByQuiz(quizId, pageable, principal));
    }
}
