package com.shubham.app.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class QuizSubmittedFormTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private void print(Object obj) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonBlockTransaction = null;
        try {
            jsonBlockTransaction = ow.writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n" + jsonBlockTransaction);
    }

    @Test
    @DisplayName("Testing DTO class QuizSubmittedForm")
    public void testDTOQuizSubmitted(){
        QuizSubmittedForm quizSubmittedForm = new QuizSubmittedForm();

        EachQuestionResponse eachQuestionResponse = new EachQuestionResponse(Long.valueOf(1), Integer.valueOf(1));
        List<EachQuestionResponse> questionResponseList = new ArrayList<>();
        questionResponseList.add(eachQuestionResponse);
        quizSubmittedForm.setQuestionResponseList(questionResponseList);

        logger.info("quizSubmittedForm : {}", quizSubmittedForm);

        print( quizSubmittedForm);

    }

}
