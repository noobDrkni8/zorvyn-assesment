const mongoose = require("mongoose");
require("dotenv").config();
const User = require("./src/models/User");
const Record = require("./src/models/Record");
const bcrypt = require("bcryptjs");

const MONGO_URI = process.env.MONGO_URI || "mongodb://127.0.0.1:27017/finance_db";

const seedData = async () => {
    try {
        await mongoose.connect(MONGO_URI);
        console.log("MongoDB Connected for seeding...");

        // Clear existing data
        await User.deleteMany({});
        await Record.deleteMany({});
        console.log("Existing data cleared.");

        // Create initial users with secure passwords
        const users = [
            {
                id: 1,
                name: "Admin User",
                email: "admin@zorvyn.com",
                role: "Admin",
                status: "active",
                password: bcrypt.hashSync("nehal@admin", 10),
                mustChangePassword: 'false'
            },
            {
                id: 2,
                name: "Analyst User",
                email: "analyst@zorvyn.com",
                role: "Analyst",
                status: "active",
                password: bcrypt.hashSync("password123", 10),
                mustChangePassword: 'true'
            },
            {
                id: 3,
                name: "Viewer User",
                email: "viewer@zorvyn.com",
                role: "Viewer",
                status: "active",
                password: bcrypt.hashSync("password123", 10),
                mustChangePassword: 'true'
            }
        ];

        await User.insertMany(users);
        console.log("Initial users seeded successfully.");

        // Create some sample records for the Viewer User (id: 3)
        const records = [
            {
                id: 1,
                amount: 5000,
                type: "income",
                category: "Salary",
                date: "2024-05-01",
                notes: "Monthly Salary",
                createdBy: 3
            },
            {
                id: 2,
                amount: 150,
                type: "expense",
                category: "Food",
                date: "2024-05-02",
                notes: "Lunch at Cafe",
                createdBy: 3
            },
            {
                id: 3,
                amount: 1200,
                type: "expense",
                category: "Rent",
                date: "2024-05-05",
                notes: "Monthly Apartment Rent",
                createdBy: 3
            }
        ];

        await Record.insertMany(records);
        console.log("Sample records seeded successfully.");

        console.log("Seeding complete!");
        process.exit();
    } catch (error) {
        console.error("Seeding failed:", error.message);
        process.exit(1);
    }
};

seedData();