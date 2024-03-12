package com.shubham.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.shubham.app.model.ApprovalLevel;
import com.shubham.app.render.RenderAdminTemplate;

/**
 * We have used this open-source theme template from
 * <a href="https://themewagon.com/themes/vintagefur/">Theme-wagon template</a>
 */
@Controller
public class AdminController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private RenderAdminTemplate renderAdminTemplate;

    @GetMapping({"/admin",})
    public String renderAdminAllQuestion(
            @RequestParam(value = "approvalLevel", required = false) ApprovalLevel approvalLevel, Model model) {

        renderAdminTemplate.renderAdminPage(approvalLevel, model);
        return "quiz-template/admin";
    }

    @PostMapping(value = {"/change-category"})
    @ResponseBody
    public RedirectView changeCategory(@RequestParam(value = "questionId") Long questionId,
            @RequestParam(value = "approvalLevel") ApprovalLevel approvalLevel, Model model,
            RedirectAttributes redirectAttrs) {

        logger.info("submitted the changeCategory with questionId : {} and approvalLevel : {}", questionId,
                approvalLevel);
        renderAdminTemplate.changeApprovalLevel(questionId, approvalLevel);

        redirectAttrs.addFlashAttribute("questionId", questionId);
        if (approvalLevel == ApprovalLevel.EDIT) {
            return new RedirectView("/add-question");
        }

        return new RedirectView("/admin");
    }
}
