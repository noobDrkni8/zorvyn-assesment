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

import app.finance.viewmodel.FinanceViewModel;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;
    private Button btnUpdate;
    private ProgressBar progressBar;
    private FinanceViewModel viewModel;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        userId = getIntent().getStringExtra("USER_ID");
        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnUpdate = findViewById(R.id.btn_update_password);
        progressBar = findViewById(R.id.pb_change_password);

        btnUpdate.setOnClickListener(v -> performPasswordUpdate());
    }

    private void performPasswordUpdate() {
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (newPass.isEmpty() || newPass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpdate.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        viewModel.changePassword(userId, newPass).observe(this, response -> {
            btnUpdate.setEnabled(true);
            progressBar.setVisibility(View.GONE);

            if (response != null && response.isSuccess()) {
                Toast.makeText(this, "Identity Secured! Accessing Terminal...", Toast.LENGTH_LONG).show();
                
                // After changing password, they are good to go. 
                // Navigate to Dashboard
                SessionManager sessionManager = new SessionManager(this);
                // Note: Logic assumes they still have their role from the login session
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USER_ROLE", sessionManager.getUserRole());
                startActivity(intent);
                finish();
            } else {
                String error = (response != null) ? response.getMessage() : "Update failed";
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
