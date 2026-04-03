package app.finance.models;

public class Record {
    private int id;
    private double amount;
    private String type;
    private String category;
    private String date;
    private String notes;
    private int createdBy;
    private String targetUserId; // To attribute record to another user during inspection

    public Record(double amount, String type, String category, String date, String notes) {
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.notes = notes;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }

    public String getNotes() {
        return notes;
    }

    public int getCreatedBy() {
        return createdBy;
    }
}
