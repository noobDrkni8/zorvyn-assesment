package app.finance;

import android.os.Bundle;
import android.view.View;
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
    private String targetUserName, targetUserEmail, activeFilter;

    private LinearLayout layoutActivityFeed;
    private com.google.android.material.button.MaterialButton btnAddIncome, btnAddExpense;

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
        btnAddIncome = findViewById(R.id.btn_mgmt_add_income_start);
        btnAddExpense = findViewById(R.id.btn_mgmt_add_expense_start);
 
        activeFilter = getIntent().getStringExtra("INITIAL_FILTER");
        updateSubtitle(toolbar);
 
        // Action Listeners
        btnAddIncome.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AddIncomeActivity.class);
            intent.putExtra("CURRENT_USER_ID", currentUserId);
            intent.putExtra("TARGET_USER_ID", targetUserId);
            startActivity(intent);
        });
 
        btnAddExpense.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AddExpenseActivity.class);
            intent.putExtra("CURRENT_USER_ID", currentUserId);
            intent.putExtra("TARGET_USER_ID", targetUserId);
            startActivity(intent);
        });
 
        fetchAuditTrail();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAuditTrail();
    }

    private void fetchAuditTrail() {
        viewModel.getSummary(currentUserId, targetUserId, activeFilter).observe(this, response -> {
            if (response != null && response.getData() != null) {
                app.finance.models.Summary s = response.getData();
                layoutActivityFeed.removeAllViews();
                if (s.getRecentActivity() != null) {
                    for (app.finance.models.Record rec : s.getRecentActivity()) {
                        displayActivityItem(rec);
                    }
                }
            }
        });
    }

    private void displayActivityItem(Record rec) {
        View card = getLayoutInflater().inflate(R.layout.item_transaction_card, layoutActivityFeed, false);
        
        TextView tvCategory = card.findViewById(R.id.card_transaction_category);
        TextView tvDate = card.findViewById(R.id.card_transaction_date);
        TextView tvAmount = card.findViewById(R.id.card_transaction_amount);
        android.widget.ImageView ivIcon = card.findViewById(R.id.card_transaction_icon);
        
        tvCategory.setText(rec.getCategory());
        tvDate.setText(String.format("%s • %s", rec.getDate(), rec.getNotes()));
        
        boolean isIncome = "income".equalsIgnoreCase(rec.getType());
        String prefix = isIncome ? "+" : "-";
        tvAmount.setText(String.format(Locale.getDefault(), "%s$%.2f", prefix, rec.getAmount()));
        tvAmount.setTextColor(isIncome ? 0xFF03DAC5 : 0xFFCF6679);
        
        // Icon Mapping
        int iconRes = getIconForCategory(rec.getCategory());
        ivIcon.setImageResource(iconRes);
        ivIcon.setColorFilter(isIncome ? 0xFF03DAC5 : 0xFFCF6679);

        card.setOnClickListener(v -> showEditDialog(rec));
        card.setOnLongClickListener(v -> {
            showDeleteConfirm(rec);
            return true;
        });

        layoutActivityFeed.addView(card);
    }

    private int getIconForCategory(String cat) {
        if (cat == null) return android.R.drawable.ic_menu_help;
        String c = cat.toLowerCase();
        if (c.contains("salary")) return android.R.drawable.ic_menu_myplaces;
        if (c.contains("food")) return android.R.drawable.ic_menu_view;
        if (c.contains("rent")) return android.R.drawable.ic_menu_today;
        if (c.contains("transport")) return android.R.drawable.ic_menu_directions;
        if (c.contains("utilities")) return android.R.drawable.ic_menu_edit;
        if (c.contains("health")) return android.R.drawable.ic_dialog_info;
        if (c.contains("investment")) return android.R.drawable.ic_menu_share;
        if (c.contains("gift")) return android.R.drawable.ic_menu_send;
        return android.R.drawable.ic_menu_help;
    }

    private void showDeleteConfirm(Record rec) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Purge Audit Record?")
            .setMessage("Are you sure you want to remove this financial entry from the auditor ledger?")
            .setPositiveButton("Purge", (dialog, which) -> deleteRecord(rec.getId()))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void updateSubtitle(Toolbar toolbar) {
        if (activeFilter != null) {
            String sub = activeFilter.substring(0, 1).toUpperCase() + activeFilter.substring(1) + " Audit History";
            toolbar.setSubtitle(sub);
            
            // Dynamic Action Filtering
            if ("income".equalsIgnoreCase(activeFilter)) {
                btnAddExpense.setVisibility(View.GONE);
                btnAddIncome.setVisibility(View.VISIBLE);
            } else if ("expense".equalsIgnoreCase(activeFilter)) {
                btnAddIncome.setVisibility(View.GONE);
                btnAddExpense.setVisibility(View.VISIBLE);
            }
        } else {
            toolbar.setSubtitle("Full Transaction History");
            btnAddIncome.setVisibility(View.VISIBLE);
            btnAddExpense.setVisibility(View.VISIBLE);
        }
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
