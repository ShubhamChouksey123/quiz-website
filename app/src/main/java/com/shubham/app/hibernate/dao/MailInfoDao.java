package com.shubham.app.hibernate.dao;

import com.shubham.app.entity.HRInfo;

import java.math.BigInteger;
import java.util.List;

public interface MailInfoDao {
    void saveOrUpdate(HRInfo resumeMailInfo);

    HRInfo getResumeMailInfoById(String id);

    List<HRInfo> getAllResumeMailInfo();

    List<HRInfo> getResumeMailInfo(BigInteger firstResult, BigInteger maxResults);

    List<HRInfo> getResumeMailInfo(BigInteger firstResult, BigInteger maxResults, String searchText);

    void deleteResumeMailInfo(String launcherId);
}
