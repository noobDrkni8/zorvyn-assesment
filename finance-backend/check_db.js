const mongoose = require("mongoose");
require("dotenv").config();
const User = require("./src/models/User");

const MONGO_URI = process.env.MONGO_URI || "mongodb://127.0.0.1:27017/finance_db";

async function checkUsers() {
    try {
        await mongoose.connect(MONGO_URI);
        const users = await User.find({}, { password: 0 }); // Exclude passwords
        console.log('--- Current Database Identities ---');
        console.table(users.map(u => ({ id: u.id, name: u.name, email: u.email, role: u.role })));
        process.exit(0);
    } catch (err) {
        console.error(err);
        process.exit(1);
    }
}

checkUsers();
