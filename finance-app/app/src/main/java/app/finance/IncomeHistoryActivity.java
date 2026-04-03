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
 
public class IncomeHistoryActivity extends AppCompatActivity {
 
    private String currentUserId, targetUserId;
    private LinearLayout layoutFeed;
    private FinanceViewModel viewModel;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_history);
 
        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
 
        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");
        targetUserId = getIntent().getStringExtra("TARGET_USER_ID");
 
        Toolbar toolbar = findViewById(R.id.toolbar_income_history);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }
 
        layoutFeed = findViewById(R.id.layout_income_history_feed);
        findViewById(R.id.btn_income_history_add).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddIncomeActivity.class);
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
        viewModel.getSummary(currentUserId, targetUserId, "income").observe(this, response -> {
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
        if (!"income".equalsIgnoreCase(rec.getType())) return;
 
        View card = getLayoutInflater().inflate(R.layout.item_transaction_card, layoutFeed, false);
        TextView tvCategory = card.findViewById(R.id.card_transaction_category);
        TextView tvDate = card.findViewById(R.id.card_transaction_date);
        TextView tvAmount = card.findViewById(R.id.card_transaction_amount);
        android.widget.ImageView ivIcon = card.findViewById(R.id.card_transaction_icon);
 
        boolean isActualIncome = "income".equalsIgnoreCase(rec.getType());
        tvAmount.setText(String.format(Locale.getDefault(), "%s$%.2f", isActualIncome ? "+" : "-", Math.abs(rec.getAmount())));
        tvAmount.setTextColor(isActualIncome ? 0xFF03DAC5 : 0xFFCF6679);
        ivIcon.setImageResource(getIcon(rec.getCategory()));
        layoutFeed.addView(card);
    }
 
    private int getIcon(String cat) {
        if (cat == null) return android.R.drawable.ic_menu_today;
        String c = cat.toLowerCase();
        if (c.contains("salary")) return android.R.drawable.ic_menu_myplaces;
        if (c.contains("investment")) return android.R.drawable.ic_menu_share;
        return android.R.drawable.ic_menu_today;
    }
}
