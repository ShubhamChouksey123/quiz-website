package com.shubham.app.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.shubham.app.dto.EachQuestion;
import com.shubham.app.emailsender.PrepareAndSendEmail;
import com.shubham.app.entity.ContactQuery;
import com.shubham.app.entity.Question;
import com.shubham.app.entity.QuizSubmission;
import com.shubham.app.hibernate.dao.ContactQueryDao;
import com.shubham.app.model.DifficultyLevel;
import com.shubham.app.model.QuestionCategory;
import com.shubham.app.service.questioncrud.QuestionCrud;
import com.shubham.app.service.questioncrud.QuestionsUtils;
import com.shubham.app.service.questioncrud.exception.InternalServerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.shubham.app.controller.QuizController.ZERO_LENGTH_STRING;
import static com.shubham.app.controller.QuizSubmissionController.TOTAL_QUESTIONS_TO_ASK;

@Service
public class RenderQuizTemplateImpl implements RenderQuizTemplate {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private QuestionCrud questionCrud;
    @Autowired
    private QuestionsUtils questionsUtils;
    @Autowired
    private ContactQueryDao contactQueryDao;
    @Autowired
    private PrepareAndSendEmail prepareAndSendEmail;

    @Override
    public void renderQuizPage(Model model) {

        logger.info("fetching the questions for quiz page");
        List<Question> questions = questionCrud.getQuestionsForAnUser(TOTAL_QUESTIONS_TO_ASK);

        List<Long> ids = new ArrayList<>();

        for (Question question : questions) {
            ids.add(question.getQuestionId());
            logger.debug("question : {}", question);
        }

        model.addAttribute("questions", questions);
        model.addAttribute("questionIds", ids);
        model.addAttribute("questionNumberToShow", 1);
        model.addAttribute("totalQuestions", TOTAL_QUESTIONS_TO_ASK);
    }

    @Override
    public void calculateScore(String name, String email, String userOptedAnswers, String questionIdsString,
            Model model) throws InternalServerException {

        List<Integer> questionIdsList = questionsUtils.convertStringQuestionsToList(questionIdsString);
        List<Integer> userOptedAnswersList = questionsUtils.convertStringQuestionsToList(userOptedAnswers);

        List<Question> questions = questionCrud.getQuestionsFromQuestionIds(questionIdsList);

        List<EachQuestion> questionsResults = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {

            Question question = questions.get(i);

            EachQuestion eachQuestion = new EachQuestion(question);
            eachQuestion.setIndex(i);
            eachQuestion.setUseOptedAnswer(userOptedAnswersList.get(i));
            questionsResults.add(eachQuestion);

            setAllBorderColorsOfOptions(eachQuestion);
            logger.debug("eachQuestion : {}", eachQuestion);
        }

        Integer score = questionCrud.calculateAndSaveScore(name, email, questionIdsList, userOptedAnswersList,
                questions);

        renderResultPage(questionsResults, score, model);
    }

    private void renderResultPage(List<EachQuestion> questions, Integer score, Model model) {

        model.addAttribute("questions", questions);
        model.addAttribute("score", score);
        model.addAttribute("maxScore", 10);

        model.addAttribute("questionNumberToShow", 1);
        model.addAttribute("totalQuestions", TOTAL_QUESTIONS_TO_ASK);
    }

    private void setAllBorderColorsOfOptions(EachQuestion eachQuestion) {
        eachQuestion.setBorderColorOptionA(getBorderColor(eachQuestion.getAns(), eachQuestion.getUseOptedAnswer(), 0));
        eachQuestion.setBorderColorOptionB(getBorderColor(eachQuestion.getAns(), eachQuestion.getUseOptedAnswer(), 1));
        eachQuestion.setBorderColorOptionC(getBorderColor(eachQuestion.getAns(), eachQuestion.getUseOptedAnswer(), 2));
        eachQuestion.setBorderColorOptionD(getBorderColor(eachQuestion.getAns(), eachQuestion.getUseOptedAnswer(), 3));
    }

    private String getBorderColor(Integer ans, Integer userOptedAnswer, Integer option) {
        if (Objects.equals(ans, userOptedAnswer) && Objects.equals(userOptedAnswer, option)) {
            return "blue-border";
        } else if (!Objects.equals(ans, option) && Objects.equals(userOptedAnswer, option)) {
            return "red-border";
        } else if (Objects.equals(ans, option) && !Objects.equals(userOptedAnswer, option)) {
            return "green-border";
        }
        return "default-border";
    }

    @Override
    public void renderLeaderBoardPage(Model model) {

        logger.info("fetching the leaderboard page");
        List<QuizSubmission> quizSubmissions = questionCrud.getTopPerformers();

        for (QuizSubmission quizSubmission : quizSubmissions) {
            logger.debug("quizSubmission : {}", quizSubmission);
        }

        model.addAttribute("performers", quizSubmissions);
        model.addAttribute("totalQuestions", TOTAL_QUESTIONS_TO_ASK);
    }

    @Override
    public void submitContactQuery(String name, String email, String phoneNumber, String message, Model model) {

        ContactQuery contactQuery = new ContactQuery(name, email, phoneNumber, message);
        contactQueryDao.saveContactQuery(contactQuery);

        prepareAndSendEmail.sendContactQueryEmails(name, email, phoneNumber, message);
    }

    @Override
    public void submitNewAddQuestion(Long questionId, QuestionCategory category, String statement, String optionA,
            String optionB, String optionC, String optionD, Integer answer, DifficultyLevel difficultyLevel) {

        EachQuestion contactQuery = new EachQuestion(null, questionId, statement, optionA, optionB, optionC, optionD,
                answer, null, difficultyLevel);
        questionCrud.addQuestion(contactQuery);
    }

    @Override
    public void renderDesiredQuestionEditPage(String questionId, Model model) {

        Question question = null;
        if (questionId == null || Objects.equals(questionId, ZERO_LENGTH_STRING)) {
            question = new Question(null, "", "", "", "", "", 0, null);
        } else {
            question = questionCrud.getAllQuestions(Long.valueOf(questionId));
        }

        model.addAttribute("question", question);
    }
}
