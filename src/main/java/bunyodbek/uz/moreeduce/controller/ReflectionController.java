package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.ReflectionDto;
import bunyodbek.uz.moreeduce.dto.ReflectionSubmissionDto;
import bunyodbek.uz.moreeduce.service.ReflectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/reflections")
@RequiredArgsConstructor
@Tag(name = "Reflection Management", description = "Refleksiya va Gibrid Baholash API")
public class ReflectionController {

    private final ReflectionService reflectionService;

    @Operation(summary = "Dars uchun refleksiya yaratish (O'qituvchi)")
    @PostMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<ReflectionDto> createReflection(
            @PathVariable Long lessonId,
            @RequestBody ReflectionDto reflectionDto,
            Principal principal) {
        return ResponseEntity.ok(reflectionService.createReflectionForLesson(lessonId, reflectionDto, principal.getName()));
    }

    @Operation(summary = "Refleksiyani yangilash (O'qituvchi)")
    @PutMapping("/{reflectionId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<ReflectionDto> updateReflection(
            @PathVariable Long reflectionId,
            @RequestBody ReflectionDto reflectionDto,
            Principal principal) {
        return ResponseEntity.ok(reflectionService.updateReflection(reflectionId, reflectionDto, principal.getName()));
    }

    @Operation(summary = "Refleksiyani o'chirish (O'qituvchi)")
    @DeleteMapping("/{reflectionId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Void> deleteReflection(
            @PathVariable Long reflectionId,
            Principal principal) {
        reflectionService.deleteReflection(reflectionId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Darsning refleksiyasini olish")
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<ReflectionDto> getReflectionByLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(reflectionService.getReflectionByLessonId(lessonId));
    }

    @Operation(summary = "Refleksiyani topshirish (Talaba)")
    @PostMapping("/{reflectionId}/submit")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<ReflectionSubmissionDto> submitReflection(
            @PathVariable Long reflectionId,
            @RequestBody ReflectionSubmissionDto submissionDto,
            Principal principal) {
        return ResponseEntity.ok(reflectionService.submitReflection(reflectionId, submissionDto, principal.getName()));
    }

    @Operation(summary = "Refleksiyani baholash (O'qituvchi)")
    @PutMapping("/submissions/{submissionId}/grade")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<ReflectionSubmissionDto> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestParam Double score,
            Principal principal) {
        return ResponseEntity.ok(reflectionService.gradeSubmission(submissionId, score, principal.getName()));
    }

    @Operation(summary = "Talaba o'z natijasini ko'rishi")
    @GetMapping("/lesson/{lessonId}/my-result")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<ReflectionSubmissionDto> getMyResult(
            @PathVariable Long lessonId,
            Principal principal) {
        return ResponseEntity.ok(reflectionService.getMyResultForLesson(lessonId, principal.getName()));
    }
}
