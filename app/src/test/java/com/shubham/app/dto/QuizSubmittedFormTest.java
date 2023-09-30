package com.shubham.app.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


}
