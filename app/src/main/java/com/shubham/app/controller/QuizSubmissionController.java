package com.shubham.app.controller;

import com.shubham.app.deliver.emailservice.EmailSenderService;
import com.shubham.app.dto.EachQuestion;
import com.shubham.app.dto.QuizSubmittedForm;
import com.shubham.app.emailsender.SendVerificationCode;
import com.shubham.app.entity.Question;
import com.shubham.app.service.questioncrud.QuestionCrud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.shubham.app.dto.*;
import com.shubham.app.deliver.emailservice.*;

import java.util.List;

@RestController
public class QuizSubmissionController {

    private static final int TOTAL_QUESTIONS_TO_ASK = 10;
    private static final String EMAIL_SUBJECT = "Hi Ayush";
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private QuestionCrud questionCrud;
    @Autowired
    private EmailSenderService emailSenderService;
    @Autowired
    private SendVerificationCode sendVerificationCode;

    /**
     * When user starts the quiz and redirected to this page
     *
     * @return
     */


    private void sendEmailUsingSpring() {
        String verificationCode = "192436";
        String emailBody = String.format("Dear Customer,\r\n" + "\r\n"
                        + "Kindly note that your one-time password (OTP) for your application request is %s.\r\n"
                        + "Note that the OTP will be valid only for the next 30 minutes. Please enter the OTP in the designated field and proceed to complete request. OTP are SECRET. DO NOT disclose it to anyone. Bank NEVER asks for OTP.\r\n"
                        + "\r\n" + "This is an auto-generated email. Please do not reply to this email.\r\n" + "\r\n"
                        + "Regards,\r\n" + "ICICI Bank\r\n" + "Visit our website at https://buy.icicibank.com/",
                verificationCode);

        EmailInformation emailInformation = new EmailInformation("", "ayushjain1212abc@gmail.com", EMAIL_SUBJECT, emailBody);
//        emailSenderService.sendTextEmail(emailInformation);
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }


    @GetMapping("/send_email")
    public String sendHTMLEmailWith() {

        sendVerificationCode.sendSMSAndEmail("128232", "IN", "9340188210", "Ayush", "ayushjain1212abc@gmail.com");

        return "result";
    }


    /**
     * When user wants to contact me
     */
    @PostMapping("/contactQuery")
    public void submitContactResponse(@RequestBody(required = false) ContactQueryResponse contactQueryResponse) {
//        sendSimpleEmail("", "", "");
        sendEmailUsingSpring();
//        questionCrud.addContactQuery(contactQueryResponse);
    }


    @GetMapping("/getQuestions")
    public List<Question> getQuestions() {
        return questionCrud.getQuestionsForAnUser(TOTAL_QUESTIONS_TO_ASK);
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
     * Admin page APIs
     */
    @PostMapping("/addQuestion")
    public String addQuestion(@RequestBody EachQuestion eachQuestion) {
        questionCrud.addQuestion(eachQuestion);
        return "Saved Successfully";
    }


    @PostMapping("/addQuestions")
    public String addQuestions(@RequestBody QuestionSubmissionForm questionSubmissionForm) {
//        questionCrud.addQuestions(questionSubmissionForm);
        return "Saved Successfully";
    }


    @GetMapping("/getAllQuestions")
    public List<Question> getAllQuestions() {
        return questionCrud.getAllQuestions();
    }

    public void removeQuestions() {

    }


    @PostMapping("/removeQuestion")
    public boolean removeQuestion(@RequestParam(name = "questionId", required = false) Long questionId) {
        return questionCrud.removeQuestions(questionId);
    }


}
