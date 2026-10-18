package com.example.internhub;

import android.content.Intent;

import android.os.Bundle;

import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;

public class AdminDashboardActivity extends AppCompatActivity {
    private static final String ADMIN_UID = "N27yzmpye7ehzJpkIwUOvLUzJHy1";

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Require an authenticated user.

        FirebaseUser user =

                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {

            openLoginScreen();

            return;

        }

        // Only the authorized admin can access this dashboard
        if (!ADMIN_UID.equals(user.getUid())) {

            startActivity(new Intent(
                    AdminDashboardActivity.this,
                    MainActivity.class
            ));

            finish();
            return;
        }

        setContentView(R.layout.activity_admin_dashboard);

        // Display the admin's registered name.

        TextView tvAdminWelcome =

                findViewById(R.id.tvAdminWelcome);

        String displayName = user.getDisplayName();

        if (displayName == null || displayName.trim().isEmpty()) {

            if (user.getEmail() != null
                    && user.getEmail().contains("@")) {

                displayName =

                        user.getEmail().substring(

                                0,

                                user.getEmail().indexOf("@")

                        );

            } else {

                displayName = "Admin";

            }

        }

        tvAdminWelcome.setText(

                "Welcome back, " + displayName + " 👋"

        );

        // Add Internship

        findViewById(R.id.btnAdminDashboardAddInternship)

                .setOnClickListener(v -> {

                    Intent intent = new Intent(

                            AdminDashboardActivity.this,

                            AdminAddInternshipActivity.class

                    );

                    startActivity(intent);

                });

        // View Published Internships

        findViewById(R.id.btnAdminDashboardViewInternships)

                .setOnClickListener(v -> {

                    Intent intent = new Intent(

                            AdminDashboardActivity.this,

                            InternshipActivity.class

                    );

                    startActivity(intent);

                });

        // Manage Faculty Mentorship and student requests

        findViewById(R.id.btnAdminDashboardMentorship)

                .setOnClickListener(v -> {

                    Intent intent = new Intent(

                            AdminDashboardActivity.this,

                            AdminMentorshipActivity.class

                    );

                    startActivity(intent);

                });

        // Industry Insights

        findViewById(R.id.btnAdminDashboardInsights)

                .setOnClickListener(v -> {

                    Intent intent = new Intent(

                            AdminDashboardActivity.this,

                            IndustryInsightsActivity.class

                    );

                    startActivity(intent);

                });

        // Log Out

        findViewById(R.id.btnAdminDashboardLogout)

                .setOnClickListener(v ->

                        new AlertDialog.Builder(

                                AdminDashboardActivity.this

                        )

                                .setTitle("Log Out")

                                .setMessage(

                                        "Are you sure you want to log out?"

                                )

                                .setNegativeButton("Cancel", null)

                                .setPositiveButton(

                                        "Log Out",

                                        (dialog, which) -> {

                                            FirebaseAuth.getInstance()

                                                    .signOut();

                                            openLoginScreen();

                                        }

                                )

                                .show()

                );

    }

    private void openLoginScreen() {

        Intent intent = new Intent(

                AdminDashboardActivity.this,

                AuthActivity.class

        );

        intent.addFlags(

                Intent.FLAG_ACTIVITY_NEW_TASK

                        | Intent.FLAG_ACTIVITY_CLEAR_TASK

        );

        startActivity(intent);

        finish();

    }

}
