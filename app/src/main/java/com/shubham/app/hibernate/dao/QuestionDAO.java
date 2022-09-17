package com.shubham.app.hibernate.dao;

import com.shubham.app.entity.Question;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Repository
@Transactional
public class QuestionDAO {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sf) {
        this.sessionFactory = sf;
    }

    public Long saveQuestion(Question question) {

        Session session = this.sessionFactory.getCurrentSession();
        Long requestId = (Long) session.save(question);

        return requestId;
    }

    public void deleteQuestion(Long questionId) {

        Session session = this.sessionFactory.getCurrentSession();

        Question question = null;
        try {
            question = session.get(Question.class, questionId);
            session.delete(question);
        } catch (Exception e) {
            logger.error("Question with {} doesn't exist !", questionId);
        }
    }

    public Integer getAnswerOfAQuestion(Long questionId) {

        Session session = this.sessionFactory.getCurrentSession();

        Question question = null;
        try {
            question = session.get(Question.class, questionId);
            return question.getAns();
        } catch (Exception e) {
            logger.error("Question with {} doesn't exist !", questionId);
        }
        return null;
    }

    public List<Question> getQuestion(Integer n) {

        Session session = this.sessionFactory.getCurrentSession();
        List<Question> questionList = new ArrayList<>();
        String sql = "from question q order by RAND()";

        try {
            questionList = (List<Question>) session.createQuery(sql).setMaxResults(n).getResultList();
            return questionList;
        } catch (Exception e) {

        }
        return questionList;
    }
}
