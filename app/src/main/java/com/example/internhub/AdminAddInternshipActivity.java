package com.example.internhub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.util.Log;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class AdminAddInternshipActivity extends AppCompatActivity {
    private String editDocumentId;
    private Button btnSave;

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

        etTitle = findViewById(R.id.etTitle);
        etCompany = findViewById(R.id.etCompany);
        etDomain = findViewById(R.id.etDomain);
        etSkills = findViewById(R.id.etSkills);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        etWorkMode = findViewById(R.id.etWorkMode);
        etStipend = findViewById(R.id.etStipend);
        etDeadline = findViewById(R.id.etDeadline);
        etDeadline.setOnClickListener(v -> showDeadlinePicker());
        etApplicationUrl = findViewById(R.id.etApplicationUrl);


        TextView btnBack = findViewById(R.id.btnAdminBack);
        btnBack.setOnClickListener(v -> finish());

        btnSave = findViewById(R.id.btnSaveInternship);

        editDocumentId = getIntent().getStringExtra("edit_document_id");

        if (editDocumentId != null && editDocumentId.trim().isEmpty()) {
            editDocumentId = null;
        }

        btnSave.setOnClickListener(v -> saveInternship());

        if (editDocumentId != null) {
            btnSave.setText("Loading Internship...");
            btnSave.setEnabled(false);
            loadInternshipForEditing();
        }

        Button btnManageMentorship = findViewById(R.id.btnManageMentorship);

        btnManageMentorship.setOnClickListener(v -> {
            Intent intent = new Intent(AdminAddInternshipActivity.this, AdminMentorshipActivity.class);
            startActivity(intent);
        });
    }

    private void showDeadlinePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                    etDeadline.setText(dateFormat.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Prevent selection of past application deadlines
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void loadInternshipForEditing() {

        FirebaseFirestore.getInstance()
                .collection("internships")
                .document(editDocumentId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        Toast.makeText(this, "Internship not found.", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    etTitle.setText(document.getString("title"));
                    etCompany.setText(document.getString("company"));
                    etDomain.setText(document.getString("domain"));
                    etSkills.setText(document.getString("required_skills"));
                    etDescription.setText(document.getString("description"));
                    etLocation.setText(document.getString("location"));
                    etWorkMode.setText(document.getString("work_mode"));
                    etStipend.setText(document.getString("stipend"));
                    etDeadline.setText(document.getString("deadline"));
                    etApplicationUrl.setText(document.getString("application_url"));

                    btnSave.setText("Update Internship");
                    btnSave.setEnabled(true);
                })
                .addOnFailureListener(error -> {

                    Log.e("InternHubCloud", "Failed to load internship for editing", error);

                    Toast.makeText(this, "Could not load internship details.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void saveInternship() {

        String title = etTitle.getText().toString().trim();
        String company = etCompany.getText().toString().trim();
        String domain = etDomain.getText().toString().trim();
        String skills = etSkills.getText().toString().trim();

        // Validate required fields
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

        // Prepare the internship document
        Map<String, Object> internship = new HashMap<>();

        internship.put("title", title);
        internship.put("company", company);
        internship.put("domain", domain);
        internship.put("required_skills", skills);
        internship.put("description", description);
        internship.put("location", location);
        internship.put("work_mode", workMode);
        internship.put("stipend", stipend);
        internship.put("deadline", deadline);
        internship.put("application_url", applicationUrl);
        internship.put("status", "approved");
        if (editDocumentId == null) {
            internship.put("created_at", FieldValue.serverTimestamp());
        } else {
            internship.put("updated_at", FieldValue.serverTimestamp());
        }

        internship.put("status", "approved");

        // Save or update the internship in Cloud Firestore
        btnSave.setEnabled(false);
        btnSave.setText(editDocumentId == null ? "Saving..." : "Updating...");

        if (editDocumentId == null) {

            // Create a new internship
            FirebaseFirestore.getInstance()
                    .collection("internships")
                    .add(internship)
                    .addOnSuccessListener(documentReference ->
                            handleSaveSuccess(
                                    "Internship saved successfully!"
                            )
                    )
                    .addOnFailureListener(this::handleSaveFailure);

        } else {
            // Update the existing Firestore document
            FirebaseFirestore.getInstance()
                    .collection("internships")
                    .document(editDocumentId)
                    .update(internship)
                    .addOnSuccessListener(unused ->
                            handleSaveSuccess(
                                    "Internship updated successfully!"
                            )
                    )
                    .addOnFailureListener(this::handleSaveFailure);
        }
    }

    private void handleSaveSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void handleSaveFailure(Exception exception) {
        Log.e("InternHubCloud", "Failed to save internship", exception);

        btnSave.setEnabled(true);
        btnSave.setText(editDocumentId == null ? "Save Internship" : "Update Internship");

        Toast.makeText(this, "Save failed. Check your connection and Firestore permissions.", Toast.LENGTH_LONG).show();
    }
}