package com.shubham.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailContentBuilder {

    private TemplateEngine templateEngine;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public MailContentBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String build(String verificationCode) {

        logger.info("Starting to edit the template");
        Context context = new Context();
        context.setVariable("code", verificationCode);
        context.setVariable("logo", "Logo");
        return templateEngine.process("index", context);
    }

}
