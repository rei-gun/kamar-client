package com.martabak.kamar.service;

import com.martabak.kamar.domain.Feedback;
import com.martabak.kamar.service.response.PostResponse;
import com.martabak.kamar.domain.SurveyAnswer;
import com.martabak.kamar.domain.SurveyQuestion;
import com.martabak.kamar.service.response.ViewResponse;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Provides feedback and survey related functionality.
 */
public interface FeedbackService {

    @POST("feedback")
    Observable<PostResponse> createFeedback(@Body Feedback feedback);

    @GET("survey_question/_design/survey_question/_view/survey_questions")
    Observable<ViewResponse<SurveyQuestion>> getSurveyQuestions();

    @POST("survey_answer")
    Observable<PostResponse> createSurveyAnswer(@Body SurveyAnswer surveyAnswer);

}
