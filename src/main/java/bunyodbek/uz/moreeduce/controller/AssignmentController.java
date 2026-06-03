package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.AssignmentDto;
import bunyodbek.uz.moreeduce.dto.AssignmentSubmissionDto;
import bunyodbek.uz.moreeduce.dto.GradeAssignmentRequest;
import bunyodbek.uz.moreeduce.dto.SubmitAssignmentRequest;
import bunyodbek.uz.moreeduce.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
@Tag(name = "Assignment Management", description = "Amaliy topshiriqlar bilan ishlash")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @Operation(summary = "Darsga amaliy topshiriq qo'shish (Faqat o'qituvchi)")
    @PostMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<AssignmentDto> createAssignment(
            @PathVariable Long lessonId,
            @RequestBody AssignmentDto assignmentDto,
            Principal principal) {
        return ResponseEntity.ok(assignmentService.createAssignment(lessonId, assignmentDto, principal.getName()));
    }

    @Operation(summary = "Topshiriqni yangilash (UPDATE)")
    @PutMapping("/{assignmentId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<AssignmentDto> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestBody AssignmentDto assignmentDto,
            Principal principal) {
        return ResponseEntity.ok(assignmentService.updateAssignment(assignmentId, assignmentDto, principal.getName()));
    }

    @Operation(summary = "Topshiriqni o'chirish (DELETE)")
    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long assignmentId,
            Principal principal) {
        assignmentService.deleteAssignment(assignmentId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Darsning amaliy topshirig'ini olish")
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<AssignmentDto> getAssignmentByLessonId(@PathVariable Long lessonId) {
        return ResponseEntity.ok(assignmentService.getAssignmentByLessonId(lessonId));
    }

    @Operation(summary = "Topshiriqqa yuborilgan o'z javobini olish (Talaba)")
    @GetMapping("/{assignmentId}/my-submission")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<AssignmentSubmissionDto> getMySubmission(
            @PathVariable Long assignmentId,
            Principal principal) {
        return ResponseEntity.ok(assignmentService.getMySubmission(assignmentId, principal.getName()));
    }

    @Operation(summary = "Topshiriq topshirish (Talaba)")
    @PostMapping(value = "/{assignmentId}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<Void> submitAssignment(
            @PathVariable Long assignmentId,
            @RequestPart("request") SubmitAssignmentRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Principal principal) throws IOException {
        assignmentService.submitAssignment(assignmentId, request, file, principal.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Topshiriqni baholash (O'qituvchi)")
    @PostMapping("/submissions/{submissionId}/grade")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Void> gradeAssignment(
            @PathVariable Long submissionId,
            @RequestBody GradeAssignmentRequest request,
            Principal principal) {
        assignmentService.gradeAssignment(submissionId, request, principal.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Topshiriqqa kelgan javoblarni ko'rish (O'qituvchi)")
    @GetMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<List<AssignmentSubmissionDto>> getAssignmentSubmissions(
            @PathVariable Long assignmentId,
            Principal principal) {
        return ResponseEntity.ok(assignmentService.getAssignmentSubmissions(assignmentId, principal.getName()));
    }
}
