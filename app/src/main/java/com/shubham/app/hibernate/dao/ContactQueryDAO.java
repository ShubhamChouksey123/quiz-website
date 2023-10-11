package com.shubham.app.hibernate.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.shubham.app.entity.ContactQuery;

import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class ContactQueryDAO {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sf) {
        this.sessionFactory = sf;
    }

    public Long saveContactQuery(ContactQuery contactQuery) {

        Session session = this.sessionFactory.getCurrentSession();
        Long contactQueryId = (Long) session.save(contactQuery);

        return contactQueryId;
    }

    public ContactQuery getContactQueryById(Long contactQueryId) {

        Session session = this.sessionFactory.getCurrentSession();

        ContactQuery contactQuery = null;
        try {
            contactQuery = session.get(ContactQuery.class, contactQueryId);
        } catch (Exception e) {
            logger.error("Contact query with {} doesn't exist !", contactQueryId);
        }
        return contactQuery;
    }

    public List<ContactQuery> getAllContactQuery() {

        Session session = this.sessionFactory.getCurrentSession();
        List<ContactQuery> contactQueryList = new ArrayList<>();
        String sql = "from contact_query q order by timeStamp desc";

        try {
            contactQueryList = (List<ContactQuery>) session.createQuery(sql).getResultList();
            return contactQueryList;
        } catch (Exception e) {
            logger.error("No Contact query exist !");
        }
        return contactQueryList;
    }
}
