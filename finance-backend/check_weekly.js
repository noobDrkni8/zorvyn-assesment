const mongoose = require('mongoose');
const Record = require('./src/models/Record');
const User = require('./src/models/User');

async function check() {
    await mongoose.connect('mongodb://localhost:27017/finance_db');
    const user = await User.findOne({ email: 'viewer@zorvyn.com' });
    if (!user) {
        console.log('User not found');
        process.exit(1);
    }
    const ownerId = user._id;
    console.log('User ID:', ownerId);

    const matchFilter = { isDeleted: false, createdBy: ownerId };
    const weeklyTrends = await Record.aggregate([
        { $match: { ...matchFilter, date: { $exists: true, $ne: '' } } },
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
    console.log('Weekly Trends:', JSON.stringify(weeklyTrends, null, 2));

    const allRecords = await Record.find(matchFilter).sort('-date').limit(5);
    console.log('Recent Records:', JSON.stringify(allRecords, null, 2));

    process.exit(0);
}

check();
