package com.shubham.app.dto;

import java.util.Date;
import java.util.List;
import jakarta.validation.constraints.NotNull;

public class HRInfoDTO {

    private Long id;
    private String hrName;
    private String hrEmail;
    private List<String> hrEmails;
    @NotNull private String company;
    private String jobTitle;
    private String role;
    private String jobURL;
    private String advertisedOn;

    private Integer times;

    private Date lastSentAt;

    public HRInfoDTO() {
    }

    public HRInfoDTO(Long id, String hrName, List<String> hrEmails, String company, String jobTitle, String role,
            String jobURL, String advertisedOn, Integer times) {
        this.id = id;
        this.hrName = hrName;
        this.hrEmails = hrEmails;
        this.company = company;
        this.jobTitle = jobTitle;
        this.role = role;
        this.jobURL = jobURL;
        this.advertisedOn = advertisedOn;
        this.times = times;
        append();
    }

    private void append() {
        if (this.hrEmails == null || this.hrEmails.isEmpty()) {
            return;
        }

        StringBuilder ans = new StringBuilder(this.hrEmails.get(0));
        for (int i = 1; i < this.hrEmails.size(); i++) {
            ans.append(",").append(this.hrEmails.get(i));
        }
        this.hrEmail = ans.toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHrName() {
        return hrName;
    }

    public void setHrName(String hrName) {
        this.hrName = hrName;
    }

    public String getHrEmail() {
        return hrEmail;
    }

    public void setHrEmail(String hrEmail) {
        this.hrEmail = hrEmail;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
        return "HRInfoDTO{" + "id=" + id + ", hrName='" + hrName + '\'' + ", hrEmail='" + hrEmail + '\'' + ", hrEmails="
                + hrEmails + ", company='" + company + '\'' + ", jobTitle='" + jobTitle + '\'' + ", role='" + role
                + '\'' + ", jobURL='" + jobURL + '\'' + ", advertisedOn='" + advertisedOn + '\'' + '}';
    }
}
