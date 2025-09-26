package com.shubham.app.deliver.emailservice;

import jakarta.mail.MessagingException;
import java.io.IOException;

public interface EmailProvider {

    boolean sendTextEmail(EmailInformation emailInformation) throws MessagingException, IOException;

    boolean prepareAndSendHtmlEmail(EmailInformation emailInformation);
}
