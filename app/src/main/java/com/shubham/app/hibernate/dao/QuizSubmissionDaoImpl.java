package com.shubham.app.hibernate.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.shubham.app.entity.QuizSubmission;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@Transactional
public class QuizSubmissionDaoImpl implements QuizSubmissionDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    public void save(QuizSubmission quizSubmission) {
        em.persist(quizSubmission);
    }
}
