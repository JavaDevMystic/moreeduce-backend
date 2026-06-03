package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.AnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
}
