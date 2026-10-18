package com.example.internhub;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import androidx.appcompat.app.AppCompatActivity;

public class InternshipActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private ListenerRegistration internshipListener;
    private LinearLayout internshipList;
    private TextView tvInternshipCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internships);

        databaseHelper = new DatabaseHelper(this);

        internshipList = findViewById(R.id.internshipList);
        tvInternshipCount = findViewById(R.id.tvInternshipCount);

        TextView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        loadInternshipsFromCloud();
    }

    private void loadInternshipsFromCloud() {

        internshipList.removeAllViews();

        internshipListener = FirebaseFirestore.getInstance()
                .collection("internships")
                .whereEqualTo("status", "approved")
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null) {
                        tvInternshipCount.setText("Unable to load internships");

                        android.util.Log.e("InternHubCloud", "Failed to read internships", error);
                        return;
                    }

                    if (snapshots == null) {
                        return;
                    }

                    internshipList.removeAllViews();

                    List<String> activeCloudIds = new ArrayList<>();

                    int count = 0;

                    for (DocumentSnapshot document :
                            snapshots.getDocuments()) {
                        String status = document.getString("status");

                        if (!"approved".equals(status)) {
                            continue;
                        }

                        activeCloudIds.add(document.getId());

                        String title = document.getString("title");
                        String company = document.getString("company");
                        String domain = document.getString("domain");
                        String skills = document.getString("required_skills");
                        String location = document.getString("location");
                        String workMode = document.getString("work_mode");
                        String stipend = document.getString("stipend");
                        String deadline = document.getString("deadline");
                        String description = document.getString("description");
                        String applicationUrl = document.getString("application_url");

// Save or update this cloud record in local SQLite
                        long localId = databaseHelper.upsertCloudInternship(
                                document.getId(),
                                title,
                                company,
                                domain,
                                skills,
                                description,
                                location,
                                workMode,
                                stipend,
                                deadline,
                                applicationUrl,
                                status
                        );

                        if (localId == -1) {
                            android.util.Log.e(
                                    "InternHubSync",
                                    "Failed to sync internship: " + document.getId()
                            );
                        }

                        addInternshipCard(
                                title == null ? "" : title,
                                company == null ? "" : company,
                                domain == null ? "" : domain,
                                skills == null ? "" : skills,
                                location == null ? "" : location,
                                workMode == null ? "" : workMode,
                                stipend == null ? "" : stipend,
                                deadline == null ? "" : deadline
                        );

                        count++;
                    }

                    // Delete stale cloud records only after a server-confirmed snapshot.
                    if (!snapshots.getMetadata().isFromCache()
                            && !snapshots.getMetadata().hasPendingWrites()) {

                        int removedCount =
                                databaseHelper.deleteCloudInternshipsNotIn(activeCloudIds);

                        android.util.Log.d(
                                "InternHubSync",
                                "Stale cloud records removed: " + removedCount
                        );
                    }

                    tvInternshipCount.setText("Available internships (" + count + ")");

                    if (count == 0) {
                        TextView emptyMessage = createTextView(
                                "No internships available yet.\n"
                                        + "Please check again later.",
                                14,
                                "#68788A"
                        );

                        emptyMessage.setGravity(Gravity.CENTER);
                        emptyMessage.setPadding(20, 40, 20, 40);

                        internshipList.addView(emptyMessage);
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
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

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

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            if (internshipListener != null) {
                internshipListener.remove();
            }
            databaseHelper.close();
        }

        super.onDestroy();
    }
}