package com.martabak.kamar.activity.survey;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.martabak.kamar.R;
import com.martabak.kamar.activity.guest.AbstractGuestBarsActivity;
import com.martabak.kamar.domain.SurveyQuestion;
import com.martabak.kamar.domain.managers.SurveyManager;
import com.martabak.kamar.service.SurveyServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;

public class SurveyActivity extends AbstractGuestBarsActivity {

    @BindView(R.id.survey_pager) ViewPager viewPager;
    @BindView(R.id.survey_button_next) ImageButton nextBtn;

    //on next click
    @OnClick(R.id.survey_button_next)
    void onNextClick(){
        if (viewPager.getCurrentItem() < viewPager.getAdapter().getCount() - 1) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        }
    }

    //on previous click
    @OnClick(R.id.survey_button_previous)
    void onPreviousClick() {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);

        }
    }

    private List<String> sections;
    private HashMap<String, List<SurveyQuestion>> sectionMappings; //section to questions
    private HashMap<String, Integer> idToRating;
    private PagerAdapter pagerAdapter;

    protected Options getOptions() {
        return new Options()
                .withBaseLayout(R.layout.activity_survey)
                .withToolbarLabel(getString(R.string.survey_label))
                .showTabLayout(false)
                .showLogoutIcon(false)
                .enableChatIcon(true)
                .enableordersIcon(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        // Instantiate a ViewPager and a PagerAdapter.
        viewPager.setOffscreenPageLimit(10); //maintains views of off-screen slides

        if (SurveyManager.getInstance().getMappings() == null) {
            sections = new ArrayList<>();
            idToRating = new HashMap<>();
            sectionMappings = new HashMap<>();
            SurveyServer.getInstance(this).getSurveyQuestions()
                    .subscribe(new Observer<List<SurveyQuestion>>() {
                        List<SurveyQuestion> surveyQuestions;

                        @Override
                        public void onCompleted() {
                            Log.d(SurveyActivity.class.getCanonicalName(), String.valueOf(surveyQuestions.size()));
                            Log.d(SurveyActivity.class.getCanonicalName(), "getSurveyQuestions() On completed");
                            for (SurveyQuestion sq : surveyQuestions) {
                                String currSection = sq.getSection();
                                idToRating.put(sq._id, 0);
                                if (!sections.contains(currSection)) { //new section found
                                    List<SurveyQuestion> ids = new ArrayList<>();
                                    ids.add(sq);
                                    sectionMappings.put(currSection, ids);
                                    sections.add(currSection);
                                } else {
                                    List<SurveyQuestion> sql = sectionMappings.get(currSection);
                                    sql.add(sq);
                                    sectionMappings.put(currSection, sql);
                                }
                            }
                            SurveyManager.getInstance().setSections(sections);
                            SurveyManager.getInstance().setRatings(idToRating);
                            SurveyManager.getInstance().setMapping(sectionMappings);
                            pagerAdapter = new SurveySlidePagerAdapter(getSupportFragmentManager());
                            viewPager.setAdapter(pagerAdapter);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(SurveyActivity.class.getCanonicalName(), "getSurveyQuestions() On error");
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(List<SurveyQuestion> results) {
                            surveyQuestions = results;
                            Log.d(SurveyActivity.class.getCanonicalName(), "getSurveyQuestions() On next");
                        }
                    });
        } else {
            pagerAdapter = new SurveySlidePagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(pagerAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

}
