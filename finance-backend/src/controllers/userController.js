const User = require("../models/User");

// Success/Error JSON Format Helper
const respond = (res, status, success, message, data = null) => {
    return res.status(status).json({ success, message, data });
};

// Create User
exports.createUser = async (req, res) => {
    try {
        const { name, email, role, status } = req.body;

        if (!name || !email || !role) {
            return respond(res, 400, false, "Missing required fields: name, email, role.");
        }

        const lastUser = await User.findOne().sort("-id");
        const nextId = lastUser ? lastUser.id + 1 : 1;

        const user = new User({
            id: nextId,
            name,
            email,
            role,
            status: status || "active"
        });

        await user.save();
        respond(res, 201, true, "User created successfully.", user);
    } catch (err) {
        if (err.code === 11000) {
            return respond(res, 400, false, "User with this email already exists.");
        }
        respond(res, 500, false, "Server Error: " + err.message);
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

        const updatedUser = await User.findOneAndUpdate(
            { id: parseInt(id) },
            { $set: { name, email, role, status } },
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
        let { name } = req.query;

        if (!name) {
            return respond(res, 400, false, "Please provide a name or email identity to search.");
        }

        const searchTerm = name.trim();

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