package com.shubham.app.hibernate.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.shubham.app.entity.HRInfo;
import com.shubham.app.entity.HRInfo_;

import java.math.BigInteger;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class MailInfoDaoImpl implements MailInfoDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @PersistenceContext
    EntityManager em;

    @Override
    public void saveOrUpdate(HRInfo resumeMailInfo) {
        try {
            em.merge(resumeMailInfo);
        } catch (Exception e) {
            logger.error("Unable to save or update resume-mail-info details with {}", resumeMailInfo);
        }
    }

    @Override
    public HRInfo getResumeMailInfoById(String id) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<HRInfo> query = cb.createQuery(HRInfo.class);
        Root<HRInfo> root = query.from(HRInfo.class);

        query.select(root);
        query.where(cb.equal(root.get(HRInfo_.hrId), id));

        return em.createQuery(query).getSingleResult();
    }

    @Override
    public List<HRInfo> getAllResumeMailInfo() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<HRInfo> query = cb.createQuery(HRInfo.class);
        Root<HRInfo> root = query.from(HRInfo.class);

        query.select(root);

        List<HRInfo> CustomerSatellite = null;
        try {
            CustomerSatellite = em.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.warn("Couldn't find a suitable resume-mail-info : {}", e.getMessage());
        }

        return CustomerSatellite;
    }

    @Override
    public List<HRInfo> getResumeMailInfo(BigInteger firstResult, BigInteger maxResults) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<HRInfo> query = cb.createQuery(HRInfo.class);
        Root<HRInfo> root = query.from(HRInfo.class);

        query.select(root);
        query.orderBy(cb.desc(root.get(HRInfo_.createdAt)));

        List<HRInfo> CustomerSatellite = null;
        try {
            CustomerSatellite = em.createQuery(query).setFirstResult(firstResult.intValue())
                    .setMaxResults(maxResults.intValue()).getResultList();

        } catch (Exception e) {
            logger.warn("Couldn't find a suitable launcher : {}", e.getMessage());
        }

        return CustomerSatellite;
    }

    @Override
    public List<HRInfo> getResumeMailInfo(BigInteger firstResult, BigInteger maxResults, String searchText) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<HRInfo> query = cb.createQuery(HRInfo.class);
        Root<HRInfo> root = query.from(HRInfo.class);

        query.select(root);

        Predicate searchPredicate = cb.or(
                cb.like(cb.lower(root.get(HRInfo_.hrId).as(String.class)), "%" + searchText + "%"),
                cb.like(cb.function("lower", String.class, root.get(HRInfo_.hrName).as(String.class)),
                        "%" + searchText + "%"),
                // cb.like(cb.function("lower", String.class,
                // root.get(HRInfo_.hrEmail).as(String.class)),
                // "%" + searchText + "%"),
                cb.like(cb.function("lower", String.class, root.get(HRInfo_.company).as(String.class)),
                        "%" + searchText + "%"),
                cb.like(cb.function("lower", String.class, root.get(HRInfo_.jobTitle).as(String.class)),
                        "%" + searchText + "%"),
                cb.like(cb.function("lower", String.class, root.get(HRInfo_.createdAt).as(String.class)),
                        "%" + searchText + "%"),
                cb.like(cb.lower(root.get(HRInfo_.advertisedOn)), "%" + searchText + "%"));

        query.where(searchPredicate);

        List<HRInfo> CustomerSatellite = null;
        try {
            CustomerSatellite = em.createQuery(query).setFirstResult(firstResult.intValue())
                    .setMaxResults(maxResults.intValue()).getResultList();

        } catch (Exception e) {
            logger.warn("Couldn't find a suitable launcher : {}", e.getMessage());
        }

        return CustomerSatellite;
    }

    @Override
    public void deleteResumeMailInfo(String hrId) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaDelete<HRInfo> criteria = cb.createCriteriaDelete(HRInfo.class);
        Root<HRInfo> root = criteria.from(HRInfo.class);

        Predicate predicateForUserId = cb.equal(root.get(HRInfo_.hrId), hrId);
        criteria.where(predicateForUserId);

        // perform delete
        em.createQuery(criteria).executeUpdate();
    }
}
