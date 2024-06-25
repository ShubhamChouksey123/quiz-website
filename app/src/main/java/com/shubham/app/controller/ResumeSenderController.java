package com.shubham.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.shubham.app.emailsender.PrepareAndSendEmailImpl;
import com.shubham.app.render.RenderCoverTemplate;
import com.shubham.app.service.HRInfoService;
import com.shubham.app.service.questioncrud.exception.InvalidRequest;
import com.shubham.app.utils.GeneralUtility;

import java.math.BigInteger;

@Controller
public class ResumeSenderController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private PrepareAndSendEmailImpl sendVerificationCode;

    @Autowired
    private HRInfoService hrInfoService;

    @Autowired
    private RenderCoverTemplate renderCoverTemplate;
    @Autowired
    private GeneralUtility generalUtility;

    @GetMapping("/send_email_hr")
    public String sendHTMLEmailWith() throws InvalidRequest {

        // sendVerificationCode.sendResumeEmail("Sunayana",
        // "shubhamchouksey1998@gmail.com", "New
        // Relic",
        // "Software Engineer SDE-2", null, "https://", "LinkedIn");

        return "result";
    }

    @GetMapping({"/send-resume"})
    public String renderAdminAllQuestion(Model model) {
        return "send-resume/form";
    }

    @PostMapping(value = {"/send-resume"})
    public String createHRInfoAndSave(
            @RequestParam(value = "hiringManagerName", required = false) String hiringManagerName,
            @RequestParam(value = "hiringManagerEmail") String hiringManagerEmail,
            @RequestParam(value = "companyName") String companyName, @RequestParam(value = "jobTitle") String jobTitle,
            @RequestParam(value = "jobRole") String jobRole, @RequestParam(value = "jobURL") String jobURL,
            @RequestParam(value = "advertisedOn") String advertisedOn,
            @RequestParam(value = "emailSubject", required = false) String emailSubject, Model model,
            RedirectAttributes redirectAttrs) {

        logger.info(
                "submitted the sending email with hiringManagerName : {}, hiringManagerEmail : {} and companyName : {}, and jobTitle : {} and jobRole : {}, advertisedOn : {}",
                hiringManagerName, hiringManagerEmail, companyName, jobTitle, jobRole, advertisedOn);
        logger.info("submitted the sending email with jobURL : {}", jobURL);

        try {
            hrInfoService.saveAndsSendResumeEmail(hiringManagerName, hiringManagerEmail, companyName, jobTitle, jobRole,
                    jobURL, advertisedOn, emailSubject, redirectAttrs);
            model.addAttribute("successMessage", "Successfully sent resume to : " + hiringManagerEmail);
        } catch (Exception e) {
            logger.error("error while sending the resume with cause : {}", e.getMessage());
            model.addAttribute("errorMessage",
                    "Unable to send resume to " + hiringManagerEmail + " with cause : " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return "send-resume/form";
    }

    @PostMapping(value = {"/web/mails/send-mail-again"})
    @ResponseBody
    public Object deleteMailInfo(@RequestParam(value = "hrId") String hrId,
            @RequestParam(value = "pageNumber", required = false) BigInteger pageNumber,
            @RequestParam(value = "pageSize", required = false) BigInteger pageSize, Model model,
            RedirectAttributes redirectAttrs) {

        logger.info("Sending mail again to the existing hr called with id: {}", hrId);
        logger.info("pageNumber: {} and pageSize : {}", pageNumber, pageSize);
        try {
            hrInfoService.sendResumeEmail(hrId);
            redirectAttrs.addFlashAttribute("successMessage",
                    "Successfully sent mail to existing hr with id : " + hrId);
        } catch (Exception e) {
            logger.error("error while sending mail to existing hr with cause: {}", e.getMessage());
            redirectAttrs.addFlashAttribute("errorMessage",
                    "Unable to sending mail to existing hr with cause :" + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        if (!generalUtility.isNullOrEmpty(pageNumber)) {
            redirectAttrs.addFlashAttribute("pageNumber", pageNumber);
        }

        logger.info("Redirecting to all hr-info page");
        return new RedirectView("/web/mails");
    }

    @GetMapping({"/resume-builder/result"})
    public String renderResultOfQuiz(@ModelAttribute("hiringManagerName") String hiringManagerName,
            @ModelAttribute("companyName") String companyName, @ModelAttribute("jobTitle") String jobTitle,
            @ModelAttribute("advertisedOn") String advertisedOn, Model model) {

        model.addAttribute("hiringManagerName", hiringManagerName);
        model.addAttribute("companyName", companyName);
        model.addAttribute("jobTitle", jobTitle);
        model.addAttribute("advertisedOn", advertisedOn);

        return "cover-letter/result";
    }
}
