package com.shubham.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.shubham.app.render.RenderQuizTemplate;

/**
 * We have used this open-source theme template from
 * https://themewagon.com/themes/vintagefur/
 */
@Controller
public class QuizController {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private RenderQuizTemplate renderQuizTemplate;

    @GetMapping({"/home", "index"})
    public String renderHome() {
        return "quiz-template/index";
    }

    @GetMapping({"/about"})
    public String renderAbout() {
        return "quiz-template/about";
    }

    @GetMapping({"/services"})
    public String renderServices() {
        return "quiz-template/services";
    }

    @GetMapping({"/quiz"})
    public String renderQuiz(Model model) {
        renderQuizTemplate.renderQuizPage(model);
        return "quiz-template/quiz";
    }

    @PostMapping(value = {"/submit-quiz"})
    @ResponseBody
    public String addQuestion(@RequestParam(value = "name") String name, @RequestParam(value = "email") String email,
            @RequestParam(value = "userOptedAnswers", required = false) String userOptedAnswers,
            @RequestParam(value = "questionIds", required = false) String questionIds) {
        logger.info("submitted the quiz with name : {}, email : {} and userOptedAnswers : {}", name, email,
                userOptedAnswers);
        logger.info("questionIds : {}", questionIds);
        return "Saved Successfully";
    }

    @GetMapping({"/shop"})
    public String renderShop() {
        return "quiz-template/shop";
    }

    @GetMapping({"/contact"})
    public String renderContact() {
        return "quiz-template/contact";
    }
}
