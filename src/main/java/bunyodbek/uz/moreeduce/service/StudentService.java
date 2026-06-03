package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.CourseDto;
import bunyodbek.uz.moreeduce.dto.LessonDto;
import bunyodbek.uz.moreeduce.dto.SubmitAssignmentRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface StudentService {
    // Kursga yozilish (Chek bilan)
    void enrollCourse(Long courseId, MultipartFile receiptFile, String studentEmail) throws IOException;

    // Talabaning kurslarini olish
    List<CourseDto> getMyCourses(String studentEmail);

    // Kurs darslarini olish (faqat ruxsat etilganlarini)
    List<LessonDto> getCourseLessons(Long courseId, String studentEmail);

    // Darsni tugatish
    void completeLesson(Long lessonId, String studentEmail);

    // Dars progressini yangilash (videoni qayerda to'xtatgani)
    void updateLessonProgress(Long lessonId, Long position, String studentEmail);

    // Vazifa javobini yuborish
    void submitAssignment(Long assignmentId, SubmitAssignmentRequest request, String studentEmail);
}
