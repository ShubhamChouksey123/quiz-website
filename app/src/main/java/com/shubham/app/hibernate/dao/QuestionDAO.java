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

    public Question questionById(Long questionId) {

        Session session = this.sessionFactory.getCurrentSession();

        Question question = null;
        try {
            question = session.get(Question.class, questionId);
        } catch (Exception e) {
            logger.error("Question with {} doesn't exist !", questionId);
        }
        return question;
    }

    public List<Question> getAllQuestion() {

        Session session = this.sessionFactory.getCurrentSession();
        List<Question> questionList = new ArrayList<>();
        String sql = "from question q order by questionId asc";

        try {
            questionList = (List<Question>) session.createQuery(sql).getResultList();
            return questionList;
        } catch (Exception e) {
            logger.error("No Question exist !");
        }
        return questionList;
    }

    public List<Question> getQuestion(Integer n) {

        Session session = this.sessionFactory.getCurrentSession();
        List<Question> questionList = new ArrayList<>();
        String sql = "from question q order by RAND()";

        try {
            questionList = (List<Question>) session.createQuery(sql).setMaxResults(n).getResultList();
            return questionList;
        } catch (Exception e) {
            logger.error("No Question exist !");
        }
        return questionList;
    }

    public boolean deleteQuestion(Long questionId) {

        Session session = this.sessionFactory.getCurrentSession();

        Question question = null;
        try {
            question = session.get(Question.class, questionId);
            session.delete(question);
            return true;
        } catch (Exception e) {
            logger.error("Question with {} doesn't exist !", questionId);
        }
        return false;
    }

    public Integer getAnswerOfAQuestion(Long questionId) {
        return questionById(questionId).getAns();
    }


}
