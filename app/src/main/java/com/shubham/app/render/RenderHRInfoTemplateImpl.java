package com.shubham.app.render;

import static com.shubham.app.controller.QuizController.ZERO_LENGTH_STRING;

import com.shubham.app.dto.HRInfoDTO;
import com.shubham.app.service.HRInfoService;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class RenderHRInfoTemplateImpl implements RenderHRInfoTemplate {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private HRInfoService satelliteService;

    private List<HRInfoDTO> getSatelliteShowList(BigInteger startingPage, BigInteger endingPage, BigInteger limit,
            String searchText) {

        BigInteger start = (startingPage).multiply(limit);
        BigInteger totalMaxPossible = ((endingPage.subtract(startingPage)).add(BigInteger.ONE)).multiply(limit);

        return satelliteService.getHRInfo(start, totalMaxPossible, searchText);
    }

    @Override
    public void renderAllMails(Model model, BigInteger pageNumber, BigInteger pageSize, String searchText) {

        if (pageNumber == null) {
            pageNumber = BigInteger.ONE;
        }
        if (pageSize == null) {
            pageSize = BigInteger.TEN;
        }

        List<HRInfoDTO> mailDTOs = getSatelliteShowList(pageNumber.subtract(BigInteger.ONE),
                pageNumber.subtract(BigInteger.ONE), pageSize, searchText);

        boolean isThisLastPage = false;
        if (mailDTOs.size() < pageSize.intValue()) {
            isThisLastPage = true;
        }

        model.addAttribute("hrs", mailDTOs);
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("isThisLastPage", isThisLastPage);
        model.addAttribute("searchText", searchText);
    }

    @Override
    public void renderDesiredSatelliteEditPage(String id, Model model) {

        HRInfoDTO mailInfoDTO = null;
        if (id == null || Objects.equals(id, ZERO_LENGTH_STRING)) {
            mailInfoDTO = new HRInfoDTO();
            model.addAttribute("isUpdate", 0);
        } else {
            mailInfoDTO = satelliteService.getHRInfo(id);
            model.addAttribute("isUpdate", 1);
        }

        model.addAttribute("hr", mailInfoDTO);
    }
}
