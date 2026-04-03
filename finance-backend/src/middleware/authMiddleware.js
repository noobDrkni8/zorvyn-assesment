const User = require("../models/User");

/**
 * Simple authentication middleware (MongoDB Version)
 * Expects X-User-Id header to identify the user.
 */
exports.authenticate = async (req, res, next) => {
    const userId = req.headers["x-user-id"];

    if (!userId) {
        return res.status(401).json({ 
            success: false, 
            message: "Authentication failed: Missing X-User-Id header." 
        });
    }

    try {
        const user = await User.findOne({ id: parseInt(userId) });

        if (!user) {
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
    } catch (err) {
        console.error("Auth Middleware Error:", err.message);
        return res.status(500).json({
            success: false,
            message: "Internal Server Error during authentication."
        });
    }
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