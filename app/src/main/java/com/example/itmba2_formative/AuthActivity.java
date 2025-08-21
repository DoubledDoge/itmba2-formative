package com.example.itmba2_formative;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class AuthActivity extends BaseActivity {

    private boolean isLoginMode = true;

    private TextView tvWelcomeBack;
    private TextInputLayout tilConfirmPassword, tilFullName;
    private MaterialButton btnPrimaryAction, btnSecondaryAction;
    private TextView tvBackToWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tvWelcomeBack = findViewById(R.id.tv_welcome_back);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        tilFullName = findViewById(R.id.til_full_name);
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
            // TODO: Implement authentication logic
            // For now, just navigate to home (placeholder)
            // navigateToHome();
        });

        // Add double-tap to toggle system UI for testing
        findViewById(R.id.iv_auth_logo).setOnClickListener(v -> {
            toggleSystemUI();
        });
    }

    private void toggleAuthMode() {
        isLoginMode = !isLoginMode;

        if (isLoginMode) {
            // Switch to Login Mode
            tvWelcomeBack.setText(getString(R.string.welcome_back));
            tilConfirmPassword.setVisibility(View.GONE);
            tilFullName.setVisibility(View.GONE);
            btnPrimaryAction.setText(getString(R.string.login_button));
            btnSecondaryAction.setText(getString(R.string.create_account_button));
        } else {
            // Switch to Registration Mode
            tvWelcomeBack.setText(R.string.join_message);
            tilConfirmPassword.setVisibility(View.VISIBLE);
            tilFullName.setVisibility(View.VISIBLE);
            btnPrimaryAction.setText(getString(R.string.register_button));
            btnSecondaryAction.setText(getString(R.string.switch_to_login));
        }
    }
}