const express = require("express");
const router = express.Router();
const authController = require("../controllers/authController");

const { authenticate } = require("../middleware/authMiddleware");

/**
 * @route   POST /api/auth/login
 * @desc    Log in a user by email and password
 * @access  Public
 */
router.post("/login", authController.login);

/**
 * @route   PUT /api/auth/change-password
 * @desc    Force password change for authenticated users
 * @access  Private
 */
router.put("/change-password", authenticate, authController.changePassword);

module.exports = router;
