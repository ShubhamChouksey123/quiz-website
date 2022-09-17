package com.shubham.app.controller;

import com.shubham.app.entity.Question;
import com.shubham.app.model.Difficulty;
import com.shubham.app.model.FormResponse;
import com.shubham.app.service.questioncrud.QuestionCrud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.util.List;

@RestController
public class QuizSubmissionController {

    private static final int TOTAL_QUESTIONS_TO_ASK = 1;

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

        return "home"; // be same as the template file name (without suffix)
    }

    /**
     * When user submits the form
     *
     * @return
     */
    @PostMapping("/submitQuestionResponse")
    public Integer submitQuestionResponse(@RequestBody(required = false) FormResponse formResponse) {
        Integer ansActual = questionCrud.getActualAns(formResponse.getQuestionId());
        Integer score = 0;
        logger.info("questionId : {} & ansOpted : {} & ansActual : {}", formResponse.getQuestionId(), formResponse.getAnsOpted(), ansActual);
        if (ansActual != null && ansActual.equals(formResponse.getAnsOpted())) {
            score++;
        }
        return score;
    }

    @PostMapping("/submitQuestionsResponse")
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
    public void submitContactResponse() {

    }


    /**
     * Admin page APIs
     */
    @PostMapping("/addQuestion")
    public void addQuestion(@RequestParam("statement") String statement, @RequestParam("optionA") String optionA,
                            @RequestParam("optionB") String optionB, @RequestParam("optionC") String optionC,
                            @RequestParam("optionD") String optionD, @RequestParam("ans") Integer ans,
                            @RequestParam("difficulty") Difficulty difficulty) {
        questionCrud.addQuestion(statement, optionA, optionB, optionC, optionD, ans, difficulty);
    }

    @PostMapping("/publish101")
    public String post101(@RequestParam String name, @RequestParam BigInteger deviceID) {

        return "punlisehd succesfully ! ..." + name + " & deviceID : " + deviceID;
    }

    @PostMapping(value = {"/devices/addDeviceFisrtForm"})
    @ResponseBody
    public String submitDevicesForm(@RequestParam String deviceType, @RequestParam String deviceName,
                                    HttpServletResponse response) throws Exception {

        return "SHubham !!";

    }

    public void addQuestions() {

    }

    public void getAllQuestions() {

    }

    public void removeQuestions() {

    }

    public void removeQuestion() {

    }


}
