package com.example.internhub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminAddInternshipActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private EditText etTitle;
    private EditText etCompany;
    private EditText etDomain;
    private EditText etSkills;
    private EditText etDescription;
    private EditText etLocation;
    private EditText etWorkMode;
    private EditText etStipend;
    private EditText etDeadline;
    private EditText etApplicationUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_internship);

        databaseHelper = new DatabaseHelper(this);

        etTitle = findViewById(R.id.etTitle);
        etCompany = findViewById(R.id.etCompany);
        etDomain = findViewById(R.id.etDomain);
        etSkills = findViewById(R.id.etSkills);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        etWorkMode = findViewById(R.id.etWorkMode);
        etStipend = findViewById(R.id.etStipend);
        etDeadline = findViewById(R.id.etDeadline);
        etApplicationUrl = findViewById(R.id.etApplicationUrl);


        TextView btnBack = findViewById(R.id.btnAdminBack);
        btnBack.setOnClickListener(v -> finish());

        Button btnSave = findViewById(R.id.btnSaveInternship);
        btnSave.setOnClickListener(v -> saveInternship());
    }

    private void saveInternship() {

        String title = etTitle.getText().toString().trim();
        String company = etCompany.getText().toString().trim();
        String domain = etDomain.getText().toString().trim();
        String skills = etSkills.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Enter the internship title");
            etTitle.requestFocus();
            return;
        }

        if (company.isEmpty()) {
            etCompany.setError("Enter the company name");
            etCompany.requestFocus();
            return;
        }

        if (domain.isEmpty()) {
            etDomain.setError("Enter the industry domain");
            etDomain.requestFocus();
            return;
        }

        if (skills.isEmpty()) {
            etSkills.setError("Enter the required skills");
            etSkills.requestFocus();
            return;
        }


        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String workMode = etWorkMode.getText().toString().trim();
        String stipend = etStipend.getText().toString().trim();
        String deadline = etDeadline.getText().toString().trim();
        String applicationUrl = etApplicationUrl.getText().toString().trim();

        // Insert the internship into SQLite
        long insertedId = databaseHelper.insertInternship(
                title,
                company,
                domain,
                skills,
                description,
                location,
                workMode,
                stipend,
                deadline,
                applicationUrl
        );

        if (insertedId == -1) {
            Toast.makeText(this, "Could not save internship. Please try again.", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, "Internship saved successfully!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}