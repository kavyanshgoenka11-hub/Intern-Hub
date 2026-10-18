package com.example.internhub;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

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
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "InternHubDB";

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check whether the user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {

            Intent authIntent = new Intent(MainActivity.this, AuthActivity.class);

            startActivity(authIntent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Profile dropdown menu
        TextView btnProfileMenu = findViewById(R.id.btnProfileMenu);

        btnProfileMenu.setOnClickListener(view -> {

            PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);

            popupMenu.getMenu().add(Menu.NONE, 1, 1, "My Account");
            popupMenu.getMenu().add(Menu.NONE, 2, 2, "Help & Support");
            popupMenu.getMenu().add(Menu.NONE, 3, 3, "Log Out");

            popupMenu.setOnMenuItemClickListener(item -> {

                if (item.getItemId() == 1) {

                    FirebaseUser user =
                            FirebaseAuth.getInstance().getCurrentUser();

                    String name = "Student";
                    String email = "Not available";

                    if (user != null) {
                        if (user.getDisplayName() != null
                                && !user.getDisplayName().trim().isEmpty()) {
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
                            .setMessage(
                                    "Welcome to Intern Hub.\n\n"
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

                                Intent intent = new Intent(
                                        MainActivity.this,
                                        AuthActivity.class
                                );

                                intent.addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                );

                                startActivity(intent);
                                finish();
                            })
                            .show();

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

        // Open Internship Explorer when the card is clicked
        findViewById(R.id.cardInternships).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    InternshipActivity.class
            );

            startActivity(intent);
        });

        // Open the Admin Add Internship screen
        findViewById(R.id.btnOpenAdmin).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AdminAddInternshipActivity.class
            );

            startActivity(intent);
        });

        // Initialize the database helper
        databaseHelper = new DatabaseHelper(this);
        testDatabase();
    }

    private void testDatabase() {

        // Check whether an approved internship already exists
        Cursor existingCursor = databaseHelper.getApprovedInternships();

        boolean hasInternship = existingCursor.moveToFirst();
        existingCursor.close();

        // Insert sample data only if the table has no approved internship
        if (!hasInternship) {
            long insertedId = databaseHelper.insertInternship(
                    "Software Development Intern",
                    "Example Technologies",
                    "Software Development",
                    "Java, SQL, Git",
                    "Sample internship record for database testing.",
                    "Pune",
                    "Hybrid",
                    "15000 per month",
                    "2026-11-30",
                    "https://example.com/apply"
            );

            Log.d(TAG, "Inserted internship ID: " + insertedId);
        }

        // Retrieve internships from the database
        Cursor cursor = databaseHelper.getApprovedInternships();

        Log.d(TAG, "Approved internships count: " + cursor.getCount());

        while (cursor.moveToNext()) {

            String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TITLE));

            String company = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMPANY)
            );

            String skills = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SKILLS)
            );

            Log.d(TAG, "Title: " + title);
            Log.d(TAG, "Company: " + company);
            Log.d(TAG, "Skills: " + skills);
        }

        cursor.close();
    }

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}