package com.shubham.app.controller;

import com.shubham.app.dto.ContactQueryResponse;
import com.shubham.app.dto.EachQuestion;
import com.shubham.app.dto.QuestionSubmissionForm;
import com.shubham.app.dto.QuizSubmittedForm;
import com.shubham.app.entity.Question;
import com.shubham.app.service.questioncrud.QuestionCrud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class QuizSubmissionController {

    private static final int TOTAL_QUESTIONS_TO_ASK = 10;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private QuestionCrud questionCrud;

    /**
     * When user starts the quiz and redirected to this page
     *
     * @return
     */
    @GetMapping("/getQuestions")
    public List<Question> getQuestions() {
        return questionCrud.getQuestionsForAnUser(TOTAL_QUESTIONS_TO_ASK);
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    /**
     * When user submits the form
     *
     * @return
     */
    @PostMapping("/submitQuestionsResponse")
    public Integer submitQuestionResponse(@RequestBody(required = true) QuizSubmittedForm quizSubmittedForm) {
        return questionCrud.calculateScore(quizSubmittedForm);
    }

    @PostMapping("/submitQuestionResponse")
    public Integer submitQuestionsResponse(@RequestParam(name = "questionId", required = false) Long questionId, @RequestParam(required = false) Integer ansOpted) {
        Integer ansActual = questionCrud.getActualAns(questionId);
        Integer score = 0;
        logger.info("questionId : {} & ansOpted : {} & ansActual : {}", questionId, ansOpted, ansActual);
        if (ansActual != null && ansActual.equals(ansOpted)) {
            score++;
        }
        return score;
    }

    /**
     * When user wants to contact me
     */
    @PostMapping("/contactQuery")
    public void submitContactResponse(@RequestBody ContactQueryResponse contactQueryResponse) {
        questionCrud.addContactQuery(contactQueryResponse);
    }


    /**
     * Admin page APIs
     */
    @PostMapping("/addQuestion")
    public String addQuestion(@RequestBody EachQuestion eachQuestion) {
        questionCrud.addQuestion(eachQuestion);
        return "Saved Successfully";
    }


    @PostMapping("/addQuestions")
    public String addQuestions(@RequestBody QuestionSubmissionForm questionSubmissionForm) {
        questionCrud.addQuestions(questionSubmissionForm);
        return "Saved Successfully";
    }


    @GetMapping("/getAllQuestions")
    public List<Question> getAllQuestions() {
        return questionCrud.getAllQuestions();
    }

    public void removeQuestions() {

    }


    @PostMapping("/removeQuestion")
    public boolean removeQuestion( @RequestParam(name = "questionId", required = false) Long questionId) {
        return questionCrud.removeQuestions(questionId);
    }


}
