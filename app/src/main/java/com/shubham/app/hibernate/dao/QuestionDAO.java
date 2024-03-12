package com.shubham.app.hibernate.dao;

import com.shubham.app.entity.Question;
import com.shubham.app.model.ApprovalLevel;

import java.util.List;

public interface QuestionDAO {
    void saveQuestion(Question question);

    Question questionById(Long questionId);

    List<Question> getAllQuestion(ApprovalLevel approvalLevel);

    List<Question> getQuestion(Integer n);

    boolean deleteQuestion(Long questionId);

    Integer getAnswerOfAQuestion(Long questionId);

    List<Question> getAnswerOfQuestions(List<Integer> questionIds);
}
