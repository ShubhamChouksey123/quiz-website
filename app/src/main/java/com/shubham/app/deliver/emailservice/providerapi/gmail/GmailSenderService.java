package com.shubham.app.deliver.emailservice.providerapi.gmail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.shubham.app.deliver.emailservice.EmailInformation;
import com.shubham.app.deliver.emailservice.EmailProvider;
import com.shubham.app.deliver.emailservice.providerapi.gmail.config.GmailProperties;

import java.util.Date;
import java.util.Map;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

@Service
public class GmailSenderService implements EmailProvider {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    JavaMailSender mailSender;
    @Autowired
    GmailProperties gmailProperties;
    @Autowired
    private TemplateEngine templateEngine;

    public Message createMessage() {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("kperme@cognam.com", "fyhktfinnznokqtu");
            }
        });
        Message msg = new MimeMessage(session);
        return msg;
    }

    @Override
    public boolean sendTextEmail(EmailInformation emailInformation) throws MessagingException {

        Message msg = createMessage();
        msg.setFrom(new InternetAddress("kperme@cognam.com", false));

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
        return true;
    }

    private String buildContext(EmailInformation emailInformation) {

        Context context = new Context();
        for (Map.Entry<String, Object> mapElement : emailInformation.getParameterMap().entrySet()) {
            context.setVariable(mapElement.getKey(), mapElement.getValue());
        }
        return templateEngine.process(emailInformation.getTemplateName(), context);
    }

    private void addInLineImages(EmailInformation emailInformation, MimeMessageHelper messageHelper)
            throws MessagingException {

        if (emailInformation.getParameterResourceMap() == null) {
            return;
        }
        for (Map.Entry<String, Resource> mapElement : emailInformation.getParameterResourceMap().entrySet()) {
            messageHelper.addAttachment(mapElement.getKey(), mapElement.getValue());
        }
    }

    public boolean prepareAndSendHtmlEmailImpl(EmailInformation emailInformation) {

        MimeMessagePreparator messagePreparatory = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom(gmailProperties.getUsername());

            messageHelper.setTo(emailInformation.getReceiverEmail());
            messageHelper.setSubject(emailInformation.getSubject());
            String content = buildContext(emailInformation);
            messageHelper.setText(content, true);

            addInLineImages(emailInformation, messageHelper);
        };

        try {
            mailSender.send(messagePreparatory);
            return true;
        } catch (MailException e) {
            logger.error("Error Here : {}", e.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean prepareAndSendHtmlEmail(EmailInformation emailInformation) {
        new Thread(() -> {
            prepareAndSendHtmlEmailImpl(emailInformation);
        }).start();
        return true;
    }
}
