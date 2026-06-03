package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.*;
import bunyodbek.uz.moreeduce.service.StudentService;
import bunyodbek.uz.moreeduce.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
@Tag(name = "Student Panel", description = "Talaba uchun shaxsiy kabinet API'lari")
public class StudentController {

    private final StudentService studentService;
    private final UserProfileService userProfileService;

    @Operation(summary = "O'z profil ma'lumotlarini olish")
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getMyProfile(Principal principal) {
        return ResponseEntity.ok(userProfileService.getMyProfile(principal.getName()));
    }

    @Operation(summary = "O'z profil ma'lumotlarini yangilash")
    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateMyProfile(@RequestBody UpdateProfileRequest request, Principal principal) {
        // XATO TUZATILDI: Servisdagi to'g'ri metod chaqirildi
        return ResponseEntity.ok(userProfileService.updateMyProfile(principal.getName(), request));
    }

    @Operation(summary = "Parolni o'zgartirish")
    @PostMapping("/profile/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request, Principal principal) {
        // XATO TUZATILDI: UserService o'rniga UserProfileService ishlatildi
        userProfileService.changeMyPassword(principal.getName(), request);
        return ResponseEntity.ok("Password changed successfully.");
    }

    @Operation(summary = "O'zi a'zo bo'lgan kurslar ro'yxatini olish")
    @GetMapping("/courses")
    public ResponseEntity<List<CourseDto>> getMyCourses(Principal principal) {
        return ResponseEntity.ok(studentService.getMyCourses(principal.getName()));
    }

    @Operation(summary = "Kursga tegishli darslar ro'yxatini olish")
    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<List<LessonDto>> getCourseLessons(@PathVariable Long courseId, Principal principal) {
        return ResponseEntity.ok(studentService.getCourseLessons(courseId, principal.getName()));
    }

    @Operation(summary = "Darsni 'tugatildi' deb belgilash")
    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<String> completeLesson(@PathVariable Long lessonId, Principal principal) {
        studentService.completeLesson(lessonId, principal.getName());
        return ResponseEntity.ok("Lesson completed successfully");
    }

    @Operation(summary = "Video darslikda to'xtagan joyni saqlash")
    @PostMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<String> updateLessonProgress(
            @PathVariable Long lessonId,
            @RequestBody UpdateProgressRequest request,
            Principal principal) {
        studentService.updateLessonProgress(lessonId, request.getPosition(), principal.getName());
        return ResponseEntity.ok("Lesson progress updated successfully");
    }
}
