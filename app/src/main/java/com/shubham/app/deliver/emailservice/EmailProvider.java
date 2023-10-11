package com.shubham.app.deliver.emailservice;

import java.io.IOException;
import jakarta.mail.MessagingException;

public interface EmailProvider {

    boolean sendTextEmail(EmailInformation emailInformation) throws MessagingException, IOException;

    boolean prepareAndSendHtmlEmail(EmailInformation emailInformation);
}
