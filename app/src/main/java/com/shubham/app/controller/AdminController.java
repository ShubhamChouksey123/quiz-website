package com.shubham.app.controller;

import com.shubham.app.model.ApprovalLevel;
import com.shubham.app.render.RenderAdminTemplate;
import com.shubham.app.service.questioncrud.QuestionCrud;
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

/**
 * We have used this open-source theme template from
 * <a href="https://themewagon.com/themes/vintagefur/">Theme-wagon template</a>
 */
@Controller
public class AdminController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    private RenderAdminTemplate renderAdminTemplate;
    @Autowired
    private QuestionCrud questionCrud;

    @GetMapping({"/admin",})
    public String renderAdminAllQuestion(
            @RequestParam(value = "approvalLevel", required = false) ApprovalLevel approvalLevel, Model model) {

        renderAdminTemplate.renderAdminPage(approvalLevel, model);
        return "quiz-template/admin";
    }

    @PostMapping(value = {"/change-category"})
    @ResponseBody
    public RedirectView changeCategory(@RequestParam(value = "questionId") Long questionId,
            @RequestParam(value = "approvalLevel") ApprovalLevel approvalLevel,
            @RequestParam(value = "currentView", required = false) String currentView,
            Model model,
            RedirectAttributes redirectAttrs) {

        logger.info("submitted the changeCategory with questionId : {} and approvalLevel : {}", questionId,
                approvalLevel);
        renderAdminTemplate.changeApprovalLevel(questionId, approvalLevel);

        redirectAttrs.addFlashAttribute("questionId", questionId);
        if (approvalLevel == ApprovalLevel.EDIT) {
            return new RedirectView("/add-question");
        }

        // Redirect back to the same filtered view if currentView is specified
        if (currentView != null && !currentView.isEmpty()) {
            return new RedirectView("/admin?approvalLevel=" + currentView);
        }

        return new RedirectView("/admin");
    }

//    @PostMapping(value = {"/delete-question"})
//    @ResponseBody
//    public RedirectView deleteQuestion(@RequestParam(value = "questionId") Long questionId,
//                                       RedirectAttributes redirectAttrs) {
//
//        logger.info("attempting to delete question with questionId: {}", questionId);
//
//        try {
//            boolean deleted = questionCrud.removeQuestions(questionId);
//            if (deleted) {
//                logger.info("successfully deleted question with questionId: {}", questionId);
//                redirectAttrs.addFlashAttribute("successMessage",
//                        "Question " + questionId + " has been successfully deleted.");
//            } else {
//                logger.warn("failed to delete question with questionId: {}", questionId);
//                redirectAttrs.addFlashAttribute("errorMessage",
//                        "Failed to delete question " + questionId + ". Question may not exist.");
//            }
//        } catch (Exception e) {
//            logger.error("error occurred while deleting question with questionId: {}", questionId, e);
//            redirectAttrs.addFlashAttribute("errorMessage",
//                    "An error occurred while deleting question " + questionId + ": " + e.getMessage());
//        }
//
//        return new RedirectView("/admin");
//    }
}
