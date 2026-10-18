package com.example.internhub;

import android.content.Intent;

import android.content.res.ColorStateList;

import android.graphics.Color;

import android.os.Bundle;

import android.util.Log;

import android.view.Gravity;

import android.widget.LinearLayout;

import android.widget.ProgressBar;

import android.widget.TextView;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

import java.util.Collections;

import java.util.Comparator;

import java.util.HashMap;

import java.util.HashSet;

import java.util.List;

import java.util.Locale;

import java.util.Map;

import java.util.Set;

public class IndustryInsightsActivity extends AppCompatActivity {

    private static final String TAG = "InternHubInsights";
    private TextView tvInsightsCount;
    private TextView tvInsightsSummary;
    private TextView tvInsightsMessage;
    private LinearLayout layoutSkillBars;
    private ListenerRegistration internshipListener;
    private final Map<String, SkillCount> skillCounts =
            new HashMap<>();

    private int analyzedInternships = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_industry_insights);

        tvInsightsCount = findViewById(R.id.tvInsightsCount);
        tvInsightsSummary = findViewById(R.id.tvInsightsSummary);
        tvInsightsMessage = findViewById(R.id.tvInsightsMessage);
        layoutSkillBars = findViewById(R.id.layoutSkillBars);

        findViewById(R.id.btnInsightsBack).setOnClickListener(v -> finish());

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        loadIndustryInsights();
    }

    private void loadIndustryInsights() {

        internshipListener = FirebaseFirestore.getInstance()
                .collection("internships")
                .whereEqualTo("status", "approved")
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null) {
                        Log.e(TAG, "Failed to load industry insights", error);

                        showMessage(
                                "Unable to load industry insights. "
                                        + "Please check your connection."
                        );
                        return;
                    }
                    if (snapshots == null) {
                        return;
                    }
                    calculateSkillDemand(snapshots.getDocuments());
                });

    }

    private void calculateSkillDemand(

            List<DocumentSnapshot> documents

    ) {

        skillCounts.clear();
        analyzedInternships = 0;

        for (DocumentSnapshot document : documents) {

            String status = document.getString("status");

            if (!"approved".equals(status)) {
                continue;
            }

            analyzedInternships++;

            String requiredSkills = document.getString("required_skills");

            if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
                continue;
            }

            // Accept comma, semicolon, pipe or newline separators.

            String[] skills = requiredSkills.split("[,;|\\n]+");

            // Count each skill at most once per internship.

            Set<String> seenInThisInternship = new HashSet<>();

            for (String rawSkill : skills) {
                String displayName = rawSkill.trim();
                if (displayName.isEmpty()) {
                    continue;
                }

                String key = displayName.toLowerCase(Locale.ROOT);
                if (!seenInThisInternship.add(key)) {
                    continue;
                }

                SkillCount skillCount = skillCounts.get(key);

                if (skillCount == null) {
                    skillCount = new SkillCount(displayName);
                    skillCounts.put(key, skillCount);
                }

                skillCount.count++;
            }
        }
        displayIndustryInsights();
    }

    private void displayIndustryInsights() {

        tvInsightsCount.setText(analyzedInternships + " approved internships");
        tvInsightsSummary.setText(skillCounts.size() + " distinct skills found across the available listings.");

        layoutSkillBars.removeAllViews();

        tvInsightsMessage.setVisibility(TextView.GONE);

        if (analyzedInternships == 0) {
            showMessage("No approved internships are available to analyze yet.");
            return;
        }

        if (skillCounts.isEmpty()) {
            showMessage("Internships are available, but their required skills "
                    + "have not been provided."
            );
            return;
        }

        List<SkillCount> rankedSkills = new ArrayList<>(skillCounts.values());

        Collections.sort(
                rankedSkills,

                new Comparator<SkillCount>() {

                    @Override
                    public int compare(

                            SkillCount first,
                            SkillCount second
                    ) {
                        int countComparison =
                                Integer.compare(second.count, first.count);

                        if (countComparison != 0) {
                            return countComparison;

                        }
                        return first.name.compareToIgnoreCase(
                                second.name
                        );
                    }
                }
        );

        int maxCount = rankedSkills.get(0).count;

        // Display the ten most frequently requested skills.

        int displayLimit = Math.min(10, rankedSkills.size());

        for (int i = 0; i < displayLimit; i++) {

            SkillCount skill = rankedSkills.get(i);

            LinearLayout row = new LinearLayout(this);

            row.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams rowParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            rowParams.bottomMargin = dpToPx(18);

            row.setLayoutParams(rowParams);

            LinearLayout heading = new LinearLayout(this);

            heading.setOrientation(LinearLayout.HORIZONTAL);

            heading.setGravity(Gravity.CENTER_VERTICAL);

            TextView skillName = createTextView(
                    skill.name,
                    15,
                    "#163653"
            );
            skillName.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );

            heading.addView(
                    skillName,
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
                    )
            );

            TextView skillCount = createTextView(
                    skill.count + (skill.count == 1
                                    ? " internship"
                                    : " internships"
                    ), 12, "#68788A"

            );

            heading.addView(skillCount);
            row.addView(heading);
            ProgressBar bar = new ProgressBar(this, null,
                    android.R.attr.progressBarStyleHorizontal
            );

            bar.setMax(maxCount);
            bar.setProgress(skill.count);
            bar.setProgressTintList(
                    ColorStateList.valueOf(Color.parseColor("#238B73"))
            );

            bar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E2E9E7"))

            );

            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dpToPx(9)
                    );

            barParams.topMargin = dpToPx(8);
            bar.setLayoutParams(barParams);
            row.addView(bar);
            layoutSkillBars.addView(row);

        }
        tvInsightsSummary.setText("Analyzed " + analyzedInternships
                        + " approved listings. Showing the top "
                        + displayLimit + " skills by frequency."
        );
    }

    private void showMessage(String message) {

        tvInsightsMessage.setText(message);
        tvInsightsMessage.setVisibility(TextView.VISIBLE);

    }

    private TextView createTextView(
            String text,
            float size,
            String color
    ) {

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(size);
        textView.setTextColor(Color.parseColor(color));
        return textView;

    }
    private int dpToPx(int dp) {
        return Math.round(
                dp * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    protected void onDestroy() {
        if (internshipListener != null) {
            internshipListener.remove();
        }
        super.onDestroy();
    }

    private static class SkillCount {
        String name;
        int count;
        SkillCount(String name) {
            this.name = name;
            this.count = 0;
        }
    }

}
