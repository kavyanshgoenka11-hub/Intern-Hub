package com.example.internhub;

import android.graphics.Color;

import android.graphics.Typeface;

import android.os.Bundle;

import android.util.Log;

import android.view.Gravity;

import android.view.View;

import android.widget.Button;

import android.widget.LinearLayout;

import android.widget.TextView;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.FieldValue;

import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

import java.util.Locale;

public class AdminMentorshipRequestsActivity extends AppCompatActivity {

    private static final String TAG = "InternHubRequests";
    private LinearLayout layoutRequests;
    private TextView tvRequestCount;
    private TextView tvRequestsMessage;
    private FirebaseFirestore firestore;
    private ListenerRegistration requestsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_mentorship_requests);

        layoutRequests = findViewById(R.id.layoutMentorshipRequests);
        tvRequestCount = findViewById(R.id.tvRequestCount);
        tvRequestsMessage = findViewById(R.id.tvRequestsMessage);
        firestore = FirebaseFirestore.getInstance();

        findViewById(R.id.btnRequestsBack).setOnClickListener(v -> finish());

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in first.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        loadRequests();
    }

    private void loadRequests() {
        requestsListener = firestore
                .collection("mentorshipRequests")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Failed to load mentorship requests", error);
                        tvRequestCount.setText("Requests unavailable");
                        showMessage("Unable to load requests. Check your admin access.");
                        return;
                    }
                    if (snapshots == null) {
                        return;
                    }
                    displayRequests(snapshots.getDocuments());
                });
    }

    private void displayRequests(List<DocumentSnapshot> documents) {

        layoutRequests.removeAllViews();
        tvRequestsMessage.setVisibility(View.GONE);
        int pendingCount = 0;
        for (DocumentSnapshot document : documents) {
            String status = document.getString("status");
            if ("pending".equalsIgnoreCase(status)) {
                pendingCount++;
            }
        }

        tvRequestCount.setText(documents.size() + " total · "
                + pendingCount + " pending"
        );
        if (documents.isEmpty()) {
            showMessage("No mentorship requests have been submitted yet.");
            return;
        }

        for (DocumentSnapshot document : documents) {
            String studentName = safe(document.getString("student_name"));
            String studentEmail = safe(document.getString("student_email"));
            String sessionTitle = safe(document.getString("session_title"));
            String facultyName = safe(document.getString("faculty_name"));
            String status = safe(document.getString("status")).toLowerCase(Locale.ROOT);

            LinearLayout card = new LinearLayout(this);

            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_card);
            card.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            cardParams.bottomMargin = dpToPx(14);
            card.setLayoutParams(cardParams);
            TextView heading = createTextView(
                    sessionTitle, 18, "#163653"
            );

            heading.setTypeface(null, Typeface.BOLD);

            TextView student = createTextView("Student: " + studentName, 14, "#3B638D");

            student.setPadding(0, dpToPx(10), 0, dpToPx(4));

            TextView email = createTextView("Email: " + studentEmail, 13, "#68788A");

            TextView faculty = createTextView("Faculty: " + facultyName, 13, "#68788A");

            faculty.setPadding(0, dpToPx(6), 0, dpToPx(8));

            TextView statusView = createTextView("Status: " + status.toUpperCase(Locale.ROOT), 13,

                    "accepted".equals(status) ? "#238B73"
                            : "declined".equals(status) ? "#B42318"
                              : "#A56A15"

            );

            statusView.setTypeface(null, Typeface.BOLD);

            statusView.setPadding(0, dpToPx(4), 0, dpToPx(12));
            card.addView(heading);
            card.addView(student);
            card.addView(email);
            card.addView(faculty);
            card.addView(statusView);

            if ("pending".equals(status)) {

                LinearLayout actions = new LinearLayout(this);
                actions.setOrientation(LinearLayout.HORIZONTAL);
                Button acceptButton = new Button(this);
                acceptButton.setText("Accept");
                acceptButton.setAllCaps(false);
                acceptButton.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                Color.parseColor("#238B73")
                        )
                );

                acceptButton.setTextColor(Color.WHITE);
                Button declineButton = new Button(this);
                declineButton.setText("Decline");
                declineButton.setAllCaps(false);
                declineButton.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                Color.parseColor("#B42318")
                        )
                );

                declineButton.setTextColor(Color.WHITE);
                LinearLayout.LayoutParams buttonParams =
                        new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

                buttonParams.setMarginEnd(dpToPx(6));

                actions.addView(acceptButton, buttonParams);

                LinearLayout.LayoutParams declineParams =

                        new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

                declineParams.setMarginStart(dpToPx(6));

                actions.addView(declineButton, declineParams);

                acceptButton.setOnClickListener(v ->
                        updateRequestStatus(document, "accepted")
                );

                declineButton.setOnClickListener(v ->
                        updateRequestStatus(document, "declined")
                );
                card.addView(actions);
            }
            layoutRequests.addView(card);
        }
    }
    private void updateRequestStatus(
            DocumentSnapshot document,
            String newStatus

    ) {
        document.getReference()
                .update("status", newStatus, "updated_at", FieldValue.serverTimestamp())
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this, "Request marked as " + newStatus + ".", Toast.LENGTH_SHORT).show();
                    // The snapshot listener refreshes the screen.

                })

                .addOnFailureListener(error -> {
                    Log.e(TAG, "Failed to update request status", error);

                    Toast.makeText(this, "Could not update request. Check admin permissions.", Toast.LENGTH_LONG).show();
                });
    }

    private void showMessage(String message) {

        tvRequestsMessage.setText(message);
        tvRequestsMessage.setVisibility(View.VISIBLE);
        tvRequestsMessage.setGravity(Gravity.CENTER);
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

    private String safe(String value) {
        return value == null || value.trim().isEmpty()
                ? "Not specified"
                : value;
    }
    private int dpToPx(int dp) {
        return Math.round(
                dp * getResources().getDisplayMetrics().density
        );
    }

    @Override

    protected void onDestroy() {
        if (requestsListener != null) {
            requestsListener.remove();
        }
        super.onDestroy();
    }
}
