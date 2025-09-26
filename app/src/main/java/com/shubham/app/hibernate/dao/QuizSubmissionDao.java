package com.shubham.app.hibernate.dao;

import com.shubham.app.entity.QuizSubmission;
import java.util.List;

public interface QuizSubmissionDao {
    void save(QuizSubmission quizSubmission);

    List<QuizSubmission> getTopPerformers(Integer totalResults);
}
