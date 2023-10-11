package com.shubham.app.deliver.emailservice.providerapi.gmail.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.mail")
@AutoConfiguration
public class GmailProperties {
    private String host;
    private String username;

    public GmailProperties() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "GmailConfiguration{" + "host='" + host + '\'' + ", username='" + username + '\'' + '}';
    }
}
