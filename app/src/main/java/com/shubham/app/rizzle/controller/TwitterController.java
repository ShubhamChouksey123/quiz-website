package com.shubham.app.rizzle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.shubham.app.rizzle.entity.Post;
import com.shubham.app.rizzle.service.TwitterService;

import java.util.List;

@Controller
public class TwitterController {

    /** Lakshya Gupta lakshya@thesilverlabs.com */
    @Autowired
    private TwitterService twitterService;

    /** createPost Post */
    @PostMapping(value = {"/createPost"})
    @ResponseBody
    public void createPost(@RequestParam(value = "userId") Long userId, @RequestParam(value = "text") String text) {
        twitterService.createPost(userId, text);
    }

    /** follow User */
    @PostMapping(value = {"/followUser"})
    @ResponseBody
    public void followUser(@RequestParam(value = "userId") Long userId,
            @RequestParam(value = "followerId") Long followerId) {

        twitterService.followUser(userId, followerId);

        //
    }

    /**
     * fetchFeed
     *
     * <p>
     * fetch feeds from the latest follower to last follower User -> Followers list
     * -> their Post
     */
    @PostMapping(value = {"/fetchFeed"})
    @ResponseBody
    public List<Post> fetchFeed(@RequestParam(value = "userId") Long userId) {

        return twitterService.fetchFeed(userId);
    }
}
