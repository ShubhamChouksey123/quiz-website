package com.shubham.app.render;

import static com.shubham.app.controller.QuizSubmissionController.TOTAL_QUESTIONS_TO_ASK;

import com.shubham.app.emailsender.PrepareAndSendEmail;
import com.shubham.app.entity.Question;
import com.shubham.app.hibernate.dao.ContactQueryDao;
import com.shubham.app.model.ApprovalLevel;
import com.shubham.app.service.questioncrud.QuestionCrud;
import com.shubham.app.service.questioncrud.QuestionsUtils;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class RenderAdminTemplateImpl implements RenderAdminTemplate {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private QuestionCrud questionCrud;
    @Autowired
    private QuestionsUtils questionsUtils;
    @Autowired
    private ContactQueryDao contactQueryDao;
    @Autowired
    private PrepareAndSendEmail prepareAndSendEmail;

    @Override
    public void renderAdminPage(ApprovalLevel approvalLevel, Model model) {

        logger.info("fetching all the questions from our quiz database");

        // Fetch all questions once for efficiency
        List<Question> allQuestions = questionCrud.getAllQuestions(null);

        // Filter questions based on approval level if specified
        List<Question> questions = (approvalLevel == null)
            ? allQuestions
            : allQuestions.stream()
                .filter(q -> q.getApprovalLevel() == approvalLevel)
                .toList();

        List<Long> ids = new ArrayList<>();
        for (Question question : questions) {
            ids.add(question.getQuestionId());
        }

        model.addAttribute("questions", questions);
        model.addAttribute("questionIds", ids);
        model.addAttribute("questionNumberToShow", 1);
        model.addAttribute("totalQuestions", TOTAL_QUESTIONS_TO_ASK);

        // Calculate and add dashboard statistics from the same list
        addDashboardStatistics(allQuestions, model);
    }

    /**
     * used to change the approval level of the question between : NEW, DISCARD,
     * EDIT
     *
     * @param approvalLevel
     * @param questionId
     */
    @Override
    public void changeApprovalLevel(Long questionId, ApprovalLevel approvalLevel) {

        logger.info("changing the approval level of question with id : {} as approval level : {}", questionId,
                approvalLevel);
        Question question = questionCrud.getQuestion(questionId);

        if (question != null) {
            question.setApprovalLevel(approvalLevel);
        }

        questionCrud.saveQuestion(question);
    }

    /**
     * Private helper method to calculate statistics from a questions list
     *
     * @param allQuestions List of all questions
     * @param model Model to add attributes to
     */
    private void addDashboardStatistics(List<Question> allQuestions, Model model) {
        try {
            // Calculate counts from the questions list (no additional database calls)
            long totalApproved = allQuestions.stream()
                .filter(q -> q.getApprovalLevel() == ApprovalLevel.APPROVED)
                .count();

            long totalNew = allQuestions.stream()
                .filter(q -> q.getApprovalLevel() == ApprovalLevel.NEW)
                .count();

            long totalDiscarded = allQuestions.stream()
                .filter(q -> q.getApprovalLevel() == ApprovalLevel.DISCARD)
                .count();

            long totalQuestions = allQuestions.size();

            model.addAttribute("totalQuestions", totalQuestions);
            model.addAttribute("activeQuizzes", totalApproved);

            // For now, set registered users to 0 (can be implemented when user management is added)
            model.addAttribute("registeredUsers", 0);

            // Get quiz completions count (still requires database call)
            long completions = questionCrud.getTopPerformers().stream().count();
            model.addAttribute("completions", completions);

            logger.info("Dashboard statistics - Total Questions: {}, Active Quizzes: {}, Completions: {}",
                    totalQuestions, totalApproved, completions);
        } catch (Exception e) {
            logger.error("Error calculating dashboard statistics", e);
            // Set default values on error
            model.addAttribute("totalQuestions", 0);
            model.addAttribute("activeQuizzes", 0);
            model.addAttribute("registeredUsers", 0);
            model.addAttribute("completions", 0);
        }
    }
}
