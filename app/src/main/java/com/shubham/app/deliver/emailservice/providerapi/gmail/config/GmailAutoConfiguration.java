package com.shubham.app.deliver.emailservice.providerapi.gmail.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@AutoConfiguration
@AutoConfigureBefore(MailSenderAutoConfiguration.class)
@ConfigurationPropertiesScan
@ConditionalOnProperty("spring.mail")
public class GmailAutoConfiguration {
    // @Bean
    // EmailProvider emailProvider() {
    // return new GmailSenderService();
    // }
}
