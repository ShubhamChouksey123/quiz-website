package com.shubham.app.hibernate.dao;

import com.shubham.app.entity.Question;
import com.shubham.app.entity.Question_;
import com.shubham.app.model.ApprovalLevel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class QuestionDAOImpl implements QuestionDAO {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    public void saveQuestion(Question question) {
        em.merge(question);
    }

    public Question questionById(Long questionId) {

        Question question = null;
        try {
            question = em.find(Question.class, questionId);
        } catch (Exception e) {
            logger.error("Question with {} doesn't exist !", questionId);
        }
        return question;
    }

    @Override
    public List<Question> getAllQuestion(ApprovalLevel approvalLevel) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Question> query = cb.createQuery(Question.class);
        Root<Question> root = query.from(Question.class);

        query.select(root);

        if (approvalLevel != null) {
            Predicate predicate = cb.equal(root.get(Question_.approvalLevel), approvalLevel);
            query.where(predicate);
        }

        List<Question> questionList = new ArrayList<>();
        try {
            questionList = em.createQuery(query).getResultList();
        } catch (NoResultException e) {
            logger.warn("Couldn't find questions with approval level : {}", approvalLevel);
        } catch (Exception e) {
            logger.error("Couldn't find a questions with cause : {}", e.getMessage());
        }

        return questionList;
    }

    @Override
    public List<Question> getQuestion(Integer n) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Question> query = cb.createQuery(Question.class);
        Root<Question> root = query.from(Question.class);

        query.select(root);
        logger.info("fetching n : {} questions ", n);

        List<Question> questionList = new ArrayList<>();
        try {
            questionList = em.createQuery(query).setMaxResults(n).getResultList();
        } catch (Exception e) {
            logger.warn("Couldn't find a suitable claim : {}", e.getMessage());
        }

        return questionList;
    }

    @Override
    public boolean deleteQuestion(Long questionId) {

        try {
            Question question = em.find(Question.class, questionId);
            if (question != null) {
                logger.info("Deleting question with ID: {}", questionId);
                em.remove(question);
                em.flush(); // Ensure the deletion is executed immediately
                logger.info("Successfully deleted question with ID: {}", questionId);
                return true;
            } else {
                logger.warn("Question with ID {} not found for deletion", questionId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting question with ID {}: {}", questionId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Integer getAnswerOfAQuestion(Long questionId) {
        return questionById(questionId).getAns();
    }

    @Override
    public List<Question> getAnswerOfQuestions(List<Integer> questionIds) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Question> query = cb.createQuery(Question.class);
        Root<Question> root = query.from(Question.class);

        query.select(root);

        Predicate predicate = root.get(Question_.QUESTION_ID).in(questionIds);
        query.where(predicate);

        return em.createQuery(query).getResultList();
    }
}
