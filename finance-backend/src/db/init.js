const db = require("./db");

db.serialize(() => {
    // Users table
    db.run(`
    CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      email TEXT UNIQUE NOT NULL,
      role TEXT CHECK(role IN ('Admin', 'Analyst', 'Viewer')) DEFAULT 'Viewer',
      status TEXT DEFAULT 'active'
    )
    `);

    // Records table
    db.run(`
    CREATE TABLE IF NOT EXISTS records (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      amount REAL NOT NULL,
      type TEXT CHECK(type IN ('income', 'expense')) NOT NULL,
      category TEXT NOT NULL,
      date TEXT NOT NULL,
      notes TEXT,
      createdBy INTEGER,
      isDeleted INTEGER DEFAULT 0,
      FOREIGN KEY (createdBy) REFERENCES users(id)
    )
    `);

    db.run("CREATE INDEX IF NOT EXISTS idx_records_type ON records(type)");
    db.run("CREATE INDEX IF NOT EXISTS idx_records_deleted ON records(isDeleted)");
    db.run("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)");

    // Seed Data (only if users table is empty)
    db.get("SELECT count(*) as count FROM users", (err, row) => {
        if (err) return;
        if (row && row.count === 0) {
            db.run(`INSERT INTO users (name, email, role) VALUES ('Admin User', 'admin@zorvyn.com', 'Admin')`);
            db.run(`INSERT INTO users (name, email, role) VALUES ('Analyst User', 'analyst@zorvyn.com', 'Analyst')`);
            db.run(`INSERT INTO users (name, email, role) VALUES ('Viewer User', 'viewer@zorvyn.com', 'Viewer')`);
            console.log("Seed data inserted");
        }
    });

    console.log("Database initialized");
});