const mongoose = require("mongoose");

const recordSchema = new mongoose.Schema({
    id: { type: Number, unique: true }, // Keep numeric for Android compatibility
    amount: { type: Number, required: true },
    type: {
        type: String,
        enum: ["income", "expense"],
        required: true
    },
    category: { type: String, required: true },
    date: { type: String, required: true },
    notes: { type: String },
    createdBy: { type: Number, required: true }, // Refers to user.id
    isDeleted: { type: Boolean, default: false }
}, {
    timestamps: true
});

module.exports = mongoose.model("Record", recordSchema);