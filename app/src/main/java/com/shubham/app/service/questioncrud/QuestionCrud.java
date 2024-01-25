package com.shubham.app.service.questioncrud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shubham.app.dto.ContactQueryResponse;
import com.shubham.app.dto.EachQuestion;
import com.shubham.app.dto.QuestionSubmissionForm;
import com.shubham.app.dtotoentity.DTOToEntity;
import com.shubham.app.entity.Question;
import com.shubham.app.entity.QuizSubmission;
import com.shubham.app.hibernate.dao.ContactQueryDaoImpl;
import com.shubham.app.hibernate.dao.QuestionDAO;
import com.shubham.app.hibernate.dao.QuizSubmissionDao;

import java.util.*;

@Service
public class QuestionCrud {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private QuestionDAO questionDAO;
    @Autowired
    private ContactQueryDaoImpl contactQueryDAO;
    @Autowired
    private QuizSubmissionDao quizSubmissionDao;
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

    public Question getAllQuestions(Long questionId) {
        return questionDAO.questionById(questionId);
    }

    public boolean removeQuestions(Long questionId) {
        return questionDAO.deleteQuestion(questionId);
    }

    public List<Question> getQuestionsFromQuestionIds(List<Integer> questionIdsList) {
        return questionDAO.getAnswerOfQuestions(questionIdsList);
    }

    private Integer calculateScore(List<Integer> questionIdsList, List<Integer> userOptedAnswersList,
            List<Question> questions) {

        Map<Long, Integer> mp = new HashMap<>();
        for (Question question : questions) {
            mp.put(question.getQuestionId(), question.getAns());
        }

        Integer score = 0;
        for (int i = 0; i < userOptedAnswersList.size(); i++) {
            Integer questionId = questionIdsList.get(i);
            Integer userOptedAnswers = userOptedAnswersList.get(i);
            Integer ansActual = mp.get(Long.valueOf(questionId));

            logger.debug("questionId : {} & userOptedAnswers : {} & ansActual : {}", questionId, userOptedAnswers,
                    ansActual);
            if (ansActual != null && userOptedAnswers != null
                    && Objects.equals(ansActual, Math.toIntExact(userOptedAnswers))) {
                score++;
            }
        }
        return score;
    }

    public Integer calculateAndSaveScore(String name, String email, List<Integer> questionIdsList,
            List<Integer> userOptedAnswersList, List<Question> questions) {

        Integer score = calculateScore(questionIdsList, userOptedAnswersList, questions);
        logger.info("score calculated : {}", score);

        QuizSubmission quizSubmission = new QuizSubmission(name, email, score, new Date());
        quizSubmissionDao.save(quizSubmission);

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

    public List<QuizSubmission> getTopPerformers() {
        return quizSubmissionDao.getTopPerformers(5);
    }
}
