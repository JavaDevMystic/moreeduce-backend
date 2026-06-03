package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.CommentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface CommentService {

    Page<CommentDto> getCommentsByLessonId(Long lessonId, Principal principal, Pageable pageable);

    CommentDto addComment(Long lessonId, CommentDto commentDto, Principal principal);

    CommentDto addReplyToComment(Long parentCommentId, CommentDto commentDto, Principal principal);

    CommentDto updateComment(Long commentId, CommentDto commentDto, Principal principal);

    void deleteComment(Long commentId, Principal principal);

    void toggleLike(Long commentId, Principal principal);
}
