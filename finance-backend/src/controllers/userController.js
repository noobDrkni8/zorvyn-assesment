const User = require("../models/User");
const bcrypt = require("bcryptjs");

// Success/Error JSON Format Helper
const respond = (res, status, success, message, data = null) => {
    return res.status(status).json({ success, message, data });
};

// Create User (Admin Only)
exports.createUser = async (req, res) => {
    try {
        const { name, email, role, status, password } = req.body;

        // Strict Role Check: Only Admin can create new identities
        if (req.user.role.toLowerCase() !== 'admin') {
            return respond(res, 403, false, "Access Denied: Only Admin can provision new identities.");
        }

        if (!name || !email || !role || !password) {
            return respond(res, 400, false, "Missing required fields: name, email, role, and temporary password.");
        }

        const lastUser = await User.findOne().sort("-id");
        const nextId = lastUser ? lastUser.id + 1 : 1;

        const formattedRole = role ? role.charAt(0).toUpperCase() + role.slice(1).toLowerCase() : "Viewer";
        
        // Hash the temporary password
        const hashedPassword = bcrypt.hashSync(password, 10);

        const user = new User({
            id: nextId,
            name,
            email,
            role: formattedRole,
            status: status || "active",
            password: hashedPassword,
            mustChangePassword: 'true'
        });

        await user.save();
        
        // Return user without password
        const userResponse = user.toObject();
        delete userResponse.password;
        
        respond(res, 201, true, "User identity provisioned successfully.", userResponse);
    } catch (err) {
        if (err.code === 11000) {
            return respond(res, 400, false, "Identity Conflict: Email already registered.");
        }
        respond(res, 500, false, "Provisioning Error: " + err.message);
    }
};

// Get All Users
exports.getUsers = async (req, res) => {
    try {
        const users = await User.find().sort("id");
        respond(res, 200, true, "Users fetched successfully.", users);
    } catch (err) {
        respond(res, 500, false, "Server Error: " + err.message);
    }
};

// Update User
exports.updateUser = async (req, res) => {
    try {
        const { id } = req.params;
        const { name, email, role, status } = req.body;

        const formattedRole = role ? role.charAt(0).toUpperCase() + role.slice(1).toLowerCase() : "Viewer";

        const updatedUser = await User.findOneAndUpdate(
            { id: parseInt(id) },
            { $set: { name, email, role: formattedRole, status } },
            { new: true, runValidators: true }
        );

        if (!updatedUser) {
            return respond(res, 404, false, "User not found.");
        }

        respond(res, 200, true, "User updated successfully.", updatedUser);
    } catch (err) {
        if (err.code === 11000) {
            return respond(res, 400, false, "User with this email already exists.");
        }
        respond(res, 500, false, "Server Error: " + err.message);
    }
};

// Delete User
exports.deleteUser = async (req, res) => {
    try {
        const { id } = req.params;
        const result = await User.findOneAndDelete({ id: parseInt(id) });

        if (!result) {
            return respond(res, 404, false, "User not found.");
        }

        respond(res, 200, true, "User deleted successfully.");
    } catch (err) {
        respond(res, 500, false, "Server Error: " + err.message);
    }
};

// Search User by Name or Email (Robust Version)
exports.searchUser = async (req, res) => {
    try {
        const { name, email } = req.query;
        const searchTerm = (name || email || "").trim();

        if (!searchTerm) {
            return respond(res, 400, false, "Please provide a name or email identity to search.");
        }


        // Use a case-insensitive fuzzy search that matches start or middle of strings
        const user = await User.findOne({
            $or: [
                { email: { $regex: searchTerm, $options: 'i' } },
                { name: { $regex: searchTerm, $options: 'i' } }
            ]
        });

        if (!user) {
            console.warn(`Search failed for term: ${searchTerm}`);
            return respond(res, 404, false, "Identity not found in system logs.");
        }

        respond(res, 200, true, "Identity identified.", user);
    } catch (err) {
        console.error("Search Error:", err.message);
        respond(res, 500, false, "Server Audit Error: " + err.message);
    }
};