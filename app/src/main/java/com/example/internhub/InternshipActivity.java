package com.example.internhub;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class InternshipActivity extends AppCompatActivity {

    private static final String TAG = "InternHubCloud";

    private ListenerRegistration internshipListener;

    private LinearLayout internshipList;
    private TextView tvInternshipCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internships);

        internshipList = findViewById(R.id.internshipList);
        tvInternshipCount = findViewById(R.id.tvInternshipCount);

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadInternshipsFromCloud();
    }

    private void loadInternshipsFromCloud() {

        tvInternshipCount.setText("Loading internships...");

        internshipListener = FirebaseFirestore.getInstance()
                .collection("internships")
                .whereEqualTo("status", "approved")
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null) {

                        Log.e(
                                TAG,
                                "Failed to load internships from Firestore",
                                error
                        );

                        tvInternshipCount.setText(
                                "Unable to load internships"
                        );

                        showMessage(
                                "Please check your internet connection "
                                        + "and try again."
                        );

                        return;
                    }

                    if (snapshots == null) {
                        return;
                    }

                    internshipList.removeAllViews();

                    int count = 0;

                    for (DocumentSnapshot document
                            : snapshots.getDocuments()) {

                        String status = document.getString("status");

                        if (!"approved".equals(status)) {
                            continue;
                        }

                        String title = safe(
                                document.getString("title")
                        );

                        String company = safe(
                                document.getString("company")
                        );

                        String domain = safe(
                                document.getString("domain")
                        );

                        String skills = safe(
                                document.getString("required_skills")
                        );

                        String location = safe(
                                document.getString("location")
                        );

                        String workMode = safe(
                                document.getString("work_mode")
                        );

                        String stipend = safe(
                                document.getString("stipend")
                        );

                        String deadline = safe(
                                document.getString("deadline")
                        );

                        addInternshipCard(
                                title,
                                company,
                                domain,
                                skills,
                                location,
                                workMode,
                                stipend,
                                deadline
                        );

                        count++;
                    }

                    tvInternshipCount.setText(
                            "Available internships (" + count + ")"
                    );

                    if (count == 0) {
                        showMessage(
                                "No internships available yet.\n"
                                        + "Please check again later."
                        );
                    }
                });
    }

    private void addInternshipCard(
            String title,
            String company,
            String domain,
            String skills,
            String location,
            String workMode,
            String stipend,
            String deadline
    ) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 18, 18, 18);
        card.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, 14);
        card.setLayoutParams(cardParams);

        TextView titleView = createTextView(
                title, 19, "#163653"
        );

        titleView.setTypeface(null, Typeface.BOLD);

        TextView companyView = createTextView(
                company, 14, "#3B638D"
        );

        TextView domainView = createTextView(
                "Domain: " + domain, 13, "#68788A"
        );

        TextView skillsView = createTextView(
                "Required skills: " + skills, 13, "#34495E"
        );

        TextView locationView = createTextView(
                "Location: " + location + " | " + workMode,
                13, "#34495E"
        );

        TextView stipendView = createTextView(
                "Stipend: " + stipend, 13, "#34495E"
        );

        TextView deadlineView = createTextView(
                "Apply before: " + deadline, 13, "#B4533C"
        );

        card.addView(titleView);
        card.addView(companyView);
        card.addView(domainView);
        card.addView(skillsView);
        card.addView(locationView);
        card.addView(stipendView);
        card.addView(deadlineView);

        internshipList.addView(card);
    }

    private TextView createTextView(
            String text,
            int textSize,
            String textColor
    ) {

        TextView textView = new TextView(this);

        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setTextColor(Color.parseColor(textColor));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, 6);
        textView.setLayoutParams(params);

        return textView;
    }

    private String safe(String value) {

        return value == null || value.trim().isEmpty()
                ? "Not specified"
                : value;
    }

    private void showMessage(String message) {

        internshipList.removeAllViews();

        TextView emptyMessage = createTextView(
                message,
                14,
                "#68788A"
        );

        emptyMessage.setGravity(Gravity.CENTER);
        emptyMessage.setPadding(20, 40, 20, 40);

        internshipList.addView(emptyMessage);
    }

    @Override
    protected void onDestroy() {

        if (internshipListener != null) {
            internshipListener.remove();
        }

        super.onDestroy();
    }
}