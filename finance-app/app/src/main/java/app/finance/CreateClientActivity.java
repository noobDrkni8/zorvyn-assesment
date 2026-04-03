package app.finance;

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

public class CreateClientActivity extends AppCompatActivity {

    private String currentUserId;
    private EditText etName, etEmail, etPassword;
    private FinanceApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_client);

        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");
        apiService = RetrofitClient.getApiService();

        Toolbar toolbar = findViewById(R.id.toolbar_create_client);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etName = findViewById(R.id.et_create_client_name);
        etEmail = findViewById(R.id.et_create_client_email);
        etPassword = findViewById(R.id.et_create_client_password);

        findViewById(R.id.btn_finalize_create_client).setOnClickListener(v -> submitNewClient());
    }

    private void submitNewClient() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields including temporary password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(name, email, "viewer", password);
        apiService.createUser(currentUserId, newUser).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateClientActivity.this, "Client Account Initialized", Toast.LENGTH_SHORT).show();
                    finish(); // Return to management list
                } else if (response.code() == 403) {
                    Toast.makeText(CreateClientActivity.this, "Access Denied: Only Admin can create users", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(CreateClientActivity.this, "Initialization failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(CreateClientActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
