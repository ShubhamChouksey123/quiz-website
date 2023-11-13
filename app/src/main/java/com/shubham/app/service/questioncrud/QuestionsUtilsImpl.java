package com.shubham.app.service.questioncrud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shubham.app.service.questioncrud.exception.InternalServerException;

import java.util.List;

@Service
public class QuestionsUtilsImpl implements QuestionsUtils {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<Integer> convertStringQuestionsToList(String questionIds) throws InternalServerException {
        // [2,3,4,5,6,7,8,9,10,11]

        List<Integer> questionIdsList = null;
        try {
            questionIdsList = objectMapper.readValue(questionIds, List.class);
        } catch (JsonProcessingException e) {
            logger.warn("unable to convert : {} string to list of integer", questionIds);
            throw new InternalServerException();
        }

        return questionIdsList;
    }
}
