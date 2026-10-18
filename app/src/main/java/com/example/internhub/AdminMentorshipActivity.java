package com.example.internhub;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;

import android.view.View;

import android.widget.Button;

import android.widget.EditText;

import android.widget.TextView;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FieldValue;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import java.util.Map;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AdminMentorshipActivity extends AppCompatActivity {

    private EditText etSessionTitle;
    private EditText etFacultyName;
    private EditText etSessionTopic;
    private EditText etSessionDate;
    private EditText etSessionTime;
    private EditText etSessionMode;
    private EditText etSessionDetails;
    private Button btnPublishSession;
    private TextView tvMessage;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_admin_mentorship);

        firestore = FirebaseFirestore.getInstance();
        etSessionTitle = findViewById(R.id.etSessionTitle);
        etFacultyName = findViewById(R.id.etFacultyName);
        etSessionTopic = findViewById(R.id.etSessionTopic);
        etSessionDate = findViewById(R.id.etSessionDate);
        etSessionDate.setOnClickListener(v -> showDatePicker());
        etSessionTime = findViewById(R.id.etSessionTime);
        etSessionTime.setOnClickListener(v -> showTimePicker());
        etSessionMode = findViewById(R.id.etSessionMode);
        etSessionDetails = findViewById(R.id.etSessionDetails);
        btnPublishSession = findViewById(R.id.btnPublishSession);
        tvMessage = findViewById(R.id.tvAdminMentorshipMessage);

        findViewById(R.id.btnAdminMentorshipBack).setOnClickListener(v -> finish());

        btnPublishSession.setOnClickListener(v -> publishSession());

        Button btnViewRequests = findViewById(R.id.btnViewMentorshipRequests);

        btnViewRequests.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMentorshipActivity.this, AdminMentorshipRequestsActivity.class);
            startActivity(intent);
        });
    }

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, (view, year, month, dayOfMonth) -> {

                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                    etSessionDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(
                System.currentTimeMillis() - 1000
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {

        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this, (view, hourOfDay, minute) -> {

                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

                    etSessionTime.setText(timeFormat.format(selectedTime.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        timePickerDialog.show();
    }

    private void publishSession() {

        String title = etSessionTitle.getText().toString().trim();
        String faculty = etFacultyName.getText().toString().trim();
        String topic = etSessionTopic.getText().toString().trim();
        String date = etSessionDate.getText().toString().trim();
        String time = etSessionTime.getText().toString().trim();
        String mode = etSessionMode.getText().toString().trim();
        String details = etSessionDetails.getText().toString().trim();

        if (title.isEmpty()) {
            etSessionTitle.setError("Enter the session title");
            etSessionTitle.requestFocus();
            return;

        }

        if (faculty.isEmpty()) {
            etFacultyName.setError("Enter the faculty name");
            etFacultyName.requestFocus();
            return;
        }

        if (topic.isEmpty()) {
            etSessionTopic.setError("Enter the session topic");
            etSessionTopic.requestFocus();
            return;
        }

        if (date.isEmpty()) {
            etSessionDate.setError("Enter the session date");
            etSessionDate.requestFocus();
            return;
        }

        if (time.isEmpty()) {
            etSessionTime.setError("Enter the session time");
            etSessionTime.requestFocus();
            return;
        }

        if (details.isEmpty()) {
            etSessionDetails.setError("Enter the session details");
            etSessionDetails.requestFocus();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            showMessage("Please sign in before publishing a session.");
            return;
        }

        Map<String, Object> session = new HashMap<>();

        session.put("title", title);
        session.put("faculty_name", faculty);
        session.put("topic", topic);
        session.put("date", date);
        session.put("time", time);
        session.put("mode", mode);
        session.put("details", details);
        session.put("status", "published");
        session.put("created_by", user.getUid());
        session.put("created_at", FieldValue.serverTimestamp());

        btnPublishSession.setEnabled(false);
        btnPublishSession.setText("Publishing...");

        tvMessage.setVisibility(View.GONE);

        firestore.collection("facultySessions")
                .add(session)
                .addOnSuccessListener(documentReference -> {
                    btnPublishSession.setEnabled(true);
                    btnPublishSession.setText("Publish Faculty Session");
                    Toast.makeText(AdminMentorshipActivity.this, "Faculty session published successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })

                .addOnFailureListener(error -> {
                    Log.e("InternHubMentorship", "Failed to publish faculty session", error);

                    btnPublishSession.setEnabled(true);

                    btnPublishSession.setText("Publish Faculty Session");

                    showMessage("Could not publish the session. Check access and connection.");
                });
    }

    private void showMessage(String message) {
        tvMessage.setText(message);
        tvMessage.setVisibility(View.VISIBLE);
    }
}
