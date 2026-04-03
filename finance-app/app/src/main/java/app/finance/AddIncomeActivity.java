package app.finance;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import app.finance.models.Record;
import app.finance.viewmodel.FinanceViewModel;

import java.util.Locale;

public class AddIncomeActivity extends AppCompatActivity {

    private String currentUserId, targetUserId;
    private EditText etAmount, etNotes;
    private Spinner spinnerCategory;
    private FinanceViewModel viewModel;

    private static final String[] CATEGORIES = {"Select Source", "Salary", "Investment", "Gift", "Freelance", "Others"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record_dedicated);

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");
        targetUserId = getIntent().getStringExtra("TARGET_USER_ID");

        Toolbar toolbar = findViewById(R.id.toolbar_add_record);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        TextView tvTitle = findViewById(R.id.tv_add_record_title);
        tvTitle.setText("Provision New Income");
        tvTitle.setTextColor(0xFF03DAC5);

        etAmount = findViewById(R.id.et_add_record_amount);
        etNotes = findViewById(R.id.et_add_record_notes);
        spinnerCategory = findViewById(R.id.spinner_add_record_category);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CATEGORIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        findViewById(R.id.btn_add_record_submit).setOnClickListener(v -> submitRecord());
    }

    private void submitRecord() {
        String amtStr = etAmount.getText().toString();
        String cat = spinnerCategory.getSelectedItem().toString();
        String notes = etNotes.getText().toString().trim();

        if (amtStr.isEmpty() || cat.equals(CATEGORIES[0])) {
            Toast.makeText(this, "Amount & Category Required", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amtStr);
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());
        
        Record r = new Record(amount, "income", cat, date, notes.isEmpty() ? "Income Entry" : notes);
        r.setTargetUserId(targetUserId);

        viewModel.addRecord(currentUserId, r).observe(this, response -> {
            if (response != null && response.isSuccess()) {
                Toast.makeText(this, "Income stored successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: " + (response != null ? response.getMessage() : "Unknown"), Toast.LENGTH_LONG).show();
            }
        });
    }
}
