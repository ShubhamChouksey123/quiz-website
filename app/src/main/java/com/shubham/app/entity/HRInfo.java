package com.shubham.app.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name = "hr_info")
@Table(name = "hr_info")
public class HRInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "hr_id")
    private Long hrId;

    @Column(name = "hr_name")
    private String hrName;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> hrEmails;

    @Column(name = "company")
    private String company;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "job_url")
    private String jobURL;

    @Column(name = "advertised_on")
    private String advertisedOn;

    @Column(name = "email_subject")
    private String emailSubject;

    @Column(name = "created_at")
    private Date createdAt;

    @OneToMany(mappedBy = "hrInfo", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<MailInfo> mailInfoList;

    @Column(name = "times")
    private Integer times;

    @Column(name = "last_sent_at")
    private Date lastSentAt;

    public HRInfo() {
    }

    public HRInfo(String hrName, String company, List<String> hrEmails) {
        this.hrName = hrName;
        this.company = company;
        this.hrEmails = hrEmails;
    }

    public HRInfo(String hrName, List<String> hrEmails, String company, String jobTitle, String jobURL,
            String advertisedOn, String emailSubject, Date createdAt, Integer times) {
        this.hrName = hrName;
        this.hrEmails = hrEmails;
        this.company = company;
        this.jobTitle = jobTitle;
        this.jobURL = jobURL;
        this.advertisedOn = advertisedOn;
        this.emailSubject = emailSubject;
        this.createdAt = createdAt;
        this.times = times;
    }

    public Long getHrId() {
        return hrId;
    }

    public void setHrId(Long hrId) {
        this.hrId = hrId;
    }

    public String getHrName() {
        return hrName;
    }

    public void setHrName(String hrName) {
        this.hrName = hrName;
    }

    public List<String> getHrEmails() {
        return hrEmails;
    }

    public void setHrEmails(List<String> hrEmails) {
        this.hrEmails = hrEmails;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobURL() {
        return jobURL;
    }

    public void setJobURL(String jobURL) {
        this.jobURL = jobURL;
    }

    public String getAdvertisedOn() {
        return advertisedOn;
    }

    public void setAdvertisedOn(String advertisedOn) {
        this.advertisedOn = advertisedOn;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<MailInfo> getMailInfoList() {
        return mailInfoList;
    }

    public void setMailInfoList(List<MailInfo> mailInfoList) {
        this.mailInfoList = mailInfoList;
    }

    public void addMailSendInfo(MailInfo mailInfo) {
        if (this.mailInfoList == null) {
            this.mailInfoList = new ArrayList<>();
        }
        mailInfoList.add(mailInfo);
        mailInfo.setHrInfo(this);
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    public Date getLastSentAt() {
        return lastSentAt;
    }

    public void setLastSentAt(Date lastSentAt) {
        this.lastSentAt = lastSentAt;
    }

    @Override
    public String toString() {
        return "HRInfo{" + "hrId=" + hrId + ", hrName='" + hrName + '\'' + ", hrEmails=" + hrEmails + ", company='"
                + company + '\'' + ", jobTitle='" + jobTitle + '\'' + ", jobURL='" + jobURL + '\'' + ", advertisedOn='"
                + advertisedOn + '\'' + ", createdAt=" + createdAt + ", mailInfoList=" + mailInfoList + ", times="
                + times + ", lastSentAt=" + lastSentAt + '}';
    }
}
