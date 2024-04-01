package com.shubham.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.shubham.app.render.RenderCoverTemplate;
import com.shubham.app.service.questioncrud.exception.InternalServerException;

import java.util.Objects;

/**
 * We have used this open-source theme template from
 * <a href="https://themewagon.com/themes/vintagefur/">Theme-wagon template</a>
 */
@Controller
public class CoverController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private RenderCoverTemplate renderCoverTemplate;

    @GetMapping({"/cover"})
    public String renderAdminAllQuestion(Model model) {
        return "cover-letter/form";
    }

    @PostMapping(value = {"/generate-cover"})
    @ResponseBody
    public RedirectView generateCover(@RequestParam(value = "hiringManagerName") String hiringManagerName,
            @RequestParam(value = "companyName") String companyName, @RequestParam(value = "jobTitle") String jobTitle,
            @RequestParam(value = "jobRole") String jobRole, @RequestParam(value = "advertisedOn") String advertisedOn,
            Model model, RedirectAttributes redirectAttrs) {

        logger.info(
                "submitted the generateCover with hiringManagerName : {} and companyName : {}, and jobTitle : {} and jobRole : {}, advertisedOn : {}",
                hiringManagerName, companyName, jobTitle, jobRole, advertisedOn);

        if ((jobTitle == null || Objects.equals(jobTitle, "")) && jobRole != null) {
            jobTitle = jobRole;
        }

        redirectAttrs.addFlashAttribute("hiringManagerName", hiringManagerName);
        redirectAttrs.addFlashAttribute("companyName", companyName);
        redirectAttrs.addFlashAttribute("jobTitle", jobTitle);
        redirectAttrs.addFlashAttribute("advertisedOn", advertisedOn);

        return new RedirectView("/cover/result");
    }

    @GetMapping({"/cover/result"})
    public String renderResultOfQuiz(@ModelAttribute("hiringManagerName") String hiringManagerName,
            @ModelAttribute("companyName") String companyName, @ModelAttribute("jobTitle") String jobTitle,
            @ModelAttribute("advertisedOn") String advertisedOn, Model model) throws InternalServerException {

        model.addAttribute("hiringManagerName", hiringManagerName);
        model.addAttribute("companyName", companyName);
        model.addAttribute("jobTitle", jobTitle);
        model.addAttribute("advertisedOn", advertisedOn);

        return "cover-letter/result";
    }
}
