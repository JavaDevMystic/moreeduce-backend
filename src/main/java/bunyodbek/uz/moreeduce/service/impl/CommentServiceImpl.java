package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.CommentDto;
import bunyodbek.uz.moreeduce.entity.*;
import bunyodbek.uz.moreeduce.repository.*;
import bunyodbek.uz.moreeduce.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Override
    public Page<CommentDto> getCommentsByLessonId(Long lessonId, Principal principal, Pageable pageable) {
        User user = getUserByPrincipal(principal);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + lessonId));

        // Xavfsizlik: Faqat kursga a'zo bo'lganlar izohlarni ko'ra oladi
        if (!enrollmentRepository.existsByStudentIdAndCourseId(user.getId(), lesson.getCourse().getId())) {
            throw new AccessDeniedException("You are not enrolled in this course.");
        }

        Page<Comment> commentsPage = commentRepository.findByLessonIdAndParentCommentIsNull(lessonId, pageable);
        return commentsPage.map(comment -> mapToDto(comment, user.getId()));
    }

    @Override
    @Transactional
    public CommentDto addComment(Long lessonId, CommentDto commentDto, Principal principal) {
        User user = getUserByPrincipal(principal);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + lessonId));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(user.getId(), lesson.getCourse().getId())) {
            throw new AccessDeniedException("You must be enrolled in the course to comment.");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .lesson(lesson)
                .user(user)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return mapToDto(savedComment, user.getId());
    }

    @Override
    @Transactional
    public CommentDto addReplyToComment(Long parentCommentId, CommentDto commentDto, Principal principal) {
        User user = getUserByPrincipal(principal);
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new EntityNotFoundException("Parent comment not found with id: " + parentCommentId));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(user.getId(), parentComment.getLesson().getCourse().getId())) {
            throw new AccessDeniedException("You must be enrolled in the course to reply.");
        }

        Comment reply = Comment.builder()
                .text(commentDto.getText())
                .lesson(parentComment.getLesson())
                .user(user)
                .parentComment(parentComment)
                .build();

        Comment savedReply = commentRepository.save(reply);
        return mapToDto(savedReply, user.getId());
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long commentId, CommentDto commentDto, Principal principal) {
        User user = getUserByPrincipal(principal);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only edit your own comments.");
        }

        comment.setText(commentDto.getText());
        comment.setEdited(true);
        Comment updatedComment = commentRepository.save(comment);
        return mapToDto(updatedComment, user.getId());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Principal principal) {
        User user = getUserByPrincipal(principal);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));

        boolean isOwner = comment.getUser().getId().equals(user.getId());
        boolean isTeacher = comment.getLesson().getCourse().getTeacher().getId().equals(user.getId());

        if (!isOwner && !isTeacher) {
            throw new AccessDeniedException("You do not have permission to delete this comment.");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void toggleLike(Long commentId, Principal principal) {
        User user = getUserByPrincipal(principal);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));

        commentLikeRepository.findByCommentIdAndUserId(commentId, user.getId())
                .ifPresentOrElse(
                        like -> { // Agar like mavjud bo'lsa (unlike qilish)
                            commentLikeRepository.delete(like);
                            comment.setLikesCount(comment.getLikesCount() - 1);
                        },
                        () -> { // Agar like mavjud bo'lmasa (like qilish)
                            CommentLike newLike = CommentLike.builder().comment(comment).user(user).build();
                            commentLikeRepository.save(newLike);
                            comment.setLikesCount(comment.getLikesCount() + 1);
                        }
                );
        commentRepository.save(comment);
    }

    private User getUserByPrincipal(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found. Please log in again."));
    }

    private CommentDto mapToDto(Comment comment, Long currentUserId) {
        List<CommentDto> replies = comment.getReplies() != null ?
                comment.getReplies().stream()
                        .map(reply -> mapToDto(reply, currentUserId))
                        .collect(Collectors.toList())
                : Collections.emptyList();

        boolean isLiked = commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), currentUserId);

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName())
                .lessonId(comment.getLesson().getId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .replies(replies)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isEdited(comment.isEdited())
                .likesCount(comment.getLikesCount())
                .isLikedByCurrentUser(isLiked)
                .build();
    }
}
