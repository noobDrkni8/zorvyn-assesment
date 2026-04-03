async function verifyAnalystCapabilities() {
    console.log('🧪 Verifying Analyst Performance & Business Intelligence CRUD...');
    const BASE_URL = 'http://localhost:3000/api';

    try {
        // 1. Admin Login
        const adminLoginRes = await fetch(`${BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: 'admin@zorvyn.com', password: 'nehal@admin' })
        });
        const adminResp = await adminLoginRes.json();
        if (!adminResp.success) throw new Error("Admin login failed: " + adminResp.message);
        const adminId = adminResp.data.id;
        console.log('✅ Admin Session Active.');

        // 2. Analyst Login
        const analystLoginRes = await fetch(`${BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: 'analyst@zorvyn.com', password: 'password123' })
        });
        const analystResp = await analystLoginRes.json();
        if (!analystResp.success) throw new Error("Analyst login failed: " + analystResp.message);
        const analystId = analystResp.data.id;
        console.log('✅ Analyst Session Active.');

        // 3. Get a Client (Viewer)
        const usersRes = await fetch(`${BASE_URL}/users`, {
            headers: { 'X-User-Id': adminId }
        });
        const users = (await usersRes.json()).data;
        const client = users.find(u => u.role === 'Viewer');
        if (!client) throw new Error("No Viewer/Client found in DB.");
        const clientId = client.id;
        console.log(`✅ Targeted Client: ${client.email} (ID: ${clientId})`);

        // 4. Analyst Create Record for Client
        console.log('\n--- [TEST] Analyst Performance: Log Transaction ---');
        const addRecordRes = await fetch(`${BASE_URL}/records`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'X-User-Id': analystId },
            body: JSON.stringify({
                amount: 7500,
                type: 'income',
                category: 'Investment',
                date: '2026-04-02',
                notes: 'Verification Audit Entry',
                targetUserId: clientId
            })
        });
        const addRecordResp = await addRecordRes.json();
        if (!addRecordResp.success) throw new Error("Analyst could not add record: " + addRecordResp.message);
        const record = addRecordResp.data;
        console.log(`✅ Analyst logged record ${record.id} for Client ${clientId}.`);

        // 5. Analyst View Summary for Client
        console.log('\n--- [TEST] Analyst Intelligence: Get Summary ---');
        const summaryRes = await fetch(`${BASE_URL}/records/summary?targetUserId=${clientId}`, {
            headers: { 'X-User-Id': analystId }
        });
        const summaryResp = await summaryRes.json();
        if (!summaryResp.success) throw new Error("Analyst could not fetch summary: " + summaryResp.message);
        const summary = summaryResp.data;
        console.log(`📊 Summary for Client ${clientId}:`);
        console.log(`   - Total Income: $${summary.totalIncome}`);
        console.log(`   - Net Balance: $${summary.netBalance}`);
        console.log(`   - Category Count: ${summary.categoryWise.length}`);
        console.log(`   - Activity Count: ${summary.recentActivity.length}`);
        
        if (summary.totalIncome >= 7500) {
            console.log('\n✨ [VERIFIED] Analyst can perform financial CRUD and access dashboard intelligence for Clients.');
        } else {
            throw new Error('Summary data mismatch!');
        }

        console.log('\n🎉 ALL ANALYST PROTOCOLS VERIFIED!');

    } catch (err) {
        console.error('\n❌ VERIFICATION FAILED:', err.message);
        process.exit(1);
    }
}

verifyAnalystCapabilities();
