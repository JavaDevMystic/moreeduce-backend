package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByLessonIdAndParentCommentIsNull(Long lessonId, Pageable pageable);
}
