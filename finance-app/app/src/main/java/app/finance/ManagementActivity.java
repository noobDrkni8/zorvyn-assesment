package app.finance;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.ChipGroup;

import app.finance.models.Record;
import app.finance.models.Summary;
import app.finance.viewmodel.FinanceViewModel;

import java.util.Locale;

public class ManagementActivity extends AppCompatActivity {

    private String currentUserId, currentUserRole, targetUserId;
    private String targetUserName, targetUserEmail;

    private LinearLayout layoutActivityFeed;
    private EditText etAmount;
    private Spinner spinnerCategory;
    private com.google.android.material.button.MaterialButton btnAddIncome, btnAddExpense;
    private ChipGroup chipFilters;

    private FinanceViewModel viewModel;

    private static final String[] CATEGORIES = {
            "Select Category", "Salary", "Food", "Rent", "Utilities", 
            "Transport", "Healthcare", "Shopping", "Entertainment", 
            "Investment", "Miscellaneous"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        // Get Data from Intent
        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");
        currentUserRole = getIntent().getStringExtra("CURRENT_USER_ROLE");
        targetUserId = getIntent().getStringExtra("TARGET_USER_ID");
        targetUserName = getIntent().getStringExtra("TARGET_USER_NAME");
        targetUserEmail = getIntent().getStringExtra("TARGET_USER_EMAIL");

        // UI Hooks
        Toolbar toolbar = findViewById(R.id.toolbar_management);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        layoutActivityFeed = findViewById(R.id.layout_mgmt_activity_feed);
        etAmount = findViewById(R.id.et_mgmt_record_amount);
        spinnerCategory = findViewById(R.id.spinner_mgmt_category);
        btnAddIncome = findViewById(R.id.btn_mgmt_add_income);
        btnAddExpense = findViewById(R.id.btn_mgmt_add_expense);
        chipFilters = findViewById(R.id.chip_group_mgmt_filters);

        // Populate Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Action Listeners
        btnAddIncome.setOnClickListener(v -> submitRecord("income"));
        btnAddExpense.setOnClickListener(v -> submitRecord("expense"));

        chipFilters.setOnCheckedChangeListener((group, checkedId) -> fetchAuditTrail());

        fetchAuditTrail();
    }

    private void fetchAuditTrail() {
        String filterType = null;
        int checkedId = chipFilters.getCheckedChipId();
        if (checkedId == R.id.chip_filter_income) filterType = "income";
        else if (checkedId == R.id.chip_filter_expense) filterType = "expense";

        final String finalType = filterType;
        viewModel.getSummary(currentUserId, targetUserId).observe(this, response -> {
            if (response != null && response.getData() != null) {
                Summary s = response.getData();
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
        android.view.View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, layoutActivityFeed, false);
        TextView tv1 = view.findViewById(android.R.id.text1);
        TextView tv2 = view.findViewById(android.R.id.text2);

        String symbol = "income".equals(rec.getType()) ? "+" : "-";
        int color = "income".equals(rec.getType()) ? 0xFF03DAC5 : 0xFFCF6679;

        tv1.setText(String.format(Locale.getDefault(), "%s %s$%.2f - %s", rec.getDate(), symbol, rec.getAmount(), rec.getCategory()));
        tv1.setTextColor(color);
        tv2.setText(rec.getNotes() != null ? rec.getNotes() : "No Audit Description");
        tv2.setTextColor((int) 0xFF88FFFFFFL);

        view.setPadding(0, 16, 0, 16);
        view.setOnClickListener(v -> showEditDialog(rec));
        view.setOnLongClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Audit Trail?")
                .setMessage("Are you sure you want to remove this financial entry?")
                .setPositiveButton("Delete", (dialog, which) -> deleteRecord(rec.getId()))
                .setNegativeButton("Cancel", null)
                .show();
            return true;
        });

        layoutActivityFeed.addView(view);
    }

    private void submitRecord(String type) {
        String amt = etAmount.getText().toString();
        String cat = spinnerCategory.getSelectedItem().toString();
        if (amt.isEmpty() || cat.equals(CATEGORIES[0])) {
            Toast.makeText(this, "Amount & Category Required", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amt);
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());
        Record r = new Record(amount, type, cat, date, "Inspector Audit");
        r.setTargetUserId(targetUserId);

        viewModel.addRecord(currentUserId, r).observe(this, response -> {
            if (response != null && response.isSuccess()) {
                Toast.makeText(this, "Audit record logged!", Toast.LENGTH_SHORT).show();
                etAmount.setText("");
                spinnerCategory.setSelection(0);
                fetchAuditTrail();
            }
        });
    }

    private void showEditDialog(Record rec) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etEditAmt = new EditText(this);
        etEditAmt.setHint("Amount");
        etEditAmt.setText(String.valueOf(rec.getAmount()));
        etEditAmt.setPadding(0, 20, 0, 20);
        etEditAmt.setTextColor(0xFFFFFFFF);
        etEditAmt.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etEditAmt);

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Modify Financial Entry")
            .setView(layout)
            .setPositiveButton("Update", (dialog, which) -> {
                String valAmt = etEditAmt.getText().toString();
                if (!valAmt.isEmpty()) {
                    rec.setAmount(Double.parseDouble(valAmt));
                    viewModel.updateRecord(currentUserId, rec.getId(), rec).observe(this, response -> {
                        if (response != null && response.isSuccess()) {
                            Toast.makeText(this, "Updated.", Toast.LENGTH_SHORT).show();
                            fetchAuditTrail();
                        }
                    });
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteRecord(int recordId) {
        viewModel.deleteRecord(currentUserId, recordId).observe(this, response -> {
            if (response != null && response.isSuccess()) {
                Toast.makeText(this, "Audit entry purged.", Toast.LENGTH_SHORT).show();
                fetchAuditTrail();
            }
        });
    }
}
