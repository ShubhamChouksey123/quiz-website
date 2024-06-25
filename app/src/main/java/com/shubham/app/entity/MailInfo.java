package com.shubham.app.entity;

import java.util.Date;
import jakarta.persistence.*;

@Entity(name = "mail_info")
@Table(name = "mail_info")
public class MailInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "mail_id")
    private Long mailId;

    @Column(name = "hr_id")
    private Long hrId;

    @Column(name = "job_url")
    private String jobURL;

    @Column(name = "sent_at")
    private Date sentAt;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "hr_info_to_mail_info_id", referencedColumnName = "hr_id")
    private HRInfo hrInfo;

    public MailInfo() {
    }

    public MailInfo(Long hrId, String jobURL, Date sentAt) {
        this.hrId = hrId;
        this.jobURL = jobURL;
        this.sentAt = sentAt;
    }

    public Long getMailId() {
        return mailId;
    }

    public void setMailId(Long mailId) {
        this.mailId = mailId;
    }

    public HRInfo getHrInfo() {
        return hrInfo;
    }

    public void setHrInfo(HRInfo hrInfo) {
        this.hrInfo = hrInfo;
    }

    public Long getHrId() {
        return hrId;
    }

    public void setHrId(Long hrId) {
        this.hrId = hrId;
    }

    public String getJobURL() {
        return jobURL;
    }

    public void setJobURL(String jobURL) {
        this.jobURL = jobURL;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    @Override
    public String toString() {
        return "MailInfo{" + "mailId=" + mailId + ", hrId=" + hrId + ", jobURL='" + jobURL + '\'' + ", sentAt=" + sentAt
                + '}';
    }
}
