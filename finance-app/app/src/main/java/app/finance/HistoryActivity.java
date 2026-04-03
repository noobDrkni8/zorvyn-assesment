package app.finance;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import app.finance.viewmodel.FinanceViewModel;

public class HistoryActivity extends AppCompatActivity {

    private String currentUserId, targetUserId;
    private RecyclerView rvHistory;
    private RecordAdapter adapter;
    private ProgressBar progressBar;
    private FinanceViewModel viewModel;

    private Handler syncHandler = new Handler();
    private Runnable syncRunnable = new Runnable() {
        @Override
        public void run() {
            fetchHistory();
            syncHandler.postDelayed(this, 30000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
        currentUserId = getIntent().getStringExtra("USER_ID");
        targetUserId = getIntent().getStringExtra("TARGET_USER_ID");

        Toolbar toolbar = findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
            if (getIntent().hasExtra("TARGET_USER_NAME")) {
                getSupportActionBar().setTitle(getIntent().getStringExtra("TARGET_USER_NAME") + "'s History");
            }
        }

        rvHistory = findViewById(rv_history);
        progressBar = findViewById(R.id.pb_history);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecordAdapter();
        rvHistory.setAdapter(adapter);

        fetchHistory();
    }

    private void fetchHistory() {
        progressBar.setVisibility(View.VISIBLE);
        // If targetUserId is null, it fetches current user's history
        viewModel.getRecords(currentUserId, targetUserId).observe(this, response -> {
            progressBar.setVisibility(View.GONE);
            if (response != null && response.getData() != null) {
                adapter.setRecords(response.getData());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncHandler.postDelayed(syncRunnable, 30000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        syncHandler.removeCallbacks(syncRunnable);
    }
}