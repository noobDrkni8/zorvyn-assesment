const mongoose = require("mongoose");
const Record = require("../models/Record");
require("dotenv").config();
 
const repairData = async () => {
    try {
        await mongoose.connect(process.env.MONGO_URI || "mongodb://localhost:27017/finance_db");
        console.log("Connected to DB for Deep Repair...");
 
        // Case-insensitive list of categories that are DEFINITELY expenses
        const expenseCategories = [
            "rent", "food", "transport", "shopping", "healthcare", 
            "entertainment", "utilities", "lunch", "dinner", "apartment"
        ];
        
        // Create regex pattern like /rent|food|transport/i
        const pattern = new RegExp(expenseCategories.join("|"), "i");
 
        const result = await Record.updateMany(
            { 
                category: { $regex: pattern },
                type: "income"
            },
            { $set: { type: "expense" } }
        );
 
        console.log(`Deep Repair Complete: ${result.modifiedCount} records corrected to 'expense'.`);
        process.exit(0);
    } catch (err) {
        console.error("Deep Repair Failed:", err.message);
        process.exit(1);
    }
};
 
repairData();
