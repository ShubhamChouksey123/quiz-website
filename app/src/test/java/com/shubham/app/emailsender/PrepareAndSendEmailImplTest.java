package com.shubham.app.emailsender;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shubham.app.service.questioncrud.QuestionsUtilsImpl;

import java.nio.file.Files;
import java.nio.file.Path;

@ContextConfiguration(classes = {QuestionsUtilsImpl.class, ObjectMapper.class})
@ExtendWith(SpringExtension.class)
class PrepareAndSendEmailImplTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private QuestionsUtilsImpl questionsUtilsImpl;

    @BeforeAll
    static void beforeAllInit() {
        System.out.println("Before All - Before the class has been created !");
    }

    @BeforeEach
    void init() {
        System.out.println("beforeEach Method Run !");
    }

    @AfterEach
    void cleanUp() {
        System.out.println("after each method has fininshed running. Cleaning up !");
    }

    @Test
    void testConvertStringQuestionsToList() {

        // Path path =
        // Path.of("D:\\OldLaptop\\New_Projects\\QuizWebsiteNewRelease\\app\\src\\main\\resources\\templates\\email-templates\\resume-send\\adobe");
        Path path = Path.of("/templates/email-templates/resume-send/adobe");

        if (Files.exists(path)) {
            logger.info("---------------------- folder exist ---------------------");
        }

        ClassPathResource s = new ClassPathResource("templates/email-templates/resume-send/shubham_chouksey_cv.pdf");

        if (s != null) {
            logger.info("----------------------  1 folder exist ---------------------");
        }
    }
}
