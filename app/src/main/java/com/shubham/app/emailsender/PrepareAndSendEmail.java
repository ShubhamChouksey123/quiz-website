package com.shubham.app.emailsender;

public interface PrepareAndSendEmail {

    void sendSMSAndEmail(String verificationCode, String countryCode, String phone, String receiverPersonalName,
            String email);

    void sendContactQueryEmails(String contactName, String contactEmail, String contactPhoneNumber, String message);
}
