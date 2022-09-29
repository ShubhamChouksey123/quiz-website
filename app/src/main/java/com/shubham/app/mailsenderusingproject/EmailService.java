package com.shubham.app.mailsenderusingproject;

import com.shubham.app.deliver.emailservice.EmailInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmailService {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    TemplateEngine templateEngine;

    @Autowired
    TemplateEngine textTemplateEngine;

    @Autowired
    TemplateEngine htmlTemplateEngine;

    @Autowired
    TemplateEngine fileTemplateEngine;


    public void sendEmail(EmailInformation emailInformation) throws MessagingException, IOException {

        // Prepare the evaluation context
        Context ctx = prepareContext();

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        // Prepare message using a Spring helper
        MimeMessageHelper message = prepareMessage(mimeMessage, emailInformation);

        // Create the HTML body using Thymeleaf
        /** possible Error TODO : */
        String htmlContent = this.fileTemplateEngine.process("index", ctx);
        message.setText(htmlContent, true /* isHtml */);

        log.info("Processing email request: ");

//        message = prepareStaticResources(message, emailDto);

        // Send mail
        this.mailSender.send(mimeMessage);

        this.fileTemplateEngine.clearTemplateCache();


    }

    private Context prepareContext() {

        String verificationCode = "479294";
        Context ctx = new Context();

        ctx.setVariable("code", verificationCode);
        ctx.setVariable("logo", "Logo");

        return ctx;
    }

    private MimeMessageHelper prepareMessage(MimeMessage mimeMessage, EmailInformation emailInformation)
            throws MessagingException, IOException {

        // Prepare message using a Spring helper
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                "UTF-8");
        message.setSubject(emailInformation.getSubject());
        message.setFrom("shubhamchouksey1998@gmail.com");
        message.setTo(emailInformation.getReceiverEmail());

        return message;

    }

    /*-
    private MimeMessageHelper prepareStaticResources(MimeMessageHelper message,
                                                     EmailDto emailDto) throws MessagingException {
        Map<String, Object> staticResources = emailDto.getStaticResourceMap();

        for (Map.Entry<String, Object> entry : staticResources.entrySet()) {

            ClassPathResource imageSource =
                    new ClassPathResource("static/" + (String) entry.getValue());
            message.addInline(entry.getKey(), imageSource, "image/png");
            message.addInline((String) entry.getValue(), imageSource, "image/png");

        }

        return message;
    }
    */
}
