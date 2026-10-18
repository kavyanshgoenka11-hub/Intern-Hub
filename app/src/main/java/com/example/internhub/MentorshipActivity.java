package com.example.internhub;

import android.graphics.Color;

import android.graphics.Typeface;

import android.os.Bundle;

import android.util.Log;

import android.view.Gravity;

import android.widget.Button;

import android.widget.LinearLayout;

import android.widget.TextView;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.FieldValue;

import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Locale;

import java.util.Map;

public class MentorshipActivity extends AppCompatActivity {

    private static final String TAG = "InternHubMentorship";

    private LinearLayout layoutSessions;

    private FirebaseFirestore firestore;

    private ListenerRegistration sessionsListener;

    private ListenerRegistration requestsListener;

    private final List<MentorshipSession> sessions =

            new ArrayList<>();

    // Maps each session ID to this student's request status.

    private final Map<String, String> requestStatuses =

            new HashMap<>();

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mentorship);

        layoutSessions = findViewById(

                R.id.layoutMentorshipSessions

        );

        firestore = FirebaseFirestore.getInstance();

        findViewById(R.id.btnMentorshipBack)

                .setOnClickListener(v -> finish());

        FirebaseUser user =

                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {

            Toast.makeText(

                    this,

                    "Please sign in to view mentorship sessions.",

                    Toast.LENGTH_LONG

            ).show();

            finish();

            return;

        }

        loadMyRequests(user);

        loadPublishedSessions();

    }

    private void loadPublishedSessions() {

        sessionsListener = firestore

                .collection("facultySessions")

                .whereEqualTo("status", "published")

                .addSnapshotListener((snapshots, error) -> {

                    if (error != null) {

                        Log.e(

                                TAG,

                                "Failed to load faculty sessions",

                                error

                        );

                        showMessage(

                                "Unable to load sessions. Please try again."

                        );

                        return;

                    }

                    if (snapshots == null) {

                        return;

                    }

                    sessions.clear();

                    for (DocumentSnapshot document

                            : snapshots.getDocuments()) {

                        sessions.add(new MentorshipSession(

                                document.getId(),

                                safe(document.getString("title")),

                                safe(document.getString("faculty_name")),

                                safe(document.getString("topic")),

                                safe(document.getString("date")),

                                safe(document.getString("time")),

                                safe(document.getString("mode")),

                                safe(document.getString("details"))

                        ));

                    }

                    displaySessions();

                });

    }

    private void loadMyRequests(FirebaseUser user) {

        requestsListener = firestore

                .collection("mentorshipRequests")

                .whereEqualTo("student_uid", user.getUid())

                .addSnapshotListener((snapshots, error) -> {

                    if (error != null) {

                        Log.e(

                                TAG,

                                "Failed to load student requests",

                                error

                        );

                        Toast.makeText(

                                MentorshipActivity.this,

                                "Unable to refresh request statuses.",

                                Toast.LENGTH_SHORT

                        ).show();

                        return;

                    }

                    if (snapshots == null) {

                        return;

                    }

                    requestStatuses.clear();

                    for (DocumentSnapshot document

                            : snapshots.getDocuments()) {

                        String sessionId =

                                document.getString("session_id");

                        String status =

                                document.getString("status");

                        if (sessionId != null && status != null) {

                            requestStatuses.put(sessionId, status);

                        }

                    }

                    displaySessions();

                });

    }

    private void displaySessions() {

        layoutSessions.removeAllViews();

        if (sessions.isEmpty()) {

            showMessage(

                    "No published faculty sessions are available yet."

            );

            return;

        }

        for (int i = 0; i < sessions.size(); i++) {

            MentorshipSession session = sessions.get(i);

            String requestStatus =

                    requestStatuses.get(session.id);

            boolean requested = requestStatus != null;

            LinearLayout card = new LinearLayout(this);

            card.setOrientation(LinearLayout.VERTICAL);

            card.setBackgroundResource(R.drawable.bg_card);

            card.setPadding(

                    dpToPx(16),

                    dpToPx(16),

                    dpToPx(16),

                    dpToPx(16)

            );

            LinearLayout.LayoutParams cardParams =

                    new LinearLayout.LayoutParams(

                            LinearLayout.LayoutParams.MATCH_PARENT,

                            LinearLayout.LayoutParams.WRAP_CONTENT

                    );

            cardParams.bottomMargin = dpToPx(14);

            card.setLayoutParams(cardParams);

            TextView label = createTextView(

                    String.format(

                            Locale.US,

                            "SESSION %02d",

                            i + 1

                    ),

                    11,

                    "#238B73"

            );

            label.setTypeface(null, Typeface.BOLD);

            TextView title = createTextView(

                    session.title,

                    18,

                    "#163653"

            );

            title.setTypeface(null, Typeface.BOLD);

            title.setPadding(

                    0,

                    dpToPx(8),

                    0,

                    dpToPx(8)

            );

            TextView faculty = createTextView(

                    "Faculty: " + session.faculty,

                    13,

                    "#3B638D"

            );

            TextView topic = createTextView(

                    "Topic: " + session.topic,

                    13,

                    "#68788A"

            );

            topic.setPadding(0, dpToPx(6), 0, 0);

            TextView schedule = createTextView(

                    "Date: " + session.date

                            + "\nTime: " + session.time

                            + "\nMode / Location: " + session.mode,

                    13,

                    "#68788A"

            );

            schedule.setPadding(

                    0,

                    dpToPx(8),

                    0,

                    dpToPx(10)

            );

            TextView description = createTextView(

                    session.details,

                    14,

                    "#68788A"

            );

            description.setLineSpacing(dpToPx(2), 1.0f);

            String displayedStatus = requested

                    ? "Request status: " + requestStatus.toUpperCase(Locale.US)

                    : "You have not requested this session";

            TextView status = createTextView(

                    displayedStatus,

                    13,

                    requested ? "#238B73" : "#68788A"

            );

            status.setTypeface(null, Typeface.BOLD);

            status.setPadding(

                    0,

                    dpToPx(14),

                    0,

                    dpToPx(10)

            );

            Button requestButton = new Button(this);

            requestButton.setAllCaps(false);

            requestButton.setTextSize(14);

            if (requested) {

                requestButton.setText("Request Submitted");

                requestButton.setEnabled(false);

                requestButton.setAlpha(0.65f);

            } else {

                requestButton.setText("Request Mentorship");

                requestButton.setOnClickListener(

                        v -> confirmRequest(session)

                );

            }

            card.addView(label);

            card.addView(title);

            card.addView(faculty);

            card.addView(topic);

            card.addView(schedule);

            card.addView(description);

            card.addView(status);

            card.addView(requestButton);

            layoutSessions.addView(card);

        }

    }

    private void confirmRequest(MentorshipSession session) {

        new AlertDialog.Builder(this)

                .setTitle("Request Mentorship")

                .setMessage(

                        "Submit a request for:\n\n"

                                + session.title

                                + "\n\nYour request will be saved "

                                + "to Cloud Firestore."

                )

                .setNegativeButton("Cancel", null)

                .setPositiveButton(

                        "Submit Request",

                        (dialog, which) -> submitRequest(session)

                )

                .show();

    }

    private void submitRequest(MentorshipSession session) {

        FirebaseUser user =

                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {

            Toast.makeText(

                    this,

                    "Please sign in again.",

                    Toast.LENGTH_SHORT

            ).show();

            return;

        }

        // Keep one request per student per session.

        String requestDocumentId =

                user.getUid() + "_" + session.id;

        Map<String, Object> request = new HashMap<>();

        request.put("session_id", session.id);

        request.put("session_title", session.title);

        request.put("faculty_name", session.faculty);

        request.put("student_uid", user.getUid());

        String studentName = user.getDisplayName();

        if (studentName == null || studentName.trim().isEmpty()) {

            studentName = user.getEmail() == null

                    ? "Student"

                    : user.getEmail().split("@")[0];

        }

        request.put("student_name", studentName);

        request.put(

                "student_email",

                user.getEmail() == null ? "" : user.getEmail()

        );

        request.put("status", "pending");

        request.put("created_at", FieldValue.serverTimestamp());

        firestore.collection("mentorshipRequests")

                .document(requestDocumentId)

                .set(request)

                .addOnSuccessListener(unused -> {

                    // The Firestore listener will refresh the status.

                    Toast.makeText(

                            MentorshipActivity.this,

                            "Mentorship request submitted!",

                            Toast.LENGTH_SHORT

                    ).show();

                })

                .addOnFailureListener(error -> {

                    Log.e(

                            TAG,

                            "Failed to submit mentorship request",

                            error

                    );

                    Toast.makeText(

                            MentorshipActivity.this,

                            "Request failed. Check your connection and permissions.",

                            Toast.LENGTH_LONG

                    ).show();

                });

    }

    private void showMessage(String message) {

        layoutSessions.removeAllViews();

        TextView messageView = createTextView(

                message,

                14,

                "#68788A"

        );

        messageView.setGravity(Gravity.CENTER);

        messageView.setPadding(

                dpToPx(12),

                dpToPx(32),

                dpToPx(12),

                dpToPx(32)

        );

        layoutSessions.addView(messageView);

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

                dp * getResources()

                        .getDisplayMetrics()

                        .density

        );

    }

    @Override

    protected void onDestroy() {

        if (sessionsListener != null) {

            sessionsListener.remove();

        }

        if (requestsListener != null) {

            requestsListener.remove();

        }

        super.onDestroy();

    }

    private static class MentorshipSession {

        String id;

        String title;

        String faculty;

        String topic;

        String date;

        String time;

        String mode;

        String details;

        MentorshipSession(
                String id,
                String title,
                String faculty,
                String topic,
                String date,
                String time,
                String mode,
                String details
        ) {

            this.id = id;
            this.title = title;
            this.faculty = faculty;
            this.topic = topic;
            this.date = date;
            this.time = time;
            this.mode = mode;
            this.details = details;
        }
    }
}
