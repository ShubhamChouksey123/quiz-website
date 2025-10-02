package com.shubham.app.rizzle.dao;

import com.shubham.app.rizzle.entity.Post;
import com.shubham.app.rizzle.entity.Post_;
import com.shubham.app.rizzle.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UserRepository {

    @Autowired
    private EntityManager em;

    public void save(User user) {
        em.merge(user);
    }

    public User getUserById(Long userId) {
        // try catch
        return em.find(User.class, userId);
    }

    public void followerUser(Long userId, Long followerId) {

        User user = getUserById(userId);
        User follower = getUserById(followerId);
        user.getFollowers().add(follower);

        save(user);
    }

    public List<Post> fetchPostsOfFollowers(Long userId) {

        User user = getUserById(userId);
        List<User> followers = user.getFollowers();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Post> query = cb.createQuery(Post.class);
        Root<Post> root = query.from(Post.class);

        query.select(root);

        CriteriaBuilder.In<Long> inClause = cb.in(root.get(Post_.userId));
        for (User follower : followers) {
            inClause.value(follower.getUserId());
        }
        query.where(inClause);

        List<Post> posts = new ArrayList<>();
        try {
            posts = em.createQuery(query).getResultList();
        } catch (NoResultException e) {
            System.out.println("No post to show");
        } catch (Exception e) {
            System.out.println("Couldn't find a questions with cause : " + e.getMessage());
        }

        return posts;
    }
}
