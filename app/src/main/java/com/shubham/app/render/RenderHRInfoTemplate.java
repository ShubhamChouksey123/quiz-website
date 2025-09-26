package com.shubham.app.render;

import java.math.BigInteger;
import org.springframework.ui.Model;

public interface RenderHRInfoTemplate {
    void renderAllMails(Model model, BigInteger pageNumber, BigInteger pageSize, String searchText);

    void renderDesiredSatelliteEditPage(String satelliteId, Model model);
}
