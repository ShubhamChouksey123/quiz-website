package com.shubham.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shubham.app.dto.HRInfoDTO;
import com.shubham.app.emailsender.PrepareAndSendEmail;
import com.shubham.app.entity.HRInfo;
import com.shubham.app.hibernate.dao.HRInfoDao;
import com.shubham.app.service.questioncrud.exception.InvalidRequest;
import com.shubham.app.utils.GeneralUtility;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.shubham.app.controller.QuizController.ZERO_LENGTH_STRING;

@Service
public class HRInfoServiceImpl implements HRInfoService {

    public static final String EMAIL_REGEX_PATTERN = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private HRInfoDao hrInfoDao;
    @Autowired
    private PrepareAndSendEmail prepareAndSendEmail;

    @Autowired
    private GeneralUtility generalUtility;

    private Date getDate(String dateInString) throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return formatter.parse(dateInString);
    }

    public String getDate(Date date) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return formatter.format(date);
    }

    private List<HRInfoDTO> getHRDtoListFromEntity(List<HRInfo> resumeMails) {

        List<HRInfoDTO> hrInfoDTOs = new ArrayList<>();

        if (resumeMails == null) {
            return hrInfoDTOs;
        }

        resumeMails.forEach(resumeMail -> {
            HRInfoDTO resumeSendInfo = new HRInfoDTO(resumeMail.getHrId(), resumeMail.getHrName(),
                    resumeMail.getHrEmails(), resumeMail.getCompany(), resumeMail.getJobTitle(), null,
                    resumeMail.getJobURL(), resumeMail.getAdvertisedOn(), resumeMail.getTimes());
            hrInfoDTOs.add(resumeSendInfo);
        });
        return hrInfoDTOs;
    }

    @Override
    public List<HRInfoDTO> getHRInfo(BigInteger firstResult, BigInteger maxResults, String searchText) {

        List<HRInfo> hrInfoList;
        if (searchText != null && !Objects.equals(searchText, ZERO_LENGTH_STRING)) {
            searchText = searchText.toLowerCase();
            hrInfoList = hrInfoDao.getHRInfo(firstResult, maxResults, searchText);
        } else {
            hrInfoList = hrInfoDao.getHRInfo(firstResult, maxResults);
        }

        return getHRDtoListFromEntity(hrInfoList);
    }

    private void validateParameters(String mailIdExisting, String hrName, String hrEmail, List<String> hrEmails,
            String company, String jobTitle, String role, String jobURL, String advertisedOn) throws InvalidRequest {

        if (generalUtility.isNullOrEmpty(hrName)) {
            throw new InvalidRequest("hrName can't be null or empty");
        }
        if (generalUtility.isNullOrEmpty(hrEmail)) {
            throw new InvalidRequest("hrEmail can't be null or empty");
        }
        if (generalUtility.isNullOrEmpty(company)) {
            throw new InvalidRequest("company can't be null or empty");
        }
        if (generalUtility.isNullOrEmpty(jobTitle) && generalUtility.isNullOrEmpty(role)) {
            throw new InvalidRequest("both jobTitle and role can't be null or empty");
        }
        if (generalUtility.isNullOrEmpty(jobURL)) {
            throw new InvalidRequest("jobURL can't be null or empty");
        }
        if (generalUtility.isNullOrEmpty(advertisedOn)) {
            throw new InvalidRequest("advertisedOn can't be null or empty");
        }
        if (hrEmails == null || hrEmails.isEmpty()) {
            throw new InvalidRequest("hrEmail can't be null or empty");
        }

        for (int i = 0; i < hrEmails.size(); i++) {
            if (!hrEmails.get(i).matches(EMAIL_REGEX_PATTERN)) {
                throw new InvalidRequest("invalid hr Email at index : " + i);
            }
        }
    }

    private String getJobTitle(String jobTitle, String role) throws InvalidRequest {

        if (generalUtility.isNullOrEmpty(jobTitle) && generalUtility.isNullOrEmpty(role)) {
            throw new InvalidRequest("both jobTitle and jobRole can't be null");
        }

        if (generalUtility.isNullOrEmpty(role)) {
            return jobTitle;
        }

        if (generalUtility.isNullOrEmpty(jobTitle)) {
            return role;
        }

        return jobTitle;
    }

    private void updateHRInfo(String hrName, List<String> hrEmails, String company, String jobTitle, String jobURL,
            String advertisedOn, HRInfo hrInfo) {

        if (hrInfo == null) {
            return;
        }

        if (hrName != null) {
            hrInfo.setHrName(hrName);
        }
        if (company != null) {
            hrInfo.setCompany(company);
        }
        if (jobTitle != null) {
            hrInfo.setJobTitle(jobTitle);
        }
        if (jobURL != null) {
            hrInfo.setJobURL(jobURL);
        }
        if (advertisedOn != null) {
            hrInfo.setAdvertisedOn(advertisedOn);
        }
        if (hrEmails != null && !hrEmails.isEmpty()) {
            hrInfo.setHrEmails(hrEmails);
        }
    }

    @Override
    public HRInfo createOrUpdateHRInfo(String mailIdExisting, String hrName, String hrEmail, String company,
            String jobTitle, String role, String jobURL, String advertisedOn, RedirectAttributes redirectAttrs)
            throws InvalidRequest {

        List<String> hrEmails = new ArrayList<>();
        if (!generalUtility.isNullOrEmpty(hrEmail)) {
            hrEmails = Arrays.asList(hrEmail.split(","));
        }

        validateParameters(mailIdExisting, hrName, hrEmail, hrEmails, company, jobTitle, role, jobURL, advertisedOn);
        jobTitle = getJobTitle(jobTitle, role);

        /** adding existing launcher */
        if (generalUtility.isNullOrEmpty(mailIdExisting)) {
            HRInfo hrInfo = new HRInfo(hrName, hrEmails, company, jobTitle, jobURL, advertisedOn, new Date(), 0);
            hrInfoDao.saveOrUpdate(hrInfo);
            if (redirectAttrs != null) {
                redirectAttrs.addFlashAttribute("successMessage",
                        "Successfully added a new mail-info satellite with hrName : " + hrName + " of hrEmail : "
                                + hrEmail + " and company : " + company);
            }
            return hrInfo;
        }

        /** updating new launcher */
        logger.info(
                "Updating existing mail-info with values as hrName : {}, hrEmail : {}, company : {}, jobTitle : {}, jobURL : {}, advertisedOn : {}",
                hrName, hrEmail, company, jobTitle, jobURL, advertisedOn);

        HRInfo hrInfo = hrInfoDao.getHRInfoById(mailIdExisting);
        updateHRInfo(hrName, hrEmails, company, jobTitle, jobURL, advertisedOn, hrInfo);
        hrInfoDao.saveOrUpdate(hrInfo);

        if (redirectAttrs != null) {
            redirectAttrs.addFlashAttribute("successMessage",
                    "Successfully updated existing mail-info with hrName : " + hrName + " of company : " + company);
        }
        return hrInfo;
    }

    @Override
    public HRInfoDTO getHRInfo(String id) {

        HRInfo hrInfo = hrInfoDao.getHRInfoById(id);
        return new HRInfoDTO(hrInfo.getHrId(), hrInfo.getHrName(), hrInfo.getHrEmails(), hrInfo.getCompany(),
                hrInfo.getJobTitle(), null, hrInfo.getJobURL(), hrInfo.getAdvertisedOn(), hrInfo.getTimes());
    }

    @Override
    public void deleteHRInfo(String id) {
        hrInfoDao.deleteHRInfo(id);
    }

    @Override
    public void sendResumeEmail(String hrId) throws InvalidRequest {

        HRInfo hrInfo = hrInfoDao.getHRInfoById(hrId);
        logger.info("sending resume to HR with id : {}, name : {} with email : {}", hrId, hrInfo.getHrName(),
                hrInfo.getHrEmails());

        prepareAndSendEmail.sendResumeEmail(hrInfo);

        hrInfoDao.saveOrUpdate(hrInfo);
    }

    @Override
    public void saveAndsSendResumeEmail(String hrName, String hrEmail, String company, String jobTitle, String role,
            String jobURL, String advertisedOn, RedirectAttributes redirectAttrs) throws InvalidRequest {

        logger.info("sending resume to HR : {}, send to name : {} with email : {}", hrName, hrName, hrEmail);
        HRInfo hrInfo = createOrUpdateHRInfo(null, hrName, hrEmail, company, jobTitle, role, jobURL, advertisedOn,
                redirectAttrs);

        prepareAndSendEmail.sendResumeEmail(hrInfo);

        hrInfoDao.saveOrUpdate(hrInfo);
    }
}
