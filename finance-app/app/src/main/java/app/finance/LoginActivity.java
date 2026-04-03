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

    private EditText etEmail;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FinanceViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        etEmail = findViewById(R.id.et_login_email);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Dummy user object for login request (only email is needed)
        User loginRequest = new User("", email, "");

        viewModel.login(loginRequest).observe(this, response -> {
            btnLogin.setEnabled(true);
            progressBar.setVisibility(View.GONE);

            if (response != null && response.isSuccess() && response.getData() != null) {
                User loggedInUser = response.getData();
                
                // Navigate to Dashboard
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("USER_ID", String.valueOf(loggedInUser.getId()));
                intent.putExtra("USER_NAME", loggedInUser.getName());
                intent.putExtra("USER_ROLE", loggedInUser.getRole());
                startActivity(intent);
                finish(); // Close login screen
            } else {
                String error = (response != null) ? response.getMessage() : "Invalid email or network error";
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
