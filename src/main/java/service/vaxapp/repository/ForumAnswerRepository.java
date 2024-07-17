package service.vaxapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import service.vaxapp.model.ForumAnswer;

import java.util.List;

@Repository
public interface ForumAnswerRepository extends JpaRepository<ForumAnswer, Integer> {
    @Query(value = "SELECT * FROM forum_answer WHERE forum_question_id=:forumQuestionId", nativeQuery = true)
    List<ForumAnswer> findForumAnswersBy(Integer forumQuestionId);
}
