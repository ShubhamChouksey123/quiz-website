package com.shubham.app;

import com.shubham.app.service.questioncrud.QuestionCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
@ComponentScan({"com.shubham.app"})
public class AppApplication {

    private static QuestionCrud questionCrud;

    public static void main(String[] args) {

        SpringApplication.run(AppApplication.class, args);

//        Question question = new Question("What's your name ?", "Shubham", "Shyam", "Ram", "Mohan", Integer.valueOf(0), Difficulty.LOW);
//        questionCrud.addQuestion("What's your name ?", "Shubham", "Shyam", "Ram", "Mohan", Integer.valueOf(0), Difficulty.LOW);
    }

    public QuestionCrud getQuestionCrud() {
        return questionCrud;
    }

    @Autowired
    public void setQuestionCrud(QuestionCrud questionCrud) {
        AppApplication.questionCrud = questionCrud;
    }

}
