package com.shubham.app.service.questioncrud;

import com.shubham.app.service.questioncrud.exception.InternalServerException;

import java.util.List;

public interface QuestionsUtils {

    public List<Integer> convertStringQuestionsToList(String questionIds) throws InternalServerException;
}
