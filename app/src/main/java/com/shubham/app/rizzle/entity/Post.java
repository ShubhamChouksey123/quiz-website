package com.shubham.app.rizzle.entity;

import jakarta.persistence.*;

@Entity(name = "post")
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue
    @Column(name = "post_id")
    private Integer postId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "content")
    private String content;

    public Post(Integer postId, Long userId, String content) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
    }

    public Post() {
    }

    public Post(Long userId, String content) {
        this.userId = userId;
        this.content = content;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Post{" + "postId=" + postId + ", userId=" + userId + ", content='" + content + '\'' + '}';
    }
}
