package com.shubham.app.rizzle.dao;

import com.shubham.app.rizzle.entity.Post;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PostRepository {

    // List<User>
    @Autowired
    private EntityManager em;

    public void save(Post post) {
        em.merge(post);
    }

    // public void (Post post){
    // em.merge(post);
    // }

}
