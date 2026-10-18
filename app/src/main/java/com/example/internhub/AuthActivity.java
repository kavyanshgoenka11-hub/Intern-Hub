package com.example.internhub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private LinearLayout layoutConfirmPassword;

    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvMessage;
    private TextView btnToggleMode;

    private Button btnAuthAction;
    private EditText etName;
    private LinearLayout layoutFullName;

    private boolean isRegisterMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        firebaseAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etAuthEmail);
        etPassword = findViewById(R.id.etAuthPassword);
        etConfirmPassword = findViewById(R.id.etAuthConfirmPassword);

        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword);

        tvTitle = findViewById(R.id.tvAuthTitle);
        tvSubtitle = findViewById(R.id.tvAuthSubtitle);
        tvMessage = findViewById(R.id.tvAuthMessage);

        btnAuthAction = findViewById(R.id.btnAuthAction);
        btnToggleMode = findViewById(R.id.btnToggleAuthMode);

        etName = findViewById(R.id.etAuthName);
        layoutFullName = findViewById(R.id.layoutFullName);

        updateScreen();

        btnToggleMode.setOnClickListener(v -> {
            isRegisterMode = !isRegisterMode;

            clearMessages();
            updateScreen();
        });

        btnAuthAction.setOnClickListener(v -> authenticateUser());
    }

    private void updateScreen() {

        if (isRegisterMode) {
            tvTitle.setText("Create your account");
            tvSubtitle.setText("Register to begin your career journey.");

            btnAuthAction.setText("Create Account");

            btnToggleMode.setText("Already registered? Sign in");

            layoutConfirmPassword.setVisibility(View.VISIBLE);
            layoutFullName.setVisibility(View.VISIBLE);

        } else {
            tvTitle.setText("Welcome back");
            tvSubtitle.setText("Sign in to continue to your account.");

            btnAuthAction.setText("Sign In");

            btnToggleMode.setText("New here? Create an account");

            layoutConfirmPassword.setVisibility(View.GONE);
            layoutFullName.setVisibility(View.GONE);
        }
    }

    private void authenticateUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String name = etName.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Enter your email address");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }

        if (isRegisterMode && password.length() < 6) {
            etPassword.setError("Password must contain at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (isRegisterMode) {

            String confirmPassword = etConfirmPassword.getText().toString();

            if (name.isEmpty()) {
                etName.setError("Enter your full name");
                etName.requestFocus();
                return;
            }

            if (confirmPassword.isEmpty()) {
                etConfirmPassword.setError("Confirm your password");
                etConfirmPassword.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }

            registerUser(name, email, password);

        } else {
            loginUser(email, password);
        }
    }

    private void registerUser(String name, String email, String password) {

        setLoading(true);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (!task.isSuccessful()) {
                        setLoading(false);

                        Exception exception = task.getException();
                        Log.e("InternHubAuth", "Registration failed", exception);

                        if (exception instanceof
                                FirebaseAuthUserCollisionException) {

                            showMessage("An account with this email already exists.");

                        } else if (exception instanceof
                                FirebaseAuthWeakPasswordException) {

                            showMessage("Choose a stronger password.");

                        } else if (exception instanceof
                                FirebaseAuthInvalidCredentialsException) {

                            showMessage("Please check your email address and password.");

                        } else {
                            showMessage("Registration failed. Check your connection and try again.");
                        }

                        return;
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if (user == null) {
                        setLoading(false);
                        firebaseAuth.signOut();

                        showMessage("Account created, but the user profile could not be loaded.");
                        return;
                    }

                    UserProfileChangeRequest profileUpdates =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(this, profileTask -> {

                                setLoading(false);

                                if (profileTask.isSuccessful()) {

                                    Toast.makeText(AuthActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                    openDashboard();

                                } else {

                                    Log.e("InternHubAuth", "Failed to save display name", profileTask.getException());

                                    firebaseAuth.signOut();
                                    isRegisterMode = false;
                                    updateScreen();
                                    showMessage("Account created, but saving your name failed. Please sign in.");
                                }
                            });
                });
    }

    private void loginUser(String email, String password) {

        setLoading(true);

        firebaseAuth.signInWithEmailAndPassword(
                email,
                password
        ).addOnCompleteListener(this, task -> {

            setLoading(false);

            if (task.isSuccessful()) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                openDashboard();
            } else {
                showMessage("Login failed. Check your email and password.");
            }
        });
    }

    private void openDashboard() {

        Intent intent = new Intent(AuthActivity.this, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {

        btnAuthAction.setEnabled(!loading);
        btnToggleMode.setEnabled(!loading);

        if (loading) {
            btnAuthAction.setText("Please wait...");
        } else {
            updateScreen();
        }
    }

    private void showMessage(String message) {

        tvMessage.setText(message);
        tvMessage.setTextColor(
                Color.parseColor("#B42318")
        );
        tvMessage.setVisibility(View.VISIBLE);
    }

    private void clearMessages() {

        tvMessage.setText("");
        tvMessage.setVisibility(View.GONE);

        etEmail.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);
    }
}