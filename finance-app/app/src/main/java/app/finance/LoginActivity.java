package app.finance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import app.finance.models.User;
import app.finance.viewmodel.FinanceViewModel;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FinanceViewModel viewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);

        // Professional Session Check: Redirect immediately if already authenticated
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard(sessionManager.getUserId(), null, sessionManager.getUserRole());
            return;
        }

        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
 
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both identity and password", Toast.LENGTH_SHORT).show();
            return;
        }
 
        btnLogin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
 
        // User constructor with password for login request
        User loginRequest = new User("", email, "", password);

        viewModel.login(loginRequest).observe(this, response -> {
            btnLogin.setEnabled(true);
            progressBar.setVisibility(View.GONE);

            if (response != null && response.isSuccess() && response.getData() != null) {
                User user = response.getData();
                
                // Initialize Persistent Session
                sessionManager.createLoginSession(String.valueOf(user.getId()), user.getRole());
                
                if ("true".equals(user.getMustChangePassword())) {
                    // Mandatory OTP Reset Flow
                    Intent intent = new Intent(LoginActivity.this, ChangePasswordActivity.class);
                    intent.putExtra("USER_ID", String.valueOf(user.getId()));
                    startActivity(intent);
                    finish();
                } else {
                    navigateToDashboard(String.valueOf(user.getId()), user.getName(), user.getRole());
                }
            } else {
                String error = (response != null) ? response.getMessage() : "Authentication failed. Please check your network.";
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToDashboard(String id, String name, String role) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("USER_ID", id);
        intent.putExtra("USER_NAME", name);
        intent.putExtra("USER_ROLE", role);
        startActivity(intent);
        finish(); // Ensure user cannot go back to login screen via back button
    }
}
