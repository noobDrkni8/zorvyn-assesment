const express = require("express");
const router = express.Router();

const recordController = require("../controllers/recordController");
const { authenticate, authorize } = require("../middleware/authMiddleware");

/**
 * @route   POST /api/records
 * @desc    Admin and Analysts can create financial records
 * @access  Protected (Admin, Analyst)
 */
router.post(
    "/records",
    authenticate,
    authorize(["admin", "analyst"]),
    recordController.addRecord
);

/**
 * @route   GET /api/records/summary
 * @desc    All roles can view their dashboard summary
 * @access  Protected (Admin, Analyst, Viewer)
 */
router.get(
    "/records/summary",
    authenticate,
    authorize(["admin", "analyst", "viewer"]),
    recordController.getSummary
);

/**
 * @route   GET /api/records
 * @desc    Admin & Analysts can view detailed records (optionally for a target user)
 * @access  Protected (Admin, Analyst)
 */
router.get(
    "/records",
    authenticate,
    authorize(["admin", "analyst"]),
    recordController.getRecords
);

/**
 * @route   PUT /api/records/:id
 * @desc    Admin can update records
 * @access  Protected (Admin)
 */
router.put(
    "/records/:id",
    authenticate,
    authorize(["admin"]),
    recordController.updateRecord
);

/**
 * @route   DELETE /api/records/:id
 * @desc    Admin can delete records
 * @access  Protected (Admin)
 */
router.delete(
    "/records/:id",
    authenticate,
    authorize(["admin"]),
    recordController.deleteRecord
);

module.exports = router;