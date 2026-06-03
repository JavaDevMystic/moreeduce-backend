package bunyodbek.uz.moreeduce.dto;

import bunyodbek.uz.moreeduce.entity.SubmissionStatus;
import lombok.Data;

@Data
public class GradeAssignmentRequest {
    private Integer grade;
    private String feedback;
    private SubmissionStatus status; // GRADED yoki RESUBMIT
}
