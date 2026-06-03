package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
}
