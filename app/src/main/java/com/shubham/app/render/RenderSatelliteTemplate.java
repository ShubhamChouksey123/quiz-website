package com.shubham.app.render;

import org.springframework.ui.Model;

import java.math.BigInteger;

public interface RenderSatelliteTemplate {
    void renderAllMails(Model model, BigInteger pageNumber, BigInteger pageSize, String searchText);

    void renderDesiredSatelliteEditPage(String satelliteId, Model model);
}
