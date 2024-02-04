package com.shubham.app.dtotoentity;

import org.springframework.stereotype.Service;

import com.shubham.app.dto.*;
import com.shubham.app.entity.ContactQuery;
import com.shubham.app.entity.Question;

@Service
public class DTOToEntity {

    public Question convertQuestionDTO(EachQuestion eachQuestion) {

        return new Question(eachQuestion.getQuestionId(), eachQuestion.getStatement(), eachQuestion.getOptionA(),
                eachQuestion.getOptionB(), eachQuestion.getOptionC(), eachQuestion.getOptionD(), eachQuestion.getAns(),
                eachQuestion.getDifficulty());
    }

    public ContactQuery convertContactQueryDTO(ContactQueryResponse contactQueryResponse) {

        return new ContactQuery(contactQueryResponse.getName(), contactQueryResponse.getEmail(),
                contactQueryResponse.getPhone(), contactQueryResponse.getDescription());
    }
}
