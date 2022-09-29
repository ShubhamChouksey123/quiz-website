package com.shubham.app.deliver.emailservice;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import com.shubham.app.deliver.emailservice.providerapi.gmail.GmailEmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service
public class EmailSenderService {

	private final EmailRequest emailRequest;

	@Autowired
	public EmailSenderService(@Qualifier("gmail") GmailEmailSender emailRequest) {
		this.emailRequest = emailRequest;
	}

	public void sendEmail(EmailInformation emailInformation) {
		try {
			emailRequest.sendEmail(emailInformation);
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
