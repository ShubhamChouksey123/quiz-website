package com.shubham.app.render;

import com.shubham.app.model.ApprovalLevel;
import org.springframework.ui.Model;

public interface RenderAdminTemplate {

    void renderAdminPage(ApprovalLevel approvalLevel, Model model);

    void changeApprovalLevel(Long questionId, ApprovalLevel approvalLevel);
}
