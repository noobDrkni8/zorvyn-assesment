const User = require("../models/User");

// Success/Error JSON Format Helper
const respond = (res, status, success, message, data = null) => {
    return res.status(status).json({ success, message, data });
};

/**
 * Login Controller (MongoDB)
 * Searches for a user by email, returns their ID and Role if active.
 */
exports.login = async (req, res) => {
    try {
        const { email } = req.body;

        if (!email || typeof email !== 'string') {
            return respond(res, 400, false, "Please provide a valid email address.");
        }

        const trimmedEmail = email.trim().toLowerCase();
        const user = await User.findOne({ email: new RegExp(`^${trimmedEmail}$`, 'i') });

        if (!user) {
            return respond(res, 404, false, "No account found with this email. Please check your spelling.");
        }

        if (user.status !== "active") {
            return respond(res, 403, false, "Your account is currently inactive. Please contact your administrator.");
        }

        // Return user profile on success
        respond(res, 200, true, "Login successful!", user);
    } catch (err) {
        console.error("Auth Error:", err.message);
        respond(res, 500, false, "Server Error: " + err.message);
    }
};