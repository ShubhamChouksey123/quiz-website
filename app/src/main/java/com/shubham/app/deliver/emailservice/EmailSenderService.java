package com.shubham.app.deliver.emailservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;

@Service
public class EmailSenderService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private EmailProvider emailProvider;

    public EmailSenderService() {
    }

    public boolean sendTextEmail(EmailInformation emailInformation) {
        try {
            boolean sent = emailProvider.sendTextEmail(emailInformation);
            return sent;
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.getCause();
        } catch (Exception e) {
            e.getCause();
        }
        logger.error("Unable to send email to : {}", emailInformation.getReceiverEmail());
        return false;
    }

    public boolean sendHtmlEmail(EmailInformation emailInformation) {

        boolean sent = emailProvider.prepareAndSendHtmlEmail(emailInformation);
        if (sent) {
            return true;
        }
        logger.error("Unable to send email to : {}", emailInformation.getReceiverEmail());
        return false;
    }
}
