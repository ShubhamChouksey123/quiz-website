package com.shubham.app.hibernate.dao;

import com.shubham.app.entity.ContactQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ContactQueryDaoImpl implements ContactQueryDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    public void saveContactQuery(ContactQuery contactQuery) {
        em.merge(contactQuery);
    }

    @Override
    public ContactQuery getContactQueryById(Long contactQueryId) {

        ContactQuery contactQuery = null;
        try {
            contactQuery = em.find(ContactQuery.class, contactQueryId);
        } catch (Exception e) {
            logger.error("No contact found with contactId : {} with the cause : {}", contactQueryId, e.getMessage());
        }
        return contactQuery;
    }

    @Override
    public List<ContactQuery> getAllContactQuery() {

        List<ContactQuery> contactQueryList = new ArrayList<>();
        String sql = "from contact_query q order by timeStamp desc";

        try {
            contactQueryList = (List<ContactQuery>) em.createQuery(sql).getResultList();
            return contactQueryList;
        } catch (Exception e) {
            logger.error("No Contact query exist !");
        }
        return contactQueryList;
    }
}
