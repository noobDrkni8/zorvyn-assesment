package app.finance;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import app.finance.models.Record;
import app.finance.models.User;
import app.finance.viewmodel.FinanceViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddRecordActivity extends AppCompatActivity {

    private EditText etAmount, etCategory, etNotes;
    private AutoCompleteTextView actvUserSearch;
    private TextView tvSelectedUserInfo;
    private FinanceViewModel viewModel;
    private String currentUserId;
    private String targetUserId;
    private List<User> allUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
        currentUserId = getIntent().getStringExtra("USER_ID");
        targetUserId = currentUserId; // Default to self

        Toolbar toolbar = findViewById(R.id.toolbar_add_record);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etAmount = findViewById(R.id.et_amount);
        etCategory = findViewById(R.id.et_category);
        etNotes = findViewById(R.id.et_notes);
        actvUserSearch = findViewById(R.id.actv_user_search);
        tvSelectedUserInfo = findViewById(R.id.tv_selected_user_info);

        setupUserSearch();

        findViewById(R.id.btn_save_income).setOnClickListener(v -> submitRecord("income"));
        findViewById(R.id.btn_save_expense).setOnClickListener(v -> submitRecord("expense"));
    }

    private void setupUserSearch() {
        viewModel.getUsers(currentUserId).observe(this, response -> {
            if (response != null && response.getData() != null) {
                allUsers = response.getData();
                List<String> userDisplayNames = new ArrayList<>();
                for (User u : allUsers) {
                    userDisplayNames.add(u.getName() + " (ID: " + u.getId() + ")");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, userDisplayNames);
                actvUserSearch.setAdapter(adapter);
            }
        });

        actvUserSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selectedText = (String) parent.getItemAtPosition(position);
            for (User u : allUsers) {
                String displayName = u.getName() + " (ID: " + u.getId() + ")";
                if (displayName.equals(selectedText)) {
                    targetUserId = String.valueOf(u.getId());
                    tvSelectedUserInfo.setText("Selected Client: " + u.getName() + " [ID: " + u.getId() + "]");
                    tvSelectedUserInfo.setTextColor(0xFF03DAC5);
                    break;
                }
            }
        });

        actvUserSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    targetUserId = currentUserId;
                    tvSelectedUserInfo.setText("No client selected (Defaulting to self)");
                    tvSelectedUserInfo.setTextColor(0x88FFFFFF);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void submitRecord(String type) {
        String amt = etAmount.getText().toString().trim();
        String cat = etCategory.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (amt.isEmpty() || cat.isEmpty()) {
            Toast.makeText(this, "Amount and Category are required", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amt);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());
        Record r = new Record(amount, type, cat, date, notes.isEmpty() ? "Standard Entry" : notes);

        viewModel.addRecord(targetUserId, r).observe(this, response -> {
            if (response != null && response.isSuccess()) {
                Toast.makeText(this, "Record saved successfully for " + 
                    (targetUserId.equals(currentUserId) ? "self" : "client"), Toast.LENGTH_SHORT).show();
                etAmount.setText("");
                etCategory.setText("");
                etNotes.setText("");
                setResult(RESULT_OK);
            } else {
                Toast.makeText(this, "Failed to save record", Toast.LENGTH_SHORT).show();
            }
        });
    }
}