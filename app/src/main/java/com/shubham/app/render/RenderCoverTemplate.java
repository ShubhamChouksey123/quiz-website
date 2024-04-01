package com.shubham.app.render;

import org.springframework.ui.Model;

public interface RenderCoverTemplate {

    void renderCoverForm(Model model);

    void renderCoverTemplate(String hiringManagerName, String companyName, String jobTitle);
}
