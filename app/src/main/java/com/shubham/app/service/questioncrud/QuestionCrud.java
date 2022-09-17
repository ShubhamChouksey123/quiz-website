package com.shubham.app.service.questioncrud;

import com.shubham.app.entity.Question;
import com.shubham.app.hibernate.dao.QuestionDAO;
import com.shubham.app.model.Difficulty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionCrud {
    @Autowired
    private QuestionDAO questionDAO;

    public void addQuestion(String statement, String optionA, String optionB, String optionC, String optionD, Integer ans, Difficulty difficulty) {

        Question question = new Question(statement, optionA, optionB, optionC, optionD, ans, difficulty);
        questionDAO.saveQuestion(question);
    }

    public List<Question> getQuestionsForAnUser(int totalQuestions) {

        List<Question> questionList = questionDAO.getQuestion(totalQuestions);
        return questionList;

    }

    public Integer getActualAns(Long questionId) {
        return questionDAO.getAnswerOfAQuestion(questionId);
    }
}
