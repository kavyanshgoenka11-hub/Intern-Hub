package com.example.internhub;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CareerRoadmapActivity extends AppCompatActivity {

    private LinearLayout layoutRoadmapSteps;
    private ProgressBar progressRoadmap;
    private TextView tvRoadmapPercentage;
    private TextView tvRoadmapProgress;

    private SharedPreferences preferences;

    private final String[] milestoneTitles = {
            "Choose Your Career Path",
            "Build Technical Skills",
            "Create Projects & Resume",
            "Prepare & Apply for Internships"
    };

    private final String[] milestoneDescriptions = {
            "Identify your target domain and explore the skills required for your preferred career.",
            "Learn programming fundamentals, practice DSA and understand the tools used in your chosen domain.",
            "Build practical projects, upload them to GitHub and prepare your resume.",
            "Practice interview questions and apply for relevant internship opportunities."
    };

    private static final String PREFS_NAME = "internhub_career_roadmap_v2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadmap);

        layoutRoadmapSteps = findViewById(R.id.layoutRoadmapSteps);
        progressRoadmap = findViewById(R.id.progressRoadmap);
        tvRoadmapPercentage = findViewById(R.id.tvRoadmapPercentage);
        tvRoadmapProgress = findViewById(R.id.tvRoadmapProgress);

        preferences = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        findViewById(R.id.btnRoadmapBack).setOnClickListener(v ->
                finish()
        );

        createMilestones();
        updateProgress();
    }

    private void createMilestones() {

        layoutRoadmapSteps.removeAllViews();

        for (int i = 0; i < milestoneTitles.length; i++) {

            final int milestoneIndex = i;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_card);
            card.setPadding(
                    dpToPx(14),
                    dpToPx(12),
                    dpToPx(14),
                    dpToPx(12)
            );

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            cardParams.bottomMargin = dpToPx(12);
            card.setLayoutParams(cardParams);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(
                    (i + 1) + ". " + milestoneTitles[i]
            );
            checkBox.setTextColor(Color.parseColor("#163653"));
            checkBox.setTextSize(15);
            checkBox.setButtonTintList(
                    ColorStateList.valueOf(
                            Color.parseColor("#238B73")
                    )
            );
            checkBox.setPadding(
                    dpToPx(2),
                    dpToPx(4),
                    dpToPx(2),
                    dpToPx(4)
            );

            TextView description = new TextView(this);
            description.setText(milestoneDescriptions[i]);
            description.setTextColor(
                    Color.parseColor("#68788A")
            );
            description.setTextSize(13);
            description.setLineSpacing(dpToPx(2), 1.0f);

            LinearLayout.LayoutParams descriptionParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            descriptionParams.leftMargin = dpToPx(36);
            descriptionParams.topMargin = dpToPx(2);

            description.setLayoutParams(descriptionParams);

            // Restore previously saved completion state.
            checkBox.setChecked(
                    preferences.getBoolean(
                            "milestone_" + milestoneIndex,
                            false
                    )
            );

            checkBox.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {

                        preferences.edit()
                                .putBoolean(
                                        "milestone_" + milestoneIndex,
                                        isChecked
                                )
                                .apply();

                        updateProgress();
                    }
            );

            card.addView(checkBox);
            card.addView(description);

            layoutRoadmapSteps.addView(card);
        }
    }

    private void updateProgress() {

        int completed = 0;

        for (int i = 0; i < milestoneTitles.length; i++) {
            if (preferences.getBoolean(
                    "milestone_" + i,
                    false
            )) {
                completed++;
            }
        }

        int percentage = Math.round(
                (completed * 100.0f) / milestoneTitles.length
        );

        progressRoadmap.setMax(milestoneTitles.length);
        progressRoadmap.setProgress(completed);

        tvRoadmapPercentage.setText(percentage + "%");

        tvRoadmapProgress.setText(
                completed + " of " + milestoneTitles.length
                        + " milestones completed"
        );
    }

    private int dpToPx(int dp) {
        return Math.round(
                dp * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}