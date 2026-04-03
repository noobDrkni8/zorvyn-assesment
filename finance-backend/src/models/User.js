const mongoose = require("mongoose");

const userSchema = new mongoose.Schema({
    id: { type: Number, unique: true }, // Keep numeric for Android compatibility
    name: { type: String, required: true },
    email: { type: String, required: true, unique: true },
    role: {
        type: String,
        enum: ["Admin", "Analyst", "Viewer"],
        default: "Viewer"
    },
    status: { type: String, default: "active" },
    password: { type: String, required: true, select: false },
    mustChangePassword: { type: String, default: 'true' } // String 'true'/'false' for easier Android parsing if needed, or Boolean. I'll use Boolean.

}, {
    timestamps: true,
    toJSON: { virtuals: true },
    toObject: { virtuals: true }
});

module.exports = mongoose.model("User", userSchema);