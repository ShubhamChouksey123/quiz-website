package com.shubham.app.deliver.emailservice;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

public interface EmailRequest {

	void sendEmail(EmailInformation emailInformation) throws AddressException, MessagingException;
}
