package com.shubham.app.rizzle.service;

import com.shubham.app.rizzle.dao.PostRepository;
import com.shubham.app.rizzle.dao.UserRepository;
import com.shubham.app.rizzle.entity.Post;
import com.shubham.app.rizzle.entity.User;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TwitterService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    public void createPost(Long userId, String text) {

        User user = userRepository.getUserById(userId);

        Post post = new Post(userId, text);
        postRepository.save(post);
    }

    /** follow user */
    public void followUser(Long userId, Long followerId) {

        userRepository.followerUser(userId, followerId);
    }

    /**
     * fetchFeed
     *
     * <p>
     * fetch feeds from the latest follower to last follower
     */
    public List<Post> fetchFeed(Long userId) {

        List<Post> posts = userRepository.fetchPostsOfFollowers(userId);
        System.out.println("posts : " + posts);

        return posts;
    }
}
