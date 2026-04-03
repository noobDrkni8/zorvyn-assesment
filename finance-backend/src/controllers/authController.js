const User = require("../models/User");
const bcrypt = require("bcryptjs");

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
        const { email, password } = req.body;
 
        if (!email || !password) {
            return respond(res, 400, false, "Please provide both an email identity and a secure password.");
        }
 
        const trimmedEmail = email.trim().toLowerCase();
        // Specifically include the hidden password field for verification
        const user = await User.findOne({ email: new RegExp(`^${trimmedEmail}$`, 'i') }).select("+password");
 
        if (!user) {
            return respond(res, 404, false, "Identity not found. Access Terminated.");
        }
 
        const isMatch = bcrypt.compareSync(password, user.password);
        if (!isMatch) {
            return respond(res, 401, false, "Authentication failed: Invalid credentials.");
        }
 
        if (user.status !== "active") {
            return respond(res, 403, false, "Identity suspended. Contact core administrator.");
        }
 
        // Return user profile on success (password is excluded automatically by Mongoose)
        const userResponse = user.toObject();
        delete userResponse.password;
        
        respond(res, 200, true, "Login successful!", userResponse);
    } catch (err) {
        respond(res, 500, false, "Server Authentication Error: " + err.message);
    }
};

/**
 * Change Password Controller
 * Updates the user's password and clears the mustChangePassword flag.
 */
exports.changePassword = async (req, res) => {
    try {
        const { newPassword } = req.body;
        const userId = req.user.id;

        if (!newPassword || newPassword.length < 6) {
            return respond(res, 400, false, "Password must be at least 6 characters long.");
        }

        const hashedPassword = bcrypt.hashSync(newPassword, 10);
        const user = await User.findOneAndUpdate(
            { id: userId },
            { 
                $set: { 
                    password: hashedPassword, 
                    mustChangePassword: 'false' 
                } 
            },
            { new: true }
        );

        if (!user) {
            return respond(res, 404, false, "Identity record not found.");
        }

        respond(res, 200, true, "Security credentials updated successfully.");
    } catch (err) {
        respond(res, 500, false, "Security Update Error: " + err.message);
    }
};