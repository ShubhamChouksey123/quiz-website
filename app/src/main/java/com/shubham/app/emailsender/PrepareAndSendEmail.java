package com.shubham.app.emailsender;

import com.shubham.app.entity.HRInfo;
import com.shubham.app.service.questioncrud.exception.InvalidRequest;

public interface PrepareAndSendEmail {

    void sendSMSAndEmail(String verificationCode, String countryCode, String phone, String receiverPersonalName,
            String email);

    void sendContactQueryEmails(String contactName, String contactEmail, String contactPhoneNumber, String message);

    void sendResumeEmail(HRInfo hrInfo) throws InvalidRequest;
}
