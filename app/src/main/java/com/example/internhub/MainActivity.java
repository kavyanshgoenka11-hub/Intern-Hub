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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "InternHubDB";

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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