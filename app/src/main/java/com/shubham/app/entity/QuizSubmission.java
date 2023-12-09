package com.shubham.app.entity;

import java.util.Date;
import jakarta.persistence.*;

@Entity(name = "quiz_submission")
@Table(name = "quiz_submission")
public class QuizSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "score")
    private Integer score;

    @Column(name = "time_stamp")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date timeStamp;

    public QuizSubmission() {
    }

    public QuizSubmission(String name, String email, Integer score, Date timeStamp) {
        this.name = name;
        this.email = email;
        this.score = score;
        this.timeStamp = timeStamp;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "QuizSubmission{" + "submissionId=" + submissionId + ", name='" + name + '\'' + ", email='" + email
                + '\'' + ", score=" + score + ", timeStamp=" + timeStamp + '}';
    }
}
