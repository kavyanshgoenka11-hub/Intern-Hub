package com.example.internhub;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InternshipActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
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

        loadInternships();
    }

    private void loadInternships() {

        internshipList.removeAllViews();

        Cursor cursor = databaseHelper.getApprovedInternships();

        int count = cursor.getCount();

        tvInternshipCount.setText(
                "Available internships (" + count + ")"
        );

        if (count == 0) {
            TextView emptyMessage = createTextView(
                    "No internships available yet.\nPlease check again later.",
                    14,
                    "#68788A"
            );

            emptyMessage.setGravity(Gravity.CENTER);
            emptyMessage.setPadding(20, 40, 20, 40);

            internshipList.addView(emptyMessage);
            cursor.close();
            return;
        }

        try {
            while (cursor.moveToNext()) {

                String title = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_TITLE
                        )
                );

                String company = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_COMPANY
                        )
                );

                String domain = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_DOMAIN
                        )
                );

                String skills = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_SKILLS
                        )
                );

                String location = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_LOCATION
                        )
                );

                String workMode = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_WORK_MODE
                        )
                );

                String stipend = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_STIPEND
                        )
                );

                String deadline = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_DEADLINE
                        )
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
            }

        } finally {
            cursor.close();
        }
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
            databaseHelper.close();
        }

        super.onDestroy();
    }
}