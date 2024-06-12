package com.shubham.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.shubham.app.dto.HRInfoDTO;
import com.shubham.app.service.HRInfoService;
import com.shubham.app.service.questioncrud.exception.InvalidRequest;
import com.shubham.app.utils.GeneralUtility;

import java.util.List;

@RestController
public class SavingHRInfo {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private HRInfoService hrInfoService;

    @Autowired
    private GeneralUtility generalUtility;

    @PostMapping(value = {"/web/mails/add-hr-infos"})
    public String createHRInfo(@RequestBody List<HRInfoDTO> values) throws InvalidRequest {
        logger.info("values: {}", values);

        for (HRInfoDTO hrInfoDTO : values) {
            String hrEmail = generalUtility.convertToString(hrInfoDTO.getHrEmails());
            hrInfoService.createOrUpdateHRInfo(null, hrInfoDTO.getHrName(), hrEmail, hrInfoDTO.getCompany(),
                    "Engineer II (Java Fullstack)", null, "https://aexp.eightfold.ai/careers/job/22702593",
                    "company's job portal", null, null);
        }

        return "shubham";
    }
}
