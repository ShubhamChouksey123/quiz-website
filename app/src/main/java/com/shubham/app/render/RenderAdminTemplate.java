package com.shubham.app.render;

import org.springframework.ui.Model;

import com.shubham.app.model.ApprovalLevel;

public interface RenderAdminTemplate {

    void renderAdminPage(ApprovalLevel approvalLevel, Model model);
}
