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
import com.shubham.app.entity.HRInfo;
import com.shubham.app.entity.MailInfo;
import com.shubham.app.utils.GeneralUtility;

import java.util.Date;
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

    private static final String TEMPLATE_NAME_RESUME_SEND = "email-templates/thank-you-contact/resume-send";

    private static final String EMAIL_SUBJECT_RESUME_SEND = "Job Application";

    public static Map<String, Resource> PARAMETER_RESOURCE_MAP_RESUME_SEND = Map
            .ofEntries(entry("shubham_chouksey_cv.pdf",
                    new ClassPathResource("templates/email-templates/resume-send/shubham_chouksey_cv.pdf")));

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
    @Autowired
    private GeneralUtility generalUtility;

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

    private String createEmailSubject(HRInfo hrInfo) {

        if (!generalUtility.isNullOrEmpty(hrInfo.getTimes()) && hrInfo.getTimes() > 5 && hrInfo.getTimes() % 5 == 0) {
            return "In case you missed it";
        }

        if (!generalUtility.isNullOrEmpty(hrInfo.getTimes()) && hrInfo.getTimes() > 5 && hrInfo.getTimes() % 4 == 0) {
            return "Job inquiry — Shubham Chouksey, 3+ years of experience, Sr. Software Engineer";
        }

        if (!generalUtility.isNullOrEmpty(hrInfo.getEmailSubject())) {
            return hrInfo.getEmailSubject();
        }
        return EMAIL_SUBJECT_RESUME_SEND;
    }

    private void addCorrectResumeFile(HRInfo hrInfo, EmailInformation emailInformation) {

        if (hrInfo.getCompany() == null) {
            emailInformation.setParameterResourceMap(PARAMETER_RESOURCE_MAP_RESUME_SEND);
            return;
        }

        String companyName = generalUtility.getFormattedName(hrInfo.getCompany());
        ClassPathResource resourceCompanySpecific = new ClassPathResource(
                "templates/email-templates/resume-send/" + companyName + "/shubham_chouksey_cv.pdf");
        if (!resourceCompanySpecific.exists()) {
            logger.warn("company specific resource not found, setting default resource");
            emailInformation.setParameterResourceMap(PARAMETER_RESOURCE_MAP_RESUME_SEND);
            return;
        }

        logger.info("setting company specific resource");
        Map<String, Resource> parameterResourceMap = Map
                .ofEntries(entry("shubham_chouksey_cv.pdf", resourceCompanySpecific));
        emailInformation.setParameterResourceMap(parameterResourceMap);
    }

    private boolean sendNewEmailToHR(HRInfo hrInfo, String email) {

        if (email == null)
            return false;

        Map<String, Object> parameterMap = new HashMap<>();
        if (hrInfo.getHrName() != null) {
            parameterMap.put("salutation", "Hi " + hrInfo.getHrName());
        } else {
            parameterMap.put("salutation", "Hello Recruiter");
        }

        parameterMap.put("hrName", hrInfo.getHrName());
        parameterMap.put("hrEmail", email);
        parameterMap.put("company", hrInfo.getCompany());
        parameterMap.put("jobTitle", hrInfo.getJobTitle());
        parameterMap.put("role", hrInfo.getJobTitle());
        parameterMap.put("jobURL", hrInfo.getJobURL());
        parameterMap.put("advertisedOn", hrInfo.getAdvertisedOn());

        if (generalUtility.isNullOrEmpty(hrInfo.getJobURL())) {
            parameterMap.put("isURLKnown", Boolean.FALSE);
        } else {
            parameterMap.put("isURLKnown", Boolean.TRUE);
        }

        String emailSubject = createEmailSubject(hrInfo);

        EmailInformation emailInformation = new EmailInformation(hrInfo.getHrName(), email, emailSubject, parameterMap,
                TEMPLATE_NAME_RESUME_SEND, null);
        addCorrectResumeFile(hrInfo, emailInformation);

        return emailSenderService.sendHtmlEmail(emailInformation);
    }

    @Override
    public void sendResumeEmail(HRInfo hrInfo) {

        logger.info("hrInfo : {}", hrInfo);

        for (String email : hrInfo.getHrEmails()) {
            boolean newConnectionEmailStatus = sendNewEmailToHR(hrInfo, email);
            logger.info("sending resume to HR with name : {} with email : {}", hrInfo.getHrName(), email);
        }

        MailInfo mailInfo = new MailInfo(hrInfo.getHrId(), hrInfo.getJobURL(), new Date());
        if (hrInfo.getTimes() == null) {
            hrInfo.setTimes(1);
        } else {
            hrInfo.setTimes(hrInfo.getTimes() + 1);
        }

        hrInfo.setLastSentAt(new Date());
        hrInfo.addMailSendInfo(mailInfo);
    }
}
