package com.shubham.app.controller;

import com.shubham.app.deliver.emailservice.EmailInformation;
import com.shubham.app.deliver.emailservice.EmailSenderService;
import com.shubham.app.dto.ContactQueryResponse;
import com.shubham.app.dto.EachQuestion;
import com.shubham.app.dto.QuestionSubmissionForm;
import com.shubham.app.dto.QuizSubmittedForm;
import com.shubham.app.emailsender.SendHtmlEmail;
import com.shubham.app.entity.Question;
import com.shubham.app.service.questioncrud.QuestionCrud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

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
    private SendHtmlEmail sendHtmlEmail;
    /**
     * When user starts the quiz and redirected to this page
     *
     * @return
     */

    @Autowired
    private JavaMailSender mailSender;

    public static String readFileToString(String path) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(path);
        return asString(resource);
    }

    public static String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void sendEmailUsingSpring() {
        String verificationCode = "192436";
        String emailBody = String.format("Dear Customer,\r\n" + "\r\n"
                        + "Kindly note that your one-time password (OTP) for your application request is %s.\r\n"
                        + "Note that the OTP will be valid only for the next 30 minutes. Please enter the OTP in the designated field and proceed to complete request. OTP are SECRET. DO NOT disclose it to anyone. Bank NEVER asks for OTP.\r\n"
                        + "\r\n" + "This is an auto-generated email. Please do not reply to this email.\r\n" + "\r\n"
                        + "Regards,\r\n" + "ICICI Bank\r\n" + "Visit our website at https://buy.icicibank.com/",
                verificationCode);

        EmailInformation emailInformation = new EmailInformation("", "ayushjain1212abc@gmail.com", EMAIL_SUBJECT, emailBody);
        emailSenderService.sendEmail(emailInformation);
    }

    @GetMapping("/send_email_inline_image")
    public String sendHTMLEmailWithInlineImage() throws MessagingException {

        String from = "shubhamchouksey1998@gmail.com";
        String to = "schouksey@cognam.com";
        String verificationCode = "379316";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setSubject("Here's your pic");
        helper.setFrom(from);
        helper.setTo(to);

        String htmlContent = readFileToString("classpath:wallet-financial-emailer/index.html");

        htmlContent = String.format(htmlContent, verificationCode);


        String content = "<b>Dear guru</b>,<br><i>Please look at this nice picture:.</i>"
                + "<br><img src='cid:image001'/><br><b>Best Regards</b>";
        helper.setText(htmlContent, true);

        FileSystemResource resource = new FileSystemResource(new File("D:\\OldLaptop\\New_Projects\\QuizWebsiteNewRelease\\app\\src\\main\\resources\\wallet-financial-emailer\\logo.png"));
//        FileSystemResource resourceLogo = new FileSystemResource(new File("classpath:wallet-financial-emailer/logo.png"));
        FileSystemResource resourceArrow = new FileSystemResource(new File("D:\\OldLaptop\\New_Projects\\QuizWebsiteNewRelease\\app\\src\\main\\resources\\wallet-financial-emailer\\arrow.png"));
        helper.addInline("arrow", resourceArrow);
        helper.addInline("logo", resource);

        mailSender.send(message);

        return "result";
    }

    @GetMapping("/send_email")
    public String sendHTMLEmailWith() {


        String verificationCode = "379316";

        EmailInformation emailInformation = new EmailInformation("", "ayushjain1212abc@gmail.com", EMAIL_SUBJECT, "");
        sendHtmlEmail.prepareAndSend(emailInformation);

        return "result";
    }

    public void sendSimpleEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("shubhamchouksey1998@gmail.com");
        message.setTo("ayushjain1212abc@gmail.com");
        message.setText("Hello Ayush, Shubham this side, hope you're good!");
        message.setSubject("Greate Man");
        mailSender.send(message);
        System.out.println("Mail Send...");


    }

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
//        sendSimpleEmail("", "", "");
        sendEmailUsingSpring();
//        questionCrud.addContactQuery(contactQueryResponse);
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
    public boolean removeQuestion(@RequestParam(name = "questionId", required = false) Long questionId) {
        return questionCrud.removeQuestions(questionId);
    }


}
