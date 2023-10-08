package com.shubham.app.controller;

import com.shubham.app.render.RenderQuizTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * We have used this open-source theme template from https://themewagon.com/themes/vintagefur/
 */
@Controller
public class QuizController {


//    private

    @Autowired
    private RenderQuizTemplate renderQuizTemplate;

    @GetMapping("/verify-phone")
    public String verifyPhone() {
        return "verify-phone";
    }


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
    public String renderQuiz() {
        return "quiz-template/quiz";
    }

    @GetMapping({"/quiz2"})
    public String renderQuizTemp(Model model) {

        renderQuizTemplate.renderQuizPage(model);
        return "quiz-template/quiz";
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
