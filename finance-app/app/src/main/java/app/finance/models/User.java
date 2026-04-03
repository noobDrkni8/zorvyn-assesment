package app.finance.models;

public class User {
    private int id;
    private String name;
    private String email;
    private String role;
    private String status;

    public User(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = "active";
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
