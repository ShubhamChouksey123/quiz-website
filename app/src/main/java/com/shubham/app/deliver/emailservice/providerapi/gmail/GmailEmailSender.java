package com.shubham.app.deliver.emailservice.providerapi.gmail;


import org.springframework.stereotype.Service;


@Service("gmail")
public class GmailEmailSender {


//    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
//
//    @Autowired
//    private TemplateEngine templateEngine;
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    public Message createMessage() {
//
//        Properties props = new Properties();
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.host", "smtp.gmail.com");
//        props.put("mail.smtp.port", "587");
//
//        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication("kperme@cognam.com", "fyhktfinnznokqtu");
//            }
//        });
//        Message msg = new MimeMessage(session);
//        return msg;
//    }
//
//    @Override
//    public boolean sendTextEmail(EmailInformation emailInformation) throws AddressException, MessagingException {
//
//        Message msg = createMessage();
//        msg.setFrom(new InternetAddress("kperme@cognam.com", false));
//
//        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailInformation.getReceiverEmail()));
//        msg.setSubject(emailInformation.getSubject());
//        msg.setContent(emailInformation.getBody(), "text/html");
//        msg.setSentDate(new Date());
//
//        MimeBodyPart messageBodyPart = new MimeBodyPart();
//        messageBodyPart.setContent(emailInformation.getBody(), "text/html");
//
//        Multipart multipart = new MimeMultipart();
//        multipart.addBodyPart(messageBodyPart);
//
//        msg.setContent(multipart);
//        Transport.send(msg);
//        return true;
//
//    }
//
//    private String buildContext(EmailInformation emailInformation) {
//
//        Context context = new Context();
//        for (Map.Entry<String, Object> mapElement : emailInformation.getParameterMap().entrySet()) {
//            String key = mapElement.getKey();
//            context.setVariable(mapElement.getKey(), mapElement.getValue());
//        }
//        return templateEngine.process(emailInformation.getTemplateName(), context);
//    }
//
//    private void addInLineImages(EmailInformation emailInformation, MimeMessageHelper messageHelper) throws MessagingException {
//        for (Map.Entry<String, Resource> mapElement : emailInformation.getParameterResourceMap().entrySet()) {
//            messageHelper.addInline(mapElement.getKey(), mapElement.getValue());
//        }
//    }
//
//    @Override
//    public boolean prepareAndSendHtmlEmail(EmailInformation emailInformation) {
//
//        MimeMessagePreparator messagePreparatory = mimeMessage -> {
//
//            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
//            messageHelper.setFrom("schouksey@cognam.com");
//
//            messageHelper.setTo(emailInformation.getReceiverEmail());
//            messageHelper.setSubject(emailInformation.getSubject());
//            String content = buildContext(emailInformation);
//            messageHelper.setText(content, true);
//
//            addInLineImages(emailInformation, messageHelper);
//
//        };
//        try {
//            mailSender.send(messagePreparatory);
//            return true;
//        } catch (MailException e) {
//            logger.error("Error Here : {}", e.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }


}
