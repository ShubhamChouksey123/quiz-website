package com.shubham.app.render;

import org.springframework.ui.Model;

import com.shubham.app.service.questioncrud.exception.InternalServerException;

public interface RenderQuizTemplate {
    void renderQuizPage(Model model);

    void calculateScore(String name, String email, String userOptedAnswers, String questionIds, Model model)
            throws InternalServerException;

    void renderLeaderBoardPage(Model model);

    /**
     * From the rest endpoint /contact, the submitted contact query will be
     * processed here. Contact query is persisted in database
     *
     * <p>
     * The service layer include:
     *
     * <ul>
     * <li>Saving the contact query in database.
     * <li>Sending mail to admin(its me Shubham) that someone has requested to
     * contact you.
     * </ul>
     *
     * @param name
     * @param email
     * @param phoneNumber
     * @param message
     * @param model
     * @throws InternalServerException
     */
    void submitContactQuery(String name, String email, String phoneNumber, String message, Model model)
            throws InternalServerException;
}
