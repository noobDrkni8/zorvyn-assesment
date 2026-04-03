package app.finance;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import app.finance.models.Record;
import app.finance.models.Summary;
import app.finance.models.User;
import app.finance.viewmodel.FinanceViewModel;

import java.util.Locale;

public class InspectionActivity extends AppCompatActivity {

    private String currentUserId, currentUserRole, targetUserId;
    private String targetUserName, targetUserEmail, targetUserStatus, targetUserRole;

    private TextView tvName, tvEmail, tvBalance, tvTotalIncome, tvTotalExpense;
    private LinearLayout layoutCategoryList, layoutTrends, layoutActivityFeed, panelAddRecord;
    private com.google.android.material.button.MaterialButton btnToggleStatus, btnAddIncome, btnAddExpense;
    private com.google.android.material.chip.ChipGroup chipGroupFilters;
    private EditText etAmount, etCategory;

    private FinanceViewModel viewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
        sessionManager = new SessionManager(this);

        // Get Data from Intent
        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");
        currentUserRole = getIntent().getStringExtra("CURRENT_USER_ROLE");
        targetUserId = getIntent().getStringExtra("TARGET_USER_ID");
        targetUserName = getIntent().getStringExtra("TARGET_USER_NAME");
        targetUserEmail = getIntent().getStringExtra("TARGET_USER_EMAIL");
        targetUserStatus = getIntent().getStringExtra("TARGET_USER_STATUS");
        targetUserRole = getIntent().getStringExtra("TARGET_USER_ROLE");

        // UI Hooks
        Toolbar toolbar = findViewById(R.id.toolbar_inspection);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        tvName = findViewById(R.id.tv_inspect_user_name);
        tvEmail = findViewById(R.id.tv_inspect_user_email);
        tvBalance = findViewById(R.id.tv_inspect_balance);
        tvTotalIncome = findViewById(R.id.tv_inspect_total_income);
        tvTotalExpense = findViewById(R.id.tv_inspect_total_expense);

        layoutCategoryList = findViewById(R.id.layout_inspect_category_list);
        layoutTrends = findViewById(R.id.layout_inspect_trends);
        layoutActivityFeed = findViewById(R.id.layout_inspect_activity_feed);
        panelAddRecord = findViewById(R.id.panel_inspect_add_record);

        btnToggleStatus = findViewById(R.id.btn_inspect_toggle_status);
        btnAddIncome = findViewById(R.id.btn_inspect_add_income);
        btnAddExpense = findViewById(R.id.btn_inspect_add_expense);
        etAmount = findViewById(R.id.et_inspect_record_amount);
        etCategory = findViewById(R.id.et_inspect_record_cat);
        chipGroupFilters = findViewById(R.id.chip_group_filters);

        tvName.setText(targetUserName);
        tvEmail.setText(targetUserEmail);

        // Role-based visibility
        String roleLower = currentUserRole != null ? currentUserRole.toLowerCase() : "";
        if ("admin".equals(roleLower)) {
            btnToggleStatus.setVisibility(android.view.View.VISIBLE);
            panelAddRecord.setVisibility(android.view.View.VISIBLE);
            updateStatusButtonUI();
        } else if ("analyst".equals(roleLower)) {
            panelAddRecord.setVisibility(android.view.View.VISIBLE);
        }

        // Action Listeners
        btnToggleStatus.setOnClickListener(v -> toggleTargetStatus());
        btnAddIncome.setOnClickListener(v -> submitTargetRecord("income"));
        btnAddExpense.setOnClickListener(v -> submitTargetRecord("expense"));

        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            fetchTargetSummary();
        });

        fetchTargetSummary();
    }

    private void updateStatusButtonUI() {
        boolean isActive = "active".equals(targetUserStatus);
        btnToggleStatus.setText(isActive ? "Disable Account" : "Enable Account");
        btnToggleStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                isActive ? 0xFFCF6679 : 0xFF03DAC5));
    }

    private void fetchTargetSummary() {
        String filterType = null;
        int checkedId = chipGroupFilters.getCheckedChipId();
        if (checkedId == R.id.chip_filter_income) filterType = "income";
        else if (checkedId == R.id.chip_filter_expense) filterType = "expense";

        final String finalType = filterType;
        viewModel.getSummary(currentUserId, targetUserId, null).observe(this, response -> {
            if (response != null && response.getData() != null) {
                Summary s = response.getData();
                
                // 1. Overview Totals
                tvBalance.setText(String.format(Locale.getDefault(), "$%.2f", s.getNetBalance()));
                tvTotalIncome.setText(String.format(Locale.getDefault(), "+$%.2f", s.getTotalIncome()));
                tvTotalExpense.setText(String.format(Locale.getDefault(), "-$%.2f", s.getTotalExpense()));
                
                // 2. Category Breakdown
                layoutCategoryList.removeAllViews();
                if (s.getCategoryWise() != null) {
                    for (Summary.CategoryTotal item : s.getCategoryWise()) {
                        TextView tv = new TextView(this);
                        tv.setText(String.format(Locale.getDefault(), "• %s: $%.2f", item.getCategory(), item.getTotal()));
                        tv.setTextColor((int) 0xFFBB86FC);
                        tv.setTextSize(13);
                        tv.setPadding(0, 4, 0, 8);
                        layoutCategoryList.addView(tv);
                    }
                }

                // 3. Historical Trends
                layoutTrends.removeAllViews();
                if (s.getMonthlyTrends() != null) {
                    for (Summary.MonthlyTrend trend : s.getMonthlyTrends()) {
                        TextView tv = new TextView(this);
                        String colorStr = "income".equals(trend.getType()) ? "📈" : "📉";
                        tv.setText(String.format(Locale.getDefault(), "%s %s: $%.2f (%s)", colorStr, trend.getMonth(), trend.getTotal(), trend.getType().toUpperCase()));
                        tv.setTextColor((int) 0xFF88FFFFFFL);
                        tv.setTextSize(12);
                        tv.setPadding(0, 4, 0, 4);
                        layoutTrends.addView(tv);
                    }
                }

                // 4. Activity Feed (with Edit and Delete capability)
                layoutActivityFeed.removeAllViews();
                if (s.getRecentActivity() != null) {
                    for (Record rec : s.getRecentActivity()) {
                        if (finalType == null || finalType.equals(rec.getType())) {
                            displayActivityItem(rec);
                        }
                    }
                }

            }
        });
    }

    private void displayActivityItem(Record rec) {
        View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, layoutActivityFeed, false);
        TextView tv1 = view.findViewById(android.R.id.text1);
        TextView tv2 = view.findViewById(android.R.id.text2);

        String symbol = "income".equals(rec.getType()) ? "+" : "-";
        int color = "income".equals(rec.getType()) ? 0xFF03DAC5 : 0xFFCF6679;
        
        tv1.setText(String.format(Locale.getDefault(), "%s %s$%.2f - %s", rec.getDate(), symbol, rec.getAmount(), rec.getCategory()));
        tv1.setTextColor(color);
        tv1.setTextSize(14);

        tv2.setText(rec.getNotes() != null ? rec.getNotes() : "No Description");
        tv2.setTextColor((int) 0xFF88FFFFFFL);
        
        view.setPadding(0, 16, 0, 16);
        
        // Edit on Single Tap
        view.setOnClickListener(v -> showEditRecordDialog(rec));

        // Add Delete on Long Press
        view.setOnLongClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Audit Trail?")
                .setMessage("Are you sure you want to remove this financial entry?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTargetRecord(rec.getId()))
                .setNegativeButton("Cancel", null)
                .show();
            return true;
        });

        layoutActivityFeed.addView(view);
    }

    private void deleteTargetRecord(int recordId) {
        // We reuse the viewModel.deleteRecord if it exists (Checking ViewModel now)
        // Let's assume it exists or I'll add it.
        // Actually I should check FinanceViewModel for deleteRecord.
        // I will add it if missing.
        viewModel.deleteRecord(currentUserId, recordId).observe(this, response -> {
            if (response != null && response.isSuccess()) {
                Toast.makeText(this, "Audit record purged.", Toast.LENGTH_SHORT).show();
                fetchTargetSummary();
            }
        });
    }

    private void toggleTargetStatus() {
        String newStatus = "active".equals(targetUserStatus) ? "inactive" : "active";
        User u = new User(targetUserName, targetUserEmail, targetUserRole);
        u.setStatus(newStatus);

        viewModel.updateUser(currentUserId, Integer.parseInt(targetUserId), u).observe(this, response -> {
            if (response != null && response.isSuccess()) {
                targetUserStatus = newStatus;
                updateStatusButtonUI();
                Toast.makeText(InspectionActivity.this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitTargetRecord(String type) {
        String amt = etAmount.getText().toString();
        String cat = etCategory.getText().toString();
        if (amt.isEmpty() || cat.isEmpty()) return;

        double amount = Double.parseDouble(amt);
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());
        Record r = new Record(amount, type, cat, date, "Inspector Entry");
        r.setTargetUserId(targetUserId);

        viewModel.addRecord(currentUserId, r).observe(this, response -> {
            if (response != null && response.isSuccess()) {
                Toast.makeText(InspectionActivity.this, "Record logged!", Toast.LENGTH_SHORT).show();
                etAmount.setText("");
                etCategory.setText("");
                fetchTargetSummary();
            }
        });
    }

    private void showEditRecordDialog(Record rec) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etEditAmt = new EditText(this);
        etEditAmt.setHint("Amount");
        etEditAmt.setText(String.valueOf(rec.getAmount()));
        etEditAmt.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etEditAmt);

        final EditText etEditCat = new EditText(this);
        etEditCat.setHint("Category");
        etEditCat.setText(rec.getCategory());
        layout.addView(etEditCat);

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Modify Financial Entry")
            .setView(layout)
            .setPositiveButton("Update", (dialog, which) -> {
                String valAmt = etEditAmt.getText().toString();
                String valCat = etEditCat.getText().toString();
                if (!valAmt.isEmpty() && !valCat.isEmpty()) {
                    rec.setAmount(Double.parseDouble(valAmt));
                    rec.setCategory(valCat);
                    viewModel.updateRecord(currentUserId, rec.getId(), rec).observe(this, response -> {
                        if (response != null && response.isSuccess()) {
                            Toast.makeText(this, "Record updated.", Toast.LENGTH_SHORT).show();
                            fetchTargetSummary();
                        }
                    });
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
