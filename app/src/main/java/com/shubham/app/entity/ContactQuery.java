package com.shubham.app.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity(name = "contact_query")
@Table(name = "contact_query")
public class ContactQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "contact_query_id")
    private Long contactQueryId;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "description")
    private String description;

    @Column(name = "time_stamp")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date timeStamp;

    public ContactQuery() {
    }

    public ContactQuery(String name, String email, String phone, String description) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.description = description;
        this.timeStamp = new Date();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getContactQueryId() {
        return contactQueryId;
    }

    public void setContactQueryId(Long contactQueryId) {
        this.contactQueryId = contactQueryId;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
