package com.shubham.app.deliver.emailservice;

import org.springframework.stereotype.Service;


@Service
public class EmailSenderService {

//    private final EmailRequest emailRequest;
//
//    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
//
//    @Autowired
//    public EmailSenderService(@Qualifier("gmail") EmailRequest emailRequest) {
//        this.emailRequest = emailRequest;
//    }
//
//    public boolean sendTextEmail(EmailInformation emailInformation) {
//        try {
//            boolean sent = emailRequest.sendTextEmail(emailInformation);
//            return sent;
//        } catch (AddressException e) {
//            // TODO Auto-generated catch block
//            e.getCause();
//        } catch (MessagingException e) {
//            // TODO Auto-generated catch block
//            e.getCause();
//        } catch (Exception e) {
//            e.getCause();
//        }
//        logger.error("Unable to send email to : {}", emailInformation.getReceiverEmail());
//        return false;
//    }
//
//    public boolean sendHtmlEmail(EmailInformation emailInformation) {
//
//        boolean sent = emailRequest.prepareAndSendHtmlEmail(emailInformation);
//        if (sent == true) {
//            return sent;
//        }
//        logger.error("Unable to send email to : {}", emailInformation.getReceiverEmail());
//        return false;
//
//    }

}
