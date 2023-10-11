package com.shubham.app.service.questioncrud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shubham.app.dto.*;
import com.shubham.app.dtotoentity.*;
import com.shubham.app.entity.Question;
import com.shubham.app.hibernate.dao.ContactQueryDAO;
import com.shubham.app.hibernate.dao.QuestionDAO;

import java.util.List;

@Service
public class QuestionCrud {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private QuestionDAO questionDAO;
    @Autowired
    private ContactQueryDAO contactQueryDAO;
    @Autowired
    private DTOToEntity dTOToEntity;

    public void addQuestion(EachQuestion eachQuestion) {
        Question question = dTOToEntity.convertQuestionDTO(eachQuestion);
        questionDAO.saveQuestion(question);
    }

    public void addQuestions(QuestionSubmissionForm questionSubmissionForm) {
        for (EachQuestion eachQuestion : questionSubmissionForm.getQuestionList()) {
            addQuestion(eachQuestion);
        }
    }

    public List<Question> getQuestionsForAnUser(int totalQuestions) {
        return questionDAO.getQuestion(totalQuestions);
    }

    public List<Question> getAllQuestions() {
        return questionDAO.getAllQuestion();
    }

    public boolean removeQuestions(Long questionId) {
        boolean isDeleted = questionDAO.deleteQuestion(questionId);
        return isDeleted;
    }

    public Integer calculateScore(QuizSubmittedForm quizSubmittedForm) {

        Integer score = 0;
        for (EachQuestionResponse eachQuestionResponse : quizSubmittedForm.getQuestionResponseList()) {
            Integer ansActual = getActualAns(eachQuestionResponse.getQuestionId());
            logger.info("questionId : {} & ansOpted : {} & ansActual : {}", eachQuestionResponse.getQuestionId(),
                    eachQuestionResponse.getAnsOpted(), ansActual);
            if (ansActual != null && ansActual.equals(eachQuestionResponse.getAnsOpted())) {
                score++;
            }
        }
        return score;
    }

    public Integer getActualAns(Long questionId) {
        return questionDAO.getAnswerOfAQuestion(questionId);
    }

    /** Contact Page */
    public void addContactQuery(ContactQueryResponse contactQueryResponse) {
        // ContactQuery contactQuery =
        // dTOToEntity.convertContactQueryDTO(contactQueryResponse);
        // dTOToEntity.convertContactQueryDTO(contactQueryResponse);
        contactQueryDAO.saveContactQuery(null);
    }
}
