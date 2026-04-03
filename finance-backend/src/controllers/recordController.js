const Record = require("../models/Record");

// Success/Error JSON Format Helper
const respond = (res, status, success, message, data = null) => {
    return res.status(status).json({ success, message, data });
};

// Create Record
exports.addRecord = async (req, res) => {
    try {
        const { amount, type, category, date, notes, targetUserId } = req.body;

        if (!amount || !type || !category || !date) {
            return respond(res, 400, false, "Missing required fields: amount, type, category, date.");
        }

        const ownerId = (targetUserId && (req.user.role.toLowerCase() === 'admin' || req.user.role.toLowerCase() === 'analyst')) ? parseInt(targetUserId) : req.user.id;

        const lastRecord = await Record.findOne().sort("-id");
        const nextId = lastRecord ? lastRecord.id + 1 : 1;

        const record = new Record({
            id: nextId,
            amount,
            type,
            category,
            date,
            notes,
            createdBy: ownerId
        });

        await record.save();
        respond(res, 201, true, "Record added successfully.", record);
    } catch (err) {
        respond(res, 500, false, "Server Error: " + err.message);
    }
};

// Get Records with Filters
exports.getRecords = async (req, res) => {
    try {
        let { category, type, startDate, endDate, search, targetUserId } = req.query;

        const ownerId = (targetUserId && (req.user.role.toLowerCase() === 'admin' || req.user.role.toLowerCase() === 'analyst')) ? parseInt(targetUserId) : req.user.id;

        let filter = { isDeleted: false, createdBy: ownerId };

        if (category) filter.category = category;
        if (type) filter.type = type;
        if (startDate || endDate) {
            filter.date = {};
            if (startDate) filter.date.$gte = startDate;
            if (endDate) filter.date.$lte = endDate;
        }
        if (search) {
            filter.$or = [
                { notes: new RegExp(search, 'i') },
                { category: new RegExp(search, 'i') }
            ];
        }

        const records = await Record.find(filter).sort("-date");
        respond(res, 200, true, "Records fetched successfully.", records);
    } catch (err) {
        respond(res, 500, false, "Server Error: " + err.message);
    }
};

// Update Record
exports.updateRecord = async (req, res) => {
    try {
        const { id } = req.params;
        const { amount, type, category, date, notes } = req.body;

        const updatedRecord = await Record.findOneAndUpdate(
            { id: parseInt(id), isDeleted: false },
            { $set: { amount, type, category, date, notes } },
            { new: true }
        );

        if (!updatedRecord) {
            return respond(res, 404, false, "Record not found.");
        }

        respond(res, 200, true, "Record updated successfully.", updatedRecord);
    } catch (err) {
        respond(res, 500, false, "Server Error: " + err.message);
    }
};

// Delete Record
exports.deleteRecord = async (req, res) => {
    try {
        const { id } = req.params;
        const result = await Record.findOneAndUpdate({ id: parseInt(id) }, { isDeleted: true });

        if (!result) {
            return respond(res, 404, false, "Record not found.");
        }

        respond(res, 200, true, "Record deleted successfully.");
    } catch (err) {
        respond(res, 500, false, "Server Error: " + err.message);
    }
};

// Dashboard Summaries
exports.getSummary = async (req, res) => {
    try {
        const { targetUserId, type } = req.query;
        const isAdminGlobal = targetUserId === 'all' && req.user.role.toLowerCase() === 'admin';
        const ownerId = (targetUserId && !isAdminGlobal && (req.user.role.toLowerCase() === 'admin' || req.user.role.toLowerCase() === 'analyst')) ? parseInt(targetUserId) : req.user.id;

        let matchFilter = { isDeleted: false };
        if (!isAdminGlobal) {
            matchFilter.createdBy = ownerId;
        }

        let normalizedType = type ? type.trim().toLowerCase() : null;
        if (normalizedType && (normalizedType === "income" || normalizedType === "expense")) {
            matchFilter.type = normalizedType;
        }

        const stats = await Record.aggregate([
            { $match: matchFilter },
            {
                $group: {
                    _id: null,
                    totalIncome: { $sum: { $cond: [{ $eq: ["$type", "income"] }, "$amount", 0] } },
                    totalExpense: { $sum: { $cond: [{ $eq: ["$type", "expense"] }, "$amount", 0] } }
                }
            }
        ]);

        const categoryWise = await Record.aggregate([
            { $match: matchFilter },
            { $group: { _id: "$category", total: { $sum: "$amount" } } },
            { $project: { category: "$_id", total: 1, _id: 0 } }
        ]);

        const totalVisible = categoryWise.reduce((sum, cat) => sum + cat.total, 0);
        const categoryWithPerc = categoryWise.map(cat => ({
            ...cat,
            percentage: totalVisible > 0 ? (cat.total / totalVisible) * 100 : 0
        }));
 
        const recentActivity = await Record.find(matchFilter).sort("-date").limit(5);

        const monthlyTrends = await Record.aggregate([
            { $match: matchFilter },
            { 
                $group: { 
                    _id: { month: { $substr: ["$date", 0, 7] }, type: "$type" }, 
                    total: { $sum: "$amount" } 
                } 
            },
            { 
                $project: { 
                    _id: 0, 
                    month: "$_id.month", 
                    type: "$_id.type", 
                    total: 1 
                } 
            },
            { $sort: { month: -1 } },
            { $limit: 12 }
        ]);

        const weeklyTrends = await Record.aggregate([
            { $match: { ...matchFilter, date: { $exists: true, $ne: "" } } },
            { 
                $group: { 
                    _id: { week: { $substr: ["$date", 0, 10] }, type: "$type" }, 
                    total: { $sum: "$amount" } 
                } 
            },
            { 
                $project: { 
                    _id: 0, 
                    week: "$_id.week", 
                    type: "$_id.type", 
                    total: 1 
                } 
            },
            { $sort: { week: -1 } },
            { $limit: 12 }
        ]);
 
        console.log("[DIAGNOSTIC] Weekly Trends Sending:", JSON.stringify(weeklyTrends));

        const totalIncome = stats.length > 0 ? stats[0].totalIncome : 0;
        const totalExpense = stats.length > 0 ? stats[0].totalExpense : 0;
 
        respond(res, 200, true, "Summary fetched successfully.", {
            totalIncome,
            totalExpense,
            netBalance: totalIncome - totalExpense,
            categoryWise: categoryWithPerc || [],
            recentActivity: recentActivity || [],
            monthlyTrends: monthlyTrends || [],
            weeklyTrends: weeklyTrends || []
        });
    } catch (error) {
        respond(res, 500, false, "Server Error: " + error.message);
    }
};