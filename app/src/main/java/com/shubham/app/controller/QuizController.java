package com.shubham.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.shubham.app.model.DifficultyLevel;
import com.shubham.app.model.QuestionCategory;
import com.shubham.app.render.RenderQuizTemplate;
import com.shubham.app.service.questioncrud.exception.InternalServerException;

import java.util.Objects;

/**
 * We have used this open-source theme template from
 * <a href="https://themewagon.com/themes/vintagefur/">Theme-wagon template</a>
 */
@Controller
public class QuizController {
    public static final String ZERO_LENGTH_STRING = "";
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private RenderQuizTemplate renderQuizTemplate;

    @GetMapping({"/", "/home", "index", ""})
    public String renderHome(@ModelAttribute("contactMessage") String contactMessage, Model model) {

        if (contactMessage != null && !Objects.equals(contactMessage, ZERO_LENGTH_STRING)) {
            model.addAttribute("contactMessage", contactMessage);
            model.addAttribute("successValue", true);
        }

        renderQuizTemplate.renderLeaderBoardPage(model);
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
    public RedirectView addQuestion(@RequestParam(value = "name") String name,
            @RequestParam(value = "email") String email,
            @RequestParam(value = "userOptedAnswers", required = false) String userOptedAnswers,
            @RequestParam(value = "questionIds", required = false) String questionIds, Model model,
            RedirectAttributes redirectAttrs) {
        logger.info("submitted the quiz with name : {}, email : {} and userOptedAnswers : {}", name, email,
                userOptedAnswers);
        logger.info("questionIds : {}", questionIds);

        redirectAttrs.addFlashAttribute("name", name);
        redirectAttrs.addFlashAttribute("email", email);
        redirectAttrs.addFlashAttribute("userOptedAnswers", userOptedAnswers);
        redirectAttrs.addFlashAttribute("questionIds", questionIds);

        return new RedirectView("/result");
    }

    @GetMapping({"/result"})
    public String renderResultOfQuiz(@ModelAttribute("name") String name, @ModelAttribute("email") String email,
            @ModelAttribute("userOptedAnswers") String userOptedAnswers,
            @ModelAttribute("questionIds") String questionIds, Model model) throws InternalServerException {

        if (!Objects.equals(name, ZERO_LENGTH_STRING)) {
            renderQuizTemplate.calculateScore(name, email, userOptedAnswers, questionIds, model);
        }
        return "quiz-template/result";
    }

    @PostMapping({"/result"})
    public String postRenderResultOfQuiz(@ModelAttribute("name") String name, @ModelAttribute("email") String email,
            @ModelAttribute("userOptedAnswers") String userOptedAnswers,
            @ModelAttribute("questionIds") String questionIds, Model model) {

        return "quiz-template/result";
    }

    @GetMapping({"/shop"})
    public String renderShop() {
        return "quiz-template/shop";
    }

    @GetMapping({"/contact"})
    public String renderContact(Model model) {
        return "quiz-template/contact";
    }

    @PostMapping(value = {"/submit-contact"})
    @ResponseBody
    public RedirectView submitContactQuery(@RequestParam(value = "name") String name,
            @RequestParam(value = "email") String email,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "message", required = false) String message, Model model,
            RedirectAttributes redirectAttrs) {

        logger.info("post method submitted with name : {}, email : {}, phoneNumber : {} and message : {}", name, email,
                phoneNumber, message);
        renderQuizTemplate.submitContactQuery(name, email, phoneNumber, message, model);

        redirectAttrs.addFlashAttribute("contactMessage",
                "Thank You for contacting, we will connect to you at the earliest.");
        return new RedirectView("/home");
    }

    @GetMapping({"/leaderboard"})
    public String renderLeaderBoard(Model model) {
        renderQuizTemplate.renderLeaderBoardPage(model);
        return "quiz-template/leaderboard";
    }

    @GetMapping({"/add-question"})
    public String renderAddQuestionGet(@ModelAttribute("questionId") String questionId, Model model) {
        renderQuizTemplate.renderDesiredQuestionEditPage(questionId, model);
        return "add-question/add-question";
    }

    @PostMapping({"/add-question"})
    public String renderAddQuestionPost(@ModelAttribute("questionId") String questionId, Model model) {
        if (!Objects.equals(questionId, 0L)) {
            logger.info("edit question page specific for questionId : {}", questionId);
            renderQuizTemplate.renderDesiredQuestionEditPage(questionId, model);
        }

        return "add-question/add-question";
    }

    @PostMapping(value = {"/submit-add-question"})
    @ResponseBody
    public RedirectView submitAddQuestion(@RequestParam(value = "question_id", required = false) Long questionId,
            @RequestParam(value = "category") QuestionCategory category,
            @RequestParam(value = "difficulty_level") DifficultyLevel difficultyLevel,
            @RequestParam(value = "question") String question, @RequestParam(value = "optionA") String optionA,
            @RequestParam(value = "optionB") String optionB, @RequestParam(value = "optionC") String optionC,
            @RequestParam(value = "optionD") String optionD, @RequestParam(value = "answer") Integer answer,
            Model model, RedirectAttributes redirectAttrs) {

        logger.info(
                "post method submitted a new question with questionId : {}category : {}, question : {}, optionA : {}, optionB : {}, optionC : {}, optionD : {} and answer : {}",
                questionId, category, question, optionA, optionB, optionC, optionD, answer);

        renderQuizTemplate.submitNewAddQuestion(questionId, category, question, optionA, optionB, optionC, optionD,
                answer, difficultyLevel);

        redirectAttrs.addFlashAttribute("contactMessage", "Added new question.");
        return new RedirectView("/home");
    }
}
