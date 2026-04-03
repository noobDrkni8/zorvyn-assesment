const mongoose = require("mongoose");
require("dotenv").config();
const User = require("./src/models/User");

const MONGO_URI = process.env.MONGO_URI || "mongodb://127.0.0.1:27017/finance_db";

async function checkUsers() {
    try {
        await mongoose.connect(MONGO_URI);
        const users = await User.find({}, { password: 0 });
        console.log(JSON.stringify(users, null, 2));
        process.exit(0);
    } catch (err) {
        process.exit(1);
    }
}

checkUsers();
