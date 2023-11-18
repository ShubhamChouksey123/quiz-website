package com.shubham.app.render;

import org.springframework.ui.Model;

import com.shubham.app.service.questioncrud.exception.InternalServerException;

public interface RenderQuizTemplate {
    void renderQuizPage(Model model);

    void calculateScore(String name, String email, String userOptedAnswers, String questionIds, Model model)
            throws InternalServerException;
}
