const express = require("express");
const router = express.Router();
const userController = require("../controllers/userController");
const { authenticate, authorize } = require("../middleware/authMiddleware");

// User Search (Admin & Analyst)
router.get("/search", authenticate, authorize(["admin", "analyst"]), userController.searchUser);

// User management routes
router.get("/users", authenticate, authorize(["admin", "analyst"]), userController.getUsers);
router.post("/users", authenticate, authorize(["admin"]), userController.createUser);
router.put("/users/:id", authenticate, authorize(["admin"]), userController.updateUser);
router.delete("/users/:id", authenticate, authorize(["admin"]), userController.deleteUser);


module.exports = router;