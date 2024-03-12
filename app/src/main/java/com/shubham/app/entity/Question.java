package com.shubham.app.entity;

import com.shubham.app.model.ApprovalLevel;
import com.shubham.app.model.DifficultyLevel;
import com.shubham.app.model.QuestionCategory;

import jakarta.persistence.*;

@Entity(name = "question")
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "statement")
    private String statement;

    @Column(name = "option_a")
    private String optionA;

    @Column(name = "option_b")
    private String optionB;

    @Column(name = "option_c")
    private String optionC;

    @Column(name = "option_d")
    private String optionD;

    @Column(name = "ans")
    private Integer ans;

    @Column(name = "difficulty")
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    @Column(name = "approval_level")
    @Enumerated(EnumType.STRING)
    private ApprovalLevel approvalLevel;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private QuestionCategory category;

    public Question(Long questionId, String statement, String optionA, String optionB, String optionC, String optionD,
            Integer ans, DifficultyLevel difficulty, QuestionCategory category, ApprovalLevel approvalLevel) {
        this.questionId = questionId;
        this.statement = statement;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.ans = ans;
        this.difficulty = difficulty;
        this.category = category;
        this.approvalLevel = approvalLevel;
    }

    public Question() {
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public Integer getAns() {
        return ans;
    }

    public void setAns(Integer ans) {
        this.ans = ans;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public ApprovalLevel getApprovalLevel() {
        return approvalLevel;
    }

    public void setApprovalLevel(ApprovalLevel approvalLevel) {
        this.approvalLevel = approvalLevel;
    }

    public QuestionCategory getCategory() {
        return category;
    }

    public void setCategory(QuestionCategory category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Question{" + "questionId=" + questionId + ", statement='" + statement + '\'' + ", optionA='" + optionA
                + '\'' + ", optionB='" + optionB + '\'' + ", optionC='" + optionC + '\'' + ", optionD='" + optionD
                + '\'' + ", ans=" + ans + ", difficulty=" + difficulty + ", approvalLevel=" + approvalLevel
                + ", category=" + category + '}';
    }
}
