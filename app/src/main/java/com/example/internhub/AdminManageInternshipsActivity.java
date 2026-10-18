package com.example.internhub;

import android.content.Intent;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.Locale;

public class AdminManageInternshipsActivity extends AppCompatActivity {

    private static final String TAG = "AdminManageInternships";
    private static final String ADMIN_UID = "N27yzmpye7ehzJpkIwUOvLUzJHy1";

    private LinearLayout layoutAdminInternships;
    private TextView tvManageInternshipsMessage;

    private FirebaseFirestore firestore;
    private ListenerRegistration internshipsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            openLoginScreen();
            return;
        }

        if (!ADMIN_UID.equals(user.getUid())) {
            Toast.makeText(this, "You are not authorized to manage internships.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_manage_internships);

        layoutAdminInternships = findViewById(R.id.layoutAdminInternships);
        tvManageInternshipsMessage = findViewById(R.id.tvManageInternshipsMessage);
        firestore = FirebaseFirestore.getInstance();

        findViewById(R.id.btnManageInternshipsBack).setOnClickListener(v -> finish());
        loadInternships();
    }

    private void loadInternships() {

        tvManageInternshipsMessage.setText("Loading internships...");
        tvManageInternshipsMessage.setVisibility(TextView.VISIBLE);

        internshipsListener = firestore
                .collection("internships")
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null) {
                        Log.e(TAG, "Could not load internships", error);

                        showMessage("Unable to load internships. "
                                        + "Check your permissions and connection."
                        );
                        return;
                    }

                    if (snapshots == null) {
                        return;
                    }
                    displayInternships(snapshots.getDocuments());
                });
    }

    private void displayInternships(
            List<DocumentSnapshot> documents
    ) {

        layoutAdminInternships.removeAllViews();

        if (documents.isEmpty()) {
            showMessage("No internship listings are available.");
            return;
        }

        tvManageInternshipsMessage.setVisibility(TextView.GONE);

        for (DocumentSnapshot document : documents) {

            String title = safe(document.getString("title"));
            String company = safe(document.getString("company"));
            String domain = safe(document.getString("domain"));
            String skills = safe(document.getString("required_skills"));
            String status = safe(document.getString("status"));

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_card);
            card.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            cardParams.bottomMargin = dpToPx(14);
            card.setLayoutParams(cardParams);

            TextView titleView = createTextView(
                    title,
                    18,
                    "#163653"
            );
            titleView.setTypeface(null, Typeface.BOLD);

            TextView companyView = createTextView(
                    "Company: " + company,
                    14,
                    "#3B638D"
            );

            TextView detailsView = createTextView(
                    "Domain: " + domain
                            + "\nRequired skills: " + skills
                            + "\nStatus: "
                            + status.toUpperCase(Locale.ROOT),
                    13,
                    "#68788A"
            );

            companyView.setPadding(0, dpToPx(8), 0, dpToPx(6));
            detailsView.setLineSpacing(dpToPx(2), 1.0f);

            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setPadding(0, dpToPx(12), 0, 0);

            Button editButton = new Button(this);
            editButton.setText("Edit");
            editButton.setAllCaps(false);
            editButton.setTextColor(Color.WHITE);
            editButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            Color.parseColor("#163653")
                    )
            );

            Button deleteButton = new Button(this);
            deleteButton.setText("Delete");
            deleteButton.setAllCaps(false);
            deleteButton.setTextColor(Color.WHITE);
            deleteButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            Color.parseColor("#B42318")
                    )
            );

            LinearLayout.LayoutParams actionParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

            actionParams.setMarginEnd(dpToPx(6));
            actions.addView(editButton, actionParams);

            LinearLayout.LayoutParams deleteParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

            deleteParams.setMarginStart(dpToPx(6));
            actions.addView(deleteButton, deleteParams);

            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManageInternshipsActivity.this, AdminAddInternshipActivity.class);

                intent.putExtra("edit_document_id", document.getId());
                startActivity(intent);
            });

            deleteButton.setOnClickListener(v ->
                    confirmDelete(document, title, company)
            );

            card.addView(titleView);
            card.addView(companyView);
            card.addView(detailsView);
            card.addView(actions);

            layoutAdminInternships.addView(card);
        }
    }

    private void confirmDelete(
            DocumentSnapshot document,
            String title,
            String company
    ) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Internship")
                .setMessage(
                        "Delete \"" + title + "\" from "
                                + company + "?\n\n"
                                + "This removes the cloud listing "
                                + "and cannot be undone from this screen."
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {

                    firestore.collection("internships")
                            .document(document.getId())
                            .delete()
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "Internship deleted.", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(error -> {
                                Log.e(TAG, "Failed to delete internship", error);

                                Toast.makeText(this, "Delete failed. Check admin permissions.", Toast.LENGTH_LONG).show();
                            });
                }).show();
    }

    private void showMessage(String message) {

        layoutAdminInternships.removeAllViews();
        tvManageInternshipsMessage.setText(message);
        tvManageInternshipsMessage.setVisibility(TextView.VISIBLE);
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

        return value == null || value.trim().isEmpty() ? "Not specified" : value;
    }

    private int dpToPx(int dp) {

        return Math.round(
                dp * getResources().getDisplayMetrics().density
        );
    }

    private void openLoginScreen() {

        Intent intent = new Intent(this, AuthActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {

        if (internshipsListener != null) {
            internshipsListener.remove();
        }

        super.onDestroy();
    }
}