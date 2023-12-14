package com.shubham.app.emailsender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.shubham.app.deliver.emailservice.EmailInformation;
import com.shubham.app.deliver.emailservice.EmailSenderService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

@Service
public class SendVerificationCode {

    private static final String TEMPLATE_NAME = "recover-account-verify-phone";
    private static final String EMAIL_SUBJECT = "Wallet Financial - Recover Account OTP";

    private static final String TEMPLATE_NAME_THANK_YOU_FOR_CONTACTING = "thank-you-contact";
    private static final String EMAIL_SUBJECT_THANK_YOU_FOR_CONTACTING = "Thank you for contacting!";

    private static final String EMAIL_SUBJECT_CONTACT_QUERY = "Somebody wants to connect to you!";

    /** both uses same resources */
    public static Map<String, Resource> PARAMETER_RESOURCE_MAP_REGISTER_AC = Map.ofEntries(
            entry("logo", new ClassPathResource("templates/email-templates/register-account-verify-phone/Logo.png")));

    public static Map<String, Resource> PARAMETER_RESOURCE_MAP_EMAIL_AC = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
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

    public void sendSMSAndEmail(String verificationCode, String countryCode, String phone, String receiverPersonalName,
            String email) {

        boolean smsSentStatus = true;
        // boolean emailSentStatus = sendEmail(verificationCode, receiverPersonalName,
        // email);
        sendThankYouMail(receiverPersonalName, email);
    }
}
