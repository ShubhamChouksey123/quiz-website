package com.shubham.app.dto;

public class EachQuestionResponse {

    private Long questionId;
    private Integer ansOpted;

    public EachQuestionResponse() {
    }

    public EachQuestionResponse(Long questionId, Integer ansOpted) {
        this.questionId = questionId;
        this.ansOpted = ansOpted;
    }



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
