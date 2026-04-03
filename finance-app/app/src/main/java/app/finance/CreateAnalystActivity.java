package app.finance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import app.finance.api.FinanceApiService;
import app.finance.api.RetrofitClient;
import app.finance.models.ApiResponse;
import app.finance.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAnalystActivity extends AppCompatActivity {

    private String currentUserId;
    private EditText etName, etEmail;
    private FinanceApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_analyst);

        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");
        apiService = RetrofitClient.getApiService();

        Toolbar toolbar = findViewById(R.id.toolbar_create_analyst);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etName = findViewById(R.id.et_create_analyst_name);
        etEmail = findViewById(R.id.et_create_analyst_email);

        findViewById(R.id.btn_finalize_create_analyst).setOnClickListener(v -> submitNewAnalyst());
    }

    private void submitNewAnalyst() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(name, email, "analyst");
        apiService.createUser(currentUserId, newUser).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateAnalystActivity.this, "Analyst Successfully Provisioned", Toast.LENGTH_SHORT).show();
                    finish(); // Return to management list
                } else {
                    Toast.makeText(CreateAnalystActivity.this, "Provisioning failed", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(CreateAnalystActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
