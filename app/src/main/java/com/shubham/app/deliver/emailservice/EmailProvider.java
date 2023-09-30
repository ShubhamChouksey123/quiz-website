package com.shubham.app.deliver.emailservice;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;

public interface EmailProvider {

    boolean sendTextEmail(EmailInformation emailInformation) throws AddressException, MessagingException;

    boolean prepareAndSendHtmlEmail(EmailInformation emailInformation);
}
