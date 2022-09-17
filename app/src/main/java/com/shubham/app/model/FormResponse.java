package com.shubham.app.model;

import org.springframework.web.bind.annotation.RequestBody;

public class FormResponse {

    private Long questionId;
    private Integer ansOpted;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Integer getAnsOpted() {
        return ansOpted;
    }

    public void setAnsOpted(Integer ansOpted) {
        this.ansOpted = ansOpted;
    }
}
