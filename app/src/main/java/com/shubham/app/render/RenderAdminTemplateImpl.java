package com.shubham.app.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.shubham.app.emailsender.PrepareAndSendEmail;
import com.shubham.app.entity.Question;
import com.shubham.app.hibernate.dao.ContactQueryDao;
import com.shubham.app.service.questioncrud.QuestionCrud;
import com.shubham.app.service.questioncrud.QuestionsUtils;

import java.util.ArrayList;
import java.util.List;

import static com.shubham.app.controller.QuizSubmissionController.TOTAL_QUESTIONS_TO_ASK;

@Service
public class RenderAdminTemplateImpl implements RenderAdminTemplate {

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
    public void renderAdminPage(Model model) {

        logger.info("fetching all the questions from our quiz database");
        List<Question> questions = questionCrud.getAllQuestions();

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
}
