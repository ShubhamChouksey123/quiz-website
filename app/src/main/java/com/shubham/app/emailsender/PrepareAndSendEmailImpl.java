package com.shubham.app.emailsender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.shubham.app.deliver.emailservice.EmailInformation;
import com.shubham.app.deliver.emailservice.EmailSenderService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

@Service
public class PrepareAndSendEmailImpl implements PrepareAndSendEmail {

    private static final String TEMPLATE_NAME = "recover-account-verify-phone";
    private static final String EMAIL_SUBJECT = "Wallet Financial - Recover Account OTP";

    private static final String TEMPLATE_NAME_THANK_YOU_FOR_CONTACTING = "email-templates/thank-you-contact/thank-you-contact";
    private static final String EMAIL_SUBJECT_THANK_YOU_FOR_CONTACTING = "Thank you for contacting!";

    private static final String TEMPLATE_NAME_CONTACT_QUERY = "email-templates/new-connection/new-connection";

    private static final String EMAIL_SUBJECT_CONTACT_QUERY = "Somebody wants to connect to you!";

    /** both uses same resources */
    public static Map<String, Resource> PARAMETER_RESOURCE_MAP_REGISTER_AC = Map.ofEntries(
            entry("logo", new ClassPathResource("templates/email-templates/register-account-verify-phone/Logo.png")));

    public static Map<String, Resource> PARAMETER_RESOURCE_MAP_EMAIL_AC = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.receiver-name}")
    private String receiverName;

    @Autowired
    private EmailSenderService emailSenderService;

    private boolean sendEmail(String verificationCode, String receiverPersonalName, String receiverEmail) {

        if (receiverEmail == null) {
            return false;
        }

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("authcode", verificationCode);
        if (receiverPersonalName != null) {
            parameterMap.put("salutation", "Hi " + receiverPersonalName);
        } else {
            parameterMap.put("salutation", "Dear Customer");
        }

        EmailInformation emailInformation = new EmailInformation(receiverPersonalName, receiverEmail, EMAIL_SUBJECT,
                parameterMap, TEMPLATE_NAME, PARAMETER_RESOURCE_MAP_REGISTER_AC);

        return emailSenderService.sendHtmlEmail(emailInformation);
    }

    private boolean sendThankYouMail(String receiverPersonalName, String receiverEmail) {

        if (receiverEmail == null) {
            return false;
        }

        Map<String, Object> parameterMap = new HashMap<>();
        if (receiverPersonalName != null) {
            parameterMap.put("salutation", "Hi " + receiverPersonalName);
        } else {
            parameterMap.put("salutation", "Dear Customer");
        }

        EmailInformation emailInformation = new EmailInformation(receiverPersonalName, receiverEmail,
                EMAIL_SUBJECT_THANK_YOU_FOR_CONTACTING, parameterMap, TEMPLATE_NAME_THANK_YOU_FOR_CONTACTING,
                PARAMETER_RESOURCE_MAP_EMAIL_AC);

        return emailSenderService.sendHtmlEmail(emailInformation);
    }

    private boolean sendNewConnectionMail(String contactName, String contactEmail, String contactPhoneNumber,
            String message, String receiverPersonalName, String receiverEmail) {

        if (receiverEmail == null)
            return false;

        Map<String, Object> parameterMap = new HashMap<>();
        if (receiverPersonalName != null) {
            parameterMap.put("salutation", "Hi " + receiverPersonalName);
        } else {
            parameterMap.put("salutation", "Dear admin");
        }

        parameterMap.put("contactName", contactName);
        parameterMap.put("contactEmail", contactEmail);
        parameterMap.put("contactPhoneNumber", contactPhoneNumber);
        parameterMap.put("message", message);

        parameterMap.put("receiverPersonalName", receiverPersonalName);
        parameterMap.put("receiverEmail", receiverEmail);

        EmailInformation emailInformation = new EmailInformation(receiverPersonalName, receiverEmail,
                EMAIL_SUBJECT_CONTACT_QUERY, parameterMap, TEMPLATE_NAME_CONTACT_QUERY,
                PARAMETER_RESOURCE_MAP_EMAIL_AC);

        return emailSenderService.sendHtmlEmail(emailInformation);
    }

    @Override
    public void sendSMSAndEmail(String verificationCode, String countryCode, String phone, String receiverPersonalName,
            String email) {

        boolean smsSentStatus = true;
        // boolean emailSentStatus = sendEmail(verificationCode, receiverPersonalName,
        // email);
        // sendThankYouMail(receiverPersonalName, email);
        // sendNewConnectionMail("Nikhil", "nikhil@gmail.com", "9479987841", "Hi
        // Shubham, maybe
        // let's connect",
        // "Shubham Chouksey", "shubhamchouksey1998@gmail.com");

        sendContactQueryEmails("Nikhil", "ayushjain1212abc@gmail.com", "9479987841", "Hi Shubham, maybe let's connect");
    }

    @Override
    public void sendContactQueryEmails(String contactName, String contactEmail, String contactPhoneNumber,
            String message) {

        boolean thankYouEmailStatus = sendThankYouMail(contactName, contactEmail);
        logger.info("thankYouEmail send status : {}, send to with name : {} with email : {}", thankYouEmailStatus,
                contactName, contactEmail);

        String receiverPersonalName = receiverName;
        String receiverEmail = adminEmail;

        if (receiverEmail == null)
            return;

        boolean newConnectionEmailStatus = sendNewConnectionMail(contactName, contactEmail, contactPhoneNumber, message,
                receiverPersonalName, receiverEmail);
        logger.info("newConnectionEmail send status : {}, send to name : {} with email : {}", newConnectionEmailStatus,
                receiverPersonalName, receiverEmail);
    }
}
