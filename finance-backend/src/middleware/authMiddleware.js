const db = require("../db/db");

/**
 * Simple authentication middleware.
 * Expects X-User-Id header to identify the user.
 */
exports.authenticate = (req, res, next) => {
    const userId = req.headers["x-user-id"];

    if (!userId) {
        return res.status(401).json({ 
            success: false, 
            message: "Authentication failed: Missing X-User-Id header." 
        });
    }

    db.get("SELECT * FROM users WHERE id = ?", [userId], (err, user) => {
        if (err || !user) {
            return res.status(401).json({ 
                success: false, 
                message: "Authentication failed: User not found." 
            });
        }

        if (user.status !== "active") {
            return res.status(403).json({ 
                success: false, 
                message: "Access denied: Account is inactive." 
            });
        }

        // Attach user to request object
        req.user = user;
        next();
    });
};

/**
 * Role-based authorization middleware.
 */
exports.authorize = (roles = []) => {
    return (req, res, next) => {
        if (!req.user) {
            return res.status(500).json({ 
                success: false, 
                message: "Authorization error: authenticate middleware must be called first." 
            });
        }

        const userRole = req.user.role.toLowerCase().trim();
        const requiredRoles = roles.map(r => r.toLowerCase().trim());

        if (!requiredRoles.includes(userRole)) {
            return res.status(403).json({ 
                success: false, 
                message: `Access denied: Required roles [${roles.join(", ")}]. Your role: ${req.user.role}` 
            });
        }

        next();

    };
};