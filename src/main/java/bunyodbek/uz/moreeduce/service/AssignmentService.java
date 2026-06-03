package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.AssignmentDto;
import bunyodbek.uz.moreeduce.dto.AssignmentSubmissionDto;
import bunyodbek.uz.moreeduce.dto.GradeAssignmentRequest;
import bunyodbek.uz.moreeduce.dto.SubmitAssignmentRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AssignmentService {
    
    // O'qituvchi: Darsga amaliy topshiriq qo'shish
    AssignmentDto createAssignment(Long lessonId, AssignmentDto assignmentDto, String teacherEmail);

    // O'qituvchi: Topshiriqni yangilash (UPDATE)
    AssignmentDto updateAssignment(Long assignmentId, AssignmentDto assignmentDto, String teacherEmail);

    // O'qituvchi: Topshiriqni o'chirish (DELETE)
    void deleteAssignment(Long assignmentId, String teacherEmail);

    // Talaba/O'qituvchi: Darsning amaliy topshirig'ini olish
    AssignmentDto getAssignmentByLessonId(Long lessonId);

    // Talaba: Topshiriqqa yuborilgan o'z javobini olish
    AssignmentSubmissionDto getMySubmission(Long assignmentId, String studentEmail);

    // Talaba: Topshiriq topshirish
    void submitAssignment(Long assignmentId, SubmitAssignmentRequest request, MultipartFile file, String studentEmail) throws IOException;

    // O'qituvchi: Topshiriqni baholash
    void gradeAssignment(Long submissionId, GradeAssignmentRequest request, String teacherEmail);

    // O'qituvchi: Topshiriqqa kelgan javoblarni ko'rish
    List<AssignmentSubmissionDto> getAssignmentSubmissions(Long assignmentId, String teacherEmail);
}
