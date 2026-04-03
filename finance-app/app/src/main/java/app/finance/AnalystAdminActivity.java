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

public class AnalystAdminActivity extends AppCompatActivity {

    private String currentUserId;
    private EditText etSearch;
    private LinearLayout layoutList;
    private ProgressBar progressBar;
    private FinanceViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyst_admin);

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");

        Toolbar toolbar = findViewById(R.id.toolbar_analyst_admin);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etSearch = findViewById(R.id.et_search_analyst);
        layoutList = findViewById(R.id.layout_analyst_list_container);
        progressBar = findViewById(R.id.pb_analyst_admin);

        findViewById(R.id.btn_search_analyst).setOnClickListener(v -> performAnalystSearch());
        findViewById(R.id.btn_nav_create_analyst).setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateAnalystActivity.class);
            intent.putExtra("CURRENT_USER_ID", currentUserId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAnalysts();
    }

    private void fetchAnalysts() {
        progressBar.setVisibility(View.VISIBLE);
        viewModel.getUsers(currentUserId).observe(this, response -> {
            progressBar.setVisibility(View.GONE);
            if (response != null && response.getData() != null) {
                layoutList.removeAllViews();
                for (User u : response.getData()) {
                    if ("analyst".equals(u.getRole().toLowerCase())) {
                        displayUserCard(u);
                    }
                }
            } else {
                Toast.makeText(this, "Failed to load analysts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performAnalystSearch() {
        String searchTerm = etSearch.getText().toString().trim();
        if (searchTerm.isEmpty()) {
            fetchAnalysts();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        viewModel.searchUser(currentUserId, searchTerm).observe(this, response -> {
            progressBar.setVisibility(View.GONE);
            layoutList.removeAllViews();
            if (response != null && response.getData() != null) {
                User u = response.getData();
                if ("analyst".equals(u.getRole().toLowerCase())) {
                    displayUserCard(u);
                } else {
                    Toast.makeText(this, "No analyst found with that name/email", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No user found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserCard(User u) {
        View card = getLayoutInflater().inflate(R.layout.item_user, null);
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
            Intent intent = new Intent(this, InspectionActivity.class);
            intent.putExtra("CURRENT_USER_ID", currentUserId);
            intent.putExtra("CURRENT_USER_ROLE", "admin");
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