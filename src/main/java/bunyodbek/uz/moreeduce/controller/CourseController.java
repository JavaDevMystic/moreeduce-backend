package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.*;
import bunyodbek.uz.moreeduce.entity.CourseStatus;
import bunyodbek.uz.moreeduce.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Course Management", description = "Kurslar bilan ishlash uchun API")
public class CourseController {

    private final CourseService courseService;

    // --- Kurslar uchun APIlar ---

    @Operation(summary = "Kurslarni qidirish, filtrlash va paginatsiya qilish (Hamma uchun ochiq)")
    @GetMapping
    public ResponseEntity<Page<CourseDto>> searchCourses(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String language,
            @Parameter(hidden = true) Pageable pageable) {
        
        CourseFilterDto filter = CourseFilterDto.builder()
                .query(query)
                .category(category)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .language(language)
                .build();

        return ResponseEntity.ok(courseService.searchCourses(filter, pageable));
    }

    @Operation(summary = "Kurs tafsilotlarini olish (Hamma uchun ochiq, tizimga kirganlar uchun shaxsiylashtirilgan)")
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDetailsDto> getCourseDetails(@PathVariable Long courseId, Principal principal) {
        String userEmail = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(courseService.getCourseDetails(courseId, userEmail));
    }

    @Operation(summary = "Yangi kurs yaratish (Faqat o'qituvchi)")
    @PostMapping
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<CourseDto> createCourse(@RequestBody CourseDto courseDto, Principal principal) {
        return new ResponseEntity<>(courseService.createCourse(courseDto, principal.getName()), HttpStatus.CREATED);
    }

    @Operation(summary = "Kursni yangilash (O'qituvchi)")
    @PutMapping("/{courseId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long courseId, @RequestBody CourseDto courseDto, Principal principal) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, courseDto, principal.getName()));
    }

    @Operation(summary = "Kurs holatini o'zgartirish (O'qituvchi)")
    @PutMapping("/{courseId}/status")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Void> updateCourseStatus(@PathVariable Long courseId, @RequestParam CourseStatus status, Principal principal) {
        courseService.updateCourseStatus(courseId, status, principal.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Kurs rasmini yuklash (O'qituvchi)")
    @PostMapping(value = "/{courseId}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<CourseDto> uploadThumbnail(@PathVariable Long courseId, @RequestParam("file") MultipartFile file, Principal principal) throws IOException {
        return ResponseEntity.ok(courseService.updateCourseThumbnail(courseId, file, principal.getName()));
    }

    @Operation(summary = "Kursni o'chirish (O'qituvchi)")
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId, Principal principal) {
        courseService.deleteCourse(courseId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "O'qituvchining o'z kurslarini olish (O'qituvchi)")
    @GetMapping("/my-courses")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<List<CourseDto>> getMyCourses(Principal principal) {
        return ResponseEntity.ok(courseService.getMyCourses(principal.getName()));
    }

    // --- Modullar uchun APIlar ---

    @Operation(summary = "Modul yaratish (O'qituvchi)")
    @PostMapping("/{courseId}/modules")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<ModuleDto> createModule(@PathVariable Long courseId, @RequestBody ModuleDto moduleDto, Principal principal) {
        return new ResponseEntity<>(courseService.createModule(courseId, moduleDto, principal.getName()), HttpStatus.CREATED);
    }

    @Operation(summary = "Modulni yangilash (O'qituvchi)")
    @PutMapping("/modules/{moduleId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<ModuleDto> updateModule(@PathVariable Long moduleId, @RequestBody ModuleDto moduleDto, Principal principal) {
        return ResponseEntity.ok(courseService.updateModule(moduleId, moduleDto, principal.getName()));
    }

    @Operation(summary = "Modullarning tartibini o'zgartirish (O'qituvchi)")
    @PostMapping("/modules/reorder")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Void> reorderModules(@RequestBody ReorderRequest reorderRequest, Principal principal) {
        courseService.reorderModules(reorderRequest.getItemIds(), principal.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Modulni o'chirish (O'qituvchi)")
    @DeleteMapping("/modules/{moduleId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Void> deleteModule(@PathVariable Long moduleId, Principal principal) {
        courseService.deleteModule(moduleId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    // --- Darslar uchun APIlar ---

    @Operation(summary = "Dars (Lesson) yaratish (faqat matnli ma'lumotlar) (O'qituvchi)")
    @PostMapping("/modules/{moduleId}/lessons")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<LessonDto> createLesson(@PathVariable Long moduleId, @RequestBody LessonDto lessonDto, Principal principal) {
        return new ResponseEntity<>(courseService.createLesson(moduleId, lessonDto, principal.getName()), HttpStatus.CREATED);
    }

    @Operation(summary = "Dars videosini yuklash (O'qituvchi)")
    @PostMapping(value = "/lessons/{lessonId}/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<LessonDto> uploadLessonVideo(@PathVariable Long lessonId, @RequestPart("videoFile") MultipartFile videoFile, Principal principal) throws IOException {
        return ResponseEntity.ok(courseService.uploadLessonVideo(lessonId, videoFile, principal.getName()));
    }
    
    @Operation(summary = "Darsga qo'shimcha fayllar yuklash (O'qituvchi)")
    @PostMapping(value = "/lessons/{lessonId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<LessonDto> uploadLessonAttachments(@PathVariable Long lessonId, @RequestPart("files") List<MultipartFile> files, Principal principal) throws IOException {
        return ResponseEntity.ok(courseService.uploadLessonAttachments(lessonId, files, principal.getName()));
    }

    @Operation(summary = "Darsga subtitr faylini yuklash (O'qituvchi)")
    @PostMapping(value = "/lessons/{lessonId}/subtitle", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<LessonDto> uploadLessonSubtitle(@PathVariable Long lessonId,
                                                          @RequestPart("file") MultipartFile subtitleFile,
                                                          @RequestPart("languageCode") String languageCode,
                                                          Principal principal) throws IOException {
        return ResponseEntity.ok(courseService.uploadLessonSubtitle(lessonId, subtitleFile, languageCode, principal.getName()));
    }

    @Operation(summary = "Darsni yangilash (O'qituvchi)")
    @PutMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<LessonDto> updateLesson(@PathVariable Long lessonId, @RequestBody LessonDto lessonDto, Principal principal) {
        return ResponseEntity.ok(courseService.updateLesson(lessonId, lessonDto, principal.getName()));
    }

    @Operation(summary = "Darslar tartibini o'zgartirish (O'qituvchi)")
    @PostMapping("/lessons/reorder")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Void> reorderLessons(@RequestBody ReorderRequest reorderRequest, Principal principal) {
        courseService.reorderLessons(reorderRequest.getItemIds(), principal.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Darsni o'chirish (O'qituvchi)")
    @DeleteMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long lessonId, Principal principal) {
        courseService.deleteLesson(lessonId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
