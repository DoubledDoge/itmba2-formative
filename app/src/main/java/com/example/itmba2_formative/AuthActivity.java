package com.example.itmba2_formative;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.itmba2_formative.objects.User;

public class AuthActivity extends BaseActivity {

    private boolean isLoginMode = true;

    // UI Components
    private TextView tvWelcomeBack, tvBackToWelcome;
    private EditText etEmail, etPassword, etConfirmPassword, etFullName;
    private Button btnPrimaryAction, btnSecondaryAction;

    // Database and Session
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.auth_main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeDependencies();

        if (checkExistingSession()) {
            return;
        }

        initViews();

        isLoginMode = true;
        switchToLoginMode();

        setupClickListeners();
    }

    private void initializeDependencies() {
        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
    }

    private boolean checkExistingSession() {
        if (sessionManager.isLoggedIn()) {
            navigateToHome();
            return true;
        }
        return false;
    }

    private void initViews() {
        tvWelcomeBack = findViewById(R.id.tv_welcome_back);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etFullName = findViewById(R.id.et_full_name);

        btnPrimaryAction = findViewById(R.id.btn_primary_action);
        btnSecondaryAction = findViewById(R.id.btn_secondary_action);
        tvBackToWelcome = findViewById(R.id.tv_back_to_welcome);
    }

    private void setupClickListeners() {
        btnSecondaryAction.setOnClickListener(v -> toggleAuthMode());

        tvBackToWelcome.setOnClickListener(v -> {
            Intent intent = new Intent(AuthActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnPrimaryAction.setOnClickListener(v -> {
            if (isLoginMode) {
                performLogin();
            } else {
                performRegistration();
            }
        });
    }

    private void toggleAuthMode() {
        isLoginMode = !isLoginMode;

        // Clear errors
        etEmail.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);
        etFullName.setError(null);

        if (isLoginMode) {
            switchToLoginMode();
        } else {
            switchToRegistrationMode();
        }
    }

    private void switchToLoginMode() {
        tvWelcomeBack.setText(getString(R.string.welcome_back));
        findViewById(R.id.ll_confirm_password).setVisibility(View.GONE);
        findViewById(R.id.ll_full_name).setVisibility(View.GONE);
        btnPrimaryAction.setText(getString(R.string.auth_action_login));
        btnSecondaryAction.setText(getString(R.string.auth_action_register));
    }

    private void switchToRegistrationMode() {
        tvWelcomeBack.setText(getString(R.string.join_message));
        findViewById(R.id.ll_confirm_password).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_full_name).setVisibility(View.VISIBLE);
        btnPrimaryAction.setText(getString(R.string.auth_action_register));
        btnSecondaryAction.setText(getString(R.string.auth_switch_to_login));
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Clear previous errors
        etEmail.setError(null);
        etPassword.setError(null);

        // Validate email
        if (HelperMethods.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (HelperMethods.isInvalidEmail(email)) {
            etEmail.setError("Please enter a valid email address");
            return;
        }

        // Validate password
        if (HelperMethods.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        setLoadingState(true, getString(R.string.loading_login));

        new Thread(() -> authenticateUser(email, password)).start();
    }

    private void authenticateUser(String email, String password) {
        User user = dbHelper.authenticateUser(email, password);

        runOnUiThread(() -> {
            setLoadingState(false, getString(R.string.auth_action_login));

            if (user != null) {
                handleSuccessfulLogin(user);
            } else {
                handleFailedLogin();
            }
        });
    }

    private void handleSuccessfulLogin(User user) {
        sessionManager.createLoginSession(user);
        HelperMethods.showToast(this,
                getString(R.string.login_success) + " " + user.getFullName() + "!");
        navigateToHome();
    }

    private void handleFailedLogin() {
        HelperMethods.showToast(this, "Invalid email or password");
        etPassword.setError("Please check your credentials");
    }

    private void performRegistration() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();

        // Clear previous errors
        etEmail.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);
        etFullName.setError(null);

        // Validate email
        if (HelperMethods.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (HelperMethods.isInvalidEmail(email)) {
            etEmail.setError("Please enter a valid email address");
            return;
        }

        // Validate password
        if (HelperMethods.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }
        if (!HelperMethods.isValidPassword(password)) {
            etPassword.setError("Password must be at least " + AppConstants.Validation.MIN_PASSWORD_LENGTH + " characters and at most " + AppConstants.Validation.MAX_PASSWORD_LENGTH + " characters");
            return;
        }

        // Validate confirm password
        if (HelperMethods.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            return;
        }
        if (!HelperMethods.passwordsMatch(password, confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Validate full name
        if (HelperMethods.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            return;
        }
        if (!HelperMethods.isValidFullName(fullName)) {
            etFullName.setError("Please enter your full name");
            return;
        }

        setLoadingState(true, getString(R.string.loading_register));

        new Thread(() -> registerUser(email, password, fullName)).start();
    }

    private void registerUser(String email, String password, String fullName) {
        boolean emailExists = dbHelper.isEmailExists(email);

        if (emailExists) {
            runOnUiThread(() -> {
                setLoadingState(false, getString(R.string.auth_action_register));
                etEmail.setError(getString(R.string.email_exists));
            });
            return;
        }

        // Create new user
        long userId = dbHelper.createUser(email, password, HelperMethods.capitalizeWords(fullName));

        runOnUiThread(() -> {
            setLoadingState(false, getString(R.string.auth_action_register));

            if (userId > 0) {
                handleSuccessfulRegistration(userId, email, fullName);
            } else {
                HelperMethods.showToast(this, "Failed to create account. Please try again.");
            }
        });
    }

    private void handleSuccessfulRegistration(long userId, String email, String fullName) {
        User newUser = new User((int) userId, email, HelperMethods.capitalizeWords(fullName));
        sessionManager.createLoginSession(newUser);

        HelperMethods.showToast(this,
                getString(R.string.account_created) + " Welcome, " + newUser.getFullName() + "!");
        navigateToHome();
    }

    private void setLoadingState(boolean isLoading, String buttonText) {
        btnPrimaryAction.setEnabled(!isLoading);
        btnPrimaryAction.setText(buttonText);
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}