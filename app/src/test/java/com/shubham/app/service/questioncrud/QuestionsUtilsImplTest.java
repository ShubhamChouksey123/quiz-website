package com.shubham.app.service.questioncrud;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shubham.app.service.questioncrud.exception.InternalServerException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

// @TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = {QuestionsUtilsImpl.class, ObjectMapper.class})
@ExtendWith(SpringExtension.class)
class QuestionsUtilsImplTest {

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
    void testConvertStringQuestionsToList() throws InternalServerException {

        String questionIds = "[2,3,4,5,6,7,8,9,10,11]";

        List<Integer> questionIdsList = questionsUtilsImpl.convertStringQuestionsToList(questionIds);
        logger.info("questionIdsList: {}", questionIdsList);
        assertNotNull(questionIdsList);
    }

    @Test
    void testConvertStringQuestionsToList2() throws InternalServerException {

        String questionIds = "[2,null,4,5,null,7,null,9,10,11]";

        List<Integer> questionIdsList = questionsUtilsImpl.convertStringQuestionsToList(questionIds);
        logger.info("questionIdsList: {}", questionIdsList);
        assertNotNull(questionIdsList);
    }
}
