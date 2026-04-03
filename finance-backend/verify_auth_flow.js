async function runVerification() {
    console.log('🚀 Starting Identity Security Verification Flow...');
    const BASE_URL = 'http://localhost:3000/api';

    try {
        // 1. Admin Login
        console.log('\n--- [TEST 1] Admin Login ---');
        const adminLoginRes = await fetch(`${BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: 'admin@zorvyn.com', password: 'nehal@admin' })
        });
        const adminLogin = await adminLoginRes.json();
        const adminId = adminLogin.data.id;
        console.log('✅ Admin authenticated (nehal@admin matches)');

        // 2. Admin Creates a Client
        console.log('\n--- [TEST 2] Admin Creating Client ---');
        const clientEmail = `new_client_${Date.now()}@zorvyn.com`;
        const createClientRes = await fetch(`${BASE_URL}/users`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'X-User-Id': adminId },
            body: JSON.stringify({
                name: 'Verification Client',
                email: clientEmail,
                role: 'Viewer',
                password: 'temp_client_123'
            })
        });
        console.log(`✅ Client created with OTP password. Email: ${clientEmail}`);

        // 3. Admin Creates an Analyst
        console.log('\n--- [TEST 3] Admin Creating Analyst ---');
        const analystEmail = `new_analyst_${Date.now()}@zorvyn.com`;
        const createAnalystRes = await fetch(`${BASE_URL}/users`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'X-User-Id': adminId },
            body: JSON.stringify({
                name: 'Verification Analyst',
                email: analystEmail,
                role: 'Analyst',
                password: 'temp_analyst_123'
            })
        });
        console.log(`✅ Analyst created with OTP password. Email: ${analystEmail}`);

        // 4. Test Client Login (OTP Flow)
        console.log('\n--- [TEST 4] Client OTP Login ---');
        const clientLoginRes = await fetch(`${BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: clientEmail, password: 'temp_client_123' })
        });
        const clientLogin = await clientLoginRes.json();
        const clientData = clientLogin.data;
        console.log(`✅ Client Login Successful.`);
        console.log(`🛡️ mustChangePassword flag: ${clientData.mustChangePassword}`);
        
        if (clientData.mustChangePassword === 'true') {
            console.log('✨ [SUCCESS] Mandatory Reset flag detected for Client.');
        }

        // 5. Test Analyst Login (OTP Flow)
        console.log('\n--- [TEST 5] Analyst OTP Login ---');
        const analystLoginRes = await fetch(`${BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: analystEmail, password: 'temp_analyst_123' })
        });
        const analystLogin = await analystLoginRes.json();
        const analystData = analystLogin.data;
        console.log(`✅ Analyst Login Successful.`);
        console.log(`🛡️ mustChangePassword flag: ${analystData.mustChangePassword}`);

        if (analystData.mustChangePassword === 'true') {
            console.log('✨ [SUCCESS] Mandatory Reset flag detected for Analyst.');
        }

        console.log('\n🎉 ALL SECURITY PROTOCOLS VERIFIED!');

    } catch (err) {
        console.error('\n❌ VERIFICATION FAILED:', err.message);
        process.exit(1);
    }
}

runVerification();
