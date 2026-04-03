package app.finance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private com.google.android.material.button.MaterialButton btnManage;

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
        btnManage = findViewById(R.id.btn_summary_goto_management);

        tvName.setText(targetUserName);
        tvEmail.setText(targetUserEmail);

        btnManage.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManagementActivity.class);
            intent.putExtra("CURRENT_USER_ID", currentUserId);
            intent.putExtra("CURRENT_USER_ROLE", currentUserRole);
            intent.putExtra("TARGET_USER_ID", targetUserId);
            intent.putExtra("TARGET_USER_NAME", targetUserName);
            intent.putExtra("TARGET_USER_EMAIL", targetUserEmail);
            startActivity(intent);
        });

        fetchSummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchSummary();
    }

    private void fetchSummary() {
        viewModel.getSummary(currentUserId, targetUserId).observe(this, response -> {
            if (response != null && response.getData() != null) {
                Summary s = response.getData();
                tvBalance.setText(String.format(Locale.getDefault(), "$%.2f", s.getNetBalance()));
                tvTotalIncome.setText(String.format(Locale.getDefault(), "+$%.2f", s.getTotalIncome()));
                tvTotalExpense.setText(String.format(Locale.getDefault(), "-$%.2f", s.getTotalExpense()));

                layoutCategoryList.removeAllViews();
                if (s.getCategoryWise() != null) {
                    for (Summary.CategoryTotal item : s.getCategoryWise()) {
                        TextView tv = new TextView(this);
                        tv.setText(String.format(Locale.getDefault(), "• %s: $%.2f", item.getCategory(), item.getTotal()));
                        tv.setTextColor((int) 0xFFBB86FC);
                        tv.setPadding(0, 4, 0, 8);
                        layoutCategoryList.addView(tv);
                    }
                }

                layoutTrendsMonthly.removeAllViews();
                if (s.getMonthlyTrends() != null) {
                    for (Summary.MonthlyTrend trend : s.getMonthlyTrends()) {
                        TextView tv = new TextView(this);
                        String symbol = "income".equals(trend.getType()) ? "📈" : "📉";
                        tv.setText(String.format(Locale.getDefault(), "%s %s: $%.2f (%s)", symbol, trend.getMonth(), trend.getTotal(), trend.getType().toUpperCase()));
                        tv.setTextColor((int) 0xFF88FFFFFFL);
                        tv.setPadding(0, 4, 0, 4);
                        layoutTrendsMonthly.addView(tv);
                    }
                }

                layoutTrendsWeekly.removeAllViews();
                if (s.getWeeklyTrends() != null) {
                    for (Summary.WeeklyTrend trend : s.getWeeklyTrends()) {
                        TextView tv = new TextView(this);
                        String symbol = "income".equals(trend.getType()) ? "🔄" : "💸";
                        tv.setText(String.format(Locale.getDefault(), "%s %s: $%.2f", symbol, trend.getWeek(), trend.getTotal()));
                        tv.setTextColor((int) 0xFFBB86FC);
                        tv.setPadding(0, 4, 0, 4);
                        layoutTrendsWeekly.addView(tv);
                    }
                }
            }
        });
    }
}
