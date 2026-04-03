package app.finance;
 
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
 
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
 
import app.finance.models.Record;
import app.finance.models.Summary;
import app.finance.viewmodel.FinanceViewModel;
 
import java.util.Locale;
 
public class ExpenseHistoryActivity extends AppCompatActivity {
 
    private String currentUserId, targetUserId;
    private LinearLayout layoutFeed;
    private FinanceViewModel viewModel;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_history);
 
        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
 
        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");
        targetUserId = getIntent().getStringExtra("TARGET_USER_ID");
 
        Toolbar toolbar = findViewById(R.id.toolbar_expense_history);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }
 
        layoutFeed = findViewById(R.id.layout_expense_history_feed);
        findViewById(R.id.btn_expense_history_add).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            intent.putExtra("CURRENT_USER_ID", currentUserId);
            intent.putExtra("TARGET_USER_ID", targetUserId);
            startActivity(intent);
        });
 
        fetchHistory();
    }
 
    @Override
    protected void onResume() {
        super.onResume();
        fetchHistory();
    }
 
    private void fetchHistory() {
        viewModel.getSummary(currentUserId, targetUserId, "expense").observe(this, response -> {
            if (response != null && response.getData() != null) {
                Summary s = response.getData();
                layoutFeed.removeAllViews();
                if (s.getRecentActivity() != null) {
                    for (Record rec : s.getRecentActivity()) {
                        displayItem(rec);
                    }
                }
            }
        });
    }
 
    private void displayItem(Record rec) {
        if (!"expense".equalsIgnoreCase(rec.getType())) return;
 
        View card = getLayoutInflater().inflate(R.layout.item_transaction_card, layoutFeed, false);
        TextView tvCategory = card.findViewById(R.id.card_transaction_category);
        TextView tvDate = card.findViewById(R.id.card_transaction_date);
        TextView tvAmount = card.findViewById(R.id.card_transaction_amount);
        android.widget.ImageView ivIcon = card.findViewById(R.id.card_transaction_icon);
 
        boolean isActualExpense = "expense".equalsIgnoreCase(rec.getType());
        tvAmount.setText(String.format(Locale.getDefault(), "%s$%.2f", isActualExpense ? "-" : "+", Math.abs(rec.getAmount())));
        tvAmount.setTextColor(isActualExpense ? 0xFFCF6679 : 0xFF03DAC5);
        ivIcon.setImageResource(getIcon(rec.getCategory()));
        layoutFeed.addView(card);
    }
 
    private int getIcon(String cat) {
        if (cat == null) return android.R.drawable.ic_menu_today;
        String c = cat.toLowerCase();
        if (c.contains("food")) return android.R.drawable.ic_menu_view;
        if (c.contains("rent")) return android.R.drawable.ic_menu_today;
        if (c.contains("transport")) return android.R.drawable.ic_menu_directions;
        if (c.contains("utilities")) return android.R.drawable.ic_menu_edit;
        if (c.contains("health")) return android.R.drawable.ic_dialog_info;
        return android.R.drawable.ic_menu_today;
    }
}
