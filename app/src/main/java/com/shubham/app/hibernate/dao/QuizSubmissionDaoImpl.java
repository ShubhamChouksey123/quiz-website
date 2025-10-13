package com.shubham.app.hibernate.dao;

import com.shubham.app.entity.QuizSubmission;
import com.shubham.app.entity.QuizSubmission_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public List<QuizSubmission> getTopPerformers(Integer totalResults) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<QuizSubmission> query = cb.createQuery(QuizSubmission.class);
        Root<QuizSubmission> root = query.from(QuizSubmission.class);

        query.select(root);

        query.orderBy(cb.desc(root.get(QuizSubmission_.score)));

        return em.createQuery(query).setFirstResult(0).setMaxResults(totalResults).getResultList();
    }
}
