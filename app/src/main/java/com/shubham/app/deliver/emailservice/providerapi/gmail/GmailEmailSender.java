package com.shubham.app.deliver.emailservice.providerapi.gmail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.shubham.app.deliver.emailservice.EmailInformation;
import com.shubham.app.deliver.emailservice.EmailRequest;
import org.springframework.stereotype.Service;


@Service("gmail")
public class GmailEmailSender implements EmailRequest {

	public Message createMessage() {

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("shubhamchouksey1998@gmail.com", "ozbseuyqnbnumbls");
			}
		});
		Message msg = new MimeMessage(session);
		return msg;
	}

	@Override
	public void sendEmail(EmailInformation emailInformation) throws AddressException, MessagingException {

		Message msg = createMessage();
		msg.setFrom(new InternetAddress("shubhamchouksey1998@gmail.com", false));

		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailInformation.getReceiverEmail()));
		msg.setSubject(emailInformation.getSubject());
		msg.setContent(emailInformation.getBody(), "text/html");
		msg.setSentDate(new Date());

		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(emailInformation.getBody(), "text/html");

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);

		msg.setContent(multipart);
		Transport.send(msg);

	}

}
