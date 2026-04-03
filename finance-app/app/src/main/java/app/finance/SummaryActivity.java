package app.finance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import app.finance.models.Summary;
import app.finance.viewmodel.FinanceViewModel;

import java.util.Locale;

public class SummaryActivity extends AppCompatActivity {

    private String currentUserId, currentUserRole, targetUserId;
    private String targetUserName, targetUserEmail;

    private TextView tvName, tvEmail, tvBalance, tvTotalIncome, tvTotalExpense;
    private LinearLayout layoutCategoryList, layoutTrendsMonthly, layoutTrendsWeekly;
    private com.google.android.material.card.MaterialCardView cardIncome, cardExpense;

    private FinanceViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        // Get Data from Intent
        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");
        currentUserRole = getIntent().getStringExtra("CURRENT_USER_ROLE");
        targetUserId = getIntent().getStringExtra("TARGET_USER_ID");
        targetUserName = getIntent().getStringExtra("TARGET_USER_NAME");
        targetUserEmail = getIntent().getStringExtra("TARGET_USER_EMAIL");

        // UI Hooks
        Toolbar toolbar = findViewById(R.id.toolbar_summary);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        tvName = findViewById(R.id.tv_summary_user_name);
        tvEmail = findViewById(R.id.tv_summary_user_email);
        tvBalance = findViewById(R.id.tv_summary_balance);
        tvTotalIncome = findViewById(R.id.tv_summary_total_income);
        tvTotalExpense = findViewById(R.id.tv_summary_total_expense);
        layoutCategoryList = findViewById(R.id.layout_summary_category_list);
        layoutTrendsMonthly = findViewById(R.id.layout_summary_trends_monthly);
        layoutTrendsWeekly = findViewById(R.id.layout_summary_trends_weekly);
        cardIncome = findViewById(R.id.card_summary_income);
        cardExpense = findViewById(R.id.card_summary_expense);

        tvName.setText(targetUserName);
        tvEmail.setVisibility(View.GONE); // Email is now merged into Name field
 
        com.google.android.material.chip.ChipGroup filterGroup = findViewById(R.id.chip_group_summary_filter);
        filterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String filter = null;
            if (checkedIds.contains(R.id.chip_summary_income)) filter = "income";
            else if (checkedIds.contains(R.id.chip_summary_expense)) filter = "expense";
            fetchSummary(filter);
        });
 
        // Card Navigation
        cardIncome.setOnClickListener(v -> {
            Intent intent = new Intent(this, IncomeHistoryActivity.class);
            intent.putExtra("CURRENT_USER_ID", currentUserId);
            intent.putExtra("TARGET_USER_ID", targetUserId);
            startActivity(intent);
        });
 
        cardExpense.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExpenseHistoryActivity.class);
            intent.putExtra("CURRENT_USER_ID", currentUserId);
            intent.putExtra("TARGET_USER_ID", targetUserId);
            startActivity(intent);
        });
 
        fetchSummary(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Keep current filter if possible, default to null for now
        fetchSummary(null);
    }

    private void fetchSummary(String type) {
        viewModel.getSummary(currentUserId, targetUserId, type).observe(this, response -> {
            if (response != null && response.getData() != null) {
                Summary s = response.getData();
                
                tvName.setText("Client - " + targetUserEmail);
                
                tvBalance.setText(String.format(Locale.getDefault(), "$%.2f", s.getNetBalance()));
                tvTotalIncome.setText(String.format(Locale.getDefault(), "+$%.2f", s.getTotalIncome()));
                tvTotalExpense.setText(String.format(Locale.getDefault(), "-$%.2f", s.getTotalExpense()));

                layoutCategoryList.removeAllViews();
                if (s.getCategoryWise() != null) {
                    for (Summary.CategoryTotal cat : s.getCategoryWise()) {
                        View row = getLayoutInflater().inflate(R.layout.item_summary_row, layoutCategoryList, false);
                        TextView tvLabel = row.findViewById(R.id.tv_summary_row_label);
                        TextView tvValue = row.findViewById(R.id.tv_summary_row_value);
                        com.google.android.material.progressindicator.LinearProgressIndicator pb = row.findViewById(R.id.progress_summary_row);
                        
                        tvLabel.setText(cat.getCategory());
                        tvValue.setText(String.format(Locale.getDefault(), "$%.2f", cat.getTotal()));
                        pb.setProgress((int) cat.getPercentage());
                        
                        // Set color based on current filter or category context
                        layoutCategoryList.addView(row);
                    }
                }

                layoutTrendsMonthly.removeAllViews();
                if (s.getMonthlyTrends() != null) {
                    for (Summary.MonthlyTrend trend : s.getMonthlyTrends()) {
                        View row = getLayoutInflater().inflate(R.layout.item_trend_row, layoutTrendsMonthly, false);
                        TextView tvLabel = row.findViewById(R.id.tv_summary_row_label);
                        TextView tvValue = row.findViewById(R.id.tv_summary_row_value);

                        boolean isInc = "income".equalsIgnoreCase(trend.getType());
                        String symbol = isInc ? "📈" : "📉";
                        tvLabel.setText(String.format("%s %s", symbol, trend.getMonth()));
                        tvValue.setText(String.format(Locale.getDefault(), "$%.2f", trend.getTotal()));
                        tvValue.setTextColor(isInc ? 0xFF03DAC5 : 0xFFCF6679);
                        layoutTrendsMonthly.addView(row);
                    }
                }

                // Weekly Data Insights (Now showing Recent Transactions)
                layoutTrendsWeekly.removeAllViews();
                if (s.getRecentActivity() != null) {
                    for (app.finance.models.Record rec : s.getRecentActivity()) {
                        View row = getLayoutInflater().inflate(R.layout.item_dashboard_transaction, layoutTrendsWeekly, false);
                        
                        TextView tvCat = row.findViewById(R.id.tv_dash_trans_category);
                        TextView tvNote = row.findViewById(R.id.tv_dash_trans_note);
                        TextView tvAmt = row.findViewById(R.id.tv_dash_trans_amount);
                        TextView tvDate = row.findViewById(R.id.tv_dash_trans_date);
 
                        boolean isInc = "income".equalsIgnoreCase(rec.getType());
                        tvCat.setText(rec.getCategory());
                        tvNote.setText(rec.getNotes());
                        tvDate.setText(rec.getDate());
                        
                        tvAmt.setText(String.format(Locale.getDefault(), "%s$%.2f", isInc ? "+" : "-", rec.getAmount()));
                        tvAmt.setTextColor(isInc ? 0xFF03DAC5 : 0xFFCF6679);
 
                        layoutTrendsWeekly.addView(row);
                    }
                }
            } else {
                Toast.makeText(this, "Audit Sync Failed: " + (response != null ? response.getMessage() : "Unknown"), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
