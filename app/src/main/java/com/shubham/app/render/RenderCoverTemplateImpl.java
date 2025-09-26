package com.shubham.app.render;

import com.shubham.app.emailsender.PrepareAndSendEmail;
import com.shubham.app.hibernate.dao.ContactQueryDao;
import com.shubham.app.service.questioncrud.QuestionCrud;
import com.shubham.app.service.questioncrud.QuestionsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class RenderCoverTemplateImpl implements RenderCoverTemplate {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private QuestionCrud questionCrud;
    @Autowired
    private QuestionsUtils questionsUtils;
    @Autowired
    private ContactQueryDao contactQueryDao;
    @Autowired
    private PrepareAndSendEmail prepareAndSendEmail;

    @Override
    public void renderCoverForm(Model model) {
    }

    /**
     * used to change the approval level of the question between : NEW, DISCARD,
     * EDIT
     */
    @Override
    public void renderCoverTemplate(String hiringManagerName, String companyName, String jobTitle) {
    }
}
