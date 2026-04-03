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

public class InsightsActivity extends AppCompatActivity {

    private String currentUserId, currentUserRole;
    private EditText etSearch;
    private LinearLayout containerResult;
    private ProgressBar progressBar;
    private TextView labelResult;
    private FinanceViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insights);

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
        currentUserId = getIntent().getStringExtra("USER_ID");
        currentUserRole = getIntent().getStringExtra("USER_ROLE");

        Toolbar toolbar = findViewById(R.id.toolbar_insights);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etSearch = findViewById(R.id.et_search_query);
        containerResult = findViewById(R.id.container_search_result);
        progressBar = findViewById(R.id.pb_insights);
        labelResult = findViewById(R.id.label_result);

        findViewById(R.id.btn_perform_search).setOnClickListener(v -> performSearch());
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter an email or name", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = null;
        String email = null;

        if (query.contains("@")) {
            email = query;
        } else {
            name = query;
        }

        progressBar.setVisibility(View.VISIBLE);
        viewModel.searchUser(currentUserId, name, email).observe(this, response -> {
            progressBar.setVisibility(View.GONE);
            containerResult.removeAllViews();
            if (response != null && response.getData() != null) {
                labelResult.setVisibility(View.VISIBLE);
                displayUserCard(response.getData());
            } else {
                labelResult.setVisibility(View.GONE);
                Toast.makeText(this, "No identity found matching that query", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserCard(User u) {
        View card = getLayoutInflater().inflate(R.layout.item_user, containerResult, false);
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
            intent.putExtra("CURRENT_USER_ROLE", currentUserRole);
            intent.putExtra("TARGET_USER_ID", String.valueOf(u.getId()));
            intent.putExtra("TARGET_USER_NAME", u.getName());
            intent.putExtra("TARGET_USER_EMAIL", u.getEmail());
            intent.putExtra("TARGET_USER_ROLE", u.getRole());
            intent.putExtra("TARGET_USER_STATUS", u.getStatus());
            startActivity(intent);
        });
        containerResult.addView(card);
    }
}