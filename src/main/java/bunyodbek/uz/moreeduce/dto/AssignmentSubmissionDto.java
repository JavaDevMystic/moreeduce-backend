package bunyodbek.uz.moreeduce.dto;

import bunyodbek.uz.moreeduce.entity.SubmissionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentSubmissionDto {
    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String studentName;
    private String submissionContent; // Talabaning javobi
    private String fileUrl; // Yuklangan fayl manzili
    private Integer grade; // Baho
    private String teacherFeedback; // O'qituvchi izohi
    private SubmissionStatus status; // Holati
    private LocalDateTime submittedAt;
}
