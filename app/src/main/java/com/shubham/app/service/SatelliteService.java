package com.shubham.app.service;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shubham.app.dto.HRInfoDTO;
import com.shubham.app.entity.HRInfo;
import com.shubham.app.service.questioncrud.exception.InvalidRequest;

import java.math.BigInteger;
import java.util.List;

public interface SatelliteService {
    List<HRInfoDTO> getMailInfo(BigInteger firstResult, BigInteger maxResults, String searchText);

    HRInfo createOrUpdateSatellites(String mailIdExisting, String hrName, String hrEmail, String company,
            String jobTitle, String role, String jobURL, String advertisedOn, RedirectAttributes redirectAttrs)
            throws InvalidRequest;

    HRInfoDTO getMailInfo(String id);

    void deleteMailInfo(String id);

    void sendResumeEmail(String hrId) throws InvalidRequest;

    void saveAndsSendResumeEmail(String hrName, String hrEmail, String company, String jobTitle, String role,
            String jobURL, String advertisedOn, RedirectAttributes redirectAttrs) throws InvalidRequest;
}
