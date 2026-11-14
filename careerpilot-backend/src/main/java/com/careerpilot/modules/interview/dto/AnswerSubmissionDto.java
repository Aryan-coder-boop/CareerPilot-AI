package com.careerpilot.modules.interview.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnswerSubmissionDto {

    @Min(value = 1, message = "Question index must be at least 1")
    private int questionIndex;

    @NotBlank(message = "Answer text cannot be blank")
    @Size(min = 5, message = "Answer must contain at least 5 characters")
    private String answerText;

    public AnswerSubmissionDto() {
    }

    public AnswerSubmissionDto(int questionIndex, String answerText) {
        this.questionIndex = questionIndex;
        this.answerText = answerText;
    }

    public int getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }
}

