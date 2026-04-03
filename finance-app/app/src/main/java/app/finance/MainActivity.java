package app.finance;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import app.finance.models.Summary;
import app.finance.models.User;
import app.finance.models.Record;
import app.finance.viewmodel.FinanceViewModel;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvNetBalance, tvTotalIncome, tvTotalExpense, tvDashboardHeading;
    private ImageButton btnLogout;
    
    // Dashboard Sections
    private View containerBalance, containerInsights, containerHistory, layoutViewerStats;
    private LinearLayout layoutCategoryList, layoutHistoryList;

    // Admin Panel Hooks
    private View cardGlobalLiquidity;
    private TextView tvGlobalBalance;
    private LinearLayout panelManageUsers;

    // Dual Progress Bars
    private View cardNavAnalysts, cardNavClients;
    private ProgressBar pbAnalystsTotal, pbAnalystsActive;
    private ProgressBar pbClientsTotal, pbClientsActive;
    private TextView tvAnalystsTotal, tvAnalystsActive;
    private TextView tvClientsTotal, tvClientsActive;

    private String currentUserId;
    private String currentUserRole;
    private FinanceViewModel viewModel;
    private SessionManager sessionManager;

    // Auto-Sync Logic
    private Handler syncHandler = new Handler();
    private static final int SYNC_INTERVAL = 30000; // 30 seconds

    private Runnable syncRunnable = new Runnable() {
        @Override
        public void run() {
            fetchDashboardSummary();
            if (currentUserRole != null && (currentUserRole.toLowerCase().equals("admin") || currentUserRole.toLowerCase().equals("analyst"))) {
                fetchUserLists();
            }
            syncHandler.postDelayed(this, SYNC_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
        sessionManager = new SessionManager(this);

        // Core Hooks
        tvNetBalance = findViewById(R.id.tv_net_balance);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        tvDashboardHeading = findViewById(R.id.tv_dashboard_heading);
        btnLogout = findViewById(R.id.btn_logout);
        
        containerBalance = findViewById(R.id.container_balance);
        layoutViewerStats = findViewById(R.id.layout_viewer_stats);
        containerInsights = findViewById(R.id.container_insights);
        layoutCategoryList = findViewById(R.id.layout_category_list);
        containerHistory = findViewById(R.id.container_history);
        layoutHistoryList = findViewById(R.id.layout_history_list);

        // Admin Hooks
        cardGlobalLiquidity = findViewById(R.id.card_global_liquidity);
        tvGlobalBalance = findViewById(R.id.tv_global_balance);
        panelManageUsers = findViewById(R.id.panel_manage_users);

        // Progress Bar Hooks
        cardNavAnalysts = findViewById(R.id.card_nav_analysts);
        pbAnalystsTotal = findViewById(R.id.pb_analysts_total);
        pbAnalystsActive = findViewById(R.id.pb_analysts_active);
        tvAnalystsTotal = findViewById(R.id.tv_total_analysts_count);
        tvAnalystsActive = findViewById(R.id.tv_count_analysts_active);

        cardNavClients = findViewById(R.id.card_nav_clients);
        pbClientsTotal = findViewById(R.id.pb_clients_total);
        pbClientsActive = findViewById(R.id.pb_clients_active);
        tvClientsTotal = findViewById(R.id.tv_total_clients_count);
        tvClientsActive = findViewById(R.id.tv_count_clients_active);

        btnLogout.setOnClickListener(v -> performLogout());

        // Dashboard Status Card Navigation
        cardNavAnalysts.setOnClickListener(v -> {
            Intent intent = new Intent(this, AnalystAdminActivity.class);
            intent.putExtra("CURRENT_USER_ID", currentUserId);
            startActivity(intent);
        });

        cardNavClients.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClientAdminActivity.class);
            intent.putExtra("CURRENT_USER_ID", currentUserId);
            startActivity(intent);
        });

        handleSession();
    }

    private void performLogout() {
        sessionManager.logoutUser();
        Toast.makeText(this, "Session Terminated", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        String role = (currentUserRole != null) ? currentUserRole.toLowerCase() : "viewer";
        
        // Professional Logic: Context-aware Tabs
        if (role.equals("admin")) {
            bottomNav.getMenu().findItem(R.id.nav_action).setTitle("Insights");
            bottomNav.getMenu().findItem(R.id.nav_action).setVisible(true);
            bottomNav.getMenu().findItem(R.id.nav_manage).setVisible(true);
        } else if (role.equals("analyst")) {
            // Analyst now uses Insights portal to find clients
            bottomNav.getMenu().findItem(R.id.nav_action).setTitle("Find Client");
            bottomNav.getMenu().findItem(R.id.nav_action).setVisible(true);
            // Hide Manage tab for Analyst as requested
            bottomNav.getMenu().findItem(R.id.nav_manage).setVisible(false);
        } else {
            // VIEWER: Only Dashboard and History
            bottomNav.getMenu().findItem(R.id.nav_action).setVisible(false);
            bottomNav.getMenu().findItem(R.id.nav_manage).setVisible(false);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                return true;
            } else if (id == R.id.nav_action) {
                if (role.equals("viewer")) {
                    Toast.makeText(this, "Access Denied", Toast.LENGTH_SHORT).show();
                    return false;
                }
                // Both Admin and Analyst now go to Insights/Search portal first
                Intent intent = new Intent(this, InsightsActivity.class);
                intent.putExtra("USER_ID", currentUserId);
                intent.putExtra("USER_ROLE", currentUserRole);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class)
                        .putExtra("USER_ID", currentUserId));
                return true;
            } else if (id == R.id.nav_manage) {
                if (role.equals("admin")) {
                    View anchor = findViewById(R.id.bottom_navigation).findViewById(R.id.nav_manage);
                    androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, anchor);
                    popup.getMenu().add("Manage Analysts");
                    popup.getMenu().add("Manage Clients");
                    
                    popup.setOnMenuItemClickListener(menuItem -> {
                        String title = menuItem.getTitle().toString();
                        if ("Manage Analysts".equals(title)) {
                            startActivity(new Intent(this, AnalystAdminActivity.class)
                                    .putExtra("CURRENT_USER_ID", currentUserId));
                        } else if ("Manage Clients".equals(title)) {
                            startActivity(new Intent(this, ClientAdminActivity.class)
                                    .putExtra("CURRENT_USER_ID", currentUserId));
                        }
                        return true;
                    });
                    popup.show();
                }
                return true;
            }
            return false;
        });
    }

    private void handleSession() {
        Intent intent = getIntent();
        currentUserId = intent.getStringExtra("USER_ID");
        currentUserRole = intent.getStringExtra("USER_ROLE");

        if (currentUserId == null || currentUserRole == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        sessionManager.createLoginSession(currentUserId, currentUserRole);

        setupNavigation();
        updateDashboardRoleUI();
        startAutoSync();
    }

    private void updateDashboardRoleUI() {
        String roleLower = (currentUserRole != null) ? currentUserRole.toLowerCase() : "viewer";

        if (roleLower.equals("admin")) {
            tvDashboardHeading.setText("Command Center");
            panelManageUsers.setVisibility(View.VISIBLE);
            cardGlobalLiquidity.setVisibility(View.VISIBLE);
            cardNavAnalysts.setVisibility(View.VISIBLE);
            containerBalance.setVisibility(View.GONE);
            layoutViewerStats.setVisibility(View.GONE);
            containerHistory.setVisibility(View.GONE);
            fetchUserLists();
        } else if (roleLower.equals("analyst")) {
            tvDashboardHeading.setText("Financial Intelligence");
            panelManageUsers.setVisibility(View.VISIBLE);
            cardGlobalLiquidity.setVisibility(View.GONE);
            cardNavAnalysts.setVisibility(View.GONE); // Only see Client status
            
            // Analyst Dashboard: No Income/Expense for themselves
            containerBalance.setVisibility(View.GONE);
            layoutViewerStats.setVisibility(View.GONE);
            containerHistory.setVisibility(View.GONE);
            
            fetchUserLists();
        } else {
            // VIEWER ROLE
            tvDashboardHeading.setText("My Portfolio");
            panelManageUsers.setVisibility(View.GONE);
            cardGlobalLiquidity.setVisibility(View.GONE);
            containerBalance.setVisibility(View.VISIBLE);
            layoutViewerStats.setVisibility(View.VISIBLE);
            containerHistory.setVisibility(View.VISIBLE);
        }

        fetchDashboardSummary();
    }

    private void fetchUserLists() {
        viewModel.getUsers(currentUserId).observe(this, response -> {
            if (response != null && response.getData() != null) {
                int totalAnalyst = 0, activeAnalyst = 0;
                int totalViewer = 0, activeViewer = 0;

                for (User u : response.getData()) {
                    if (String.valueOf(u.getId()).equals(currentUserId)) continue;
                    boolean isAnalyst = "analyst".equals(u.getRole().toLowerCase());
                    boolean isActive = "active".equals(u.getStatus());
                    if (isAnalyst) {
                        totalAnalyst++;
                        if (isActive) activeAnalyst++;
                    } else {
                        totalViewer++;
                        if (isActive) activeViewer++;
                    }
                }

                tvAnalystsTotal.setText("Total: " + totalAnalyst);
                tvAnalystsActive.setText("Active: " + activeAnalyst);
                pbAnalystsTotal.setProgress(100); 
                if (totalAnalyst > 0) pbAnalystsActive.setProgress((activeAnalyst * 100) / totalAnalyst);

                tvClientsTotal.setText("Total: " + totalViewer);
                tvClientsActive.setText("Active: " + activeViewer);
                pbClientsTotal.setProgress(100);
                if (totalViewer > 0) pbClientsActive.setProgress((activeViewer * 100) / totalViewer);
            }
        });
    }

    private void fetchDashboardSummary() {
        String targetId = (currentUserRole != null && currentUserRole.toLowerCase().equals("admin")) ? "all" : null;
        viewModel.getSummary(currentUserId, targetId).observe(this, response -> {
            if (response != null && response.getData() != null) {
                Summary s = response.getData();
                tvNetBalance.setText(String.format(Locale.getDefault(), "$%.2f", s.getNetBalance()));
                tvTotalIncome.setText(String.format(Locale.getDefault(), "$%.2f", s.getTotalIncome()));
                tvTotalExpense.setText(String.format(Locale.getDefault(), "$%.2f", s.getTotalExpense()));
                tvGlobalBalance.setText(String.format(Locale.getDefault(), "$%.2f", s.getNetBalance()));

                if (currentUserRole.toLowerCase().equals("viewer")) {
                    populateHistoryPreview(s.getRecentActivity());
                }

                layoutCategoryList.removeAllViews();
                for (Summary.CategoryTotal item : s.getCategoryWise()) {
                    renderInsightItem(layoutCategoryList, item.getCategory(), item.getTotal(), 0xFFBB86FC);
                }
            }
        });
    }

    private void populateHistoryPreview(java.util.List<Record> records) {
        layoutHistoryList.removeAllViews();
        if (records == null || records.isEmpty()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Record r : records) {
            View itemView = inflater.inflate(R.layout.item_record, layoutHistoryList, false);
            
            View indicator = itemView.findViewById(R.id.view_type_indicator);
            TextView tvCategory = itemView.findViewById(R.id.tv_record_category);
            TextView tvDate = itemView.findViewById(R.id.tv_record_date);
            TextView tvAmount = itemView.findViewById(R.id.tv_record_amount);

            tvCategory.setText(r.getCategory());
            tvDate.setText(r.getDate());
            
            boolean isIncome = "income".equalsIgnoreCase(r.getType());
            tvAmount.setText(String.format(Locale.getDefault(), "%s$%.2f", isIncome ? "+" : "-", r.getAmount()));
            tvAmount.setTextColor(isIncome ? 0xFF03DAC5 : 0xFFCF6679);
            indicator.setBackgroundColor(isIncome ? 0xFF03DAC5 : 0xFFCF6679);

            layoutHistoryList.addView(itemView);
        }
    }

    private void renderInsightItem(LinearLayout container, String label, double amount, int color) {
        TextView tv = new TextView(this);
        tv.setText(String.format(Locale.getDefault(), "• %s: $%.2f", label, amount));
        tv.setTextColor(color);
        tv.setPadding(0, 8, 0, 8);
        container.addView(tv);
    }

    private void startAutoSync() {
        syncHandler.removeCallbacks(syncRunnable);
        syncHandler.postDelayed(syncRunnable, SYNC_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        syncHandler.removeCallbacks(syncRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != null) {
            startAutoSync();
            fetchDashboardSummary();
        }
    }
}