package com.example.internhub;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;

import android.widget.TextView;
import com.google.firebase.auth.FirebaseUser;

import android.view.Menu;
import android.widget.PopupMenu;
import androidx.appcompat.app.AlertDialog;


public class MainActivity extends AppCompatActivity {

    private static final String ADMIN_UID = "N27yzmpye7ehzJpkIwUOvLUzJHy1";
    private TextView tvUpcomingMessage;
    private LinearLayout layoutUpcomingSessions;

    private ListenerRegistration upcomingSessionsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check the authenticated user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {

            Intent authIntent = new Intent(MainActivity.this, AuthActivity.class);
            startActivity(authIntent);
            finish();
            return;
        }

// Route the authorized admin to the Admin Dashboard
        if (ADMIN_UID.equals(currentUser.getUid())) {

            Intent adminIntent = new Intent(MainActivity.this, AdminDashboardActivity.class);
            startActivity(adminIntent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tvUpcomingMessage = findViewById(R.id.tvUpcomingMessage);
        layoutUpcomingSessions = findViewById(R.id.layoutUpcomingSessions);

        loadUpcomingMentorship();

        LinearLayout cardCareerRoadmap = findViewById(R.id.cardCareerRoadmap);

        cardCareerRoadmap.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    CareerRoadmapActivity.class
            );

            startActivity(intent);
        });

        findViewById(R.id.tvExploreRoadmap).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CareerRoadmapActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardIndustryInsights).setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, IndustryInsightsActivity.class);
            startActivity(intent);

        });

        findViewById(R.id.cardMentorship).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MentorshipActivity.class);
            startActivity(intent);
        });

        TextView btnProfileMenu = findViewById(R.id.btnProfileMenu);

        btnProfileMenu.setOnClickListener(view -> {

            PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);

            popupMenu.getMenu().add(Menu.NONE, 1, 1, "My Account");
            popupMenu.getMenu().add(Menu.NONE, 2, 2, "Help & Support");
            popupMenu.getMenu().add(Menu.NONE, 3, 3, "Log Out");

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    String name = "Student";
                    String email = "Not available";

                    if (user != null) {
                        if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
                            name = user.getDisplayName();
                        }

                        if (user.getEmail() != null) {
                            email = user.getEmail();
                        }
                    }

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("My Account")
                            .setMessage("Name: " + name + "\nEmail: " + email)
                            .setPositiveButton("Close", null)
                            .show();
                    return true;

                } else if (item.getItemId() == 2) {

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Help & Support")
                            .setMessage("Welcome to Intern Hub.\n\n"
                                            + "Use the dashboard to explore internships, "
                                            + "career preparation and mentorship.\n\n"
                                            + "For further assistance : \n\n Contact: "
                                            + "Email : placeecom@nmims.in \n"
                                            + "Phone: 1234-5678"
                            )
                            .setPositiveButton("Close", null)
                            .show();
                    return true;

                } else if (item.getItemId() == 3) {

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Log Out")
                            .setMessage("Are you sure you want to log out?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Log Out", (dialog, which) -> {

                                FirebaseAuth.getInstance().signOut();

                                Intent intent = new Intent(MainActivity.this, AuthActivity.class);

                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }).show();

                    return true;
                }

                return false;
            });

            popupMenu.show();
        });

        // Display the logged-in user's name
        TextView tvWelcome = findViewById(R.id.tvWelcome);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String displayName = null;

        if (user != null) {
            displayName = user.getDisplayName();
        }

// Fallback for accounts that do not have a saved display name
        if (displayName == null || displayName.trim().isEmpty()) {

            if (user != null && user.getEmail() != null) {
                String email = user.getEmail();

                if (email.contains("@")) {
                    displayName = email.substring(0, email.indexOf("@"));
                }
            }

            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = "Student";
            }
        }

        tvWelcome.setText("Welcome back, " + displayName + " 👋");

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {

                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                }
        );

        findViewById(R.id.cardInternships).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InternshipActivity.class);
            startActivity(intent);
        });

    }

    private void loadUpcomingMentorship() {

        upcomingSessionsListener = FirebaseFirestore.getInstance()
                .collection("facultySessions")
                .whereEqualTo("status", "published")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("InternHubUpcoming", "Failed to load upcoming mentorship", error);

                        showUpcomingMessage("Unable to load upcoming sessions.");
                        return;
                    }
                    if (snapshots == null) {
                        return;

                    }
                    List<UpcomingFacultySession> upcomingSessions = new ArrayList<>();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                    formatter.setLenient(false);

                    Date now = new Date();
                    for (DocumentSnapshot document :
                            snapshots.getDocuments()) {
                        String status = document.getString("status");

                        if (!"published".equals(status)) {
                            continue;
                        }
                        String title = document.getString("title");
                        String faculty = document.getString("faculty_name");
                        String date = document.getString("date");
                        String time = document.getString("time");
                        String mode = document.getString("mode");
                        if (title == null || date == null || time == null) {
                            continue;
                        }

                        Date scheduledAt;
                        try {
                            scheduledAt = formatter.parse(date + " " + time);
                        } catch (ParseException exception) {
                            Log.w("InternHubUpcoming", "Invalid session date/time: " + document.getId());
                            continue;
                        }
                        if (scheduledAt == null || !scheduledAt.after(now)) {
                            continue;
                        }

                        upcomingSessions.add(new UpcomingFacultySession(title, faculty == null ? "Faculty not specified" : faculty,
                                        date,time, mode == null || mode.trim().isEmpty() ? "Not specified" : mode,
                                        scheduledAt
                                )
                        );
                    }
                    Collections.sort(upcomingSessions, Comparator.comparing(
                            session -> session.scheduledAt
                            )
                    );
                    displayUpcomingMentorship(upcomingSessions);
                });
    }

    private void displayUpcomingMentorship(
            List<UpcomingFacultySession> sessions
    ) {

        layoutUpcomingSessions.removeAllViews();

        if (sessions.isEmpty()) {
            showUpcomingMessage("No upcoming published mentorship sessions at the moment.");
            return;
        }

        tvUpcomingMessage.setVisibility(View.GONE);

        int displayCount = Math.min(3, sessions.size());
        for (int i = 0; i < displayCount; i++) {
            UpcomingFacultySession session = sessions.get(i);
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            params.bottomMargin = dpToPx(14);
            item.setLayoutParams(params);
            TextView title = createUpcomingText(
                    session.title,
                    15,
                    "#163653",
                    true
            );
            TextView faculty = createUpcomingText(
                    "Faculty: " + session.faculty,
                    13,
                    "#3B638D",
                    false
            );

            faculty.setPadding(0, dpToPx(5), 0, 0);
            TextView schedule = createUpcomingText(
                    "Date: " + session.date
                            + "\nTime: " + session.time
                            + "\nMode: " + session.mode,
                    13,
                    "#68788A",
                    false
            );

            schedule.setPadding(0, dpToPx(5), 0, 0);
            item.addView(title);
            item.addView(faculty);
            item.addView(schedule);
            layoutUpcomingSessions.addView(item);
        }

    }

    private void showUpcomingMessage(String message) {
        layoutUpcomingSessions.removeAllViews();
        tvUpcomingMessage.setText(message);
        tvUpcomingMessage.setVisibility(View.VISIBLE);
    }

    private TextView createUpcomingText(
            String text,
            float size,
            String color,
            boolean bold
    ) {

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(size);
        textView.setTextColor(Color.parseColor(color));
        if (bold) {
            textView.setTypeface(null, Typeface.BOLD);
        }
        return textView;
    }

    private int dpToPx(int dp) {
        return Math.round(
                dp * getResources()
                        .getDisplayMetrics()
                        .density
        );

    }

    private static class UpcomingFacultySession {
        String title;
        String faculty;
        String date;
        String time;
        String mode;
        Date scheduledAt;

        UpcomingFacultySession(
                String title,
                String faculty,
                String date,
                String time,
                String mode,
                Date scheduledAt
        ) {

            this.title = title;
            this.faculty = faculty;
            this.date = date;
            this.time = time;
            this.mode = mode;
            this.scheduledAt = scheduledAt;
        }
    }


    @Override
    protected void onDestroy() {
        if (upcomingSessionsListener != null) {
            upcomingSessionsListener.remove();
        }
        super.onDestroy();
    }
}