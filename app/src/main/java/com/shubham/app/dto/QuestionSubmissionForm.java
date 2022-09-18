package com.shubham.app.dto;

import java.util.List;

public class QuestionSubmissionForm {

    private List<EachQuestion> questionList;

    public List<EachQuestion> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<EachQuestion> questionList) {
        this.questionList = questionList;
    }
}
