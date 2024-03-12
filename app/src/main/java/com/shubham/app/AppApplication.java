package com.shubham.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
@ComponentScan({"com.shubham"})
public class AppApplication {

    public static void main(String[] args) {

        SpringApplication.run(AppApplication.class, args);

        // Question question = new Question("What's your name ?", "Shubham", "Shyam",
        // "Ram",
        // "Mohan", Integer.valueOf(0), Difficulty.LOW);
        // questionCrud.addQuestion("What's your name ?", "Shubham", "Shyam", "Ram",
        // "Mohan",
        // Integer.valueOf(0), Difficulty.LOW);
    }
}
