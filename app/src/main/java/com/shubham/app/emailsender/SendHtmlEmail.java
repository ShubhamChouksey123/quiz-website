package com.shubham.app.emailsender;

import com.shubham.app.controller.MailContentBuilder;
import com.shubham.app.deliver.emailservice.EmailInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class SendHtmlEmail {


    private JavaMailSender mailSender;
    private MailContentBuilder mailContentBuilder;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public static String readFileToString(String path) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(path);
        return asString(resource);
    }

    public static String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Autowired
    public SendHtmlEmail(JavaMailSender mailSender, MailContentBuilder mailContentBuilder) {
        this.mailSender = mailSender;
        this.mailContentBuilder = mailContentBuilder;
    }

    public void prepareAndSend(EmailInformation emailInformation) {

        logger.info("emailInformation : {}", emailInformation);
        MimeMessagePreparator messagePreparator = mimeMessage -> {

            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8");

//            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom("shubhamchouksey1998@gmail.com");

            String htmlContent = readFileToString("classpath:templates/index.html");
            String verificationCode = "379316";

            messageHelper.setTo(emailInformation.getReceiverEmail());
            messageHelper.setSubject(emailInformation.getSubject());
            String content = mailContentBuilder.build(verificationCode);
            messageHelper.setText(content, true);

            FileSystemResource resource = new FileSystemResource(new File("D:\\OldLaptop\\New_Projects\\QuizWebsiteNewRelease\\app\\src\\main\\resources\\templates\\Logo.png"));
            messageHelper.addInline("logo", resource);
            FileSystemResource resourceArrow = new FileSystemResource(new File("D:\\OldLaptop\\New_Projects\\QuizWebsiteNewRelease\\app\\src\\main\\resources\\templates\\arrow.png"));
            messageHelper.addInline("arrow", resourceArrow);


        };
        try {
            mailSender.send(messagePreparator);
        } catch (MailException e) {
            logger.error("Error Here");
            e.printStackTrace();
        }
    }
}
