package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.*;
import bunyodbek.uz.moreeduce.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CourseService {

    // --- Kurslar ---
    Page<CourseDto> searchCourses(CourseFilterDto filter, Pageable pageable);
    CourseDetailsDto getCourseDetails(Long courseId, String userEmail);
    List<CourseDto> getMyCourses(String teacherEmail);
    CourseDto createCourse(CourseDto courseDto, String teacherEmail);
    CourseDto updateCourse(Long courseId, CourseDto courseDto, String teacherEmail);
    void updateCourseStatus(Long courseId, CourseStatus status, String teacherEmail);
    CourseDto updateCourseThumbnail(Long courseId, MultipartFile thumbnailFile, String teacherEmail) throws IOException;
    void deleteCourse(Long courseId, String teacherEmail);

    // --- Modullar ---
    ModuleDto createModule(Long courseId, ModuleDto moduleDto, String teacherEmail);
    ModuleDto updateModule(Long moduleId, ModuleDto moduleDto, String teacherEmail);
    void reorderModules(List<Long> moduleIds, String teacherEmail);
    void deleteModule(Long moduleId, String teacherEmail);

    // --- Darslar ---
    LessonDto createLesson(Long moduleId, LessonDto lessonDto, String teacherEmail);
    LessonDto updateLesson(Long lessonId, LessonDto lessonDto, String teacherEmail);
    LessonDto uploadLessonVideo(Long lessonId, MultipartFile videoFile, String teacherEmail) throws IOException;
    LessonDto uploadLessonAttachments(Long lessonId, List<MultipartFile> files, String teacherEmail) throws IOException;
    LessonDto uploadLessonSubtitle(Long lessonId, MultipartFile subtitleFile, String languageCode, String teacherEmail) throws IOException;
    void reorderLessons(List<Long> lessonIds, String teacherEmail);
    void deleteLesson(Long lessonId, String teacherEmail);
}
