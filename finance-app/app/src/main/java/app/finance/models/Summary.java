package app.finance.models;

import java.util.List;

public class Summary {
    private double totalIncome;
    private double totalExpense;
    private double netBalance;
    private List<CategoryTotal> categoryWise;
    private List<Record> recentActivity;
    private List<MonthlyTrend> monthlyTrends;
    private List<WeeklyTrend> weeklyTrends;

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public double getNetBalance() {
        return netBalance;
    }

    public List<CategoryTotal> getCategoryWise() {
        return categoryWise;
    }

    public List<Record> getRecentActivity() {
        return recentActivity;
    }

    public List<MonthlyTrend> getMonthlyTrends() {
        return monthlyTrends;
    }

    public List<WeeklyTrend> getWeeklyTrends() {
        return weeklyTrends;
    }

    public static class CategoryTotal {
        private String category;
        private double total;

        public String getCategory() {
            return category;
        }

        public double getTotal() {
            return total;
        }
    }

    public static class MonthlyTrend {
        private String month;
        private String type;
        private double total;

        public String getMonth() { return month; }
        public String getType() { return type; }
        public double getTotal() { return total; }
    }

    public static class WeeklyTrend {
        private String week; // This will hold the year-month-day or YYYY-WW
        private String type;
        private double total;

        public String getWeek() { return week; }
        public String getType() { return type; }
        public double getTotal() { return total; }
    }
}

