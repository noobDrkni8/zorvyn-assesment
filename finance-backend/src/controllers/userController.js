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

        // Handle custom auto-increment id for Android
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

// Search User by Name or Email
exports.searchUser = async (req, res) => {
    try {
        const { name } = req.query;

        if (!name) {
            return respond(res, 400, false, "Please provide a name or email to search for.");
        }

        const searchTerm = name.trim();
        const user = await User.findOne({
            $or: [
                { name: new RegExp(searchTerm, 'i') },
                { email: new RegExp(searchTerm, 'i') }
            ]
        });

        if (!user) {
            return respond(res, 404, false, "No user found with that name or email.");
        }

        respond(res, 200, true, "User found successfully.", user);
    } catch (err) {
        respond(res, 500, false, "Server Error: " + err.message);
    }
};