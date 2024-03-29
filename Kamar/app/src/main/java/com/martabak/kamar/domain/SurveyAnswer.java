package com.martabak.kamar.domain;

import com.google.gson.annotations.SerializedName;

public class SurveyAnswer {

    @SerializedName("question_id") public final String questionId;

    public final String section;

    public final String question;

    public final Integer rating;

    public SurveyAnswer() {
        this.questionId = null;
        this.section = null;
        this.question = null;
        this.rating = null;
    }


    public SurveyAnswer(String questionId, String section, String question, Integer rating) {
        this.section = section;
        this.question = question;
        this.questionId = questionId;
        this.rating = rating;
    }

}
