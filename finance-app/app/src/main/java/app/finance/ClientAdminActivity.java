package app.finance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import app.finance.models.User;
import app.finance.viewmodel.FinanceViewModel;

public class ClientAdminActivity extends AppCompatActivity {

    private String currentUserId;
    private EditText etSearch;
    private LinearLayout layoutList;
    private ProgressBar progressBar;
    private FinanceViewModel viewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_admin);

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");

        Toolbar toolbar = findViewById(R.id.toolbar_client_admin);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etSearch = findViewById(R.id.et_search_client);
        layoutList = findViewById(R.id.layout_client_list_container);
        progressBar = findViewById(R.id.pb_client_admin);

        findViewById(R.id.btn_search_client).setOnClickListener(v -> performClientSearch());
        
        sessionManager = new SessionManager(this);
        String role = sessionManager.getUserRole();
        
        View btnCreate = findViewById(R.id.btn_nav_create_client);
        if ("analyst".equalsIgnoreCase(role)) {
            btnCreate.setVisibility(View.GONE);
        }
        
        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateClientActivity.class);
            intent.putExtra("CURRENT_USER_ID", currentUserId);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchClients();
    }

    private void fetchClients() {
        progressBar.setVisibility(View.VISIBLE);
        viewModel.getUsers(currentUserId).observe(this, response -> {
            progressBar.setVisibility(View.GONE);
            if (response != null && response.getData() != null) {
                layoutList.removeAllViews();
                for (User u : response.getData()) {
                    if ("viewer".equalsIgnoreCase(u.getRole())) {
                        displayUserCard(u);
                    }
                }
            } else {
                Toast.makeText(this, "Failed to load clients", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performClientSearch() {
        String searchTerm = etSearch.getText().toString().trim();
        if (searchTerm.isEmpty()) {
            fetchClients();
            return;
        }

        String name = null;
        String email = null;
        if (searchTerm.contains("@")) {
            email = searchTerm;
        } else {
            name = searchTerm;
        }

        progressBar.setVisibility(View.VISIBLE);
        viewModel.searchUser(currentUserId, name, email).observe(this, response -> {
            progressBar.setVisibility(View.GONE);
            layoutList.removeAllViews();
            if (response != null && response.getData() != null) {
                User u = response.getData();
                if ("viewer".equalsIgnoreCase(u.getRole())) {
                    displayUserCard(u);
                } else {
                    Toast.makeText(this, "No client found with that name/email", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No user found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserCard(User u) {
        View card = getLayoutInflater().inflate(R.layout.item_user, layoutList, false);
        TextView tvInitial = card.findViewById(R.id.tv_user_initial);
        TextView tvName = card.findViewById(R.id.tv_item_user_name);
        TextView tvEmail = card.findViewById(R.id.tv_item_user_email);
        TextView tvStatus = card.findViewById(R.id.tv_item_user_status);

        String initial = (u.getName() != null && !u.getName().isEmpty()) ? u.getName().substring(0, 1).toUpperCase() : "?";
        tvInitial.setText(initial);
        tvName.setText(u.getName());
        tvEmail.setText(u.getEmail());

        boolean isActive = "active".equals(u.getStatus());
        tvStatus.setText(isActive ? "ACTIVE" : "DISABLED");
        tvStatus.setTextColor(isActive ? 0xFF03DAC5 : 0xFFCF6679);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, SummaryActivity.class);
            intent.putExtra("CURRENT_USER_ID", currentUserId);
            intent.putExtra("CURRENT_USER_ROLE", sessionManager.getUserRole());
            intent.putExtra("TARGET_USER_ID", String.valueOf(u.getId()));
            intent.putExtra("TARGET_USER_NAME", u.getName());
            intent.putExtra("TARGET_USER_EMAIL", u.getEmail());
            intent.putExtra("TARGET_USER_ROLE", u.getRole());
            intent.putExtra("TARGET_USER_STATUS", u.getStatus());
            startActivity(intent);
        });
        layoutList.addView(card);
    }
}