package com.shubham.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.shubham.app.render.RenderHRInfoTemplate;
import com.shubham.app.service.HRInfoService;
import com.shubham.app.service.questioncrud.exception.InvalidRequest;

import java.math.BigInteger;
import java.util.Objects;

import static com.shubham.app.controller.QuizController.ZERO_LENGTH_STRING;

@Controller
public class HRInfoController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private RenderHRInfoTemplate renderHRInfoTemplate;
    @Autowired
    private HRInfoService hrInfoService;

    /**
     * Endpoint to render all mail-info
     *
     * @param model
     * @param searchText
     *            : the search filter
     * @param pageNumber
     *            : the pagination current page number
     * @param pageSize
     *            : the pagination page size
     * @return
     */
    @GetMapping(value = {"/web/mails"})
    public String getHRInfo(Model model, @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(value = "pageNumber", required = false) BigInteger pageNumber,
            @RequestParam(value = "pageSize", required = false) BigInteger pageSize) {

        logger.info("Admin all mails called with pageNumber : {}, pageSize : {} and searchText : {}", pageNumber,
                pageSize, searchText);
        model.addAttribute("errorMessage", model.asMap().get("errorMessage"));

        renderHRInfoTemplate.renderAllMails(model, pageNumber, pageSize, searchText);

        return "send-resume/mails-info";
    }

    /**
     * Endpoint to render the page that contains the form to create the new
     * mail-info
     */
    @GetMapping(value = {"/web/add-mail"})
    public String getHRInfo(@ModelAttribute("hrId") String hrId, Model model) {

        logger.info("adding new mail-info form page called");
        model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        if (!Objects.equals(hrId, ZERO_LENGTH_STRING)) {
            logger.info("edit mail-info page specific for mailId : {}", hrId);
            renderHRInfoTemplate.renderDesiredSatelliteEditPage(hrId, model);
        }

        return "send-resume/edit-mail";
    }

    /**
     * Endpoint used, when user fills and submits the form for creating a new
     * mail-info
     */
    @PostMapping(value = {"/web/mails/create-mail"})
    @ResponseBody
    public Object createOrUpdateHRInfo(@RequestParam(value = "mailIdExisting", required = false) String mailIdExisting,
            @RequestParam(value = "hrName", required = false) String hrName,
            @RequestParam(value = "hrEmail") String hrEmail, @RequestParam(value = "company") String company,
            @RequestParam(value = "jobTitle") String jobTitle, @RequestParam(value = "jobURL") String jobURL,
            @RequestParam(value = "advertisedOn") String advertisedOn,
            @RequestParam(value = "emailSubject", required = false) String emailSubject, Model model,
            RedirectAttributes redirectAttrs) {

        logger.info(
                "Create new mail-info called with mailId : {}, hrName : {}, hrEmail : {}, company : {}, jobTitle : {}, jobURL : {}, and advertisedOn : {}",
                mailIdExisting, hrName, hrEmail, company, jobTitle, jobURL, advertisedOn);
        logger.info("mailIdExisting : {}, and emailSubject : {}", mailIdExisting, emailSubject);

        try {
            /** TODO : think about role */
            hrInfoService.createOrUpdateHRInfo(mailIdExisting, hrName, hrEmail, company, jobTitle, null, jobURL,
                    advertisedOn, emailSubject, redirectAttrs);
        } catch (InvalidRequest e) {
            logger.error("error while creating a launcher : {}", e.getMessage());
            redirectAttrs.addFlashAttribute("errorMessage", "Invalid parameters : " + e.getMessage());
        } catch (Exception e) {
            logger.error("error while creating a launcher : {}", e.getMessage());
            redirectAttrs.addFlashAttribute("errorMessage", "Unable to create launcher : " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        logger.info("Redirecting to all mail page");
        return new RedirectView("/web/mails");
    }

    @PostMapping(value = {"/web/mails/delete-mail"})
    @ResponseBody
    public Object deleteHRInfo(@RequestParam(value = "hrId") String hrId, Model model,
            RedirectAttributes redirectAttrs) {

        logger.info("Deleting the existing hr-info called with id: {}", hrId);
        try {
            hrInfoService.deleteHRInfo(hrId);
            redirectAttrs.addFlashAttribute("successMessage",
                    "Successfully deleted existing hr-info with id : " + hrId);
        } catch (Exception e) {
            logger.error("error while deleting the hr-info : {}", e.getMessage());
            redirectAttrs.addFlashAttribute("errorMessage", "Unable to delete hr-info with cause :" + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        logger.info("Redirecting to all hr-info page");
        return new RedirectView("/web/mails");
    }

    /** Endpoint used, when user clicks on update this mail-info button */
    @PostMapping(value = {"/web/update-mail"})
    @ResponseBody
    public RedirectView updateHRInfo(@RequestParam(value = "hrId") String hrId, Model model,
            RedirectAttributes redirectAttrs) {

        logger.info("submitted the update hr-info with hrId : {}", hrId);
        redirectAttrs.addFlashAttribute("hrId", hrId);

        return new RedirectView("/web/add-mail");
    }
}
