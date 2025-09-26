package com.shubham.app.hibernate.dao;

import com.shubham.app.entity.ContactQuery;
import java.util.List;

public interface ContactQueryDao {
    void saveContactQuery(ContactQuery contactQuery);

    ContactQuery getContactQueryById(Long contactQueryId);

    List<ContactQuery> getAllContactQuery();
}
