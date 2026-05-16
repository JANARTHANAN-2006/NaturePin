package com.example.naturepin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import io.github.jan.supabase.auth.user.UserInfo;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etUsername, etBio;
    private MaterialButton btnAuthAction, btnShowLogin, btnShowSignup;
    private TextView btnBack;
    private LinearLayout layoutWelcome, layoutAuthForm;
    private TextInputLayout tilUsername, tilBio;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        
        // Check session
        UserInfo sessionUser = SupabaseHelper.INSTANCE.getCurrentUser();
        if (sessionUser != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setTheme(R.style.Theme_NaturePin);
        setContentView(R.layout.activity_login);
        
        etUsername = findViewById(R.id.et_login_username);
        etBio = findViewById(R.id.et_login_bio);
        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnAuthAction = findViewById(R.id.btn_auth_action);
        btnShowLogin = findViewById(R.id.btn_show_login);
        btnShowSignup = findViewById(R.id.btn_show_signup);
        btnBack = findViewById(R.id.btn_back_to_welcome);
        layoutWelcome = findViewById(R.id.layout_welcome_options);
        layoutAuthForm = findViewById(R.id.layout_auth_form);
        tilUsername = findViewById(R.id.til_username);
        tilBio = findViewById(R.id.til_bio);

        btnShowLogin.setOnClickListener(v -> showAuthForm(true));
        btnShowSignup.setOnClickListener(v -> showAuthForm(false));
        btnBack.setOnClickListener(v -> showWelcomeOptions());

        btnAuthAction.setOnClickListener(v -> {
            if (isLoginMode) {
                handleLogin();
            } else {
                handleSignup();
            }
        });
    }

    private void showAuthForm(boolean login) {
        isLoginMode = login;
        layoutWelcome.setVisibility(View.GONE);
        layoutAuthForm.setVisibility(View.VISIBLE);
        
        if (login) {
            tilUsername.setVisibility(View.GONE);
            tilBio.setVisibility(View.GONE);
            btnAuthAction.setText("Login");
        } else {
            tilUsername.setVisibility(View.VISIBLE);
            tilBio.setVisibility(View.VISIBLE);
            btnAuthAction.setText("Create Account");
        }
    }

    private void showWelcomeOptions() {
        layoutWelcome.setVisibility(View.VISIBLE);
        layoutAuthForm.setVisibility(View.GONE);
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        SupabaseHelper.INSTANCE.signInWithEmail(email, password, (success, error) -> {
            if (success) {
                UserInfo user = SupabaseHelper.INSTANCE.getCurrentUser();
                if (user != null) {
                    fetchUserDetailsAndMove(user.getId());
                }
            } else {
                Toast.makeText(this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
            }
            return null;
        });
    }

    private void handleSignup() {
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || bio.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        SupabaseHelper.INSTANCE.signUpWithEmail(email, password, (success, error) -> {
            if (success) {
                UserInfo user = SupabaseHelper.INSTANCE.getCurrentUser();
                if (user != null) {
                    SupabaseHelper.INSTANCE.saveUser(user.getId(), username, email, bio, saveSuccess -> {
                        Toast.makeText(this, "Account created! Please verify your email.", Toast.LENGTH_LONG).show();
                        showAuthForm(true);
                        return null;
                    });
                }
            } else {
                Toast.makeText(this, "Signup failed: " + error, Toast.LENGTH_SHORT).show();
            }
            return null;
        });
    }

    private void fetchUserDetailsAndMove(String uid) {
        SupabaseHelper.INSTANCE.getUserDetails(uid, (username) -> {
            saveUserSession(uid, username);
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return null;
        });
    }

    private void saveUserSession(String uid, String username) {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        pref.edit()
            .putString("userUid", uid)
            .putString("username", username)
            .apply();
    }
}
