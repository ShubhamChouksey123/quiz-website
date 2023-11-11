package com.shubham.app.dto;

import com.shubham.app.entity.Question;

public class EachQuestion {

    /** GUI index of the question nu,ber between 0-9 */
    private Integer index;

    /** the uniques identifier of this question, stored as primary key in DB */
    private Long questionId;

    private String statement;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    /** actual answer of the question */
    private Integer ans;

    /** option opted by the user, for this question */
    private Integer useOptedAnswer;

    private String borderColorOptionA;
    private String borderColorOptionB;
    private String borderColorOptionC;
    private String borderColorOptionD;

    public EachQuestion() {
    }

    public EachQuestion(Integer index, Long questionId, String statement, String optionA, String optionB,
            String optionC, String optionD, Integer ans, Integer useOptedAnswer) {
        this.index = index;
        this.questionId = questionId;
        this.statement = statement;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.ans = ans;
        this.useOptedAnswer = useOptedAnswer;
    }

    public EachQuestion(Question question) {
        this.questionId = question.getQuestionId();
        this.statement = question.getStatement();
        this.optionA = question.getOptionA();
        this.optionB = question.getOptionB();
        this.optionC = question.getOptionC();
        this.optionD = question.getOptionD();
        this.ans = question.getAns();
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

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Integer getUseOptedAnswer() {
        return useOptedAnswer;
    }

    public void setUseOptedAnswer(Integer useOptedAnswer) {
        this.useOptedAnswer = useOptedAnswer;
    }

    public String getBorderColorOptionA() {
        return borderColorOptionA;
    }

    public void setBorderColorOptionA(String borderColorOptionA) {
        this.borderColorOptionA = borderColorOptionA;
    }

    public String getBorderColorOptionB() {
        return borderColorOptionB;
    }

    public void setBorderColorOptionB(String borderColorOptionB) {
        this.borderColorOptionB = borderColorOptionB;
    }

    public String getBorderColorOptionC() {
        return borderColorOptionC;
    }

    public void setBorderColorOptionC(String borderColorOptionC) {
        this.borderColorOptionC = borderColorOptionC;
    }

    public String getBorderColorOptionD() {
        return borderColorOptionD;
    }

    public void setBorderColorOptionD(String borderColorOptionD) {
        this.borderColorOptionD = borderColorOptionD;
    }

    @Override
    public String toString() {
        return "EachQuestion{" + "index=" + index + ", questionId=" + questionId + ", ans=" + ans + ", useOptedAnswer="
                + useOptedAnswer + ", borderColorOptionA='" + borderColorOptionA + '\'' + ", borderColorOptionB='"
                + borderColorOptionB + '\'' + ", borderColorOptionC='" + borderColorOptionC + '\''
                + ", borderColorOptionD='" + borderColorOptionD + '\'' + ", statement='" + statement + '\'' + '}';
    }
}
