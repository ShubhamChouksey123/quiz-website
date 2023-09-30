package com.shubham.app.dto;

import java.util.List;
import com.shubham.app.dto.*;

public class QuizSubmittedForm {

    private List<EachQuestionResponse> questionResponseList;

    public List<EachQuestionResponse> getQuestionResponseList() {
        return questionResponseList;
    }

    public void setQuestionResponseList(List<EachQuestionResponse> questionResponseList) {
        this.questionResponseList = questionResponseList;
    }
}
