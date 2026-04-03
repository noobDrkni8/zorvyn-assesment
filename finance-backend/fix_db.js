const sqlite3 = require('sqlite3').verbose();
const db = new sqlite3.Database('d:/Projects/backend/finance-backend/finance.db');

db.serialize(() => {
    // Delete existing records to ensure a clean state
    db.run("DELETE FROM users");
    
    // Insert correct credentials
    const stmt = db.prepare("INSERT INTO users (name, email, role, status) VALUES (?, ?, ?, 'active')");
    stmt.run("Admin User", "admin@zorvyn.com", "Admin");
    stmt.run("Analyst User", "analyst@zorvyn.com", "Analyst");
    stmt.run("Viewer User", "viewer@zorvyn.com", "Viewer");
    stmt.finalize();

    console.log("Database credentials reset to @zorvyn.com");
});

db.close();
