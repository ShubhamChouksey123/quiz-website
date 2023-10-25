package com.shubham.app.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.shubham.app.entity.Question;
import com.shubham.app.service.questioncrud.QuestionCrud;
import com.shubham.app.service.questioncrud.QuestionsUtils;
import com.shubham.app.service.questioncrud.exception.InternalServerException;

import java.util.ArrayList;
import java.util.List;

import static com.shubham.app.controller.QuizSubmissionController.TOTAL_QUESTIONS_TO_ASK;

@Service
public class RenderQuizTemplateImpl implements RenderQuizTemplate {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

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

        questionCrud.proceedWithSave(name, email, questionIdsList, userOptedAnswersList);
    }
}
