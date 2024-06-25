package com.shubham.app.hibernate.dao;

import com.shubham.app.entity.HRInfo;

import java.math.BigInteger;
import java.util.List;

public interface HRInfoDao {
    void saveOrUpdate(HRInfo resumeMailInfo);

    HRInfo getHRInfoById(String id);

    List<HRInfo> getAllHRInfo();

    List<HRInfo> getHRInfo(BigInteger firstResult, BigInteger maxResults);

    List<HRInfo> getHRInfo(BigInteger firstResult, BigInteger maxResults, String searchText);

    void deleteHRInfo(String launcherId);
}
