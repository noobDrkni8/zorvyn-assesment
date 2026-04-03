const express = require("express");
const cors = require("cors");
const rateLimit = require("express-rate-limit");

const app = express();

// Security Middleware: Rate Limiting
const limiter = rateLimit({
    windowMs: 15 * 60 * 1000,
    max: 100,
    message: { success: false, message: "Too many requests from this IP, please try again later." }
});

// Middleware
app.use(cors());
app.use(express.json());

app.use((req, res, next) => {
    const userId = req.headers["x-user-id"];
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.originalUrl} | X-User-Id: ${userId || 'MISSING'}`);
    
    res.on('finish', () => {
        console.log(`[${new Date().toISOString()}] Response: ${res.statusCode}`);
    });
    next();
});

// Routes
const authRoutes = require("./routes/authRoutes");
app.use("/api/auth", authRoutes);

const userRoutes = require("./routes/userRoutes");
app.use("/api", userRoutes);

const recordRoutes = require("./routes/recordRoutes");
app.use("/api", recordRoutes);

// Health Check
app.get("/", (req, res) => {
    res.status(200).json({ status: "Online", platform: "Zorvyn FinTech" });
});

module.exports = app;