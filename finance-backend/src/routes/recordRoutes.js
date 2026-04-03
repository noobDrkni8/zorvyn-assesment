const express = require("express");
const router = express.Router();

const recordController = require("../controllers/recordController");
const { authenticate, authorize } = require("../middleware/authMiddleware");

/**
 * @route   POST /api/records
 * @desc    Admin and Analysts can create financial records
 */
router.post(
    "/records",
    authenticate,
    authorize(["admin", "analyst"]),
    recordController.addRecord
);

/**
 * @route   GET /api/records/summary
 */
router.get(
    "/records/summary",
    authenticate,
    authorize(["admin", "analyst", "viewer"]),
    recordController.getSummary
);

/**
 * @route   GET /api/records
 * @desc    All roles can view records (Admin/Analyst can see target user data)
 */
router.get(
    "/records",
    authenticate,
    authorize(["admin", "analyst", "viewer"]),
    recordController.getRecords
);

/**
 * @route   PUT /api/records/:id
 * @desc    Admin and Analysts can update records
 */
router.put(
    "/records/:id",
    authenticate,
    authorize(["admin", "analyst"]),
    recordController.updateRecord
);

/**
 * @route   DELETE /api/records/:id
 * @desc    Admin and Analysts can delete records
 */
router.delete(
    "/records/:id",
    authenticate,
    authorize(["admin", "analyst"]),
    recordController.deleteRecord
);

module.exports = router;