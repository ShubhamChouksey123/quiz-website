package com.shubham.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shubham.app.emailsender.PrepareAndSendEmailImpl;

@RestController
public class EmailSenderController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private PrepareAndSendEmailImpl sendVerificationCode;

    @GetMapping("/send_email")
    public String sendHTMLEmailWith() {

        sendVerificationCode.sendSMSAndEmail("128232", "IN", "9340188210", "Ayush", "shubhamchouksey1998@gmail.com");

        return "result";
    }
}
