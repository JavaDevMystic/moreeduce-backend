package bunyodbek.uz.moreeduce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Null bo'lgan maydonlarni JSON'da ko'rsatmaslik
public class CommentDto {
    private Long id;
    private String text;
    private Long userId;
    private String userName;
    private Long lessonId;
    private Long parentCommentId;
    private List<CommentDto> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isEdited;
    private int likesCount;
    private boolean isLikedByCurrentUser; // Joriy foydalanuvchi like bosganmi?
}
