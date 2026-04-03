const app = require("./app");
const connectDB = require("./db/db");

const PORT = process.env.PORT || 3000;

// Connect to MongoDB before starting server
connectDB().then(() => {
    app.listen(PORT, "0.0.0.0", () => {
        console.log(`Server running on port ${PORT}`);
    });
});