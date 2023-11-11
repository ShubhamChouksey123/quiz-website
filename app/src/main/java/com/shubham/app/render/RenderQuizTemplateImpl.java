package com.shubham.app.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.shubham.app.dto.EachQuestion;
import com.shubham.app.entity.Question;
import com.shubham.app.service.questioncrud.QuestionCrud;
import com.shubham.app.service.questioncrud.QuestionsUtils;
import com.shubham.app.service.questioncrud.exception.InternalServerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.shubham.app.controller.QuizSubmissionController.TOTAL_QUESTIONS_TO_ASK;

@Service
public class RenderQuizTemplateImpl implements RenderQuizTemplate {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private QuestionCrud questionCrud;

    @Autowired
    private QuestionsUtils questionsUtils;

    @Override
    public void renderQuizPage(Model model) {

        List<Question> questions = questionCrud.getQuestionsForAnUser(TOTAL_QUESTIONS_TO_ASK);

        List<Long> ids = new ArrayList<>();

        for (Question question : questions) {
            ids.add(question.getQuestionId());
            logger.info("question : {}", question);
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
            logger.info("eachQuestion : {}", eachQuestion);
        }

        Integer score = questionCrud.calculateAndSaveScore(name, email, questionIdsList, userOptedAnswersList,
                questions);

        renderResultPage(questionsResults, score, model);
    }

    public void renderResultPage(List<EachQuestion> questions, Integer score, Model model) {

        model.addAttribute("questions", questions);
        model.addAttribute("score", score);
        model.addAttribute("maxScore", 10);

        model.addAttribute("questionNumberToShow", 1);
        model.addAttribute("totalQuestions", TOTAL_QUESTIONS_TO_ASK);
    }

    @Override
    public void renderResultPagePrepareFake(Model model) {

        List<Question> questions = questionCrud.getQuestionsForAnUser(TOTAL_QUESTIONS_TO_ASK);

        List<EachQuestion> questionsResults = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {

            Question question = questions.get(i);

            EachQuestion eachQuestion = new EachQuestion(question);
            eachQuestion.setIndex(i);
            eachQuestion.setUseOptedAnswer(1);
            questionsResults.add(eachQuestion);

            setAllBorderColorsOfOptions(eachQuestion);
            logger.info("eachQuestion : {}", eachQuestion);
        }

        renderResultPage(questionsResults, 8, model);
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

    private void setAllBorderColorsOfOptions(EachQuestion eachQuestion) {
        eachQuestion.setBorderColorOptionA(getBorderColor(eachQuestion.getAns(), eachQuestion.getUseOptedAnswer(), 0));
        eachQuestion.setBorderColorOptionB(getBorderColor(eachQuestion.getAns(), eachQuestion.getUseOptedAnswer(), 1));
        eachQuestion.setBorderColorOptionC(getBorderColor(eachQuestion.getAns(), eachQuestion.getUseOptedAnswer(), 2));
        eachQuestion.setBorderColorOptionD(getBorderColor(eachQuestion.getAns(), eachQuestion.getUseOptedAnswer(), 3));
    }
}
