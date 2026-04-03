package app.finance;

import android.os.Bundle;
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

    private TextView tvName, tvEmail, tvBalance;
    private LinearLayout layoutCategoryList, panelAddRecord;
    private com.google.android.material.button.MaterialButton btnToggleStatus, btnAddIncome, btnAddExpense;
    private EditText etAmount, etCategory;

    private FinanceViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

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
        layoutCategoryList = findViewById(R.id.layout_inspect_category_list);
        panelAddRecord = findViewById(R.id.panel_inspect_add_record);
        btnToggleStatus = findViewById(R.id.btn_inspect_toggle_status);
        btnAddIncome = findViewById(R.id.btn_inspect_add_income);
        btnAddExpense = findViewById(R.id.btn_inspect_add_expense);
        etAmount = findViewById(R.id.et_inspect_record_amount);
        etCategory = findViewById(R.id.et_inspect_record_cat);

        tvName.setText(targetUserName);
        tvEmail.setText(targetUserEmail);

        // Role-based visibility
        String roleLower = currentUserRole != null ? currentUserRole.toLowerCase() : "";
        if ("admin".equals(roleLower)) {
            btnToggleStatus.setVisibility(android.view.View.VISIBLE);
            updateStatusButtonUI();
        } else if ("analyst".equals(roleLower)) {
            panelAddRecord.setVisibility(android.view.View.VISIBLE);
        }

        // Action Listeners
        btnToggleStatus.setOnClickListener(v -> toggleTargetStatus());
        btnAddIncome.setOnClickListener(v -> submitTargetRecord("income"));
        btnAddExpense.setOnClickListener(v -> submitTargetRecord("expense"));

        fetchTargetSummary();
    }

    private void updateStatusButtonUI() {
        boolean isActive = "active".equals(targetUserStatus);
        btnToggleStatus.setText(isActive ? "Disable Account" : "Enable Account");
        btnToggleStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                isActive ? 0xFFCF6679 : 0xFF03DAC5));
    }

    private void fetchTargetSummary() {
        viewModel.getSummary(currentUserId, targetUserId).observe(this, response -> {
            if (response != null && response.getData() != null) {
                Summary s = response.getData();
                tvBalance.setText(String.format(Locale.getDefault(), "$%.2f", s.getNetBalance()));
                
                layoutCategoryList.removeAllViews();
                if (s.getCategoryWise() != null) {
                    for (Summary.CategoryTotal item : s.getCategoryWise()) {
                        TextView tv = new TextView(InspectionActivity.this);
                        tv.setText(String.format(Locale.getDefault(), "• %s: $%.2f", item.getCategory(), item.getTotal()));
                        tv.setTextColor(0xFFBB86FC);
                        tv.setPadding(0, 0, 0, 16);
                        layoutCategoryList.addView(tv);
                    }
                }
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
}
