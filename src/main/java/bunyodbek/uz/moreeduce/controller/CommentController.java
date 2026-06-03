package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.CommentDto;
import bunyodbek.uz.moreeduce.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Izohlar bilan ishlash")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Dars uchun izohlarni paginatsiya bilan olish")
    @GetMapping("/lessons/{lessonId}")
    @PreAuthorize("isAuthenticated()") // Kursga a'zolik service'da tekshiriladi
    public ResponseEntity<Page<CommentDto>> getCommentsByLesson(
            @PathVariable Long lessonId,
            @Parameter(hidden = true) Pageable pageable,
            Principal principal) {
        Page<CommentDto> comments = commentService.getCommentsByLessonId(lessonId, principal, pageable);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "Darsga yangi izoh qo'shish")
    @PostMapping("/lessons/{lessonId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long lessonId,
            @RequestBody CommentDto commentDto,
            Principal principal) {
        CommentDto newComment = commentService.addComment(lessonId, commentDto, principal);
        return new ResponseEntity<>(newComment, HttpStatus.CREATED);
    }

    @Operation(summary = "Mavjud izohga javob yozish")
    @PostMapping("/{parentCommentId}/replies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDto> addReply(
            @PathVariable Long parentCommentId,
            @RequestBody CommentDto commentDto,
            Principal principal) {
        CommentDto newReply = commentService.addReplyToComment(parentCommentId, commentDto, principal);
        return new ResponseEntity<>(newReply, HttpStatus.CREATED);
    }

    @Operation(summary = "O'z izohini tahrirlash")
    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentDto commentDto,
            Principal principal) {
        CommentDto updatedComment = commentService.updateComment(commentId, commentDto, principal);
        return ResponseEntity.ok(updatedComment);
    }

    @Operation(summary = "Izohni o'chirish (izoh egasi yoki kurs o'qituvchisi)")
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, Principal principal) {
        commentService.deleteComment(commentId, principal);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Izohga 'like' bosish yoki qaytarib olish")
    @PostMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> toggleLike(@PathVariable Long commentId, Principal principal) {
        commentService.toggleLike(commentId, principal);
        return ResponseEntity.ok().build();
    }
}
