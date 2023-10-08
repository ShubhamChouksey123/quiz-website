package com.shubham.app.render;

import com.shubham.app.entity.Question;
import com.shubham.app.service.questioncrud.QuestionCrud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

import static com.shubham.app.controller.QuizSubmissionController.TOTAL_QUESTIONS_TO_ASK;

@Service
public class RenderQuizTemplateImpl implements RenderQuizTemplate {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private QuestionCrud questionCrud;


    @Override
    public void renderQuizPage(Model model) {

        List<Question> questions = questionCrud.getQuestionsForAnUser(TOTAL_QUESTIONS_TO_ASK);

        logger.info("all questions : {}", questions);
        for (Question question : questions) {
            logger.info("question : {}", question);
        }

        model.addAttribute("questions", questions);
        model.addAttribute("questionNumberToShow", 1);
    }

}
